package com.loai.spring.ai.chat_assistant.domain.repository;

import com.loai.spring.ai.chat_assistant.domain.model.Message;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ConversationId;
import com.loai.spring.ai.chat_assistant.domain.model.vo.MessageId;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for Message entity.
 * Defines operations without specifying implementation details.
 */
public interface MessageRepository {

    /**
     * Saves a message.
     */
    Message save(Message message);

    /**
     * Finds a message by its unique identifier.
     */
    Optional<Message> findById(MessageId id);

    /**
     * Finds all messages for a conversation, ordered by creation time.
     */
    List<Message> findByConversationIdOrderByCreatedAtAsc(ConversationId conversationId);

    /**
     * Counts messages in a conversation.
     */
    long countByConversationId(ConversationId conversationId);

    /**
     * Deletes all messages in a conversation (cascade delete).
     */
    void deleteByConversationId(ConversationId conversationId);
}
