package com.price.manager.application.services;

import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;
import com.price.manager.utils.PriceDomainMocks;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceServiceUseCaseTest {

    @Mock
    private PriceRepositoryPort priceRepositoryPort;

    @InjectMocks
    private PriceServiceUseCase priceServiceUseCase;

    private PriceDomainMocks mocks;

    static Stream<Arguments> provideRealBusinessScenarios() {
        final var mocks = new PriceDomainMocks();

        return Stream.of(
                Arguments.of(
                        1L,
                        35455L,
                        LocalDateTime.of(2020, 6, 14, 10, 0, 0),
                        "Test 1: Morning query - base price should apply",
                        35.5,
                        mocks.mockListTest1().get(0)
                ),
                Arguments.of(
                        1L,
                        35455L,
                        LocalDateTime.of(2020, 6, 14, 16, 0, 0),
                        "Test 2: Afternoon promotion - higher priority price should apply",
                        25.45,
                        mocks.mockListTest2()
                                .stream()
                                .filter(p -> p.getPriority() == 1)
                                .findFirst()
                                .orElse(mocks.mockListTest2().get(0))
                ),
                Arguments.of(
                        1L,
                        35455L,
                        LocalDateTime.of(2020, 6, 14, 21, 0, 0),
                        "Test 3: Evening query - base price should apply",
                        35.5,
                        mocks.mockListTest3().get(0)
                ),
                Arguments.of(
                        1L,
                        35455L,
                        LocalDateTime.of(2020, 6, 15, 10, 0, 0),
                        "Test 4: Morning special price - higher priority should win",
                        30.5,
                        mocks.mockListTest4()
                                .stream()
                                .filter(p -> p.getPriority() == 1)
                                .findFirst()
                                .orElse(mocks.mockListTest4().get(0))
                ),
                Arguments.of(
                        1L,
                        35455L,
                        LocalDateTime.of(2020, 6, 16, 21, 0, 0),
                        "Test 5: Premium evening price - higher priority should win",
                        38.95,
                        mocks.mockListTest5()
                                .stream()
                                .filter(p -> p.getPriority() == 1)
                                .findFirst()
                                .orElse(mocks.mockListTest5().get(0))
                )
        );
    }

    @BeforeEach
    void setUp() {
        this.mocks = new PriceDomainMocks();
    }

    @Test
    void findByBrandProductBetweenDateWithValidParametersShouldReturnPrice() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        final var expectedPrice = this.mocks.mockListTest1().get(0); // Precio base: 35.5

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(expectedPrice));

        // When
        final var result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result);
        assertEquals(expectedPrice, result);
        assertEquals(35.5, result.getPrice());
        assertEquals(0, result.getPriority());
        assertEquals(1L, result.getPriceList());

        verify(this.priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }

    @Test
    void findByBrandProductBetweenDateWithNoResultsShouldReturnNull() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class))).thenReturn(Optional.empty());

        // When
        final var result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNull(result);
        verify(this.priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }

    @ParameterizedTest(name = "Test {index}: {3} - Expected price: {4}")
    @MethodSource("provideRealBusinessScenarios")
    void findByBrandProductBetweenDateRealBusinessScenariosShouldReturnCorrectPrice(
            Long brandId,
            Long productId,
            LocalDateTime queryDate,
            String testDescription,
            double expectedPrice,
            Price mockPrice) {

        // Given - Usar mocks reales de casos de negocio
        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class))).thenReturn(Optional.of(mockPrice));

        // When
        final var result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result, "Result should not be null for: " + testDescription);
        assertEquals(brandId, result.getBrandId(), "Brand ID should match");
        assertEquals(productId, result.getProductId(), "Product ID should match");
        assertEquals(expectedPrice, result.getPrice(), "Price should match expected value for: " + testDescription);

        verify(this.priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }

    @Test
    void findByBrandProductBetweenDateShouldCreateCorrectCriteria() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        final var expectedPrice = this.mocks.mockListTest1().get(0);

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(expectedPrice));

        // When
        this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        verify(this.priceRepositoryPort).findBestPrice(argThat(criteria ->
                criteria.brandId().equals(brandId)
                        && criteria.productId().equals(productId)
                        && criteria.queryDate().equals(queryDate)));
    }

    @Test
    void findByBrandProductBetweenDateShouldBeEfficient() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        final var mockPrice = this.mocks.mockListTest1().get(0);

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class))).thenReturn(Optional.of(mockPrice));

        // When
        final var result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result);
        assertEquals(mockPrice, result);

        verify(this.priceRepositoryPort, times(1)).findBestPrice(any());
        verifyNoMoreInteractions(this.priceRepositoryPort);
    }

    @Test
    void findByBrandProductBetweenDateWithHighPriorityScenarioShouldReturnCorrectPrice() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var queryDate = LocalDateTime.of(2020, 6, 14, 16, 0, 0); // Hora de promoción

        final var highPriorityPrice = this.mocks.mockListTest2()
                .stream()
                .filter(p -> p.getPriority() == 1)
                .findFirst()
                .orElseThrow();

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(highPriorityPrice));

        // When
        final var result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result);
        assertEquals(25.45, result.getPrice());
        assertEquals(1, result.getPriority());
        assertEquals(2L, result.getPriceList());
        assertEquals("EUR", result.getCurr());

        verify(this.priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }

    @Test
    void findByBrandProductBetweenDateWithDateBoundaryConditionsShouldWork() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;

        final var boundaryDate = LocalDateTime.of(2020, 6, 14, 0, 0, 0);
        final var mockPrice = this.mocks.mockListTest1().get(0);

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class))).thenReturn(Optional.of(mockPrice));

        // When
        final var result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, boundaryDate);

        // Then
        assertNotNull(result);
        assertEquals(35.5, result.getPrice());
        assertEquals(boundaryDate, result.getStartDate());

        verify(this.priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }
}
