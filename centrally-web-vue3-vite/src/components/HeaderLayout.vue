<!-- src/components/common/AppHeader.vue -->
<template>
  <header class="navbar">
    <div class="navbar-inner">
      <!-- 로고(1300px 숨김) -->
      <RouterLink :to='ROUTES.MAIN' class="logo" @click="handleLogoClick">
        <!-- <img src="/img/common/logo/5.png" alt="logo" /> -->
        <img class="logo-desktop" :src="logoDesktopSrc" alt="logo" />
        <img class="logo-mobile" :src="logoMobileSrc" alt="logo-mobile" />
        <img class="logo-compact" :src="logoCompactSrc" alt="logo-compact" />
      </RouterLink>

      <!-- 확성기 아이콘 -->
      <div class="megaphone-icon">
        <i class="ri-megaphone-line"></i>
      </div>

      <!-- ─────────── 메인 메뉴 + 메가메뉴 ─────────── -->
      <!-- 사이드바 사용으로 숨김 -->
      <div
        v-if="false"
        class="menu-group"
        @mouseenter="cancelHide"
        @mouseleave="scheduleHide"
      >
        <nav class="desktop-nav">
          <div class="main-nav">
              <!-- 메뉴 박스의 click / touch 리스너 삭제 호버 기능만 사용하려 제거 -->
              <!-- @click.prevent="toggleByClick(m.key)"
                   @touchstart.prevent.stop="toggleByClick(m.key)" -->
            <div
              v-for="m in visibleMenus"
              :key="m.key"
              class="menu-box"
              :data-key="m.key"
              @mouseenter="setHover(m.key, $event)"
            >
              <a href="#" @click.prevent>
                <!-- 메인 메뉴용 아이콘 -->
                <i
                  :class="[
                    'menu-main-ico',
                    m.key === 'notice'      ? 'ri-megaphone-line' :
                    m.key === 'receipt'     ? 'ri-receipt-line' :
                    m.key === 'management'  ? 'ri-user-settings-line' :
                    m.key === 'system'      ? 'ri-settings-3-line' :
                                            ''
                  ]"
                ></i>
                {{ m.label }}
              </a>
            </div>
          </div>
        </nav>

        <!-- 메가 드롭다운 -->
        <transition name="mega" mode="out-in">
          <div v-if="hoveredMenu"
          :key="hoveredMenu.key"
          :style="megaMenuStyle"
          class="mega-all"
          :class="{
            receipt: hoveredMenu.key==='receipt',
            management: hoveredMenu.key==='management',
            notice : hoveredMenu.key==='notice'
          }"
          @mouseenter="cancelHide" @mouseleave="scheduleHide">


                <!-- ③ 공지사항 1-열 + 소개 -->
                <template v-if="hoveredMenu.key==='notice'">
                  <div class="mega-col single">
                    <p class="mega-title">공지사항</p>
                    <RouterLink v-for="s in visibleSubs(hoveredMenu.sub)"
                                :key="s.to"
                                :to="s.to"
                                class="mega-link"
                                @click="closeMega">
                      <i :class="['menu-ico', s.icon]"></i>{{ s.label }}
                    </RouterLink>
                  </div>

                  <!-- 오른쪽 소개 박스 -->
                   <div class="notice-intro" v-html="sanitizedNoticeIntro"></div>
                </template>


                <!-- ① 영수증 메뉴인 경우 → 4 칸 -->
                <template v-else-if="hoveredMenu.key === 'receipt'">
                  <div class="mega-col"
                      v-for="grp in visibleReceiptGroups(hoveredMenu.sub)"
                      :key="grp.group">
                    <p class="mega-title">{{ grp.group }}</p>
                    <RouterLink v-for="link in grp.items"
                                :key="link.to"
                                :to="link.to"
                                class="mega-link"
                                @click="closeMega">
                      <i :class="['menu-ico', link.icon]"></i>{{ link.label }}
                    </RouterLink>
                  </div>
                  <div v-if="visibleColumnCount <= 2"
                      class="notice-intro"
                      v-html="sanitizedHoveredIntro">
                  </div>
                </template>

                <!-- ② 사원 관리 3-열 -->
                <template v-else-if="hoveredMenu.key==='management'">
                  <div class="mega-col"
                      v-for="grp in visibleMngGroups(hoveredMenu.sub)"
                      :key="grp.group">
                    <p class="mega-title">{{ grp.group }}</p>
                    <RouterLink v-for="link in grp.items"
                                :key="link.to"
                                :to="link.to"
                                class="mega-link"
                                @click="closeMega">
                      <i :class="['menu-ico', link.icon]"></i>{{ link.label }}
                    </RouterLink>
                  </div>
                  <div v-if="visibleColumnCount <= 2"
                      class="notice-intro"
                      v-html="sanitizedHoveredIntro">
                  </div>
                </template>

                <!-- ② 나머지 메뉴는 기존 방식 -->
                <template v-else>
                  <div class="mega-col single">          <!--★ 새로운 래퍼-->
                    <p class="mega-title">{{ hoveredMenu.label }}</p>
                    <RouterLink v-for="s in visibleSubs(hoveredMenu.sub)"
                                :key="s.to"
                                :to="s.to"
                                class="mega-link"
                                @click="closeMega">
                      <i :class="['menu-ico', s.icon]"></i>{{ s.label }}
                    </RouterLink>
                  </div>
                  <!-- 공용 소개 박스 -->
                  <div class="notice-intro" v-html="sanitizedHoveredIntro"></div>
                </template>




          </div>
        </transition>
      </div>

      <!-- ─────────── 유저 영역 ─────────── -->
      <!-- <div class="user-box">
        <RouterLink to="/hrm/user-information" class="user-name">
          {{ authStore.getUser }}
        </RouterLink>
        <span class="divider">|</span>
        <a href="#" class="user-name" @click.prevent="doLogout">로그아웃</a>
      </div> -->
      
      <!-- <NotificationBell /> -->

      <!-- ─────────── 유저 영역 ─────────── -->
      <div class="user-box">
        <!-- 다국어 선택 -->
        <LanguageSwitcher mode="header-icon-mode" />
        
        <NotificationBell v-if="canShowNotification" />
        
        <!-- 프로필 -->
        <RouterLink :to="ROUTES.USER.USER_INFORMATION" class="icon-btn">
          <template v-if="profileImageUrl">
            <img :src="profileImageUrl" alt="profile" class="header-avatar" />
          </template>
          <template v-else>
            <svg class="icon"><use href="#icon-user" /></svg>
          </template>
        </RouterLink>

        <span class="divider">|</span>

        <!-- 로그아웃 -->
        <a href="#" class="icon-btn" @click.prevent="doLogout">
          <svg class="icon"><use href="#icon-logout" /></svg>
        </a>
      </div>
      <svg style="display: none">
        <!-- user-circle -->
        <symbol
          id="icon-user"
          viewBox="0 0 24 24"
          stroke="currentColor"
          fill="none"
        >
          <circle cx="12" cy="12" r="10" stroke-width="2" />
          <circle cx="12" cy="10" r="3" stroke-width="2" />
          <path d="M6 19c1.5-3 10.5-3 12 0" stroke-width="2" />
        </symbol>

        <!-- log-out -->
        <symbol
          id="icon-logout"
          viewBox="0 0 24 24"
          stroke="currentColor"
          fill="none"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M16 17l5-5-5-5" stroke-width="2" />
          <path d="M21 12H9" stroke-width="2" />
          <path
            d="M13 5v-2a1 1 0 0 0-1-1H4a1 1 0 0 0-1 1v18a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1v-2"
            stroke-width="2"
          />
        </symbol>
      </svg>
    </div>
  </header>
