<template>
  <div>
    <div class="content content-wrapper">
      <MobileDetailTitle :title="canEdit ? $t('info.schedule.edit') : $t('info.schedule.detail')" />

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
            :disabled="!canEdit"
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
              :disabled="!canEdit"
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
                :disabled="!canEdit"
              />
              <input
                id="scheduleColor"
                v-model="form.color"
                type="color"
                class="color-input"
                :disabled="!canEdit"
              />
            </div>
          </div>
        </div>
        
        <!-- 비활성화된 일정 유형 경고 문구 -->
        <div v-if="isDeletedScheduleType" style="margin-top: 4px; margin-left: 0; padding-left: 0;">
          <p style="font-size: 0.7rem; color: #ef4444 !important; margin: 0; font-weight: 500;">
            {{ $t('info.schedule.labels.deletedScheduleTypeWarningPrefix') }} [{{ deletedScheduleTypeLabel }}] {{ $t('info.schedule.labels.deletedScheduleTypeWarning') }}
          </p>
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
              :disabled="!canEdit"
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
              :disabled="!canEdit"
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
                :class="{ disabled: !canEdit }"
                title="시간 초기화"
              ></i>
            </div>
            <MobileMintTextfield
              type="time"
              id="startTime"
              v-model="form.startTime"
              size="full"
              style="width: 100%"
              :disabled="!canEdit"
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
                :class="{ disabled: !canEdit }"
                title="시간 초기화"
              ></i>
            </div>
            <MobileMintTextfield
              type="time"
              id="endTime"
              v-model="form.endTime"
              size="full"
              style="width: 100%"
              :disabled="!canEdit"
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
            :disabled="!canEdit"
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
                  :disabled="!canEdit"
                />
                <span>{{ $t('info.schedule.notificationTiming.none') }}</span>
              </label>
              <label class="timing-radio" :class="{ active: selectedTimingType === NOTIFICATION_TIMING_UNIT.DAY }">
                <input
                  type="radio"
                  :value="NOTIFICATION_TIMING_UNIT.DAY"
                  v-model="selectedTimingType"
                  class="radio-input"
                  :disabled="!canEdit"
                />
                <span>{{ $t('info.schedule.notificationTiming.day') }}</span>
              </label>
              <label class="timing-radio" :class="{ active: selectedTimingType === NOTIFICATION_TIMING_UNIT.HOUR }">
                <input
                  type="radio"
                  :value="NOTIFICATION_TIMING_UNIT.HOUR"
                  v-model="selectedTimingType"
                  class="radio-input"
                  :disabled="!canEdit"
                />
                <span>{{ $t('info.schedule.notificationTiming.hour') }}</span>
              </label>
              <label class="timing-radio" :class="{ active: selectedTimingType === NOTIFICATION_TIMING_UNIT.MINUTE }">
                <input
                  type="radio"
                  :value="NOTIFICATION_TIMING_UNIT.MINUTE"
                  v-model="selectedTimingType"
                  class="radio-input"
                  :disabled="!canEdit"
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
                    :disabled="!canEdit"
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
                      :disabled="!canEdit"
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
                      :disabled="!canEdit"
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
                      :disabled="!canEdit"
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
                      :disabled="!canEdit"
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
                      :disabled="!canEdit"
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
            v-if="canEdit"
            align="right"
            marginRight="5px"
            type="submit"
          >
            {{ $t('info.schedule.buttons.save') }}
          </MobileMintButton>
          <MobileMintButton
            v-if="canDelete && canEdit"
            align="right"
            color="red"
            @click="handleDelete"
          >
            {{ $t('info.schedule.buttons.delete') }}
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
import { validateTimingValue, calculateNotificationTime, validateNotificationTiming } from '@/utils/info/scheduleUtils';
import MobileDetailTitle from '@/components/common/title/MobileDetailTitle.vue';
import MobileMintLabel from '@/components/common/label/MobileMintLabel.vue';
import MobileMintTextfield from '@/components/common/textfield/MobileMintTextfield.vue';
import MobileMintSelect from '@/components/common/select/MobileMintSelect.vue';
import MobileMintButton from '@/components/common/button/MobileMintButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import { ROUTES } from '@/config/menuConfig';
import { useViewStateStore } from '@/store/viewState';

