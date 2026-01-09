<template>
  <!-- ───────── 풀 메뉴(≡) ───────── -->
  <aside class="drawer" @click.self="closeMenu" :class="{ open: menu }">
    <div class="drawer-panel" :class="[ { open: menu }, themeClass ]">
      <!-- ① 상단 프로필 헤더 (첫 줄) -->
      <div class="profile-header profile-row">
        <div class="profile-meta">
          <div class="name">{{ user?.name || $t('common.mobileDrawer.nameUnspecified') }}</div>
          <div class="email">{{ user?.email || $t('common.mobileDrawer.emailUnspecified') }}</div>
          <div class="position-row">
            <span class="position">{{ user?.position || $t('common.mobileDrawer.positionUnspecified') }}</span>
            <!-- 테마 토글 아이콘 (직책 옆, 1300px 이하 전용) -->
            <button 
              class="theme-icon-btn-inline mobile-only" 
              @click="toggleTheme"
              :title="currentTheme === 'light' ? $t('common.mobileDrawer.theme.switchToDark') : $t('common.mobileDrawer.theme.switchToLight')"
            >
              <i v-if="currentTheme === 'light'" class="ri-moon-line"></i>
              <i v-else class="ri-sun-line"></i>
            </button>
          </div>
        </div>
        <RouterLink :to="ROUTES.USER.USER_INFORMATION" class="avatar-link" @click="closeMenu">
          <div class="avatar" :class="{ placeholder: !user?.photoUrl }">
            <img v-if="user?.photoUrl" :src="user.photoUrl" :alt="$t('common.mobileDrawer.profileAlt')" />
            <i v-else class="ri-user-3-fill"></i>
          </div>
        </RouterLink>
      </div>

      <!-- ② 카테고리 탭 (둘째 줄) -->
      <div class="tabs category-row">
        <button
          v-for="c in tabCats"
          :key="c.key"
          class="tab"
          :class="{ active: c.key === cat }"
          @click="cat = c.key"
          type="button"
        >
          <i :class="['tab-icon', c.icon || 'ri-apps-2-line', iconColorClass(c.key)]"></i>
          <span class="tab-label">{{ getCategoryLabel(c.key) }}</span>
        </button>
      </div>

      <!-- 탭/리스트 구분선 -->
      <div class="divider"></div>

      <!-- ③ 리스트: 선택된 카테고리 항목 (셋째 줄) -->
      <transition-group
        name="submenu"
        tag="div"
        class="items content-row"
        style="position: relative"
        :style="itemsOffsetStyle"
        @touchstart.passive="handleTouchStart"
        @touchmove.passive="handleTouchMove"
        @touchend="handleTouchEnd"
        @touchcancel="handleTouchEnd"
      >
        <!-- 그룹 구조가 있을 때: 섹션 카드 + 헤더(아이콘/포인트 컬러) + 리스트 -->
        <template v-if="effectiveIsGrouped">
          <div
            class="group group-card"
            v-for="grp in effectiveGroups"
            :key="grp.group"
            :class="groupClass(grp.group)"
          >
            <div class="group-header">
              <!-- <i :class="['group-icon', groupIcon(grp.group)]"></i> -->
              <span class="group-title-text">{{ $t(getCategoryI18nKey(grp.group)) }}</span>
            </div>
            <div class="group-list">
              <template v-for="i in grp.items" :key="i.label">
                <router-link :to="i.to" class="item" @click="closeMenu">
                  <i :class="['item-icon', itemIcon(i)]"></i>
                  <span class="item-text">{{ $t(i.label) }}</span>
                </router-link>
              </template>
            </div>
          </div>
          <div v-if="!effectiveGroups.length" class="empty">{{ $t('common.mobileDrawer.noMenu') }}</div>
        </template>
        <!-- 그룹 구조가 없을 때: 평면 리스트 -->
        <template v-else>
          <template v-for="i in currentItems" :key="i.label">
            <router-link :to="i.to" class="item" @click="closeMenu">
              <i :class="['item-icon', itemIcon(i)]"></i>
              <span class="item-text">{{ $t(i.label) }}</span>
            </router-link>
          </template>
          <div v-if="!currentItems.length" class="empty">{{ $t('common.mobileDrawer.noMenu') }}</div>
        </template>
      </transition-group>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { RouterLink } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useMobileMenuStore } from '@/store/navigation';
