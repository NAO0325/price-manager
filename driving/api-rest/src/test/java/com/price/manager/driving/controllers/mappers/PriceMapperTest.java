package com.price.manager.driving.controllers.mappers;

import com.price.manager.driving.controllers.utils.PriceMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class PriceMapperTest {

    private PriceMocks mocks;

    private PriceMapper priceMapper;

    @BeforeEach
    void setup() {
        this.mocks = new PriceMocks();
        this.priceMapper = Mappers.getMapper(PriceMapper.class);
    }

    @Test
    void toResponseDto() {
        final var start = LocalDateTime.of(2022, 1, 1, 1, 30, 59);
        final var end = LocalDateTime.of(2022, 1, 31, 1, 30, 59);
        final var price = this.mocks.createPrice();
        final var priceResponse = this.priceMapper.toResponseDto(price);
        assertNotNull(priceResponse);
        assertEquals(1L, priceResponse.getBrandId());
        assertEquals(25.45, priceResponse.getPrice());
        assertEquals(2L, priceResponse.getId());
        assertEquals(start, priceResponse.getStartDate().toLocalDateTime());
        assertEquals(end, priceResponse.getEndDate().toLocalDateTime());
    }

    @Test
    void toResponseDtoWithNullEndDate() {
        final var start = LocalDateTime.of(2022, 1, 1, 1, 30, 59);
        final var price = this.mocks.createPrice();
        price.setEndDate(null);
        final var priceResponse = this.priceMapper.toResponseDto(price);
        assertNotNull(priceResponse);
        assertEquals(1L, priceResponse.getBrandId());
        assertEquals(25.45, priceResponse.getPrice());
        assertEquals(2L, priceResponse.getId());
        assertEquals(start, priceResponse.getStartDate().toLocalDateTime());
        assertNull(priceResponse.getEndDate());
    }
}

