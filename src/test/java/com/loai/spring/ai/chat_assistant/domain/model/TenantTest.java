package com.loai.spring.ai.chat_assistant.domain.model;

import com.loai.spring.ai.chat_assistant.domain.model.vo.TokenCount;
import org.junit.jupiter.api.Test;

import static com.loai.spring.ai.chat_assistant.testutil.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;

class TenantTest {

    // ========== Token Consumption - Daily Limit Tests ==========

    @Test
    void shouldAllowConsumption_whenNoDailyLimitSet() {
        // Given
        Tenant tenant = aTenantWithoutLimits().build();
        TokenCount tokensToConsume = tokenCount(5000);
        TokenCount tokensUsedToday = tokenCount(10000); // Already used more than typical limit

        // When
        boolean canConsume = tenant.canConsumeTokens(tokensToConsume, tokensUsedToday);

        // Then
        assertThat(canConsume).isTrue();
    }

    @Test
    void shouldAllowConsumption_whenUnderDailyLimit() {
        // Given
        Tenant tenant = aTenantWithDailyLimit(10000).build();
        TokenCount tokensToConsume = tokenCount(3000);
        TokenCount tokensUsedToday = tokenCount(5000);

        // When
        boolean canConsume = tenant.canConsumeTokens(tokensToConsume, tokensUsedToday);

        // Then - Total would be 8000, which is under 10000
        assertThat(canConsume).isTrue();
    }

    @Test
    void shouldDenyConsumption_whenExceedsDailyLimit() {
        // Given
        Tenant tenant = aTenantWithDailyLimit(10000).build();
        TokenCount tokensToConsume = tokenCount(6000);
        TokenCount tokensUsedToday = tokenCount(5000);

        // When
        boolean canConsume = tenant.canConsumeTokens(tokensToConsume, tokensUsedToday);

        // Then - Total would be 11000, which exceeds 10000
        assertThat(canConsume).isFalse();
    }

    @Test
    void shouldAllowConsumption_whenExactlyAtDailyLimit() {
        // Given
        Tenant tenant = aTenantWithDailyLimit(10000).build();
        TokenCount tokensToConsume = tokenCount(5000);
        TokenCount tokensUsedToday = tokenCount(5000);

        // When
        boolean canConsume = tenant.canConsumeTokens(tokensToConsume, tokensUsedToday);

        // Then - Total would be exactly 10000, which should be allowed (not exceeding)
        assertThat(canConsume).isTrue();
    }

    @Test
    void shouldAllowConsumption_whenZeroTokensUsedAndUnderLimit() {
        // Given
        Tenant tenant = aTenantWithDailyLimit(10000).build();
        TokenCount tokensToConsume = tokenCount(5000);
        TokenCount tokensUsedToday = zeroTokens();

        // When
        boolean canConsume = tenant.canConsumeTokens(tokensToConsume, tokensUsedToday);

        // Then
        assertThat(canConsume).isTrue();
    }

    @Test
    void shouldDenyConsumption_whenExceedsFromZero() {
        // Given
        Tenant tenant = aTenantWithDailyLimit(10000).build();
        TokenCount tokensToConsume = tokenCount(15000);
        TokenCount tokensUsedToday = zeroTokens();

        // When
        boolean canConsume = tenant.canConsumeTokens(tokensToConsume, tokensUsedToday);

        // Then
        assertThat(canConsume).isFalse();
    }

    @Test
    void shouldDenyConsumption_whenAlreadyAtLimit() {
        // Given
        Tenant tenant = aTenantWithDailyLimit(10000).build();
        TokenCount tokensToConsume = tokenCount(1);
        TokenCount tokensUsedToday = tokenCount(10000);

        // When
        boolean canConsume = tenant.canConsumeTokens(tokensToConsume, tokensUsedToday);

        // Then - Already at limit, any additional consumption should be denied
        assertThat(canConsume).isFalse();
    }

    // ========== Token Consumption - Monthly Limit Tests ==========

    @Test
    void shouldAllowConsumption_whenNoMonthlyLimitSet() {
        // Given
        Tenant tenant = aTenantWithoutLimits().build();
        TokenCount tokensToConsume = tokenCount(50000);
        TokenCount tokensUsedThisMonth = tokenCount(500000);

        // When
        boolean canConsume = tenant.canConsumeTokensMonthly(tokensToConsume, tokensUsedThisMonth);

        // Then
        assertThat(canConsume).isTrue();
    }

    @Test
    void shouldAllowConsumption_whenUnderMonthlyLimit() {
        // Given
        Tenant tenant = aTenantWithMonthlyLimit(300000).build();
        TokenCount tokensToConsume = tokenCount(50000);
        TokenCount tokensUsedThisMonth = tokenCount(200000);

        // When
        boolean canConsume = tenant.canConsumeTokensMonthly(tokensToConsume, tokensUsedThisMonth);

        // Then - Total would be 250000, which is under 300000
        assertThat(canConsume).isTrue();
    }

    @Test
    void shouldDenyConsumption_whenExceedsMonthlyLimit() {
        // Given
        Tenant tenant = aTenantWithMonthlyLimit(300000).build();
        TokenCount tokensToConsume = tokenCount(150000);
        TokenCount tokensUsedThisMonth = tokenCount(200000);

        // When
        boolean canConsume = tenant.canConsumeTokensMonthly(tokensToConsume, tokensUsedThisMonth);

        // Then - Total would be 350000, which exceeds 300000
        assertThat(canConsume).isFalse();
    }

