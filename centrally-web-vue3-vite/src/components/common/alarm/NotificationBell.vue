<template>
  <div class="notification-bell">
    <button class="icon-btn bell" @click.stop="toggle" :aria-label="$t('common.notification.title')">
      <i class="ri-notification-3-line" />
      <span v-if="totalCount" class="badge">{{ totalCount }}</span>
    </button>

    <transition name="fade">
      <div v-if="isOpen && notifications.length" class="dropdown">
        <p class="summary">
          {{ $t('common.notification.summary', { approvals, concurrences }) }}
        </p>
        <ul>
          <li v-for="n in notifications" :key="n.id">
            <RouterLink :to="n.link" @click.prevent="go(n.link)">
              <i :class="n.type === 'approval'
                          ? 'ri-survey-line'
                          : 'ri-hand-coin-line'" />
              <span>{{ t(n.messageKey, n.params) }}</span>
            </RouterLink>
            <!-- <div v-if="n.months && n.months.length" class="months">{{ formatMonths(n.months) }}</div> -->
            <div v-if="n.months && n.months.length" class="months">
              <span
                v-for="(m, idx) in n.months"
                :key="idx"
                class="month-item"
                @click="goToMonth(n.link, m)"
              >
                {{ toYYMM(m) }}
              </span>
            </div>
          </li>
        </ul>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, RouterLink } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { useI18n } from 'vue-i18n';
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi';

const { t } = useI18n();

/* ─────────────────────────────────────────────
   0. 기능 플래그 : 영수증 알림 사용 여부
   ───────────────────────────────────────────── */
const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';

/* ─────────────────────────────────────────────
   1. 알림 배열 (서비스 통합)
   ───────────────────────────────────────────── */
const notifications = ref([]);

/* ─────────────────────────────────────────────
   2. 서비스별 fetch 로직 정의
   ───────────────────────────────────────────── */
const authStore = useAuthStore();

/** 영수증 알림(결재/합의) */
async function fetchReceiptNoti() {
  const approverId = authStore.getUserId;
  if (!approverId) return;

  const { data } = await ReceiptsSearchApi.getMyPendingCounts(approverId);
  const approvals    = data?.approvals    ?? 0;
  const concurrences = data?.concurrences ?? 0;
  const approvalMonths    = data?.approvalMonths    ?? [];
  const concurrenceMonths = data?.concurrenceMonths ?? [];

  notifications.value = notifications.value.filter(n => n.service !== 'receipt');
  notifications.value.push(
    {
      id: 'receipt-approval',
      service: 'receipt',
      type: 'approval',
      messageKey: 'common.notification.approvalWaiting',
      params: { count: approvals },
      link: '/receipt/receipt-approval-request-overview',
      count: approvals,
      months: approvalMonths
    },
    {
      id: 'receipt-concurrence',
      service: 'receipt',
      type: 'concurrence',
      messageKey: 'common.notification.concurrenceRequest',
      params: { count: concurrences },
      link: '/receipt/receipt-concurrence-request-overview',
      count: concurrences,
      months: concurrenceMonths
    }
  );
}

/* ❶ 추후 다른 서비스 알림을 추가하려면
   ----------------------------------------------------
   async function fetchBoardNoti() { … }
   fetchers.push(fetchBoardNoti);
*/
const fetchers = [];
if (isReceiptEnabled) fetchers.push(fetchReceiptNoti);

/* ─────────────────────────────────────────────
   Helper : YYYY-MM → YY-MM 리스트 변환
───────────────────────────────────────────── */
// function formatMonths(arr) {
//   return arr.map(m => m?.length >= 7 ? m.slice(2) : m).join(', ');
// }
/**
 * YYYY-MM 문자열을 YY-MM 형식으로 변환한다.
 * @param {string} str
 * @returns {string}
 */
function toYYMM(str) {
  return str?.length >= 7 ? str.slice(2) : str;
}

/* ─────────────────────────────────────────────
   3. 공통 계산 속성
   ───────────────────────────────────────────── */
const totalCount = computed(() =>
  notifications.value.reduce((sum, n) => sum + (n.count ?? 1), 0)
);
const approvals = computed(() =>
  notifications.value.find(n => n.type === 'approval')?.count ?? 0
);
const concurrences = computed(() =>
  notifications.value.find(n => n.type === 'concurrence')?.count ?? 0
);

/* ─────────────────────────────────────────────
   4. UI 상태 & 헬퍼
   ───────────────────────────────────────────── */
const isOpen = ref(false);
const router = useRouter();
/**
 * 알림 패널 열림/닫힘을 토글한다.
 */
function toggle() { isOpen.value = !isOpen.value; }
/**
 * 알림 패널을 닫는다.
 */
