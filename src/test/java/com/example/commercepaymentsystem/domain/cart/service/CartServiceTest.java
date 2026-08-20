package com.example.commercepaymentsystem.domain.cart.service;

import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.fixture.CartFixture;
import com.example.commercepaymentsystem.domain.cart.fixture.MemberFixture;
import com.example.commercepaymentsystem.domain.cart.repository.CartRepository;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Test
    @DisplayName("유저의 장바구니가 없을 경우 새로생성을 한다.")
    void createCart_createCart() {

        // given
        Member member = MemberFixture.createMemberWithId(1L);
        Cart cart = CartFixture.createCartWithId(member, 1L);
        // DB에서 일부러 Optional을 던지게 함
        given(cartRepository.findById(member.getId())).willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willReturn(cart);

        // when
        Cart getCart = cartService.getOrCreateCart(member);

        //then
        assertThat(getCart).isEqualTo(cart);
    }

    @Test
    @DisplayName("유저의 장바구니가 있다면 존재하는 Cart를 반환한다.")
    void createCart_returnCart() {

        // Given
        Member member = MemberFixture.createMemberWithId(1L);
        Cart cart = CartFixture.createCartWithId(member, 1L);
        // DB에서 Optional이 아닌걸 줌
        given(cartRepository.findById(member.getId())).willReturn(Optional.of(cart));

        // When
        Cart  getCart = cartService.getOrCreateCart(member);

        //then
        assertThat(getCart).isEqualTo(cart);
    }


}