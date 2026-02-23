package com.loai.spring.ai.chat_assistant.application.dto.response;

import com.loai.spring.ai.chat_assistant.domain.model.vo.MessageRole;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a message.
 */
@Value
@Builder
@Jacksonized
public class MessageResponse {

    UUID id;
    MessageRole role;
    String content;
    Integer promptTokens;
    Integer completionTokens;
    Instant createdAt;
}
