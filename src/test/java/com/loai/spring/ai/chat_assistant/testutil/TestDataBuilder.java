package com.loai.spring.ai.chat_assistant.testutil;

import com.loai.spring.ai.chat_assistant.domain.model.Conversation;
import com.loai.spring.ai.chat_assistant.domain.model.Message;
import com.loai.spring.ai.chat_assistant.domain.model.Tenant;
import com.loai.spring.ai.chat_assistant.domain.model.vo.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class providing test data builders for domain objects.
 * Simplifies test data creation with sensible defaults and fluent API.
 */
public class TestDataBuilder {

    // ========== Tenant Builders ==========

    /**
     * Creates a valid tenant builder with sensible defaults.
     * Active tenant with daily limit of 10,000 tokens and monthly limit of 300,000 tokens.
     */
    public static Tenant.TenantBuilder aValidTenant() {
        return Tenant.builder()
                .id(TenantId.generate())
                .name("Test Tenant")
                .apiKey("test-api-key-" + UUID.randomUUID())
                .active(true)
                .dailyTokenLimit(10000)
                .monthlyTokenLimit(300000)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    /**
     * Creates an inactive tenant builder.
     */
    public static Tenant.TenantBuilder anInactiveTenant() {
        return aValidTenant()
                .active(false);
    }

    /**
     * Creates a tenant builder without token limits (unlimited).
     */
    public static Tenant.TenantBuilder aTenantWithoutLimits() {
        return aValidTenant()
                .dailyTokenLimit(null)
                .monthlyTokenLimit(null);
    }

    /**
     * Creates a tenant builder with custom daily limit.
     */
    public static Tenant.TenantBuilder aTenantWithDailyLimit(int dailyLimit) {
        return aValidTenant()
                .dailyTokenLimit(dailyLimit);
    }

    /**
     * Creates a tenant builder with custom monthly limit.
     */
    public static Tenant.TenantBuilder aTenantWithMonthlyLimit(int monthlyLimit) {
        return aValidTenant()
                .monthlyTokenLimit(monthlyLimit);
    }

    // ========== Message Builders ==========

    /**
     * Creates a user message builder with sensible defaults.
     */
    public static Message.MessageBuilder aUserMessage() {
        return Message.builder()
                .id(MessageId.generate())
                .conversationId(ConversationId.generate())
                .role(MessageRole.USER)
                .content(MessageContent.of("Test user message"))
                .promptTokens(TokenCount.of(50))
                .completionTokens(TokenCount.zero())
                .moderationResult(ModerationResult.approved())
                .createdAt(Instant.now());
    }

    /**
     * Creates an assistant message builder with sensible defaults.
     */
    public static Message.MessageBuilder anAssistantMessage() {
        return Message.builder()
                .id(MessageId.generate())
                .conversationId(ConversationId.generate())
                .role(MessageRole.ASSISTANT)
                .content(MessageContent.of("Test assistant response"))
                .promptTokens(TokenCount.zero())
                .completionTokens(TokenCount.of(100))
                .moderationResult(ModerationResult.approved())
                .createdAt(Instant.now());
    }

    /**
     * Creates a system message builder with sensible defaults.
     */
    public static Message.MessageBuilder aSystemMessage() {
        return Message.builder()
                .id(MessageId.generate())
                .conversationId(ConversationId.generate())
                .role(MessageRole.SYSTEM)
                .content(MessageContent.of("You are a helpful assistant"))
                .promptTokens(TokenCount.of(30))
                .completionTokens(TokenCount.zero())
                .moderationResult(ModerationResult.approved())
                .createdAt(Instant.now());
    }

    /**
     * Creates a user message with specified content.
     */
    public static Message.MessageBuilder aUserMessageWithContent(String content) {
        return aUserMessage()
                .content(MessageContent.of(content));
    }

    /**
     * Creates a message with specified token counts.
     */
    public static Message.MessageBuilder aMessageWithTokens(int promptTokens, int completionTokens) {
        return aUserMessage()
                .promptTokens(TokenCount.of(promptTokens))
                .completionTokens(TokenCount.of(completionTokens));
    }

    /**
     * Creates a message that failed moderation.
     */
    public static Message.MessageBuilder aRejectedMessage() {
        return aUserMessage()
                .moderationResult(ModerationResult.rejected(List.of("hate", "violence")));
    }

    /**
     * Creates a message with pending moderation.
     */
    public static Message.MessageBuilder aPendingModerationMessage() {
        return aUserMessage()
                .moderationResult(ModerationResult.pending());
    }

    // ========== Conversation Builders ==========

    /**
     * Creates an empty conversation builder with sensible defaults.
     */
    public static Conversation.ConversationBuilder aValidConversation() {
        return Conversation.builder()
                .id(ConversationId.generate())
                .tenantId(TenantId.generate())
                .title("Test Conversation")
                .messages(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    /**
     * Creates a conversation builder with specified number of user messages.
     */
    public static Conversation.ConversationBuilder aConversationWithMessages(int messageCount) {
        ConversationId conversationId = ConversationId.generate();
        List<Message> messages = IntStream.range(0, messageCount)
                .mapToObj(i -> aUserMessage()
                        .conversationId(conversationId)
                        .content(MessageContent.of("Message " + (i + 1)))
                        .build())
                .collect(Collectors.toList());

        return aValidConversation()
                .id(conversationId)
                .messages(messages);
    }

    /**
     * Creates a conversation with alternating user and assistant messages.
     */
    public static Conversation.ConversationBuilder aConversationWithDialogue(int exchanges) {
        ConversationId conversationId = ConversationId.generate();
        List<Message> messages = new ArrayList<>();

        for (int i = 0; i < exchanges; i++) {
            messages.add(aUserMessage()
                    .conversationId(conversationId)
                    .content(MessageContent.of("User question " + (i + 1)))
                    .build());
            messages.add(anAssistantMessage()
                    .conversationId(conversationId)
                    .content(MessageContent.of("Assistant answer " + (i + 1)))
                    .build());
        }

        return aValidConversation()
                .id(conversationId)
                .messages(messages);
    }

    /**
     * Creates a conversation with messages totaling specified token count.
     */
    public static Conversation.ConversationBuilder aConversationWithTotalTokens(int totalTokens) {
        ConversationId conversationId = ConversationId.generate();
        int tokensPerMessage = totalTokens / 2; // Split between user and assistant

        List<Message> messages = List.of(
                aUserMessage()
                        .conversationId(conversationId)
                        .promptTokens(TokenCount.of(tokensPerMessage))
                        .completionTokens(TokenCount.zero())
                        .build(),
                anAssistantMessage()
                        .conversationId(conversationId)
                        .promptTokens(TokenCount.zero())
                        .completionTokens(TokenCount.of(tokensPerMessage))
                        .build()
        );

        return aValidConversation()
                .id(conversationId)
                .messages(messages);
    }

    /**
     * Creates a conversation belonging to a specific tenant.
     */
    public static Conversation.ConversationBuilder aConversationForTenant(TenantId tenantId) {
        return aValidConversation()
                .tenantId(tenantId);
    }

    // ========== Value Object Helpers ==========

    /**
     * Creates a TokenCount with specified value.
     */
    public static TokenCount tokenCount(int value) {
        return TokenCount.of(value);
    }

    /**
     * Creates a zero token count.
     */
    public static TokenCount zeroTokens() {
        return TokenCount.zero();
    }

    /**
     * Creates MessageContent with specified text.
     */
    public static MessageContent messageContent(String text) {
        return MessageContent.of(text);
    }

    /**
     * Creates an approved moderation result.
     */
    public static ModerationResult approvedModeration() {
        return ModerationResult.approved();
    }

    /**
     * Creates a rejected moderation result with categories.
     */
    public static ModerationResult rejectedModeration(String... categories) {
        return ModerationResult.rejected(List.of(categories));
    }

    /**
     * Creates a flagged moderation result with categories.
     */
    public static ModerationResult flaggedModeration(String... categories) {
        return ModerationResult.flagged(List.of(categories));
    }

    /**
     * Creates a pending moderation result.
     */
    public static ModerationResult pendingModeration() {
        return ModerationResult.pending();
    }

    /**
     * Generates a new TenantId.
     */
    public static TenantId tenantId() {
        return TenantId.generate();
    }

    /**
     * Creates a TenantId from UUID string.
     */
    public static TenantId tenantId(String uuid) {
        return TenantId.of(uuid);
    }

    /**
     * Generates a new ConversationId.
     */
    public static ConversationId conversationId() {
        return ConversationId.generate();
    }

    /**
     * Creates a ConversationId from UUID string.
     */
    public static ConversationId conversationId(String uuid) {
        return ConversationId.of(uuid);
    }

    /**
     * Generates a new MessageId.
     */
    public static MessageId messageId() {
        return MessageId.generate();
    }

    /**
     * Creates a MessageId from UUID string.
     */
    public static MessageId messageId(String uuid) {
        return MessageId.of(uuid);
    }

    // Prevent instantiation
    private TestDataBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }
}
