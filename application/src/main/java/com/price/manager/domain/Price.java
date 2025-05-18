package com.price.manager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Price {

    private Long brandId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Long priceList;

    private Long productId;

    private Integer priority;

    private Double price;

    private String curr;
}
