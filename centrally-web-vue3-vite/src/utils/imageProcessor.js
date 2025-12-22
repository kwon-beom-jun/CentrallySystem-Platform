import imageCompression from 'browser-image-compression';

/**
 * 이미지 파일을 주어진 임계치(maxSizeMB, maxWidthOrHeight)를 초과할 때만
 * browser-image-compression 으로 압축/리사이즈한 뒤, 원본과 동일한 이름의
 * File 객체로 반환합니다.
 * 조건을 충족하지 않는 작은 이미지는 원본 File 을 그대로 반환합니다.
 *
 * @param {File} file                       - 원본 이미지 파일
 * @param {Object} [cfg]
 * @param {number} [cfg.maxSizeMB=2]        - 목표 용량(MB)
 * @param {number} [cfg.maxWidthOrHeight=1024] - 최대 너비/높이(px)
 * @param {number} [cfg.minSizeMB]          - 최소 용량(MB) - 원본이 이것보다 작으면 압축 안 함
 * @returns {Promise<File>}                 - 처리(압축/리사이즈 or 원본)된 File
 */
export async function processImageFile (file,{ maxSizeMB = 2, maxWidthOrHeight = 1024, minSizeMB } = {}) {

  /* 전역 로딩 시작 ------------------------------------------- */
  const { useLoadingStore } = await import('@/store/loading'); // 동적 import
  const loadingStore = useLoadingStore();
  loadingStore.startLoading();

  try {
    if (!(file instanceof File)) {
      throw new Error('processImageFile: "file" 매개변수는 File 인스턴스여야 합니다.');
    }
  
    // 0) minSizeMB가 설정되어 있고, 원본 파일이 그것보다 작으면 압축하지 않고 그대로 반환
    if (minSizeMB && file.size < minSizeMB * 1024 * 1024) {
      return file;
    }
  
    const thresholdBytes = maxSizeMB * 1024 * 1024;
    let blob = file;
  
    // 1) 용량이 임계치를 초과할 때만 압축 시도
    if (file.size > thresholdBytes) {
      try {
        blob = await imageCompression(file, {
          maxSizeMB,
          maxWidthOrHeight,
          useWebWorker: true,
        });
      } catch (err) {
        console.error('processImageFile: 압축 실패 → 원본 사용', err);
        blob = file; // 실패 시 원본 유지
      }
    }
  
    // 2) Blob → File 변환 (preview / FormData upload 편의를 위해)
    const finalFile = new File([blob], file.name, {
      type: blob.type || file.type,
      lastModified: Date.now(),
    });
  
    // 3) 최종 크기 체크: 여전히 임계치 초과 시 예외 발생
    if (finalFile.size > thresholdBytes) {
      throw new Error('FILE_SIZE_EXCEEDED');
    }
  
    return finalFile;
    
  } finally {
    /* 전역 로딩 종료 -------------------------- */
    loadingStore.stopLoading();
  }
} 