package com.cs.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.cs.auth.controller.request.AuthLoginRequest;
import com.cs.auth.controller.request.AuthPasswordVerifyRequest;
import com.cs.auth.service.AuthLoginService;
import com.cs.auth.service.RsaKeyService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증을 처리하는 컨트롤러 - OAuth2 인증 성공 후 /oauth2/success 에서 리다이렉트로 진입한것이라 반환 시 강제적으로
 * 프론트로 보내줘야함
 * 
 * AuthSocialLinkToken, AuthSocialLinkTokenRepository 사용
 * 
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class AuthLoginController {

	private final AuthLoginService authLoginService;
	private final RsaKeyService rsaKeyService;

	/**
	 * 테스트용 로그인 페이지 (GET /auth/login-page)
	 */
	@GetMapping("/login-page")
	public String loginPage() {
		return "login"; // Thymeleaf 템플릿이 있다면 렌더링
	}

	/**
	 * JSON 기반 로그인 - username, password를 JSON으로 받음 - 인증 성공 시 JWT 발급(또는 원하는 로직) 처리
	 */
	@PostMapping("/login")
	@ResponseBody
	public ResponseEntity<?> login(@RequestBody AuthLoginRequest loginRequest, HttpServletRequest request,
			HttpServletResponse response) {
		// 서비스로 로직 위임
		return authLoginService.login(loginRequest, request, response);
	}

	/**
	 * OAuth2 로그인 성공 시 이동 (GET /auth/oauth2/success)
	 */
	@GetMapping("/oauth2/success")
	public String oauth2LoginSuccess(Authentication authentication, HttpServletRequest request,
			HttpServletResponse response) {
		// 서비스로 로직 위임
		return authLoginService.oauth2LoginSuccess(authentication, request, response);
	}

	/**
	 * 로그인 후, JWT Token으로 사용자 정보 확인 (테스트용)
	 */
	@GetMapping("/token")
	@ResponseBody
	public String getToken(Authentication authentication) {
		return authLoginService.getToken(authentication);
	}

	/**
	 * 로그인 된 사용자의 정보를 반환 (예: JWT 파싱 후 반환)
	 */
	@GetMapping("/me")
	@ResponseBody
	public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
		return authLoginService.getMyInfo(request);
	}

	/**
	 * 비밀번호 확인 - 로그인된 사용자의 비밀번호를 확인
	 */
	@PostMapping("/verify-password")
	@ResponseBody
	public ResponseEntity<?> verifyPassword(@RequestBody AuthPasswordVerifyRequest verifyRequest, 
			HttpServletRequest request) {
		return authLoginService.verifyPassword(verifyRequest, request);
	}
	
	/**
	 * RSA 공개키 조회 API
	 * 로그인 전에 클라이언트가 공개키를 요청하여 비밀번호를 암호화
	 */
	@GetMapping("/public-key")
	@ResponseBody
	public ResponseEntity<Map<String, String>> getPublicKey() {
		Map<String, String> response = new HashMap<>();
		response.put("publicKey", rsaKeyService.getPublicKey());
		return ResponseEntity.ok(response);
	}
	
	/**
	 * Refresh Token으로 Access Token 갱신
	 * - Refresh Token Rotation 적용
	 * - 권한 변경 실시간 반영
	 */
	@PostMapping("/refresh")
	@ResponseBody
	public ResponseEntity<?> refreshToken(
			HttpServletRequest request,
			HttpServletResponse response) {
		return authLoginService.refreshToken(request, response);
	}
	
	/**
	 * 로그아웃 - 현재 기기의 Refresh Token 무효화
	 */
	@PostMapping("/logout")
	@ResponseBody
	public ResponseEntity<?> logout(
			HttpServletRequest request,
			HttpServletResponse response) {
		return authLoginService.logout(request, response);
	}
	
	/**
	 * 모든 기기에서 로그아웃 - 사용자의 모든 Refresh Token 무효화
	 */
	@PostMapping("/logout-all")
	@ResponseBody
	public ResponseEntity<?> logoutAll(
			HttpServletRequest request,
			HttpServletResponse response) {
		return authLoginService.logoutAll(request, response);
	}
}