import { useAuthStore } from '@/store/auth';
import { useHrmStore } from '@/store/hrm';
import { ROUTES } from '@/config/routeConfig';
import { getAllMenus } from '@/config/menuConfig';
import StylesApi from '@/api/hrm/StylesApi';
import { STYLE_CATEGORY, INFO_MOBILE_STYLE } from '@/constants';
import { toast } from 'vue3-toastify';

const { t } = useI18n();

const props = defineProps({
  menu: { type: Boolean, required: true },
  cats: { type: Array, required: true },
  user: {
    type: Object,
    required: false, // 부모에서 없을 수도 있어 안전 처리
    default: () => ({ name: '', email: '', position: '', photoUrl: '' }),
  },
});

const mobileMenu = useMobileMenuStore();
const authStore = useAuthStore();
const hrmStore = useHrmStore();

/**
 * menuConfig의 모든 메뉴를 path를 키로 하는 맵으로 생성
 */
const menuByPath = computed(() => {
  const allMenus = getAllMenus();
  const map = new Map();
  for (const menu of allMenus) {
    map.set(menu.path, menu);
  }
  return map;
});

/**
 * 카테고리 키를 i18n 라벨로 변환
 */
function getCategoryLabel(key) {
  switch (key) {
    case 'info': return t('common.sidebar.workspace.info');
    case 'receipt': return t('common.sidebar.workspace.receipt');
    case 'management': return t('common.sidebar.workspace.management');
    case 'user': return t('common.sidebar.workspace.user');
    case 'system': return t('common.sidebar.workspace.system');
    default: return key || '';
  }
}

/**
 * 카테고리 한글 문자열을 i18n 키로 변환
 */
function getCategoryI18nKey(category) {
  const categoryMap = {
    '일정 관리': 'menu.category.scheduleManagement',
    '게시판': 'menu.category.board',
    '가이드': 'menu.category.guide',
    '등록자': 'menu.category.registrar',
    '결재자': 'menu.category.approver',
    '대리결재자': 'menu.category.proxy',
    '검수자': 'menu.category.inspector',
    '관리자': 'menu.category.manager',
    '사용자·권한': 'menu.category.userAuth',
    '조직': 'menu.category.organization',
    '실적': 'menu.category.performance',
    '가입 승인': 'menu.category.approval',
    '개인정보': 'menu.category.personal',
    '시스템 관리': 'menu.category.systemAdmin',
    '개인': 'menu.category.personal',
    '운영': 'menu.category.systemAdmin',
    '대시보드': 'menu.category.systemAdmin',
    '메시징': 'menu.category.systemAdmin',
    '공지': 'menu.category.board',
    '기타': 'menu.category.board',
  };
  return categoryMap[category] || category;
}

/**
 * 현재 선택된 카테고리 키
 */
const cat = ref(props.cats[0]?.key);

watch(
  () => props.cats,
  (nv) => {
    if (!nv?.length) {
      cat.value = undefined;
      return;
    }
    if (!nv.some((c) => c.key === cat.value)) {
      cat.value = nv[0]?.key;
    }
  },
  { immediate: true },
);

/**
 * 탭에 표시할 카테고리 (최대 5개)
 */
const tabCats = computed(() => (props.cats || []).slice(0, 5));

/**
 * 카테고리별 테마 클래스 (탭/헤더 포인트 컬러에 사용)
 */
const themeClass = computed(() => {
  switch (currentCatKey.value) {
    case 'notice': return 'theme-notice';
    case 'receipt': return 'theme-receipt';
    case 'management': return 'theme-management';
    case 'user': return 'theme-user';
    case 'system': return 'theme-system';
    default: return '';
  }
});

