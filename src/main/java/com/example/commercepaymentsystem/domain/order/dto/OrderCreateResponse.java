package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.entity.Order;

    public record OrderCreateResponse(Long orderId, String orderNumber, int totalPrice) {
        public static com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse from(Order order) {
            return new com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse(order.getId(), order.getOrderNumber(), order.getTotalPrice());
        }
    }

