package com.loai.spring.ai.chat_assistant.domain.model.vo;

import lombok.Value;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique message identifier.
 * Immutable and type-safe.
 */
@Value
public class MessageId {
    UUID value;

    private MessageId(UUID value) {
        this.value = Objects.requireNonNull(value, "Message ID cannot be null");
    }

    public static MessageId of(UUID value) {
        return new MessageId(value);
    }

    public static MessageId of(String value) {
        return new MessageId(UUID.fromString(value));
    }

    public static MessageId generate() {
        return new MessageId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
