/**
 * 전역 이미지 에러 핸들러
 * 
 * 모든 img 태그에 자동으로 error 이벤트 리스너를 추가하여
 * 이미지 로드 실패 시 일관된 엑박 아이콘과 디자인을 표시하고
 * 백엔드 에러 메시지를 토스트로 표시
 */

// 전역 에러 표시 플래그 (중복 토스트 방지)
let hasShownErrorToast = false;
let errorToastResetTimer = null;

/**
 * 이미지 엑박 아이콘 적용
 * @param {HTMLImageElement} img - 이미지 요소
 */
function applyErrorIcon(img) {
  // 이미 에러 아이콘이 적용되어 있으면 스킵
  if (img.classList.contains('image-error')) {
    return;
  }

  // 원본 src 저장 (필요시 사용)
  if (!img.dataset.originalSrc) {
    img.dataset.originalSrc = img.src;
  }

  // 에러 클래스 추가
  img.classList.add('image-error');
  
  // 이미지 숨기고 대체 아이콘 표시를 위한 처리
  img.style.display = 'none';
  
  // 부모 요소 확인
  const parent = img.parentElement;
  if (!parent) return;

  // 이미 에러 플레이스홀더가 있으면 스킵
  if (parent.querySelector('.image-error-placeholder')) {
    return;
  }

  // 에러 플레이스홀더 생성
  const placeholder = document.createElement('div');
  placeholder.className = 'image-error-placeholder';
  placeholder.innerHTML = '<i class="ri-image-line"></i>';
  
  // 원본 이미지의 스타일 정보 복사
  const imgStyle = window.getComputedStyle(img);
  if (imgStyle.width && imgStyle.width !== 'auto') {
    placeholder.style.width = imgStyle.width;
  }
  if (imgStyle.height && imgStyle.height !== 'auto') {
    placeholder.style.height = imgStyle.height;
  }
  if (imgStyle.maxWidth && imgStyle.maxWidth !== 'none') {
    placeholder.style.maxWidth = imgStyle.maxWidth;
  }
  if (imgStyle.maxHeight && imgStyle.maxHeight !== 'none') {
    placeholder.style.maxHeight = imgStyle.maxHeight;
  }
  if (imgStyle.borderRadius) {
    placeholder.style.borderRadius = imgStyle.borderRadius;
  }

  // 이미지 다음에 플레이스홀더 삽입
  img.parentNode.insertBefore(placeholder, img.nextSibling);
}

/**
 * 백엔드 에러 메시지 확인 및 토스트 표시
 * @param {string} imageUrl - 이미지 URL
 */
async function checkBackendError(imageUrl) {
  // 이미 에러 토스트를 표시했으면 스킵 (중복 방지)
  if (hasShownErrorToast) {
    return;
  }

  // 유효하지 않은 URL은 무시
  if (!imageUrl || !imageUrl.startsWith('http')) {
    return;
  }

  // 플래그 설정 (다른 이미지들의 fetch 방지)
  hasShownErrorToast = true;

  // 3초 후 플래그 리셋 (다른 페이지로 이동했을 때를 위해)
  if (errorToastResetTimer) {
    clearTimeout(errorToastResetTimer);
  }
  errorToastResetTimer = setTimeout(() => {
    hasShownErrorToast = false;
  }, 3000);

  try {
    // toast store 동적 import (main.js 초기화 순서 문제 회피)
    const { useToastStore } = await import('@/store/toast');
    const toastStore = useToastStore();

    // credentials: 'include'로 쿠키 포함하여 인증 정보 전달
    const response = await fetch(imageUrl, {
      credentials: 'include'
    });

    if (!response.ok) {
      let errorMessage = '이미지를 불러올 수 없습니다.';

      // JSON 파싱 시도 (Content-Type이 없어도 백엔드가 JSON을 보낼 수 있음)
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorData.error || errorMessage;
      } catch (jsonErr) {
        // JSON 파싱 실패 시 기본 메시지 사용
      }
      // 토스트 표시
      toastStore.error(errorMessage);
    }
  } catch (err) {
    // fetch 실패 시 (네트워크 에러 등)
    try {
      const { useToastStore } = await import('@/store/toast');
      const toastStore = useToastStore();
      toastStore.error('이미지를 불러올 수 없습니다.');
    } catch (e) {
      // toast store를 불러올 수 없는 경우 무시
    }
  }
}

/**
 * 전역 이미지 에러 핸들러
 * @param {Event} event - 이미지 error 이벤트
 */
async function handleGlobalImageError(event) {
  const img = event.target;
  const imageUrl = img.src;

  // 엑박 아이콘 적용 (항상 실행)
  applyErrorIcon(img);

  // 백엔드 에러 메시지 확인 (비동기, 중복 방지)
  checkBackendError(imageUrl);
}

/**
 * 전역 이미지 에러 핸들러 초기화
 * MutationObserver를 사용하여 동적으로 추가되는 img 태그에도 자동으로 적용
 */
export function initGlobalImageErrorHandler() {
  // 기존 img 태그에 이벤트 리스너 추가
  document.querySelectorAll('img').forEach(img => {
    img.addEventListener('error', handleGlobalImageError, { once: true });
  });

  // MutationObserver로 새로 추가되는 img 태그에도 자동 적용
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      mutation.addedNodes.forEach((node) => {
        if (node.nodeType === 1) { // Element node
          // 추가된 노드가 img인 경우
          if (node.tagName === 'IMG') {
            node.addEventListener('error', handleGlobalImageError, { once: true });
          }
          // 추가된 노드 내부의 img 태그들
          const images = node.querySelectorAll && node.querySelectorAll('img');
          if (images) {
            images.forEach((img) => {
              img.addEventListener('error', handleGlobalImageError, { once: true });
            });
          }
        }
      });
    });
  });

  // DOM 변경 감지 시작
  observer.observe(document.body, {
    childList: true,
    subtree: true
  });

  return observer;
}

