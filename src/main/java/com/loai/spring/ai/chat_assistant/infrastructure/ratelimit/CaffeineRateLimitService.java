package com.loai.spring.ai.chat_assistant.infrastructure.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.loai.spring.ai.chat_assistant.application.port.output.RateLimitService;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

import static com.loai.spring.ai.chat_assistant.application.port.output.RateLimitService.RateLimitStatus;

/**
 * Caffeine-based rate limiting service using sliding window algorithm.
 * Tracks request timestamps per tenant to enforce rate limits.
 */
@Service
@Slf4j
public class CaffeineRateLimitService implements RateLimitService {

    private final Cache<String, Queue<Instant>> requestTimestamps;
    private final int requestsPerMinute;
    private final int requestsPerHour;

    public CaffeineRateLimitService(
            @Value("${chat.rate-limit.requests-per-minute:60}") int requestsPerMinute,
            @Value("${chat.rate-limit.requests-per-hour:1000}") int requestsPerHour) {

        this.requestsPerMinute = requestsPerMinute;
        this.requestsPerHour = requestsPerHour;

        this.requestTimestamps = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofHours(1))
            .build();

        log.info("Initialized rate limiter: {} req/min, {} req/hour", requestsPerMinute, requestsPerHour);
    }

    @Override
    public boolean allowRequest(TenantId tenantId) {
        String key = tenantId.getValue().toString();
        Instant now = Instant.now();

        Queue<Instant> timestamps = requestTimestamps.get(key, k -> new LinkedList<>());

        // Remove timestamps older than 1 hour (outside sliding window)
        timestamps.removeIf(timestamp ->
            Duration.between(timestamp, now).toHours() >= 1);

        // Check hourly limit
        if (timestamps.size() >= requestsPerHour) {
            log.warn("Rate limit exceeded for tenant {}: {} requests in last hour",
                tenantId.getValue(), timestamps.size());
            return false;
        }

        // Check per-minute limit
        long requestsInLastMinute = timestamps.stream()
            .filter(timestamp -> Duration.between(timestamp, now).toMinutes() < 1)
            .count();

        if (requestsInLastMinute >= requestsPerMinute) {
            log.warn("Rate limit exceeded for tenant {}: {} requests in last minute",
                tenantId.getValue(), requestsInLastMinute);
            return false;
        }

        return true;
    }

    @Override
    public void recordRequest(TenantId tenantId) {
        String key = tenantId.getValue().toString();
        Instant now = Instant.now();

        Queue<Instant> timestamps = requestTimestamps.get(key, k -> new LinkedList<>());
        timestamps.add(now);

        log.debug("Recorded request for tenant {}. Total requests in window: {}",
            tenantId.getValue(), timestamps.size());
    }

    @Override
    public RateLimitStatus getStatus(TenantId tenantId) {
        String key = tenantId.getValue().toString();
        Instant now = Instant.now();

        Queue<Instant> timestamps = requestTimestamps.get(key, k -> new LinkedList<>());

        // Remove old timestamps
        timestamps.removeIf(timestamp ->
            Duration.between(timestamp, now).toHours() >= 1);

        int requestsInLastMinute = (int) timestamps.stream()
            .filter(timestamp -> Duration.between(timestamp, now).toMinutes() < 1)
            .count();

        int requestsRemaining = Math.max(0, requestsPerMinute - requestsInLastMinute);

        // Calculate reset time (next minute boundary)
        long secondsUntilReset = 60 - (now.getEpochSecond() % 60);

        return new RateLimitStatus(requestsRemaining, secondsUntilReset);
    }
}
