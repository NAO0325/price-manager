package com.price.manager.application.services;

import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.application.utils.PriceDomainMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceServiceUseCaseTest {

    private PriceDomainMocks mocks;

    @Mock
    private PriceRepositoryPort priceRepositoryPort;

    @InjectMocks
    private PriceServiceUseCase priceServiceUseCase;

    @BeforeEach
    void setUp() {
        mocks = new PriceDomainMocks();
    }

    @Test
    void findByBrandProductBetweenEmptyList() {
        // given
        var dateTest = LocalDateTime.of(2020,6,14, 10, 0, 0);

        // when
        when(priceRepositoryPort.findAllByBrandIdAndProductIdBetweenDates(anyLong(), anyLong(), any()))
                .thenReturn(List.of());

        //then
        var result = priceServiceUseCase.findByBrandProductBetweenDate(3L, 1L, dateTest);
        assertNull(result);

    }

    @Test
    void findByBrandProductBetweenDateTest1() {

        // given
        var dateTest = LocalDateTime.of(2020,6,14, 10, 0, 0);

        // when
        when(priceRepositoryPort.findAllByBrandIdAndProductIdBetweenDates(anyLong(), anyLong(), any()))
                .thenReturn(mocks.mockListTest1());

        //then
        var result = priceServiceUseCase.findByBrandProductBetweenDate(1L, 35455L, dateTest);
        assertEquals(1L, result.getBrandId());
        assertEquals(35455L, result.getProductId());
        assertEquals(35.5, result.getPrice());
    }

    @Test
    void findByBrandProductBetweenDateTest2() {

        // given
        var dateTest = LocalDateTime.of(2020,6,14, 16, 0, 0);

        // when
        when(priceRepositoryPort.findAllByBrandIdAndProductIdBetweenDates(anyLong(), anyLong(), any()))
                .thenReturn(mocks.mockListTest2());

        //then
        var result = priceServiceUseCase.findByBrandProductBetweenDate(1L, 35455L, dateTest);
        assertEquals(1L, result.getBrandId());
        assertEquals(35455L, result.getProductId());
        assertEquals(25.45, result.getPrice());
    }

    @Test
    void findByBrandProductBetweenDateTest3() {

        // given
        var dateTest = LocalDateTime.of(2020,6,14, 21, 0, 0);

        // when
        when(priceRepositoryPort.findAllByBrandIdAndProductIdBetweenDates(anyLong(), anyLong(), any()))
                .thenReturn(mocks.mockListTest3());

        //then
        var result = priceServiceUseCase.findByBrandProductBetweenDate(1L, 35455L, dateTest);
        assertEquals(1L, result.getBrandId());
        assertEquals(35455L, result.getProductId());
        assertEquals(35.5, result.getPrice());
    }

    @Test
    void findByBrandProductBetweenDateTest4() {

        // given
        var dateTest = LocalDateTime.of(2020,6,15, 10, 0, 0);

        // when
        when(priceRepositoryPort.findAllByBrandIdAndProductIdBetweenDates(anyLong(), anyLong(), any()))
                .thenReturn(mocks.mockListTest4());

        //then
        var result = priceServiceUseCase.findByBrandProductBetweenDate(1L, 3595L, dateTest);
        assertEquals(1L, result.getBrandId());
        assertEquals(35455L, result.getProductId());
        assertEquals(30.5, result.getPrice());
    }

    @Test
    void findByBrandProductBetweenDateTest5() {

        // given
        var dateTest = LocalDateTime.of(2020,6,15, 21, 0, 0);

        // when
        when(priceRepositoryPort.findAllByBrandIdAndProductIdBetweenDates(anyLong(), anyLong(), any()))
                .thenReturn(mocks.mockListTest5());

        //then
        var result = priceServiceUseCase.findByBrandProductBetweenDate(1L, 3595L, dateTest);
        assertEquals(1L, result.getBrandId());
        assertEquals(35455L, result.getProductId());
        assertEquals(38.95, result.getPrice());
    }

}