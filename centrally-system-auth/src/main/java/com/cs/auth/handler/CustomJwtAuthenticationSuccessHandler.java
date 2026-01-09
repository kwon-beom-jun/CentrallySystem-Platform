package com.cs.auth.handler;

import com.cs.auth.entity.AuthUser;
import com.cs.auth.repository.AuthUserRepository;
import com.cs.auth.service.AuthJwtService;
import com.cs.core.constants.JwtConstants;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 일반 로그인 성공 시
 * - 여기서 JWT 토큰을 생성하여 콘솔에 찍어줍니다.
 */
@Component
public class CustomJwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthJwtService jwtService;
    private final AuthUserRepository authUsersRepository;
    
    // 프론트엔드 리다이렉트 URL (프로퍼티 파일에서 주입)
    @Value("${oauth.success.redirect.frontend-url}")
    private String oauthSuccessRedirectFrontendUrl;
    
    @Value("${jwt.expiration}")
    private int jwtMaxAge;

    public CustomJwtAuthenticationSuccessHandler(AuthJwtService jwtService, AuthUserRepository authUsersRepository) {
        this.jwtService = jwtService;
        this.authUsersRepository = authUsersRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication)
                                        throws IOException, ServletException {
        // 현재 인증된 사용자 이름 가져오기
        String email = authentication.getName();

//      AuthUsersEntity userEntity = authUsersRepository
//        		.findByUsername(username)
//      		.orElseThrow(() -> new RuntimeException("User not found in DB"));
        AuthUser userEntity = null;
        Optional<AuthUser> userOptional = authUsersRepository.findByEmail(email);
    	if (userOptional.isPresent()) {
            userEntity = userOptional.get();
        } else {
            // FIXME : 유저가 없으면 에러 발생 로직 수정
            System.out.println("No Search User");
            return;
        }

        // JWT 발급
        String jwt = jwtService.createToken(userEntity);
        System.out.println("JWT Generated on Success: " + jwt);

        // HttpOnly 쿠키에 JWT 저장 (HTTPS 환경이면 setSecure(true) 사용)
        Cookie jwtCookie = new Cookie(JwtConstants.JWT_TOKEN_NAME, jwt);
        jwtCookie.setHttpOnly(true);
        // FIXME : 추후 https에서 하면 true로 변경해줘야함
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(jwtMaxAge); // 1시간(초 단위) 동안 유효
        
//        SameSite
//	        Strict
//	        동일 사이트에서만 쿠키를 전송합니다.
//	        → 외부 사이트에서 링크 클릭 등으로 진입하면 쿠키가 전송되지 않아, CSRF 공격에 대한 보호 효과가 큽니다.
//	
//	        Lax
//	        기본적으로는 동일 사이트에서만 전송되지만, 일부 안전한 HTTP 메서드(예: GET)로 이루어진 크로스 사이트 요청에서는 쿠키가 전송될 수 있습니다.
//	        → 대부분의 일반적인 내비게이션 요청에는 쿠키가 포함되지만, POST나 기타 민감한 요청에는 전송되지 않습니다.
//	
//	        None
//	        쿠키가 크로스 사이트 요청에도 항상 전송되도록 합니다.
//	        → 즉, 출처(Origin)가 달라도 쿠키가 요청 헤더에 포함되어 전달됩니다.
//        - addCokie와 동시에 사용시 쿠키가 2개 생기는 상황이 발생
//        - 또한 쿠키 제거시 설정과 동일한게 삭제되야하므로 해당것은 주석처리함
//        response.setHeader(
//        	    "Set-Cookie",
//        	    String.format(
//        	    		"jwt=%s; "
//        	    	  + "Max-Age=%d; "
//        	    	  + "Path=/; "
//        	    	  + "HttpOnly; "
//        	    	  + "Secure=%s; "
//        	    	  + "SameSite=None", // 크로스 사이트 요청 시 전송되는 방식을 제어
//        	    	  
//        	        jwt, jwtMaxAge, "false")
//        	);
        
        response.addCookie(jwtCookie);
    }
}
