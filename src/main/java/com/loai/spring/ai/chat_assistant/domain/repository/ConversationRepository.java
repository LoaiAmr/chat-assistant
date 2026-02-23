package com.loai.spring.ai.chat_assistant.domain.repository;

import com.loai.spring.ai.chat_assistant.domain.model.Conversation;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Repository port for Conversation aggregate root.
 * Defines operations without specifying implementation details.
 */
public interface ConversationRepository {

    /**
     * Saves a conversation (create or update).
     */
    Conversation save(Conversation conversation);

    /**
     * Finds a conversation by its unique identifier.
     */
    Optional<Conversation> findById(ConversationId id);

    /**
     * Finds conversations for a specific tenant with pagination.
     */
    Page<Conversation> findByTenantId(TenantId tenantId, Pageable pageable);

    /**
     * Checks if a conversation exists for a given tenant.
     * Used for tenant isolation verification.
     */
    boolean existsByIdAndTenantId(ConversationId id, TenantId tenantId);

    /**
     * Deletes a conversation by ID.
     */
    void deleteById(ConversationId id);

    /**
     * Counts conversations for a tenant.
     */
    long countByTenantId(TenantId tenantId);
}
