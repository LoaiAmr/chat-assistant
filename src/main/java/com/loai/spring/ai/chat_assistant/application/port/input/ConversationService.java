package com.loai.spring.ai.chat_assistant.application.port.input;

import com.loai.spring.ai.chat_assistant.application.dto.response.ConversationResponse;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Primary port for conversation management operations.
 * Handles listing, retrieving, and deleting conversations.
 */
public interface ConversationService {

    /**
     * Lists all conversations for the current tenant with pagination.
     *
     * @param pageable pagination parameters
     * @return page of conversation summaries
     */
    Page<ConversationResponse> listConversations(Pageable pageable);

    /**
     * Gets a single conversation with full message history.
     *
     * @param conversationId the conversation identifier
     * @return conversation with all messages
     */
    ConversationResponse getConversation(ConversationId conversationId);

    /**
     * Deletes a conversation and all its messages.
     *
     * @param conversationId the conversation identifier
     */
    void deleteConversation(ConversationId conversationId);
}
