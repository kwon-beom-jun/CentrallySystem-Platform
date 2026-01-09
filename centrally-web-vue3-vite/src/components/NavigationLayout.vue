<!-- MobileLayer.vue ─ 650px 이하에서만 나타나는 하단-바 + 플로팅 시트 + 풀메뉴 -->
<template>
  <!-- ❶ 전체를 650px 이하에서만 렌더 -->
  <div v-if="isMobile">
      <!-- ───────── 하단 고정 바 ───────── -->
    <nav class="bottom-bar">
      <button @click="go(ROUTES.INFO.NOTICE)" :aria-label="$t('common.navigation.notice')">
        <i class="ri-notification-3-line"></i><span>{{ $t('common.navigation.notice') }}</span>
        <!-- <i class="ri-chat-1-line"></i><span>공지</span> -->
      </button>

      <!-- 영수증 내역(개인)으로 이동 -->
      <template v-if="canAccessReceiptFeature">
        <button
          @click="go(ROUTES.RECEIPT.SUBMISSION)"
          :aria-label="$t('common.navigation.receiptRegister')"
        >
          <i class="ri-file-add-line"></i><span>{{ $t('common.navigation.receiptRegister') }}</span>
        </button>
      </template>

      <!-- <button @click="go('/receipt/receipt-approval-request-overview')" aria-label="신청내역 현황">
        <i class="ri-code-s-slash-line"></i><span>신청내역</span>
      </button> -->

      <!-- 가운데 파란 + -->
      <button class="fab" @click="openSheet" :aria-label="$t('common.navigation.write')">
        <i class="ri-add-line"></i>
      </button>

      <!-- <button @click="go('/receipt/annual-receipt-summary')" aria-label="년도별 요약">
        <i class="ri-calendar-2-line"></i><span>년도별 요약</span>
      </button> -->

      <!-- <button
        @click="go('/hrm/user-information')"
        aria-label="개인 정보"
      >
        <i class="ri-box-3-line"></i><span>개인 정보</span>
      </button> -->

      <!-- 영수증 내역(개인)으로 이동 -->
      <template v-if="canAccessReceiptFeature">
        <button
          @click="go(ROUTES.RECEIPT.PERSONAL_HISTORY)"
          :aria-label="$t('common.navigation.receiptHistory')"
        >
          <i class="ri-file-list-3-line"></i><span>{{ $t('common.navigation.receiptHistory') }}</span>
        </button>
      </template>

      <button @click="toggleMenu" :aria-label="$t('common.navigation.more')">
        <i class="ri-menu-line"></i><span>{{ $t('common.navigation.more') }}</span>
      </button>
    </nav>

    <!-- ───────── 퀵메뉴(＋) ───────── -->
    <transition name="fade">
      <div v-if="sheet" class="quick-backdrop" @click.self="sheet = false">
        <div class="quick-menu">
          <!-- <router-link
            to="/receipt/receipt-submission"
            class="quick-item"
            @click="sheet = false"
          >
            <i class="ri-file-add-line"></i><span>영수증 등록</span>
          </router-link> --><!-- ✔ 버튼으로 교체 : 클릭 시 모달 오픈 -->
          <button
            class="quick-item"
            @click="openReceiptModal"
            :disabled="!CAN_RECEIPT_WRITE"
            v-if="CAN_RECEIPT_WRITE"
          >
            <i class="ri-file-add-line"></i><span>{{ $t('common.navigation.receiptRegister') }}</span>
          </button>

          <button
            v-if="CAN_RECEIPT_APPROVE"
            class="quick-item"
            @click="openReceiptApprovalRequestOverview"
          >
            <i class="ri-file-edit-line"></i><span>{{ $t('common.navigation.receiptApproval') }}</span>
          </button>

          <div v-if="isQuickMenuEmpty" class="quick-item-empty">
            {{ $t('common.navigation.quickMenu.empty') }}
          </div>
        </div>
      </div>
    </transition>

    <MobileDrawerMenu :menu="menu" :cats="cats" :user="drawerUser" />
  </div>

  <!-- <ReceiptSubmissionCreateModal
    v-if="CAN_RECEIPT_WRITE"
    :isVisible="receiptModalVisible"
    @close="receiptModalVisible = false"
    @confirm="
      () => {
        receiptModalVisible = false; /* 필요 시 새로고침 등 */
      }
    "
  /> -->
  <ReceiptSubmissionCreateModal
    v-if="CAN_RECEIPT_WRITE && !isNarrow650"
    :isVisible="receiptModalVisible"
    :currentDeptId="currentDeptId"
    :currentDeptName="currentDeptName"
    :currentTeamId="currentTeamId"
    :currentTeamName="currentTeamName"
    @close="receiptModalVisible = false"
    @confirm="handleReceiptConfirm"
  />

