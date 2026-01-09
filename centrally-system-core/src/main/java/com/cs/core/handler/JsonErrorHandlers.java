// com.cs.core.handler.JsonErrorHandlers.java
package com.cs.core.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.*;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

// Security 에러 처리
@RequiredArgsConstructor
public class JsonErrorHandlers {

    private static final ObjectMapper om = new ObjectMapper();

    /* 401 ― 인증 실패 (토큰 없음/만료 등) */
    public static class JsonAuthEntryPoint implements AuthenticationEntryPoint {
        @Override public void commence(HttpServletRequest req,
                                       HttpServletResponse res,
                                       org.springframework.security.core.AuthenticationException ex)
                                       throws IOException {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType("application/json;charset=UTF-8");
            om.writeValue(res.getWriter(),
                new ErrorResponse("UNAUTHORIZED", "인증이 필요합니다."));
        }
    }

    /* 403 ― 인가 실패 (권한 부족) */
    public static class JsonAccessDenied implements AccessDeniedHandler {
        @Override public void handle(HttpServletRequest req,
                                     HttpServletResponse res,
                                     AccessDeniedException ex)
                                     throws IOException {
            res.setStatus(HttpStatus.FORBIDDEN.value());
            res.setContentType("application/json;charset=UTF-8");
            om.writeValue(res.getWriter(),
                new ErrorResponse("FORBIDDEN", ex.getMessage()));
        }
    }

    /* 공통 DTO */
    private record ErrorResponse(String code, String message) {}
}
