package com.loai.spring.ai.chat_assistant.infrastructure.persistence.adapter;

import com.loai.spring.ai.chat_assistant.domain.model.Tenant;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.domain.repository.TenantRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.TenantEntity;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.jpa.TenantJpaRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.mapper.TenantEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter implementing TenantRepository using JPA.
 * Translates between domain models and JPA entities.
 */
@Repository
@RequiredArgsConstructor
public class TenantRepositoryAdapter implements TenantRepository {

    private final TenantJpaRepository jpaRepository;
    private final TenantEntityMapper mapper;

    @Override
    public Tenant save(Tenant tenant) {
        TenantEntity entity = mapper.toEntity(tenant);
        TenantEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Tenant> findById(TenantId id) {
        return jpaRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Tenant> findByApiKey(String apiKey) {
        return jpaRepository.findByApiKey(apiKey)
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(TenantId id) {
        return jpaRepository.existsById(id.getValue());
    }

    @Override
    public void deleteById(TenantId id) {
        jpaRepository.deleteById(id.getValue());
    }
}
