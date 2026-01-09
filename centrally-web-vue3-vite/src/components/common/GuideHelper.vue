<template>
  <!-- 가이드 헬퍼 -->
  <div 
    class="guide-helper"
    :style="{ right: position.x + 'px', bottom: position.y + 'px' }"
    @mousedown="startDrag"
    @touchstart="startDrag"
  >
    <!-- 헤드셋 아이콘 -->
    <div class="icon-headset" :class="{ 'show-mobile': showBubble }">🎧</div>
    
    <!-- 말풍선 -->
    <div class="speech-bubble" :class="{ 'show-mobile': showBubble }" @click.stop="handleBubbleClick">
      <div class="bubble-text">
        <p class="bubble-title"><span class="text-blue">{{ $t('common.guide.title') }}</span> </p>
        <p class="bubble-subtitle" v-html="$t('common.guide.clickHint')"></p>
      </div>
    </div>
    
    <!-- 가이드 헬퍼 이미지 -->
    <div class="helper-face" @click.stop="handleIconClick">
      <img src="/guide/helper.png" alt="Guide Helper" class="helper-image" draggable="false" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ROUTES } from '@/config/menuConfig';

const router = useRouter();
const { t } = useI18n();
const showBubble = ref(false);
let autoHideTimer = null;

// 드래그 관련 상태
const getInitialPosition = () => {
  const isMobile = window.innerWidth <= 650;
  return isMobile ? { x: 10, y: 0 } : { x: 100, y: 30 };
};

const position = ref(getInitialPosition()); // 초기 위치 (right, bottom)
const isDragging = ref(false);
const dragStart = ref({ x: 0, y: 0 });
const clickThreshold = 10; // 클릭과 드래그를 구분하기 위한 이동 거리 임계값
let totalMoved = 0;

/**
 * 드래그 시작
 */
const startDrag = (e) => {
  totalMoved = 0;
  
  // 터치 이벤트와 마우스 이벤트 모두 지원
  const clientX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX;
  const clientY = e.type.includes('touch') ? e.touches[0].clientY : e.clientY;
  
  dragStart.value = {
    x: clientX,
    y: clientY,
    startPosX: position.value.x,
    startPosY: position.value.y
  };
  
  // 드래그 중 이벤트 리스너 등록
  if (e.type.includes('touch')) {
    document.addEventListener('touchmove', onDrag, { passive: false });
    document.addEventListener('touchend', stopDrag);
  } else {
    document.addEventListener('mousemove', onDrag);
    document.addEventListener('mouseup', stopDrag);
  }
};

/**
 * 드래그 중
 */
const onDrag = (e) => {
  const clientX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX;
  const clientY = e.type.includes('touch') ? e.touches[0].clientY : e.clientY;
  
  // 이동 거리 계산 (right, bottom 기준이므로 반대 방향)
  const deltaX = dragStart.value.x - clientX;
  const deltaY = dragStart.value.y - clientY;
  
  // 총 이동 거리 누적
  const movedDistance = Math.abs(deltaX) + Math.abs(deltaY);
  totalMoved = movedDistance;
  
  // 임계값을 넘었을 때만 실제 드래그로 간주
  if (movedDistance > clickThreshold) {
    isDragging.value = true;
    
    // 새로운 위치 계산
    const newX = dragStart.value.startPosX + deltaX;
    const newY = dragStart.value.startPosY + deltaY;
    
    // 화면 경계 체크
    const maxX = window.innerWidth - 150;
    const maxY = window.innerHeight - 150;
    
    position.value = {
      x: Math.max(10, Math.min(newX, maxX)),
      y: Math.max(10, Math.min(newY, maxY))
    };
    
    if (e.type.includes('touch')) {
      e.preventDefault();
    }
  }
};

/**
 * 드래그 종료
 */
const stopDrag = (e) => {
  // 이벤트 리스너 제거
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
  document.removeEventListener('touchmove', onDrag);
  document.removeEventListener('touchend', stopDrag);
  
  // 드래그가 아니었다면 isDragging을 바로 false로
  setTimeout(() => {
    isDragging.value = false;
  }, 10);
};

/**
 * 헬퍼 아이콘 클릭 핸들러
 * 모바일: 첫 클릭시 말풍선 표시, 두번째 클릭시 가이드로 이동
 * 데스크톱: 바로 가이드로 이동
 */
