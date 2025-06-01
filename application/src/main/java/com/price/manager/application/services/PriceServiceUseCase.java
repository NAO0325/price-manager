package com.price.manager.application.services;


import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.application.ports.driving.PriceServicePort;
import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PriceServiceUseCase implements PriceServicePort {

    private final PriceRepositoryPort priceRepositoryPort;

    @Override
    public Price findByBrandProductBetweenDate(Long brandId, Long productId, LocalDateTime dateBetween) {
        var criteria = PriceSearchCriteria.of(brandId, productId, dateBetween);
        return priceRepositoryPort.findBestPrice(criteria).orElse(null);
    }

}

