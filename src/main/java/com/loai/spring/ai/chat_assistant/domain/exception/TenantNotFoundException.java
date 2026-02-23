package com.loai.spring.ai.chat_assistant.domain.exception;

import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;

/**
 * Exception thrown when a tenant cannot be found.
 */
public class TenantNotFoundException extends DomainException {

    public TenantNotFoundException(TenantId tenantId) {
        super("Tenant not found with ID: " + tenantId);
    }

    public TenantNotFoundException(String apiKey) {
        super("Tenant not found with API key: " + apiKey);
    }
}
