package com.price.manager.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.price.manager.utils.PriceDomainMocks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests para entidad de dominio Price - SIN Spring
 */
@DisplayName("Price Domain Entity Tests")
class PriceTest {

    private PriceDomainMocks mocks;

    @BeforeEach
    void setUp() {
        this.mocks = new PriceDomainMocks();
    }

    @Test
    @DisplayName("Should validate date range correctly")
    void shouldValidateDateRangeCorrectly() {
        // Given
        final var price = this.mocks.createValidPrice();
        final var validDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        final var invalidDateBefore = LocalDateTime.of(2020, 6, 13, 23, 59);
        final var invalidDateAfter = LocalDateTime.of(2021, 1, 1, 0, 1);

        // When & Then
        assertTrue(price.isValidAt(validDate));
        assertFalse(price.isValidAt(invalidDateBefore));
        assertFalse(price.isValidAt(invalidDateAfter));
    }

    @Test
    @DisplayName("Should handle null dates in isValidAt")
    void shouldHandleNullDatesInIsValidAt() {
        // Given
        final var price = this.mocks.createValidPrice();
        final var priceWithNullStart = price.toBuilder().startDate(null).build();
        final var priceWithNullEnd = price.toBuilder().endDate(null).build();

        // When & Then
        assertFalse(price.isValidAt(null));
        assertFalse(priceWithNullStart.isValidAt(LocalDateTime.now()));
        assertFalse(priceWithNullEnd.isValidAt(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should compare priorities according to Business rules")
    void shouldComparePrioritiesAccordingToBusinessRules() {
        // Given
        final var lowPriority = this.createPriceWithPriority(0, 1L);
        final var highPriority = this.createPriceWithPriority(1, 2L);
        final var samePriorityHigherList = this.createPriceWithPriority(1, 3L);

        // When & Then
        assertTrue(highPriority.hasHigherPriorityThan(lowPriority));
        assertFalse(lowPriority.hasHigherPriorityThan(highPriority));
        assertTrue(samePriorityHigherList.hasHigherPriorityThan(highPriority));
    }

    @Test
    @DisplayName("Should handle null comparison in hasHigherPriorityThan")
    void shouldHandleNullComparisonInHasHigherPriorityThan() {
        // Given
        final var price = this.mocks.createValidPrice();

        // When & Then
        assertTrue(price.hasHigherPriorityThan(null));
    }

    @Test
    @DisplayName("Should handle same priority comparison")
    void shouldHandleSamePriorityComparison() {
        // Given
        final var price1 = this.createPriceWithPriority(1, 2L);
        final var price2 = this.createPriceWithPriority(1, 2L); // Misma prioridad y priceList

        // When & Then
        assertFalse(price1.hasHigherPriorityThan(price2));
        assertFalse(price2.hasHigherPriorityThan(price1));
    }

    @Test
    @DisplayName("Should validate price consistency correctly")
    void shouldValidatePriceConsistencyCorrectly() {
        // Given
        final var validPrice = this.mocks.createValidPrice();

        // When & Then
        assertTrue(validPrice.isConsistent());
    }

    @Test
    @DisplayName("Should detect invalid brandId")
    void shouldDetectInvalidBrandId() {
        // Given
        final var invalidBrandId = this.mocks.createValidPrice().toBuilder().brandId(-1L).build();
        final var nullBrandId = this.mocks.createValidPrice().toBuilder().brandId(null).build();

        // When & Then
        assertFalse(invalidBrandId.isConsistent());
        assertFalse(nullBrandId.isConsistent());
    }

    @Test
    @DisplayName("Should detect invalid productId")
    void shouldDetectInvalidProductId() {
        // Given
        final var invalidProductId = this.mocks.createValidPrice().toBuilder().productId(-1L).build();
        final var nullProductId = this.mocks.createValidPrice().toBuilder().productId(null).build();

        // When & Then
        assertFalse(invalidProductId.isConsistent());
        assertFalse(nullProductId.isConsistent());
    }

    @Test
    @DisplayName("Should detect invalid priority")
    void shouldDetectInvalidPriority() {
        // Given
        final var invalidPriority = this.mocks.createValidPrice().toBuilder().priority(-1).build();
        final var nullPriority = this.mocks.createValidPrice().toBuilder().priority(null).build();

        // When & Then
        assertFalse(invalidPriority.isConsistent());
        assertFalse(nullPriority.isConsistent());
    }

    @Test
    @DisplayName("Should detect invalid price")
    void shouldDetectInvalidPrice() {
        // Given
        final var negativePrice = this.mocks.createValidPrice().toBuilder().price(new BigDecimal("-10.00")).build();
        final var zeroPrice = this.mocks.createValidPrice().toBuilder().price(BigDecimal.ZERO).build();
        final var nullPrice = this.mocks.createValidPrice().toBuilder().price(null).build();

        // When & Then
        assertFalse(negativePrice.isConsistent());
        assertFalse(zeroPrice.isConsistent());
        assertFalse(nullPrice.isConsistent());
    }

    @Test
    @DisplayName("Should detect invalid date range")
    void shouldDetectInvalidDateRange() {
        // Given
        final var invalidDateRange = this.mocks.createValidPrice().toBuilder()
                .startDate(LocalDateTime.of(2020, 6, 15, 0, 0))
                .endDate(LocalDateTime.of(2020, 6, 14, 0, 0)) // End antes que start
                .build();
        final var nullStartDate = this.mocks.createValidPrice().toBuilder().startDate(null).build();
        final var nullEndDate = this.mocks.createValidPrice().toBuilder().endDate(null).build();

        // When & Then
        assertFalse(invalidDateRange.isConsistent());
        assertFalse(nullStartDate.isConsistent());
        assertFalse(nullEndDate.isConsistent());
    }

    @Test
    @DisplayName("Should detect invalid currency")
    void shouldDetectInvalidCurrency() {
        // Given
        final var emptyCurrency = this.mocks.createValidPrice().toBuilder().curr("").build();
        final var whitespaceCurrency = this.mocks.createValidPrice().toBuilder().curr("   ").build();
        final var nullCurrency = this.mocks.createValidPrice().toBuilder().curr(null).build();

        // When & Then
        assertFalse(emptyCurrency.isConsistent());
        assertFalse(whitespaceCurrency.isConsistent());
        assertFalse(nullCurrency.isConsistent());
    }

    private Price createPriceWithPriority(int priority, Long priceList) {
        return this.mocks.createValidPrice().toBuilder()
                .priority(priority)
                .priceList(priceList)
                .build();
    }
}
