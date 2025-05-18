package com.price.manager.driving.controllers.adapters;

import com.price.manager.application.ports.driving.PriceServicePort;
import com.price.manager.domain.Price;
import com.price.manager.driving.controllers.error.PriceNotFoundException;
import com.price.manager.driving.controllers.mappers.PriceMapper;
import com.price.manager.driving.controllers.models.PriceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceControllerAdapterTest {

    @Mock
    private PriceServicePort priceServicePort;

    @Mock
    private PriceMapper priceMapper;

    @InjectMocks
    private PriceControllerAdapter priceRestController;


    @Test
    void returnPriceDataOK() {
        var dateTest = LocalDateTime.of(2020, 6, 14, 10, 0, 0)
                .atOffset(ZoneOffset.UTC);

        when(priceMapper.toResponseDto(any())).thenReturn(new PriceResponse());
        when(priceServicePort.findByBrandProductBetweenDate(anyLong(), anyLong(), any()))
                .thenReturn(Price.builder().startDate(LocalDateTime.now()).build());
        var result = priceRestController.findByBrandProductBetweenDate(2L, 2L, dateTest);
        assertNotNull(result);
    }

    @Test
    void returnNumberFormatException() {

        var dateTest = LocalDateTime.of(2020, 6, 14, 10, 0, 0)
                .atOffset(ZoneOffset.UTC);

        willThrow(new NumberFormatException("exception"))
                .given(priceServicePort).findByBrandProductBetweenDate(anyLong(), anyLong(), any());
        assertThrows(NumberFormatException.class, () ->
            priceRestController.findByBrandProductBetweenDate(3L, 1L, dateTest)
        );
    }

    @Test
    void returnResourceNotFoundException() {
        var dateTest = LocalDateTime.of(2020, 6, 14, 10, 0, 0)
                .atOffset(ZoneOffset.UTC);

        when(priceServicePort.findByBrandProductBetweenDate(anyLong(), anyLong(), any()))
                .thenReturn(null);
        assertThrows(PriceNotFoundException.class, () ->
            priceRestController.findByBrandProductBetweenDate(2L, 2L, dateTest)
        );
    }
}
