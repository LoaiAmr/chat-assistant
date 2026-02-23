package com.loai.spring.ai.chat_assistant.domain.exception;

/**
 * Exception thrown when message validation fails.
 */
public class MessageValidationException extends DomainException {

    public MessageValidationException(String message) {
        super(message);
    }

    public MessageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
