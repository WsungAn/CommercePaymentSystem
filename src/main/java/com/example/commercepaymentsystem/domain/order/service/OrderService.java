package com.example.commercepaymentsystem.domain.order.service;


import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.common.response.PageResponse;
import com.example.commercepaymentsystem.domain.member.service.MemberService;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderSummaryResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderCancelResponse;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.order.repository.OrderRepository;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentStatus;

import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Member;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final DateTimeFormatter ORDER_NUMBER_TIME =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MemberService memberService;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public OrderCreateResponse createOrder(Long memberId, OrderCreateRequest request) {
        Member member = memberService.findMember(memberId);
        List<CartItem> cartItems = loadCartItems(memberId, request.cartItemIds());

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    cartItem.getProduct().deductStock(cartItem.getQuantity());
                    return new OrderItem(
                            cartItem.getProduct(),
                            cartItem.getProduct().getName(),
                            cartItem.getProduct().getPrice(),
                            cartItem.getQuantity()
                    );
                })
                .toList();

        int totalPrice = orderItems.stream()
                .mapToInt(OrderItem::getLineTotal)
                .reduce(0, Math::addExact);

        Order order = new Order(member, generateOrderNumber(), totalPrice, orderItems);
        Order savedOrder = orderRepository.save(order);
        paymentRepository.save(new Payment(savedOrder, totalPrice));
        return OrderCreateResponse.from(savedOrder);
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

    @Transactional
    public OrderCancelResponse cancelOrder(Long memberId, Long orderId, String reason) {
        Order order = findOwnedOrderWithItems(memberId, orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.IN_PROGRESS) {
            payment.markAsFailed();
        } else if (payment.getStatus() == PaymentStatus.PAID) {
            payment.markAsCancelled();
        } else {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        order.cancel(reason);
        order.getOrderItems().forEach(orderItem ->
                orderItem.getProduct().restoreStock(orderItem.getQuantity()));
        return OrderCancelResponse.of(order, payment);
    }

    private Order findOwnedOrderWithItems(Long memberId, Long orderId) {
        return orderRepository.findWithItemsByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> orderRepository.existsById(orderId)
                        ? new BusinessException(ErrorCode.NO_AUTHORITY)
                        : new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private List<CartItem> loadCartItems(Long memberId, List<Long> requestedIds) {
        if (requestedIds.isEmpty()) {
            List<CartItem> allItems = cartItemRepository.findAllForOrder(memberId);
            if (allItems.isEmpty()) {
                throw new BusinessException(ErrorCode.CART_EMPTY);
            }
            return allItems;
        }

        List<Long> distinctIds = new HashSet<>(requestedIds).stream().toList();
        List<CartItem> selectedItems = cartItemRepository.findSelectedForOrder(memberId, distinctIds);
        if (selectedItems.size() != distinctIds.size()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        return selectedItems;
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
