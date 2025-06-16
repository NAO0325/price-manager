package com.price.manager.application.services;

import java.time.LocalDateTime;

import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.application.ports.driving.PriceServicePort;
import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementación del caso de uso principal para la gestión de precios.
 *
 * <p>Esta clase actúa como orquestador entre la capa de presentación y el dominio,
 * aplicando las reglas de negocio para encontrar el precio más apropiado según
 * los criterios de búsqueda proporcionados.</p>
 *
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Validar parámetros de entrada del caso de uso</li>
 *   <li>Crear criterios de búsqueda estructurados</li>
 *   <li>Delegar la búsqueda al repositorio especializado</li>
 *   <li>Manejar casos cuando no se encuentra precio aplicable</li>
 * </ul>
 *
 * <p><strong>Arquitectura:</strong></p>
 * <p>Siguiendo los principios de Clean Architecture, esta clase:</p>
 * <ul>
 *   <li>No contiene lógica de dominio - delega al repositorio</li>
 *   <li>Es independiente de frameworks - solo usa abstracciones</li>
 *   <li>Implementa el puerto driving - define el contrato de entrada</li>
 *   <li>Usa el puerto driven - para acceso a datos</li>
 * </ul>
 *
 * <p><strong>Ejemplo de Uso:</strong></p>
 * <pre>{@code
 * @Autowired
 * private PriceServicePort priceService;
 *
 * // Buscar precio aplicable
 * Price price = priceService.findByBrandProductBetweenDate(
 *     1L,                                        // brandId (ZARA)
 *     35455L,                                    // productId
 *     LocalDateTime.of(2020, 6, 14, 16, 0)      // queryDate
 * );
 *
 * if (price != null) {
 *     log.info("Precio aplicable: {}", price.getPrice());
 * }
 * }</pre>
 *
 * @version 1.0.0
 * @see PriceServicePort
 * @see PriceRepositoryPort
 * @see Price
 * @see PriceSearchCriteria
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class PriceServiceUseCase implements PriceServicePort {

    /**
     * Puerto de salida para acceso a la persistencia de precios.
     *
     * <p>Inyectado por el contenedor IoC de Spring, permite desacoplar
     * este caso de uso de la implementación específica del repositorio.</p>
     */
    private final PriceRepositoryPort priceRepositoryPort;

    /**
     * Encuentra el precio aplicable para una marca y producto en una fecha específica.
     *
     * <p>Este método implementa el caso de uso principal del sistema: dado una marca,
     * un producto y una fecha, retorna el precio que debe aplicarse según las reglas
     * de prioridad y vigencia temporal definidas en el dominio.</p>
     *
     * <h4>Algoritmo de Búsqueda:</h4>
     * <ol>
     *   <li>Validar parámetros de entrada</li>
     *   <li>Crear criterio de búsqueda estructurado</li>
     *   <li>Delegar al repositorio la selección del mejor precio</li>
     *   <li>Retornar el resultado o {@code null} si no hay precio aplicable</li>
     * </ol>
     *
     * <h4>Casos de Negocio Cubiertos:</h4>
     * <ul>
     *   <li><strong>Precio base:</strong> Cuando solo hay un precio vigente</li>
     *   <li><strong>Promociones:</strong> Precios con mayor prioridad en rangos específicos</li>
     *   <li><strong>Sin precio:</strong> Cuando no hay precio vigente para la fecha</li>
     * </ul>
     *
     * @param brandId     identificador de la marca. Debe ser positivo y no {@code null}.
     * @param productId   identificador del producto. Debe ser positivo y no {@code null}.
     * @param dateBetween fecha para la cual se busca el precio. No puede ser {@code null}.
     *
     * @return el precio aplicable según las reglas de negocio, o {@code null} si no
     *         existe precio vigente para los criterios especificados
     *
     * @throws IllegalArgumentException si algún parámetro es {@code null} o inválido
     *
     * @see PriceRepositoryPort#findBestPrice(PriceSearchCriteria)
     * @see Price#hasHigherPriorityThan(Price)
     * @see Price#isValidAt(LocalDateTime)
     *
     * @since 1.0.0
     */
    @Override
    public Price findByBrandProductBetweenDate(Long brandId, Long productId, LocalDateTime dateBetween) {
        final var criteria = PriceSearchCriteria.of(brandId, productId, dateBetween);
        return this.priceRepositoryPort.findBestPrice(criteria).orElse(null);
    }

}
