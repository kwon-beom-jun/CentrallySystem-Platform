<template>
  <!-- Bootstrap 모달 -->
  <b-modal
    ref="scheduleModal"
    style="--bs-modal-width:600px"
    v-model="innerVisible"
    :title="isCreate ? $t('info.schedule.create') : $t('info.schedule.edit')"
    :keyboard="false"
    :backdrop="'static'"
    centered
    fade
    hide-footer
    @hide="onHideModal"
  >
    <!-- 모달 본문 -->
    <form>
      <div class="layer">
        <DefaultLabel :text="$t('info.schedule.labels.title')" forId="scheduleTitle" size="small" :required="true" />
        <DefaultTextfield
          type="text"
          id="scheduleTitle"
          v-model="form.title"
          size="full"
          :placeholder="$t('info.schedule.placeholders.title')"
          :disabled="!isCreate && !canEdit"
        />
      </div>

      <DefaultFormRow marginBottom="20px" customClass="two-col" gap="12px" :style="{ display: 'grid', gridTemplateColumns: '1fr 1fr', alignItems: 'flex-start' }">
        <div class="col-wrapper">
          <DefaultLabel :text="$t('info.schedule.labels.scheduleType')" forId="scheduleType" size="small" alignment="left" :required="true" />
          <DefaultSelect
            id="scheduleType"
            v-model="form.scheduleType"
            :options="scheduleTypeOptions"
            size="full"
            style="width: 100%;"
            :disabled="!isCreate && !canEdit"
          />
          <p v-if="!isCreate && isDeletedScheduleType" style="font-size: 0.7rem; color: #ef4444 !important; margin-top: 4px; font-weight: 500;">
            {{ $t('info.schedule.labels.deletedScheduleTypeWarningPrefix') }} [{{ deletedScheduleTypeLabel }}] {{ $t('info.schedule.labels.deletedScheduleTypeWarning') }}
          </p>
        </div>
        <div class="col-wrapper">
          <DefaultLabel :text="colorLabelText" forId="scheduleColor" size="small" alignment="left" />
          <div class="color-picker-wrapper">
            <DefaultTextfield
              v-model="form.color"
              type="text"
              size="full"
              placeholder="#000000"
              style="flex: 1;"
              :disabled="!isCreate && !canEdit"
            />
            <input
              id="scheduleColor"
              v-model="form.color"
              type="color"
              class="color-input"
              :disabled="!isCreate && !canEdit"
            />
          </div>
        </div>
      </DefaultFormRow>

      <DefaultFormRow marginBottom="20px" customClass="two-col" gap="12px" :style="{ display: 'grid', gridTemplateColumns: '1fr 1fr', alignItems: 'flex-start' }">
        <div class="col-wrapper">
          <DefaultLabel :text="$t('info.schedule.labels.startDate')" forId="startDate" size="small" alignment="left" :required="true" />
          <input
            id="startDate"
            v-model="form.startDate"
            type="date"
            class="form-control"
            :disabled="!isCreate && !canEdit"
          />
        </div>
        <div class="col-wrapper">
          <DefaultLabel :text="$t('info.schedule.labels.endDate')" forId="endDate" size="small" alignment="left" :required="true" />
          <input
            id="endDate"
            v-model="form.endDate"
            type="date"
            class="form-control"
            :disabled="!isCreate && !canEdit"
          />
        </div>
      </DefaultFormRow>

      <DefaultFormRow marginBottom="20px" customClass="two-col" gap="12px" :style="{ display: 'grid', gridTemplateColumns: '1fr 1fr', alignItems: 'flex-start' }">
        <div class="col-wrapper">
          <div class="label-with-icon">
            <DefaultLabel :text="$t('info.schedule.labels.startTime')" forId="startTime" size="small" alignment="left" />
            <i 
              class="ri-refresh-line time-reset-icon" 
              @click="resetStartTime"
              :class="{ disabled: !isCreate && !canEdit }"
              title="시간 초기화"
            ></i>
          </div>
          <input
            id="startTime"
            v-model="form.startTime"
            type="time"
            class="form-control"
            :disabled="!isCreate && !canEdit"
          />
        </div>
        <div class="col-wrapper">
          <div class="label-with-icon">
            <DefaultLabel :text="$t('info.schedule.labels.endTime')" forId="endTime" size="small" alignment="left" />
            <i 
              class="ri-refresh-line time-reset-icon" 
              @click="resetEndTime"
              :class="{ disabled: !isCreate && !canEdit }"
              title="시간 초기화"
            ></i>
          </div>
          <input
            id="endTime"
            v-model="form.endTime"
            type="time"
            class="form-control"
            :disabled="!isCreate && !canEdit"
          />
        </div>
      </DefaultFormRow>

      <div class="layer">
        <DefaultLabel :text="$t('info.schedule.labels.assignee')" forId="assignee" size="small" alignment="left" />
        <input
          id="assignee"
          type="text"
          :value="displayAssigneeName"
          disabled
          class="form-control"
          readonly
        />
      </div>

      <div class="layer">
        <DefaultLabel :text="$t('info.schedule.labels.description')" forId="description" size="small" />
        <textarea
          id="description"
          v-model="form.description"
          class="form-control"
          rows="4"
          :placeholder="$t('info.schedule.placeholders.description')"
          :disabled="!isCreate && !canEdit"
        ></textarea>
      </div>

      <!-- 알림 시점 설정 -->
      <div class="layer notification-timings-section">
        <DefaultLabel :text="`${$t('info.schedule.notificationTiming.title')} ${$t('info.schedule.notificationTiming.mailDeliveryInfoLabel')}`" forId="notificationTimings" size="small" />
        <div class="notification-timings-container">
          <!-- 라디오 버튼 그룹 -->
          <div class="timing-radio-group">
            <label class="timing-radio" :class="{ active: selectedTimingType === 'none' }">
              <input
                type="radio"
                value="none"
                v-model="selectedTimingType"
                class="radio-input"
                :disabled="!isCreate && !canEdit"
              />
              <span>{{ $t('info.schedule.notificationTiming.none') }}</span>
            </label>
            <label class="timing-radio" :class="{ active: selectedTimingType === NOTIFICATION_TIMING_UNIT.DAY }">
              <input
                type="radio"
                :value="NOTIFICATION_TIMING_UNIT.DAY"
                v-model="selectedTimingType"
                class="radio-input"
                :disabled="!isCreate && !canEdit"
              />
              <span>{{ $t('info.schedule.notificationTiming.day') }}</span>
            </label>
            <label class="timing-radio" :class="{ active: selectedTimingType === NOTIFICATION_TIMING_UNIT.HOUR }">
              <input
                type="radio"
                :value="NOTIFICATION_TIMING_UNIT.HOUR"
                v-model="selectedTimingType"
                class="radio-input"
                :disabled="!isCreate && !canEdit"
              />
              <span>{{ $t('info.schedule.notificationTiming.hour') }}</span>
            </label>
            <label class="timing-radio" :class="{ active: selectedTimingType === NOTIFICATION_TIMING_UNIT.MINUTE }">
              <input
                type="radio"
                :value="NOTIFICATION_TIMING_UNIT.MINUTE"
                v-model="selectedTimingType"
                class="radio-input"
                :disabled="!isCreate && !canEdit"
              />
              <span>{{ $t('info.schedule.notificationTiming.minute') }}</span>
            </label>
          </div>

          <!-- 알림 시간 표시 -->
          <div v-if="selectedTimingType !== 'none'" class="notification-time-preview" :class="{ 'error-message': calculatedNotificationTime && !calculatedNotificationTime.includes('년') }">
            {{ calculatedNotificationTime }}
          </div>

          <!-- 입력 필드 영역 -->
          <div v-if="selectedTimingType !== 'none'" class="timing-input-section">
            <!-- 일: 일자, 시, 분 -->
            <template v-if="selectedTimingType === NOTIFICATION_TIMING_UNIT.DAY">
              <div class="timing-input-row">
                <div class="timing-input-item">
                  <label class="timing-input-label">{{ $t('info.schedule.notificationTiming.date') }}</label>
                  <input
                    type="date"
                    v-model="selectedTimingValue.date"
                    class="timing-input timing-date-input"
                    :disabled="!isCreate && !canEdit"
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
                      :disabled="!isCreate && !canEdit"
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
                      :disabled="!isCreate && !canEdit"
                    />
                  </div>
                </div>
              </div>
            </template>
            
            <!-- 시간: 시, 분 -->
            <template v-else-if="selectedTimingType === NOTIFICATION_TIMING_UNIT.HOUR">
              <div class="timing-input-row">
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
                      :disabled="!isCreate && !canEdit"
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
                      :disabled="!isCreate && !canEdit"
                    />
                  </div>
                </div>
              </div>
            </template>
            
            <!-- 분: 분만 -->
            <template v-else-if="selectedTimingType === NOTIFICATION_TIMING_UNIT.MINUTE">
              <div class="timing-input-row">
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
                      :disabled="!isCreate && !canEdit"
                    />
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </form>
    
    <DefaultFormRow marginTop="20px" marginBottom="5px" align="right">
      <!-- 모달 푸터 -->
      <DefaultButton 
        @click="closeModal"
        color="gray"
        marginRight="5px"
        size="small"
      >
        {{ $t('info.schedule.buttons.cancel') }}
      </DefaultButton>
      
      <DefaultButton
        v-if="isCreate || canEdit"
        @click="handleSave"
        marginRight="5px"
        size="small"
      >
        {{ $t('info.schedule.buttons.save') }}
      </DefaultButton>
      
      <!-- 수정 모드일 때만 삭제 버튼 표시 (가장 오른쪽, 권한이 있는 경우만) -->
      <DefaultButton 
        v-if="!isCreate && canDelete && canEdit"
        @click="handleDelete"
        color="red"
        size="small"
      >
        {{ $t('info.schedule.buttons.delete') }}
      </DefaultButton>
    </DefaultFormRow>
  </b-modal>
