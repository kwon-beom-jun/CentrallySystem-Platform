<template>
  <b-modal
    v-model="show"
    :title="$t('auth.passwordModify.title')"
    :keyboard="false"
    :backdrop="'static'"
    centered
    hide-footer
    size="sm"
    @hidden="onModalHidden"
  >
  
    <DefaultLabel v-if="!adminMode" :text="$t('auth.passwordModify.currentPassword')" forId="currentPassword" size="small" marginBottom="5px"/>
    <DefaultTextfield
      v-if="!adminMode" 
      type="password"
      id="currentPassword"
      v-model="passwords.current"
      size="full"
      :placeholder="$t('auth.passwordModify.currentPasswordPlaceholder')"
      marginBottom="15px"
    />
  
    <DefaultLabel :text="$t('auth.passwordModify.newPassword')" forId="newPassword" size="small" marginBottom="5px"/>
    <DefaultTextfield
      type="password"
      id="newPassword"
      v-model="passwords.new"
      size="full"
      :placeholder="$t('auth.passwordModify.newPasswordPlaceholder')"
      marginBottom="15px"
    />

    <DefaultLabel :text="$t('auth.passwordModify.confirmPassword')" forId="confirmPassword" size="small" marginBottom="5px"/>
    <DefaultTextfield
      type="password"
      id="confirmPassword"
      v-model="passwords.confirm"
      size="full"
      :placeholder="$t('auth.passwordModify.confirmPasswordPlaceholder')"
      marginBottom="15px"
    />
    <div v-if="passwordMismatch" class="text-danger">
      {{ $t('auth.passwordModify.passwordMismatch') }}
    </div>

    <DefaultFormRow align="right" marginTop="25px">
      <DefaultButton color="gray" @click="closeModal" marginRight="5px">
        {{ $t('common.button.cancel') }}
      </DefaultButton>
      <DefaultButton @click="handleChangePassword">
        {{ $t('common.button.modify') }}
      </DefaultButton>
    </DefaultFormRow>
  </b-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vue3-toastify';
import AuthUserApi from '@/api/auth/UsersApi';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';

const { t } = useI18n();

const props = defineProps({
  visible: { type: Boolean, required: true },
  userId: { type: Number, required: true },
  // 관리자 모드 여부를 받는 prop
  adminMode: { type: Boolean, default: false }
});

const emit = defineEmits(['close']);

const show = ref(false);

const passwords = reactive({ current: '', new: '', confirm: '' });

const passwordMismatch = computed(() => 
  passwords.new && passwords.confirm && passwords.new !== passwords.confirm
);

watch(() => props.visible, (newVal) => {
  show.value = newVal;
  if (!newVal) {
    Object.assign(passwords, { current: '', new: '', confirm: '' });
  }
});

function closeModal() {
  show.value = false;
}

function onModalHidden() {
  emit('close');
}

async function handleChangePassword() {
  // [수정] 유효성 검사 분기 처리
  if (!props.adminMode && !passwords.current) {
    toast.error(t('auth.passwordModify.currentPasswordRequired'));
    return;
  }
  if (!passwords.new || !passwords.confirm) {
    toast.error(t('auth.passwordModify.newPasswordRequired'));
    return;
  }
  if (passwordMismatch.value) {
    toast.error(t('auth.passwordModify.passwordMismatch'));
    return;
  }

  // adminMode에 따라 API에 보낼 데이터 분기
  const patchData = { password: passwords.new };
  if (!props.adminMode) {
    patchData.currentPassword = passwords.current;
  }
  
  await AuthUserApi.patchUser(props.userId, patchData);
  toast.success(t('auth.passwordModify.changeSuccess'));
  closeModal();
}
</script>

<style scoped>
.text-danger {
  color: red;
  font-size: 0.8rem;
  margin-top: 5px;
}
</style>