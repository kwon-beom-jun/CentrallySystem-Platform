import { getRolesFrom } from '@/utils/roleUtils';
import { WORKSPACES, WORKSPACE_METADATA, CATEGORIES, ROLE_GROUPS } from '@/config/roleConfig';
import { ROUTES, MENUS } from '@/config/routeConfig';

/* ═══════════════════════════════════════════════════════════════
 * 전체 메뉴 구조 정의 (Central Menu Configuration)
 * ═══════════════════════════════════════════════════════════════
 * 
 * 이 파일에서 모든 메뉴를 중앙 관리합니다.
 * - FavoriteMenuManagement.vue
 * - NavigationLayout.vue
 * - SecondSidebar.vue
 * 
 * 메뉴 추가/수정 시 이 파일만 수정하면 됩니다.
 * 
 * - 워크스페이스/카테고리: roleConfig.js에서 import
 * - 경로: routeConfig.js에서 import
 */

/* ═══════════════════════════════════════════════════════════════
 * roleConfig.js에서 re-export
 * ═══════════════════════════════════════════════════════════════ */
export { WORKSPACES, WORKSPACE_METADATA as WORKSPACE_INFO, CATEGORIES };

/* ═══════════════════════════════════════════════════════════════
 * routeConfig.js에서 re-export
 * ═══════════════════════════════════════════════════════════════ */
export { ROUTES };

/* ═══════════════════════════════════════════════════════════════
 * 카테고리 → i18n 키 매핑 함수
 * ═══════════════════════════════════════════════════════════════ */
function getCategoryI18nKey(category) {
  const categoryMap = {
    [CATEGORIES.INFO_SCHEDULE_MANAGEMENT]: 'menu.category.scheduleManagement',
    [CATEGORIES.INFO_BOARD]: 'menu.category.board',
    [CATEGORIES.INFO_GUIDE]: 'menu.category.guide',
    [CATEGORIES.RECEIPT_REGISTRAR]: 'menu.category.registrar',
    [CATEGORIES.RECEIPT_APPROVER]: 'menu.category.approver',
    [CATEGORIES.RECEIPT_PROXY]: 'menu.category.proxy',
    [CATEGORIES.RECEIPT_INSPECTOR]: 'menu.category.inspector',
    [CATEGORIES.RECEIPT_MANAGER]: 'menu.category.manager',
    [CATEGORIES.HRM_USER_AUTH]: 'menu.category.userAuth',
    [CATEGORIES.HRM_ORGANIZATION]: 'menu.category.organization',
    [CATEGORIES.HRM_PERFORMANCE]: 'menu.category.performance',
    [CATEGORIES.HRM_APPROVAL]: 'menu.category.approval',
    [CATEGORIES.USER_INFO]: 'menu.category.personal',
    [CATEGORIES.SYSTEM_ADMIN]: 'menu.category.systemAdmin',
  };
  return categoryMap[category] || category;
}

/* ═══════════════════════════════════════════════════════════════
 * 전체 메뉴 구조
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 일정 관리 메뉴
 */
export const SCHEDULE_MENUS = [
  {
    path: ROUTES.INFO.SCHEDULE,
    label: MENUS.INFO.SCHEDULE,
    i18nKey: 'menu.info.calendar',
    icon: 'ri-calendar-line',
    workspace: WORKSPACES.INFO,
    category: CATEGORIES.INFO_SCHEDULE_MANAGEMENT,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.INFO_SCHEDULE_MANAGEMENT),
    roles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
  },
  // 일정 통계 (현재 사용 안함)
  // {
  //   path: ROUTES.INFO.SCHEDULE_STATISTICS,
  //   label: MENUS.INFO.SCHEDULE_STATISTICS,
  //   i18nKey: 'menu.info.scheduleStatistics',
  //   icon: 'ri-bar-chart-line',
  //   workspace: WORKSPACES.INFO,
  //   category: CATEGORIES.INFO_SCHEDULE_MANAGEMENT,
  //   categoryI18nKey: getCategoryI18nKey(CATEGORIES.INFO_SCHEDULE_MANAGEMENT),
  //   roles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
  // },
  {
    path: ROUTES.INFO.SCHEDULE_SETTINGS,
    label: MENUS.INFO.SCHEDULE_SETTINGS,
    i18nKey: 'menu.info.scheduleSettings',
    icon: 'ri-settings-3-line',
    workspace: WORKSPACES.INFO,
    category: CATEGORIES.INFO_SCHEDULE_MANAGEMENT,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.INFO_SCHEDULE_MANAGEMENT),
    roles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
  },
  {
    path: ROUTES.INFO.SCHEDULE_TYPE_MANAGEMENT,
    label: MENUS.INFO.SCHEDULE_TYPE_MANAGEMENT,
    i18nKey: 'menu.info.scheduleTypeManagement',
    icon: 'ri-list-settings-line',
    workspace: WORKSPACES.INFO,
    category: CATEGORIES.INFO_SCHEDULE_MANAGEMENT,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.INFO_SCHEDULE_MANAGEMENT),
    roles: getRolesFrom(ROLE_GROUPS.INFO_ADMIN),
  },
  {
    path: ROUTES.INFO.SCHEDULE_NOTIFICATION_HISTORY,
    label: MENUS.INFO.SCHEDULE_NOTIFICATION_HISTORY,
    i18nKey: 'menu.info.scheduleNotificationHistory',
    icon: 'ri-history-line',
    workspace: WORKSPACES.INFO,
    category: CATEGORIES.INFO_SCHEDULE_MANAGEMENT,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.INFO_SCHEDULE_MANAGEMENT),
    roles: getRolesFrom(ROLE_GROUPS.INFO_ADMIN),
  },
];

