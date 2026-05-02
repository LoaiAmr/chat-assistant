package com.loai.spring.ai.chat_assistant.infrastructure.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loai.spring.ai.chat_assistant.application.dto.response.ErrorResponse;
import com.loai.spring.ai.chat_assistant.domain.model.Tenant;
import com.loai.spring.ai.chat_assistant.domain.repository.TenantRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.security.TenantContextHolder;
import com.loai.spring.ai.chat_assistant.infrastructure.security.TenantContextHolder.TenantContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Single entry point for all request-scoped observability concerns:
 * - Correlation ID propagation (from header or generated)
 * - Tenant authentication and MDC enrichment
 * - HTTP request timing and access logging
 * - Full MDC and TenantContext cleanup in finally
 *
 * Replaces both CorrelationIdFilter and TenantInterceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MDCFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String TENANT_API_KEY_HEADER = "X-Tenant-API-Key";

    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. Correlation ID — reuse from client header or generate a new one
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

        // 2. Tenant auth + MDC enrichment for API paths
        if (requiresTenantAuth(httpRequest)) {
            String apiKey = httpRequest.getHeader(TENANT_API_KEY_HEADER);

            if (apiKey == null || apiKey.isBlank()) {
                log.warn("Missing tenant API key for {}", httpRequest.getRequestURI());
                writeUnauthorized(httpResponse, "Tenant API key is required. Provide it via X-Tenant-API-Key header.", correlationId);
                MDC.clear();
                return;
            }

            Optional<Tenant> tenantOpt = tenantRepository.findByApiKey(apiKey);
            if (tenantOpt.isEmpty()) {
                log.warn("Invalid tenant API key: {}", maskApiKey(apiKey));
                writeUnauthorized(httpResponse, "Invalid tenant API key", correlationId);
                MDC.clear();
                return;
            }

            Tenant tenant = tenantOpt.get();
            if (!tenant.isActive()) {
                log.warn("Inactive tenant attempted access: tenantId={}", tenant.getId().getValue());
                writeUnauthorized(httpResponse, "Tenant account is inactive", correlationId);
                MDC.clear();
                return;
            }

            MDC.put("tenantId", tenant.getId().getValue().toString());
            TenantContextHolder.setTenantContext(new TenantContext(tenant.getId(), tenant));
        }

        // 3. Execute request with timing
        long startNano = System.nanoTime();
        try {
            chain.doFilter(request, response);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
            log.info("HTTP {} {} -> {} ({}ms)",
                httpRequest.getMethod(), httpRequest.getRequestURI(),
                httpResponse.getStatus(), durationMs);
        } finally {
            // Single cleanup point — clears correlationId, tenantId, conversationId, traceId/spanId
            TenantContextHolder.clear();
            MDC.clear();
        }
    }

    private boolean requiresTenantAuth(HttpServletRequest request) {
        return request.getRequestURI().contains("/v1/");
    }

    private void writeUnauthorized(HttpServletResponse response, String message, String correlationId)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(401)
            .error("Unauthorized")
            .message(message)
            .correlationId(correlationId)
            .build();
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    private String maskApiKey(String apiKey) {
        if (apiKey.length() <= 8) return "***";
        return apiKey.substring(0, 8) + "***";
    }
}
