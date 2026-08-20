package com.example.commercepaymentsystem.domain.cart.fixture;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;

import java.util.List;

public class OrderFixture {

    private OrderFixture() {}

    public static int TOTAL_PRICE = 10000;
    public static String ORDER_NUMBER = "order-001";

    public static Order createOrder(Member member) {
        return new Order(
                member,
                ORDER_NUMBER,
                TOTAL_PRICE,
                List.of()
        );
    }

}