<template>
  <div class="mobile-user-card">
    <!-- 배경 그라데이션 레이어 -->
    <div class="card-background"></div>
    
    <!-- 카드 콘텐츠 -->
    <div class="card-content">
      <!-- 프로필 영역 -->
      <div class="profile-area">
        <div class="profile-container" @click="goToUserInfo">
          <div class="avatar-wrapper">
            <div class="avatar-ring"></div>
            <div class="avatar">
              <img v-if="userPhotoUrl" :src="userPhotoUrl" :alt="$t('common.mobileUserCard.profileAlt')" />
              <div v-else class="avatar-placeholder">
                <i class="ri-user-3-fill"></i>
              </div>
            </div>
          </div>
          <div class="profile-info">
            <div class="greeting">{{ $t('common.mobileUserCard.greetingSimple') }}</div>
            <div class="user-name">{{ userName }}</div>
            <div class="user-badges">
              <span class="badge badge-dept">
                <i class="ri-building-line"></i>
                {{ userDepartment }}
              </span>
              <span class="badge badge-position">
                <i class="ri-briefcase-line"></i>
                {{ userPosition }}
              </span>
            </div>
          </div>
        </div>
        <button class="settings-icon" @click="goToSettings" :aria-label="$t('common.sidebar.favorites.manage')">
          <i class="ri-settings-3-line"></i>
        </button>
      </div>

      <!-- 즐겨찾기 섹션 -->
      <div class="favorites-section">
        <div class="section-header">
          <div class="header-left">
            <i class="ri-star-smile-fill"></i>
            <span class="section-title">{{ $t('common.mobileUserCard.favorites') }}</span>
          </div>
          <span v-if="favoriteMenus.length > 0" class="count-badge">{{ favoriteMenus.length }}</span>
        </div>
        
        <!-- 즐겨찾기 목록 -->
        <div v-if="favoriteMenus.length > 0" class="favorites-grid">
          <button
            v-for="menu in favoriteMenus"
            :key="menu.favoriteId"
            class="fav-card"
            :class="`fav-${menu.color || 'blue'}`"
            @click="goToFavorite(menu.menuPath)"
          >
            <div class="fav-icon-container">
              <i :class="menu.menuIcon || 'ri-star-line'"></i>
            </div>
            <div class="fav-info">
              <div class="fav-label">{{ $t(menu.menuLabel) }}</div>
              <div class="fav-category">{{ $t(menu.category) }}</div>
            </div>
          </button>
        </div>
        
        <!-- 빈 상태 -->
        <div v-else class="empty-state">
          <i class="ri-star-line"></i>
          <div class="empty-text">{{ $t('common.mobileUserCard.addFavoritesGuide') }}</div>
          <div class="empty-hint">{{ $t('common.mobileUserCard.addFavoritesGuideHint') }}</div>
        </div>
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
 * 메인 카드 컨테이너
 * ═══════════════════════════════════════════════════════════════ */
.mobile-user-card {
  position: relative;
  border-radius: 24px;
  margin: 16px 0 24px 0;
  overflow: hidden;
  box-shadow: 
    0 10px 40px rgba(79, 70, 229, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 배경 그라데이션 */
.card-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 140px;
  background: linear-gradient(135deg, #B3DAF9 0%, #4c88ff 100%);
  opacity: 1;
}

/* 카드 콘텐츠 */
.card-content {
  position: relative;
  z-index: 1;
  padding: 20px 18px 22px 18px;
}

/* ═══════════════════════════════════════════════════════════════
 * 프로필 영역
 * ═══════════════════════════════════════════════════════════════ */
.profile-area {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}

.profile-container {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

/* 아바타 래퍼 */
.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
  border-radius: 50%;
}

.avatar-ring {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0.1) 100%);
  animation: pulse-ring 3s ease-in-out infinite;
}

@keyframes pulse-ring {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}

.avatar {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  background: #ffffff;
  border: 3px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f0f4ff 0%, #e0e7ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder i {
  color: #6366f1;
  font-size: 2rem;
}

