package com.example.commercepaymentsystem.domain.cart.service;

import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.repository.CartRepository;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;

    // 장바구니에 담을 때 유저의 장바구니를 꺼내옴 없으면 장바구니 생성
    public Cart getOrCreateCart(Member member) {

        Optional<Cart> cart = cartRepository.findById(member.getId());

        if (cart.isEmpty()) {
            Cart newCart = new Cart(member);
            return cartRepository.save(newCart);
        }

        return cart.get();
    }

    // 유저가 가지고 있는 장바구니를 꺼내온다.
    @Transactional(readOnly = true)
    public Optional<Cart> getCart(Long memberId) {
        return cartRepository.findById(memberId);
    }


    // 현재

}
