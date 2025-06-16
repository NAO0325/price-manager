package com.price.manager.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;
import com.price.manager.utils.PriceDomainMocks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Price Service Use Case - Complete Tests")
class PriceServiceUseCaseTest {

    @Mock
    private PriceRepositoryPort priceRepositoryPort;

    @InjectMocks
    private PriceServiceUseCase priceServiceUseCase;

    private PriceDomainMocks mocks;

    @BeforeEach
    void setUp() {
        this.mocks = new PriceDomainMocks();
    }

    /**
     * Casos de prueba específicos según requerimientos de negocio.
     */
    static Stream<Arguments> businessScenarios() {
        return Stream.of(
                Arguments.of(
                        LocalDateTime.of(2020, 6, 14, 10, 0, 0),
                        PriceDomainMocks.createExpectedPrice(
                                1L, 0, "35.50", "2020-06-14T00:00:00", "2020-12-31T23:59:59"
                        ),
                        "Test 1: 10:00 día 14 - Precio base 35.50€"
                ),
                Arguments.of(
                        LocalDateTime.of(2020, 6, 14, 16, 0, 0),
                        PriceDomainMocks.createExpectedPrice(
                                2L, 1, "25.45", "2020-06-14T15:00:00", "2020-06-14T18:30:00"
                        ),
                        "Test 2: 16:00 día 14 - Promoción tarde 25.45€"
                ),
                Arguments.of(
                        LocalDateTime.of(2020, 6, 14, 21, 0, 0),
                        PriceDomainMocks.createExpectedPrice(
                                1L, 0, "35.50", "2020-06-14T00:00:00", "2020-12-31T23:59:59"
                        ),
                        "Test 3: 21:00 día 14 - Vuelve a precio base 35.50€"
                ),
                Arguments.of(
                        LocalDateTime.of(2020, 6, 15, 10, 0, 0),
                        PriceDomainMocks.createExpectedPrice(
                                3L, 1, "30.50", "2020-06-15T00:00:00", "2020-06-15T11:00:00"
                        ),
                        "Test 4: 10:00 día 15 - Promoción mañana 30.50€"
                ),
                Arguments.of(
                        LocalDateTime.of(2020, 6, 16, 21, 0, 0),
                        PriceDomainMocks.createExpectedPrice(
                                4L, 1, "38.95", "2020-06-15T16:00:00", "2020-12-31T23:59:59"
                        ),
                        "Test 5: 21:00 día 16 - Precio premium 38.95€"
                )
        );
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("businessScenarios")
    @DisplayName("Should return correct price for business scenarios")
    void shouldReturnCorrectPriceForBusinessScenarios(
            LocalDateTime queryDate,
            Price expectedPrice,
            String scenarioDescription) {

        // Given
        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(expectedPrice));

        // When
        final Price result = this.priceServiceUseCase.findByBrandProductBetweenDate(1L, 35455L, queryDate);

        // Then - Usando solo JUnit assertions
        assertNotNull(result, "Result should not be null for: " + scenarioDescription);
        assertEquals(1L, result.getBrandId(), "Brand ID should match");
        assertEquals(35455L, result.getProductId(), "Product ID should match");
        assertEquals(expectedPrice.getPrice(), result.getPrice(), "Price should match expected value");
        assertEquals(expectedPrice.getPriceList(), result.getPriceList(), "Price list should match");
        assertEquals(expectedPrice.getPriority(), result.getPriority(), "Priority should match");
        assertEquals("EUR", result.getCurr(), "Currency should be EUR");

        // Verificar que se llamó al repositorio con los parámetros correctos
        final ArgumentCaptor<PriceSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(PriceSearchCriteria.class);
        verify(this.priceRepositoryPort).findBestPrice(criteriaCaptor.capture());

        final PriceSearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertEquals(1L, capturedCriteria.brandId());
        assertEquals(35455L, capturedCriteria.productId());
        assertEquals(queryDate, capturedCriteria.queryDate());
    }

    @Test
    @DisplayName("Should return price when repository finds valid result")
    void shouldReturnPriceWhenRepositoryFindsValidResult() {
        // Given
        final Long brandId = 1L;
        final Long productId = 35455L;
        final LocalDateTime queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        final Price expectedPrice = this.mocks.createValidPrice();

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(expectedPrice));

