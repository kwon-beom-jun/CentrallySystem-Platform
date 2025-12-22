// apiConfig.js
import axios from 'axios';
import router from '@/router';
import { useToastStore } from '@/store/toast'
import { useHrmStore } from '@/store/hrm'
import qs from 'qs';

/* ───────────────────────────────
 * 1) 전역 토스트 스토어(초기에 undefined)
 * ─────────────────────────────── */
let toastStore
let isForcingLogout = false // 한번 실행 중이면 true


/* 현재 위치가 ‘로그인 화면’인지 판별  */
const LOGIN_PATHS = ['/', '/guide', '/temp/join'];
/**
 * ▸ 인수로 path 문자열을 주면 그대로 검사
 * ▸ 인수 생략 → 현재 router.currentRoute.value.path 를 자동 사용
 *   (즉 “지금 화면이 로그인 페이지인가?” 를 바로 확인 가능)
 */
const isLoginRoute = (raw = router.currentRoute?.value?.path ?? '') => {
  /* '', '/#', '/#/' → 모두 '/' 로 통일 */
  const norm = raw === '' || raw === '/#' || raw === '/#/' ? '/' : raw;
  return LOGIN_PATHS.includes(norm);
};

/* ────────────────────────────────────────────────
 * 공통 유틸
 * ──────────────────────────────────────────────── */
const recentToasts = new Set();
function pushToastOnce(msg, opts = {}) {
  if (recentToasts.has(msg)) return;          // 같은 메시지 1.5초 내 재표시 방지
  // recentToasts.add(msg);
  setTimeout(() => recentToasts.delete(msg), 1500)   // 중복 차단용 1.5초
  toastStore.error(msg, { ...opts })
}

// Refresh Token 갱신 관련 상태
let isRefreshing = false;
let failedQueue = [];

/**
 * 대기열 처리 (갱신 성공/실패 시 대기 중인 요청들 처리)
 */
const processQueue = (error) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve();
    }
  });
  failedQueue = [];
};

/* ────────────────────────────────────────────────
 * axios 인스턴스 공통 팩토리
 * ──────────────────────────────────────────────── */
const createApi = (baseURL) =>
  axios.create({
    baseURL,
    withCredentials: true,                    // HttpOnly JWT 쿠키 전송
    headers: { 'Content-Type': 'application/json' },
    // 배열 → roles=AAA&roles=BBB 형태로 직렬화
    // roles[]=… 대신 roles=…&roles=… 로
    paramsSerializer: (params) =>
      qs.stringify(params, { arrayFormat: 'repeat' }),
  });

export const systemApi  = createApi(import.meta.env.VITE_SYSTEM_API_BASE_URL);
export const authApi    = createApi(import.meta.env.VITE_AUTH_API_BASE_URL);
export const hrmApi     = createApi(import.meta.env.VITE_HRM_API_BASE_URL);
export const receiptApi = createApi(import.meta.env.VITE_RECEIPT_API_BASE_URL);
export const infoApi    = createApi(import.meta.env.VITE_INFO_API_BASE_URL);

/* ────────────────────────────────────────────────
 * 인터셉터 초기화 (main.js 등에서 1회 호출)
 * ──────────────────────────────────────────────── */
