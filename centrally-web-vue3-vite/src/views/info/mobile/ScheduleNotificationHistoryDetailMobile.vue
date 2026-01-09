<template>
  <div>
    <div class="content content-wrapper">
      <MobileDetailTitle 
        :title="$t('info.scheduleNotificationHistory.detailTitle')" 
        :showBackButton="true"
        :backButtonLabel="$t('common.button.back')"
      />

      <div class="modal-body">
        <div class="form-pane">
          <!-- 알림 발송 상태 안내 -->
          <InfoNotice 
            v-if="formData.channel && formData.sent !== undefined"
            :color="formData.sent ? 'green' : 'red'"
            :icon="formData.sent ? 'ri-checkbox-circle-line' : 'ri-error-warning-line'"
            :text="`[${formData.channel}] 알림 발송 ${formData.sent ? '성공' : '실패'}`"
            fontWeight="bold"
            marginBottom="10px"
          />

          <!-- 사용자명과 로그 ID (한 줄) -->
          <div class="form-group cs-card--blue">
            <DefaultFormRow align="betweenEqual" gap="12px">
              <div class="field-half">
                <MobileMintLabel :text="$t('info.scheduleNotificationHistory.userName')" size="small" />
                <MobileMintTextfield type="text" v-model="formData.userName" size="full" :disabled="true" />
              </div>
              <div class="field-half">
                <MobileMintLabel :text="$t('info.scheduleNotificationHistory.logId')" size="small" />
                <MobileMintTextfield type="text" v-model="formData.logId" size="full" :disabled="true" />
              </div>
            </DefaultFormRow>
          </div>

          <!-- 이메일 -->
          <div class="form-group cs-card--blue">
            <MobileMintLabel :text="$t('info.scheduleNotificationHistory.userEmail')" size="small" />
            <MobileMintTextfield type="text" v-model="formData.userEmail" size="full" :disabled="true" />
          </div>

          <!-- 일정 시작 시간과 일정 종료 시간 (한 줄) -->
          <div class="form-group cs-card--blue">
            <DefaultFormRow align="betweenEqual" gap="12px">
              <div class="field-half">
                <MobileMintLabel :text="$t('info.scheduleNotificationHistory.scheduleStartTime')" size="small" />
                <MobileMintTextfield type="text" v-model="formData.scheduleStartTime" size="full" :disabled="true" />
              </div>
              <div class="field-half">
                <MobileMintLabel :text="$t('info.scheduleNotificationHistory.scheduleEndTime')" size="small" />
                <MobileMintTextfield type="text" v-model="formData.scheduleEndTime" size="full" :disabled="true" />
              </div>
            </DefaultFormRow>
          </div>

          <!-- 예정 시간과 발송 시간 (한 줄) -->
          <div class="form-group cs-card--blue">
            <DefaultFormRow align="betweenEqual" gap="12px">
              <div class="field-half">
                <MobileMintLabel :text="$t('info.scheduleNotificationHistory.scheduledTime')" size="small" />
                <MobileMintTextfield type="text" v-model="formData.scheduledTime" size="full" :disabled="true" />
              </div>
              <div class="field-half">
                <MobileMintLabel :text="$t('info.scheduleNotificationHistory.sentTime')" size="small" />
                <MobileMintTextfield type="text" v-model="formData.sentTime" size="full" :disabled="true" />
              </div>
            </DefaultFormRow>
          </div>

          <!-- 알림 유형과 알림 설정 시간 (한 줄) -->
          <div class="form-group cs-card--blue">
            <DefaultFormRow align="betweenEqual" gap="12px">
              <div class="field-half">
                <MobileMintLabel :text="$t('info.scheduleNotificationHistory.notificationType')" size="small" />
                <MobileMintTextfield type="text" v-model="formData.notificationType" size="full" :disabled="true" />
              </div>
              <div class="field-half">
                <MobileMintLabel :text="$t('info.scheduleNotificationHistory.notificationValue')" size="small" />
                <MobileMintTextfield type="text" v-model="formData.notificationValue" size="full" :disabled="true" />
              </div>
            </DefaultFormRow>
          </div>

          <!-- 실패 사유 -->
          <div v-if="formData.failureReason" class="form-group cs-card--blue cs-card--white">
            <MobileMintLabel :text="$t('info.scheduleNotificationHistory.failureReason')" size="small" />
            <MobileMintTextfield 
              type="text" 
              v-model="formData.failureReason" 
              size="full" 
              :disabled="true" 
              bg-color="#fff0f0" 
            />
          </div>

          <DefaultFormRow align="right" marginTop="10px">
            <MobileMintButton color="gray" @click="goBack">{{ $t('common.button.back') }}</MobileMintButton>
          </DefaultFormRow>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import MobileDetailTitle from '@/components/common/title/MobileDetailTitle.vue';