</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from 'vue-i18n';
import { useAuthStore } from "@/store/auth";
import { useHrmStore } from "@/store/hrm";
import { useMobileMenuStore } from '@/store/navigation';
import ReceiptSubmissionCreateModal from "@/components/receipt/ReceiptSubmissionCreateModal.vue";
import MobileDrawerMenu from "@/components/MobileDrawerMenu.vue";
import { useToastStore } from '@/store/toast'
import { getRolesFrom, hasRole as checkRole } from '@/utils/roleUtils';
import { ROLE_GROUPS } from '@/config/roleConfig';
import { getNavigationMenus, ROUTES } from '@/config/menuConfig';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { HRM_API_BASE_URL, HRM_SERVICE_NAME } from '@/constants';

const { t } = useI18n();

// 피처 플래그 변수
const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';
// toast store 사용
const toastStore = useToastStore();

/* 현재 사용자 부서/팀 정보 (hrmStore에서 가져옴) */
const currentDeptId = computed(() => hrmStore.getMyProfile?.departmentId ?? null);
const currentDeptName = computed(() => hrmStore.getMyProfile?.departmentName ?? '');
const currentTeamId = computed(() => hrmStore.getMyProfile?.teamId ?? null);
const currentTeamName = computed(() => hrmStore.getMyProfile?.teamName ?? '');

/* ---------- 650px 이하 여부 ---------- */
const mq = window.matchMedia("(max-width:1300px)");
const isMobile = ref(mq.matches);
mq.addEventListener("change", (e) => (isMobile.value = e.matches));
// 650px 이하 여부 (모바일 상세 전용 페이지 사용)
const mq650 = window.matchMedia("(max-width:650px)");
const isNarrow650 = ref(mq650.matches);
mq650.addEventListener("change", (e) => (isNarrow650.value = e.matches));

/* ---------- 라우터 ---------- */
const router = useRouter();
function go(p) {
  /* ✔ 안전하게 한 번 받은 router 사용 */
  router.push(p);
  // Sheet + Drawer 모두 닫기
  mobileMenu.reset();
}

/* ---------- 문서 트리 Drawer 토글 ---------- */
 

/* ---------- HRM 관리자 여부 ---------- */
const hrmStore = useHrmStore();
const isAdmin = computed(() => hrmStore.isHrmAdmin);

/* ---------- 상태 ---------- */
const mobileMenu = useMobileMenuStore();
const sheet = computed({
  get: () => mobileMenu.sheetOpen,
  set: v  => (v ? mobileMenu.openSheet() : mobileMenu.closeSheet()),
});
const menu = computed({
  get: () => mobileMenu.menuOpen,
  set: v  => (v ? mobileMenu.openMenu() : mobileMenu.closeMenu()),
});

/* ----------------- 영수증 등록/공지 작성 모달 열기 ----------------- */
const receiptModalVisible = ref(false);

/**
 * 650px 이하에서는 전용 상세 페이지로 이동, 그 외에는 전역 모달 오픈
 */
function openReceiptModal() {
  if (!CAN_RECEIPT_WRITE.value) {
    toastStore.warn(t('common.navigation.receipt.noPermission'));
    return;
  }
  const isNarrow = window.innerWidth <= 650;
  if (isNarrow) {
    sheet.value = false;
    router.push(ROUTES.RECEIPT.SUBMISSION_CREATE_MOBILE);
    return;
  }
  receiptModalVisible.value = true;
  sheet.value = false;
}

// 영수증 등록 모달 확인 버튼 클릭 시 실행할 함수
function handleReceiptConfirm() {
  // 모달을 먼저 닫습니다.
  receiptModalVisible.value = false;
  // 영수증 등록 페이지로 이동합니다.
  router.push(ROUTES.RECEIPT.SUBMISSION);
  // 스토어에 다음 페이지에서 띄울 메시지를 '예약'합니다.
  toastStore.setNextPageMessage(t('common.navigation.receipt.movedToPage'), "info");
  // 퀵메뉴, 풀메뉴 등 오버레이 요소를 모두 닫습니다.
  mobileMenu.reset();
}

/* 필요한 권한 (roleUtils.js의 hasRole 사용) */
const roles = computed(() => auth.roles);

/**
 * 권한 체크 함수
 */
function hasRole(allowed) {
  return checkRole(roles.value, allowed);
}

