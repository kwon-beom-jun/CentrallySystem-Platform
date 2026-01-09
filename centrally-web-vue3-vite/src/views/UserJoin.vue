<template>
  <!-- 전체 컨테이너 -->
  <div class="signup-page">
    <!-- 상단 헤더 -->
    <FrontHeader />

    <!-- 회색 배경 전체 래퍼 -->
    <div class="signup-wrapper">
      <!-- 중앙 직사각형 박스 -->
      <div class="signup-box">
        <!-- 왼쪽 : 영상 영역 -->
        <div class="left-section">
          <!-- <div class="video-container">
          <video autoplay loop muted playsinline class="video-fg">
            <source src="/img/common/login/login2.mp4" type="video/mp4" />
          </video>
          <video autoplay loop muted playsinline class="video-bg">
            <source src="/img/common/login/login2.mp4" type="video/mp4" />
          </video>
        </div> -->

          <!-- 이미지로 교체 -->
          <div class="image-container">
            <img src="/img/common/login/004.png" alt="signup" class="signup-image" />
          </div>
        </div>

        <!-- 오른쪽 : 폼 -->
        <div class="right-section">
          <div class="form-container">
            <!-- 로그인 페이지와 동일한 헤더  -->
            <h1 class="title-login">{{ $t('auth.join.title') }}</h1>
            <h2 class="title">{{ $t('auth.join.systemTitle') }}</h2>
            <div class="system-header">
              <p class="system-subtitle">{{ $t('auth.join.systemSubtitle') }}</p>
            </div>

            <!-- ===== 회원가입 폼 ===== -->
            <form ref="formRef" @submit.prevent="handleSubmit">
              <!-- 이름 / 휴대폰 -->
              <DefaultFormRow
                align="betweenEqual"
                gap="12px"
                marginBottom="10px"
                marginTop="8px"
              >
                <div class="field-half">
                  <DefaultLabel
                    :text="$t('auth.join.name')"
                    forId="userName"
                    size="small"
                    alignment="left"
                  />
                  <DefaultTextfield
                    id="userName"
                    v-model="userName"
                    size="full"
                    required
                    customHeight="40px"
                  />
                </div>
                <div class="field-half">
                  <DefaultLabel
                    :text="$t('auth.join.phone')"
                    forId="phone"
                    size="small"
                    alignment="left"
                  />
                  <DefaultTextfield
                    id="phone"
                    v-model="phone"
                    size="full"
                    :placeholder="$t('auth.join.phonePlaceholder')"
                    required
                    customHeight="40px"
                  />
                </div>
              </DefaultFormRow>

              <!-- 비밀번호 / 확인 -->
              <DefaultFormRow gap="12px" marginBottom="10px" align="betweenEqual">
                <div class="field-half">
                  <DefaultLabel
                    :text="$t('auth.join.password')"
                    forId="password"
                    size="small"
                    alignment="left"
                  />
                  <DefaultTextfield
                    type="password"
                    id="password"
                    v-model="password"
                    size="full"
                    required
                    customHeight="40px"
                  />
                </div>
                <div class="field-half">
                  <DefaultLabel
                    :text="$t('auth.join.passwordConfirm')"
                    forId="confirmPassword"
                    size="small"
                    alignment="left"
                  />
                  <DefaultTextfield
                    type="password"
                    id="confirmPassword"
                    v-model="confirmPassword"
                    size="full"
                    required
                    customHeight="40px"
                  />
                </div>
              </DefaultFormRow>

              <!-- 이메일 / 인증 메일 -->
              <DefaultLabel :text="$t('auth.join.email')" forId="email" size="small" alignment="left" />
              <DefaultFormRow growFirst>
                <DefaultTextfield
                  id="email"
                  v-model="email"
                  validationType="email"
                  reserveErrorSpace
                  required
                  size="full"
                  customHeight="40px"
                />
                <DefaultButton
                  color="gray"
                  class="btn-sm no-pad"
                  customWidth="110"
                  marginBottom="21px"
                  @click="sendEmailVerification"
                  customHeight="40px"
                >
                  {{ $t('auth.join.sendVerificationEmail') }}
                </DefaultButton>
              </DefaultFormRow>

              <!-- 인증코드 / 타이머 -->
              <DefaultLabel
                :text="$t('auth.join.verificationCode')"
                forId="email"
                size="small"
                alignment="left"
              />
              <DefaultFormRow marginBottom="35px">
                <DefaultTextfield
                  id="verificationCode"
                  v-model="verificationCode"
                  size="small"
                  :disabled="verificationCodeDisable"
                  :placeholder="$t('auth.join.verificationCodePlaceholder')"
                  customHeight="40px"
                />
                <DefaultButton
                  color="yellow"
                  size="small"
                  @click="sameVerification"
                  customHeight="40px"
                >
                  {{ $t('auth.join.verifyButton') }}
                </DefaultButton>
                <span
                  v-if="!verificationCodeDisable && countdown > 0"
                  class="timer-caption"
                >
                  {{ countdown }}s
                </span>
              </DefaultFormRow>

              <!-- (위로 이동) -->
              <!-- 약관 동의 버튼 & 상태 표시 -->
              <div class="agree-row">
                <input
                  type="checkbox"
                  :checked="hasAgreedRequired"
                  disabled
                  class="agree-checkbox"
                />
                <a
                  href="#"
                  class="agree-link"
                  :class="{ ok: hasAgreedRequired }"
                  @click.prevent="openAgreementModal"
                >
                  {{ hasAgreedRequired ? $t('auth.join.agreementCompleted') : $t('auth.join.agreementRequired2') }}
                </a>
              </div>

              <button class="action-btn join-btn" type="submit">{{ $t('auth.join.signUpButton') }}</button>
              <button class="action-btn back-btn" @click="onGoBack">{{ $t('auth.join.backButton') }}</button>
            </form>
          </div>
        </div>
        <!-- 가이드 헬퍼 -->
        <GuideHelper />
      </div>
    </div>

    <!-- 약관 동의 모달 -->
    <div v-if="modalVisible" class="modal-overlay" @click.self="modalVisible = false">
      <div class="modal-card">
        <h3 class="modal-title">{{ $t('auth.join.agreementModalTitle') }}</h3>

        <!-- 약관 목록 -->
        <div class="modal-body" ref="modalBodyRef">
          <div 
            class="agreement-item" 
            v-for="ag in tempAgreements" 
            :key="ag.agreementId"
            :ref="el => setItemRef(el, ag.agreementId)"
          >
            <div class="agreement-header">
              <label class="agreement-checkbox">
                <input type="checkbox" v-model="ag.checked" @change="toggleSingle" />
                <span>[{{ ag.isRequired ? $t('auth.join.agreementRequired') : $t('auth.join.agreementOptional') }}] {{ ag.title }}</span>
              </label>
              <button 
                type="button"
                class="toggle-btn" 
                @click="toggleAgreement(ag)"
                :aria-label="ag.expanded ? $t('common.button.collapse') : $t('common.button.expand')"
              >
                <i :class="ag.expanded ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'"></i>
              </button>
            </div>
            <transition name="expand">
              <div v-if="ag.expanded" class="agreement-text" v-html="ag.sanitizedContent"></div>
            </transition>
          </div>
        </div>

        <!-- 모두 동의 -->
        <div class="modal-all">
          <label>
            <input type="checkbox" v-model="agreeAll" @change="toggleAll" />
            <span>{{ $t('auth.join.agreementAgreeAll') }}</span>
          </label>
        </div>

        <div class="modal-actions">
          <DefaultButton color="gray" @click="modalVisible = false" customHeight="33px">
            {{ $t('common.button.cancel') }}
          </DefaultButton>
          <DefaultButton color="blue" @click="confirmAgreements" customHeight="33px">
            {{ $t('common.button.confirm') }}
          </DefaultButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import DOMPurify from 'dompurify';
