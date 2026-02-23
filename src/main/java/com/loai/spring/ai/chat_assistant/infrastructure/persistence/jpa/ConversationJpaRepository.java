package com.loai.spring.ai.chat_assistant.infrastructure.persistence.jpa;

import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ConversationEntity.
 * Provides database access methods for conversations.
 */
@Repository
public interface ConversationJpaRepository extends JpaRepository<ConversationEntity, UUID> {

    /**
     * Finds conversations for a specific tenant with pagination.
     *
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of conversations
     */
    Page<ConversationEntity> findByTenantId(UUID tenantId, Pageable pageable);

    /**
     * Finds a conversation with eagerly loaded messages.
     *
     * @param id the conversation ID
     * @return optional conversation with messages
     */
    @Query("SELECT c FROM ConversationEntity c LEFT JOIN FETCH c.messages WHERE c.id = :id")
    Optional<ConversationEntity> findByIdWithMessages(@Param("id") UUID id);

    /**
     * Checks if a conversation exists for a given tenant.
     *
     * @param id the conversation ID
     * @param tenantId the tenant ID
     * @return true if exists
     */
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Counts conversations for a tenant.
     *
     * @param tenantId the tenant ID
     * @return conversation count
     */
    long countByTenantId(UUID tenantId);
}
