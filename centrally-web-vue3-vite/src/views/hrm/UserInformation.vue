<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div
      v-if="isPasswordVerified"
      class="content content-wrapper"
      :class="{ 'center-vertically': !canAccessReceiptFeature && !mobile }"
    >
      <!-- 모바일 전용 프로필 이미지 + 업로드 버튼 -->
      <div
        v-if="mobile"
        class="mobile-profile-block d-flex flex-column align-items-center"
      >
        <div class="mobile-avatar mb-1">
          <div class="mobile-avatar__ring"></div>
          <img
            v-if="profileImageUrl"
            :src="profileImageUrl"
            :alt="$t('common.label.profileImage') || 'profile'"
            class="mobile-avatar__img"
          />
          <div v-else class="mobile-avatar__placeholder">
            <i class="ri-user-3-fill"></i>
          </div>
          <button
            type="button"
            class="mobile-avatar__upload"
            :aria-label="$t('hrm.userInformation.actions.uploadPhoto')"
            @click="triggerImageUpload"
          >
            <i class="ri-camera-line"></i>
          </button>
        </div>
        <input
          type="file"
          ref="imageInput"
          accept="image/*"
          @change="onImageSelected"
          hidden
        />
        <button type="button" class="mobile-avatar__link" @click="openResetModal">
          {{ $t('hrm.userInformation.actions.resetToDefault') }}
        </button>
      </div>

      <div v-if="!mobile">
        <PageTitle 
          :title="$t('hrm.userInformation.title')" 
          :subtitle="$t('hrm.userInformation.subtitle')"
          icon="ri-user-line"
          desktopMarginBottom="30px"
        />
        
        <form @submit.prevent="handleSubmit" class="modern-form">
          <!-- 안내 문구 -->
          <InfoNotice :text="$t('hrm.userInformation.notice')" marginBottom="15px" />

          <!-- 프로필 헤더 섹션 -->
          <div class="profile-header box-shadow-gray">
            <div class="profile-header__avatar">
              <div class="avatar-container">
                <img
                  v-if="profileImageUrl"
                  :src="profileImageUrl"
                  :alt="$t('common.label.profileImage') || 'profile'"
                  class="avatar-image"
                />
                <div v-else class="avatar-placeholder">
                  <i class="ri-user-3-fill"></i>
                </div>
                <button
                  type="button"
                  class="avatar-upload-btn"
                  @click="triggerImageUpload"
                  :title="$t('hrm.userInformation.actions.uploadPhoto')"
                >
                  <i class="ri-camera-line"></i>
                </button>
              </div>
              <input
                type="file"
                ref="imageInput"
                accept="image/*"
                @change="onImageSelected"
                hidden
              />
              <button 
                type="button" 
                class="reset-image-link" 
                @click="openResetModal"
              >
                {{ $t('hrm.userInformation.actions.resetToDefaultLong') }}
              </button>
            </div>
            
            <div class="profile-header__info">
              <div class="info-title">
                <h1 class="user-name">{{ userName.split('(')[0].trim() }}</h1>
                <IconBadge :color="statusBadgeColor" :icon="statusBadgeIcon">
                  {{ userStatusLabel }}
                </IconBadge>
              </div>
              <div class="info-meta">
                <div class="meta-item">
                  <i class="ri-mail-line"></i>
                  <span>{{ userName.match(/\((.*?)\)/)?.[1] || '' }}</span>
                </div>
                <div class="meta-item">
                  <i class="ri-briefcase-line"></i>
                  <span>{{ position }}</span>
                </div>
                <div class="meta-item">
                  <i class="ri-building-line"></i>
                  <span>{{ department }} › {{ team }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 권한 정보 카드 -->
          <div class="info-card info-card--full box-shadow-gray">
            <div class="card-header">
              <i class="ri-shield-keyhole-line"></i>
              <h3>{{ $t('hrm.userInformation.sections.myPermissions') }}</h3>
            </div>
            <div class="card-body">
              <div v-if="authStore.getRoles && authStore.getRoles.length > 0" class="roles-grid">
                <div 
                  v-for="role in authStore.getRoles" 
                  :key="role"
                  class="role-badge"
                  :class="getRoleBadgeClass(role)"
                >
                  <i :class="getRoleIcon(role)"></i>
                  <div class="role-info">
                    <span class="role-name">{{ getRoleLabel(role) }}</span>
                    <span class="role-code">{{ role }}</span>
                  </div>
                </div>
              </div>
              <div v-else class="no-roles">
                <i class="ri-information-line"></i>
                <span>{{ $t('hrm.userInformation.empty.noPermissions') }}</span>
              </div>
            </div>
          </div>

          <!-- 정보 카드 (단일 너비) -->
          <div class="info-card info-card--full box-shadow-gray">
            <div class="card-header">
              <i class="ri-user-line"></i>
              <h3>{{ $t('hrm.userInformation.sections.detailInfo') }}</h3>
            </div>
            <div class="card-body">
              <div class="info-grid-2col">
                <!-- 왼쪽 열 -->
                <div class="info-column">
                  <div class="info-row">
                    <div class="info-label">
                      <i class="ri-calendar-2-line"></i>
                      <span>{{ $t('hrm.userInformation.sections.birthDate') }}</span>
                    </div>
                    <div class="info-value">
                      <span v-if="!editingDob">{{ dob || $t('hrm.userInformation.labels.unregistered') }}</span>
                      <input
                        v-else
                        type="date"
                        v-model="dob"
                        @change="onDobChange"
                        class="modern-date-input"
                      />
                      <button
                        type="button"
                        class="edit-icon-btn"
                        @click="editingDob = !editingDob;"
                      >
                        <i :class="!editingDob ? 'ri-pencil-line' : 'ri-check-line'"></i>
                      </button>
                    </div>
                  </div>

                  <div class="info-row">
                    <div class="info-label">
                      <i class="ri-run-line"></i>
                      <span>{{ $t('hrm.userInformation.sections.joinDate') }}</span>
                    </div>
                    <div class="info-value">
                      <span>{{ joinDate || $t('hrm.userInformation.labels.unregistered') }}</span>
                    </div>
                  </div>
                </div>

                <!-- 오른쪽 열 -->
                <div class="info-column">
                  <div class="info-row">
                    <div class="info-label">
                      <i class="ri-smartphone-line"></i>
                      <span>{{ $t('hrm.userInformation.sections.contact') }}</span>
                    </div>
                    <div class="info-value">
                      <a
                        v-if="!editingPhone"
                        :href="phone ? `tel:${phone}` : undefined"
                        class="phone-link"
                        :class="{ 'phone-empty': !phone }"
                      >
                        <i class="ri-phone-line"></i>
                        {{ formattedPhone || $t('hrm.userInformation.labels.unregistered') }}
                      </a>
                      <DefaultTextfield
                        v-else
                        type="text"
                        id="desk-phone"
                        v-model="phone"
                        validationType="number"
                        :placeholder="$t('hrm.userInformation.labels.phonePlaceholder')"
                        size="full"
                        customHeight="30px"
                        style="width: 180px"
                      />
                      <button
                        type="button"
                        class="edit-icon-btn"
                        @click="toggleEditPhone"
                        :title="editingPhone ? $t('common.button.save') : $t('common.button.edit')"
                      >
                        <i :class="!editingPhone ? 'ri-pencil-line' : 'ri-check-line'"></i>
                      </button>
                      <button
                        type="button"
                        class="action-btn action-btn--primary"
                        :disabled="!phone"
                        @click="copyPhoneToClipboard"
                        :title="$t('hrm.userInformation.actions.copy')"
                      >
                        <i class="ri-file-copy-line"></i>
                      </button>
                    </div>
                  </div>

                  <div class="info-row">
                    <div class="info-label">
                      <i class="ri-shield-keyhole-line"></i>
                      <span>{{ $t('hrm.userInformation.sections.linkedSocial') }}</span>
                    </div>
                    <div class="info-value">
                      <span class="social-count">
                        {{ socialLogins.length > 0 ? $t('hrm.userInformation.social.linkedCount', { count: socialLogins.length }) : $t('hrm.userInformation.empty.noLinkedAccounts') }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 소셜 로그인 정보 -->
          <div v-if="socialLogins.length > 0" class="social-card">
            <div class="card-header">
              <i class="ri-link"></i>
              <h3>{{ $t('hrm.userInformation.sections.linkedSocial') }}</h3>
            </div>
            <div class="card-body">
              <div
                v-for="(social, index) in socialLogins"
                :key="index"
                class="social-item"
              >
                <div class="social-info">
                  <i class="ri-shield-check-line"></i>
                  <div class="social-details">
                    <span class="social-provider">{{ social.socialName }}</span>
                    <span class="social-email">{{ social.socialEmail }}</span>
                  </div>
                </div>
                <button
                  type="button"
                  class="social-remove-btn"
                  @click="deleteSocialLogin(index, social)"
                  :title="$t('hrm.userInformation.social.unlinkTitle')"
                >
                  <i class="ri-close-line"></i>
                </button>
              </div>
            </div>
          </div>

          <!-- Receipt 섹션 -->
          <div v-if="canAccessReceiptFeature" class="receipt-section">
            <UserReceiptSection :mobile="false" />
          </div>

          <!-- 액션 버튼 -->
          <div class="action-bar">
            <DefaultButton 
              size="small" 
              @click="openPasswordModal"
              class="action-bar__btn"
            >
              <i class="ri-lock-password-line"></i>&nbsp;
              {{ $t('hrm.userInformation.actions.changePassword') }}
            </DefaultButton>
          </div>
        </form>
      </div>

      <!-- 모바일 화면일 때 -->
      <div v-else>
        <form @submit.prevent="handleSubmit" class="form-content">
          <div class="card-layout">
            <div :class="cardClass">
              <div class="bm-card__header">
                <i class="ri-id-card-line"></i>
                <p class="bm-card__title">{{ $t('hrm.userInformation.sections.basicInfo') }}</p>
              </div>
              <div class="bm-card__body">
                <div class="bm-chips">
                  <IconBadge :color="statusBadgeColor" :icon="statusBadgeIcon">{{ userStatusLabel }}</IconBadge>
                  <span class="chip chip--mint">{{ position }}</span>
                  <span class="chip chip--dark">{{ userName }}</span>
                </div>
                <div class="bm-crumb">
                  <i class="ri-building-line mobile-icon"></i>
                  <span>{{ department }}</span>
                  <span class="sep">›</span>
                  <span>{{ team }}</span>
                </div>

                <div class="bm-row">
                  <div class="bm-row__left">
                    <i class="ri-calendar-2-line mobile-icon"></i>
                    <span>{{ $t('hrm.userInformation.sections.birthDate') }}</span>
                  </div>
                  <div class="bm-row__right">
                    <span v-if="!editingDob" class="bm-row__value">{{
                      dob || $t('hrm.userInformation.labels.unregistered')
                    }}</span>
                    <input
                      v-else
                      type="date"
                      v-model="dob"
                      @change="onDobChange"
                      class="date-input"
                    />
                    <button
                      type="button"
                      class="edit-btn"
                      @click="editingDob = !editingDob;"
                    >
                      <i class="ri-pencil-line"></i>
                    </button>
                  </div>
                </div>

                <div class="bm-row">
                  <div class="bm-row__left">
                    <i class="ri-run-line mobile-icon"></i>
                    <span>{{ $t('hrm.userInformation.sections.joinDate') }}</span>
                  </div>
                  <div class="bm-row__right">
                    <span class="bm-row__value">{{
                      joinDate || $t('hrm.userInformation.labels.unregistered')
                    }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="phone-card">
            <div class="phone-card__left">
              <i class="ri-smartphone-line"></i>
              <span class="phone-card__title">{{ $t('hrm.userInformation.sections.contact') }}</span>
            </div>
            <div class="phone-card__center">
              <a
                v-if="!editingPhone"
                :href="phone ? `tel:${phone}` : undefined"
                class="phone-card__number"
                :class="{ 'phone-card__number--empty': !phone }"
              >
                {{ formattedPhone || $t('hrm.userInformation.labels.unregistered') }}
              </a>
              <DefaultTextfield
                v-else
                type="text"
                id="phone"
                v-model="phone"
                validationType="number"
                :placeholder="$t('hrm.userInformation.labels.phonePlaceholder')"
                size="full"
                customHeight="30px"
                style="width: 140px"
              />
            </div>
            <div class="phone-card__right">
              <button type="button" class="icon-btn" @click="toggleEditPhone">
                <i v-if="!editingPhone" class="ri-pencil-line"></i>
                <i v-else class="ri-check-line"></i>
              </button>
              <button
                type="button"
                class="icon-btn call"
                :disabled="!phone"
                @click="copyPhoneToClipboard"
              >
                <i class="ri-file-copy-line"></i>
              </button>
            </div>
          </div>

          <!-- 소셜 로그인 정보 -->
          <div v-if="socialLogins.length > 0" class="mobile-social-card">
            <div class="mobile-social-card__header">
              <i class="ri-link"></i>
              <p class="mobile-social-card__title">{{ $t('hrm.userInformation.sections.linkedSocial') }}</p>
            </div>
            <div class="mobile-social-card__body">
              <div
                v-for="(social, index) in socialLogins"
                :key="index"
                class="mobile-social-item"
              >
                <div class="mobile-social-info">
                  <i class="ri-shield-check-line"></i>
                  <div class="mobile-social-details">
                    <span class="mobile-social-provider">{{ social.socialName }}</span>
                    <span class="mobile-social-email">{{ social.socialEmail }}</span>
                  </div>
                </div>
                <button
                  type="button"
                  class="mobile-social-remove-btn"
                  @click="deleteSocialLogin(index, social)"
                  :title="$t('hrm.userInformation.social.unlinkTitle')"
                >
                  <i class="ri-close-line"></i>
                </button>
              </div>
            </div>
          </div>

          <!-- 모바일: 권한 정보 카드 -->
          <div class="mobile-roles-card box-shadow-gray">
            <div class="mobile-roles-card__header">
              <i class="ri-shield-keyhole-line"></i>
              <p class="mobile-roles-card__title">{{ $t('hrm.userInformation.sections.myPermissions') }}</p>
            </div>
            <div class="mobile-roles-card__body">
              <div v-if="authStore.getRoles && authStore.getRoles.length > 0">
                <div 
                  v-for="role in authStore.getRoles" 
                  :key="role"
                  class="mobile-social-item"
                >
                  <div class="mobile-social-info">
                    <i :class="[getRoleIcon(role), 'mobile-role-icon', getRoleBadgeClass(role)]"></i>
                    <div class="mobile-social-details">
                      <span class="mobile-social-provider">{{ getRoleLabel(role) }}</span>
                      <span class="mobile-social-email">{{ role }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="mobile-no-roles">
                <i class="ri-information-line"></i>
                <span>{{ $t('hrm.userInformation.empty.noPermissions') }}</span>
              </div>
            </div>
          </div>

          <!-- 모바일: 영수증 섹션 컴포넌트 렌더링 -->
          <div v-if="canAccessReceiptFeature">
            <UserReceiptSection :mobile="true" />
          </div>

          <hr />

          <DefaultFormRow marginBottom="15px" align="right">
            <DefaultButton size="small" @click="openPasswordModal" marginRight="5px">
              <i class="ri-lock-password-line"></i>&nbsp;
              {{ $t('hrm.userInformation.actions.changePassword') }}
            </DefaultButton>
          </DefaultFormRow>
        </form>
      </div>
    </div>

    <!-- 카카오 주소 모달 -->
    <KakaoAddressModal
      :visible="isModalOpen"
      @close="closeModal"
      @selectAddress="handleSelectAddress"
    />

    <PasswordVerificationModal
      v-if="!isPasswordVerified"
      :visible="isPasswordModalVisible"
      :userId="authStore.getUserId"
      @close=""
      @verified="onPasswordVerified"
    />

    <UserPasswordModifyModal
      v-if="userId !== null"
      :visible="isPasswordChangeModalVisible"
      :userId="userId"
      @close="closePasswordChangeModal"
    />

    <!-- 이미지 업로드 확인 모달 -->
    <AlertModal
      :isVisible="isImageModalVisible"
      :title="$t('hrm.userInformation.modals.imageChangeTitle')"
      :confirmText="$t('common.button.confirm')"
      :cancelText="$t('common.button.cancel')"
      @confirm="confirmImageUpload"
      @close="cancelImageUpload"
    >
      <template #body> {{ $t('hrm.userInformation.modals.imageChangeBody') }} </template>
    </AlertModal>

    <!-- 기본 이미지 확인 모달 -->
    <AlertModal
      :isVisible="isResetModalVisible"
      :title="$t('hrm.userInformation.modals.resetImageTitle')"
      :confirmText="$t('common.button.confirm')"
      :cancelText="$t('common.button.cancel')"
      @confirm="confirmResetImage"
      @close="cancelResetImage"
    >
      <template #body> {{ $t('hrm.userInformation.modals.resetImageBody') }} </template>
    </AlertModal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import KakaoAddressModal from '@/components/common/kakao/address/KakaoAddressModal.vue';
import UserPasswordModifyModal from '@/components/auth/UserPasswordModifyModal.vue';
import PasswordVerificationModal from '@/components/auth/PasswordVerificationModal.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import UserReceiptSection from '@/components/receipt/UserReceiptSection.vue';
import IconBadge from '@/components/common/badge/IconBadge.vue';
import PageTitle from '@/components/common/title/PageTitle.vue';
import InfoNotice from '@/components/common/notice/InfoNotice.vue';

import { toast } from 'vue3-toastify';
import { useAuthStore } from '@/store/auth';
import { processImageFile } from '@/utils/imageProcessor.js';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { HRM_API_BASE_URL, HRM_SERVICE_NAME } from '@/constants';
import { 
  getRolesFrom, 
  hasRole as checkRole,
  getRoleLabel,
  getRoleIcon,
  getRoleBadgeClass,
  isLocalEnv
} from '@/utils/roleUtils';
import { ROLE_GROUPS } from '@/config/roleConfig';

const { t } = useI18n();
// API
import AuthUserApi from '@/api/auth/UsersApi';
import HrmUserApi from '@/api/hrm/UsersApi';
import StylesApi from '@/api/hrm/StylesApi';
import { STYLE_CATEGORY, INFO_MOBILE_STYLE, getInfoMobileStyleLabel } from '@/constants';
// HrmUserApi는 이미 상단에서 import되어 있으면 재선언하지 않음

// Vuex/Pinia
const authStore = useAuthStore();

// 영수증 기능 활성화 여부
const isReceiptFeatureEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';

/**
 * 권한 체크 함수 (roleUtils.js의 hasRole 사용)
 * @param {Array} allowed 허용할 권한 목록
 * @returns {boolean} 권한 보유 여부
 */
function hasRole(allowed) {
  const roles = authStore.getRoles || [];
  return checkRole(roles, allowed);
}

/**
 * 영수증 권한 확인 (NavigationLayout과 동일한 로직)
 */
const CAN_RECEIPT_WRITE = computed(() =>
  isReceiptFeatureEnabled && hasRole(getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR))
);

const canAccessReceiptFeature = computed(() => {
  const result = CAN_RECEIPT_WRITE.value;
  
  console.log('🔍 영수증 권한 체크:', {
    isReceiptFeatureEnabled,
    userRoles: authStore.getRoles,
    requiredRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
    isLocal: isLocalEnv(),
    result
  });
  
  // 임시: 디버깅용으로 강제 true (실제 배포시 제거 필요)
  // return true;
  
  return result;
});

// ===== 상태 정의 =====
const userId = ref(null);
const userName = ref('');
const department = ref('');
const team = ref('');
const position = ref('');
const phone = ref('');
const dob = ref('');
const joinDate = ref('');
const userStatusRaw = ref('');
const socialLogins = ref([]);
const editingDob = ref(false); // ← 달력 열림 여부
const editingPhone = ref(false);

const originalUserData = ref({});

// 기본 정보 모바일 테마
const infoMobileStyle = ref(INFO_MOBILE_STYLE.LIGHT); // 기본값

// getRoleLabel, getRoleIcon, getRoleBadgeClass는 roleUtils.js에서 import하여 사용

/* 달력 닫기 헬퍼 */
function closePickers() {
  editingDob.value = false;
}
function onDobChange() {
  closePickers();
  saveUser(); // 날짜 고르자마자 PATCH
}
/**
 * 사용자 상태 라벨 정규화 (영문 코드 → 한글 라벨)
 * @param {string} raw 원본 상태 문자열
 * @returns {string} 한글 라벨(재직/휴직/퇴사/비활성 등)
 */
function normalizeStatusLabel(raw) {
  const value = (raw || '').toString().trim();
  const upper = value.toUpperCase();
  if (!value) return '재직';
  if (['ACTIVE', 'ENABLED', 'EMPLOYED'].includes(upper)) return '재직';
  if (['LEAVE', 'ON_LEAVE'].includes(upper)) return '휴직';
  if (['RESIGNED', 'TERMINATED'].includes(upper)) return '퇴사';
  if (['INACTIVE', 'DISABLED'].includes(upper)) return '비활성';
  // 이미 한글일 수 있음
  if (['재직', '휴직', '퇴사', '비활성'].includes(value)) return value;
  return value;
}

/**
 * 상태 라벨에 따른 뱃지 표현 매핑
 * @param {string} label 한글 상태 라벨
 * @returns {{color: string, icon: string}}
 */
function getStatusPresentation(label) {
  switch (label) {
    case '재직':
      return { color: 'success', icon: 'ri-checkbox-circle-line' };
    case '휴직':
      return { color: 'warning', icon: 'ri-pause-circle-line' };
    case '퇴사':
      return { color: 'secondary', icon: 'ri-logout-circle-line' };
    case '비활성':
      return { color: 'danger', icon: 'ri-close-circle-line' };
    default:
      return { color: 'info', icon: 'ri-information-line' };
  }
}

/**
 * 상태 라벨(표시용)
 */
const userStatusLabel = computed(() => normalizeStatusLabel(userStatusRaw.value));

/**
 * 상태 뱃지 컬러
 */
const statusBadgeColor = computed(() => getStatusPresentation(userStatusLabel.value).color);

/**
 * 상태 뱃지 아이콘
 */
const statusBadgeIcon = computed(() => getStatusPresentation(userStatusLabel.value).icon);
/**
 * 휴대전화 번호 하이픈 포맷 변환 (예: 01012345678 → 010-1234-5678)
 * @param {string} rawNumber 숫자 문자열
 * @returns {string} 하이픈 포함 포맷 문자열
 */
function formatPhone(rawNumber) {
  const digits = String(rawNumber || '').replace(/\D/g, '');
  if (digits.length <= 3) return digits;
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7, 11)}`;
}

/**
 * 포맷된 휴대전화 번호 계산 속성
 * @returns {string} formatted phone
 */
const formattedPhone = computed(() => formatPhone(phone.value));

/**
 * 휴대전화 번호 복사
 */
function copyPhoneToClipboard() {
  if (!phone.value) return;
  navigator.clipboard
    .writeText(phone.value)
    .then(() => toast.success(t('hrm.userInformation.toasts.copySuccess')))
    .catch(() => toast.error(t('hrm.userInformation.toasts.copyFailed')));
}

/**
 * 휴대전화 걸기 (모바일에서 다이얼러 오픈)
 */
function dialPhone() {
  if (!phone.value) return;
  window.location.href = `tel:${phone.value}`;
}

/**
 * 연락처 편집 토글 (편집 종료 시 저장)
 */
function toggleEditPhone() {
  if (editingPhone.value) {
    saveUser();
  }
  editingPhone.value = !editingPhone.value;
}

// ===== 프로필 이미지 관련 =====
// 최초에는 이미지가 없도록 빈 문자열로 설정
const profileImageUrl = ref('');
const imageInput = ref(null);
const previousImageUrl = ref('');
// 업로드 전 임시 저장 파일 & 모달 상태
const pendingFile = ref(null);
const pendingPreview = ref('');
const isImageModalVisible = ref(false);
const isResetModalVisible = ref(false);

/**
 * 이미지 업로드 트리거
 */
function triggerImageUpload() {
  if (imageInput.value) {
    imageInput.value.click();
  }
}

/**
 * 이미지 파일 선택 시 압축 처리
 * @param {Event} event 파일 선택 이벤트
 */
async function onImageSelected(event) {
  const file = event.target.files[0];
  if (!file) return;

  // 이미지 압축 처리 (프로필 이미지는 작게)
  let processedFile;
  try {
    processedFile = await processImageFile(file, { minSizeMB: 0.05, maxSizeMB: 0.15, maxWidthOrHeight: 300 });
  } catch (err) {
    if (err.message === 'FILE_SIZE_EXCEEDED') {
      toast.error(t('hrm.userInformation.toasts.imageTooLarge'));
    } else {
      toast.error(t('hrm.userInformation.toasts.imageProcessError'));
      console.error(err);
    }
    // 파일 input 초기화
    if (imageInput.value) imageInput.value.value = null;
    return;
  }

  // 미리보기만 먼저 보여주고 확정 모달 표시
  previousImageUrl.value = profileImageUrl.value;
  pendingFile.value = processedFile;
  pendingPreview.value = URL.createObjectURL(processedFile);
  profileImageUrl.value = pendingPreview.value;
  isImageModalVisible.value = true;
}

function confirmImageUpload() {
  if (!pendingFile.value || !userId.value) {
    toast.error(t('hrm.userInformation.toasts.noFileOrUser'));
    return;
  }
  HrmUserApi.uploadProfileImage(userId.value, pendingFile.value)
    .then((res) => {
      if (res && res.data && res.data.url) {
        // 백엔드에서 받은 상대 경로를 게이트웨이를 거치는 전체 URL로 변환
        const fullUrl = getImagePreviewUrl(res.data.url, HRM_API_BASE_URL, HRM_SERVICE_NAME);
        profileImageUrl.value = fullUrl;
        authStore.userProfileImgUrl = fullUrl;
      }
      toast.success(t('hrm.userInformation.toasts.profileSaved'));
    })
    .catch(() => {
      toast.error(t('hrm.userInformation.toasts.uploadFailed'));
      // 실패 시 원상복구
      profileImageUrl.value = '';
    })
    .finally(() => {
      cleanupPending();
    });
}

function cancelImageUpload() {
  // 미리보기를 제거하고 pending 정리
  profileImageUrl.value = previousImageUrl.value;
  cleanupPending();
}

function cleanupPending() {
  if (pendingPreview.value) URL.revokeObjectURL(pendingPreview.value);
  pendingFile.value = null;
  pendingPreview.value = '';
  isImageModalVisible.value = false;
  if (imageInput.value) imageInput.value.value = null;
}
function openResetModal() {
  isResetModalVisible.value = true;
}

function confirmResetImage() {
  if (!userId.value) {
    toast.error(t('hrm.userInformation.toasts.noUser'));
    return;
  }
  HrmUserApi.patchUser(userId.value, { profileImgId: null })
    .then(() => {
      if (profileImageUrl.value && profileImageUrl.value.startsWith('blob:'))
        URL.revokeObjectURL(profileImageUrl.value);
      fetchMetadata();
      toast.success(t('hrm.userInformation.toasts.resetSuccess'));
    })
    .catch(() => toast.error(t('hrm.userInformation.toasts.resetFailed')))
    .finally(() => {
      isResetModalVisible.value = false;
      if (imageInput.value) imageInput.value.value = null;
    });
}

function cancelResetImage() {
  isResetModalVisible.value = false;
}

function resetImage() {
  // 객체 URL 해제
  if (profileImageUrl.value && profileImageUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(profileImageUrl.value);
  }
  profileImageUrl.value = '';
  if (imageInput.value) {
    imageInput.value.value = null;
  }
}

/* ---------- 650px 이하 여부 ---------- */
const mq = window.matchMedia('(max-width:650px)');
const mobile = ref(mq.matches);
mq.addEventListener('change', (e) => (mobile.value = e.matches));

// 주소 관련
const zonecode = ref('');
const address = ref('');
const detailAddress = ref('');
const isModalOpen = ref(false);

// 비밀번호 변경용 변수
// const newPassword = ref('');       // 새 비번 입력
// const confirmPassword = ref('');   // 새 비번 확인

// “비번 불일치” 여부 계산
// const passwordMismatch = computed(() => {
//   // 둘 중 하나만 입력했거나, 둘 다 입력했으나 값이 다르면 true
//   return newPassword.value !== ''
//     && confirmPassword.value !== ''
//     && newPassword.value !== confirmPassword.value;
// });

// 비밀번호 변경 모달 상태 및 제어 함수
const isPasswordModalVisible = ref(false);
const isPasswordChangeModalVisible = ref(false);
const isPasswordVerified = ref(false);

/**
 * 비밀번호 변경 모달 열기
 */
function openPasswordModal() {
  if (userId.value) {
    isPasswordChangeModalVisible.value = true;
  } else {
    toast.error(t('hrm.userInformation.toasts.userNotLoaded'));
  }
}

/**
 * 비밀번호 변경 모달 닫기
 */
function closePasswordChangeModal() {
  isPasswordChangeModalVisible.value = false;
}

/**
 * 비밀번호 인증 성공 처리
 */
function onPasswordVerified() {
  isPasswordVerified.value = true;
  isPasswordModalVisible.value = false;
  // 인증 성공 후에만 데이터 로드
  fetchMetadata();
}
/**
 * 비밀번호 변경 모달 닫기
 */
function closePasswordModal() {
  isPasswordModalVisible.value = false;
}

// 모달 열기/닫기
function openModal() {
  isModalOpen.value = true;
}
function closeModal() {
  isModalOpen.value = false;
}
function handleSelectAddress(payload) {
  address.value = payload.address;
  zonecode.value = payload.zipCode;
}

// 폼 제출
const handleSubmit = () => {
  console.log('Form submitted');
};

// 소셜 로그인 삭제
async function deleteSocialLogin(index, socialItem) {
  if (userId.value && socialItem.socialName) {
    await AuthUserApi.deleteUserSocial(userId.value, socialItem.socialName);
    toast.success(t('hrm.userInformation.toasts.socialDeleted', { provider: socialItem.socialName }));
  }
  fetchMetadata();
}

// 사용자 정보 저장
function saveUser() {
  // 숫자만 입력되는지 확인
  const numberRegex = /^[0-9]+$/;
  if (phone.value && !numberRegex.test(phone.value)) {
    toast.error(t('hrm.userInformation.toasts.phoneNumberOnly'));
    return;
  }
  if (zonecode.value && !numberRegex.test(zonecode.value)) {
    toast.error(t('hrm.userInformation.toasts.zipNumberOnly'));
    return;
  }
  // 비밀번호 불일치 체크
  // let typedPassword = (newPassword.value || confirmPassword.value);
  // if (typedPassword) {
  //   // 하나만 입력
  //   if (!newPassword.value || !confirmPassword.value) {
  //     toast.error("'새 비밀번호'와 '비밀번호 확인'을 둘 다 입력해주세요.");
  //     return;
  //   }
  //   // 값이 다르면
  //   if (newPassword.value !== confirmPassword.value) {
  //     toast.error("비밀번호가 일치하지 않습니다.");
  //     return;
  //   }
  // }

  // 변경된 필드만 모아서 업데이트
  const updates = {};

  if (phone.value !== originalUserData.value.phone) {
    updates.phoneNumber = phone.value;
  }
  if (dob.value !== originalUserData.value.dob) {
    updates.birth = dob.value;
  }
  if (joinDate.value !== originalUserData.value.joinDate) {
    updates.joiningDate = joinDate.value;
  }
  if (detailAddress.value !== originalUserData.value.detailAddress) {
    updates.addressDetail = detailAddress.value;
  }
  if (address.value !== originalUserData.value.address) {
    updates.address = address.value;
  }
  if (zonecode.value !== originalUserData.value.zonecode) {
    updates.zipCode = zonecode.value;
  }

  // “새 비밀번호”가 입력된 경우만 업데이트 (빈 값이면 변경 없음)
  // if (newPassword.value) {
  //   updates.password = newPassword.value;
  // }

  if (Object.keys(updates).length > 0) {
    HrmUserApi.patchUser(userId.value, updates).then(() => {
      toast.success(t('hrm.userInformation.toasts.saved'));
      // 원본 데이터 업데이트
      originalUserData.value = {
        ...originalUserData.value,
        dob: dob.value,
        phone: phone.value,
        joinDate: joinDate.value,
        detailAddress: detailAddress.value,
        address: address.value,
        zonecode: zonecode.value,
      };
      fetchMetadata();
    });
  } else {
    toast.info(t('hrm.userInformation.toasts.noChanges'));
  }
}

/**
 * 사용자 스타일 로드
 */
async function loadUserStyles() {
  const styles = await StylesApi.getUserStyles(authStore.userId);
  infoMobileStyle.value = styles[STYLE_CATEGORY.INFO_MOBILE] || INFO_MOBILE_STYLE.LIGHT;
}

/**
 * 기본 정보 모바일 스타일 변경
 */
async function changeInfoMobileStyle(styleCode) {
  await StylesApi.updateUserStyle(authStore.userId, STYLE_CATEGORY.INFO_MOBILE, styleCode);
  infoMobileStyle.value = styleCode;
  toast.success(t('hrm.userInformation.toasts.styleChanged'));
}

/**
 * 동적 CSS 클래스 계산 (항상 bm-card, 테마는 CSS 변수로 처리)
 */
const cardClass = computed(() => {
  return 'bm-card';
});

// ========== 데이터 로딩 ==========
// 1) HrmUserApi.getUserById(userId) → 유저 기본정보 + team + department + position
// 2) AuthUserApi.getUserSocialByEmail(userEmail) → 소셜 로그인 목록
const fetchMetadata = async () => {
  // (1) HRM 유저 정보
  const hrmRes = await HrmUserApi.getUserById(authStore.getUserId);
  if (hrmRes && hrmRes.data) {
    const user = hrmRes.data;
    // user에 team, position이 이미 들어 있음

    userId.value = user.userId;
    userName.value = `${user.name} (${user.email})`;
    // newPassword.value = user.password;
    // confirmPassword.value = user.password;
    phone.value = user.phoneNumber || '';
    dob.value = user.birth || '';
    joinDate.value = user.joiningDate || '';

    // 부서/팀/직급 값이 없으면 "미지정"으로 설정
    department.value =
      user.team?.department?.departmentId === 0
        ? '미지정'
        : user.team?.department?.departmentName || '미지정';
    team.value = user.team?.teamId === 0 ? '미지정' : user.team?.teamName || '미지정';
    position.value =
      user.position?.positionId === 0
        ? '미지정'
        : user.position?.positionName || '미지정';

    zonecode.value = user.zipCode ? String(user.zipCode) : '';

    // 상태: 가능한 필드 우선순위로 선택 (employmentStatus → status → enabled/disabled)
    userStatusRaw.value =
      user.employmentStatus || user.status || (user.enabled === false ? '비활성' : '재직');

    // 프로필 이미지 URL 세팅 (있을 시 보여줌)
    if (user.profileImg && user.profileImg.fileUrl) {
      profileImageUrl.value = getImagePreviewUrl(user.profileImg.fileUrl, HRM_API_BASE_URL, HRM_SERVICE_NAME);
      // 기본 이미지로 변경 시 패치하며 헤더의 프로필 이미지와 동기화
      authStore.userProfileImgUrl = profileImageUrl.value;
    }
    address.value = user.address || '';
    detailAddress.value = user.addressDetail || '';

    // 원본 데이터 저장
    originalUserData.value = {
      phone: phone.value,
      joinDate: joinDate.value,
      dob: dob.value,
      zonecode: zonecode.value,
      address: address.value,
      detailAddress: detailAddress.value,
      department: department.value,
      team: team.value,
      position: position.value,
      userName: userName.value,
      profileImageUrl: profileImageUrl.value,
    };
  }

  // (2) 소셜 로그인 정보
  const socialRes = await AuthUserApi.getUserSocialByEmail(authStore.getUserEmail);
  if (socialRes && socialRes.data) {
    socialLogins.value = socialRes.data.socialLogins || [];
  }
};

onMounted(() => {
  setTimeout(() => {
    isPasswordModalVisible.value = true;
  });
  loadUserStyles();
});
</script>

<style scoped>
hr {
  margin: 10px 0 10px 0;
}

.hr-line {
  margin: 20px 0;
}

.center-vertically {
  min-height: calc(100vh - 130px - 80px);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.social-label {
  width: 150px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

label {
  margin-bottom: 10px;
}

#zonecode {
  max-width: 160px;
}

.form-group,
.form-group-name-phone {
  margin-bottom: 12px;
}

.w-50 {
  width: 50%;
}
.w-33 {
  width: 33%;
}
.col-dept {
  width: 42%;
}
.col-team {
  width: 42%;
}
.col-pos {
  width: 16%;
}
.pr-2 {
  padding-right: 10px;
}
.pl-2 {
  padding-left: 10px;
}
.px-2 {
  padding: 0 10px;
}

/* 프로필 이미지 */
.profile-image-wrapper {
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #ddd;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
}
.profile-image-wrapper img,
.mobile-profile-block img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center center;
  border-radius: 50% !important;
}

/* 데스크톱 기본 정보 그리드 */
.profile-grid {
  display: grid;
  grid-template-columns: 200px 1fr;
  grid-template-areas:
    'photo card'
    'photo card'
    'photo card';
  column-gap: 40px;
  grid-row-gap: 12px;
  margin-bottom: 30px;
  align-items: center;
}

.button-row {
  margin: 0 auto;
  width: 100%;
}

.social-section {
  max-width: 500px;
  margin: 0 auto;
}

.grid-name {
  grid-area: auto;
}
.grid-phone {
  grid-area: auto;
  justify-self: end;
  width: 100%;
}
.grid-namephone {
  grid-area: name;
  display: grid;
  grid-template-columns: 1.2fr 0.5fr;
  gap: 10px;
}
.grid-birth {
  grid-area: birth;
}
.grid-join {
  grid-area: join;
}
.grid-dept {
  grid-area: dept;
}
.grid-name,
.grid-birth,
.grid-dept {
  margin-left: 30px;
}
.profile-image-cell {
  grid-area: photo;
  align-self: center;
}

/* dm-card를 이미지 옆 전 영역으로 배치 */
.dm-card-grid {
  grid-area: card;
}

.profile-image-wrapper {
  width: 180px;
  height: 180px;
  margin-bottom: 22px !important;
}

/* 데스크탑: 기본 정보 카드 */
.info-card {
  background: var(--theme-card-bg);
  padding: 0;
  margin-bottom: 20px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.info-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 800;
  color: var(--theme-text-primary);
  margin-bottom: 20px;
  /* border-bottom: 1px solid #e0e0e0; */
}
.info-card__header i {
  font-size: 2rem;
}
.info-card__title {
  font-size: 1.1rem;
}
.info-card__body {
  padding-top: 6px;
}

.info-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.info-card--full {
  width: 100%;
}

/* 데스크탑 요약 카드 (CSS 변수 사용) */
.dm-card {
  border: 1px solid var(--theme-border);
  background: var(--theme-bg-main);
  border-radius: 12px;
  box-shadow: 0 4px 14px var(--theme-shadow);
  padding: 20px 30px;
  margin-top: 10px;
}
.dm-chips {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.dm-crumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--theme-text-secondary);
  font-size: 0.82rem;
  margin-bottom: 10px;
}
.dm-crumb .sep {
  opacity: 0.6;
}
.dm-icon {
  font-size: 1rem;
  margin-right: 4px;
}
.dm-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-top: 1px dashed #e6efff;
}
.dm-row:first-of-type {
  border-top: none;
}
.dm-row__left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--theme-text-secondary);
  font-weight: 700;
}
.dm-row__right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.dm-row__value {
  color: var(--theme-text-primary);
  font-size: 0.82rem;
}
.dm-date {
  font-size: 0.82rem;
  padding: 6px 8px;
}
.dm-phone {
  color: #111827;
  font-size: 0.82rem;
  text-decoration: none;
  font-weight: 600;
}
.dm-phone--empty {
  color: #94a3b8;
  font-weight: 500;
}
.dm-edit {
  border: none;
  background: transparent;
  cursor: pointer;
  color: #0b6bcb;
}
.dm-icon-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1px solid #cfe6ff;
  background: #ffffff;
  color: #0b6bcb;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0, 140, 255, 0.12);
  cursor: pointer;
}
.dm-icon-btn.call {
  background: #0b6bcb;
  color: #ffffff;
  border-color: #0b6bcb;
}
.dm-icon-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 기존 데스크탑 입력 필드 숨김 (이미지 제외) */
.profile-grid .grid-name,
.profile-grid .grid-phone,
.profile-grid .grid-birth,
.profile-grid .grid-join,
.profile-grid .grid-dept {
  display: none;
}

/* 모바일 프로필 컨테이너 */
.mobile-profile-block {
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
  margin-top: 10px;
}
.mobile-avatar {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: visible;
  background: linear-gradient(180deg, #eaf4ff 0%, #ffffff 100%);
  box-shadow: 0 8px 22px rgba(0, 140, 255, 0.2);
}
.mobile-avatar__ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  /* box-shadow: inset 0 0 0 3px #cfe6ff, inset 0 0 0 6px #eaf4ff; */
  border: 2px solid #ffffff;
  pointer-events: none;
  z-index: 1;
}
.mobile-avatar__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  display: block; /* 이미지 로딩 전 공백 방지 */
  font-size: 0.7rem; /* alt 텍스트 작은 글씨 */
  text-align: center; /* alt 텍스트 중앙 정렬 */
  line-height: 120px; /* 수직 중앙 정렬 (컨테이너 높이와 동일) */
}
.mobile-avatar__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0b6bcb;
  background: #f7fbff;
  font-size: 2.2rem;
  border-radius: 50%;
}
.mobile-avatar__upload {
  position: absolute;
  right: 3px;
  bottom: 3px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: #008cffa6;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 140, 255, 0.4);
  cursor: pointer;
  z-index: 20;
  overflow: visible;
}
.mobile-avatar__link {
  margin-top: 8px;
  font-size: 0.72rem;
  color: #0b6bcb;
  text-decoration: underline;
  background: transparent;
  border: none;
  cursor: pointer;
}

/* Grid Dept 내부 간격 축소 */
.grid-dept .pr-2 {
  padding-right: 0px !important;
}
.grid-dept .pl-2 {
  padding-left: 0px !important;
}
.grid-dept .px-2 {
  padding: 0 5px !important;
}

/* 버튼 가로 정렬 공통 */
.btn-row {
  display: flex;
  gap: 5px;
}

/* ========== 데스크톱: 모던 폼 스타일 ========== */
.modern-form {
  max-width: 1000px;
  padding: 0;
}


/* 프로필 헤더 */
.profile-header {
  display: flex;
  gap: 40px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafb 100%);
  margin-bottom: 20px;
}

.profile-header__avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-container {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: visible;
  border: 3px solid #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: #e6e6e6; /* 이미지 로딩 전 배경 */
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50% !important;
  display: block; /* 이미지 로딩 전 공백 방지 */
  font-size: 0.7rem; /* alt 텍스트 작은 글씨 */
  text-align: center; /* alt 텍스트 중앙 정렬 */
  line-height: 100px; /* 수직 중앙 정렬 (컨테이너 높이와 동일) */
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 2.2rem;
  border-radius: 50%; /* 원형으로 명시 */
}

.avatar-upload-btn {
  position: absolute;
  bottom: 0px;
  right: 0px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #4e46e5bb;
  border: 2px solid #ffffff;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 10;
  overflow: visible;
}

.avatar-upload-btn:hover {
  background: #4338ca;
  transform: scale(1.05);
}

.avatar-upload-btn i {
  font-size: 0.9rem;
}

.reset-image-link {
  background: transparent;
  border: none;
  color: #6b7280;
  font-size: 0.75rem;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.2s;
}

.reset-image-link:hover {
  color: #4f46e5;
}

.profile-header__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.info-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--theme-text-primary);
  margin: 0;
}

.info-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--theme-text-secondary);
  font-size: 0.85rem;
}

.meta-item i {
  color: #9ca3af;
  font-size: 0.95rem;
}

/* 카드 내부 2열 그리드 */
.info-grid-2col {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.info-column {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  background: var(--theme-bg-secondary);
  border-bottom: 1px solid var(--theme-border);
}

.card-header i {
  font-size: 1.1rem;
  color: #4f46e5;
}

.card-header h3 {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--theme-text-primary);
}

.card-body {
  padding: 20px;
}

/* 정보 행 */
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--theme-border);
}

.info-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-row:first-child {
  padding-top: 0;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--theme-text-secondary);
  font-weight: 500;
  font-size: 0.85rem;
}

.info-label i {
  color: #9ca3af;
  font-size: 0.95rem;
}

.info-value {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--theme-text-primary);
  font-size: 0.85rem;
}

.modern-date-input {
  padding: 5px 10px;
  border: 1px solid var(--theme-border);
  border-radius: 6px;
  font-size: 0.85rem;
  color: var(--theme-text-primary);
  outline: none;
  transition: border-color 0.2s;
}

.modern-date-input:focus {
  border-color: #4f46e5;
}

.edit-icon-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: none;
  background: var(--theme-bg-secondary);
  color: #4f46e5;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-icon-btn:hover {
  background: var(--theme-bg-hover);
}

.edit-icon-btn i {
  font-size: 0.9rem;
}

/* 연락처 카드 */
.contact-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.contact-value {
  flex: 1;
}

.phone-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--theme-text-primary);
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 500;
  transition: color 0.2s;
}

.phone-link:hover {
  color: #4f46e5;
}

.phone-link i {
  color: #4f46e5;
  font-size: 1rem;
}

.phone-empty {
  color: #9ca3af;
}

.phone-empty i {
  color: #9ca3af;
}

.social-count {
  font-size: 0.85rem;
  color: var(--theme-text-secondary);
}

.contact-actions {
  display: flex;
  gap: 6px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: 1px solid var(--theme-border);
  background: var(--theme-bg-main);
  color: var(--theme-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: var(--theme-bg-hover);
  border-color: #4f46e5;
  color: #4f46e5;
}

.action-btn--primary {
  background: #4f46e5;
  border-color: #4f46e5;
  color: #ffffff;
}

.action-btn--primary:hover {
  background: #4338ca;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn i {
  font-size: 0.95rem;
}

/* 소셜 로그인 카드 */
.social-card {
  background: var(--theme-card-bg);
  border-radius: 10px;
  border: 1px solid var(--theme-border);
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.social-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid var(--theme-border);
}

.social-item:last-child {
  border-bottom: none;
}

.social-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.social-info > i {
  font-size: 1.3rem;
  color: #10b981;
}

.social-details {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.social-provider {
  font-weight: 600;
  color: #111827;
  font-size: 0.85rem;
}

.social-email {
  color: #6b7280;
  font-size: 0.8rem;
}

.social-remove-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px solid #fee2e2;
  background: #fef2f2;
  color: #ef4444;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.social-remove-btn:hover {
  background: #fee2e2;
  border-color: #ef4444;
}

.social-remove-btn i {
  font-size: 0.95rem;
}

/* 다크 테마: 소셜 제거 버튼 */
body[data-theme="dark"] .social-remove-btn {
  border-color: #dc3545 !important;
  background-color: #dc3545 !important;
  color: #ffffff !important;
}

body[data-theme="dark"] .social-remove-btn:hover {
  background-color: #c82333 !important;
  border-color: #c82333 !important;
}

/* Receipt 섹션 */
.receipt-section {
  margin-bottom: 20px;
}

/* 액션 바 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px 0;
  border-top: 1px solid #e5e7eb;
}

.action-bar__btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.action-bar__btn i {
  font-size: 0.9rem;
}

/* ===== 권한 정보 카드 스타일 ===== */
.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.role-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid;
  transition: all 0.2s ease;
  cursor: default;
}


.role-badge > i {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.role-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  flex: 1;
}

.role-name {
  font-size: 0.9rem;
  font-weight: 600;
  line-height: 1.2;
}

.role-code {
  font-size: 0.7rem;
  opacity: 0.7;
  font-family: 'Courier New', monospace;
}

/* 권한별 색상 테마 */
.role-badge--manager {
  border-color: #fbbf24;
  color: #92400e;
}

.role-badge--manager > i {
  color: #d97706;
}

.role-badge--inspector {
  border-color: #60a5fa;
  color: #1e3a8a;
}

.role-badge--inspector > i {
  color: #2563eb;
}

.role-badge--approver {
  border-color: #818cf8;
  color: #3730a3;
}

.role-badge--approver > i {
  color: #6366f1;
}

.role-badge--system {
  border-color: #f472b6;
  color: #831843;
}

.role-badge--system > i {
  color: #db2777;
}

.role-badge--default {
  border-color: #d1d5db;
  color: #374151;
}

.role-badge--default > i {
  color: #6b7280;
}

.no-roles {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 30px;
  color: #9ca3af;
  font-size: 0.9rem;
}

.no-roles i {
  font-size: 1.2rem;
}

/* ===== 모바일 권한 정보 카드 ===== */
.mobile-roles-card {
    width: 100%;
    background: #ffffff;
    margin: 12px 0 10px 0;
    overflow: hidden;
}

.mobile-roles-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.mobile-roles-card__header i {
  font-size: 1rem;
}

.mobile-roles-card__title {
    font-size: 0.85rem;
    font-weight: 600;
    color: var(--theme-text-primary);
    margin-bottom: 0px;
}

.mobile-roles-card__body {
  padding: 12px 16px;
}

/* 권한 아이콘 색상 */
.mobile-role-icon.role-badge--manager {
  color: #d97706 !important;
}

.mobile-role-icon.role-badge--inspector {
  color: #2563eb !important;
}

.mobile-role-icon.role-badge--approver {
  color: #6366f1 !important;
}

.mobile-role-icon.role-badge--system {
  color: #db2777 !important;
}

.mobile-role-icon.role-badge--default {
  color: #6b7280 !important;
}

.mobile-no-roles {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 20px;
  color: #9ca3af;
  font-size: 0.75rem;
}

.mobile-no-roles i {
  font-size: 1rem;
}

@media (max-width: 650px) {
  hr {
    margin: 15px 0px 15px 0px !important;
  }
  .content {
    width: 100% !important;
    min-width: 100% !important;
    padding: 80px 20px 100px;
  }
  .form-group,
  .form-group-name-phone {
    margin-bottom: 8px !important;
  }
  .form-group-password {
    margin-top: 22px;
  }
  label,
  .date-input,
  .edit-btn {
    font-size: 0.7rem !important;
  }
  .mobile-icon {
    font-size: 0.9rem;
    margin-right: 5px;
  }
  /* 스타일 선택 버튼 */
  .style-selector-mobile {
    display: flex;
    gap: 8px;
    justify-content: center;
    margin-bottom: 16px;
    padding: 0 20px;
  }
  .style-btn {
    flex: 1;
    padding: 8px 12px;
    border-radius: 10px;
    border: 2px solid #e5e7eb;
    background: #ffffff;
    color: #64748b;
    font-size: 0.75rem;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.2s;
  }
  .style-btn.active {
    border-color: #3b82f6;
    background: #eff6ff;
    color: #3b82f6;
  }
  .style-btn:active {
    transform: scale(0.96);
  }

  /* 기본 정보 카드 (CSS 변수 사용) */
  .bm-card {
    width: 100%;
    background: var(--theme-bg-main);
    border-radius: 16px;
    border: 1px solid var(--theme-border);
    box-shadow: 0 4px 14px var(--theme-shadow);
    margin: 12px 0 10px 0 !important;
    overflow: hidden;
  }
  .bm-card__header {
    display: flex;
    align-items: center;
    gap: 8px;
    background: linear-gradient(135deg, var(--theme-card-header-from) 0%, var(--theme-card-header-to) 100%);
    padding: 12px 16px !important;
    color: #ffffff;
  }
  .bm-card__header i {
    font-size: 1rem;
  }
  .bm-card__title {
    font-size: 0.9rem !important;
    font-weight: 800;
    margin-bottom: 0px;
  }
  .bm-card__body {
    padding: 15px 20px 12px 20px;
  }
  .bm-chips {
    display: flex;
    gap: 6px;
    margin-bottom: 8px;
    flex-wrap: wrap;
  }
  .chip {
    display: inline-flex;
    align-items: center;
    padding: 4px 10px;
    border-radius: 999px;
    font-size: 0.7rem;
    line-height: 1;
    border: 1px solid transparent;
  }
  .chip--mint {
    background: var(--theme-chip-bg);
    color: var(--theme-chip-text);
    border-color: var(--theme-chip-border);
    font-weight: 700;
  }
  .chip--dark {
    background: var(--theme-chip-bg);
    color: var(--theme-chip-text);
    border-color: var(--theme-chip-border);
  }
  .bm-crumb {
    display: flex;
    align-items: center;
    gap: 6px;
    color: var(--theme-text-secondary);
    font-size: 0.72rem;
    margin-bottom: 8px;
  }
  .bm-crumb .sep {
    opacity: 0.6;
  }
  .bm-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-top: 1px dashed #e6efff;
  }
  .bm-row:first-of-type {
    border-top: none;
  }
  .bm-row__left {
    display: flex;
    align-items: center;
    gap: 6px;
    color: var(--theme-text-secondary);
    font-weight: 700;
    font-size: 0.75rem;
  }
  .bm-row__right {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  .bm-row__value {
    color: var(--theme-text-primary);
    font-size: 0.7rem;
  }
  .input-group {
    display: flex;
    align-items: center;
  }
  .input-group .input-group-append {
    margin: 0;
  }
  .input-group-append > * {
    width: auto;
    flex-shrink: 0;
  }
  .edit-btn {
    border: none;
    background: transparent;
    font-size: 0.8rem;
    margin-left: 3px;
    cursor: pointer;
  }
  .date-input {
    font-size: 0.6rem !important;
    padding: 0px !important;
    width: 93px !important;
  }
  .profile-image-wrapper {
    width: 120px !important;
    height: 120px !important;
  }

  /* 모바일 연락처 카드 (CSS 변수 사용) */
  .phone-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding: 10px 12px;
    border: 1px solid var(--theme-border-hover);
    background: var(--theme-primary-lighter);
    border-radius: 12px;
    box-shadow: 0 2px 10px var(--theme-shadow);
    margin: 8px 0 15px 0;
  }
  .phone-card__left {
    display: flex;
    align-items: center;
    gap: 6px;
    color: var(--theme-icon-primary);
  }
  .phone-card__title {
    font-weight: 800;
    font-size: 0.75rem;
  }
  .phone-card__center {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 0 6px;
  }
  .phone-card__number {
    font-size: 0.7rem;
    color: var(--theme-text-primary);
    text-decoration: none;
  }
  .phone-card__number--empty {
    color: var(--theme-text-muted);
  }
  .phone-card__right {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  .icon-btn {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    border: 1px solid #cfe6ff;
    background: #ffffff;
    color: #0b6bcb;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 6px rgba(0, 140, 255, 0.12);
    cursor: pointer;
  }
  .icon-btn.call {
    background: #0b6bcb;
    color: #ffffff;
    border-color: #0b6bcb;
  }
  .icon-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  /* 모바일 소셜 계정 카드 */
  .mobile-social-card {
    width: 100%;
    background: var(--theme-card-bg);
    border-radius: 12px;
    border: 1px solid var(--theme-border);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    margin: 12px 0 10px 0;
    overflow: hidden;
  }

  .mobile-social-card__header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 14px 16px;
    background: var(--theme-bg-secondary);
    border-bottom: 1px solid var(--theme-border);
  }

  .mobile-social-card__header i {
    font-size: 1rem;
    color: #6b7280;
  }

  .mobile-social-card__title {
    font-size: 0.85rem;
    font-weight: 600;
    color: var(--theme-text-primary);
    margin-bottom: 0px;
  }

  .mobile-social-card__body {
    padding: 12px 16px;
  }

  /* 모바일 소셜 계정 아이템 */
  .mobile-social-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px dashed #e6efff;
  }

  .mobile-social-item:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  .mobile-social-item:first-child {
    padding-top: 0;
  }

  .mobile-social-info {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .mobile-social-info > i {
    font-size: 1.2rem;
    color: #10b981;
    flex-shrink: 0;
  }

  .mobile-social-details {
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  .mobile-social-provider {
    font-weight: 700;
    color: var(--theme-text-primary);
    font-size: 0.75rem;
  }

  .mobile-social-email {
    color: var(--theme-text-secondary);
    font-size: 0.7rem;
  }

  .mobile-social-remove-btn {
    width: 28px;
    height: 28px;
    border-radius: 6px;
    border: 1px solid #fee2e2;
    background: #fef2f2;
    color: #ef4444;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s;
  }

  .mobile-social-remove-btn:hover {
    background: #fee2e2;
    border-color: #ef4444;
  }

  .mobile-social-remove-btn i {
    font-size: 0.9rem;
  }

  /* 다크 테마: 모바일 소셜 제거 버튼 */
  body[data-theme="dark"] .mobile-social-remove-btn {
    border-color: #dc3545 !important;
    background-color: #dc3545 !important;
    color: #ffffff !important;
  }

  body[data-theme="dark"] .mobile-social-remove-btn:hover {
    background-color: #c82333 !important;
    border-color: #c82333 !important;
  }
  .roles-grid {
    grid-template-columns: 1fr;
  }
  
  .role-badge {
    padding: 12px 14px;
  }
  
  .role-badge > i {
    font-size: 1.3rem;
  }
  
  .role-name {
    font-size: 0.8rem;
  }
  
  .role-code {
    font-size: 0.65rem;
  }
}

/* ═══════════════════════════════════════════════════════════════
 * 다크모드 스타일
 * ═══════════════════════════════════════════════════════════════ */

/* phone-card 다크모드 */
body[data-theme="dark"] .phone-card {
  border: 1px solid var(--theme-border);
  background: var(--theme-bg-tertiary);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

body[data-theme="dark"] .phone-card__left {
  color: var(--theme-text-secondary);
}

body[data-theme="dark"] .phone-card__title {
  color: var(--theme-text-primary);
}

body[data-theme="dark"] .phone-card__number {
  color: var(--theme-text-primary);
}

body[data-theme="dark"] .phone-card__number--empty {
  color: var(--theme-text-muted);
}

body[data-theme="dark"] .icon-btn {
  border: 1px solid var(--theme-border);
  background: var(--theme-bg-secondary);
  color: var(--theme-primary);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

body[data-theme="dark"] .icon-btn:hover {
  background: var(--theme-bg-hover);
  border-color: var(--theme-primary);
}

</style>
