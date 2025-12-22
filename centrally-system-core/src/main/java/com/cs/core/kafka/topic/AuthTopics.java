package com.cs.core.kafka.topic;

/**
 * Auth 서비스 → 외부 마이크로서비스로 전파되는 이벤트
 */
public final class AuthTopics {
	
	private AuthTopics() {}

	/* 회원 활성(비활성) 관리 */
	public static final String USER_DEACTIVATED = "auth.user.deactivated"; // 비활성(탈퇴)
	public static final String USER_ENABLED_CHANGED = "auth.user.enabled-changed"; // 활성

    /* 가입 요청 */
    public static final String USER_CREATE_CMD = "auth.user.create";

    /* 사원 권한 */
    public static final String PERMISSION_CREATED = "auth.permission.created";
    public static final String PERMISSION_UPDATED = "auth.permission.updated";
    public static final String PERMISSION_DELETED = "auth.permission.deleted";
}