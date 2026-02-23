package com.loai.spring.ai.chat_assistant.infrastructure.persistence.mapper;

import com.loai.spring.ai.chat_assistant.domain.model.Message;
import com.loai.spring.ai.chat_assistant.domain.model.vo.*;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.MessageEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper for converting between Message domain model and MessageEntity.
 */
@Component
public class MessageEntityMapper {

    public Message toDomain(MessageEntity entity) {
        if (entity == null) {
            return null;
        }

        ModerationResult moderationResult = buildModerationResult(
            entity.getModerationStatus(),
            entity.getModerationFlags()
        );

        return Message.builder()
            .id(MessageId.of(entity.getId()))
            .conversationId(ConversationId.of(entity.getConversationId()))
            .role(entity.getRole())
            .content(MessageContent.of(entity.getContent()))
            .promptTokens(entity.getPromptTokens() != null ? TokenCount.of(entity.getPromptTokens()) : TokenCount.zero())
            .completionTokens(entity.getCompletionTokens() != null ? TokenCount.of(entity.getCompletionTokens()) : TokenCount.zero())
            .moderationResult(moderationResult)
            .createdAt(entity.getCreatedAt())
            .build();
    }

    public MessageEntity toEntity(Message domain) {
        if (domain == null) {
            return null;
        }

        Map<String, Object> moderationFlags = buildModerationFlags(domain.getModerationResult());

        return MessageEntity.builder()
            .id(domain.getId() != null ? domain.getId().getValue() : null)
            .conversationId(domain.getConversationId().getValue())
            .role(domain.getRole())
            .content(domain.getContent().toString())
            .promptTokens(domain.getPromptTokens() != null ? domain.getPromptTokens().getValue() : 0)
            .completionTokens(domain.getCompletionTokens() != null ? domain.getCompletionTokens().getValue() : 0)
            .moderationStatus(domain.getModerationResult() != null ?
                domain.getModerationResult().getStatus() : ModerationStatus.PENDING)
            .moderationFlags(moderationFlags)
            .createdAt(domain.getCreatedAt())
            .build();
    }

    private ModerationResult buildModerationResult(ModerationStatus status, Map<String, Object> flags) {
        if (status == null) {
            return ModerationResult.pending();
        }

        List<String> categories = flags != null && flags.containsKey("categories") ?
            (List<String>) flags.get("categories") : List.of();

        return switch (status) {
            case APPROVED -> ModerationResult.approved();
            case FLAGGED -> ModerationResult.flagged(categories);
            case REJECTED -> ModerationResult.rejected(categories);
            default -> ModerationResult.pending();
        };
    }

    private Map<String, Object> buildModerationFlags(ModerationResult moderationResult) {
        if (moderationResult == null || moderationResult.getFlaggedCategories().isEmpty()) {
            return null;
        }

        Map<String, Object> flags = new HashMap<>();
        flags.put("categories", moderationResult.getFlaggedCategories());
        return flags;
    }
}
