<template>
  <!-- 전체 컨테이너 -->
  <div class="login-page">
    <!-- 상단 헤더 -->
    <FrontHeader />

    <!-- 회색 배경 전체 래퍼 -->
    <div class="login-wrapper">
      <!-- 중앙 직사각형 박스 -->
      <div class="login-box">
        <!-- 왼쪽 : 영상 영역 -->
        <div class="left-section">
          <!-- <div class="video-container">
          <video autoplay loop muted playsinline class="video-fg">
            <source src="/img/common/login/login.mp4" type="video/mp4" />
          </video>
          <video autoplay loop muted playsinline class="video-bg">
            <source src="/img/common/login/login.mp4" type="video/mp4" />
          </video>
        </div> -->

          <!-- 이미지로 교체 -->
          <div class="image-container">
            <img src="/img/common/login/004.png" alt="login" class="login-image" />
          </div>
        </div>

        <!-- 오른쪽 : 로그인 영역 -->
        <div class="right-section">
          <div class="form-container">
            <h1 class="title-login">{{ $t('auth.login.title') }}</h1>

            <!-- 타이틀 -->
            <h2 class="title">{{ $t('auth.login.systemTitle') }}</h2>

            <div class="system-header">
              <p class="system-subtitle">{{ $t('auth.login.systemSubtitle') }}</p>
            </div>

            <!-- ───── 이메일 ───── -->
            <DefaultLabel :text="$t('auth.login.accountLabel')" forId="email" size="small" marginBottom="5px" />
            <v-text-field
              v-model="userEmail"
              density="compact"
              :placeholder="$t('auth.login.emailPlaceholder')"
              prepend-inner-icon="mdi-email-outline"
              variant="outlined"
              rounded="10"
              class="email"
              hide-details
            />

            <!-- ───── 패스워드 ───── -->
            <DefaultFormRow align="between" marginBottom="5px">
              <DefaultLabel :text="$t('auth.login.passwordLabel')" size="small" forId="Password" />
              <a
                class="text-caption text-decoration-none text-blue"
                href="#"
                @click.prevent="forgotPasswordModalVisible = true"
              >
                {{ $t('auth.login.forgotPassword') }}
              </a>
            </DefaultFormRow>
            <v-text-field
              v-model="password"
              :append-inner-icon="visible ? 'mdi-eye-off' : 'mdi-eye'"
              :type="visible ? 'text' : 'password'"
              density="compact"
              :placeholder="$t('auth.login.passwordPlaceholder')"
              prepend-inner-icon="mdi-lock-outline"
              variant="outlined"
              rounded="10"
              hide-details
              class="password-input"
              @click:append-inner="visible = !visible"
              @keyup.enter="login"
            />
            <!-- 아이디 저장 체크박스 -->
            <div class="remember-row">
              <v-checkbox
                v-model="rememberId"
                :label="$t('auth.login.rememberMe')"
                density="compact"
                hide-details
                class="remember-checkbox"
              />
            </div>
            <!-- 주의 문구 -->
            <DefaultLabel
              alignment="left"
              :text="$t('auth.login.ipWarning')"
              size="small"
              marginTop="10px"
              marginBottom="30px"
            />

            <!-- ───── 액션 버튼 ───── -->
            <button class="action-btn login-btn" @click="login">{{ $t('auth.login.loginButton') }}</button>
            <button class="action-btn join-btn" @click="join">{{ $t('auth.login.signUp') }}</button>

            <!-- ───── 소셜 로그인(원형) ───── -->
            <div class="social-row">
              <button class="social-circle google-circle" @click="oauth('google')">
                <img
                  class="social-icon-circle"
                  src="/img/logo/google/google_icon.svg"
                  alt="Google"
                />
              </button>
              <button class="social-circle kakao-circle" @click="oauth('kakao')">
                <img
                  class="social-icon-circle"
                  src="/img/logo/kakao/kakao_icon.svg"
                  alt="Kakao"
                />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 가이드 헬퍼 -->
      <GuideHelper />
    </div>
  </div>

  <!-- 알림 모달 -->
  <AlertModal
    :isVisible="confirmationDepartmentModalVisible"
    :disableBackgroundClose="true"
    :confirmText="$t('common.button.confirm')"
    :cancelText="$t('common.button.cancel')"
    @confirm="confirmationDepartmentModalVisible = false"
    @close="goToMain"
  >
    <template #body>
      <p>※ {{ $t('auth.login.socialModalTitle') }} ※</p>
      <p style="white-space: pre-line">{{ $t('auth.login.socialModalMessage') }}</p>

      <hr style="margin: 15px 0" />
      <span style="white-space: pre-line">{{ $t('auth.login.socialModalFooter') }}</span>
    </template>
  </AlertModal>

  <!-- 비밀번호 찾기 다이얼로그 -->
  <ForgotPasswordModal v-model="forgotPasswordModalVisible" />
