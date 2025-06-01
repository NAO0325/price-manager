package com.price.manager.application.ports.driven;

import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;

import java.util.Optional;

public interface PriceRepositoryPort {

    Optional<Price> findBestPrice(PriceSearchCriteria criteria);
}