import { useRouter } from 'vue-router';
import { ROUTES } from '@/config/menuConfig';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import KakaoAddressModal from '@/components/common/kakao/address/KakaoAddressModal.vue';
import EmailApi from '@/api/auth/EmailApi';
import TempUsersApi from '@/api/auth/TempUsersApi';
import LoginApi from '@/api/auth/LoginApi';
import { RsaEncryptionUtil } from '@/utils/rsaEncryption';
import AgreementApi from '@/api/auth/AgreementApi.js';
import { useToastStore } from '@/store/toast';
import GuideHelper from '@/components/common/GuideHelper.vue';
import FrontHeader from '@/components/auth/FrontHeader.vue';
import { useI18n } from 'vue-i18n';

// toast store 사용
const toastStore = useToastStore();
const { t, locale } = useI18n();

// 라우터 사용 설정
const router = useRouter();

const userName = ref('');
const email = ref('');
const phone = ref('');
const dob = ref('');
const joinDate = ref('');
const department = ref('');
const team = ref(''); // 팀을 위한 상태 추가
const position = ref('');
const password = ref('');
const confirmPassword = ref('');

// 인증 코드 입력값
const verificationCode = ref('');
const verificationCodeDisable = ref(false);
const isVerified = ref(false);

// 타이머 관련 상태
const countdown = ref(0);
let timerInterval = null;

