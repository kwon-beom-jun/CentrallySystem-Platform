<template>
  <div>
    <div class="content content-wrapper">
      <MobileDetailTitle :title="$t('info.schedule.create')" />

      <form class="form-pane modal-body" @submit.prevent="handleSave" @keydown.enter.prevent>
        <!-- 제목 -->
        <div class="form-group cs-card--blue">
          <MobileMintLabel
            :text="$t('info.schedule.labels.title')"
            forId="scheduleTitle"
            size="small"
            :required="true"
            marginBottom="5px"
          />
          <MobileMintTextfield
            type="text"
            id="scheduleTitle"
            v-model="form.title"
            :placeholder="$t('info.schedule.placeholders.title')"
            size="full"
          />
        </div>

        <!-- 일정 유형 | 색상 -->
        <div class="form-row align-items-center" style="margin-top: 20px;">
          <div class="col">
            <MobileMintLabel
              :text="$t('info.schedule.labels.scheduleType')"
              forId="scheduleType"
              size="small"
              :required="true"
              marginBottom="5px"
            />
            <MobileMintSelect
              id="scheduleType"
              v-model="form.scheduleType"
              :options="scheduleTypeOptions"
              size="full"
              style="width: 100%"
            />
          </div>
          <div class="col">
            <MobileMintLabel
              :text="colorLabelText"
              forId="scheduleColor"
              size="small"
              marginBottom="5px"
            />
            <div class="color-picker-wrapper">
              <MobileMintTextfield
                v-model="form.color"
                type="text"
                size="full"
                placeholder="#000000"
                style="flex: 1;"
              />
              <input
                id="scheduleColor"
                v-model="form.color"
                type="color"
                class="color-input"
              />
            </div>
          </div>
        </div>

        <hr class="search-wrapper-hr-2" />

        <!-- 시작일 | 종료일 -->
        <div class="form-row align-items-center">
          <div class="col">
            <MobileMintLabel
              :text="$t('info.schedule.labels.startDate')"
              forId="startDate"
              size="small"
              :required="true"
              marginBottom="5px"
            />
            <MobileMintTextfield
              type="date"
              id="startDate"
              v-model="form.startDate"
              size="full"
              style="width: 100%"
              :required="true"
            />
          </div>
          <div class="col">
            <MobileMintLabel
              :text="$t('info.schedule.labels.endDate')"
              forId="endDate"
              size="small"
              :required="true"
              marginBottom="5px"
            />
            <MobileMintTextfield
              type="date"
              id="endDate"
              v-model="form.endDate"
              size="full"
              style="width: 100%"
              :required="true"
            />
          </div>
        </div>

        <!-- 시작 시간 | 종료 시간 -->
        <div class="form-row align-items-center">
          <div class="col">
            <div class="label-with-icon">
              <MobileMintLabel
                :text="$t('info.schedule.labels.startTime')"
                forId="startTime"
                size="small"
                marginBottom="5px"
              />
              <i 
                class="ri-refresh-line time-reset-icon" 
                @click="resetStartTime"
                title="시간 초기화"
              ></i>
            </div>
            <MobileMintTextfield
              type="time"
              id="startTime"
              v-model="form.startTime"
              size="full"
              style="width: 100%"
            />
          </div>
          <div class="col">
            <div class="label-with-icon">
              <MobileMintLabel
                :text="$t('info.schedule.labels.endTime')"
                forId="endTime"
                size="small"
                marginBottom="5px"
              />
              <i 
                class="ri-refresh-line time-reset-icon" 
                @click="resetEndTime"
                title="시간 초기화"
              ></i>
            </div>
            <MobileMintTextfield
              type="time"
              id="endTime"
              v-model="form.endTime"
              size="full"
              style="width: 100%"
            />
          </div>
        </div>

        <hr class="search-wrapper-hr-1" />

        <!-- 담당자 (읽기 전용) -->
        <div class="form-group cs-card--blue">
          <MobileMintLabel
            :text="$t('info.schedule.labels.assignee')"
            forId="assignee"
            size="small"
            marginBottom="5px"
          />
          <MobileMintTextfield
            id="assignee"
            type="text"
            :modelValue="displayAssigneeName"
            size="full"
            :disabled="true"
          />
        </div>

        <!-- 설명 -->
        <div class="form-group reason cs-card--blue" style="margin-top: 20px;">
          <MobileMintLabel
            :text="$t('info.schedule.labels.description')"
            forId="description"
            size="small"
            marginBottom="5px"
          />
          <textarea
            id="description"
            v-model="form.description"
            class="form-control"
            rows="4"
            :placeholder="$t('info.schedule.placeholders.description')"
          ></textarea>
        </div>

        <hr class="search-wrapper-hr-1" />

        <!-- 알림 시점 설정 -->
        <div class="form-group cs-card--blue" style="margin-top: 20px;">
          <MobileMintLabel
            :text="`${$t('info.schedule.notificationTiming.title')} ${$t('info.schedule.notificationTiming.mailDeliveryInfoLabel')}`"
            forId="notificationTimings"
            size="small"
            marginBottom="10px"
          />
          <div class="notification-timings-container">
            <!-- 라디오 버튼 그룹 (첫 번째 줄) -->
            <div class="timing-radio-group">
              <label class="timing-radio" :class="{ active: selectedTimingType === 'none' }">
                <input
                  type="radio"
                  value="none"
                  v-model="selectedTimingType"
                  class="radio-input"
                />
                <span>{{ $t('info.schedule.notificationTiming.none') }}</span>
              </label>
              <label class="timing-radio" :class="{ active: selectedTimingType === NOTIFICATION_TIMING_UNIT.DAY }">
                <input
                  type="radio"
                  :value="NOTIFICATION_TIMING_UNIT.DAY"
                  v-model="selectedTimingType"
                  class="radio-input"
                />
                <span>{{ $t('info.schedule.notificationTiming.day') }}</span>
              </label>
              <label class="timing-radio" :class="{ active: selectedTimingType === NOTIFICATION_TIMING_UNIT.HOUR }">
                <input
                  type="radio"
                  :value="NOTIFICATION_TIMING_UNIT.HOUR"
                  v-model="selectedTimingType"
                  class="radio-input"
                />
                <span>{{ $t('info.schedule.notificationTiming.hour') }}</span>
              </label>
              <label class="timing-radio" :class="{ active: selectedTimingType === NOTIFICATION_TIMING_UNIT.MINUTE }">
                <input
                  type="radio"
                  :value="NOTIFICATION_TIMING_UNIT.MINUTE"
                  v-model="selectedTimingType"
                  class="radio-input"
                />
                <span>{{ $t('info.schedule.notificationTiming.minute') }}</span>
              </label>
            </div>

            <!-- 알림 시간 표시 -->
            <div v-if="selectedTimingType !== 'none'" class="notification-time-preview" :class="{ 'error-message': calculatedNotificationTime && !calculatedNotificationTime.includes('년') }">
              {{ calculatedNotificationTime }}
            </div>

            <!-- 입력 필드 (두 번째 줄) -->
            <div v-if="selectedTimingType !== 'none'" class="timing-input-section">
              <!-- 일: 일자, 시, 분 -->
              <template v-if="selectedTimingType === NOTIFICATION_TIMING_UNIT.DAY">
                <div class="timing-input-item">
                  <label class="timing-input-label">{{ $t('info.schedule.notificationTiming.date') }}</label>
                  <input
                    type="date"
                    v-model="selectedTimingValue.date"
                    class="timing-input timing-date-input"
                  />
                </div>
                <div class="timing-input-item">
                  <label class="timing-input-label">{{ $t('info.schedule.notificationTiming.time') }}</label>
                  <div class="timing-time-group">
                    <input
                      type="number"
                      v-model.number="selectedTimingValue.hour"
                      @input="validateTimingValueInput"
                      placeholder="00"
                      :min="0"
                      :max="23"
                      class="timing-input timing-hour-input"
                    />
                    <span class="timing-separator">:</span>
                    <input
                      type="number"
                      v-model.number="selectedTimingValue.minute"
                      @input="validateTimingValueInput"
                      placeholder="00"
                      :min="0"
                      :max="59"
                      class="timing-input timing-minute-input"
                    />
                  </div>
                </div>
              </template>
              
              <!-- 시간: 시, 분 -->
              <template v-else-if="selectedTimingType === NOTIFICATION_TIMING_UNIT.HOUR">
                <div class="timing-input-item">
                  <label class="timing-input-label">{{ $t('info.schedule.notificationTiming.hourBefore') }}</label>
                  <div class="timing-time-group">
                    <input
                      type="number"
                      v-model.number="selectedTimingValue.hour"
                      @input="validateTimingValueInput"
                      placeholder="00"
                      :min="0"
                      :max="23"
                      class="timing-input timing-hour-input"
                    />
                    <span class="timing-separator">:</span>
                    <input
                      type="number"
                      v-model.number="selectedTimingValue.minute"
                      @input="validateTimingValueInput"
                      placeholder="00"
                      :min="0"
                      :max="59"
                      class="timing-input timing-minute-input"
                    />
                  </div>
                </div>
              </template>
              
              <!-- 분: 분만 -->
              <template v-else-if="selectedTimingType === NOTIFICATION_TIMING_UNIT.MINUTE">
                <div class="timing-input-item">
                  <label class="timing-input-label">{{ $t('info.schedule.notificationTiming.minuteBefore') }}</label>
                  <div class="timing-time-group">
                    <input
                      type="number"
                      v-model.number="selectedTimingValue.minute"
                      @input="validateTimingValueInput"
                      placeholder="00"
                      :min="3"
                      :max="59"
                      class="timing-input timing-minute-input"
                    />
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- 버튼 -->
        <DefaultFormRow align="right" marginTop="5px">
          <MobileMintButton
            align="right"
            color="gray"
            marginRight="5px"
            @click="goBack"
          >
            {{ $t('info.schedule.buttons.cancel') }}
          </MobileMintButton>
          <MobileMintButton
            type="submit"
            align="right"
          >
            {{ $t('info.schedule.buttons.save') }}
          </MobileMintButton>
        </DefaultFormRow>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useHrmStore } from '@/store/hrm';
