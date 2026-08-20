package com.example.commercepaymentsystem.domain.payment.dto;

import com.example.commercepaymentsystem.domain.order.entity.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentStatus;

public record PaymentResponse(
        PaymentStatus paymentStatus,
        OrderStatus orderStatus
) {
}