/**
 * INFO 메뉴
 */
export const INFO_MENUS = [
  {
    path: ROUTES.INFO.NOTICE,
    label: MENUS.INFO.NOTICE,
    i18nKey: 'menu.info.notice',
    icon: 'ri-megaphone-line',
    workspace: WORKSPACES.INFO,
    category: CATEGORIES.INFO_BOARD,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.INFO_BOARD),
    roles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
  },
  {
    path: ROUTES.INFO.RESOURCE,
    label: MENUS.INFO.RESOURCE,
    i18nKey: 'menu.info.resource',
    icon: 'ri-folder-line',
    workspace: WORKSPACES.INFO,
    category: CATEGORIES.INFO_BOARD,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.INFO_BOARD),
    roles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
  },
  {
    path: ROUTES.INFO.COMMUNITY,
    label: MENUS.INFO.COMMUNITY,
    i18nKey: 'menu.info.community',
    icon: 'ri-chat-3-line',
    workspace: WORKSPACES.INFO,
    category: CATEGORIES.INFO_BOARD,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.INFO_BOARD),
    roles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
  },
];

/**
 * 가이드 메뉴
 */
export const GUIDE_MENU = {
  path: ROUTES.INFO.GUIDE,
  label: MENUS.INFO.GUIDE,
  i18nKey: 'menu.info.guide',
  icon: 'ri-guide-line',
  workspace: WORKSPACES.INFO,
  category: CATEGORIES.INFO_GUIDE,
  categoryI18nKey: getCategoryI18nKey(CATEGORIES.INFO_GUIDE),
  roles: [],
};

/**
 * 영수증 메뉴 - 등록자
 */
export const RECEIPT_REGISTRAR_MENUS = [
  {
    path: ROUTES.RECEIPT.SUBMISSION,
    label: MENUS.RECEIPT.SUBMISSION,
    i18nKey: 'menu.receipt.submission',
    icon: 'ri-file-add-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_REGISTRAR,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_REGISTRAR),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
  },
  {
    path: ROUTES.RECEIPT.PERSONAL_HISTORY,
    label: MENUS.RECEIPT.PERSONAL_HISTORY,
    i18nKey: 'menu.receipt.personalHistory',
    icon: 'ri-file-list-3-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_REGISTRAR,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_REGISTRAR),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
  },
  {
    path: ROUTES.RECEIPT.ANNUAL_SUMMARY,
    label: MENUS.RECEIPT.ANNUAL_SUMMARY,
    i18nKey: 'menu.receipt.annualSummary',
    icon: 'ri-calendar-2-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_REGISTRAR,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_REGISTRAR),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
  },
];

/**
 * 영수증 메뉴 - 결재자
 */
export const RECEIPT_APPROVER_MENUS = [
  {
    path: ROUTES.RECEIPT.APPROVAL_OVERVIEW,
    label: MENUS.RECEIPT.APPROVAL_REQUEST,
    i18nKey: 'menu.receipt.approvalRequest',
    icon: 'ri-survey-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_APPROVER,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_APPROVER),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
  },
  {
    path: ROUTES.RECEIPT.CONCURRENCE_OVERVIEW,
    label: MENUS.RECEIPT.CONCURRENCE_REQUEST,
    i18nKey: 'menu.receipt.concurrenceRequest',
    icon: 'ri-hand-coin-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_APPROVER,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_APPROVER),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
  },
  {
    path: ROUTES.RECEIPT.HISTORY,
    label: MENUS.RECEIPT.HISTORY,
    i18nKey: 'menu.receipt.history',
    icon: 'ri-history-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_APPROVER,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_APPROVER),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
  },
];

