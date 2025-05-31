package com.price.manager.domain.services;

import com.price.manager.domain.Price;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class PriceSelectionService {

    public Optional<Price> selectBestPrice(List<Price> prices) {

        if (CollectionUtils.isEmpty(prices)) {
            return Optional.empty();
        }

        return prices.stream()
                .max(Comparator.comparing(Price::getPriority)
                        .thenComparing(Price::getPriceList));
    }
}
