package com.loai.spring.ai.chat_assistant.application.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

/**
 * Response DTO for a chat message.
 * Contains the AI-generated response and metadata.
 */
@Value
@Builder
@Jacksonized
public class ChatResponse {

    /**
     * The conversation ID (existing or newly created).
     */
    UUID conversationId;

    /**
     * The AI-generated message.
     */
    MessageResponse message;

    /**
     * Token usage for this request.
     */
    TokenUsageInfo tokensUsed;

    /**
     * Whether the message passed moderation.
     */
    boolean moderationPassed;

    /**
     * Nested DTO for token usage information.
     */
    @Value
    @Builder
    @Jacksonized
    public static class TokenUsageInfo {
        int prompt;
        int completion;
        int total;
    }
}
