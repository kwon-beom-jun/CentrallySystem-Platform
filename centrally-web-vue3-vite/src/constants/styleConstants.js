/* ═══════════════════════════════════════════════════════════════
 * 스타일 상수 (Style Constants)
 * ═══════════════════════════════════════════════════════════════
 * 
 * HRM 스타일 서비스에서 사용하는 모든 상수를 중앙 관리합니다.
 */

/* ═══════════════════════════════════════════════════════════════
 * 스타일 카테고리 (Style Category)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 스타일 카테고리 코드
 */
export const STYLE_CATEGORY = {
  MAIN_CARD: 'MAIN_CARD',       // 메인 페이지 카드 스타일
  INFO_MOBILE: 'INFO_MOBILE',   // 기본 정보 모바일 스타일
};

/**
 * 스타일 카테고리 → 한글 라벨 매핑
 */
export const STYLE_CATEGORY_LABELS = {
  [STYLE_CATEGORY.MAIN_CARD]: '메인 카드',
  [STYLE_CATEGORY.INFO_MOBILE]: '기본 정보 모바일',
};

/* ═══════════════════════════════════════════════════════════════
 * 메인 카드 스타일 코드 (Main Card Style)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 메인 카드 스타일 코드
 */
export const MAIN_CARD_STYLE = {
  DEFAULT: 'default',
  VER1: 'ver1',
  VER2: 'ver2',
  VER3: 'ver3',
  VER4: 'ver4',
};

/**
 * 메인 카드 스타일 → 한글 라벨 매핑
 */
export const MAIN_CARD_STYLE_LABELS = {
  [MAIN_CARD_STYLE.DEFAULT]: '기본',
  [MAIN_CARD_STYLE.VER1]: '스타일 1',
  [MAIN_CARD_STYLE.VER2]: '스타일 2',
  [MAIN_CARD_STYLE.VER3]: '스타일 3',
  [MAIN_CARD_STYLE.VER4]: '스타일 4',
};

/* ═══════════════════════════════════════════════════════════════
 * 기본 정보 모바일 테마 코드 (Info Mobile Theme)
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 기본 정보 모바일 테마 코드
 */
export const INFO_MOBILE_STYLE = {
  LIGHT: 'light',
  DARK: 'dark',
};

/**
 * 기본 정보 모바일 테마 → 한글 라벨 매핑
 */
export const INFO_MOBILE_STYLE_LABELS = {
  [INFO_MOBILE_STYLE.LIGHT]: '밝은 테마',
  [INFO_MOBILE_STYLE.DARK]: '어두운 테마',
};

/**
 * 기본 정보 모바일 테마 → data-theme 속성값 매핑
 */
export const INFO_MOBILE_STYLE_THEMES = {
  [INFO_MOBILE_STYLE.LIGHT]: 'light',
  [INFO_MOBILE_STYLE.DARK]: 'dark',
};

/* ═══════════════════════════════════════════════════════════════
 * 유틸 함수
 * ═══════════════════════════════════════════════════════════════ */

/**
 * 스타일 카테고리 코드를 한글 라벨로 변환
 * @param {string} category - 카테고리 코드 (예: 'MAIN_CARD', 'INFO_MOBILE')
 * @returns {string} 한글 라벨 (예: '메인 카드', '기본 정보 모바일')
 */
export function getStyleCategoryLabel(category) {
  return STYLE_CATEGORY_LABELS[category] || category;
}

/**
 * 메인 카드 스타일 코드를 한글 라벨로 변환
 * @param {string} code - 스타일 코드 (예: 'default', 'ver1')
 * @returns {string} 한글 라벨 (예: '기본', '스타일 1')
 */
export function getMainCardStyleLabel(code) {
  return MAIN_CARD_STYLE_LABELS[code] || '기본';
}

/**
 * 기본 정보 모바일 테마 코드를 한글 라벨로 변환
 * @param {string} code - 테마 코드 (예: 'light', 'dark')
 * @returns {string} 한글 라벨 (예: '밝은 테마', '어두운 테마')
 */
export function getInfoMobileStyleLabel(code) {
  return INFO_MOBILE_STYLE_LABELS[code] || '밝은 테마';
}

/**
 * 기본 정보 모바일 테마 코드를 data-theme 속성값으로 변환
 * @param {string} code - 테마 코드 (예: 'light', 'dark')
 * @returns {string} data-theme 속성값 (예: 'light', 'dark')
 */
export function getInfoMobileStyleTheme(code) {
  return INFO_MOBILE_STYLE_THEMES[code] || INFO_MOBILE_STYLE_THEMES[INFO_MOBILE_STYLE.LIGHT];
}

