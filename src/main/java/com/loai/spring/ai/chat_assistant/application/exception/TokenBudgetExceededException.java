package com.loai.spring.ai.chat_assistant.application.exception;

import lombok.Getter;

/**
 * Exception thrown when a tenant's token budget is exceeded.
 */
@Getter
public class TokenBudgetExceededException extends RuntimeException {

    private final long usedTokens;
    private final long tokenLimit;

    public TokenBudgetExceededException(String message, long usedTokens, long tokenLimit) {
        super(message);
        this.usedTokens = usedTokens;
        this.tokenLimit = tokenLimit;
    }

    public TokenBudgetExceededException(long usedTokens, long tokenLimit) {
        super("Token budget exceeded. Used: " + usedTokens + ", Limit: " + tokenLimit);
        this.usedTokens = usedTokens;
        this.tokenLimit = tokenLimit;
    }
}
