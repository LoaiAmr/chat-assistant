package com.loai.spring.ai.chat_assistant.infrastructure.persistence.jpa;

import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.TokenUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for TokenUsageEntity.
 * Provides database access methods for token usage tracking.
 */
@Repository
public interface TokenUsageJpaRepository extends JpaRepository<TokenUsageEntity, UUID> {

    /**
     * Finds token usage for a specific tenant and date.
     *
     * @param tenantId the tenant ID
     * @param usageDate the usage date
     * @return optional token usage entity
     */
    Optional<TokenUsageEntity> findByTenantIdAndUsageDate(UUID tenantId, LocalDate usageDate);

    /**
     * Finds token usage for a tenant within a date range.
     *
     * @param tenantId the tenant ID
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of token usage entities
     */
    List<TokenUsageEntity> findByTenantIdAndUsageDateBetween(
        UUID tenantId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Calculates total tokens used by a tenant within a date range.
     *
     * @param tenantId the tenant ID
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return total tokens (prompt + completion)
     */
    @Query("SELECT COALESCE(SUM(t.promptTokens + t.completionTokens), 0) " +
           "FROM TokenUsageEntity t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.usageDate BETWEEN :startDate AND :endDate")
    long sumTokensByTenantIdAndDateRange(
        @Param("tenantId") UUID tenantId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
