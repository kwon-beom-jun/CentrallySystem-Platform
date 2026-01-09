<template>
  <div class="workspace-card">
    <!-- 프로필 바 -->
    <div class="profile-bar" @click="goToUserInfo">
      <div class="avatar-small">
        <img v-if="userPhotoUrl" :src="userPhotoUrl" :alt="$t('common.mobileUserCard.profileAlt')" />
        <i v-else class="ri-user-line"></i>
      </div>
      <div class="user-info-compact">
        <div class="user-name-line">{{ userName }}</div>
        <div class="user-dept-line">
          <i class="ri-building-4-line"></i>
          {{ userDepartment }}
          <span class="dot">•</span>
          {{ userPosition }}
        </div>
      </div>
      <i class="ri-arrow-right-s-line profile-arrow"></i>
    </div>

    <!-- 즐겨찾기 타이틀 -->
    <div class="fav-title-row">
      <div class="title-left">
        <i class="ri-star-fill"></i>
        <span>{{ $t('common.mobileUserCard.favorites') }}</span>
        <span v-if="favoriteMenus.length > 0" class="fav-count">{{ favoriteMenus.length }}</span>
      </div>
      <button class="manage-btn" @click="goToSettings">
        <i class="ri-settings-4-line"></i>
        {{ $t('common.mobileUserCard.manage') }}
      </button>
    </div>

    <!-- 즐겨찾기 슬라이더 -->
    <div v-if="favoriteMenus.length > 0" class="fav-slider">
      <button
        v-for="menu in favoriteMenus"
        :key="menu.favoriteId"
        class="fav-chip"
        :class="`color-${menu.color || 'blue'}`"
        @click="goToFavorite(menu.menuPath)"
      >
        <div class="chip-icon">
          <i :class="menu.menuIcon || 'ri-star-line'"></i>
        </div>
        <div class="chip-text">
          <div class="chip-label">{{ $t(menu.menuLabel) }}</div>
          <div class="chip-category">{{ $t(menu.category) }}</div>
        </div>
      </button>
    </div>
    
    <!-- 빈 상태 -->
    <div v-else class="empty-box">
      <i class="ri-star-line"></i>
      <p>{{ $t('common.mobileUserCard.registerFavorites') }}</p>
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
 * 워크스페이스 카드 - 비즈니스 스타일
 * ═══════════════════════════════════════════════════════════════ */
.workspace-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 0;
  margin: 16px 0 20px 0;
  border: 2px solid #e5e7eb;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* ═══════════════════════════════════════════════════════════════
 * 프로필 바 (70px)
 * ═══════════════════════════════════════════════════════════════ */
.profile-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 2px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
}

.profile-bar:active {
  background: linear-gradient(135deg, #f1f5f9 0%, #e5e7eb 100%);
}

.avatar-small {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #8d8d8d81;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.avatar-small img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-small i {
  font-size: 1.5rem;
  color: #3b82f6;
}

.user-info-compact {
  flex: 1;
  min-width: 0;
}

.user-name-line {
  font-size: 1.05rem;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-dept-line {
  font-size: 0.75rem;
  color: #475569;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-dept-line i {
  font-size: 0.75rem;
  color: #94a3b8;
  flex-shrink: 0;
}

.dot {
  color: #969696;
  font-size: 0.5rem;
  margin: 0 2px;
  background: transparent;
}

.profile-arrow {
  font-size: 1.3rem;
  color: #cbd5e1;
  flex-shrink: 0;
}

/* ═══════════════════════════════════════════════════════════════
 * 즐겨찾기 타이틀 (50px)
 * ═══════════════════════════════════════════════════════════════ */
.fav-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  background: #ffffff;
}

.title-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-left i {
  color: #fbbf24;
}

.title-left span {
  font-size: 1rem;
  font-weight: 900;
  color: rgb(63, 73, 97);
}

.fav-count {
  font-size: 0.55rem !important;
  font-weight: 700 !important;
  color: #ffffff !important;
  background: #b3b6bd;
  padding: 4px 6px;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.25);
  line-height: 1;
}

