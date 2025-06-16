package com.price.manager.driven.repositories.adapters;

import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;
import com.price.manager.driven.repositories.PriceJpaRepository;
import com.price.manager.driven.repositories.mappers.PriceEntityMapper;
import com.price.manager.driven.repositories.models.PriceEntity;
import com.price.manager.driven.repositories.utils.PriceRepositoryMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


/**
 * Tests mejorados para PriceRepositoryAdapter con casos adicionales.
 * Mantiene los tests existentes y añade validaciones de error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Price Repository Adapter - Enhanced Tests")
class PriceRepositoryAdapterTest {

    private PriceRepositoryMocks mocks;

    @Mock
    private PriceJpaRepository repository;

    @Mock
    private PriceEntityMapper mapper;

    @InjectMocks
    private PriceRepositoryAdapter priceRepositoryAdapter;

    private PriceSearchCriteria testCriteria;
    private PriceEntity testEntity;
    private Price testPrice;

    @BeforeEach
    void setUp() {
        this.mocks = new PriceRepositoryMocks();
        this.testCriteria = PriceSearchCriteria.of(1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0));
        this.testEntity = this.mocks.createTestPriceEntity();
        this.testPrice = this.mocks.createTestPrice();
    }

    /**
     * Casos de prueba con diferentes escenarios de búsqueda
     */
    static Stream<Arguments> searchScenarios() {
        return Stream.of(
                Arguments.of(
                        1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0),
                        "Test 1 scenario: Morning query should find base price"
                ),
                Arguments.of(
                        1L, 35455L, LocalDateTime.of(2020, 6, 14, 16, 0, 0),
                        "Test 2 scenario: Afternoon query should find promotion price"
                ),
                Arguments.of(
                        2L, 67890L, LocalDateTime.of(2021, 3, 15, 14, 30, 45),
                        "Different brand and product scenario"
                )
        );
    }

    @ParameterizedTest(name = "{3}")
    @MethodSource("searchScenarios")
    @DisplayName("Should return price when entity found and mapped successfully")
    void shouldReturnPriceWhenEntityFoundAndMappedSuccessfully(
            Long brandId, Long productId, LocalDateTime queryDate, String description) {

        // Given
        final var criteria = PriceSearchCriteria.of(brandId, productId, queryDate);
        final var entity = this.mocks.createTestPriceEntityFor(brandId, productId);
        final var expectedPrice = this.mocks.createTestPriceFor(brandId, productId);

        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(brandId, productId, queryDate))
                .thenReturn(Optional.of(entity));
        when(this.mapper.toDomain(entity))
                .thenReturn(expectedPrice);

        // When
        final var result = this.priceRepositoryAdapter.findBestPrice(criteria);

        // Then
        assertTrue(result.isPresent(), "Should find a price for: " + description);
        assertEquals(expectedPrice, result.get(), "Should return mapped price");
        assertEquals(brandId, result.get().getBrandId(), "Should match brand ID");
        assertEquals(productId, result.get().getProductId(), "Should match product ID");
        assertNotNull(result.get().getPrice(), "Price should not be null");
        assertNotNull(result.get().getCurr(), "Currency should not be null");

        // Verify
        verify(this.repository).findBestPriceByBrandIdAndProductIdAtDate(brandId, productId, queryDate);
        verify(this.mapper).toDomain(entity);
        verifyNoMoreInteractions(this.repository, this.mapper);
    }

    @Test
    @DisplayName("Should return empty when no entity found in repository")
    void shouldReturnEmptyWhenNoEntityFoundInRepository() {
        // Given
        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        // When
        final var result = this.priceRepositoryAdapter.findBestPrice(this.testCriteria);

        // Then
        assertFalse(result.isPresent(), "Should return empty Optional when no entity found");
        assertTrue(result.isEmpty(), "Result should be empty");

        verify(this.repository).findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, this.testCriteria.queryDate());
        verifyNoInteractions(this.mapper);
    }

    @Test
    @DisplayName("Should propagate repository exceptions correctly")
    void shouldPropagateRepositoryExceptionsCorrectly() {
        // Given
        final var expectedException = new DataAccessException("Database connection failed") {
        };
        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenThrow(expectedException);

        // When & Then
        final var thrownException = assertThrows(DataAccessException.class,
                () -> this.priceRepositoryAdapter.findBestPrice(this.testCriteria),
                "Should propagate DataAccessException");

        assertEquals("Database connection failed", thrownException.getMessage());

        verify(this.repository).findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, this.testCriteria.queryDate());
        verifyNoInteractions(this.mapper);
    }

    @Test
    @DisplayName("Should handle mapper exceptions gracefully")
    void shouldHandleMapperExceptionsGracefully() {
        // Given
        final var expectedException = new RuntimeException("Mapping failed for invalid data");
        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(this.testEntity));
        when(this.mapper.toDomain(this.testEntity))
                .thenThrow(expectedException);

        // When & Then
        final var thrownException = assertThrows(RuntimeException.class,
                () -> this.priceRepositoryAdapter.findBestPrice(this.testCriteria),
                "Should propagate mapper exceptions");

        assertEquals("Mapping failed for invalid data", thrownException.getMessage());

        verify(this.repository).findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, this.testCriteria.queryDate());
        verify(this.mapper).toDomain(this.testEntity);
    }

    @Test
    @DisplayName("Should pass exact parameters to repository method")
    void shouldPassExactParametersToRepositoryMethod() {
        // Given
        final var specificCriteria = PriceSearchCriteria.of(99L, 12345L,
                LocalDateTime.of(2021, 3, 15, 14, 30, 45, 123456789));

        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        // When
        this.priceRepositoryAdapter.findBestPrice(specificCriteria);

        // Then - Verify exact parameters were passed
        verify(this.repository).findBestPriceByBrandIdAndProductIdAtDate(
                eq(99L),
                eq(12345L),
                eq(LocalDateTime.of(2021, 3, 15, 14, 30, 45, 123456789))
        );
    }

    @Test
    @DisplayName("Should handle multiple consecutive calls correctly")
    void shouldHandleMultipleConsecutiveCallsCorrectly() {
        // Given
        final var criteria1 = PriceSearchCriteria.of(1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0));
        final var criteria2 = PriceSearchCriteria.of(1L, 35455L, LocalDateTime.of(2020, 6, 14, 16, 0, 0));

        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(this.testEntity));
        when(this.mapper.toDomain(this.testEntity))
                .thenReturn(this.testPrice);

        // When
        final var result1 = this.priceRepositoryAdapter.findBestPrice(criteria1);
        final var result2 = this.priceRepositoryAdapter.findBestPrice(criteria2);

        // Then
        assertTrue(result1.isPresent(), "First call should return result");
        assertTrue(result2.isPresent(), "Second call should return result");
        assertEquals(result1.get(), result2.get(), "Both calls should return same mapped object");

        verify(this.repository, times(2)).findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any());
        verify(this.mapper, times(2)).toDomain(this.testEntity);
    }

    @Test
    @DisplayName("Should validate PriceSearchCriteria creation with factory method")
    void shouldValidatePriceSearchCriteriaCreationWithFactoryMethod() {
        // Given
        final var brandId = 2L;
        final var productId = 67890L;
        final var queryDate = LocalDateTime.of(2020, 6, 15, 16, 0, 0);

        // When
        final var criteria = PriceSearchCriteria.of(brandId, productId, queryDate);

        // Then
        assertNotNull(criteria, "Criteria should not be null");
        assertEquals(brandId, criteria.brandId(), "Brand ID should match");
        assertEquals(productId, criteria.productId(), "Product ID should match");
        assertEquals(queryDate, criteria.queryDate(), "Query date should match");
    }

    @Test
    @DisplayName("Should handle null response from mapper gracefully")
    void shouldHandleNullResponseFromMapperGracefully() {
        // Given
        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(this.testEntity));
        when(this.mapper.toDomain(this.testEntity))
                .thenReturn(null); // Mapper returns null

        // When
        final var result = this.priceRepositoryAdapter.findBestPrice(this.testCriteria);

        // Then
        assertFalse(result.isPresent(), "Repository adapter should return Optional.of(null)");
        assertTrue(result.isEmpty(), "Content should be null when mapper returns null");

        verify(this.repository).findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, this.testCriteria.queryDate());
        verify(this.mapper).toDomain(this.testEntity);
    }

    @Test
    @DisplayName("Should validate interaction sequence is correct")
    void shouldValidateInteractionSequenceIsCorrect() {
        // Given
        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(this.testEntity));
        when(this.mapper.toDomain(this.testEntity))
                .thenReturn(this.testPrice);

        // When
        this.priceRepositoryAdapter.findBestPrice(this.testCriteria);

        // Then
        final var inOrder = inOrder(this.repository, this.mapper);
        inOrder.verify(this.repository).findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L,
                this.testCriteria.queryDate());
        inOrder.verify(this.mapper).toDomain(this.testEntity);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Should validate criteria immutability")
    void shouldValidateCriteriaImmutability() {
        // Given
        final var originalDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        final var criteria = PriceSearchCriteria.of(1L, 35455L, originalDate);

        // When
        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        this.priceRepositoryAdapter.findBestPrice(criteria);
        this.priceRepositoryAdapter.findBestPrice(criteria);

        // Then
        assertEquals(1L, criteria.brandId(), "Brand ID should remain unchanged");
        assertEquals(35455L, criteria.productId(), "Product ID should remain unchanged");
        assertEquals(originalDate, criteria.queryDate(), "Query date should remain unchanged");

        verify(this.repository, times(2)).findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, originalDate);
    }
}
