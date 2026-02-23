package com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity for the token_usage table.
 * Maps to the database schema defined in V4__create_token_usage_table.sql
 */
@Entity
@Table(
    name = "token_usage",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "usage_date"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenUsageEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "prompt_tokens", nullable = false)
    @Builder.Default
    private Long promptTokens = 0L;

    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private Long completionTokens = 0L;

    @Column(name = "estimated_cost_usd", precision = 10, scale = 6)
    private BigDecimal estimatedCostUsd;

    @Column(name = "request_count", nullable = false)
    @Builder.Default
    private Integer requestCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
