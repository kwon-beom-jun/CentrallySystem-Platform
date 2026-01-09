<template>
  <div class="user-info-card" @click="goToUserInfo">
    <div class="card-header">
      <i class="ri-user-3-line"></i>
      <span>{{ $t('common.sidebar.user.myInfo') }}</span>
    </div>
    <div class="user-profile">
      <div class="user-avatar">
        <img 
          v-if="userInfo.profileImage" 
          :src="userInfo.profileImage" 
          :alt="$t('common.sidebar.user.profileImageAlt')"
        />
        <i v-else class="ri-user-3-fill"></i>
      </div>
      <div class="user-details">
        <div class="user-name">{{ userInfo.name || $t('common.sidebar.user.defaultName') }}</div>
        <div class="user-meta">
          <span v-if="userInfo.department">{{ userInfo.department }}</span>
          <span v-if="userInfo.position"> · {{ userInfo.position }}</span>
        </div>
        <div class="user-contact" v-if="userInfo.email || userInfo.phone">
          <div v-if="userInfo.email" class="contact-item" :title="userInfo.email">
            <i class="ri-mail-line"></i>
            <span class="ellipsis">{{ userInfo.email }}</span>
          </div>
          <div v-if="userInfo.phone" class="contact-item" :title="formattedPhone">
            <i class="ri-phone-line"></i>
            <span class="ellipsis">{{ formattedPhone }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { ROUTES } from '@/config/menuConfig';
import { useAuthStore } from '@/store/auth';
import { useHrmStore } from '@/store/hrm';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { HRM_API_BASE_URL, HRM_SERVICE_NAME } from '@/constants';

const router = useRouter();
const { t } = useI18n();
const authStore = useAuthStore();
const hrmStore = useHrmStore();

/**
 * 전화번호 형식화 함수
 */
function formatPhone(rawNumber) {
  const digits = String(rawNumber || '').replace(/\D/g, '');
  if (digits.length <= 3) return digits;
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7, 11)}`;
}

/**
 * 현재 로그인한 사용자 정보 (hrmStore에서 가져옴)
 */
const userInfo = computed(() => {
  const profile = hrmStore.getMyProfile;
  if (!profile) {
    return {
      name: '',
      department: '',
      position: '',
      email: '',
      phone: '',
      profileImage: ''
    };
  }
  
  // profileImgUrl을 전체 URL로 변환
  const profileImageUrl = profile.profileImgUrl 
    ? getImagePreviewUrl(profile.profileImgUrl, HRM_API_BASE_URL, HRM_SERVICE_NAME)
    : '';
  
  return {
    name: profile.name || '',
    department: profile.departmentName || '',
    position: profile.positionName || '',
    email: profile.email || '',
    phone: profile.phoneNumber || '',
    profileImage: profileImageUrl
  };
});

/**
 * 포맷된 전화번호
 */
const formattedPhone = computed(() => formatPhone(userInfo.value.phone));

/**
 * 내 정보 페이지로 이동
 */
function goToUserInfo() {
  router.push(ROUTES.USER.USER_INFORMATION);
}
</script>

<style scoped>
.user-info-card {
  margin: 12px 5px 12px 15px;
  padding: 16px;
  background: var(--theme-card-bg);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--theme-border);
  cursor: pointer;
  will-change: opacity, transform;
  transform: translate3d(0, 0, 0);
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s, 
              visibility 0s linear,
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s;
}

.user-info-card:hover {
  transform: translate3d(0, -2px, 0);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: -15px -16px 15px -16px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--theme-border);
  border-top-left-radius: 12px !important;
  border-top-right-radius: 12px !important;
}

.card-header i {
  font-size: 14px;
  color: #667eea;
}

.card-header span {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--theme-text-primary);
}

.user-profile {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.ri-user-3-fill {
  color: #9b9b9bcc !important;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
  border: 1px solid #cacaca;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar i {
  font-size: 24px;
  color: white;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--theme-text-primary);
}

.user-meta {
  font-size: 0.7rem;
  color: var(--theme-text-secondary);
  line-height: 1.3;
}

.user-contact {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-top: 4px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.65rem;
  color: var(--theme-text-secondary);
}

.contact-item i {
  font-size: 11px;
  color: #a0aec0;
  flex-shrink: 0;
}

.contact-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ellipsis {
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  white-space: nowrap !important;
  display: block !important;
}
</style>

