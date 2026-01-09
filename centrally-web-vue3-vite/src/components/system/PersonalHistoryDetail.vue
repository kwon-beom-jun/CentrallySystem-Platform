<template>
  <!-- 모달 -->
  <div 
    class="modal fade"
    id="personalHistoryDetailModal"
    tabindex="-1"
    aria-labelledby="personalHistoryDetailModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <!-- 모달 헤더 -->
        <div class="modal-header">
          <h5 class="modal-title" id="personalHistoryDetailModalLabel">
            {{ modalTitle }}
          </h5>
          <button 
            type="button" 
            class="btn-close" 
            data-bs-dismiss="modal" 
            aria-label="Close"
            @click="closeModal"
          ></button>
        </div>

        <!-- 모달 본문 -->
        <div class="modal-body">
          <!-- (1) 이름 + 발생 시간 -->
          <div class="row mb-2">
            <div class="col-6">
              <DefaultLabel :text="$t('system.personalHistory.name')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localHistoryData.username"
                size="full"
                disabled
              />
            </div>
            <div class="col-6">
              <DefaultLabel :text="$t('system.personalHistory.occurredAt')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localHistoryData.timestamp"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- (2) 이메일 (단독 줄) -->
          <div class="row mb-2">
            <div class="col-12">
              <DefaultLabel :text="$t('system.personalHistory.email')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localHistoryData.userEmail"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- (3) 메뉴 + 기능 -->
          <div class="row mb-2">
            <div class="col-4">
              <DefaultLabel :text="$t('system.personalHistory.menu')" size="small" />
              <DefaultTextfield
                type="text"
                :modelValue="displayMenu"
                size="full"
                disabled
              />
            </div>
            <div class="col-8">
              <DefaultLabel :text="$t('system.personalHistory.feature')" size="small" />
              <DefaultTextfield
                type="text"
                :modelValue="displayFunction"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- (4) HTTP Method + IP -->
          <div class="row mb-2">
            <div class="col-4">
              <DefaultLabel :text="$t('system.personalHistory.method')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localHistoryData.httpMethod"
                size="full"
                disabled
              />
            </div>
            <div class="col-8">
              <DefaultLabel :text="$t('system.personalHistory.ip')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localHistoryData.ip"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- (5) 브라우저/OS 정보 -->
          <div class="row mb-2">
            <div class="col-6">
              <DefaultLabel :text="$t('system.personalHistory.browser')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="displayBrowserInfo"
                size="full"
                disabled
              />
            </div>
            <div class="col-6">
              <DefaultLabel :text="$t('system.personalHistory.os')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="displayOSInfo"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- (6) HTTP Body (단독 줄, 여러 줄 표시) - showBody가 true일 때만 표시 -->
          <div v-if="showBody" class="row mb-2">
            <div class="col-12">
              <DefaultLabel :text="$t('system.personalHistory.body')" size="small" />
              <DefaultTextarea
                v-model="localHistoryData.httpBody"
                size="full"
                disabled
                rows="8"
              />
            </div>
          </div>
        </div>

        <!-- 모달 푸터 -->
        <div class="modal-footer">
          <DefaultButton 
            @click="closeModal"
            color="gray"
            size="small">
            {{ $t('system.common.close') }}
          </DefaultButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
import * as bootstrap from 'bootstrap';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultTextarea from '@/components/common/textarea/DefaultTextarea.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import { cloneDeep } from 'lodash';
import { translateMenuKey, translateFunctionKey } from '@/utils/i18nDisplay';

/**
 * 부모로부터 받은 이력 데이터
 */
const props = defineProps({
  historyData: {
    type: Object,
    default: null
  },
  title: {
    type: String,
    default: ''
  },
  showBody: {
    type: Boolean,
    default: true
  }
});

/**
 * 모달 닫기 이벤트
 */
const emit = defineEmits(['close']);

/**
 * 모달 인스턴스
 */
const modalInstance = ref(null);

/**
 * 모달에서 직접 조작할 로컬 데이터
 */
const localHistoryData = ref({});

/**
 * 모달 타이틀 (props 우선 적용)
 */
const modalTitle = computed(() => props.title || t('system.personalHistory.detailTitle'));

/**
 * 표시용 브라우저 정보
 */
const displayBrowserInfo = computed(() => {
  if (!localHistoryData.value.fullData) return '-';
  const browser = localHistoryData.value.fullData.browser;
  return browser && browser.trim() ? browser : '-';
});

/**
 * 표시용 OS 정보
 */
const displayOSInfo = computed(() => {
  if (!localHistoryData.value.fullData) return '-';
  const os = localHistoryData.value.fullData.os;
  return os && os.trim() ? os : '-';
});

/**
 * 표시용 메뉴명 (다국어)
 */
const displayMenu = computed(() => {
  const menuKey = localHistoryData.value?.menu;
  const translated = translateMenuKey(menuKey, t);
  return translated || '-';
});

/**
 * 표시용 기능명 (다국어)
 */
const displayFunction = computed(() => {
  const functionKey = localHistoryData.value?.function;
  const translated = translateFunctionKey(functionKey, t);
  return translated || '-';
});

/**
 * 전달받은 historyData가 변경될 때마다 localHistoryData 갱신
 */
watch(
  () => props.historyData,
  (newData) => {
    if (newData) {
      localHistoryData.value = cloneDeep(newData);
    }
  },
  { deep: true, immediate: true }
);

/**
 * 컴포넌트 마운트 시 모달 초기화
 */
onMounted(() => {
  const modalEl = document.getElementById('personalHistoryDetailModal');
  modalInstance.value = new bootstrap.Modal(modalEl, {});
  
  // 모달이 ESC나 배경 클릭 등으로 닫힐 때 발생하는 hidden.bs.modal 이벤트 처리
  modalEl.addEventListener('hidden.bs.modal', () => {
    emit('close');
  });
  
  // 모달 표시
  modalInstance.value.show();
});

/**
 * 모달 닫기
 */
function closeModal() {
  modalInstance.value.hide();
  emit('close');
}
</script>

<style scoped>
.modal-header {
  display: flex;
  justify-content: space-between;
  font-size: 1.2rem;
}
</style>
