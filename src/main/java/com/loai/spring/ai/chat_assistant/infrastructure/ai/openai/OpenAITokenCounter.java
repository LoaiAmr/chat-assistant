package com.loai.spring.ai.chat_assistant.infrastructure.ai.openai;

import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;
import org.springframework.stereotype.Component;

/**
 * Utility for estimating OpenAI token counts.
 * Uses approximate calculation: ~4 characters per token for English text.
 *
 * Note: This is an approximation. For exact counts, use OpenAI's tiktoken library.
 * The actual count may vary by ±20% depending on the text content.
 */
@Component
public class OpenAITokenCounter {

    private static final double CHARS_PER_TOKEN = 4.0;
    private static final int MESSAGE_OVERHEAD_TOKENS = 4; // Overhead per message for formatting

    /**
     * Estimates token count for a given text.
     */
    public TokenCount estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return TokenCount.of(0);
        }

        int estimatedTokens = (int) Math.ceil(text.length() / CHARS_PER_TOKEN);
        return TokenCount.of(estimatedTokens);
    }

    /**
     * Estimates token count for a message including formatting overhead.
     */
    public TokenCount estimateMessageTokens(String content) {
        TokenCount contentTokens = estimateTokens(content);
        return TokenCount.of(contentTokens.getValue() + MESSAGE_OVERHEAD_TOKENS);
    }

    /**
     * Calculates the exact token count from actual usage metadata.
     */
    public TokenCount fromUsageMetadata(Integer tokens) {
        return TokenCount.of(tokens != null ? tokens : 0);
    }
}
