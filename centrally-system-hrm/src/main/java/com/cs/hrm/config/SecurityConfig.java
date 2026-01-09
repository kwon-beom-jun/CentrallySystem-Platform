package com.cs.hrm.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cs.core.filter.JwtAuthenticationFilter;
import com.cs.core.handler.JsonErrorHandlers;
import com.cs.hrm.filter.RoleAuthorizationFilter;
import com.cs.hrm.filter.RoleAuthorizationFilter.ExcludeEntry;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public RoleAuthorizationFilter roleAuthorizationFilter(
            @Value("${hrm.roles}")            String hrmRoles,
            @Value("${hrm.role.exclude.url:}") String excludeUrls,
            AccessDeniedHandler jsonAccessDeniedHandler) {

        List<String> roles = Arrays.stream(hrmRoles.split(","))
                                   .map(String::trim)
                                   .filter(s -> !s.isEmpty())
                                   .toList();

        List<ExcludeEntry> excludes = Arrays.stream(excludeUrls.split(","))
                                            .map(String::trim)
                                            .filter(s -> !s.isEmpty())
                                            .map(RoleAuthorizationFilter.ExcludeEntry::new)
                                            .toList();

        return new RoleAuthorizationFilter(roles, excludes, jsonAccessDeniedHandler);
    }
    

	@Bean
	public AccessDeniedHandler jsonAccessDeniedHandler() {
	    return new JsonErrorHandlers.JsonAccessDenied();
	}
    
    /**
     * BCrypt PasswordEncoder Bean
     *  - HRM 마이크로서비스에서도 비밀번호 암호화를 동일한 로직으로 사용하기 위해 추가
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
    /**
     * SecurityFilterChain 빈을 등록
     * 게이트웨이에서 이미 인증/인가가 완료되었다고 가정하고,
     * 내부 서비스에서는 게이트웨이가 전달한 헤더만 확인하여 인증 정보를 설정함.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
    									   RoleAuthorizationFilter roleAuthorizationFilter,
    							           AccessDeniedHandler jsonAccessDeniedHandler) 
										   throws Exception {

        http
	        // 1) CORS 설정 적용
        	//		Gateway가 적용되어있으므로 Access-Control-Allow-Origin 헤더가 둘 이상 있으면 에러
        	//		“The 'Access-Control-Allow-Origin' header contains multiple values” 에러가 발생
        	.cors(cors -> cors.disable())
        
            // 2) CSRF 보호 비활성화 (필요에 따라 활성화 가능)
            .csrf(csrf -> csrf.disable())

            // 3) 요청에 대한 인가 정책 설정
            .authorizeHttpRequests(auth -> auth
                // "/public/**" 경로는 인증 없이 접근 허용
                .requestMatchers("/public/**").permitAll()
                .requestMatchers(HttpMethod.POST  , "/users").permitAll()
                // Actuator Prometheus 엔드포인트 허용 (모니터링용)
                .requestMatchers("/actuator/prometheus", "/actuator/health", "/actuator/info").permitAll()
                // 프로필 이미지 제외
//                .requestMatchers("/upload/profile/**").permitAll()
//                .requestMatchers(HttpMethod.PATCH , "/users/*").permitAll()
//                .requestMatchers(HttpMethod.DELETE, "/users/*").permitAll()
                // 그 외의 경로는 인증 필요
                .anyRequest().authenticated()
            )

            // 4) 게이트웨이 필터를 UsernamePasswordAuthenticationFilter 이전에 실행하도록 설정
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        	.addFilterAfter(roleAuthorizationFilter, JwtAuthenticationFilter.class)
        
            .exceptionHandling(h -> h
                .authenticationEntryPoint(new JsonErrorHandlers.JsonAuthEntryPoint())
                .accessDeniedHandler(jsonAccessDeniedHandler)
            );
        
        // 5) SecurityFilterChain 반환
        return http.build();
    }

    /**
     * AuthenticationManager 빈을 등록
     * (DB나 DaoAuthenticationProvider를 사용하지 않을 경우,
     *  단순히 getAuthenticationManager()만 쓰면 됨)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
