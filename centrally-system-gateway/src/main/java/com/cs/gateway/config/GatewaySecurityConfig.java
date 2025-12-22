package com.cs.gateway.config;

//import com.cs.gateway.filter.ActivityLogFilter;
import com.cs.gateway.filter.JwtAuthFilter;
import com.cs.gateway.filter.RoleChangeValidationFilter;
import com.cs.gateway.service.JwtService;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import reactor.core.publisher.Mono;

/**
 * Spring Cloud Gateway + WebFlux Security 설정
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class GatewaySecurityConfig {

//    private final ActivityLogFilter activityLogFilter;
    private final JwtService jwtService;
    
    // Spring에서는 콤마(,)로 구분된 문자열을 자동으로 배열로 변환할 수 있으므로, @Value 어노테이션에 String[] 타입을 사용하면 됨
	// jwt.excluded-paths 값을 배열로 주입받음 (콤마로 구분되어 자동 변환됨)
    @Value("${jwt.excluded-paths}")
    private String[] jwtExcludedPaths;
    
    @Value("${cors.domains}")
    private String[] corsDomains;
    
   /**
    * CORS 설정을 담은 CorsConfiguration Bean
    */
   @Bean
   public CorsConfiguration corsConfiguration() {
       CorsConfiguration config = new CorsConfiguration();
       // 공백 제거 후 등록
       config.setAllowedOrigins(
           Arrays.stream(corsDomains)
                 .map(String::trim)
                 .toList()
       );
       // 허용할 도메인
       config.setAllowedOrigins(Arrays.asList(corsDomains));
       // 허용할 메서드
       config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
       // 허용할 헤더
       config.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "X-Func-Vue", "X-Menu-Vue", "X-Reason"));
       // 브라우저에 노출할 헤더
       // Content-Disposition : 엑셀 다운로드 헤더
       config.setExposedHeaders(Arrays.asList("X-Reason", "Content-Disposition"));
       // 인증정보(쿠키 등) 전송 허용
       config.setAllowCredentials(true);

       return config;
   }

    /**
     * [중요] 리액티브 환경에서 CORS를 처리하는 CorsWebFilter Bean
     */
    @Bean
    public CorsWebFilter corsWebFilter(CorsConfiguration corsConfiguration) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로(**)에 대해 CORS 설정 적용
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
    
    /**
     * JwtAuthFilter Bean 등록
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter(CorsConfiguration corsConfiguration) {
        // 여기서 필수 의존성들을 생성자 파라미터로 전달
        return new JwtAuthFilter(jwtService, corsConfiguration, jwtExcludedPaths);
    }

    /**
     * SecurityWebFilterChain 설정
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
    		ServerHttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            RoleChangeValidationFilter roleChangeValidationFilter) {

        http
        
            // CORS 설정은 CorsWebFilter를 통해 처리 → 여기서는 특별히 추가 설정 없어도 됨
            // 필요하다면 .cors(cors -> {}) 정도로만 사용 (disable()도 가능)
        	.cors(cors -> {})

        	// CSRF 비활성화 (API 서버인 경우 주로 비활성화)
            .csrf(csrf -> csrf.disable())
            
            // HTTP Basic 비활성화
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            
            // Form Login 비활성화
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

            // 인증/인가 규칙
            .authorizeExchange(exchanges -> exchanges
                // 인증 없이 허용할 경로 설정 예시
//                .pathMatchers(
//                		"/auth/**", 
//                		"/login/**", 
//                		"/public/**", 
//                		"/centrally-system-service-test").permitAll()
            	
        		/**
        		 * (A) JwtAuthFilter
        		 * 		if (OPTIONS) return chain.filter(...) 필터 자체가 OPTIONS 요청에
        		 * 		대해 쿠키 검사를 하지 않고, 401을 리턴하지 않도록 하는 조치. 그렇지 않으면 "No JWT Cookie"로 바로 401 발생
        		 * 		 → 브라우저가 CORS 에러 발생. 
        		 * (B) SecurityWebFilterChain(GatewaySecurityConfig)
        		 * 		.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() Spring Security WebFlux는
        		 * 		요청이 필터를 통과한 뒤에도 인가(Authorization) 규칙을 확인. 만약 OPTIONS 요청을 permitAll로 설정하지 않으면,
        		 * 		"Authentication required" → 또 다른 401/403 → 브라우저는 CORS 에러로 인식.
        		 */
        		// WebFlux에서는 exchanges.pathMatchers(...) 형태로 HTTP 메서드 + 경로를 함께 지정
        		// Security 레벨에서 여전히 OPTIONS 요청을 인증받으려 하며, 결과적으로 CORS 에러가 날 수 있음
        		.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        		
        		// Actuator Prometheus 엔드포인트 허용 (모니터링용) - WebFlux 경로 매칭
        		.pathMatchers("/actuator/prometheus", "/actuator/health", "/actuator/info").permitAll()
        		
        		// jwt.excluded-paths 값(배열)을 그대로 사용하여 인증 없이 허용할 경로 설정
                .pathMatchers(jwtExcludedPaths).permitAll()
                
                // 그 외 경로는 인증 필요
                .anyExchange().authenticated()
            )

            // 인증 실패 시 401 반환
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((exchange, ex) ->
                    Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED))
                )
            )

            // JWT 필터를 Authentication 필터 *이전*에 배치
            .addFilterBefore(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        
            // 실시간 권한 변동 체크
        	.addFilterAfter(roleChangeValidationFilter, SecurityWebFiltersOrder.AUTHENTICATION);

            // ActivityLogFilter를 Authentication 필터 *이후*에 배치
//            .addFilterAfter(activityLogFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }
}
