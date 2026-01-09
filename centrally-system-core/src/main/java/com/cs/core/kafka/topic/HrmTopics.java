package com.cs.core.kafka.topic;

/**
 * HRM 서비스 → 외부 마이크로서비스로 전파되는 이벤트
 */
public final class HrmTopics {
    private HrmTopics() {}

    public static final String USER_DEPT_TEAM_CHANGED = "hrm.user.dept-team-changed";

    /* 프로필 생성/실패 (가입 Saga 응답) */
    public static final String USER_CREATED_SUCCESS = "hrm.user.created.success";
    public static final String USER_CREATED_FAILED  = "hrm.user.created.failed";

    /* 부서/팀 관리 */
    public static final String DEPARTMENT_CREATED = "hrm.department.created";
    public static final String DEPARTMENT_RENAMED = "hrm.department.renamed";
    public static final String DEPARTMENT_DELETED = "hrm.department.deleted";
    public static final String TEAM_CREATED       = "hrm.team.created";
    public static final String TEAM_RENAMED       = "hrm.team.renamed";
    public static final String TEAM_DELETED       = "hrm.team.deleted";
} 