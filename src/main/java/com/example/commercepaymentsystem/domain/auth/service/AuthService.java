package com.example.commercepaymentsystem.domain.auth.service;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.common.jwt.JwtProvider;
import com.example.commercepaymentsystem.domain.auth.dto.AuthResponse;
import com.example.commercepaymentsystem.domain.auth.dto.LoginRequest;
import com.example.commercepaymentsystem.domain.auth.dto.SignupRequest;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = new Member(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.phoneNumber()
        );
        memberRepository.save(member);
    }

    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        //요청값으로 받은 비밀번호와 DB 비밀번호 비교확인
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        //토큰 발급
        String token = jwtProvider.createToken(member.getId(), member.getEmail());
        return new AuthResponse(token, toMemberInfo(member));
    }


    private AuthResponse.MemberInfo toMemberInfo(Member member) {
        return new AuthResponse.MemberInfo(member.getId(), member.getName(), member.getEmail(), member.getPhoneNumber());
    }
}