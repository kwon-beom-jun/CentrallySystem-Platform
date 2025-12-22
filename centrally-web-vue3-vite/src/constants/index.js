/* ═══════════════════════════════════════════════════════════════
 * 상수 통합 Export (Constants Index)
 * ═══════════════════════════════════════════════════════════════
 * 
 * 모든 상수 파일을 한 곳에서 import할 수 있도록 re-export합니다.
 * 
 * 사용 예시:
 * import { RECEIPT_STATUS, BOARD_CODE } from '@/constants';
 * 
 * 권한 관련:
 * import { INFO_ROLES, HRM_ROLES } from '@/config/roleConfig';
 */

// 영수증 상수
export * from './receiptConstants';

// 정보 서비스 상수
export * from './infoConstants';

// 스타일 상수
export * from './styleConstants';

// 권한 상수는 roleConfig.js에서 통합 관리됨 (roleConstants.js를 import하여 re-export)
// 권한이 필요한 경우: import { INFO_ROLES, HRM_ROLES } from '@/config/roleConfig';