// 영수증 권한 확인에 isReceiptEnabled 조건 추가
const CAN_RECEIPT_WRITE = computed(() =>
  isReceiptEnabled && hasRole(getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR))
);
const CAN_RECEIPT_APPROVE = computed(() =>
  isReceiptEnabled && hasRole(getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER))
);
const canAccessReceiptFeature = computed(() => {
  // 영수증 기능이 활성화 되어있고, 영수증 권한이 하나라도 있으면 true
  return isReceiptEnabled && (CAN_RECEIPT_WRITE.value);
});
// 퀵메뉴에 표시할 버튼이 하나도 없는지 확인하는 computed 속성
const isQuickMenuEmpty = computed(() => {
  return !CAN_RECEIPT_WRITE.value && !CAN_RECEIPT_APPROVE.value;
});
/* ----------------- 영수증 등록 모달 END ----------------- */

/* ---------- 문서 작성 화면으로 이동 ---------- */
 

/* ---------- 영수증 신청 내역 조회 현황 화면으로 이동 ---------- */
function openReceiptApprovalRequestOverview() {
  /** 1) 퀵메뉴 닫기 */
  sheet.value = false;
  /** 2) 혹시 열려있을지 모를 Drawer 도 닫기 */
  
  /** 3) 라우터 이동 */
  router.push({ name: "ReceiptApprovalRequestOverview" });
}

/* ---------- 더보기 토글 ---------- */
function toggleMenu() {
  if (!mobileMenu.menuOpen) pushOverlayState() 
  /* 열려 있던 Sheet(+) 는 무조건 닫기 */
  mobileMenu.closeSheet();
  /* '지금' 열려고 하는 상태인지 미리 계산 */
  mobileMenu.toggleMenu();

  

  /* ② 토글 적용 */
  // menu.value = willOpen;
}

/* ─── 플로팅 시트(+) 열기 ─── */
function openSheet () {
  if (!mobileMenu.sheetOpen) pushOverlayState()
  mobileMenu.openSheet();    // Sheet 열기
  mobileMenu.closeMenu();    // Drawer는 닫기

}

/* ---------- 메뉴 & 권한 (menuConfig.js에서 import) ---------- */
const auth = useAuthStore();

/**
 * 메뉴 권한 체크 함수 (roleUtils.js 사용)
 */
function canShow(r) {
  return checkRole(roles.value, r || []);
}

/**
 * 전체 메뉴 구조 (menuConfig.js에서 가져오기)
 * 영수증 기능 활성화 여부에 따라 필터링
 */
const rawMenus = computed(() => {
  const menus = getNavigationMenus();
  
  // 영수증 기능이 비활성화된 경우 영수증 메뉴 제거
  if (!isReceiptEnabled) {
    return menus.filter(menu => menu.key !== 'receipt');
  }
  
  return menus;
});

/**
 * 권한에 따라 필터링된 메뉴 카테고리
 */
const cats = computed(() => 
  rawMenus.value
    .map((c) => ({ ...c, sub: c.sub.filter((s) => canShow(s.roles || [])) }))
    .filter((c) => c.sub.length)
);

// 드로어 상단에 표시할 사용자 정보 (이름/직책/사진)
const drawerUser = computed(() => {
  const profile = hrmStore.getMyProfile;
  const profileImgUrl = profile?.profileImgUrl;
  const photoUrl = profileImgUrl 
    ? getImagePreviewUrl(profileImgUrl, HRM_API_BASE_URL, HRM_SERVICE_NAME)
    : '';
  
  return {
    name: profile?.name ?? '',
    email: profile?.email ?? '',
    position: profile?.positionName ?? '',
    photoUrl: photoUrl
  };
});

/* ──────────────────────────  
   sheet / menu / drawer 중
   하나라도 true 면 body 잠금
────────────────────────── */
const anyOverlayOpen = computed(() =>
  // mobileMenu.sheetOpen || mobileMenu.menuOpen
  mobileMenu.menuOpen // mobileMenu.sheetOpen 퀵메뉴 제거
)
/* ─────────────────────────────────────────────────────────────
 * 모바일 화면에서 오버레이(퀵메뉴, 풀메뉴, 문서트리)가 열릴 때,
 * 뒷 배경(body) 스크롤을 막기 위한 처리 (iOS 바운스 효과 방지)
 * ───────────────────────────────────────────────────────────── */
let savedScrollY = 0;
watch(anyOverlayOpen, (open) => {
  const body = document.body;

  if (open) {
    // 1. 현재 스크롤 위치 저장
    savedScrollY = window.scrollY;
    
    // 2. body에 스크롤 잠금 스타일 적용
    body.style.position = 'fixed';
    body.style.top = `-${savedScrollY}px`;
    body.style.width = '100%';
    body.style.overflow = 'hidden'; // 만약을 위한 추가
  } else {
    // 1. body에 적용했던 스타일 모두 제거
    body.style.position = '';
    body.style.top = '';
    body.style.width = '';
    body.style.overflow = '';
    
    // 2. 저장했던 스크롤 위치로 복원
    window.scrollTo(0, savedScrollY);
  }
});

