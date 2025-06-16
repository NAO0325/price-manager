package com.price.manager.application.ports.driving;

import java.time.LocalDateTime;

import com.price.manager.domain.Price;

public interface PriceServicePort {

    Price findByBrandProductBetweenDate(Long brandId, Long productId, LocalDateTime dateBetween);
}
