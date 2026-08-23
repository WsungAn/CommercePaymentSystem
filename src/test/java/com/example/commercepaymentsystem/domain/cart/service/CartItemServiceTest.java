package com.example.commercepaymentsystem.domain.cart.service;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.domain.cart.dto.CartResponse;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.cart.repository.CartItemRepository;
import com.example.commercepaymentsystem.domain.fixture.CartFixture;
import com.example.commercepaymentsystem.domain.fixture.CartItemFixture;
import com.example.commercepaymentsystem.domain.fixture.MemberFixture;
import com.example.commercepaymentsystem.domain.fixture.ProductFixture;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

    @InjectMocks
    private CartItemService cartItemService;

    @Mock
    private CartItemRepository cartItemRepository;

    @Test
    @DisplayName("장바구니에 상품을 담을 때 장바구니에 없던 상품이라면 cartItem을 생성한다.")
    void addCartItem_crateCartItem() {

        // Given
        Member member = MemberFixture.createMemberWithId(1L);
        Cart cart = CartFixture.createCartWithId(member, 1L);
        Product product = ProductFixture.createProduct();
        // 장바구니에 없는 상품
        given(cartItemRepository.findByCartAndProduct(cart, product)).willReturn(Optional.empty());

        // when
        cartItemService.addCartItem(cart, product, 1);

        // then (save가 1번 일어났다면 정상)
        then(cartItemRepository).should().save(any(CartItem.class));

    }

    @Test
    @DisplayName("장바구니에 상품을 담을 때 장바구니에 있는 상품이라면 cartItem의 수량을 더한다.")
    void addCartItem_addCartItemQuantity() {

        // Given
        Member member = MemberFixture.createMemberWithId(1L);
        Cart cart = CartFixture.createCartWithId(member, 1L);
        Product product = ProductFixture.createProduct();
        // 장바구니에 상품이 10개 들어 있음
        CartItem cartItem = CartItemFixture.createCartItemWithId(cart, product, 10, 1L);
        given(cartItemRepository.findByCartAndProduct(cart, product)).willReturn(Optional.of(cartItem));

        // When 상품을 기존 10개에서 10개를 더 담음
        cartItemService.addCartItem(cart, product, 10);

        // then (총합 20개)
        assertThat(cartItem.getQuantity()).isEqualTo(20);
        // then (save가 1번 일어났다면 정상)
        then(cartItemRepository).should().save(any(CartItem.class));

    }

    @Test
    @DisplayName("장바구니 조회 요청 시 장바구니가 비어 있다면 비어있는 리스트 dto를 전달한다.")
    void getCartItems_getEmpty() {

        // Given
        Member member = MemberFixture.createMemberWithId(1L);
        Cart cart = CartFixture.createCartWithId(member, 1L);
        given(cartItemRepository.findByCart(cart)).willReturn(List.of());

        // when
        CartResponse cartResponse = cartItemService.getCartItems(cart);

        // Then
        assertThat(cartResponse).isNotNull();
        assertThat(cartResponse.cartItems().isEmpty()).isTrue();
        assertThat(cartResponse.totalPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("장바구니 조회 요청 시 장바구니가 존재하면 상품리스트와 총 합을 담은 dto를 전달한다.")
    void getCartItems_getCartItems() {

        // Given
        Member member = MemberFixture.createMemberWithId(1L);
        Cart cart = CartFixture.createCartWithId(member, 1L);

        Product product1 =  new Product("test", 10_000, 100, "test", "good");
        Product product2 = new Product("test", 20_000, 100, "test", "good");

        // 1만원 10개, 2만원 10개 장바구니 담기
        CartItem cartItem1 = new CartItem(cart, product1, 10);
        CartItem cartItem2 = new CartItem(cart, product2, 10);
        List<CartItem> cartItems = List.of(cartItem1, cartItem2);
        given(cartItemRepository.findByCart(cart)).willReturn(cartItems);

        // when
        CartResponse cartResponse = cartItemService.getCartItems(cart);

        // then
        assertThat(cartResponse).isNotNull();
        assertThat(cartResponse.cartItems().size()).isEqualTo(2);
        assertThat(cartResponse.totalPrice()).isEqualTo(300_000);
    }

    @Test
    @DisplayName("본인 소유의 카트가 아니라면 예외 발생")
    void checkCartItemAuthor_fail() {

        // given
        Member member = MemberFixture.createMemberWithId(1L);
        Cart cart = CartFixture.createCartWithId(member, 1L);
        given(cartItemRepository.findByIdAndCartId(member.getId(), cart)).willReturn(Optional.empty());

        // when&then
        assertThatThrownBy(
                () -> cartItemService.checkCartItemAuthor(1L, cart))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("권한이 없는 요청입니다.");
    }

    @Test
    @DisplayName("본인 소유의 카트가 아니라면 성공")
    void checkCartItemAuthor_success() {

        // given
        Member member = MemberFixture.createMemberWithId(1L);
        Cart cart = CartFixture.createCartWithId(member, 1L);
        Product product = ProductFixture.createProduct();
        CartItem cartItem = CartItemFixture.createCartItemWithId(cart, product, 10, 1L);
        given(cartItemRepository.findByIdAndCartId(member.getId(), cart)).willReturn(Optional.of(cartItem));

        // when
        CartItem getCartItem = cartItemService.checkCartItemAuthor(1L, cart);

        // then
        assertThat(getCartItem).isNotNull();
        assertThat(getCartItem).isEqualTo(cartItem);
        assertThat(getCartItem.getProduct()).isEqualTo(product);

    }

    @Test
    @DisplayName("장바구니에 담긴 상품의 합계(sum)의 결과가 null이면 0을 리턴한다.")
    void getExistingQuantity_returnZero() {
        // given
        Member member = MemberFixture.createMemberWithId(1L);
        Cart cart = CartFixture.createCartWithId(member, 1L);
        Product product = ProductFixture.createProduct();
        given(cartItemRepository.sumQuantityByCartAndProduct(cart, product)).willReturn(null);

        // when
        int count = cartItemService.getExistingQuantity(cart, product);

        // then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품의 합계(sum)의 결과가 null이 아니면 N을 그대로 리턴한다..")
    void getExistingQuantity_returnNum() {
        // given
        Member member = MemberFixture.createMemberWithId(1L);
        Cart cart = CartFixture.createCartWithId(member, 1L);
        Product product = ProductFixture.createProduct();
        given(cartItemRepository.sumQuantityByCartAndProduct(cart, product)).willReturn(10);

        // when
        int count = cartItemService.getExistingQuantity(cart, product);

        // then
        assertThat(count).isEqualTo(10);
    }


}