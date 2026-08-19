package com.example.commercepaymentsystem.domain.cart.dto;

import jakarta.validation.constraints.Min;

public record AddCartItemRequest(
       @Min(value = 1, message = "장바구니에 최소 1개 이상 상품을 담으세요") int quantity) {
}