/**
 * 현재 선택된 카테고리 객체/키/원본 서브 데이터
 */
const currentCatObj = computed(() => props.cats.find((c) => c.key === cat.value) || {});
const currentCatKey = computed(() => currentCatObj.value?.key || '');
const currentRaw = computed(() => currentCatObj.value?.sub || []);

/**
 * 그룹 여부 판단: 첫 요소에 group 키가 있으면 그룹 구조로 간주
 */
const isGrouped = computed(() => Array.isArray(currentRaw.value) && currentRaw.value[0]?.group);

/**
 * 그룹 구조일 때: [{ group, items[] }]
 */
const currentGroups = computed(() => (isGrouped.value ? currentRaw.value : []));

/**
 * 평면 구조일 때: item 배열
 */
const currentItems = computed(() => (isGrouped.value ? [] : currentRaw.value));

/**
 * 메뉴 항목의 그룹 결정 (menuConfig의 category 우선 사용)
 * @param {string} catKey 현재 카테고리 키
 * @param {object} item 메뉴 항목
 * @returns {string} 그룹명
 * 
 * NOTE: 반환값은 한글이지만 백엔드 DB의 category 값과 일치해야 합니다.
 * 표시 시 i18n 처리가 필요하지만, 백엔드 호환성 때문에 한글 유지합니다.
 */
function categorizeByCategory(catKey, item) {
  // 1순위: menuConfig에서 category 찾기
  const menuData = menuByPath.value.get(item?.to);
  if (menuData?.category) return menuData.category;

  // 2순위: 경로 기반 fallback
  const to = String(item?.to || '').toLowerCase();
  
  switch (catKey) {
    case 'receipt':
      if (to.includes('receipt-submission') || to.includes('personal-receipt-history') || to.includes('annual-receipt-summary')) return '등록자';
      if (to.includes('delegate-')) return '대리결재자';
      if (to.includes('approval-summary') || to.includes('report-for-ceo') || to.includes('closure')) return '검수자';
      if (to.includes('receipt-management') || to.includes('receipt-meta-management')) return '관리자';
      if (to.includes('approval-request-overview') || to.includes('concurrence-request-overview') || (to.includes('receipt-history') && !to.includes('delegate'))) return '결재자';
      break;
    
    case 'management':
      if (to.includes('user-management') || to.includes('user-permissions')) return '사용자·권한';
      if (to.includes('dept-team-manager') || to.includes('org-directory')) return '조직';
      if (to.includes('performance-management')) return '실적';
      if (to.includes('temp-user-approvals')) return '가입 승인';
      break;
    
    case 'system':
      if (to.includes('activity-log')) return '운영';
      if (to.includes('statistics-screen')) return '대시보드';
      if (to.includes('kafka-outbox')) return '메시징';
      break;
    
    case 'notice':
    case 'info':
      if (to.includes('schedule')) return '일정 관리';
      if (to.includes('notice') || to.includes('resource') || to.includes('community')) return '게시판';
      if (to.includes('guide')) return '가이드';
      break;
    
    case 'user':
      return '개인';
  }
  
  return '기타';
}

/**
 * 원본이 그룹이 아닐 때 자동 그룹핑 결과 생성 (menuConfig category 기반)
 */
const autoGroups = computed(() => {
  if (isGrouped.value) return [];
  const byGroup = new Map();
  for (const it of currentItems.value) {
    const g = categorizeByCategory(currentCatKey.value, it);
    if (!byGroup.has(g)) byGroup.set(g, []);
    byGroup.get(g).push(it);
  }
  
  // 그룹 순서 정의 (INFO 카테고리의 경우 일정 관리가 첫 번째)
  const groupOrder = ['일정 관리', '게시판', '가이드'];
  
  return Array.from(byGroup.entries())
    .map(([group, items]) => ({ group, items }))
    .filter((g) => g.items.length)
    .sort((a, b) => {
      const indexA = groupOrder.indexOf(a.group);
      const indexB = groupOrder.indexOf(b.group);
      // 순서에 정의된 그룹은 순서대로, 그 외는 뒤로
      if (indexA !== -1 && indexB !== -1) return indexA - indexB;
      if (indexA !== -1) return -1;
      if (indexB !== -1) return 1;
      return 0; // 둘 다 순서에 없으면 원래 순서 유지
    });
});

