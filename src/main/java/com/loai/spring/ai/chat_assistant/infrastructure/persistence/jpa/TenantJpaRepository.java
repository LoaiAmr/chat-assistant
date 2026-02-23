package com.loai.spring.ai.chat_assistant.infrastructure.persistence.jpa;

import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for TenantEntity.
 * Provides database access methods for tenants.
 */
@Repository
public interface TenantJpaRepository extends JpaRepository<TenantEntity, UUID> {

    /**
     * Finds a tenant by API key.
     *
     * @param apiKey the API key
     * @return optional tenant entity
     */
    Optional<TenantEntity> findByApiKey(String apiKey);

    /**
     * Checks if a tenant exists by API key.
     *
     * @param apiKey the API key
     * @return true if exists
     */
    boolean existsByApiKey(String apiKey);
}
