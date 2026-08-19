package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // 주문 생성시 결제 생성
    @Transactional
    public Payment createPayment(Order order, int amount) {
        Payment payment = new Payment(order, amount);
        return paymentRepository.save(payment);
    }

    // 주문 단건 조회 화면에서 결제 ID 한건만 필요한지? -> 필요하다면 추후 id만 조회하는걸로변경
    // 결제 시도시 주문 ID로 결제정보조회
    public Payment findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

}
