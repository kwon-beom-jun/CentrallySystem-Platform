<template>
  <div class="page-title-wrapper" :class="{ 'has-back-button': showBackButton }" ref="wrapperRef">
    <h2 class="content-title">
      <button 
        v-if="showBackButton"
        class="page-title-back-btn" 
        @click="handleBack" 
        type="button"
        :aria-label="backButtonLabel"
      ></button>
      <span class="page-title-text">{{ title }}</span>
    </h2>
    <div 
      v-if="subtitle || showActionButton" 
      class="content-sub-title-wrapper"
      :style="marginBottomStyle"
    >
      <p v-if="subtitle" class="content-sub-title">
        {{ subtitle }}
      </p>
      <div v-if="showActionButton" class="content-sub-title-action">
        <button
          @click="handleActionButtonClick"
          class="action-btn"
          :class="{ 'active': actionButtonActive }"
        >
          <slot name="actionButton">버튼</slot>
        </button>
      </div>
    </div>
  </div>
  <div class="page-title-padding" :style="paddingStyle"></div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, useAttrs } from 'vue'
import { useRouter } from 'vue-router'

/**
 * 페이지 타이틀 컴포넌트
 * - 모바일: 스크롤 시 서브타이틀 사라지고 타이틀만 상단에 고정, 흰색 배경 + 그림자
 * - 데스크톱: 일반 타이틀처럼 표시
 * - 선택적 뒤로가기 버튼 포함
 */

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  scrollThreshold: {
    type: Number,
    default: 50
  },
  showBackButton: {
    type: Boolean,
    default: false
  },
  backButtonLabel: {
    type: String,
    default: '뒤로가기'
  },
  backTo: {
    type: String,
    default: null
  },
  // 서브타이틀 옆 액션 버튼
  showActionButton: {
    type: Boolean,
    default: false
  },
  actionButtonActive: {
    type: Boolean,
    default: false
  },
  // 데스크톱 하단 마진 조절 (px 단위 숫자 또는 문자열)
  desktopMarginBottom: {
    type: [Number, String],
    default: null
  },
  // 모바일 하단 마진 조절 (px 단위 숫자 또는 문자열)
  mobileMarginBottom: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['back', 'action-button-click'])
const router = useRouter()
const attrs = useAttrs()

/**
 * 뒤로가기 버튼 클릭 핸들러
 */
const handleBack = () => {
  // backTo prop이 명시된 경우
  if (props.backTo) {
    router.push(props.backTo)
    return
  }
  
  // @back 이벤트 리스너가 있으면 emit만 하고 부모가 처리
  if (attrs.onBack) {
    emit('back')
    return
  }
  
  // 둘 다 없으면 기본 동작으로 뒤로가기
  router.back()
}

/**
 * 액션 버튼 클릭 핸들러
 */
const handleActionButtonClick = () => {
  emit('action-button-click')
}

/**
 * 하단 마진 스타일 계산 (서브타이틀 래퍼용)
 * CSS 변수로 데스크톱/모바일 마진 전달
 */
const marginBottomStyle = computed(() => {
  const style = {}
  
  if (props.desktopMarginBottom !== null) {
    const value = typeof props.desktopMarginBottom === 'number' 
      ? `${props.desktopMarginBottom}px` 
      : props.desktopMarginBottom
    style['--desktop-margin-bottom'] = value
  }
  
  if (props.mobileMarginBottom !== null) {
    const value = typeof props.mobileMarginBottom === 'number' 
      ? `${props.mobileMarginBottom}px` 
      : props.mobileMarginBottom
    style['--mobile-margin-bottom'] = value
  }
  
  return style
})

/**
 * 패딩 스타일 계산 (page-title-padding용)
 * subtitle이 없을 때 marginBottom 적용
 */
const paddingStyle = computed(() => {
  if (!props.subtitle && !props.showActionButton) {
    const style = {}
    
    if (props.desktopMarginBottom !== null) {
      const value = typeof props.desktopMarginBottom === 'number' 
        ? `${props.desktopMarginBottom}px` 
        : props.desktopMarginBottom
      style['--desktop-padding-bottom'] = value
    }
    
    if (props.mobileMarginBottom !== null) {
      const value = typeof props.mobileMarginBottom === 'number' 
        ? `${props.mobileMarginBottom}px` 
        : props.mobileMarginBottom
      style['--mobile-padding-bottom'] = value
    }
    
    return style
  }
  return {}
})

const wrapperRef = ref(null)
let scrollListener = null
const isScrolled = ref(false)

