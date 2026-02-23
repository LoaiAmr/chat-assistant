package com.loai.spring.ai.chat_assistant.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.loai.spring.ai.chat_assistant.application.port.output.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Caffeine-based cache implementation for AI responses.
 * Caches responses to reduce OpenAI API calls and costs.
 */
@Service
@Slf4j
public class CaffeineCacheService implements CacheService {

    private final Cache<String, String> responseCache;

    public CaffeineCacheService() {
        this.responseCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .recordStats()
            .build();

        log.info("Initialized Caffeine cache with max size 1000, TTL 10 minutes");
    }

    @Override
    public Optional<String> getCachedResponse(String cacheKey) {
        String hashedKey = hashKey(cacheKey);
        String cachedValue = responseCache.getIfPresent(hashedKey);

        if (cachedValue != null) {
            log.debug("Cache hit for key: {}", hashedKey.substring(0, 8));
            return Optional.of(cachedValue);
        }

        log.debug("Cache miss for key: {}", hashedKey.substring(0, 8));
        return Optional.empty();
    }

    @Override
    public void cacheResponse(String cacheKey, String response) {
        String hashedKey = hashKey(cacheKey);
        responseCache.put(hashedKey, response);
        log.debug("Cached value for key: {}", hashedKey.substring(0, 8));
    }

    @Override
    public String generateCacheKey(String content, String model, Double temperature) {
        return String.format("%s:%s:%.2f", content, model, temperature);
    }

    public void evict(String key) {
        String hashedKey = hashKey(key);
        responseCache.invalidate(hashedKey);
        log.debug("Evicted cache entry for key: {}", hashedKey.substring(0, 8));
    }

    public void clear() {
        responseCache.invalidateAll();
        log.info("Cleared all cache entries");
    }

    /**
     * Generates MD5 hash of the cache key to ensure consistent key length.
     */
    private String hashKey(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            log.warn("MD5 algorithm not available, using raw key");
            return key;
        }
    }

    /**
     * Returns cache statistics for monitoring.
     */
    public String getCacheStats() {
        var stats = responseCache.stats();
        return String.format("Cache stats - Hits: %d, Misses: %d, Hit rate: %.2f%%",
            stats.hitCount(), stats.missCount(), stats.hitRate() * 100);
    }
}
