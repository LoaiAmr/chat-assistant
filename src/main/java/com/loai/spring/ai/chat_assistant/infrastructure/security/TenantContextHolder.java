package com.loai.spring.ai.chat_assistant.infrastructure.security;

import com.loai.spring.ai.chat_assistant.domain.model.Tenant;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import lombok.Value;

import java.time.Instant;

/**
 * Thread-local storage for tenant context.
 * Provides tenant information for the current request.
 */
public class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    public static void setTenantContext(TenantContext context) {
        CONTEXT.set(context);
    }

    public static TenantContext getTenantContext() {
        TenantContext context = CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("Tenant context not set. Ensure TenantInterceptor is configured.");
        }
        return context;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    @Value
    public static class TenantContext {
        TenantId tenantId;
        Tenant tenant;
    }
}
