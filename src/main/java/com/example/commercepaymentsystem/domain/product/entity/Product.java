package com.example.commercepaymentsystem.domain.product.entity;

import com.example.commercepaymentsystem.common.entity.BaseEntity;
import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "int UNSIGNED")
    private int price;

    @Column(nullable = false, columnDefinition = "int UNSIGNED DEFAULT 0")
    private int stock = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    public Product(String name, int price, int stock, String description, String category) {
        if (price < 0) {
            throw new BusinessException(ErrorCode.INVALID_PRICE);
        }
        if (stock < 0) {
            throw new BusinessException(ErrorCode.INVALID_STOCK);
        }
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.category = category;
    }

    public void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }
        if (quantity > this.stock) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }

}