import { useAuthStore } from '@/store/auth';
import { useToastStore } from '@/store/toast';
import ScheduleApi from '@/api/info/ScheduleApi';
import { 
  fetchScheduleTypes, 
  getScheduleTypeOptionsSync, 
  getScheduleTypeColorSync,
  NOTIFICATION_TIMING_UNIT,
  NOTIFICATION_TIMING_UNIT_LIMITS
} from '@/constants/infoConstants';
import { toast } from 'vue3-toastify';
import { getCurrentDateKST } from '@/utils/dateUtils';
import { validateTimingValue, calculateNotificationTime, validateNotificationTiming } from '@/utils/info/scheduleUtils';
import MobileDetailTitle from '@/components/common/title/MobileDetailTitle.vue';
import MobileMintLabel from '@/components/common/label/MobileMintLabel.vue';
import MobileMintTextfield from '@/components/common/textfield/MobileMintTextfield.vue';
import MobileMintSelect from '@/components/common/select/MobileMintSelect.vue';
import MobileMintButton from '@/components/common/button/MobileMintButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import { ROUTES } from '@/config/menuConfig';

const router = useRouter();
const { t } = useI18n();
const hrmStore = useHrmStore();
const authStore = useAuthStore();
const toastStore = useToastStore();

const form = ref({
  title: '',
  scheduleType: '',
  startDate: '',
  endDate: '',
  startTime: '',
  endTime: '',
  assigneeId: null,
  assigneeName: '',
  departmentId: null,
  departmentName: '',
  color: '',
  description: '',
  notificationTimings: null,
});

