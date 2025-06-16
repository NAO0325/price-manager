package com.price.manager.domain.criteria;

import java.time.LocalDateTime;

/**
 * Record que encapsula los criterios de búsqueda para consultar precios en el sistema.
 *
 * <p>Este criterio se utiliza para filtrar precios por marca, producto y fecha,
 * permitiendo encontrar el precio aplicable según las reglas de negocio de Inditex.</p>
 *
 * <p><strong>Uso Típico:</strong></p>
 * <pre>{@code
 * PriceSearchCriteria criteria = PriceSearchCriteria.of(
 *     1L,                                        // brandId (ZARA)
 *     35455L,                                    // productId
 *     LocalDateTime.of(2020, 6, 14, 16, 0)      // queryDate
 * );
 *
 * Optional<Price> price = repository.findBestPrice(criteria);
 * }</pre>
 *
 * <p><strong>Inmutabilidad:</strong> Este record es inmutable por diseño,
 * garantizando que los criterios de búsqueda no puedan ser modificados
 * después de su creación.</p>
 *
 * @param brandId   identificador de la marca (debe ser mayor que 0)
 * @param productId identificador del producto (debe ser mayor que 0)
 * @param queryDate fecha para la cual se busca el precio aplicable
 *
 * @author Napoleon Avila Ochoa
 * @version 1.0.0
 * @since 1.0.0
 *
 * @see com.price.manager.domain.Price
 * @see com.price.manager.application.ports.driven.PriceRepositoryPort#findBestPrice(PriceSearchCriteria)
 */
public record PriceSearchCriteria(Long brandId, Long productId, LocalDateTime queryDate) {
    /**
     * Factory method para crear instancias de {@code PriceSearchCriteria} de forma conveniente.
     *
     * <p>Este método proporciona una forma más legible de crear criterios de búsqueda:</p>
     *
     * <pre>{@code
     * var criteria = PriceSearchCriteria.of(1L, 35455L, LocalDateTime.now());
     * }</pre>
     *
     * @param brandId   identificador de la marca (debe ser > 0)
     * @param productId identificador del producto (debe ser > 0)
     * @param queryDate fecha para buscar precios vigentes
     * @return nueva instancia de {@code PriceSearchCriteria}
     *
     * @throws IllegalArgumentException si algún parámetro es inválido
     *
     * @since 1.0.0
     */
    public static PriceSearchCriteria of(Long brandId, Long productId, LocalDateTime queryDate) {
        return new PriceSearchCriteria(brandId, productId, queryDate);
    }
}
