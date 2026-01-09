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

/* ═══════════════════════════════════════════════════════════════
 * 일정 유형 (Schedule Type) - DB 관리
 * ═══════════════════════════════════════════════════════════════ */

import ScheduleTypeApi from '@/api/info/ScheduleTypeApi';

/**
 * 일정 유형 목록 조회 (캘린더용 - enabled=true이고 isActive=true인 항목만)
 */
export async function fetchScheduleTypes() {
  try {
    const response = await ScheduleTypeApi.getScheduleTypesForCalendar();
    return response.data || [];
  } catch (error) {
    console.error('일정 유형 조회 실패:', error);
    return [];
  }
}

/**
 * 일정 유형 코드를 한글 라벨로 변환
 * @param {string|object} type - 일정 유형 코드 또는 일정 유형 객체
 * @returns {string} 한글 라벨
 */
export async function getScheduleTypeLabel(type) {
  if (!type) return '';
  
  // 객체인 경우 (DB에서 가져온 경우)
  if (typeof type === 'object' && type.label) {
    return type.label;
  }
  
  // 코드인 경우
  if (typeof type === 'string') {
    const scheduleTypes = await fetchScheduleTypes();
    const scheduleType = scheduleTypes.find(st => st.code === type);
    return scheduleType ? scheduleType.label : type;
  }
  
  return type;
}

/**
 * 일정 유형 코드를 기본 색상으로 변환
 * @param {string|object} type - 일정 유형 코드 또는 일정 유형 객체
 * @returns {string} 색상 코드 (HEX)
 */
export async function getScheduleTypeColor(type) {
  if (!type) return '#9E9E9E';
  
  // 객체인 경우 (DB에서 가져온 경우)
  if (typeof type === 'object' && type.color) {
    return type.color || '#9E9E9E';
  }
  
  // 코드인 경우
  if (typeof type === 'string') {
    const scheduleTypes = await fetchScheduleTypes();
    const scheduleType = scheduleTypes.find(st => st.code === type);
    return scheduleType ? (scheduleType.color || '#9E9E9E') : '#9E9E9E';
  }
  
  return '#9E9E9E';
}

/**
 * Select 컴포넌트용 일정 유형 옵션 생성
 * @returns {Array<{value: string, label: string}>} 옵션 배열
 */
export async function getScheduleTypeOptions() {
  const scheduleTypes = await fetchScheduleTypes();
  return scheduleTypes.map(st => ({
    value: st.code,
    label: st.label,
    color: st.color,
  }));
}

/**
 * 일정 유형 코드를 한글 라벨로 변환 (동기식 - scheduleTypes 배열 필요)
 * @param {string|object} type - 일정 유형 코드 또는 일정 유형 객체 또는 scheduleTypeInfo 객체
 * @param {Array} scheduleTypes - 일정 유형 배열
 * @param {object} scheduleTypeInfo - 일정 유형 정보 객체 (삭제된 일정 유형도 표시)
 * @returns {string} 한글 라벨
 */
export function getScheduleTypeLabelSync(type, scheduleTypes = [], scheduleTypeInfo = null) {
  if (!type && !scheduleTypeInfo) return '';
  
  // scheduleTypeInfo가 있으면 우선 사용 (삭제된 일정 유형도 표시)
  if (scheduleTypeInfo && scheduleTypeInfo.label) {
    return scheduleTypeInfo.label;
  }
  
  // 객체인 경우
  if (typeof type === 'object' && type.label) {
    return type.label;
  }
  
  // 코드인 경우
  if (typeof type === 'string' && scheduleTypes.length > 0) {
    const scheduleType = scheduleTypes.find(st => st.code === type);
    return scheduleType ? scheduleType.label : type;
  }
  
  return type || '';
}

/**
 * 일정 유형 코드를 기본 색상으로 변환 (동기식 - scheduleTypes 배열 필요)
 * @param {string|object} type - 일정 유형 코드 또는 일정 유형 객체 또는 scheduleTypeInfo 객체
 * @param {Array} scheduleTypes - 일정 유형 배열
 * @param {object} scheduleTypeInfo - 일정 유형 정보 객체 (삭제된 일정 유형도 표시)
 * @returns {string} 색상 코드 (HEX)
 */
