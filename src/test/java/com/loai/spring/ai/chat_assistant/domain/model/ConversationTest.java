package com.loai.spring.ai.chat_assistant.domain.model;

import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.MessageContent;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.loai.spring.ai.chat_assistant.testutil.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversationTest {

    // ========== Message Management Tests ==========

    @Test
    void shouldAddMessage_whenValidMessageProvided() {
        // Given
        Conversation conversation = aValidConversation().build();
        Message message = aUserMessage()
                .conversationId(conversation.getId())
                .build();

        // When
        Conversation updated = conversation.addMessage(message);

        // Then
        assertThat(updated.getMessages()).hasSize(1);
        assertThat(updated.getMessages().get(0)).isEqualTo(message);
    }

    @Test
    void shouldReturnNewInstance_whenAddingMessage() {
        // Given
        Conversation original = aValidConversation().build();
        Message message = aUserMessage()
                .conversationId(original.getId())
                .build();

        // When
        Conversation updated = original.addMessage(message);

        // Then - Verify immutability
        assertThat(updated).isNotSameAs(original);
        assertThat(original.getMessages()).isEmpty();
        assertThat(updated.getMessages()).hasSize(1);
    }

    @Test
    void shouldUpdateTimestamp_whenMessageAdded() {
        // Given
        Conversation conversation = aValidConversation().build();
        Message message = aUserMessage()
                .conversationId(conversation.getId())
                .build();

        // When
        Conversation updated = conversation.addMessage(message);

        // Then
        assertThat(updated.getUpdatedAt()).isAfter(conversation.getUpdatedAt());
    }

    @Test
    void shouldReturnImmutableList_whenGetMessagesCalled() {
        // Given
        Conversation conversation = aConversationWithMessages(3).build();

        // When
        List<Message> messages = conversation.getMessages();

        // Then
        assertThatThrownBy(() -> messages.add(aUserMessage().build()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldNotModifyOriginal_whenAttemptingToModifyReturnedList() {
        // Given
        Conversation conversation = aConversationWithMessages(3).build();
        List<Message> messages = conversation.getMessages();
        int originalSize = messages.size();

        // When & Then
        assertThatThrownBy(() -> messages.clear())
                .isInstanceOf(UnsupportedOperationException.class);

        assertThat(conversation.getMessages()).hasSize(originalSize);
    }

    @Test
    void shouldAddMultipleMessages_whenAddingSequentially() {
        // Given
        Conversation conversation = aValidConversation().build();
        ConversationId conversationId = conversation.getId();

        Message message1 = aUserMessage().conversationId(conversationId).build();
        Message message2 = anAssistantMessage().conversationId(conversationId).build();
        Message message3 = aUserMessage().conversationId(conversationId).build();

        // When
        Conversation updated = conversation
                .addMessage(message1)
                .addMessage(message2)
                .addMessage(message3);

        // Then
        assertThat(updated.getMessages()).hasSize(3);
        assertThat(updated.getMessages().get(0)).isEqualTo(message1);
        assertThat(updated.getMessages().get(1)).isEqualTo(message2);
        assertThat(updated.getMessages().get(2)).isEqualTo(message3);
    }

    // ========== Token Aggregation Tests ==========

    @Test
    void shouldCalculateTotalTokens_whenMultipleMessages() {
        // Given
        ConversationId conversationId = conversationId();
        Message message1 = aMessageWithTokens(100, 50).conversationId(conversationId).build();
        Message message2 = aMessageWithTokens(200, 150).conversationId(conversationId).build();
        Message message3 = aMessageWithTokens(50, 25).conversationId(conversationId).build();

        Conversation conversation = aValidConversation()
                .id(conversationId)
                .messages(List.of(message1, message2, message3))
                .build();

        // When
        TokenCount totalTokens = conversation.getTotalTokens();

        // Then - (100+50) + (200+150) + (50+25) = 575
        assertThat(totalTokens.getValue()).isEqualTo(575);
    }

    @Test
    void shouldReturnZero_whenNoMessages() {
        // Given
        Conversation conversation = aValidConversation().build();

        // When
        TokenCount totalTokens = conversation.getTotalTokens();

        // Then
        assertThat(totalTokens.getValue()).isEqualTo(0);
    }

    @Test
    void shouldHandleMessagesWithNullTokens_whenCalculating() {
        // Given
        ConversationId conversationId = conversationId();
        Message message = aUserMessage()
                .conversationId(conversationId)
                .promptTokens(null)
                .completionTokens(null)
                .build();

        Conversation conversation = aValidConversation()
                .id(conversationId)
                .messages(List.of(message))
                .build();

        // When
        TokenCount totalTokens = conversation.getTotalTokens();

        // Then - Message.getTotalTokens() should handle null by treating as zero
        assertThat(totalTokens.getValue()).isEqualTo(0);
    }

    // ========== Message Count & Limits Tests ==========

    @Test
    void shouldReturnCorrectCount_whenGetMessageCountCalled() {
        // Given
        Conversation conversation = aConversationWithMessages(5).build();

        // When
        int count = conversation.getMessageCount();

        // Then
        assertThat(count).isEqualTo(5);
    }

    @Test
    void shouldReturnZero_whenNoMessagesInConversation() {
        // Given
        Conversation conversation = aValidConversation().build();

        // When
        int count = conversation.getMessageCount();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldAllowAddMessage_whenUnderLimit() {
        // Given
        Conversation conversation = aConversationWithMessages(5).build();
        int maxMessages = 10;

        // When
        boolean canAdd = conversation.canAddMessage(maxMessages);

        // Then
        assertThat(canAdd).isTrue();
    }

    @Test
    void shouldDenyAddMessage_whenAtLimit() {
        // Given
        Conversation conversation = aConversationWithMessages(10).build();
        int maxMessages = 10;

        // When
        boolean canAdd = conversation.canAddMessage(maxMessages);

        // Then
        assertThat(canAdd).isFalse();
    }

    @Test
    void shouldDenyAddMessage_whenOverLimit() {
        // Given
        Conversation conversation = aConversationWithMessages(15).build();
        int maxMessages = 10;

        // When
        boolean canAdd = conversation.canAddMessage(maxMessages);

        // Then
        assertThat(canAdd).isFalse();
    }

    @Test
    void shouldAllowAddMessage_whenExactlyOneBelowLimit() {
        // Given
        Conversation conversation = aConversationWithMessages(9).build();
        int maxMessages = 10;

        // When
        boolean canAdd = conversation.canAddMessage(maxMessages);

        // Then
        assertThat(canAdd).isTrue();
    }

    // ========== Recent Messages Retrieval Tests ==========

    @Test
    void shouldReturnLastNMessages_whenGetRecentMessagesCalled() {
        // Given
        Conversation conversation = aConversationWithMessages(10).build();

        // When
        List<Message> recentMessages = conversation.getRecentMessages(3);

        // Then
        assertThat(recentMessages).hasSize(3);
        // Verify it returns the last 3 messages
        List<Message> allMessages = conversation.getMessages();
        assertThat(recentMessages.get(0)).isEqualTo(allMessages.get(7));
        assertThat(recentMessages.get(1)).isEqualTo(allMessages.get(8));
        assertThat(recentMessages.get(2)).isEqualTo(allMessages.get(9));
    }

    @Test
    void shouldReturnAllMessages_whenCountExceedsTotal() {
        // Given
        Conversation conversation = aConversationWithMessages(5).build();

        // When
        List<Message> recentMessages = conversation.getRecentMessages(10);

        // Then
        assertThat(recentMessages).hasSize(5);
        assertThat(recentMessages).isEqualTo(conversation.getMessages());
    }

    @Test
    void shouldReturnEmptyList_whenNoMessages() {
        // Given
        Conversation conversation = aValidConversation().build();

        // When
        List<Message> recentMessages = conversation.getRecentMessages(5);

        // Then
        assertThat(recentMessages).isEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenCountIsZero() {
        // Given
        Conversation conversation = aConversationWithMessages(5).build();

        // When
        List<Message> recentMessages = conversation.getRecentMessages(0);

        // Then
        assertThat(recentMessages).isEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenCountIsNegative() {
        // Given
        Conversation conversation = aConversationWithMessages(5).build();

        // When
        List<Message> recentMessages = conversation.getRecentMessages(-1);

        // Then
        assertThat(recentMessages).isEmpty();
    }

    @Test
    void shouldReturnImmutableList_whenGetRecentMessagesCalled() {
        // Given
        Conversation conversation = aConversationWithMessages(5).build();

        // When
        List<Message> recentMessages = conversation.getRecentMessages(3);

        // Then
        assertThatThrownBy(() -> recentMessages.add(aUserMessage().build()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ========== Token Budget Management Tests (Most Complex) ==========

    @Test
    void shouldReturnMessagesWithinBudget_whenBudgetProvided() {
        // Given
        ConversationId conversationId = conversationId();
        Message message1 = aMessageWithTokens(50, 50).conversationId(conversationId).build(); // 100 tokens
        Message message2 = aMessageWithTokens(50, 50).conversationId(conversationId).build(); // 100 tokens
        Message message3 = aMessageWithTokens(50, 50).conversationId(conversationId).build(); // 100 tokens

        Conversation conversation = aValidConversation()
                .id(conversationId)
                .messages(List.of(message1, message2, message3))
                .build();

        TokenCount budget = tokenCount(250); // Can fit 2 messages

        // When
        List<Message> withinBudget = conversation.getMessagesWithinTokenBudget(budget);

        // Then - Should return last 2 messages (most recent)
        assertThat(withinBudget).hasSize(2);
        assertThat(withinBudget.get(0)).isEqualTo(message2);
        assertThat(withinBudget.get(1)).isEqualTo(message3);
    }

    @Test
    void shouldReturnEmptyList_whenBudgetIsZero() {
        // Given
        Conversation conversation = aConversationWithMessages(5).build();
        TokenCount zeroBudget = zeroTokens();

        // When
        List<Message> withinBudget = conversation.getMessagesWithinTokenBudget(zeroBudget);

        // Then
        assertThat(withinBudget).isEmpty();
    }

    @Test
    void shouldReturnAllMessages_whenBudgetIsUnlimited() {
        // Given
        ConversationId conversationId = conversationId();
        Message message1 = aMessageWithTokens(100, 100).conversationId(conversationId).build();
        Message message2 = aMessageWithTokens(100, 100).conversationId(conversationId).build();
        Message message3 = aMessageWithTokens(100, 100).conversationId(conversationId).build();

        Conversation conversation = aValidConversation()
                .id(conversationId)
                .messages(List.of(message1, message2, message3))
                .build();

        TokenCount hugeBudget = tokenCount(1000000);

        // When
        List<Message> withinBudget = conversation.getMessagesWithinTokenBudget(hugeBudget);

        // Then
        assertThat(withinBudget).hasSize(3);
        assertThat(withinBudget).isEqualTo(conversation.getMessages());
    }

    @Test
    void shouldReturnMostRecentFirst_whenLimitedBudget() {
        // Given
        ConversationId conversationId = conversationId();
        Message oldest = aMessageWithTokens(100, 0)
                .conversationId(conversationId)
                .content(MessageContent.of("Oldest"))
                .build();
        Message middle = aMessageWithTokens(100, 0)
                .conversationId(conversationId)
                .content(MessageContent.of("Middle"))
                .build();
        Message newest = aMessageWithTokens(100, 0)
                .conversationId(conversationId)
                .content(MessageContent.of("Newest"))
                .build();

        Conversation conversation = aValidConversation()
                .id(conversationId)
                .messages(List.of(oldest, middle, newest))
                .build();

        TokenCount budget = tokenCount(150); // Can fit only 1 message

        // When
        List<Message> withinBudget = conversation.getMessagesWithinTokenBudget(budget);

        // Then - Should return only the newest message
        assertThat(withinBudget).hasSize(1);
        assertThat(withinBudget.get(0).getContent().toString()).isEqualTo("Newest");
    }

    @Test
    void shouldStopAtBudget_whenNextMessageExceedsLimit() {
        // Given
        ConversationId conversationId = conversationId();
        Message message1 = aMessageWithTokens(100, 0).conversationId(conversationId).build(); // 100 tokens
        Message message2 = aMessageWithTokens(100, 0).conversationId(conversationId).build(); // 100 tokens
        Message message3 = aMessageWithTokens(150, 0).conversationId(conversationId).build(); // 150 tokens

        Conversation conversation = aValidConversation()
                .id(conversationId)
                .messages(List.of(message1, message2, message3))
                .build();

        TokenCount budget = tokenCount(200); // Can fit message3 (150) but not message2+message3 (250)

        // When
        List<Message> withinBudget = conversation.getMessagesWithinTokenBudget(budget);

        // Then - Should return only message3
        assertThat(withinBudget).hasSize(1);
        assertThat(withinBudget.get(0)).isEqualTo(message3);
    }

    @Test
    void shouldReturnEmptyList_whenNoMessagesInBudget() {
        // Given
        Conversation conversation = aValidConversation().build();
        TokenCount budget = tokenCount(1000);

        // When
        List<Message> withinBudget = conversation.getMessagesWithinTokenBudget(budget);

        // Then
        assertThat(withinBudget).isEmpty();
    }

    @Test
    void shouldReturnImmutableList_whenGetMessagesWithinBudgetCalled() {
        // Given
        Conversation conversation = aConversationWithMessages(5).build();
        TokenCount budget = tokenCount(1000);

        // When
        List<Message> withinBudget = conversation.getMessagesWithinTokenBudget(budget);

        // Then
        assertThatThrownBy(() -> withinBudget.add(aUserMessage().build()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldMaintainMessageOrder_whenReturningMessagesWithinBudget() {
        // Given
        ConversationId conversationId = conversationId();
        Message msg1 = aUserMessageWithContent("First")
                .conversationId(conversationId)
                .promptTokens(tokenCount(50))
                .build();
        Message msg2 = aUserMessageWithContent("Second")
                .conversationId(conversationId)
                .promptTokens(tokenCount(50))
                .build();
        Message msg3 = aUserMessageWithContent("Third")
                .conversationId(conversationId)
                .promptTokens(tokenCount(50))
                .build();

        Conversation conversation = aValidConversation()
                .id(conversationId)
                .messages(List.of(msg1, msg2, msg3))
                .build();

        TokenCount budget = tokenCount(120); // Can fit last 2 messages

        // When
        List<Message> withinBudget = conversation.getMessagesWithinTokenBudget(budget);

        // Then - Should maintain order
        assertThat(withinBudget).hasSize(2);
        assertThat(withinBudget.get(0).getContent().toString()).isEqualTo("Second");
        assertThat(withinBudget.get(1).getContent().toString()).isEqualTo("Third");
    }

    // ========== Edge Cases ==========

    @Test
    void shouldHandleEmptyConversation_whenNoMessages() {
        // Given
        Conversation conversation = aValidConversation().build();

        // When & Then
        assertThat(conversation.getMessages()).isEmpty();
        assertThat(conversation.getMessageCount()).isEqualTo(0);
        assertThat(conversation.getTotalTokens().getValue()).isEqualTo(0);
        assertThat(conversation.getRecentMessages(10)).isEmpty();
        assertThat(conversation.getMessagesWithinTokenBudget(tokenCount(1000))).isEmpty();
    }

    @Test
    void shouldHandleSingleMessage_whenOneMessagePresent() {
        // Given
        ConversationId conversationId = conversationId();
        Message message = aUserMessage()
                .conversationId(conversationId)
                .promptTokens(tokenCount(100))
                .build();

        Conversation conversation = aValidConversation()
                .id(conversationId)
                .messages(List.of(message))
                .build();

        // When & Then
        assertThat(conversation.getMessageCount()).isEqualTo(1);
        assertThat(conversation.getTotalTokens().getValue()).isEqualTo(100);
        assertThat(conversation.getRecentMessages(1)).hasSize(1);
        assertThat(conversation.getMessagesWithinTokenBudget(tokenCount(100))).hasSize(1);
    }
}
