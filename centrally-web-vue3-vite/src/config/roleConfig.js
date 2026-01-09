/* ═══════════════════════════════════════════════════════════════
 * 권한 설정 (Role Configuration)
 * ═══════════════════════════════════════════════════════════════
 * 
 * 이 파일에서 권한 관련 모든 설정을 중앙 관리합니다:
 * - 권한 계층 구조
 * - 권한 상수 (roleConstants.js에서 import)
 * - 권한별 라벨, 아이콘, 색상
 * - 워크스페이스, 카테고리
 */

import * as RoleConst from '@/constants/roleConstants';

// roleConstants.js의 모든 권한 객체를 re-export
export { 
  HRM_ROLES, 
  RECEIPT_ROLES, 
  INFO_ROLES, 
  SYSTEM_ROLES,
  RECEIPT_APPROVAL_ROLES,
  RECEIPT_DELEGATE_ROLES,
  INFO_WRITE_ROLES
} from '@/constants/roleConstants';

/* ═══════════════════════════════════════════════════════════════
 * 권한 계층 구조 (Role Hierarchy)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 권한 계층 구조 (하위 → 상위 순서)
 * roleUtils.js에서 getRolesFrom() 함수가 이 구조를 참조합니다.
 */
export const roleHierarchy = {
  hrm: [
    'ROLE_HRM_EMPLOYEE',        // 하위
    'ROLE_HRM_LEADER',
    'ROLE_HRM_ASSISTANT_MANAGER',
    'ROLE_HRM_MANAGER'          // 상위
  ],
  receipt: [
    'ROLE_RECEIPT_REGISTRAR',   // 하위
    'ROLE_RECEIPT_APPROVER',
    'ROLE_RECEIPT_INSPECTOR',
    'ROLE_RECEIPT_FINALIZER',
    'ROLE_RECEIPT_MANAGER',     // 상위
  ],
  info: [
    'ROLE_INFO_GUEST',          // 하위
    'ROLE_INFO_USER',
    'ROLE_INFO_MANAGER',
    'ROLE_INFO_ADMIN'           // 상위
  ],
  system: [
    'ROLE_GATE_SYSTEM'
  ]
};

/* ═══════════════════════════════════════════════════════════════
 * 권한 상수 정의 (roleConstants.js에서 import)
 * ═══════════════════════════════════════════════════════════════ */

// 시스템 권한
export const ROLE_GATE_SYSTEM = RoleConst.SYSTEM_ROLES.ADMIN;

// HRM 권한
export const ROLE_HRM_EMPLOYEE = RoleConst.HRM_ROLES.EMPLOYEE;
export const ROLE_HRM_LEADER = RoleConst.HRM_ROLES.LEADER;
export const ROLE_HRM_ASSISTANT_MANAGER = RoleConst.HRM_ROLES.ASSISTANT_MANAGER;
export const ROLE_HRM_MANAGER = RoleConst.HRM_ROLES.MANAGER;

// RECEIPT 권한
export const ROLE_RECEIPT_REGISTRAR = RoleConst.RECEIPT_ROLES.REGISTRAR;
export const ROLE_RECEIPT_APPROVER = RoleConst.RECEIPT_ROLES.APPROVER;
export const ROLE_RECEIPT_INSPECTOR = RoleConst.RECEIPT_ROLES.INSPECTOR;
export const ROLE_RECEIPT_FINALIZER = RoleConst.RECEIPT_ROLES.FINALIZER;
export const ROLE_RECEIPT_MANAGER = RoleConst.RECEIPT_ROLES.MANAGER;

// INFO 권한
export const ROLE_INFO_GUEST = RoleConst.INFO_ROLES.GUEST;
export const ROLE_INFO_USER = RoleConst.INFO_ROLES.USER;
export const ROLE_INFO_MANAGER = RoleConst.INFO_ROLES.MANAGER;
export const ROLE_INFO_ADMIN = RoleConst.INFO_ROLES.ADMIN;

/* ═══════════════════════════════════════════════════════════════
 * 권한 그룹 별칭
 * ═══════════════════════════════════════════════════════════════ */