export async function initInterceptors() {
  /* Pinia 등록 시점 문제를 피하려고 require() 사용 */
  // const { useLoadingStore } = require('@/store/loading');
  // const { useAuthStore    } = require('@/store/auth');
  const { useLoadingStore } = await import('@/store/loading');
  const { useAuthStore    } = await import('@/store/auth');

  const loadingStore = useLoadingStore();
  const authStore    = useAuthStore();
  toastStore         = useToastStore() 

  const attachInterceptors = (api) => {
    /* ▸ 1) Request */
    api.interceptors.request.use(
      (config) => {
        /* ── 전역 로딩 제외 옵션 ---------------------- */
        if (!config.skipGlobalLoading) {
          loadingStore.startLoading();
          /* 나중에 stop 때 구분하기 위해 플래그 저장 */
          config.__didStartGlobalLoading = true;
        }
        const menu = router.currentRoute.value?.meta?.menu ?? '';
        document.cookie = `X-Menu-Vue=${encodeURIComponent(menu)}; path=/`;

        // GET 요청일 때는 X-Func-Vue 쿠키 삭제 (조회는 이력에 남지 않으므로)
        if (config.method?.toLowerCase() === 'get') {
          document.cookie = 'X-Func-Vue=; Max-Age=0; path=/';
        }

        return config;
      },
      async (error) => {
        loadingStore.stopLoading();
        toastStore.error(`에러 발생 : ${error}`);
        // await delay(1000);
        return Promise.reject(error.response);
      },
    );

    /* ▸ 2) Response */
    api.interceptors.response.use(
      (res) => {
        if (res.config?.__didStartGlobalLoading) {
          loadingStore.stopLoading();
        }
        return res;                            // 전체 응답 객체 유지
      },
      async (error) => {
        if (error.config?.__didStartGlobalLoading) {
          loadingStore.stopLoading();
        }
        
        const originalRequest = error.config;
        
        // 🔄 401 에러 & Refresh Token 자동 갱신 처리
        if (error.response?.status === 401 && !originalRequest._retry) {
          
          // skipErrorHandling 옵션이 있으면 자동 갱신 시도하지 않음 (로그인 페이지 등)
          if (originalRequest?.skipErrorHandling) {
            return Promise.reject(error);
          }
          
          // 이미 갱신 중이면 대기열에 추가
          if (isRefreshing) {
            return new Promise((resolve, reject) => {
              failedQueue.push({ resolve, reject });
            }).then(() => {
              return api(originalRequest);
            }).catch(err => {
              return Promise.reject(err);
            });
          }
          
          originalRequest._retry = true;
          isRefreshing = true;
          
          try {
            // Refresh Token으로 갱신 시도
            const { default: LoginApi } = await import('@/api/auth/LoginApi');
            await LoginApi.refreshToken();
            
            // 대기열 처리 (성공)
            processQueue(null);
            isRefreshing = false;
            
            // 원래 요청 재시도
            return api(originalRequest);
            
          // 갱신 실패 → 대기열 처리 (실패) 및 로그아웃
          } catch (refreshError) {
            processQueue(refreshError);
            isRefreshing = false;
            const handledError = refreshError?.response ? refreshError : error;

            if (handledError.config) {
              handledError.config.skipErrorHandling = false;
            }

            const fallbackMessage =
              handledError?.response?.data?.message ||
              handledError?.message ||
              '세션이 만료되었습니다.\n다시 로그인해주세요';

            await forceLogout(
              fallbackMessage,
              authStore,
              router.currentRoute.value.path,
              true
            );
            return new Promise(() => {});
          }
        }
        
        // 공통 에러 처리 건너뛰기 옵션
        if (originalRequest?.skipErrorHandling) {
          return Promise.reject(error);
        }
        
        // Blob 에러 처리: responseType==='blob' 인 경우 Blob → JSON으로 변환
        if (originalRequest?.responseType === 'blob' && error.response?.data instanceof Blob) {
          try {
            const text = await error.response.data.text();
            const json = JSON.parse(text);
            // overwrite error.response.data 로 JSON 세팅
            error.response.data = json;
          } catch (e) {
            // 변환 실패 시 무시
          }
        }
        await handleError(error, authStore);
        /* resolve 되지 않는 Promise 반환 → 이후 then 체인 차단 */
        return new Promise(() => {});
      },
    );
  };

  [systemApi, authApi, hrmApi, receiptApi, infoApi].forEach(attachInterceptors);
}

/* ────────────────────────────────────────────────
 * 공통 에러 처리
 * ──────────────────────────────────────────────── */
