package com.example.commercepaymentsystem.domain.fixture;

import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import org.springframework.test.util.ReflectionTestUtils;

public class CartItemFixture {

    private CartItemFixture() {
    }

    public static CartItem createCartItemWithId(
            Cart cart, Product product, int quantity, Long id) {
        CartItem cartItem = new CartItem(cart, product, quantity);
        ReflectionTestUtils.setField(cartItem, "id", id);
        return cartItem;
    }


}
