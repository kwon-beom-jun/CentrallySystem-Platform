<template>
  <div class="main-dashboard">
    <!-- 프로필 헤더 -->
    <div class="dash-header">
      <div class="user-badge" @click="goToUserInfo">
        <div class="badge-avatar">
          <img v-if="userPhotoUrl" :src="userPhotoUrl" :alt="$t('common.mobileUserCard.profileAlt')" />
          <i v-else class="ri-user-3-fill"></i>
        </div>
        <div class="badge-info">
          <div class="badge-name">{{ userName }}</div>
          <div class="badge-org">
            {{ userDepartment }}
            <span class="dot">•</span>
            {{ userPosition }}</div>
        </div>
      </div>
    </div>

    <!-- 즐겨찾기 섹션 -->
    <div class="quick-access">
      <div class="section-top">
        <div class="section-label">
          <i class="ri-heart-3-fill"></i>
          <span>{{ $t('common.mobileUserCard.quickMenu') }}</span>
        </div>
        <button class="btn-manage" @click="goToSettings">
          <i class="ri-settings-4-line"></i>
        </button>
      </div>

      <!-- 즐겨찾기 그리드 -->
      <div v-if="favoriteMenus.length > 0" class="menu-grid">
        <button
          v-for="menu in favoriteMenus"
          :key="menu.favoriteId"
          class="grid-item"
          :class="`theme-${menu.color || 'blue'}`"
          @click="goToFavorite(menu.menuPath)"
        >
          <i :class="menu.menuIcon || 'ri-star-line'" class="item-icon"></i>
          <div class="item-name">{{ $t(menu.menuLabel) }}</div>
        </button>
      </div>
      
      <!-- 빈 상태 -->
      <div v-else class="empty-state">
        <i class="ri-heart-line"></i>
        <p>{{ $t('common.mobileUserCard.addFavorites') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { useHrmStore } from '@/store/hrm';
import { useI18n } from 'vue-i18n';
import { ROUTES } from '@/config/routeConfig';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { HRM_API_BASE_URL, HRM_SERVICE_NAME } from '@/constants';

const router = useRouter();
const authStore = useAuthStore();
const hrmStore = useHrmStore();
const { t } = useI18n();

/**
 * Props
 */
const props = defineProps({
  favoriteMenus: {
    type: Array,
    default: () => []
  }
});

/**
 * 사용자 정보
 */
const userName = computed(() => authStore.getUser || t('common.mobileUserCard.user'));
const userPhotoUrl = computed(() => {
  const url = authStore.getUserProfileImgUrl;
  if (!url) return '';
  return getImagePreviewUrl(url, HRM_API_BASE_URL, HRM_SERVICE_NAME);
});
const userDepartment = computed(() => hrmStore.getMyProfile?.departmentName || t('common.mobileUserCard.departmentUnspecified'));
const userPosition = computed(() => hrmStore.getMyProfile?.positionName || t('common.mobileUserCard.positionUnspecified'));

/**
 * 사용자 정보 페이지로 이동
 */
function goToUserInfo() {
  router.push(ROUTES.USER.USER_INFORMATION);
}

/**
 * 설정 페이지로 이동 (즐겨찾기 관리)
 */
function goToSettings() {
  router.push(ROUTES.MANAGEMENT.FAVORITE_MENU_MANAGEMENT);
}

/**
 * 즐겨찾기 메뉴로 이동
 */
function goToFavorite(menuPath) {
  router.push(menuPath);
}
</script>

<style scoped>
/* ═══════════════════════════════════════════════════════════════
 * 메인 대시보드 - 밝은 모던 스타일
 * ═══════════════════════════════════════════════════════════════ */
.main-dashboard {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 20px;
  padding: 0;
  margin: 16px 0 20px 0;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.15);
  border: 1px solid #bfdbfe;
}

/* ═══════════════════════════════════════════════════════════════
 * 대시 헤더
 * ═══════════════════════════════════════════════════════════════ */
.dash-header {
  padding: 24px 20px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.user-badge:active {
  opacity: 0.8;
}

.badge-avatar {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #3b83f68f;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.25);
}

.badge-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.badge-avatar i {
  font-size: 1.8rem;
  color: #3b82f6;
}

.badge-info {
  flex: 1;
  min-width: 0;
}

.dot {
  color: #969696;
  font-size: 0.5rem;
  margin: 0 2px;
  background: transparent;
}

.badge-name {
  font-size: 1.15rem;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.badge-org {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ═══════════════════════════════════════════════════════════════
 * 즐겨찾기 섹션
 * ═══════════════════════════════════════════════════════════════ */
.quick-access {
  padding: 20px;
  background: #ffffff;
}

.section-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-label i {
  font-size: 1rem;
  color: #ef4444;
}

.section-label span {
  font-size: 0.9rem;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: 0.5px;
  line-height: 1 !important;
}

.btn-manage {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-manage:active {
  background: #e2e8f0;
  transform: scale(0.95);
}

.btn-manage i {
  font-size: 1rem;
  color: #64748b;
}

/* ═══════════════════════════════════════════════════════════════
 * 즐겨찾기 그리드 (4칸)
 * ═══════════════════════════════════════════════════════════════ */
.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 8px;
  background: #fafafa;
  border-radius: 14px;
  border: 1.5px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 90px;
}

.grid-item:active {
  transform: scale(0.95);
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.item-icon {
  font-size: 1.8rem;
  margin-bottom: 2px;
}

.item-name {
  font-size: 0.65rem;
  font-weight: 700;
  color: #1e293b;
  text-align: center;
  line-height: 1.3;
  word-break: keep-all;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 색상별 아이콘 색상 */
.theme-blue .item-icon { color: #60a5fa; }
.theme-green .item-icon { color: #34d399; }
.theme-purple .item-icon { color: #a78bfa; }
.theme-pink .item-icon { color: #f472b6; }
.theme-orange .item-icon { color: #fb923c; }
.theme-yellow .item-icon { color: #fbbf24; }
.theme-red .item-icon { color: #f87171; }
.theme-indigo .item-icon { color: #818cf8; }
.theme-teal .item-icon { color: #2dd4bf; }
.theme-cyan .item-icon { color: #22d3ee; }
.theme-gray .item-icon { color: #9ca3af; }

/* ═══════════════════════════════════════════════════════════════
 * 빈 상태
 * ═══════════════════════════════════════════════════════════════ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  background: #fafafa;
  border-radius: 14px;
  border: 1px dashed #cbd5e1;
}

.empty-state i {
  font-size: 2.5rem;
  color: #cbd5e1;
  margin-bottom: 12px;
}

.empty-state p {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
  margin: 0;
}

/* ═══════════════════════════════════════════════════════════════
 * 반응형
 * ═══════════════════════════════════════════════════════════════ */
@media (max-width: 380px) {
  .dash-header {
    padding: 20px 18px;
  }
  
  .badge-avatar {
    width: 52px;
    height: 52px;
  }
  
  .badge-avatar i {
    font-size: 1.6rem;
  }
  
  .badge-name {
    font-size: 1.05rem;
  }
  
  .badge-org {
    font-size: 0.7rem;
  }
  
  .quick-access {
    padding: 18px;
  }
  
  .section-label span {
    font-size: 0.85rem;
  }
  
  .menu-grid {
    gap: 10px;
  }
  
  .grid-item {
    padding: 14px 6px;
    min-height: 85px;
  }
  
  .item-icon {
    font-size: 1.6rem;
  }
  
  .item-name {
    font-size: 0.6rem;
  }
}

/* ═══════════════════════════════════════════════════════════════
 * 다크모드 스타일
 * ═══════════════════════════════════════════════════════════════ */
body[data-theme="dark"] .main-dashboard {
  background: var(--theme-card-bg) !important;
  border: 2px solid var(--theme-border) !important;
  box-shadow: 0 4px 12px var(--theme-shadow-sm) !important;
}

body[data-theme="dark"] .dash-header {
  background: linear-gradient(135deg, var(--theme-bg-secondary) 0%, var(--theme-bg-tertiary) 100%) !important;
  border-bottom: 2px solid var(--theme-border) !important;
}

body[data-theme="dark"] .user-badge {
  background: transparent !important;
}

body[data-theme="dark"] .user-badge:active {
  background: var(--theme-bg-hover) !important;
}

body[data-theme="dark"] .badge-avatar {
  background: var(--theme-card-bg) !important;
  border: 2px solid var(--theme-border) !important;
}

body[data-theme="dark"] .badge-avatar i {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .badge-name {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .badge-org {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .quick-access {
  background: var(--theme-card-bg) !important;
  border-bottom: 1px solid var(--theme-border);
}

body[data-theme="dark"] .section-label span {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .btn-manage {
  background: #807F7F !important;
  color: #ffffff !important;
  border: 1px solid #6b6a6a !important;
}

body[data-theme="dark"] .btn-manage:active {
  background: #6b6a6a !important;
  color: #ffffff !important;
}

body[data-theme="dark"] .btn-manage i {
  color: #ffffff !important;
}

body[data-theme="dark"] .grid-item {
  background: #e8e8e8 !important;
  border: 2px solid var(--theme-border) !important;
  box-shadow: 0 2px 8px var(--theme-shadow-sm) !important;
}

body[data-theme="dark"] .grid-item:active {
  background: var(--theme-bg-hover) !important;
  border-color: var(--theme-border-hover) !important;
  box-shadow: 0 1px 4px var(--theme-shadow-sm) !important;
}

body[data-theme="dark"] .item-name {
  color: #000000 !important;
}

body[data-theme="dark"] .empty-state i {
  color: var(--theme-text-tertiary) !important;
}

body[data-theme="dark"] .empty-state p {
  color: var(--theme-text-secondary) !important;
}

/* 다크모드에서도 아이콘 색상별로 표시 */
body[data-theme="dark"] .main-dashboard .grid-item.theme-blue .item-icon { color: #60a5fa !important; }
body[data-theme="dark"] .main-dashboard .grid-item.theme-green .item-icon { color: #34d399 !important; }
body[data-theme="dark"] .main-dashboard .grid-item.theme-purple .item-icon { color: #a78bfa !important; }
body[data-theme="dark"] .main-dashboard .grid-item.theme-pink .item-icon { color: #f472b6 !important; }
body[data-theme="dark"] .main-dashboard .grid-item.theme-orange .item-icon { color: #fb923c !important; }
body[data-theme="dark"] .main-dashboard .grid-item.theme-yellow .item-icon { color: #fbbf24 !important; }
body[data-theme="dark"] .main-dashboard .grid-item.theme-red .item-icon { color: #f87171 !important; }
body[data-theme="dark"] .main-dashboard .grid-item.theme-indigo .item-icon { color: #818cf8 !important; }
body[data-theme="dark"] .main-dashboard .grid-item.theme-teal .item-icon { color: #2dd4bf !important; }
body[data-theme="dark"] .main-dashboard .grid-item.theme-cyan .item-icon { color: #22d3ee !important; }
body[data-theme="dark"] .main-dashboard .grid-item.theme-gray .item-icon { color: #9ca3af !important; }
</style>

