package com.example.commercepaymentsystem.domain.cart.fixture;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;

public class PaymentFixture {

    private PaymentFixture() {}

    public static int AMOUNT = 10000;

    public static Payment createPayment(Order order, Member member) {
        return new Payment(order, AMOUNT, member);
    }
}
