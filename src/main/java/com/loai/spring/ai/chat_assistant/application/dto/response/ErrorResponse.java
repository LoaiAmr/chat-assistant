package com.loai.spring.ai.chat_assistant.application.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response DTO.
 * Used by the global exception handler to provide consistent error responses.
 */
@Value
@Builder
@Jacksonized
public class ErrorResponse {

    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
    String correlationId;
    Map<String, Object> details;
    Integer retryAfter;
}
