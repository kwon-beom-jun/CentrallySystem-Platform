<template>
  <div class="mobile-user-card box-shadow-blue">
    <!-- 상단: 프로필 영역 -->
    <div class="profile-section">
      <div class="profile-left">
        <div class="avatar box-shadow-blue" @click="goToUserInfo">
          <img v-if="userPhotoUrl" :src="userPhotoUrl" :alt="$t('common.mobileUserCard.profileAlt')" />
          <i v-else class="ri-user-3-fill"></i>
        </div>
        <div class="user-info">
          <div class="greeting">{{ $t('common.mobileUserCard.greeting') }}</div>
          <div class="user-name">{{ userName }}</div>
          <div class="user-meta">
            <span class="department">{{ userDepartment }}</span>
            <span v-if="userDepartment && userPosition" class="divider">•</span>
            <span class="position">{{ userPosition }}</span>
          </div>
        </div>
      </div>
      <button class="settings-btn box-shadow-blue" @click="goToSettings" :aria-label="$t('common.mobileUserCard.settings')">
        <i class="ri-settings-3-line"></i>
      </button>
    </div>

    <!-- 하단: 즐겨찾기 메뉴 -->
    <div class="favorites-section">
      <div class="favorites-header">
        <i class="ri-star-fill"></i>
        <span>{{ $t('common.mobileUserCard.favorites') }}</span>
        <span class="favorites-count" v-if="favoriteMenus.length > 0">({{ favoriteMenus.length }})</span>
      </div>
      
      <!-- 즐겨찾기가 있을 때 -->
      <div v-if="favoriteMenus.length > 0" class="favorites-scroll-container">
        <button
          v-for="(menu, index) in favoriteMenus"
          :key="menu.favoriteId"
          class="favorite-item"
          :class="`color-${menu.color || 'blue'}`"
          @click="goToFavorite(menu.menuPath)"
          :aria-label="$t(menu.menuLabel)"
        >
          <div class="favorite-icon">
            <i :class="menu.menuIcon || 'ri-star-line'"></i>
          </div>
          <span class="favorite-label">{{ $t(menu.menuLabel) }}</span>
          <span class="favorite-category">{{ $t(menu.category) }}</span>
        </button>
      </div>
      
      <!-- 즐겨찾기가 없을 때 -->
      <div v-else class="favorites-empty">
        <i class="ri-star-line"></i>
        <p>{{ $t('common.mobileUserCard.noFavorites') }}</p>
        <small>{{ $t('common.mobileUserCard.addFavoritesHint') }}</small>
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
.mobile-user-card {
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
  /* background: linear-gradient(135deg, #e0f2fe 0%, #dbeafe 100%); */
  /* background: linear-gradient(135deg, #ffffff 0%, #ffffff 100%); */
  /* background: linear-gradient(135deg, #f4fbff 0%, #dcf2ff 100%); */
  /* background: linear-gradient(135deg, #f8fcff 0%, #e2f4ff 100%); */
  /* border-radius: 20px; */
  padding: 24px 20px;
  margin: 16px 0px 20px 0px;
  /* box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15); */
}

/* 프로필 섹션 */
.profile-section {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}

.profile-left {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
}

.avatar {
  width: 64px;
  height: 64px;
  /* border-radius: 16px; */
  /* border: 3px solid rgba(59, 130, 246, 0.2); */
  overflow: hidden;
  background: #ffffff;
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.avatar:active {
  transform: scale(0.95);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar i {
  color: #94a3b8;
  font-size: 2rem;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.greeting {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
  margin-bottom: 4px;
}

.user-name {
  font-size: 1.25rem;
  font-weight: 800;
  color: #0f172a;
  /* margin-bottom: 4px; */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.7rem;
  color: #646a70;
  font-weight: 500;
}

.divider {
  color: #969696;
  font-size: 0.5rem;
  margin: 0 2px;
  background: transparent;
}

.settings-btn {
  width: 40px;
  height: 40px;
  /* border-radius: 12px; */
  /* border: 1px solid rgba(59, 130, 246, 0.2); */
  background: #ffffff;
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.settings-btn:active {
  transform: scale(0.9);
  background: #f1f5f9;
}

.settings-btn i {
  color: #3b82f6;
  font-size: 1.3rem;
}

/* 즐겨찾기 섹션 */
.favorites-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(59, 130, 246, 0.2);
}

.favorites-header {
  display: flex;
  align-items: center;
  gap: 6px;
  /* margin-bottom: 12px; */
  padding: 0 2px;
}

.favorites-header i {
  font-size: 0.9rem;
  color: #f59e0b;
}

.favorites-header span {
  font-size: 0.75rem;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 0.3px;
}

.favorites-count {
  margin-left: 2px;
  font-size: 0.7rem !important;
  color: #64748b !important;
  font-weight: 500 !important;
}

/* 즐겨찾기 스크롤 컨테이너 */
.favorites-scroll-container {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 10px 2px 4px 2px;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  scroll-snap-type: x mandatory;
}

.favorites-scroll-container::-webkit-scrollbar {
  display: none;
}

/* 즐겨찾기 아이템 */
.favorite-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  min-width: 75px;
  max-width: 75px;
  padding: 10px 6px;
  background: #ffffff;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.15);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  scroll-snap-align: start;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.08);
}

.favorite-item:active {
  transform: translateY(2px) scale(0.97);
  background: #f8fafc;
}

.favorite-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.1);
  transition: all 0.2s ease;
}

