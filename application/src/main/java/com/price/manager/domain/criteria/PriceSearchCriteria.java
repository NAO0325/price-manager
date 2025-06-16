package com.price.manager.domain.criteria;

import java.time.LocalDateTime;

/**
 * Criterio de búsqueda para precios.
 * <p>
 * ✅ RECORD PURO: Solo datos, sin lógica
 * ✅ FACTORY METHOD: Para creación conveniente
 */
public record PriceSearchCriteria(Long brandId, Long productId, LocalDateTime queryDate) {
    public static PriceSearchCriteria of(Long brandId, Long productId, LocalDateTime queryDate) {
        return new PriceSearchCriteria(brandId, productId, queryDate);
    }
}