</template>

<script setup>
/* ───────── 기존 스크립트 그대로 ───────── */
import { ref, onBeforeMount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ROUTES } from '@/config/menuConfig';
import LoginApi from '@/api/auth/LoginApi';
import { useAuthStore } from '@/store/auth';
import { toast } from 'vue3-toastify';
import UsersApi from '@/api/hrm/UsersApi';
import { useHrmStore } from '@/store/hrm';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import ForgotPasswordModal from '@/components/auth/ForgotPasswordModal.vue';
import GuideHelper from '@/components/common/GuideHelper.vue';
import FrontHeader from '@/components/auth/FrontHeader.vue';
import { RsaEncryptionUtil } from '@/utils/rsaEncryption';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const visible = ref(false);
const confirmationDepartmentModalVisible = ref(false);
const router = useRouter();
const route = useRoute();
const userEmail = ref('');
const password = ref('');
const rememberId = ref(false);
const authStore = useAuthStore();
const hrmStore = useHrmStore();
const forgotPasswordModalVisible = ref(false);

/**
 * 로그인 후 사용자 정보(auth)와 HRM 프로필을 순차 저장
 * - 먼저 HRM 프로필 저장 (단건 조회 1회만)
 * - HRM 프로필에서 이미지 URL을 가져와 auth 스토어 저장
 */
const fetchUserInfo = async () => {
  const res = await LoginApi.getMe();
  
  // HRM 프로필 먼저 로드 (단건 조회 1회)
  await hrmStore.loadMyProfileByUserId(res.data.userId);
  
  // HRM 프로필에서 이미지 URL과 cardStyle 가져오기
  const imgUrl = hrmStore.getMyProfile?.profileImgUrl || null;
  const cardStyle = hrmStore.getMyProfile?.cardStyle || 'default';
  
  // auth 스토어에 저장
  authStore.login({
    userId: res.data.userId,
    username: res.data.username,
    userEmail: res.data.userEmail,
    userProfileImgUrl: imgUrl,
    roles: res.data.roles,
    cardStyle: cardStyle,
  });
  
  router.push(ROUTES.MAIN);
  // window.location.replace('/#/main')
};

/**
 * 로그인 처리
 * - RSA 공개키 조회 후 비밀번호 암호화하여 전송
 */
const login = async () => {
  if (!userEmail.value || !password.value) {
    toast.error(t('auth.login.emailRequired'));
    return;
  }
  
  // 포트폴리오용: 기능 비활성화
  toast.error('포트폴리오용: 로그인 기능이 비활성화되어 있습니다.');
};

const oauth = (social) => {
  const base = import.meta.env.VITE_SYSTEM_API_BASE_URL.replace(/\/$/, '');
  if (social === 'google')
    window.location.href = `${base}/auth/oauth2/authorization/google`;
  else if (social === 'kakao')
    window.location.href = `${base}/auth/oauth2/authorization/kakao`;
  else toast.error(t('auth.login.socialUnknown'));
};

