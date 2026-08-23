package com.example.commercepaymentsystem.domain.auth.service;

import com.example.commercepaymentsystem.common.jwt.JwtProvider;
import com.example.commercepaymentsystem.domain.auth.dto.SignupRequest;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    // 인자 순서가 어긋나면 해시가 전화번호 자리로 들어간다. 실제 인코더라야 그걸 잡을 수 있다
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(memberRepository, passwordEncoder, jwtProvider);
    }

    // Member 생성자는 String 네 개를 받아서 순서가 어긋나도 컴파일된다.
    // 실제로 phone_number(varchar 20) 에 60자짜리 BCrypt 해시가 들어가 회원가입이 통째로 깨졌던 자리다.
    @Test
    @DisplayName("회원가입은 비밀번호를 암호화해 저장하고 나머지 필드는 제자리에 넣는다")
    void 회원가입_필드가_뒤바뀌지_않는다() {
        SignupRequest request = new SignupRequest("데모유저", "demo@commerce.com", "demo1234", "010-1234-5678");
        given(memberRepository.existsByEmail("demo@commerce.com")).willReturn(false);

        authService.signup(request);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        Member saved = captor.getValue();

        assertEquals("데모유저", saved.getName());
        assertEquals("demo@commerce.com", saved.getEmail());
        assertEquals("010-1234-5678", saved.getPhoneNumber());
        assertTrue(passwordEncoder.matches("demo1234", saved.getPassword()),
                "비밀번호는 BCrypt 로 암호화되어 password 필드에 들어가야 한다");
    }
}
