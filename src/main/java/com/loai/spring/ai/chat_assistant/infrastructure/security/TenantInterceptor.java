package com.loai.spring.ai.chat_assistant.infrastructure.security;

import com.loai.spring.ai.chat_assistant.domain.exception.TenantNotFoundException;
import com.loai.spring.ai.chat_assistant.domain.model.Tenant;
import com.loai.spring.ai.chat_assistant.domain.repository.TenantRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.security.TenantContextHolder.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for extracting and validating tenant information from request headers.
 * Sets up TenantContext for the current request thread.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_API_KEY_HEADER = "X-Tenant-API-Key";

    private final TenantRepository tenantRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String apiKey = request.getHeader(TENANT_API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Missing tenant API key in request to {}", request.getRequestURI());
            throw new TenantNotFoundException("Tenant API key is required. Provide it via X-Tenant-API-Key header.");
        }

        Tenant tenant = tenantRepository.findByApiKey(apiKey)
            .orElseThrow(() -> {
                log.error("Invalid tenant API key: {}", maskApiKey(apiKey));
                return new TenantNotFoundException("Invalid tenant API key");
            });

        if (!tenant.isActive()) {
            log.warn("Inactive tenant attempted access: {}", tenant.getId().getValue());
            throw new TenantNotFoundException("Tenant account is inactive");
        }

        // Set tenant context for this request thread
        TenantContextHolder.setTenantContext(new TenantContext(tenant.getId(), tenant));

        log.debug("Tenant context set for request: tenantId={}, uri={}",
            tenant.getId().getValue(), request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // Clear tenant context after request completes
        TenantContextHolder.clear();
        log.debug("Tenant context cleared for request to {}", request.getRequestURI());
    }

    /**
     * Masks API key for logging (shows only first 8 characters).
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 8) + "***";
    }
}
