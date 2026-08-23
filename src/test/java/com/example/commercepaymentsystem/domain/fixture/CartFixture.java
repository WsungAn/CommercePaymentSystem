package com.example.commercepaymentsystem.domain.fixture;

import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class CartFixture {

    private CartFixture() {}

    public static Cart createCartWithId(Member member, Long id) {
        Cart cart = new Cart(member);
        ReflectionTestUtils.setField(cart, "id", id);
        return cart;
    }

    public static Cart createCartWithoutId(Member member) {
        return  new Cart(member);
    }

}
