// src/router/index.js
// import { createRouter, createWebHistory } from 'vue-router';
import { createRouter, createWebHashHistory } from 'vue-router';
import { useAuthStore } from '@/store/auth';  // Pinia 스토어
import { useLoadingStore } from '@/store/loading'; // Pinia 스토어
import authRoutes from './auth';
import infoRoutes from './info';
import userRoutes from './user';
import managementRoutes from './management';
import receiptRoutes from './receipt';
import systemRoutes from './system';
// Lazy-loaded views

const MainPage   = () => import('@/views/MainPage.vue');
const GuidePage  = () => import('@/views/GuidePage.vue');
const UserLogin  = () => import('@/views/UserLogin.vue');
const UserJoin   = () => import('@/views/UserJoin.vue');
import { toast } from 'vue3-toastify';
import { useSidebarStore } from '@/store/sidebar';
import { useToastStore } from '@/store/toast';
import { PATH_NAMES, ROUTE_NAMES, MENUS } from '@/config/routeConfig';
import { checkAuthForRouterGuard } from '@/utils/session';

// 피처 플래그
const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';


/**
 * 로그인 확인 필요 없을 시 : meta에 requiresAuth=false 추가
 * menu는 백엔드에서 이력관리에서 사용
 */
const baseRoutes = [
  {
    path: '/main',
    name: 'MainPage',
    component: MainPage,
    meta: { 
      menu: MENUS.COMMON.MAIN
    },
  },
  {
    path: '/',
    name: 'UserLogin',
    component: UserLogin,
    meta: { 
      isLoginPage: true,
      requiresAuth: false,
      menu: MENUS.AUTH.LOGIN
    },
    beforeEnter: async (to, from, next) => {
      const isAuthenticated = await checkAuthForRouterGuard();
      if (isAuthenticated) {
        next('/main'); // JWT가 유효하면 메인페이지로
      } else {
        next(); // JWT가 없거나 무효하면 로그인 페이지로
      }
    },
  },
  {
    path: '/guide',
    name: 'GuidePage',
    component: GuidePage,
    meta: {
      requiresAuth: false,
      menu: MENUS.COMMON.GUIDE
    },
  },
  {
    path: '/temp/join',
    name: 'UserJoin',
    component: UserJoin,
    meta: {
      isJoinPage: true,
      requiresAuth: false,
      menu: MENUS.AUTH.JOIN
    },
    beforeEnter: async (to, from, next) => {
      const isAuthenticated = await checkAuthForRouterGuard();
      if (isAuthenticated) {
        next('/main'); // JWT가 유효하면 메인페이지로
      } else {
        next(); // JWT가 없거나 무효하면 회원가입 페이지로
      }
    },
  },

  
  {
    path: '/auth',
    name: 'Auth',
    children: authRoutes,
  },
  {
    path: '/info',
    name: 'Info',
    children: infoRoutes,
  },
  {
    path: '/user',
    name: 'User',
    children: userRoutes,
  },
  {
    path: '/management',
    name: 'Management',
    children: managementRoutes,
  },
  {
    path: '/receipt',
    name: 'Receipt',
    children: receiptRoutes,
  },
  {
    path: '/system',
    name: 'System',
    children: systemRoutes,
  },
  // 404 모든 잘못된 경로 처리 → /main으로 리다이렉트
  // {
  //   path: '/:pathMatch(.*)*',
  //   redirect: '/main',
  // },
];

// 영수증 기능이 활성화된 경우에만 receiptRoutes를 추가
if (isReceiptEnabled) {
  baseRoutes.push({
    path: '/receipt',
    name: 'Receipt',
    children: receiptRoutes,
  });
}

// 404 리디렉션은 항상 마지막에 추가
baseRoutes.push({
  path: '/:pathMatch(.*)*',
  redirect: '/main',
});

const router = createRouter({
  // history: createWebHistory(),
  history: createWebHashHistory(), // 해시모드(#) 사용
  routes: baseRoutes,
});

