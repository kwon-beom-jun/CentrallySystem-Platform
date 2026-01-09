package com.cs.info.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import com.cs.info.filter.RoleAuthorizationFilter;

import lombok.RequiredArgsConstructor;

/**
 * INFO 서비스 보안 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * INFO 서비스 권한 필터 빈 생성
     */
    @Bean
    public RoleAuthorizationFilter roleAuthorizationFilter(
            @Value("${info.roles}")            String infoRoles,
            @Value("${info.role.exclude.url:}") String excludeUrls,
            AccessDeniedHandler jsonAccessDeniedHandler) {

        List<String> roles = Arrays.stream(infoRoles.split(","))
                                   .map(String::trim)
                                   .filter(s -> !s.isEmpty())
                                   .toList();

        List<RoleAuthorizationFilter.ExcludeEntry> excludes = Arrays.stream(excludeUrls.split(","))
                                            .map(String::trim)
                                            .filter(s -> !s.isEmpty())
                                            .map(RoleAuthorizationFilter.ExcludeEntry::new)
                                            .toList();

        return new RoleAuthorizationFilter(roles, excludes, jsonAccessDeniedHandler);
    }

    /**
     * JSON 형식 Access Denied Handler
     */
    @Bean
    public AccessDeniedHandler jsonAccessDeniedHandler() {
        return new JsonErrorHandlers.JsonAccessDenied();
    }

    /**
     * BCrypt PasswordEncoder Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SecurityFilterChain 빈 등록
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           RoleAuthorizationFilter roleAuthorizationFilter,
                                           AccessDeniedHandler jsonAccessDeniedHandler) 
                                           throws Exception {

        http
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                // Actuator Prometheus 엔드포인트 허용 (모니터링용)
                .requestMatchers("/actuator/prometheus", "/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(roleAuthorizationFilter, JwtAuthenticationFilter.class)
            .exceptionHandling(h -> h
                .authenticationEntryPoint(new JsonErrorHandlers.JsonAuthEntryPoint())
                .accessDeniedHandler(jsonAccessDeniedHandler)
            );

        return http.build();
    }

    /**
     * AuthenticationManager 빈 등록
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

