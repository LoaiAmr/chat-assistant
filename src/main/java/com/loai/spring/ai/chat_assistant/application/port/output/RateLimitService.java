package com.loai.spring.ai.chat_assistant.application.port.output;

import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;

/**
 * Secondary port for rate limiting.
 * Abstracts the rate limiting implementation (Caffeine, Redis, etc.)
 */
public interface RateLimitService {

    /**
     * Checks if a request is allowed for the tenant.
     *
     * @param tenantId tenant identifier
     * @return true if request is allowed, false if rate limit exceeded
     */
    boolean allowRequest(TenantId tenantId);

    /**
     * Records a request for rate limiting purposes.
     *
     * @param tenantId tenant identifier
     */
    void recordRequest(TenantId tenantId);

    /**
     * Gets the rate limit status for a tenant.
     *
     * @param tenantId tenant identifier
     * @return rate limit status
     */
    RateLimitStatus getStatus(TenantId tenantId);

    /**
     * Rate limit status information.
     */
    record RateLimitStatus(
        int requestsRemaining,
        long resetTimeSeconds
    ) {}
}
