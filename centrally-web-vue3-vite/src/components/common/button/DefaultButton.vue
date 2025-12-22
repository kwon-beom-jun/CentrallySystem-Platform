<template>
  <div :class="alignmentClass" :style="marginStyle">
    <button
      :type="type"
      :class="['btn', buttonClass, customClass]"
      @click="handleClick"
      :style="buttonStyle"
      :disabled="props.disabled"
    >
      <slot />
    </button>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps({
  type: {
    type: String,
    default: 'button', // 기본값은 button
    validator: (value) => ['button', 'submit', 'reset'].includes(value),
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => [
      'xsmall', 'small', 'medium', 'large', 'full',
      'full-xsmall', 'full-small', 'full-medium', 'full-large'
    ].includes(value),
  },
  customClass: {
    type: String,
    default: '',
  },
  align: {
    type: String,
    default: 'center',
    validator: (value) => ['left', 'center', 'right'].includes(value),
  },
  color: {
    type: String,
    default: 'default', // 기본 색상
    validator: (value) => ['default', 'blue', 'red', 'green', 'yellow', 'gray'].includes(value),
  },
  // 마진 (왼쪽, 오른쪽, 위, 아래)
  marginLeft: {
    type: String,
    required: false,
    default: '0'
  },
  marginRight: {
    type: String,
    required: false,
    default: '0'
  },
  marginTop: {
    type: String,
    required: false,
    default: '0'
  },
  marginBottom: {
    type: String,
    required: false,
    default: '0'
  },
  customHeight: {
    type: String,
    default: '' // 값이 없으면 기본 스타일 유지
  },
  customWidth: {
    type: String,
    default: ''
  },
  customFontSize: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false,
  },
});

// margin 관련 computed 스타일 객체 생성
const marginStyle = computed(() => {
  return `
    margin-left: ${props.marginLeft} !important;
    margin-right: ${props.marginRight} !important;
    margin-top: ${props.marginTop} !important;
    margin-bottom: ${props.marginBottom} !important;
  `;
});

const emits = defineEmits(['click']);

const handleClick = (event) => {
  emits('click', event);
};

const buttonClass = computed(() => {
  let sizeClass = '';

  // 'full-'로 시작하는 조합형 사이즈 처리
  if (props.size.startsWith('full-')) {
    // 'full-' 뒤에 오는 사이즈를 파싱 (xsmall, small, medium, large)
    const normalSize = props.size.replace('full-', ''); // 예: full-small → small
    sizeClass = 'btn-full'; // 100% 너비

    // small, xsmall, large 등에 대응
    switch (normalSize) {
      case 'xsmall':
        sizeClass += ' btn-xs';
        break;
      case 'small':
        sizeClass += ' btn-sm';
        break;
      case 'large':
        sizeClass += ' btn-lg';
        break;
      case 'medium':
      default:
        // medium은 별도 클래스 없음
        break;
    }
  }
  // 기존 사이즈 처리
  else {
    switch (props.size) {
      case 'full':
        sizeClass = 'btn-full';
        break;
      case 'xsmall':
        sizeClass = 'btn-xs';
        break;
      case 'small':
        sizeClass = 'btn-sm';
        break;
      case 'large':
        sizeClass = 'btn-lg';
        break;
      // medium / default
      default:
        sizeClass = '';
        break;
    }
  }

  const colorClass = `btn-${props.color}`;
  return `${sizeClass} ${colorClass}`;
});

const buttonStyle = computed(() => {
  const style = {};
  if (props.customHeight)   style.height    = props.customHeight;   // e.g. '32px'
  if (props.customWidth)    style.width     = props.customWidth;    // e.g. '120px'
  if (props.customFontSize) style.fontSize  = props.customFontSize; // e.g. '0.9rem'
  return style;
});

const alignmentClass = computed(() => {
  switch (props.align) {
    case 'left':
      return 'text-start';
    case 'right':
      return 'text-end';
    default:
      return 'text-center';
  }
});
</script>

<style scoped>
.btn {
  --bs-btn-color: var(--theme-text-primary);
  --bs-btn-bg: var(--theme-bg-main);
  --bs-btn-border-color: var(--theme-border);
  --bs-btn-hover-color: var(--theme-text-primary);
  --bs-btn-hover-bg: var(--theme-bg-hover);
  /* --bs-btn-hover-border-color: var(--theme-bg-main); */
  --bs-btn-focus-shadow-rgb: 60, 153, 110;
  --bs-btn-active-color: var(--theme-text-inverse);
  --bs-btn-active-bg: var(--theme-bg-main);
  --bs-btn-active-border-color: var(--theme-border);
  --bs-btn-active-shadow: inset 0 3px 5px rgba(0, 0, 0, 0.125);
  --bs-btn-disabled-color: var(--theme-text-inverse);
  --bs-btn-disabled-bg: var(--theme-bg-main);
  --bs-btn-disabled-border-color: var(--theme-bg-main);
  /* === 기본 사이즈 === */
  height:38px;
  font-size:.8rem;
  font-weight: 500;
  padding-left: 1rem;
  padding-right: 1rem;
  border-radius:3px;

  /* === 세로 가운데 정렬 핵심 === */
  display:inline-flex;          /* flex 컨테이너로 */
  justify-content:center;       /* 가로 가운데 */
  align-items:center;           /* 세로 가운데 */
  vertical-align:middle;        /* 표·아이콘과 함께 쓸 때 */
}

