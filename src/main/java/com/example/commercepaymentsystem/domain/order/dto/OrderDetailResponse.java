package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

// 주문 상세 조회의 최종 응답 DTO

public record OrderDetailResponse(
        Long orderId,
        // 사용자에게 보여줄 주문번호
        String orderNumber,
        int totalPrice,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        LocalDateTime orderedAt,
        LocalDateTime cancelledAt,
        // 주문 취소 사유
        String cancellationReason,
        // 주문상품 스냅샷 목록
        List<OrderDetailItemResponse> items
) {

//  Order와 Payment를 하나의 상세 응답 DTO로 합친다.

    public static OrderDetailResponse of(
            Order order,
            Payment payment
    ) {
        // OrderItem Entity 목록을 상세 응답 목록으로 변환한다.
        List<OrderDetailItemResponse> items =
                order.getOrderItems().stream()
                        .map(OrderDetailItemResponse::from)
                        .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getStatus(),
                payment.getStatus(),
                order.getCreatedAt(),
                order.getCancelledAt(),
                order.getCancellationReason(),
                items
        );
    }
}