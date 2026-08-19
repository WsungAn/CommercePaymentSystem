package com.example.commercepaymentsystem.domain.payment.entity;

import com.example.commercepaymentsystem.common.entity.BaseEntity;
import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.IN_PROGRESS;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public Payment(Order order, int amount) {
        this.order = order;
        this.amount = amount;
    }

    public void changeStatus(PaymentStatus status) {
        if (!this.status.canTransitTo(status)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
        this.status = status;
    }

}
