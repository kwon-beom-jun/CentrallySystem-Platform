package com.cs.core.service.impl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.cs.core.service.JwtService;

import java.security.Key;
import java.util.*;

/**
 * JWT 토큰 생성 및 검증 서비스 구현체
 *  - JWT 관련 수정 시 gateway, core의 JwtService가 수정되어야 합니다.
 */
@Service
@Primary
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    /**
     * JWT 토큰 파싱 → 전체 Claims 반환
     */
    @Override
    public Claims getAllClaims(String token) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.parserBuilder()
                   .setSigningKey(key)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    /**
     * JWT에서 userEmail 추출
     */
    @Override
    public String getUserEmail(String token) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
        return claims.getSubject();
    }
    
    /**
     * JWT에서 userId 추출
     */
    @Override
    public Integer getUserId(String token) {
        Claims claims = getAllClaims(token);
        return (Integer) claims.get("userId");
    }
    
    /**
     * JWT에서 username 추출
     */
    @Override
    public String getUsername(String token) {
        Claims claims = getAllClaims(token);
        return (String) claims.get("username");
    }
    
    /**
     * JWT에서 nickname 추출
     */
    @Override
    public String getNickname(String token) {
        Claims claims = getAllClaims(token);
        return (String) claims.get("nickname");
    }

    /**
     * JWT에서 roles(권한) 목록 추출
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getRoles(String token) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
        return (List<String>) claims.get("roles", List.class);
    }

    /**
     * JWT에서 지정한 claim 값을 제네릭 타입으로 반환
     *
     * @param token       JWT 토큰
     * @param claimName   추출할 claim 이름
     * @param requiredType 반환 타입
     * @param <T>         반환 타입 제네릭
     * @return claim 값
     */
    @Override
    public <T> T getClaim(String token, String claimName, Class<T> requiredType) {
        Claims claims = getAllClaims(token);
        return claims.get(claimName, requiredType);
    }

    /**
     * 토큰 유효성 검증
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

