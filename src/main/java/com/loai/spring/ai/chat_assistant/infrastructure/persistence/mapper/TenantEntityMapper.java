package com.loai.spring.ai.chat_assistant.infrastructure.persistence.mapper;

import com.loai.spring.ai.chat_assistant.domain.model.Tenant;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.TenantEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Tenant domain model and TenantEntity.
 */
@Component
public class TenantEntityMapper {

    public Tenant toDomain(TenantEntity entity) {
        if (entity == null) {
            return null;
        }

        return Tenant.builder()
            .id(TenantId.of(entity.getId()))
            .name(entity.getName())
            .apiKey(entity.getApiKey())
            .active(entity.isActive())
            .dailyTokenLimit(entity.getDailyTokenLimit())
            .monthlyTokenLimit(entity.getMonthlyTokenLimit())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public TenantEntity toEntity(Tenant domain) {
        if (domain == null) {
            return null;
        }

        return TenantEntity.builder()
            .id(domain.getId() != null ? domain.getId().getValue() : null)
            .name(domain.getName())
            .apiKey(domain.getApiKey())
            .active(domain.isActive())
            .dailyTokenLimit(domain.getDailyTokenLimit())
            .monthlyTokenLimit(domain.getMonthlyTokenLimit())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }
}
