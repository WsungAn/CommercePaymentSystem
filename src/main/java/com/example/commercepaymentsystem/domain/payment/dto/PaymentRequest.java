package com.example.commercepaymentsystem.domain.payment.dto;

import com.example.commercepaymentsystem.domain.payment.entity.PaymentResult;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentStatus;

public record PaymentRequest (
        Long orderId,
        PaymentResult result,
        int amount
) {
}
