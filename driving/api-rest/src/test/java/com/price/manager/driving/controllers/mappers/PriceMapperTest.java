package com.price.manager.driving.controllers.mappers;

import com.price.manager.driving.controllers.utils.PriceMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PriceMapperTest {

    private PriceMocks mocks;

    private PriceMapper priceMapper;

    @BeforeEach
    public void setup() {
        mocks = new PriceMocks();
        priceMapper = Mappers.getMapper(PriceMapper.class);
    }

    @Test
    void toResponseDto() {
        var start = LocalDateTime.of(2022, 1, 1, 1, 30, 59);
        var end = LocalDateTime.of(2022,1,31,1,30,59);
        var price = mocks.createPrice();
        var priceResponse = priceMapper.toResponseDto(price);
        assertNotNull(priceResponse);
        assertEquals(1L, priceResponse.getBrandId());
        assertEquals(25.45, priceResponse.getPrice());
        assertEquals(2L, priceResponse.getId());
        assertEquals(start, priceResponse.getStartDate().toLocalDateTime());
        assertEquals(end, priceResponse.getEndDate().toLocalDateTime());
    }

    @Test
    void toResponseDtoWithNullEndDate() {
        var start = LocalDateTime.of(2022, 1, 1, 1, 30, 59);
        var price = mocks.createPrice();
        price.setEndDate(null);
        var priceResponse = priceMapper.toResponseDto(price);
        assertNotNull(priceResponse);
        assertEquals(1L, priceResponse.getBrandId());
        assertEquals(25.45, priceResponse.getPrice());
        assertEquals(2L, priceResponse.getId());
        assertEquals(start, priceResponse.getStartDate().toLocalDateTime());
        assertNull(priceResponse.getEndDate());
    }

}
