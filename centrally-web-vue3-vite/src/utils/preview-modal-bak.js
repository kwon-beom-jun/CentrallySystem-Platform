import { ref } from 'vue';

const isPreviewVisible = ref(false);
const previewImage = ref('');
const isZoomed = ref(false);
const zoomedPosition = ref({ x: 0, y: 0 });
const dragStart = ref({ x: 0, y: 0 });
const isDragging = ref(false);
const zoomOrigin = ref({ x: 0, y: 0 });
let animationFrameId = null;

const openPreviewModal = (imageUrl) => {
  previewImage.value = imageUrl;
  isPreviewVisible.value = true;
  isZoomed.value = false;
  zoomedPosition.value = { x: 0, y: 0 };
  zoomOrigin.value = { x: 0, y: 0 };
};

const closePreviewModal = () => {
  isPreviewVisible.value = false;
  isZoomed.value = false;
  zoomedPosition.value = { x: 0, y: 0 };
  zoomOrigin.value = { x: 0, y: 0 };
};

const toggleZoom = (event) => {
  if (isDragging.value) return; // 드래그 중에는 확대/축소를 토글하지 않음

  if (!isZoomed.value) {
    isZoomed.value = true;
    const rect = event.target.getBoundingClientRect();
    zoomOrigin.value = {
      x: event.clientX - rect.left,
      y: event.clientY - rect.top,
    };
  } else {
    isZoomed.value = false;
    zoomedPosition.value = { x: 0, y: 0 };
  }
};

const startDrag = (event) => {
  if (!isZoomed.value) return;
  isDragging.value = true;
  dragStart.value = { x: event.clientX, y: event.clientY };
  window.addEventListener('mousemove', onDrag); // 이벤트 등록
  event.preventDefault(); // 기본 동작 방지
};

const onDrag = (event) => {
  if (!isDragging.value || !isZoomed.value) return;

  const deltaX = event.clientX - dragStart.value.x;
  const deltaY = event.clientY - dragStart.value.y;

  // 애니메이션 프레임을 사용하여 딜레이를 줄이고 중복 호출 방지
  if (!animationFrameId) {
    animationFrameId = requestAnimationFrame(() => {
      zoomedPosition.value = {
        x: zoomedPosition.value.x + deltaX,
        y: zoomedPosition.value.y + deltaY,
      };

      dragStart.value = { x: event.clientX, y: event.clientY };
      animationFrameId = null;
    });
  }

  event.preventDefault(); // 기본 동작 방지
};

const endDrag = () => {
  if (!isZoomed.value) return;
  isDragging.value = false; // 드래그 중 상태 해제
  window.removeEventListener('mousemove', onDrag); // 이벤트 제거
};

// Export as a function to use in components
export function usePreviewModal() {
  return {
    isPreviewVisible,
    previewImage,
    isZoomed,
    zoomedPosition,
    zoomOrigin,
    openPreviewModal,
    closePreviewModal,
    toggleZoom,
    startDrag,
    onDrag,
    endDrag,
  };
}
