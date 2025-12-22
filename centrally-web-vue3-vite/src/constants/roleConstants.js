/* ═══════════════════════════════════════════════════════════════
 * 권한 상수 (Role Constants)
 * ═══════════════════════════════════════════════════════════════
 * 
 * 시스템 전체 권한 코드를 중앙 관리합니다.
 * 
 * 주의: roleConfig.js는 계속 사용됩니다 (ROLE_METADATA, roleHierarchy 등)
 * 이 파일은 권한 코드 상수만 관리합니다.
 */

/* ═══════════════════════════════════════════════════════════════
 * HRM 권한 (Human Resource Management)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * HRM 서비스 권한 코드
 */
export const HRM_ROLES = {
  EMPLOYEE: 'ROLE_HRM_EMPLOYEE',
  LEADER: 'ROLE_HRM_LEADER',
  ASSISTANT_MANAGER: 'ROLE_HRM_ASSISTANT_MANAGER',
  MANAGER: 'ROLE_HRM_MANAGER',
};

/* ═══════════════════════════════════════════════════════════════
 * RECEIPT 권한 (영수증)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * RECEIPT 서비스 권한 코드
 */
export const RECEIPT_ROLES = {
  REGISTRAR: 'ROLE_RECEIPT_REGISTRAR',   // 등록자
  APPROVER: 'ROLE_RECEIPT_APPROVER',     // 결재자
  INSPECTOR: 'ROLE_RECEIPT_INSPECTOR',   // 검수자
  FINALIZER: 'ROLE_RECEIPT_FINALIZER',   // 확정자
  MANAGER: 'ROLE_RECEIPT_MANAGER',       // 관리자
};

/* ═══════════════════════════════════════════════════════════════
 * INFO 권한 (정보)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * INFO 서비스 권한 코드
 */
export const INFO_ROLES = {
  GUEST: 'ROLE_INFO_GUEST',       // 게스트
  USER: 'ROLE_INFO_USER',         // 사용자
  MANAGER: 'ROLE_INFO_MANAGER',   // 운영자
  ADMIN: 'ROLE_INFO_ADMIN',       // 관리자
};

/* ═══════════════════════════════════════════════════════════════
 * SYSTEM 권한 (시스템)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * SYSTEM 서비스 권한 코드
 */
export const SYSTEM_ROLES = {
  ADMIN: 'ROLE_GATE_SYSTEM',  // 시스템 관리자 (모든 권한)
};

/* ═══════════════════════════════════════════════════════════════
 * 권한 그룹 (Role Groups)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 영수증 결재 관련 권한 그룹
 */
export const RECEIPT_APPROVAL_ROLES = [
  RECEIPT_ROLES.APPROVER,
  RECEIPT_ROLES.INSPECTOR,
  RECEIPT_ROLES.FINALIZER,
  RECEIPT_ROLES.MANAGER,
];

/**
 * 영수증 대리결재 지정 가능 권한 그룹
 */
export const RECEIPT_DELEGATE_ROLES = [
  RECEIPT_ROLES.APPROVER,
  RECEIPT_ROLES.INSPECTOR,
  RECEIPT_ROLES.MANAGER,
];

/**
 * INFO 게시판 쓰기 권한 (게시판별 다름)
 */
export const INFO_WRITE_ROLES = {
  NOTICE: [INFO_ROLES.ADMIN],                           // 공지사항: ADMIN만
  RESOURCE: [INFO_ROLES.MANAGER, INFO_ROLES.ADMIN],     // 자료실: MANAGER 이상
  COMMUNITY: [INFO_ROLES.USER, INFO_ROLES.MANAGER, INFO_ROLES.ADMIN],  // 자유게시판: USER 이상
};

