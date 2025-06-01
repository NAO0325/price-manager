package com.price.manager.utils;

import com.price.manager.domain.Price;

import java.time.LocalDateTime;
import java.util.List;

public class PriceDomainMocks {

	public List<Price> mockListTest1() {
		return List.of(Price.builder().brandId(1L).startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
				.endDate(LocalDateTime.of(2020, 6, 14, 23, 59, 59)).priceList(1L).productId(35455L).priority(0)
				.price(35.5).curr("EUR").build());
	}

	public List<Price> mockListTest2() {
		return List.of(
				Price.builder().brandId(1L).startDate(LocalDateTime.of(2020, 6, 14, 15, 0, 0))
						.endDate(LocalDateTime.of(2020, 6, 14, 18, 30, 0)).priceList(2L).productId(35455L).priority(1)
						.price(25.45).curr("EUR").build(),
				Price.builder().brandId(1L).startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
						.endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59)).priceList(1L).productId(35455L).priority(0)
						.price(35.5).curr("EUR").build());
	}

	public List<Price> mockListTest3() {
		return List.of(Price.builder().brandId(1L).startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
				.endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59)).priceList(1L).productId(35455L).priority(0)
				.price(35.5).curr("EUR").build());
	}

	public List<Price> mockListTest4() {
		return List.of(
				Price.builder().brandId(1L).startDate(LocalDateTime.of(2020, 6, 15, 0, 0, 0))
						.endDate(LocalDateTime.of(2020, 6, 15, 11, 0, 0)).priceList(3L).productId(35455L).priority(1)
						.price(30.5).curr("EUR").build(),
				Price.builder().brandId(1L).startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
						.endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59)).priceList(1L).productId(35455L).priority(0)
						.price(35.5).curr("EUR").build());
	}

	public List<Price> mockListTest5() {
		return List.of(
				Price.builder().brandId(1L).startDate(LocalDateTime.of(2020, 6, 15, 16, 0, 0))
						.endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59)).priceList(1L).productId(35455L).priority(1)
						.price(38.95).curr("EUR").build(),
				Price.builder().brandId(1L).startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
						.endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59)).priceList(1L).productId(35455L).priority(0)
						.price(35.5).curr("EUR").build());
	}
}
