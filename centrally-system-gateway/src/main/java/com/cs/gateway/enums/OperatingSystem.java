package com.cs.gateway.enums;

/**
 * 운영체제(OS) 종류 Enum
 */
public enum OperatingSystem {
    WINDOWS("Windows"),
    MACOS("MacOS"),
    LINUX("Linux"),
    ANDROID("Android"),
    IOS("iOS"),
    UNKNOWN("Unknown");

    private final String displayName;

    OperatingSystem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * User-Agent 문자열로부터 OS 판별
     *
     * @param userAgent User-Agent 문자열
     * @return OperatingSystem enum
     */
    public static OperatingSystem fromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return UNKNOWN;
        }

        String ua = userAgent.toLowerCase();

        // Android (Linux보다 먼저 체크해야 함)
        if (ua.contains("android")) {
            return ANDROID;
        }
        // iOS (iPhone, iPad)
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            return IOS;
        }
        // Windows
        if (ua.contains("win")) {
            return WINDOWS;
        }
        // MacOS
        if (ua.contains("mac")) {
            return MACOS;
        }
        // Linux (Android 체크 후에 해야 함)
        if (ua.contains("linux")) {
            return LINUX;
        }

        return UNKNOWN;
    }

    /**
     * 문자열로부터 enum 변환 (DB에서 읽을 때 사용)
     *
     * @param value OS 이름 문자열
     * @return OperatingSystem enum
     */
    public static OperatingSystem fromString(String value) {
        if (value == null || value.isEmpty()) {
            return UNKNOWN;
        }
        
        for (OperatingSystem os : OperatingSystem.values()) {
            if (os.displayName.equalsIgnoreCase(value) || os.name().equalsIgnoreCase(value)) {
                return os;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

