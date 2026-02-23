package com.loai.spring.ai.chat_assistant.infrastructure.persistence.jpa;

import com.loai.spring.ai.chat_assistant.infrastructure.persistence.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for MessageEntity.
 * Provides database access methods for messages.
 */
@Repository
public interface MessageJpaRepository extends JpaRepository<MessageEntity, UUID> {

    /**
     * Finds all messages for a conversation, ordered by creation time.
     *
     * @param conversationId the conversation ID
     * @return list of messages
     */
    List<MessageEntity> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    /**
     * Counts messages in a conversation.
     *
     * @param conversationId the conversation ID
     * @return message count
     */
    long countByConversationId(UUID conversationId);

    /**
     * Deletes all messages in a conversation.
     *
     * @param conversationId the conversation ID
     */
    void deleteByConversationId(UUID conversationId);
}
