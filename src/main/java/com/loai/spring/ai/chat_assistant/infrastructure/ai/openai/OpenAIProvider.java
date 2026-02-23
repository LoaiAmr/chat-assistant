package com.loai.spring.ai.chat_assistant.infrastructure.ai.openai;

import com.loai.spring.ai.chat_assistant.application.exception.AIProviderException;
import com.loai.spring.ai.chat_assistant.application.port.output.AIProvider;
import com.loai.spring.ai.chat_assistant.domain.model.Message;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OpenAI implementation of AIProvider using Spring AI.
 * Includes circuit breaker for resilience against OpenAI API failures.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIProvider implements AIProvider {

    private final ChatModel chatModel;
    private final MessageMapper messageMapper;
    private final OpenAITokenCounter tokenCounter;

    @Override
    @CircuitBreaker(name = "openai", fallbackMethod = "generateResponseFallback")
    public AIResponse generateResponse(List<Message> messages, Double temperature, Integer maxTokens) {
        log.debug("Generating response for {} messages with temperature={}, maxTokens={}",
            messages.size(), temperature, maxTokens);

        try {
            // Convert domain messages to Spring AI format
            List<org.springframework.ai.chat.messages.Message> springAIMessages =
                messageMapper.toSpringAIMessages(messages);

            // Build chat options
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_4_O_MINI.getValue())
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

            // Create prompt and call OpenAI
            Prompt prompt = new Prompt(springAIMessages, options);
            ChatResponse response = chatModel.call(prompt);

            // Extract response content
            String content = response.getResult().getOutput().getText();

            // Extract token usage from metadata
            var usage = response.getMetadata().getUsage();
            TokenCount promptTokens = tokenCounter.fromUsageMetadata(
                usage != null ? (int) usage.getPromptTokens() : 0
            );
            TokenCount completionTokens = tokenCounter.fromUsageMetadata(
                usage != null ? (int) usage.getTotalTokens() - (int) usage.getPromptTokens() : 0
            );

            String model = response.getMetadata() != null ? response.getMetadata().getModel() : "unknown";

            log.info("OpenAI response generated successfully. Model={}, PromptTokens={}, CompletionTokens={}",
                model, promptTokens.getValue(), completionTokens.getValue());

            return new AIResponse(content, promptTokens, completionTokens, model);

        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage(), e);
            throw new AIProviderException("Failed to generate response from OpenAI: " + e.getMessage(), "OpenAI", e);
        }
    }

    /**
     * Fallback method when circuit breaker opens.
     */
    @SuppressWarnings("unused")
    private AIResponse generateResponseFallback(List<Message> messages, Double temperature,
                                                Integer maxTokens, Exception e) {
        log.error("Circuit breaker activated. OpenAI service unavailable: {}", e.getMessage());
        throw new AIProviderException(
            "AI service is temporarily unavailable. Please try again later.", "OpenAI", e);
    }

    @Override
    public TokenCount estimateTokens(String content) {
        return tokenCounter.estimateTokens(content);
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
    }
}
