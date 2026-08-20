package com.example.commercepaymentsystem.domain.order.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "unit_price", nullable = false, columnDefinition = "INT UNSIGNED")
    private int unitPrice;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private int quantity;

    public OrderItem(Product product, String productName, int unitPrice, int quantity) {
        this.product = product;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getLineTotal() {
        return Math.multiplyExact(unitPrice, quantity);
    }
}