export const ROLE_GROUPS = {
  HRM_EMPLOYEE: 'hrm_employee',
  HRM_LEADER: 'hrm_leader',
  HRM_ASSISTANT_MANAGER: 'hrm_assistant_manager',
  HRM_MANAGER: 'hrm_manager',
  
  RECEIPT_REGISTRAR: 'receipt_registrar',
  RECEIPT_APPROVER: 'receipt_approver',
  RECEIPT_INSPECTOR: 'receipt_inspector',
  RECEIPT_FINALIZER: 'receipt_finalizer',
  RECEIPT_MANAGER: 'receipt_manager',
  
  INFO_GUEST: 'info_guest',
  INFO_USER: 'info_user',
  INFO_MANAGER: 'info_manager',
  INFO_ADMIN: 'info_admin',
  
  SYSTEM_GATE_SYSTEM: 'system_gate_system',
};

/* ═══════════════════════════════════════════════════════════════
 * 권한별 메타데이터 (라벨, 아이콘, 색상, 카테고리)
 * ═══════════════════════════════════════════════════════════════ */

export const ROLE_METADATA = {
  // 시스템
  [ROLE_GATE_SYSTEM]: {
    label: 'SYSTEM (모든 권한)',
    icon: 'ri-shield-star-line',
    badgeClass: 'role-badge--system',
    color: '#db2777',
    bgColor: '#fce7f3',
    borderColor: '#f472b6',
  },
  
  // HRM
  [ROLE_HRM_EMPLOYEE]: {
    label: 'HRM 사원',
    icon: 'ri-user-line',
    badgeClass: 'role-badge--default',
    color: '#6b7280',
    bgColor: '#f3f4f6',
    borderColor: '#d1d5db',
  },
  [ROLE_HRM_LEADER]: {
    label: 'HRM 리더',
    icon: 'ri-user-star-line',
    badgeClass: 'role-badge--default',
    color: '#6b7280',
    bgColor: '#f3f4f6',
    borderColor: '#d1d5db',
  },
  [ROLE_HRM_ASSISTANT_MANAGER]: {
    label: 'HRM 서브관리자',
    icon: 'ri-user-settings-line',
    badgeClass: 'role-badge--manager',
    color: '#d97706',
    bgColor: '#fef3c7',
    borderColor: '#fbbf24',
  },
  [ROLE_HRM_MANAGER]: {
    label: 'HRM 관리자',
    icon: 'ri-user-settings-line',
    badgeClass: 'role-badge--manager',
    color: '#d97706',
    bgColor: '#fef3c7',
    borderColor: '#fbbf24',
  },
  
  // RECEIPT
  [ROLE_RECEIPT_REGISTRAR]: {
    label: 'RECEIPT 등록자',
    icon: 'ri-receipt-line',
    badgeClass: 'role-badge--default',
    color: '#6b7280',
    bgColor: '#f3f4f6',
    borderColor: '#d1d5db',
  },
  [ROLE_RECEIPT_APPROVER]: {
    label: 'RECEIPT 결재자',
    icon: 'ri-receipt-line',
    badgeClass: 'role-badge--approver',
    color: '#6366f1',
    bgColor: '#e0e7ff',
    borderColor: '#818cf8',
  },
  [ROLE_RECEIPT_INSPECTOR]: {
    label: 'RECEIPT 검수자',
    icon: 'ri-receipt-line',
    badgeClass: 'role-badge--inspector',
    color: '#2563eb',
    bgColor: '#dbeafe',
    borderColor: '#60a5fa',
  },
  [ROLE_RECEIPT_FINALIZER]: {
    label: 'RECEIPT 확정자',
    icon: 'ri-receipt-line',
    badgeClass: 'role-badge--inspector',
    color: '#2563eb',
    bgColor: '#dbeafe',
    borderColor: '#60a5fa',
  },
  [ROLE_RECEIPT_MANAGER]: {
    label: 'RECEIPT 관리자',
    icon: 'ri-receipt-line',
    badgeClass: 'role-badge--manager',
    color: '#d97706',
    bgColor: '#fef3c7',
    borderColor: '#fbbf24',
  },
  
  // INFO
  [ROLE_INFO_GUEST]: {
    label: 'INFO 게스트',
    icon: 'ri-user-line',
    badgeClass: 'role-badge--default',
    color: '#6b7280',
    bgColor: '#f3f4f6',
    borderColor: '#d1d5db',
  },
  [ROLE_INFO_USER]: {
    label: 'INFO 일반사용자',
    icon: 'ri-user-line',
    badgeClass: 'role-badge--default',
    color: '#6b7280',
    bgColor: '#f3f4f6',
    borderColor: '#d1d5db',
  },
  [ROLE_INFO_MANAGER]: {
    label: 'INFO 운영자',
    icon: 'ri-user-settings-line',
    badgeClass: 'role-badge--manager',
    color: '#d97706',
    bgColor: '#fef3c7',
    borderColor: '#fbbf24',
  },
  [ROLE_INFO_ADMIN]: {
    label: 'INFO 관리자',
    icon: 'ri-user-settings-line',
    badgeClass: 'role-badge--manager',
    color: '#d97706',
    bgColor: '#fef3c7',
    borderColor: '#fbbf24',
  },
};

