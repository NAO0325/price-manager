package com.price.manager.driving.controllers.adapters;

import com.price.manager.application.ports.driving.PriceServicePort;
import com.price.manager.driving.controllers.api.PriceControllerApi;
import com.price.manager.driving.controllers.error.PriceNotFoundException;
import com.price.manager.driving.controllers.models.PriceResponse;
import com.price.manager.driving.controllers.mappers.PriceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@RestController
@RequiredArgsConstructor
public class PriceControllerAdapter implements PriceControllerApi {

    private final PriceServicePort priceServicePort;

    private final PriceMapper mapper;

    @Override
    public ResponseEntity<PriceResponse> findByBrandProductBetweenDate(
            Long brandId,
            Long productId,
            OffsetDateTime dateQuery) {

        PriceResponse response = mapper.toResponseDto(
                priceServicePort.findByBrandProductBetweenDate(brandId, productId, dateQuery.toLocalDateTime())
        );

        if (response == null) {
            throw new PriceNotFoundException("No price found for the given parameters");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
