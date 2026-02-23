package com.loai.spring.ai.chat_assistant.infrastructure.audit;

import com.loai.spring.ai.chat_assistant.application.port.output.AuditService;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.AuditLogEntity;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.AuditLogEntity.AuditEventType;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.jpa.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AuditService for logging important events.
 * Uses async processing to avoid blocking main request flow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogJpaRepository auditLogRepository;

    @Override
    @Async
    public void logChatRequest(TenantId tenantId, ConversationId conversationId, Map<String, Object> eventData) {
        try {
            AuditLogEntity auditLog = AuditLogEntity.builder()
                .tenantId(tenantId.getValue())
                .conversationId(conversationId.getValue())
                .eventType(AuditEventType.CHAT_REQUEST)
                .eventData(eventData)
                .correlationId(MDC.get("correlationId"))
                .build();

            auditLogRepository.save(auditLog);

            log.debug("Audit log created for chat request: tenantId={}, conversationId={}",
                tenantId.getValue(), conversationId.getValue());

        } catch (Exception e) {
            log.error("Failed to create audit log for chat request: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void logModerationFlagged(TenantId tenantId, ConversationId conversationId, List<String> flaggedCategories) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("flaggedCategories", flaggedCategories);

            AuditLogEntity auditLog = AuditLogEntity.builder()
                .tenantId(tenantId.getValue())
                .conversationId(conversationId.getValue())
                .eventType(AuditEventType.MODERATION_FLAGGED)
                .eventData(eventData)
                .correlationId(MDC.get("correlationId"))
                .build();

            auditLogRepository.save(auditLog);

            log.warn("Audit log created for moderation flagged: tenantId={}, categories={}",
                tenantId.getValue(), flaggedCategories);

        } catch (Exception e) {
            log.error("Failed to create audit log for moderation flagged: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void logRateLimitExceeded(TenantId tenantId) {
        try {
            AuditLogEntity auditLog = AuditLogEntity.builder()
                .tenantId(tenantId.getValue())
                .eventType(AuditEventType.RATE_LIMIT_EXCEEDED)
                .eventData(new HashMap<>())
                .correlationId(MDC.get("correlationId"))
                .build();

            auditLogRepository.save(auditLog);

            log.warn("Audit log created for rate limit exceeded: tenantId={}", tenantId.getValue());

        } catch (Exception e) {
            log.error("Failed to create audit log for rate limit exceeded: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void logTokenBudgetExceeded(TenantId tenantId, long tokensUsed, long tokenLimit) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("tokensUsed", tokensUsed);
            eventData.put("tokenLimit", tokenLimit);

            AuditLogEntity auditLog = AuditLogEntity.builder()
                .tenantId(tenantId.getValue())
                .eventType(AuditEventType.TOKEN_BUDGET_EXCEEDED)
                .eventData(eventData)
                .correlationId(MDC.get("correlationId"))
                .build();

            auditLogRepository.save(auditLog);

            log.warn("Audit log created for token budget exceeded: tenantId={}, used={}, limit={}",
                tenantId.getValue(), tokensUsed, tokenLimit);

        } catch (Exception e) {
            log.error("Failed to create audit log for token budget exceeded: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void logError(TenantId tenantId, String errorMessage, Throwable exception) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("errorMessage", errorMessage);
            if (exception != null) {
                eventData.put("exceptionClass", exception.getClass().getName());
                eventData.put("exceptionMessage", exception.getMessage());
            }

            AuditLogEntity auditLog = AuditLogEntity.builder()
                .tenantId(tenantId.getValue())
                .eventType(AuditEventType.ERROR)
                .eventData(eventData)
                .correlationId(MDC.get("correlationId"))
                .build();

            auditLogRepository.save(auditLog);

            log.error("Audit log created for error: tenantId={}, message={}",
                tenantId.getValue(), errorMessage);

        } catch (Exception e) {
            log.error("Failed to create audit log for error: {}", e.getMessage(), e);
        }
    }
}