    @Test
    void shouldAllowConsumption_whenExactlyAtMonthlyLimit() {
        // Given
        Tenant tenant = aTenantWithMonthlyLimit(300000).build();
        TokenCount tokensToConsume = tokenCount(100000);
        TokenCount tokensUsedThisMonth = tokenCount(200000);

        // When
        boolean canConsume = tenant.canConsumeTokensMonthly(tokensToConsume, tokensUsedThisMonth);

        // Then - Total would be exactly 300000, which should be allowed
        assertThat(canConsume).isTrue();
    }

    @Test
    void shouldAllowConsumption_whenZeroTokensUsedThisMonthAndUnderLimit() {
        // Given
        Tenant tenant = aTenantWithMonthlyLimit(300000).build();
        TokenCount tokensToConsume = tokenCount(50000);
        TokenCount tokensUsedThisMonth = zeroTokens();

        // When
        boolean canConsume = tenant.canConsumeTokensMonthly(tokensToConsume, tokensUsedThisMonth);

        // Then
        assertThat(canConsume).isTrue();
    }

    @Test
    void shouldDenyConsumption_whenMonthlyLimitExceededFromZero() {
        // Given
        Tenant tenant = aTenantWithMonthlyLimit(300000).build();
        TokenCount tokensToConsume = tokenCount(400000);
        TokenCount tokensUsedThisMonth = zeroTokens();

        // When
        boolean canConsume = tenant.canConsumeTokensMonthly(tokensToConsume, tokensUsedThisMonth);

        // Then
        assertThat(canConsume).isFalse();
    }

    // ========== Request Authorization Tests ==========

    @Test
    void shouldAllowRequests_whenTenantActive() {
        // Given
        Tenant tenant = aValidTenant()
                .active(true)
                .build();

        // When
        boolean canMakeRequests = tenant.canMakeRequests();

        // Then
        assertThat(canMakeRequests).isTrue();
    }

    @Test
    void shouldDenyRequests_whenTenantInactive() {
        // Given
        Tenant tenant = anInactiveTenant().build();

        // When
        boolean canMakeRequests = tenant.canMakeRequests();

        // Then
        assertThat(canMakeRequests).isFalse();
    }

    // ========== Builder & Immutability Tests ==========

    @Test
    void shouldCreateTenant_whenBuilderUsed() {
        // When
        Tenant tenant = aValidTenant().build();

        // Then
        assertThat(tenant.getId()).isNotNull();
        assertThat(tenant.getName()).isEqualTo("Test Tenant");
        assertThat(tenant.getApiKey()).isNotNull();
        assertThat(tenant.isActive()).isTrue();
        assertThat(tenant.getDailyTokenLimit()).isEqualTo(10000);
        assertThat(tenant.getMonthlyTokenLimit()).isEqualTo(300000);
        assertThat(tenant.getCreatedAt()).isNotNull();
        assertThat(tenant.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCreateNewInstance_whenWithMethodUsed() {
        // Given
        Tenant original = aValidTenant().build();

        // When
        Tenant modified = original.withName("Modified Tenant");

        // Then - Verify immutability
        assertThat(modified).isNotSameAs(original);
        assertThat(modified.getName()).isEqualTo("Modified Tenant");
        assertThat(original.getName()).isEqualTo("Test Tenant"); // Original unchanged
    }

    @Test
    void shouldNotModifyOriginal_whenWithMethodUsed() {
        // Given
        Tenant original = aValidTenant()
                .active(true)
                .build();

        // When
        Tenant deactivated = original.withActive(false);

        // Then
        assertThat(original.isActive()).isTrue(); // Original unchanged
        assertThat(deactivated.isActive()).isFalse(); // New instance has change
    }

    // ========== Combined Scenarios ==========

    @Test
    void shouldAllowConsumption_whenBothLimitsNotSet() {
        // Given
        Tenant tenant = aTenantWithoutLimits().build();
        TokenCount tokensToConsume = tokenCount(1000000);
        TokenCount tokensUsedToday = tokenCount(500000);
        TokenCount tokensUsedThisMonth = tokenCount(5000000);

        // When
        boolean canConsumeDaily = tenant.canConsumeTokens(tokensToConsume, tokensUsedToday);
        boolean canConsumeMonthly = tenant.canConsumeTokensMonthly(tokensToConsume, tokensUsedThisMonth);

        // Then
        assertThat(canConsumeDaily).isTrue();
        assertThat(canConsumeMonthly).isTrue();
    }

    @Test
    void shouldRespectBothLimits_whenBothAreSet() {
        // Given
        Tenant tenant = aValidTenant()
                .dailyTokenLimit(10000)
                .monthlyTokenLimit(300000)
                .build();

        TokenCount tokensToConsume = tokenCount(5000);
        TokenCount tokensUsedToday = tokenCount(6000); // Would exceed daily (11000 > 10000)
        TokenCount tokensUsedThisMonth = tokenCount(200000); // Would not exceed monthly (205000 < 300000)

        // When
        boolean canConsumeDaily = tenant.canConsumeTokens(tokensToConsume, tokensUsedToday);
        boolean canConsumeMonthly = tenant.canConsumeTokensMonthly(tokensToConsume, tokensUsedThisMonth);

        // Then
        assertThat(canConsumeDaily).isFalse();
        assertThat(canConsumeMonthly).isTrue();
    }
}
