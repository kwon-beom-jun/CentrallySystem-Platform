//package com.cs.auth.controller;
//
//import lombok.RequiredArgsConstructor;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.*;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import com.cs.auth.entity.AuthSocialLinkToken;
//import com.cs.auth.entity.AuthSocialLogin;
//import com.cs.auth.entity.AuthUser;
//import com.cs.auth.handler.CustomJwtAuthenticationSuccessHandler;
//import com.cs.auth.repository.AuthSocialLinkTokenRepository;
//import com.cs.auth.repository.AuthSocialLoginRepository;
//import com.cs.auth.repository.AuthTempUserRepository;
//import com.cs.auth.repository.AuthUserRepository;
//import com.cs.auth.service.AuthEmailCodeService;
//import com.cs.auth.service.AuthJwtService;
//import com.cs.auth.util.social.SocialLoginCryptoUtil;
//import com.cs.core.handler.GlobalExceptionHandler;
//import com.cs.core.util.UrlEncodingUtil;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
///**
// * 인증을 처리하는 컨트롤러
// *  - OAuth2 인증 성공 후 /oauth2/success 에서 리다이렉트로 진입한것이라 반환 시 강제적으로 프론트로 보내줘야함
// *  
// *  AuthSocialLinkToken, AuthSocialLinkTokenRepository 사용
// *  
// */
//@Controller
//@RequestMapping
//@RequiredArgsConstructor
//public class AuthLoginController {
//
//    private final AuthenticationManager authenticationManager;
//    private final AuthJwtService jwtService;
//    private final AuthUserRepository authUsersRepository;
//    private final AuthSocialLoginRepository authSocialLoginRepository;
//    private final AuthEmailCodeService authEmailCodeService;
//    private final AuthTempUserRepository authTempUserRepository; 
//    
//    // 🔹 새로 추가된 리포지토리 (DB에 임시 토큰 저장)
//    private final AuthSocialLinkTokenRepository authSocialLinkTokenRepository;
//    
//    private final CustomJwtAuthenticationSuccessHandler jwtSuccessHandler;
//
//    private final String SOCIAL_GOOGLE = "google";
//    private final String SOCIAL_KAKAO = "kakao";
//
//    @Value("${oauth.success.redirect.frontend-url}")
//    private String oauthSuccessRedirectFrontendUrl;
//
//    @Value("${oauth.fail.redirect.frontend-url}")
//    private String oauthFailRedirectFrontendUrl;
//
//    @Value("${oauth.fail.already.login.redirect.frontend-url}")
//    private String oauthFailAlreadyLoginRedirectFrontendUrl;
//
//    @Value("${oauth.social.no.search.redirect.frontend-url}")
//    private String oauthSocialNoSearchRedirectFrontendUrl;
//
//    @Value("${jwt.secret}")
//    String secretKey;
//
//    @Value("${jwt.expiration}")
//    private int jwtMaxAge;
//
//    /**
//     * 테스트용 로그인 페이지 (GET /auth/login-page)
//     */
//    @GetMapping("/login-page")
//    public String loginPage() {
//        return "login"; // Thymeleaf 템플릿이 있다면 렌더링
//    }
//
//    /**
//     * JSON 기반 로그인
//     * - username, password를 JSON으로 받음
//     * - 인증 성공 시 JWT 발급(또는 원하는 로직) 처리
//     */
//    @PostMapping("/login")
//    @ResponseBody
//    public ResponseEntity<?> login(
//            @RequestBody LoginRequest loginRequest,
//            HttpServletRequest request,
//            HttpServletResponse response) {
//
//
//		/* 임시 회원 여부 먼저 검사 */
//		if (authTempUserRepository.existsByEmail(loginRequest.getUserEmail())) {
//			throw new InsufficientAuthenticationException(
//				GlobalExceptionHandler.CC + "해당 이메일은 관리자 승인 대기 중인 계정입니다"
//			);
//		}
//
//        /* 비활성 계정 여부 검사 */
//        authUsersRepository.findByEmail(loginRequest.getUserEmail())
//            .ifPresent(user -> {
//                if (!user.getEnabled()) {
//                    throw new DisabledException(GlobalExceptionHandler.CC + "비활성화된 계정입니다.");
//                }
//            });
//
//        /* 이메일 존재 여부를 먼저 확인 */
//        boolean emailExists = authUsersRepository.existsByEmail(loginRequest.getUserEmail());
//
//        UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getUserEmail(),
//                        loginRequest.getPassword()
//                );
//
//        // 실제 인증 시도 (UserDetailsService → DB 조회 후 비밀번호 매칭)
//        Authentication authentication;
//        try {
//            authentication = authenticationManager.authenticate(authToken);
//        } catch (AuthenticationException ex) {
//            /* 이메일 존재 여부에 따라 다른 오류 메시지 반환 */
//            if (emailExists) {
//                // 이메일은 존재하므로, 비밀번호가 틀렸을 가능성이 높음
//                throw new InternalAuthenticationServiceException(GlobalExceptionHandler.CC + "비밀번호가 틀렸습니다.");
//            } else {
//                // 이메일이 존재하지 않는 경우 (기존과 동일한 메시지)
//                throw new InternalAuthenticationServiceException(GlobalExceptionHandler.CC + "이메일과 비밀번호를 확인해 주세요");
//            }
//        }
//
//        /* ────────── 권한이 비었는지 별도로 검사 ────────── */
//        if (authentication.getAuthorities() == null || authentication.getAuthorities().isEmpty()) {
//            throw new InsufficientAuthenticationException(GlobalExceptionHandler.CC + "부여된 권한이 없습니다\n관리자에게 문의하세요"
//            );
//        }
//
//        // 인증 성공 시, JWT를 쿠키나 헤더에 담는 등 원하는 로직 처리
//        try {
//        	jwtSuccessHandler.onAuthenticationSuccess(request, response, authentication);
//		} catch (Exception e) {
//            throw new InternalAuthenticationServiceException(GlobalExceptionHandler.CC + "로그인 실패 : " + e.getMessage());
//		}
//
//        // 🔹 (중요) 소셜 매핑 로직 (DB 임시토큰 기반)
//        if (loginRequest.getEncryptData() != null && !loginRequest.getEncryptData().isEmpty()) {
//            String tokenValue = loginRequest.getEncryptData();
//
//            // 1) DB에서 토큰 조회
//            AuthSocialLinkToken linkToken = authSocialLinkTokenRepository.findByTokenValue(tokenValue)
//                    .orElseThrow(() -> new InternalAuthenticationServiceException(
//                            GlobalExceptionHandler.CC + "유효하지 않은 소셜 토큰이거나 만료되었습니다"
//                    ));
//
//            // 2) 토큰이 이미 사용되었거나 만료되었는지 확인
//            if (linkToken.isInvalid()) {
//                throw new InternalAuthenticationServiceException(GlobalExceptionHandler.CC + "소셜 토큰이 만료되었거나 이미 사용되었습니다");
//            }
//
//            // 3) 사용자 정보(현재 로그인 계정) 찾아서 userId 획득
//            Optional<AuthUser> existingUser = authUsersRepository.findByEmail(loginRequest.getUserEmail());
//            if (existingUser.isEmpty()) {
//                throw new InternalAuthenticationServiceException(GlobalExceptionHandler.CC + "로그인 사용자를 찾을 수 없습니다");
//            }
//
//            // 4) 이미 존재하는 소셜로그인인지 검사
//            Optional<AuthSocialLogin> existingSocialLogin =
//                    authSocialLoginRepository.findBySocialNameAndSocialUserId(
//                            linkToken.getSocialName(),
//                            linkToken.getSocialId()
//                    );
//
//            if (existingSocialLogin.isPresent()) {
//                throw new InternalAuthenticationServiceException(GlobalExceptionHandler.CC + "이미 존재하는 소셜 로그인 정보입니다");
//            }
//
//            // 5) 새 소셜 로그인 정보 등록
//            AuthSocialLogin socialLogin = AuthSocialLogin.builder()
//                    .socialName(linkToken.getSocialName())
//                    .socialUserId(linkToken.getSocialId())
//                    .userId(existingUser.get().getUserId())
//                    .socialEmail(linkToken.getSocialEmail())
//                    .build();
//
//            authSocialLoginRepository.save(socialLogin);
//
//            // 6) 토큰 사용 처리(used = true)
//            linkToken.setUsed(true);
//            authSocialLinkTokenRepository.save(linkToken);
//
//            System.out.println("✅ 소셜 로그인 데이터 저장 완료: " + socialLogin);
//            
//            // 7) "소셜 연동 알림" 이메일 발송
//            //    - 연동된 소셜 이메일로 보냄
//            //    - 카카오는 추후 알림톡, 여기서는 메일로
//            String userEmail = existingUser.get().getEmail(); 
//            String provider = linkToken.getSocialName();
//            String linkedSocialEmail = linkToken.getSocialEmail(); 
//            
//            authEmailCodeService.sendSocialLinkNotification(userEmail, provider, linkedSocialEmail);
//        }
//
//        // 클라이언트에게 원하는 응답(JSON, 상태코드 등)
//        return ResponseEntity.status(HttpStatus.OK).body("로그인 성공");
//    }
//
//    /**
//     * OAuth2 로그인 성공 시 이동 (GET /auth/oauth2/success)
//     */
//    @GetMapping("/oauth2/success")
//    public String oauth2LoginSuccess(
//            Authentication authentication,
//            HttpServletRequest request,
//            HttpServletResponse response) {
//
//        if (!(authentication instanceof OAuth2AuthenticationToken)) {
//            System.out.println("이미 일반 로그인 상태이거나, OAuth2 토큰이 아님");
//
//            // JWT 쿠키 제거
//            Cookie jwtCookie = new Cookie("jwt", null);
//            jwtCookie.setPath("/");
//            jwtCookie.setHttpOnly(true);
//            jwtCookie.setSecure(true);
//            jwtCookie.setMaxAge(0);
//            response.addCookie(jwtCookie);
//
//            // 기존 인증 제거
//            try {
//                SecurityContextHolder.clearContext();
//                request.logout(); // 세션 무효화
//                request.getSession().invalidate();
//            } catch (ServletException e) {
//                e.printStackTrace();
//            }
//
//            // 프론트엔드 콜백 페이지로 리다이렉트 + 메시지 전달
//            try {
//                response.sendRedirect(oauthFailAlreadyLoginRedirectFrontendUrl);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        // OAuth2AuthenticationToken으로 다운캐스팅
//        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//
//        // 어느 Provider(google/kakao)인지 확인
//        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
//
//        // 인증 후 principal 정보(소셜 프로필 정보 꺼내기)
//        OAuth2User oAuth2User = oauthToken.getPrincipal();
//
//        AuthUser userEntity;
//        String nickname = null;
//        String userEmail = null;
//
//        // ************************
//        // (A) 구글 로그인 처리
//        // ************************
//        if (SOCIAL_GOOGLE.equals(registrationId)) {
//            String googleSubject = oAuth2User.getAttribute("sub"); 
//            nickname = (String) oAuth2User.getAttributes().get("name");
//            userEmail = oAuth2User.getAttribute("email");
//
//            // 소셜 가입이 있는지 확인
//            Optional<AuthSocialLogin> optionalSocialUser =
//                authSocialLoginRepository.findBySocialNameAndSocialUserId(SOCIAL_GOOGLE, googleSubject);
//
//            if (optionalSocialUser.isPresent()) {
//                // DB에서 유저 찾기
//                Optional<AuthUser> optionalUser =
//                    authUsersRepository.findById(optionalSocialUser.get().getUserId());
//                try {
//                    userEntity = optionalUser.get();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return "redirect:" + oauthFailRedirectFrontendUrl 
//                           + UrlEncodingUtil.encode("소셜 정보와 매칭되는 사용자가 없습니다");
//                }
//            } else {
//                // 🔹 소셜 가입 정보가 없으면, DB에 임시 토큰 생성
//                String tokenValue = UUID.randomUUID().toString();
//                
//                AuthSocialLinkToken linkToken = AuthSocialLinkToken.builder()
//                        .tokenValue(tokenValue)
//                        .socialName(SOCIAL_GOOGLE)
//                        .socialId(googleSubject)
//                        .socialEmail(userEmail)
//                        .nickname(nickname)
//                        .createdAt(LocalDateTime.now())
//                        .expiresAt(LocalDateTime.now().plusMinutes(5)) // 5분 만료 예시
//                        .used(false)
//                        .build();
//                
//                authSocialLinkTokenRepository.save(linkToken);
//
//                // 프론트엔드로 리다이렉트 (URL 파라미터로 tokenValue 전달)
//                // 예: http://localhost:3000/#/login?encrypt=xxxx-xxxx
//                return "redirect:" + oauthSocialNoSearchRedirectFrontendUrl 
//                       + "&encrypt=" + tokenValue;
//            }
//
//            System.out.println("\n\n===============================");
//            System.out.println("OAuth2 구글 로그인 성공!");
//            System.out.println("googleSubject : " + googleSubject);
//            System.out.println("email : " + userEmail);
//            System.out.println("nickname : " + nickname);
//            System.out.println("===============================\n\n");
//
//        // ************************
//        // (B) 카카오 로그인 처리
//        // ************************
//        } else if (SOCIAL_KAKAO.equals(registrationId)) {
//            Map<String, Object> attributes = oAuth2User.getAttributes();
//
//            String kakaoId = String.valueOf(attributes.get("id"));
//            
//            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
//            if (kakaoAccount != null) {
//                userEmail = (String) kakaoAccount.get("email");
//            }
//
//            Map<String, Object> props = (Map<String, Object>) attributes.get("properties");
//            if (props != null) {
//                nickname = (String) props.get("nickname");
//            }
//
//            // 소셜 가입이 있는지 확인
//            Optional<AuthSocialLogin> optionalSocialUser =
//                authSocialLoginRepository.findBySocialNameAndSocialUserId(SOCIAL_KAKAO, kakaoId);
//
//            if (optionalSocialUser.isPresent()) {
//                Optional<AuthUser> optionalUser =
//                    authUsersRepository.findById(optionalSocialUser.get().getUserId());
//                try {
//                    userEntity = optionalUser.get();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return "redirect:" + oauthFailRedirectFrontendUrl 
//                           + UrlEncodingUtil.encode("소셜 정보와 매칭되는 사용자가 없습니다");
//                }
//            } else {
//                // 🔹 소셜 가입 정보가 없으면, DB에 임시 토큰 생성
//                String tokenValue = UUID.randomUUID().toString();
//                
//                AuthSocialLinkToken linkToken = AuthSocialLinkToken.builder()
//                        .tokenValue(tokenValue)
//                        .socialName(SOCIAL_KAKAO)
//                        .socialId(kakaoId)
//                        .socialEmail(userEmail)
//                        .nickname(nickname)
//                        .createdAt(LocalDateTime.now())
//                        .expiresAt(LocalDateTime.now().plusMinutes(5))
//                        .used(false)
//                        .build();
//
//                authSocialLinkTokenRepository.save(linkToken);
//
//                return "redirect:" + oauthSocialNoSearchRedirectFrontendUrl 
//                       + "&encrypt=" + tokenValue;
//            }
//
//            System.out.println("\n\n===============================");
//            System.out.println("OAuth2 카카오 로그인 성공!");
//            System.out.println("email : " + userEmail);
//            System.out.println("nickname : " + nickname);
//            System.out.println("===============================\n\n");
//
//        } else {
//            // 그 외 Provider
//            return "알 수 없는 소셜 로그인입니다. provider=" + registrationId;
//        }
//
//        if (userEntity.getUserRoles() == null || userEntity.getUserRoles().isEmpty()) {
//            // 권한 없음 → 실패 페이지로 리다이렉트
//            return "redirect:" + oauthFailRedirectFrontendUrl
//                   + UrlEncodingUtil.encode("부여된 권한이 없습니다\n관리자에게 문의하세요");
//        }
//        
//        // 만약 (위에서) 소셜 가입이 이미 존재해 userEntity를 찾았다면 → JWT 발급
//        String jwt = jwtService.createToken(userEntity);
//
//        System.out.println("\n\n===============================");
//        System.out.println("OAuth2 Login JWT: " + jwt);
//        System.out.println("===============================\n\n");
//
//        Cookie jwtCookie = new Cookie("jwt", jwt);
//        jwtCookie.setHttpOnly(true);
//        jwtCookie.setSecure(false); // HTTPS 환경이면 true
//        jwtCookie.setPath("/");
//        jwtCookie.setMaxAge(jwtMaxAge);
//        response.addCookie(jwtCookie);
//
//        return "redirect:" + oauthSuccessRedirectFrontendUrl;
//    }
//
//    /**
//     * 로그인 후, JWT Token으로 사용자 정보 확인 (테스트용)
//     */
//    @GetMapping("/token")
//    @ResponseBody
//    public String getToken(Authentication authentication) {
//        String userEmail = authentication.getName();
//        String jwt;
//        try {
//            AuthUser userEntity = authUsersRepository
//                    .findByEmail(userEmail)
//                    .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "User not found in DB"));
//            jwt = jwtService.createToken(userEntity);
//        } catch (Exception e) {
//            return "User not found in DB";
//        }
//
//        Claims claims = Jwts.parserBuilder()
//            .setSigningKey(secretKey.getBytes())
//            .build()
//            .parseClaimsJws(jwt)
//            .getBody();
//
//        System.out.println("OAuth2 Token Generated: " + jwt);
//        System.out.println("Claims Subject: " + claims.getSubject());
//
//        return jwt;
//    }
//
//    /**
//     * 로그인 된 사용자의 정보를 반환 (예: JWT 파싱 후 반환)
//     */
//    @GetMapping("/me")
//    @ResponseBody
//    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies == null) {
//            throw new BadCredentialsException(GlobalExceptionHandler.CC + "쿠키가 없습니다. 로그인 필요");
//        }
//
//        String token = null;
//        for (Cookie c : cookies) {
//            if ("jwt".equals(c.getName())) {
//                token = c.getValue();
//                break;
//            }
//        }
//        if (token == null) {
//            throw new BadCredentialsException(GlobalExceptionHandler.CC + "JWT 쿠키가 없습니다. 로그인 필요");
//        }
//
//        if (!jwtService.validateToken(token)) {
//            throw new BadCredentialsException(GlobalExceptionHandler.CC + "JWT가 유효하지 않습니다.");
//        }
//
//        int userId = jwtService.getUserId(token);
//        String userEmail = jwtService.getUserEmail(token);
//        String username = jwtService.getUsername(token);
//        List<String> roles = jwtService.getRoles(token);
//
//        Map<String, Object> responseBody = new HashMap<>();
//        responseBody.put("userId", userId);
//        responseBody.put("userEmail", userEmail);
//        responseBody.put("username", username);
//        responseBody.put("roles", roles);
//
//        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
//    }
//
//    /**
//     * 로그인 요청 DTO
//     */
//    static class LoginRequest {
//        private String userEmail;
//        private String password;
//        private String encryptData;
//
//        public String getUserEmail() { return userEmail; }
//        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
//        public String getPassword() { return password; }
//        public void setPassword(String password) { this.password = password; }
//        public String getEncryptData() { return encryptData; }
//        public void setEncryptData(String encryptData) { this.encryptData = encryptData; }
//    }
//}
