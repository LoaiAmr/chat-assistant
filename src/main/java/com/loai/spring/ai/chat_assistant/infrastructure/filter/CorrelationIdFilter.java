package com.loai.spring.ai.chat_assistant.infrastructure.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter for adding correlation IDs to requests for distributed tracing.
 * The correlation ID is propagated through MDC for logging.
 */
@Component
@Slf4j
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get or generate correlation ID
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add to MDC for logging
        MDC.put(MDC_CORRELATION_ID_KEY, correlationId);

        // Add to response header
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            log.debug("Request started: method={}, uri={}, correlationId={}",
                httpRequest.getMethod(), httpRequest.getRequestURI(), correlationId);

            chain.doFilter(request, response);

            log.debug("Request completed: correlationId={}, status={}",
                correlationId, httpResponse.getStatus());

        } finally {
            // Clear MDC after request
            MDC.remove(MDC_CORRELATION_ID_KEY);
        }
    }
}
