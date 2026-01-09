package com.cs.info.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
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
 * INFO 서비스 권한 필터
 * ① info.role.exclude.url 에 등록된 엔드포인트는 그냥 통과  
 * ② 그 외 요청은 JWT roles 중 info.roles 교집합이 있는지 검사
 */
@RequiredArgsConstructor
public class RoleAuthorizationFilter extends OncePerRequestFilter {

    /** INFO 서비스가 요구하는 권한 목록 */
    private final List<String> infoRoles;

    /** Role 체크를 건너뛸 (path,method) 리스트 */
    private final List<ExcludeEntry> excludes;
    
    private final AccessDeniedHandler accessDeniedHandler;

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
            chain.doFilter(request, response);
            return;
        }

        List<String> userRoles = auth.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .toList();

        boolean allowed = userRoles.stream().anyMatch(infoRoles::contains);

        if (!allowed) {
            accessDeniedHandler.handle(request, response,
                new AccessDeniedException("서비스 권한이 없습니다"));
            return;
        }

        /* ── 3. 통과 ───────────────────────────────── */
        chain.doFilter(request, response);
    }

    /**
     * 현재 요청이 exclude 목록과 일치하는지 (+ {id}=본인 체크)
     */
    private boolean isExcluded(HttpServletRequest req) {
        String uri    = req.getRequestURI();
        String method = req.getMethod().toUpperCase(Locale.ENGLISH);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer jwtUserId = extractUserId(auth);

        for (ExcludeEntry ex : excludes) {
            if (!ex.method.equals("*") && !ex.method.equals(method)) continue;
            if (!matcher.match(ex.pattern, uri)) continue;
            if (!ex.pattern.contains("{id}")) return true;

            Map<String, String> variables =
                    matcher.extractUriTemplateVariables(ex.pattern, uri);
            try {
                Integer pathId = Integer.valueOf(variables.get("id"));
                if (Objects.equals(pathId, jwtUserId)) return true;
            } catch (NumberFormatException ignore) {}
        }
        return false;
    }

    /**
     * Authentication(JWT)에서 userId 클레임 추출
     */
    private Integer extractUserId(Authentication auth) {
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            Object principal = token.getPrincipal();
            if (principal instanceof com.cs.core.security.CustomUserPrincipal user) {
                return user.getUserId();
            }
        }
        return null;
    }

    /**
     * path|method 파싱용 내부 클래스
     */
    public static record ExcludeEntry(String pattern, String method) {
        public ExcludeEntry(String raw) {
            this(
                raw.split("\\|")[0].trim(),
                raw.contains("|") ? raw.split("\\|")[1].trim().toUpperCase()
                                  : "*"
            );
        }
    }
}

