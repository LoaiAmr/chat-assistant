package com.loai.spring.ai.chat_assistant.infrastructure.persistence.mapper;

import com.loai.spring.ai.chat_assistant.domain.model.TokenUsage;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.TokenUsageEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between TokenUsage domain model and TokenUsageEntity.
 */
@Component
public class TokenUsageEntityMapper {

    public TokenUsage toDomain(TokenUsageEntity entity) {
        if (entity == null) {
            return null;
        }

        return TokenUsage.builder()
            .id(entity.getId())
            .tenantId(TenantId.of(entity.getTenantId()))
            .usageDate(entity.getUsageDate())
            .promptTokens(entity.getPromptTokens())
            .completionTokens(entity.getCompletionTokens())
            .estimatedCostUsd(entity.getEstimatedCostUsd())
            .requestCount(entity.getRequestCount())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public TokenUsageEntity toEntity(TokenUsage domain) {
        if (domain == null) {
            return null;
        }

        return TokenUsageEntity.builder()
            .id(domain.getId())
            .tenantId(domain.getTenantId().getValue())
            .usageDate(domain.getUsageDate())
            .promptTokens(domain.getPromptTokens())
            .completionTokens(domain.getCompletionTokens())
            .estimatedCostUsd(domain.getEstimatedCostUsd())
            .requestCount(domain.getRequestCount())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }
}
