package com.cs.gateway.enums;

/**
 * 브라우저 종류 Enum
 */
public enum Browser {
    CHROME("Chrome"),
    SAFARI("Safari"),
    FIREFOX("Firefox"),
    EDGE("Edge"),
    IE("IE"),
    OPERA("Opera"),
    UNKNOWN("Unknown");

    private final String displayName;

    Browser(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * User-Agent 문자열로부터 브라우저 판별
     *
     * @param userAgent User-Agent 문자열
     * @return Browser enum
     */
    public static Browser fromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return UNKNOWN;
        }

        String ua = userAgent.toLowerCase();

        // Edge (Chromium 기반) - "Edg/" 포함
        if (ua.contains("edg/")) {
            return EDGE;
        }
        // Chrome - "chrome" 포함하지만 "edg"는 제외
        if (ua.contains("chrome") && !ua.contains("edg")) {
            return CHROME;
        }
        // Safari - "safari" 포함하지만 "chrome"은 제외
        if (ua.contains("safari") && !ua.contains("chrome")) {
            return SAFARI;
        }
        // Firefox
        if (ua.contains("firefox")) {
            return FIREFOX;
        }
        // IE (Internet Explorer)
        if (ua.contains("msie") || ua.contains("trident/")) {
            return IE;
        }
        // Opera
        if (ua.contains("opera") || ua.contains("opr/")) {
            return OPERA;
        }

        return UNKNOWN;
    }

    /**
     * 문자열로부터 enum 변환 (DB에서 읽을 때 사용)
     *
     * @param value 브라우저 이름 문자열
     * @return Browser enum
     */
    public static Browser fromString(String value) {
        if (value == null || value.isEmpty()) {
            return UNKNOWN;
        }
        
        for (Browser browser : Browser.values()) {
            if (browser.displayName.equalsIgnoreCase(value) || browser.name().equalsIgnoreCase(value)) {
                return browser;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