/**
 * 카카오 주소 API
 */
const zipCode = ref('');
const address = ref('');
const detailAddress = ref('');
const isModalOpen = ref(false);

function openModal() {
  isModalOpen.value = true;
}
function closeModal() {
  isModalOpen.value = false;
}
function handleSelectAddress(payload) {
  // payload = { address: ..., zipCode: ... }
  address.value = payload.address;
  zipCode.value = payload.zipCode;
}

/**
 * 타이머 시작 함수
 * 5분(300초)부터 1초마다 감소
 */
function startTimer() {
  countdown.value = 300; // 300초 = 5분
  verificationCode.value = '';
  verificationCodeDisable.value = false;
  isVerified.value = false;
  if (timerInterval) clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    } else {
      clearInterval(timerInterval);
    }
  }, 1000);
}

/**
 * 인증 메일 발송 버튼 클릭 시
 *  - 백엔드에 email 보내서 인증코드 생성 & 메일 발송 요청
 */
async function sendEmailVerification() {
  if (!email.value) {
    toastStore.error(t('auth.join.emailRequired'));
    return;
  }

  const response = await EmailApi.mailSend({
    email: email.value,
  });

  if (response.status === 200) {
    // 인증 메일 발송 후 타이머 시작(약간의 오차가 있을 수 있음)
    // 인증 실패시 백엔드 호출하는동안에도 멈춰있어서 오차가 있을 수 있음
    startTimer();
    toastStore.info(response.data.message);
  } else {
    toastStore.error(response.data.message);
  }
}

/**
 * 인증 확인 버튼 클릭 시
 *  - 백엔드에 인증코드 보내서 검증
 */
async function sameVerification() {
  /**
   *  verificationCode는 백엔드에서 암호화 된 값으로 해줘야하나?
   *  이후 암호화 된 값을 백엔드에서 검증해야하나?
   */
  const response = await EmailApi.mailVerify({
    email: email.value,
    code: verificationCode.value,
  });

  if (response.status === 200) {
    verificationCodeDisable.value = true;
    isVerified.value = true;
    toastStore.info(response.data.message);
    if (timerInterval) clearInterval(timerInterval); // 타이머 중지
  } else {
    toastStore.error(response.data.message);
  }
}

/**
 * 회원 가입 벡엔드 호출
 *  - 폼 제출 시 호출되는 함수
 *  - 폼은 주로 클라이언트에서 데이터의 유효성을 확인하거나 기본적인 입력값이 있는지 체크하는 용도
 */
const formRef = ref(null);
async function handleSubmit() {
  // formRef를 통해 폼 요소의 유효성을 검사합니다.
  if (!formRef.value.checkValidity()) {
    formRef.value.reportValidity();
    return;
  }

  if (password.value !== confirmPassword.value) {
    toastStore.warn(t('auth.join.passwordMismatch'));
    return;
  }

  if (!verificationCode.value) {
    toastStore.warn(t('auth.join.verificationCodeRequired'));
    return;
  }

  if (!isVerified.value) {
    toastStore.warn(t('auth.join.verificationRequired'));
    return;
  }

  // 필수 약관 체크 여부 확인
  const requiredUnchecked = agreements.value.filter((a) => a.isRequired && !a.checked);
  if (requiredUnchecked.length > 0) {
    toastStore.warn(t('auth.join.termsRequired'));
    return;
  }

  // 포트폴리오용: 기능 비활성화
  toastStore.warn('포트폴리오용: 회원가입 기능이 비활성화되어 있습니다.');

  // 서버로 데이터를 전송하는 로직을 추가하세요
  console.log('회원가입 완료:', {
    userName: userName.value,
    email: email.value,
    phone: phone.value,
    dob: dob.value,
    joinDate: joinDate.value,
    // department: department.value,
    // team: team.value,
    // position: position.value,
    zipCode: zipCode.value,
    address: address.value,
    detailAddress: detailAddress.value,
    password: password.value,
    verificationCode: verificationCode.value,
  });
}

