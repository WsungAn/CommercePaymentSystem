package com.example.commercepaymentsystem.domain.product.service;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.common.response.PageResponse;
import com.example.commercepaymentsystem.domain.product.dto.ProductResponse;
import com.example.commercepaymentsystem.domain.product.dto.ProductSearchCondition;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import com.example.commercepaymentsystem.domain.product.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    public PageResponse<ProductResponse> findAll(ProductSearchCondition condition, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.search(condition);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return PageResponse.of(productPage, ProductResponse::from);
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
