<template>
  <!--
    Pinia에 useLoadingStore를 만들어 요청 카운트 기반으로 isLoading 여부를 관리합
    Axios 인터셉터에 startLoading() / stopLoading()를 심어 모든 API 호출을 로딩 관리
    Router 가드(beforeEach / afterEach)에도 동일하게 로딩 관리를 추가
    메인 레이아웃 (App.vue 등) 에서 vue3-loading-overlay를 읽어 isLoading이 true면 스피너를 표시

    :active="isLoading"
      true일 때 스피너 활성화, false면 숨김
    opacity, backgroundColor
      로딩 오버레이 배경 투명도와 배경색
    loader, color
      스피너 타입(spinner, dots, 등)과 색상
    zIndex
      오버레이가 다른 요소 위에 표시되도록 설정
  -->
  <!-- 로딩 오버레이 컴포넌트 -->
   <!--
    불투명색 (속도 개선을 위해 불투명 제거)
      :opacity="0.8"
      :backgroundColor="loadingBackgroundColor"
   -->
  <LoadingOverlay
    :active="isLoading"
    :opacity="0"
    backgroundColor="transparent"
    :loader="'spinner'"
    :color="'#375a7f'"
    :zIndex="9999"
  />

  <div class="app-layout">
    <!-- v-if 조건을 사용하여 로그인 페이지가 아닐 때만 렌더링 -->
    <!-- 로그인 정보(userId) 가 준비된 경우에만 렌더링 -->
    <Header v-if="showLayout && isAuthed" />

    <Navigation v-if="showLayout && isAuthed" />

    <!-- 사이드바들 (로그인 상태이고 레이아웃이 표시되는 페이지에서만) -->
    <FirstSidebar v-if="showLayout && isAuthed" />
    <SecondSidebar v-if="showLayout && isAuthed" />

    

    <!-- 중앙 콘텐츠 영역 -->
    <div class="main-content-area" :class="{ 'with-sidebars': showLayout && isAuthed }">
      <router-view />
    </div>
    
    <Footer v-if="showLayout" />
  </div>
</template>

<script setup>
import { computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import Header from './components/HeaderLayout.vue';
import Footer from './components/FooterLayout.vue';
import Navigation from './components/NavigationLayout.vue';
import FirstSidebar from '@/components/sidebar/FirstSidebar.vue';
import SecondSidebar from '@/components/sidebar/SecondSidebar.vue';

import LoadingOverlay from 'vue3-loading-overlay';
import { useLoadingStore } from '@/store/loading';
import 'vue3-loading-overlay/dist/vue3-loading-overlay.css';

import { useAuthStore }   from '@/store/auth';
import { useHrmStore } from '@/store/hrm';
import { getInfoMobileStyleTheme } from '@/constants';

// 로딩 스토어 사용
const loadingStore = useLoadingStore();
const isLoading = computed(() => loadingStore.isLoading);

/**
 * 로딩 오버레이 배경색 (현재 테마에 따라 동적 설정)
 */
// const loadingBackgroundColor = computed(() => {
//   return currentTheme.value === 'dark' ? '#1a1a1a' : '#ffffff';
// });

const route = useRoute();

// authStore에서 userId가 세팅되었는지 확인
const authStore = useAuthStore();
const hrmStore = useHrmStore();
const isAuthed  = computed(() => authStore.userId !== null);

// 현재 경로의 메타 정보를 확인하여 로그인 페이지 여부를 판별
// ✅ 중괄호 + return
const showLayout = computed(() => {
  // console.log('route.name →', route.name);        // 디버그
  const hidden = ['userlogin', 'userjoin'];        // ❶
  return !!route.name && !hidden.includes(String(route.name).toLowerCase()); // ❷
});

/**
 * 현재 테마 계산 (hrmStore에서 INFO_MOBILE 스타일 가져오기)
 */
const currentTheme = computed(() => {
  const infoMobileStyle = hrmStore.getMyProfile?.infoMobileStyle || 'light';
  return getInfoMobileStyleTheme(infoMobileStyle);
});

/**
 * html과 body에 data-theme 속성 동적 적용
 */
function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  document.body.setAttribute('data-theme', theme);
}

/**
 * 로그인, 회원가입, 가이드(비로그인) 페이지는 무조건 밝은 테마로 고정
 */
const alwaysLightThemeRoutes = ['userlogin', 'userjoin'];

// 라우트 변경 감지 - 특정 페이지에서는 강제로 light 테마 적용
watch(() => route.name, (newRouteName) => {
  const routeName = String(newRouteName || '').toLowerCase();
  
  // 로그인, 회원가입 페이지는 무조건 light
  if (alwaysLightThemeRoutes.includes(routeName)) {
    applyTheme('light');
    return;
  }
  
  // 가이드 페이지이면서 로그인하지 않은 경우 light
  if (routeName === 'guidepage' && !authStore.isLoggedIn) {
    applyTheme('light');
    return;
  }
  
  // 그 외의 경우 사용자 설정 테마 적용
  applyTheme(currentTheme.value);
}, { immediate: true });

// 테마 변경 감지 (로그인 후 프로필 로드 시)
watch(currentTheme, (newTheme) => {
  const routeName = String(route.name || '').toLowerCase();
  
  // 특정 페이지가 아닌 경우에만 테마 변경 적용
  if (!alwaysLightThemeRoutes.includes(routeName)) {
    if (routeName !== 'guidepage' || authStore.isLoggedIn) {
      applyTheme(newTheme);
    }
  }
});
</script>

<style scoped>
body {
  font-family: 'Inter', sans-serif;
  background-color: #F5F7FA;
  padding-top: 56px;
}

/* ===== 앱 레이아웃 ===== */
.app-layout {
  position: relative;
  min-height: 100vh;
}

/* 중앙 콘텐츠 영역 - 기존 .content 영역 그대로 유지 */
.main-content-area {
  position: relative;
}

#dataTable tbody tr:hover td {
  background-color: #e7f1ff !important;
}

/* 날짜 선택 셀렉트 박스 크기 조정 */
#yearSelect {
  font-size: 0.8rem; /* 폰트 크기 줄이기 */
  padding: 0.25rem 0.5rem; /* 패딩 줄이기 */
  width: auto; /* 너비 자동 조정 */
}

/* 반응형 스타일 */
@media (min-width: 1920px) {
  .navbar-text {
    font-size: 1.5rem; /* 더 큰 텍스트 크기 */
  }
}
</style>
