<template>
  <transition name="fade">
    <div 
      v-if="isVisible" 
      class="custom-modal-pop-up" 
      :style="{ zIndex: 1100 + props.zIndex }"
      @click.self="handleBackgroundClick"
    >
      <div class="custom-modal-pop-up-dialog">
        <div class="custom-modal-pop-up-content">
          <div v-if="title" class="custom-modal-pop-up-header">
            {{ title }}
          </div>
          <div class="custom-modal-pop-up-body" :class="{ 'no-title': !title }">
            <input type="text" v-model="inputValue" class="form-control" />
          </div>
          <div class="custom-modal-pop-up-footer">
            <DefaultFormRow align="right">
              <DefaultButton
                v-if="cancelTextComputed"
                color="gray"
                marginRight="5px"
                size="small"
                @click="closeModal"
              >
                {{ cancelTextComputed }}
              </DefaultButton>

              <DefaultButton 
                size="small" 
                @click="confirmEdit"
              >
                {{ confirmTextComputed }}
              </DefaultButton>
            </DefaultFormRow>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';

const props = defineProps({
  isVisible: Boolean,
  title: {
    type: String,
    default: '',
  },
  confirmText: {
    type: String,
    default: '',
  },
  cancelText: {
    type: String,
    default: '',
  },
  value: {
    type: String,
    default: '',
  },
  disableBackgroundClose: {
    type: Boolean,
    default: false,
  },
  zIndex: {
    type: Number,
    default: 0, // 기본값은 0 (1100으로 더해질 값)
  },
});

const emits = defineEmits(['confirm', 'close', 'update:value']);

const inputValue = ref(props.value);

/* i18n */
const { t } = useI18n();
const confirmTextComputed = computed(() => props.confirmText || t('common.button.save'));
const cancelTextComputed = computed(() => props.cancelText || t('common.button.cancel'));

// Watch for prop changes to update the input value
watch(
  () => props.value,
  (newValue) => {
    inputValue.value = newValue;
  }
);

const handleBackgroundClick = () => {
  if (!props.disableBackgroundClose) {
    closeModal();
  }
};

const closeModal = () => {
  emits('close');
};

const confirmEdit = () => {
  emits('update:value', inputValue.value);
  emits('confirm');
};
</script>

<style scoped>
h5 {
  margin-top: 10px !important;
  margin-bottom: 10px !important;
}

/* 기존 스타일 */
.custom-modal-pop-up {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: var(--theme-modal-overlay);
  z-index: 1100;
}

.custom-modal-pop-up-dialog {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  border-radius: 8px;
  max-width: 400px;
  margin: 0 auto;
}

.custom-modal-pop-up-content {
  background-color: var(--theme-modal-bg);
  color: var(--theme-text-primary);
  border-radius: 8px;
  padding: 30px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 5px 15px var(--theme-shadow-lg);
}

.custom-modal-pop-up-header {
  font-size: 1rem;
  text-align: left;
  border-bottom: none;
}

.custom-modal-pop-up-body {
  padding: 30px 0;
  text-align: left;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--theme-text-secondary);
}

.custom-modal-pop-up-body.no-title {
  padding-top: 10px;
}

.custom-modal-pop-up-footer {
  border-top: none;
  padding: 0;
  margin: 0;
}

/* 애니메이션 효과 */
.fade-enter-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
}

.form-control {
  font-size: 0.875rem;
}

/* 반응형: 모바일 환경에서 폰트 크기 조정 */
@media (max-width: 650px) {
  .custom-modal-pop-up-content {
    width: 70%;
    padding: 20px;
  }

  .custom-modal-pop-up-header {
    font-size: 0.875rem;
    font-weight: 900;
    margin-bottom: 0px;
  }

  .custom-modal-pop-up-body {
    padding: 20px 0;
    font-size: 0.75rem;
  }

  .form-control {
    font-size: 0.75rem;
  }
}
</style>