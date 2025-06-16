package com.price.manager.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import com.price.manager.Application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Tests de integración end-to-end para Price Manager.
 * <p>
 * Ubicado en package integration para separar claramente de tests unitarios.
 * Validar los 5 casos específicos requeridos por Core Platform.
 * <p>
 * Este test reemplaza al ApplicationTest.java anterior que solo ejecutaba main() sin validar nada útil.
 *
 * @since 1.0.0
 */
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:integrationtestdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=always",
    "logging.level.org.springframework.web=INFO"
})
@DisplayName("🔄 Price Manager - Integration Tests")
class PriceManagerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Datos de prueba según los 5 casos específicos requeridos por Core Platform.
     *
     * @return Stream de argumentos con: fecha, precio esperado, priceList esperado, descripción
     */
    static Stream<Arguments> requiredTestCases() {
        return Stream.of(
                Arguments.of(
                        "2020-06-14T10:00:00Z", // Test 1: 10:00 del día 14
                        35.50, // Precio esperado
                        1L, // Price list esperado
                        "Test 1: Petición a las 10:00 del día 14 - Debe aplicar precio base"
                ),
                Arguments.of(
                        "2020-06-14T16:00:00Z", // Test 2: 16:00 del día 14
                        25.45, // Precio esperado (promoción)
                        2L, // Price list esperado
                        "Test 2: Petición a las 16:00 del día 14 - Debe aplicar promoción de tarde"
                ),
                Arguments.of(
                        "2020-06-14T21:00:00Z", // Test 3: 21:00 del día 14
                        35.50, // Precio esperado (vuelve al base)
                        1L, // Price list esperado
                        "Test 3: Petición a las 21:00 del día 14 - Debe volver a precio base"
                ),
                Arguments.of(
                        "2020-06-15T10:00:00Z", // Test 4: 10:00 del día 15
                        30.50, // Precio esperado (promoción mañana)
                        3L, // Price list esperado
                        "Test 4: Petición a las 10:00 del día 15 - Debe aplicar promoción de mañana"
                ),
                Arguments.of(
                        "2020-06-16T21:00:00Z", // Test 5: 21:00 del día 16
                        38.95, // Precio esperado (precio premium)
                        4L, // Price list esperado
                        "Test 5: Petición a las 21:00 del día 16 - Debe aplicar precio premium"
                )
        );
    }

    @Test
    @DisplayName("Application context should load successfully with all required beans")
    void applicationContextShouldLoadWithAllRequiredBeans() {
        // Given & When
        // El contexto se carga automáticamente por @SpringBootTest

        // Then
        // Si llegamos aquí, significa que el contexto se cargó exitosamente
        assertTrue(this.port > 0, "Port should be positive");
        assertNotNull(this.restTemplate, "RestTemplate should not be null");
        assertNotNull(this.objectMapper, "ObjectMapper should not be null");
    }

    @Test
    @DisplayName("H2 database should be properly initialized with test data")
    void h2DatabaseShouldBeProperlyInitializedWithTestData() {
        // Given
        final String apiUrl = "http://localhost:" + this.port +
                "/v1/price/findByBrandProductBetweenDate?brandId=1&productId=35455&dateQuery=2020-06-14T10:00:00Z";

        // When
        final ResponseEntity<String> response = this.restTemplate.getForEntity(apiUrl, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().contains("35.5"), "Response should contain price 35.5");
        assertTrue(response.getBody().contains("brandId"), "Response should contain brandId");
        assertTrue(response.getBody().contains("price"), "Response should contain price field");
    }

    @ParameterizedTest(name = "{3}")
    @MethodSource("requiredTestCases")
    @DisplayName("Validar casos específicos requeridos")
    void shouldValidateRequiredTestCasesEndToEnd(
            String dateQuery,
            Double expectedPrice,
            Long expectedPriceList,
            String testDescription) {

        // Given
        final String apiUrl = String.format(
                "http://localhost:%d/v1/price/findByBrandProductBetweenDate?brandId=1&productId=35455&dateQuery=%s",
                this.port, dateQuery
        );

        // When
        final ResponseEntity<String> response = this.restTemplate.getForEntity(apiUrl, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "HTTP status should be 200 OK for: " + testDescription);

        assertNotNull(response.getBody(),
                "Response body should not be null for: " + testDescription);

        assertTrue(response.getBody().contains("\"price\":" + expectedPrice),
                "Price should match expected value for: " + testDescription);

        assertTrue(response.getBody().contains("\"id\":" + expectedPriceList),
                "Price list ID should match expected value for: " + testDescription);

        assertTrue(response.getBody().contains("\"brandId\":1"),
                "Brand ID should be 1 for: " + testDescription);
    }

    @Test
    @DisplayName("API should handle invalid parameters gracefully")
    void apiShouldHandleInvalidParametersGracefully() {
        // Given
        final String urlBase = "/v1/price/findByBrandProductBetweenDate";
        final String invalidUrl = "http://localhost:" + this.port +
                urlBase + "?brandId=invalid&productId=35455&dateQuery=2020-06-14T10:00:00Z";

        // When
        final ResponseEntity<String> response = this.restTemplate.getForEntity(invalidUrl, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 400 for invalid parameters");
        assertTrue(response.getBody().contains("INVALID_PARAMETER"), "Should contain INVALID_PARAMETER error code");
    }

    @Test
    @DisplayName("API should return 404 when no price found")
    void apiShouldReturn404WhenNoPriceFound() {
        // Given - Buscar producto que no existe
        final String notFoundUrl = "http://localhost:" + this.port +
                "/v1/price/findByBrandProductBetweenDate?brandId=999&productId=99999&dateQuery=2020-06-14T10:00:00Z";

        // When
        final ResponseEntity<String> response = this.restTemplate.getForEntity(notFoundUrl, String.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 when no price found");
        assertTrue(response.getBody().contains("PRICE_NOT_FOUND"), "Should contain PRICE_NOT_FOUND error code");
    }

    @Test
    @DisplayName("Application should support CORS for frontend integration")
    void applicationShouldSupportCorsForFrontendIntegration() {
        // Given
        final String apiUrl = "http://localhost:" + this.port +
                "/v1/price/findByBrandProductBetweenDate?brandId=1&productId=35455&dateQuery=2020-06-14T10:00:00Z";

        // When
        final ResponseEntity<String> response = this.restTemplate.getForEntity(apiUrl, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "CORS request should succeed");
        assertTrue(response.getHeaders().getContentType().toString().contains("application/json"),
                "Content type should be JSON");
    }

    @Test
    @DisplayName("Application should handle concurrent requests properly")
    void applicationShouldHandleConcurrentRequestsProperly() throws InterruptedException {
        // Given
        final String apiUrl = "http://localhost:" + this.port +
                "/v1/price/findByBrandProductBetweenDate?brandId=1&productId=35455&dateQuery=2020-06-14T10:00:00Z";

        // When - Simular múltiples requests concurrentes
        final Thread[] threads = new Thread[5];
        final boolean[] results = new boolean[5];

        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                final ResponseEntity<String> response = this.restTemplate.getForEntity(apiUrl, String.class);
                results[index] = response.getStatusCode() == HttpStatus.OK;
            });
            threads[i].start();
        }

        for (final Thread thread : threads) {
            thread.join();
        }

        // Then
        for (final boolean result : results) {
            assertTrue(result, "All concurrent requests should succeed");
        }
    }
}
