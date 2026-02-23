package com.loai.spring.ai.chat_assistant.domain.model.vo;

import lombok.Value;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique conversation identifier.
 * Immutable and type-safe.
 */
@Value
public class ConversationId {
    UUID value;

    private ConversationId(UUID value) {
        this.value = Objects.requireNonNull(value, "Conversation ID cannot be null");
    }

    public static ConversationId of(UUID value) {
        return new ConversationId(value);
    }

    public static ConversationId of(String value) {
        return new ConversationId(UUID.fromString(value));
    }

    public static ConversationId generate() {
        return new ConversationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
