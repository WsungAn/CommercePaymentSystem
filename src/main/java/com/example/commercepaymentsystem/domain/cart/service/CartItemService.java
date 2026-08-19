package com.example.commercepaymentsystem.cart.service;

import com.example.commercepaymentsystem.cart.entity.Cart;
import com.example.commercepaymentsystem.cart.entity.CartItem;
import com.example.commercepaymentsystem.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    // Product 구현 시 import 필요
    // 장바구니에 담긴 상품이 몇개 담겨있는지 조회
    @Transactional(readOnly = true)
    public int getExistingQuantity(Cart cart, Product product) {

        return cartItemRepository.countQuantityByCartAndProduct(cart, product );
    }

    // cart_id + product_id 조합으로 장바구니 수량 증감
    public void addCartItem(Cart cart, Product product, int quantity) {

        // Cart와 Product조합의 CartItem 이 있는지 확인
        Optional<CartItem> foundCartItem = cartItemRepository.findByCartAndProduct(cart, product);

        // Cart와 Product조합이 없을 경우 생성
        if (foundCartItem.isEmpty()) {
            CartItem cartItem = new CartItem(cart, product, quantity);
            cartItemRepository.save(cartItem);
            return;
        }

        // Cart와 Product조합이
        foundCartItem.get().addQuantity(quantity);
        cartItemRepository.save(foundCartItem.get());

    }
}
