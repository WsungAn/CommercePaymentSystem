package com.example.commercepaymentsystem.domain.product.repository;

import com.example.commercepaymentsystem.domain.product.dto.ProductSearchCondition;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> search(ProductSearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(condition.category())) {
                predicates.add(cb.equal(root.get("category"), condition.category()));
            }

            if (condition.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), condition.minPrice()));
            }

            if (condition.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), condition.maxPrice()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}