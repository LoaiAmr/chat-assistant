package com.loai.spring.ai.chat_assistant.infrastructure.persistence.mapper;

import com.loai.spring.ai.chat_assistant.domain.model.Conversation;
import com.loai.spring.ai.chat_assistant.domain.model.Message;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.ConversationEntity;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.MessageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Conversation domain model and ConversationEntity.
 */
@Component
@RequiredArgsConstructor
public class ConversationEntityMapper {

    private final MessageEntityMapper messageMapper;

    public Conversation toDomain(ConversationEntity entity) {
        if (entity == null) {
            return null;
        }

        var messages = entity.getMessages() != null ?
            entity.getMessages().stream()
                .map(messageMapper::toDomain)
                .collect(Collectors.toList()) :
            new ArrayList<Message>();

        return Conversation.builder()
            .id(ConversationId.of(entity.getId()))
            .tenantId(TenantId.of(entity.getTenantId()))
            .title(entity.getTitle())
            .messages(messages)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public ConversationEntity toEntity(Conversation domain) {
        if (domain == null) {
            return null;
        }

        List<MessageEntity> messageEntities = domain.getMessages() != null ?
            domain.getMessages().stream()
                .map(messageMapper::toEntity)
                .collect(Collectors.toList()) :
            new ArrayList<>();

        return ConversationEntity.builder()
            .id(domain.getId() != null ? domain.getId().getValue() : null)
            .tenantId(domain.getTenantId().getValue())
            .title(domain.getTitle())
            .messages(messageEntities)
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }
}
