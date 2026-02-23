package com.loai.spring.ai.chat_assistant.presentation.exception;

import com.loai.spring.ai.chat_assistant.application.exception.*;
import com.loai.spring.ai.chat_assistant.domain.exception.ConversationNotFoundException;
import com.loai.spring.ai.chat_assistant.domain.exception.MessageValidationException;
import com.loai.spring.ai.chat_assistant.domain.exception.TenantNotFoundException;
import com.loai.spring.ai.chat_assistant.domain.exception.TenantInactiveException;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {
        GlobalExceptionHandler.class,
        GlobalExceptionHandlerTest.TestController.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== Test Controller ==========

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/tenant-not-found")
        void throwTenantNotFoundException() {
            throw new TenantNotFoundException("Tenant with ID xyz not found");
        }

        @GetMapping("/conversation-not-found")
        void throwConversationNotFoundException() {
            throw new ConversationNotFoundException("Conversation with ID abc not found");
        }

        @GetMapping("/rate-limit-exceeded")
        void throwRateLimitExceededException() {
            throw new RateLimitExceededException(TenantId.generate(), 60);
        }

        @GetMapping("/token-budget-exceeded")
        void throwTokenBudgetExceededException() {
            throw new TokenBudgetExceededException("Daily token budget exceeded", 15000, 10000);
        }

        @GetMapping("/moderation-failed")
        void throwModerationFailedException() {
            throw new ModerationFailedException("Content flagged for hate speech", List.of("hate", "violence"));
        }

        @GetMapping("/ai-provider-error")
        void throwAIProviderException() {
            throw new AIProviderException("OpenAI API is unavailable", "OpenAI");
        }

        @GetMapping("/message-validation-error")
        void throwMessageValidationException() {
            throw new MessageValidationException("Message content is invalid");
        }

        @GetMapping("/tenant-inactive")
        void throwTenantInactiveException() {
            throw new TenantInactiveException(TenantId.generate());
        }

        @GetMapping("/illegal-argument")
        void throwIllegalArgumentException() {
            throw new IllegalArgumentException("Invalid argument provided");
        }

        @GetMapping("/illegal-state")
        void throwIllegalStateException() {
            throw new IllegalStateException("System is in an illegal state");
        }

        @GetMapping("/generic-exception")
        void throwGenericException() throws Exception {
            throw new Exception("Unexpected error occurred");
        }

        @PostMapping("/validation-test")
        void validationTest(@Valid @RequestBody TestRequest request) {
            // Method for testing validation
        }
    }

    @Value
    static class TestRequest {
        @NotBlank(message = "Field cannot be blank")
        String field;
    }

    // ========== Validation Exception Tests ==========

    @Test
    void shouldReturn400_whenMethodArgumentNotValid() throws Exception {
        // Given
        String invalidRequest = "{\"field\": \"\"}";

        // When & Then
        mockMvc.perform(post("/test/validation-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Input validation failed"));
    }

    @Test
    void shouldIncludeFieldErrors_whenValidationFails() throws Exception {
        // Given
        String invalidRequest = "{\"field\": \"   \"}";

        // When & Then
        mockMvc.perform(post("/test/validation-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details.field").value("Field cannot be blank"));
    }

    @Test
    void shouldIncludeTimestampAndPath_whenValidationFails() throws Exception {
        // Given
        String invalidRequest = "{\"field\": \"\"}";

        // When & Then
        mockMvc.perform(post("/test/validation-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    // ========== Domain Exception Tests ==========

    @Test
    void shouldReturn401_whenTenantNotFoundException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/tenant-not-found"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Tenant with ID xyz not found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void shouldReturn404_whenConversationNotFoundException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/conversation-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Conversation with ID abc not found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void shouldReturn400_whenMessageValidationException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/message-validation-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Invalid Message"))
                .andExpect(jsonPath("$.message").value("Message content is invalid"));
    }

    @Test
    void shouldReturn400_whenTenantInactiveException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/tenant-inactive"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Domain Error"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ========== Application Exception Tests ==========

    @Test
    void shouldReturn429_whenRateLimitExceededException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/rate-limit-exceeded"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(HttpStatus.TOO_MANY_REQUESTS.value()))
                .andExpect(jsonPath("$.error").value("Rate Limit Exceeded"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("Retry-After", "60"));
    }

    @Test
    void shouldIncludeRetryAfterHeader_whenRateLimitExceeded() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/rate-limit-exceeded"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("Retry-After", "60"));
    }

    @Test
    void shouldReturn402_whenTokenBudgetExceededException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/token-budget-exceeded"))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.status").value(HttpStatus.PAYMENT_REQUIRED.value()))
                .andExpect(jsonPath("$.error").value("Token Budget Exceeded"))
                .andExpect(jsonPath("$.message").value("Daily token budget exceeded"));
    }

    @Test
    void shouldReturn400_whenModerationFailedException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/moderation-failed"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Content Moderation Failed"))
                .andExpect(jsonPath("$.message").value("Content flagged for hate speech"));
    }

    @Test
    void shouldReturn503_whenAIProviderException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/ai-provider-error"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
                .andExpect(jsonPath("$.error").value("AI Service Unavailable"))
                .andExpect(jsonPath("$.message").value("OpenAI API is unavailable"));
    }

    // ========== System Exception Tests ==========

    @Test
    void shouldReturn400_whenIllegalArgumentException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Invalid Argument"))
                .andExpect(jsonPath("$.message").value("Invalid argument provided"));
    }

    @Test
    void shouldReturn500_whenIllegalStateException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("System is in an illegal state"));
    }

    @Test
    void shouldReturn500_whenGenericException() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void shouldHideSensitiveInfo_whenGenericExceptionOccurs() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    // ========== Error Response Structure Tests ==========

    @Test
    void shouldIncludeAllFields_whenErrorResponseReturned() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/tenant-not-found"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }
}
