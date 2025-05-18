package com.price.manager.driving.controllers.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(String message) {
        super(message);
    }
}
