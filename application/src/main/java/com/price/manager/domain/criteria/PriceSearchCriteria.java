package com.price.manager.domain.criteria;

import java.time.LocalDateTime;

public record PriceSearchCriteria(
        Long brandId,
        Long productId,
        LocalDateTime queryDate
) {
    public static PriceSearchCriteria of(Long brandId, Long productId, LocalDateTime queryDate) {
        return new PriceSearchCriteria(brandId, productId, queryDate);
    }
}

