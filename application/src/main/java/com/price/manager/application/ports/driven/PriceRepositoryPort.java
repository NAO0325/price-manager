package com.price.manager.application.ports.driven;

import com.price.manager.domain.Price;
import com.price.manager.domain.criteria.PriceSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface PriceRepositoryPort {

    List<Price> findByCriteria(PriceSearchCriteria priceSearchCriteria);

    Optional<Price> findBestPrice(PriceSearchCriteria criteria);

}
