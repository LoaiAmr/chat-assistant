package com.loai.spring.ai.chat_assistant.infrastructure.persistence.adapter;

import com.loai.spring.ai.chat_assistant.domain.model.TokenUsage;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.domain.repository.TokenUsageRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.TokenUsageEntity;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.jpa.TokenUsageJpaRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.mapper.TokenUsageEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing TokenUsageRepository using JPA.
 * Translates between domain models and JPA entities.
 */
@Repository
@RequiredArgsConstructor
public class TokenUsageRepositoryAdapter implements TokenUsageRepository {

    private final TokenUsageJpaRepository jpaRepository;
    private final TokenUsageEntityMapper mapper;

    @Override
    public TokenUsage save(TokenUsage tokenUsage) {
        TokenUsageEntity entity = mapper.toEntity(tokenUsage);
        TokenUsageEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<TokenUsage> findByTenantIdAndUsageDate(TenantId tenantId, LocalDate usageDate) {
        return jpaRepository.findByTenantIdAndUsageDate(tenantId.getValue(), usageDate)
            .map(mapper::toDomain);
    }

    @Override
    public List<TokenUsage> findByTenantIdAndUsageDateBetween(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        List<TokenUsageEntity> entities = jpaRepository.findByTenantIdAndUsageDateBetween(
            tenantId.getValue(),
            startDate,
            endDate
        );
        return entities.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long sumTokensByTenantIdAndDateRange(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.sumTokensByTenantIdAndDateRange(
            tenantId.getValue(),
            startDate,
            endDate
        );
    }
}
