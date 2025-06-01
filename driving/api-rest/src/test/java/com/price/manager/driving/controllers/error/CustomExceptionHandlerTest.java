package com.price.manager.driving.controllers.error;

import com.price.manager.driving.controllers.models.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CustomExceptionHandlerTest {

    private CustomExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodParameter methodParameter;

    @BeforeEach
    void setUp() {
        this.exceptionHandler = new CustomExceptionHandler();
    }

    @Test
    void handleNotFoundShouldReturnNotFoundErrorResponse() {
        // Given
        final String errorMessage = "Price not found for the given criteria";
        final PriceNotFoundException exception = new PriceNotFoundException(errorMessage);

        // When
        final ResponseEntity<Error> response = this.exceptionHandler.handleNotFound(exception, this.webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PRICE_NOT_FOUND", response.getBody().getCode());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(ZoneOffset.UTC, response.getBody().getTimestamp().getOffset());
    }

    @Test
    void handleNumberFormatShouldReturnBadRequestErrorResponse() {
        // Given
        final String errorMessage = "For input string: \"abc\"";
        final NumberFormatException exception = new NumberFormatException(errorMessage);

        // When
        final ResponseEntity<Error> response = this.exceptionHandler.handleNumberFormat(exception, this.webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_FORMAT", response.getBody().getCode());
        assertEquals("Invalid format: " + errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(ZoneOffset.UTC, response.getBody().getTimestamp().getOffset());
    }

    @Test
    void handleTypeMismatchShouldReturnBadRequestErrorResponse() {
        // Given
        final String parameterName = "productId";
        final String errorMessage = "Invalid product ID format";

        final MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc",
                Long.class,
                parameterName,
                this.methodParameter,
                new NumberFormatException(errorMessage)
        );

        // When
        final ResponseEntity<Error> response = this.exceptionHandler.handleTypeMismatch(exception, this.webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_PARAMETER", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().startsWith("Parameter '" + parameterName + "' must be valid:"));
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(ZoneOffset.UTC, response.getBody().getTimestamp().getOffset());
    }

    @Test
    void handleAllExceptionsShouldReturnInternalServerErrorResponse() {
        // Given
        final String errorMessage = "Database connection failed";
        final Exception exception = new RuntimeException(errorMessage);

        // When
        final ResponseEntity<Error> response = this.exceptionHandler.handleAllExceptions(exception, this.webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("An unexpected error occurred: " + errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(ZoneOffset.UTC, response.getBody().getTimestamp().getOffset());
    }

    @Test
    void handleAllExceptionsShouldReturnInternalServerErrorResponseForNullMessage() {
        // Given
        final Exception exception = new RuntimeException();

        // When
        final ResponseEntity<Error> response = this.exceptionHandler.handleAllExceptions(exception, this.webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("An unexpected error occurred: null", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void timestampShouldBeInUtcFormat() {
        // Given
        final Exception exception = new RuntimeException("Test error");

        // When
        final ResponseEntity<Error> response = this.exceptionHandler.handleAllExceptions(exception, this.webRequest);

        // Then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(ZoneOffset.UTC, response.getBody().getTimestamp().getOffset());
        assertEquals(0, response.getBody().getTimestamp().getNano());
    }
}
