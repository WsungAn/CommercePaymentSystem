package com.example.commercepaymentsystem.cart.entity;

import com.example.commercepaymentsystem.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
// cartItem은 cart_id와 product_id 2개를 기준으로 유일키를 식별한다.
// 한 사용자와 여러 상품을 답을 수 있기 때문에 cartId만으로는 유일키를 정하지 못하기 때문
@Table(name = "cartitems", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// BaseEntity 상속 필요
public class CartItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private int quantity;


    public CartItem(Cart cart, Product product, int productQuantity) {

        validateQuantity(quantity);

        this.cart = cart;
        this.product = product;
        quantity = productQuantity;
    }

    // 지정한 수량만큼 상품의 수량을 더한다. (메인 페이지, 상품 상세보기에서 상품 담기)
    public void addQuantity(int addQuantity) {

        validateQuantity(addQuantity);

        quantity += addQuantity;
    }

    // 지정한 수량만큼 상품의 수량을 변경한다. ( 카트 페이지에서 +, - 혹은 숫자 입력 )
    public void changeQuantity(int newQuantity) {

        validateQuantity(newQuantity);

        quantity = newQuantity;

    }

    // 장바구니에 담을 수량 혹은 변경 할 수량이 1이상인지 검증한다.
    private void validateQuantity(int checkQuantity) {

        if (checkQuantity <= 0) {
            // INVALID_CART_QUANTITY 변경 필요
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
    }

}
