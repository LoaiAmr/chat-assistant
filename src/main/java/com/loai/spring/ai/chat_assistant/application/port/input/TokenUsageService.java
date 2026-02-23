package com.loai.spring.ai.chat_assistant.application.port.input;

import com.loai.spring.ai.chat_assistant.application.dto.response.TokenUsageResponse;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;

import java.time.LocalDate;

/**
 * Primary port for token usage tracking and budget management.
 * Handles recording token usage and checking budgets.
 */
public interface TokenUsageService {

    /**
     * Records token usage for a chat request.
     *
     * @param tenantId tenant identifier
     * @param promptTokens tokens used in the prompt
     * @param completionTokens tokens used in the completion
     */
    void recordUsage(TenantId tenantId, TokenCount promptTokens, TokenCount completionTokens);

    /**
     * Checks if the tenant can consume the estimated tokens within their budget.
     * Throws exception if budget would be exceeded.
     *
     * @param tenantId tenant identifier
     * @param estimatedTokens estimated tokens to be consumed
     */
    void checkBudget(TenantId tenantId, TokenCount estimatedTokens);

    /**
     * Gets token usage statistics for the current tenant.
     *
     * @param startDate start date for the report
     * @param endDate end date for the report
     * @return token usage statistics
     */
    TokenUsageResponse getUsageStatistics(LocalDate startDate, LocalDate endDate);
}
