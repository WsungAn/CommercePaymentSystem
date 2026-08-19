package com.example.commercepaymentsystem.domain.payment.dto;

import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentDetailResponse(
        int amount,
        PaymentStatus status,
        LocalDateTime paidAt
) {
    public static PaymentDetailResponse from(Payment payment) {
        return new PaymentDetailResponse(
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaidAt()
        );
    }
}
