package com.cs.core.enums;

/**
 * 시스템 전역 권한 Enum (도메인별 권한 포함)
 */
public enum Role {
    // HRM
    ROLE_HRM_EMPLOYEE("ROLE_HRM_EMPLOYEE", "사원", "hrm"),
    ROLE_HRM_LEADER("ROLE_HRM_LEADER", "리더", "hrm"),
    ROLE_HRM_ASSISTANT_MANAGER("ROLE_HRM_ASSISTANT_MANAGER", "중간관리자", "hrm"),
    ROLE_HRM_MANAGER("ROLE_HRM_MANAGER", "관리자", "hrm"),

    // RECEIPT
    ROLE_RECEIPT_REGISTRAR("ROLE_RECEIPT_REGISTRAR", "등록자", "receipt"),
    ROLE_RECEIPT_APPROVER("ROLE_RECEIPT_APPROVER", "결재자", "receipt"),
    ROLE_RECEIPT_INSPECTOR("ROLE_RECEIPT_INSPECTOR", "검수자", "receipt"),
    ROLE_RECEIPT_FINALIZER("ROLE_RECEIPT_FINALIZER", "확정자", "receipt"),
    ROLE_RECEIPT_MANAGER("ROLE_RECEIPT_MANAGER", "관리자", "receipt"),

    // INFO
    ROLE_INFO_GUEST("ROLE_INFO_GUEST", "게스트", "info"),
    ROLE_INFO_USER("ROLE_INFO_USER", "일반사용자", "info"),
    ROLE_INFO_MANAGER("ROLE_INFO_MANAGER", "운영자", "info"),
    ROLE_INFO_ADMIN("ROLE_INFO_ADMIN", "관리자", "info"),

    // SYSTEM
    ROLE_GATE_SYSTEM("ROLE_GATE_SYSTEM", "시스템", "system");

    private final String code;
    private final String description;
    private final String domain;

    Role(String code, String description, String domain) {
        this.code = code;
        this.description = description;
        this.domain = domain;
    }

    /**
     * Spring Security 권한 문자열
     */
    public String authority() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getDomain() {
        return domain;
    }

    /**
     * 코드로 Enum 역매핑
     */
    public static Role from(String code) {
        if (code == null) return null;
        for (Role r : values()) {
            if (r.code.equals(code)) return r;
        }
        return null;
    }
}