const join = () => router.push(ROUTES.TEMP.JOIN);

// 로그인 페이지 재로딩
function goToMain() {
  confirmationDepartmentModalVisible.value = false;
  router.replace('/');
}

onBeforeMount(async () => {
  // 저장된 아이디가 있으면 불러오기
  const savedEmail = localStorage.getItem('rememberedEmail');
  if (savedEmail) {
    userEmail.value = savedEmail;
    rememberId.value = true;
  }

  if (route.query?.encrypt) {
    confirmationDepartmentModalVisible.value = true;
    return;
  }
  if (route.query?.error) {
    toast.error(route.query.error);
    await LoginApi.logout();
    await new Promise((r) => setTimeout(r, 1500));
    router.push(ROUTES.HOME);
    return;
  }
  if (route.query?.logout) toast.success(t('auth.login.loginSuccess'));
});
</script>

<style scoped>
/* ═══════════════════════ 전체 페이지 ═══════════════════════ */
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-top: 65px; /* 헤더 높이만큼 여백 추가 */
  font-weight: 500 !important; /* 기본 폰트 weight 500 설정 */
}

/* 모든 요소의 최소 폰트 weight를 500으로 보장 */
.login-page * {
  font-weight: inherit;
}

.login-page *[style*="font-weight: 400"],
.login-page *[style*="font-weight: 300"],
.login-page *[style*="font-weight: 200"],
.login-page *[style*="font-weight: 100"] {
  font-weight: 500 !important;
}

/* ═══════════════════════ 콘텐츠 래퍼 ═══════════════════════ */
.login-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f7f7;
  padding: 20px;
}

/* ═══════════════════════ 중앙 박스 ═══════════════════════ */
.login-box {
  width: 1434px;
  height: 680px;
  margin: 0 auto;
  display: flex;
  background: #ffffff;
  overflow: hidden;
}

/* 높이가 충분할 때만 위로 20px 올리기 */
@media (min-height: 851px) {
  .login-box {
    margin: -30px auto 0 auto;
  }
}

/* ═══════════════════════ 왼쪽 영상 영역 ═══════════════════════ */
.left-section {
  flex: 0 0 853px;
  position: relative;
  overflow: hidden;
  background-color: #b8b8ba;
  /* border-left: 1px solid #d0d0d0; */
  border-top: 1px solid #d0d0d0;
  border-bottom: 1px solid #d0d0d0;
}

.video-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background-color: #b8b8ba;
}

.video-fg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  z-index: 2;
}

.video-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scale(1.1);
  filter: blur(30px) brightness(0.8);
  z-index: 1;
}

/* 이미지 컨테이너 */
.image-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #b8b8ba;
}

.login-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* ═══════════════════════ 오른쪽 로그인 영역 ═══════════════════════ */
.right-section {
  flex: 0 0 580px;
  display: flex;
  /* align-items: center; */
  justify-content: center;
  padding: 2rem;
  background: #fff;
  border-right: 1px solid #d0d0d0;
  border-top: 1px solid #d0d0d0;
  border-bottom: 1px solid #d0d0d0;
}

.form-container {
  width: 100%;
  max-width: 380px;
  transform: scale(0.85);
  transform-origin: center;
}

.title {
  text-align: left;
  font-size: 1.5rem;
  font-weight: 800;
  margin-bottom: 10px;
}

.title-login {
  text-align: left;
  font-weight: 900;
  margin-bottom: 40px;
}

