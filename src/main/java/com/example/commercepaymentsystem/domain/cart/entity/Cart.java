package com.example.commercepaymentsystem.domain.cart.entity;

import com.example.commercepaymentsystem.common.entity.BaseEntity;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "carts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// BaseEntity 상속 필요
public class Cart extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    public Cart(Member member) {
        this.member = member;
    }
}
