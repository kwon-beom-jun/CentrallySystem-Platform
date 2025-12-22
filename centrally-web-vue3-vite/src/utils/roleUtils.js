/* ═══════════════════════════════════════════════════════════════
 * roleConfig.js에서 import (내부 사용 + re-export)
 * ═══════════════════════════════════════════════════════════════ */
import { 
  roleHierarchy,
  ROLE_GATE_SYSTEM,
  ROLE_HRM_EMPLOYEE,
  ROLE_HRM_LEADER,
  ROLE_HRM_ASSISTANT_MANAGER,
  ROLE_HRM_MANAGER,
  ROLE_RECEIPT_REGISTRAR,
  ROLE_RECEIPT_APPROVER,
  ROLE_RECEIPT_INSPECTOR,
  ROLE_RECEIPT_FINALIZER,
  ROLE_RECEIPT_MANAGER,
  ROLE_GROUPS,
  getRoleLabel,
  getRoleIcon,
  getRoleBadgeClass,
  getRoleColorInfo,
} from '@/config/roleConfig';

// 외부로 re-export
export {
  ROLE_GATE_SYSTEM,
  ROLE_HRM_EMPLOYEE,
  ROLE_HRM_LEADER,
  ROLE_HRM_ASSISTANT_MANAGER,
  ROLE_HRM_MANAGER,
  ROLE_RECEIPT_REGISTRAR,
  ROLE_RECEIPT_APPROVER,
  ROLE_RECEIPT_INSPECTOR,
  ROLE_RECEIPT_FINALIZER,
  ROLE_RECEIPT_MANAGER,
  ROLE_GROUPS,
  getRoleLabel,
  getRoleIcon,
  getRoleBadgeClass,
  getRoleColorInfo,
};


/* ═══════════════════════════════════════════════════════════════
 * 권한 계층 조회 내부 함수
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 권한 키로부터 계층 구조에서 인덱스를 찾는 내부 함수
 * @private
 * @param {string} key - 예: 'hrm_manager', 'receipt_approver'
 * @returns {{hierarchy: string[]|null, index: number}}
 */
function findRoleIndex(key) {
  if (!key) return { hierarchy: null, index: -1 };

  const parts = key.split('_');
  const service = parts[0];
  const level = parts.slice(1).join('_').toUpperCase(); // MANAGER, INSPECTOR, GATE_SYSTEM

  const hierarchy = roleHierarchy[service];
  if (!hierarchy) return { hierarchy: null, index: -1 };

  // (1) 요청된 레벨을 토큰 배열로 준비 (예: 'ASSISTANT_MANAGER' → ['ASSISTANT','MANAGER'])
  const levelParts = level.split('_');

  // (2) 계층 배열에서 정확히 같은 레벨 토큰을 가진 role 찾기
  const index = hierarchy.findIndex(r => {
    const roleTokens = r.split('_');          // ['ROLE','HRM', 'ASSISTANT', 'MANAGER']

    // 첫 토큰 'ROLE' 제거
    if (roleTokens[0] === 'ROLE') roleTokens.shift();

    // 다음 토큰이 서비스명이라면 제거 (예: 'HRM')
    if (roleTokens[0] === service.toUpperCase()) roleTokens.shift();

    // 남은 토큰이 요청 레벨과 정확히 일치해야 함
    return roleTokens.join('_') === levelParts.join('_');
  });

  if (index === -1) return { hierarchy: null, index: -1 };
  return { hierarchy, index };
}


/* ═══════════════════════════════════════════════════════════════
 * 권한 배열 조회 함수 (Role Array Functions)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 특정 권한 레벨까지의 모든 하위 권한을 배열로 반환 (포함관계)
 * 예: 'hrm_manager' → ['ROLE_HRM_EMPLOYEE', 'ROLE_HRM_LEADER', 'ROLE_HRM_ASSISTANT_MANAGER', 'ROLE_HRM_MANAGER']
 * 
 * @param {string} key - 예: 'hrm_manager', 'receipt_approver' 등
 * @returns {string[]} 포함관계로 누적된 권한 배열
 */
export function getRoleHierarchyArray(key) {
  const { hierarchy, index } = findRoleIndex(key);
  if (index === -1) return [];
  return hierarchy.slice(0, index + 1);
}

/**
 * 여러 서비스-레벨 키에 대한 권한 배열을 합쳐 반환 (중복 제거)
 * 
 * @param {string[]} keys - 예: ['hrm_manager', 'receipt_inspector']
 * @returns {string[]} 권한 배열
 */
export function getCombinedRoleHierarchyArray(keys) {
  if (!Array.isArray(keys)) return [];
  const all = keys
    .map(getRoleHierarchyArray)
    .flat();
  // 중복 제거
  return Array.from(new Set(all));
}

/**
 * 특정 권한 레벨부터 그 상위 레벨까지의 모든 권한을 배열로 반환
 * 예: getRolesFrom('receipt_approver')
 * -> ['ROLE_RECEIPT_APPROVER', 'ROLE_RECEIPT_INSPECTOR', 'ROLE_RECEIPT_FINALIZER', 'ROLE_RECEIPT_MANAGER']
 *
 * @param {string} key - 예: 'receipt_approver', 'hrm_manager'
 * @returns {string[]} 특정 레벨 이상의 권한 배열
 */
