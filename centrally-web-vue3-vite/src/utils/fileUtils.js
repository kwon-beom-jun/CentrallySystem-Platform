/**
 * 파일 URL 관련 유틸리티 함수
 */

/**
 * URL에서 경로만 추출 (전체 URL이면 pathname만, 상대 경로면 그대로)
 */
export function extractPath(url) {
  if (!url) return '';
  
  if (url.startsWith('http://') || url.startsWith('https://')) {
    try {
      return new URL(url).pathname;
    } catch (e) {
      return url;
    }
  }
  
  return url.startsWith('/') ? url : '/' + url;
}

/**
 * 경로에서 서비스 prefix 제거 (baseURL에 이미 포함되어 있으므로)
 * 예: /info/upload/info/... → /upload/info/...
 * 예: /upload/hrm/profile/... → /upload/hrm/profile/...
 */
export function removeServicePrefix(path, serviceName) {
  if (!serviceName) return path;
  
  // /{serviceName}/upload/로 시작하면 앞의 /{serviceName}만 제거
  // 예: /hrm/upload/hrm/profile/... → /upload/hrm/profile/...
  if (path.startsWith(`/${serviceName}/upload/`)) {
    return path.substring(serviceName.length + 1); // '/{serviceName}' 제거, '/'는 유지
  }
  // /upload/{serviceName}/로 시작하는 경우는 이미 올바른 형태이므로 그대로 반환
  // 예: /upload/hrm/profile/... → /upload/hrm/profile/...
  if (path.startsWith(`/upload/${serviceName}/`)) {
    return path;
  }
  // /{serviceName}/로 시작하는 경우도 처리
  if (path.startsWith(`/${serviceName}/`)) {
    return path.substring(serviceName.length + 1);
  }
  return path;
}

/**
 * baseURL에서 서비스명 추출 (경로 부분만 파싱)
 */
function getServiceNameFromBaseURL(baseURL) {
  if (!baseURL) return null;
  
  try {
    const url = new URL(baseURL);
    // 경로 부분만 추출 (예: /info, /receipt)
    const pathname = url.pathname.replace(/\/$/, ''); // 끝의 슬래시 제거
    const segments = pathname.split('/').filter(s => s);
    
    // 마지막 경로 세그먼트가 서비스명 (예: /info -> info, /receipt -> receipt)
    if (segments.length > 0) {
      return segments[segments.length - 1];
    }
  } catch (e) {
    // URL 파싱 실패 시 정규식으로 fallback
    // 경로 부분만 추출 (도메인은 무시)
    const match = baseURL.match(/:\d+\/([^\/\?]+)/); // 포트 뒤의 첫 경로 세그먼트
    if (match) {
      return match[1];
    }
  }
  
  return null;
}

/**
 * 이미지 미리보기용 URL 생성
 * - 기본 프로필 이미지(/img/profile/random/...): 프론트엔드 정적 리소스로 처리
 * - 업로드된 프로필(/upload/{serviceName}/profile/...): 백엔드 컨트롤러를 통해 접근
 */
export function getImagePreviewUrl(fileUrl, baseURL, serviceName = null) {
  if (!fileUrl) return '';
  
  // 기본 프로필 이미지는 프론트엔드 정적 리소스로 처리 (/img/profile/random/...)
  if (fileUrl.startsWith('/img/profile/')) {
    return fileUrl;  // 그대로 반환 (Vite 개발 서버나 Nginx가 자동으로 public 폴더에서 서빙)
  }
  
  // serviceName이 없으면 baseURL에서 추출
  if (!serviceName && baseURL) {
    serviceName = getServiceNameFromBaseURL(baseURL);
  }
  
  let path = extractPath(fileUrl);
  path = removeServicePrefix(path, serviceName);
  
  const cleanBase = (baseURL || '').replace(/\/$/, '');
  return cleanBase + path;
}

/**
 * 파일 접근용 경로 정규화 (서비스 prefix 제거)
 */
export function normalizeDownloadPath(fileUrl, serviceName = null) {
  let path = extractPath(fileUrl);
  
  // serviceName이 없으면 경로에서 추출 시도
  if (!serviceName) {
    // /upload/info/... 또는 /upload/receipt/... 형태에서 추출
    const match = path.match(/\/upload\/([^\/]+)\//);
    if (match) {
      serviceName = match[1];
    }
  }
  
  return removeServicePrefix(path, serviceName);
}

