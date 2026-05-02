package com.loai.spring.ai.chat_assistant.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * AOP-based method-level timing and tracing.
 *
 * Applied to two boundary layers:
 * - Use cases (application/usecase): full orchestration flow entry/exit
 * - Infrastructure adapters (AI, moderation, rate limit, cache, audit): external service calls
 *
 * All logs are at DEBUG level — visible in dev/staging, suppressible in prod.
 * Parameters are intentionally NOT logged to avoid capturing sensitive message content.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.loai.spring.ai.chat_assistant.application.usecase..*(..))")
    private void useCases() {}

    @Pointcut("execution(* com.loai.spring.ai.chat_assistant.infrastructure.ai..*(..))"
            + " || execution(* com.loai.spring.ai.chat_assistant.infrastructure.ratelimit..*(..))"
            + " || execution(* com.loai.spring.ai.chat_assistant.infrastructure.cache..*(..))"
            + " || execution(* com.loai.spring.ai.chat_assistant.infrastructure.audit..*(..))")
    private void infrastructureAdapters() {}

    @Around("useCases()")
    public Object logUseCases(ProceedingJoinPoint pjp) throws Throwable {
        return logExecution(pjp);
    }

    @Around("infrastructureAdapters()")
    public Object logInfrastructure(ProceedingJoinPoint pjp) throws Throwable {
        return logExecution(pjp);
    }

    private Object logExecution(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        long start = System.nanoTime();

        log.debug(">> {}", method);
        try {
            Object result = pjp.proceed();
            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            log.debug("<< {} completed ({}ms)", method, ms);
            return result;
        } catch (Exception e) {
            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            log.error("<< {} failed after {}ms: {}", method, ms, e.getMessage());
            throw e;
        }
    }
}
