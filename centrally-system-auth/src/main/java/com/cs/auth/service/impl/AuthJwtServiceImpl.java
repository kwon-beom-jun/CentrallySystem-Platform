package com.cs.auth.service.impl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cs.auth.entity.AuthUser;
import com.cs.auth.service.AuthJwtService;
import com.cs.core.service.JwtService;
import io.jsonwebtoken.Claims;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

/**
 * JWT 토큰 생성 및 검증 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class AuthJwtServiceImpl implements AuthJwtService {

    private final JwtService jwtService;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    /**
     * Secret Key 반환
     */
    @Override
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * JWT 토큰 파싱 → 전체 Claims 반환
     */
    @Override
    public Claims getAllClaims(String token) {
        return jwtService.getAllClaims(token);
    }

    /**
     * JWT에서 userEmail 추출
     */
    @Override
    public String getUserEmail(String token) {
        return jwtService.getUserEmail(token);
    }

    /**
     * JWT에서 userId 추출
     */
    @Override
    public Integer getUserId(String token) {
        return jwtService.getUserId(token);
    }

    /**
     * JWT에서 username 추출
     */
    @Override
    public String getUsername(String token) {
        return jwtService.getUsername(token);
    }

    /**
     * JWT에서 nickname 추출
     */
    @Override
    public String getNickname(String token) {
        return jwtService.getNickname(token);
    }

    /**
     * JWT에서 roles(권한) 목록 추출
     */
    @Override
    public List<String> getRoles(String token) {
        return jwtService.getRoles(token);
    }

    /**
     * JWT에서 지정한 claim 값을 제네릭 타입으로 반환
     */
    @Override
    public <T> T getClaim(String token, String claimName, Class<T> requiredType) {
        return jwtService.getClaim(token, claimName, requiredType);
    }

    /**
     * 토큰 유효성 검증
     */
    @Override
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    /**
     * 유저 정보를 바탕으로 JWT 토큰 생성
     *  - 여기서는 AuthUser에 최소 정보만 있다고 가정
     *  - teamId, phoneNumber 등 불필요/없는 필드는 제거
     */
    @Override
    public String createToken(AuthUser user) {
        // 현재 시간
        Date now = new Date();
        // 만료 시간 (현재 시간 + 설정된 만료 ms)
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        // SecretKey -> HMAC-SHA 키
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        // 유저가 가진 역할명 리스트
        // (AuthUserRoles → AuthRoles → roleName)
        List<String> roleNames = user.getUserRoles().stream()
                .map(authUserRole -> authUserRole.getRole().getRoleName())
                .collect(Collectors.toList());
        
        // JWT 클레임에 담을 내용
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roleNames);          // 권한 정보
        claims.put("username", user.getName());  // 이름
        claims.put("userId", user.getUserId());  // DB PK
        claims.put("userEmail", user.getEmail());  // DB PK
        claims.put("enabled", true);             // 필요 시 계정 활성 여부 등
        
        // 👉 AuthUser에 존재하지 않거나 더 이상 쓰지 않는 필드는 제거
        //    (phoneNumber, birth, address, profileImgId, teamId 등)

        // 실제 발급
        return Jwts.builder()
                .setSubject(user.getEmail())  // 토큰의 주체(보통 이메일)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}

