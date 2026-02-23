package com.loai.spring.ai.chat_assistant.domain.model;

import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain entity representing aggregated token usage for a tenant on a specific date.
 * Used for cost tracking and budget enforcement.
 */
@Value
@Builder
@With
public class TokenUsage {
    UUID id;
    TenantId tenantId;
    LocalDate usageDate;
    long promptTokens;
    long completionTokens;
    BigDecimal estimatedCostUsd;
    int requestCount;
    Instant createdAt;
    Instant updatedAt;

    /**
     * Calculates total tokens (prompt + completion).
     */
    public long getTotalTokens() {
        return promptTokens + completionTokens;
    }

    /**
     * Adds usage from a chat request.
     * Returns a new TokenUsage instance with updated values.
     */
    public TokenUsage addUsage(long promptTokens, long completionTokens) {
        return this.withPromptTokens(this.promptTokens + promptTokens)
            .withCompletionTokens(this.completionTokens + completionTokens)
            .withRequestCount(this.requestCount + 1)
            .withUpdatedAt(Instant.now());
    }

    /**
     * Calculates estimated cost based on token counts and pricing.
     */
    public TokenUsage calculateCost(BigDecimal costPer1kPromptTokens, BigDecimal costPer1kCompletionTokens) {
        BigDecimal promptCost = BigDecimal.valueOf(promptTokens)
            .multiply(costPer1kPromptTokens)
            .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        BigDecimal completionCost = BigDecimal.valueOf(completionTokens)
            .multiply(costPer1kCompletionTokens)
            .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        BigDecimal totalCost = promptCost.add(completionCost);

        return this.withEstimatedCostUsd(totalCost);
    }
}
