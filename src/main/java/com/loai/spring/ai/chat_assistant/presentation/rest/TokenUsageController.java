package com.loai.spring.ai.chat_assistant.presentation.rest;

import com.loai.spring.ai.chat_assistant.application.dto.response.TokenUsageResponse;
import com.loai.spring.ai.chat_assistant.application.port.input.TokenUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * REST controller for token usage statistics.
 * Provides endpoints to query current token usage for the authenticated tenant.
 */
@RestController
@RequestMapping("/v1/tenants/usage")
@RequiredArgsConstructor
@Slf4j
public class TokenUsageController {

    private final TokenUsageService tokenUsageService;

    /**
     * Retrieves token usage statistics for the current tenant.
     * Includes daily and monthly breakdowns with cost estimates.
     *
     * @param startDate Optional start date (defaults to first day of current month)
     * @param endDate Optional end date (defaults to last day of current month)
     * @return Token usage statistics
     */
    @GetMapping
    public ResponseEntity<TokenUsageResponse> getUsageStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate today = LocalDate.now();
        LocalDate effectiveStartDate = startDate != null ? startDate : today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate effectiveEndDate = endDate != null ? endDate : today.with(TemporalAdjusters.lastDayOfMonth());

        log.debug("Retrieving usage statistics from {} to {}", effectiveStartDate, effectiveEndDate);

        TokenUsageResponse usageStats = tokenUsageService.getUsageStatistics(effectiveStartDate, effectiveEndDate);

        log.info("Usage statistics retrieved: total tokens={}",
            usageStats.getSummary().getTotalTokens());

        return ResponseEntity.ok(usageStats);
    }
}