/* ───────── 기본 버튼 스타일 ───────── */
.action-btn {
  width: 100%;
  height: 42px;
  margin-bottom: 10px;
  border: none;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-btn,
.join-btn {
  font-size: 0.875rem;
  background: #f8f8f8;
  border: 1px solid #dadce0;
  color: #555;
}

.login-btn:hover,
.join-btn:hover {
  /* background: #f7f7f7; */
  background: #ececec;
}

.join-btn {
  /* background: #a6c2ff; */
  margin-bottom: 30px;
}

/* ───────── 원형 소셜 버튼 ───────── */
.social-row {
  display: flex;
  gap: 20px;
  justify-content: right;
  margin-top: 10px;
}

.social-circle {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
}

.google-circle {
  background: #fff;
  border: 1px solid #dadce0;
}

.kakao-circle {
  background: #fee500;
}

.social-icon-circle {
  width: 30px;
  height: 30px;
}

/* ───────── 기타 텍스트 및 요소 ───────── */
.system-header {
  text-align: left;
  margin-bottom: 30px;
}

.system-subtitle {
  font-size: 0.9rem;
  color: #6c757d;
}

.email {
  margin-bottom: 20px;
}

.text-caption {
  font-size: 0.7rem !important;
}

::v-deep .v-field__input {
  font-size: 0.95rem;
  display: block !important;
  gap: 0 !important;
  padding: 10px 10px 10px 25px !important;
}

/* ═══════════════════════ 모바일 (≤1500px) ═══════════════════════ */
@media (max-width: 1500px) {
  body {
    min-height: 650px;
  }
  /* 래퍼 스타일 변경 */
  .login-wrapper {
    background: #fff;
    /* max-height: calc(100% - 65px); */
    padding: 0;
  }

  /* 박스 스타일 제거하고 전체 화면 사용 */
  .login-box {
    width: 100%;
    height: auto;
    min-height: 650px;
    max-height: max(calc(100dvh - 65px), 650px);
    border: none;
    flex-direction: column;
    margin: 0 auto;
  }

  /* 왼쪽 영상 숨김 */
  .left-section {
    display: none !important;
  }

  /* 오른쪽 영역 전체 사용 */
  .right-section {
    width: 100%;
    padding: 1.5rem 2rem;
    border: 0px;
  }

  .form-container {
    transform: scale(0.875);
  }

  .action-btn {
    height: 38px;
    font-size: 0.7rem;
  }

  .login-btn,
  .join-btn {
    font-size: 0.7rem;
    background: #f8f8f8;
    border: 1px solid #dadce0;
    color: #555;
  }

  .social-row {
    gap: 20px;
  }

  .title {
    font-size: 1.3rem;
  }

  .system-header {
    margin-bottom: 25px;
  }

  .system-subtitle {
    font-size: 0.75rem;
  }

  ::v-deep .v-field__input {
    font-size: 0.8rem;
    display: block !important;
    gap: 0 !important;
    padding: 8px 8px 8px 20px !important;
  }

  .remember-checkbox {
    --v-checkbox-size: 12px;
    font-size: 0.8rem;
  }

  .remember-checkbox ::v-deep .v-label {
    font-size: 0.8rem;
  }
}

/* ═══════════════════════ 기타 스타일 ═══════════════════════ */
.password-input {
  margin-bottom: 0;
}

/* 패스워드 입력칸 내부 디테일 영역 숨김 보강 */
.password-input ::v-deep .v-input__details {
  display: none !important;
}

.remember-row {
  margin: 5px 0 8px 0;
  display: flex;
  justify-content: flex-end;
}

.remember-checkbox {
  --v-checkbox-size: 14px;
  font-size: 0.875rem;
}

/* 라벨 글자 크기 오버라이드 */
.remember-checkbox ::v-deep .v-label {
  padding-top: 2px;
  font-size: 0.875rem;
}

@media (max-width: 650px) {
  .form-container {
    width: 100%;
    max-width: 380px;
    transform: scale(0.8);
    transform-origin: center;
  }
  .right-section {
    padding: 0rem 1rem;
    flex: 0 0 600px;
  }
}

/* 화면 높이 650px 이상일 때만 padding-top 제거 */
@media (max-width: 1500px) and (min-height: 700px) {
  .login-page {
    padding-top: 35px;
  }
}
</style>
