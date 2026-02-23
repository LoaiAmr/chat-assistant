package com.loai.spring.ai.chat_assistant.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Request DTO for sending a chat message.
 * Includes validation constraints to ensure data integrity.
 */
@Value
@Builder
@Jacksonized
public class ChatRequest {

    /**
     * Optional conversation ID. If not provided, a new conversation is created.
     */
    @Pattern(
        regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        message = "Invalid conversation ID format"
    )
    String conversationId;

    /**
     * The user's message content.
     */
    @NotBlank(message = "Message cannot be blank")
    @Size(min = 1, max = 10000, message = "Message length must be between 1 and 10000 characters")
    String message;

    /**
     * Optional system prompt to guide AI behavior.
     */
    @Size(max = 2000, message = "System prompt cannot exceed 2000 characters")
    String systemPrompt;

    /**
     * Temperature parameter for AI creativity (0.0 = deterministic, 2.0 = very creative).
     */
    @DecimalMin(value = "0.0", message = "Temperature must be >= 0.0")
    @DecimalMax(value = "2.0", message = "Temperature must be <= 2.0")
    Double temperature;

    /**
     * Maximum tokens for the AI response.
     */
    @Min(value = 1, message = "Max tokens must be >= 1")
    @Max(value = 4000, message = "Max tokens must be <= 4000")
    Integer maxTokens;
}
