package com.loai.spring.ai.chat_assistant.application.port.output;

import java.util.Optional;

/**
 * Secondary port for caching AI responses.
 * Abstracts the cache implementation (Caffeine, Redis, etc.)
 */
public interface CacheService {

    /**
     * Gets a cached AI response if available.
     *
     * @param cacheKey unique key for the request
     * @return cached response if present
     */
    Optional<String> getCachedResponse(String cacheKey);

    /**
     * Caches an AI response.
     *
     * @param cacheKey unique key for the request
     * @param response response to cache
     */
    void cacheResponse(String cacheKey, String response);

    /**
     * Generates a cache key from request parameters.
     *
     * @param content message content
     * @param model AI model name
     * @param temperature temperature parameter
     * @return cache key
     */
    String generateCacheKey(String content, String model, Double temperature);
}
