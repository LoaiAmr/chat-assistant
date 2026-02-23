package com.loai.spring.ai.chat_assistant.domain.model.vo;

/**
 * Enum representing the moderation status of a message.
 * - PENDING: Moderation not yet performed or in progress
 * - APPROVED: Content passed moderation checks
 * - FLAGGED: Content flagged but not rejected (warning)
 * - REJECTED: Content failed moderation and was rejected
 */
public enum ModerationStatus {
    PENDING,
    APPROVED,
    FLAGGED,
    REJECTED
}
