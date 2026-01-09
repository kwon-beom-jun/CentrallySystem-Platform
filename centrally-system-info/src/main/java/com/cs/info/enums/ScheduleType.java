package com.cs.info.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 일정 유형
 * - BUSINESS_TRIP : 외근
 * - VACATION      : 휴가
 * - MEETING      : 회의
 * - EDUCATION    : 교육
 * - OTHER        : 기타
 */
public enum ScheduleType {
    BUSINESS_TRIP("BUSINESS_TRIP", "외근"),
    VACATION("VACATION", "휴가"),
    MEETING("MEETING", "회의"),
    EDUCATION("EDUCATION", "교육"),
    OTHER("OTHER", "기타");

    private final String code;
    private final String label;

    ScheduleType(String code, String label) {
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
}