export function getRolesFrom(key) {
  const { hierarchy, index } = findRoleIndex(key);
  if (index === -1) return [];
  return hierarchy.slice(index); // idx부터 끝까지
}


/* ═══════════════════════════════════════════════════════════════
 * 환경 체크 함수 (Environment Check Functions)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 로컬 테스트 환경인지 확인
 * @returns {boolean}
 */
export function isLocalTestMode() {
  return String(import.meta.env.VITE_LOCALTEST ?? import.meta.env.VITE_APP_LOCALTEST) === 'true';
}

/**
 * 로컬 개발 환경인지 확인
 * @returns {boolean}
 */
export function isLocalEnv() {
  return import.meta.env.VITE_APP_ENV === 'local';
}


/* ═══════════════════════════════════════════════════════════════
 * 권한 체크 함수 (Permission Check Functions)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 사용자가 필요한 권한을 가지고 있는지 확인
 * - 로컬 테스트 모드에서는 항상 true 반환
 * - ROLE_GATE_SYSTEM 권한이 있으면 항상 true 반환
 * - 허용된 권한 배열이 비어있으면 true 반환 (권한 제한 없음)
 * 
 * @param {string[]} userRoles - 사용자가 가진 권한 배열
 * @param {string[]} allowedRoles - 허용할 권한 목록
 * @returns {boolean}
 */
export function hasPermission(userRoles = [], allowedRoles = []) {
  // 권한 제한이 없는 경우
  if (allowedRoles.length === 0) return true;
  
  // 로컬 테스트 모드
  if (isLocalTestMode()) return true;
  
  // 시스템 권한 보유
  if (userRoles.includes(ROLE_GATE_SYSTEM)) return true;
  
  // 허용된 권한 중 하나라도 보유하고 있는지 확인
  return allowedRoles.some((role) => userRoles.includes(role));
}

/**
 * 사용자가 필요한 권한을 가지고 있는지 확인 (로컬 환경 체크)
 * - 로컬 개발 환경에서는 항상 true 반환
 * - ROLE_GATE_SYSTEM 권한이 있으면 항상 true 반환
 * 
 * @param {string[]} userRoles - 사용자가 가진 권한 배열
 * @param {string[]} allowedRoles - 허용할 권한 목록
 * @returns {boolean}
 */
export function hasRole(userRoles = [], allowedRoles = []) {
  // 로컬 개발 환경
  if (isLocalEnv()) return true;
  
  // 시스템 권한 보유
  if (userRoles.includes(ROLE_GATE_SYSTEM)) return true;
  
  // 허용된 권한 중 하나라도 보유하고 있는지 확인
  return allowedRoles.some((role) => userRoles.includes(role));
}

/**
 * 메뉴/기능 표시 여부를 확인 (hasPermission의 별칭)
 * 주로 UI 컴포넌트에서 v-if와 함께 사용
 * 
 * @param {string[]} userRoles - 사용자가 가진 권한 배열
 * @param {string[]} allowedRoles - 허용할 권한 목록
 * @returns {boolean}
 */
export function canShow(userRoles = [], allowedRoles = []) {
  return hasPermission(userRoles, allowedRoles);
}


/* ═══════════════════════════════════════════════════════════════
 * 권한 UI 정보 함수는 roleConfig.js에서 re-export됨
 * - getRoleLabel: 권한별 한글 라벨
 * - getRoleIcon: 권한별 아이콘 클래스
 * - getRoleBadgeClass: 권한별 뱃지 CSS 클래스
 * - getRoleColorInfo: 권한별 색상 정보
 * ═══════════════════════════════════════════════════════════════ */


/* ═══════════════════════════════════════════════════════════════
 * Composable 헬퍼 함수 (Vue Composition API 사용 시)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * Vue 컴포넌트에서 권한 체크를 위한 헬퍼 함수 생성
 * @param {import('@/store/auth').AuthStore} authStore - Pinia auth store
 * @returns {Object} 권한 체크 함수들
 */
export function usePermissions(authStore) {
  const userRoles = authStore.getRoles || authStore.roles || [];
  
  return {
    /**
     * 권한 체크
     * @param {string[]} allowedRoles 
     * @returns {boolean}
     */
    hasPermission: (allowedRoles = []) => hasPermission(userRoles, allowedRoles),
    
    /**
     * 메뉴 표시 여부 체크
     * @param {string[]} allowedRoles 
     * @returns {boolean}
     */
    canShow: (allowedRoles = []) => canShow(userRoles, allowedRoles),
    
    /**
     * 로컬 환경 포함 권한 체크
     * @param {string[]} allowedRoles 
     * @returns {boolean}
     */
    hasRole: (allowedRoles = []) => hasRole(userRoles, allowedRoles),
    
    /**
     * 사용자 권한 목록
     */
    userRoles,
  };
} 