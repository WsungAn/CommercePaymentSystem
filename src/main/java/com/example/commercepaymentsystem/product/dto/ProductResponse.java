package com.example.commercepaymentsystem.product.dto;

import com.example.commercepaymentsystem.product.entity.Product;

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