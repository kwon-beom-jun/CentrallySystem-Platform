<template>
  <h2 ref="titleRef" class="content-mobile-detail-title">
    <span class="content-mobile-detail-title-text">{{ title }}</span>
    <button 
      v-if="showBackButton"
      class="content-mobile-detail-back-btn" 
      @click="handleBack" 
      type="button"
      :aria-label="backButtonLabel"
    ></button>
  </h2>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

/**
 * 모바일 상세 페이지 타이틀 컴포넌트
 * - 스크롤 시 배경색 자동 변경
 * - 선택적 뒤로가기 버튼 포함
 */

const props = defineProps({
  /**
   * 표시할 타이틀 텍스트
   */
  title: {
    type: String,
    required: true
  },
  /**
   * 뒤로가기 버튼 표시 여부
   */
  showBackButton: {
    type: Boolean,
    default: true
  },
  /**
   * 뒤로가기 버튼 접근성 라벨
   */
  backButtonLabel: {
    type: String,
    default: '뒤로가기'
  },
  /**
   * 커스텀 뒤로가기 경로 (없으면 router.back() 사용)
   */
  backTo: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['back'])
const router = useRouter()
const titleRef = ref(null)

/**
 * 뒤로가기 버튼 클릭 핸들러
 */
const handleBack = () => {
  emit('back')
  
  if (props.backTo) {
    router.push(props.backTo)
  } else {
    router.back()
  }
}

/**
 * 스크롤 이벤트 핸들러
 */
const handleScroll = () => {
  if (!titleRef.value) return
  
  // 스크롤 위치가 10px 이상이면 scrolled 클래스 추가
  if (window.scrollY > 10) {
    titleRef.value.classList.add('scrolled')
  } else {
    titleRef.value.classList.remove('scrolled')
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  // 초기 상태 체크
  handleScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* 모바일 전용 스타일 */
@media (max-width: 650px) {
  .content-mobile-detail-title {
    position: sticky;
    top: 64px; /* 헤더 높이만큼 위에서 고정 */
    z-index: 100;
    font-weight: 800;
    font-size: 1rem !important;
    margin-top: 5px !important;
    /* flexbox로 제목과 버튼 배치 */
    display: flex;
    align-items: center;
    justify-content: space-between;
    /* 초기 상태: 투명 배경 */
    background-color: transparent;
    border-bottom: 1px solid transparent;
    box-shadow: none;
    /* content의 좌우 패딩 20px을 상쇄하여 전체 너비로 배경 표시 */
    margin-left: -20px;
    margin-right: -20px;
    margin-top: -20px !important;
    padding-left: 20px;
    padding-right: 20px;
    padding-top: 20px;
    padding-bottom: 15px;
    /* 부드러운 전환 효과 */
    transition: all 0.0s cubic-bezier(0.4, 0, 0.2, 1);
    line-height: 1.3 !important;
  }

  /* 타이틀 텍스트 스타일 */
  .content-mobile-detail-title-text {
    flex: 1;
    text-align: left;
  }

  /* 모바일 타이틀 뒤로가기 버튼 */
  .content-mobile-detail-back-btn {
    width: 25px;
    height: 25px;
    border: none;
    background-color: transparent;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    transition: background-color 0.2s ease;
    flex-shrink: 0;
    margin-left: 10px;
  }

  .content-mobile-detail-back-btn:hover {
    background-color: rgba(0, 0, 0, 0.05);
  }

  .content-mobile-detail-back-btn:active {
    background-color: rgba(0, 0, 0, 0.1);
  }

  /* 뒤로가기 화살표 아이콘 (CSS로 구현) */
  .content-mobile-detail-back-btn::before {
    content: '';
    width: 10px;
    height: 10px;
    border-left: 2px solid #555;
    border-bottom: 2px solid #555;
    transform: rotate(45deg);
    display: block;
  }

  /* 스크롤 시 버튼 색상 변경 */
  .content-mobile-detail-title.scrolled .content-mobile-detail-back-btn::before {
    border-left-color: #333;
    border-bottom-color: #333;
  }

  /* 스크롤 시 활성화되는 스타일 */
  .content-mobile-detail-title.scrolled {
    background-color: rgba(255, 255, 255, 0.95) !important;
    backdrop-filter: blur(10px); /* iOS 스타일 블러 효과 */
    border-bottom: 1px solid rgba(0,0,0,0.05) !important;
    box-shadow:
      0 -2px 8px rgba(0,0,0,0.02),   /* 위로 살짝 퍼지는 그림자 */
      0 4px 12px rgba(0,0,0,0.06) !important;   /* 아래로 부드럽게 확산 */
  }

  /* 다크모드: 스크롤 시 배경 */
  body[data-theme="dark"] .content-mobile-detail-title.scrolled {
    background-color: rgba(36, 36, 36, 0.95) !important;
    backdrop-filter: blur(10px);
    border-bottom: 1px solid var(--theme-border) !important;
    box-shadow:
      0 -2px 8px rgba(0, 0, 0, 0.3),
      0 4px 12px rgba(0, 0, 0, 0.5) !important;
  }

  /* 다크모드: 뒤로가기 버튼 */
  body[data-theme="dark"] .content-mobile-detail-back-btn::before {
    border-left-color: #d1d5db;
    border-bottom-color: #d1d5db;
  }

  body[data-theme="dark"] .content-mobile-detail-title.scrolled .content-mobile-detail-back-btn::before {
    border-left-color: #ffffff;
    border-bottom-color: #ffffff;
  }

  body[data-theme="dark"] .content-mobile-detail-back-btn:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }

  body[data-theme="dark"] .content-mobile-detail-back-btn:active {
    background-color: rgba(255, 255, 255, 0.15);
  }
}
</style>

