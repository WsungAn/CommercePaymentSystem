package com.example.commercepaymentsystem.domain.member.service;

import com.example.commercepaymentsystem.domain.member.entity.Member;

public interface MemberService {
    Member findMember(Long memberId);
}