function close()  { isOpen.value = false; }
/**
 * 지정한 경로로 이동한다.
 * @param {string} path
 */
function go(path) { router.push(path); close(); }
/**
 * 특정 월 데이터를 쿼리로 전달하며 페이지를 이동한다.
 * @param {string} path
 * @param {string} month
 */
function goToMonth(path, month) {
  router.push({ path, query: { monthDate: month } });
  close();
}

/* ─────────────────────────────────────────────
   5. 마운트 / 언마운트
   ───────────────────────────────────────────── */
let clickListener, navGuard;
/**
 * 알림 데이터를 최신 상태로 갱신한다.
 */
async function refresh() {
  await Promise.allSettled(fetchers.map(fn => fn()));
}

onMounted(async () => {
  await refresh();                         // 초기 로드
  navGuard = router.afterEach(refresh);    // 라우트 변경 시 재조회

  /**
   * 벨 영역 외부 클릭 시 드롭다운을 닫는다.
   * @param {MouseEvent} e
   */
  clickListener = e => {
    if (!e.target.closest('.notification-bell')) close();
  };
  document.addEventListener('click', clickListener);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', clickListener);
  navGuard && navGuard();                  // afterEach 해제
});
</script>

<style scoped>
/* ─── 벨 ─── */
.bell {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  color: #6b7280;
  transition: all .2s ease;
}
.bell i {
  font-size: 22px;
}
.bell:hover {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
  transform: translateY(-1px) scale(1.05);
  box-shadow: 0 4px 12px rgba(37,99,235,.3);
}
.bell:active {
  transform: translateY(0) scale(.98);
  box-shadow: 0 2px 6px rgba(37,99,235,.2);
}

/* 모바일 크기 조정 */
@media (max-width: 1300px) {
  .bell {
    width: 35px;
    height: 35px;
  }
  .bell i {
    font-size: 20px;
  }
}
.notification-bell {
  position: relative; /* ← 드롭다운의 기준 */
  display: inline-block;
}
.badge {
  position: absolute;
  top: -3px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 4px;
  border-radius: 9px;
  font-size: 11px;
  font-weight: 700;
  text-align: center;
  background-color: #dc3545 !important;
  color: #ffffff !important;
}

/* ─── 드롭다운 ─── */
.dropdown {
  position: absolute;
  top: 45px;
  /* 기본은 우측 정렬 */
  /* right: 0; */
  right: auto;
  left: 50%;
  transform: translateX(-50%);
  width: 180px;
  max-width: 90vw;
  background: #fff;
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 14px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  padding: 12px 14px 16px 14px;
  z-index: 1016;
}
.summary {
  font-size: 0.78rem;
  margin-top: 5px;
  margin-bottom: 13px;
  font-weight: 600;
  color: #555;
}
ul {
  list-style: none;
  margin: 0;
  padding: 0;
}
li {
  margin-bottom: 6px;
}
li:last-child {
  margin-bottom: 0;
}
a {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #333;
  padding: 6px 8px;
  border-radius: 4px;
  text-decoration: none;
  font-size: 0.7rem;
}
a:hover {
  background: #f5f6f8;
}

/* .months {
  font-size: 0.68rem;
  color: #777;
  margin-left: 10px;
} */

.months {
  /* ‘결재 대기’ 텍스트 첫 글자(결)와 좌측 정렬 맞추기
     = 링크 왼쪽 padding 8px + 아이콘 14px + gap 6px ≒ 28px */
  margin-left: 28px;

  /* 2열(가변) 그리드 */
  display: grid;
  grid-template-columns: repeat(2, auto);
  column-gap: 0px;
  row-gap: 5px;

  font-size: 0.68rem;
  color: #777;
}

.month-item {
  cursor: pointer;
  text-decoration: underline;
}

.month-item:hover {
  color: #005dcf;
}

/* fade */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.12s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 다크 테마에서도 배경 빨간색, 글자 흰색 유지 */
body[data-theme="dark"] .badge {
  background-color: #dc3545 !important;
  color: #ffffff !important;
}

body[data-theme="dark"] .dropdown {
  background: #111827;
  border: 1px solid #1f2937;
  border-radius: 14px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.35);
}

body[data-theme="dark"] .dropdown a {
  color: #e5e7eb;
}

body[data-theme="dark"] .dropdown a:hover {
  background: rgba(37, 99, 235, 0.18);
  color: #ffffff;
}

body[data-theme="dark"] .summary {
  color: #ffffff;
}

/* ─── 좁은 화면(≤480px) : 가운데 정렬 ─── */
@media (max-width: 480px) {
  .dropdown {
    right: auto;
    left: 50%;
    transform: translateX(-50%);
    /* width:85vw; */
  }
}
</style>
