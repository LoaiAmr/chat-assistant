package com.loai.spring.ai.chat_assistant.application.port.output;

import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;

import java.util.Map;

/**
 * Secondary port for audit logging.
 * Records important events for compliance and debugging.
 */
public interface AuditService {

    /**
     * Logs a chat request event.
     *
     * @param tenantId tenant identifier
     * @param conversationId conversation identifier
     * @param eventData additional event data
     */
    void logChatRequest(TenantId tenantId, ConversationId conversationId, Map<String, Object> eventData);

    /**
     * Logs a moderation flag event.
     *
     * @param tenantId tenant identifier
     * @param conversationId conversation identifier
     * @param flaggedCategories categories that were flagged
     */
    void logModerationFlagged(TenantId tenantId, ConversationId conversationId, java.util.List<String> flaggedCategories);

    /**
     * Logs a rate limit exceeded event.
     *
     * @param tenantId tenant identifier
     */
    void logRateLimitExceeded(TenantId tenantId);

    /**
     * Logs a token budget exceeded event.
     *
     * @param tenantId tenant identifier
     * @param tokensUsed tokens used
     * @param tokenLimit token limit
     */
    void logTokenBudgetExceeded(TenantId tenantId, long tokensUsed, long tokenLimit);

    /**
     * Logs an error event.
     *
     * @param tenantId tenant identifier
     * @param errorMessage error message
     * @param exception exception details
     */
    void logError(TenantId tenantId, String errorMessage, Throwable exception);
}
