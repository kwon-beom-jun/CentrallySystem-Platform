/**
 * ═══════════════════════════════════════════════════════════════
 * 활동 이력 통합 설정 (Activity Log Configuration)
 * ═══════════════════════════════════════════════════════════════
 * 
 * [목적]
 * - 기능명 (API X-Func-Vue)
 * - 아이콘 & 색상
 * - 메뉴 → 서비스 매핑
 * 
 * [사용처]
 * - API: X-Func-Vue 쿠키 값으로 사용
 * - RecentActivityCard: 아이콘/색상 매핑
 * 
 * [메뉴명]
 * - MENUS는 routeConfig.js에서 관리 (ROUTES와 1:1 매핑)
 */

import { MENUS } from '@/config/routeConfig';

/* ═══════════════════════════════════════════════════════════════
 * 기능 상수 (Function Constants)
 * NOTE: 각 키는 activity.js (i18n) 의 activity.function.* 항목과 1:1로 매핑됨
 * ═══════════════════════════════════════════════════════════════ */

export const FUNCTIONS = {
  // ─────────────────────────────────────────────────────────────
  // Auth (인증)
  // ─────────────────────────────────────────────────────────────
  AUTH: {
    // 로그인/로그아웃
    LOGIN: 'activity.function.auth.login',
    LOGIN_SUCCESS: 'activity.function.auth.loginSuccess',
    LOGIN_FAILED: 'activity.function.auth.loginFailed',
    LOGOUT: 'activity.function.auth.logout',
    
    // 사용자 관리 (UsersApi)
    USER_LIST: 'activity.function.auth.userList',
    USER_DETAIL: 'activity.function.auth.userDetail',
    USER_BY_EMAIL: 'activity.function.auth.userByEmail',
    USER_WITH_ROLES: 'activity.function.auth.userWithRoles',
    USER_SOCIAL: 'activity.function.auth.userSocial',
    USER_SOCIAL_DELETE: 'activity.function.auth.userSocialDelete',
    USER_CREATE: 'activity.function.auth.userCreate',
    USER_UPDATE: 'activity.function.auth.userUpdate',
    USER_PATCH: 'activity.function.auth.userPatch',
    USER_DELETE: 'activity.function.auth.userDelete',
    PASSWORD_VERIFY: 'activity.function.auth.passwordVerify',
    
    // 임시 사용자 (TempUsersApi)
    TEMP_USER_LIST: 'activity.function.auth.tempUserList',
    TEMP_USER_CREATE: 'activity.function.auth.tempUserCreate',
    TEMP_USER_APPROVE: 'activity.function.auth.tempUserApprove',
    TEMP_USER_DELETE: 'activity.function.auth.tempUserDelete',
    
    // 권한 관리 (UserPermissionApi)
    PERMISSION_LIST: 'activity.function.auth.permissionList',
    PERMISSION_UPDATE: 'activity.function.auth.permissionUpdate',
    PERMISSION_CREATE: 'activity.function.auth.permissionCreate',
    PERMISSION_DELETE: 'activity.function.auth.permissionDelete',
    
    // 이메일 인증 (EmailApi)
    EMAIL_SEND: 'activity.function.auth.emailSend',
    EMAIL_VERIFY: 'activity.function.auth.emailVerify',
    EMAIL_RESET_SEND: 'activity.function.auth.emailResetSend',
    EMAIL_RESET_VERIFY: 'activity.function.auth.emailResetVerify',
    
    // 약관 (AgreementApi)
    AGREEMENT_LIST: 'activity.function.auth.agreementList',
  },

  // ─────────────────────────────────────────────────────────────
  // HRM (인사관리)
  // ─────────────────────────────────────────────────────────────
  HRM: {
    // 즐겨찾기
    FAVORITE_LIST: 'activity.function.hrm.favoriteList',
    FAVORITE_CREATE: 'activity.function.hrm.favoriteCreate',
    FAVORITE_DELETE: 'activity.function.hrm.favoriteDelete',
    FAVORITE_DELETE_ALL: 'activity.function.hrm.favoriteDeleteAll',
    FAVORITE_REORDER: 'activity.function.hrm.favoriteReorder',
    FAVORITE_COLOR: 'activity.function.hrm.favoriteColor',
    
    // 사용자 (UsersApi - HRM)
    USER_LIST: 'activity.function.hrm.userList',
    USER_LIST_WITH_INACTIVE: 'activity.function.hrm.userListWithInactive',
    USER_DETAIL: 'activity.function.hrm.userDetail',
    USER_BULK: 'activity.function.hrm.userBulk',
    USER_BULK_WITH_ROLES: 'activity.function.hrm.userBulkWithRoles',
    USER_ACTIVE_WITH_ROLES: 'activity.function.hrm.userActiveWithRoles',
    USER_INACTIVE_CHECK: 'activity.function.hrm.userInactiveCheck',
    USER_PATCH: 'activity.function.hrm.userPatch',
    USER_SEARCH: 'activity.function.hrm.userSearch',
    
    // 부서/팀 (DepartmentApi, TeamApi)
    DEPT_LIST: 'activity.function.hrm.deptList',
    DEPT_BY_TEAM: 'activity.function.hrm.deptByTeam',
    DEPT_CREATE: 'activity.function.hrm.deptCreate',
    DEPT_UPDATE: 'activity.function.hrm.deptUpdate',
    DEPT_DELETE: 'activity.function.hrm.deptDelete',
    TEAM_CREATE: 'activity.function.hrm.teamCreate',
    TEAM_UPDATE: 'activity.function.hrm.teamUpdate',
    TEAM_DELETE: 'activity.function.hrm.teamDelete',
    TEAM_LIST: 'activity.function.hrm.teamList',
    
    // 직책 (PositionsApi)
    POSITION_LIST: 'activity.function.hrm.positionList',
    POSITION_BY_USER: 'activity.function.hrm.positionByUser',
    
    // 고용형태 (EmploymentTypeApi)
    EMPLOYMENT_TYPE_LIST: 'activity.function.hrm.employmentTypeList',
    
    // 메타데이터 (UserManagementMetadataApi)
    METADATA_LIST: 'activity.function.hrm.metadataList',
    
    // 실적 (PerformanceApi)
    PERFORMANCE_LIST: 'activity.function.hrm.performanceList',
    PERFORMANCE_CREATE: 'activity.function.hrm.performanceCreate',
    PERFORMANCE_DELETE: 'activity.function.hrm.performanceDelete',
    PERFORMANCE_UPDATE: 'activity.function.hrm.performanceUpdate',
    
    // 스타일 (StylesApi)
    STYLE_LIST: 'activity.function.hrm.styleList',
    STYLE_LIST_BY_CATEGORY: 'activity.function.hrm.styleListByCategory',
    STYLE_USER_GET: 'activity.function.hrm.styleUserGet',
    STYLE_USER_UPDATE: 'activity.function.hrm.styleUserUpdate',
  },

  // ─────────────────────────────────────────────────────────────
  // INFO (정보)
  // ─────────────────────────────────────────────────────────────
  INFO: {
    // 일정
    SCHEDULE_CREATE: 'activity.function.info.scheduleCreate',
    SCHEDULE_UPDATE: 'activity.function.info.scheduleUpdate',
    SCHEDULE_DELETE: 'activity.function.info.scheduleDelete',
    
    // 일정 유형
    SCHEDULE_TYPE_LIST: 'activity.function.info.scheduleTypeList',
    SCHEDULE_TYPE_DETAIL: 'activity.function.info.scheduleTypeDetail',
    SCHEDULE_TYPE_CREATE: 'activity.function.info.scheduleTypeCreate',
    SCHEDULE_TYPE_UPDATE: 'activity.function.info.scheduleTypeUpdate',
    SCHEDULE_TYPE_DELETE: 'activity.function.info.scheduleTypeDelete',
    
    // 일정 알림 설정
    SCHEDULE_NOTIFICATION_GET: 'activity.function.info.scheduleNotificationGet',
    SCHEDULE_NOTIFICATION_UPDATE: 'activity.function.info.scheduleNotificationUpdate',
    SCHEDULE_NOTIFICATION_DELETE: 'activity.function.info.scheduleNotificationDelete',
    SCHEDULE_NOTIFICATION_HISTORY: 'activity.function.info.scheduleNotificationHistory',
    
    // 게시판
    BOARD_LIST: 'activity.function.info.boardList',
    BOARD_DETAIL: 'activity.function.info.boardDetail',
    
    // 게시글
    POST_LIST: 'activity.function.info.postList',
    POST_DETAIL: 'activity.function.info.postDetail',
    POST_CREATE: 'activity.function.info.postCreate',
    POST_UPDATE: 'activity.function.info.postUpdate',
    POST_DELETE: 'activity.function.info.postDelete',
    POST_PIN: 'activity.function.info.postPin',
    
    // 댓글
    COMMENT_LIST: 'activity.function.info.commentList',
    COMMENT_CREATE: 'activity.function.info.commentCreate',
    COMMENT_UPDATE: 'activity.function.info.commentUpdate',
    COMMENT_DELETE: 'activity.function.info.commentDelete',
  },

  // ─────────────────────────────────────────────────────────────
  // Receipt (영수증)
  // ─────────────────────────────────────────────────────────────
  RECEIPT: {
    // 조회
    LIST: 'activity.function.receipt.list',
    LIST_WITH_STATUS: 'activity.function.receipt.listWithStatus',
    SEARCH_USER: 'activity.function.receipt.searchUser',
    USER_LIST_STATUS: 'activity.function.receipt.userListStatus',
    USER_LIST: 'activity.function.receipt.userList',
    DETAIL: 'activity.function.receipt.detail',
    YEAR_SUMMARY: 'activity.function.receipt.yearSummary',
    DATE_RANGE: 'activity.function.receipt.dateRange',
    
    // CUD
    CREATE: 'activity.function.receipt.create',
    UPDATE: 'activity.function.receipt.update',
    DELETE: 'activity.function.receipt.delete',
    
    // 대리결재
    DELEGATE_ACTIVE: 'activity.function.receipt.delegateActive',
    DELEGATE_PRINCIPAL_LIST: 'activity.function.receipt.delegatePrincipalList',
    DELEGATE_SAVE: 'activity.function.receipt.delegateSave',
    DELEGATE_DELETE: 'activity.function.receipt.delegateDelete',
    DELEGATE_DELETE_ALL: 'activity.function.receipt.delegateDeleteAll',
    
    // 검색 API (ReceiptsSearchApi)
    HISTORY_OVERVIEW: 'activity.function.receipt.historyOverview',
    SUMMARY_BY_NAME_DATE: 'activity.function.receipt.summaryByNameDate',
    MY_APPROVAL_DETAIL: 'activity.function.receipt.myApprovalDetail',
    USER_APPROVED_LIST: 'activity.function.receipt.userApprovedList',
    MY_PENDING: 'activity.function.receipt.myPending',
    MY_APPROVAL_PENDING: 'activity.function.receipt.myApprovalPending',
    MY_PENDING_SUMMARY: 'activity.function.receipt.myPendingSummary',
    MY_PENDING_SUMMARY_ROWS: 'activity.function.receipt.myPendingSummaryRows',
    MY_PENDING_BY_ROLE: 'activity.function.receipt.myPendingByRole',
    DELEGATE_APPROVAL_PENDING: 'activity.function.receipt.delegateApprovalPending',
    DELEGATE_DATE_RANGE: 'activity.function.receipt.delegateDateRange',
    DELEGATE_SUMMARY_ALL: 'activity.function.receipt.delegateSummaryAll',
    DELEGATE_SUMMARY_PENDING: 'activity.function.receipt.delegateSummaryPending',
    APPROVAL_CLOSED_SUMMARY: 'activity.function.receipt.approvalClosedSummary',
    ALL_STATISTICS: 'activity.function.receipt.allStatistics',
    MY_PENDING_LIST: 'activity.function.receipt.myPendingList',
    MY_PENDING_COUNT: 'activity.function.receipt.myPendingCount',
    
    // 결재 요청 (ReceiptsRequestApi)
    REQUEST: 'activity.function.receipt.request',
    STATUS_SAVE: 'activity.function.receipt.statusSave',
    BATCH_APPROVE: 'activity.function.receipt.batchApprove',
    BATCH_REJECT: 'activity.function.receipt.batchReject',
    BATCH_CLOSE: 'activity.function.receipt.batchClose',
    USER_DEACTIVATE_PROCESS: 'activity.function.receipt.userDeactivateProcess',
    FORCE_STATUS_CHANGE: 'activity.function.receipt.forceStatusChange',
    
    // 카테고리 (ReceiptsCategoryApi)
    CATEGORY_LIST: 'activity.function.receipt.categoryList',
    CATEGORY_LIST_ALL: 'activity.function.receipt.categoryListAll',
    CATEGORY_DELETE: 'activity.function.receipt.categoryDelete',
    CATEGORY_CREATE: 'activity.function.receipt.categoryCreate',
    CATEGORY_UPDATE: 'activity.function.receipt.categoryUpdate',
    
    // 기본 합의자 (ReceiptsDefaultApproverApi)
    DEFAULT_APPROVER_LIST: 'activity.function.receipt.defaultApproverList',
    DEFAULT_APPROVER_CHECK: 'activity.function.receipt.defaultApproverCheck',
    DEFAULT_APPROVER_CREATE: 'activity.function.receipt.defaultApproverCreate',
    DEFAULT_APPROVER_DELETE: 'activity.function.receipt.defaultApproverDelete',
    DEFAULT_APPROVER_REORDER: 'activity.function.receipt.defaultApproverReorder',
    
    // 즐겨찾기 결재자 (ReceiptsApproverFavoriteApi)
    APPROVER_FAVORITE_LIST: 'activity.function.receipt.approverFavoriteList',
    APPROVER_FAVORITE_ADD: 'activity.function.receipt.approverFavoriteAdd',
    APPROVER_FAVORITE_REMOVE: 'activity.function.receipt.approverFavoriteRemove',
    APPROVER_FAVORITE_REORDER: 'activity.function.receipt.approverFavoriteReorder',
    APPROVER_FAVORITE_CLEAN: 'activity.function.receipt.approverFavoriteClean',
    
    // 프린트 (ReceiptsPrintApi)
    PRINT_APPROVAL_SUMMARY: 'activity.function.receipt.printApprovalSummary',
    PRINT_CEO_REPORT: 'activity.function.receipt.printCeoReport',
  },

  // ─────────────────────────────────────────────────────────────
  // System (시스템)
  // ─────────────────────────────────────────────────────────────
  SYSTEM: {
    ACTIVITY_LOG_LIST: 'activity.function.system.activityLogList',
    PERSONAL_HISTORY_LIST: 'activity.function.system.personalHistoryList',
  },
};

