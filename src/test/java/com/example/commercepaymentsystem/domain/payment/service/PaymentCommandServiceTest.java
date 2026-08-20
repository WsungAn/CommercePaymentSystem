package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.fixture.*;
import com.example.commercepaymentsystem.domain.cart.service.CartItemService;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentRequest;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentResult;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentCommandServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderService orderService;

    @Mock
    private CartService cartService;

    @Mock
    private CartItemService cartItemService;

    @InjectMocks
    private PaymentCommandService paymentCommandService;

    private Long memberId;
    private Member member;
    private Order order;
    private Payment payment;
    private OrderItem orderItem;
    private List<OrderItem> orderItems = new ArrayList<>();
    private Product product;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        product = ProductFixture.createProduct();
        orderItem = OrderItemFixture.createOrderItem(product);
        orderItems.add(orderItem);
        member = MemberFixture.createMemberWithId(memberId);
        order = OrderFixture.createOrder(member, orderItems);
        payment = PaymentFixture.createPayment(order, member);
    }


    @Test
    @DisplayName("모의결제 성공 케이스 - 결제 성공시 각 메서드가 호출되는가")
    void tryPaymentSuccess() {
        // given
        // 1. 본인소유조회
        PaymentRequest request = new PaymentRequest(1L, PaymentResult.SUCCESS, 10000);
        when(orderService.findByIdAndMemberId(request.orderId(), memberId)).thenReturn(order);

        // 2. 상태검증
        when(paymentService.findByOrderIdAndMemberId(request.orderId(), memberId)).thenReturn(payment);
        // 3. 금액일치여부 = fixture 금액이랑 맞춰놨기 때문에 pass

        Cart cart = CartFixture.createCartWithId(member, memberId);
        when(cartService.getCart(memberId)).thenReturn(Optional.of(cart));

        // when
        paymentCommandService.tryPayment(request, memberId);

        // then
        verify(paymentService).confirmPayment(payment);
        verify(orderService).confirmOrder(order);
        verify(cartItemService).deleteCartItems(cart);
    }

    @Test
    @DisplayName("모의결제 실패 케이스 - 결제 실패 처리 및 주문 취소와 재고 복구")
    void tryPaymentFailed() {
        // given
        PaymentRequest request = new PaymentRequest(1L, PaymentResult.FAIL, 10000);
        when(orderService.findByIdAndMemberId(request.orderId(), memberId)).thenReturn(order);
        when(paymentService.findByOrderIdAndMemberId(request.orderId(), memberId)).thenReturn(payment);
        int initialStock = product.getStock();

        // when
        paymentCommandService.tryPayment(request, memberId);

        // then
        verify(paymentService).failPayment(payment);
        verify(orderService).cancelOrder(order);
        assertThat(product.getStock()).isEqualTo(initialStock + orderItem.getQuantity());
    }
}
