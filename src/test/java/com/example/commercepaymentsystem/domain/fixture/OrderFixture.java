package com.example.commercepaymentsystem.domain.fixture;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class OrderFixture {

    private OrderFixture() {}

    public static int TOTAL_PRICE = 10000;
    public static String ORDER_NUMBER = "order-001";


    public static Order createOrderWithId(Member member, List<OrderItem> orderItems, Long orderId) {
        Order order = new Order(member, ORDER_NUMBER, TOTAL_PRICE, orderItems);
        ReflectionTestUtils.setField(order, "id", orderId);
        return order;
    }

}