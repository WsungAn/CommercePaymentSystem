package com.example.commercepaymentsystem.domain.product.service;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.domain.product.dto.ProductResponse;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(Long id) {
        Product product = findProduct(id);
        return ProductResponse.from(product);
    }

    public Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    public boolean validateStock(int quantity, Product product) {
        return product.hasEnoughStock(quantity);
    }

}
