package com.price.manager.application.services;


import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.application.ports.driving.PriceServicePort;
import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;
import com.price.manager.domain.services.PriceSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class PriceServiceUseCase implements PriceServicePort {

    private final PriceRepositoryPort priceRepositoryPort;
    private final PriceSelectionService priceSelectionService;

    @Override
    public Price findByBrandProductBetweenDate(Long brandId, Long productId, LocalDateTime dateBetween) {

        var criteria = PriceSearchCriteria.of(brandId, productId, dateBetween);

        var availablePrices = priceRepositoryPort.findByCriteria(criteria);

        return priceSelectionService.selectBestPrice(availablePrices)
                .orElse(null);
    }

}