/**
 * 뒤로가기 (routeConfig.js의 ROUTES 사용)
 */
const onGoBack = () => {
  router.push(ROUTES.HOME);
};

const agreements = ref([]); // 최종 확정된 동의 상태

// 모달 관련 상태
const modalVisible = ref(false);
const tempAgreements = ref([]); // 모달 내부에서 사용하는 임시 리스트
const agreeAll = ref(false); // 모달의 '모두 동의'
const modalBodyRef = ref(null); // 모달 본문 ref
const agreementItemRefs = ref({}); // 각 약관 항목 ref

const hasAgreedRequired = computed(() => {
  const requiredAgreements = agreements.value.filter((a) => a.isRequired);
  // 필수 약관이 없거나 빈 배열이면 false 반환
  if (requiredAgreements.length === 0) {
    return false;
  }
  // 모든 필수 약관이 체크되었는지 확인
  return requiredAgreements.every((a) => a.checked);
});

/**
 * 선택한 언어에 맞춰 약관 목록을 불러온다.
 */
async function loadAgreements(languageCode) {
  try {
    const { data } = await AgreementApi.getAgreements(languageCode);
    const normalized = data.map((a) => ({
      ...a,
      checked: false,
      expanded: false,
      sanitizedContent: DOMPurify.sanitize(a.content),
    }));

    agreements.value = normalized.map((a) => ({ ...a }));
    if (modalVisible.value) {
      tempAgreements.value = normalized.map((a) => ({ ...a }));
    } else {
      tempAgreements.value = [];
    }

    agreementItemRefs.value = {};
    agreeAll.value = false;
  } catch (error) {
    console.error(error);
    agreements.value = [];
    agreeAll.value = false;
    tempAgreements.value = [];
    agreementItemRefs.value = {};
  }
}

watch(locale, (nextLanguage) => {
  loadAgreements(nextLanguage);
});

/**
 * 약관 항목 ref 설정
 */
const setItemRef = (el, agreementId) => {
  if (el) {
    agreementItemRefs.value[agreementId] = el;
  }
};

/**
 * 약관 토글 및 포커스
 */
const toggleAgreement = (ag) => {
  const wasExpanded = ag.expanded;
  ag.expanded = !ag.expanded;
  
  // 펼칠 때만 스크롤
  if (!wasExpanded && ag.expanded) {
    // setTimeout을 사용하여 DOM 업데이트 후 스크롤
    setTimeout(() => {
      const itemEl = agreementItemRefs.value[ag.agreementId];
      if (itemEl && modalBodyRef.value) {
        // 항목의 위치 계산
        const itemTop = itemEl.offsetTop;
        const modalBody = modalBodyRef.value;
        
        // 부드럽게 스크롤
        modalBody.scrollTo({
          top: itemTop - 10,
          behavior: 'smooth'
        });
      }
    }, 50); // 애니메이션 시작 후 스크롤
  }
};

// ─── 약관 모달 열기 ───
function openAgreementModal() {
  // 깊은 복사하여 tempAgreements 세팅 (초기에 모두 접힌 상태)
  tempAgreements.value = agreements.value.map((a) => ({ ...a, expanded: false }));
  agreeAll.value = tempAgreements.value.every((a) => a.checked);
  modalVisible.value = true;
  agreementItemRefs.value = {}; // ref 초기화
}

function toggleAll() {
  tempAgreements.value.forEach((a) => (a.checked = agreeAll.value));
}

function toggleSingle() {
  agreeAll.value = tempAgreements.value.every((a) => a.checked);
}

