package com.loai.spring.ai.chat_assistant.domain.model.vo;

/**
 * Enum representing the role of a message in a conversation.
 * - SYSTEM: System prompts that guide the AI's behavior
 * - USER: Messages from the user
 * - ASSISTANT: Responses from the AI assistant
 */
public enum MessageRole {
    SYSTEM,
    USER,
    ASSISTANT
}
