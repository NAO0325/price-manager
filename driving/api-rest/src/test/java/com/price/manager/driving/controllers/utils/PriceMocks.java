package com.price.manager.driving.controllers.utils;

import com.price.manager.domain.Price;
import com.price.manager.driving.controllers.models.PriceResponse;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class PriceMocks {

    public Price createPrice() {
        return Price.builder()
                .priceList(2L)
                .brandId(1L)
                .productId(35455L)
                .price(25.45)
                .startDate(LocalDateTime.of(2022, 1, 1, 1, 30, 59))
                .endDate(LocalDateTime.of(2022, 1, 31, 1, 30, 59))
                .build();
    }

    public PriceResponse createPriceResponse1() {
        PriceResponse response = new PriceResponse();
        response.setId(1L);
        response.setBrandId(1L);
        response.setPrice(35.50);
        response.setStartDate(
                OffsetDateTime.of(
                        LocalDateTime.of(2020, 6, 14, 0, 0, 0), ZoneOffset.UTC));
        response.setEndDate(
                OffsetDateTime.of(
                        LocalDateTime.of(2020, 12, 31, 23, 59, 59), ZoneOffset.UTC));
        return response;
    }

    public PriceResponse createPriceResponse2() {
        PriceResponse response = new PriceResponse();
        response.setId(2L);
        response.setBrandId(1L);
        response.setPrice(25.45);
        response.setStartDate(
                OffsetDateTime.of(
                        LocalDateTime.of(2020, 6, 14, 15, 0, 0), ZoneOffset.UTC));
        response.setEndDate(
                OffsetDateTime.of(
                        LocalDateTime.of(2020, 6, 14, 18, 30, 0), ZoneOffset.UTC));
        return response;

    }

    public PriceResponse createPriceResponse3() {
        PriceResponse response = new PriceResponse();
        response.setId(3L);
        response.setBrandId(1L);
        response.setPrice(30.50);
        response.setStartDate(
                OffsetDateTime.of(
                        LocalDateTime.of(2020, 6, 15, 0, 0, 0), ZoneOffset.UTC));
        response.setEndDate(
                OffsetDateTime.of(
                        LocalDateTime.of(2020, 6, 15, 11, 0, 0), ZoneOffset.UTC));
        return response;
    }

    public PriceResponse createPriceResponse4() {
        PriceResponse response = new PriceResponse();
        response.setId(4L);
        response.setBrandId(1L);
        response.setPrice(38.95);
        response.setStartDate(
                OffsetDateTime.of(
                        LocalDateTime.of(2020, 6, 15, 16, 0, 0), ZoneOffset.UTC));
        response.setEndDate(
                OffsetDateTime.of(
                        LocalDateTime.of(2020, 12, 31, 23, 59, 59), ZoneOffset.UTC));
        return response;
    }
}
