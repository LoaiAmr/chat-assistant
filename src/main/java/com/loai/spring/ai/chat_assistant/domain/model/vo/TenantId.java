package com.loai.spring.ai.chat_assistant.domain.model.vo;

import lombok.Value;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique tenant identifier.
 * Immutable and type-safe.
 */
@Value
public class TenantId {
    UUID value;

    private TenantId(UUID value) {
        this.value = Objects.requireNonNull(value, "Tenant ID cannot be null");
    }

    public static TenantId of(UUID value) {
        return new TenantId(value);
    }

    public static TenantId of(String value) {
        return new TenantId(UUID.fromString(value));
    }

    public static TenantId generate() {
        return new TenantId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
