package com.price.manager.driven.repositories.adapters;

import com.price.manager.domain.criteria.PriceSearchCriteria;
import com.price.manager.driven.repositories.PriceJpaRepository;
import com.price.manager.driven.repositories.mappers.PriceEntityMapper;
import com.price.manager.driven.repositories.utils.PriceRepositoryMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PriceRepositoryAdapterTest {

    private PriceRepositoryMocks mocks;

    @Mock
    private PriceJpaRepository repository;

    @Mock
    private PriceEntityMapper mapper;

    @InjectMocks
    private PriceRepositoryAdapter priceRepositoryAdapter;

    @BeforeEach
    void setUp() {
        mocks = new PriceRepositoryMocks();
    }

    @Test
    void findByCriteria_ShouldReturnMappedPrices() {
        // Given
        var criteria = PriceSearchCriteria.of(1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0));

        when(repository.findAllByBrandIdAndProductIdBetweenDates(anyLong(), anyLong(), any()))
                .thenReturn(mocks.mockEntityListTest());

        when(mapper.toDomain(any()))
                .thenReturn(mocks.createPrice());

        // When
        var result = priceRepositoryAdapter.findByCriteria(criteria);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBrandId());
        assertEquals(35455L, result.get(0).getProductId());
        assertEquals(35.5, result.get(0).getPrice());

        // Verify interactions
        verify(repository, times(1)).findAllByBrandIdAndProductIdBetweenDates(1L, 35455L, criteria.queryDate());
        verify(mapper, times(1)).toDomain(any());
    }

    @Test
    void findByCriteria_WithEmptyResult_ShouldReturnEmptyList() {
        // Given
        var criteria = PriceSearchCriteria.of(1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0));

        when(repository.findAllByBrandIdAndProductIdBetweenDates(anyLong(), anyLong(), any()))
                .thenReturn(java.util.List.of());

        // When
        var result = priceRepositoryAdapter.findByCriteria(criteria);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repository, times(1)).findAllByBrandIdAndProductIdBetweenDates(1L, 35455L, criteria.queryDate());
        verify(mapper, times(0)).toDomain(any());
    }

    @Test
    void findBestPrice_ShouldReturnOptionalPrice() {
        // Given
        var criteria = PriceSearchCriteria.of(1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0));

        when(repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(mocks.createPriceEntity()));

        when(mapper.toDomain(any()))
                .thenReturn(mocks.createPrice());

        // When
        var result = priceRepositoryAdapter.findBestPrice(criteria);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getBrandId());
        assertEquals(35455L, result.get().getProductId());
        assertEquals(35.5, result.get().getPrice());

        verify(repository, times(1)).findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, criteria.queryDate());
        verify(mapper, times(1)).toDomain(any());
    }

    @Test
    void findBestPrice_WithNoResult_ShouldReturnEmptyOptional() {
        // Given
        var criteria = PriceSearchCriteria.of(1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0));

        when(repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        // When
        var result = priceRepositoryAdapter.findBestPrice(criteria);

        // Then
        assertTrue(result.isEmpty());

        verify(repository, times(1)).findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, criteria.queryDate());
        verify(mapper, times(0)).toDomain(any());
    }

    @Test
    void findByCriteria_ShouldPassCorrectParametersToRepository() {
        // Given
        var brandId = 2L;
        var productId = 12345L;
        var queryDate = LocalDateTime.of(2023, 12, 25, 15, 30, 0);
        var criteria = PriceSearchCriteria.of(brandId, productId, queryDate);

        when(repository.findAllByBrandIdAndProductIdBetweenDates(brandId, productId, queryDate))
                .thenReturn(java.util.List.of());

        // When
        priceRepositoryAdapter.findByCriteria(criteria);

        // Then
        verify(repository, times(1)).findAllByBrandIdAndProductIdBetweenDates(brandId, productId, queryDate);
    }

    @Test
    void priceSearchCriteria_ShouldCreateCorrectly() {
        // Given
        var brandId = 1L;
        var productId = 35455L;
        var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        // When
        var criteria = PriceSearchCriteria.of(brandId, productId, queryDate);

        // Then
        assertEquals(brandId, criteria.brandId());
        assertEquals(productId, criteria.productId());
        assertEquals(queryDate, criteria.queryDate());
    }
}