package com.cs.hrm.enums;

/**
 * 스타일 카테고리 분류
 */
public enum StyleCategory {
    MAIN_CARD("MAIN_CARD", "메인 카드"),
    INFO_MOBILE("INFO_MOBILE", "기본 정보 모바일");

    private final String code;
    private final String label;

    StyleCategory(String code, String label) {
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
    public static StyleCategory from(String code) {
        if (code == null) return null;
        for (StyleCategory c : values()) {
            if (c.code.equals(code)) return c;
        }
        return null;
    }
}