const router = useRouter();
const { t } = useI18n();
const hrmStore = useHrmStore();
const authStore = useAuthStore();
const viewState = useViewStateStore();
const toastStore = useToastStore();

const scheduleId = ref(null);
const canEdit = ref(true);
const canDelete = ref(false);

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

const scheduleTypes = ref([]);

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

/**
 * 일정 유형 옵션
 */
const scheduleTypeOptions = computed(() => getScheduleTypeOptionsSync(scheduleTypes.value));

/**
 * 원본 일정 유형 코드 저장
 */
const originalScheduleType = ref(null);

/**
 * 현재 선택된 일정 유형이 삭제되었는지 확인
 */
const isDeletedScheduleType = computed(() => {
  if (!originalScheduleType.value || scheduleTypes.value.length === 0) return false;
  const currentType = scheduleTypes.value.find(st => st.code === originalScheduleType.value && st.enabled !== false && st.isActive === true);
  // 원본 일정 유형이 옵션에 없으면 삭제된 것으로 간주
  return !currentType;
});

/**
 * 삭제된 일정 유형의 라벨
 */
const deletedScheduleTypeLabel = computed(() => {
  if (!originalScheduleType.value || scheduleTypes.value.length === 0) return '';
  // 원본 일정 데이터에서 일정 유형 라벨 찾기
  const scheduleData = JSON.parse(sessionStorage.getItem('scheduleEdit') || '{}');
  if (scheduleData?.scheduleTypeInfo?.label) {
    return scheduleData.scheduleTypeInfo.label;
  }
  return originalScheduleType.value;
});

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
 * 알림 시점 파싱 (JSON 형식만 지원)
 * 새 형식: {"unit": "DAY", "date": "2025-12-18", "hour": 15, "minute": 30}
 * 기존 형식: {"unit": "DAY", "value": 1} (하위 호환)
 */
function parseTimingString(timing) {
  if (!timing) {
    selectedTimingType.value = 'none';
    selectedTimingValue.value = {
      date: '',
      hour: null,
      minute: null
    };
    return;
  }

  // JSON 형식만 지원
  if (typeof timing !== 'object') {
    console.warn('지원하지 않는 알림 시점 형식:', timing);
    selectedTimingType.value = 'none';
    selectedTimingValue.value = {
      date: '',
      hour: null,
      minute: null
    };
    return;
  }

  const unit = timing.unit || timing.unitCode;
  
  if (!unit) {
    console.warn('알림 시점 형식이 올바르지 않음:', timing);
    selectedTimingType.value = 'none';
    selectedTimingValue.value = {
      date: '',
      hour: null,
      minute: null
    };
    return;
  }

  // 새 형식 파싱
  if (unit === NOTIFICATION_TIMING_UNIT.DAY || unit === 'DAY') {
    if (timing.date && timing.hour !== null && timing.hour !== undefined && timing.minute !== null && timing.minute !== undefined) {
      selectedTimingType.value = NOTIFICATION_TIMING_UNIT.DAY;
      selectedTimingValue.value = {
        date: timing.date,
        hour: timing.hour,
        minute: timing.minute
      };
      return;
    }
    // 하위 호환: 기존 형식 {"unit": "DAY", "value": 1}
    const value = timing.value;
    if (value !== null && value !== undefined) {
      const limits = NOTIFICATION_TIMING_UNIT_LIMITS[NOTIFICATION_TIMING_UNIT.DAY];
      if (value >= limits.min && value <= limits.max) {
        selectedTimingType.value = NOTIFICATION_TIMING_UNIT.DAY;
        // 기본값으로 설정 (일정 시작일 - value일, 00:00)
        const defaultDate = new Date();
        defaultDate.setDate(defaultDate.getDate() + value);
        selectedTimingValue.value = {
          date: defaultDate.toISOString().split('T')[0],
          hour: 0,
          minute: 0
        };
        return;
      }
    }
  } else if (unit === NOTIFICATION_TIMING_UNIT.HOUR || unit === 'HOUR') {
    if (timing.hour !== null && timing.hour !== undefined && timing.minute !== null && timing.minute !== undefined) {
      selectedTimingType.value = NOTIFICATION_TIMING_UNIT.HOUR;
      selectedTimingValue.value = {
        date: '',
        hour: timing.hour,
        minute: timing.minute
      };
      return;
    }
    // 하위 호환: 기존 형식 {"unit": "HOUR", "value": 1}
    const value = timing.value;
    if (value !== null && value !== undefined) {
      const limits = NOTIFICATION_TIMING_UNIT_LIMITS[NOTIFICATION_TIMING_UNIT.HOUR];
      if (value >= limits.min && value <= limits.max) {
        selectedTimingType.value = NOTIFICATION_TIMING_UNIT.HOUR;
        selectedTimingValue.value = {
          date: '',
          hour: value,
          minute: 0
        };
        return;
      }
    }
  } else if (unit === NOTIFICATION_TIMING_UNIT.MINUTE || unit === 'MINUTE') {
    if (timing.minute !== null && timing.minute !== undefined) {
      selectedTimingType.value = NOTIFICATION_TIMING_UNIT.MINUTE;
      selectedTimingValue.value = {
        date: '',
        hour: null,
        minute: timing.minute
      };
      return;
    }
    // 하위 호환: 기존 형식 {"unit": "MINUTE", "value": 30}
    const value = timing.value;
    if (value !== null && value !== undefined) {
      const limits = NOTIFICATION_TIMING_UNIT_LIMITS[NOTIFICATION_TIMING_UNIT.MINUTE];
      if (value >= limits.min && value <= limits.max) {
        selectedTimingType.value = NOTIFICATION_TIMING_UNIT.MINUTE;
        selectedTimingValue.value = {
          date: '',
          hour: null,
          minute: value
        };
        return;
      }
    }
  }

  // 파싱 실패
  console.warn('알 수 없는 알림 시점 단위 또는 범위 초과:', timing);
  selectedTimingType.value = 'none';
  selectedTimingValue.value = {
    date: '',
    hour: null,
    minute: null
  };
}

