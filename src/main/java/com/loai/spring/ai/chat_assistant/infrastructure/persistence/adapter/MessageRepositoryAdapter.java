package com.loai.spring.ai.chat_assistant.infrastructure.persistence.adapter;

import com.loai.spring.ai.chat_assistant.domain.model.Message;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.MessageId;
import com.loai.spring.ai.chat_assistant.domain.repository.MessageRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.MessageEntity;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.jpa.MessageJpaRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.mapper.MessageEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing MessageRepository using JPA.
 * Translates between domain models and JPA entities.
 */
@Repository
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageJpaRepository jpaRepository;
    private final MessageEntityMapper mapper;

    @Override
    public Message save(Message message) {
        MessageEntity entity = mapper.toEntity(message);
        MessageEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Message> findById(MessageId id) {
        return jpaRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public List<Message> findByConversationIdOrderByCreatedAtAsc(ConversationId conversationId) {
        List<MessageEntity> entities = jpaRepository.findByConversationIdOrderByCreatedAtAsc(conversationId.getValue());
        return entities.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long countByConversationId(ConversationId conversationId) {
        return jpaRepository.countByConversationId(conversationId.getValue());
    }

    @Override
    public void deleteByConversationId(ConversationId conversationId) {
        jpaRepository.deleteByConversationId(conversationId.getValue());
    }
}