/**
 * 선택된 알림 시점 타입: 'none' | 'day' | 'hour' | 'minute'
 */
const selectedTimingType = ref('none');

/**
 * 선택된 알림 시점 값
 * - 일: { date: '2025-12-18', hour: 15, minute: 30 }
 * - 시간: { hour: 15, minute: 30 }
 * - 분: { minute: 30 }
 */
const selectedTimingValue = ref({
  date: '',
  hour: null,
  minute: null
});

const scheduleTypes = ref([]);

/**
 * 일정 유형 옵션
 */
const scheduleTypeOptions = computed(() => getScheduleTypeOptionsSync(scheduleTypes.value));

/**
 * 색상 라벨 텍스트 (투명도 정보 포함)
 */
const colorLabelText = computed(() => {
  const baseText = t('info.schedule.labels.color');
  const opacityText = t('info.schedule.labels.opacity', { opacity: '0.7' });
  return `${baseText} (${opacityText})`;
});

/**
 * 표시할 담당자 이름
 */
const displayAssigneeName = computed(() => {
  if (form.value.assigneeName && form.value.assigneeName.trim() !== '') {
    return form.value.assigneeName;
  }
  if (hrmStore.myProfile?.name) {
    return hrmStore.myProfile.name;
  }
  return '';
});

