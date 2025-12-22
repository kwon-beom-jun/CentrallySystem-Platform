/**
 * 영수증 공통 유틸 모음
 */
import { ROLE_GROUPS } from '@/config/roleConfig';

/**
 * 결재선에서 첫 반려 사유 반환
 * @param {Array<any>} approvalLines
 * @returns {string}
 */
export function extractFirstRejectedReason(approvalLines) {
  const lines = Array.isArray(approvalLines) ? approvalLines : [];
  const found = lines.find(ln => ln && (ln.rejectedAt || ln.rejectedReason));
  return (found && found.rejectedReason) || '';
}

/**
 * 1인당 한도 기준 총 한도 계산 (카테고리 한도 × (참여자수 + 본인))
 * @param {number} perPersonLimit
 * @param {number} participantCount
 * @returns {number}
 */
export function computeTotalLimit(perPersonLimit, participantCount) {
  const persons = Number(participantCount || 0) + 1;
  return Math.floor(Number(perPersonLimit || 0) * persons);
}

/**
 * 금액 문자열을 숫자로 변환 (숫자 외 제거)
 * @param {string|number} value
 * @returns {number}
 */
export function parseAmountNumber(value) {
  if (value == null) return 0;
  return Number(String(value).replace(/\D/g, '')) || 0;
}

/**
 * 금액 문자열에서 숫자/콤마만 남김
 * @param {string|number} value
 * @returns {string}
 */
export function sanitizeMoneyString(value) {
  return String(value ?? '').replace(/[^0-9,]/g, '');
}

/**
 * 숫자 문자열(0-9)만 남김
 * @param {string|number} value
 * @returns {string}
 */
export function sanitizeDigitString(value) {
  return String(value ?? '').replace(/[^0-9]/g, '');
}

/**
 * 금액 문자열을 포맷/클램프하여 반환
 * @param {string|number} raw 현재 입력 값(콤마 포함 가능)
 * @param {number} max 최대 한도(Infinity 허용)
 * @returns {{ formatted:string, wasClamped:boolean, numeric:number }}
 */
export function formatAndClampAmount(raw, max) {
  const entered = parseAmountNumber(raw);
  const limit = Number.isFinite(max) ? max : entered;
  const clamped = entered > limit ? limit : entered;
  return {
    formatted: clamped ? clamped.toLocaleString('ko-KR') : '',
    wasClamped: entered > limit,
    numeric: clamped,
  };
}

/**
 * 금액 키다운 가드: 숫자/제어키만 허용하고, 한도 도달시 끝에서 추가 입력 차단
 * @param {KeyboardEvent} e
 * @param {number} max
 * @param {string|number} currentValue
 */
export function enforceMoneyKeydown(e, max, currentValue) {
  const allowedCtrl = ['Backspace','Delete','Tab','Escape','Enter','ArrowLeft','ArrowRight','Home','End'];
  if (e.ctrlKey || e.metaKey) return; // 복사/붙여넣기 허용
  if (allowedCtrl.includes(e.key)) return;
  const isDigit = /^[0-9]$/.test(e.key);
  if (!isDigit) { e.preventDefault(); return; }
  if (!Number.isFinite(max)) return;
  const input = e.target;
  const val = String(currentValue ?? '');
  const hasSelection = input && input.selectionStart != null && input.selectionEnd != null && input.selectionStart !== input.selectionEnd;
  const atEnd = input && input.selectionStart != null && input.selectionStart === val.length;
  const current = parseAmountNumber(val);
  if (!hasSelection && atEnd && current >= max) e.preventDefault();
}

/**
 * 금액 붙여넣기 처리: 클립보드 텍스트를 즉시 포맷/클램프
 * @param {ClipboardEvent} e
 * @param {number} max
 * @returns {string} formatted
 */
export function formatClampFromClipboard(e, max) {
  const t = e.clipboardData?.getData('text') ?? '';
  const { formatted } = formatAndClampAmount(t, max);
  e.preventDefault();
  return formatted;
}


/* ═══════════════════════════════════════════════════════════════
 * 권한 상실 시 영수증 정리 로직 (Permission Loss Cleanup)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 사용자가 결재선에 포함되어 있는지 확인하고 반려 대상 목록 반환
 * @param {number} userId - 확인할 사용자 ID
 * @param {Object} ReceiptsSearchApi - API 객체
 * @returns {Promise<{hasPending: boolean, receiptIds: number[], rejectIds: number[], rejectCodes: string[]}>}
 */
