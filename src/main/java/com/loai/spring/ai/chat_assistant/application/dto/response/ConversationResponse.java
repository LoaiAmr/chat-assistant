package com.loai.spring.ai.chat_assistant.application.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a conversation.
 * Can be used for both list view (without messages) and detail view (with messages).
 */
@Value
@Builder
@Jacksonized
public class ConversationResponse {

    UUID id;
    String title;
    List<MessageResponse> messages;
    int messageCount;
    long totalTokens;
    Instant createdAt;
    Instant updatedAt;
}
