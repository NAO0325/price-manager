package com.price.manager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio Price - Representa un precio aplicable a un producto
 * en un rango de fechas específico con reglas de prioridad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Price {

    private Long brandId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Long priceList;

    private Long productId;

    private Integer priority;

    private BigDecimal price;

    private String curr;

    /**
     * Verifica si este precio es válido para una fecha específica.
     *
     * @param queryDate fecha a consultar
     * @return true si el precio está vigente en esa fecha
     */
    public boolean isValidAt(LocalDateTime queryDate) {
        if (queryDate == null || this.startDate == null || this.endDate == null) {
            return false;
        }
        return !queryDate.isBefore(this.startDate) && !queryDate.isAfter(this.endDate);
    }

    /**
     * Compara si este precio tiene mayor prioridad que otro según reglas de Inditex:
     * 1. Mayor priority gana
     * 2. En caso de empate, mayor priceList gana
     *
     * @param other precio a comparar
     * @return true si este precio tiene mayor prioridad
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
     * Válida que el precio cumple con las reglas básicas de consistencia.
     *
     * @return true si el precio es válido
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