/**
 * 렌더링에 사용할 최종 그룹 데이터 및 그룹 여부
 */
const hasAutoGroups = computed(() => autoGroups.value.length > 0);
const effectiveIsGrouped = computed(() => isGrouped.value || hasAutoGroups.value);
const effectiveGroups = computed(() => (isGrouped.value ? currentGroups.value : autoGroups.value));

/**
 * 리스트 상단 오프셋 (탭 레이아웃에서는 0으로 고정)
 */
const itemsOffsetStyle = computed(() => ({ marginTop: '0px' }));

/**
 * 배경(오버레이) 클릭 시 메뉴 닫기
 */
/**
 * 메뉴 닫기
 */
function closeMenu() {
  mobileMenu.closeMenu();
}


/**
 * 오버레이 상태를 히스토리에 저장 (뒤로가기 처리용)
 */
/**
 * 현재 히스토리에 오버레이 상태 한 칸 push
 */
function pushOverlayState() {
  history.pushState({ mobileOverlay: true }, '');
}

/**
 * 타일 클릭 핸들러: action은 router-link 동작을 프로그래매틱하게 수행
 */
function handleTileClick(item) {
  if (item?.to) {
    // RouterLink를 쓰지 않고 이동: 모바일 레이어 상태 정리 후 이동
    closeMenu();
    window.location.hash = '#' + item.to; // 해시 기반 라우팅 안전 이동
  }
}

/**
 * 그룹명에 따른 아이콘 (menuConfig의 category 기반 + fallback)
 */
function groupIcon(groupName) {
  // menuConfig에서 해당 카테고리의 첫 번째 메뉴 아이콘 사용
  const allMenus = getAllMenus();
  const categoryMenu = allMenus.find(m => m.category === groupName);
  if (categoryMenu?.icon) return categoryMenu.icon;

  // fallback: 그룹명 기반
  const g = String(groupName || '').toLowerCase();
  if (g.includes('등록')) return 'ri-file-add-line';
  if (g.includes('결재자') || g.includes('결재')) return 'ri-survey-line';
  if (g.includes('대리')) return 'ri-user-shared-line';
  if (g.includes('검수')) return 'ri-checkbox-circle-line';
  if (g.includes('관리')) return 'ri-settings-3-line';
  if (g.includes('사용자') || g.includes('권한')) return 'ri-user-settings-line';
  if (g.includes('조직')) return 'ri-building-line';
  if (g.includes('실적')) return 'ri-bar-chart-2-line';
  if (g.includes('가입')) return 'ri-user-add-line';
  if (g.includes('시스템')) return 'ri-settings-3-line';
  return 'ri-folder-2-line';
}
function groupClass(groupName) {
  const g = String(groupName || '').toLowerCase();
  return {
    'grp-registrar': g.includes('등록'),
    'grp-approver': g.includes('결재자'),
    'grp-delegate': g.includes('대리'),
    'grp-inspector': g.includes('검수'),
    'grp-manager': g.includes('관리'),
  };
}

/**
 * 탭 아이콘 색상 클래스 (카테고리별 고정 색상)
 */
function iconColorClass(key) {
  switch (key) {
    case 'notice': return 'icon-notice';  // 연파랑
    case 'receipt': return 'icon-receipt'; // 파랑
    case 'management': return 'icon-management'; // 초록
    case 'user': return 'icon-user'; // 보라
    case 'system': return 'icon-system'; // 주황
    default: return 'icon-neutral';
  }
}

/**
 * 하위 항목 아이콘 매핑 (menuConfig 우선, fallback은 경로 패턴)
 */
