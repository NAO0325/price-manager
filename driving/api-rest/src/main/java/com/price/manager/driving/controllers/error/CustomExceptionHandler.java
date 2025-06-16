package com.price.manager.driving.controllers.error;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.price.manager.driving.controllers.models.Error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PriceNotFoundException.class)
    protected ResponseEntity<Error> handleNotFound(PriceNotFoundException ex, WebRequest request) {
        final Error error = new Error();
        error.setCode("PRICE_NOT_FOUND");
        error.setMessage(ex.getMessage());
        error.setTimestamp(this.nowToUtcOffsetDateTime());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NumberFormatException.class)
    protected ResponseEntity<Error> handleNumberFormat(NumberFormatException ex, WebRequest request) {
        final Error error = new Error();
        error.setCode("INVALID_FORMAT");
        error.setMessage("Invalid format: " + ex.getMessage());
        error.setTimestamp(this.nowToUtcOffsetDateTime());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Error> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        final Error error = new Error();
        error.setCode("INVALID_PARAMETER");
        error.setMessage("Parameter '" + ex.getName() + "' must be valid: " + ex.getMessage());
        error.setTimestamp(this.nowToUtcOffsetDateTime());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleAllExceptions(Exception ex, WebRequest request) {
        final Error error = new Error();
        error.setCode("INTERNAL_SERVER_ERROR");
        error.setMessage("An unexpected error occurred: " + ex.getMessage());
        error.setTimestamp(this.nowToUtcOffsetDateTime());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private OffsetDateTime nowToUtcOffsetDateTime() {
        return OffsetDateTime.now().withNano(0).withOffsetSameInstant(ZoneOffset.UTC);
    }
}
