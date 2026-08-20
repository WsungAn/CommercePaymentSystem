package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentStatus;

public record OrderCancelResponse(
        Long orderId,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus
) {
    public static OrderCancelResponse of(Order order, Payment payment) {
        return new OrderCancelResponse(order.getId(), order.getStatus(), payment.getStatus());
    }
}
