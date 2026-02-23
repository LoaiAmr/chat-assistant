package com.loai.spring.ai.chat_assistant.infrastructure.ai.openai;

import com.loai.spring.ai.chat_assistant.application.exception.ModerationFailedException;
import com.loai.spring.ai.chat_assistant.application.port.output.ModerationService;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ModerationResult;
import com.loai.spring.ai.chat_assistant.domain.model.vo.ModerationStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.moderation.Categories;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI Moderation API implementation.
 * Checks content for policy violations using OpenAI's moderation endpoint.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIModerationService implements ModerationService {

    private final OpenAiModerationModel moderationModel;

    @Override
    @CircuitBreaker(name = "openai-moderation", fallbackMethod = "moderateContentFallback")
    public ModerationResult moderateContent(String content) {
        log.debug("Moderating content: {} characters", content.length());

        try {
            ModerationPrompt prompt = new ModerationPrompt(content);
            ModerationResponse response = moderationModel.call(prompt);

            // Get the Moderation object from the response
            var moderation = response.getResult().getOutput();

            // Get the first result (typically there's only one)
            var results = moderation.getResults();
            if (results.isEmpty()) {
                log.debug("No moderation results returned, approving by default");
                return ModerationResult.approved();
            }

            var moderationResult = results.get(0);
            boolean flagged = moderationResult.isFlagged();

            if (!flagged) {
                log.debug("Content passed moderation");
                return ModerationResult.approved();
            }

            // Extract flagged categories
            List<String> flaggedCategories = extractFlaggedCategories(moderationResult.getCategories());

            log.warn("Content flagged for moderation. Categories: {}", flaggedCategories);

            // For MVP, we'll reject flagged content
            return ModerationResult.rejected(flaggedCategories);

        } catch (Exception e) {
            log.error("Error calling OpenAI Moderation API: {}", e.getMessage(), e);
            throw new ModerationFailedException("Failed to moderate content: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the list of flagged category names from the Categories object.
     */
    private List<String> extractFlaggedCategories(Categories categories) {
        List<String> flagged = new ArrayList<>();

        if (categories.isHate()) flagged.add("HATE");
        if (categories.isHateThreatening()) flagged.add("HATE_THREATENING");
        if (categories.isSelfHarm()) flagged.add("SELF_HARM");
        if (categories.isSexual()) flagged.add("SEXUAL");
        if (categories.isSexualMinors()) flagged.add("SEXUAL_MINORS");
        if (categories.isViolence()) flagged.add("VIOLENCE");
        if (categories.isViolenceGraphic()) flagged.add("VIOLENCE_GRAPHIC");

        return flagged;
    }

    @Override
    public boolean isContentSafe(String content) {
        ModerationResult result = moderateContent(content);
        return result.getStatus() == ModerationStatus.APPROVED;
    }

    /**
     * Fallback method when circuit breaker opens.
     * In production, consider allowing content through vs blocking.
     */
    @SuppressWarnings("unused")
    private ModerationResult moderateContentFallback(String content, Exception e) {
        log.error("Moderation service unavailable: {}. Allowing content through.", e.getMessage());
        // For MVP: fail-open (allow content when moderation unavailable)
        // Production might want fail-closed (reject when unavailable)
        return ModerationResult.approved();
    }
}
