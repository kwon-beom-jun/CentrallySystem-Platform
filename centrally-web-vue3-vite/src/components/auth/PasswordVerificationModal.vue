<template>
  <b-modal
    v-model="show"
    :title="$t('auth.passwordVerification.title')"
    :keyboard="false"
    :backdrop="'static'"
    :no-close-on-backdrop="true"
    :no-close-on-esc="true"
    :hide-header-close="true"
    centered
    hide-footer
    size="sm"
    @hidden="onModalHidden"
  >
    <div class="verification-content">
      <div class="verification-icon">
        <i class="ri-lock-line"></i>
      </div>
      <div class="verification-text">
        <p>{{ $t('auth.passwordVerification.message1') }}</p>
        <p>{{ $t('auth.passwordVerification.message2') }}</p>
      </div>
      
      <div class="password-field">
        <DefaultLabel :text="$t('auth.passwordVerification.currentPassword')" forId="currentPassword" size="small" marginBottom="5px"/>
        <DefaultTextfield
          type="password"
          id="currentPassword"
          v-model="currentPassword"
          size="full"
          :placeholder="$t('auth.passwordVerification.currentPasswordPlaceholder')"
          marginBottom="20px"
          @keydown.enter="handleVerification"
        />
      </div>
      
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </div>

    <DefaultFormRow align="right" marginTop="25px">
      <DefaultButton color="gray" @click="handleCancel" marginRight="5px">
        {{ $t('common.button.cancel') }}
      </DefaultButton>
      <DefaultButton @click="handleVerification" :disabled="!currentPassword">
        {{ $t('common.button.confirm') }}
      </DefaultButton>
    </DefaultFormRow>
  </b-modal>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vue3-toastify';
import AuthUserApi from '@/api/auth/UsersApi';
import { useAuthStore } from '@/store/auth';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';

const { t } = useI18n();

const props = defineProps({
  visible: { type: Boolean, required: true },
  userId: { type: Number, required: true }
});

const emit = defineEmits(['close', 'verified']);

const authStore = useAuthStore();
const show = ref(false);
const currentPassword = ref('');
const errorMessage = ref('');

watch(() => props.visible, (newVal) => {
  show.value = newVal;
  if (!newVal) {
    currentPassword.value = '';
    errorMessage.value = '';
  }
});

function onModalHidden() {
  emit('close');
}

function handleCancel() {
  // 이전 화면으로 이동
  window.history.back();
}

async function handleVerification() {
  if (!currentPassword.value) {
    errorMessage.value = t('auth.passwordVerification.passwordRequired');
    return;
  }

  try {
    // 새로운 비밀번호 확인 API 사용
    const response = await AuthUserApi.verifyPassword(currentPassword.value);
    
    if (response && response.data) {
      toast.success(t('auth.passwordVerification.verifySuccess'));
      emit('verified');
    }
  } catch (error) {
    errorMessage.value = t('auth.passwordVerification.passwordMismatch');
    currentPassword.value = '';
  }
}
</script>

<style scoped>
.verification-content {
  text-align: center;
}

.verification-icon {
  margin-bottom: 20px;
}

.verification-icon i {
  font-size: 3rem;
  color: #0b6bcb;
}

.verification-text {
  font-size: 1rem;
  color: #374151;
  margin-bottom: 20px;
}

.verification-text p {
  margin: 5px 0;
}

.password-field {
  text-align: left;
}

.error-message {
  color: #dc2626;
  font-size: 0.875rem;
  margin-top: 10px;
  text-align: center;
}

/* 모바일 스타일 (650px 이하) */
@media (max-width: 650px) {
  .verification-icon {
    margin-bottom: 12px;
  }
  
  .verification-icon i {
    font-size: 2.2rem;
  }
  
  .verification-text {
    font-size: 0.9rem;
    margin-bottom: 15px;
  }
  
  .verification-text p {
    margin: 3px 0;
  }
}
</style>
