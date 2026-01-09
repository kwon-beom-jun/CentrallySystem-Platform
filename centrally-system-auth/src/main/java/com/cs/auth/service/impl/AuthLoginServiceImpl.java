package com.cs.auth.service.impl;

import com.cs.auth.controller.request.AuthLoginRequest;
import com.cs.auth.controller.request.AuthPasswordVerifyRequest;
import com.cs.auth.entity.*;
import com.cs.auth.repository.*;
import com.cs.auth.service.AuthEmailCodeService;
import com.cs.auth.service.AuthJwtService;
import com.cs.auth.service.AuthLoginService;
import com.cs.auth.service.RsaKeyService;
import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.util.UrlEncodingUtil;
import com.cs.core.constants.JwtConstants;
import com.cs.core.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequestMapping
@RequiredArgsConstructor
public class AuthLoginServiceImpl implements AuthLoginService {

    private final AuthenticationManager authenticationManager;
    private final AuthJwtService jwtService;
    private final AuthUserRepository authUsersRepository;
    private final AuthSocialLoginRepository authSocialLoginRepository;
    private final AuthEmailCodeService authEmailCodeService;
    private final AuthTempUserRepository authTempUserRepository; 
    
    // 🔹 새로 추가된 리포지토리 (DB에 임시 토큰 저장)
    private final AuthSocialLinkTokenRepository authSocialLinkTokenRepository;
    
    // 🔹 Refresh Token Repository
    private final AuthRefreshTokenRepository authRefreshTokenRepository;
    
    private final RsaKeyService rsaKeyService;

    private final String SOCIAL_GOOGLE = "google";
    private final String SOCIAL_KAKAO = "kakao";

    @Value("${oauth.success.redirect.frontend-url}")
    private String oauthSuccessRedirectFrontendUrl;

    @Value("${oauth.fail.redirect.frontend-url}")
    private String oauthFailRedirectFrontendUrl;

    @Value("${oauth.fail.already.login.redirect.frontend-url}")
    private String oauthFailAlreadyLoginRedirectFrontendUrl;

    @Value("${oauth.social.no.search.redirect.frontend-url}")
    private String oauthSocialNoSearchRedirectFrontendUrl;

    @Value("${jwt.secret}")
    String secretKey;

    @Value("${jwt.expiration}")
    private int jwtMaxAge;

    @Value("${jwt.refresh.expiration:604800}")
    private int refreshTokenMaxAge; // 기본값: 7일 (초 단위)

    @Value("${jwt.refresh.max-per-user:5}")
    private int maxRefreshTokensPerUser; // 사용자당 최대 Refresh Token 수

    private static final String COOKIE_REFRESH_TOKEN = "refresh_token";

    
    @Override
    public ResponseEntity<?> login(
            AuthLoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 1) 임시회원 검사
        if (authTempUserRepository.existsByEmail(loginRequest.getUserEmail())) {
            throw new InsufficientAuthenticationException(
                GlobalExceptionHandler.CC + "관리자 승인 대기 중인 계정입니다"
            );
        }

        // 2) 비활성 계정 검사
        authUsersRepository.findByEmail(loginRequest.getUserEmail())
            .ifPresent(u -> {
                if (!u.getEnabled()) {
                    throw new DisabledException(
                        GlobalExceptionHandler.CC + "비활성화된 계정입니다."
                    );
                }
            });

