package com.example.commercepaymentsystem.domain.cart.dto;

import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.product.entity.Product;

public record CartItemResponse(
        // cartItem pk (프론트에서 수량변경 및 삭제를 위해 필요)
        Long id,
        // 상품 pk
        Long productId,
        // 상품 이름
        String productName,
        // 상품 가격
        int price,
        // 장바구니에 있는 상품 수량
        int quantity
) {
    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity()
        );
    }
}
