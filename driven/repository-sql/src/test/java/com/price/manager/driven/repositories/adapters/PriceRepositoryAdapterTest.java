package com.price.manager.driven.repositories.adapters;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceRepositoryAdapterTest {

    private PriceRepositoryMocks mocks;

    @Mock
    private PriceJpaRepository repository;

    @Mock
    private PriceEntityMapper mapper;

    @InjectMocks
    PriceRepositoryAdapter priceRepositoryAdapter;

    @BeforeEach
    void setUp() {
        mocks = new PriceRepositoryMocks();
    }

    @Test
    void findAllByBrandIdAndProductIdBetweenDates() {

        // given
        var dateTest = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        // when
        when(repository.findAllByBrandIdAndProductIdBetweenDates(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(mocks.mockEntityListTest()));

        when(mapper.toDomain(any()))
                .thenReturn(mocks.createPrice());

        //then
        var result = priceRepositoryAdapter.findAllByBrandIdAndProductIdBetweenDates(1L, 35455L, dateTest);
        assertNotNull(result);
        assertEquals(1L, result.get(0).getBrandId());
        assertEquals(35455L, result.get(0).getProductId());
        assertEquals(35.5, result.get(0).getPrice());

    }
}