export async function checkUserInApprovalLine(userId, ReceiptsSearchApi) {
  const response = await ReceiptsSearchApi.getMyPendingApprovals(userId, { 
    page: 0, 
    size: 100000 
  });
  
  const hasPending = response.data.totalElements > 0;
  const receiptIds = [];
  const rejectIds = [];
  const rejectCodes = [];

  if (hasPending) {
    const receipts = response.data.content;
    receipts.forEach(r => {
      receiptIds.push(r.receiptId);
      if (r.status === 'REQUEST') {
        rejectIds.push(r.receiptId);
        rejectCodes.push(r.receiptCode);
      }
    });
  }

  return {
    hasPending,
    receiptIds,
    rejectIds,
    rejectCodes
  };
}

/**
 * 사용자 비활성화/권한 변경 시 영수증 관련 데이터 정리
 * - 대기중인 영수증 반려 처리
 * - 대리결재 매핑 삭제
 * - 즐겨찾기 결재자 제거
 * 
 * @param {Object} params
 * @param {number} params.userId - 대상 사용자 ID
 * @param {string} params.userName - 사용자 이름
 * @param {number[]} params.receiptIds - 반려 처리할 영수증 ID 목록
 * @param {string} params.reason - 반려 사유 (선택, 기본값: "사용자가 비활성화되었습니다")
 * @param {Object} params.apis - API 객체들
 * @param {Object} params.apis.ReceiptsRequestApi
 * @param {Object} params.apis.ReceiptsDelegateApi
 * @param {Object} params.apis.ReceiptsApproverFavoriteApi
 * @returns {Promise<void>}
 */
export async function cleanupReceiptsOnDeactivation({ 
  userId, 
  userName, 
  receiptIds = [], 
  reason = null,
  apis 
}) {
  const { 
    ReceiptsRequestApi, 
    ReceiptsDelegateApi, 
    ReceiptsApproverFavoriteApi 
  } = apis;

  // 1. 대기중인 영수증 반려 처리
  if (receiptIds.length > 0) {
    const defaultReason = `${userName} 사용자가 비활성화되었습니다.`;
    const requestData = {
      receiptIds,
      userToRemoveId: userId
    };
    await ReceiptsRequestApi.rejectAndRemoveApprover(requestData, reason || defaultReason);
  }

  // 2. 사용자 관련 대리결재 매핑 전체 소프트 삭제 (원/대리 모두)
  await ReceiptsDelegateApi.softDeleteAllByUser(userId);

  // 3. 즐겨찾기 결재자 일괄 제거
  await ReceiptsApproverFavoriteApi.deleteAllByFavoriteUser(userId);
}

/**
 * 고정 합의자 여부 확인
 * @param {number} userId - 확인할 사용자 ID
 * @param {Object} ReceiptsDefaultApproverApi - API 객체
 * @returns {Promise<boolean>}
 */
export async function isDefaultApprover(userId, ReceiptsDefaultApproverApi) {
  const response = await ReceiptsDefaultApproverApi.checkIfDefaultApprover(userId);
  return response.data === true;
}

/**
 * 결재자 권한 검증
 * - 결재선에 포함된 사용자들이 결재 권한을 가지고 있는지 확인
 * 
 * @param {number[]} approverIds - 결재자 ID 목록
 * @param {Object} UsersApi - API 객체
 * @param {Function} getRolesFrom - roleUtils의 getRolesFrom 함수
 * @returns {Promise<{valid: boolean, unauthorizedUsers: Array}>}
 */
