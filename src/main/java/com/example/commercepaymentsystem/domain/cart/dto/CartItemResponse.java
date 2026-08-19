package com.example.commercepaymentsystem.cart.dto;

public record CartItemResponse(
        // 상품 pk
        Long productId,
        // 상품 이름
        String productName,
        // 상품 가격
        int price,
        // 상품 수량
        int quantity
) {
}
