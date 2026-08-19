package com.example.commercepaymentsystem.cart.dto;

import java.util.List;

public record CartResponse(
        // 장바구니에 담긴 상품 리스트
        List<CartItemResponse> cartItems,
        // 장바구니에 담긴 상품 가격*수량의 총합
        int totalPrice
) { }
