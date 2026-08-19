package com.example.commercepaymentsystem.cart.repository;

import com.example.commercepaymentsystem.cart.entity.Cart;
import com.example.commercepaymentsystem.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 장바구니에 담긴 상품이 몇개 담겨있는지 조회
    @Query("""
    SELECT COUNT(c)
    FROM CartItem c
    WHERE c.cart = :cart AND c.product = :product
""")
    int countQuantityByCartAndProduct(
            @Param("cart") Cart cart, @Param("product") Product product);

    @Query("""
    SELECT c
    FROM CartItem c
    WHERE c.cart = :cart AND c.product = :product
""")
    Optional<CartItem>  findByCartAndProduct(Cart cart, Product product);
}
