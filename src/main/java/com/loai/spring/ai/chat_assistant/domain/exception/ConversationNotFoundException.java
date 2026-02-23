package com.loai.spring.ai.chat_assistant.domain.exception;

import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;

/**
 * Exception thrown when a conversation cannot be found.
 */
public class ConversationNotFoundException extends DomainException {

    public ConversationNotFoundException(ConversationId conversationId) {
        super("Conversation not found with ID: " + conversationId);
    }

    public ConversationNotFoundException(String message) {
        super(message);
    }
}
