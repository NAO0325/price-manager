package com.price.manager.driven.repositories.adapters;

import java.util.Optional;

import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;
import com.price.manager.driven.repositories.PriceJpaRepository;
import com.price.manager.driven.repositories.mappers.PriceEntityMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Adaptador de repositorio que implementa el puerto de salida para persistencia de precios.
 *
 * <p>Esta clase forma parte del patrón <strong>Adapter</strong> en la arquitectura hexagonal,
 * actuando como driven adapter que traduce las operaciones del dominio en llamadas
 * específicas a la capa de persistencia JPA.</p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li><strong>Traducción de abstracciones:</strong> Puerto → Implementación JPA</li>
 *   <li><strong>Mapeo de entidades:</strong> Dominio ↔ Persistencia</li>
 *   <li><strong>Manejo de Optional:</strong> Traducción de resultados de DB</li>
 *   <li><strong>Logging de operaciones:</strong> Trazabilidad de acceso a datos</li>
 * </ul>
 *
 * <h3>Flujo de Datos:</h3>
 * <pre>
 * Domain (PriceSearchCriteria)
 *    ↓
 * Adapter (extrae parámetros)
 *    ↓
 * JPA Repository (consulta SQL)
 *    ↓
 * PriceEntity (resultado de DB)
 *    ↓
 * EntityMapper (mapeo automático)
 *    ↓
 * Price (entidad de dominio)
 * </pre>
 *
 * <h3>Principios Aplicados:</h3>
 * <ul>
 *   <li><strong>Dependency Inversion:</strong> Depende de abstracciones, no implementaciones</li>
 *   <li><strong>Single Responsibility:</strong> Solo traduce entre capas</li>
 *   <li><strong>Open/Closed:</strong> Extensible sin modificar código existente</li>
 * </ul>
 *
 * @version 1.0.0
 * @since 1.0.0
 *
 * @see PriceRepositoryPort
 * @see PriceJpaRepository
 * @see PriceEntityMapper
 * @see PriceSearchCriteria
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PriceRepositoryAdapter implements PriceRepositoryPort {

    /**
     * Repositorio JPA para operaciones de persistencia sobre precios.
     *
     * <p>Proporciona acceso a la base de datos H2 con consultas optimizadas
     * para las reglas de negocio específicas del dominio de precios.</p>
     */
    private final PriceJpaRepository repository;

    /**
     * Mapper automático para conversión entre entidades JPA y entidades de dominio.
     *
     * <p>Implementado con MapStruct para garantizar mapeo type-safe y eficiente.</p>
     */
    private final PriceEntityMapper mapper;

    /**
     * Busca el mejor precio aplicable según criterios de búsqueda del dominio.
     *
     * <p>Este método implementa la lógica de acceso a datos para el caso de uso principal
     * del sistema: encontrar el precio óptimo según las reglas de prioridad y vigencia
     * temporal definidas en el dominio.</p>
     *
     * <h4>Proceso de Búsqueda:</h4>
     * <ol>
     *   <li><strong>Extracción de criterios:</strong> Descompone {@link PriceSearchCriteria}</li>
     *   <li><strong>Consulta especializada:</strong> Usa {@link PriceJpaRepository} con lógica optimizada</li>
     *   <li><strong>Mapeo de resultado:</strong> Convierte {@code PriceEntity} → {@code Price}</li>
     *   <li><strong>Encapsulación Optional:</strong> Maneja casos de ausencia de datos</li>
     * </ol>
     *
     * <h4>Logging de Operaciones:</h4>
     * <p>Registra información relevante para debugging y monitoreo:</p>
     * <ul>
     *   <li><strong>DEBUG:</strong> Parámetros de búsqueda</li>
     *   <li><strong>INFO:</strong> Resultado de búsqueda (encontrado/no encontrado)</li>
     *   <li><strong>WARN:</strong> Casos de búsqueda sin resultados</li>
     * </ul>
     *
     * <h4>Casos de Uso Manejados:</h4>
     * <ul>
     *   <li><strong>Precio encontrado:</strong> Retorna {@code Optional<Price>} con datos</li>
     *   <li><strong>Sin precio aplicable:</strong> Retorna {@code Optional.empty()}</li>
     *   <li><strong>Múltiples candidatos:</strong> JPA Repository selecciona el mejor</li>
     * </ul>
     *
     * @param priceSearchCriteria criterios de búsqueda que encapsulan:
     *                           <ul>
     *                             <li><strong>brandId:</strong> marca del producto</li>
     *                             <li><strong>productId:</strong> identificador del producto</li>
     *                             <li><strong>queryDate:</strong> fecha de consulta</li>
     *                           </ul>
     *
     * @return {@link Optional} con el precio encontrado o vacío si no hay precio aplicable:
     *         <ul>
     *           <li><strong>Optional&lt;Price&gt;:</strong> precio que cumple criterios de negocio</li>
     *           <li><strong>Optional.empty():</strong> no existe precio vigente</li>
     *         </ul>
     *
     * @throws IllegalArgumentException si {@code priceSearchCriteria} es {@code null}
     *                                 o contiene datos inválidos
     *
     * @see PriceJpaRepository#findBestPriceByBrandIdAndProductIdAtDate
     * @see PriceEntityMapper#toDomain
     * @see PriceSearchCriteria
     *
     * @since 1.0.0
     */
    @Override
    public Optional<Price> findBestPrice(PriceSearchCriteria priceSearchCriteria) {
        return this.repository.findBestPriceByBrandIdAndProductIdAtDate(
                priceSearchCriteria.brandId(),
                priceSearchCriteria.productId(), priceSearchCriteria.queryDate()
        ).map(this.mapper::toDomain);
    }
}
