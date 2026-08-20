package com.example.commercepaymentsystem.domain.order.dto;

import java.util.List;

public record OrderCreateRequest(List<Long> cartItemIds) {
    public OrderCreateRequest {
        cartItemIds = cartItemIds == null ? List.of() : List.copyOf(cartItemIds);
    }
}
