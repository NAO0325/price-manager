package com.price.manager.driven.repositories;

import com.price.manager.driven.repositories.models.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    @Query("""
            SELECT p
            FROM PriceEntity p
            WHERE p.brandId = :brandId
              AND p.productId = :productId
              AND :dateBetween BETWEEN p.startDate AND p.endDate
            ORDER BY p.priority DESC, p.priceList DESC
            """)
    List<PriceEntity> findAllByBrandIdAndProductIdBetweenDates(
            @Param("brandId") Long brandId,
            @Param("productId") Long productId,
            @Param("dateBetween") LocalDateTime dateBetween
    );

    @Query("""
            SELECT p
            FROM PriceEntity p
            WHERE p.brandId = :brandId
              AND p.productId = :productId
              AND :dateBetween BETWEEN p.startDate AND p.endDate
            ORDER BY p.priority DESC, p.priceList DESC
            LIMIT 1
            """)
    Optional<PriceEntity> findBestPriceByBrandIdAndProductIdAtDate(
            @Param("brandId") Long brandId,
            @Param("productId") Long productId,
            @Param("dateBetween") LocalDateTime dateBetween
    );
}
