package com.example.commercepaymentsystem.domain.cart.fixture;

import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {

    private MemberFixture() {}

    public static String MEMBER_NAME = "memberName";
    public static String MEMBER_EMAIL = "memberEmail@example.com";
    public static String MEMBER_PASSWORD = "memberPassword";
    public static String MEMBER_PHONE = "010-1234-5678";

    public static Member createMemberWithId(Long memberId) {
        Member member = new Member(MEMBER_NAME, MEMBER_EMAIL, MEMBER_PASSWORD, MEMBER_PHONE);
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }

    public static Member createNewMemberWithId(
            String name ,String email, String password, String phoneNumber, Long memberId)
    {
        Member member = new Member(name, email, password, phoneNumber);
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }


}
