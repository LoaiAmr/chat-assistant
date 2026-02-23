package com.loai.spring.ai.chat_assistant.domain.model.vo;

import lombok.Value;

import java.util.Objects;

/**
 * Value object representing message content.
 * Ensures content is not null or empty.
 */
@Value
public class MessageContent {
    String value;

    private MessageContent(String value) {
        Objects.requireNonNull(value, "Message content cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        this.value = value;
    }

    public static MessageContent of(String value) {
        return new MessageContent(value);
    }

    public int length() {
        return value.length();
    }

    @Override
    public String toString() {
        return value;
    }
}
