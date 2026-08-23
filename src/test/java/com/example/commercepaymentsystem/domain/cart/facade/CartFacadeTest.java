package com.example.commercepaymentsystem.domain.cart.facade;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.service.CartItemService;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.domain.fixture.CartFixture;
import com.example.commercepaymentsystem.domain.fixture.MemberFixture;
import com.example.commercepaymentsystem.domain.fixture.ProductFixture;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.service.MemberService;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// validateStock 은 재고가 "충분하면" true 를 준다.
// 이 부호를 뒤집어 쓰면 재고 안쪽 수량이 전부 막히고 초과 수량만 통과하는데,
// 담기 API 를 실제로 호출해 보기 전까지 아무도 눈치채지 못했던 자리다.
@ExtendWith(MockitoExtension.class)
class CartFacadeTest {

    @Mock
    private MemberService memberService;

    @Mock
    private ProductService productService;

    @Mock
    private CartService cartService;

    @Mock
    private CartItemService cartItemService;

    private CartFacade cartFacade;

    private Member member;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        cartFacade = new CartFacade(memberService, productService, cartService, cartItemService);
        member = MemberFixture.createMemberWithId(1L);
        cart = CartFixture.createCartWithId(member, 1L);
        product = ProductFixture.createProduct(); // 재고 5개
    }

    @Test
    @DisplayName("재고 안쪽 수량은 장바구니에 담긴다")
    void 재고_안쪽_수량은_담긴다() {
        given(memberService.findMember(1L)).willReturn(member);
        given(productService.findProduct(1L)).willReturn(product);
        given(cartService.getOrCreateCart(member)).willReturn(cart);
        given(cartItemService.getExistingQuantity(cart, product)).willReturn(0);
        given(productService.validateStock(3, product)).willReturn(true);

        assertThatCode(() -> cartFacade.addProductToCart(1L, 1L, 3))
                .doesNotThrowAnyException();

        verify(cartItemService, times(1)).addCartItem(cart, product, 3);
    }

    @Test
    @DisplayName("이미 담긴 수량까지 더해 재고를 넘으면 INSUFFICIENT_STOCK 이다")
    void 재고를_넘으면_예외() {
        given(memberService.findMember(1L)).willReturn(member);
        given(productService.findProduct(1L)).willReturn(product);
        given(cartService.getOrCreateCart(member)).willReturn(cart);
        // 이미 4개 담겨 있는데 3개를 더 담으려 한다 - 합계 7개는 재고 5개를 넘는다
        given(cartItemService.getExistingQuantity(cart, product)).willReturn(4);
        given(productService.validateStock(7, product)).willReturn(false);

        assertThatThrownBy(() -> cartFacade.addProductToCart(1L, 1L, 3))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("재고가 부족합니다");

        verify(cartItemService, never()).addCartItem(cart, product, 3);
    }
}
