package com.loai.spring.ai.chat_assistant.domain.exception;

import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;

/**
 * Exception thrown when attempting to use an inactive tenant.
 */
public class TenantInactiveException extends DomainException {

    public TenantInactiveException(TenantId tenantId) {
        super("Tenant is inactive: " + tenantId);
    }
}
