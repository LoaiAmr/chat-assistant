package com.loai.spring.ai.chat_assistant.application.exception;

import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when content moderation fails.
 */
@Getter
public class ModerationFailedException extends RuntimeException {

    private final List<String> flaggedCategories;

    public ModerationFailedException(String message, List<String> flaggedCategories) {
        super(message);
        this.flaggedCategories = flaggedCategories;
    }

    public ModerationFailedException(List<String> flaggedCategories) {
        super("Content failed moderation checks. Flagged categories: " + String.join(", ", flaggedCategories));
        this.flaggedCategories = flaggedCategories;
    }

    public ModerationFailedException(String message, Throwable cause) {
        super(message, cause);
        this.flaggedCategories = List.of();
    }
}
