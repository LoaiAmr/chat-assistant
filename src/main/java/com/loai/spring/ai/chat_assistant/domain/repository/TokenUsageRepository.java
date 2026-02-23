package com.loai.spring.ai.chat_assistant.domain.repository;

import com.loai.spring.ai.chat_assistant.domain.model.TokenUsage;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for TokenUsage aggregate.
 * Defines operations without specifying implementation details.
 */
public interface TokenUsageRepository {

    /**
     * Saves token usage (create or update).
     */
    TokenUsage save(TokenUsage tokenUsage);

    /**
     * Finds token usage for a specific tenant and date.
     */
    Optional<TokenUsage> findByTenantIdAndUsageDate(TenantId tenantId, LocalDate usageDate);

    /**
     * Finds token usage for a tenant within a date range.
     */
    List<TokenUsage> findByTenantIdAndUsageDateBetween(
        TenantId tenantId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Calculates total tokens used by a tenant within a date range.
     */
    long sumTokensByTenantIdAndDateRange(TenantId tenantId, LocalDate startDate, LocalDate endDate);
}
