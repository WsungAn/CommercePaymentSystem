package com.example.commercepaymentsystem.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// TODO: BASE 엔티티 추가, Order 만들어지면 import
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.IN_PROGRESS;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public Payment(Order order, int amout) {
        this.order = order;
        this.amount = amout;
    }
}