/* ═══════════════════════════════════════════════════════════════
 * 워크스페이스 정의
 * ═══════════════════════════════════════════════════════════════ */

export const WORKSPACES = {
  HOME: 'home',
  INFO: 'info',
  RECEIPT: 'receipt',
  MANAGEMENT: 'management',
  USER: 'user',
  SYSTEM: 'system',
  FAVORITES: 'favorites',
};

export const WORKSPACE_METADATA = {
  [WORKSPACES.HOME]: { 
    label: '홈', 
    icon: 'ri-home-4-line' 
  },
  [WORKSPACES.INFO]: { 
    label: '정보', 
    icon: 'ri-information-line' 
  },
  [WORKSPACES.RECEIPT]: { 
    label: '영수증', 
    icon: 'ri-receipt-line' 
  },
  [WORKSPACES.MANAGEMENT]: { 
    label: '사원관리', 
    icon: 'ri-user-settings-line' 
  },
  [WORKSPACES.USER]: { 
    label: '개인정보', 
    icon: 'ri-user-line' 
  },
  [WORKSPACES.SYSTEM]: { 
    label: '시스템', 
    icon: 'ri-settings-3-line' 
  },
  [WORKSPACES.FAVORITES]: { 
    label: '즐겨찾기', 
    icon: 'ri-star-line' 
  },
};

/* ═══════════════════════════════════════════════════════════════
 * 카테고리 정의
 * ═══════════════════════════════════════════════════════════════ */

export const CATEGORIES = {
  INFO_SCHEDULE_MANAGEMENT: '일정 관리',
  INFO_BOARD: '게시판',
  INFO_GUIDE: '가이드',
  
  RECEIPT_REGISTRAR: '등록자',
  RECEIPT_APPROVER: '결재자',
  RECEIPT_PROXY: '대리결재자',
  RECEIPT_INSPECTOR: '검수자',
  RECEIPT_MANAGER: '관리자',
  
  HRM_USER_AUTH: '사용자·권한',
  HRM_ORGANIZATION: '조직',
  HRM_PERFORMANCE: '실적',
  HRM_APPROVAL: '가입 승인',
  
  USER_INFO: '개인정보',

  SYSTEM_ADMIN: '시스템 관리',
  SYSTEM_USER: '개인 정보',
};

/* ═══════════════════════════════════════════════════════════════
 * 권한 UI 정보 함수
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 권한별 한글 라벨 반환
 * @param {string} role - 권한 문자열
 * @returns {string} 한글 라벨
 */
export function getRoleLabel(role) {
  return ROLE_METADATA[role]?.label || '알 수 없는 권한';
}

/**
 * 권한별 아이콘 클래스 반환
 * @param {string} role - 권한 문자열
 * @returns {string} 아이콘 클래스
 */
export function getRoleIcon(role) {
  return ROLE_METADATA[role]?.icon || 'ri-shield-line';
}

/**
 * 권한별 뱃지 CSS 클래스 반환
 * @param {string} role - 권한 문자열
 * @returns {string} CSS 클래스명
 */
export function getRoleBadgeClass(role) {
  return ROLE_METADATA[role]?.badgeClass || 'role-badge--default';
}

/**
 * 권한별 색상 정보 반환
 * @param {string} role - 권한 문자열
 * @returns {{color: string, bgColor: string, borderColor: string}}
 */
export function getRoleColorInfo(role) {
  const metadata = ROLE_METADATA[role];
  if (!metadata) {
    return { 
      color: '#6b7280', 
      bgColor: '#f3f4f6', 
      borderColor: '#d1d5db' 
    };
  }
  
  return {
    color: metadata.color,
    bgColor: metadata.bgColor,
    borderColor: metadata.borderColor,
  };
}

