package com.price.manager.utils;

import com.price.manager.domain.Price;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceDomainMocks {

	public Price createValidPrice() {
		return Price.builder()
				.brandId(1L)
				.productId(35455L)
				.priceList(1L)
				.price(new BigDecimal("35.50"))
				.startDate(LocalDateTime.of(2020, 6, 14, 0, 0))
				.endDate(LocalDateTime.of(2020, 12, 31, 23, 59))
				.priority(0)
				.curr("EUR")
				.build();
	}

	public static Price createExpectedPrice(Long priceList, Integer priority, String priceValue,
											 String startDateStr, String endDateStr) {
		return Price.builder()
				.brandId(1L)
				.productId(35455L)
				.priceList(priceList)
				.priority(priority)
				.price(new BigDecimal(priceValue))
				.startDate(LocalDateTime.parse(startDateStr))
				.endDate(LocalDateTime.parse(endDateStr))
				.curr("EUR")
				.build();
	}
}
