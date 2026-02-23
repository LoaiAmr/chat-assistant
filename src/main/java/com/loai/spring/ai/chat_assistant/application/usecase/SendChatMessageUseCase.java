package com.loai.spring.ai.chat_assistant.application.usecase;

import com.loai.spring.ai.chat_assistant.application.dto.request.ChatRequest;
import com.loai.spring.ai.chat_assistant.application.dto.response.ChatResponse;
import com.loai.spring.ai.chat_assistant.application.dto.response.MessageResponse;
import com.loai.spring.ai.chat_assistant.application.exception.AIProviderException;
import com.loai.spring.ai.chat_assistant.application.exception.ModerationFailedException;
import com.loai.spring.ai.chat_assistant.application.exception.RateLimitExceededException;
import com.loai.spring.ai.chat_assistant.application.port.input.ChatService;
import com.loai.spring.ai.chat_assistant.application.port.input.TokenUsageService;
import com.loai.spring.ai.chat_assistant.application.port.output.*;
import com.loai.spring.ai.chat_assistant.domain.exception.TenantInactiveException;
import com.loai.spring.ai.chat_assistant.domain.exception.TenantNotFoundException;
import com.loai.spring.ai.chat_assistant.domain.model.Conversation;
import com.loai.spring.ai.chat_assistant.domain.model.Message;
import com.loai.spring.ai.chat_assistant.domain.model.Tenant;
import com.loai.spring.ai.chat_assistant.domain.model.vo.*;
import com.loai.spring.ai.chat_assistant.domain.repository.ConversationRepository;
import com.loai.spring.ai.chat_assistant.domain.repository.MessageRepository;
import com.loai.spring.ai.chat_assistant.domain.repository.TenantRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use case for sending a chat message and receiving an AI response.
 * This orchestrates the entire flow following SOLID principles.
 * <p>
 * Flow:
 * 1. Validate tenant
 * 2. Check rate limits
 * 3. Moderate input
 * 4. Load/create conversation
 * 5. Check token budget
 * 6. Call AI provider (with circuit breaker)
 * 7. Moderate output
 * 8. Save messages
 * 9. Update token usage
 * 10. Audit
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SendChatMessageUseCase implements ChatService {

    private final AIProvider aiProvider;
    private final AuditService auditService;
    private final CacheService cacheService;
    private final RateLimitService rateLimitService;
    private final TenantRepository tenantRepository;
    private final ModerationService moderationService;
    private final TokenUsageService tokenUsageService;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Override
    @Transactional
    public ChatResponse sendMessage(ChatRequest request) {
        log.debug("Processing chat request: conversationId={}", request.getConversationId());

        // Step 1: Get and validate tenant
        TenantId tenantId = TenantContextHolder.getTenantContext().getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        if (!tenant.canMakeRequests()) {
            throw new TenantInactiveException(tenantId);
        }

        // Step 2: Check rate limits
        if (!rateLimitService.allowRequest(tenantId)) {
            auditService.logRateLimitExceeded(tenantId);
            throw new RateLimitExceededException(tenantId, 60);
        }
        rateLimitService.recordRequest(tenantId);

        // Step 3: Moderate input
        ModerationResult inputModeration = moderationService.moderateContent(request.getMessage());
        if (inputModeration.isFailed()) {
            log.warn("Input moderation failed for tenant {}: {}", tenantId, inputModeration.getFlaggedCategories());
            throw new ModerationFailedException(inputModeration.getFlaggedCategories());
        }

        // Step 4: Load or create conversation
        Conversation conversation;
        if (request.getConversationId() != null) {
            ConversationId conversationId = ConversationId.of(request.getConversationId());
            conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));
        } else {
            conversation = createNewConversation(tenantId);
            conversation = conversationRepository.save(conversation);
        }

        // Step 5: Build message history for AI context
        List<Message> conversationHistory = new ArrayList<>(conversation.getMessages());

        // Add system prompt if provided
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            Message systemMessage = Message.builder()
                    .id(MessageId.generate())
                    .conversationId(conversation.getId())
                    .role(MessageRole.SYSTEM)
                    .content(MessageContent.of(request.getSystemPrompt()))
                    .promptTokens(TokenCount.zero())
                    .completionTokens(TokenCount.zero())
                    .moderationResult(ModerationResult.approved())
                    .createdAt(Instant.now())
                    .build();
            conversationHistory.add(0, systemMessage); // Add at beginning
        }

        // Add user message
        Message userMessage = Message.builder()
                .id(MessageId.generate())
                .conversationId(conversation.getId())
                .role(MessageRole.USER)
                .content(MessageContent.of(request.getMessage()))
                .promptTokens(TokenCount.zero())
                .completionTokens(TokenCount.zero())
                .moderationResult(inputModeration)
                .createdAt(Instant.now())
                .build();
        conversationHistory.add(userMessage);

        // Step 6: Estimate tokens and check budget
        TokenCount estimatedTokens = aiProvider.estimateTokens(request.getMessage());
        tokenUsageService.checkBudget(tenantId, estimatedTokens);

        // Step 7: Check cache (optional optimization)
        String cacheKey = cacheService.generateCacheKey(
                request.getMessage(),
                "gpt-4o-mini",
                request.getTemperature() != null ? request.getTemperature() : 0.7
        );

        AIProvider.AIResponse aiResponse;
        try {
            // Try to get from the cache first
            aiResponse = cacheService.getCachedResponse(cacheKey)
                    .map(cachedContent -> new AIProvider.AIResponse(
                            cachedContent,
                            estimatedTokens,
                            aiProvider.estimateTokens(cachedContent),
                            "gpt-4o-mini"
                    ))
                    .orElseGet(() -> {
                        // Call AI provider
                        AIProvider.AIResponse response = aiProvider.generateResponse(
                                conversationHistory,
                                request.getTemperature(),
                                request.getMaxTokens()
                        );
                        // Cache the response
                        cacheService.cacheResponse(cacheKey, response.content());
                        return response;
                    });
        } catch (Exception e) {
            log.error("AI provider error for tenant {}: {}", tenantId, e.getMessage(), e);
            auditService.logError(tenantId, "AI provider error", e);
            throw new AIProviderException("Failed to generate AI response", aiProvider.getProviderName(), e);
        }

        // Step 8: Moderate AI output
        ModerationResult outputModeration = moderationService.moderateContent(aiResponse.content());
        if (outputModeration.isFailed()) {
            log.warn("Output moderation failed for tenant {}: {}", tenantId, outputModeration.getFlaggedCategories());
            auditService.logModerationFlagged(
                    tenantId,
                    conversation.getId(),
                    outputModeration.getFlaggedCategories()
            );
            throw new ModerationFailedException("AI response failed moderation", outputModeration.getFlaggedCategories());
        }

        // Step 9: Save user message
        userMessage = userMessage
                .withPromptTokens(aiResponse.promptTokens())
                .withCompletionTokens(TokenCount.zero());
        messageRepository.save(userMessage);

        // Step 10: Create and save assistant message
        Message assistantMessage = Message.builder()
                .id(MessageId.generate())
                .conversationId(conversation.getId())
                .role(MessageRole.ASSISTANT)
                .content(MessageContent.of(aiResponse.content()))
                .promptTokens(TokenCount.zero())
                .completionTokens(aiResponse.completionTokens())
                .moderationResult(outputModeration)
                .createdAt(Instant.now())
                .build();
        messageRepository.save(assistantMessage);

        // Step 11: Record token usage
        tokenUsageService.recordUsage(tenantId, aiResponse.promptTokens(), aiResponse.completionTokens());

        // Step 12: Audit
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("model", aiResponse.model());
        auditData.put("promptTokens", aiResponse.promptTokens().getValue());
        auditData.put("completionTokens", aiResponse.completionTokens().getValue());
        auditService.logChatRequest(tenantId, conversation.getId(), auditData);

        log.info("Chat request completed for tenant {}: conversationId={}, tokens={}",
                tenantId, conversation.getId(), aiResponse.promptTokens().add(aiResponse.completionTokens()));

        // Step 13: Build response
        return ChatResponse.builder()
                .conversationId(conversation.getId().getValue())
                .message(MessageResponse.builder()
                        .id(assistantMessage.getId().getValue())
                        .role(assistantMessage.getRole())
                        .content(assistantMessage.getContent().toString())
                        .promptTokens(null)
                        .completionTokens(assistantMessage.getCompletionTokens().getValue())
                        .createdAt(assistantMessage.getCreatedAt())
                        .build())
                .tokensUsed(ChatResponse.TokenUsageInfo.builder()
                        .prompt(aiResponse.promptTokens().getValue())
                        .completion(aiResponse.completionTokens().getValue())
                        .total(aiResponse.promptTokens().add(aiResponse.completionTokens()).getValue())
                        .build())
                .moderationPassed(true)
                .build();
    }

    private Conversation createNewConversation(TenantId tenantId) {
        return Conversation.builder()
                .id(ConversationId.generate())
                .tenantId(tenantId)
                .title("New Conversation")
                .messages(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
