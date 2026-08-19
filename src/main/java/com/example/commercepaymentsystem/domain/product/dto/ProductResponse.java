package com.example.commercepaymentsystem.domain.product.dto;

import com.example.commercepaymentsystem.domain.product.entity.Product;

public record ProductResponse(
        Long id,
        String name,
        int price,
        int stock,
        String description,
        String category
) {
    public static  ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getDescription(),
                product.getCategory()
        );
    }
}