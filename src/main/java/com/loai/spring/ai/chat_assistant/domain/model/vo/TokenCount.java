package com.loai.spring.ai.chat_assistant.domain.model.vo;

import lombok.Value;

/**
 * Value object representing a token count.
 * Ensures token count is non-negative.
 */
@Value
public class TokenCount {
    int value;

    private TokenCount(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Token count cannot be negative: " + value);
        }
        this.value = value;
    }

    public static TokenCount of(int value) {
        return new TokenCount(value);
    }

    public static TokenCount zero() {
        return new TokenCount(0);
    }

    public TokenCount add(TokenCount other) {
        return new TokenCount(this.value + other.value);
    }

    public boolean exceeds(TokenCount limit) {
        return this.value > limit.value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