async function handleError(error, authStore) {

  /* 로그아웃 처리 중엔 아무것도 하지 않는다 */
  if (isForcingLogout) return;

  /* i18n 가져오기 */
  let t;
  try {
    const i18n = await import('@/locales');
    t = i18n.default.global.t;
  } catch (e) {
    // i18n 로드 실패 시 기본 메시지 사용
    console.warn('i18n 로드 실패:', e);
  }

  /* 실패 서비스 식별 */
  const url = error?.config?.baseURL ?? '';
  const serviceKey =
    url.includes('/hrm')     ? 'hrm' :
    url.includes('/auth')    ? 'auth' :
    url.includes('/receipt') ? 'receipt' :
    url.includes('/info')    ? 'info' :
    'system';
  
  /* 서비스 키를 짧은 식별자로 변환 */
  const serviceLabel = 
    serviceKey === 'hrm'     ? 'H' :
    serviceKey === 'auth'    ? 'A' :
    serviceKey === 'receipt' ? 'R' :
    serviceKey === 'info'    ? 'I' :
    'S';

  const { status, data = {}, headers = {} } = error.response || {};
  const msg    = data.message || data.error || (t ? t('error.http.500') : '알 수 없는 오류가 발생했습니다');
  const reason = headers['x-reason'];
  const here   = router.currentRoute.value.path;

  /* ▸ 401 : 권한 변경 or 토큰 만료 */
  if (status === 401) {
    const errorMsg = t
      ? (reason === 'ROLE_CHANGED'
          ? t('error.service.auth.roleChanged')
          : t('error.service.auth.unauthorized'))
      : (reason === 'ROLE_CHANGED'
          ? '권한이 변경되었습니다\n다시 로그인해주세요'
          : '로그아웃 되었습니다\n다시 로그인해주세요');
    
    // skipErrorHandling 옵션이 있으면 강제 로그아웃 처리 안함 (로그인/회원가입 페이지용)
    if (!error.config?.skipErrorHandling) {
      await forceLogout(errorMsg, authStore, here, !isLoginRoute(here));
    }
    return;
  }

  /* ▸ 403 : 금지 */
  if (status === 403) {
    const errorMsg = t
      ? t(`error.service.${serviceKey}.forbidden`)
      : `[${serviceLabel}] 접근 권한이 없습니다`;
    pushToastOnce(errorMsg);
    // await delay(1500);
    return;
  }

  /* ▸ 503 : 서비스 사용 불가 */
  if (status === 503) {
    const errorMsg = t
      ? t(`error.service.${serviceKey}.unavailable`)
      : `[${serviceLabel}] 서비스를 사용할 수 없습니다`;
    pushToastOnce(errorMsg);
    // await delay(1500);
    return;
  }

  /* ▸ 네트워크 / 잘못된 요청 */
  if (
    error.code === 'ERR_NETWORK' ||
    (error.code === 'ERR_BAD_REQUEST' && !data.message)
  ) {
    const errorMsg = t
      ? t(`error.service.${serviceKey}.connection`)
      : `[${serviceLabel}] 서비스와의 연결에 실패했습니다`;
    pushToastOnce(errorMsg);
    // await delay(1500);
    return;
  }

  /* ▸ 기타 백엔드 오류 */
  // 백엔드가 보낸 메시지가 있으면 그대로 사용 (백엔드도 다국어 처리됨)
  pushToastOnce(msg);
  // await delay(1500);
}

/**
 * @param {string} message     토스트 메시지
 * @param {PiniaStore} authStore  인증 스토어
 * @param {string} [currentPath]  현재 경로 (생략 시 즉시 router 에서 조회)
 * @param {boolean} [showMessage=true]  메시지 표시 여부
 */
async function forceLogout(message, authStore, currentPath = router.currentRoute.value.path, showMessage = true) {
  console.log('[forceLogout] called!!'); 
  if (isForcingLogout) return;      // 이미 실행 중이면 바로 종료
  isForcingLogout = true;           // ★ 잠금
  
  /* 0) 테마를 light로 즉시 변경 (깜빡임 방지) */
  document.documentElement.setAttribute('data-theme', 'light');
  document.body.setAttribute('data-theme', 'light');
  document.documentElement.style.backgroundColor = '#ffffff';
  
  /* 1) Pinia 상태 초기화 --------------- */
  authStore.$reset()                        // auth state 모두 비움
  
  // HRM store 초기화
  const hrmStore = useHrmStore();
  hrmStore.clearMyProfile();

  /* 2) persistedstate(로컬스토리지) 제거 */
  // pinia-plugin-persistedstate 기본 키: "pinia"
  localStorage.removeItem('pinia')
  // 개별 스토어 키도 명시적으로 제거 (persist: true인 스토어들)
  localStorage.removeItem('auth')
  localStorage.removeItem('hrm')
  localStorage.removeItem('viewState')

  /* 3) JS 로 지울 수 있는 쿠키 제거 ------ */
  function deleteCookie(name) {
    document.cookie = `${name}=; Max-Age=0; path=/`
  }
  // 앱이 쓰는 쿠키 이름들만 나열 (HttpOnly 인 JWT 쿠키는 JS에서 삭제 불가)
  ;['X-Menu-Vue', 'YOUR_NON_HTTPONLY_COOKIE'].forEach(deleteCookie)

  /* 4) 라우터 이동 & 토스트 ------------ */
  /* 현재 화면이 로그인 X → 먼저 이동  */
  if (!isLoginRoute(currentPath)) {
    await router.push("/").catch(() => {});  // 동일 경로일 땐 무시
  }
  
  /* 메시지 표시 (showMessage가 true일 때만) */
  if (showMessage) {
    pushToastOnce(message);
  }

  isForcingLogout = false;
}
