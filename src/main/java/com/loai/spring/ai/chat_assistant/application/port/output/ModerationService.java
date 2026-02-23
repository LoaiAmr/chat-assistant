package com.loai.spring.ai.chat_assistant.application.port.output;

import com.loai.spring.ai.chat_assistant.domain.model.vo.ModerationResult;

/**
 * Secondary port for content moderation.
 * Abstracts the moderation provider (OpenAI Moderation API, etc.)
 */
public interface ModerationService {

    /**
     * Moderates content for inappropriate material.
     *
     * @param content text content to moderate
     * @return moderation result with status and flagged categories
     */
    ModerationResult moderateContent(String content);

    /**
     * Quick check if content is safe (approved).
     *
     * @param content text content to check
     * @return true if content is safe, false otherwise
     */
    boolean isContentSafe(String content);
}
