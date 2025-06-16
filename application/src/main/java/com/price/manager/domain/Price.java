package com.price.manager.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio que representa un precio aplicable a un producto específico
 * de una marca durante un rango de fechas determinado.
 *
 * <p>Esta clase encapsula las reglas de negocio principales del sistema de precios,
 * incluyendo la lógica de prioridad, validación de fechas y consistencia
 * de datos.</p>
 *
 * <p><strong>Reglas de Negocio:</strong></p>
 * <ul>
 *   <li>Prioridad: Mayor valor de prioridad tiene precedencia sobre menor</li>
 *   <li>Desempate: En caso de igual prioridad, mayor priceList gana</li>
 *   <li>Vigencia: El precio debe estar dentro del rango startDate-endDate</li>
 *   <li>Consistencia: Todos los campos obligatorios deben ser válidos</li>
 * </ul>
 *
 * <p><strong>Ejemplo de Uso:</strong></p>
 * <pre>{@code
 * Price price = Price.builder()
 *     .brandId(1L)
 *     .productId(35455L)
 *     .priceList(2L)
 *     .price(new BigDecimal("25.45"))
 *     .startDate(LocalDateTime.of(2020, 6, 14, 15, 0))
 *     .endDate(LocalDateTime.of(2020, 6, 14, 18, 30))
 *     .priority(1)
 *     .curr("EUR")
 *     .build();
 *
 * boolean isValid = price.isValidAt(LocalDateTime.of(2020, 6, 14, 16, 0));
 * }</pre>
 *
 * @version 1.0.0
 * @since 1.0.0
 * @see com.price.manager.domain.criteria.PriceSearchCriteria
 * @see com.price.manager.application.services.PriceServiceUseCase
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Price {

    /**
     * Identificador único de la marca a la que pertenece el producto.
     *
     * <p>Ejemplo: 1 representa la marca ZARA en el sistema.</p>
     *
     */
    private Long brandId;

    /**
     * Fecha y hora de inicio de vigencia del precio.
     *
     * <p>El precio será aplicable desde esta fecha (inclusive) hasta {@link #endDate}.</p>
     *
     */
    private LocalDateTime startDate;

    /**
     * Fecha y hora de fin de vigencia del precio.
     *
     * <p>El precio será aplicable hasta esta fecha (inclusive).</p>
     *
     * @see #isValidAt(LocalDateTime)
     * @see #isConsistent()
     */
    private LocalDateTime endDate;

    /**
     * Identificador de la lista de precios (tarifa).
     *
     * <p>Se utiliza como criterio de desempate cuando dos precios tienen la misma
     * prioridad. El precio con mayor priceList prevalece.</p>
     *
     * @see #hasHigherPriorityThan(Price)
     */
    private Long priceList;

    /**
     * Identificador único del producto al que se aplica el precio.
     *
     * <p>Ejemplo: 35455 representa un producto específico en el catálogo.</p>
     *
     */
    private Long productId;

    /**
     * Nivel de prioridad del precio para resolver conflictos.
     *
     * <p>Cuando múltiples precios son aplicables para la misma fecha, el precio
     * con mayor prioridad prevalece. Valores típicos:</p>
     * <ul>
     *   <li>0: Precio base</li>
     *   <li>1: Promoción o descuento</li>
     *   <li>2+: Promociones especiales</li>
     * </ul>
     *
     * @see #hasHigherPriorityThan(Price)
     */
    private Integer priority;

    /**
     * Valor monetario del precio final de venta del producto.
     *
     * <p>Representa el PVP (Precio de Venta al Público) que debe aplicarse
     * cuando este precio es seleccionado por el sistema.</p>
     */
    private BigDecimal price;

    /**
     * Código ISO 4217 de la moneda en la que se expresa el precio.
     *
     * <p>Ejemplos: "EUR", "USD", "GBP"</p>
     *
     * @see #isConsistent()
     */
    private String curr;

    /**
     * Verifica si este precio es válido (vigente) para una fecha específica.
     *
     * <p>Un precio es válido cuando la fecha consultada está dentro del rango
     * definido por {@link #startDate} y {@link #endDate} (ambos inclusive).</p>
     *
     * @param queryDate la fecha para la cual se quiere verificar la validez.
     *                  No puede ser {@code null}.
     * @return {@code true} si el precio está vigente en la fecha especificada,
     *         {@code false} en caso contrario o si algún parámetro es {@code null}
     *
     * @throws IllegalArgumentException si queryDate es {@code null}
     *
     * @see #startDate
     * @see #endDate
     *
     * @since 1.0.0
     */
    public boolean isValidAt(LocalDateTime queryDate) {
        if (queryDate == null || this.startDate == null || this.endDate == null) {
            return false;
        }
        return !queryDate.isBefore(this.startDate) && !queryDate.isAfter(this.endDate);
    }

    /**
     * Compara si este precio tiene mayor prioridad que otro según las reglas de negocio.
     *
     * <p>Las reglas de comparación son:</p>
     * <ol>
     *   <li>Mayor {@link #priority} gana</li>
     *   <li>En caso de empate en prioridad, mayor {@link #priceList} gana</li>
     * </ol>
     *
     * @param other el precio a comparar. Puede ser {@code null}.
     * @return {@code true} si este precio tiene mayor prioridad que {@code other},
     *         {@code false} en caso contrario. Si {@code other} es {@code null},
     *         retorna {@code true}.
     *
     * @see #priority
     * @see #priceList
     *
     * @since 1.0.0
     */
    public boolean hasHigherPriorityThan(Price other) {
        if (other == null) {
            return true;
        }

        // Comparar por prioridad primero
        final int priorityComparison = Integer.compare(this.priority, other.priority);
        if (priorityComparison != 0) {
            return priorityComparison > 0;
        }

        // Si empatan en prioridad, comparar por priceList
        return this.priceList > other.priceList;
    }

    /**
     * Valida que el precio cumple con las reglas básicas de consistencia del dominio.
     *
     * <p>Un precio es consistente cuando:</p>
     * <ul>
     *   <li>Tiene brandId y productId válidos (> 0)</li>
     *   <li>Tiene prioridad válida (>= 0)</li>
     *   <li>Tiene precio positivo</li>
     *   <li>Tiene fechas válidas (startDate lq endDate)</li>
     *   <li>Tiene moneda válida (no vacía)</li>
     * </ul>
     *
     * @return {@code true} si el precio es consistente, {@code false} en caso contrario
     *
     * @see #brandId
     * @see #productId
     * @see #priority
     * @see #price
     * @see #startDate
     * @see #endDate
     * @see #curr
     *
     * @since 1.0.0
     */
    public boolean isConsistent() {
        return this.brandId != null && this.brandId > 0 &&
               this.productId != null && this.productId > 0 &&
                this.priority != null && this.priority >= 0 &&
                this.price != null && this.price.compareTo(BigDecimal.ZERO) > 0 &&
                this.startDate != null && this.endDate != null &&
                !this.startDate.isAfter(this.endDate) &&
                this.curr != null && !this.curr.trim().isEmpty();
    }

}