/**
 * 알림 시점 단위별 placeholder 반환
 */
function getTimingPlaceholder() {
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.DAY) return '1';
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.HOUR) return '1';
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.MINUTE) return '1';
  return '';
}

/**
 * 알림 시점 단위별 최소값 반환
 */
function getTimingMin() {
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.DAY) {
    return NOTIFICATION_TIMING_UNIT_LIMITS[NOTIFICATION_TIMING_UNIT.DAY].min;
  }
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.HOUR) {
    return NOTIFICATION_TIMING_UNIT_LIMITS[NOTIFICATION_TIMING_UNIT.HOUR].min;
  }
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.MINUTE) {
    return NOTIFICATION_TIMING_UNIT_LIMITS[NOTIFICATION_TIMING_UNIT.MINUTE].min;
  }
  return 1;
}

/**
 * 알림 시점 단위별 최대값 반환
 */
function getTimingMax() {
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.DAY) {
    return NOTIFICATION_TIMING_UNIT_LIMITS[NOTIFICATION_TIMING_UNIT.DAY].max;
  }
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.HOUR) {
    return NOTIFICATION_TIMING_UNIT_LIMITS[NOTIFICATION_TIMING_UNIT.HOUR].max;
  }
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.MINUTE) {
    return NOTIFICATION_TIMING_UNIT_LIMITS[NOTIFICATION_TIMING_UNIT.MINUTE].max;
  }
  return 60;
}

/**
 * 알림 시점 단위별 step 반환
 */
function getTimingStep() {
  return 1;
}

/**
 * 알림 시점 단위별 suffix 반환
 */
function getTimingSuffix() {
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.DAY) {
    return t('info.schedule.notificationTiming.dayBefore');
  }
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.HOUR) {
    return t('info.schedule.notificationTiming.hourBefore');
  }
  if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.MINUTE) {
    return t('info.schedule.notificationTiming.minuteBefore');
  }
  return '';
}

/**
 * 알림 시점 값 검증
 */
function validateTimingValueInput() {
  selectedTimingValue.value = validateTimingValue(
    selectedTimingType.value,
    selectedTimingValue.value
  );
}

/**
 * 알림 시점 타입 변경 시 값 초기화
 */
watch(selectedTimingType, (newType) => {
  selectedTimingValue.value = {
    date: '',
    hour: null,
    minute: null
  };
});

/**
 * 계산된 알림 시간 (실시간 표시용)
 */
const calculatedNotificationTime = computed(() => {
  return calculateNotificationTime(
    selectedTimingType.value,
    selectedTimingValue.value,
    form.value.startDate,
    form.value.startTime
  );
});

/**
 * 시작 시간 초기화
 */
function resetStartTime() {
  form.value.startTime = '';
}

/**
 * 종료 시간 초기화
 */
function resetEndTime() {
  form.value.endTime = '';
}

/**
 * 폼 초기화
 */
function initForm() {
  // 첫 번째 일정 유형을 기본값으로 사용
  const options = getScheduleTypeOptionsSync(scheduleTypes.value);
  const firstType = options.length > 0 ? options[0].value : null;
  const defaultColor = firstType ? getScheduleTypeColorSync(firstType, scheduleTypes.value) : '#9E9E9E';
  const currentDate = getCurrentDateKST();
    form.value = {
      title: '',
      scheduleType: firstType || '',
      startDate: currentDate,
      endDate: currentDate,
      startTime: '',
      endTime: '',
      assigneeId: hrmStore.myProfile?.userId || null,
      assigneeName: hrmStore.myProfile?.name || '',
      departmentId: hrmStore.myProfile?.departmentId || null,
      departmentName: hrmStore.myProfile?.departmentName || '',
      color: defaultColor || '#9E9E9E',
      description: '',
      notificationTimings: null,
    };
    selectedTimingType.value = 'none';
    selectedTimingValue.value = {
      date: '',
      hour: null,
      minute: null
    };
}

/**
 * 폼 검증
 */
function validateForm() {
  if (!form.value.title || form.value.title.trim() === '') {
    toast.error(t('info.schedule.validation.titleRequired'));
    return false;
  }
  if (!form.value.startDate) {
    toast.error(t('info.schedule.validation.startDateRequired'));
    return false;
  }
  if (!form.value.endDate) {
    toast.error(t('info.schedule.validation.endDateRequired'));
    return false;
  }
  if (new Date(form.value.endDate) < new Date(form.value.startDate)) {
    toast.error(t('info.schedule.validation.endDateBeforeStart'));
    return false;
  }
  return true;
}

