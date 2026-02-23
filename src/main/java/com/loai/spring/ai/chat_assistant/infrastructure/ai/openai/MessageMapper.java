package com.loai.spring.ai.chat_assistant.infrastructure.ai.openai;

import com.loai.spring.ai.chat_assistant.domain.model.Message;
import com.loai.spring.ai.chat_assistant.domain.model.vo.MessageRole;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between domain Message and Spring AI Message formats.
 */
@Component
public class MessageMapper {

    /**
     * Converts a domain Message to Spring AI message format.
     */
    public org.springframework.ai.chat.messages.Message toSpringAIMessage(Message message) {
        String content = message.getContent().getValue();

        return switch (message.getRole()) {
            case USER -> new UserMessage(content);
            case ASSISTANT -> new AssistantMessage(content);
            case SYSTEM -> new SystemMessage(content);
        };
    }

    /**
     * Converts a list of domain Messages to Spring AI message format.
     */
    public List<org.springframework.ai.chat.messages.Message> toSpringAIMessages(List<Message> messages) {
        return messages.stream()
            .map(this::toSpringAIMessage)
            .collect(Collectors.toList());
    }

    /**
     * Determines the MessageRole from Spring AI message type.
     */
    public MessageRole fromSpringAIMessage(org.springframework.ai.chat.messages.Message message) {
        return switch (message.getMessageType()) {
            case USER -> MessageRole.USER;
            case ASSISTANT -> MessageRole.ASSISTANT;
            case SYSTEM -> MessageRole.SYSTEM;
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
        };
    }
}
