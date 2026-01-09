/* ═══════════════════════════════════════════════════════════════
 * 상수 통합 Export (Constants Index)
 * ═══════════════════════════════════════════════════════════════
 * 
 * 모든 상수 파일을 한 곳에서 import할 수 있도록 re-export합니다.
 * 
 * 사용 예시:
 * import { RECEIPT_STATUS, BOARD_CODE } from '@/constants';
 * import { RECEIPT_API_BASE_URL, INFO_SERVICE_NAME } from '@/constants';
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

/* ═══════════════════════════════════════════════════════════════
 * 환경변수 상수 (Vite Environment Variables)
 * ═══════════════════════════════════════════════════════════════
 * 
 * import.meta.env를 직접 사용하는 대신 여기서 한 번만 import하고
 * 깔끔한 이름으로 re-export합니다.
 * 
 * 사용 예시:
 * import { RECEIPT_API_BASE_URL, INFO_SERVICE_NAME } from '@/constants';
 * 
 * 기존: import.meta.env.VITE_RECEIPT_API_BASE_URL
 * 변경: RECEIPT_API_BASE_URL
 */

// API Base URLs
export const SYSTEM_API_BASE_URL = import.meta.env.VITE_SYSTEM_API_BASE_URL;
export const AUTH_API_BASE_URL = import.meta.env.VITE_AUTH_API_BASE_URL;
export const HRM_API_BASE_URL = import.meta.env.VITE_HRM_API_BASE_URL;
export const RECEIPT_API_BASE_URL = import.meta.env.VITE_RECEIPT_API_BASE_URL;
export const INFO_API_BASE_URL = import.meta.env.VITE_INFO_API_BASE_URL;

// Service Names
export const HRM_SERVICE_NAME = import.meta.env.VITE_HRM_SERVICE_NAME;
export const AUTH_SERVICE_NAME = import.meta.env.VITE_AUTH_SERVICE_NAME;
export const INFO_SERVICE_NAME = import.meta.env.VITE_INFO_SERVICE_NAME;
export const RECEIPT_SERVICE_NAME = import.meta.env.VITE_RECEIPT_SERVICE_NAME;

