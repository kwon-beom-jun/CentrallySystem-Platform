<template>
  <div class="info-notice" :class="`info-notice--${color}`" :style="{ marginBottom: marginBottom }">
    <i :class="[icon, { 'icon-multiline': isMultiline }]"></i>
    <span v-if="text" ref="textRef" :class="`font-weight-${fontWeight}`">{{ text }}</span>
    <span v-else ref="textRef" :class="`font-weight-${fontWeight}`"><slot></slot></span>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';

/**
 * 안내 문구 컴포넌트
 * 
 * 개인정보 확인/수정 안내 등 정보성 메시지를 표시하는 컴포넌트입니다.
 * 밝은 테마와 어두운 테마를 자동으로 지원합니다.
 */
const props = defineProps({
  /**
   * 안내 문구 텍스트
   */
  text: {
    type: String,
    default: ''
  },
  /**
   * 아이콘 클래스 (RemixIcon)
   * 기본값: ri-information-line
   */
  icon: {
    type: String,
    default: 'ri-information-line'
  },
  /**
   * 색상 변형
   * - blue: 파랑 (기본값)
   * - yellow: 연노랑
   * - red: 연빨강
   * - green: 초록색
   */
  color: {
    type: String,
    default: 'blue',
    validator: (value) => ['blue', 'yellow', 'red', 'green'].includes(value)
  },
  /**
   * 하단 마진 (CSS 값: '20px', '0', '1rem' 등)
   * 기본값: '0'
   */
  marginBottom: {
    type: String,
    default: '0'
  },
  /**
   * 텍스트 굵기
   * - thin: 얇게 (300)
   * - normal: 보통 (500, 기본값)
   * - bold: 굵게 (900)
   */
  fontWeight: {
    type: String,
    default: 'normal',
    validator: (value) => ['thin', 'normal', 'bold'].includes(value)
  }
});

const textRef = ref(null);
const isMultiline = ref(false);

/**
 * 텍스트가 2줄 이상인지 확인
 */
function checkMultiline() {
  nextTick(() => {
    if (textRef.value) {
      const lineHeight = parseFloat(getComputedStyle(textRef.value).lineHeight);
      const height = textRef.value.offsetHeight;
      isMultiline.value = height > lineHeight * 1.5; // 1.5줄 이상이면 2줄 이상으로 간주
    }
  });
}

onMounted(() => {
  checkMultiline();
});

watch(() => props.text, () => {
  checkMultiline();
});
</script>

<style scoped>
.info-notice {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 12px;
}

/* 파랑 (기본) */
.info-notice--blue {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  box-shadow: 0 2px 8px rgba(0, 140, 255, 0.06);
  border: 1px solid #b3d5f9;
}

.info-notice--blue i {
  color: #3b82f6;
}

.info-notice--blue span {
  color: #1e40af;
}

/* 연노랑 */
.info-notice--yellow {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  box-shadow: 0 2px 8px rgba(255, 193, 7, 0.06);
  border: 1px solid #ffe8b3;
}

.info-notice--yellow i {
  color: #f59e0b;
}

.info-notice--yellow span {
  color: #92400e;
}

/* 연빨강 */
.info-notice--red {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  box-shadow: 0 2px 8px rgba(255, 0, 0, 0.06);
  border: 1px solid #ffc4c4;
}

.info-notice--red i {
  color: #ef4444;
}

.info-notice--red span {
  color: #991b1b;
}

/* 초록색 */
.info-notice--green {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  box-shadow: 0 2px 8px rgba(0, 200, 83, 0.06);
  border: 1px solid #b8f0d0;
}

.info-notice--green i {
  color: #10b981;
}

.info-notice--green span {
  color: #065f46;
}

.info-notice i {
  font-size: 1.1rem;
  flex-shrink: 0;
}

.info-notice i.icon-multiline {
  margin-top: -3px;
}

.info-notice span {
  font-size: 0.85rem;
  line-height: 1.5;
  white-space: pre-line;
  align-self: center;
}

/* 텍스트 굵기 */
.info-notice span.font-weight-thin {
  font-weight: 300 !important;
}

.info-notice span.font-weight-normal {
  font-weight: 500 !important;
}

.info-notice span.font-weight-bold {
  font-weight: 900 !important;
}

/* 모바일 스타일 */
@media (max-width: 650px) {
  .info-notice {
    padding: 10px 12px;
  }

  .info-notice i {
    font-size: 1rem;
  }

  .info-notice span {
    font-size: 0.7rem;
  }

  .info-notice i.icon-multiline {
    margin-top: -4px;
  }
}

/* 다크모드 스타일 */
body[data-theme="dark"] .info-notice {
  background: linear-gradient(135deg, #2f3640 0%, #252a31 100%) !important;
  border: 1px solid var(--theme-border) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2) !important;
}

body[data-theme="dark"] .info-notice--blue i {
  color: #60a5fa !important;
}

body[data-theme="dark"] .info-notice--yellow i {
  color: #fbbf24 !important;
}

body[data-theme="dark"] .info-notice--red i {
  color: #f87171 !important;
}

body[data-theme="dark"] .info-notice--green i {
  color: #34d399 !important;
}

body[data-theme="dark"] .info-notice span {
  color: #d1d5db !important;
}
</style>

