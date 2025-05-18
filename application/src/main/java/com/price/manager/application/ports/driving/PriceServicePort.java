package com.price.manager.application.ports.driving;

import com.price.manager.domain.Price;

import java.time.LocalDateTime;

public interface PriceServicePort {

    Price findByBrandProductBetweenDate(Long brandId, Long productId, LocalDateTime dateBetween);
}
