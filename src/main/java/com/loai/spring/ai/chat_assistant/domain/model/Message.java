package com.loai.spring.ai.chat_assistant.domain.model;

import com.loai.spring.ai.chat_assistant.domain.model.vo.*;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Instant;

/**
 * Domain entity representing a single message in a conversation.
 * Immutable value object.
 */
@Value
@Builder
@With
public class Message {
    MessageId id;
    ConversationId conversationId;
    MessageRole role;
    MessageContent content;
    TokenCount promptTokens;
    TokenCount completionTokens;
    ModerationResult moderationResult;
    Instant createdAt;

    /**
     * Calculates the total tokens used by this message.
     */
    public TokenCount getTotalTokens() {
        TokenCount prompt = promptTokens != null ? promptTokens : TokenCount.zero();
        TokenCount completion = completionTokens != null ? completionTokens : TokenCount.zero();
        return prompt.add(completion);
    }

    /**
     * Checks if this message passed moderation.
     */
    public boolean isModerated() {
        return moderationResult != null && moderationResult.isPassed();
    }

    /**
     * Checks if this is a user message.
     */
    public boolean isUserMessage() {
        return role == MessageRole.USER;
    }

    /**
     * Checks if this is an assistant message.
     */
    public boolean isAssistantMessage() {
        return role == MessageRole.ASSISTANT;
    }

    /**
     * Checks if this is a system message.
     */
    public boolean isSystemMessage() {
        return role == MessageRole.SYSTEM;
    }
}
