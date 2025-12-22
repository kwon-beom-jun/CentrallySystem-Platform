/**
 * 브라우저 및 OS 정보 유틸리티
 */

/**
 * 브라우저 정보 추출
 */
export function getBrowserInfo() {
  const userAgent = navigator.userAgent;
  let browser = 'Unknown';
  
  if (userAgent.indexOf('Chrome') > -1 && userAgent.indexOf('Edg') === -1) {
    browser = 'Chrome';
  } else if (userAgent.indexOf('Safari') > -1 && userAgent.indexOf('Chrome') === -1) {
    browser = 'Safari';
  } else if (userAgent.indexOf('Firefox') > -1) {
    browser = 'Firefox';
  } else if (userAgent.indexOf('Edg') > -1) {
    browser = 'Edge';
  } else if (userAgent.indexOf('MSIE') > -1 || userAgent.indexOf('Trident/') > -1) {
    browser = 'IE';
  } else if (userAgent.indexOf('Opera') > -1 || userAgent.indexOf('OPR') > -1) {
    browser = 'Opera';
  }
  
  return browser;
}

/**
 * OS 정보 추출
 */
export function getOSInfo() {
  const userAgent = navigator.userAgent;
  let os = 'Unknown';
  
  if (userAgent.indexOf('Win') > -1) {
    os = 'Windows';
  } else if (userAgent.indexOf('Mac') > -1) {
    os = 'MacOS';
  } else if (userAgent.indexOf('Linux') > -1) {
    os = 'Linux';
  } else if (userAgent.indexOf('Android') > -1) {
    os = 'Android';
  } else if (userAgent.indexOf('iOS') > -1 || userAgent.indexOf('iPhone') > -1 || userAgent.indexOf('iPad') > -1) {
    os = 'iOS';
  }
  
  return os;
}

/**
 * 전체 환경 정보 반환
 */
export function getEnvironmentInfo() {
  return {
    browser: getBrowserInfo(),
    os: getOSInfo(),
    userAgent: navigator.userAgent,
  };
}

