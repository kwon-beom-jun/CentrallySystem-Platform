/* ═══════════════════════════════════════════════════════════════
 * 라우터 경로 설정 (Route Configuration)
 * ═══════════════════════════════════════════════════════════════
 * 
 * 이 파일에서 모든 라우터 경로를 중앙 관리합니다:
 * - 라우터 파일 (hrm.js, receipt.js, system.js, auth.js)
 * - menuConfig.js
 * - 컴포넌트의 router.push()
 * 
 * 경로 수정 시 이 파일만 수정하면 모든 곳에 자동 반영됩니다.
 */

/* ═══════════════════════════════════════════════════════════════
 * 라우터 경로명 상수 (Router Path Names)
 * ═══════════════════════════════════════════════════════════════
 * 라우터 파일에서 사용 (prefix 없는 경로명)
 */

export const PATH_NAMES = {
  // ─────────── MANAGEMENT ───────────
  MANAGEMENT: {
    // 사용자
    USER_MANAGEMENT: 'user-management',
    USER_MODIFY_MOBILE: 'user-modify-mobile',
    
    // 조직
    DEPT_TEAM_MANAGER: 'dept-team-manager',
    ORG_DIRECTORY: 'org-directory',
    
    // 실적
    PERFORMANCE_HISTORY: 'performance-history',
    PERFORMANCE_MANAGEMENT: 'performance-management',
    
    // 권한
    USER_PERMISSIONS: 'user-permissions',
    
    // 즐겨찾기
    FAVORITE_MENU_MANAGEMENT: 'favorite-menu-management',
  },
  
  // ─────────── INFO ───────────
  INFO: {
    SCHEDULE: 'schedule',
    SCHEDULE_DAY_DETAIL: 'schedule-day-detail',
    SCHEDULE_STATISTICS: 'schedule-statistics',
    SCHEDULE_SETTINGS: 'schedule-settings',
    SCHEDULE_CREATE_MOBILE: 'schedule-create-mobile',
    SCHEDULE_EDIT_MOBILE: 'schedule-edit-mobile',
    SCHEDULE_TYPE_MANAGEMENT: 'schedule-type-management',
    SCHEDULE_NOTIFICATION_HISTORY: 'schedule-notification-history',
    SCHEDULE_NOTIFICATION_HISTORY_DETAIL_MOBILE: 'schedule-notification-history-detail-mobile',
    NOTICE: 'notice',
    RESOURCE: 'resource',
    COMMUNITY: 'community',
    POST_DETAIL: 'post-detail',
    POST_CREATE_MOBILE: 'post-create-mobile',
    POST_EDIT_MOBILE: 'post-edit-mobile',
    GUIDE: 'guide',
  },
  
  // ─────────── AUTH ───────────
  AUTH: {
    OAUTH_CALLBACK: 'oauth-call-back',
    TEMP_USER_APPROVALS: 'temp-user-approvals',
  },
  
  // ─────────── RECEIPT ───────────
  RECEIPT: {
    // 등록자
    SUBMISSION: 'receipt-submission',
    SUBMISSION_CREATE_MOBILE: 'receipt-submission-create',
    SUBMISSION_EDIT_MOBILE: 'receipt-submission-edit',
    PERSONAL_HISTORY: 'personal-receipt-history',
    DETAIL_MOBILE: 'receipt-detail-mobile',
    ANNUAL_SUMMARY: 'annual-receipt-summary',
    
    // 결재자
    APPROVAL_OVERVIEW: 'receipt-approval-request-overview',
    APPROVAL_DETAILS: 'receipt-approval-request-details',
    CONCURRENCE_OVERVIEW: 'receipt-concurrence-request-overview',
    CONCURRENCE_DETAILS: 'receipt-concurrence-request-details',
    HISTORY: 'receipt-history',
    HISTORY_DETAIL: 'receipt-history-detail',
    
    // 대리 결재자
    DELEGATE_APPROVAL_OVERVIEW: 'receipt-delegate-approval-request-overview',
    DELEGATE_APPROVAL_DETAILS: 'receipt-delegate-approval-request-details',
    DELEGATE_CONCURRENCE_OVERVIEW: 'receipt-delegate-concurrence-request-overview',
    DELEGATE_CONCURRENCE_DETAILS: 'receipt-delegate-concurrence-request-details',
    DELEGATE_HISTORY: 'receipt-delegate-history',
    DELEGATE_HISTORY_DETAIL: 'receipt-delegate-history-detail',
    
    // 검수자
    APPROVAL_SUMMARY: 'receipt-approval-summary',
    APPROVAL_SUMMARY_DETAIL: 'receipt-approval-summary-detail',
    REPORT_FOR_CEO: 'receipt-report-for-ceo',
    CLOSURE: 'receipt-closure',
    
    // 관리자
    MANAGEMENT: 'receipt-management',
    META_MANAGEMENT: 'receipt-meta-management',
  },
  
  // ─────────── USER ───────────
  USER: {
    USER_INFORMATION: 'user-information',
    PERSONAL_HISTORY: 'personal-history',
  },
  
  // ─────────── SYSTEM ───────────
  SYSTEM: {
    ACTIVITY_LOG: 'activity-log',
    STATISTICS_SCREEN: 'statistics-screen',
    ROLE_MANAGEMENT: 'role-management',
    KAFKA_OUTBOX: 'kafka-outbox',
    COMPONENT_TEST: 'component-test',
  },
  
  
  
  // ─────────── GUIDE ───────────
  GUIDE: 'guide',
};