        // When
        final Price result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result);
        assertEquals(expectedPrice, result);
        assertEquals(expectedPrice.getBrandId(), result.getBrandId());
        assertEquals(expectedPrice.getProductId(), result.getProductId());
        assertEquals(expectedPrice.getPrice(), result.getPrice());

        verify(this.priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }

    @Test
    @DisplayName("Should return null when repository finds no result")
    void shouldReturnNullWhenRepositoryFindsNoResult() {
        // Given
        final Long brandId = 2L; // Marca que no existe
        final Long productId = 99999L; // Producto que no existe
        final LocalDateTime queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.empty());

        // When
        final Price result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNull(result, "Result should be null when no price is found");
        verify(this.priceRepositoryPort, times(1)).findBestPrice(any(PriceSearchCriteria.class));
    }

    @Test
    @DisplayName("Should create correct search criteria")
    void shouldCreateCorrectSearchCriteria() {
        // Given
        final Long brandId = 1L;
        final Long productId = 35455L;
        final LocalDateTime queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(this.mocks.createValidPrice()));

        // When
        this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        final ArgumentCaptor<PriceSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(PriceSearchCriteria.class);
        verify(this.priceRepositoryPort).findBestPrice(criteriaCaptor.capture());

        final PriceSearchCriteria criteria = criteriaCaptor.getValue();
        assertEquals(brandId, criteria.brandId(), "Brand ID in criteria should match input");
        assertEquals(productId, criteria.productId(), "Product ID in criteria should match input");
        assertEquals(queryDate, criteria.queryDate(), "Query date in criteria should match input");
    }

    @Test
    @DisplayName("Should handle repository exceptions gracefully")
    void shouldHandleRepositoryExceptionsGracefully() {
        // Given
        final Long brandId = 1L;
        final Long productId = 35455L;
        final LocalDateTime queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate),
                "Should propagate repository exceptions"
        );

        assertEquals("Database connection failed", exception.getMessage());
        verify(this.priceRepositoryPort, times(1)).findBestPrice(any());
    }

    @Test
    @DisplayName("Should only call repository once per request")
    void shouldOnlyCallRepositoryOncePerRequest() {
        // Given
        final Long brandId = 1L;
        final Long productId = 35455L;
        final LocalDateTime queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(this.mocks.createValidPrice()));

        // When
        final Price result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result);
        verify(this.priceRepositoryPort, times(1)).findBestPrice(any());
        verifyNoMoreInteractions(this.priceRepositoryPort);
    }

    @Test
    @DisplayName("Should handle minimum valid values")
    void shouldHandleMinimumValidValues() {
        // Given
        final Long brandId = 1L; // Mínimo valor válido
        final Long productId = 1L; // Mínimo valor válido
        final LocalDateTime queryDate = LocalDateTime.of(1970, 1, 1, 0, 0, 0); // Época Unix

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(this.mocks.createValidPrice()));

        // When
        final Price result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result, "Should handle minimum valid values");
        verify(this.priceRepositoryPort, times(1)).findBestPrice(any());
    }

    @Test
    @DisplayName("Should handle large valid values")
    void shouldHandleLargeValidValues() {
        // Given
        final Long brandId = 999999L;
        final Long productId = 999999L;
        final LocalDateTime queryDate = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(this.mocks.createValidPrice()));

        // When
        final Price result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result, "Should handle large valid values");
        verify(this.priceRepositoryPort, times(1)).findBestPrice(any());
    }

    @Test
    @DisplayName("Should validate service behavior for exact scenario 1")
    void shouldValidateServiceBehaviorForExactScenario1() {
        // Given - Test 1: petición a las 10:00 del día 14
        final Long brandId = 1L;
        final Long productId = 35455L;
        final LocalDateTime queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        final Price expectedPrice = PriceDomainMocks.createExpectedPrice(1L, 0, "35.50",
                "2020-06-14T00:00:00", "2020-12-31T23:59:59");

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(expectedPrice));

        // When
        final Price result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("35.50"), result.getPrice());
        assertEquals(1L, result.getPriceList());
        assertEquals(0, result.getPriority());
        assertEquals("EUR", result.getCurr());
    }

    @Test
    @DisplayName("Should validate service behavior for exact scenario 2")
    void shouldValidateServiceBehaviorForExactScenario2() {
        // Given - Test 2: petición a las 16:00 del día 14 (promoción)
        final Long brandId = 1L;
        final Long productId = 35455L;
        final LocalDateTime queryDate = LocalDateTime.of(2020, 6, 14, 16, 0, 0);
        final Price expectedPrice = PriceDomainMocks.createExpectedPrice(2L, 1, "25.45",
                "2020-06-14T15:00:00", "2020-06-14T18:30:00");

        when(this.priceRepositoryPort.findBestPrice(any(PriceSearchCriteria.class)))
                .thenReturn(Optional.of(expectedPrice));

        // When
        final Price result = this.priceServiceUseCase.findByBrandProductBetweenDate(brandId, productId, queryDate);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("25.45"), result.getPrice());
        assertEquals(2L, result.getPriceList());
        assertEquals(1, result.getPriority());
        assertEquals("EUR", result.getCurr());
    }
}