        // 포트폴리오용: 기능 비활성화
        throw new InternalAuthenticationServiceException(
            "포트폴리오용: 로그인 기능이 비활성화되어 있습니다."
        );
    }

    
    @Override
    public String oauth2LoginSuccess(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 1) OAuth2AuthenticationToken 검사
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            clearJwtCookie(response);
            // 기존 인증 제거
            try {
                SecurityContextHolder.clearContext();
                request.logout(); // 세션 무효화
                request.getSession().invalidate();
            } catch (ServletException e) {
                e.printStackTrace();
            }
            // 프론트엔드 콜백 페이지로 리다이렉트 + 메시지 전달
            return "redirect:" + oauthFailAlreadyLoginRedirectFrontendUrl;
        }
        // OAuth2AuthenticationToken으로 다운캐스팅
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        // 어느 Provider(google/kakao)인지 확인
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        // 인증 후 principal 정보(소셜 프로필 정보 꺼내기)
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        // 2) 프로바이더별 이메일·닉네임 추출
        String userEmail;
        String nickname;
        if (SOCIAL_GOOGLE.equals(provider)) {
            userEmail = oAuth2User.getAttribute("email");
            nickname  = oAuth2User.getAttribute("name");
        } else if (SOCIAL_KAKAO.equals(provider)) {
            Map<String, Object> acc   = oAuth2User.getAttribute("kakao_account");
            Map<String, Object> props = oAuth2User.getAttribute("properties");
            userEmail = (String) acc.get("email");
            nickname  = (String) props.get("nickname");
        } else {
            return "알 수 없는 Provider: " + provider;
        }

        AuthUser userEntity;
        Optional<AuthSocialLogin> socialOpt =
            authSocialLoginRepository.findBySocialNameAndSocialUserId(
                provider, oAuth2User.getName());

        // 3) 기존 연동 정보가 있으면 회원 조회, 없으면 임시 토큰 생성 후 리다이렉트
        if (socialOpt.isPresent()) {
            userEntity = authUsersRepository.findById(socialOpt.get().getUserId())
                .orElseThrow(() -> new RuntimeException(
                    GlobalExceptionHandler.CC + "사용자를 찾을 수 없습니다"
                ));
        } else {
            // 소셜 가입 정보가 없으면, DB에 임시 토큰 생성
            String tokenValue = UUID.randomUUID().toString();
            AuthSocialLinkToken link = AuthSocialLinkToken.builder()
                .tokenValue(tokenValue)
                .socialName(provider)
                .socialId(oAuth2User.getName())
                .socialEmail(userEmail)
                .nickname(nickname)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
            authSocialLinkTokenRepository.save(link);
            // 프론트엔드로 리다이렉트 (URL 파라미터로 tokenValue 전달)
            return "redirect:"
                + oauthSocialNoSearchRedirectFrontendUrl
                + "&encrypt=" + tokenValue;
        }

        // 4) 권한 검사
        // HRM 권한 중 하나라도 있는지 확인
        boolean hasHrmRole = userEntity.getUserRoles().stream()
            .anyMatch(userRole -> {
                String roleName = userRole.getRole().getRoleName();
                return roleName.equals(Role.ROLE_HRM_EMPLOYEE.authority()) ||
                       roleName.equals(Role.ROLE_HRM_LEADER.authority()) ||
                       roleName.equals(Role.ROLE_HRM_ASSISTANT_MANAGER.authority()) ||
                       roleName.equals(Role.ROLE_HRM_MANAGER.authority());
            });
        if (!hasHrmRole) {
            return "redirect:" + oauthFailRedirectFrontendUrl
                   + UrlEncodingUtil.encode("HRM 권한이 필요합니다\n관리자에게 HRM 권한 부여를 요청하세요");
        }

        // 5) Access Token 발급
        String accessToken = jwtService.createToken(userEntity);
        
        // 6) Refresh Token 생성 및 저장
        String refreshTokenValue = createAndSaveRefreshToken(userEntity.getUserId(), request);
        
        // 7) 쿠키 설정
        setAuthCookies(response, accessToken, refreshTokenValue);

        // 8) 최종 성공 리다이렉트
        return "redirect:" + oauthSuccessRedirectFrontendUrl;
    }
    
    @Override
    public String getToken(Authentication authentication) {
        String userEmail = authentication.getName();
        AuthUser user = authUsersRepository.findByEmail(userEmail)
            .orElseThrow(() ->
                new UsernameNotFoundException(GlobalExceptionHandler.CC + "User not found in DB")
            );
        return jwtService.createToken(user);
    }

    @Override
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new InsufficientAuthenticationException(
                GlobalExceptionHandler.CC + "쿠키가 없습니다. 로그인 필요"
            );
        }

        String token = null;
        for (Cookie c : cookies) {
            if (JwtConstants.JWT_TOKEN_NAME.equals(c.getName())) {
                token = c.getValue();
                break;
            }
        }
        if (token == null) {
            throw new BadCredentialsException(GlobalExceptionHandler.CC + "JWT 쿠키가 없습니다. 로그인 필요");
        }
        if (!jwtService.validateToken(token)) {
            throw new BadCredentialsException(GlobalExceptionHandler.CC + "JWT가 유효하지 않습니다.");
        }

        int userId      = jwtService.getUserId(token);
        String email    = jwtService.getUserEmail(token);
        String username = jwtService.getUsername(token);
        List<String> roles = jwtService.getRoles(token);

        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("userEmail", email);
        body.put("username", username);
        body.put("roles", roles);

        return ResponseEntity.ok(body);
    }

    // 공통으로 쓰는 쿠키 제거 메서드
    private void clearJwtCookie(HttpServletResponse response) {
        Cookie c = new Cookie(JwtConstants.JWT_TOKEN_NAME, null);
        c.setPath("/");
        c.setHttpOnly(true);
        c.setSecure(true);
        c.setMaxAge(0);
        response.addCookie(c);
    }
    
    @Override
    public ResponseEntity<?> verifyPassword(AuthPasswordVerifyRequest verifyRequest, HttpServletRequest request) {
        try {
            // JWT에서 사용자 정보 추출
            String userEmail = getCurrentUserEmail(request);
            
            if (userEmail == null) {
                throw new InternalAuthenticationServiceException(
                    GlobalExceptionHandler.CC + "인증되지 않은 사용자입니다."
                );
            }
            
            // 사용자 존재 확인
            authUsersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                    GlobalExceptionHandler.CC + "사용자를 찾을 수 없습니다."
                ));
            
            // 비밀번호 확인
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userEmail, verifyRequest.getPassword());
            
            try {
                authenticationManager.authenticate(authToken);
                return ResponseEntity.ok(Map.of("message", "비밀번호가 확인되었습니다."));
            } catch (AuthenticationException ex) {
                throw new InternalAuthenticationServiceException(
                    GlobalExceptionHandler.CC + "비밀번호가 일치하지 않습니다."
                );
            }
            
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(
                GlobalExceptionHandler.CC + "비밀번호를 확인해 주시기 바랍니다."
            );
        }
    }
    
    /**
     * JWT에서 현재 사용자 이메일 추출
     */
    private String getCurrentUserEmail(HttpServletRequest request) {
        try {
            // JWT 쿠키에서 토큰 추출
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (JwtConstants.JWT_TOKEN_NAME.equals(cookie.getName())) {
                        String token = cookie.getValue();
                        
                        // JWT 파싱하여 사용자 이메일 추출
                        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(Keys.hmacShaKeyFor(jwtService.getSecretKey().getBytes()))
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
                        
                        return claims.getSubject();
                    }
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
    
    /**
     * Base64 인코딩된 문자열인지 확인
     */
    private boolean isBase64Encoded(String str) {
        try {
            java.util.Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════
    //  Refresh Token 관련 메서드
    // ═══════════════════════════════════════════════════════════════════
    
    /**
     * Refresh Token 생성 및 DB 저장
     * - 사용자당 최대 토큰 개수 제한
     * - Token Rotation 구현을 위한 기반
     */
    private String createAndSaveRefreshToken(Integer userId, HttpServletRequest request) {
        // 1) 사용자당 최대 토큰 개수 확인
        Long tokenCount = authRefreshTokenRepository.countByUserId(userId);
        if (tokenCount >= maxRefreshTokensPerUser) {
            // 가장 오래된 토큰 삭제
            List<AuthRefreshToken> oldestTokens = authRefreshTokenRepository.findOldestByUserId(userId);
            if (!oldestTokens.isEmpty()) {
                authRefreshTokenRepository.delete(oldestTokens.get(0));
            }
        }
        
        // 2) Refresh Token 생성
        String refreshTokenValue = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusSeconds(refreshTokenMaxAge);
        
        // 3) DB 저장
        AuthRefreshToken refreshToken = AuthRefreshToken.builder()
            .userId(userId)
            .tokenValue(refreshTokenValue)
            .expiresAt(expiry)
            .createdAt(now)
            .lastUsedAt(now)
            .ipAddress(request.getRemoteAddr())
            .userAgent(request.getHeader("User-Agent"))
            .isUsed(false)
            .build();
        
        authRefreshTokenRepository.save(refreshToken);
        
        return refreshTokenValue;
    }
    
    /**
     * Access Token + Refresh Token 쿠키 설정
     */
    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token 쿠키
        Cookie accessCookie = new Cookie(JwtConstants.JWT_TOKEN_NAME, accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false); // HTTPS 환경이면 true
        accessCookie.setPath("/");
        accessCookie.setMaxAge(jwtMaxAge);
        response.addCookie(accessCookie);
        
        // Refresh Token 쿠키
        Cookie refreshCookie = new Cookie(COOKIE_REFRESH_TOKEN, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // HTTPS 환경이면 true
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(refreshTokenMaxAge);
        response.addCookie(refreshCookie);
    }
    
    @Override
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 1) Refresh Token 추출
        String refreshTokenValue = extractRefreshTokenFromCookie(request);
        
        if (refreshTokenValue == null) {
            throw new BadCredentialsException(
                // Refresh Token이 없음
                GlobalExceptionHandler.CC + "로그인 세션이 만료되었습니다.\n다시 로그인해 주세요."
            );
        }
        
        // 2) DB에서 Refresh Token 조회
        AuthRefreshToken refreshToken = authRefreshTokenRepository
            .findByTokenValue(refreshTokenValue)
            .orElseThrow(() -> new BadCredentialsException(
                // 유효하지 않은 Refresh Token
                GlobalExceptionHandler.CC + "로그인 세션이 만료되었습니다.\n다시 로그인해 주세요."
            ));
        
        // 3) Refresh Token Reuse Detection (보안 강화)
        if (refreshToken.getIsUsed()) {
            // ⚠️ 이미 사용된 토큰 재사용 감지 → 모든 토큰 무효화
            authRefreshTokenRepository.deleteAllByUserId(refreshToken.getUserId());
            throw new BadCredentialsException(
                GlobalExceptionHandler.CC + "보안 위협 감지: 모든 세션이 종료되었습니다. 다시 로그인해 주세요."
            );
        }
        
        // 4) 만료 확인
        if (refreshToken.isExpired()) {
            authRefreshTokenRepository.delete(refreshToken);
            throw new BadCredentialsException(
                // Refresh Token이 만료
                GlobalExceptionHandler.CC + "로그인 세션이 만료되었습니다.\n다시 로그인해 주세요."
            );
        }
        
        // 5) IP 검증 (선택적 - 보안 강화)
        String currentIp = request.getRemoteAddr();
        if (refreshToken.getIpAddress() != null && !refreshToken.getIpAddress().equals(currentIp)) {
            // 로그 기록만 하고 통과 (엄격한 검증 필요 시 예외 발생)
            System.out.println("[WARN] Refresh Token IP 변경 감지: " 
                + refreshToken.getIpAddress() + " -> " + currentIp);
        }
        
        // 6) 사용자 정보 조회
        AuthUser user = authUsersRepository.findById(refreshToken.getUserId())
            .orElseThrow(() -> new UsernameNotFoundException(
                GlobalExceptionHandler.CC + "사용자를 찾을 수 없습니다."
            ));
        
        // 7) 권한 재검증 (권한 변경 반영)
        // HRM 권한 중 하나라도 있는지 확인
        boolean hasHrmRole = user.getUserRoles().stream()
            .anyMatch(userRole -> {
                String roleName = userRole.getRole().getRoleName();
                return roleName.equals(Role.ROLE_HRM_EMPLOYEE.authority()) ||
                       roleName.equals(Role.ROLE_HRM_LEADER.authority()) ||
                       roleName.equals(Role.ROLE_HRM_ASSISTANT_MANAGER.authority()) ||
                       roleName.equals(Role.ROLE_HRM_MANAGER.authority());
            });
        if (!hasHrmRole) {
            throw new InsufficientAuthenticationException(
                GlobalExceptionHandler.CC + "HRM 권한이 필요합니다\n관리자에게 HRM 권한 부여를 요청하세요"
            );
        }
        
        // 8) 새 Access Token 생성
        String newAccessToken = jwtService.createToken(user);
        
        // 9) 🔄 Refresh Token Rotation (보안 강화)
        // 기존 Refresh Token 사용 처리 (재사용 감지용)
        refreshToken.markAsUsed();
        authRefreshTokenRepository.save(refreshToken);
        
        // 새 Refresh Token 생성
        String newRefreshTokenValue = createAndSaveRefreshToken(user.getUserId(), request);
        
        // 기존 Refresh Token 삭제
        authRefreshTokenRepository.delete(refreshToken);
        
        // 10) 쿠키 재설정
        setAuthCookies(response, newAccessToken, newRefreshTokenValue);
        
        // 11) 사용자 정보 반환 (프론트 동기화용)
        Map<String, Object> body = new HashMap<>();
        body.put("userId", user.getUserId());
        body.put("userEmail", user.getEmail());
        body.put("username", user.getName());
        body.put("roles", user.getUserRoles().stream()
            .map(r -> r.getRole().getRoleName())
            .toList());
        
        return ResponseEntity.ok(body);
    }
    
    @Override
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1) Refresh Token 추출
        String refreshTokenValue = extractRefreshTokenFromCookie(request);
        
        // 2) DB에서 Refresh Token 삭제
        if (refreshTokenValue != null) {
            authRefreshTokenRepository.deleteByTokenValue(refreshTokenValue);
        }
        
        // 3) 쿠키 제거
        clearAuthCookies(response);
        
        return ResponseEntity.ok("로그아웃 완료");
    }
    
    @Override
    public ResponseEntity<?> logoutAll(HttpServletRequest request, HttpServletResponse response) {
        // 1) 현재 사용자 ID 추출
        String refreshTokenValue = extractRefreshTokenFromCookie(request);
        
        if (refreshTokenValue != null) {
            AuthRefreshToken refreshToken = authRefreshTokenRepository
                .findByTokenValue(refreshTokenValue)
                .orElse(null);
            
            if (refreshToken != null) {
                // 2) 해당 사용자의 모든 Refresh Token 삭제
                authRefreshTokenRepository.deleteAllByUserId(refreshToken.getUserId());
            }
        }
        
        // 3) 쿠키 제거
        clearAuthCookies(response);
        
        return ResponseEntity.ok("모든 기기에서 로그아웃 완료");
    }
    
    /**
     * 쿠키에서 Refresh Token 추출
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_REFRESH_TOKEN.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    /**
     * 인증 쿠키 제거
     */
    private void clearAuthCookies(HttpServletResponse response) {
        // Access Token 쿠키 제거
        Cookie accessCookie = new Cookie(JwtConstants.JWT_TOKEN_NAME, null);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);
        
        // Refresh Token 쿠키 제거
        Cookie refreshCookie = new Cookie(COOKIE_REFRESH_TOKEN, null);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
        
        // 기존 JWT 쿠키도 제거 (하위 호환성)
        Cookie jwtCookie = new Cookie(JwtConstants.JWT_TOKEN_NAME, null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
    }
}