function itemIcon(item) {
  // 1순위: menuConfig에서 아이콘 찾기
  const menuData = menuByPath.value.get(item?.to);
  if (menuData?.icon) return menuData.icon;

  // 2순위: 경로 패턴 기반 간소화된 fallback
  const to = String(item?.to || '').toLowerCase();
  
  // 카테고리별 대표 아이콘
  if (to.includes('/notice')) return 'ri-megaphone-line';
  if (to.includes('/guide')) return 'ri-guide-line';
  if (to.includes('/receipt')) return 'ri-receipt-line';
  if (to.includes('/hrm/user')) return 'ri-user-line';
  if (to.includes('/hrm/dept') || to.includes('/hrm/org')) return 'ri-building-line';
  if (to.includes('/hrm/performance')) return 'ri-bar-chart-2-line';
  if (to.includes('/system')) return 'ri-settings-3-line';
  if (to.includes('/auth')) return 'ri-user-add-line';

  return 'ri-arrow-right-s-line';
}

/**
 * 현재 테마
 */
const currentTheme = computed(() => {
  return hrmStore.getMyProfile?.infoMobileStyle || INFO_MOBILE_STYLE.LIGHT;
});

// 스와이프 제스처 계산에 사용하는 상태값들
const touchStartX = ref(0);
const touchCurrentX = ref(0);
const isSwiping = ref(false);
const SWIPE_THRESHOLD = 50;
const touchTargetWidth = ref(0);

/**
 * 테마 라벨
 */
const currentThemeLabel = computed(() => {
  return currentTheme.value === INFO_MOBILE_STYLE.LIGHT ? '라이트 모드' : '다크 모드';
});

/**
 * 테마 토글
 */
async function toggleTheme() {
  const newTheme = currentTheme.value === INFO_MOBILE_STYLE.LIGHT 
    ? INFO_MOBILE_STYLE.DARK 
    : INFO_MOBILE_STYLE.LIGHT;
  
  try {
    await StylesApi.updateUserStyle(authStore.userId, STYLE_CATEGORY.INFO_MOBILE, newTheme);
    // HRM 스토어의 프로필 업데이트
    await hrmStore.loadMyProfileByUserId(authStore.userId);
    toast.success(t('common.mobileDrawer.theme.changed'));
  } catch (error) {
    console.error('테마 변경 실패:', error);
    toast.error(t('common.mobileDrawer.theme.changeFailed'));
  }
}

/**
 * 터치 시작 시 좌표 기록
 * @param {TouchEvent} event
 */
function handleTouchStart(event) {
  const firstTouch = event.touches?.[0];
  touchStartX.value = firstTouch?.clientX ?? 0;
  touchCurrentX.value = touchStartX.value;
  isSwiping.value = true;
  const target = event.currentTarget;
  touchTargetWidth.value = target?.offsetWidth ?? 0;
}

/**
 * 터치 이동 중 현재 좌표 업데이트
 * @param {TouchEvent} event
 */
function handleTouchMove(event) {
  if (!isSwiping.value) return;
  const touch = event.touches?.[0];
  touchCurrentX.value = touch?.clientX ?? touchCurrentX.value;
}

/**
 * 터치 종료 시 스와이프 거리만큼 카테고리 전환
 */
function handleTouchEnd() {
  if (!isSwiping.value) return;
  const deltaX = touchCurrentX.value - touchStartX.value;
  isSwiping.value = false;
  // 메뉴 영역 너비의 절반 이상 드래그해야 카테고리가 전환되도록 보정
  const dynamicThreshold = touchTargetWidth.value > 0 
    ? Math.max(touchTargetWidth.value / 2, SWIPE_THRESHOLD)
    : SWIPE_THRESHOLD;
  if (Math.abs(deltaX) < dynamicThreshold) return;

  const catList = tabCats.value;
  if (!Array.isArray(catList) || !catList.length) return;

  const currentIndex = catList.findIndex((c) => c.key === cat.value);
  if (currentIndex === -1) return;

  if (deltaX < 0 && currentIndex < catList.length - 1) {
    cat.value = catList[currentIndex + 1].key;
    return;
  }

  if (deltaX > 0 && currentIndex > 0) {
    cat.value = catList[currentIndex - 1].key;
  }
}
</script>