/**
 * 영수증 메뉴 - 대리 결재자
 */
export const RECEIPT_PROXY_MENUS = [
  {
    path: ROUTES.RECEIPT.DELEGATE_APPROVAL_OVERVIEW,
    label: MENUS.RECEIPT.DELEGATE_APPROVAL,
    i18nKey: 'menu.receipt.delegateApproval',
    icon: 'ri-user-shared-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_PROXY,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_PROXY),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
  },
  {
    path: ROUTES.RECEIPT.DELEGATE_CONCURRENCE_OVERVIEW,
    label: MENUS.RECEIPT.DELEGATE_CONCURRENCE,
    i18nKey: 'menu.receipt.delegateConcurrence',
    icon: 'ri-user-follow-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_PROXY,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_PROXY),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
  },
  {
    path: ROUTES.RECEIPT.DELEGATE_HISTORY,
    label: MENUS.RECEIPT.DELEGATE_HISTORY,
    i18nKey: 'menu.receipt.delegateHistory',
    icon: 'ri-history-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_PROXY,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_PROXY),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
  },
];

/**
 * 영수증 메뉴 - 검수자
 */
export const RECEIPT_INSPECTOR_MENUS = [
  {
    path: ROUTES.RECEIPT.APPROVAL_SUMMARY,
    label: MENUS.RECEIPT.APPROVAL_SUMMARY,
    i18nKey: 'menu.receipt.approvalSummary',
    icon: 'ri-checkbox-circle-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_INSPECTOR,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_INSPECTOR),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR),
  },
  {
    path: ROUTES.RECEIPT.REPORT_FOR_CEO,
    label: MENUS.RECEIPT.REPORT_CEO,
    i18nKey: 'menu.receipt.reportCeo',
    icon: 'ri-file-chart-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_INSPECTOR,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_INSPECTOR),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR),
  },
  {
    path: ROUTES.RECEIPT.CLOSURE,
    label: MENUS.RECEIPT.CLOSURE,
    i18nKey: 'menu.receipt.closure',
    icon: 'ri-lock-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_INSPECTOR,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_INSPECTOR),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR),
  },
];

/**
 * 영수증 메뉴 - 관리자
 */
export const RECEIPT_MANAGER_MENUS = [
  {
    path: ROUTES.RECEIPT.MANAGEMENT,
    label: MENUS.RECEIPT.MANAGEMENT,
    i18nKey: 'menu.receipt.management',
    icon: 'ri-database-2-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_MANAGER,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_MANAGER),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_MANAGER),
  },
  {
    path: ROUTES.RECEIPT.META_MANAGEMENT,
    label: MENUS.RECEIPT.META_MANAGEMENT,
    i18nKey: 'menu.receipt.metaManagement',
    icon: 'ri-settings-3-line',
    workspace: WORKSPACES.RECEIPT,
    category: CATEGORIES.RECEIPT_MANAGER,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.RECEIPT_MANAGER),
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_MANAGER),
  },
];

/**
 * 사원관리 메뉴 - 사용자·권한
 */
export const HRM_USER_AUTH_MENUS = [
  {
    path: ROUTES.MANAGEMENT.USER_MANAGEMENT,
    label: MENUS.HRM.USER_MANAGEMENT,
    i18nKey: 'menu.hrm.userManagement',
    icon: 'ri-user-settings-line',
    workspace: WORKSPACES.MANAGEMENT,
    category: CATEGORIES.HRM_USER_AUTH,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.HRM_USER_AUTH),
    roles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER),
  },
  {
    path: ROUTES.MANAGEMENT.USER_PERMISSIONS,
    label: MENUS.HRM.USER_PERMISSIONS,
    i18nKey: 'menu.hrm.userPermissions',
    icon: 'ri-key-2-line',
    workspace: WORKSPACES.MANAGEMENT,
    category: CATEGORIES.HRM_USER_AUTH,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.HRM_USER_AUTH),
    roles: getRolesFrom(ROLE_GROUPS.HRM_ASSISTANT_MANAGER),
  },
];

