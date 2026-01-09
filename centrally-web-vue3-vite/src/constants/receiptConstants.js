/* ═══════════════════════════════════════════════════════════════
 * 영수증 상수 (Receipt Constants)
 * ═══════════════════════════════════════════════════════════════
 * 
 * 영수증 서비스에서 사용하는 모든 상수를 중앙 관리합니다.
 */

/* ═══════════════════════════════════════════════════════════════
 * 영수증 상태 코드 (Receipt Status)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 영수증 처리 상태 코드
 */
export const RECEIPT_STATUS = {
  WAITING: 'WAITING',     // 대기
  REQUEST: 'REQUEST',     // 신청
  APPROVED: 'APPROVED',   // 승인
  REJECTED: 'REJECTED',   // 반려
  CLOSED: 'CLOSED',       // 마감
};

/**
 * 영수증 상태 코드 → 한글 라벨 매핑
 */
export const RECEIPT_STATUS_LABELS = {
  [RECEIPT_STATUS.WAITING]: '대기',
  [RECEIPT_STATUS.REQUEST]: '신청',
  [RECEIPT_STATUS.APPROVED]: '승인',
  [RECEIPT_STATUS.REJECTED]: '반려',
  [RECEIPT_STATUS.CLOSED]: '마감',
};

/**
 * 영수증 상태 코드 → CSS 클래스 매핑
 */
export const RECEIPT_STATUS_CLASSES = {
  [RECEIPT_STATUS.WAITING]: 'text-gray',
  [RECEIPT_STATUS.REQUEST]: 'text-blue',
  [RECEIPT_STATUS.APPROVED]: 'text-green',
  [RECEIPT_STATUS.REJECTED]: 'text-red',
  [RECEIPT_STATUS.CLOSED]: 'text-dark',
};

/* ═══════════════════════════════════════════════════════════════
 * 결재 역할 (Approval Role)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 결재선 역할 코드
 */
export const APPROVAL_ROLE = {
  APPROVER: 1,      // 결재
  CONCURRENCE: 2,   // 합의
};

/**
 * 결재 역할 → 한글 라벨
 */
export const APPROVAL_ROLE_LABELS = {
  [APPROVAL_ROLE.APPROVER]: '결재',
  [APPROVAL_ROLE.CONCURRENCE]: '합의',
};

/* ═══════════════════════════════════════════════════════════════
 * 참여자 타입 (Participant Type)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 영수증 참여자 타입
 */
export const PARTICIPANT_TYPE = {
  INTERNAL: 'INTERNAL',  // 내부 사원
  EXTERNAL: 'EXTERNAL',  // 외부 인력
};

/**
 * 참여자 타입 → 한글 라벨
 */
export const PARTICIPANT_TYPE_LABELS = {
  [PARTICIPANT_TYPE.INTERNAL]: '내부',
  [PARTICIPANT_TYPE.EXTERNAL]: '외부',
};

/* ═══════════════════════════════════════════════════════════════
 * 유틸 함수
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 영수증 상태 코드를 한글 라벨로 변환
 * @param {string} code - 상태 코드 (예: 'WAITING', 'APPROVED')
 * @returns {string} 한글 라벨 (예: '대기', '승인')
 */
export function getReceiptStatusLabel(code) {
  return RECEIPT_STATUS_LABELS[code] || '알수없음';
}

/**
 * 영수증 상태 라벨을 CSS 클래스로 변환
 * @param {string} label - 상태 라벨 (예: '신청', '승인')
 * @returns {string} CSS 클래스 (예: 'text-blue', 'text-green')
 */
export function getReceiptStatusClass(label) {
  const code = Object.keys(RECEIPT_STATUS_LABELS).find(
    k => RECEIPT_STATUS_LABELS[k] === label
  );
  return RECEIPT_STATUS_CLASSES[code] || '';
}

/**
 * 영수증 상태 코드를 CSS 클래스로 직접 변환
 * @param {string} code - 상태 코드
 * @returns {string} CSS 클래스
 */
export function getReceiptStatusClassByCode(code) {
  return RECEIPT_STATUS_CLASSES[code] || '';
}

/**
 * 결재 역할 코드를 한글 라벨로 변환
 * @param {number} role - 역할 코드 (1: 결재, 2: 합의)
 * @returns {string} 한글 라벨
 */
export function getApprovalRoleLabel(role) {
  return APPROVAL_ROLE_LABELS[role] || '';
}

/**
 * 참여자 타입을 한글 라벨로 변환
 * @param {string} type - 타입 코드
 * @returns {string} 한글 라벨
 */
export function getParticipantTypeLabel(type) {
  return PARTICIPANT_TYPE_LABELS[type] || type;
}

