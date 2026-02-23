package com.loai.spring.ai.chat_assistant.infrastructure.persistence.adapter;

import com.loai.spring.ai.chat_assistant.domain.model.Conversation;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.domain.repository.ConversationRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.ConversationEntity;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.jpa.ConversationJpaRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.mapper.ConversationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter implementing ConversationRepository using JPA.
 * Translates between domain models and JPA entities.
 */
@Repository
@RequiredArgsConstructor
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final ConversationJpaRepository jpaRepository;
    private final ConversationEntityMapper mapper;

    @Override
    public Conversation save(Conversation conversation) {
        ConversationEntity entity = mapper.toEntity(conversation);
        ConversationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Conversation> findById(ConversationId id) {
        return jpaRepository.findByIdWithMessages(id.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public Page<Conversation> findByTenantId(TenantId tenantId, Pageable pageable) {
        Page<ConversationEntity> entities = jpaRepository.findByTenantId(tenantId.getValue(), pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    public boolean existsByIdAndTenantId(ConversationId id, TenantId tenantId) {
        return jpaRepository.existsByIdAndTenantId(id.getValue(), tenantId.getValue());
    }

    @Override
    public void deleteById(ConversationId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public long countByTenantId(TenantId tenantId) {
        return jpaRepository.countByTenantId(tenantId.getValue());
    }
}
