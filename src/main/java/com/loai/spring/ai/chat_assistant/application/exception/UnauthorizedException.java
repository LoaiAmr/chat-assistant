package com.loai.spring.ai.chat_assistant.application.exception;

/**
 * Exception thrown when authentication/authorization fails.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
