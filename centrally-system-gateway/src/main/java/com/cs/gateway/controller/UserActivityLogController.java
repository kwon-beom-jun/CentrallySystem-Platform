package com.cs.gateway.controller;

import com.cs.gateway.constants.JwtConstants;
import com.cs.gateway.dao.dto.ActivityLogPage;
import com.cs.gateway.entity.UserActivityLog;
import com.cs.gateway.service.JwtService;
import com.cs.gateway.service.UserActivityLogService;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;
import java.util.List;

/**
 * 웹플럭스 컨트롤러
 * - GET /activity-log
 * - 파라미터: startDate, endDate, searchData, currentPage
 */
@RestController
public class UserActivityLogController {

    private final UserActivityLogService userActivityLogService;
    private final JwtService jwtService;

    public UserActivityLogController(UserActivityLogService userActivityLogService, JwtService jwtService) {
        this.userActivityLogService = userActivityLogService;
        this.jwtService = jwtService;
    }

    /**
     * GET /activity-log
     * 파라미터: startDate(옵션), endDate(옵션), searchData(옵션), currentPage(기본=1)
     * 반환: ActivityLogPage (Mono)
     */
    @GetMapping("/activity-log")
    public Mono<ActivityLogPage> getsActivityLog(
        @RequestParam(name="startDate",   required=false) String startDate,
        @RequestParam(name="endDate",     required=false) String endDate,
        @RequestParam(name="searchData",  required=false) String searchData,
        @RequestParam(name="currentPage", defaultValue="1") int currentPage,
        @RequestParam(name="pageSize", defaultValue="10") int pageSize
    ) {
        return userActivityLogService.getActivityLogs(
            startDate,
            endDate,
            searchData,
            currentPage,
            pageSize
        );
    }

    /**
     * GET /activity-log/personal
     * 현재 로그인한 사용자의 개인 활동 이력 조회 (페이지네이션)
     * 
     * @param request     ServerHttpRequest (JWT 쿠키에서 사용자 정보 추출)
     * @param startDate   시작 날짜 (옵션)
     * @param endDate     종료 날짜 (옵션)
     * @param searchData  검색어 (옵션)
     * @param currentPage 현재 페이지 (기본값: 1)
     * @param pageSize    페이지 크기 (기본값: 10)
     * @return 개인 활동 이력 페이지
     */
    @GetMapping("/activity-log/personal")
    public Mono<ActivityLogPage> getPersonalHistory(
        ServerHttpRequest request,
        @RequestParam(name="startDate", required=false) String startDate,
        @RequestParam(name="endDate", required=false) String endDate,
        @RequestParam(name="searchData", required=false) String searchData,
        @RequestParam(name="currentPage", defaultValue="1") int currentPage,
        @RequestParam(name="pageSize", defaultValue="10") int pageSize
    ) {
        // 1. JWT 쿠키에서 사용자 이메일 추출
        String jwtToken = extractJwtFromCookie(request);
        if (jwtToken == null || !jwtService.validateToken(jwtToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다");
        }
        
        String jwtUserEmail = jwtService.getUserEmail(jwtToken);
        
        // 2. Spring Security Context에서 인증된 사용자 정보 추출 및 검증
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(authentication -> {
                // 인증되지 않은 경우
                if (authentication == null || !authentication.isAuthenticated()) {
                    throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, 
                        "인증되지 않은 사용자입니다"
                    );
                }
                
                // Principal에서 이메일 추출
                String authenticatedEmail = authentication.getName();
                
                // JWT의 이메일과 인증된 이메일이 일치하는지 검증
                if (!jwtUserEmail.equals(authenticatedEmail)) {
                    throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, 
                        "JWT 토큰의 사용자와 인증된 사용자가 일치하지 않습니다"
                    );
                }
                
                // 검증 통과 후 개인 활동 이력 조회
                return userActivityLogService.getPersonalHistory(
                    jwtUserEmail,
                    startDate,
                    endDate,
                    searchData,
                    currentPage,
                    pageSize
                );
            })
            .switchIfEmpty(
                // SecurityContext가 비어있는 경우
                Mono.error(new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, 
                    "인증 정보를 찾을 수 없습니다"
                ))
            );
    }

    /**
     * GET /activity-log/recent
     * 현재 로그인한 사용자의 최신 활동 이력 조회
     * 
     * @param request ServerHttpRequest (JWT 쿠키에서 사용자 정보 추출)
     * @param limit   조회할 최대 개수 (기본값: 5)
     * @return 최신 활동 이력 목록
     */
    @GetMapping("/activity-log/recent")
    public Mono<List<UserActivityLog>> getRecentActivities(
        ServerHttpRequest request,
        @RequestParam(name="limit", defaultValue="5") int limit
    ) {
        // 1. JWT 쿠키에서 사용자 이메일 추출
        String jwtToken = extractJwtFromCookie(request);
        if (jwtToken == null || !jwtService.validateToken(jwtToken)) {
            return Mono.just(List.of());
        }
        
        String jwtUserEmail = jwtService.getUserEmail(jwtToken);
        
        // 2. Spring Security Context에서 인증된 사용자 정보 추출 및 검증
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(authentication -> {
                // 인증되지 않은 경우
                if (authentication == null || !authentication.isAuthenticated()) {
                    throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, 
                        "인증되지 않은 사용자입니다"
                    );
                }
                
                // Principal에서 이메일 추출 (Principal이 이메일 문자열인 경우)
                String authenticatedEmail = authentication.getName();
                
                // JWT의 이메일과 인증된 이메일이 일치하는지 검증
                if (!jwtUserEmail.equals(authenticatedEmail)) {
                    throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, 
                        "JWT 토큰의 사용자와 인증된 사용자가 일치하지 않습니다"
                    );
                }
                
                // 검증 통과 후 활동 이력 조회
                return userActivityLogService.getRecentActivities(jwtUserEmail, limit);
            })
            .switchIfEmpty(
                // SecurityContext가 비어있는 경우
                Mono.error(new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, 
                    "인증 정보를 찾을 수 없습니다"
                ))
            );
    }

    /**
     * 쿠키에서 JWT 토큰 추출
     *
     * @param request ServerHttpRequest
     * @return JWT 토큰 문자열 (없으면 null)
     */
    private String extractJwtFromCookie(ServerHttpRequest request) {
        if (request.getCookies().containsKey(JwtConstants.JWT_TOKEN_NAME)) {
            HttpCookie cookie = request.getCookies().getFirst(JwtConstants.JWT_TOKEN_NAME);
            return cookie != null ? cookie.getValue() : null;
        }
        return null;
    }
}
