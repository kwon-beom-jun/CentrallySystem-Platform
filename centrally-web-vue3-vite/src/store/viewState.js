import { defineStore } from 'pinia';

export const useViewStateStore = defineStore('viewState', {
  state: () => ({
    byKey: {},
    _modeWatcherAttached: false,
    _lastIsMobile: null,
    _restoreFrom: {}, // { key: Set<string routeName> }
    _breakpoint: 650,
  }),
  actions: {
    /**
     * 목록 화면 상태 저장 (페이지, 필터, 스크롤 등)
     * @param {string} key 고유 키(화면 식별자)
     * @param {object} state 임의의 상태 객체
     */
    saveState(key, state) {
      const bp = this._breakpoint || 650;
      const isMobileNow = typeof window !== 'undefined' ? (window.innerWidth <= bp) : false;
      this.byKey[key] = {
        ...(state || {}),
        __mode: isMobileNow ? 'mobile' : 'desktop',
      };
    },
    /**
     * 저장된 목록 화면 상태 조회
     * @param {string} key 고유 키(화면 식별자)
     * @returns {object|null}
     */
    getState(key) {
      return this.byKey[key] || null;
    },
    /**
     * 저장된 상태 삭제
     * @param {string} key 고유 키(화면 식별자)
     */
    clearState(key) {
      if (this.byKey && Object.prototype.hasOwnProperty.call(this.byKey, key)) {
        delete this.byKey[key];
      }
    },
    /** 모든 저장 상태 초기화 */
    clearAll() {
      this.byKey = {};
    },
    /**
     * 특정 key에 대해, 허용되는 이전 라우트 이름 집합을 설정한다
     * @param {string} key
     * @param {string[]} fromRouteNames
     */
    allowRestoreFrom(key, fromRouteNames = []) {
      this._restoreFrom[key] = new Set(fromRouteNames);
    },
    /**
     * 직전 라우트가 허용된 상세 라우트인 경우에만 복원 허용
     * @param {string} key
     * @returns {boolean}
     */
    canRestore(key) {
      try {
        // 화면 모드가 막 전환된 뒤에는 어떤 복원도 허용하지 않는다 (전역 1회)
        const modeSwitched = sessionStorage.getItem('vs_mode_switched') === '1';
        if (modeSwitched) {
          sessionStorage.removeItem('vs_mode_switched');
          return false;
        }
        // 저장 당시 화면 모드와 현재 모드가 다르면 복원 금지
        const bp = this._breakpoint || 650;
        const isMobileNow = typeof window !== 'undefined' ? (window.innerWidth <= bp) : false;
        const saved = this.byKey[key];
        if (saved && saved.__mode && saved.__mode !== (isMobileNow ? 'mobile' : 'desktop')) {
          return false;
        }
        const prevName = sessionStorage.getItem('vs_prev_name') || '';
        const allow = this._restoreFrom[key];
        if (allow && allow.size > 0) return allow.has(prevName);
      } catch (e) {}
      return false;
    },
    /**
     * 모바일/데스크탑 전환 감지 리스너를 전역 1회 등록
     * @param {number} breakpoint px 단위, 기본 650
     */
    attachModeWatcher(breakpoint = 650) {
      if (typeof window === 'undefined') return;
      if (this._modeWatcherAttached) return;
      this._modeWatcherAttached = true;
      this._breakpoint = breakpoint;
      this._lastIsMobile = window.innerWidth <= breakpoint;
      window.addEventListener('resize', () => {
        const now = window.innerWidth <= breakpoint;
        if (now !== this._lastIsMobile) {
          this._lastIsMobile = now;
          // 화면 모드가 변하면 저장된 화면 상태를 초기화한다
          this.clearAll();
          try { sessionStorage.setItem('vs_mode_switched', '1'); } catch (e) {}
        }
      });
    },
  },
  persist: true,
});


