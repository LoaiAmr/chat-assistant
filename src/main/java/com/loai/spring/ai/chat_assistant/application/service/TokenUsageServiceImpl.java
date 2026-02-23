package com.loai.spring.ai.chat_assistant.application.service;

import com.loai.spring.ai.chat_assistant.application.dto.response.TokenUsageResponse;
import com.loai.spring.ai.chat_assistant.application.exception.TokenBudgetExceededException;
import com.loai.spring.ai.chat_assistant.application.port.input.TokenUsageService;
import com.loai.spring.ai.chat_assistant.domain.model.Tenant;
import com.loai.spring.ai.chat_assistant.domain.model.TokenUsage;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;
import com.loai.spring.ai.chat_assistant.domain.repository.TenantRepository;
import com.loai.spring.ai.chat_assistant.domain.repository.TokenUsageRepository;
import com.loai.spring.ai.chat_assistant.infrastructure.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for tracking and managing token usage per tenant.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenUsageServiceImpl implements TokenUsageService {

    private final TokenUsageRepository tokenUsageRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public void recordUsage(TenantId tenantId, TokenCount promptTokens, TokenCount completionTokens) {
        LocalDate today = LocalDate.now();

        TokenUsage usage = tokenUsageRepository
            .findByTenantIdAndUsageDate(tenantId, today)
            .map(existing -> existing.addUsage(promptTokens.getValue(), completionTokens.getValue()))
            .orElseGet(() -> TokenUsage.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .usageDate(today)
                .promptTokens(promptTokens.getValue())
                .completionTokens(completionTokens.getValue())
                .requestCount(1)
                .estimatedCostUsd(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        tokenUsageRepository.save(usage);

        log.info("Recorded token usage for tenant {}: prompt={}, completion={}",
            tenantId.getValue(), promptTokens.getValue(), completionTokens.getValue());
    }

    @Override
    public void checkBudget(TenantId tenantId, TokenCount estimatedTokens) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId.getValue()));

        LocalDate today = LocalDate.now();

        // Check daily limit
        if (tenant.getDailyTokenLimit() != null) {
            TokenUsage todayUsage = tokenUsageRepository
                .findByTenantIdAndUsageDate(tenantId, today)
                .orElse(TokenUsage.builder()
                    .promptTokens(0)
                    .completionTokens(0)
                    .build());

            long currentDaily = todayUsage.getTotalTokens();
            long projectedDaily = currentDaily + estimatedTokens.getValue();

            if (projectedDaily > tenant.getDailyTokenLimit()) {
                log.warn("Daily token budget exceeded for tenant {}. Current: {}, Limit: {}",
                    tenantId.getValue(), currentDaily, tenant.getDailyTokenLimit());

                throw new TokenBudgetExceededException(
                    "Daily token budget exceeded",
                    currentDaily,
                    tenant.getDailyTokenLimit().longValue()
                );
            }
        }

        // Check monthly limit
        if (tenant.getMonthlyTokenLimit() != null) {
            LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

            long currentMonthly = tokenUsageRepository.sumTokensByTenantIdAndDateRange(
                tenantId, startOfMonth, endOfMonth
            );

            long projectedMonthly = currentMonthly + estimatedTokens.getValue();

            if (projectedMonthly > tenant.getMonthlyTokenLimit()) {
                log.warn("Monthly token budget exceeded for tenant {}. Current: {}, Limit: {}",
                    tenantId.getValue(), currentMonthly, tenant.getMonthlyTokenLimit());

                throw new TokenBudgetExceededException(
                    "Monthly token budget exceeded",
                    currentMonthly,
                    tenant.getMonthlyTokenLimit().longValue()
                );
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TokenUsageResponse getUsageStatistics(LocalDate startDate, LocalDate endDate) {
        TenantId tenantId = TenantContextHolder.getTenantContext().getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId.getValue()));

        // Get usage for the specified period
        List<TokenUsage> usages = tokenUsageRepository.findByTenantIdAndUsageDateBetween(
            tenantId, startDate, endDate
        );

        // Calculate summary
        long totalTokens = usages.stream().mapToLong(TokenUsage::getTotalTokens).sum();
        long promptTokens = usages.stream().mapToLong(TokenUsage::getPromptTokens).sum();
        long completionTokens = usages.stream().mapToLong(TokenUsage::getCompletionTokens).sum();
        BigDecimal totalCost = usages.stream()
            .map(TokenUsage::getEstimatedCostUsd)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalRequests = usages.stream().mapToInt(TokenUsage::getRequestCount).sum();

        // Calculate remaining limits
        LocalDate today = LocalDate.now();
        TokenUsage todayUsage = tokenUsageRepository.findByTenantIdAndUsageDate(tenantId, today).orElse(null);
        long todayTokens = todayUsage != null ? todayUsage.getTotalTokens() : 0;

        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        long monthlyTokens = tokenUsageRepository.sumTokensByTenantIdAndDateRange(
            tenantId, startOfMonth, endOfMonth
        );

        return TokenUsageResponse.builder()
            .tenantId(tenantId.getValue())
            .period(TokenUsageResponse.PeriodInfo.builder()
                .start(startDate)
                .end(endDate)
                .build())
            .summary(TokenUsageResponse.UsageSummary.builder()
                .totalTokens(totalTokens)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .estimatedCost(totalCost)
                .requestCount(totalRequests)
                .build())
            .dailyBreakdown(usages.stream()
                .map(u -> TokenUsageResponse.DailyUsage.builder()
                    .date(u.getUsageDate())
                    .tokens(u.getTotalTokens())
                    .cost(u.getEstimatedCostUsd())
                    .requests(u.getRequestCount())
                    .build())
                .collect(Collectors.toList()))
            .limits(TokenUsageResponse.LimitInfo.builder()
                .dailyLimit(tenant.getDailyTokenLimit())
                .monthlyLimit(tenant.getMonthlyTokenLimit())
                .remainingToday(tenant.getDailyTokenLimit() != null ?
                    tenant.getDailyTokenLimit().longValue() - todayTokens : null)
                .remainingThisMonth(tenant.getMonthlyTokenLimit() != null ?
                    tenant.getMonthlyTokenLimit().longValue() - monthlyTokens : null)
                .build())
            .build();
    }
}