<style scoped>
.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between; /* 왼쪽: 이름/직책, 오른쪽: 아바타+테마버튼 */
  gap: 12px;
  padding: 30px 30px 20px 30px;
}

.profile-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  flex: 1;
}

.name {
  font-size: 1rem;
  font-weight: 800;
  color: var(--theme-text-primary);
}

.email {
  font-size: 0.75rem;
  color: var(--theme-text-secondary);
  margin-top: 2px;
}

.position-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 2px;
}

.position {
  font-size: 0.78rem;
  color: var(--theme-text-secondary);
}

.avatar-link {
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border-radius: 12px;
}

.avatar-link:hover {
  transform: scale(1.05);
}

.avatar-link:active {
  transform: scale(0.98);
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  background: var(--theme-bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--theme-border);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar.placeholder i {
  color: #9aa7b2;
  font-size: 1.6rem;
}

/* 테마 아이콘 버튼 (직책 옆 인라인) */
.theme-icon-btn-inline {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 6px;
  background: var(--theme-bg-tertiary);
  border: 1px solid var(--theme-border);
  color: var(--theme-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 0;
  flex-shrink: 0;
}

.theme-icon-btn-inline i {
  font-size: 0.75rem;
}

.theme-icon-btn-inline:hover {
  background: #e0f2fe;
  border-color: #7dd3fc;
  color: #0284c7;
  transform: scale(1.1);
}

.theme-icon-btn-inline:active {
  transform: scale(0.92);
}

.tabs {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 4px;
  padding: 6px 10px 8px 10px;
}
.tab {
  flex: 1 1 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 8px 4px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--theme-text-primary);
  border-radius: 10px;
}
.tab-icon {
  font-size: 1.4rem;
  color: #94a3b8; /* 아이콘은 항상 중립색 */
}
.tab-label {
  font-size: 0.62rem;
  margin-top: 2px;
}
.tab.active {
  color: var(--theme-text-primary);
  background: var(--theme-bg-hover);
}
.tab.active .tab-icon {
  text-shadow: 0 0 0.1px currentColor;
  color: #94a3b8; /* 활성에도 색 고정 */
}

.divider {
  height: 5px;
  background: var(--theme-bg-secondary);
  margin: 4px 0 0px 0;
}

.drawer {
  position: fixed;
  inset: 0;
  padding-bottom: 70px;
  background: rgba(0, 0, 0, 0.3);
  min-width: 360px !important;
  z-index: 1013;
  top: 65px;
  display: flex;
  justify-content: flex-end;
  opacity: 0;
  pointer-events: none;
  transition: transform var(--sidebar-slide), opacity var(--sidebar-slide);
}
.drawer.open {
  background: rgba(0, 0, 0, 0.3);
  opacity: 1;
  pointer-events: auto;
}

.drawer-panel {
  width: 100%;
  max-width: 330px;
  min-width: 360px !important;
  background: var(--theme-bg-main);
  display: flex;
  flex-direction: column; /* 세로로 3개 파트가 쌓이도록 */
  height: 100% !important;
  transform: translateX(100%);
  transition: transform 0.22s ease;
}
.drawer-panel.open {
  transform: translateX(0);
}

/* 카테고리 테마 색상 변수 */
.theme-notice { --theme-color: #00a6ff; --theme-soft-bg: #eaf6ff; --theme-soft-border: #cfe9ff; }
.theme-receipt { --theme-color: #2563eb; --theme-soft-bg: #eef4ff; --theme-soft-border: #d7e3ff; }
.theme-management { --theme-color: #16a34a; --theme-soft-bg: #ecfdf5; --theme-soft-border: #c8f5de; }
.theme-user { --theme-color: #8b5cf6; --theme-soft-bg: #f3efff; --theme-soft-border: #e2d9ff; }
.theme-system { --theme-color: #f59e0b; --theme-soft-bg: #fff7ed; --theme-soft-border: #ffe5c2; }

/* 하위 메뉴 전환(잔상 제거) */
.submenu-enter-active,
.submenu-leave-active {
  transition: opacity 0s ease;
  position: absolute;
  inset: 0;
}
.submenu-enter-from,
.submenu-leave-to {
  opacity: 0;
}

.cat {
  list-style: none;
  margin: 0;
  padding: 0;
  width: 35%;
  border-right: 1px solid #eee;
}
.cat li {
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 14px 16px;
  height: 48px;
  font-size: 0.75rem;
  cursor: pointer;
  box-sizing: border-box;
}
.cat li i {
  font-size: 0.875rem;
  width: 24px;
  text-align: center;
  color: #555;
  transition: color 0.2s;
}
.cat li.sel,
.cat li:hover {
  background: #f5f5f5;
}
.cat li.sel span,
.cat li:hover span {
  color: #0061ff;
  font-weight: 600;
}
.cat li.sel i,
.cat li:hover i {
  color: #0061ff;
}

.items {
  flex: 1;
  overflow-y: auto;
  background: var(--theme-bg-secondary);
  transition: margin-top 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  padding-bottom: 20px;
}
.group { padding: 0px 0 4px 0; }
.group-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px 6px 16px;
}
.group-header i { color: #94a3b8; font-size: 0.9rem; }
.group-title-text {
  font-size: 0.72rem;
  color: var(--theme-text-secondary);
  font-weight: 800;
}
.group-list { padding-bottom: 4px; }

/* 섹션 카드 */
.group-card {
  border: 1px solid var(--theme-border);
  border-radius: 12px;
  background: var(--theme-card-bg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin: 10px 12px;
  overflow: hidden;
}
.group-icon { font-size: 1rem; }
.grp-registrar .group-header i { color: var(--theme-color, #2563eb); }
.grp-approver .group-header i { color: var(--theme-color, #0ea5e9); }
.grp-delegate .group-header i { color: var(--theme-color, #10b981); }
.grp-inspector .group-header i { color: var(--theme-color, #f59e0b); }
.grp-manager .group-header i { color: var(--theme-color, #ef4444); }

/* 모바일 타일 그리드 */
.tile-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 6px 12px 12px 12px;
}
.tile {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 6px;
  border: 1px solid var(--theme-border);
  border-radius: 12px;
  background: var(--theme-card-bg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  color: var(--theme-text-primary);
  cursor: pointer;
}
.tile:active { transform: translateY(1px); }
.tile-icon { font-size: 1.4rem; color: #2563eb; }
.tile-label { font-size: 0.72rem; text-align: center; line-height: 1.2; }

@media (max-width: 400px) {
  .tile-grid { grid-template-columns: repeat(2, 1fr); }
}
.item {
  display: flex;
  align-items: center;
  min-height: 48px;
  padding: 12px 14px;
  margin: 6px 12px;
  box-sizing: border-box;
  position: relative;
  font-size: 0.75rem;
  color: var(--theme-text-primary);
  text-decoration: none;
  background: var(--theme-card-bg);
  border: 1px solid var(--theme-border);
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.05);
}
.item-icon { font-size: 1.05rem; margin-right: 8px; color: var(--theme-text-secondary); }
.item-text { flex: 1; }
.item:hover {
  background: var(--theme-bg-hover);
}

.empty {
  padding: 20px;
  color: var(--theme-text-muted);
  text-align: center;
  font-size: 0.85rem;
}

.slide-enter-active {
  transition: transform 0.2s ease;
}
.slide-leave-active {
  transition: transform 0.15s ease;
}
.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}

.drawer-panel {
  overscroll-behavior: contain;
}

/* 1300px 초과에서 모바일 전용 요소 숨김 */
@media (min-width: 1301px) {
  .mobile-only {
    display: none !important;
  }
}

/* 다크모드: 프로필 헤더 배경 */
body[data-theme="dark"] .profile-header {
  background: #3a4556 !important;
}
</style>
