package com.cs.hrm.filter;

import com.cs.core.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * ① hrm.role.exclude.url 에 등록된 엔드포인트는 그냥 통과  
 * ② 그 외 요청은 JWT roles 중 hrm.roles 교집합이 있는지 검사
 */
@RequiredArgsConstructor
public class RoleAuthorizationFilter extends OncePerRequestFilter {

    /* --------------- 설정 값 --------------- */

    /** HRM 서비스가 요구하는 권한 목록 */
    private final List<String> hrmRoles;

    /** Role 체크를 건너뛸 (path,method) 리스트 */
    private final List<ExcludeEntry> excludes;
    
    private final AccessDeniedHandler accessDeniedHandler;

    /* --------------- 필터 로직 --------------- */

    private static final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
                                    throws ServletException, IOException {

        /* ── 1. 예외 URL이면 즉시 통과 ────────────────── */
        if (isExcluded(request)) {
            chain.doFilter(request, response);
            return;
        }

        /* ── 2. JWT 권한 검사 ───────────────────────── */
        Authentication auth = SecurityContextHolder.getContext()
                                                   .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            chain.doFilter(request, response);          // 인증 자체가 없으면 다음 필터로
            return;
        }

        List<String> userRoles = auth.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .toList();

        boolean allowed = userRoles.stream().anyMatch(hrmRoles::contains);

        if (!allowed) {                                 // 2-1. 실패 → 403
            accessDeniedHandler.handle(request, response,
                new AccessDeniedException("서비스 권한이 없습니다"));
            return;
        }

        /* ── 3. 통과 ───────────────────────────────── */
        chain.doFilter(request, response);
    }

    /* --------------------------------------------------------- */
    /** 현재 요청이 exclude 목록과 일치하는지 (+ {id}=본인 체크) */
    private boolean isExcluded(HttpServletRequest req) {
        String uri    = req.getRequestURI();
        String method = req.getMethod().toUpperCase(Locale.ENGLISH);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer jwtUserId = extractUserId(auth);   // 🔹 JWT에서 userId 꺼냄

        for (ExcludeEntry ex : excludes) {

            /* 1) HTTP 메서드가 맞지 않으면 skip */
            if (!ex.method.equals("*") && !ex.method.equals(method)) continue;

            /* 2) 패턴 매칭 실패 → skip */
            if (!matcher.match(ex.pattern, uri)) continue;

            /* 3) 패턴에 {id} 가 없으면 즉시 허용 */
            if (!ex.pattern.contains("{id}")) return true;

            /* 4) {id}가 있다면 URL에서 추출 → JWT userId 와 비교 */
            Map<String, String> variables =
                    matcher.extractUriTemplateVariables(ex.pattern, uri);
            try {
                Integer pathId = Integer.valueOf(variables.get("id"));
                if (Objects.equals(pathId, jwtUserId)) return true; // 본인 → 허용
            } catch (NumberFormatException ignore) { /* id가 숫자가 아니면 불허 */ }
        }
        return false;
    }

    /** Authentication(JWT)에서 userId 클레임 추출 */
    private Integer extractUserId(Authentication auth) {
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            Object principal = token.getPrincipal();
            if (principal instanceof com.cs.core.security.CustomUserPrincipal user) {
                return user.getUserId(); // userId를 반환하는 getter가 있어야 합니다
            }
        }
        return null;
    }

    /** path|method 파싱용 내부 클래스 */
    public static record ExcludeEntry(String pattern, String method) {
        public ExcludeEntry(String raw) {
            this(
                raw.split("\\|")[0].trim(),
                raw.contains("|") ? raw.split("\\|")[1].trim().toUpperCase()
                                  : "*"
            );
        }
    }

    /** 에러 응답 JSON */
    private record ErrorBody(String code, String message) {}
}
