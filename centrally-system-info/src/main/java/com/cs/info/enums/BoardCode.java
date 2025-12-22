package com.cs.info.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 게시판 코드
 * - NOTICE    : 공지사항
 * - RESOURCE  : 자료실
 * - COMMUNITY : 자유 게시판
 */
public enum BoardCode {
    NOTICE("NOTICE", "공지사항"),
    RESOURCE("RESOURCE", "자료실"),
    COMMUNITY("COMMUNITY", "자유게시판");

    private final String code;
    private final String label;

    BoardCode(String code, String label) {
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

