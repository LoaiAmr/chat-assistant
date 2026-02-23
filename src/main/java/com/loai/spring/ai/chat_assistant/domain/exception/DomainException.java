package com.loai.spring.ai.chat_assistant.domain.exception;

/**
 * Base exception for all domain-level exceptions.
 * Domain exceptions represent violations of business rules.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
