package com.price.manager.driven.repositories.utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.price.manager.domain.Price;
import com.price.manager.driven.repositories.models.PriceEntity;

public class PriceRepositoryMocks {

    public PriceEntity createTestPriceEntity() {
        return this.createTestPriceEntityFor(1L, 35455L);
    }

    public PriceEntity createTestPriceEntityFor(Long brandId, Long productId) {
        return PriceEntity.builder()
                .brandId(brandId)
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 6, 14, 23, 59, 59))
                .priceList(1L)
                .productId(productId)
                .priority(0)
                .price(new BigDecimal("35.5"))
                .curr("EUR")
                .build();
    }

    public Price createTestPrice() {
        return this.createTestPriceFor(1L, 35455L);
    }

    public Price createTestPriceFor(Long brandId, Long productId) {
        return Price.builder()
                .brandId(brandId)
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 6, 14, 23, 59, 59))
                .priceList(1L)
                .productId(productId)
                .priority(0)
                .price(new BigDecimal("35.5"))
                .curr("EUR")
                .build();
    }
}
