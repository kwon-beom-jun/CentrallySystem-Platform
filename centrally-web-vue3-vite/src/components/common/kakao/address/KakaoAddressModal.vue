<template>
  <!-- 모달 백드롭 (배경) -->
  <div v-if="visible" class="modal-backdrop">
    <!-- 모달 컨테이너 -->
    <div class="modal-container">
      <div class="modal-header">
        <span class="modal-title">{{ $t('common.address.search') }}</span>
        <button class="modal-close" @click="closeModal">✖</button>
      </div>
      <div class="modal-body">
        <!-- 카카오 우편번호 검색 영역 -->
        <!-- 아래 div에 우편번호 서비스가 embed(iFrame) 형태로 표시됨 -->
        <div
          ref="postcodeContainer"
          style="width: 100%; height: 100%;"
          class="scaled-postcode" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = defineProps({
  visible: Boolean
});

const emits = defineEmits(['close', 'selectAddress']);

/**
 * 카카오 주소 검색 창이 삽입될 div
 */
const postcodeContainer = ref(null);

/**
 * 모달이 열릴 때마다 카카오 우편번호 검색창 embed
 */
watch(() => props.visible, async (newVal) => {
  if (newVal) {
    await nextTick();
    openPostcodePopup();
  }
});

/**
 * 카카오 주소 검색 창 오픈
 */
function openPostcodePopup() {
  if (!window.daum?.Postcode) {
    console.error('카카오 우편번호 스크립트가 로드되지 않았습니다.');
    return;
  }

  const postcode = new window.daum.Postcode({
    oncomplete(data) {
      emits('selectAddress', {
        address: data.address,
        zipCode: data.zonecode
      });
      closeModal();
    },
    onresize() {
      // 필요 시 구현 가능
    },
  });

  // 팝업 대신 embed -> 모달 내부를 사용
  postcode.embed(postcodeContainer.value, {
    width: '100%',
    height: '100%'
  });
}

function closeModal() {
  emits('close');
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0; /* top:0, left:0, right:0, bottom:0 와 동일 */
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1050; /* 모달 뒤 배경 */
}

.modal-container {
  width: 515px;
  height: 560px;
  margin: 50px auto;
  background: #fff;
  position: relative;
  top: 50px;
  padding: 5px 5px 5px 5px;
  display: flex;
  flex-direction: column;
  z-index: 1100; /* 모달 자체는 백드롭보다 한 단계 위 */
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #F2F2F2; /* 카카오 스타일 회색 */
}

.modal-title {
  font-size: 1.2rem;
  font-weight: bold;
}

.modal-body {
  /* iFrame이 부모보다 작아지지 않도록 overflow 숨기거나 auto */
  flex: 1;
  overflow: hidden; /* 또는 auto. 필요에 따라 조정 */
  position: relative;
}

@media (max-width: 650px) {
  .modal-title {
    font-size: 0.85rem !important;
    font-weight: bold;
  }

  .modal-header {
    padding: 7px 10px;
  }

  .modal-container {
    width: 410px;
    height: 450px;
    padding: 5px;
  }

  .scaled-postcode {
    transform: scale(0.8);
    transform-origin: top left;
  }
}

@media (max-width: 450px) {
  .modal-title {
    font-size: 0.65rem;
    font-weight: bold;
  }

  .modal-header {
    padding: 5px 7px;
  }

  .modal-container {
    width: 350px;
    height: 390px;
    padding: 3px;
  }

  .scaled-postcode {
    transform: scale(0.7);
    transform-origin: top left;
  }
}

@media (max-width: 380px) {
  .modal-title {
    font-size: 0.65rem;
    font-weight: bold;
  }

  .modal-header {
    padding: 5px 7px;
  }

  .modal-container {
    width: 310px;
    height: 345px;
    padding: 3px;
  }

  .scaled-postcode {
    transform: scale(0.61);
    transform-origin: top left;
  }
}
</style>
