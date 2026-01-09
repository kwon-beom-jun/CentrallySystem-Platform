<template>
  <!-- v-model 로 부모에서 열고 닫음 -->
  <v-dialog
    v-model="internalOpen"
    max-width="380"
    transition="fade-transition"
    persistent
  >
    <v-card class="forgot-card" elevation="2">
      <!-- 타이틀 -->
      <v-card-title class="forgot-title">{{ $t('auth.forgotPassword.title') }}</v-card-title>
      <v-card-text class="forgot-subtitle">{{ $t('auth.forgotPassword.subtitle') }}</v-card-text>
      <!-- 본문 -->
      <v-card-text class="forgot-body">
        <!-- ① 이메일 -->
        <v-text-field
          v-model="email"
          :placeholder="$t('auth.forgotPassword.emailPlaceholder')"
          prepend-inner-icon="mdi-email-outline"
          density="compact"
          variant="outlined"
          hide-details
          class="forgot-field"
          :disabled="emailSent"
        />

        <!-- ② 인증코드 (메일 발송 후 노출) -->
        <v-text-field
          v-if="emailSent"
          v-model="code"
          :placeholder="$t('auth.forgotPassword.codePlaceholder')"
          prepend-inner-icon="mdi-shield-key-outline"
          maxlength="6"
          density="compact"
          variant="outlined"
          hide-details
          class="forgot-field"
          :disabled="verificationCodeDisable"
        />

        <!-- 남은 시간 표시 (메일 발송 후) -->
        <small
          v-if="emailSent && !verificationCodeDisable && countdown > 0"
          class="timer-caption"
        >
          {{ countdown }}s
        </small>
        
        <!-- 안내문 (메일 발송 후) -->
        <small v-if="emailSent" class="guide-text"> {{ $t('auth.forgotPassword.guideText') }} </small>
      </v-card-text>

      <!-- 액션 버튼 -->
      <v-card-actions class="forgot-actions">
        <!-- 인증메일 발송 버튼 (초기 단계) -->
        <v-btn
          v-if="!emailSent"
          class="forgot-btn tonal"
          variant="tonal"
          color="primary"
          block
          @click="sendCode"
        >
          {{ $t('auth.forgotPassword.sendCode') }}
        </v-btn>

        <!-- 인증 확인 버튼 (메일 발송 후) -->
        <v-btn
          v-if="emailSent"
          class="forgot-btn flat"
          variant="flat"
          color="primary"
          block
          @click="verifyCode"
        >
          {{ $t('auth.forgotPassword.verifyCode') }}
        </v-btn>

        <v-btn
          class="forgot-btn text"
          variant="text"
          block
          @click="$emit('update:modelValue', false)"
        >
          {{ $t('common.button.close') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref, watch, onUnmounted } from "vue";
import { useI18n } from 'vue-i18n';
import { toast } from "vue3-toastify";
import EmailApi from "@/api/auth/EmailApi";

const { t } = useI18n();

/* props & emits */
const props = defineProps({ modelValue: Boolean });
const emit = defineEmits(['update:modelValue']);

/* 상태 */
const internalOpen = ref(false);
const email = ref("");
const code = ref("");
const emailSent = ref(false);
const countdown = ref(0);              // 남은 시간(초)
const verificationCodeDisable = ref(false);
let timerInterval = null;

function startTimer() {
  countdown.value = 300; // 5분
  verificationCodeDisable.value = false;
  if (timerInterval) clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    if (countdown.value > 0) countdown.value--;
    else clearInterval(timerInterval);
  }, 1000);
}

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});

/* 외부 ↔ 내부 동기화 */
watch(
  () => props.modelValue,
  (open) => {
    internalOpen.value = open;
    if (open) {
      email.value = "";
      code.value = "";
      countdown.value = 0;
      verificationCodeDisable.value = false;
      emailSent.value = false;
      if (timerInterval) clearInterval(timerInterval);
    }
  },
  { immediate: true }
);

