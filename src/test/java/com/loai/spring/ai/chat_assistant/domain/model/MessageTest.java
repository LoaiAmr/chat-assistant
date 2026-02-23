package com.loai.spring.ai.chat_assistant.domain.model;

import com.loai.spring.ai.chat_assistant.domain.model.vo.MessageRole;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ModerationResult;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.loai.spring.ai.chat_assistant.testutil.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;

class MessageTest {

    // ========== Token Calculation Tests ==========

    @Test
    void shouldCalculateTotalTokens_whenBothTokensProvided() {
        // Given
        Message message = aUserMessage()
                .promptTokens(tokenCount(100))
                .completionTokens(tokenCount(50))
                .build();

        // When
        TokenCount totalTokens = message.getTotalTokens();

        // Then
        assertThat(totalTokens.getValue()).isEqualTo(150);
    }

    @Test
    void shouldHandleZeroTokens_whenPromptTokensNull() {
        // Given
        Message message = aUserMessage()
                .promptTokens(null)
                .completionTokens(tokenCount(50))
                .build();

        // When
        TokenCount totalTokens = message.getTotalTokens();

        // Then
        assertThat(totalTokens.getValue()).isEqualTo(50);
    }

    @Test
    void shouldHandleZeroTokens_whenCompletionTokensNull() {
        // Given
        Message message = aUserMessage()
                .promptTokens(tokenCount(100))
                .completionTokens(null)
                .build();

        // When
        TokenCount totalTokens = message.getTotalTokens();

        // Then
        assertThat(totalTokens.getValue()).isEqualTo(100);
    }

    @Test
    void shouldHandleZeroTokens_whenBothTokensNull() {
        // Given
        Message message = aUserMessage()
                .promptTokens(null)
                .completionTokens(null)
                .build();

        // When
        TokenCount totalTokens = message.getTotalTokens();

        // Then
        assertThat(totalTokens.getValue()).isEqualTo(0);
    }

    @Test
    void shouldCalculateZero_whenBothTokensAreZero() {
        // Given
        Message message = aUserMessage()
                .promptTokens(zeroTokens())
                .completionTokens(zeroTokens())
                .build();

        // When
        TokenCount totalTokens = message.getTotalTokens();

        // Then
        assertThat(totalTokens.getValue()).isEqualTo(0);
    }

    // ========== Moderation Checks Tests ==========

    @Test
    void shouldReturnTrue_whenModeratedWithApprovedStatus() {
        // Given
        Message message = aUserMessage()
                .moderationResult(ModerationResult.approved())
                .build();

        // When
        boolean isModerated = message.isModerated();

        // Then
        assertThat(isModerated).isTrue();
    }

    @Test
    void shouldReturnFalse_whenModerationResultNull() {
        // Given
        Message message = aUserMessage()
                .moderationResult(null)
                .build();

        // When
        boolean isModerated = message.isModerated();

        // Then
        assertThat(isModerated).isFalse();
    }

    @Test
    void shouldReturnFalse_whenModerationNotApproved() {
        // Given
        Message message = aUserMessage()
                .moderationResult(ModerationResult.rejected(List.of("hate")))
                .build();

        // When
        boolean isModerated = message.isModerated();

        // Then
        assertThat(isModerated).isFalse();
    }

    @Test
    void shouldReturnFalse_whenModerationPending() {
        // Given
        Message message = aPendingModerationMessage().build();

        // When
        boolean isModerated = message.isModerated();

        // Then
        assertThat(isModerated).isFalse();
    }

    @Test
    void shouldReturnFalse_whenModerationFlagged() {
        // Given
        Message message = aUserMessage()
                .moderationResult(ModerationResult.flagged(List.of("violence")))
                .build();

        // When
        boolean isModerated = message.isModerated();

        // Then
        assertThat(isModerated).isFalse();
    }

    // ========== Role Checks Tests ==========

    @Test
    void shouldReturnTrue_whenIsUserMessageForUserRole() {
        // Given
        Message message = aUserMessage()
                .role(MessageRole.USER)
                .build();

        // When & Then
        assertThat(message.isUserMessage()).isTrue();
        assertThat(message.isAssistantMessage()).isFalse();
        assertThat(message.isSystemMessage()).isFalse();
    }

    @Test
    void shouldReturnTrue_whenIsAssistantMessageForAssistantRole() {
        // Given
        Message message = anAssistantMessage()
                .role(MessageRole.ASSISTANT)
                .build();

        // When & Then
        assertThat(message.isAssistantMessage()).isTrue();
        assertThat(message.isUserMessage()).isFalse();
        assertThat(message.isSystemMessage()).isFalse();
    }

