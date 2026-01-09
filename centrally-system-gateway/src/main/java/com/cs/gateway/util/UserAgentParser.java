package com.cs.gateway.util;

import com.cs.gateway.enums.Browser;
import com.cs.gateway.enums.OperatingSystem;

/**
 * User-Agent 파싱 유틸리티
 * - User-Agent 문자열에서 브라우저와 OS 정보 추출 (Enum 사용)
 */
public class UserAgentParser {

    /**
     * User-Agent 문자열에서 브라우저 정보 추출
     *
     * @param userAgent User-Agent 문자열
     * @return Browser enum
     */
    public static Browser getBrowser(String userAgent) {
        return Browser.fromUserAgent(userAgent);
    }

    /**
     * User-Agent 문자열에서 OS 정보 추출
     *
     * @param userAgent User-Agent 문자열
     * @return OperatingSystem enum
     */
    public static OperatingSystem getOS(String userAgent) {
        return OperatingSystem.fromUserAgent(userAgent);
    }

    /**
     * User-Agent 문자열에서 브라우저 이름 추출 (문자열 반환)
     *
     * @param userAgent User-Agent 문자열
     * @return 브라우저 이름 문자열
     */
    public static String getBrowserString(String userAgent) {
        return getBrowser(userAgent).getDisplayName();
    }

    /**
     * User-Agent 문자열에서 OS 이름 추출 (문자열 반환)
     *
     * @param userAgent User-Agent 문자열
     * @return OS 이름 문자열
     */
    public static String getOSString(String userAgent) {
        return getOS(userAgent).getDisplayName();
    }
}

