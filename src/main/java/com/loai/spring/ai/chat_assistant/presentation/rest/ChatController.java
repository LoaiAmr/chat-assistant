package com.loai.spring.ai.chat_assistant.presentation.rest;

import com.loai.spring.ai.chat_assistant.application.dto.request.ChatRequest;
import com.loai.spring.ai.chat_assistant.application.dto.response.ChatResponse;
import com.loai.spring.ai.chat_assistant.application.port.input.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for chat operations.
 * Handles sending messages and receiving AI responses.
 */
@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * Sends a chat message and receives AI response.
     *
     * @param request Chat request with message and optional conversation ID
     * @return Chat response with AI message and metadata
     */
    @PostMapping
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request for conversation: {}",
            request.getConversationId() != null ? request.getConversationId() : "new");

        ChatResponse response = chatService.sendMessage(request);

        log.info("Chat response generated successfully. Conversation: {}, Tokens: {}",
            response.getConversationId(), response.getTokensUsed());

        return ResponseEntity.ok(response);
    }
}
