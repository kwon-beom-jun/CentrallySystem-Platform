package com.cs.info.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 알림 시점 단위
 * - DAY    : 일 단위
 * - HOUR   : 시간 단위
 * - MINUTE : 분 단위
 */
public enum NotificationTimingUnit {
    DAY("DAY", "일"),
    HOUR("HOUR", "시간"),
    MINUTE("MINUTE", "분");

    private final String code;
    private final String label;

    NotificationTimingUnit(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 코드로 enum 찾기
     */
    public static NotificationTimingUnit fromCode(String code) {
        for (NotificationTimingUnit unit : values()) {
            if (unit.code.equals(code)) {
                return unit;
            }
        }
        return null;
    }
}

