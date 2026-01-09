package com.cs.auth.service;

import com.cs.auth.controller.request.AuthLoginRequest;
import com.cs.auth.controller.request.AuthPasswordVerifyRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 로그인 서비스 인터페이스
 * - 일반 로그인 및 OAuth2 로그인 처리
 * - JWT 토큰 발급 및 갱신
 * - 로그아웃 처리
 */
public interface AuthLoginService {

    /**
     * JSON 기반 로그인 - username, password를 JSON으로 받음
     * 인증 성공 시 JWT 발급 처리
     * @param loginRequest 로그인 요청 DTO
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 로그인 결과 응답
     */
    ResponseEntity<?> login(AuthLoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response);

    /**
     * OAuth2 로그인 성공 시 이동
     * @param authentication 인증 정보
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 리다이렉트 URL
     */
    String oauth2LoginSuccess(Authentication authentication, HttpServletRequest request, HttpServletResponse response);

    /**
     * 로그인 후, JWT Token으로 사용자 정보 확인 (테스트용)
     * @param authentication 인증 정보
     * @return JWT 토큰 문자열
     */
    String getToken(Authentication authentication);

    /**
     * 로그인 된 사용자의 정보를 반환 (예: JWT 파싱 후 반환)
     * @param request HTTP 요청
     * @return 사용자 정보 응답
     */
    ResponseEntity<?> getMyInfo(HttpServletRequest request);

    /**
     * 비밀번호 확인 - 로그인된 사용자의 비밀번호를 확인
     * @param verifyRequest 비밀번호 확인 요청 DTO
     * @param request HTTP 요청
     * @return 확인 결과 응답
     */
    ResponseEntity<?> verifyPassword(AuthPasswordVerifyRequest verifyRequest, HttpServletRequest request);

    /**
     * Refresh Token으로 Access Token 갱신
     * - Refresh Token Rotation 적용 (매번 새로운 Refresh Token 발급)
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 갱신된 토큰 정보 응답
     */
    ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response);

    /**
     * 로그아웃 - 현재 기기의 Refresh Token 무효화
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 로그아웃 결과 응답
     */
    ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 모든 기기에서 로그아웃 - 사용자의 모든 Refresh Token 무효화
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 로그아웃 결과 응답
     */
    ResponseEntity<?> logoutAll(HttpServletRequest request, HttpServletResponse response);
}
