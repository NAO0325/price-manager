package com.price.manager.driven.repositories.mappers;

import com.price.manager.domain.Price;
import com.price.manager.driven.repositories.models.PriceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PriceEntityMapper {

    Price toDomain(PriceEntity entity);

}