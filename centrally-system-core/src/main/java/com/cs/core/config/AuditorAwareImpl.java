package com.cs.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * ✅ JPA Auditing을 위한 AuditorAware 구현체
 * 
 * 📌 역할:
 * - `@CreatedBy`, `@LastModifiedBy` 필드의 값을 자동으로 설정
 * - 현재 로그인한 사용자의 정보를 가져와 저장
 * 
 * 📌 동작 방식:
 * 1. Spring Security의 `SecurityContextHolder`에서 현재 인증된 사용자 정보 가져오기
 * 2. JWT 기반 인증을 사용하는 경우, JWT 토큰에서 `sub`(사용자 이메일 또는 ID) 값을 추출
 * 3. 인증 정보가 없거나, JWT 기반 인증이 아닐 경우 `authentication.getName()`을 사용하여 사용자 ID 반환
 */
@Configuration
@EnableJpaAuditing
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // 🔹 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 🔹 인증 정보가 없거나, 인증되지 않은 경우 빈 값 반환
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // 🔹 JWT 기반 인증을 사용하는 경우, 토큰에서 사용자 정보 추출
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String username = jwt.getClaimAsString("sub");  // 🔥 JWT의 "sub" 클레임에서 사용자 ID 가져오기
            return Optional.ofNullable(username);
        }

        // 🔹 기본적으로 Spring Security의 Authentication에서 사용자명 반환
        return Optional.ofNullable(authentication.getName());
    }
}
