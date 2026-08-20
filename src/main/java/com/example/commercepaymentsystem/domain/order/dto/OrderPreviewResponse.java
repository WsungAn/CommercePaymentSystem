package com.example.commercepaymentsystem.domain.order.dto;

import java.util.List;

public record OrderPreviewResponse(
        List<OrderPreviewItemResponse> items,
        int totalPrice
) {
    public record OrderPreviewItemResponse(
            Long productId,
            String productName,
            int price,
            int quantity,
            int subtotal // price * quantity
    ) {}
}