export async function validateApprovers(approverIds, UsersApi, getRolesFrom) {
  if (!approverIds || approverIds.length === 0) {
    return { valid: false, unauthorizedUsers: [], error: 'NO_APPROVERS' };
  }

  // 활성화된 사용자 정보 및 권한 조회
  const enabledUsers = await UsersApi.getActiveUsersWithRoles(approverIds);
  const enabledIdSet = new Set(enabledUsers.map(u => u.userId));
  
  // 비활성화된 사용자 확인
  const disabledIds = approverIds.filter(id => !enabledIdSet.has(id));
  if (disabledIds.length > 0) {
    return { 
      valid: false, 
      unauthorizedUsers: disabledIds, 
      error: 'DISABLED_USERS' 
    };
  }

  // 필수 결재자(approvalRole === 1) 확인
  const hasRequiredApprover = enabledUsers.some(u => 
    u.approvalRole === 1 || 
    enabledUsers.length > 0  // 기본적으로 첫 번째가 결재자
  );
  
  if (!hasRequiredApprover) {
    return { 
      valid: false, 
      unauthorizedUsers: [], 
      error: 'NO_REQUIRED_APPROVER' 
    };
  }

  // 결재 권한 확인
  const requiredRoles = [
    ...getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
    'ROLE_GATE_SYSTEM'
  ];

  const unauthorizedUsers = enabledUsers.filter(approver => 
    !approver.roles || !approver.roles.some(userRole => 
      requiredRoles.includes(userRole)
    )
  );

  return {
    valid: unauthorizedUsers.length === 0,
    unauthorizedUsers,
    error: unauthorizedUsers.length > 0 ? 'UNAUTHORIZED_ROLES' : null
  };
}

/**
 * 권한 상실 확인 및 안내 메시지 생성
 * - 시스템 권한 or 영수증 결재자 이상 → 하위 권한으로 변경 시
 * - 관련 영수증, 대리결재, 즐겨찾기 정리 필요 여부 체크\
 * 
 * 현재 안쓰여지고있음 추후 내용 수정해서 UserPermissions.vue에 적용하면 될듯
 * (checkCriticalPermissionLoss 부분 교체?)
 * 
 * @param {Object} params
 * @param {Array} params.beforeRoles - 변경 전 권한 배열 (roleName 또는 {serviceName, roleName})
 * @param {Array} params.afterRoles - 변경 후 권한 배열 (roleName 또는 {serviceName, roleName})
 * @param {Function} params.hasPermission - roleUtils의 hasPermission 함수
 * @param {Function} params.getRolesFrom - roleUtils의 getRolesFrom 함수
 * @returns {{needsCleanup: boolean, lostPermissions: string[], message: string}}
 */
export function checkPermissionLoss({ beforeRoles, afterRoles, hasPermission, getRolesFrom }) {
  // roleName 배열로 변환 (객체 배열인 경우 대비)
  const beforeRoleNames = Array.isArray(beforeRoles) 
    ? beforeRoles.map(r => typeof r === 'string' ? r : r.roleName)
    : beforeRoles;
  const afterRoleNames = Array.isArray(afterRoles)
    ? afterRoles.map(r => typeof r === 'string' ? r : r.roleName)
    : afterRoles;
  
  // 시스템 권한 체크
  const hadSystemPermission = beforeRoleNames.includes('ROLE_GATE_SYSTEM');
  const hasSystemPermission = afterRoleNames.includes('ROLE_GATE_SYSTEM');
  const isLosingSystemPermission = hadSystemPermission && !hasSystemPermission;
  
  // 영수증 결재 권한 체크
  const requiredRoles = getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER);
  const hadApprovalPermission = hasPermission(beforeRoleNames, requiredRoles);
  const hasApprovalPermission = hasPermission(afterRoleNames, requiredRoles);
  const isLosingReceiptApproval = hadApprovalPermission && !hasApprovalPermission;
  
  // 권한 상실 여부
  const needsCleanup = isLosingSystemPermission || isLosingReceiptApproval;
  
  if (!needsCleanup) {
    return {
      needsCleanup: false,
      lostPermissions: [],
      message: ''
    };
  }

  // 상실한 권한 목록
  const lostPermissions = [];
  if (isLosingSystemPermission) {
    lostPermissions.push('시스템 권한');
  }
  if (isLosingReceiptApproval) {
    lostPermissions.push('영수증 결재 권한');
  }

  const message = `
권한이 변경되어 다음 데이터가 정리됩니다:
• 결재 대기중인 영수증: 반려 처리
• 대리결재자 설정: 전체 해제
• 즐겨찾기 결재자: 전체 제거

상실된 권한: ${lostPermissions.join(', ')}
  `.trim();

  return {
    needsCleanup,
    lostPermissions,
    message,
    isLosingSystemPermission,
    isLosingReceiptApproval
  };
}

