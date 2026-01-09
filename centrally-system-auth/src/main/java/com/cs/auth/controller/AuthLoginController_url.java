//package com.cs.auth.controller;
//
//
//import lombok.RequiredArgsConstructor;
//
//import java.io.IOException;
//import java.net.URLDecoder;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import com.cs.auth.entity.AuthSocialLogin;
//import com.cs.auth.entity.AuthUser;
//import com.cs.auth.handler.CustomJwtAuthenticationSuccessHandler;
//import com.cs.auth.repository.AuthSocialLoginRepository;
//import com.cs.auth.repository.AuthUserRepository;
//import com.cs.auth.serivce.AuthJwtService;
//import com.cs.auth.util.SocialLoginCryptoUtil;
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
// * 
// * 인증을 처리하는 컨트롤러
// * 	- OAuth 인증 성공 후 /oauth2/success 에서는 리다이렉트로 진입한것이라 반환 시 강제적으로 프론트로 보내줘야함
// * 
// * SocialLoginCryptoUtil 사용
// * 
// *	🔥 중요 🔥
// * 		@RequestParam("파라미터 이름") 파라미터 이름 없이 자동 인식
// * 		Spring Boot 2.x (Java 8~11)	✅ 자동으로 매핑됨	추가 설정 필요 없음
// * 		Spring Boot 3.x (Java 17)	❌ 자동 매핑 안됨 (파라미터 정보 사라짐)	@RequestParam("username") 명시 or -parameters 추가
// * 		
// * 		[ Gradle 설정 추가 예시 ]
// * 		- 해당 기능 작동이 잘 되지 않아서 명시해서 사용했음
// * 		tasks.withType(JavaCompile) {
// * 			options.compilerArgs << "-parameters"  // ✅ 파라미터 이름 유지 설정 추가
// * 		}
// * 
// */
////@RestController
//@Controller
//@RequestMapping
//// final 붙은 항목은 생성자로 자동 생성해줌
//// ⚡ 스프링 프레임워크는 @Autowired 없이도 final 필드를 포함한 생성자가 있으면 자동으로 의존성을 주입
//@RequiredArgsConstructor
//public class AuthLoginController {
//
//    private final AuthenticationManager authenticationManager;
//    private final AuthJwtService jwtService;
//    private final AuthUserRepository authUsersRepository;
//    private final AuthSocialLoginRepository authSocialLoginRepository;
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
//        // 실제 서비스라면 View를 반환하거나, 프론트엔드 페이지로 리다이렉트
//        return "login"; // Thymeleaf에서 templates/login.html을 찾아 렌더링
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
//        UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getUserEmail(),
//                        loginRequest.getPassword()
//                );
//        
//        try {
//            // 실제 인증 시도 (UserDetailsService → DB 조회 후 비밀번호 매칭)
//            Authentication authentication = authenticationManager.authenticate(authToken);
//
//            // 인증 성공 시, JWT를 쿠키나 헤더에 담는 등 원하는 로직 처리
//            // 여기서는 CustomJwtAuthenticationSuccessHandler를 활용
//            jwtSuccessHandler.onAuthenticationSuccess(request, response, authentication);
//
//        } catch (AuthenticationException ex) {
//            // 인증 실패 시
//        	throw new InternalAuthenticationServiceException(GlobalExceptionHandler.CC + "아이디와 비밀번호를 확인해 주세요");
//        } catch (Exception e) {
//        	throw new InternalAuthenticationServiceException(GlobalExceptionHandler.CC + "로그인 실패 : " + e.getMessage());
//		}
//            
//        // 유저의 정보가 없고 소셜로그인 성공시 받은 인크립트 데이터를 가지고 있어야 소셜 데이터 확인 및 DB에 저장됨
//        if (loginRequest != null && loginRequest.getEncryptData() != null && !loginRequest.getEncryptData().isEmpty()) {
//        	
//        	// URL 인(디)코딩은 암(복)호화때 한번 Vue와 현재 한번 각 2번씩 진행
//        	// URL 디코딩 수행 후 Base64 복호화
//        	String decodedEncryptData = URLDecoder.decode(loginRequest.getEncryptData(), StandardCharsets.UTF_8);
//        	Map<String, String> encryptMap = SocialLoginCryptoUtil.oauthDataJsonDecrypt(decodedEncryptData);
//
//            String socialName = encryptMap.get("social");
//            String socialEmail = encryptMap.get("userEmail");
//            String socialUserId = "";
//
//            if (socialName.equals(SOCIAL_GOOGLE)) {
//                socialUserId = encryptMap.get("googleSubject");
//            } else if (socialName.equals(SOCIAL_KAKAO)) {
//                socialUserId = encryptMap.get("kakaoId");
//            }
//
//            Optional<AuthUser> existingUser = authUsersRepository.findByEmail(loginRequest.getUserEmail());
//
//            // 🔹 기존에 존재하는 소셜 로그인 정보 확인
//            Optional<AuthSocialLogin> existingSocialLogin = authSocialLoginRepository
//                    .findBySocialNameAndSocialUserId(socialName, socialUserId);
//
//            if (existingSocialLogin.isEmpty()) {  
//                // 🔸 새 소셜 로그인 정보 저장 (기존 계정과 연동)
//                AuthSocialLogin socialLogin = AuthSocialLogin.builder()
//                        .socialName(socialName)
//                        .socialUserId(socialUserId)
//                        .userId(existingUser.get().getUserId())
//                        .socialEmail(socialEmail)
//                        .build();
//
//                authSocialLoginRepository.save(socialLogin);
//                
//                System.out.println("✅ 소셜 로그인 데이터 저장 완료: " + socialLogin);
//            } else {
//                System.out.println("✅ 이미 존재하는 소셜 로그인 정보: " + existingSocialLogin.get());
//            }
//        }
//
//        // 클라이언트에게 원하는 응답(JSON, 상태코드 등)
//        return ResponseEntity.status(HttpStatus.OK).body("로그인 성공");
//    }
//
//    /**
//     * OAuth2 로그인 성공 시 이동 (GET /auth/oauth2/success)
//     * - 여기서 JWT 토큰을 생성하여 콘솔에 찍어줍니다.
//     * - 구글/카카오 모두 이곳으로 리다이렉트됨
//     * - 소셜 로그인으로 들어와도 DB에서 소셜로그인 정보로 유저를 찾아서 해당 유저로 JWT 생성
//     */
//    @GetMapping("/oauth2/success")
////    public @ResponseBody String oauth2LoginSuccess(Authentication authentication) {
//	public String oauth2LoginSuccess(
//			Authentication authentication,
//            HttpServletRequest request,
//            HttpServletResponse response) {
//    	
//    	/**
//    	 *	스프링 시큐리티는 기본적으로 “단일 사용자 세션 = 단일 인증 객체” 정책으로 소셜 로그인이 성공해도
//    	 *	스프링 시큐리티가 “이미 기존(일반 로그인) 인증 상태”라고 판단하여 OAuth2 인증 로직을 생략해 버리는 경우가 발생함
//    	 *	일반 로그인이면 다운캐스팅시 에러가 발생하므로
//    	 *	일반 로그인일때를 체크해서 일반 로그인 로그아웃 처리했으니 다시 소셜 로그인 진행하라고 전달
//    	 */
//    	 if (!(authentication instanceof OAuth2AuthenticationToken)) {
//	        System.out.println("이미 일반 로그인 상태이거나, OAuth2 토큰이 아님");
//
//	        Cookie jwtCookie = new Cookie("jwt", null);
//            jwtCookie.setPath("/");
//            jwtCookie.setHttpOnly(true);
//            jwtCookie.setSecure(true);  // HTTPS 환경에서만 활성화된 경우
//            jwtCookie.setMaxAge(0);
//            response.addCookie(jwtCookie);
//	        
//	        // 기존 인증 제거
//	        try {
//	            SecurityContextHolder.clearContext();
//	            request.logout(); // 세션 무효화
//	            request.getSession().invalidate(); // 세션 무효화 추가
//	        } catch (ServletException e) {
//	            e.printStackTrace();
//	        }
//
//	        // 프론트엔드 콜백 페이지로 리다이렉트 + 메시지 전달
//	        try {
//	            response.sendRedirect(oauthFailAlreadyLoginRedirectFrontendUrl);
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//
//	        // 메서드 중단
//	        return null;
//	    }
//    	
//    	// OAuth2AuthenticationToken으로 다운캐스팅
//        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//
//        // 어느 Provider(google/kakao)인지 확인
//        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
//        
//        // 인증 후 principal 정보(소셜 프로필 정보 꺼내기)
//        OAuth2User oAuth2User = oauthToken.getPrincipal();
//        
//        // userEntity를 가져오거나 없으면 가입 처리(DB 매핑용 사용자 엔티티)
//        AuthUser userEntity;
//        
//        String nickname = null;
//        String userEmail = null;
//        
//        // ************************
//        // (A) 구글 로그인 처리
//        // ************************
//        // 구글은 보통 "sub"라는 고유 ID가 있음 / 구글 계정마다 변하지 않는 유니크 ID
//        // authentication.getName() => sub 값이 들어오거나 이메일이 들어올 수도 있음 (설정에 따라 다름)
//        if (SOCIAL_GOOGLE.equals(registrationId)) {
//
//            String googleSubject = oAuth2User.getAttribute("sub");		// e.g. "1175547227..."
//            nickname = (String) oAuth2User.getAttributes().get("name");	// 예) 구글 계정 이름
//            userEmail = oAuth2User.getAttribute("email");				// e.g. "xxxxx@gmail.com"
//
//            // 소셜 가입이 있는지 확인
//            Optional<AuthSocialLogin> optionalSocialUser
//            	= authSocialLoginRepository.findBySocialNameAndSocialUserId(SOCIAL_GOOGLE, googleSubject);
//            
//            if (optionalSocialUser.isPresent()) {
//            	// DB에서 유저 찾기
//            	Optional<AuthUser> optionalUser
//            		= authUsersRepository.findById(optionalSocialUser.get().getUserId());
//
//            	try {
//            		userEntity = optionalUser.get();
//				} catch (Exception e) {
//					e.printStackTrace();
//					return "redirect:" + oauthFailRedirectFrontendUrl + UrlEncodingUtil.encode("소셜 정보와 매칭되는 사용자가 없습니다");
//				}
//            } else {
//                return "redirect:" + oauthSocialNoSearchRedirectFrontendUrl
//                		+ "&encrypt=" + 
//                		SocialLoginCryptoUtil.oauthDataJsonEncrypt(registrationId, new String[]{googleSubject, nickname, userEmail});
//            }
//
//            System.out.println("\n\n===============================");
//            System.out.println("OAuth2 구글 로그인 성공!");
//            System.out.println("googleSubject : " + googleSubject);
//            System.out.println("email : " + userEmail);
//            System.out.println("nickname : " + nickname);
//            System.out.println("===============================\n\n");
//            
//            // 필요 시 userEntity에 googleSubject 필드를 추가로 저장할 수도 있음
//
//            
//        // ************************
//        // (B) 카카오 로그인 처리
//        // ************************
//        } else if (SOCIAL_KAKAO.equals(registrationId)) {
//            
//        	// 카카오는 oAuth2User.getAttributes() 구조가 조금 다름
//        	// {id=1234567890, kakao_account={..., email=...}, properties={nickname=...} ...}
//        	Map<String, Object> attributes = oAuth2User.getAttributes();
//        	
//            // 카카오 고유 id (Long형이거나 String으로 변환)
//            String kakaoId = String.valueOf(attributes.get("id"));
//            
//            // 카카오 계정 정보
//            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
//            if (kakaoAccount != null) {
//                userEmail = (String) kakaoAccount.get("email"); // 사용자가 동의 필수
//            }
//            
//            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
//            if (properties != null) {
//                nickname = (String) properties.get("nickname");
//            }
//
//            // 소셜 가입이 있는지 확인
//            Optional<AuthSocialLogin> optionalSocialUser
//            	= authSocialLoginRepository.findBySocialNameAndSocialUserId(SOCIAL_KAKAO, kakaoId);
//
//            if (optionalSocialUser.isPresent()) {
//            	// DB에서 유저 찾기
//            	Optional<AuthUser> optionalUser
//            		= authUsersRepository.findById(optionalSocialUser.get().getUserId());
//            	
//            	try {
//            		userEntity = optionalUser.get();
//				} catch (Exception e) {
//					e.printStackTrace();
//					return "redirect:" + oauthFailRedirectFrontendUrl + UrlEncodingUtil.encode("소셜 정보와 매칭되는 사용자가 없습니다");
//				}
//            } else {
//                return "redirect:" + oauthSocialNoSearchRedirectFrontendUrl
//                		+ "&encrypt=" + 
//                		SocialLoginCryptoUtil.oauthDataJsonEncrypt(registrationId, new String[]{kakaoId, userEmail, nickname});
//            }
//
//            System.out.println("\n\n===============================");
//            System.out.println("OAuth2 카카오 로그인 성공!");
//            System.out.println("email : " + userEmail);
//            System.out.println("nickname : " + nickname);
//            System.out.println("===============================\n\n");
//            
//        } else {
//            // 그 외 다른 Provider면 에러 처리 or 확장 가능
//            return "알 수 없는 소셜 로그인입니다. provider=" + registrationId;
//        }
//        
//        // JWT 발급
//        String jwt = jwtService.createToken(userEntity);
//        
//        // 콘솔에 찍어 확인
//        System.out.println("\n\n===============================");
//        System.out.println("OAuth2 Login JWT: " + jwt);
//        System.out.println("===============================\n\n");
//
//        // HttpOnly 쿠키 설정 (HTTPS 환경이라면 cookie.setSecure(true); 추가)
//        Cookie jwtCookie = new Cookie("jwt", jwt);
//        jwtCookie.setHttpOnly(true);
//        // FIXME : 추후 https에서 하면 true로 변경해줘야함
//        jwtCookie.setSecure(false);         // HTTPS 사용 시 활성화
//        jwtCookie.setPath("/");            // 전체 애플리케이션에서 접근 가능하도록 경로 설정
//        jwtCookie.setMaxAge(jwtMaxAge);         // 예: 1시간(초 단위) 동안 유효
//        
//        response.addCookie(jwtCookie);
//
//        // 최종적으로 클라이언트 웹 (예: http://localhost:8088/success)으로 리다이렉트
////        return "OAuth2 로그인 성공! 발급된 JWT: " + jwt;
//        return "redirect:" + oauthSuccessRedirectFrontendUrl;
//    }
//    
//    
//    /**
//     * 로그인 성공 후, JWT Token으로 사용자 정보 확인 (테스트용)
//     *
//     * @param authentication Spring Security가 제공하는 Authentication 객체
//     * @return 생성된 JWT 토큰 문자열
//     */
//    @GetMapping("/token")
//    @ResponseBody
//    public String getToken(Authentication authentication) {
//        // 현재 인증된 사용자 이름(username) 가져오기
//        String userEmail = authentication.getName();
//
//        // JWT 토큰을 담을 변수
//        String jwt;
//        try {
//            // DB에서 AuthUsersEntity 조회 (username이 email이라면 이메일 컬럼 기준으로 수정)
//        	AuthUser userEntity = authUsersRepository
//            		.findByEmail(userEmail)
//            		.orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "User not found in DB"));
//
//            // JWT 생성
//            jwt = jwtService.createToken(userEntity);
//        } catch (Exception e) {
//            // 사용자 정보가 없거나 기타 예외가 발생하면 에러 메시지 반환
//            return "User not found in DB";
//        }
//
//        // JWT Claims 파싱
//        Claims claims = Jwts.parserBuilder()
//            .setSigningKey(secretKey.getBytes())
//            .build()
//            .parseClaimsJws(jwt)
//            .getBody();
//
//        // 콘솔에 출력(테스트용)
//        System.out.println("OAuth2 Token Generated: " + jwt);
//        System.out.println("Claims Subject: " + claims.getSubject());
//
//        // 생성된 JWT 토큰을 그대로 반환 → 브라우저나 API 클라이언트에서 확인 가능
//        return jwt;
//    }
//    
//
//    /**
//     * 로그인 된 사용자의 정보를 반환하는 API
//     * - HttpOnly 쿠키에 담긴 JWT를 파싱하여 username과 roles를 꺼냄
//     */
//    @GetMapping("/me")
//    @ResponseBody
//    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
//        // (1) 쿠키에서 JWT 토큰 찾기
//        Cookie[] cookies = request.getCookies();
//        if (cookies == null) {
//        	throw new BadCredentialsException(GlobalExceptionHandler.CC + "쿠키가 없습니다. 로그인 필요");
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
//        	throw new BadCredentialsException(GlobalExceptionHandler.CC + "JWT 쿠키가 없습니다. 로그인 필요");
//        }
//
//        // (2) JWT 검증
//        if (!jwtService.validateToken(token)) {
//        	throw new BadCredentialsException(GlobalExceptionHandler.CC + "JWT가 유효하지 않습니다.");
//        }
//
//        // (3) JWT에서 사용자 정보(roles 등) 파싱
//        int userId = jwtService.getUserId(token);
//        String userEmail = jwtService.getUserEmail(token);
//        String username = jwtService.getUsername(token);
////        String profileImgUrl = jwtService.getProfileImgUrl(token);
//        List<String> roles = jwtService.getRoles(token); // ["ROLE_ADMIN", "ROLE_USER", ...]
//        
//
//        // (4) 필요한 정보를 DTO 형태로 만들어 반환
//        Map<String, Object> responseBody = new HashMap<>();
//        responseBody.put("userId", userId);
//        responseBody.put("userEmail", userEmail);
//        responseBody.put("username", username);
//        responseBody.put("roles", roles);
////        responseBody.put("userProfileImgUrl", profileImgUrl);
//
//        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
//    }
//
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
//        public void setUserEmail(String userEmail) {this.userEmail = userEmail;}
//        public String getPassword() { return password; }
//        public void setPassword(String password) { this.password = password; }
//		public String getEncryptData() {return encryptData;}
//		public void setEncryptData(String encryptData) {this.encryptData = encryptData;}
//    }
//}




