package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentDetailResponse;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
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
        Payment payment = new Payment(order, amount, order.getMember());
        return paymentRepository.save(payment);
    }

    // 결제 시도시 주문 ID로 결제정보조회 및 소유권 검증
    public Payment findByOrderIdAndMemberId(Long orderId, Long memberId) {
        return paymentRepository.findByOrder_IdAndMember_Id(orderId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    // 결제 상태 변경 - PAID
    @Transactional
    public void confirmPayment(Payment payment) {
        payment.markAsPaid();
    }

    // 결제 상태 변경 - FAILED
    @Transactional
    public void failPayment(Payment payment) {
        payment.markAsFailed();
    }

    // 결제 상태 변경 - CANCELLED
    @Transactional
    public void cancelPayment(Payment payment) {
        payment.markAsCancelled();
    }

    // 결제 단건 조회 기능
    public PaymentDetailResponse getPayment(Long orderId, Long memberId) {
        Payment payment = findByOrderIdAndMemberId(orderId, memberId);
        return PaymentDetailResponse.from(payment);
    }

}