/**
 * 스크롤 이벤트 핸들러 - 히스테리시스 적용
 * 내릴 때: 25px에서 사라짐
 * 올릴 때: 2px에서 나타남
 * 이렇게 차이를 두면 깜빡거림 없음
 */
const handleScroll = () => {
  if (!wrapperRef.value) return
  
  const scrollY = window.scrollY
  
  if (!isScrolled.value && scrollY > 25) {
    // 내릴 때: 25px 넘으면 사라짐
    wrapperRef.value.classList.add('scrolled')
    isScrolled.value = true
  } else if (isScrolled.value && scrollY < 2) {
    // 올릴 때: 2px 아래로 내려오면 나타남
    wrapperRef.value.classList.remove('scrolled')
    isScrolled.value = false
  }
}

onMounted(() => {
  // 강제로 맨 위로 스크롤 (다른 로직보다 먼저 실행)
  window.scrollTo(0, 0)
  document.documentElement.scrollTop = 0
  document.body.scrollTop = 0
  
  // throttle 적용된 스크롤 리스너
  let ticking = false
  scrollListener = () => {
    if (!ticking) {
      window.requestAnimationFrame(() => {
        handleScroll()
        ticking = false
      })
      ticking = true
    }
  }
  
  window.addEventListener('scroll', scrollListener, { passive: true })
  
  // 초기 상태 설정
  handleScroll()
})

onBeforeUnmount(() => {
  if (scrollListener) {
    window.removeEventListener('scroll', scrollListener)
  }
})
</script>

<style scoped>
.content-title {
  margin-bottom: 10px;
  font-size: 1.7rem;
  /* 데스크톱에서도 뒤로가기 버튼을 위한 flexbox */
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
/* 서브타이틀 래퍼 */
.content-sub-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: var(--desktop-margin-bottom, 50px);
}

.content-sub-title {
  margin-bottom: 0;
  padding-left: 10px;
  min-height: 28px;
  color: #979797;
  flex: 0 0 auto;
}

.content-sub-title-action {
  flex: 0 0 auto;
}

/* 액션 버튼 공통 스타일 */
.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 5px 12px;
  min-height: 28px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  color: #666;
  font-size: 0.75rem;
  font-weight: 500;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  box-sizing: border-box;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.action-btn:hover {
  background: #f8f8f8;
  border-color: #d0d0d0;
  color: #333;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.action-btn:active {
  background: #f0f0f0;
  transform: scale(0.98);
}

