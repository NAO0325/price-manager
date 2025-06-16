package com.price.manager.driven.repositories.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla PRICES en la base de datos H2.
 *
 * <p>Esta clase actúa como adaptador de persistencia en la arquitectura hexagonal,
 * mapeando la estructura de datos del dominio a la representación relacional
 * requerida por la base de datos.</p>
 *
 * <h3>Mapeo de Base de Datos:</h3>
 * <table border="1">
 *   <tr>
 *     <th>Campo Java</th>
 *     <th>Columna DB</th>
 *     <th>Tipo</th>
 *     <th>Descripción</th>
 *   </tr>
 *   <tr>
 *     <td>priceList</td>
 *     <td>PRICE_LIST</td>
 *     <td>BIGINT</td>
 *     <td>ID único, clave primaria</td>
 *   </tr>
 *   <tr>
 *     <td>brandId</td>
 *     <td>BRAND_ID</td>
 *     <td>BIGINT</td>
 *     <td>Identificador de marca</td>
 *   </tr>
 *   <tr>
 *     <td>productId</td>
 *     <td>PRODUCT_ID</td>
 *     <td>BIGINT</td>
 *     <td>Identificador de producto</td>
 *   </tr>
 *   <tr>
 *     <td>startDate</td>
 *     <td>START_DATE</td>
 *     <td>TIMESTAMP</td>
 *     <td>Fecha inicio vigencia</td>
 *   </tr>
 *   <tr>
 *     <td>endDate</td>
 *     <td>END_DATE</td>
 *     <td>TIMESTAMP</td>
 *     <td>Fecha fin vigencia</td>
 *   </tr>
 *   <tr>
 *     <td>priority</td>
 *     <td>PRIORITY</td>
 *     <td>INTEGER</td>
 *     <td>Nivel de prioridad</td>
 *   </tr>
 *   <tr>
 *     <td>price</td>
 *     <td>PRICE</td>
 *     <td>DECIMAL</td>
 *     <td>Valor monetario</td>
 *   </tr>
 *   <tr>
 *     <td>curr</td>
 *     <td>CURR</td>
 *     <td>VARCHAR(3)</td>
 *     <td>Código de moneda</td>
 *   </tr>
 * </table>
 *
 * <h3>Datos de Ejemplo (init.sql):</h3>
 * <pre>
 * | PRICE_LIST | BRAND_ID | PRODUCT_ID | START_DATE          | END_DATE            | PRIORITY | PRICE | CURR |
 * |------------|----------|------------|---------------------|---------------------|----------|-------|------|
 * | 1          | 1        | 35455      | 2020-06-14 00:00:00 | 2020-12-31 23:59:59 | 0        | 35.50 | EUR  |
 * | 2          | 1        | 35455      | 2020-06-14 15:00:00 | 2020-06-14 18:30:00 | 1        | 25.45 | EUR  |
 * | 3          | 1        | 35455      | 2020-06-15 00:00:00 | 2020-06-15 11:00:00 | 1        | 30.50 | EUR  |
 * | 4          | 1        | 35455      | 2020-06-15 16:00:00 | 2020-12-31 23:59:59 | 1        | 38.95 | EUR  |
 * </pre>
 *
 * <h3>Índices Recomendados:</h3>
 * <ul>
 *   <li><strong>Índice compuesto:</strong> (BRAND_ID, PRODUCT_ID, START_DATE, END_DATE)</li>
 *   <li><strong>Índice de prioridad:</strong> (PRIORITY DESC, PRICE_LIST DESC)</li>
 * </ul>
 *
 * @author Napoleon Avila Ochoa
 * @version 1.0.0
 * @since 1.0.0
 *
 * @see com.price.manager.domain.Price
 * @see com.price.manager.driven.repositories.PriceJpaRepository
 * @see com.price.manager.driven.repositories.mappers.PriceEntityMapper
 */
