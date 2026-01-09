<template>
  <aside class="first-sidebar">
    <div
      v-for="(item, index) in visibleWorkspaces"
      :key="item.key"
      class="sidebar-item"
      :class="{ active: sidebarStore.selectedWorkspace === item.key }"
      @click="handleWorkspaceClick(item.key)"
      :title="item.label"
    >
      <i :class="item.icon"></i>
    </div>

    <!-- 테마 토글 버튼 (회사 정보 위) -->
    <div 
      class="sidebar-item theme-toggle-button desktop-only"
      @click="toggleTheme"
      :title="currentTheme === 'light' ? $t('common.sidebar.tooltip.toDark') : $t('common.sidebar.tooltip.toLight')"
    >
      <i v-if="currentTheme === 'light'" class="ri-sun-line"></i>
      <i v-else class="ri-moon-line"></i>
    </div>

    <!-- 회사 정보 버튼 (맨 아래 고정) -->
    <div 
      class="sidebar-item company-info-button"
      @click="showCompanyInfo"
      :title="$t('common.sidebar.tooltip.companyInfo')"
    >
      <i class="ri-question-line"></i>
    </div>

    <!-- 회사 정보 모달 -->
    <CompanyInfoModal
      :isVisible="isCompanyInfoModalVisible"
      @close="isCompanyInfoModalVisible = false"
    />
  </aside>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter, useRoute } from 'vue-router';
import { useSidebarStore } from '@/store/sidebar';
import { useAuthStore } from '@/store/auth';
import { useHrmStore } from '@/store/hrm';
import { getRolesFrom } from '@/utils/roleUtils';
import { ROLE_GROUPS } from '@/config/roleConfig';
import { ROUTES } from '@/config/routeConfig';
import CompanyInfoModal from '@/components/common/modal/CompanyInfoModal.vue';
import StylesApi from '@/api/hrm/StylesApi';
import { STYLE_CATEGORY, INFO_MOBILE_STYLE } from '@/constants';
import { toast } from 'vue3-toastify';

const router = useRouter();
const { t } = useI18n();
const route = useRoute();
const sidebarStore = useSidebarStore();
const authStore = useAuthStore();
const hrmStore = useHrmStore();

/**
 * 회사 정보 모달 표시 여부
 */
const isCompanyInfoModalVisible = ref(false);

/**
 * 사용자 권한
 */
const roles = computed(() => authStore.roles);
const isLocaltest = computed(() =>
  String(import.meta.env.VITE_LOCALTEST ?? import.meta.env.VITE_APP_LOCALTEST) === "true"
);

/**
 * 영수증 기능 활성화 여부
 */
const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';

/**
 * 권한 체크 함수
 */
const canShow = (roleArr = []) => {
  if (roleArr.length === 0) return true;
  if (isLocaltest.value) return true;
  if (roles.value.includes("ROLE_GATE_SYSTEM")) return true;
  return roleArr.some((r) => roles.value.includes(r));
};

/**
 * 워크스페이스 정의
 */
const workspaces = computed(() => ([
  { key: 'home',        label: t('common.sidebar.workspace.home'),        icon: 'ri-home-4-line',            roles: [] },
  { key: 'info',        label: t('common.sidebar.workspace.info'),        icon: 'ri-information-line',       roles: [] }, // 가이드 때문에 모든 사용자 접근 가능
  { key: 'receipt',     label: t('common.sidebar.workspace.receipt'),     icon: 'ri-receipt-line',           roles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR), enabled: isReceiptEnabled },
  { key: 'management',  label: t('common.sidebar.workspace.management'),  icon: 'ri-user-settings-line',     roles: getRolesFrom(ROLE_GROUPS.HRM_EMPLOYEE) },
  { key: 'user',        label: t('common.sidebar.workspace.user'),        icon: 'ri-user-line',              roles: getRolesFrom(ROLE_GROUPS.HRM_EMPLOYEE) },
  { key: 'system',      label: t('common.sidebar.workspace.system'),      icon: 'ri-settings-3-line',        roles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM) },
  { key: 'favorites',   label: t('common.sidebar.workspace.favorites'),   icon: 'ri-star-line',              roles: [] },
]));

/**
 * 권한에 따라 보이는 워크스페이스만 필터링
 */
const visibleWorkspaces = computed(() => {
  return workspaces.value.filter(ws => {
    // enabled가 false인 경우 숨김 (영수증 기능 비활성화 등)
    if (ws.enabled === false) return false;
    // 권한 체크
    return canShow(ws.roles);
  });
});

/**
 * 워크스페이스 클릭 핸들러
 */
const handleWorkspaceClick = (key) => {
  sidebarStore.selectWorkspace(key);
  
  // 홈 클릭 시 대시보드로 이동
  if (key === 'home') {
    router.push(ROUTES.MAIN);
  } else {
    // 다른 워크스페이스는 route가 있으면 이동
    const workspace = workspaces.value.find(w => w.key === key);
    if (workspace?.route) {
      router.push(workspace.route);
    }
  }
};

/**
 * 회사 정보 모달 열기
 */
const showCompanyInfo = () => {
  isCompanyInfoModalVisible.value = true;
};

/**
 * 현재 경로에 따라 워크스페이스를 자동으로 선택
 */
const syncWorkspaceFromRoute = () => {
  const path = route.path;
  console.log('[FirstSidebar] 경로 동기화:', path);
  
  let workspace = null;
  
  // 경로 패턴에 따라 워크스페이스 결정
  if (path === ROUTES.MAIN || path === ROUTES.HOME) {
    workspace = 'home';
  } else if (path.includes(ROUTES.MANAGEMENT.FAVORITE_MENU_MANAGEMENT)) {
    workspace = 'favorites';
  } else if (path.startsWith('/info')) {
    workspace = 'info';
  } else if (path.startsWith('/receipt')) {
    workspace = 'receipt';
  } else if (path.startsWith('/hrm') || path.startsWith('/department') || path.startsWith('/team') || path.startsWith('/management')) {
    workspace = 'management';
  } else if (path.startsWith('/user') || path.startsWith('/my-info')) {
    workspace = 'user';
  } else if (path.startsWith('/system') || path.startsWith('/statistics') || path.startsWith('/log')) {
    workspace = 'system';
  }
  
  if (workspace) {
    console.log('[FirstSidebar] 워크스페이스 선택:', workspace);
    sidebarStore.selectWorkspace(workspace);
  } else {
    console.warn('[FirstSidebar] 일치하는 워크스페이스를 찾을 수 없음:', path);
  }
};

/**
 * 라우트 변경 감지
 */
watch(() => route.path, () => {
  syncWorkspaceFromRoute();
}, { immediate: true });

/**
 * 컴포넌트 마운트 시 초기 동기화
 */
onMounted(() => {
  syncWorkspaceFromRoute();
});

/**
 * 현재 테마
 */
const currentTheme = computed(() => {
  return hrmStore.getMyProfile?.infoMobileStyle || INFO_MOBILE_STYLE.LIGHT;
});

/**
 * 테마 토글
 */
async function toggleTheme() {
  const newTheme = currentTheme.value === INFO_MOBILE_STYLE.LIGHT 
    ? INFO_MOBILE_STYLE.DARK 
    : INFO_MOBILE_STYLE.LIGHT;
  
  await StylesApi.updateUserStyle(authStore.userId, STYLE_CATEGORY.INFO_MOBILE, newTheme);
  // HRM 스토어의 프로필 업데이트
  await hrmStore.loadMyProfileByUserId(authStore.userId);
  toast.success(t('common.sidebar.message.themeChanged'));
}
</script>

<style scoped>
</style>