/* ═══════════════════════════════════════════════════════════════
 * 메뉴 상수 (Menu Constants)
 * ═══════════════════════════════════════════════════════════════
 * 라우터 meta.menu, menuConfig label 등에서 사용
 * ROUTES와 1:1 매핑되는 메뉴명 정의
 */

export const MENUS = {
  // ─────────────────────────────────────────────────────────────
  // Auth (인증)
  // ─────────────────────────────────────────────────────────────
  AUTH: {
    LOGIN: 'menu.auth.login',
    JOIN: 'menu.auth.join',
    OAUTH_CALLBACK: 'menu.auth.oauthCallback',
    TEMP_USER_APPROVAL: 'menu.hrm.tempUserApproval',
  },

  // ─────────────────────────────────────────────────────────────
  // HRM (인사관리)
  // ─────────────────────────────────────────────────────────────
  HRM: {
    USER_INFO: 'menu.hrm.userInfo',
    USER_MANAGEMENT: 'menu.hrm.userManagement',
    USER_MODIFY_MOBILE: 'menu.hrm.userModifyMobile',
    DEPT_TEAM: 'menu.hrm.deptTeam',
    PERFORMANCE_HISTORY: 'menu.hrm.performanceHistory',
    PERFORMANCE_MANAGEMENT: 'menu.hrm.performanceManagement',
    USER_PERMISSIONS: 'menu.hrm.userPermissions',
    ORG_DIRECTORY: 'menu.hrm.orgDirectory',
    FAVORITE_MENU: 'menu.hrm.favoriteMenu',
  },

  // ─────────────────────────────────────────────────────────────
  // INFO (정보)
  // ─────────────────────────────────────────────────────────────
  INFO: {
    SCHEDULE: 'menu.info.schedule',
    SCHEDULE_DAY_DETAIL: 'menu.info.scheduleDayDetail',
    SCHEDULE_STATISTICS: 'menu.info.scheduleStatistics',
    SCHEDULE_SETTINGS: 'menu.info.scheduleSettings',
    SCHEDULE_TYPE_MANAGEMENT: 'menu.info.scheduleTypeManagement',
    SCHEDULE_NOTIFICATION_HISTORY: 'menu.info.scheduleNotificationHistory',
    SCHEDULE_NOTIFICATION_HISTORY_DETAIL_MOBILE: 'menu.info.scheduleNotificationHistoryDetailMobile',
    NOTICE: 'menu.info.notice',
    RESOURCE: 'menu.info.resource',
    COMMUNITY: 'menu.info.community',
    POST_DETAIL: 'menu.info.postDetail',
    POST_CREATE_MOBILE: 'menu.info.postCreateMobile',
    POST_EDIT_MOBILE: 'menu.info.postEditMobile',
    GUIDE: 'menu.info.guide',
  },

  // ─────────────────────────────────────────────────────────────
  // Receipt (영수증)
  // ─────────────────────────────────────────────────────────────
  RECEIPT: {
    SUBMISSION: 'menu.receipt.submission',
    SUBMISSION_CREATE_MOBILE: 'menu.receipt.submissionCreateMobile',
    SUBMISSION_EDIT_MOBILE: 'menu.receipt.submissionEditMobile',
    PERSONAL_HISTORY: 'menu.receipt.personalHistory',
    DETAIL_MOBILE: 'menu.receipt.detailMobile',
    ANNUAL_SUMMARY: 'menu.receipt.annualSummary',
    APPROVAL_REQUEST: 'menu.receipt.approvalRequest',
    DELEGATE_APPROVAL: 'menu.receipt.delegateApproval',
    APPROVAL_DETAIL: 'menu.receipt.approvalDetail',
    DELEGATE_APPROVAL_DETAIL: 'menu.receipt.delegateApprovalDetail',
    CONCURRENCE_REQUEST: 'menu.receipt.concurrenceRequest',
    DELEGATE_CONCURRENCE: 'menu.receipt.delegateConcurrence',
    CONCURRENCE_DETAIL: 'menu.receipt.concurrenceDetail',
    DELEGATE_CONCURRENCE_DETAIL: 'menu.receipt.delegateConcurrenceDetail',
    HISTORY: 'menu.receipt.history',
    DELEGATE_HISTORY: 'menu.receipt.delegateHistory',
    HISTORY_DETAIL: 'menu.receipt.historyDetail',
    DELEGATE_HISTORY_DETAIL: 'menu.receipt.delegateHistoryDetail',
    APPROVAL_SUMMARY: 'menu.receipt.approvalSummary',
    APPROVAL_SUMMARY_DETAIL: 'menu.receipt.approvalSummaryDetail',
    REPORT_CEO: 'menu.receipt.reportCeo',
    CLOSURE: 'menu.receipt.closure',
    META_MANAGEMENT: 'menu.receipt.metaManagement',
    MANAGEMENT: 'menu.receipt.management',
  },

  // ─────────────────────────────────────────────────────────────
  // System (시스템)
  // ─────────────────────────────────────────────────────────────
  SYSTEM: {
    ACTIVITY_LOG: 'menu.system.activityLog',
    KAFKA_OUTBOX: 'menu.system.kafkaOutbox',
    STATISTICS: 'menu.system.statistics',
    AUTHORITY_MANAGEMENT: 'menu.system.authorityManagement',
    COMPONENT_TEST: 'menu.system.componentTest',
    PERSONAL_HISTORY: 'menu.system.personalHistory',
  },

  

  // ─────────────────────────────────────────────────────────────
  // Common (공통)
  // ─────────────────────────────────────────────────────────────
  COMMON: {
    MAIN: 'menu.common.main',
    GUIDE: 'menu.common.guide',
  },
};


