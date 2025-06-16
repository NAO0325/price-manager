package com.price.manager.application.ports.driven;

import java.util.Optional;

import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;

public interface PriceRepositoryPort {

    Optional<Price> findBestPrice(PriceSearchCriteria criteria);
}
