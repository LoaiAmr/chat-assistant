package com.loai.spring.ai.chat_assistant.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loai.spring.ai.chat_assistant.application.dto.request.ChatRequest;
import com.loai.spring.ai.chat_assistant.application.dto.response.ChatResponse;
import com.loai.spring.ai.chat_assistant.application.dto.response.MessageResponse;
import com.loai.spring.ai.chat_assistant.application.exception.*;
import com.loai.spring.ai.chat_assistant.application.port.input.ChatService;
import com.loai.spring.ai.chat_assistant.domain.exception.ConversationNotFoundException;
import com.loai.spring.ai.chat_assistant.domain.exception.TenantNotFoundException;
import com.loai.spring.ai.chat_assistant.domain.model.vo.MessageRole;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    // ========== Happy Path Tests ==========

    @Test
    void shouldReturnChatResponse_whenValidRequestWithoutConversationId() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Hello, how are you?")
                .build();

        ChatResponse response = ChatResponse.builder()
                .conversationId(UUID.randomUUID())
                .message(MessageResponse.builder()
                        .id(UUID.randomUUID())
                        .role(MessageRole.ASSISTANT)
                        .content("I'm doing well, thank you!")
                        .createdAt(Instant.now())
                        .build())
                .tokensUsed(ChatResponse.TokenUsageInfo.builder()
                        .prompt(20)
                        .completion(50)
                        .total(70)
                        .build())
                .moderationPassed(true)
                .build();

        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").exists())
                .andExpect(jsonPath("$.message.content").value("I'm doing well, thank you!"))
                .andExpect(jsonPath("$.message.role").value("ASSISTANT"))
                .andExpect(jsonPath("$.tokensUsed.total").value(70))
                .andExpect(jsonPath("$.moderationPassed").value(true));

        verify(chatService, times(1)).sendMessage(any(ChatRequest.class));
    }

    @Test
    void shouldReturnChatResponse_whenValidRequestWithExistingConversationId() throws Exception {
        // Given
        UUID existingConversationId = UUID.randomUUID();
        ChatRequest request = ChatRequest.builder()
                .conversationId(existingConversationId.toString())
                .message("What's the weather like?")
                .build();

        ChatResponse response = ChatResponse.builder()
                .conversationId(existingConversationId)
                .message(MessageResponse.builder()
                        .id(UUID.randomUUID())
                        .role(MessageRole.ASSISTANT)
                        .content("I don't have access to real-time weather data.")
                        .createdAt(Instant.now())
                        .build())
                .tokensUsed(ChatResponse.TokenUsageInfo.builder()
                        .prompt(15)
                        .completion(60)
                        .total(75)
                        .build())
                .moderationPassed(true)
                .build();

        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(existingConversationId.toString()))
                .andExpect(jsonPath("$.message.content").exists())
                .andExpect(jsonPath("$.tokensUsed.total").value(75));

        verify(chatService, times(1)).sendMessage(any(ChatRequest.class));
    }

    @Test
    void shouldReturn200_whenValidRequestWithAllOptionalFields() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .systemPrompt("You are a helpful assistant")
                .temperature(0.7)
                .maxTokens(500)
                .build();

        ChatResponse response = ChatResponse.builder()
                .conversationId(UUID.randomUUID())
                .message(MessageResponse.builder()
                        .id(UUID.randomUUID())
                        .role(MessageRole.ASSISTANT)
                        .content("Response")
                        .createdAt(Instant.now())
                        .build())
                .tokensUsed(ChatResponse.TokenUsageInfo.builder()
                        .prompt(30)
                        .completion(40)
                        .total(70)
                        .build())
                .moderationPassed(true)
                .build();

        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").exists())
                .andExpect(jsonPath("$.tokensUsed.total").value(70));
    }

    @Test
    void shouldIncludeTokenUsageInResponse_whenResponseReturned() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .build();

        ChatResponse response = ChatResponse.builder()
                .conversationId(UUID.randomUUID())
                .message(MessageResponse.builder()
                        .id(UUID.randomUUID())
                        .role(MessageRole.ASSISTANT)
                        .content("Response")
                        .createdAt(Instant.now())
                        .build())
                .tokensUsed(ChatResponse.TokenUsageInfo.builder()
                        .prompt(25)
                        .completion(75)
                        .total(100)
                        .build())
                .moderationPassed(true)
                .build();

        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokensUsed.prompt").value(25))
                .andExpect(jsonPath("$.tokensUsed.completion").value(75))
                .andExpect(jsonPath("$.tokensUsed.total").value(100));
    }

    // ========== Validation Error Tests ==========

    @Test
    void shouldReturn400_whenMessageIsBlank() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("   ")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.message").exists());

        verify(chatService, never()).sendMessage(any());
    }

    @Test
    void shouldReturn400_whenMessageIsNull() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message(null)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.message").exists());

        verify(chatService, never()).sendMessage(any());
    }

    @Test
    void shouldReturn400_whenMessageExceedsMaxLength() throws Exception {
        // Given
        String longMessage = "a".repeat(10001);
        ChatRequest request = ChatRequest.builder()
                .message(longMessage)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.message").exists());

        verify(chatService, never()).sendMessage(any());
    }

    @Test
    void shouldReturn400_whenInvalidConversationIdFormat() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .conversationId("invalid-uuid-format")
                .message("Test message")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.conversationId").exists());

        verify(chatService, never()).sendMessage(any());
    }

    @Test
    void shouldReturn400_whenTemperatureOutOfRangeLow() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .temperature(-0.1)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.temperature").exists());

        verify(chatService, never()).sendMessage(any());
    }

    @Test
    void shouldReturn400_whenTemperatureOutOfRangeHigh() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .temperature(2.1)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.temperature").exists());

        verify(chatService, never()).sendMessage(any());
    }

    @Test
    void shouldReturn400_whenMaxTokensNegative() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .maxTokens(0)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.maxTokens").exists());

        verify(chatService, never()).sendMessage(any());
    }

    @Test
    void shouldReturn400_whenMaxTokensExceedsLimit() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .maxTokens(5000)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.maxTokens").exists());

        verify(chatService, never()).sendMessage(any());
    }

    @Test
    void shouldReturn400_whenSystemPromptExceedsMaxLength() throws Exception {
        // Given
        String longSystemPrompt = "a".repeat(2001);
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .systemPrompt(longSystemPrompt)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.systemPrompt").exists());

        verify(chatService, never()).sendMessage(any());
    }

    // ========== Business Logic Error Tests (via GlobalExceptionHandler) ==========

    @Test
    void shouldReturn401_whenTenantNotFound() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .build();

        when(chatService.sendMessage(any(ChatRequest.class)))
                .thenThrow(new TenantNotFoundException("Tenant not found"));

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Tenant not found"));
    }

    @Test
    void shouldReturn404_whenConversationNotFound() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .conversationId(UUID.randomUUID().toString())
                .message("Test message")
                .build();

        when(chatService.sendMessage(any(ChatRequest.class)))
                .thenThrow(new ConversationNotFoundException("Conversation not found"));

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Conversation not found"));
    }

    @Test
    void shouldReturn429_whenRateLimitExceeded() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .build();

        when(chatService.sendMessage(any(ChatRequest.class)))
                .thenThrow(new RateLimitExceededException(TenantId.generate(), 60));

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Rate Limit Exceeded"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void shouldReturn402_whenTokenBudgetExceeded() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .build();

        when(chatService.sendMessage(any(ChatRequest.class)))
                .thenThrow(new TokenBudgetExceededException("Token budget exceeded", 15000, 10000));

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.error").value("Token Budget Exceeded"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400_whenModerationFailed() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Inappropriate content")
                .build();

        when(chatService.sendMessage(any(ChatRequest.class)))
                .thenThrow(new ModerationFailedException("Content moderation failed", List.of("hate", "violence")));

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Content Moderation Failed"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn503_whenAIProviderUnavailable() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Test message")
                .build();

        when(chatService.sendMessage(any(ChatRequest.class)))
                .thenThrow(new AIProviderException("AI service unavailable", "OpenAI"));

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("AI Service Unavailable"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ========== Edge Cases ==========

    @Test
    void shouldHandleMinimalRequest_whenOnlyRequiredFields() throws Exception {
        // Given
        ChatRequest request = ChatRequest.builder()
                .message("Hi")
                .build();

        ChatResponse response = ChatResponse.builder()
                .conversationId(UUID.randomUUID())
                .message(MessageResponse.builder()
                        .id(UUID.randomUUID())
                        .role(MessageRole.ASSISTANT)
                        .content("Hello!")
                        .createdAt(Instant.now())
                        .build())
                .tokensUsed(ChatResponse.TokenUsageInfo.builder()
                        .prompt(5)
                        .completion(10)
                        .total(15)
                        .build())
                .moderationPassed(true)
                .build();

        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").exists())
                .andExpect(jsonPath("$.message.content").value("Hello!"));
    }
}