const handleIconClick = () => {
  // 드래그한 경우 클릭 이벤트 무시
  if (totalMoved > clickThreshold) {
    return;
  }
  const isMobile = window.innerWidth <= 650;
  
  if (isMobile) {
    if (showBubble.value) {
      // 말풍선이 이미 표시된 경우 가이드로 이동
      router.push(ROUTES.GUIDE);
    } else {
      // 말풍선 표시
      showBubble.value = true;
      
      // 기존 타이머가 있으면 클리어
      if (autoHideTimer) {
        clearTimeout(autoHideTimer);
      }
      
      // 3초 후 자동으로 말풍선 숨김
      autoHideTimer = setTimeout(() => {
        showBubble.value = false;
      }, 3000);
    }
  } else {
    // 데스크톱은 바로 가이드로 이동
    router.push('/guide');
  }
};

/**
 * 말풍선 클릭 핸들러
 * 말풍선 클릭 시 가이드로 이동
 */
const handleBubbleClick = () => {
  // 드래그한 경우 클릭 이벤트 무시
  if (totalMoved > clickThreshold) {
    return;
  }
  
  router.push('/guide');
};

/**
 * 외부 클릭 감지하여 말풍선 숨김
 */
const handleOutsideClick = () => {
  if (showBubble.value) {
    showBubble.value = false;
    
    // 타이머 클리어
    if (autoHideTimer) {
      clearTimeout(autoHideTimer);
      autoHideTimer = null;
    }
  }
};

// 컴포넌트 마운트 시 외부 클릭 리스너 등록
onMounted(() => {
  document.addEventListener('click', handleOutsideClick);
});

// 컴포넌트 언마운트 시 리스너 제거 및 타이머 클리어
onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick);
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
  document.removeEventListener('touchmove', onDrag);
  document.removeEventListener('touchend', stopDrag);
  if (autoHideTimer) {
    clearTimeout(autoHideTimer);
  }
});
</script>

<style scoped>
/* ═══════════════════════ 가이드 헬퍼 ═══════════════════════ */
.guide-helper {
  position: fixed;
  z-index: 999;
  animation: float 3s ease-in-out infinite;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  cursor: move;
  user-select: none;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-15px);
  }
}

/* 헤드셋 아이콘 */
.icon-headset {
  position: absolute;
  top: -5px;
  left: -25px;
  font-size: 24px;
  animation: pulse 2s ease-in-out infinite;
  z-index: 10;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.15);
    opacity: 0.9;
  }
}

/* 말풍선 */
.speech-bubble {
  position: relative;
  background: #ffffffa8;
  color: #000000;
  padding: 16px 20px;
  border-radius: 16px;
  margin-bottom: 8px;
  margin-right: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border: 2px solid #e5e7eb;
  min-width: 200px;
  transition: all 0.3s ease;
  z-index: 5;
  cursor: pointer;
}

.speech-bubble::after {
  content: '';
  position: absolute;
  bottom: -8px;
  right: 15px;
  width: 0;
  height: 0;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-top: 10px solid #ffffff;
  filter: drop-shadow(2px 3px 2px rgba(0, 0, 0, 0.1));
}

.guide-helper:hover .speech-bubble {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

/* 말풍선 텍스트 */
.bubble-text {
  position: relative;
  z-index: 1;
}

.bubble-title {
  font-size: 0.95rem;
  font-weight: 700;
  margin: 0 0 8px 0;
  text-align: left;
  color: #000000;
}

.text-blue {
  color: #3EC2FF;
}

.bubble-subtitle {
  font-size: 0.8rem;
  font-weight: 400;
  margin: 0;
  text-align: left;
  line-height: 1.5;
  color: #333333;
}

/* 헬퍼 이미지 */
.helper-face {
  position: relative;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
  z-index: 6;
  cursor: grab;
}

.helper-face:active {
  cursor: grabbing;
}

.helper-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.guide-helper:hover .helper-face {
  transform: scale(1.1);
}

/* ═══════════════════════ 모바일 반응형 ═══════════════════════ */
@media (max-width: 650px) {
  .guide-helper {
    cursor: move;
  }

  /* 모바일에서 기본적으로 말풍선과 헤드셋 아이콘 숨김 */
  .speech-bubble {
    display: none;
    opacity: 0;
    transform: translateY(10px);
    transition: opacity 0.3s ease, transform 0.3s ease;
  }

  /* 클릭 시 표시 */
  .speech-bubble.show-mobile {
    display: block;
    opacity: 1;
    transform: translateY(0);
    min-width: 140px;
    padding: 10px 12px;
    margin-bottom: 0px;
  }

  .speech-bubble.show-mobile .bubble-title {
    font-size: 0.75rem;
  }

  .speech-bubble.show-mobile .bubble-subtitle {
    font-size: 0.65rem;
  }

  .icon-headset {
    display: none;
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  /* 클릭 시 표시 */
  .icon-headset.show-mobile {
    display: block;
    opacity: 1;
    font-size: 15px;
    top: -3px;
    left: -10px;
  }

  .helper-face {
    width: 80px;
    height: 80px;
  }
}
</style>

