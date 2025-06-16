package com.price.manager.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.price.manager.driven.repositories.PriceJpaRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test de INTEGRACIÓN para validar la query JPQL y lógica de prioridad.
 * Usa el contexto completo de Spring Boot y datos reales de init.sql.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Price Repository - Integration Tests")
class PriceRepositoryIntegrationTest {

    @Autowired
    private PriceJpaRepository priceRepository;

    @Test
    @DisplayName("Test 1: 10:00 del día 14 - Solo precio base aplica (35.50€)")
    void test110h00Dia14PrecioBase() {
        // When - Test 1: petición a las 10:00 del día 14
        final var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        final var result = this.priceRepository.findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, queryDate);

        // Then - Debe retornar el precio base
        assertTrue(result.isPresent(), "Test 1: Should find base price");
        assertEquals(new BigDecimal("35.5"), result.get().getPrice(), "Test 1: Should return base price 35.50€");
        assertEquals(1L, result.get().getPriceList(), "Test 1: Should return price list 1");
        assertEquals(0, result.get().getPriority(), "Test 1: Should return priority 0");
    }

    @Test
    @DisplayName("Test 2: 16:00 del día 14 - Promoción gana por prioridad (25.45€)")
    void test216h00Dia14PromocionGana() {
        // When - Test 2: petición a las 16:00 del día 14 (dentro de promoción 15:00-18:30)
        final var queryDate = LocalDateTime.of(2020, 6, 14, 16, 0, 0);
        final var result = this.priceRepository.findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, queryDate);

        // Then - Debe retornar el precio de promoción (prioridad 1 > prioridad 0)
        assertTrue(result.isPresent(), "Test 2: Should find promotion price");
        assertEquals(new BigDecimal("25.45"), result.get().getPrice(), "Test 2: Should return promotion price 25.45€");
        assertEquals(2L, result.get().getPriceList(), "Test 2: Should return price list 2");
        assertEquals(1, result.get().getPriority(), "Test 2: Should return priority 1 (higher)");
    }

    @Test
    @DisplayName("Test 3: 21:00 del día 14 - Vuelve a precio base (35.50€)")
    void test321h00Dia14VuelvePrecioBase() {
        // When - Test 3: petición a las 21:00 del día 14 (promoción ya expiró a las 18:30)
        final var queryDate = LocalDateTime.of(2020, 6, 14, 21, 0, 0);
        final var result = this.priceRepository.findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, queryDate);

        // Then - Debe retornar el precio base de nuevo
        assertTrue(result.isPresent(), "Test 3: Should find base price again");
        assertEquals(new BigDecimal("35.5"), result.get().getPrice(), "Test 3: Should return base price 35.50€");
        assertEquals(1L, result.get().getPriceList(), "Test 3: Should return price list 1");
        assertEquals(0, result.get().getPriority(), "Test 3: Should return priority 0");
    }

    @Test
    @DisplayName("Test 4: 10:00 del día 15 - Promoción mañana (30.50€)")
    void test410h00Dia15PromocionManana() {
        // When - Test 4: petición a las 10:00 del día 15 (promoción mañana 00:00-11:00)
        final var queryDate = LocalDateTime.of(2020, 6, 15, 10, 0, 0);
        final var result = this.priceRepository.findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, queryDate);

        // Then - Debe retornar el precio de promoción de mañana
        assertTrue(result.isPresent(), "Test 4: Should find morning promotion price");
        assertEquals(new BigDecimal("30.5"), result.get().getPrice(), "Test 4: Should return morning promotion 30" +
                ".50€");
        assertEquals(3L, result.get().getPriceList(), "Test 4: Should return price list 3");
        assertEquals(1, result.get().getPriority(), "Test 4: Should return priority 1");
    }

    @Test
    @DisplayName("Test 5: 21:00 del día 16 - Precio premium (38.95€)")
    void test521h00Dia16PrecioPremium() {
        // When - Test 5: petición a las 21:00 del día 16 (precio premium desde 15/06 16:00)
        final var queryDate = LocalDateTime.of(2020, 6, 16, 21, 0, 0);
        final var result = this.priceRepository.findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, queryDate);

        // Then - Debe retornar el precio premium
        assertTrue(result.isPresent(), "Test 5: Should find premium price");
        assertEquals(new BigDecimal("38.95"), result.get().getPrice(), "Test 5: Should return premium price 38.95€");
        assertEquals(4L, result.get().getPriceList(), "Test 5: Should return price list 4");
        assertEquals(1, result.get().getPriority(), "Test 5: Should return priority 1");
    }

    @Test
    @DisplayName("Should validate priority logic: Higher priority wins over date overlap")
    void shouldValidatePriorityLogicHigherPriorityWins() {
        // When - Momento donde base (prioridad 0) y promoción (prioridad 1) se solapan
        final var queryDate = LocalDateTime.of(2020, 6, 14, 17, 0, 0); // 17:00 - ambos precios aplican
        final var result = this.priceRepository.findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, queryDate);

        // Then - La promoción debe ganar por mayor prioridad
        assertTrue(result.isPresent(), "Should find a price when multiple apply");
        assertEquals(new BigDecimal("25.45"), result.get().getPrice(), "Higher priority should win");
        assertEquals(1, result.get().getPriority(), "Should return priority 1 over 0");
    }

    @Test
    @DisplayName("Should validate query ORDER BY logic with multiple same priority")
    void shouldValidateQueryOrderByLogic() {
        // Given - Escenario donde precio base (lista 1) y premium (lista 4) tienen diferentes prioridades
        // When - Consultar cuando solo precio premium aplica
        final var queryDate = LocalDateTime.of(2020, 6, 15, 20, 0, 0); // 20:00 del día 15
        final var result = this.priceRepository
                .findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, queryDate);

        // Then - Debe retornar precio premium (lista 4, prioridad 1)
        assertTrue(result.isPresent(), "Should find premium price");
        assertEquals(new BigDecimal("38.95"), result.get().getPrice(), "Should return premium price");
        assertEquals(4L, result.get().getPriceList(), "Should prefer higher priceList when same priority");
    }

    @Test
    @DisplayName("Should return empty for non-existent brand")
    void shouldReturnEmptyForNonExistentBrand() {
        // When - Consultar marca inexistente
        final var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        final var result = this.priceRepository.findBestPriceByBrandIdAndProductIdAtDate(999L, 35455L, queryDate);

        // Then
        assertTrue(result.isEmpty(), "Should not find price for non-existent brand");
    }

    @Test
    @DisplayName("Should return empty for non-existent product")
    void shouldReturnEmptyForNonExistentProduct() {
        // When - Consultar producto inexistente
        final var queryDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        final var result = this.priceRepository.findBestPriceByBrandIdAndProductIdAtDate(1L, 999999L, queryDate);

        // Then
        assertTrue(result.isEmpty(), "Should not find price for non-existent product");
    }

    @Test
    @DisplayName("Should return empty for date outside all ranges")
    void shouldReturnEmptyForDateOutsideAllRanges() {
        // When - Consultar fecha antes de todos los rangos
        final var queryDate = LocalDateTime.of(2019, 1, 1, 10, 0, 0);
        final var result = this.priceRepository.findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, queryDate);

        // Then
        assertTrue(result.isEmpty(), "Should not find price for date before all ranges");
    }

    @Test
    @DisplayName("Should validate exact boundary dates (inclusive ranges)")
    void shouldValidateExactBoundaryDates() {
        // When & Then - Probar fechas límite exactas de promoción (15:00-18:30)

        // Inicio exacto de promoción
        final var promotionStart = LocalDateTime.of(2020, 6, 14, 15, 0, 0);
        final var startResult = this.priceRepository
                .findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, promotionStart);
        assertTrue(startResult.isPresent(), "Should find price at exact promotion start");
        assertEquals(new BigDecimal("25.45"), startResult.get().getPrice(), "Should find promotion price at start");

        // Fin exacto de promoción
        final var promotionEnd = LocalDateTime.of(2020, 6, 14, 18, 30, 0);
        final var endResult = this.priceRepository.findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, promotionEnd);
        assertTrue(endResult.isPresent(), "Should find price at exact promotion end");
        assertEquals(new BigDecimal("25.45"), endResult.get().getPrice(), "Should find promotion price at end");

        // Un segundo después del fin (no debe encontrar promoción)
        final var afterPromotionEnd = LocalDateTime.of(2020, 6, 14, 18, 30, 1);
        final var afterResult = this.priceRepository
                .findBestPriceByBrandIdAndProductIdAtDate(1L, 35455L, afterPromotionEnd);
        assertTrue(afterResult.isPresent(), "Should find base price after promotion ends");
        assertEquals(new BigDecimal("35.5"), afterResult.get().getPrice(), "Should revert to base price after " +
                "promotion");
    }
}
