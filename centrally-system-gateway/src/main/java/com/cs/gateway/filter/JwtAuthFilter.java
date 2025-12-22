package com.cs.gateway.filter;

import com.cs.gateway.constants.JwtConstants;
import com.cs.gateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.*;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * 게이트웨이(WebFlux)에서 쿠키 기반 JWT 검증 필터
 *	- 정상적으로 JWT 검증이 완료되면 헤더에 유저의 이메일과 권한을 헤더에 포함하여 마이크로서비스로 전달 
 *	- JWT가 유효하면, ReactiveSecurityContextHolder 에 인증 객체를 넣어
 *	  GatewaySecurityConfig 의 `.anyExchange().authenticated()`를 통과하게 만든다.
 */
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private static final Logger log = Loggers.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    
    // CORS 설정을 재사용하기 위해 주입받음
    private final CorsConfiguration corsConfiguration;

//    @Value("${jwt.excluded-paths}")
//    private String[] jwtExcludedPaths;
    private final String[] jwtExcludedPaths;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // 인증이 필요 없는 경로는 바로 통과
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
		/**
		 * (A) JwtAuthFilter
		 * 		if (OPTIONS) return chain.filter(...) 필터 자체가 OPTIONS 요청에
		 * 		대해 쿠키 검사를 하지 않고, 401을 리턴하지 않도록 하는 조치. 그렇지 않으면 “No JWT Cookie”로 바로 401 발생
		 * 		 → 브라우저가 CORS 에러 발생. 
		 * (B) SecurityWebFilterChain(GatewaySecurityConfig)
		 * 		.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() Spring Security WebFlux는
		 * 		요청이 필터를 통과한 뒤에도 인가(Authorization) 규칙을 확인. 만약 OPTIONS 요청을 permitAll로 설정하지 않으면,
		 * 		“Authentication required” → 또 다른 401/403 → 브라우저는 CORS 에러로 인식.
		 */
        // 만약 OPTIONS면 바로 체인 통과
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 쿠키에서 "jwt" 토큰 추출
        String jwtToken = extractJwtFromCookie(exchange.getRequest());
        if (!StringUtils.hasText(jwtToken)) {
            return onError(exchange, "No JWT Cookie", HttpStatus.UNAUTHORIZED);
        }

        // JWT 검증
        if (!jwtService.validateToken(jwtToken)) {
            return onError(exchange, "Invalid JWT", HttpStatus.UNAUTHORIZED);
        }

        // userEmail, roles 추출
        String userEmail = jwtService.getUserEmail(jwtToken);
        String username = jwtService.getUsername(jwtToken);
        List<String> roles = jwtService.getRoles(jwtToken);
        Integer userId = jwtService.getClaim(jwtToken, "userId", Integer.class);
        String phoneNumber = jwtService.getClaim(jwtToken, "phoneNumber", String.class);
        String birth = jwtService.getClaim(jwtToken, "birth", String.class);
        String address = jwtService.getClaim(jwtToken, "address", String.class);
        Integer teamId = jwtService.getClaim(jwtToken, "teamId", Integer.class);
        Integer positionId = jwtService.getClaim(jwtToken, "positionId", Integer.class);
        Integer empTypeId    = jwtService.getClaim(jwtToken, "employmentTypeId", Integer.class);

        log.info(">>> Gateway JWT Cookie 검증 성공 - userEmail={}, username={}, roles={}", userEmail, username, roles);


        /**
         * Spring WebFlux에서 Security Context(인증 정보 저장소)를 유지하려면,
         * Spring MVC(SecurityContextHolder) 방식이 아니라 WebFlux 전용 방식으로 처리
         */
        //  WebFlux SecurityContext에 인증 정보 넣기
        //	Authentication 객체 생성
        var authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

        // SecurityContext 생성
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
    }

    /**
     * 인증 없이 접근 가능한 경로 정의
     */
    private boolean isPublicPath(String path) {
        if (jwtExcludedPaths != null) {
            for (String excluded : jwtExcludedPaths) {
                // "/**"로 끝나면 prefix만 추출
                String prefix = excluded.endsWith("/**")
                        ? excluded.substring(0, excluded.length() - 3)
                        : excluded;

                if (path.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 쿠키에서 "jwt" 라는 이름의 값을 추출
     */
    public static String extractJwtFromCookie(ServerHttpRequest request) {
        List<HttpCookie> jwtCookies = request.getCookies().get(JwtConstants.JWT_TOKEN_NAME);
        if (CollectionUtils.isEmpty(jwtCookies)) {
            return null;
        }
        // 동일 이름 쿠키가 여러 개라면 첫 번째만 사용
        return jwtCookies.get(0).getValue();
    }

    /**
     * 에러 응답 처리
     * - CORS 설정 안하고 바로 Vue로 넘어가서 401 에러로 잡기 어려움
     */
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        log.error("[JwtAuthFilter] {}", err);

        var response = exchange.getResponse();
        response.setStatusCode(status);

        // 실제 요청 헤더에서 Origin 추출
        String requestOrigin = exchange.getRequest().getHeaders().getFirst("Origin");

        // 1) 허용 가능한 Origin 목록
        List<String> allowedOrigins = corsConfiguration.getAllowedOrigins();

        // 2) 요청 Origin이 null이 아니고, 허용 목록에 포함되어 있다면 반영
        if (StringUtils.hasText(requestOrigin)
                && allowedOrigins != null
                && allowedOrigins.contains(requestOrigin)) {
            response.getHeaders().add("Access-Control-Allow-Origin", requestOrigin);
        }

        // CORS Credential 허용 여부
        if (Boolean.TRUE.equals(corsConfiguration.getAllowCredentials())) {
            response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        }

        // 허용 메서드
        if (corsConfiguration.getAllowedMethods() != null && !corsConfiguration.getAllowedMethods().isEmpty()) {
            String methods = String.join(", ", corsConfiguration.getAllowedMethods());
            response.getHeaders().add("Access-Control-Allow-Methods", methods);
        }

        // 허용 헤더
        if (corsConfiguration.getAllowedHeaders() != null && !corsConfiguration.getAllowedHeaders().isEmpty()) {
            String headers = String.join(", ", corsConfiguration.getAllowedHeaders());
            response.getHeaders().add("Access-Control-Allow-Headers", headers);
        }

        // 바디 없이 에러 코드만 내려줌
        return response.setComplete();
    }

}