/* ═══════════════════════════════════════════════════════════════
 * 서비스별 아이콘 & 색상 매핑
 * ═══════════════════════════════════════════════════════════════ */

export const SERVICE_STYLES = {
  AUTH: {
    icon: 'ri-shield-user-line',
    color: '#f59e0b', // 주황색
    label: 'common.activity.service.auth',  // i18n 키
  },
  HRM: {
    icon: 'ri-user-settings-line',
    color: '#3b82f6', // 파란색
    label: 'common.activity.service.hrm',  // i18n 키
  },
  INFO: {
    icon: 'ri-information-line',
    color: '#ec4899', // 분홍색
    label: 'common.activity.service.info',  // i18n 키
  },
  RECEIPT: {
    icon: 'ri-file-text-line',
    color: '#10b981', // 초록색
    label: 'common.activity.service.receipt',  // i18n 키
  },
  SYSTEM: {
    icon: 'ri-settings-3-line',
    color: '#8b5cf6', // 보라색
    label: 'common.activity.service.system',  // i18n 키
  },
  COMMON: {
    icon: 'ri-home-5-line',
    color: '#64748b', // 회색
    label: 'common.activity.service.common',  // i18n 키
  },
  DEFAULT: {
    icon: 'ri-file-list-line',
    color: '#94a3b8', // 연한 회색
    label: 'common.activity.service.default',  // i18n 키
  },
};