/* ═══════════════════════════════════════════════════════════════
 * 전체 경로 상수 (Full Paths)
 * ═══════════════════════════════════════════════════════════════
 * menuConfig.js와 컴포넌트에서 사용 (prefix 포함)
 */

export const ROUTES = {
  // ─────────── 최상위 ───────────
  HOME: '/',
  MAIN: '/main',
  LOGIN: '/login',
  GUIDE: '/guide',
  
  // ─────────── 임시 (가입 등) ───────────
  TEMP: {
    JOIN: '/temp/join',
  },
  
  // ─────────── MANAGEMENT ───────────
  MANAGEMENT: {
    // 사용자
    USER_MANAGEMENT: '/management/user-management',
    USER_MODIFY_MOBILE: '/management/user-modify-mobile',
    
    // 조직
    DEPT_TEAM_MANAGER: '/management/dept-team-manager',
    ORG_DIRECTORY: '/management/org-directory',
    
    // 실적
    PERFORMANCE_HISTORY: '/management/performance-history',
    PERFORMANCE_MANAGEMENT: '/management/performance-management',
    
    // 권한
    USER_PERMISSIONS: '/management/user-permissions',
    
    // 즐겨찾기
    FAVORITE_MENU_MANAGEMENT: '/management/favorite-menu-management',
  },
  
  // ─────────── AUTH ───────────
  AUTH: {
    OAUTH_CALLBACK: '/auth/oauth-call-back',
    TEMP_USER_APPROVALS: '/auth/temp-user-approvals',
  },
  
  // ─────────── RECEIPT ───────────
  RECEIPT: {
    // 등록자
    SUBMISSION: '/receipt/receipt-submission',
    SUBMISSION_CREATE_MOBILE: '/receipt/receipt-submission-create',
    SUBMISSION_EDIT_MOBILE: '/receipt/receipt-submission-edit',
    PERSONAL_HISTORY: '/receipt/personal-receipt-history',
    DETAIL_MOBILE: '/receipt/receipt-detail-mobile',
    ANNUAL_SUMMARY: '/receipt/annual-receipt-summary',
    
    // 결재자
    APPROVAL_OVERVIEW: '/receipt/receipt-approval-request-overview',
    APPROVAL_DETAILS: '/receipt/receipt-approval-request-details',
    CONCURRENCE_OVERVIEW: '/receipt/receipt-concurrence-request-overview',
    CONCURRENCE_DETAILS: '/receipt/receipt-concurrence-request-details',
    HISTORY: '/receipt/receipt-history',
    HISTORY_DETAIL: '/receipt/receipt-history-detail',
    
    // 대리 결재자
    DELEGATE_APPROVAL_OVERVIEW: '/receipt/receipt-delegate-approval-request-overview',
    DELEGATE_APPROVAL_DETAILS: '/receipt/receipt-delegate-approval-request-details',
    DELEGATE_CONCURRENCE_OVERVIEW: '/receipt/receipt-delegate-concurrence-request-overview',
    DELEGATE_CONCURRENCE_DETAILS: '/receipt/receipt-delegate-concurrence-request-details',
    DELEGATE_HISTORY: '/receipt/receipt-delegate-history',
    DELEGATE_HISTORY_DETAIL: '/receipt/receipt-delegate-history-detail',
    
    // 검수자
    APPROVAL_SUMMARY: '/receipt/receipt-approval-summary',
    APPROVAL_SUMMARY_DETAIL: '/receipt/receipt-approval-summary-detail',
    REPORT_FOR_CEO: '/receipt/receipt-report-for-ceo',
    CLOSURE: '/receipt/receipt-closure',
    
    // 관리자
    MANAGEMENT: '/receipt/receipt-management',
    META_MANAGEMENT: '/receipt/receipt-meta-management',
  },
  
  // ─────────── USER ───────────
  USER: {
    USER_INFORMATION: '/user/user-information',
    PERSONAL_HISTORY: '/user/personal-history',
  },
  
  // ─────────── INFO ───────────
  INFO: {
    SCHEDULE_DAY_DETAIL: '/info/schedule-day-detail/:date',
    SCHEDULE_STATISTICS: '/info/schedule-statistics',
    SCHEDULE_SETTINGS: '/info/schedule-settings',
    SCHEDULE_CREATE_MOBILE: '/info/schedule-create-mobile',
    SCHEDULE_EDIT_MOBILE: '/info/schedule-edit-mobile',
    SCHEDULE_TYPE_MANAGEMENT: '/info/schedule-type-management',
    SCHEDULE_NOTIFICATION_HISTORY: '/info/schedule-notification-history',
    SCHEDULE_NOTIFICATION_HISTORY_DETAIL_MOBILE: '/info/schedule-notification-history-detail-mobile',
    NOTICE: '/info/notice',
    RESOURCE: '/info/resource',
    COMMUNITY: '/info/community',
    SCHEDULE: '/info/schedule',
    POST_DETAIL: '/info/post-detail',
    POST_CREATE_MOBILE: '/info/post-create-mobile',
    POST_EDIT_MOBILE: '/info/post-edit-mobile',
    GUIDE: '/info/guide',
  },
  
  // ─────────── SYSTEM ───────────
  SYSTEM: {
    ACTIVITY_LOG: '/system/activity-log',
    STATISTICS_SCREEN: '/system/statistics-screen',
    ROLE_MANAGEMENT: '/system/role-management',
    KAFKA_OUTBOX: '/system/kafka-outbox',
    COMPONENT_TEST: '/system/component-test',
  },
  
  
};

