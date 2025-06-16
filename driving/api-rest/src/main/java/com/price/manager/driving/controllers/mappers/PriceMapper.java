package com.price.manager.driving.controllers.mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.price.manager.domain.Price;
import com.price.manager.driving.controllers.models.PriceResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface PriceMapper {

    @Mapping(target = "id", source = "priceList")
    @Mapping(target = "startDate", expression = "java(toUtcOffsetDateTime(price.getStartDate()))")
    @Mapping(target = "endDate", expression = "java(toUtcOffsetDateTime(price.getEndDate()))")
    PriceResponse toResponseDto(Price price);

    default OffsetDateTime toUtcOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atOffset(ZoneOffset.UTC);
    }
}