/**
 * 시작 시간 초기화
 */
function resetStartTime() {
  if (canEdit.value) {
    form.value.startTime = '';
  }
}

/**
 * 종료 시간 초기화
 */
function resetEndTime() {
  if (canEdit.value) {
    form.value.endTime = '';
  }
}

/**
 * 일정 데이터 로드
 */
async function loadSchedule() {
  try {
    const s = sessionStorage.getItem('scheduleEdit');
    const parsed = s ? JSON.parse(s) : null;
    const id = parsed?.scheduleId || parsed?.id;
    
    if (!id) {
      toast.error('일정 정보를 찾을 수 없습니다.');
      goBack();
      return;
    }
    
    scheduleId.value = id;
    canEdit.value = parsed?.canEdit ?? true;
    canDelete.value = parsed?.canDelete ?? false;
    
    const { data } = await ScheduleApi.getSchedule(id);
    
    let startTimeStr = '';
    let endTimeStr = '';
    
    if (data.startTime) {
      const startTime = new Date(data.startTime);
      startTimeStr = startTime.toTimeString().slice(0, 5);
    }
    
    if (data.endTime) {
      const endTime = new Date(data.endTime);
      endTimeStr = endTime.toTimeString().slice(0, 5);
    }
    
    const scheduleType = data.scheduleTypeInfo?.code || '';
    
    // 원본 일정 유형 저장
    originalScheduleType.value = scheduleType;
    
    // 일정 유형이 옵션에 없으면 첫 번째 항목 선택
    const options = getScheduleTypeOptionsSync(scheduleTypes.value);
    const firstType = options.length > 0 ? options[0].value : null;
    const finalScheduleType = scheduleTypes.value.find(st => st.code === scheduleType && st.enabled !== false && st.isActive === true) ? scheduleType : (firstType || '');
    const finalColor = finalScheduleType ? getScheduleTypeColorSync(finalScheduleType, scheduleTypes.value) : (data.color || '#9E9E9E');
    
    form.value = {
      title: data.title || '',
      scheduleType: finalScheduleType,
      startDate: data.startDate || '',
      endDate: data.endDate || '',
      startTime: startTimeStr,
      endTime: endTimeStr,
      assigneeId: data.assigneeId || hrmStore.myProfile?.userId || null,
      assigneeName: data.assigneeName || hrmStore.myProfile?.name || '',
      departmentId: data.departmentId || hrmStore.myProfile?.departmentId || null,
      departmentName: data.departmentName || hrmStore.myProfile?.departmentName || '',
      color: data.color || getScheduleTypeColorSync(scheduleType, scheduleTypes.value) || '#9E9E9E',
      description: data.description || '',
      notificationTimings: data.notificationTimings || null,
    };
    
    // 알림 시점 파싱 (JSON 형식만 지원)
    if (data.notificationTimings) {
      try {
        const timings = JSON.parse(data.notificationTimings);
        if (Array.isArray(timings) && timings.length > 0) {
          const timing = timings[0]; // 첫 번째 값만 사용
          parseTimingString(timing);
        } else {
          selectedTimingType.value = 'none';
          selectedTimingValue.value = {
            date: '',
            hour: null,
            minute: null
          };
        }
      } catch (e) {
        console.warn('알림 시점 파싱 실패:', e);
        selectedTimingType.value = 'none';
        selectedTimingValue.value = {
          date: '',
          hour: null,
          minute: null
        };
      }
    } else {
      selectedTimingType.value = 'none';
      selectedTimingValue.value = {
        date: '',
        hour: null,
        minute: null
      };
    }
  } catch (error) {
    console.error('일정 로드 실패:', error);
    toast.error('일정을 불러오는데 실패했습니다.');
    goBack();
  }
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
  if (!canEdit.value) {
    toast.error('수정 권한이 없습니다.');
    return;
  }
  
  if (!validateForm()) return;
  
  // 알림 시점 설정 검증
  const validationError = validateNotificationTiming(
    selectedTimingType.value,
    selectedTimingValue.value,
    form.value.startDate,
    form.value.startTime
  );
  if (validationError) {
    toast.error(validationError);
    return;
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
    
    await ScheduleApi.updateSchedule(scheduleId.value, scheduleData);
    toastStore.setNextPageMessage(t('info.schedule.success.update'), 'success');
    goBack();
  } catch (error) {
    console.error('일정 수정 실패:', error);
    toast.error('일정 수정에 실패했습니다.');
  }
}

