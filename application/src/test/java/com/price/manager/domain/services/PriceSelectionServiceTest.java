package com.price.manager.domain.services;

import com.price.manager.domain.Price;
import com.price.manager.utils.PriceDomainMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PriceSelectionServiceTest {

    private PriceSelectionService priceSelectionService;
    private PriceDomainMocks mocks;

    @BeforeEach
    void setUp() {
        priceSelectionService = new PriceSelectionService();
        mocks = new PriceDomainMocks();
    }

    @Test
    void selectBestPrice_WithEmptyList_ShouldReturnEmpty() {
        // Given
        List<Price> prices = List.of();

        // When
        var result = priceSelectionService.selectBestPrice(prices);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void selectBestPrice_WithSinglePrice_ShouldReturnThatPrice() {
        // Given
        var prices = mocks.mockListTest1(); // Single price scenario

        // When
        var result = priceSelectionService.selectBestPrice(prices);

        // Then
        assertTrue(result.isPresent());
        var selectedPrice = result.get();
        assertEquals(35.5, selectedPrice.getPrice());
        assertEquals(0, selectedPrice.getPriority());
        assertEquals(1L, selectedPrice.getPriceList());
    }

    @ParameterizedTest(name = "Test {index}: {2}")
    @MethodSource("providePriceSelectionScenariosFromMocks")
    void selectBestPrice_RealBusinessScenarios(
            List<Price> prices,
            double expectedPrice,
            String testDescription,
            Integer expectedPriority) {

        // When
        var result = priceSelectionService.selectBestPrice(prices);

        // Then
        assertTrue(result.isPresent(), "Should find a price for: " + testDescription);
        assertEquals(expectedPrice, result.get().getPrice(),
                "Price should match for: " + testDescription);
        assertEquals(expectedPriority, result.get().getPriority(),
                "Priority should match for: " + testDescription);
    }

    static Stream<Arguments> providePriceSelectionScenariosFromMocks() {
        var mocks = new PriceDomainMocks();

        return Stream.of(
                Arguments.of(
                        mocks.mockListTest1(),
                        35.5,
                        "Single price scenario - should return the only available price",
                        0
                ),
                Arguments.of(
                        mocks.mockListTest2(),
                        25.45,
                        "Multiple prices - higher priority (1) should win over lower (0)",
                        1
                ),
                Arguments.of(
                        mocks.mockListTest3(),
                        35.5,
                        "Single price at evening - should return available price",
                        0
                ),
                Arguments.of(
                        mocks.mockListTest4(),
                        30.5,
                        "Morning scenario - higher priority (1) should win",
                        1
                ),
                Arguments.of(
                        mocks.mockListTest5(),
                        38.95,
                        "Evening scenario - higher priority (1) should win",
                        1
                )
        );
    }

    @Test
    void selectBestPrice_WithCustomPriorityScenario_ShouldSelectHighestPriority() {
        // Given - Create a custom scenario not covered by existing mocks
        var lowPriorityPrice = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priority(0)
                .price(50.0)
                .priceList(1L)
                .curr("EUR")
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59))
                .build();

        var mediumPriorityPrice = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priority(1)
                .price(40.0)
                .priceList(2L)
                .curr("EUR")
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59))
                .build();

        var highPriorityPrice = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priority(2)
                .price(30.0)
                .priceList(3L)
                .curr("EUR")
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59))
                .build();

        List<Price> prices = List.of(lowPriorityPrice, mediumPriorityPrice, highPriorityPrice);

        // When
        var result = priceSelectionService.selectBestPrice(prices);

        // Then
        assertTrue(result.isPresent());
        assertEquals(highPriorityPrice, result.get());
        assertEquals(2, result.get().getPriority());
        assertEquals(30.0, result.get().getPrice());
    }

    @Test
    void selectBestPrice_WithSamePriorityDifferentPriceList_ShouldSelectHigherPriceList() {
        // Given - Test the secondary sorting criteria
        var price1 = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priority(1)
                .price(25.0)
                .priceList(1L)
                .curr("EUR")
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59))
                .build();

        var price2 = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priority(1)
                .price(30.0)
                .priceList(3L)  // Higher priceList should win
                .curr("EUR")
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59))
                .build();

        List<Price> prices = List.of(price1, price2);

        // When
        var result = priceSelectionService.selectBestPrice(prices);

        // Then
        assertTrue(result.isPresent());
        assertEquals(price2, result.get());
        assertEquals(3L, result.get().getPriceList());
        assertEquals(30.0, result.get().getPrice());
    }

    @Test
    void selectBestPrice_WithNullList_ShouldReturnEmpty() {
        // Given
        List<Price> prices = null;

        // When
        var result = priceSelectionService.selectBestPrice(prices);

        // Then
        assertTrue(result.isEmpty());
    }
}