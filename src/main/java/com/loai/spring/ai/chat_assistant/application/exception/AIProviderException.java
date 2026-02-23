package com.loai.spring.ai.chat_assistant.application.exception;

import lombok.Getter;

/**
 * Exception thrown when the AI provider encounters an error.
 */
@Getter
public class AIProviderException extends RuntimeException {

    private final String providerName;

    public AIProviderException(String message, String providerName) {
        super(message);
        this.providerName = providerName;
    }

    public AIProviderException(String message, String providerName, Throwable cause) {
        super(message, cause);
        this.providerName = providerName;
    }
}