/* 프로필 정보 */
.profile-info {
  flex: 1;
  min-width: 0;
}

.greeting {
  font-size: 0.7rem;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 600;
  margin-bottom: 4px;
  letter-spacing: 0.3px;
}

.user-name {
  font-size: 1.3rem;
  font-weight: 800;
  color: #ffffff;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.65rem;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.badge i {
  font-size: 0.7rem;
}

/* 설정 아이콘 */
.settings-icon {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.settings-icon:active {
  transform: scale(0.92);
  background: rgba(255, 255, 255, 0.35);
}

.settings-icon i {
  color: #ffffff;
  font-size: 1.2rem;
}

/* ═══════════════════════════════════════════════════════════════
 * 즐겨찾기 섹션
 * ═══════════════════════════════════════════════════════════════ */
.favorites-section {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px 14px 18px 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 7px;
}

.header-left i {
  font-size: 1rem;
  color: #f59e0b;
}

.section-title {
  font-size: 0.8rem;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.01em;
}

.count-badge {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  color: #0369a1;
  font-size: 0.65rem;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 12px;
  border: 1px solid #bae6fd;
}

/* 즐겨찾기 그리드 */
.favorites-grid {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 2px;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.favorites-grid::-webkit-scrollbar {
  display: none;
}

/* 즐겨찾기 카드 */
.fav-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  min-width: 70px;
  padding: 12px 8px;
  background: #ffffff;
  border-radius: 14px;
  border: 1.5px solid;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.fav-card:active {
  transform: scale(0.96);
}

.fav-icon-container {
  width: 37px;
  height: 37px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.fav-icon-container i {
  font-size: 1.3rem;
  color: #ffffff;
}

.fav-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  width: 100%;
}

.fav-label {
  font-size: 0.65rem;
  font-weight: 700;
  text-align: center;
  line-height: 1.3;
  max-height: 2.6rem;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: keep-all;
}

.fav-category {
  font-size: 0.6rem;
  font-weight: 500;
  opacity: 0.7;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

/* 색상 테마 - Blue */
.fav-blue {
  border-color: #bfdbfe;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
}

.fav-blue .fav-icon-container {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.fav-blue .fav-label {
  color: #1e40af;
}

.fav-blue .fav-category {
  color: #3b82f6;
}

/* 색상 테마 - Green */
.fav-green {
  border-color: #a7f3d0;
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
}

.fav-green .fav-icon-container {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.fav-green .fav-label {
  color: #065f46;
}

.fav-green .fav-category {
  color: #10b981;
}

/* 색상 테마 - Purple */
.fav-purple {
  border-color: #ddd6fe;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
}

.fav-purple .fav-icon-container {
  background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.fav-purple .fav-label {
  color: #5b21b6;
}

.fav-purple .fav-category {
  color: #8b5cf6;
}

/* 색상 테마 - Pink */
.fav-pink {
  border-color: #fbcfe8;
  background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
}

.fav-pink .fav-icon-container {
  background: linear-gradient(135deg, #f472b6 0%, #ec4899 100%);
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.3);
}

.fav-pink .fav-label {
  color: #9f1239;
}

.fav-pink .fav-category {
  color: #ec4899;
}

/* 색상 테마 - Orange */
.fav-orange {
  border-color: #fed7aa;
  background: linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%);
}

.fav-orange .fav-icon-container {
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}

.fav-orange .fav-label {
  color: #9a3412;
}

.fav-orange .fav-category {
  color: #f97316;
}

/* 색상 테마 - Yellow */
.fav-yellow {
  border-color: #fde68a;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.fav-yellow .fav-icon-container {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.fav-yellow .fav-label {
  color: #92400e;
}

.fav-yellow .fav-category {
  color: #f59e0b;
}

/* 색상 테마 - Red */
.fav-red {
  border-color: #fecaca;
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
}

.fav-red .fav-icon-container {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.fav-red .fav-label {
  color: #991b1b;
}

.fav-red .fav-category {
  color: #ef4444;
}

/* 색상 테마 - Indigo */
.fav-indigo {
  border-color: #c7d2fe;
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
}

.fav-indigo .fav-icon-container {
  background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.fav-indigo .fav-label {
  color: #3730a3;
}

.fav-indigo .fav-category {
  color: #6366f1;
}

/* 색상 테마 - Teal */
.fav-teal {
  border-color: #99f6e4;
  background: linear-gradient(135deg, #ccfbf1 0%, #99f6e4 100%);
}

.fav-teal .fav-icon-container {
  background: linear-gradient(135deg, #2dd4bf 0%, #14b8a6 100%);
  box-shadow: 0 4px 12px rgba(20, 184, 166, 0.3);
}

.fav-teal .fav-label {
  color: #115e59;
}

.fav-teal .fav-category {
  color: #14b8a6;
}

/* 색상 테마 - Cyan */
.fav-cyan {
  border-color: #a5f3fc;
  background: linear-gradient(135deg, #cffafe 0%, #a5f3fc 100%);
}

.fav-cyan .fav-icon-container {
  background: linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%);
  box-shadow: 0 4px 12px rgba(6, 182, 212, 0.3);
}

.fav-cyan .fav-label {
  color: #164e63;
}

.fav-cyan .fav-category {
  color: #06b6d4;
}

/* 색상 테마 - Gray */
.fav-gray {
  border-color: #d1d5db;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
}

.fav-gray .fav-icon-container {
  background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
  box-shadow: 0 4px 12px rgba(107, 114, 128, 0.3);
}

.fav-gray .fav-label {
  color: #374151;
}

.fav-gray .fav-category {
  color: #6b7280;
}

/* ═══════════════════════════════════════════════════════════════
 * 빈 상태
 * ═══════════════════════════════════════════════════════════════ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  text-align: center;
}

.empty-state i {
  font-size: 3rem;
  color: #e2e8f0;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 0.8rem;
  color: #475569;
  font-weight: 600;
  margin-bottom: 6px;
}

.empty-hint {
  font-size: 0.7rem;
  color: #94a3b8;
  line-height: 1.4;
}

/* ═══════════════════════════════════════════════════════════════
 * 반응형
 * ═══════════════════════════════════════════════════════════════ */
@media (max-width: 380px) {
  .mobile-user-card {
    margin: 12px 0 20px 0;
    border-radius: 20px;
  }
  
  .card-content {
    padding: 18px 16px 20px 16px;
  }
  
  .avatar-wrapper {
    transform: scale(0.92);
  }
  
  .avatar {
    width: 68px;
    height: 68px;
  }
  
  .user-name {
    font-size: 1.15rem;
  }
  
  .badge {
    font-size: 0.6rem;
    padding: 3px 8px;
  }
  
  .fav-card {
    min-width: 68px;
    padding: 10px 6px;
  }
  
  .fav-icon-container {
    width: 38px;
    height: 38px;
  }
  
  .fav-icon-container i {
    font-size: 1.2rem;
  }
  
  .fav-label {
    font-size: 0.63rem;
  }
  
  .fav-category {
    font-size: 0.55rem;
  }
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

body[data-theme="dark"] .fav-card {
  background: #a8a8a8 !important;
  border: 2px solid var(--theme-border) !important;
  box-shadow: 0 2px 8px var(--theme-shadow-sm) !important;
}

body[data-theme="dark"] .fav-card:active {
  background: var(--theme-bg-hover) !important;
  border-color: var(--theme-border-hover) !important;
  box-shadow: 0 1px 4px var(--theme-shadow-sm) !important;
}

body[data-theme="dark"] .fav-label {
  color: #ffffff !important;
}

body[data-theme="dark"] .fav-category {
  color: #e5e5e5 !important;
}

body[data-theme="dark"] .empty-box i {
  color: var(--theme-text-tertiary) !important;
}

body[data-theme="dark"] .empty-box p {
  color: var(--theme-text-secondary) !important;
}

/* 스타일2 전용 다크모드 추가 스타일 */
body[data-theme="dark"] .mobile-user-card {
  background: var(--theme-card-bg) !important;
  border: 2px solid var(--theme-border) !important;
  box-shadow: 0 4px 12px var(--theme-shadow-sm) !important;
}

body[data-theme="dark"] .card-background {
  background: linear-gradient(135deg, var(--theme-bg-secondary) 0%, var(--theme-bg-tertiary) 100%) !important;
}

body[data-theme="dark"] .profile-area {
  background: linear-gradient(135deg, var(--theme-bg-secondary) 0%, var(--theme-bg-tertiary) 100%) !important;
  border-bottom: 2px solid var(--theme-border) !important;
}

body[data-theme="dark"] .profile-area:active {
  background: linear-gradient(135deg, var(--theme-bg-tertiary) 0%, var(--theme-bg-hover) 100%) !important;
}

body[data-theme="dark"] .avatar-wrapper {
  background: var(--theme-card-bg) !important;
  border: 2px solid var(--theme-border) !important;
}

body[data-theme="dark"] .avatar-placeholder i {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .greeting {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .user-name {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .badge {
  background: var(--theme-chip-bg) !important;
  border: 1px solid var(--theme-chip-border) !important;
  color: var(--theme-chip-text) !important;
}

body[data-theme="dark"] .badge i {
  color: var(--theme-text-tertiary) !important;
}

body[data-theme="dark"] .settings-icon {
  background: #807F7F !important;
  color: #ffffff !important;
  border: 1px solid #6b6a6a !important;
}

body[data-theme="dark"] .settings-icon:active {
  background: #6b6a6a !important;
  color: #ffffff !important;
}

body[data-theme="dark"] .settings-icon i {
  color: #ffffff !important;
}

body[data-theme="dark"] .favorites-section {
  background: var(--theme-card-bg) !important;
  border-bottom: 1px solid var(--theme-border);
}

body[data-theme="dark"] .section-title {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .count-badge {
  background: var(--theme-primary) !important;
  color: #ffffff !important;
}

body[data-theme="dark"] .empty-state i {
  color: var(--theme-text-tertiary) !important;
}

body[data-theme="dark"] .empty-text {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .empty-hint {
  color: var(--theme-text-tertiary) !important;
}

/* 다크모드에서도 아이콘 배경 그라데이션 유지 */
body[data-theme="dark"] .fav-blue .fav-icon-container { background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%) !important; }
body[data-theme="dark"] .fav-green .fav-icon-container { background: linear-gradient(135deg, #34d399 0%, #10b981 100%) !important; }
body[data-theme="dark"] .fav-purple .fav-icon-container { background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%) !important; }
body[data-theme="dark"] .fav-pink .fav-icon-container { background: linear-gradient(135deg, #f472b6 0%, #ec4899 100%) !important; }
body[data-theme="dark"] .fav-orange .fav-icon-container { background: linear-gradient(135deg, #fb923c 0%, #f97316 100%) !important; }
body[data-theme="dark"] .fav-yellow .fav-icon-container { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%) !important; }
body[data-theme="dark"] .fav-red .fav-icon-container { background: linear-gradient(135deg, #f87171 0%, #ef4444 100%) !important; }
body[data-theme="dark"] .fav-indigo .fav-icon-container { background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%) !important; }
body[data-theme="dark"] .fav-teal .fav-icon-container { background: linear-gradient(135deg, #2dd4bf 0%, #14b8a6 100%) !important; }
body[data-theme="dark"] .fav-cyan .fav-icon-container { background: linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%) !important; }
body[data-theme="dark"] .fav-gray .fav-icon-container { background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%) !important; }

body[data-theme="dark"] .fav-icon-container i {
  color: #ffffff !important;
}
</style>

