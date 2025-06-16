package com.price.manager.driving.controllers.mappers;

import com.price.manager.domain.Price;
import com.price.manager.driving.controllers.utils.PriceMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests comprensivos para PriceMapper con validaciones robustas.
 * Cubre mapeo completo, edge cases y conversiones de zona horaria.
 */
@DisplayName("Price Mapper - Comprehensive Tests")
class PriceMapperTest {

    private PriceMocks mocks;

    private PriceMapper priceMapper;

    @BeforeEach
    void setUp() {
        this.mocks = new PriceMocks();
        this.priceMapper = Mappers.getMapper(PriceMapper.class);
    }

    @Test
    @DisplayName("Should map all fields correctly from domain to response DTO")
    void shouldMapAllFieldsCorrectlyFromDomainToResponseDto() {
        // Given
        final var domainPrice = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priceList(2L)
                .price(new BigDecimal("25.45"))
                .startDate(LocalDateTime.of(2020, 6, 14, 15, 0, 0))
                .endDate(LocalDateTime.of(2020, 6, 14, 18, 30, 0))
                .priority(1)
                .curr("EUR")
                .build();

        // When
        final var responseDto = this.priceMapper.toResponseDto(domainPrice);

        // Then - Validar TODOS los campos mapeados
        assertNotNull(responseDto, "Response DTO should not be null");
        assertEquals(2L, responseDto.getId(), "ID should map from priceList");
        assertEquals(1L, responseDto.getBrandId(), "Brand ID should be mapped correctly");
        assertEquals(25.45, responseDto.getPrice(), "Price should be mapped correctly");

        // Verificar conversión UTC específica
        final var expectedStartDate = OffsetDateTime.of(2020, 6, 14, 15, 0, 0, 0, ZoneOffset.UTC);
        final var expectedEndDate = OffsetDateTime.of(2020, 6, 14, 18, 30, 0, 0, ZoneOffset.UTC);

        assertEquals(expectedStartDate, responseDto.getStartDate(), "Start date should be converted to UTC");
        assertEquals(expectedEndDate, responseDto.getEndDate(), "End date should be converted to UTC");
        assertEquals(ZoneOffset.UTC, responseDto.getStartDate().getOffset(), "Start date should have UTC offset");
        assertEquals(ZoneOffset.UTC, responseDto.getEndDate().getOffset(), "End date should have UTC offset");
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    void shouldHandleNullInputGracefully() {
        // When
        final var result = this.priceMapper.toResponseDto(null);

        // Then
        assertNull(result, "Mapper should return null for null input");
    }

    @Test
    @DisplayName("Should handle null dates in domain object correctly")
    void shouldHandleNullDatesInDomainObjectCorrectly() {
        // Given
        final var priceWithNullDates = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priceList(1L)
                .price(new BigDecimal("35.50"))
                .startDate(null)
                .endDate(null)
                .priority(0)
                .curr("EUR")
                .build();

        // When
        final var result = this.priceMapper.toResponseDto(priceWithNullDates);

        // Then
        assertNotNull(result, "Result should not be null even with null dates");
        assertNull(result.getStartDate(), "Start date should be null when input is null");
        assertNull(result.getEndDate(), "End date should be null when input is null");
        assertEquals(1L, result.getBrandId(), "Other fields should still be mapped");
        assertEquals(35.50, result.getPrice(), "Price should be mapped correctly");
        assertEquals(1L, result.getId(), "ID should be mapped from priceList");
    }

    @Test
    @DisplayName("Should convert LocalDateTime to UTC OffsetDateTime correctly")
    void shouldConvertLocalDateTimeToUtcOffsetDateTimeCorrectly() {
        // Given
        final var testDate = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

        // When
        final var result = this.priceMapper.toUtcOffsetDateTime(testDate);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(ZoneOffset.UTC, result.getOffset(), "Should have UTC offset");
        assertEquals(testDate, result.toLocalDateTime(), "Local date time should be preserved");
        assertEquals(2020, result.getYear(), "Year should be correct");
        assertEquals(12, result.getMonthValue(), "Month should be correct");
        assertEquals(31, result.getDayOfMonth(), "Day should be correct");
        assertEquals(23, result.getHour(), "Hour should be correct");
        assertEquals(59, result.getMinute(), "Minute should be correct");
        assertEquals(59, result.getSecond(), "Second should be correct");
    }

    @Test
    @DisplayName("Should handle null LocalDateTime in UTC conversion")
    void shouldHandleNullLocalDateTimeInUtcConversion() {
        // When
        final var result = this.priceMapper.toUtcOffsetDateTime(null);

        // Then
        assertNull(result, "Should return null for null input");
    }

    /**
     * Casos de prueba para fechas extremas y edge cases
     */
    static Stream<Arguments> edgeDateCases() {
        return Stream.of(
                Arguments.of(
                        LocalDateTime.MIN, "LocalDateTime.MIN should be handled",
                        OffsetDateTime.of(LocalDateTime.MIN, ZoneOffset.UTC)
                ),
                Arguments.of(
                        LocalDateTime.MAX, "LocalDateTime.MAX should be handled",
                        OffsetDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC)
                ),
                Arguments.of(
                        LocalDateTime.of(2020, 1, 1, 0, 0, 0), "Start of year 2020",
                        OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
                ),
                Arguments.of(
                        LocalDateTime.of(2020, 12, 31, 23, 59, 59), "End of year 2020",
                        OffsetDateTime.of(2020, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)
                ),
                Arguments.of(
                        LocalDateTime.of(2020, 2, 29, 12, 0, 0), "Leap year date",
                        OffsetDateTime.of(2020, 2, 29, 12, 0, 0, 0, ZoneOffset.UTC)
                )
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("edgeDateCases")
    @DisplayName("Should handle edge case dates correctly")
    void shouldHandleEdgeCaseDatesCorrectly(LocalDateTime inputDate, String description,
                                            OffsetDateTime expectedOutput) {
        // Given
        final var priceWithEdgeDate = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priceList(1L)
                .price(new BigDecimal("35.50"))
                .startDate(inputDate)
                .endDate(inputDate)
                .priority(0)
                .curr("EUR")
                .build();

        // When
        final var result = this.priceMapper.toResponseDto(priceWithEdgeDate);

        // Then
        assertNotNull(result, "Result should not be null for: " + description);
        assertEquals(expectedOutput, result.getStartDate(), "Start date should match expected for: " + description);
        assertEquals(expectedOutput, result.getEndDate(), "End date should match expected for: " + description);
        assertEquals(ZoneOffset.UTC, result.getStartDate().getOffset(), "Should have UTC offset");
        assertEquals(ZoneOffset.UTC, result.getEndDate().getOffset(), "Should have UTC offset");
    }

    /**
     * Casos de prueba con diferentes tipos de precios y valores
     */
    static Stream<Arguments> priceValueCases() {
        return Stream.of(
                Arguments.of("0.01", "Minimum price value"),
                Arguments.of("999999.99", "Maximum reasonable price"),
                Arguments.of("25.45", "Standard price with decimals"),
                Arguments.of("100.00", "Round price value"),
                Arguments.of("33.333", "Price with many decimals"),
                Arguments.of("0.0", "Zero price value"),
                Arguments.of("1.0", "Unit price value")
        );
    }

    @ParameterizedTest(name = "Price: {0} - {1}")
    @MethodSource("priceValueCases")
    @DisplayName("Should handle different price values correctly")
    void shouldHandleDifferentPriceValuesCorrectly(BigDecimal priceValue, String description) {
        // Given
        final var domainPrice = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priceList(1L)
                .price(priceValue)
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59))
                .priority(0)
                .curr("EUR")
                .build();

        // When
        final var result = this.priceMapper.toResponseDto(domainPrice);

        // Then
        assertNotNull(result, "Result should not be null for: " + description);
        assertEquals(priceValue.doubleValue(), result.getPrice(), "Price should match input for: " + description);
        assertEquals(1L, result.getId(), "ID should be mapped correctly");
        assertEquals(1L, result.getBrandId(), "Brand ID should be mapped correctly");
    }

