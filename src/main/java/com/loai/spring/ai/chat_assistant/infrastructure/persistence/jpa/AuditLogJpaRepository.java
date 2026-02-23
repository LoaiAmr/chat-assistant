package com.loai.spring.ai.chat_assistant.infrastructure.persistence.jpa;

import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for AuditLogEntity.
 * Provides database access methods for audit logs.
 */
@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, UUID> {
    // Basic CRUD operations provided by JpaRepository
    // Additional query methods can be added as needed
}
