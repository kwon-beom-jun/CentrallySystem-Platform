package com.cs.hrm.enums;

/**
 * 기본 정보 모바일 테마 분류
 */
public enum InfoMobileStyle {
    LIGHT("light", "밝은 테마"),
    DARK("dark", "어두운 테마");

    private final String code;
    private final String label;

    InfoMobileStyle(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 코드로 Enum 역매핑
     */
    public static InfoMobileStyle from(String code) {
        if (code == null) return null;
        for (InfoMobileStyle s : values()) {
            if (s.code.equals(code)) return s;
        }
        return null;
    }
    
    /**
     * 코드로 Enum 역매핑 (기본값 반환)
     */
    public static InfoMobileStyle fromOrDefault(String code) {
        InfoMobileStyle style = from(code);
        return style != null ? style : LIGHT;
    }
}

