package com.price.manager.driven.repositories.utils;

import com.price.manager.domain.Price;
import com.price.manager.driven.repositories.models.PriceEntity;

import java.time.LocalDateTime;

public class PriceRepositoryMocks {

	public PriceEntity createPriceEntity() {
		return PriceEntity.builder().brandId(1L).startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
				.endDate(LocalDateTime.of(2020, 6, 14, 23, 59, 59)).priceList(1L).productId(35455L).priority(0)
				.price(35.5).curr("EUR").build();
	}

	public Price createPrice() {
		return Price.builder().brandId(1L).startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
				.endDate(LocalDateTime.of(2020, 6, 14, 23, 59, 59)).priceList(1L).productId(35455L).priority(0)
				.price(35.5).curr("EUR").build();
	}
}
