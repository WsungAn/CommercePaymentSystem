package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.fixture.CartFixture;
import com.example.commercepaymentsystem.domain.cart.fixture.MemberFixture;
import com.example.commercepaymentsystem.domain.cart.fixture.OrderFixture;
import com.example.commercepaymentsystem.domain.cart.fixture.PaymentFixture;
import com.example.commercepaymentsystem.domain.cart.service.CartItemService;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderStatus;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentRequest;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentResult;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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


    @Test
    @DisplayName("모의 결제 성공 케이스 - 결제 성공시 각 메서드가 호출되는가")
    void tryPayment() {
        // given
        // 1. 본인소유조회
        PaymentRequest request = new PaymentRequest(1L, PaymentResult.SUCCESS, 10000);
        Long memberId = 1L;
        Member member = MemberFixture.createMemberWithId(memberId);
        Order order = OrderFixture.createOrder(member);
        when(orderService.findByIdAndMemberId(request.orderId(), memberId)).thenReturn(order);

        // 2. 상태검증
        Payment payment = PaymentFixture.createPayment(order, member);
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


}