/* ═══════════════════════════════════════════════════════════════
 * 라우터 이름 상수 (Route Names)
 * ═══════════════════════════════════════════════════════════════
 * router.push({ name: ROUTE_NAMES.HRM.USER_MANAGEMENT }) 형태로 사용
 */

export const ROUTE_NAMES = {
  // 최상위
  MAIN: 'MainPage',
  GUIDE: 'GuidePage',
  
  // MANAGEMENT
  MANAGEMENT: {
    USER_MANAGEMENT: 'UserManagement',
    USER_MODIFY_MOBILE: 'UserManagementModifyMobile',
    DEPT_TEAM_MANAGER: 'DeptTeamManager',
    ORG_DIRECTORY: 'OrgDirectory',
    PERFORMANCE_HISTORY: 'PerformanceHistory',
    PERFORMANCE_MANAGEMENT: 'PerformanceManagement',
    USER_PERMISSIONS: 'UserPermissions',
    FAVORITE_MENU_MANAGEMENT: 'FavoriteMenuManagement',
  },
  
  // AUTH
  AUTH: {
    OAUTH_CALLBACK: 'OAuthCallback',
    TEMP_USER_APPROVALS: 'TempUserApprovals',
  },
  
  // RECEIPT
  RECEIPT: {
    SUBMISSION: 'ReceiptSubmission',
    SUBMISSION_CREATE_MOBILE: 'ReceiptSubmissionCreateMobile',
    SUBMISSION_EDIT_MOBILE: 'ReceiptSubmissionEditMobile',
    PERSONAL_HISTORY: 'PersonalReceiptHistory',
    DETAIL_MOBILE: 'ReceiptDetailViewMobile',
    ANNUAL_SUMMARY: 'AnnualReceiptSummary',
    APPROVAL_OVERVIEW: 'ReceiptApprovalRequestOverview',
    APPROVAL_DETAILS: 'ReceiptApprovalRequestDetails',
    CONCURRENCE_OVERVIEW: 'ReceiptConcurrenceRequestOverview',
    CONCURRENCE_DETAILS: 'ReceiptConcurrenceRequestDetails',
    HISTORY: 'ReceiptHistory',
    HISTORY_DETAIL: 'ReceiptHistoryDetail',
    DELEGATE_APPROVAL_OVERVIEW: 'ReceiptDelegateApprovalRequestOverview',
    DELEGATE_APPROVAL_DETAILS: 'ReceiptDelegateApprovalRequestDetails',
    DELEGATE_CONCURRENCE_OVERVIEW: 'ReceiptDelegateConcurrenceRequestOverview',
    DELEGATE_CONCURRENCE_DETAILS: 'ReceiptDelegateConcurrenceRequestDetails',
    DELEGATE_HISTORY: 'ReceiptDelegateHistory',
    DELEGATE_HISTORY_DETAIL: 'ReceiptDelegateHistoryDetail',
    APPROVAL_SUMMARY: 'ReceiptApprovalSummary',
    APPROVAL_SUMMARY_DETAIL: 'ReceiptApprovalSummaryDetail',
    REPORT_FOR_CEO: 'ReceiptReportForCEO',
    CLOSURE: 'ReceiptClosure',
    MANAGEMENT: 'ReceiptManagement',
    META_MANAGEMENT: 'ReceiptMetaManagement',
  },
  
  // USER
  USER: {
    USER_INFORMATION: 'UserInformation',
    PERSONAL_HISTORY: 'PersonalHistory',
  },
  
  // INFO
  INFO: {
    SCHEDULE_DAY_DETAIL: 'ScheduleDayDetail',
    SCHEDULE_STATISTICS: 'ScheduleStatistics',
    SCHEDULE_SETTINGS: 'ScheduleSettings',
    SCHEDULE_CREATE_MOBILE: 'ScheduleCreateMobile',
    SCHEDULE_EDIT_MOBILE: 'ScheduleEditMobile',
    SCHEDULE_TYPE_MANAGEMENT: 'ScheduleTypeManagement',
    SCHEDULE_NOTIFICATION_HISTORY: 'ScheduleNotificationHistory',
    SCHEDULE_NOTIFICATION_HISTORY_DETAIL_MOBILE: 'ScheduleNotificationHistoryDetailMobile',
    NOTICE: 'InfoNoticeBoard',
    RESOURCE: 'InfoResourceBoard',
    COMMUNITY: 'InfoCommunityBoard',
    SCHEDULE: 'ScheduleCalendar',
    POST_DETAIL: 'InfoPostDetail',
    POST_CREATE_MOBILE: 'InfoPostCreateMobile',
    POST_EDIT_MOBILE: 'InfoPostEditMobile',
    GUIDE: 'InfoGuide',
  },
  
  // SYSTEM
  SYSTEM: {
    ACTIVITY_LOG: 'ActivityLog',
    STATISTICS_SCREEN: 'StatisticsScreen',
    ROLE_MANAGEMENT: 'RoleManagement',
    KAFKA_OUTBOX: 'KafkaOutboxMonitor',
    COMPONENT_TEST: 'ComponentTest',
  },
  
  
};

