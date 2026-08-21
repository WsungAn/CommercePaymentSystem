package com.example.commercepaymentsystem.domain.order.facade;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.cart.service.CartItemService;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderPreviewResponse;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import com.example.commercepaymentsystem.domain.member.service.MemberService;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {
    @Mock
    private CartService cartService;

    @Mock
    private CartItemService cartItemService;

    @Mock
    private MemberService memberService;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private Cart cart;

    @Mock
    private CartItem cartItem1;

    @Mock
    private CartItem cartItem2;

    @Mock
    private Product product1;

    @Mock
    private Product product2;

    @Mock
    private Member member;

    @Mock
    private Order order;

    @Mock
    private OrderItem orderItem1;

    @Mock
    private OrderItem orderItem2;

    @Mock
    private Payment payment;

    @InjectMocks
    private OrderFacade orderFacade;

    private Long memberId;
    private Long orderId;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        orderId = 100L;
    }

    // =========================
    // getCheckout()
    // =========================

    @Test
    void 주문서_미리보기를_전체_장바구니로_조회한다() {

        // given
        when(cartService.getCart(memberId))
                .thenReturn(Optional.of(cart));

        when(cartItemService.getCartItem(cart))
                .thenReturn(List.of(cartItem1, cartItem2));

        givenCartItem(cartItem1, product1, 2, "상품1", 10_000);
        givenCartItem(cartItem2, product2, 3, "상품2", 5_000);

        // when
        OrderPreviewResponse response =
                orderFacade.getCheckout(memberId, List.of());

        // then
        assertThat(response.items())
                .hasSize(2);

        assertThat(response.totalPrice())
                .isEqualTo(35_000);

        verify(cartItemService)
                .getCartItem(cart);

        verify(cartItemService, never())
                .getCartItemSelected(any(), any());
    }


    @Test
    void 주문서_미리보기를_선택한_상품으로_조회한다() {

        // given
        List<Long> cartItemIds = List.of(1L, 2L);

        when(cartService.getCart(memberId))
                .thenReturn(Optional.of(cart));

        when(cartItemService.getCartItemSelected(cart, cartItemIds))
                .thenReturn(List.of(cartItem1, cartItem2));

        givenCartItem(cartItem1, product1, 2, "상품1", 10_000);
        givenCartItem(cartItem2, product2, 1, "상품2", 5_000);

        // when
        OrderPreviewResponse response =
                orderFacade.getCheckout(memberId, cartItemIds);

        // then
        assertThat(response.items())
                .hasSize(2);

        assertThat(response.totalPrice())
                .isEqualTo(25_000);

        verify(cartItemService)
                .getCartItemSelected(cart, cartItemIds);

        verify(cartItemService, never())
                .getCartItem(cart);
    }


    @Test
    void 장바구니가_없으면_CART_EMPTY_예외가_발생한다() {

        // given
        when(cartService.getCart(memberId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                orderFacade.getCheckout(memberId, List.of())
        )
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CART_EMPTY);

        verify(cartService)
                .getCart(memberId);

        verifyNoInteractions(cartItemService);
    }


    @Test
    void 장바구니에_상품이_없으면_CART_EMPTY_예외가_발생한다() {

        // given
        when(cartService.getCart(memberId))
                .thenReturn(Optional.of(cart));

        when(cartItemService.getCartItem(cart))
                .thenReturn(List.of());

        // when & then
        assertThatThrownBy(() ->
                orderFacade.getCheckout(memberId, List.of())
        )
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CART_EMPTY);

        verify(cartItemService)
                .getCartItem(cart);

        verify(cartItemService, never())
                .getCartItemSelected(any(), any());
    }

    @Test
    void 선택한_상품_중_일부가_없으면_CART_ITEM_NOT_FOUND_예외가_발생한다() {

        // given
        List<Long> cartItemIds = List.of(1L, 2L);

        when(cartService.getCart(memberId))
                .thenReturn(Optional.of(cart));

        when(cartItemService.getCartItemSelected(cart, cartItemIds))
                .thenReturn(List.of(cartItem1));

        // when & then
        assertThatThrownBy(() ->
                orderFacade.getCheckout(memberId, cartItemIds)
        )
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);

        verify(cartItemService)
                .getCartItemSelected(cart, cartItemIds);
    }

    // =========================
    // createOrder()
    // =========================
    @Test
    void 주문을_정상적으로_생성한다() {

        // given
        OrderCreateRequest request =
                new OrderCreateRequest(List.of(1L, 2L));

        when(memberService.findMember(memberId))
                .thenReturn(member);

        when(cartService.getCart(memberId))
                .thenReturn(Optional.of(cart));

        when(cartItemService.getCartItemSelected(
                cart,
                request.cartItemIds()
        )).thenReturn(List.of(cartItem1, cartItem2));

        givenCartItem(
                cartItem1,
                product1,
                2,
                "상품1",
                10_000
        );

        givenCartItem(
                cartItem2,
                product2,
                3,
                "상품2",
                5_000
        );

        when(orderService.createOrder(
                eq(member),
                eq(35_000),
                anyList()
        )).thenReturn(order);

        // when
        OrderCreateResponse response =
                orderFacade.createOrder(memberId, request);

        // then
        assertThat(response)
                .isNotNull();

        // 상품 재고 차감
        verify(product1)
                .deductStock(2);

        verify(product2)
                .deductStock(3);

        // 주문 생성
        verify(orderService)
                .createOrder(
                        eq(member),
                        eq(35_000),
                        anyList()
                );

        // 결제 생성
        verify(paymentService)
                .createPayment(order, 35_000);
    }



    @Test
    void 존재하지_않는_장바구니_상품이면_주문을_생성하지_않는다() {

        // given
        OrderCreateRequest request =
                new OrderCreateRequest(List.of(1L, 2L));

        when(memberService.findMember(memberId))
                .thenReturn(member);

        when(cartService.getCart(memberId))
                .thenReturn(Optional.of(cart));

        // 2개를 요청했지만 1개만 조회됨
        when(cartItemService.getCartItemSelected(
                cart,
                request.cartItemIds()
        )).thenReturn(List.of(cartItem1));

        // when & then
        assertThatThrownBy(() ->
                orderFacade.createOrder(memberId, request)
        )
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);

        // 주문 생성 X
        verify(orderService, never())
                .createOrder(any(), anyInt(), anyList());

        // 결제 생성 X
        verify(paymentService, never())
                .createPayment(any(), anyInt());
    }

    // =========================
    // cancelOrder()
    // =========================
    @Test
    void 주문_취소시_결제가_IN_PROGRESS이면_결제를_실패처리한다() {

        // given
        givenOrderCancellation(PaymentStatus.IN_PROGRESS);

        // when
        orderFacade.cancelOrder(
                memberId,
                orderId,
                "단순 변심"
        );

        // then
        verify(paymentService)
                .failPayment(payment);

        verify(paymentService, never())
                .cancelPayment(payment);

        // 주문 취소
        verify(order)
                .cancel("단순 변심");

        // 재고 복구
        verify(product1)
                .restoreStock(2);

        verify(product2)
                .restoreStock(3);
    }


    @Test
    void 주문_취소시_결제가_PAID이면_결제를_취소한다() {

        // given
        givenOrderCancellation(PaymentStatus.PAID);

        // when
        orderFacade.cancelOrder(
                memberId,
                orderId,
                "단순 변심"
        );

        // then
        verify(paymentService)
                .cancelPayment(payment);

        verify(paymentService, never())
                .failPayment(payment);

        // 주문 취소
        verify(order)
                .cancel("단순 변심");

        // 재고 복구
        verify(product1)
                .restoreStock(2);

        verify(product2)
                .restoreStock(3);
    }

    @Test
    void 주문_취소시_잘못된_결제상태이면_예외가_발생한다() {

        // given
        when(orderService.findOwnedOrderWithItems(memberId, orderId))
                .thenReturn(order);

        when(paymentService.findByOrderIdAndMemberId(orderId, memberId))
                .thenReturn(payment);

        when(payment.getStatus())
                .thenReturn(PaymentStatus.CANCELLED);

        // when & then
        assertThatThrownBy(() ->
                orderFacade.cancelOrder(memberId, orderId, "단순 변심")
        )
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAYMENT_STATUS);

        verify(paymentService, never())
                .failPayment(payment);

        verify(paymentService, never())
                .cancelPayment(payment);

        verify(order, never())
                .cancel(anyString());
    }

    // =========================
    // Helper
    // =========================
    private void givenOrderCancellation(PaymentStatus paymentStatus) {

        when(orderService.findOwnedOrderWithItems(
                memberId,
                orderId
        )).thenReturn(order);

        when(paymentService.findByOrderIdAndMemberId(
                orderId,
                memberId
        )).thenReturn(payment);

        when(payment.getStatus())
                .thenReturn(paymentStatus);

        when(order.getOrderItems())
                .thenReturn(List.of(orderItem1, orderItem2));

        when(orderItem1.getProduct())
                .thenReturn(product1);

        when(orderItem1.getQuantity())
                .thenReturn(2);

        when(orderItem2.getProduct())
                .thenReturn(product2);

        when(orderItem2.getQuantity())
                .thenReturn(3);
    }



    private void givenCartItem(
            CartItem cartItem,
            Product product,
            int quantity,
            String name,
            int price
    ) {
        when(cartItem.getProduct())
                .thenReturn(product);

        when(cartItem.getQuantity())
                .thenReturn(quantity);

        when(product.getName())
                .thenReturn(name);

        when(product.getPrice())
                .thenReturn(price);
    }
}