.favorite-icon i {
  font-size: 1.1rem;
  color: #ffffff;
}

.favorite-label {
  font-size: 0.65rem;
  font-weight: 700;
  color: #1e293b;
  text-align: center;
  line-height: 1.2;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: keep-all;
  width: 100%;
  min-height: 1.56rem; /* 2줄 높이 고정: 0.65rem * 1.2 * 2 */
  height: 1.56rem;

  /* 핵심 추가 ↓↓↓ */
  display: flex;
  align-items: center;
  justify-content: center;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: normal; /* 2줄 이상 허용 */
  word-break: keep-all;
}

.favorite-category {
  font-size: 0.55rem;
  font-weight: 500;
  color: #64748b;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

/* 색상 변형 */
.favorite-item.color-blue .favorite-icon {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
}

.favorite-item.color-green .favorite-icon {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
}

.favorite-item.color-purple .favorite-icon {
  background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
}

.favorite-item.color-pink .favorite-icon {
  background: linear-gradient(135deg, #f472b6 0%, #ec4899 100%);
}

.favorite-item.color-orange .favorite-icon {
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
}

.favorite-item.color-yellow .favorite-icon {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
}

.favorite-item.color-red .favorite-icon {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
}

.favorite-item.color-indigo .favorite-icon {
  background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%);
}

.favorite-item.color-teal .favorite-icon {
  background: linear-gradient(135deg, #2dd4bf 0%, #14b8a6 100%);
}

.favorite-item.color-cyan .favorite-icon {
  background: linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%);
}

.favorite-item.color-gray .favorite-icon {
  background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
}

/* 즐겨찾기 빈 상태 */
.favorites-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 20px 20px 20px;
  text-align: center;
  min-height: 100px;
}

.favorites-empty i {
  font-size: 2rem;
  color: #cbd5e1;
  margin-bottom: 8px;
}

.favorites-empty p {
  font-size: 0.75rem;
  color: #475569;
  margin: 0 0 4px 0;
  font-weight: 600;
}

.favorites-empty small {
  font-size: 0.65rem;
  color: #64748b;
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

body[data-theme="dark"] .favorites-empty i {
  color: var(--theme-text-tertiary) !important;
}

body[data-theme="dark"] .favorites-empty p {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .favorites-empty small {
  color: var(--theme-text-tertiary) !important;
}

/* 즐겨찾기 버튼 다크모드 */
body[data-theme="dark"] .favorite-item {
  background: #a8a8a8 !important;
  border: 2px solid var(--theme-border) !important;
}

body[data-theme="dark"] .favorite-label {
  color: #ffffff !important;
}

body[data-theme="dark"] .favorite-category {
  color: #e5e5e5 !important;
}

/* 설정 버튼 다크모드 */
body[data-theme="dark"] .settings-btn {
  background: #807F7F !important;
  border: 1px solid #6b6a6a !important;
}

body[data-theme="dark"] .settings-btn:active {
  background: #6b6a6a !important;
}

body[data-theme="dark"] .settings-btn i {
  color: #ffffff !important;
}

/* 다크모드에서도 아이콘 배경 그라데이션 유지 */
body[data-theme="dark"] .favorite-item.color-blue .favorite-icon { background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%) !important; }
body[data-theme="dark"] .favorite-item.color-green .favorite-icon { background: linear-gradient(135deg, #34d399 0%, #10b981 100%) !important; }
body[data-theme="dark"] .favorite-item.color-purple .favorite-icon { background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%) !important; }
body[data-theme="dark"] .favorite-item.color-pink .favorite-icon { background: linear-gradient(135deg, #f472b6 0%, #ec4899 100%) !important; }
body[data-theme="dark"] .favorite-item.color-orange .favorite-icon { background: linear-gradient(135deg, #fb923c 0%, #f97316 100%) !important; }
body[data-theme="dark"] .favorite-item.color-yellow .favorite-icon { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%) !important; }
body[data-theme="dark"] .favorite-item.color-red .favorite-icon { background: linear-gradient(135deg, #f87171 0%, #ef4444 100%) !important; }
body[data-theme="dark"] .favorite-item.color-indigo .favorite-icon { background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%) !important; }
body[data-theme="dark"] .favorite-item.color-teal .favorite-icon { background: linear-gradient(135deg, #2dd4bf 0%, #14b8a6 100%) !important; }
body[data-theme="dark"] .favorite-item.color-cyan .favorite-icon { background: linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%) !important; }
body[data-theme="dark"] .favorite-item.color-gray .favorite-icon { background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%) !important; }

body[data-theme="dark"] .favorite-icon i {
  color: #ffffff !important;
}

/* 반응형 - 매우 작은 화면 */
@media (max-width: 380px) {
  .mobile-user-card {
    padding: 20px 16px;
    margin: 12px 0px 16px 0px;
  }
  
  .avatar {
    width: 56px;
    height: 56px;
  }
  
  .user-name {
    font-size: 1.1rem;
  }
  
  .favorite-item {
    min-width: 65px;
    max-width: 65px;
    padding: 8px 5px;
  }
  
  .favorite-icon {
    width: 32px;
    height: 32px;
  }
  
  .favorite-icon i {
    font-size: 1rem;
  }
  
  .favorite-label {
    font-size: 0.6rem;
    min-height: 1.44rem; /* 2줄 높이 고정: 0.6rem * 1.2 * 2 */
    height: 1.44rem;
  }
  
  .favorite-category {
    font-size: 0.5rem;
  }
}
</style>

