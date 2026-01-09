package com.cs.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    @Value("${oauth.fail.redirect.frontend-url}")
    private String oauthFailRedirectFrontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        AuthenticationException exception)
                                        throws IOException, ServletException {
        // 예외 메시지 출력 (또는 로깅)
        System.out.println("Authentication Failure: " + exception.getMessage());

        // 예를 들어, 로그인 페이지로 리다이렉트하면서 에러 메시지를 전달할 수 있음
        response.sendRedirect(oauthFailRedirectFrontendUrl);
    }
}
