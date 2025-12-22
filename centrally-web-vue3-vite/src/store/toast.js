// src/store/toast.js
import { defineStore } from 'pinia'
import { toast } from 'vue3-toastify'

/* ─────────────────────────────
 * 1.5초 내 중복 메시지 차단용 Set
 * ───────────────────────────── */
const recent = new Set()
function pushOnce(msg, opts = {}) {
  if (recent.has(msg)) return          // 같은 메시지 재표시 방지
  // recent.add(msg)
  setTimeout(() => recent.delete(msg), 1500)
  toast(msg, { autoClose : 1500, ...opts }) // 기본 1.5초, 필요하면 개별 덮어쓰기
}

export const useToastStore = defineStore('toast', {
  // state에 다음 페이지 메시지를 저장할 변수 추가
  state: () => ({
    nextPageMessage: null, // { type: 'info', text: '메시지 내용' } 형태의 객체를 저장
  }),
  actions: {
    // 다음 페이지에서 띄울 메시지를 설정하는 액션 추가
    setNextPageMessage(message, type = 'info') {
      this.nextPageMessage = { text: message, type: type };
    },
    success(msg, opts = {}) { pushOnce(msg, { type: 'success', ...opts }) },
    error(msg,   opts = {}) { pushOnce(msg, { type: 'error',   ...opts }) },
    info(msg,    opts = {}) { pushOnce(msg, { type: 'info',    ...opts }) },
    warn(msg,    opts = {}) { pushOnce(msg, { type: 'warning', ...opts }) },

    /* 모든 토스트 또는 특정 ID 닫기 */
    clear(id) { id ? toast.dismiss(id) : toast.dismiss() },
  },
})