export function getScheduleTypeColorSync(type, scheduleTypes = [], scheduleTypeInfo = null) {
  if (!type && !scheduleTypeInfo) return '#9E9E9E';
  
  // scheduleTypeInfo가 있으면 우선 사용 (삭제된 일정 유형도 표시)
  if (scheduleTypeInfo && scheduleTypeInfo.color) {
    return scheduleTypeInfo.color || '#9E9E9E';
  }
  
  // 객체인 경우
  if (typeof type === 'object' && type.color) {
    return type.color || '#9E9E9E';
  }
  
  // 코드인 경우
  if (typeof type === 'string' && scheduleTypes.length > 0) {
    const scheduleType = scheduleTypes.find(st => st.code === type);
    return scheduleType ? (scheduleType.color || '#9E9E9E') : '#9E9E9E';
  }
  
  return '#9E9E9E';
}

/**
 * Select 컴포넌트용 일정 유형 옵션 생성 (정렬 포함)
 * @param {Array} scheduleTypes - 일정 유형 배열
 * @returns {Array<{value: string, label: string}>} 옵션 배열
 */
export function getScheduleTypeOptionsSync(scheduleTypes = []) {
  // enabled=true AND isActive=true인 항목만 필터링
  const filtered = scheduleTypes.filter(st => 
    st.enabled !== false && st.isActive === true
  );
  
  // displayOrder >= 0인 항목을 먼저 정렬, 그 다음 displayOrder = -1인 항목 정렬
  const sorted = [...filtered].sort((a, b) => {
    const orderA = a.displayOrder ?? 0;
    const orderB = b.displayOrder ?? 0;
    if (orderA >= 0 && orderB >= 0) {
      return orderA - orderB;
    }
    if (orderA < 0) return 1;
    if (orderB < 0) return -1;
    return 0;
  });
  
  return sorted.map(st => ({
    value: st.code,
    label: st.label,
    color: st.color,
  }));
}

// 하위 호환성을 위한 상수 (deprecated - DB에서 관리)
export const SCHEDULE_TYPE = {
  BUSINESS_TRIP: 'BUSINESS_TRIP',
  VACATION: 'VACATION',
  MEETING: 'MEETING',
  EDUCATION: 'EDUCATION',
  OTHER: 'OTHER',
};

/* ═══════════════════════════════════════════════════════════════
 * 알림 시점 단위 (Notification Timing Unit)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 알림 시점 단위 코드
 */
export const NOTIFICATION_TIMING_UNIT = {
  DAY: 'DAY',
  HOUR: 'HOUR',
  MINUTE: 'MINUTE',
};

/**
 * 알림 시점 단위 코드 → 한글 라벨 매핑
 */
export const NOTIFICATION_TIMING_UNIT_LABELS = {
  [NOTIFICATION_TIMING_UNIT.DAY]: '일',
  [NOTIFICATION_TIMING_UNIT.HOUR]: '시간',
  [NOTIFICATION_TIMING_UNIT.MINUTE]: '분',
};

/**
 * 알림 시점 단위 코드 → 한글 라벨 매핑 (전 포함)
 */
export const NOTIFICATION_TIMING_UNIT_LABELS_WITH_BEFORE = {
  [NOTIFICATION_TIMING_UNIT.DAY]: '일 전',
  [NOTIFICATION_TIMING_UNIT.HOUR]: '시간 전',
  [NOTIFICATION_TIMING_UNIT.MINUTE]: '분 전',
};

/**
 * 알림 시점 단위 코드를 한글 라벨로 변환
 * @param {string} unit - 알림 시점 단위 코드 (예: 'DAY', 'HOUR', 'MINUTE')
 * @param {boolean} withBefore - '전' 포함 여부
 * @returns {string} 한글 라벨
 */
export function getNotificationTimingUnitLabel(unit, withBefore = false) {
  if (withBefore) {
    return NOTIFICATION_TIMING_UNIT_LABELS_WITH_BEFORE[unit] || unit;
  }
  return NOTIFICATION_TIMING_UNIT_LABELS[unit] || unit;
}

/**
 * 알림 시점 단위별 최소/최대 값
 */
export const NOTIFICATION_TIMING_UNIT_LIMITS = {
  [NOTIFICATION_TIMING_UNIT.DAY]: { min: 1, max: 30 },
  [NOTIFICATION_TIMING_UNIT.HOUR]: { min: 1, max: 24 },
  [NOTIFICATION_TIMING_UNIT.MINUTE]: { min: 3, max: 60 },
};