/**
 * 이메일 형식 검증
 */
const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/* ① 인증메일 발송 */
const sendCode = async () => {
  if (!email.value) return toast.error(t('auth.forgotPassword.emailRequired'));
  if (!validateEmail(email.value)) return toast.error(t('auth.forgotPassword.emailFormat'));
  
  const response = await EmailApi.mailResetSend({ email: email.value });
  toast.success(response.data.message);
  emailSent.value = true; // 이메일 발송 성공 플래그
  startTimer();
};

/* ② 인증번호 검증 */
const verifyCode = async () => {
  if (!email.value || !code.value) return toast.error(t('auth.forgotPassword.emailAndCodeRequired'));
  const response = await EmailApi.mailResetVerify({ email: email.value, code: code.value });
  verificationCodeDisable.value = true;
  toast.success(t('auth.forgotPassword.verifySuccess'));
  
  internalOpen.value = false;
  // 부모 컴포넌트에도 닫힘 전달
  emit("update:modelValue", false);
  if (timerInterval) clearInterval(timerInterval);
};
</script>

<style scoped>
/* 카드 */
.forgot-card {
  padding: 24px;
  background: #fdfdfd;
}

/* 타이틀 */
.forgot-title {
  margin-bottom: 10px;
  font-size: 1.1rem;
  font-weight: 900;
  padding: 0;
}

/* 서브 타이틀 */
.forgot-subtitle {
  margin-bottom: 10px;
  font-size: 0.8rem !important;
  padding: 0 !important;
  color: #6b6b6b;
}

/* 본문 여백 제거 */
.forgot-body {
  padding: 10px 0 10px 0 !important;
}

/* 입력 필드 간격 */
.forgot-field + .forgot-field {
  margin-top: 8px; /* field 사이 8px 간격 */
}

/* 안내 문구 */
.guide-text {
  display: block;
  margin-top: 12px;
  font-size: 0.78rem;
  color: #6b6b6b;
}

/* 타이머 */
.timer-caption {
  font-size: 0.74rem;
  color: #e03131;
  margin-top: 6px;
}

/* 버튼 영역 */
.forgot-actions {
  padding: 0;
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

/* 버튼 공통 */
.forgot-btn {
  font-size: 0.8rem;
  width: 100%;
  border-radius: 4px !important;
}

/* 버튼 간 여백 */
.forgot-btn.flat, .forgot-btn.tonal {
  margin-top: 8px;
  border: 1px solid #71a0ff;
  color: #555;
}
.forgot-btn.text {
  margin-top: 4px;
  border: 1px solid #c0c0c0;
  color: #555;
}

/* 입력 필드 폰트 크기 */
::v-deep .forgot-field .v-field__input {
  font-size: 0.75rem !important; /* 기본(데스크톱) 1rem */
}


/* ─── 모바일(≤600px) ─── */
@media (max-width: 600px) {
  .forgot-card {
    padding: 18px;
  }
  .forgot-title {
    font-size: 0.9rem;
  }
  /* 서브 타이틀 */
  .forgot-subtitle {
    margin-bottom: 10px;
    font-size: 0.75rem !important;
    padding: 0 !important;
    color: #6b6b6b;
  }
  .guide-text {
    font-size: 0.72rem;
  }
  .forgot-btn {
    font-size: 0.75rem;
  }
  
  ::v-deep .forgot-field .v-field__input {
    font-size: 0.68rem !important;
    min-height: 35px;
    padding: 6px 6px 6px 10px;
  }

  /* 모바일에서 입력 필드 높이 조정 */
  :deep(.forgot-field .v-field) {
    min-height: 35px;
  }

  /* 모바일에서 입력 필드 내부 아이콘 크기 조정 */
  :deep(.forgot-field .v-icon) {
    font-size: 1.1rem; /* Vuetify 기본값은 약 1.5rem 입니다. */
  }
}
</style>
