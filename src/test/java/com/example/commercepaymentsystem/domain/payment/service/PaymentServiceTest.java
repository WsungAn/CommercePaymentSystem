package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.fixture.*;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentDetailResponse;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import com.example.commercepaymentsystem.domain.product.entity.Product;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 정보 단건 조회")
    void getPayment() {
        // given
        Long memberId = 1L;
        Member member = MemberFixture.createMemberWithId(memberId);
        Product product = ProductFixture.createProduct();
        OrderItem orderItem = OrderItemFixture.createOrderItem(product);
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        Long orderId = 10L;
        Order order = OrderFixture.createOrderWithId(member, orderItems, orderId);
        Payment payment = PaymentFixture.createPayment(order, member);
        when(paymentRepository.findByOrderIdAndMemberId(orderId, memberId))
                .thenReturn(Optional.of(payment));

        // when
        PaymentDetailResponse response = paymentService.getPayment(orderId, memberId);

        // then
        assertThat(response.amount()).isEqualTo(payment.getAmount());
        assertThat(response.status()).isEqualTo(payment.getStatus());
        assertThat(response.paidAt()).isEqualTo(payment.getPaidAt());
    }
}
