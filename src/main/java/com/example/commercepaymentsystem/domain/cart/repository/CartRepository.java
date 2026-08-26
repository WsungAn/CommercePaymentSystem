package com.example.commercepaymentsystem.domain.cart.repository;

import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // cart의 memberId를 통해서 cart를 가져온다. cart에 member는 유니크라서 괜찮음
    @Query("""
    SELECT c
    FROM Cart c
    WHERE c.member.id = :memberId
""")
    Optional<Cart> findCartByMemberId(@Param("memberId") Long memberId);
}