</template>

<script setup>
import { ref, watch, computed, nextTick, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useHrmStore } from '@/store/hrm';
import { useAuthStore } from '@/store/auth';
import ScheduleApi from '@/api/info/ScheduleApi';
import DepartmentApi from '@/api/hrm/DepartmentApi';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import UserSearchDropdown from '@/components/auth/UserSearchDropdown.vue';
import { 
  fetchScheduleTypes, 
  getScheduleTypeOptionsSync, 
  getScheduleTypeColorSync,
  NOTIFICATION_TIMING_UNIT,
  NOTIFICATION_TIMING_UNIT_LIMITS,
  getNotificationTimingUnitLabel
} from '@/constants/infoConstants';
import { toast } from 'vue3-toastify';
import { getCurrentDateKST } from '@/utils/dateUtils';
import { validateTimingValue, calculateNotificationTime, validateNotificationTiming } from '@/utils/info/scheduleUtils';

const props = defineProps({
  isVisible: {
    type: Boolean,
    default: false,
  },
  schedule: {
    type: Object,
    default: null,
  },
  isCreate: {
    type: Boolean,
    default: true,
  },
  canDelete: {
    type: Boolean,
    default: false,
  },
  canEdit: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['close', 'save', 'delete']);

const { t } = useI18n();
const hrmStore = useHrmStore();
const authStore = useAuthStore();

const scheduleModal = ref(null);
const innerVisible = ref(false);

const form = ref({
  title: '',
  scheduleType: 'OTHER',
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

const departments = ref([]);
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
  if (props.schedule?.scheduleTypeInfo?.label) {
    return props.schedule.scheduleTypeInfo.label;
  }
  return originalScheduleType.value;
});

/**
 * 부서 옵션
 */
const departmentOptions = computed(() => {
  const options = [
    { value: null, label: t('info.schedule.filters.all') }
  ];
  departments.value.forEach(dept => {
    options.push({
      value: dept.departmentId,
      label: dept.departmentName
    });
  });
  return options;
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
  // form에 assigneeName이 있으면 사용
  if (form.value.assigneeName && form.value.assigneeName.trim() !== '') {
    return form.value.assigneeName;
  }
  // props.schedule에서 가져오기 (수정 모드)
  if (props.schedule && props.schedule.assigneeName && props.schedule.assigneeName.trim() !== '') {
    return props.schedule.assigneeName;
  }
  // hrmStore에서 가져오기
  if (hrmStore.myProfile?.name) {
    return hrmStore.myProfile.name;
  }
  return '';
});

/**
 * 일정 데이터를 폼에 로드
 */
function loadScheduleData() {
  if (props.isCreate) {
    // 새 일정 생성 - 첫 번째 일정 유형을 기본값으로 사용
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
  } else if (props.schedule) {
    // 일정 수정 - 백엔드에서 startDate/endDate와 startTime/endTime을 별도로 받음
    let startTimeStr = '';
    let endTimeStr = '';
    
    if (props.schedule.startTime) {
      const startTime = new Date(props.schedule.startTime);
      startTimeStr = startTime.toTimeString().slice(0, 5);
    }
    
    if (props.schedule.endTime) {
      const endTime = new Date(props.schedule.endTime);
      endTimeStr = endTime.toTimeString().slice(0, 5);
    }
    
    const scheduleType = props.schedule.scheduleTypeInfo?.code || '';
    
    // 원본 일정 유형 저장
    originalScheduleType.value = scheduleType;
    
    // 일정 유형이 옵션에 없으면 첫 번째 항목 선택
    const options = getScheduleTypeOptionsSync(scheduleTypes.value);
    const firstType = options.length > 0 ? options[0].value : null;
    const finalScheduleType = scheduleTypes.value.find(st => st.code === scheduleType && st.enabled !== false && st.isActive === true) ? scheduleType : (firstType || '');
    const finalColor = finalScheduleType ? getScheduleTypeColorSync(finalScheduleType, scheduleTypes.value) : (props.schedule.color || '#9E9E9E');
    
    form.value = {
      title: props.schedule.title || '',
      scheduleType: finalScheduleType,
      startDate: props.schedule.startDate || '',
      endDate: props.schedule.endDate || '',
      startTime: startTimeStr,
      endTime: endTimeStr,
      assigneeId: props.schedule.assigneeId || hrmStore.myProfile?.userId || null,
      assigneeName: props.schedule.assigneeName || hrmStore.myProfile?.name || '',
      departmentId: props.schedule.departmentId || hrmStore.myProfile?.departmentId || null,
      departmentName: props.schedule.departmentName || hrmStore.myProfile?.departmentName || '',
      color: finalColor,
      description: props.schedule.description || '',
      notificationTimings: props.schedule.notificationTimings || null,
    };
    
    // 알림 시점 파싱 (JSON 형식만 지원)
    if (props.schedule.notificationTimings) {
      try {
        const timings = JSON.parse(props.schedule.notificationTimings);
        if (Array.isArray(timings) && timings.length > 0) {
          const timing = timings[0]; // 첫 번째 값만 사용
          parseTimingString(timing);
        } else {
          selectedTimingType.value = 'none';
          selectedTimingValue.value = null;
        }
      } catch (e) {
        log.warn('알림 시점 파싱 실패:', e);
        selectedTimingType.value = 'none';
        selectedTimingValue.value = null;
      }
    } else {
      selectedTimingType.value = 'none';
      selectedTimingValue.value = null;
    }
  }
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
watch(selectedTimingType, (newType, oldType) => {
  // 값이 변경될 때만 초기화 (초기 렌더링 시 oldType이 undefined일 수 있음)
  if (oldType !== undefined && newType !== oldType) {
    selectedTimingValue.value = {
      date: '',
      hour: null,
      minute: null
    };
  }
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
 * 담당자는 항상 본인으로 고정
 */

/**
 * 부서 목록 조회
 */
async function loadDepartments() {
  try {
    const res = await DepartmentApi.getDepartments();
    departments.value = res.data || [];
  } catch (error) {
    console.error('부서 목록 조회 실패:', error);
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
    // 백엔드 엔티티 구조에 맞게 데이터 변환
    // startDate, endDate는 LocalDate (YYYY-MM-DD)
    // startTime, endTime은 LocalDateTime (있을 경우만)
    
    // 담당자는 항상 본인
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
      startDate: form.value.startDate, // LocalDate 형식 (YYYY-MM-DD) - 필수
      endDate: form.value.endDate || form.value.startDate, // LocalDate 형식 (YYYY-MM-DD) - 필수, 없으면 startDate와 동일
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
    
    // 시간이 있으면 LocalDateTime 형식으로 변환 (ISO 문자열)
    if (form.value.startTime) {
      const [hours, minutes] = form.value.startTime.split(':');
      scheduleData.startTime = `${form.value.startDate}T${hours}:${minutes}:00`;
    }
    
    if (form.value.endTime) {
      const [hours, minutes] = form.value.endTime.split(':');
      scheduleData.endTime = `${scheduleData.endDate}T${hours}:${minutes}:00`;
    }
    
    if (props.isCreate) {
      await ScheduleApi.createSchedule(scheduleData);
      toast.success(t('info.schedule.success.create'));
    } else {
      // scheduleId 또는 id 사용
      const scheduleId = props.schedule?.scheduleId || props.schedule?.id;
      if (!scheduleId) {
        console.error('일정 ID를 찾을 수 없습니다:', props.schedule);
        toast.error('일정 ID를 찾을 수 없습니다.');
        return;
      }
      console.log('일정 수정 - scheduleId:', scheduleId, 'schedule:', props.schedule);
      await ScheduleApi.updateSchedule(scheduleId, scheduleData);
      toast.success(t('info.schedule.success.update'));
    }
    
    // 모달 닫기
    innerVisible.value = false;
    emit('save');
  } catch (error) {
    console.error('일정 저장 실패:', error);
    toast.error('일정 저장에 실패했습니다.');
  }
}

/**
 * 삭제 처리
 */
function handleDelete() {
  if (props.schedule) {
    // 모달은 닫지 않고 삭제 확인 모달만 표시
    // 삭제 이벤트 emit (부모에서 삭제 확인 모달 표시)
    emit('delete', props.schedule);
  }
}

/**
 * 모달 닫기
 */
async function closeModal() {
  innerVisible.value = false;
  emit('close');
  // 모달이 완전히 닫힌 후 폼 데이터 초기화
  await nextTick();
  resetForm();
  // textarea 크기 초기화
  const descriptionTextarea = document.getElementById('description');
  if (descriptionTextarea) {
    descriptionTextarea.style.height = 'auto';
    descriptionTextarea.style.minHeight = '80px';
  }
}

/**
 * 모달 숨김 처리
 */
async function onHideModal() {
  innerVisible.value = false;
  emit('close');
  // 모달이 완전히 닫힌 후 폼 데이터 초기화
  await nextTick();
  resetForm();
  // textarea 크기 초기화
  const descriptionTextarea = document.getElementById('description');
  if (descriptionTextarea) {
    descriptionTextarea.style.height = 'auto';
    descriptionTextarea.style.minHeight = '80px';
  }
}

/**
 * 시작 시간 초기화
 */
function resetStartTime() {
  if (props.isCreate || props.canEdit) {
    form.value.startTime = '';
  }
}

/**
 * 종료 시간 초기화
 */
function resetEndTime() {
  if (props.isCreate || props.canEdit) {
    form.value.endTime = '';
  }
}

/**
 * 폼 데이터 초기화
 */
function resetForm() {
  // 첫 번째 일정 유형을 기본값으로 사용
  const options = getScheduleTypeOptionsSync(scheduleTypes.value);
  const firstType = options.length > 0 ? options[0].value : null;
  const defaultColor = firstType ? getScheduleTypeColorSync(firstType, scheduleTypes.value) : '#9E9E9E';
  originalScheduleType.value = null;
  form.value = {
    title: '',
    scheduleType: firstType || '',
    startDate: '',
    endDate: '',
    startTime: '',
    endTime: '',
    assigneeId: null,
    assigneeName: '',
    departmentId: null,
    departmentName: '',
    color: defaultColor || '#9E9E9E',
    description: '',
  };
  // 알림 시점 초기화
  selectedTimingType.value = 'none';
  selectedTimingValue.value = {
    date: '',
    hour: null,
    minute: null
  };
}

/**
 * 모달 표시 시 데이터 로드
 */
watch(() => props.isVisible, async (newVal, oldVal) => {
  // props.isVisible이 true로 변경될 때 모달 열기
  if (newVal) {
    // 모달이 열리기 전에 폼 데이터 초기화
    resetForm();
    innerVisible.value = true;
    await nextTick();
    // 일정 유형 데이터 로드
    const types = await fetchScheduleTypes();
    scheduleTypes.value = types;
    await loadDepartments();
    loadScheduleData();
    // textarea 크기 초기화
    await nextTick();
    const descriptionTextarea = document.getElementById('description');
    if (descriptionTextarea) {
      descriptionTextarea.style.height = 'auto';
      descriptionTextarea.style.minHeight = '80px';
    }
  } else {
    // props.isVisible이 false로 변경될 때 모달 닫기
    innerVisible.value = false;
    // 모달이 완전히 닫힌 후 폼 데이터 초기화
    await nextTick();
    resetForm();
  }
});

/**
 * 일정 변경 시 폼 업데이트
 */
watch(() => props.schedule, () => {
  if (props.isVisible) {
    loadScheduleData();
  }
}, { deep: true });

/**
 * 일정 유형 변경 시 색상 자동 업데이트
 */
watch(() => form.value.scheduleType, (newType, oldType) => {
  if (newType && newType !== oldType) {
    const newColor = getScheduleTypeColorSync(newType, scheduleTypes.value) || '#9E9E9E';
    // 일정 유형이 변경되면 항상 색상을 업데이트 (사용자가 수동으로 변경한 경우도 포함)
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

/**
 * 일정 유형 데이터 초기화
 */
onMounted(async () => {
  await fetchScheduleTypes();
});
</script>

<style scoped>
/* PostCreateModifyModal과 동일한 스타일 */
.layer {
  margin-bottom: 15px;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.8rem;
  height: 38px;
  font-family: inherit;
  box-sizing: border-box;
}

.form-control:focus {
  outline: none;
  border-color: #0d6efd;
  box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.25);
}

/* PostCreateModifyModal과 동일한 스타일 */
/* DefaultFormRow의 d-flex를 grid로 덮어쓰기 */
:deep(.default-form-row.two-col) {
  display: grid !important;
  grid-template-columns: 1fr 1fr !important;
  column-gap: 12px !important;
  align-items: flex-start !important;
  width: 100% !important;
  gap: 0 !important; /* DefaultFormRow의 gap 제거 */
}

:deep(.default-form-row.two-col .col-wrapper) {
  width: 100% !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 5px !important;
  min-width: 0 !important;
}

:deep(.default-form-row.two-col .col-wrapper .form-control) {
  min-width: 0 !important;
  width: 100% !important;
  height: 38px !important;
  box-sizing: border-box !important;
}

:deep(.default-form-row.two-col .col-wrapper .default-textfield),
:deep(.default-form-row.two-col .col-wrapper .default-select) {
  height: 38px !important;
}

:deep(.default-form-row.two-col .col-wrapper .default-textfield input),
:deep(.default-form-row.two-col .col-wrapper .default-select select) {
  height: 38px !important;
  box-sizing: border-box !important;
}

:deep(.default-form-row.two-col .col-wrapper > *) {
  width: 100% !important;
}

.color-picker-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
}

.color-input {
  width: 50px;
  height: 38px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  padding: 2px;
  box-sizing: border-box;
}

.color-input:hover {
  border-color: #0d6efd;
}

textarea.form-control {
  resize: vertical;
  min-height: 80px;
  height: auto;
}

.notification-timings-section {
  margin-top: 15px;
}

.notification-timings-container {
  padding: 10px;
  background: #ffffff;
  border-radius: 4px;
  border: 1px solid #ddd;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.timing-radio-group {
  display: flex;
  flex-direction: row;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.timing-radio {
  display: flex;
  align-items: center;
  padding: 4px 10px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
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
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e9ecef;
}

.timing-input-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.timing-input-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 180px;
}

.timing-input-label {
  font-size: 0.75rem;
  font-weight: 600;
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
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.8rem;
  background: white;
  color: #212529;
  transition: all 0.2s ease;
  height: 38px;
  box-sizing: border-box;
}

.timing-date-input {
  min-width: 160px;
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
  font-size: 0.85rem;
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
  font-size: 0.65rem;
  color: #495057;
  font-weight: 500;
  text-align: center;
}

.notification-time-preview.error-message {
  color: #dc3545 !important;
}

.notification-info-text {
  margin-top: 4px;
  font-size: 0.6rem;
  color: #6c757d;
  text-align: center;
  font-style: italic;
}

.label-with-icon {
  display: flex;
  align-items: center;
  gap: 0px;
  line-height: 1;
}

.label-with-icon :deep(.default-label) {
  display: inline-flex;
  align-items: center;
  line-height: 1;
  margin: 0;
  margin-right: 6px;
  vertical-align: baseline;
}

.time-reset-icon {
  font-size: 0.875rem;
  color: #6c757d;
  cursor: pointer;
  transition: color 0.2s ease;
  flex-shrink: 0;
  font-weight: 800;
  margin: 0;
  padding: 0;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  vertical-align: baseline;
}

.time-reset-icon:hover:not(.disabled) {
  color: #0d6efd;
}

.time-reset-icon.disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

@media (max-width: 650px) {
  .two-col {
    grid-template-columns: 1fr !important;
  }
  
  .form-control {
    font-size: 0.7rem;
    height: 38px !important;
  }
  
  textarea.form-control {
    height: auto !important;
  }
  
  :deep(.default-textfield),
  :deep(.default-select) {
    height: 38px !important;
  }
  
  :deep(.default-textfield input),
  :deep(.default-select select) {
    height: 38px !important;
  }
}
</style>
