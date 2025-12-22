package com.cs.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cs.auth.entity.AuthUser;
import com.cs.core.service.JwtService;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성 및 검증 서비스
 */
@Service
public class AuthJwtService extends JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    /**
     * Secret Key 반환
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * 유저 정보를 바탕으로 JWT 토큰 생성
     *  - 여기서는 AuthUser에 최소 정보만 있다고 가정
     *  - teamId, phoneNumber 등 불필요/없는 필드는 제거
     */
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
