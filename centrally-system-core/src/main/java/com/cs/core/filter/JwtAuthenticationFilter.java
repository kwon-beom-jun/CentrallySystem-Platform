package com.cs.core.filter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cs.core.constants.JwtConstants;
import com.cs.core.security.CustomUserPrincipal;
import com.cs.core.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// JWT 쿠키를 검증하는 OncePerRequestFilter
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	/**
	 *  HTTP 요청 시 쿠키에서 JWT 토큰을 읽어와서 검증한 후, 
	 *  토큰이 유효하면 사용자 이름과 권한 정보를 추출하여 
	 *  Spring Security의 SecurityContextHolder에 Authentication 객체를 설정
	 */
	@Override
	protected void doFilterInternal(
			HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain filterChain)
			throws ServletException, IOException {
		
		// 1) 쿠키에서 "jwt" 가져오기
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (JwtConstants.JWT_TOKEN_NAME.equals(c.getName())) {
					String token = c.getValue();
					// 2) 토큰 검증
					if (jwtService.validateToken(token)) {
						
						// 3) username/roles 등 Jwt에서 꺼내 인증 생성
				        String userEmail = jwtService.getUserEmail(token);
				        String username = jwtService.getUsername(token);
				        List<String> roles = jwtService.getRoles(token);
				        Integer userId = jwtService.getClaim(token, "userId", Integer.class);
						
				        // 둘 다 존재하면 → 인증된 사용자로 처리
				        // 권한은 각 마이크로 서비스마다 체크하는것으로 수정
//				        if (StringUtils.hasText(userEmail) && roles != null && roles.size() != 0) {
			        	if (StringUtils.hasText(userEmail)) {
				        	
				        	// FIXME : 추후 경로에 따라서 여기서 권한 체크?
				            // 예: "ROLE_USER,ROLE_MANAGER"
				            List<SimpleGrantedAuthority> authorities = roles.stream()
				                    .map(String::trim)
				                    .map(SimpleGrantedAuthority::new)
				                    .collect(Collectors.toList());
				            
				            // CustomUserPrincipal 생성 (이메일, 사용자 이름, 권한)
				            CustomUserPrincipal principal = new CustomUserPrincipal(
				                    userEmail, 
				                    username, 
				                    userId,
				                    authorities
				            );

				            
				            // Spring Security 인증 객체 생성
				            UsernamePasswordAuthenticationToken authentication =
				            	    new UsernamePasswordAuthenticationToken(principal, null, authorities);

				            // SecurityContext 에 저장
				            SecurityContextHolder.getContext().setAuthentication(authentication);
				        }
					}
					break;
				}
			}
		}
		// 다음 필터로 진행
		filterChain.doFilter(request, response);
	}
}
