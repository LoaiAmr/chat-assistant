package com.loai.spring.ai.chat_assistant.application.port.output;

import com.loai.spring.ai.chat_assistant.domain.model.Message;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;

import java.util.List;

/**
 * Secondary port for AI provider abstraction.
 * This allows swapping between different AI providers (OpenAI, Anthropic, etc.)
 * following the Open-Closed Principle.
 */
public interface AIProvider {

    /**
     * Generates an AI response based on conversation history.
     *
     * @param messages conversation history (system, user, assistant messages)
     * @param temperature creativity parameter (0.0 - 2.0)
     * @param maxTokens maximum tokens for the response
     * @return AI-generated response message
     */
    AIResponse generateResponse(List<Message> messages, Double temperature, Integer maxTokens);

    /**
     * Estimates the number of tokens in a text string.
     * Used for budget checks before making API calls.
     *
     * @param content text content to estimate
     * @return estimated token count
     */
    TokenCount estimateTokens(String content);

    /**
     * Gets the provider name (e.g., "OpenAI", "Anthropic").
     *
     * @return provider name
     */
    String getProviderName();

    /**
     * Response object from the AI provider.
     */
    record AIResponse(
        String content,
        TokenCount promptTokens,
        TokenCount completionTokens,
        String model
    ) {}
}
