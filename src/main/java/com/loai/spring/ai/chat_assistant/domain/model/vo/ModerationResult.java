package com.loai.spring.ai.chat_assistant.domain.model.vo;

import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * Value object representing the result of content moderation.
 * Contains the status and any flagged categories.
 */
@Value
public class ModerationResult {
    ModerationStatus status;
    List<String> flaggedCategories;

    private ModerationResult(ModerationStatus status, List<String> flaggedCategories) {
        this.status = status;
        this.flaggedCategories = flaggedCategories != null
            ? Collections.unmodifiableList(flaggedCategories)
            : Collections.emptyList();
    }

    public static ModerationResult approved() {
        return new ModerationResult(ModerationStatus.APPROVED, Collections.emptyList());
    }

    public static ModerationResult pending() {
        return new ModerationResult(ModerationStatus.PENDING, Collections.emptyList());
    }

    public static ModerationResult flagged(List<String> categories) {
        return new ModerationResult(ModerationStatus.FLAGGED, categories);
    }

    public static ModerationResult rejected(List<String> categories) {
        return new ModerationResult(ModerationStatus.REJECTED, categories);
    }

    public boolean isPassed() {
        return status == ModerationStatus.APPROVED;
    }

    public boolean isFailed() {
        return status == ModerationStatus.REJECTED;
    }
}
