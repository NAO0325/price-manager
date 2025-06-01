package com.price.manager.application.services;

import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.domain.criteria.PriceSearchCriteria;
import com.price.manager.utils.PriceDomainMocks;
import com.price.manager.domain.Price;
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
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceUseCaseTest {

    @Mock
    private PriceRepositoryPort priceRepositoryPort;

    @InjectMocks
    private PriceServiceUseCase priceServiceUseCase;

    private PriceDomainMocks mocks;

    @BeforeEach
    void setUp() {
        mocks = new PriceDomainMocks();
    }

    @Test
    void findByBrandProductBetweenDate_WithValidParameters_ShouldReturnPrice() {
        // Given
        var brandId = 1L;
        var productId = 35455L;
        var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        var expectedPrice = mocks.mockListTest1().get(0); // Precio base: 35.5

        when(priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(expectedPrice));

        // When
        var result = priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result);
        assertEquals(expectedPrice, result);
        assertEquals(35.5, result.getPrice());
        assertEquals(0, result.getPriority());
        assertEquals(1L, result.getPriceList());

        verify(priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }

    @Test
    void findByBrandProductBetweenDate_WithNoResults_ShouldReturnNull() {
        // Given
        var brandId = 1L;
        var productId = 35455L;
        var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        when(priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.empty());

        // When
        var result = priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNull(result);
        verify(priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }

    @ParameterizedTest(name = "Test {index}: {3} - Expected price: {4}")
    @MethodSource("provideRealBusinessScenarios")
    void findByBrandProductBetweenDate_RealBusinessScenarios_ShouldReturnCorrectPrice(
            Long brandId,
            Long productId,
            LocalDateTime queryDate,
            String testDescription,
            double expectedPrice,
            Price mockPrice) {

        // Given - Usar mocks reales de casos de negocio
        when(priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(mockPrice));

        // When
        var result = priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result, "Result should not be null for: " + testDescription);
        assertEquals(brandId, result.getBrandId(), "Brand ID should match");
        assertEquals(productId, result.getProductId(), "Product ID should match");
        assertEquals(expectedPrice, result.getPrice(), "Price should match expected value for: " + testDescription);

        verify(priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }

    static Stream<Arguments> provideRealBusinessScenarios() {
        var mocks = new PriceDomainMocks();

        return Stream.of(
                Arguments.of(
                        1L, 35455L,
                        LocalDateTime.of(2020, 6, 14, 10, 0, 0),
                        "Test 1: Morning query - base price should apply",
                        35.5,
                        mocks.mockListTest1().get(0)
                ),
                Arguments.of(
                        1L, 35455L,
                        LocalDateTime.of(2020, 6, 14, 16, 0, 0),
                        "Test 2: Afternoon promotion - higher priority price should apply",
                        25.45,
                        mocks.mockListTest2().stream()
                                .filter(p -> p.getPriority() == 1)
                                .findFirst()
                                .orElse(mocks.mockListTest2().get(0))
                ),
                Arguments.of(
                        1L, 35455L,
                        LocalDateTime.of(2020, 6, 14, 21, 0, 0),
                        "Test 3: Evening query - base price should apply",
                        35.5,
                        mocks.mockListTest3().get(0)
                ),
                Arguments.of(
                        1L, 35455L,
                        LocalDateTime.of(2020, 6, 15, 10, 0, 0),
                        "Test 4: Morning special price - higher priority should win",
                        30.5,
                        mocks.mockListTest4().stream()
                                .filter(p -> p.getPriority() == 1)
                                .findFirst()
                                .orElse(mocks.mockListTest4().get(0))
                ),
                Arguments.of(
                        1L, 35455L,
                        LocalDateTime.of(2020, 6, 16, 21, 0, 0),
                        "Test 5: Premium evening price - higher priority should win",
                        38.95,
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
        var expectedPrice = mocks.mockListTest1().get(0);

        when(priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(expectedPrice));

        // When
        priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        verify(priceRepositoryPort).findBestPrice(argThat(criteria ->
                criteria.brandId().equals(brandId) &&
                        criteria.productId().equals(productId) &&
                        criteria.queryDate().equals(queryDate)
        ));
    }

    @Test
    void findByBrandProductBetweenDate_ShouldBeEfficient() {
        // Given
        var brandId = 1L;
        var productId = 35455L;
        var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        var mockPrice = mocks.mockListTest1().get(0);

        when(priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(mockPrice));

        // When
        var result = priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result);
        assertEquals(mockPrice, result);

        verify(priceRepositoryPort, times(1)).findBestPrice(any());
        verifyNoMoreInteractions(priceRepositoryPort);
    }

    @Test
    void findByBrandProductBetweenDate_WithHighPriorityScenario_ShouldReturnCorrectPrice() {
        // Given
        var brandId = 1L;
        var productId = 35455L;
        var queryDate = LocalDateTime.of(2020, 6, 14, 16, 0, 0); // Hora de promoción

        var highPriorityPrice = mocks.mockListTest2().stream()
                .filter(p -> p.getPriority() == 1)
                .findFirst()
                .orElseThrow();

        when(priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(highPriorityPrice));

        // When
        var result = priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result);
        assertEquals(25.45, result.getPrice());
        assertEquals(1, result.getPriority());
        assertEquals(2L, result.getPriceList());
        assertEquals("EUR", result.getCurr());

        verify(priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }

    @Test
    void findByBrandProductBetweenDate_WithDateBoundaryConditions_ShouldWork() {
        // Given
        var brandId = 1L;
        var productId = 35455L;

        var boundaryDate = LocalDateTime.of(2020, 6, 14, 0, 0, 0);
        var mockPrice = mocks.mockListTest1().get(0);

        when(priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(mockPrice));

        // When
        var result = priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, boundaryDate);

        // Then
        assertNotNull(result);
        assertEquals(35.5, result.getPrice());
        assertEquals(boundaryDate, result.getStartDate());

        verify(priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }
}