/**
 * 사원관리 메뉴 - 조직
 */
export const HRM_ORGANIZATION_MENUS = [
  {
    path: ROUTES.MANAGEMENT.DEPT_TEAM_MANAGER,
    label: MENUS.HRM.DEPT_TEAM,
    i18nKey: 'menu.hrm.deptTeam',
    icon: 'ri-building-line',
    workspace: WORKSPACES.MANAGEMENT,
    category: CATEGORIES.HRM_ORGANIZATION,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.HRM_ORGANIZATION),
    roles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER),
  },
  {
    path: ROUTES.MANAGEMENT.ORG_DIRECTORY,
    label: MENUS.HRM.ORG_DIRECTORY,
    i18nKey: 'menu.hrm.orgDirectory',
    icon: 'ri-group-line',
    workspace: WORKSPACES.MANAGEMENT,
    category: CATEGORIES.HRM_ORGANIZATION,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.HRM_ORGANIZATION),
    roles: getRolesFrom(ROLE_GROUPS.HRM_EMPLOYEE),
  },
];

/**
 * 사원관리 메뉴 - 실적
 */
export const HRM_PERFORMANCE_MENUS = [
  {
    path: ROUTES.MANAGEMENT.PERFORMANCE_MANAGEMENT,
    label: MENUS.HRM.PERFORMANCE_MANAGEMENT,
    i18nKey: 'menu.hrm.performanceManagement',
    icon: 'ri-bar-chart-2-line',
    workspace: WORKSPACES.MANAGEMENT,
    category: CATEGORIES.HRM_PERFORMANCE,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.HRM_PERFORMANCE),
    roles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER),
  },
];

/**
 * 사원관리 메뉴 - 가입 승인
 */
export const HRM_APPROVAL_MENUS = [
  {
    path: ROUTES.AUTH.TEMP_USER_APPROVALS,
    label: MENUS.AUTH.TEMP_USER_APPROVAL,
    i18nKey: 'menu.hrm.tempUserApproval',
    icon: 'ri-user-add-line',
    workspace: WORKSPACES.MANAGEMENT,
    category: CATEGORIES.HRM_APPROVAL,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.HRM_APPROVAL),
    roles: getRolesFrom(ROLE_GROUPS.HRM_ASSISTANT_MANAGER),
  },
];

/**
 * 시스템 메뉴 - 시스템 관리
 */
export const SYSTEM_ADMIN_MENUS = [
  {
    path: ROUTES.SYSTEM.ACTIVITY_LOG,
    label: MENUS.SYSTEM.ACTIVITY_LOG,
    i18nKey: 'menu.system.activityLog',
    icon: 'ri-clipboard-line',
    workspace: WORKSPACES.SYSTEM,
    category: CATEGORIES.SYSTEM_ADMIN,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.SYSTEM_ADMIN),
    roles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
  },
  {
    path: ROUTES.SYSTEM.STATISTICS_SCREEN,
    label: MENUS.SYSTEM.STATISTICS,
    i18nKey: 'menu.system.statistics',
    icon: 'ri-dashboard-3-line',
    workspace: WORKSPACES.SYSTEM,
    category: CATEGORIES.SYSTEM_ADMIN,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.SYSTEM_ADMIN),
    roles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
  },
  {
    path: ROUTES.SYSTEM.KAFKA_OUTBOX,
    label: MENUS.SYSTEM.KAFKA_OUTBOX,
    i18nKey: 'menu.system.kafkaOutbox',
    icon: 'ri-stack-line',
    workspace: WORKSPACES.SYSTEM,
    category: CATEGORIES.SYSTEM_ADMIN,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.SYSTEM_ADMIN),
    roles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
  },
];

/**
 * 개인정보 메뉴 - 사용자 정보
 */
export const USER_INFO_MENUS = [
  {
    path: ROUTES.USER.USER_INFORMATION,
    label: MENUS.HRM.USER_INFO,
    i18nKey: 'menu.hrm.userInfo',
    icon: 'ri-user-line',
    workspace: WORKSPACES.USER,
    category: CATEGORIES.USER_INFO,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.USER_INFO),
    roles: getRolesFrom(ROLE_GROUPS.HRM_EMPLOYEE),
  },
];

/**
 * 개인정보 메뉴 - 개인 이력
 */