/**
 * 저장 처리
 */
async function handleSave() {
  if (!validateForm()) return;
  
  // 알림 시점 설정 검증
  if (selectedTimingType.value !== 'none') {
    const notificationTime = calculatedNotificationTime.value;
    if (!notificationTime || !notificationTime.includes('년')) {
      // 에러 메시지가 있는 경우
      toast.error(notificationTime || '알림 시점을 올바르게 설정해 주세요');
      return;
    }
  }
  
  try {
    const assigneeId = hrmStore.myProfile?.userId;
    const assigneeName = hrmStore.myProfile?.name || '';
    const assigneeEmail = hrmStore.myProfile?.email || '';
    const departmentId = hrmStore.myProfile?.departmentId;
    const departmentName = hrmStore.myProfile?.departmentName || '';
    
    // 알림 시점을 JSON 형식으로 변환
    // 새 형식: [{"unit": "DAY", "date": "2025-12-18", "hour": 15, "minute": 30}]
    let notificationTimingsJson = null;
    if (selectedTimingType.value !== 'none') {
      const timing = selectedTimingValue.value;
      let timingObject = { unit: selectedTimingType.value };
      
      if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.DAY) {
        // 일: 일자, 시, 분 모두 필요
        if (timing.date && timing.hour !== null && timing.minute !== null) {
          timingObject.date = timing.date;
          timingObject.hour = timing.hour;
          timingObject.minute = timing.minute;
          notificationTimingsJson = JSON.stringify([timingObject]);
        }
      } else if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.HOUR) {
        // 시간: 시, 분 필요
        if (timing.hour !== null && timing.minute !== null) {
          timingObject.hour = timing.hour;
          timingObject.minute = timing.minute;
          notificationTimingsJson = JSON.stringify([timingObject]);
        }
      } else if (selectedTimingType.value === NOTIFICATION_TIMING_UNIT.MINUTE) {
        // 분: 분만 필요
        if (timing.minute !== null) {
          timingObject.minute = timing.minute;
          notificationTimingsJson = JSON.stringify([timingObject]);
        }
      }
    }
    
    const scheduleData = {
      title: form.value.title,
      scheduleType: form.value.scheduleType,
      startDate: form.value.startDate,
      endDate: form.value.endDate || form.value.startDate,
      startTime: null,
      endTime: null,
      assigneeId: assigneeId,
      assigneeName: assigneeName,
      assigneeEmail: assigneeEmail,
      departmentId: departmentId,
      departmentName: departmentName,
      description: form.value.description || '',
      color: form.value.color || getScheduleTypeColorSync(form.value.scheduleType, scheduleTypes.value) || '#9E9E9E',
      notificationTimings: notificationTimingsJson,
    };
    
    if (form.value.startTime) {
      const [hours, minutes] = form.value.startTime.split(':');
      scheduleData.startTime = `${form.value.startDate}T${hours}:${minutes}:00`;
    }
    
    if (form.value.endTime) {
      const [hours, minutes] = form.value.endTime.split(':');
      scheduleData.endTime = `${scheduleData.endDate}T${hours}:${minutes}:00`;
    }
    
    await ScheduleApi.createSchedule(scheduleData);
    toastStore.setNextPageMessage(t('info.schedule.success.create'), 'success');
    goBack();
  } catch (error) {
    console.error('일정 저장 실패:', error);
    toast.error('일정 저장에 실패했습니다.');
  }
}

/**
 * 뒤로가기
 */
function goBack() {
  router.push(ROUTES.INFO.SCHEDULE);
}

/**
 * 화면 폭이 650px 초과 시 데스크톱 페이지로 리다이렉트
 */
function handleResizeRedirect() {
  if (window.innerWidth > 650) {
    router.replace(ROUTES.INFO.SCHEDULE);
  }
}

/**
 * 일정 유형 변경 시 색상 자동 업데이트
 */
watch(() => form.value.scheduleType, (newType, oldType) => {
  if (newType && newType !== oldType) {
    const newColor = getScheduleTypeColorSync(newType, scheduleTypes.value) || '#9E9E9E';
    form.value.color = newColor;
  }
});

/**
 * 색상이 비어있을 때 기본값 설정
 */
