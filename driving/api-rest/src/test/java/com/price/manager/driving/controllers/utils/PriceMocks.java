package com.price.manager.driving.controllers.utils;

import com.price.manager.domain.Price;
import com.price.manager.driving.controllers.models.PriceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class PriceMocks {

    public Price createBasicPriceWithPriceList(Long priceList) {
        return Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priceList(priceList)
                .price(new BigDecimal("35.50"))
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59))
                .priority(0)
                .curr("EUR")
                .build();
    }

    public Price createTestDomainPrice() {
        return this.createDomainPriceFor(1L, 35455L, new BigDecimal("35455.0"), 2L);
    }

    public Price createDomainPriceFor(Long brandId, Long productId, BigDecimal price, Long priceList) {
        return Price.builder()
                .brandId(brandId)
                .productId(productId)
                .priceList(priceList)
                .price(price)
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59))
                .priority(0)
                .curr("EUR")
                .build();
    }

    public PriceResponse createTestPriceResponse() {
        return this.createPriceResponseFor(1L, 35455L, 35455.0, 2L);
    }

    public PriceResponse createPriceResponseFor(Long brandId, Long productId, Double price, Long priceList) {
        final var response = new PriceResponse();
        response.setBrandId(brandId);
        response.setPrice(price);
        response.setId(priceList);
        response.setStartDate(OffsetDateTime.of(2020, 6, 14, 0, 0, 0, 0, ZoneOffset.UTC));
        response.setEndDate(OffsetDateTime.of(2020, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC));
        return response;
    }
}
