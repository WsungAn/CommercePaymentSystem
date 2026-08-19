package com.example.commercepaymentsystem.domain.order.entity;

import com.example.commercepaymentsystem.domain.product.entity.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "orderitems")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Order order;
    @ManyToOne
    private Product product;

    public void setOrder(Order order) {

    }

    public String getProductName() {
            return null;
    }
}
