package com.loai.spring.ai.chat_assistant.domain.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenCountTest {

    // ========== Creation & Validation Tests ==========

    @Test
    void shouldCreateTokenCount_whenValidPositiveValue() {
        // Given
        int value = 100;

        // When
        TokenCount tokenCount = TokenCount.of(value);

        // Then
        assertThat(tokenCount.getValue()).isEqualTo(value);
    }

    @Test
    void shouldCreateZeroTokenCount_whenZeroProvided() {
        // When
        TokenCount tokenCount = TokenCount.of(0);

        // Then
        assertThat(tokenCount.getValue()).isEqualTo(0);
    }

    @Test
    void shouldCreateZeroTokenCount_whenZeroMethodCalled() {
        // When
        TokenCount tokenCount = TokenCount.zero();

        // Then
        assertThat(tokenCount.getValue()).isEqualTo(0);
    }

    @Test
    void shouldThrowIllegalArgumentException_whenNegativeValue() {
        // Given
        int negativeValue = -1;

        // When & Then
        assertThatThrownBy(() -> TokenCount.of(negativeValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void shouldThrowIllegalArgumentException_whenLargeNegativeValue() {
        // Given
        int largeNegativeValue = -1000;

        // When & Then
        assertThatThrownBy(() -> TokenCount.of(largeNegativeValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    // ========== Arithmetic Operations Tests ==========

    @Test
    void shouldAddTwoTokenCounts_whenAddMethodCalled() {
        // Given
        TokenCount first = TokenCount.of(100);
        TokenCount second = TokenCount.of(50);

        // When
        TokenCount result = first.add(second);

        // Then
        assertThat(result.getValue()).isEqualTo(150);
    }

    @Test
    void shouldReturnNewInstance_whenAddMethodCalled() {
        // Given
        TokenCount first = TokenCount.of(100);
        TokenCount second = TokenCount.of(50);

        // When
        TokenCount result = first.add(second);

        // Then - Verify immutability
        assertThat(result).isNotSameAs(first);
        assertThat(result).isNotSameAs(second);
        assertThat(first.getValue()).isEqualTo(100); // Original unchanged
        assertThat(second.getValue()).isEqualTo(50); // Original unchanged
    }

    @Test
    void shouldHandleZeroAddition_whenAddingZero() {
        // Given
        TokenCount tokens = TokenCount.of(100);
        TokenCount zero = TokenCount.zero();

        // When
        TokenCount result = tokens.add(zero);

        // Then
        assertThat(result.getValue()).isEqualTo(100);
    }

    @Test
    void shouldHandleAddingToZero_whenZeroIsFirst() {
        // Given
        TokenCount zero = TokenCount.zero();
        TokenCount tokens = TokenCount.of(100);

        // When
        TokenCount result = zero.add(tokens);

        // Then
        assertThat(result.getValue()).isEqualTo(100);
    }

    @Test
    void shouldAddMultipleTokenCounts_whenChainedAdditions() {
        // Given
        TokenCount first = TokenCount.of(100);
        TokenCount second = TokenCount.of(50);
        TokenCount third = TokenCount.of(25);

        // When
        TokenCount result = first.add(second).add(third);

        // Then
        assertThat(result.getValue()).isEqualTo(175);
    }

    @Test
    void shouldHandleMaxInteger_whenLargeValues() {
        // Given - Using large but safe values
        TokenCount large1 = TokenCount.of(1000000);
        TokenCount large2 = TokenCount.of(1000000);

        // When
        TokenCount result = large1.add(large2);

        // Then
        assertThat(result.getValue()).isEqualTo(2000000);
    }

    // ========== Comparison Operations Tests ==========

    @Test
    void shouldReturnTrue_whenExceedsLimit() {
        // Given
        TokenCount tokens = TokenCount.of(150);
        TokenCount limit = TokenCount.of(100);

        // When
        boolean exceeds = tokens.exceeds(limit);

        // Then
        assertThat(exceeds).isTrue();
    }

    @Test
    void shouldReturnFalse_whenBelowLimit() {
        // Given
        TokenCount tokens = TokenCount.of(50);
        TokenCount limit = TokenCount.of(100);

        // When
        boolean exceeds = tokens.exceeds(limit);

        // Then
        assertThat(exceeds).isFalse();
    }

    @Test
    void shouldReturnFalse_whenEqualToLimit() {
        // Given
        TokenCount tokens = TokenCount.of(100);
        TokenCount limit = TokenCount.of(100);

        // When
        boolean exceeds = tokens.exceeds(limit);

        // Then
        assertThat(exceeds).isFalse();
    }

    @Test
    void shouldReturnFalse_whenComparingZeros() {
        // Given
        TokenCount zero1 = TokenCount.zero();
        TokenCount zero2 = TokenCount.zero();

        // When
        boolean exceeds = zero1.exceeds(zero2);

        // Then
        assertThat(exceeds).isFalse();
    }

    @Test
    void shouldReturnFalse_whenZeroComparedToNonZeroLimit() {
        // Given
        TokenCount zero = TokenCount.zero();
        TokenCount limit = TokenCount.of(100);

        // When
        boolean exceeds = zero.exceeds(limit);

        // Then
        assertThat(exceeds).isFalse();
    }

    @Test
    void shouldReturnTrue_whenNonZeroExceedsZeroLimit() {
        // Given
        TokenCount tokens = TokenCount.of(1);
        TokenCount zeroLimit = TokenCount.zero();

        // When
        boolean exceeds = tokens.exceeds(zeroLimit);

        // Then
        assertThat(exceeds).isTrue();
    }

    // ========== Equality & HashCode Tests ==========

    @Test
    void shouldBeEqual_whenSameValue() {
        // Given
        TokenCount tokens1 = TokenCount.of(100);
        TokenCount tokens2 = TokenCount.of(100);

        // When & Then
        assertThat(tokens1).isEqualTo(tokens2);
    }

    @Test
    void shouldNotBeEqual_whenDifferentValue() {
        // Given
        TokenCount tokens1 = TokenCount.of(100);
        TokenCount tokens2 = TokenCount.of(50);

        // When & Then
        assertThat(tokens1).isNotEqualTo(tokens2);
    }

    @Test
    void shouldHaveSameHashCode_whenSameValue() {
        // Given
        TokenCount tokens1 = TokenCount.of(100);
        TokenCount tokens2 = TokenCount.of(100);

        // When & Then
        assertThat(tokens1.hashCode()).isEqualTo(tokens2.hashCode());
    }

    @Test
    void shouldBeUsableAsMapKey_whenSameValue() {
        // Given
        TokenCount key1 = TokenCount.of(100);
        TokenCount key2 = TokenCount.of(100);

        // When & Then
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        assertThat(key1).isEqualTo(key2);
    }

    // ========== String Representation Tests ==========

    @Test
    void shouldReturnStringValue_whenToStringCalled() {
        // Given
        TokenCount tokens = TokenCount.of(100);

        // When
        String result = tokens.toString();

        // Then
        assertThat(result).isEqualTo("100");
    }

    @Test
    void shouldReturnZeroString_whenZeroTokenCount() {
        // Given
        TokenCount zero = TokenCount.zero();

        // When
        String result = zero.toString();

        // Then
        assertThat(result).isEqualTo("0");
    }

    // ========== Edge Cases ==========

    @Test
    void shouldHandleOne_whenMinimalPositiveValue() {
        // When
        TokenCount one = TokenCount.of(1);

        // Then
        assertThat(one.getValue()).isEqualTo(1);
    }

    @Test
    void shouldHandleVeryLargeValue_whenWithinIntegerRange() {
        // Given
        int largeValue = 10_000_000;

        // When
        TokenCount tokens = TokenCount.of(largeValue);

        // Then
        assertThat(tokens.getValue()).isEqualTo(largeValue);
    }
}
