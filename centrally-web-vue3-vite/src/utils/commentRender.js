import DOMPurify from 'dompurify'

export const defaultSanitizeOptions = {
  USE_PROFILES: { html: true },
  ADD_TAGS: ['a', 'br', 'p', 'pre'],
  ADD_ATTR: ['target', 'rel', 'href', 'class', 'style'],
  FORBID_TAGS: ['script', 'iframe', 'object']
}

export function escapeHtml(str) {
  if (str == null) return ''
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

export function linkifyUrls(str) {
  if (!str) return ''
  const urlRegex = /((https?:\/\/|www\.)[\w\-._~:\/?#\[\]@!$&'()*+,;=%]+)(?![^<]*?>)/gi
  return str.replace(urlRegex, (match) => {
    const href = match.startsWith('www.') ? `http://${match}` : match
    return `<a href="${href}" target="_blank" rel="noopener noreferrer">${match}</a>`
  })
}

export function renderCommentYouTubeStyle(text, sanitizeOptions = defaultSanitizeOptions) {
  const escaped = escapeHtml(typeof text === 'string' ? text : '')
  const withLinks = linkifyUrls(escaped)
  return DOMPurify.sanitize(withLinks, sanitizeOptions)
}

// Alias: 일반적인 이름으로도 사용 가능하도록 제공
export function renderCommentStyle(text, sanitizeOptions = defaultSanitizeOptions) {
  return renderCommentYouTubeStyle(text, sanitizeOptions)
}

// 에디터 본문(공지 등)용: 최소 태그 유지 + 기본 텍스트 서식 허용
export const contentSanitizeOptions = {
  USE_PROFILES: { html: true },
  ADD_TAGS: [
    'span', 'strong', 'b', 'em', 'i', 'u', 's',
    'p', 'br', 'ul', 'ol', 'li', 'blockquote', 'pre', 'code'
  ],
  ADD_ATTR: ['class', 'style', 'data-list', 'data-indent', 'data-checked', 'target', 'rel'],
  FORBID_TAGS: ['script', 'iframe', 'object']
}

export function sanitizeHtmlContent(html, sanitizeOptions = contentSanitizeOptions) {
  return DOMPurify.sanitize(html || '', sanitizeOptions)
}

function convertWhitespaceToHtml(str) {
  // 개행 -> <br>, 연속 공백(2개 이상) -> &nbsp; 반복
  const withBr = str.replace(/\r\n|\n/g, '<br>')
  return withBr.replace(/ {2,}/g, (m) => '&nbsp;'.repeat(m.length))
}

// 단일 진입점: mode에 따라 댓글/본문 렌더를 선택
// options: { mode: 'comment' | 'content', whitespace: 'css' | 'br' }
export function renderSafeHtml(value, { mode = 'comment', whitespace = 'css' } = {}) {
  const input = typeof value === 'string' ? value : ''
  if (mode === 'content') {
    return sanitizeHtmlContent(input, contentSanitizeOptions)
  }
  // comment 모드
  let escaped = escapeHtml(input)
  if (whitespace === 'br') {
    escaped = convertWhitespaceToHtml(escaped)
  }
  const withLinks = linkifyUrls(escaped)
  return DOMPurify.sanitize(withLinks, defaultSanitizeOptions)
}