/**
 * 삭제 처리
 */
async function handleDelete() {
  if (!canDelete.value || !canEdit.value) {
    toast.error('삭제 권한이 없습니다.');
    return;
  }
  
  if (!confirm(t('info.schedule.confirm.deleteMessage'))) {
    return;
  }
  
  try {
    await ScheduleApi.deleteSchedule(scheduleId.value);
    toastStore.setNextPageMessage(t('info.schedule.success.delete'), 'success');
    goBack();
  } catch (error) {
    console.error('일정 삭제 실패:', error);
    toast.error('일정 삭제에 실패했습니다.');
  }
}

/**
 * 뒤로가기
 */
function goBack() {
  // 이전 경로가 있으면 그곳으로, 없으면 캘린더로
  const returnPath = sessionStorage.getItem('scheduleEditReturnPath');
  if (returnPath) {
    sessionStorage.removeItem('scheduleEditReturnPath');
    router.push(returnPath);
  } else {
    router.push(ROUTES.INFO.SCHEDULE);
  }
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
  await loadSchedule();
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

.color-input:disabled {
  cursor: not-allowed;
  opacity: 0.6;
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

.form-control:disabled {
  border: 1px dashed #cfe6ff;
  color: #0b162f;
  font-weight: 600;
  opacity: 0.6;
  cursor: not-allowed;
}

textarea.form-control {
  min-height: 80px;
  height: auto;
}

.notification-timings-container {
  padding: 12px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.timing-radio-group {
  display: flex;
  flex-direction: row;
  gap: 6px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.timing-radio {
  display: flex;
  align-items: center;
  padding: 6px 10px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  font-size: 0.75rem;
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
  margin-right: 6px;
  cursor: pointer;
  width: 14px;
  height: 14px;
}

.timing-input-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.timing-input-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
}

.timing-input-label {
  font-size: 0.7rem;
  font-weight: 500;
  color: #495057;
  margin-bottom: 2px;
}

.timing-time-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.timing-input {
  padding: 8px 10px;
  border: 1px solid #e6f2ff;
  border-radius: 10px;
  font-size: 0.875rem;
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
  width: 70px;
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
  font-size: 0.9rem;
  color: #495057;
  font-weight: 500;
  margin: 0 2px;
}

.timing-suffix {
  font-size: 0.75rem;
  color: #6c757d;
  white-space: nowrap;
  margin-left: 6px;
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

.time-reset-icon:hover:not(.disabled) {
  color: #0d6efd;
}

.time-reset-icon.disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
</style>
