package com.cs.info.enums;

/**
 * 게시글 열람 범위
 * - ALL           : 모든 로그인 사용자 (GUEST 포함)
 * - USER_ABOVE    : USER 이상 (MANAGER, ADMIN 포함, GUEST 제외)
 * - MANAGER_ABOVE : MANAGER 이상 (ADMIN 포함)
 */
public enum VisibilityScope {
    ALL,
    USER_ABOVE,
    MANAGER_ABOVE
}

