/**
 * HTML 원문을 목록/테이블 등 텍스트 영역에 안전하게 보여주기 위한 공통 유틸
 * - 태그 제거 → 엔티티 디코딩 → 공백 정리 순서로 가공
 */

/**
 * HTML 태그 제거 (줄바꿈은 공백으로 치환)
 */
export function stripTags(html) {
  if (!html) return '';
  return String(html)
    .replace(/<[^>]*>?/gm, ' ')
    .replace(/\n/g, ' ');
}

/**
 * HTML 엔티티 디코딩 (`&lt;` → `<` 등)
 */
export function decodeEntities(text) {
  if (!text) return '';
  const textArea = document.createElement('textarea');
  textArea.innerHTML = String(text);
  return textArea.value;
}

/**
 * 공백 정규화 (다중 공백/개행 → 단일 공백, 양끝 trim)
 */
export function normalizeSpaces(text) {
  if (!text) return '';
  return String(text).replace(/\s+/g, ' ').trim();
}

/**
 * 목록/테이블에 표시할 최종 텍스트로 변환
 * - 태그 제거 → 엔티티 디코딩 → 공백 정리
 */
export function toPlainTextFromHtml(html) {
  const withoutTags = stripTags(html);
  const decoded = decodeEntities(withoutTags);
  return normalizeSpaces(decoded);
}