/* ═══════════════════════════════════════════════════════════════
 * 메뉴 → 서비스 매핑
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 메뉴명으로 서비스 타입 반환
 * 
 * NOTE: menu는 백엔드 DB에서 온 값(한글 문자열 또는 i18n 키)일 수 있습니다.
 * - 한글 문자열: MENUS 객체의 값과 비교
 * - i18n 키: menuConfig.js의 i18nKey와 비교
 */
export function getServiceByMenu(menu) {
  if (!menu) return 'DEFAULT';

  // i18n 키인 경우 처리 (menu.info.notice 같은 형식)
  if (typeof menu === 'string' && menu.startsWith('menu.')) {
    if (menu.startsWith('menu.info.')) return 'INFO';
    if (menu.startsWith('menu.receipt.')) return 'RECEIPT';
    if (menu.startsWith('menu.hrm.')) return 'HRM';
    if (menu.startsWith('menu.system.')) return 'SYSTEM';
     if (menu.startsWith('menu.auth.')) return 'AUTH';
     if (menu.startsWith('menu.common.')) return 'COMMON';
    return 'DEFAULT';
  }

  // 한글 문자열인 경우 (기존 로직)
  // Auth
  if (Object.values(MENUS.AUTH).includes(menu)) {
    return 'AUTH';
  }

  // Receipt
  if (Object.values(MENUS.RECEIPT).includes(menu)) {
    return 'RECEIPT';
  }

  // HRM
  if (Object.values(MENUS.HRM).includes(menu)) {
    return 'HRM';
  }

  // INFO
  if (Object.values(MENUS.INFO).includes(menu)) {
    return 'INFO';
  }

  // System
  if (Object.values(MENUS.SYSTEM).includes(menu)) {
    return 'SYSTEM';
  }

  // Common
  if (Object.values(MENUS.COMMON).includes(menu)) {
    return 'COMMON';
  }

  return 'DEFAULT';
}

/**
 * 메뉴명으로 아이콘 스타일 반환
 */
export function getMenuStyle(menu) {
  const service = getServiceByMenu(menu);
  return SERVICE_STYLES[service];
}