function confirmAgreements() {
  // 필수 체크 검증
  const requiredUnchecked = tempAgreements.value.filter(
    (a) => a.isRequired && !a.checked,
  );
  if (requiredUnchecked.length > 0) {
    toastStore.warn(t('auth.join.termsRequired'));
    return;
  }
  // 최종 반영
  agreements.value = tempAgreements.value.map((a) => ({ ...a }));
  modalVisible.value = false;
}

onMounted(() => {
  loadAgreements(locale.value);
});
</script>

<style scoped>
/* ═══════════════════════ 전체 페이지 ═══════════════════════ */
.signup-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-top: 65px; /* 헤더 높이만큼 여백 추가 */
  font-weight: 500 !important; /* 기본 폰트 weight 500 설정 */
}

/* 모든 요소의 최소 폰트 weight를 500으로 보장 */
.signup-page * {
  font-weight: inherit;
}

.signup-page *[style*="font-weight: 400"],
.signup-page *[style*="font-weight: 300"],
.signup-page *[style*="font-weight: 200"],
.signup-page *[style*="font-weight: 100"] {
  font-weight: 500 !important;
}

/* ═══════════════════════ 콘텐츠 래퍼 ═══════════════════════ */
.signup-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f7f7;
  padding: 20px;
}

/* ═══════════════════════ 중앙 박스 ═══════════════════════ */
.signup-box {
  width: 1434px;
  height: 680px;
  margin: 0 auto;
  display: flex;
  background: #ffffff;
  overflow: hidden;
}

/* 높이가 충분할 때만 위로 20px 올리기 */
@media (min-height: 851px) {
  .signup-box {
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

.signup-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* ═══════════════════════ 오른쪽 폼 영역 ═══════════════════════ */
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

/* ───────── 헤더 타이포 ───────── */
.title-login {
  text-align: left;
  font-weight: 900;
  margin-bottom: 40px;
}
.title {
  text-align: left;
  font-size: 1.5rem;
  font-weight: 800;
  margin-bottom: 10px;
}
.system-header {
  text-align: left;
  margin-bottom: 30px;
}
.system-subtitle {
  font-size: 0.9rem;
  color: #6c757d;
}

/* 타이머 */
.timer-caption {
  font-size: 0.74rem;
  color: #e03131;
  margin-left: 6px;
}

/* 필드 내부 폰트/패딩 */
::v-deep .v-field__input {
  font-size: 0.92rem;
  padding: 12px 14px 12px 26px !important;
}

/* 기본 버튼 색상 */
.primary-btn {
  background: #3d6ef7;
  color: #fff;
  transition: 0.2s;
}
.primary-btn:hover {
  background: #335eea;
}

/* ─── 약관 동의 스타일 ─── */

/* ─── 약관 동의 버튼 행 ─── */
.agree-row {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: flex-end;
  margin-bottom: 22px;
}
/* 체크박스 기본 스타일 유지 → 크기만 조절 */
.agree-checkbox {
  width: 18px;
  height: 18px;
  pointer-events: none; /* 링크로만 동작 */
}
.agree-checkbox:checked {
  background-color: #3d6ef7;
  border-color: #3d6ef7;
}
.agree-checkbox:checked::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 12px;
}
.agree-link {
  font-size: 0.8rem;
  color: #e03131;
  text-decoration: underline;
  cursor: pointer;
  transition: color 0.2s;
}
.agree-link.ok {
  color: #2f9e44;
}

/* ─── 모달 ─── */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100dvh;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.modal-card {
  background: #fff;
  width: 700px;
  max-width: 90%;
  max-height: 83vh;
  padding: 24px 28px;
  border-radius: 5px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.modal-scroll {
  flex: 1;
  overflow-y: auto;
  margin: 14px 0;
  border: 1px solid #f0f0f0;
  padding: 12px 16px;
  font-size: 0.82rem;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 5px;
}
.agreement-item {
  margin-top: 10px;
}
.agreement-content {
  margin-left: 20px;
  color: #555;
}

/* 모달 타이틀 */
.modal-title {
  font-size: 1.05rem;
  font-weight: 600;
  margin-bottom: 10px;
}

/* 약관 동의 모달 타이틀 - 전역 스타일 오버라이드 (더 구체적인 선택자 사용) */
body[data-theme="light"] .modal-title {
  color: #000000 !important;
}

/* 다크 모드일 때 모달 타이틀 색상 */
body[data-theme="dark"] .modal-title {
  color: #ffffff;
}

/* 모달 본문 스크롤 */
.modal-body {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  padding: 12px 16px;
  font-size: 0.7rem;
  background: #fafafa;
  
  /* 스크롤바 숨김 - 크로스 브라우저 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE, Edge */
}

/* 웹킷(Chrome, Safari) 스크롤바 숨김 */
.modal-body::-webkit-scrollbar {
  display: none;
}

/* 약관 항목 헤더 (체크박스+제목+토글버튼) */
.agreement-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
  font-weight: 600;
}

.agreement-checkbox {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex: 1;
  cursor: pointer;
}

.toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 25px;
  height: 25px;
  background: transparent;
  border: 1px solid #d0d0d0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
  padding: 0;
}

