package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.service.CartItemService;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.order.entity.OrderStatus;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentResponse;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentResult;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final CartService cartService;
    private final CartItemService cartItemService;

    @Transactional
    public PaymentResponse tryPayment(PaymentRequest request, Long memberId) {

        // 1. 무엇을 조회? - 본인 소유 주문인지 조회
        Order order = orderService.findByIdAndMemberId(request.orderId(), memberId);

        // 2. 무엇을 검증? - 주문상태 = 결제대기 / 결제 상태 = 대기 인가
        Payment payment = paymentService.findByOrderIdAndMemberId(request.orderId(), memberId);

        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        } else if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        // 3. 요청 금액이 서버가 저장해 둔 결제 금액과 일치하는가
        if (request.amount() != payment.getAmount()) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 4. SUCCESS면 무엇을 변경?
        //  4-1)결제 상태 -> 완료 + 결제 완료 일시 기록
        //  4-2)주문 상태 -> 주문 완료
        //  4-3)장바구니 비우기
        if (request.result() == PaymentResult.SUCCESS) {
            paymentService.confirmPayment(payment); // 4-1)
            orderService.confirmOrder(order); // 4-2)
            Optional<Cart> cart = cartService.getCart(memberId);// 4-3)
            if (cart.isEmpty()) {
                throw new BusinessException(ErrorCode.CART_EMPTY);
            }
            cartItemService.deleteCartItems(cart.get());


        }

        // 5. FAIL이면 무엇을 변경?
        //  5-1)결제 상태 -> 실패
        //  5-2)주문 상태 -> 주문 취소
        //  5-3)선차감 재고 전량 복구
        if (request.result() == PaymentResult.FAIL) {
            paymentService.failPayment(payment); // 5-1)
            orderService.cancelOrder(order); // 5-2)
            restoreStock(order); // 5-3)
        }

        return new PaymentResponse(payment.getStatus(), order.getStatus());
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.restoreStock(item.getQuantity());
        }
    }
}
