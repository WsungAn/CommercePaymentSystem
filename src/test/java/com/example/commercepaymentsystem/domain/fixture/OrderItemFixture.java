package com.example.commercepaymentsystem.domain.fixture;

import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.product.entity.Product;

public class OrderItemFixture {

    private OrderItemFixture() {}

    public static String PRODUCT_NAME = "Product Name";
    public static int UNIT_PRICE = 10000;
    public static int QUANTITY = 2;

    public static OrderItem createOrderItem(Product product) {
        return new OrderItem(
                product,
                PRODUCT_NAME,
                UNIT_PRICE,
                QUANTITY
        );
    }
}
