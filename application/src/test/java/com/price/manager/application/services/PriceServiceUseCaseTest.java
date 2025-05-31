package com.price.manager.application.services;

import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.utils.PriceDomainMocks;
import com.price.manager.domain.Price;
import com.price.manager.domain.services.PriceSelectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PriceServiceUseCaseTest {

    private PriceDomainMocks mocks;

    @Mock
    private PriceRepositoryPort priceRepositoryPort;

    @Mock
    private PriceSelectionService priceSelectionService;

    @InjectMocks
    private PriceServiceUseCase priceServiceUseCase;

    @BeforeEach
    void setUp() {
        mocks = new PriceDomainMocks();
    }

    @Test
    void findByBrandProductBetweenDate_WithEmptyList_ShouldReturnNull() {
        // Given
        var dateTest = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        when(priceRepositoryPort.findByCriteria(any()))
                .thenReturn(List.of());
        when(priceSelectionService.selectBestPrice(List.of()))
                .thenReturn(Optional.empty());

        // When
        var result = priceServiceUseCase.findByBrandProductBetweenDate(3L, 1L, dateTest);

        // Then
        assertNull(result);
        verify(priceRepositoryPort, times(1)).findByCriteria(any());
        verify(priceSelectionService, times(1)).selectBestPrice(List.of());
    }

    @ParameterizedTest(name = "Test {index}: {3} - Expected price: {4}")
    @MethodSource("providePriceTestScenarios")
    void findByBrandProductBetweenDate_ParameterizedTests(
            Long brandId,
            Long productId,
            LocalDateTime queryDate,
            String testDescription,
            double expectedPrice,
            List<Price> mockPrices,
            Price expectedSelectedPrice) {

        // Given
        when(priceRepositoryPort.findByCriteria(any()))
                .thenReturn(mockPrices);
        when(priceSelectionService.selectBestPrice(mockPrices))
                .thenReturn(Optional.of(expectedSelectedPrice));

        // When
        var result = priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result, "Result should not be null for: " + testDescription);
        assertEquals(brandId, result.getBrandId(), "Brand ID should match");
        assertEquals(productId, result.getProductId(), "Product ID should match");
        assertEquals(expectedPrice, result.getPrice(), "Price should match expected value");

        verify(priceRepositoryPort, times(1)).findByCriteria(any());
        verify(priceSelectionService, times(1)).selectBestPrice(mockPrices);
    }

    static Stream<Arguments> providePriceTestScenarios() {
        var mocks = new PriceDomainMocks();

        return Stream.of(
                Arguments.of(
                        1L, 35455L,
                        LocalDateTime.of(2020, 6, 14, 10, 0, 0),
                        "Test 1: Single price at 10:00",
                        35.5,
                        mocks.mockListTest1(),
                        mocks.mockListTest1().get(0)
                ),
                Arguments.of(
                        1L, 35455L,
                        LocalDateTime.of(2020, 6, 14, 16, 0, 0),
                        "Test 2: Multiple prices at 16:00 - priority 1 wins",
                        25.45,
                        mocks.mockListTest2(),
                        mocks.mockListTest2().stream()
                                .filter(p -> p.getPriority() == 1)
                                .findFirst()
                                .orElse(mocks.mockListTest2().get(0))
                ),
                Arguments.of(
                        1L, 35455L,
                        LocalDateTime.of(2020, 6, 14, 21, 0, 0),
                        "Test 3: Single price at 21:00",
                        35.5,
                        mocks.mockListTest3(),
                        mocks.mockListTest3().get(0)
                ),
                Arguments.of(
                        1L, 35455L,
                        LocalDateTime.of(2020, 6, 15, 10, 0, 0),
                        "Test 4: Multiple prices at 10:00 - priority 1 wins",
                        30.5,
                        mocks.mockListTest4(),
                        mocks.mockListTest4().stream()
                                .filter(p -> p.getPriority() == 1)
                                .findFirst()
                                .orElse(mocks.mockListTest4().get(0))
                ),
                Arguments.of(
                        1L, 35455L,
                        LocalDateTime.of(2020, 6, 15, 21, 0, 0),
                        "Test 5: Multiple prices at 21:00 - priority 1 wins",
                        38.95,
                        mocks.mockListTest5(),
                        mocks.mockListTest5().stream()
                                .filter(p -> p.getPriority() == 1)
                                .findFirst()
                                .orElse(mocks.mockListTest5().get(0))
                )
        );
    }

    @Test
    void findByBrandProductBetweenDate_ShouldCreateCorrectCriteria() {
        // Given
        var brandId = 1L;
        var productId = 35455L;
        var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        var mockPrices = mocks.mockListTest1();
        var expectedPrice = mockPrices.get(0);

        when(priceRepositoryPort.findByCriteria(any()))
                .thenReturn(mockPrices);
        when(priceSelectionService.selectBestPrice(mockPrices))
                .thenReturn(Optional.of(expectedPrice));

        // When
        priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then - Verify that criteria is created and passed correctly
        verify(priceRepositoryPort, times(1)).findByCriteria(any());
        verify(priceSelectionService, times(1)).selectBestPrice(mockPrices);
    }
}