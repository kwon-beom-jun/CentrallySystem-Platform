// src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'
import { BootstrapVue3 } from 'bootstrap-vue-3';
import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import { usePreviewModal } from './utils/preview-modal'; // Import the preview-modal composable
import i18n from './locales'; // i18n 다국어 설정


import { VTooltip } from 'floating-vue'
import 'floating-vue/dist/style.css' // CSS 파일 임포트

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-vue-3/dist/bootstrap-vue-3.css';

/* ==================== Styles ==================== */
// 📌 통합 스타일 진입점 (하나만 import)
import './assets/styles/index.css';

import { initInterceptors } from '@/api/apiConfig'; // axios 설정
import { useViewStateStore } from '@/store/viewState';

import Vue3Toastify from 'vue3-toastify'; // toast
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'; // toast

import 'quill/dist/quill.core.css';
import 'quill/dist/quill.snow.css';

// https://remixicon.com/
import 'remixicon/fonts/remixicon.css';

import clickAway from '@/utils/clickAway';
import { initScrollEffect, cleanupScrollEffect } from '@/utils/globalScrollEffect';
import { initGlobalImageErrorHandler } from '@/utils/globalImageErrorHandler';

// 공통 컴포넌트 전역 등록
import PageTitle from '@/components/common/title/PageTitle.vue';

// import './assets/styles/font.css'; // 공통 Font CSS 임포트
// import axios from 'axios';

//https://apexcharts.com/
import VueApexCharts from 'vue3-apexcharts'; // ApexCharts


// 모바일 스타일 적용
// import '@/styles/mobile.css'; 

/**
  async 대신 사용시 Chrome 89·Firefox 108·Safari 15 이상의 브라우저만 이용가능
    // vite.config.js
    import { defineConfig } from 'vite'
    import vue from '@vitejs/plugin-vue'

    export default defineConfig({
      plugins: [vue()],
      build: {
        target: 'esnext'       // 또는 'es2022'
      }
    })
 */
// 다양한 브라우저(사내 IE 모드, 구형 Edge 등)도 지원
(async () => {
  const app = createApp(App);

  // toast 전역 등록 (아래 간단한 예시)
  // toast.success("성공 메시지입니다!");
  // toast.error("에러 메시지입니다!");
  // toast.info("정보 메시지입니다!");
  // toast.warning("경고 메시지입니다!");
  app.use(Vue3Toastify, {
    autoClose: 2000,           // 1000ms 후 자동 닫힘
    position: "bottom-center", // 하단 중앙
    theme: "dark",             // 다크 테마 (검정색 바)
    limit: 3,                  // 동시 최대 3개 유지
    newestOnTop: true,         // 위에 새로 쌓이도록
    pauseOnHover: false,       // 마우스 올리면 자동 닫힘 타이머 일시정지
    draggable: false,          // 드래그하여 토스트 이동 가능 여부
    hideProgressBar: true,     // 타이머 바 숨김
    transition: "slide",       // 이벤트 발생 유지, CSS 0.01s로 체감 0
  });

  // Register the preview modal globally
  app.config.globalProperties.$previewModal = usePreviewModal();

  const pinia = createPinia();
  pinia.use(piniaPluginPersistedstate);
  app.use(pinia);

  /* 
    툴팁을 아래에 표시: v-tooltip.bottom="..."
    툴팁을 위쪽에 표시: v-tooltip.top="..."
    툴팁을 왼쪽에 표시: v-tooltip.left="..."
    툴팁을 오른쪽에 표시: v-tooltip.right="..."
  */
  // FloatingVue 컴포넌트 등록 충돌(Vuetify VTooltip) 회피: 디렉티브만 등록
  app.directive('tooltip', VTooltip)
  // 툴팁 지연 시간을 0ms 로 설정하여 즉시 표시되도록 오버라이드
  // app.use(FloatingVue, {
  //   delay: {
  //     show: 0,
  //     hide: 0,
  //   },
  // })

  app.use(router);
  app.use(vuetify);
  app.use(BootstrapVue3);
  app.use(i18n); // i18n 등록

  // 전역 컴포넌트 등록
  app.component('PageTitle', PageTitle);

  // 전역 스크롤 효과 적용: 라우터 네비게이션 후 자동으로 초기화
  // 모바일 상세화면 타이틀 스크롤 효과
  router.afterEach(() => {
    // 기존 스크롤 효과 정리
    cleanupScrollEffect();
    
    // DOM 업데이트 완료 후 새로운 스크롤 효과 적용
    setTimeout(() => {
      const titleElement = document.querySelector('.content-mobile-detail-title');
      if (titleElement) {
        initScrollEffect();
      }
    }, 200);
  });

  // ApexCharts
  app.use(VueApexCharts); 

  await initInterceptors();

  app.directive('click-away', clickAway);
  app.mount('#app');

  // 초기 페이지 로드 시 스크롤 효과 적용
  setTimeout(() => {
    const titleElement = document.querySelector('.content-mobile-detail-title');
    if (titleElement) {
      initScrollEffect();
    }
  }, 300);

  // 전역: 모바일/데스크탑 전환 감지하여 저장 상태 초기화
  try {
    const vs = useViewStateStore();
    vs.attachModeWatcher(650);
  } catch {}

  // 전역 이미지 에러 핸들러 초기화
  initGlobalImageErrorHandler();
})()