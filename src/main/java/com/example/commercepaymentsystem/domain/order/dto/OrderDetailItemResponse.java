package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.entity.OrderItem;

/**
 * 주문 상세 화면에 표시할 주문상품 한 건의 정보
 */
public record OrderDetailItemResponse(
        Long orderItemId,
        // 원본 상품 PK
        Long productId,
        // 주문 당시 상품명 스냅샷
        String productName,
        // 주문 당시 상품 단가 스냅샷
        int unitPrice,
        int quantity,
        // 상품별 총금액: 단가 × 수량
        int lineTotal
) {

//      OrderItem Entity를 응답 DTO로 변환한다.

    public static OrderDetailItemResponse from(OrderItem orderItem) {
        return new OrderDetailItemResponse(

                orderItem.getId(),
                orderItem.getProduct().getId(),
                // 현재 상품명이 아니라 주문 당시 저장한 상품명
                orderItem.getProductName(),
                // 현재 상품 가격이 아니라 주문 당시 저장한 가격
                orderItem.getUnitPrice(),
                orderItem.getQuantity(),
                // 주문 당시 단가 * 주문 수량
                orderItem.getLineTotal()
        );
    }
}