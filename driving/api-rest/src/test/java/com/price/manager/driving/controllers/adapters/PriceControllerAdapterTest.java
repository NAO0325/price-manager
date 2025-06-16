package com.price.manager.driving.controllers.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import com.price.manager.application.ports.driving.PriceServicePort;
import com.price.manager.domain.Price;
import com.price.manager.driving.controllers.error.PriceNotFoundException;
import com.price.manager.driving.controllers.mappers.PriceMapper;
import com.price.manager.driving.controllers.models.PriceResponse;
import com.price.manager.driving.controllers.utils.PriceMocks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/**
 * Tests mejorados para PriceControllerAdapter con validaciones robustas.
 * Validar comportamiento del adaptador de entrada sin dependencias externas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Price Controller Adapter - Enhanced Unit Tests")
class PriceControllerAdapterTest {

    @Mock
    private PriceServicePort priceServicePort;

    @Mock
    private PriceMapper priceMapper;

    @InjectMocks
    private PriceControllerAdapter priceControllerAdapter;

    private Price testDomainPrice;

    private PriceResponse testResponse;

    private PriceMocks mocks;

    @BeforeEach
    void setUp() {
        this.mocks = new PriceMocks();
        this.testDomainPrice = this.mocks.createTestDomainPrice();
        this.testResponse = this.mocks.createTestPriceResponse();
    }

    /**
     * Casos de prueba que validan diferentes escenarios según requerimientos de negocio.
     */
    static Stream<Arguments> testScenarios() {
        return Stream.of(
                Arguments.of(
                        "2020-06-14T10:00:00Z", 1L, 35455L, "35.50", 1L,
                        "Test 1: Should return base price at 10:00"
                ),
                Arguments.of(
                        "2020-06-14T16:00:00Z", 1L, 35455L, "25.45", 2L,
                        "Test 2: Should return promotion price at 16:00"
                ),
                Arguments.of(
                        "2020-06-14T21:00:00Z", 1L, 35455L, "35.50", 1L,
                        "Test 3: Should return base price at 21:00"
                ),
                Arguments.of(
                        "2020-06-15T10:00:00Z", 1L, 35455L, "30.50", 3L,
                        "Test 4: Should return morning promotion at 10:00"
                ),
                Arguments.of(
                        "2020-06-16T21:00:00Z", 1L, 35455L, "38.95", 4L,
                        "Test 5: Should return premium price at 21:00"
                )
        );
    }

    @ParameterizedTest(name = "{5}")
    @MethodSource("testScenarios")
    @DisplayName("Should return valid price response with correct data for scenarios")
    void shouldReturnValidPriceResponseForScenarios(
            String dateQueryStr, Long brandId, Long productId,
            BigDecimal expectedPrice, Long expectedPriceList, String description) {

        // Given
        final var dateQuery = OffsetDateTime.parse(dateQueryStr);
        final var domainPrice = this.mocks.createDomainPriceFor(brandId, productId, expectedPrice, expectedPriceList);
        final var expectedResponse = this.mocks.createPriceResponseFor(brandId, expectedPrice.doubleValue(),
                expectedPriceList);

        when(this.priceServicePort.findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime()))
                .thenReturn(domainPrice);
        when(this.priceMapper.toResponseDto(domainPrice))
                .thenReturn(expectedResponse);

        // When
        final var response = this.priceControllerAdapter.findByBrandProductBetweenDate(brandId, productId, dateQuery);

        // Then - Validaciones específicas del contenido
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");

        final var responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(brandId, responseBody.getBrandId(), "Brand ID should match");
        assertEquals(expectedPrice.doubleValue(), responseBody.getPrice(), "Price should match expected value");
        assertEquals(expectedPriceList, responseBody.getId(), "Price list ID should match");
        assertNotNull(responseBody.getStartDate(), "Start date should not be null");
        assertNotNull(responseBody.getEndDate(), "End date should not be null");

        // Verify service interactions
        verify(this.priceServicePort).findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime());
        verify(this.priceMapper).toResponseDto(domainPrice);
        verifyNoMoreInteractions(this.priceServicePort, this.priceMapper);
    }

    @Test
    @DisplayName("Should return HTTP 200 OK with valid data for successful request")
    void shouldReturnHttp200OkWithValidDataForSuccessfulRequest() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var dateQuery = OffsetDateTime.of(2020, 6, 14, 10, 0, 0, 0, ZoneOffset.UTC);

        when(this.priceServicePort.findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime()))
                .thenReturn(this.testDomainPrice);
        when(this.priceMapper.toResponseDto(this.testDomainPrice))
                .thenReturn(this.testResponse);

        // When
        final var response = this.priceControllerAdapter.findByBrandProductBetweenDate(brandId, productId, dateQuery);

        // Then - Validaciones específicas NO SOLO assertNotNull
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");

        final var body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals(1L, body.getBrandId(), "Brand ID should be 1");
        assertEquals(35455L, body.getPrice(), "Price should match test response");
        assertEquals(2L, body.getId(), "ID should match price list");

        // Validar que las fechas están presentes y en formato correcto
        assertNotNull(body.getStartDate(), "Start date should not be null");
        assertNotNull(body.getEndDate(), "End date should not be null");
        assertEquals(ZoneOffset.UTC, body.getStartDate().getOffset(), "Start date should be in UTC");
        assertEquals(ZoneOffset.UTC, body.getEndDate().getOffset(), "End date should be in UTC");
    }

    @Test
    @DisplayName("Should throw PriceNotFoundException when service returns null")
    void shouldThrowPriceNotFoundExceptionWhenServiceReturnsNull() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var dateQuery = OffsetDateTime.of(2020, 6, 14, 10, 0, 0, 0, ZoneOffset.UTC);

        when(this.priceServicePort.findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime()))
                .thenReturn(null);
        when(this.priceMapper.toResponseDto(null))
                .thenReturn(null);

        // When & Then
        final var exception = assertThrows(PriceNotFoundException.class, () ->
                        this.priceControllerAdapter.findByBrandProductBetweenDate(brandId, productId, dateQuery),
                "Should throw PriceNotFoundException when service returns null");

        assertEquals("No price found for the given parameters", exception.getMessage(),
                "Exception message should be specific");

        verify(this.priceServicePort).findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime());
        verify(this.priceMapper).toResponseDto(null);
    }

    @Test
    @DisplayName("Should throw PriceNotFoundException when mapper returns null")
    void shouldThrowPriceNotFoundExceptionWhenMapperReturnsNull() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var dateQuery = OffsetDateTime.of(2020, 6, 14, 10, 0, 0, 0, ZoneOffset.UTC);

        when(this.priceServicePort.findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime()))
                .thenReturn(this.testDomainPrice);
        when(this.priceMapper.toResponseDto(this.testDomainPrice))
                .thenReturn(null); // Mapper falla y retorna null

        // When & Then
        final var exception = assertThrows(PriceNotFoundException.class, () ->
                        this.priceControllerAdapter.findByBrandProductBetweenDate(brandId, productId, dateQuery),
                "Should throw PriceNotFoundException when mapper returns null");

        assertEquals("No price found for the given parameters", exception.getMessage(),
                "Exception message should be consistent");

        verify(this.priceServicePort).findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime());
        verify(this.priceMapper).toResponseDto(this.testDomainPrice);
    }

    @Test
    @DisplayName("Should propagate service exceptions correctly")
    void shouldPropagateServiceExceptionsCorrectly() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var dateQuery = OffsetDateTime.of(2020, 6, 14, 10, 0, 0, 0, ZoneOffset.UTC);
        final var expectedException = new RuntimeException("Database connection failed");

        when(this.priceServicePort.findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime()))
                .thenThrow(expectedException);

        // When & Then
        final var thrownException = assertThrows(RuntimeException.class, () ->
                        this.priceControllerAdapter.findByBrandProductBetweenDate(brandId, productId, dateQuery),
                "Should propagate service exceptions");

        assertEquals(expectedException, thrownException, "Should propagate the exact same exception");
        assertEquals("Database connection failed", thrownException.getMessage(),
                "Exception message should be preserved");

        verify(this.priceServicePort).findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime());
        verifyNoInteractions(this.priceMapper);
    }

    @Test
    @DisplayName("Should convert OffsetDateTime to LocalDateTime correctly")
    void shouldConvertOffsetDateTimeToLocalDateTimeCorrectly() {
        // Given
        final var offsetDateTime = OffsetDateTime.of(2020, 6, 14, 16, 30, 45, 123000000, ZoneOffset.UTC);
        final var expectedLocalDateTime = LocalDateTime.of(2020, 6, 14, 16, 30, 45, 123000000);
        final var brandId = 1L;
        final var productId = 35455L;

        when(this.priceServicePort.findByBrandProductBetweenDate(brandId, productId, expectedLocalDateTime))
                .thenReturn(null);
        when(this.priceMapper.toResponseDto(null))
                .thenReturn(null);

        // When & Then
        assertThrows(PriceNotFoundException.class, () ->
                this.priceControllerAdapter.findByBrandProductBetweenDate(brandId, productId, offsetDateTime));

        // Verify that exact LocalDateTime conversion happened
        verify(this.priceServicePort).findByBrandProductBetweenDate(brandId, productId, expectedLocalDateTime);
    }

    @Test
    @DisplayName("Should handle different timezone offsets correctly")
    void shouldHandleDifferentTimezoneOffsetsCorrectly() {
        // Given - Fecha con offset diferente a UTC
        final var offsetDateTime = OffsetDateTime.of(2020, 6, 14, 18, 0, 0, 0, ZoneOffset.ofHours(2)); // +02:00
        final var expectedLocalDateTime = LocalDateTime.of(2020, 6, 14, 18, 0, 0); // Debe usar la hora local
        final var brandId = 1L;
        final var productId = 35455L;

        when(this.priceServicePort.findByBrandProductBetweenDate(brandId, productId, expectedLocalDateTime))
                .thenReturn(this.testDomainPrice);
        when(this.priceMapper.toResponseDto(this.testDomainPrice))
                .thenReturn(this.testResponse);

        // When
        final var response = this.priceControllerAdapter.findByBrandProductBetweenDate(brandId, productId,
                offsetDateTime);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return OK status");

        // Verify conversion happened correctly (toLocalDateTime() ignores offset)
        verify(this.priceServicePort).findByBrandProductBetweenDate(brandId, productId, expectedLocalDateTime);
    }

    @Test
    @DisplayName("Should validate parameter passing to service layer accurately")
    void shouldValidateParameterPassingToServiceLayerAccurately() {
        // Given
        final var brandId = 99L;
        final var productId = 12345L;
        final var dateQuery = OffsetDateTime.of(2021, 3, 15, 14, 30, 45, 987654321, ZoneOffset.UTC);

        when(this.priceServicePort.findByBrandProductBetweenDate(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(null);
        when(this.priceMapper.toResponseDto(null))
                .thenReturn(null);

        // When
        assertThrows(PriceNotFoundException.class, () ->
                this.priceControllerAdapter.findByBrandProductBetweenDate(brandId, productId, dateQuery));

        // Then - Verify exact parameters were passed
        verify(this.priceServicePort).findByBrandProductBetweenDate(
                99L,
                12345L,
                LocalDateTime.of(2021, 3, 15, 14, 30, 45, 987654321)
        );
    }

    @Test
    @DisplayName("Should handle NumberFormatException from service gracefully")
    void shouldHandleNumberFormatExceptionFromServiceGracefully() {
        // Given
        final var brandId = 1L;
        final var productId = 35455L;
        final var dateQuery = OffsetDateTime.of(2020, 6, 14, 10, 0, 0, 0, ZoneOffset.UTC);
        final var expectedException = new NumberFormatException("Invalid number format");

        when(this.priceServicePort.findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime()))
                .thenThrow(expectedException);

        // When & Then
        final var thrownException = assertThrows(NumberFormatException.class, () ->
                        this.priceControllerAdapter.findByBrandProductBetweenDate(brandId, productId, dateQuery),
                "Should propagate NumberFormatException");

        assertEquals("Invalid number format", thrownException.getMessage(),
                "Exception message should be preserved");

        verify(this.priceServicePort).findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime());
        verifyNoInteractions(this.priceMapper);
    }
}
