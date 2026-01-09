<template>
  <!-- 모달 -->
  <div 
    class="modal fade"
    id="scheduleNotificationHistoryDetailModal"
    tabindex="-1"
    aria-labelledby="scheduleNotificationHistoryDetailModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <!-- 모달 헤더 -->
        <div class="modal-header">
          <h5 class="modal-title" id="scheduleNotificationHistoryDetailModalLabel">
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
          <!-- 알림 발송 상태 안내 -->
          <InfoNotice 
            v-if="localData.channel && localData.sent !== undefined"
            :color="localData.sent ? 'green' : 'red'"
            :icon="localData.sent ? 'ri-checkbox-circle-line' : 'ri-error-warning-line'"
            :text="`[${localData.channel}] 알림 발송 ${localData.sent ? '성공' : '실패'}`"
            fontWeight="bold"
            marginBottom="15px"
          />

          <!-- 사용자명과 로그 ID (한 줄) -->
          <div class="row mb-2">
            <div class="col-6">
              <DefaultLabel :text="$t('info.scheduleNotificationHistory.userName')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.userName"
                size="full"
                disabled
              />
            </div>
            <div class="col-6">
              <DefaultLabel :text="$t('info.scheduleNotificationHistory.logId')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.logId"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- 이메일 -->
          <div class="row mb-2">
            <div class="col-12">
              <DefaultLabel :text="$t('info.scheduleNotificationHistory.userEmail')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.userEmail"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- 일정 시작 시간과 일정 종료 시간 (한 줄) -->
          <div class="row mb-2">
            <div class="col-6">
              <DefaultLabel :text="$t('info.scheduleNotificationHistory.scheduleStartTime')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="displayScheduleStartTime"
                size="full"
                disabled
              />
            </div>
            <div class="col-6">
              <DefaultLabel :text="$t('info.scheduleNotificationHistory.scheduleEndTime')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="displayScheduleEndTime"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- 예정 시간과 발송 시간 (한 줄) -->
          <div class="row mb-2">
            <div class="col-6">
              <DefaultLabel :text="$t('info.scheduleNotificationHistory.scheduledTime')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="displayScheduledTime"
                size="full"
                disabled
              />
            </div>
            <div class="col-6">
              <DefaultLabel :text="$t('info.scheduleNotificationHistory.sentTime')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="displaySentTime"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- 알림 유형과 알림 설정 시간 (한 줄) -->
          <div class="row mb-2">
            <div class="col-6">
              <DefaultLabel :text="$t('info.scheduleNotificationHistory.notificationType')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.notificationType"
                size="full"
                disabled
              />
            </div>
            <div class="col-6">
              <DefaultLabel :text="$t('info.scheduleNotificationHistory.notificationValue')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.notificationValue"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- 실패 사유 -->
          <div v-if="localData.failureReason" class="row mb-2">
            <div class="col-12">
              <DefaultLabel :text="$t('info.scheduleNotificationHistory.failureReason')" size="small" />
              <DefaultTextarea
                v-model="localData.failureReason"
                size="full"
                disabled
                rows="3"
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
            {{ $t('common.button.close') }}
          </DefaultButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits, onMounted, onBeforeUnmount, watch } from 'vue';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
import * as bootstrap from 'bootstrap';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultTextarea from '@/components/common/textarea/DefaultTextarea.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import InfoNotice from '@/components/common/notice/InfoNotice.vue';
import { cloneDeep } from 'lodash';

/**
 * 부모로부터 받은 알림 발송 이력 데이터
 */
const props = defineProps({
  historyData: {
    type: Object,
    default: null
  },
  title: {
    type: String,
    default: ''
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
const localData = ref({});

/**
 * 모달 타이틀 (props 우선 적용)
 */
const modalTitle = computed(() => props.title || t('info.scheduleNotificationHistory.detailTitle'));

/**
 * 날짜/시간 포맷팅
 * @param {string} dateTimeString 날짜/시간 문자열
 * @returns {string} 포맷팅된 문자열
 */
function formatDateTime(dateTimeString) {
  if (!dateTimeString) return '-';
  try {
    const date = new Date(dateTimeString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  } catch {
    return dateTimeString;
  }
}

/**
 * 표시용 일정 시작 시간
 */
const displayScheduleStartTime = computed(() => {
  return formatDateTime(localData.value.scheduleStartTime);
});

/**
 * 표시용 일정 종료 시간
 */
const displayScheduleEndTime = computed(() => {
  return formatDateTime(localData.value.scheduleEndTime);
});

/**
 * 표시용 예정 시간
 */
const displayScheduledTime = computed(() => {
  return formatDateTime(localData.value.scheduledTime);
});

/**
 * 표시용 발송 시간
 */
const displaySentTime = computed(() => {
  return formatDateTime(localData.value.sentTime);
});

/**
 * 전달받은 historyData가 변경될 때마다 localData 갱신
 */
watch(
  () => props.historyData,
  (newData) => {
    if (newData) {
      localData.value = cloneDeep(newData);
      // logId를 문자열로 변환
      if (localData.value.logId !== undefined) {
        localData.value.logId = String(localData.value.logId);
      }
      // null 값 처리
      if (!localData.value.userName) localData.value.userName = '-';
      if (!localData.value.userEmail) localData.value.userEmail = '-';
      if (!localData.value.notificationType) localData.value.notificationType = '-';
      if (!localData.value.notificationValue) localData.value.notificationValue = '-';
    }
  },
  { deep: true, immediate: true }
);

/**
 * 모바일로 전환 시 모달 닫기
 */
function handleResize() {
  if (window.innerWidth <= 650) {
    closeModal();
  }
}

/**
 * 컴포넌트 마운트 시 모달 초기화
 */
onMounted(() => {
  const modalEl = document.getElementById('scheduleNotificationHistoryDetailModal');
  modalInstance.value = new bootstrap.Modal(modalEl, {});
  
  // 모달이 ESC나 배경 클릭 등으로 닫힐 때 발생하는 hidden.bs.modal 이벤트 처리
  modalEl.addEventListener('hidden.bs.modal', () => {
    emit('close');
  });
  
  // 리사이즈 이벤트 리스너 추가
  window.addEventListener('resize', handleResize);
  
  // 모달 표시
  modalInstance.value.show();
});

/**
 * 컴포넌트 언마운트 시 리스너 제거
 */
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
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

