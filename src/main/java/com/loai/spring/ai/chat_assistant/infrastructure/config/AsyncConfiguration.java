package com.loai.spring.ai.chat_assistant.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration for enabling asynchronous processing.
 * Used for audit logging to avoid blocking main request flow.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
}
