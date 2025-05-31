package com.price.manager.driven.repositories.adapters;

import com.price.manager.application.ports.driven.PriceRepositoryPort;
import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;
import com.price.manager.driven.repositories.PriceJpaRepository;
import com.price.manager.driven.repositories.mappers.PriceEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Service
public class PriceRepositoryAdapter implements PriceRepositoryPort {

    private final PriceJpaRepository repository;

    private final PriceEntityMapper mapper;

    @Override
    public List<Price> findByCriteria(PriceSearchCriteria priceSearchCriteria) {
        return repository.findAllByBrandIdAndProductIdBetweenDates(
                        priceSearchCriteria.brandId(),
                        priceSearchCriteria.productId(),
                        priceSearchCriteria.queryDate()
                ).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Price> findBestPrice(PriceSearchCriteria priceSearchCriteria) {
        return repository.findBestPriceByBrandIdAndProductIdAtDate(
                priceSearchCriteria.brandId(),
                priceSearchCriteria.productId(),
                priceSearchCriteria.queryDate()
        ).map(mapper::toDomain);
    }
}