/* ───────────────────────────
   ① 오버레이 열 때 한 번 pushState
─────────────────────────── */
function pushOverlayState () {
  /* 바로 이전 history 를 그대로 복제해서 한 칸 밀어둠
     (같은 URL, 다른 state) */
  history.pushState({ mobileOverlay: true }, '')
}

/* ───────────────────────────
   ② “닫을 게 있으면 닫고 true” 헬퍼
─────────────────────────── */
function closeAnyOverlay () {
  let closed = false

  if (mobileMenu.sheetOpen) {          // 퀵메뉴(+)
    mobileMenu.closeSheet(); closed = true
  }
  if (mobileMenu.menuOpen) {           // 햄버거(≡)
    mobileMenu.closeMenu(); closed = true
  }
  
  return closed
}

/* ───────────────────────────
   ③ popstate(뒤로-가기) 핸들러
      – 오버레이만 닫고, URL 이동은 소비
─────────────────────────── */
function handlePop (e) {
  if (closeAnyOverlay()) {
    /* 지금 이벤트는 “오버레이 닫기” 용으로만 사용됐음
       더 뒤로 가면 안 되니 같은 state 를 다시 push */
    history.pushState(null, '')        // 현재 페이지 상태 복구
  }
  /* 닫을 게 없으면 브라우저/라우터 기본 동작 */
}


/* 컴포넌트가 마운트될 때 사용자 정보 조회 실행 */
onMounted(async () => {
  // HRM 프로필 로드 (이름/직책/사진/부서/팀)
  // 이미 로드된 경우 재조회하지 않음 (Pinia persist로 복원된 경우)
  if (!hrmStore.getMyProfile && auth.getUserId) {
    try {
      await hrmStore.loadMyProfileByUserId(auth.getUserId);
    } catch (e) { /* noop */ }
  }
  window.addEventListener('popstate', handlePop);
});
onUnmounted(() => window.removeEventListener('popstate', handlePop))
</script>

<style scoped>
/* ───── 하단 바 ───── */
.bottom-bar {
  position: fixed;
  inset-block-end: 0;
  inset-inline: 0;
  height: 70px;
  background: var(--theme-bg-main);
  border-top: 1px solid var(--theme-border);
  min-width: 360px !important;
  z-index: 1014;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 0px 15px 0px 15px;
}
button {
  all: unset;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: var(--theme-text-primary);
  cursor: pointer;
}
button span {
  font-size: 0.6rem;
  font-weight: 800;
}
button i {
  font-size: 24px;
}
.fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  margin-top: -30px;
  background: #008cff;
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.25);
}
.fab i {
  font-size: 22px;
}

/* ───── 퀵메뉴 드롭다운 ───── */
.quick-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1015;
}
.quick-menu {
  position: fixed;
  left: 50%;
  bottom: 72px; /* 툴바(56) + 여백 16 */
  transform: translateX(-50%);
  background: var(--theme-bg-main);
  border: 1px solid var(--theme-border);
  border-radius: 8px;
  padding: 16px 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.quick-item {
  all: unset;
  display: flex;
  align-items: center;
  /* justify-content: center; */
  gap: 6px; /* ← 중앙정렬 + 5px 간격 */
  cursor: pointer;
  padding: 6px 0;
  color: var(--theme-text-primary);
}
.quick-item span {
  font-size: 0.75rem;
}
.quick-item:hover {
  color: #008cff;
}
.quick-item i {
  font-size: 18px;
}
/* ───── 말풍선 꼬리 (테두리) ───── */
.quick-menu::before {
  content: "";
  position: absolute;
  left: 50%; /* 가운데 정렬 */
  top: 100%; /* 상자 아래에 붙이기 */
  transform: translateX(-50%) translateY(1px); /* 1px 만큼 아래로 내려 테두리 겹침 방지 */
  width: 0;
  height: 0;
  border: 9px solid transparent; /* 꼬리 크기 = 9px */
  border-top-color: var(--theme-border); /* 테두리색과 동일 */
}
/* ───── 말풍선 꼬리 (흰색 내부) ───── */
.quick-menu::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 100%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border: 8px solid transparent; /* 1px 작게 → 테두리 안쪽 */
  border-top-color: var(--theme-bg-main); /* 상자 배경과 동일 */
}
.quick-item-empty {
  font-size: 0.8rem;
  color: #888;
  text-align: center;
  padding: 10px 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

html.lock-scroll,
body.lock-scroll,
#app.lock-scroll {           /* #app 이 스크롤 루트일 때 대비 */
  overflow: hidden !important;
  height: 100%;              /* iOS bounce 방지 */
}
</style>