.toggle-btn:hover {
  background: #f0f0f0;
  border-color: #a0a0a0;
}

.toggle-btn i {
  font-size: 18px;
  color: #666;
  line-height: 1;
}

.agreement-item {
  margin-bottom: 18px;
}

.agreement-text {
  border: 1px solid #e9ecef;
  background: #fff;
  padding: 8px 10px;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  margin-bottom: 0;
  margin-top: 8px;
  
  /* 스크롤바 숨김 - 크로스 브라우저 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE, Edge */
}

/* 웹킷(Chrome, Safari) 스크롤바 숨김 */
.agreement-text::-webkit-scrollbar {
  display: none;
}

/* 약관 펼침/접힘 애니메이션 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.2s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
  margin-top: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 200px;
  opacity: 1;
  margin-top: 8px;
}

/* 모두 동의 라인 */
.modal-all {
  margin: 10px 0 15px 0;
  display: flex;
  justify-content: flex-end; /* 오른쪽 정렬 */
}

.modal-all label {
  display: flex;
  align-items: center;
  gap: 5px; /* 체크박스 ↔ 텍스트 5px */
  flex-direction: row-reverse; /* 텍스트-체크박스 순서 */
}

/* '모두 동의합니다' 글자 크기 */
.modal-all label span {
  font-size: 0.8rem;
}

/* ───────── 기본 버튼 스타일 ───────── */
.action-btn {
  width: 100%;
  height: 45px;
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

.join-btn,
.back-btn {
  font-size: 0.875rem;
  background: #f8f8f8;
  border: 1px solid #dadce0;
  color: #555;
}

.join-btn:hover,
.back-btn:hover {
  /* background: #f7f7f7; */
  background: #ececec;
}

.back-btn {
  /* background: #a6c2ff; */
  margin-bottom: 30px;
}

/* ═══════════════════════ 모바일 (≤1500px) ═══════════════════════ */
@media (max-width: 1500px) {
  body {
    min-height: 650px;
  }
  /* 래퍼 스타일 변경 */
  .signup-wrapper {
    background: #fff;
    padding: 0;
  }

  /* 박스 스타일 제거하고 전체 화면 사용 */
  .signup-box {
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
    display: none;
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

  .join-btn,
  .back-btn {
    font-size: 0.7rem;
    border: 1px solid #dadce0;
    color: #555;
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
    font-size: 0.85rem;
    padding: 10px 12px 10px 22px !important;
  }

  /* ─── 모달 카드 패딩 축소 ─── */
  .modal-card {
    max-height: 82dvh;
    padding: 12px 14px;
  }

  /* 모달 본문 글자 크기 */
  .modal-body {
    font-size: 0.65rem;
    min-width: 300px !important;
    max-height: calc(82dvh - 120px);
  }

  /* 타이틀 크기 & line-height */
  .modal-title {
    font-size: 0.9rem;
    line-height: 40px;
  }

  /* 모두 동의합니다 폰트 크기 */
  .modal-all label span {
    font-size: 0.8rem;
  }

  .modal-all label {
    gap: 5px;
  }

  .agreement-text {
    max-height: 300px;
  }
}

@media (max-width: 650px) {
  .modal-card {
    max-height: 75dvh;
    padding: 12px 14px;
  }
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
  .signup-page {
    padding-top: 20px;
  }
}
</style>