/* ═══════════════════════════════════════════════════════════════
 * 유틸리티 함수
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 경로명으로 전체 경로 반환
 * @param {string} pathName - 예: 'notice-integrated'
 * @param {string} prefix - 예: 'hrm'
 * @returns {string} 전체 경로
 */
export function getFullPath(pathName, prefix) {
  return `/${prefix}/${pathName}`;
}

/**
 * 전체 경로로 라우터 이름 찾기
 * @param {string} fullPath - 예: '/hrm/user-management'
 * @returns {string|null} 라우터 이름
 */
export function findRouteNameByPath(fullPath) {
  // ROUTES를 순회하며 일치하는 경로 찾기
  for (const [category, paths] of Object.entries(ROUTES)) {
    if (typeof paths === 'string') {
      if (paths === fullPath) return ROUTE_NAMES[category];
    } else {
      for (const [key, path] of Object.entries(paths)) {
        if (path === fullPath) return ROUTE_NAMES[category][key];
      }
    }
  }
  return null;
}

/**
 * 라우터 이름으로 전체 경로 찾기
 * @param {string} routeName - 예: 'UserManagement'
 * @returns {string|null} 전체 경로
 */
export function findPathByRouteName(routeName) {
  for (const [category, names] of Object.entries(ROUTE_NAMES)) {
    if (typeof names === 'string') {
      if (names === routeName) return ROUTES[category];
    } else {
      for (const [key, name] of Object.entries(names)) {
        if (name === routeName) return ROUTES[category][key];
      }
    }
  }
  return null;
}

