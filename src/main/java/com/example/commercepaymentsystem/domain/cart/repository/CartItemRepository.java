package com.example.commercepaymentsystem.domain.cart.repository;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    // Cart와 Product조합의 CartItem 이 있는지 확인
    @Query("""
    SELECT c
    FROM CartItem c
    WHERE c.cart = :cart AND c.product = :product
""")
    Optional<CartItem>  findByCartAndProduct(Cart cart, Product product);

    // [Cart, Order ] 장바구니에 담긴 상품들을 조회
    @Query("""
    SELECT c
    FROM CartItem c
    LEFT JOIN FETCH c.product p
    WHERE c.cart = :cart
""")
    List<CartItem> findByCart(@Param("cart") Cart cart);

    // 수량변경을 요청한 사용자의 CartId와 변경 대상 cartItemId가 모두 일치하는지 확인
    // 일치하지 않는다면 그건 본인 소유의 cartId가 아님
    // 상품 수량변경 시 재고 확인이 필요하여 left join 진행
    @Query("""
    SELECT c
    FROM CartItem c
    LEFT JOIN FETCH c.product p
    WHERE c.id = :id AND c.cart = :cart
""")
    Optional<CartItem> findByIdAndCartId(
           @Param("id") Long id, @Param("cart") Cart cart);

    // 장바구니에 있는 모든 상품들을 지운다.
    @Modifying
    @Query("""
    DELETE
    FROM CartItem c
    WHERE c.cart = :cart
""")
    void deleteAllByCart(@Param("cart") Cart cart);

    // [ Order ] 장바구니에서 주문을 할 때 몇몇 상품들만 선택하여 주문을 하면 cartItemId List를 전달 받는다.
    // Cart id와 cartItemId List를 기준으로 CartItem을 조회함
    @Query("""
    SELECT c
    FROM CartItem c
    LEFT JOIN FETCH c.product p
    WHERE c.id IN :cartItemIds AND c.cart = :cart
""")
    List<CartItem> findSelectedForOrder(
            @Param("cart") Cart cart,
            @Param("cartItemIds")  List<Long> cartItemIds);

}
