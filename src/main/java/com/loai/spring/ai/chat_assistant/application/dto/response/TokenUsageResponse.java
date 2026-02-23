package com.loai.spring.ai.chat_assistant.application.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for token usage statistics.
 */
@Value
@Builder
@Jacksonized
public class TokenUsageResponse {

    UUID tenantId;
    PeriodInfo period;
    UsageSummary summary;
    List<DailyUsage> dailyBreakdown;
    LimitInfo limits;

    @Value
    @Builder
    @Jacksonized
    public static class PeriodInfo {
        LocalDate start;
        LocalDate end;
    }

    @Value
    @Builder
    @Jacksonized
    public static class UsageSummary {
        long totalTokens;
        long promptTokens;
        long completionTokens;
        BigDecimal estimatedCost;
        int requestCount;
    }

    @Value
    @Builder
    @Jacksonized
    public static class DailyUsage {
        LocalDate date;
        long tokens;
        BigDecimal cost;
        int requests;
    }

    @Value
    @Builder
    @Jacksonized
    public static class LimitInfo {
        Integer dailyLimit;
        Integer monthlyLimit;
        Long remainingToday;
        Long remainingThisMonth;
    }
}
