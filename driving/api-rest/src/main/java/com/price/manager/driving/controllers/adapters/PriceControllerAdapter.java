package com.price.manager.driving.controllers.adapters;

import com.price.manager.application.ports.driving.PriceServicePort;
import com.price.manager.driving.controllers.api.PriceControllerApi;
import com.price.manager.driving.controllers.error.PriceNotFoundException;
import com.price.manager.driving.controllers.mappers.PriceMapper;
import com.price.manager.driving.controllers.models.PriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PriceControllerAdapter implements PriceControllerApi {

    private final PriceServicePort priceServicePort;

    private final PriceMapper mapper;

    @Override
    public ResponseEntity<PriceResponse> findByBrandProductBetweenDate(Long brandId, Long productId,
                                                                       OffsetDateTime dateQuery) {

        final var price = this.priceServicePort.findByBrandProductBetweenDate(
                brandId,
                productId,
                dateQuery.toLocalDateTime()
        );

        final var response = this.mapper.toResponseDto(price);

        if (response == null) {
            throw new PriceNotFoundException("No price found for the given parameters");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