export const SYSTEM_USER_MENUS = [
  {
    path: ROUTES.USER.PERSONAL_HISTORY,
    label: MENUS.SYSTEM.PERSONAL_HISTORY,
    i18nKey: 'menu.system.personalHistory',
    icon: 'ri-file-user-line',
    workspace: WORKSPACES.USER,
    category: CATEGORIES.USER_INFO,
    categoryI18nKey: getCategoryI18nKey(CATEGORIES.USER_INFO),
    roles: [], // 모든 사용자
  },
];

/**
 * 시스템 메뉴 - 통합
 */
export const SYSTEM_MENUS = [
  ...SYSTEM_ADMIN_MENUS,
];

/* ═══════════════════════════════════════════════════════════════
 * 통합 메뉴 목록
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 모든 메뉴를 하나의 배열로 통합
 * @returns {Array} 전체 메뉴 배열
 */
export function getAllMenus() {
  return [
    ...SCHEDULE_MENUS,
    ...INFO_MENUS,
    GUIDE_MENU,
    ...RECEIPT_REGISTRAR_MENUS,
    ...RECEIPT_APPROVER_MENUS,
    ...RECEIPT_PROXY_MENUS,
    ...RECEIPT_INSPECTOR_MENUS,
    ...RECEIPT_MANAGER_MENUS,
    ...HRM_USER_AUTH_MENUS,
    ...HRM_ORGANIZATION_MENUS,
    ...HRM_PERFORMANCE_MENUS,
    ...HRM_APPROVAL_MENUS,
    ...USER_INFO_MENUS,
    ...SYSTEM_ADMIN_MENUS,
    ...SYSTEM_USER_MENUS,
  ];
}

/**
 * 워크스페이스별로 그룹화된 메뉴
 * @returns {Object} 워크스페이스별 메뉴 객체
 */
export function getMenusByWorkspace() {
  return {
    [WORKSPACES.INFO]: [
      ...SCHEDULE_MENUS,
      ...INFO_MENUS,
      GUIDE_MENU,
    ],
    [WORKSPACES.RECEIPT]: [
      ...RECEIPT_REGISTRAR_MENUS,
      ...RECEIPT_APPROVER_MENUS,
      ...RECEIPT_PROXY_MENUS,
      ...RECEIPT_INSPECTOR_MENUS,
      ...RECEIPT_MANAGER_MENUS,
    ],
    [WORKSPACES.MANAGEMENT]: [
      ...HRM_USER_AUTH_MENUS,
      ...HRM_ORGANIZATION_MENUS,
      ...HRM_PERFORMANCE_MENUS,
      ...HRM_APPROVAL_MENUS,
    ],
    [WORKSPACES.USER]: [
      ...USER_INFO_MENUS,
      ...SYSTEM_USER_MENUS,
    ],
    [WORKSPACES.SYSTEM]: SYSTEM_MENUS,
  };
}

/**
 * 카테고리별로 그룹화된 메뉴
 * @returns {Object} 카테고리별 메뉴 객체
 */
export function getMenusByCategory() {
  return {
    // INFO
    [CATEGORIES.INFO_SCHEDULE_MANAGEMENT]: SCHEDULE_MENUS,
    [CATEGORIES.INFO_BOARD]: INFO_MENUS,
    [CATEGORIES.INFO_GUIDE]: [GUIDE_MENU],
    
    // 영수증
    [CATEGORIES.RECEIPT_REGISTRAR]: RECEIPT_REGISTRAR_MENUS,
    [CATEGORIES.RECEIPT_APPROVER]: RECEIPT_APPROVER_MENUS,
    [CATEGORIES.RECEIPT_PROXY]: RECEIPT_PROXY_MENUS,
    [CATEGORIES.RECEIPT_INSPECTOR]: RECEIPT_INSPECTOR_MENUS,
    [CATEGORIES.RECEIPT_MANAGER]: RECEIPT_MANAGER_MENUS,
    
    // 사원관리
    [CATEGORIES.HRM_USER_AUTH]: HRM_USER_AUTH_MENUS,
    [CATEGORIES.HRM_ORGANIZATION]: HRM_ORGANIZATION_MENUS,
    [CATEGORIES.HRM_PERFORMANCE]: HRM_PERFORMANCE_MENUS,
    [CATEGORIES.HRM_APPROVAL]: HRM_APPROVAL_MENUS,
    
    // 개인정보
    [CATEGORIES.USER_INFO]: [
      ...USER_INFO_MENUS,
      ...SYSTEM_USER_MENUS,
    ],
    
    // 시스템
    [CATEGORIES.SYSTEM_ADMIN]: SYSTEM_ADMIN_MENUS,
  };
}

