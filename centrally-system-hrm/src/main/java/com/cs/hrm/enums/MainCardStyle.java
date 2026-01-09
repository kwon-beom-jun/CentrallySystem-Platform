package com.cs.hrm.enums;

/**
 * 메인 카드 스타일 분류
 */
public enum MainCardStyle {
    DEFAULT("default", "메인 카드 기본"),
    VER1("ver1", "메인 카드 스타일 1"),
    VER2("ver2", "메인 카드 스타일 2"),
    VER3("ver3", "메인 카드 스타일 3"),
    VER4("ver4", "메인 카드 스타일 4");

    private final String code;
    private final String label;

    MainCardStyle(String code, String label) {
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
    public static MainCardStyle from(String code) {
        if (code == null) return null;
        for (MainCardStyle s : values()) {
            if (s.code.equals(code)) return s;
        }
        return null;
    }
    
    /**
     * 코드로 Enum 역매핑 (기본값 반환)
     */
    public static MainCardStyle fromOrDefault(String code) {
        MainCardStyle style = from(code);
        return style != null ? style : DEFAULT;
    }
}