.action-btn.active {
  background: linear-gradient(135deg, #f0f7ff 0%, #e3f2fd 100%);
  border: 1px solid #bbdefb;
  color: #1565c0;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.15);
}

.action-btn.active:hover {
  background: linear-gradient(135deg, #e3f2fd 0%, #d6edff 100%);
  border-color: #90caf9;
  color: #0d47a1;
  box-shadow: 0 3px 10px rgba(33, 150, 243, 0.25);
}

.action-btn svg {
  flex-shrink: 0;
  display: block;
}

/* 데스크톱에서 뒤로가기 버튼이 있을 때 subtitle 왼쪽 패딩 조정 */
.page-title-wrapper.has-back-button .content-sub-title {
  padding-left: 45px;
}
.page-title-wrapper {
  position: relative;
}

/* 타이틀 텍스트 */
.page-title-text {
  flex: 0 1 auto;
  display: flex;
  align-items: center;
}

/* 데스크톱 뒤로가기 버튼 스타일 */
.page-title-back-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  margin-right: 25px;
  background-color: transparent;
  border: none;
  color: #666;
  font-size: 1.8rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.page-title-back-btn:hover {
  color: #333;
}

.page-title-back-btn:active {
  color: #000;
  transform: scale(0.95);
}

/* 뒤로가기 < 기호 */
.page-title-back-btn::before {
  content: '<';
  display: inline-block;
  font-weight: 800;
  transform: scale(0.7, 1.2);
}

.page-title-back-btn::after {
  content: none;
}

/* page-title-padding - subtitle 없을 때 사용 */
.page-title-padding {
  padding-bottom: var(--desktop-padding-bottom, 0);
}

/* 모바일 전용 스타일 */
@media (max-width: 650px) {
  .page-title-padding {
    padding-bottom: var(--mobile-padding-bottom, 30px) !important;
  }
  .page-title-wrapper {
    position: sticky;
    top: 65px;
    z-index: 100;
    background-color: transparent;
    border-bottom: none;
    box-shadow: none;
    /* content의 좌우 패딩 상쇄 */
    margin-left: -20px;
    margin-right: -20px;
    margin-top: -20px;
    padding: 12px 20px 12px 20px;
    /* 부드러운 전환 효과 없음 */
    transition: none;
  }
  
  /* wrapper 하단 보더 - 좌우 20px 여백 */
  .page-title-wrapper::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 20px;
    right: 20px;
    height: 1px;
    background-color: rgba(0, 0, 0, 0.06);
  }

  /* content-title 기본 스타일 */
  .page-title-wrapper .content-title {
    font-weight: 800;
    font-size: 1rem !important;
    margin-bottom: 0px !important;
    margin-top: 0px !important;
    text-align: left !important;
    color: #1a1a1a;
    padding: 2px 0 2px 0;
    letter-spacing: -0.02em;
    line-height: 1.3;
    /* flexbox로 제목과 버튼 배치 */
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  /* 타이틀 텍스트 스타일 */
  .page-title-text {
    flex: 1;
    text-align: left;
    order: 1;
    display: flex;
    align-items: center;
  }

  /* 모바일 뒤로가기 버튼 - 오른쪽 정렬 */
  .page-title-back-btn {
    width: 25px;
    height: 25px;
    padding: 0;
    margin-right: 0;
    margin-left: 10px;
    border: none;
    background-color: transparent;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    transition: background-color 0.2s ease;
    flex-shrink: 0;
    order: 2;
  }

  .page-title-back-btn:hover {
    background-color: rgba(0, 0, 0, 0.05);
  }

  .page-title-back-btn:active {
    background-color: rgba(0, 0, 0, 0.1);
  }

  /* 모바일 화살표 아이콘 (CSS로 구현) */
  .page-title-back-btn::before {
    content: '';
    width: 10px;
    height: 10px;
    border-left: 2px solid #555;
    border-bottom: 2px solid #555;
    transform: rotate(45deg);
    display: block;
  }

  .page-title-back-btn::after {
    content: none;
  }

  /* 스크롤 시 버튼 색상 변경 */
  .page-title-wrapper.scrolled .page-title-back-btn::before {
    border-left-color: #333;
    border-bottom-color: #333;
  }

  /* 모바일 서브타이틀 래퍼 */
  .content-sub-title {
    min-height: 0px;
  }
  .page-title-wrapper .content-sub-title-wrapper {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: var(--mobile-margin-bottom, 0px);
    border-bottom: none;
    padding-bottom: 0;
    padding-top: 5px;
  }

  /* content-sub-title 기본 스타일 */
  .page-title-wrapper .content-sub-title {
    font-size: 0.75rem !important;
    margin-bottom: 0 !important;
    text-align: left !important;
    border-bottom: none;
    padding-bottom: 0;
    color: #666;
    padding-left: 0 !important;
    font-weight: 500;
    line-height: 1.4;
    opacity: 1;
    flex: 1;
  }

  .page-title-wrapper .content-sub-title-action {
    flex: 0 0 auto;
    position: absolute;
    right: 20px;
  }

  /* 모바일 액션 버튼 */
  .page-title-wrapper .action-btn {
    padding: 4px 10px;
    font-size: 0.65rem;
    min-height: 26px;
    line-height: 1;
  }
  
  .page-title-wrapper .action-btn svg {
    width: 12px;
    height: 12px;
    display: block;
  }

  /* 모바일에서는 뒤로가기 버튼이 오른쪽이므로 subtitle 패딩 초기화 */
  .page-title-wrapper.has-back-button .content-sub-title {
    padding-left: 0 !important;
  }

  .page-title-wrapper .content-sub-title :deep(.default-form-row) {
    justify-content: flex-start !important;
  }

  /* 스크롤 시: 서브타이틀 래퍼 제거 */
  .page-title-wrapper.scrolled .content-sub-title-wrapper {
    display: none !important;
  }

  /* 스크롤 시: 배경 활성화 */
  .page-title-wrapper.scrolled {
    background-color: rgba(255, 255, 255, 0.95) !important;
    backdrop-filter: blur(10px);
    border-bottom: none !important;
    box-shadow:
      0 -2px 8px rgba(0, 0, 0, 0.02),
      0 4px 12px rgba(0, 0, 0, 0.06) !important;
    padding: 12px 20px 12px 20px;
  }
  
  /* 스크롤 시: 보더 색상 변경 */
  .page-title-wrapper.scrolled::after {
    background-color: rgba(0, 0, 0, 0.05);
  }
}
</style>

