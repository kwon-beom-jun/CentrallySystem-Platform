package com.cs.core.service;

import io.jsonwebtoken.Claims;

import java.util.List;

/**
 * JWT 토큰 생성 및 검증 서비스 인터페이스
 * - JWT 관련 수정 시 gateway, core의 JwtService가 수정되어야 합니다.
 */
public interface JwtService {

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