</template>

<script setup>
/* ─────────── imports ─────────── */
import { ref, computed, nextTick, onBeforeUnmount, onMounted, watch } from "vue";
import { useRouter, RouterLink } from "vue-router";
import { ROUTES } from '@/config/routeConfig';
import { useAuthStore } from "@/store/auth";
import { useHrmStore } from "@/store/hrm";
import { useMobileMenuStore } from '@/store/navigation';
import { useSidebarStore } from '@/store/sidebar';
import { toast } from "vue3-toastify";
import LoginApi from "@/api/auth/LoginApi";
import NotificationBell from '@/components/common/alarm/NotificationBell.vue';
import LanguageSwitcher from '@/components/common/LanguageSwitcher.vue';
import { getRolesFrom } from '@/utils/roleUtils';
import { ROLE_GROUPS } from '@/config/roleConfig';
import DOMPurify from 'dompurify';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { HRM_API_BASE_URL, HRM_SERVICE_NAME } from '@/constants';


// 피처 플래그 변수
const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';

/* ─────────── 스토어 & 라우터 ─────────── */
const authStore = useAuthStore();
const hrmStore = useHrmStore();
const sidebarStore = useSidebarStore();
const router = useRouter();
const mobileMenu = useMobileMenuStore();  

/* ─────────── Role 확인 유틸 ─────────── */
const roles = computed(() => authStore.roles);
const isLocaltest = computed(() =>
  String(import.meta.env.VITE_LOCALTEST ?? import.meta.env.VITE_APP_LOCALTEST) ===
  "true"
);

