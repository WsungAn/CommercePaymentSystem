package com.example.commercepaymentsystem.domain.order.service;

import com.example.commercepaymentsystem.cart.entity.CartItem;
import com.example.commercepaymentsystem.cart.repository.CartItemRepository;
import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.order.repository.OrderRepository;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
}
