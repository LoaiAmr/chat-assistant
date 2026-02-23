package com.loai.spring.ai.chat_assistant.infrastructure.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiModerationApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAI API clients.
 * Sets up chat and moderation models with API key authentication.
 */
@Configuration
public class OpenAIConfiguration {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    /**
     * Creates OpenAI API client for chat completions.
     */
    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .build();
    }

    /**
     * Creates OpenAI chat model using builder pattern.
     * The builder provides default values for options, retry template, and observation registry.
     */
    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .build();
    }

    /**
     * Creates OpenAI Moderation API client.
     */
    @Bean
    public OpenAiModerationApi openAiModerationApi() {
        return OpenAiModerationApi.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .build();
    }

    /**
     * Creates OpenAI moderation model.
     */
    @Bean
    public OpenAiModerationModel openAiModerationModel(OpenAiModerationApi moderationApi) {
        return new OpenAiModerationModel(moderationApi);
    }
}
