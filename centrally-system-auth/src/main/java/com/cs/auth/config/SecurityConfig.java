package com.cs.auth.config;


import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.cs.core.constants.JwtConstants;

import com.cs.auth.handler.CustomAuthenticationFailureHandler;
import com.cs.auth.service.CustomUserDetailsService;
import com.cs.core.filter.JwtAuthenticationFilter;

import jakarta.servlet.http.Cookie;

/**
 * Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationFailureHandler failureHandler;
//    private final CustomJwtAuthenticationSuccessHandler jwtSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${login.page.redirect.frontend-url}")
    private String loginPageUrl;

    /**
     * PasswordEncoder Bean 등록 (BCrypt 사용)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider 설정
     * - CustomUserDetailsService와 PasswordEncoder 연동
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager Bean 등록
     * - 인증 시도 시 (일반 로그인) 사용
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * SecurityFilterChain 설정
     * - HTTP 요청 보안을 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (API 서버 시 주로 비활성화)
            .csrf(csrf -> csrf.disable())

            // 인증/인가 규칙 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 경로
                .requestMatchers(
                		"/email/**",
                        "/login/**", 
                		"/public-key", 
                		"/users/temp/join", 
                		"/login-page",
                		"/agreements",
                        "/oauth2/**",
                        "/refresh",
                        "/logout",
                        "/logout-all",
                		"/centrally-system-service-test",
                		// Actuator Prometheus 엔드포인트 허용 (모니터링용)
                		"/actuator/prometheus", "/actuator/health", "/actuator/info").permitAll()
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                // 구글, 카카오 등 소셜 로그인을 시도할 때도 동일한 커스텀 로그인 페이지
            	// 인증되지 않은 사용자가 보호된 리소스에 접근할 경우, /login-page URL로 
        		// 리다이렉트하여 커스텀 로그인 폼을 제공하도록 지정
        		// 구글로 인증 시도 : /oauth2/authorization/google
            	// 카카오로 인증 시도 : /oauth2/authorization/kakao
            	// 인증 후 : /auth/oauth2/success
//                .loginPage("/login-page")
                
                // OAuth2 로그인 성공 시 이동 URL (강제)
                // 구글, 카카오 등 여러 소셜 로그인이 여기 통합
//                .defaultSuccessUrl("/auth/oauth2/success", true)
                .successHandler((request, response, authentication) -> {
                    response.sendRedirect("/auth/oauth2/success");
                })
                
//                .failureHandler((request, response, exception) -> {
//                    System.out.println("OAuth2 로그인 실패: " + exception.getMessage());
//                    response.sendRedirect("/auth/login-page?error=" + exception.getMessage());
//                })
                // 로그인 실패 시
                .failureHandler(failureHandler)
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/logout")
//                .logoutSuccessUrl("/login-page")
                .logoutSuccessHandler((request, response, authentication) -> {
                	
                    // JWT 쿠키 삭제: 값 null, 경로 설정, maxAge 0으로 설정하면 쿠키 삭제 효과
                	// ※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※
                	// ※※※  🔒 기존 생성된 설정과 동일해야 제거가 됨 🔒  ※※※
                	// ※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※
                    Cookie jwtCookie = new Cookie(JwtConstants.JWT_TOKEN_NAME, null);
                    jwtCookie.setHttpOnly(true);
                    // HTTPS 환경에서만 활성화된 경우
                    jwtCookie.setSecure(false);
                    jwtCookie.setPath("/");
                    jwtCookie.setMaxAge(0);
                    response.addCookie(jwtCookie);
                    
                    // 로그아웃 성공 후 로그인 페이지로 리다이렉트(Vue 사용시 필요없음)
                    // response.sendRedirect(loginPageUrl);
                })
                .permitAll()
            )
            
            // JWT 필터 등록
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