/**
 * 프로필 이미지 URL 변환 (게이트웨이 경로 포함)
 */
const profileImageUrl = computed(() => {
  const url = authStore.getUserProfileImgUrl;
  if (!url) return '';
  return getImagePreviewUrl(url, HRM_API_BASE_URL, HRM_SERVICE_NAME);
});

/**
 * 현재 테마 상태
 */
const currentTheme = ref(document.documentElement.getAttribute('data-theme') || 'light');

/**
 * 테마 변경 감지
 */
const themeObserver = new MutationObserver(() => {
  const theme = document.documentElement.getAttribute('data-theme') || 'light';
  if (currentTheme.value !== theme) {
    currentTheme.value = theme;
  }
});

/**
 * 로고 이미지 경로 (테마별)
 */
const logoDesktopSrc = computed(() => {
  return currentTheme.value === 'dark' 
    ? '/img/common/logo/centrally-logo.png'
    : '/img/common/logo/centrally-logo_2.png';
});

const logoMobileSrc = computed(() => {
  return currentTheme.value === 'dark'
    ? '/img/common/logo/centrally-logo2.png'
    : '/img/common/logo/centrally-logo2_2.png';
});

const logoCompactSrc = computed(() => {
  return currentTheme.value === 'dark'
    ? '/img/common/logo/centrally_logo_4.png'
    : '/img/common/logo/centrally_logo_3.png';
});

onMounted(() => {
  // 초기 테마 설정
  currentTheme.value = document.documentElement.getAttribute('data-theme') || 'light';
  // 테마 변경 감지 시작
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme']
  });
});

onBeforeUnmount(() => {
  themeObserver.disconnect();
});

/**
 * 로고 클릭 → 모든 모바일 레이어 닫기 & 홈으로 이동
 */
function handleLogoClick () {
  mobileMenu.reset();                 // 햄버거 & 시트 전부 닫기
  
  sidebarStore.selectWorkspace('home'); // 사이드바를 홈으로 전환
}

function canShow(roleArr = []) {
  if (roleArr.length === 0) return true;
  if (isLocaltest.value) return true;
  if (roles.value.includes("ROLE_GATE_SYSTEM")) return true;
  return roleArr.some((r) => roles.value.includes(r));
}