watch(() => form.value.color, (newColor) => {
  if (!newColor || newColor.trim() === '') {
    form.value.color = getScheduleTypeColorSync(form.value.scheduleType, scheduleTypes.value) || '#9E9E9E';
  }
}, { immediate: true });

onMounted(async () => {
  handleResizeRedirect();
  window.addEventListener('resize', handleResizeRedirect);
  // 일정 유형 데이터 로드
  const types = await fetchScheduleTypes();
  scheduleTypes.value = types;
  initForm();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResizeRedirect);
});
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

.modal-body {
  padding-top: 15px;
  max-height: none !important;
  overflow-y: visible !important;
  background-color: #f9fafb !important;
}

hr {
  margin: 20px 0 20px 0 !important;
  border: none;
  height: 1px;
  background: #000000;
}

.search-wrapper-hr-1 {
  margin: 20px 0 20px 0 !important;
}

.search-wrapper-hr-2 {
  margin: 20px 0 20px 0 !important;
}

.form-row {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.form-row .col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: 100%;
}

.color-picker-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%;
}

.color-input {
  width: 50px;
  height: 38px;
  border: 1px solid #e6f2ff;
  border-radius: 10px;
  cursor: pointer;
  padding: 2px;
  box-sizing: border-box;
}

.color-input:hover {
  border-color: #0d6efd;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e6f2ff;
  border-radius: 10px;
  font-size: 0.875rem;
  font-family: inherit;
  box-sizing: border-box;
  resize: vertical;
}

.form-control:focus {
  outline: none;
  border-color: #0d6efd;
  box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.25);
}

textarea.form-control {
  min-height: 80px;
  height: auto;
}

.notification-timings-container {
  padding: 10px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.timing-radio-group {
  display: flex;
  flex-direction: row;
  gap: 4px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.timing-radio {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-size: 0.7rem;
  color: #495057;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  font-weight: 400;
}

.timing-radio:hover {
  border-color: #0d6efd;
  background: #f0f7ff;
}

.timing-radio.active {
  background: #e7f3ff;
  border-color: #0d6efd;
  color: #0d6efd;
  font-weight: 500;
}

.timing-radio .radio-input {
  margin-right: 4px;
  cursor: pointer;
  width: 12px;
  height: 12px;
}

.timing-input-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.timing-input-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
  width: 100%;
}

.timing-input-label {
  font-size: 0.7rem;
  font-weight: 800;
  color: #334155;
  margin-bottom: 2px;
}

.timing-time-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.timing-input {
  padding: 6px 8px;
  border: 1px solid #e6f2ff;
  border-radius: 10px;
  font-size: 0.75rem;
  background: white;
  color: #212529;
  transition: all 0.2s ease;
  width: 100%;
  box-sizing: border-box;
}

.timing-date-input {
  width: 100%;
}

.timing-hour-input,
.timing-minute-input {
  width: 60px;
  text-align: center;
  font-weight: 400;
}

.timing-input:focus {
  outline: none;
  border-color: #0d6efd;
  box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.25);
}

.timing-input:disabled {
  background: #f8f9fa;
  cursor: not-allowed;
  opacity: 0.6;
}

.timing-input::placeholder {
  color: #adb5bd;
  font-weight: 400;
}

.timing-separator {
  font-size: 0.8rem;
  color: #495057;
  font-weight: 500;
  margin: 0 2px;
}

.timing-suffix {
  font-size: 0.7rem;
  color: #6c757d;
  white-space: nowrap;
  margin-left: 4px;
  font-weight: 400;
}

.notification-time-preview {
  margin-top: 8px;
  margin-bottom: 8px;
  padding-top: 8px;
  border-top: 1px solid #e9ecef;
  font-size: 0.6rem;
  color: #495057;
  font-weight: 500;
  text-align: center;
}

.notification-time-preview.error-message {
  color: #dc3545 !important;
}

.notification-info-text {
  margin-top: 4px;
  font-size: 0.55rem;
  color: #6c757d;
  text-align: center;
  font-style: italic;
}

.label-with-icon {
  display: flex;
  align-items: center;
  gap: 0px;
}

.time-reset-icon {
  font-size: 0.875rem;
  color: #6c757d;
  cursor: pointer;
  transition: color 0.2s ease;
  flex-shrink: 0;
  font-weight: 800;
  margin-bottom: 5px;
}

.time-reset-icon:hover {
  color: #0d6efd;
}
</style>
