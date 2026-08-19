package com.example.commercepaymentsystem.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 장바구니 담기를 하거나 장바구니에서 특정 상품의 수량을 변경할 때 사용
public record CartItemRequest(
       @Min(value = 1, message = "장바구니에 최소 1개 이상 상품을 담으세요")
       @NotNull(message = "상품수량은 필수 입니다.")
       int quantity) {
}
