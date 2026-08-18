package com.example.commercepaymentsystem.product.repository;

import com.example.commercepaymentsystem.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
