//package com.cs.core.filter;
//
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.util.StringUtils;
//
//import com.cs.core.security.CustomUserPrincipal;
//
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * 		 
// * 		 
// * 		 
// * FIXME : 게이트웨이 서버와 마이크로 서버가 아예 달라지면(도메인 자체가 달라지면)
// * 		 JWT 토큰이 제대로 가지 않을수도 있음
// * 		 쿠키를 발급할 때 Domain 속성을 명시적으로 설정해야 다른 서브도메인에서 접근 가능
// * 		 만약 Domain 속성을 설정하지 않으면, 쿠키는 기본적으로 발급한 도메인(auth.example.com)에 한정되서 남겨둠
// * 		 여러 서버가 서로 다른 서브도메인에 있더라도 HttpOnly 쿠키를 공유하려면, 쿠키를 설정할 때 Domain 속성을 공통 상위 도메인(예: ".example.com")으로 지정
// * 		 
// * 		 AuthLoginController, CustomJwtAuthenticationSuccessHandler 에다가 설정
// * 		 Set-Cookie: jwt=your_jwt_token; Domain=.example.com; Path=/; HttpOnly; Secure; SameSite=None
// * 		 
// * 		 
// * 		 
// * 게이트웨이가 X-User-Id, X-User-Roles 헤더를 붙여줬다고 가정하고,
// * 이를 읽어 Spring Security 인증 객체를 세팅하는 필터
// */
//public class GatewayHeaderAuthFilter implements Filter {
//
//    @Override
//    public void doFilter(ServletRequest request, 
//                         ServletResponse response, 
//                         FilterChain chain)
//            throws IOException, ServletException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//
//        // 1) 헤더에서 정보 추출
//        String userEmail = httpRequest.getHeader("X-User-Email");
//        String username  = httpRequest.getHeader("X-User-Name");
//        String rolesHeader = httpRequest.getHeader("X-User-Roles");
//        String userIdHeader = httpRequest.getHeader("X-User-Id");
//        String phoneNumber  = httpRequest.getHeader("X-User-PhoneNumber");
//        String birth        = httpRequest.getHeader("X-User-Birth");
//        String address      = httpRequest.getHeader("X-User-Address");
//        String teamId      = httpRequest.getHeader("X-User-TeamId");
//        String positionId      = httpRequest.getHeader("X-User-PositionId");
//        String empTypeId      = httpRequest.getHeader("X-User-EmpTypeId");
//
//        // 2) 둘 다 존재하면 → 인증된 사용자로 처리
//        if (StringUtils.hasText(userEmail) && StringUtils.hasText(rolesHeader)) {
//        	
//        	int userId = Integer.parseInt(userIdHeader);
//        	
//            // 예: "ROLE_USER,ROLE_MANAGER"
//            String[] roleArray = rolesHeader.split(",");
//            List<SimpleGrantedAuthority> authorities = Arrays.stream(roleArray)
//                    .map(String::trim)
//                    .map(SimpleGrantedAuthority::new)
//                    .collect(Collectors.toList());
//            
//            // CustomUserPrincipal 생성 (이메일, 사용자 이름, 권한)
//            CustomUserPrincipal principal = new CustomUserPrincipal(
//                    userEmail, 
//                    username, 
//                    userId,
//                    phoneNumber,   // 새로 추가
//                    birth,         // 새로 추가
//                    address,       // 새로 추가
//                    authorities
//            );
//
//            
//            // 3) Spring Security 인증 객체 생성
//            UsernamePasswordAuthenticationToken authentication =
//            	    new UsernamePasswordAuthenticationToken(principal, null, authorities);
//
//            // 4) SecurityContext 에 저장
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        // 다음 필터로 진행
//        chain.doFilter(request, response);
//    }
//}
