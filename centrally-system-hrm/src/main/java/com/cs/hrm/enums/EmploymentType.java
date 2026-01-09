package com.cs.hrm.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 고용형태 분류 (비즈니스 로직용)
 * - UNASSIGNED(0), REGULAR(1), NONREGULAR(2)
 *   → ORDINAL(0,1,2)과 DB 코드(0,1,2)를 일치시키기 위해 순서 고정
 */
public enum EmploymentType {
    UNASSIGNED(0, "미지정"),
    REGULAR(1, "정규직"),
    NONREGULAR(2, "비정규직");

    private final int id;
    private final String label;

    EmploymentType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    @JsonValue
    public int getId() { return id; }
    public String getLabel() { return label; }
}


