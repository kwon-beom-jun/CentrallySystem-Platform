/**
 * 날짜 관련 유틸리티 함수들
 */

/**
 * 한국 시간 기준으로 현재 월을 YYYY-MM 형식으로 반환
 * @returns {string} YYYY-MM 형식의 문자열 (예: "2024-08")
 */
export function getCurrentMonthKST() {
  // 브라우저 호환성을 위한 안전한 방법
  try {
    // 1. Intl.DateTimeFormat 사용 (최신 브라우저)
    const formatter = new Intl.DateTimeFormat('sv-SE', { 
      timeZone: 'Asia/Seoul',
      year: 'numeric', 
      month: '2-digit' 
    });
    const parts = formatter.formatToParts(new Date());
    const year = parts.find(part => part.type === 'year').value;
    const month = parts.find(part => part.type === 'month').value;
    const result = `${year}-${month}`;
    console.log('🔍 [dateUtils] Intl 방법 사용:', result); // 디버깅용
    return result;
  } catch (e) {
    // 2. fallback: UTC + 9시간
    const now = new Date();
    const koreaTime = new Date(now.getTime() + (9 * 60 * 60 * 1000));
    const year = koreaTime.getUTCFullYear();
    const month = String(koreaTime.getUTCMonth() + 1).padStart(2, '0');
    const result = `${year}-${month}`;
    console.log('🔍 [dateUtils] Fallback 방법 사용:', result, '(에러:', e.message, ')'); // 디버깅용
    return result;
  }
}

/**
 * 한국 시간 기준으로 현재 날짜를 YYYY-MM-DD 형식으로 반환
 * @returns {string} YYYY-MM-DD 형식의 문자열 (예: "2024-08-01")
 */
export function getCurrentDateKST() {
  const now = new Date();
  // UTC 시간에 9시간(한국 시간대)을 추가
  const koreaTime = new Date(now.getTime() + (9 * 60 * 60 * 1000));
  
  const year = koreaTime.getUTCFullYear();
  const month = String(koreaTime.getUTCMonth() + 1).padStart(2, '0');
  const day = String(koreaTime.getUTCDate()).padStart(2, '0');
  
  return `${year}-${month}-${day}`;
}

/**
 * 한국 시간 기준으로 현재 시간을 반환
 * @returns {Date} 한국 시간 기준 Date 객체
 */
export function getNowKST() {
  const now = new Date();
  // UTC 시간에 9시간(한국 시간대)을 추가
  return new Date(now.getTime() + (9 * 60 * 60 * 1000));
}

/**
 * 주어진 ISO/로컬 날짜 문자열을 KST 기준 Date로 변환
 * @param {string|Date} input - ISO 문자열(예: 2025-09-25T09:15:29.112493) 또는 Date
 * @returns {Date|null} KST 기준 Date (파싱 실패 시 null)
 */
export function toKstDate(input) {
  if (!input) return null;
  try {
    if (input instanceof Date) return new Date(input.getTime() + (9 * 60 * 60 * 1000));
    // 일부 백엔드에서 나노초 자릿수 포함: 소수점 3자리(ms)까지만 사용
    const normalized = String(input).replace(/(\.\d{3})\d+$/, '$1');
    const d = new Date(normalized);
    if (isNaN(d.getTime())) return null;
    // 입력이 이미 UTC 기준이라고 가정하고 +9h 적용
    return new Date(d.getTime() + (9 * 60 * 60 * 1000));
  } catch {
    return null;
  }
}

/**
 * 상대 시간 문자열 생성 (방금 전/분 전/시간 전/어제/며칠 전/몇달 전/몇년 전)
 * @param {string|Date} dateLike - 비교할 날짜 (서버 regTime 등)
 * @param {Date} [now=new Date()] - 현재 시각(테스트 용)
 * @returns {string}
 */
export function formatRelativeKST(dateLike, now = new Date()) {
  const target = toKstDate(dateLike);
  if (!target) return '';
  const nowKst = new Date(now.getTime() + (9 * 60 * 60 * 1000));
  const diffMs = nowKst.getTime() - target.getTime();

  if (diffMs < 0) return '방금 전';

  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  const month = 30 * day;
  const year = 365 * day;

  if (diffMs < minute) return '방금 전';
  if (diffMs < hour) {
    const m = Math.floor(diffMs / minute);
    return `${m}분 전`;
  }
  if (diffMs < day) {
    const h = Math.floor(diffMs / hour);
    return `${h}시간 전`;
  }
  if (diffMs < 2 * day) return '어제';
  if (diffMs < month) {
    const d = Math.floor(diffMs / day);
    return `${d}일 전`;
  }
  if (diffMs < year) {
    const mo = Math.floor(diffMs / month);
    return `${mo}개월 전`;
  }
  const y = Math.floor(diffMs / year);
  return `${y}년 전`;
}

/**
 * 절대 날짜 포맷 (YYYY-MM-DD HH:mm) - KST 기준
 * @param {string|Date} dateLike
 * @returns {string}
 */
export function formatAbsoluteKST(dateLike) {
  const d = toKstDate(dateLike);
  if (!d) return '';
  const y = d.getUTCFullYear();
  const M = String(d.getUTCMonth() + 1).padStart(2, '0');
  const D = String(d.getUTCDate()).padStart(2, '0');
  const hh = String(d.getUTCHours()).padStart(2, '0');
  const mm = String(d.getUTCMinutes()).padStart(2, '0');
  return `${y}-${M}-${D} ${hh}:${mm}`;
}

/**
 * 주어진 월의 1일부터 마지막 날까지의 날짜 범위를 반환
 * @param {string} monthDate - YYYY-MM 형식의 월 문자열 (예: "2025-07")
 * @returns {Object} { startDate: "YYYY-MM-01", endDate: "YYYY-MM-DD" } 형태의 객체
 * @example
 * getMonthDateRange("2025-07") // { startDate: "2025-07-01", endDate: "2025-07-31" }
 * getMonthDateRange("2025-02") // { startDate: "2025-02-01", endDate: "2025-02-28" }
 */
export function getMonthDateRange(monthDate) {
  if (!monthDate || typeof monthDate !== 'string') {
    throw new Error('monthDate는 YYYY-MM 형식의 문자열이어야 합니다.');
  }

  // YYYY-MM 형식 검증
  const monthRegex = /^\d{4}-\d{2}$/;
  if (!monthRegex.test(monthDate)) {
    throw new Error('monthDate는 YYYY-MM 형식이어야 합니다. (예: "2025-07")');
  }

  const [year, month] = monthDate.split('-').map(Number);
  
  // 유효한 월인지 검증 (1-12)
  if (month < 1 || month > 12) {
    throw new Error('월은 01부터 12까지 입력해야 합니다.');
  }

  // 시작일: 해당 월의 1일
  const startDate = `${year}-${month.toString().padStart(2, '0')}-01`;
  
  // 종료일: 해당 월의 마지막 날
  const lastDay = new Date(year, month, 0).getDate(); // 다음 달 0일 = 이번 달 마지막 날
  const endDate = `${year}-${month.toString().padStart(2, '0')}-${lastDay.toString().padStart(2, '0')}`;

  return {
    startDate,
    endDate
  };
} 