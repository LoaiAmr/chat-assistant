package com.loai.spring.ai.chat_assistant.presentation.rest;

import com.loai.spring.ai.chat_assistant.application.dto.response.ConversationResponse;
import com.loai.spring.ai.chat_assistant.application.port.input.ConversationService;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for conversation management.
 * Handles listing, retrieving, and deleting conversations.
 */
@RestController
@RequestMapping("/v1/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Lists all conversations for the current tenant with pagination.
     *
     * @param pageable Pagination parameters
     * @return Page of conversation summaries
     */
    @GetMapping
    public ResponseEntity<Page<ConversationResponse>> listConversations(
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {

        log.debug("Listing conversations with pagination: {}", pageable);

        Page<ConversationResponse> conversations = conversationService.listConversations(pageable);

        log.info("Retrieved {} conversations", conversations.getNumberOfElements());

        return ResponseEntity.ok(conversations);
    }

    /**
     * Retrieves a specific conversation with full message history.
     *
     * @param id Conversation ID
     * @return Conversation with all messages
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getConversation(@PathVariable String id) {
        log.debug("Retrieving conversation: {}", id);

        ConversationId conversationId = ConversationId.of(UUID.fromString(id));
        ConversationResponse conversation = conversationService.getConversation(conversationId);

        log.info("Retrieved conversation {} with {} messages",
            id, conversation.getMessages() != null ? conversation.getMessages().size() : 0);

        return ResponseEntity.ok(conversation);
    }

    /**
     * Deletes a conversation and all its messages.
     *
     * @param id Conversation ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String id) {
        log.info("Deleting conversation: {}", id);

        ConversationId conversationId = ConversationId.of(UUID.fromString(id));
        conversationService.deleteConversation(conversationId);

        log.info("Conversation {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }
}
