package com.price.manager.application.ports.driven;

import com.price.manager.domain.Price;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceRepositoryPort {

    List<Price> findAllByBrandIdAndProductIdBetweenDates(Long brandId, Long productId, LocalDateTime dateBetween);
}
