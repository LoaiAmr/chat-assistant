package com.loai.spring.ai.chat_assistant.application.service;

import com.loai.spring.ai.chat_assistant.application.dto.response.ConversationResponse;
import com.loai.spring.ai.chat_assistant.application.dto.response.MessageResponse;
import com.loai.spring.ai.chat_assistant.application.port.input.ConversationService;
import com.loai.spring.ai.chat_assistant.domain.model.Conversation;
import com.loai.spring.ai.chat_assistant.domain.model.Message;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.domain.repository.ConversationRepository;
import com.loai.spring.ai.chat_assistant.domain.repository.MessageRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ConversationService for managing conversations.
 * Handles listing, retrieving, and deleting conversations for the current tenant.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> listConversations(Pageable pageable) {
        TenantId tenantId = TenantContextHolder.getTenantContext().getTenantId();

        log.debug("Listing conversations for tenant: {}", tenantId.getValue());

        Page<Conversation> conversations = conversationRepository.findByTenantId(tenantId, pageable);

        return conversations.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(ConversationId conversationId) {
        TenantId tenantId = TenantContextHolder.getTenantContext().getTenantId();

        log.debug("Getting conversation {} for tenant: {}", conversationId.getValue(), tenantId.getValue());

        // Verify conversation exists and belongs to tenant
        if (!conversationRepository.existsByIdAndTenantId(conversationId, tenantId)) {
            throw new IllegalArgumentException("Conversation not found or access denied: " + conversationId.getValue());
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId.getValue()));

        // Load messages for this conversation
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        // Build conversation with messages
        Conversation conversationWithMessages = conversation.withMessages(messages);

        return mapToResponseWithMessages(conversationWithMessages);
    }

    @Override
    @Transactional
    public void deleteConversation(ConversationId conversationId) {
        TenantId tenantId = TenantContextHolder.getTenantContext().getTenantId();

        log.info("Deleting conversation {} for tenant: {}", conversationId.getValue(), tenantId.getValue());

        // Verify conversation exists and belongs to tenant
        if (!conversationRepository.existsByIdAndTenantId(conversationId, tenantId)) {
            throw new IllegalArgumentException("Conversation not found or access denied: " + conversationId.getValue());
        }

        // Delete all messages first (cascade delete)
        messageRepository.deleteByConversationId(conversationId);

        // Delete the conversation
        conversationRepository.deleteById(conversationId);

        log.info("Successfully deleted conversation {} and its messages", conversationId.getValue());
    }

    /**
     * Maps a Conversation domain model to ConversationResponse DTO without messages.
     * Used for list view.
     */
    private ConversationResponse mapToResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId().getValue())
                .title(conversation.getTitle())
                .messages(null) // No messages in list view
                .messageCount(conversation.getMessageCount())
                .totalTokens(conversation.getTotalTokens().getValue())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    /**
     * Maps a Conversation domain model to ConversationResponse DTO with messages.
     * Used for detail view.
     */
    private ConversationResponse mapToResponseWithMessages(Conversation conversation) {
        List<MessageResponse> messageResponses = conversation.getMessages().stream()
                .map(this::mapMessageToResponse)
                .collect(Collectors.toList());

        return ConversationResponse.builder()
                .id(conversation.getId().getValue())
                .title(conversation.getTitle())
                .messages(messageResponses)
                .messageCount(conversation.getMessageCount())
                .totalTokens(conversation.getTotalTokens().getValue())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    /**
     * Maps a Message domain model to MessageResponse DTO.
     */
    private MessageResponse mapMessageToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId().getValue())
                .role(message.getRole())
                .content(message.getContent().toString())
                .promptTokens(message.getPromptTokens() != null ? message.getPromptTokens().getValue() : null)
                .completionTokens(message.getCompletionTokens() != null ? message.getCompletionTokens().getValue() : null)
                .createdAt(message.getCreatedAt())
                .build();
    }
}
