package com.example.commercepaymentsystem.domain.payment.repository;

import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderIdAndMemberId(Long orderId, Long memberId);

    //주문 단건 조회 화면 -> 결제 ID 한 건만 필요하므로 payment 엔티티 전체를 로딩하지 않고 ID만 프로젝션
    @Query("SELECT p.id FROM Payment p WHERE p.order.id =:orderId")
    Optional<Long>findByOrderId(@Param("orderId")Long orderId);
}
