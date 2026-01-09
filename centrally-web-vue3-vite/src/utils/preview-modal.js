import { ref } from 'vue';

const isPreviewVisible = ref(false);
const previewImage = ref('');
const isZoomed = ref(false);
const zoomedPosition = ref({ x: 0, y: 0 });
const dragStart = ref({ x: 0, y: 0 });
const isDragging = ref(false);
const zoomOrigin = ref({ x: 0, y: 0 });
let animationFrameId = null;
let lastTapTime = 0;

const isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0;

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
  if (isDragging.value) return;

  if (isTouchDevice && event.type === 'touchstart') {
    const currentTime = new Date().getTime();
    const timeDifference = currentTime - lastTapTime;

    if (timeDifference < 300 && timeDifference > 0) {
      handleZoom(event.touches[0].clientX, event.touches[0].clientY, event.target);
    }

    lastTapTime = currentTime;
  } else if (!isTouchDevice && event.type === 'dblclick') {
    handleZoom(event.clientX, event.clientY, event.target);
  }
};

const handleZoom = (clientX, clientY, target) => {
  if (!isZoomed.value) {
    isZoomed.value = true;
    const rect = target.getBoundingClientRect();
    zoomOrigin.value = {
      x: clientX - rect.left,
      y: clientY - rect.top,
    };
  } else {
    isZoomed.value = false;
    zoomedPosition.value = { x: 0, y: 0 };
  }
};

const startDrag = (event) => {
  if (!isZoomed.value) return;
  isDragging.value = true;
  const clientX = event.touches ? event.touches[0].clientX : event.clientX;
  const clientY = event.touches ? event.touches[0].clientY : event.clientY;
  dragStart.value = { x: clientX, y: clientY };
  window.addEventListener(event.type.includes('touch') ? 'touchmove' : 'mousemove', onDrag);
  event.preventDefault();
};

const onDrag = (event) => {
  if (!isDragging.value || !isZoomed.value) return;

  const clientX = event.touches ? event.touches[0].clientX : event.clientX;
  const clientY = event.touches ? event.touches[0].clientY : event.clientY;
  const deltaX = clientX - dragStart.value.x;
  const deltaY = clientY - dragStart.value.y;

  const image = document.querySelector('.preview-modal-image');
  const container = document.querySelector('.preview-modal-content');
  
  if (image && container) {
    const containerRect = container.getBoundingClientRect();
    const imageRect = image.getBoundingClientRect();
    
    // Calculate the max drag limits
    const maxX = Math.max((imageRect.width * 1.5 - containerRect.width) / 2, 0);
    const maxY = Math.max((imageRect.height * 1.5 - containerRect.height) / 2, 0);

    let newX = zoomedPosition.value.x + deltaX;
    let newY = zoomedPosition.value.y + deltaY;

    // Constrain the new position within the limits
    newX = Math.max(Math.min(newX, maxX), -maxX);
    newY = Math.max(Math.min(newY, maxY), -maxY);

    if (!animationFrameId) {
      animationFrameId = requestAnimationFrame(() => {
        zoomedPosition.value = { x: newX, y: newY };
        dragStart.value = { x: clientX, y: clientY };
        animationFrameId = null;
      });
    }
  }

  event.preventDefault();
};

const endDrag = (event) => {
  if (!isZoomed.value) return;
  isDragging.value = false;
  window.removeEventListener(event.type.includes('touch') ? 'touchmove' : 'mousemove', onDrag);
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
