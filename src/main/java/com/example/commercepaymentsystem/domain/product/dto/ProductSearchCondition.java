package com.example.commercepaymentsystem.domain.product.dto;

public record ProductSearchCondition(
        String category,
        Integer minPrice,
        Integer maxPrice
) {
}