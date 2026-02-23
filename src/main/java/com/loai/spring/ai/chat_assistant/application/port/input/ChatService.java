package com.loai.spring.ai.chat_assistant.application.port.input;

import com.loai.spring.ai.chat_assistant.application.dto.request.ChatRequest;
import com.loai.spring.ai.chat_assistant.application.dto.response.ChatResponse;

/**
 * Primary port for chat operations.
 * This is the main interface for sending chat messages and receiving AI responses.
 */
public interface ChatService {

    /**
     * Sends a chat message and receives an AI-generated response.
     * This orchestrates the entire flow: validation, moderation, AI call, persistence, audit.
     *
     * @param request the chat request containing message and optional conversation context
     * @return the chat response with AI-generated message
     */
    ChatResponse sendMessage(ChatRequest request);
}