/* ─────────── 메뉴 데이터 ─────────── */
const baseMenus = [
  {
    key: "management",
    label: "사원 관리",
    roles: getRolesFrom(ROLE_GROUPS.HRM_EMPLOYEE),
    // 사원관리 메뉴용 안내 문구 (메뉴 컬럼 2개 이하)
    intro:`<strong>인사 관리 시스템</strong><br>
    ☑ 사용자 정보 및 권한 관리<br>
    ☑ 조직도 및 부서/팀 설정<br>
    ☑ 임직원 실적 관리`,
    sub:[
      { group:"사용자·권한",
        items:[
          { to: ROUTES.MANAGEMENT.USER_MANAGEMENT,   label:"사용자 관리", icon:"ri-user-settings-line", roles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER) },
          { to: ROUTES.MANAGEMENT.USER_PERMISSIONS,  label:"권한 부여",   icon:"ri-key-2-line",   roles: getRolesFrom(ROLE_GROUPS.HRM_ASSISTANT_MANAGER) },
        ]},
      { group:"조직",
        items:[
          { to: ROUTES.MANAGEMENT.DEPT_TEAM_MANAGER, label:"부서/팀 관리", icon:"ri-building-line", roles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER) },
          { to: ROUTES.MANAGEMENT.ORG_DIRECTORY,     label:"조직도",      icon:"ri-group-line",       roles: getRolesFrom(ROLE_GROUPS.HRM_EMPLOYEE) },
        ]},
      { group:"실적",
        items:[
          { to: ROUTES.MANAGEMENT.PERFORMANCE_MANAGEMENT, label:"실적 관리", icon:"ri-bar-chart-2-line", roles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER) },
        ]},
      { group:"가입 승인",
        items:[
          { to: ROUTES.AUTH.TEMP_USER_APPROVALS, label:"임시 가입자 승인", icon:"ri-user-add-line", roles: getRolesFrom(ROLE_GROUPS.HRM_ASSISTANT_MANAGER) },
        ]},
    ]
  },




  {
    key: "system",
    label: "시스템",
    roles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
    intro:`<strong>System Admin</strong><br>
      ☑ 운영 로그 모니터링<br>
      ☑ 통계 · 대시보드 설정<br>
      ☑ 플랫폼 전역 환경 관리`,
    sub: [
      {
        to: ROUTES.SYSTEM.ACTIVITY_LOG,      label:"이력 관리",    icon:"ri-clipboard-line",
        roles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
      },
      {
        to: ROUTES.SYSTEM.STATISTICS_SCREEN, label:"대시보드 관리",icon:"ri-dashboard-3-line",
        roles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
      },
      {
        to: ROUTES.SYSTEM.KAFKA_OUTBOX, label:"Kafka Outbox", icon:"ri-stack-line",
        roles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
      },
    ],
  },
  /* {
    key: "systemtest",
    label: "테스트",
    roles: ["ROLE_GATE_SYSTEM"],
    sub: [
      {
        to: "/system/component-test",
        label: "테스트 컴포넌트",
        roles: ["ROLE_GATE_SYSTEM"],
      },
    ],
  }, */
];

