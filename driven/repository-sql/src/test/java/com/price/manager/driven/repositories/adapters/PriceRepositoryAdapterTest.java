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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        this.mocks = new PriceRepositoryMocks();
    }

    @Test
    void findBestPriceShouldReturnOptionalPrice() {
        // Given
        final var criteria = PriceSearchCriteria.of(1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0));

        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(this.mocks.createPriceEntity()));

        when(this.mapper.toDomain(any())).thenReturn(this.mocks.createPrice());

        // When
        final var result = this.priceRepositoryAdapter.findBestPrice(criteria);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getBrandId());
        assertEquals(35455L, result.get().getProductId());
        assertEquals(35.5, result.get().getPrice());

        verify(this.repository, times(1)).findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, criteria.queryDate());
        verify(this.mapper, times(1)).toDomain(any());
    }

    @Test
    void findBestPriceWithNoResultShouldReturnEmptyOptional() {
        // Given
        final var criteria = PriceSearchCriteria.of(1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0));

        when(this.repository.findBestPriceByBrandIdAndProductIdAtDate(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        // When
        final var result = this.priceRepositoryAdapter.findBestPrice(criteria);

        // Then
        assertTrue(result.isEmpty());

        verify(this.repository, times(1)).findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, criteria.queryDate());
        verify(this.mapper, times(0)).toDomain(any());
    }

    @Test
    void priceSearchCriteriaShouldCreateCorrectly() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        // When
        final var criteria = PriceSearchCriteria.of(brandId, productId, queryDate);

        // Then
        assertEquals(brandId, criteria.brandId());
        assertEquals(productId, criteria.productId());
        assertEquals(queryDate, criteria.queryDate());
    }
}
