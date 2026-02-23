package com.loai.spring.ai.chat_assistant.domain.repository;

import com.loai.spring.ai.chat_assistant.domain.model.Tenant;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;

import java.util.Optional;

/**
 * Repository port for Tenant aggregate.
 * Defines operations without specifying implementation details.
 * This is a port in hexagonal architecture - infrastructure will provide adapters.
 */
public interface TenantRepository {

    /**
     * Saves a tenant (create or update).
     */
    Tenant save(Tenant tenant);

    /**
     * Finds a tenant by its unique identifier.
     */
    Optional<Tenant> findById(TenantId id);

    /**
     * Finds a tenant by API key for authentication.
     */
    Optional<Tenant> findByApiKey(String apiKey);

    /**
     * Checks if a tenant exists by ID.
     */
    boolean existsById(TenantId id);

    /**
     * Deletes a tenant by ID.
     */
    void deleteById(TenantId id);
}