/**
 * NavigationLayout용 메뉴 구조 생성
 * @returns {Array} NavigationLayout 형식의 메뉴 배열
 */
/**
 * NavigationLayout용 메뉴 구조 생성
 * @returns {Array} NavigationLayout 형식의 메뉴 배열
 * 
 * NOTE: i18n 키를 사용합니다.
 * - label: i18n 키 (예: 'menu.info.notice')
 * - 컴포넌트에서 $t(label)로 번역된 텍스트 표시
 */
export function getNavigationMenus() {
  return [
    {
      key: 'info',
      label: 'common.sidebar.workspace.info',  // i18n 키
      icon: 'ri-information-line',
      roles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      sub: [
        ...SCHEDULE_MENUS.map(m => ({
          to: m.path,
          label: m.i18nKey || m.label,  // i18n 키 우선 사용
          roles: m.roles,
        })),
        ...INFO_MENUS.map(m => ({
          to: m.path,
          label: m.i18nKey || m.label,  // i18n 키 우선 사용
          roles: m.roles,
        })),
        {
          to: GUIDE_MENU.path,
          label: GUIDE_MENU.i18nKey || GUIDE_MENU.label,  // i18n 키 우선 사용
          roles: GUIDE_MENU.roles,
        },
      ],
    },
    {
      key: 'receipt',
      label: 'common.sidebar.workspace.receipt',  // i18n 키
      icon: 'ri-file-list-2-line',
      roles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
      sub: [
        ...RECEIPT_REGISTRAR_MENUS,
        ...RECEIPT_APPROVER_MENUS,
        ...RECEIPT_PROXY_MENUS,
        ...RECEIPT_INSPECTOR_MENUS,
        ...RECEIPT_MANAGER_MENUS,
      ].map(m => ({
        to: m.path,
        label: m.i18nKey || m.label,  // i18n 키 우선 사용
        roles: m.roles,
      })),
    },
    {
      key: 'management',
      label: 'common.sidebar.workspace.management',  // i18n 키
      icon: 'ri-team-line',
      roles: getRolesFrom(ROLE_GROUPS.HRM_EMPLOYEE),
      sub: [
        ...HRM_USER_AUTH_MENUS,
        ...HRM_ORGANIZATION_MENUS,
        ...HRM_PERFORMANCE_MENUS,
        ...HRM_APPROVAL_MENUS,
      ].map(m => ({
        to: m.path,
        label: m.i18nKey || m.label,  // i18n 키 우선 사용
        roles: m.roles,
      })),
    },
    {
      key: 'user',
      label: 'common.sidebar.workspace.user',  // i18n 키
      icon: 'ri-user-line',
      sub: [
        ...USER_INFO_MENUS,
        ...SYSTEM_USER_MENUS,
      ].map(m => ({
        to: m.path,
        label: m.i18nKey || m.label,  // i18n 키 우선 사용
        roles: m.roles,
      })),
    },
    {
      key: 'system',
      label: 'common.sidebar.workspace.system',  // i18n 키
      icon: 'ri-settings-3-line',
      sub: SYSTEM_MENUS.map(m => ({
        to: m.path,
        label: m.i18nKey || m.label,  // i18n 키 우선 사용
        roles: m.roles,
      })),
    },
  ];
}

/* ═══════════════════════════════════════════════════════════════
 * 유틸리티 함수
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 경로로 메뉴 찾기
 * @param {string} path - 메뉴 경로
 * @returns {Object|null} 메뉴 객체 또는 null
 */
export function findMenuByPath(path) {
  return getAllMenus().find(menu => menu.path === path) || null;
}

/**
 * 워크스페이스로 메뉴 필터링
 * @param {string} workspace - 워크스페이스 키
 * @returns {Array} 해당 워크스페이스의 메뉴 배열
 */
export function filterMenusByWorkspace(workspace) {
  return getAllMenus().filter(menu => menu.workspace === workspace);
}

/**
 * 권한으로 메뉴 필터링
 * @param {Array} userRoles - 사용자 권한 배열
 * @returns {Array} 권한이 있는 메뉴 배열
 */
export function filterMenusByRoles(userRoles) {
  return getAllMenus().filter(menu => {
    if (!menu.roles || menu.roles.length === 0) return true;
    return menu.roles.some(role => userRoles.includes(role));
  });
}

