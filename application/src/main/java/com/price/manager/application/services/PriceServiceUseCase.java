package com.price.manager.application.services;


import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.application.ports.driving.PriceServicePort;
import com.price.manager.domain.Price;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class PriceServiceUseCase implements PriceServicePort {

    private final PriceRepositoryPort priceRepositoryPort;

    @Override
    public Price findByBrandProductBetweenDate(Long brandId, Long productId, LocalDateTime dateBetween) {

        return priceRepositoryPort.findAllByBrandIdAndProductIdBetweenDates(brandId, productId, dateBetween)
                .stream()
                .max(Comparator.comparing(Price::getPriority))
                .orElse(null);
    }

}