.manage-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #828996;
  border: none;
  border-radius: 10px;
  font-size: 0.65rem;
  font-weight: 700;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
}

.manage-btn:active {
  background: #2563eb;
  transform: scale(0.96);
  box-shadow: 0 1px 3px rgba(59, 130, 246, 0.4);
}

.manage-btn i {
  font-size: 0.7rem;
}

/* ═══════════════════════════════════════════════════════════════
 * 즐겨찾기 슬라이더 (최대 220px)
 * ═══════════════════════════════════════════════════════════════ */
.fav-slider {
  display: flex;
  gap: 10px;
  padding: 5px 0 16px 0;
  margin: 0 16px;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.fav-slider::-webkit-scrollbar {
  display: none;
}

.fav-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  min-width: 75px;
  max-width: 100px;
  padding: 12px 7px;
  background: #ffffff;
  border-radius: 14px;
  border: 2px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.fav-chip:active {
  transform: scale(0.96);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.chip-icon {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.15);
}

.chip-icon i {
  font-size: 1.2rem;
  color: #ffffff;
  margin-top: 3px;
}

.chip-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  width: 100%;
}

.chip-label {
  font-size: 0.65rem;
  font-weight: 700;
  color: #0f172a;
  text-align: center;
  line-height: 1.2;
  word-break: keep-all;
  width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.chip-category {
  font-size: 0.6rem;
  font-weight: 500;
  color: #94a3b8;
  text-align: center;
  line-height: 1;
  width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 색상별 아이콘 배경 - 그라데이션 */
.color-blue .chip-icon { background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%); }
.color-green .chip-icon { background: linear-gradient(135deg, #34d399 0%, #10b981 100%); }
.color-purple .chip-icon { background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%); }
.color-pink .chip-icon { background: linear-gradient(135deg, #f472b6 0%, #ec4899 100%); }
.color-orange .chip-icon { background: linear-gradient(135deg, #fb923c 0%, #f97316 100%); }
.color-yellow .chip-icon { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); }
.color-red .chip-icon { background: linear-gradient(135deg, #f87171 0%, #ef4444 100%); }
.color-indigo .chip-icon { background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%); }
.color-teal .chip-icon { background: linear-gradient(135deg, #2dd4bf 0%, #14b8a6 100%); }
.color-cyan .chip-icon { background: linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%); }
.color-gray .chip-icon { background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%); }

/* ═══════════════════════════════════════════════════════════════
 * 빈 상태 (130px)
 * ═══════════════════════════════════════════════════════════════ */
.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 35px 20px;
  text-align: center;
}

.empty-box i {
  font-size: 2.5rem;
  color: #e2e8f0;
  margin-bottom: 10px;
}

.empty-box p {
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 500;
  margin: 0;
  line-height: 1.4;
}

/* ═══════════════════════════════════════════════════════════════
 * 다크모드 스타일
 * ═══════════════════════════════════════════════════════════════ */
body[data-theme="dark"] .workspace-card {
  background: var(--theme-card-bg) !important;
  border: 2px solid var(--theme-border) !important;
  box-shadow: 0 4px 12px var(--theme-shadow-sm) !important;
}

body[data-theme="dark"] .profile-bar {
  background: linear-gradient(135deg, var(--theme-bg-secondary) 0%, var(--theme-bg-tertiary) 100%) !important;
  border-bottom: 2px solid var(--theme-border) !important;
}

body[data-theme="dark"] .profile-bar:active {
  background: linear-gradient(135deg, var(--theme-bg-tertiary) 0%, var(--theme-bg-hover) 100%) !important;
}

body[data-theme="dark"] .avatar-small {
  background: var(--theme-card-bg) !important;
  border: 2px solid var(--theme-border) !important;
}

body[data-theme="dark"] .avatar-small i {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .user-name-line {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .user-dept-line {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .user-dept-line i {
  color: var(--theme-text-tertiary) !important;
}

body[data-theme="dark"] .dot {
  color: #d1d5db !important;
  background: transparent !important;
}

body[data-theme="dark"] .profile-arrow {
  color: var(--theme-text-tertiary) !important;
}

body[data-theme="dark"] .fav-title-row {
  background: var(--theme-card-bg) !important;
  border-bottom: 1px solid var(--theme-border);
}

body[data-theme="dark"] .title-left span {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .fav-count {
  background: var(--theme-primary) !important;
  color: #ffffff !important;
}

body[data-theme="dark"] .manage-btn {
  background: #807F7F !important;
  color: #ffffff !important;
  border: 1px solid #6b6a6a !important;
}

body[data-theme="dark"] .manage-btn:active {
  background: #6b6a6a !important;
  color: #ffffff !important;
}

body[data-theme="dark"] .manage-btn i {
  color: #ffffff !important;
}

body[data-theme="dark"] .fav-chip {
  background: #a8a8a8 !important;
  border: 2px solid var(--theme-border) !important;
  box-shadow: 0 2px 8px var(--theme-shadow-sm) !important;
}

body[data-theme="dark"] .fav-chip:active {
  background: var(--theme-bg-hover) !important;
  border-color: var(--theme-border-hover) !important;
  box-shadow: 0 1px 4px var(--theme-shadow-sm) !important;
}

body[data-theme="dark"] .chip-label {
  color: #ffffff !important;
}

body[data-theme="dark"] .chip-category {
  color: #e5e5e5 !important;
}

body[data-theme="dark"] .empty-box i {
  color: var(--theme-text-tertiary) !important;
}

body[data-theme="dark"] .empty-box p {
  color: var(--theme-text-secondary) !important;
}

/* 다크모드에서도 아이콘 배경 그라데이션 유지 */
body[data-theme="dark"] .color-blue .chip-icon { background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%) !important; }
body[data-theme="dark"] .color-green .chip-icon { background: linear-gradient(135deg, #34d399 0%, #10b981 100%) !important; }
body[data-theme="dark"] .color-purple .chip-icon { background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%) !important; }
body[data-theme="dark"] .color-pink .chip-icon { background: linear-gradient(135deg, #f472b6 0%, #ec4899 100%) !important; }
body[data-theme="dark"] .color-orange .chip-icon { background: linear-gradient(135deg, #fb923c 0%, #f97316 100%) !important; }
body[data-theme="dark"] .color-yellow .chip-icon { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%) !important; }
body[data-theme="dark"] .color-red .chip-icon { background: linear-gradient(135deg, #f87171 0%, #ef4444 100%) !important; }
body[data-theme="dark"] .color-indigo .chip-icon { background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%) !important; }
body[data-theme="dark"] .color-teal .chip-icon { background: linear-gradient(135deg, #2dd4bf 0%, #14b8a6 100%) !important; }
body[data-theme="dark"] .color-cyan .chip-icon { background: linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%) !important; }
body[data-theme="dark"] .color-gray .chip-icon { background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%) !important; }

body[data-theme="dark"] .chip-icon i {
  color: #ffffff !important;
}

/* ═══════════════════════════════════════════════════════════════
 * 반응형
 * ═══════════════════════════════════════════════════════════════ */
@media (max-width: 380px) {
  .profile-bar {
    padding: 14px 16px;
  }
  
  .avatar-small {
    width: 42px;
    height: 42px;
  }
  
  .avatar-small i {
    font-size: 1.2rem;
  }
  
  .user-name-line {
    font-size: 0.9rem;
  }
  
  .user-dept-line {
    font-size: 0.65rem;
  }

  .title-left span {
    font-size: 0.85rem;
  }
  
  .fav-title-row {
    padding: 12px 16px;
  }
  
  .fav-slider {
    padding: 5px 0px 16px 0px;
  }
  
  .fav-chip {
    min-width: 70px;
    max-width: 100px;
    padding: 12px 6px;
  }
  
  .chip-icon {
    width: 35px;
    height: 35px;
  }
  
  .chip-icon i {
    font-size: 1.1rem;
    margin-top: 0px;
  }
  
  .chip-label {
    font-size: 0.63rem;
  }
  
  .chip-category {
    font-size: 0.55rem;
  }
}
</style>

