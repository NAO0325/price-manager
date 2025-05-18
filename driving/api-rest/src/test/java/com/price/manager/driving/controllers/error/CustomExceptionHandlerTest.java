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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomExceptionHandlerTest {

    private CustomExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodParameter methodParameter;

    @BeforeEach
    void setUp() {
        exceptionHandler = new CustomExceptionHandler();
    }

    @Test
    void handleNotFound_ShouldReturnNotFoundErrorResponse() {
        // Given
        String errorMessage = "Price not found for the given criteria";
        PriceNotFoundException exception = new PriceNotFoundException(errorMessage);

        // When
        ResponseEntity<Error> response = exceptionHandler.handleNotFound(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PRICE_NOT_FOUND", response.getBody().getCode());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(ZoneOffset.UTC, response.getBody().getTimestamp().getOffset());
    }

    @Test
    void handleNumberFormat_ShouldReturnBadRequestErrorResponse() {
        // Given
        String errorMessage = "For input string: \"abc\"";
        NumberFormatException exception = new NumberFormatException(errorMessage);

        // When
        ResponseEntity<Error> response = exceptionHandler.handleNumberFormat(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_FORMAT", response.getBody().getCode());
        assertEquals("Invalid format: " + errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(ZoneOffset.UTC, response.getBody().getTimestamp().getOffset());
    }

    @Test
    void handleTypeMismatch_ShouldReturnBadRequestErrorResponse() {
        // Given
        String parameterName = "productId";
        String errorMessage = "Invalid product ID format";

        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc",
                Long.class,
                parameterName,
                methodParameter,
                new NumberFormatException(errorMessage)
        );

        // When
        ResponseEntity<Error> response = exceptionHandler.handleTypeMismatch(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_PARAMETER", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().startsWith("Parameter '" + parameterName + "' must be valid:"));
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(ZoneOffset.UTC, response.getBody().getTimestamp().getOffset());
    }

    @Test
    void handleAllExceptions_ShouldReturnInternalServerErrorResponse() {
        // Given
        String errorMessage = "Database connection failed";
        Exception exception = new RuntimeException(errorMessage);

        // When
        ResponseEntity<Error> response = exceptionHandler.handleAllExceptions(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("An unexpected error occurred: " + errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(ZoneOffset.UTC, response.getBody().getTimestamp().getOffset());
    }

    @Test
    void handleAllExceptions_ShouldReturnInternalServerErrorResponseForNullMessage() {
        // Given
        Exception exception = new RuntimeException();

        // When
        ResponseEntity<Error> response = exceptionHandler.handleAllExceptions(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("An unexpected error occurred: null", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void timestamp_ShouldBeInUtcFormat() {
        // Given
        Exception exception = new RuntimeException("Test error");

        // When
        ResponseEntity<Error> response = exceptionHandler.handleAllExceptions(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(ZoneOffset.UTC, response.getBody().getTimestamp().getOffset());
        assertEquals(0, response.getBody().getTimestamp().getNano());
    }
}