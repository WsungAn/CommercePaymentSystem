package com.example.commercepaymentsystem.config;


import com.example.commercepaymentsystem.common.filter.JwtAuthFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toStaticResources;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        // 위에서부터 먼저 매칭되는 쪽이 쓰인다: API 는 JSON 401, 화면은 로그인 페이지로 리다이렉트
                        .defaultAuthenticationEntryPointFor(
                                jsonAuthenticationEntryPoint(),
                                PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                AnyRequestMatcher.INSTANCE)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/favicon.*").permitAll()
                        .requestMatchers("/", "/login", "/signup", "/products/**", "/cart", "/orders/**", "/checkout", "/mypage").permitAll()
                        .requestMatchers("/api/auth/**", "/api/products/**", "/api/webhooks/**", "/api/config/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** API 요청은 리다이렉트 대신 JSON 401 을 유지한다 */
    private AuthenticationEntryPoint jsonAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"AUTH_001\",\"message\":\"인증이 필요합니다\"}");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