// 영수증 기능이 활성화된 경우에만 메뉴 데이터에 추가
if (isReceiptEnabled) {
  baseMenus.splice(1, 0, { // 공지사항 뒤(인덱스 1)에 영수증 메뉴 삽입
    key: "receipt",
    label: "영수증",
    roles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
    // 영수증 메뉴용 안내 문구 (메뉴 컬럼 2개 이하)
    intro:`<strong>경비 처리의 모든 것</strong><br>
    ☑ 영수증 제출부터 정산까지<br>
    ☑ 개인/팀 경비 처리<br>
    ☑ 효율적인 비용 관리`,
    sub: [
      { group:"등록자",
        items:[
          { to: ROUTES.RECEIPT.SUBMISSION,       label:"영수증 등록",          icon:"ri-file-add-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR) },
          { to: ROUTES.RECEIPT.PERSONAL_HISTORY, label:"신청 내역 조회",       icon:"ri-file-list-3-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR) },
          { to: ROUTES.RECEIPT.ANNUAL_SUMMARY,   label:"년도별 요약",          icon:"ri-calendar-2-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR) },
        ]},
      { group:"결재자",
        items:[
          { to: ROUTES.RECEIPT.APPROVAL_OVERVIEW,    label:"결재 신청 현황", icon:"ri-survey-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER) },
          { to: ROUTES.RECEIPT.CONCURRENCE_OVERVIEW, label:"합의 신청 현황", icon:"ri-hand-coin-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER) },
          { to: ROUTES.RECEIPT.HISTORY,                      label:"결재·합의 내역", icon:"ri-history-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER) },
        ]},
      { group:"대리결재자",
        items:[
          { to: ROUTES.RECEIPT.DELEGATE_APPROVAL_OVERVIEW,  label:"대리 결재 신청 현황", icon:"ri-user-shared-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER) },
          { to: ROUTES.RECEIPT.DELEGATE_CONCURRENCE_OVERVIEW, label:"대리 합의 신청 현황", icon:"ri-user-follow-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER) },
          { to: ROUTES.RECEIPT.DELEGATE_HISTORY, label:"대리 결재·합의 내역", icon:"ri-history-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER) },
        ]},
      { group:"검수자",
        items:[
          { to: ROUTES.RECEIPT.APPROVAL_SUMMARY, label:"승인(마감) 현황", icon:"ri-checkbox-circle-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR) },
          { to: ROUTES.RECEIPT.REPORT_FOR_CEO,   label:"제출용 요약(보고용)",   icon:"ri-file-chart-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR) },
          { to: ROUTES.RECEIPT.CLOSURE,          label:"결재 마감",     icon:"ri-lock-line",
            roles: getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR) },
        ]},
      { group:"관리자",
        items:[
          { to: ROUTES.RECEIPT.MANAGEMENT,      label:"내역 관리",  icon:"ri-database-2-line",  roles: getRolesFrom(ROLE_GROUPS.RECEIPT_MANAGER) },
          { to: ROUTES.RECEIPT.META_MANAGEMENT, label:"설정 관리",  icon:"ri-settings-3-line",  roles: getRolesFrom(ROLE_GROUPS.RECEIPT_MANAGER) },
        ]}
    ]
  });
}

const allMenus = baseMenus;

/* 보이는(필터링된) 메뉴 / 서브메뉴 */
const visibleMenus = computed(() => allMenus.filter((m) => canShow(m.roles)));
const visibleSubs = (subs) => subs.filter((s) => canShow(s.roles));

// 영수증 전용: 그룹 내부도 필터링
const visibleReceiptGroups = (groups) =>
  groups
    .map(g => ({ group: g.group, items: g.items.filter(i => canShow(i.roles)) }))
    .filter(g => g.items.length > 0);
// 인적관리 전용: 그룹 내부도 필터링
const visibleMngGroups = (groups)=>
  groups
    .map(g=>({group:g.group,items:g.items.filter(i=>canShow(i.roles))}))
    .filter(g=>g.items.length);

// 공지사항 전용: 그룹 내부도 필터링
const noticeIntro = computed(() =>
  hoveredMenu.value?.key==='notice' ? hoveredMenu.value.intro : '');
const sanitizedNoticeIntro = computed(() => DOMPurify.sanitize(noticeIntro.value));
const sanitizedHoveredIntro = computed(() => DOMPurify.sanitize(hoveredMenu.value?.intro ?? ''));

// 현재 메뉴의 표시 컬럼 수를 계산
const visibleColumnCount = computed(() => {
  if (!hoveredMenu.value) return 0;
  const key = hoveredMenu.value.key;
  const sub = hoveredMenu.value.sub;

  if (key === 'receipt') {
    return visibleReceiptGroups(sub).length;
  }
  if (key === 'management') {
    return visibleMngGroups(sub).length;
  }
  // notice, system과 같은 단일 컬럼 메뉴들은 1을 반환
  if (key === 'notice' || key === 'system' || (sub && !sub[0]?.group) ) {
    return 1;
  }
  return 0;
});

// 영수증/시스템 권한 중 하나라도 있는지 체크
const canShowNotification = computed(() => {
  const userRoles = roles.value || [];
  return (
    isReceiptEnabled &&
    (userRoles.includes("ROLE_RECEIPT_APPROVER") || userRoles.includes("ROLE_GATE_SYSTEM"))
  );
});

/* ─────────── 메가메뉴 호버 상태 ─────────── */
const hovered = ref(null);
let hideTimer; // 메가메뉴 지연 해제용 타이머

// 지금 호버 중인 메인 메뉴 객체 (없으면 null)
const hoveredMenu = computed(() =>
  visibleMenus.value.find(m => m.key === hovered.value) || null
);

/**
 * 메뉴 호버
 */
function setHover(key) {
  cancelHide()
  hovered.value = key
}

// 컬럼 수에만 의존하는 단순하고 일관된 로직으로 변경
const megaMenuStyle = computed(() => {
  if (!hoveredMenu.value) return {};
  
  // 현재 호버 중인 메뉴 요소
  const menuEl = document.querySelector(`.menu-box[data-key="${hovered.value}"]`);
  const groupEl = document.querySelector('.menu-group');
  let leftPos = 0;

  if (menuEl) {
   const rect = menuEl.getBoundingClientRect();
   // 화면 스크롤 고려한 절대좌표
   const groupRect = groupEl?.getBoundingClientRect?.() ?? { left: 0 };
   // 부모(.menu-group) 기준 좌표로 변환: “메뉴 텍스트 시작점”에 붙이기
   leftPos = rect.left - groupRect.left;
   // 필요하면 아주 살짝 오른쪽 미세조정 (아이콘/패딩 보정)
   // leftPos += 8;
  }

  const baseStyle = {
    position: 'absolute',
    left: `${leftPos}px`,
    transform: 'none',
  };

  // 표시되는 컬럼 수가 2개 이하면 최소 너비를 600px로 설정
  if (visibleColumnCount.value <= 2) {
    return { ...baseStyle, minWidth: '600px' };
  }
  // 3개 이상이면 유연하게 너비 조절
  return { ...baseStyle, minWidth: '0px' };
});

/** ms 후 숨김 예약 */
/** “메뉴 → 메가드롭다운” 사이의 마우스 이동 틈새에서 깜빡임을 막아 주는 ‘완충 지연’ 역할 */
function scheduleHide() {
  hideTimer = setTimeout(() => (hovered.value = null), 0);
}

/** 예약된 숨김 취소 */
function cancelHide() {
  clearTimeout(hideTimer);
}

function closeMega () {
  hovered.value = null                      // 메가드롭다운 닫기
  removeOutsideClickListener()              // 외부-클릭 리스너도 해제
}

/* ─────────── 터치 기기 지원 :  click 으로 열고 다시 click 하면 닫기 ─────────── */
function toggleByClick(key) {
  // 터치 시 auto-hide 타이머 제거
  cancelHide();
  // 동일 메뉴를 누르면 닫기, 다른 메뉴를 누르면 교체
  hovered.value = (hovered.value === key ? null : key);

  // 문서 전체(바깥)를 한 번 터치하면 닫히도록 리스너 등록
  addOutsideClickListener();
}

let outsideListener;                             // 한 번만 설치
function addOutsideClickListener() {
  if (outsideListener) return;                   // 이미 있음
  outsideListener = (e) => {
    if (!e.target.closest('.menu-group')) {      // 메뉴 바깥
      hovered.value = null;
      removeOutsideClickListener();
    }
  };
  document.addEventListener('click', outsideListener);
}
function removeOutsideClickListener() {
  if (!outsideListener) return;
  document.removeEventListener('click', outsideListener);
  outsideListener = null;
}
onBeforeUnmount(removeOutsideClickListener);

 

/* ─────────── 로그아웃 ─────────── */
/**
 * 로그아웃 처리
 */
async function doLogout() {
  // 로그아웃 전에 테마를 light로 즉시 변경 (깜빡임 방지)
  document.documentElement.setAttribute('data-theme', 'light');
  document.body.setAttribute('data-theme', 'light');
  
  // HRM 프로필 초기화
  hrmStore.clearMyProfile();
  
  const res = await LoginApi.logout();
  if (res.status !== 200) toast.error("로그아웃 실패");
  authStore.logout();
  router.push(ROUTES.HOME + "?logout=success");
}
</script>

<style scoped>
/* ───── 헤더 기본 ───── */
.navbar {
  height: 65px;
  background: var(--theme-header-bg);
  /* box-shadow: 0 2px 5px rgba(0, 0, 0, 0.06); */
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.06);
  position: fixed !important;
  inset: 0 0 auto 0;
  width: 100%;
  min-width: 360px !important;
  padding: 0;
  z-index: 1015;
}
.navbar-inner {
  width: 100%;
  height: 100%;
  /* max-width: 1200px; */
  /* margin: 0 auto; */
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

/* 로고 */
.logo-desktop {
  /* width: 45px; */
  width: 170px;
}
.logo-mobile {
  /* width: 45px; */
  display: none;
  width: 170px;
}
.logo-compact {
  display: none;
}

/* ───── 메인 메뉴 ───── */
.menu-group {
  flex: 1;
  display: flex;
  justify-content: flex-start;
  position: relative;
}
.desktop-nav {
  height: 100%;
}
.main-nav {
  display: flex;
  height: 100%;
}
.menu-box {
  height: 100%;
  display: flex;
  align-items: center;
}
.menu-box > a {
  display: block;
  height: 100%;
  line-height: 65px;
  padding: 0 22px;
  margin: 0 10px;
  color: var(--theme-text-secondary);
  font-weight: 700;
  font-size: 15px;
  text-decoration: none;
  position:relative;
  z-index: 1016;
}
/* 변경: 색을 더 짙게(거의 검정) + 굵게 */
/* 글자 강조는 유지 */
.menu-box:hover > a{
  color: var(--theme-text-primary);
  font-weight:800;
}
/* 헤더 메뉴 아래선 */
.menu-box > a::after{
  content:'';
  position:absolute;
  left:0;
  bottom:0;
  width:100%;
  height:1px;               /* 선 두께 */
  background:#8f8f8f;
  transform:scaleX(0);      /* 완전히 접힘 → 안 보임 */
  transform-origin:center bottom;
  transition:transform .15s ease;
}
/* ② 호버 시 scaleX 를 1 로 펼쳐서 선 보이기 */
.menu-box:hover > a::after{
  transform:scaleX(1);
}
.menu-main-ico{
   font-size:16px;
   margin-right:3px;
   vertical-align:-0.5px;
   color:#888;
}

/* ───── 메가 드롭다운 ───── */
.mega-all {
  overflow:hidden;
  position: absolute;
  top: 65px;
  width: max-content;
  max-width: calc(100% - 80px);
  /* min-width: 600px; */
  min-width: 0px;
  min-height: 200px;
  background: var(--theme-header-bg);
  /* border: 1px solid #e5e5e5; */
  /* box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08); */
  /* border: 1px solid #dadde1; */
  box-shadow:
      0  6px 18px  rgba(0,0,0,.08),           /* 전체적으로 부드러운 첫 그림자 */
      0  14px 36px  rgba(0,0,0,.04);           /* 아래쪽이 조금 더 진한 두 번째 그림자 */
  /* border-radius: 6px; */
  padding: 30px 40px 40px 40px;
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 15px;
  z-index: 999;
}
.mega-col {
  position: relative;
  padding-left: 20px;
}
.mega-col:first-child {
  padding-left: 0;
}
/*
메가 메뉴에서 메뉴들 사이 선
.mega-col::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 1px;
  background: #e5e5e5;
}
.mega-col:first-child::before {
  display: none;
} 
 */
.mega-title {
  padding:6px 18px 6px 10px;
  font-size: 0.65rem;
  margin-bottom: 15px;
  color: var(--theme-text-secondary);
  font-weight: 700;
}
.mega-link {
  /* display: block;
  padding: 6px 0;
  font-size: 0.83rem;
  color: #333;
  text-decoration: none;
  margin-bottom: 10px;
  font-weight: 800; */
  display:block;
  /* 좌우에 살짝 여유를 주고  호버-백그라운드가 잘 보이도록  line-height를 넉넉히 */
  padding:6px 18px 6px 10px;
  margin-bottom:8px;
  font-size:.83rem;
  font-weight:800;
  color: var(--theme-text-primary);
  text-decoration:none;
  border-radius:6px;          /* ← 둥근 직사각형 모양 준비 */
}
.mega-link:hover {
  /* color: #ff9f43;
  text-decoration: none; */
  background: var(--theme-bg-secondary);         /* 아주 연한 회색 배경 */
  color: var(--theme-text-primary);                 /* 글자색은 그대로 */
}

:deep(.mega-enter-active) {
  transition: opacity .1s ease, transform .1s ease;
  transform-origin: top;
}
:deep(.mega-leave-active) {
  transition: none !important;
}
/* 등장 : 위 10px + 0.8배 축소 + 완전 투명 */
:deep(.mega-enter-from){
  opacity:0;
  transform: translateY(-10px) scaleY(.9);
}
/* 등장 종료(정상 위치·크기·불투명) */
:deep(.mega-enter-to){
  opacity:1 !important;
  transform: translateY(0) scaleY(1) !important;
}
/* 퇴장 : 그대로 있다가 불투명도만 0 */
:deep(.mega-leave-from){ opacity:1; }
:deep(.mega-leave-to)  { opacity:0; }


/* 메가 드롭 사라지는 시간 */
:deep(.fade-leave-active) {
  transition-duration: 0ms !important;   /* 기본 300ms → 0ms */
}
/* 공지 사항용 : 왼쪽 링크 + 오른쪽 소개 */
.mega-all.notice{      /* flex 그대로, 두 칸 */
  gap:60px;
}
.notice-intro {
  align-self: center;
  max-width:280px;
  font-size:.8rem;
  line-height:3;
  color: var(--theme-text-muted);
  padding-left:20px;
  padding-right:40px;
  border-left:1px solid var(--theme-border);
  margin-left: 45px;
}
.notice-intro strong {
  display:block;
  font-size:.9rem;
  margin-bottom:.6rem;
  color:#004497;
}

/* 확성기 아이콘 */
.megaphone-icon {
  margin-left: 0 !important;
  display: flex;
  align-items: center;
  align-self: flex-end;
  padding-bottom: 5px;
  font-weight: 900 !important;
  color: #888;
}

.megaphone-icon i {
  font-size: 30px;
}

/* ───── 유저 영역 ───── */
.user-box {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0;
  font-size: 0.875rem;
}

.user-name {
  color: #004497;
  font-weight: 600;
  text-decoration: none;
}
.user-name:hover {
  color: #ff9f43;
}
.divider {
  color: #888;
}

/* ───── 애니메이션 ───── */

/* -------- user-box 아이콘용 -------- */
.icon {
  width: 22px;
  height: 22px;
  vertical-align: middle;
}

/* 새 라운드-스퀘어 버튼 디자인 */
.icon-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;              /* 둥근 사각형 */
  background: var(--theme-bg-tertiary);              /* 연한 회색 */
  border: 1px solid #e5e7eb;        /* 테두리 */
  color: #6b7280;                   /* 아이콘 회색 */
  transition: all .2s ease;
  overflow: hidden;                 /* 내부 이미지가 버튼 영역 넘지 않도록 */
  padding: 0;                       /* 이미지가 100% 채우도록 */
}
.icon-btn:hover {
  background: #2563eb;              /* 파랑 */
  border-color: #2563eb;
  color: #fff;                      /* 흰색 아이콘 */
  transform: translateY(-1px) scale(1.05);
  box-shadow: 0 4px 12px rgba(37,99,235,.3);
}
.icon-btn:active {
  transform: translateY(0) scale(.98);
  box-shadow: 0 2px 6px rgba(37,99,235,.2);
}

.menu-ico{
  font-size:14px;        /* 작게  */
  margin-right:6px;      /* 텍스트와 간격 */
  color:#b0b0b0;         /* 연한 회색     */
  vertical-align:middle;
}
/* ───── 반응형 ───── */
@media (max-width: 1100px) {
  .menu-box > a {
    padding: 0 15px;
  }
}

/* ───── 반응형 ───── */
@media (max-width: 1300px) {
  /* ───── 유저 영역 ───── */
  .user-box {
    padding: 0px 0px 0px 0px;
    gap: 7px;
  }
  
  .megaphone-icon {
    margin-left: auto;
    padding-bottom: 10px;
  }

  .megaphone-icon i {
    font-size: 25px;
  }
  
  /* 로고 */
  .logo-desktop {
    display: none;
    width: 170px;
  }
  .logo-mobile {
    display: block;
    width: 80px;
  }
  /* .logo {
    display: none;
  } */
  /* .menu-group {
    justify-content: flex-start;
  } */
  .menu-box > a {
    padding: 0 25px;
  }
  .mega-col {
    padding-left: 10px;
  }
  .mega-title {
    font-size: 0.75rem;
  }
  .mega-link {
    font-size: 0.7rem;
  }
}

/* 650px↓ : 메뉴 숨김, 이름/로그아웃만 */
@media (max-width: 1300px) {
  .icon {
    width: 20px;
    height: 20px;
  }
  .icon-btn {
    width: 35px;
    height: 35px;
  }
  .user-btn {
    font-size: 1.55rem; /* 아이콘 크기 */
  }
  .navbar-inner {
    padding: 0 18px;
    gap: 5px;
  }
  .desktop-nav {
    display: none;
  }
  
}

/* 버튼 내부 이미지: 버튼과 동일한 라운드 사각형 */
.icon-btn .header-avatar{
  width:100%;
  height:100%;
  border-radius: 10px;
  object-fit:cover;
}

/* 650px 이하에서 언어 설정 숨김 */
@media (max-width: 500px) {
  .logo-mobile {
    display: none;
  }

  .logo-compact {
    display: block;
    width: 30px !important;
    margin-right: 3px;
  }
}
</style>
