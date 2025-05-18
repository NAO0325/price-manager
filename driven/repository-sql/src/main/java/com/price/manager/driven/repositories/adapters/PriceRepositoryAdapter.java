package com.price.manager.driven.repositories.adapters;

import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.domain.Price;
import com.price.manager.driven.repositories.PriceJpaRepository;
import com.price.manager.driven.repositories.mappers.PriceEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@RequiredArgsConstructor
@Service
public class PriceRepositoryAdapter implements PriceRepositoryPort {

    private final PriceJpaRepository repository;

    private final PriceEntityMapper mapper;

    @Override
    public List<Price> findAllByBrandIdAndProductIdBetweenDates(Long brandId, Long productId, LocalDateTime dateBetween) {
        return repository.findAllByBrandIdAndProductIdBetweenDates(brandId, productId, dateBetween)
                .stream()
                .flatMap(List::stream)
                .map(mapper::toDomain)
                .toList();
    }
}