.no-pad {
  padding-left: 0 !important;
  padding-right: 0 !important;
  min-width: 0 !important;     /* 글자 길이만큼 튀어나오는 것 방지 */
  box-sizing: border-box;      /* width안에 padding 포함시키려면(optional) */
}

/* 색상별 커스텀 */
/* .btn-default {
  --bs-btn-bg: #375a7f;
  --bs-btn-border-color: #375a7f;
  --bs-btn-hover-bg: #5a7694;
  --bs-btn-hover-border-color: #5a7694;
} */

.btn-blue {
  color: #fff;
  --bs-btn-hover-color: #fff;
  --bs-btn-hover-bg: #ffffff;
  --bs-btn-bg: var(--theme-btn-primary-bg);
  --bs-btn-border-color: var(--theme-btn-primary-bg);
  --bs-btn-hover-bg: var(--theme-btn-primary-hover);
  --bs-btn-hover-border-color: var(--theme-btn-primary-hover);
}

.btn-red {
  color: #fff;
  --bs-btn-hover-color: #fff;
  --bs-btn-hover-bg: #ffffff;
  --bs-btn-bg: var(--theme-btn-red-bg);
  --bs-btn-border-color: var(--theme-btn-red-bg);
  --bs-btn-hover-bg: #bd2130;
  --bs-btn-hover-border-color: #bd2130;
}

.btn-green {
  color: #fff;
  --bs-btn-hover-color: #fff;
  --bs-btn-hover-bg: #ffffff;
  --bs-btn-bg: var(--theme-btn-green-bg);
  --bs-btn-border-color: var(--theme-btn-green-bg);
  --bs-btn-hover-bg: #218838;
  --bs-btn-hover-border-color: #218838;
}

.btn-yellow {
  color: #fff;
  --bs-btn-hover-color: #fff;
  --bs-btn-hover-bg: #ffffff;
  --bs-btn-bg: #ffc107;
  --bs-btn-border-color: #ffc107;
  --bs-btn-hover-bg: #e0a800;
  --bs-btn-hover-border-color: #e0a800;
}

.btn-gray {
  color: #fff;
  --bs-btn-hover-color: #fff;
  --bs-btn-hover-bg: #ffffff;
  --bs-btn-bg: var(--theme-btn-gray-bg);
  --bs-btn-border-color: var(--theme-btn-gray-bg);
  --bs-btn-hover-bg: #272727;
  --bs-btn-hover-border-color: var(--theme-btn-gray-bg);
}

/* full 버튼: 너비 100% */
.btn, .btn-xs, .btn-sm, .btn-lg, .btn-full {
  /* padding: 0.3rem 0.6rem; */
  font-size: 0.8rem;
}

.btn:disabled, .btn[disabled] {
  /* 1. 클릭이 안 된다는 것을 알려주는 커서 모양으로 변경 */
  cursor: not-allowed;
  /* 2. (핵심) 버튼이 흐릿해지지 않도록 투명도를 1로 강제합니다. */
  /* 단, 이 경우 활성화 버튼과 시각적 구분이 어려울 수 있으니,
     아래 배경색/글자색을 함께 조절하는 것을 권장합니다. */
  opacity: 1;
}

/* 4. (선택사항) 비활성화 시 색상 커스텀 */
/* 모든 색상 버튼에 일관된 비활성화 스타일을 적용합니다. */
.btn:disabled, .btn[disabled] {
  background-color: var(--theme-btn-disabled-bg) !important; /* 비활성화 배경색 */
  border-color: var(--theme-btn-disabled-bg) !important;     /* 비활성화 테두리색 */
  color: var(--theme-btn-disabled-text) !important;          /* 비활성화 글자색 */
  box-shadow: none !important;                                /* 그림자 제거 */
}

.text-start {
  text-align: left !important;
}

.text-end {
  text-align: right !important;
}

.text-center {
  text-align: center !important;
}

@media (max-width: 650px) {
  .btn {
    height: 33px !important;
    font-size: 0.7rem !important;
    padding-left: 0.8rem;
    padding-right: 0.8rem;
  }
  .btn ,.btn-xs ,.btn-sm ,.btn-lg {
    /* height: 33px !important; */
    /* padding: 0.4rem 0.4rem !important; */
    /* height: auto !important; */
    line-height: 1.5;
  }
}

/* @media (max-width: 500px) {
  .btn, .btn-xs, .btn-sm, .btn-lg {
    padding-left: 0.8rem;
    padding-right: 0.8rem;
    line-height: 1.5;
  }
} */

</style>
