package com.example.commercepaymentsystem.domain.order.service;


import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.common.response.PageResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderDetailResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderSummaryResponse;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.order.repository.OrderRepository;


import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final DateTimeFormatter ORDER_NUMBER_TIME =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    private final OrderRepository orderRepository;


   public Order createOrder(Member member, int totalPrice, List<OrderItem> orderItems) {
       Order order = new Order(member, generateOrderNumber(), totalPrice, orderItems);
       return orderRepository.save(order);
   }

    public PageResponse<OrderSummaryResponse> getMyOrders(Long memberId, int page, int size) {
        return PageResponse.of(
                orderRepository.findByMemberIdOrderByCreatedAtDesc(
                        memberId,
                        PageRequest.of(page, size)
                ),
                OrderSummaryResponse::from
        );
    }

    public Order findOwnedOrderWithItems(Long memberId, Long orderId) {
        return orderRepository.findWithItemsByIdAndMemberId(orderId, memberId) // 모두 일치하는 주문 조회
                .orElseThrow(() -> orderRepository.existsById(orderId)
                        ? new BusinessException(ErrorCode.NO_AUTHORITY)
                        : new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }


    private String generateOrderNumber() {
        String orderNumber;
        do {
            orderNumber = "ORD-" + LocalDateTime.now().format(ORDER_NUMBER_TIME)
                    + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }

    // 주문 상태 변경 - CONFIRMED
    @Transactional
    public void confirmOrder(Order order) {
        order.markAsConfirmed();
    }

    // 주문 상태 변경 - CANCELLED
    @Transactional
    public void cancelOrder(Order order) {
        order.markAsCancelled();
    }

    // 주문시도 로직에 필요 - 본인소유주문인지 조회
    public Order findByIdAndMemberId(Long orderId, Long memberId) {
        return orderRepository.findWithItemsByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }
}
