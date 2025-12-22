/* ═══════════════════════════════════════════════════════════════
 * 정보 서비스 상수 (Info Constants)
 * ═══════════════════════════════════════════════════════════════
 * 
 * INFO 서비스 게시판에서 사용하는 모든 상수를 중앙 관리합니다.
 */

/* ═══════════════════════════════════════════════════════════════
 * 게시판 코드 (Board Code)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 게시판 타입 코드
 */
export const BOARD_CODE = {
  NOTICE: 'NOTICE',       // 공지사항
  RESOURCE: 'RESOURCE',   // 자료실
  COMMUNITY: 'COMMUNITY', // 자유게시판
};

/**
 * 게시판 코드 → 한글 라벨 매핑
 */
export const BOARD_LABELS = {
  [BOARD_CODE.NOTICE]: '공지사항',
  [BOARD_CODE.RESOURCE]: '자료실',
  [BOARD_CODE.COMMUNITY]: '자유게시판',
};

/* ═══════════════════════════════════════════════════════════════
 * 열람 범위 (Visibility Scope)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 게시글 열람 범위 코드
 */
export const VISIBILITY_SCOPE = {
  ALL: 'ALL',                     // 전체 (GUEST 포함)
  USER_ABOVE: 'USER_ABOVE',       // USER 이상
  MANAGER_ABOVE: 'MANAGER_ABOVE', // MANAGER 이상
};

/**
 * 열람 범위 코드 → 한글 라벨 매핑
 */
export const VISIBILITY_SCOPE_LABELS = {
  [VISIBILITY_SCOPE.ALL]: '전체 (GUEST 포함)',
  [VISIBILITY_SCOPE.USER_ABOVE]: 'USER 이상',
  [VISIBILITY_SCOPE.MANAGER_ABOVE]: 'MANAGER 이상',
};

/* ═══════════════════════════════════════════════════════════════
 * 유틸 함수
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 게시판 코드를 한글 라벨로 변환
 * @param {string} code - 게시판 코드 (예: 'NOTICE', 'RESOURCE')
 * @returns {string} 한글 라벨 (예: '공지사항', '자료실')
 */
export function getBoardLabel(code) {
  return BOARD_LABELS[code] || code;
}

/**
 * 열람 범위 코드를 한글 라벨로 변환
 * @param {string} scope - 열람 범위 코드
 * @returns {string} 한글 라벨
 */
export function getVisibilityScopeLabel(scope) {
  return VISIBILITY_SCOPE_LABELS[scope] || scope;
}

/**
 * Select 컴포넌트용 열람 범위 옵션 생성
 * @returns {Array<{value: string, label: string}>} 옵션 배열
 */
export function getVisibilityOptions() {
  return Object.entries(VISIBILITY_SCOPE_LABELS).map(([value, label]) => ({
    value,
    label,
  }));
}

/**
 * Select 컴포넌트용 게시판 옵션 생성
 * @returns {Array<{value: string, label: string}>} 옵션 배열
 */
export function getBoardOptions() {
  return Object.entries(BOARD_LABELS).map(([value, label]) => ({
    value,
    label,
  }));
}