import MobileMintLabel from '@/components/common/label/MobileMintLabel.vue';
import MobileMintTextfield from '@/components/common/textfield/MobileMintTextfield.vue';
import MobileMintButton from '@/components/common/button/MobileMintButton.vue';
import InfoNotice from '@/components/common/notice/InfoNotice.vue';
import ScheduleNotificationHistoryApi from '@/api/info/ScheduleNotificationHistoryApi';

const router = useRouter();
const formData = ref({
  logId: '',
  userName: '',
  userEmail: '',
  scheduleStartTime: '',
  scheduleEndTime: '',
  notificationType: '',
  notificationValue: '',
  scheduledTime: '',
  sentTime: '',
  channel: '',
  sent: false,
  sentText: '',
  failureReason: '',
});

/**
 * 뒤로가기 네비게이션
 */
function goBack() {
  router.back();
}

/**
 * 모바일 해상도에서만 유지. 데스크탑으로 전환 시 뒤로가기
 */
function handleResizeRedirect() {
  if (window.innerWidth > 650) router.back();
}

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
 * 단건 조회 결과를 화면 표시용으로 매핑
 * @param {object} data 백엔드 응답 데이터
 */
async function populateFromRaw(data) {
  if (!data) return;
  
  formData.value = {
    logId: data.logId ? String(data.logId) : '-',
    userName: data.userName || '-',
    userEmail: data.userEmail || '-',
    scheduleStartTime: data.scheduleStartTime ? formatDateTime(data.scheduleStartTime) : '-',
    scheduleEndTime: data.scheduleEndTime ? formatDateTime(data.scheduleEndTime) : '-',
    notificationType: data.notificationType || '-',
    notificationValue: data.notificationValue || '-',
    scheduledTime: formatDateTime(data.scheduledTime),
    sentTime: formatDateTime(data.sentTime),
    channel: data.channel || '-',
    sent: data.sent || false,
    sentText: data.sent ? '성공' : '실패',
    failureReason: data.failureReason || '',
  };
}

/**
 * logId 기준 단건 조회 실행
 * @param {number|string} logId 로그 ID
 */
async function loadById(logId) {
  try {
    // 목록에서 전달받은 데이터를 사용하거나, 필요시 단건 조회 API 호출
    // 현재는 목록에서 전달받은 데이터를 사용
    const saved = sessionStorage.getItem('scheduleNotificationHistoryDetail');
    if (saved) {
      const parsed = JSON.parse(saved);
      populateFromRaw(parsed);
    }
  } catch (error) {
    console.error('Failed to load notification history detail:', error);
    goBack();
  }
}

onMounted(async () => {
  // 초기 진입 시 상태 초기화
  formData.value = {
    logId: '',
    userName: '',
    userEmail: '',
    scheduleStartTime: '',
    scheduleEndTime: '',
    notificationType: '',
    notificationValue: '',
    scheduledTime: '',
    sentTime: '',
    channel: '',
    sent: false,
    sentText: '',
    failureReason: '',
  };

  handleResizeRedirect();
  window.addEventListener('resize', handleResizeRedirect);
  
  try {
    const saved = sessionStorage.getItem('scheduleNotificationHistoryDetail');
    const parsed = saved ? JSON.parse(saved) : null;
    if (!parsed) return goBack();
    // 데이터 선 로드 후 페이지 렌더링
    await populateFromRaw(parsed);
  } catch {
    return goBack();
  }
});

onBeforeUnmount(() => window.removeEventListener('resize', handleResizeRedirect));
</script>

<style scoped>
:deep(.modal) {
  display: block !important;
}

:deep(.modal-backdrop) {
  display: none !important;
}

:deep(.modal-dialog) {
  margin: 0;
  max-width: 100%;
  width: 100vw;
  height: calc(100vh - 130px);
}

:deep(.modal-content) {
  height: 100%;
  border-radius: 0;
}

/* 모달 본문과 동일한 스타일 복제 */
hr {
  margin: 20px 0 20px 0 !important;
  border: none;
  height: 1px;
  background: #000000;
}

.content-sub-title {
  margin-bottom: 10px !important;
}

.search-wrapper-hr {
  margin: 20px 0 20px 0 !important;
}

.modal-body {
  padding-top: 15px;
  max-height: none !important;
  overflow-y: visible !important;
  background-color: #f9fafb !important;
}

.form-group {
  margin-bottom: 20px;
  background: #ffffff;
  border: 1px solid #e6f2ff;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.06);
}

/* 텍스트 입력/셀렉트 비활성 배경 톤 보정 */
.form-group input[disabled],
.form-group select[disabled] {
  background: #f9fcff !important;
  border: 1px solid #e6f2ff !important;
}

/* 실패 사유 카드 강조 */
.cs-card--white input[disabled] {
  background: #ffffff !important;
  border: 1px dashed #cfe6ff !important;
  color: #0b162f !important;
  font-weight: 700;
}

/* 한 줄 배치 필드 스타일 */
.field-half {
  flex: 1 1 0;
  min-width: 0;
}

/* 라벨 아래 마진 */
.form-group :deep(.mm-label-wrapper) {
  margin-bottom: 5px;
}
</style>