    @Test
    void shouldReturnTrue_whenIsSystemMessageForSystemRole() {
        // Given
        Message message = aSystemMessage()
                .role(MessageRole.SYSTEM)
                .build();

        // When & Then
        assertThat(message.isSystemMessage()).isTrue();
        assertThat(message.isUserMessage()).isFalse();
        assertThat(message.isAssistantMessage()).isFalse();
    }

    @Test
    void shouldReturnFalse_whenIsUserMessageForOtherRoles() {
        // Given
        Message assistantMessage = anAssistantMessage().build();
        Message systemMessage = aSystemMessage().build();

        // When & Then
        assertThat(assistantMessage.isUserMessage()).isFalse();
        assertThat(systemMessage.isUserMessage()).isFalse();
    }

    @Test
    void shouldReturnFalse_whenIsAssistantMessageForOtherRoles() {
        // Given
        Message userMessage = aUserMessage().build();
        Message systemMessage = aSystemMessage().build();

        // When & Then
        assertThat(userMessage.isAssistantMessage()).isFalse();
        assertThat(systemMessage.isAssistantMessage()).isFalse();
    }

    @Test
    void shouldReturnFalse_whenIsSystemMessageForOtherRoles() {
        // Given
        Message userMessage = aUserMessage().build();
        Message assistantMessage = anAssistantMessage().build();

        // When & Then
        assertThat(userMessage.isSystemMessage()).isFalse();
        assertThat(assistantMessage.isSystemMessage()).isFalse();
    }

    // ========== Immutability Tests ==========

    @Test
    void shouldCreateNewInstance_whenWithMethodUsed() {
        // Given
        Message original = aUserMessage().build();

        // When
        Message modified = original.withContent(messageContent("Modified content"));

        // Then - Verify immutability
        assertThat(modified).isNotSameAs(original);
        assertThat(modified.getContent().toString()).isEqualTo("Modified content");
        assertThat(original.getContent().toString()).isEqualTo("Test user message");
    }

    @Test
    void shouldNotModifyOriginal_whenWithMethodUsed() {
        // Given
        Message original = aUserMessage()
                .role(MessageRole.USER)
                .build();

        // When
        Message modified = original.withRole(MessageRole.ASSISTANT);

        // Then
        assertThat(original.getRole()).isEqualTo(MessageRole.USER);
        assertThat(modified.getRole()).isEqualTo(MessageRole.ASSISTANT);
    }

    // ========== Builder Tests ==========

    @Test
    void shouldCreateMessage_whenBuilderUsed() {
        // When
        Message message = aUserMessage().build();

        // Then
        assertThat(message.getId()).isNotNull();
        assertThat(message.getConversationId()).isNotNull();
        assertThat(message.getRole()).isEqualTo(MessageRole.USER);
        assertThat(message.getContent()).isNotNull();
        assertThat(message.getPromptTokens()).isNotNull();
        assertThat(message.getCompletionTokens()).isNotNull();
        assertThat(message.getModerationResult()).isNotNull();
        assertThat(message.getCreatedAt()).isNotNull();
    }

    // ========== Complex Scenarios ==========

    @Test
    void shouldHandleCompleteUserMessage_whenAllFieldsSet() {
        // Given & When
        Message message = aUserMessage()
                .promptTokens(tokenCount(150))
                .completionTokens(zeroTokens())
                .moderationResult(approvedModeration())
                .build();

        // Then
        assertThat(message.getTotalTokens().getValue()).isEqualTo(150);
        assertThat(message.isModerated()).isTrue();
        assertThat(message.isUserMessage()).isTrue();
    }

    @Test
    void shouldHandleCompleteAssistantMessage_whenAllFieldsSet() {
        // Given & When
        Message message = anAssistantMessage()
                .promptTokens(zeroTokens())
                .completionTokens(tokenCount(200))
                .moderationResult(approvedModeration())
                .build();

        // Then
        assertThat(message.getTotalTokens().getValue()).isEqualTo(200);
        assertThat(message.isModerated()).isTrue();
        assertThat(message.isAssistantMessage()).isTrue();
    }

    @Test
    void shouldHandleRejectedMessage_whenModerationFailed() {
        // Given & When
        Message message = aRejectedMessage().build();

        // Then
        assertThat(message.isModerated()).isFalse();
        assertThat(message.getModerationResult().isFailed()).isTrue();
        assertThat(message.getModerationResult().getFlaggedCategories()).contains("hate", "violence");
    }
}
