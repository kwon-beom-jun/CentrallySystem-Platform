<template>
  <div v-if="isPreviewVisible" class="preview-modal" @click.self="onOverlayTap">
    <div
      class="preview-modal-content"
      @mousedown="startDrag"   @mousemove="onDrag"
      @mouseup="endDrag"       @mouseleave="endDrag"
      @touchstart="startDrag"  @touchmove="onDrag"
      @touchend="endDrag"
    >
      <button type="button" class="close-btn" aria-label="닫기" @click.stop.prevent="onCloseTap" @touchstart.stop @touchend.stop.prevent="onCloseTap">
        <span aria-hidden="true">×</span>
      </button>
      <img
        :src="previewImage"
        class="preview-modal-image"
        :class="{ zoomed: isZoomed }"
        :style="{
          transform: isZoomed
            ? `translate(${zoomedPosition.x}px, ${zoomedPosition.y}px) scale(1.5)`
            : 'none',
          transformOrigin: `${zoomOrigin.x}px ${zoomOrigin.y}px`
        }"
        @dblclick="toggleZoom"
        @touchstart="toggleZoom"
      />
    </div>
  </div>
</template>

<script setup>
import { defineExpose, onMounted, onBeforeUnmount, watch } from 'vue';
import { usePreviewModal } from '@/utils/preview-modal';

// 미리보기 상태/핸들러 (공통 로직 재사용)
const {
  isPreviewVisible,
  previewImage,
  isZoomed,
  zoomedPosition,
  zoomOrigin,
  openPreviewModal,
  toggleZoom,
  startDrag,
  onDrag,
  endDrag,
  closePreviewModal,
} = usePreviewModal();

/**
 * 외부에서 열기 위한 공개 메서드
 * @param {string} imageUrl 미리보기로 표시할 이미지 URL
 */
function open(imageUrl) {
  openPreviewModal(imageUrl);
  // 뒤로가기로 닫히도록 더미 히스토리 추가
  history.pushState({ modalOpen: true }, '');
}

/**
 * 외부/오버레이 클릭으로 닫기
 */
function close() {
  closePreviewModal();
  // 수동 닫기 시 더미 히스토리 제거
  if (history.state?.modalOpen) {
    history.back();
  }
}

/**
 * 모바일/브라우저 뒤로가기(popstate) 발생 시 모달만 닫기
 */
function handleBackToClose() {
  if (!isPreviewVisible.value) return;
  // 뒤로가기로 닫힐 때는 history.back() 호출하지 않음
  closePreviewModal();
}

/**
 * 터치/클릭 클릭스루 방지를 위한 안전 닫기 핸들러
 * - requestAnimationFrame 2회로 렌더 사이클 이후 DOM 제거
 */
function onCloseTap() {
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      close();
    });
  });
}

/**
 * 오버레이 탭으로 닫기 시에도 클릭스루 방지
 */
function onOverlayTap(event) {
  event?.preventDefault?.();
  event?.stopPropagation?.();
  onCloseTap();
}

/**
 * 모달 열림/닫힘에 따른 배경 스크롤 제어
 */
let savedScrollY = 0;
watch(isPreviewVisible, (visible) => {
  const body = document.body;
  if (visible) {
    // 모달 열릴 때: 현재 스크롤 위치 저장 후 스크롤 잠금
    savedScrollY = window.scrollY;
    body.style.position = 'fixed';
    body.style.top = `-${savedScrollY}px`;
    body.style.width = '100%';
    body.style.overflow = 'hidden';
  } else {
    // 모달 닫힐 때: 스크롤 잠금 해제 후 원래 위치로 복원
    body.style.position = '';
    body.style.top = '';
    body.style.width = '';
    body.style.overflow = '';
    window.scrollTo(0, savedScrollY);
  }
});

// 라이프사이클: 뒤로가기(popstate) 리스너 등록/해제
onMounted(() => window.addEventListener('popstate', handleBackToClose));
onBeforeUnmount(() => window.removeEventListener('popstate', handleBackToClose));

defineExpose({ open, close });
</script>

<style scoped>
.preview-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
.preview-modal-content {
  position: relative;
  overflow: visible !important;
}
.preview-modal-image {
  max-width: 80vw;
  max-height: 80vh;
  transition: transform 0.2s ease;
  position: relative;
  z-index: 1;
}
.close-btn {
  position: absolute;
  top: -15px;
  right: -15px;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(156, 156, 156, 0.6);
  color: #ffffff;
  font-size: 22px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 2;
}
.close-btn:hover { background: rgba(0, 0, 0, 0.75); }
</style>


