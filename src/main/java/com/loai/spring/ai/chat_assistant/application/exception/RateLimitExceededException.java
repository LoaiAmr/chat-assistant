package com.loai.spring.ai.chat_assistant.application.exception;

import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import lombok.Getter;

/**
 * Exception thrown when a tenant exceeds their rate limit.
 */
@Getter
public class RateLimitExceededException extends RuntimeException {

    private final TenantId tenantId;
    private final int retryAfterSeconds;

    public RateLimitExceededException(TenantId tenantId, int retryAfterSeconds) {
        super("Rate limit exceeded for tenant: " + tenantId + ". Retry after " + retryAfterSeconds + " seconds.");
        this.tenantId = tenantId;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimitExceededException(String message, int retryAfterSeconds) {
        super(message);
        this.tenantId = null;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
