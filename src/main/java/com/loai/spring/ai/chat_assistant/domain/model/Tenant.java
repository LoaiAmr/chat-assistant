package com.loai.spring.ai.chat_assistant.domain.model;

import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Instant;

/**
 * Domain entity representing a tenant in the multi-tenant system.
 * Immutable value object with rich business logic.
 */
@Value
@Builder
@With
public class Tenant {
    TenantId id;
    String name;
    String apiKey;
    boolean active;
    Integer dailyTokenLimit;
    Integer monthlyTokenLimit;
    Instant createdAt;
    Instant updatedAt;

    /**
     * Checks if the tenant can consume the specified number of tokens
     * within their daily limit.
     */
    public boolean canConsumeTokens(TokenCount tokensToConsume, TokenCount tokensUsedToday) {
        if (dailyTokenLimit == null) {
            return true; // No limit set
        }

        TokenCount dailyLimit = TokenCount.of(dailyTokenLimit);
        TokenCount totalAfterConsumption = tokensUsedToday.add(tokensToConsume);

        return !totalAfterConsumption.exceeds(dailyLimit);
    }

    /**
     * Checks if the tenant can consume the specified number of tokens
     * within their monthly limit.
     */
    public boolean canConsumeTokensMonthly(TokenCount tokensToConsume, TokenCount tokensUsedThisMonth) {
        if (monthlyTokenLimit == null) {
            return true; // No limit set
        }

        TokenCount monthlyLimit = TokenCount.of(monthlyTokenLimit);
        TokenCount totalAfterConsumption = tokensUsedThisMonth.add(tokensToConsume);

        return !totalAfterConsumption.exceeds(monthlyLimit);
    }

    /**
     * Validates if the tenant is active and can make requests.
     */
    public boolean canMakeRequests() {
        return active;
    }
}