@Entity
@Data
@Builder
@Table(name = "PRICES")
@NoArgsConstructor
@AllArgsConstructor
public class PriceEntity implements Serializable {

    /**
     * Identificador único de la lista de precios y clave primaria de la tabla.
     *
     * <p>Generado automáticamente por la base de datos usando estrategia de identidad.
     * Este campo corresponde al concepto de "tarifa" en el dominio de negocio.</p>
     *
     * <p><strong>Mapeo:</strong> Columna PRICE_LIST, tipo BIGINT, AUTO_INCREMENT</p>
     *
     * @see jakarta.persistence.GenerationType#IDENTITY
     */
    @Id
    @Column(name = "PRICE_LIST", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priceList;

    /**
     * Fecha y hora de inicio de vigencia del precio.
     *
     * <p>Define el momento a partir del cual este precio es aplicable.
     * Usado en consultas de rango temporal para filtrar precios vigentes.</p>
     *
     * <p><strong>Mapeo:</strong> Columna START_DATE, tipo TIMESTAMP, NOT NULL</p>
     *
     * @see #endDate
     */
    @Column(name = "START_DATE", nullable = false)
    private LocalDateTime startDate;

    /**
     * Fecha y hora de fin de vigencia del precio.
     *
     * <p>Define el momento hasta el cual este precio es aplicable.
     * Usado en consultas de rango temporal para filtrar precios vigentes.</p>
     *
     * <p><strong>Mapeo:</strong> Columna END_DATE, tipo TIMESTAMP, NOT NULL</p>
     *
     * @see #startDate
     */
    @Column(name = "END_DATE", nullable = false)
    private LocalDateTime endDate;

    /**
     * Identificador de la marca propietaria del producto.
     *
     * <p>Referencia a la marca en el sistema de Inditex. Ejemplo: 1 = ZARA.</p>
     *
     * <p><strong>Mapeo:</strong> Columna BRAND_ID, tipo BIGINT, NOT NULL</p>
     *
     * @see #productId
     */
    @Column(name = "BRAND_ID", nullable = false)
    private Long brandId;

    /**
     * Identificador único del producto al que se aplica este precio.
     *
     * <p>Referencia al catálogo de productos. Ejemplo: 35455.</p>
     *
     * <p><strong>Mapeo:</strong> Columna PRODUCT_ID, tipo BIGINT, NOT NULL</p>
     *
     * @see #brandId
     */
    @Column(name = "PRODUCT_ID", nullable = false)
    private Long productId;

    /**
     * Nivel de prioridad para resolver conflictos entre precios superpuestos.
     *
     * <p>Cuando múltiples precios son aplicables para la misma fecha, el precio
     * con mayor prioridad prevalece. Valores típicos:</p>
     * <ul>
     *   <li><strong>0:</strong> Precio base</li>
     *   <li><strong>1:</strong> Promoción estándar</li>
     *   <li><strong>2+:</strong> Promociones especiales</li>
     * </ul>
     *
     * <p><strong>Mapeo:</strong> Columna PRIORITY, tipo INTEGER, NOT NULL</p>
     *
     */
    @Column(name = "PRIORITY", nullable = false)
    private Integer priority;

    /**
     * Valor monetario del precio de venta al público (PVP).
     *
     * <p>Representa el precio final que debe aplicarse cuando este registro
     * es seleccionado por las reglas de negocio.</p>
     *
     * <p><strong>Mapeo:</strong> Columna PRICE, tipo DECIMAL, NOT NULL</p>
     *
     * @see #curr
     */
    @Column(name = "PRICE", nullable = false)
    private BigDecimal price;

    /**
     * Código ISO 4217 de la moneda en la que se expresa el precio.
     *
     * <p>Ejemplos válidos: "EUR", "USD", "GBP"</p>
     *
     * <p><strong>Mapeo:</strong> Columna CURR, tipo VARCHAR(3), NOT NULL</p>
     *
     * @see #price
     */
    @Column(name = "CURR", nullable = false)
    private String curr;

}
