<template>
  <label :for="forId" :class="['default-label', 'mr-2', customClass]" :style="labelStyle">
    <slot>{{ text }}</slot>
    <span v-if="required" class="required-asterisk">*</span>
    <span 
      v-if="tooltipText" 
      class="info-icon-wrapper"
      v-tooltip="tooltipText"
    >
      <i class="ri-information-line"></i>
    </span>
  </label>
</template>

<script setup>
import { computed, defineProps, ref, onMounted, onBeforeUnmount } from 'vue';

const props = defineProps({
  text: {
    type: String,
    default: ''
  },
  forId: {
    type: String,
    default: ''
  },
  required: {
    type: Boolean,
    default: false
  },
  customClass: {
    type: String,
    default: ''
  },
  // 'small', 'medium', 'large' 옵션
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  color: {
    type: String,
    default: '#333'
  },
  /**
   * alignment: 라벨 텍스트 정렬
   *  - 'left', 'center', 'right' 중 하나
   *  - 기본값: 'center'
   */
  alignment: {
    type: String,
    default: 'center',
    validator: (value) => ['left', 'center', 'right'].includes(value)
  },
  marginLeft: {
    type: String,
    default: '0'
  },
  marginRight: {
    type: String,
    default: '0'
  },
  marginTop: {
    type: String,
    default: '0'
  },
  marginBottom: {
    type: String,
    default: '0'
  },
  tooltipText: {
    type: String,
    default: ''
  }
});

// 반응형 윈도우 너비 감지
const windowWidth = ref(window.innerWidth);
const updateWidth = () => {
  windowWidth.value = window.innerWidth;
};

onMounted(() => {
  window.addEventListener('resize', updateWidth);
});
onBeforeUnmount(() => {
  window.removeEventListener('resize', updateWidth);
});

// 기본 폰트 크기를 숫자(rem 단위)로 결정 (props.size에 따라)
const baseFontSize = computed(() => {
  switch(props.size) {
    case 'small':
      return 0.8;
    case 'large':
      return 1.2;
    case 'medium':
    default:
      return 1.0;
  }
});

// 화면 너비가 650px 이하이면 기본 크기의 90%로 줄임
const labelStyle = computed(() => {
  const factor = windowWidth.value <= 650 ? 0.9 : 1;
  return {
    fontSize: `${baseFontSize.value * factor}rem`,
    color: props.color,
    textAlign: props.alignment,
    marginLeft: props.marginLeft,
    marginRight: props.marginRight,
    marginTop: props.marginTop,
    marginBottom: props.marginBottom
  };
});
</script>

<style scoped>
.default-label {
  display: inline-block;
}

.required-asterisk {
  color: red !important;
  margin-left: 0.25rem;
}

.info-icon-wrapper {
  display: inline-flex;
  align-items: center;
  margin-left: 0.25rem;
  cursor: help;
  vertical-align: middle;
}

.info-icon-wrapper i {
  font-size: 1.3em;
  color: #6b7280;
  display: inline-block;
  line-height: 1;
  font-weight: 900;
}

/* 모바일 스타일 */
@media (max-width: 650px) {
  .info-icon-wrapper {
    margin-left: 0rem;
  }

  .info-icon-wrapper i {
    font-size: 1.3em;
  }
}
</style>
