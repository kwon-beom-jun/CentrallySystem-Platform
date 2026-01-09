package com.cs.core.constants;

/**
 * JWT 관련 상수
 */
public class JwtConstants {
    
    /**
     * JWT 쿠키 이름
     */
    public static final String JWT_TOKEN_NAME = "jwt";
    
    /**
     * JWT 쿠키 경로
     */
    public static final String JWT_COOKIE_PATH = "/";
    
    /**
     * JWT 쿠키 HttpOnly 설정
     */
    public static final boolean JWT_COOKIE_HTTP_ONLY = true;
    
    /**
     * JWT 쿠키 Secure 설정
     */
    public static final boolean JWT_COOKIE_SECURE = true;
}