    @Test
    @DisplayName("Should preserve precision in price mapping")
    void shouldPreservePrecisionInPriceMapping() {
        // Given - Precio con precisión decimal específica
        final var precisePrice = new BigDecimal("25.4567891234");
        final var domainPrice = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priceList(1L)
                .price(precisePrice)
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59))
                .priority(0)
                .curr("EUR")
                .build();

        // When
        final var result = this.priceMapper.toResponseDto(domainPrice);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(precisePrice.doubleValue(), result.getPrice(), "Price precision should be preserved");
        assertTrue(result.getPrice() > 25.4567, "Price should maintain high precision");
        assertTrue(result.getPrice() < 25.4568, "Price should maintain exact precision");
    }

    @Test
    @DisplayName("Should map priceList to id field correctly for different values")
    void shouldMapPriceListToIdFieldCorrectlyForDifferentValues() {
        // Given - Different priceList values
        final var testCases = new Long[]{1L, 2L, 3L, 4L, 999L, 0L};

        for (final Long priceListValue : testCases) {
            final var domainPrice = this.mocks.createBasicPriceWithPriceList(priceListValue);

            // When
            final var result = this.priceMapper.toResponseDto(domainPrice);

            // Then
            assertNotNull(result, "Result should not be null for priceList: " + priceListValue);
            assertEquals(priceListValue, result.getId(), "ID should match priceList value: " + priceListValue);
        }
    }

    @Test
    @DisplayName("Should maintain timezone consistency in date conversion")
    void shouldMaintainTimezoneConsistencyInDateConversion() {
        // Given - Fechas en diferentes momentos del día
        final var morningDate = LocalDateTime.of(2020, 6, 14, 9, 0, 0);
        final var eveningDate = LocalDateTime.of(2020, 6, 14, 21, 45, 0);

        final var domainPrice = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priceList(1L)
                .price(new BigDecimal("35.50"))
                .startDate(morningDate)
                .endDate(eveningDate)
                .priority(0)
                .curr("EUR")
                .build();

        // When
        final var result = this.priceMapper.toResponseDto(domainPrice);

        // Then - Todas las fechas deben estar en UTC
        assertNotNull(result, "Result should not be null");
        assertEquals(ZoneOffset.UTC, result.getStartDate().getOffset(), "Start date should have UTC offset");
        assertEquals(ZoneOffset.UTC, result.getEndDate().getOffset(), "End date should have UTC offset");
        assertEquals(morningDate, result.getStartDate().toLocalDateTime(), "Start date should preserve local time");
        assertEquals(eveningDate, result.getEndDate().toLocalDateTime(), "End date should preserve local time");
    }

    @Test
    @DisplayName("Should handle microseconds and nanoseconds in date conversion")
    void shouldHandleMicrosecondsAndNanosecondsInDateConversion() {
        // Given - Fecha con precisión de nanosegundos
        final var preciseDate = LocalDateTime.of(2020, 6, 14, 15, 30, 45, 123456789);

        final var domainPrice = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priceList(1L)
                .price(new BigDecimal("35.50"))
                .startDate(preciseDate)
                .endDate(preciseDate)
                .priority(0)
                .curr("EUR")
                .build();

        // When
        final var result = this.priceMapper.toResponseDto(domainPrice);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(preciseDate, result.getStartDate().toLocalDateTime(), "Should preserve nanosecond precision");
        assertEquals(123456789, result.getStartDate().getNano(), "Should preserve exact nanoseconds");
        assertEquals(15, result.getStartDate().getHour(), "Should preserve hour");
        assertEquals(30, result.getStartDate().getMinute(), "Should preserve minute");
        assertEquals(45, result.getStartDate().getSecond(), "Should preserve second");
    }

    @Test
    @DisplayName("Should validate Test 2 scenario mapping correctly")
    void shouldValidateTest2ScenarioMappingCorrectly() {
        // Given - Datos específicos del Test 2
        final var test2Price = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priceList(2L)  // Lista 2 (promoción)
                .price(new BigDecimal("25.45"))   // Precio promoción
                .startDate(LocalDateTime.of(2020, 6, 14, 15, 0, 0))  // 15:00
                .endDate(LocalDateTime.of(2020, 6, 14, 18, 30, 0))   // 18:30
                .priority(1)    // Prioridad mayor
                .curr("EUR")
                .build();

        // When
        final var result = this.priceMapper.toResponseDto(test2Price);

        // Then - Validar mapeo específico para Test 2
        assertNotNull(result, "Test 2 result should not be null");
        assertEquals(2L, result.getId(), "Should map priceList 2 to id for Test 2");
        assertEquals(1L, result.getBrandId(), "Should map brand ID 1 for Test 2");
        assertEquals(25.45, result.getPrice(), "Should map promotion price 25.45 for Test 2");

        final var expectedStart = OffsetDateTime.of(2020, 6, 14, 15, 0, 0, 0, ZoneOffset.UTC);
        final var expectedEnd = OffsetDateTime.of(2020, 6, 14, 18, 30, 0, 0, ZoneOffset.UTC);

        assertEquals(expectedStart, result.getStartDate(), "Should map start date correctly for Test 2");
        assertEquals(expectedEnd, result.getEndDate(), "Should map end date correctly for Test 2");
    }

    @Test
    @DisplayName("Should handle all test scenarios correctly")
    void shouldHandleTestScenariosCorrectly() {
        // Test scenario data from requirements
        final var testScenarios = new Object[][]{
                {1L, 0, "35.50", "2020-06-14T00:00:00", "2020-12-31T23:59:59"}, // Test 1
                {2L, 1, "25.45", "2020-06-14T15:00:00", "2020-06-14T18:30:00"}, // Test 2
                {3L, 1, "30.50", "2020-06-15T00:00:00", "2020-06-15T11:00:00"}, // Test 4
                {4L, 1, "38.95", "2020-06-15T16:00:00", "2020-12-31T23:59:59"}  // Test 5
        };

        for (final var scenario : testScenarios) {
            // Given
            final var priceList = (Long) scenario[0];
            final var priority = (Integer) scenario[1];
            final var price = new BigDecimal(scenario[2].toString());
            final var startDateStr = (String) scenario[3];
            final var endDateStr = (String) scenario[4];

            final var domainPrice = Price.builder()
                    .brandId(1L)
                    .productId(35455L)
                    .priceList(priceList)
                    .price(price)
                    .startDate(LocalDateTime.parse(startDateStr))
                    .endDate(LocalDateTime.parse(endDateStr))
                    .priority(priority)
                    .curr("EUR")
                    .build();

            // When
            final var result = this.priceMapper.toResponseDto(domainPrice);

            // Then
            assertNotNull(result, "Result should not be null for scenario with priceList: " + priceList);
            assertEquals(priceList, result.getId(), "ID should match priceList for scenario: " + priceList);
            assertEquals(price.doubleValue(), result.getPrice(), "Price should match for scenario: " + priceList);
            assertEquals(1L, result.getBrandId(), "Brand ID should be 1 for scenario: " + priceList);
        }
    }

    @Test
    @DisplayName("Should handle mapping with null currency correctly")
    void shouldHandleMappingWithNullCurrencyCorrectly() {
        // Given
        final var priceWithNullCurrency = Price.builder()
                .brandId(1L)
                .productId(35455L)
                .priceList(1L)
                .price(new BigDecimal("35.50"))
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59))
                .priority(0)
                .curr(null)  // Null currency
                .build();

        // When
        final var result = this.priceMapper.toResponseDto(priceWithNullCurrency);

        // Then
        assertNotNull(result, "Result should not be null even with null currency");
        assertEquals(1L, result.getBrandId(), "Brand ID should be mapped correctly");
        assertEquals(35.50, result.getPrice(), "Price should be mapped correctly");
        assertEquals(1L, result.getId(), "ID should be mapped correctly");
        // Note: Currency is not mapped to response DTO, so we don't test it
    }

    @Test
    @DisplayName("Should validate direct UTC conversion method")
    void shouldValidateDirectUtcConversionMethod() {
        // Given - Various LocalDateTime instances
        final var testDates = new LocalDateTime[]{
                LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                LocalDateTime.of(2020, 6, 14, 15, 30, 45),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59, 999999999),
                LocalDateTime.now()
        };

        for (final var testDate : testDates) {
            // When
            final var result = this.priceMapper.toUtcOffsetDateTime(testDate);

            // Then
            assertNotNull(result, "Result should not be null for date: " + testDate);
            assertEquals(ZoneOffset.UTC, result.getOffset(), "Should always convert to UTC");
            assertEquals(testDate, result.toLocalDateTime(), "Local time should be preserved");
            assertEquals(testDate.getYear(), result.getYear(), "Year should be preserved");
            assertEquals(testDate.getMonth(), result.getMonth(), "Month should be preserved");
            assertEquals(testDate.getDayOfMonth(), result.getDayOfMonth(), "Day should be preserved");
            assertEquals(testDate.getHour(), result.getHour(), "Hour should be preserved");
            assertEquals(testDate.getMinute(), result.getMinute(), "Minute should be preserved");
            assertEquals(testDate.getSecond(), result.getSecond(), "Second should be preserved");
        }
    }
}