// 라우트 네비게이션 전역 가드 설정
// 라우트 권한 확인
router.beforeEach(async (to, from, next) => {

  // [디버깅용 로그 추가] 페이지 이동 경로를 추적합니다.
  console.log(`[Router] Navigating from: ${from.path} -> to: ${to.path}`);

  // 직전 라우트 이름만 세션에 기록 → 복원 조건에서 참조
  try { sessionStorage.setItem('vs_prev_name', from.name || ''); } catch (e) {}

  // .env에서 LOCALTEST가 true일 경우 인증 없이 통과
  if (import.meta.env.VITE_LOCALTEST === 'true') {
    return next();
  }

  const authStore = useAuthStore(); // Pinia
  if (authStore.roles.includes('ROLE_GATE_SYSTEM')) {
    return next();
  }

  // 기본: 모든 라우트는 인증 필요로 가정
  // 인증 불필요한 경우에만 requiresAuth: false가 명시되어 있음
  const requiresAuth = !to.matched.some(record => record.meta.requiresAuth === false);

  // (1) 인증이 필요한 라우트인지
  if (requiresAuth) {
    // 1-A) 로그인 여부 체크
    if (!authStore.isAuthenticated) {
      // 로그인 안 되어 있음 → 로그인 페이지로
      toast.warning('로그인이 필요합니다');
      await new Promise((resolve) => setTimeout(resolve, 1000));
      return next({ name: 'UserLogin' });
    }

    // 1-B) 권한 체크 (allowedRoles가 있는 경우만 검사)
    const routeAllowedRoles = to.meta.allowedRoles; // 예: ['ROLE_HRM_MANAGER', 'ROLE_HRM_EMPLOYEE']
    if (routeAllowedRoles && routeAllowedRoles.length > 0) {
      // 사용자 권한 배열
      const userRoles = authStore.roles; // 예: ['ROLE_HRM_EMPLOYEE', 'ROLE_HRM_MANAGER']

      // “하나라도 매칭되면 통과” 방식
      const hasRole = userRoles.some(role => routeAllowedRoles.includes(role));
      if (!hasRole) {
        // 권한 없음 → 접근 불가
        toast.warning('해당 메뉴에 접근할 권한이 없습니다');
        await new Promise((resolve) => setTimeout(resolve, 1000));
        // 그냥 이전 페이지로 (또는 다른 경로로)
        // return next(false);
        return next({ path: '/main' });
      }
    }
  }
  
  // (2) 인증 불필요 or 권한 체크 통과
  return next();
});

// 라우트 이동 완료 후 (혹은 중단 포함)
// Axios 사용 중 라우터 이동 시 로딩 중단
router.afterEach(() => {

  // Pinia 스토어는 이 안에서 호출해야 안전
  const toastStore = useToastStore();
  // 다음 페이지에 보여줄 메시지가 있는지 확인
  if (toastStore.nextPageMessage) {
    const { text, type } = toastStore.nextPageMessage;
    // 저장된 타입에 따라 적절한 토스트를 띄움
    toastStore[type](text);
    // 메시지를 사용했으므로 즉시 스토어에서 비움
    toastStore.nextPageMessage = null; 
  }

  // 라우트 이동 완료 시 로딩 종료
  const loadingStore = useLoadingStore();
  loadingStore.stopLoading();
  
  // 라우트 이동이 끝난 뒤, 사이드바 닫기
  const sidebarStore = useSidebarStore();
  sidebarStore.closeSidebar();

  

  // 라우트 이동이 끝난 뒤, 스크롤 맨 위로 이동
  window.scrollTo(0, 0)
});

export default router;

// ───────────────────────────────────────────────────────────────
// 동적 import(코드 스플리팅)된 청크 로드 실패 시 자동 새로고침
//    배포 후 브라우저가 구버전 청크(.js) 파일을 캐시하고 있을 경우
//    "Loading chunk N failed" 오류가 발생하면서 컴포넌트가 렌더되지 않는
//    현상을 예방합니다. (로그인/회원가입 페이지가 빈 화면으로 남는 문제)
router.onError((error) => {
  // Vite·Webpack 공통 패턴: Loading chunk [name] failed
  const chunkLoadFailedPattern = /Loading chunk (\d)+ failed/;
  if (chunkLoadFailedPattern.test(error.message)) {
    // 캐시된 index.html을 무시하고 강제 새로고침 → 최신 청크 로드
    window.location.reload();
  }
});
