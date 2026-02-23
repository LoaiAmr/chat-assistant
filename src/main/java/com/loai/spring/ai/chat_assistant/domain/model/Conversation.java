package com.loai.spring.ai.chat_assistant.domain.model;

import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TenantId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregate root for a conversation.
 * Contains a collection of messages and provides business logic
 * for managing the conversation lifecycle.
 */
@Value
@Builder
@With
public class Conversation {
    ConversationId id;
    TenantId tenantId;
    String title;
    @Builder.Default
    List<Message> messages = new ArrayList<>();
    Instant createdAt;
    Instant updatedAt;

    /**
     * Returns an immutable view of the messages.
     */
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /**
     * Adds a message to the conversation.
     * Returns a new Conversation instance with the added message.
     */
    public Conversation addMessage(Message message) {
        List<Message> newMessages = new ArrayList<>(this.messages);
        newMessages.add(message);
        return this.withMessages(newMessages).withUpdatedAt(Instant.now());
    }

    /**
     * Calculates the total tokens used across all messages in the conversation.
     */
    public TokenCount getTotalTokens() {
        return messages.stream()
            .map(Message::getTotalTokens)
            .reduce(TokenCount.zero(), TokenCount::add);
    }

    /**
     * Gets the number of messages in the conversation.
     */
    public int getMessageCount() {
        return messages.size();
    }

    /**
     * Checks if the conversation can accept more messages based on a limit.
     */
    public boolean canAddMessage(int maxMessagesPerConversation) {
        return messages.size() < maxMessagesPerConversation;
    }

    /**
     * Gets the most recent N messages for context window management.
     */
    public List<Message> getRecentMessages(int count) {
        if (messages.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }

        int startIndex = Math.max(0, messages.size() - count);
        return Collections.unmodifiableList(messages.subList(startIndex, messages.size()));
    }

    /**
     * Gets messages within a token budget for context window management.
     */
    public List<Message> getMessagesWithinTokenBudget(TokenCount tokenBudget) {
        List<Message> result = new ArrayList<>();
        TokenCount currentTotal = TokenCount.zero();

        // Iterate from most recent to oldest
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            TokenCount messageTokens = message.getTotalTokens();
            TokenCount newTotal = currentTotal.add(messageTokens);

            if (newTotal.exceeds(tokenBudget)) {
                break;
            }

            result.add(0, message); // Add to beginning to maintain order
            currentTotal = newTotal;
        }

        return Collections.unmodifiableList(result);
    }
}
