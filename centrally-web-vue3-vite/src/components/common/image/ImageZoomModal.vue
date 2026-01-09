<template>
  <!-- 이미지 확대 모달 -->
  <b-modal
    v-model="innerModalVisible"
    title=""
    hide-footer
    hide-header
    centered
    dialog-class="image-zoom-dialog"
    body-class="p-0 bg-transparent"
    content-class="bg-transparent border-0"
    @hidden="closeModal"
  >
    <div class="modal-content-wrapper" @click="closeModal">
      <div class="image-zoom-container" @click.stop>
        <button @click="closeModal" class="image-close-btn">
          <i class="ri-close-line"></i>
        </button>
        <img
          v-if="imageUrl"
          :src="imageUrl"
          :alt="altComputed"
          class="zoomed-image"
          @click.stop
        />
      </div>
    </div>
  </b-modal>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  visible: { type: Boolean, default: false },
  imageUrl: { type: String, default: '' },
  altText: { type: String, default: '' }
});

const emits = defineEmits(['close']);

/* i18n */
const { t } = useI18n();
const altComputed = computed(() => props.altText || t('common.label.image'));

// b-modal 제어를 위한 내부 ref
const innerModalVisible = ref(props.visible);

// 모달 닫기
function closeModal() {
  emits('close');
}

// props.visible 변화 감지
watch(
  () => props.visible,
  (newVal) => {
    innerModalVisible.value = newVal;
  }
);

// 모달이 닫혔을 때 부모로 close 이벤트
watch(
  () => innerModalVisible.value,
  (newVal) => {
    if (!newVal) {
      emits('close');
    }
  }
);
</script>

<style scoped>
/* ==================================
 * 이미지 확대 모달 스타일
===================================== */
:deep(.image-zoom-dialog) {
  max-width: 350px !important;
  width: 350px !important;
  max-height: 350px !important;
}

:deep(.image-zoom-dialog .modal-dialog) {
  max-width: 350px !important;
  width: 350px !important;
  margin: 0 auto !important;
}

:deep(.image-zoom-dialog .modal-content) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  width: 100% !important;
}

:deep(.image-zoom-dialog .modal-body) {
  background: transparent !important;
  padding: 0 !important;
}

.modal-content-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  min-height: 350px;
}

.image-zoom-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  width: 310px;
  height: 310px;
  cursor: default;
}

.image-close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-size: 1.3rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
}

.image-close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.zoomed-image {
  width: 300px;
  height: 300px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}
</style> 