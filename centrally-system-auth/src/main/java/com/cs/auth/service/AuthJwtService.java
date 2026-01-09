package com.cs.auth.service;

import com.cs.auth.entity.AuthUser;
import io.jsonwebtoken.Claims;

import java.util.List;

/**
 * JWT 토큰 생성 및 검증 서비스 인터페이스
 */
public interface AuthJwtService {

    /**
     * Secret Key 반환
     * @return JWT 서명에 사용되는 Secret Key
     */
    String getSecretKey();

    /**
     * 유저 정보를 바탕으로 JWT 토큰 생성
     * - 여기서는 AuthUser에 최소 정보만 있다고 가정
     * - teamId, phoneNumber 등 불필요/없는 필드는 제거
     * @param user 인증된 사용자 엔티티
     * @return 생성된 JWT 토큰 문자열
     */
    String createToken(AuthUser user);

    /**
     * JWT 토큰 파싱 → 전체 Claims 반환
     * @param token JWT 토큰 문자열
     * @return Claims 객체
     */
    Claims getAllClaims(String token);

    /**
     * JWT에서 userEmail 추출
     * @param token JWT 토큰 문자열
     * @return 사용자 이메일
     */
    String getUserEmail(String token);

    /**
     * JWT에서 userId 추출
     * @param token JWT 토큰 문자열
     * @return 사용자 ID
     */
    Integer getUserId(String token);

    /**
     * JWT에서 username 추출
     * @param token JWT 토큰 문자열
     * @return 사용자 이름
     */
    String getUsername(String token);

    /**
     * JWT에서 nickname 추출
     * @param token JWT 토큰 문자열
     * @return 사용자 닉네임
     */
    String getNickname(String token);

    /**
     * JWT에서 roles(권한) 목록 추출
     * @param token JWT 토큰 문자열
     * @return 권한 목록
     */
    List<String> getRoles(String token);

    /**
     * JWT에서 지정한 claim 값을 제네릭 타입으로 반환
     * @param token JWT 토큰
     * @param claimName 추출할 claim 이름
     * @param requiredType 반환 타입
     * @param <T> 반환 타입 제네릭
     * @return claim 값
     */
    <T> T getClaim(String token, String claimName, Class<T> requiredType);

    /**
     * 토큰 유효성 검증
     * @param token JWT 토큰 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    boolean validateToken(String token);
}
