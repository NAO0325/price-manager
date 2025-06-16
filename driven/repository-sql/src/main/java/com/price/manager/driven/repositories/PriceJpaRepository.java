package com.price.manager.driven.repositories;

import com.price.manager.driven.repositories.models.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio JPA para operaciones de persistencia sobre la entidad {@link PriceEntity}.
 *
 * <p>Este repositorio implementa la lógica de acceso a datos específica para la consulta
 * de precios según las reglas de negocio de Inditex, incluyendo filtrado temporal
 * y selección por prioridad.</p>
 *
 * <h3>Consultas Especializadas:</h3>
 * <p>Además de las operaciones CRUD básicas heredadas de {@link JpaRepository},
 * este repositorio proporciona consultas optimizadas para:</p>
 * <ul>
 *   <li><strong>Filtrado temporal:</strong> Precios vigentes en una fecha específica</li>
 *   <li><strong>Selección por prioridad:</strong> Mejor precio según reglas de negocio</li>
 *   <li><strong>Filtrado por marca y producto:</strong> Criterios de búsqueda principales</li>
 * </ul>
 *
 * <h3>Performance:</h3>
 * <p>Para optimizar el rendimiento, se recomienda crear los siguientes índices:</p>
 * <pre>
 * CREATE INDEX idx_prices_search ON PRICES(BRAND_ID, PRODUCT_ID, START_DATE, END_DATE);
 * CREATE INDEX idx_prices_priority ON PRICES(PRIORITY DESC, PRICE_LIST DESC);
 * </pre>
 *
 * @author Napoleon Avila Ochoa
 * @version 1.0.0
 * @since 1.0.0
 *
 * @see PriceEntity
 * @see com.price.manager.driven.repositories.adapters.PriceRepositoryAdapter
 */
public interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    /**
     * Encuentra el mejor precio aplicable para una marca y producto en una fecha específica.
     *
     * <p>Esta consulta implementa el algoritmo central del sistema de precios de negocio:</p>
     *
     * <h4>Algoritmo de Selección:</h4>
     * <ol>
     *   <li><strong>Filtro temporal:</strong> {@code dateBetween BETWEEN startDate AND endDate}</li>
     *   <li><strong>Filtro de contexto:</strong> {@code brandId = :brandId AND productId = :productId}</li>
     *   <li><strong>Ordenación por prioridad:</strong> {@code ORDER BY priority DESC}</li>
     *   <li><strong>Desempate:</strong> {@code ORDER BY priceList DESC}</li>
     *   <li><strong>Selección:</strong> {@code LIMIT 1} - solo el mejor resultado</li>
     * </ol>
     *
     * <h4>Casos de Uso Cubiertos:</h4>
     * <ul>
     *   <li><strong>Precio único:</strong> Solo hay un precio vigente → lo retorna</li>
     *   <li><strong>Múltiples precios:</strong> Varios precios vigentes → retorna el de mayor prioridad</li>
     *   <li><strong>Empate en prioridad:</strong> Misma prioridad → retorna el de mayor priceList</li>
     *   <li><strong>Sin precios:</strong> No hay precios vigentes → retorna {@code Optional.empty()}</li>
     * </ul>
     *
     * <h4>Ejemplos de Comportamiento:</h4>
     * <pre>{@code
     * // Caso 1: Test Inditex - 14/06/2020 10:00 → Precio base
     * Optional<PriceEntity> result1 = repository.findBestPriceByBrandIdAndProductIdAtDate(
     *     1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0)
     * );
     * // result1.get().getPrice() == 35.50, result1.get().getPriceList() == 1
     *
     * // Caso 2: Test Inditex - 14/06/2020 16:00 → Promoción (mayor prioridad)
     * Optional<PriceEntity> result2 = repository.findBestPriceByBrandIdAndProductIdAtDate(
     *     1L, 35455L, LocalDateTime.of(2020, 6, 14, 16, 0)
     * );
     * // result2.get().getPrice() == 25.45, result2.get().getPriceList() == 2
     * }</pre>
     *
     * <h4>Optimización de Query:</h4>
     * <p>La consulta utiliza:</p>
     * <ul>
     *   <li><strong>JPQL nativo</strong> para control preciso del SQL generado</li>
     *   <li><strong>Parámetros nombrados</strong> para prevenir inyección SQL</li>
     *   <li><strong>LIMIT 1</strong> para optimizar performance (solo necesitamos el mejor)</li>
     *   <li><strong>Orden determinista</strong> para garantizar consistencia</li>
     * </ul>
     *
     * @param brandId     identificador de la marca (ej: 1 para ZARA).
     *                    Debe ser un valor positivo y no {@code null}.
     * @param productId   identificador del producto (ej: 35455).
     *                    Debe ser un valor positivo y no {@code null}.
     * @param dateBetween fecha para la cual se busca el precio vigente.
     *                    No puede ser {@code null}.
     *
     * @return {@link Optional} conteniendo:
     *         <ul>
     *           <li><strong>PriceEntity:</strong> el mejor precio según reglas de negocio</li>
     *           <li><strong>empty():</strong> si no hay precio vigente para los criterios</li>
     *         </ul>
     *
     * @see PriceEntity
     *
     * @since 1.0.0
     */
    @Query("""
            SELECT p
            FROM PriceEntity p
            WHERE p.brandId = :brandId
              AND p.productId = :productId
              AND :dateBetween BETWEEN p.startDate AND p.endDate
            ORDER BY p.priority DESC, p.priceList DESC
            LIMIT 1
            """)
    Optional<PriceEntity> findBestPriceByBrandIdAndProductIdAtDate(@Param("brandId") Long brandId,
                                                                   @Param("productId") Long productId,
                                                                   @Param("dateBetween") LocalDateTime dateBetween);
}
