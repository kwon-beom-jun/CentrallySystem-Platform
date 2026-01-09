<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle
        :title="$t('menu.info.scheduleSettings')"
        subtitle="Schedule Settings"
        icon="ri-settings-3-line"
        desktopMarginBottom="20px"
      />

      <!-- 알림 설정 폼 -->
      <div class="settings-container">
        <form @submit.prevent="handleSave" v-if="!loading">
          <!-- 1. 일정 유형별 알림 설정 -->
          <div class="feature-section">
            <div class="feature-header">
              <span class="feature-number">1</span>
              <h4>{{ $t('info.schedule.settings.scheduleTypeNotification.title') }}</h4>
            </div>
            <div class="feature-content">
              <InfoNotice 
                color="blue"
                icon="ri-information-line"
                :text="$t('info.schedule.settings.scheduleTypeNotification.description')"
              />
              <div class="type-list">
                <div class="type-item" v-for="type in scheduleTypes" :key="type.value">
                  <div class="type-info">
                    <span class="type-badge" :style="{ backgroundColor: type.color }"></span>
                    <span class="type-label">{{ type.label }}</span>
                  </div>
                  <div class="type-toggle">
                    <span class="toggle-label">{{ $t('info.schedule.settings.scheduleTypeNotification.title').replace('일정 유형별 ', '') }}</span>
                    <label class="toggle-switch" :class="{ active: isScheduleTypeEnabled(type.value) }">
                      <input
                        type="checkbox"
                        :checked="isScheduleTypeEnabled(type.value)"
                        @change="toggleScheduleType(type.value, $event.target.checked)"
                        class="toggle-input"
                      />
                      <span class="toggle-slider"></span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 2. 알림 시간대 설정 -->
          <div class="feature-section">
            <div class="feature-header">
              <span class="feature-number">2</span>
              <h4>{{ $t('info.schedule.settings.timeWindow.title') }}</h4>
            </div>
            <div class="feature-content">
              <InfoNotice 
                color="blue"
                icon="ri-information-line"
                :text="$t('info.schedule.settings.timeWindow.description')"
              />
              <div class="time-setting-group">
                <div class="time-setting-item">
                  <label class="time-label">{{ $t('info.schedule.settings.timeWindow.weekdayStart') }}:</label>
                  <input
                    type="time"
                    v-model="form.weekdayStartTime"
                    class="time-input"
                  />
                </div>
                <div class="time-setting-item">
                  <label class="time-label">{{ $t('info.schedule.settings.timeWindow.weekdayEnd') }}:</label>
                  <input
                    type="time"
                    v-model="form.weekdayEndTime"
                    class="time-input"
                  />
                </div>
              </div>
              <div class="feature-item">
                <i class="ri-calendar-2-line"></i>
                <span>{{ $t('info.schedule.settings.timeWindow.weekend') }}</span>
                <label class="toggle-switch" :class="{ active: form.weekendEnabled }">
                  <input
                    type="checkbox"
                    v-model="form.weekendEnabled"
                    class="toggle-input"
                  />
                  <span class="toggle-slider"></span>
                </label>
              </div>
              <div class="feature-item">
                <i class="ri-calendar-event-line"></i>
                <span>{{ $t('info.schedule.settings.timeWindow.holiday') }}</span>
                <label class="toggle-switch" :class="{ active: form.holidayEnabled }">
                  <input
                    type="checkbox"
                    v-model="form.holidayEnabled"
                    class="toggle-input"
                  />
                  <span class="toggle-slider"></span>
                </label>
              </div>
            </div>
          </div>

          <!-- 3. 알림 방법 선택 -->
          <div class="feature-section">
            <div class="feature-header">
              <span class="feature-number">3</span>
              <h4>{{ $t('info.schedule.settings.method.title') }}</h4>
            </div>
            <div class="feature-content">
              <div class="method-list">
                <div class="method-item" :class="{ current: form.emailEnabled }">
                  <i class="ri-mail-line"></i>
                  <span>{{ $t('info.schedule.settings.method.email') }}</span>
                  <label class="toggle-switch" :class="{ active: form.emailEnabled }">
                    <input
                      type="checkbox"
                      v-model="form.emailEnabled"
                      class="toggle-input"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                </div>
                <div class="method-item future" :class="{ disabled: true }">
                  <i class="ri-message-3-line"></i>
                  <span>{{ $t('info.schedule.settings.method.sms') }}</span>
                  <span class="method-badge future">추후</span>
                </div>
                <div class="method-item future" :class="{ disabled: true }">
                  <i class="ri-notification-3-line"></i>
                  <span>{{ $t('info.schedule.settings.method.push') }}</span>
                  <span class="method-badge future">추후</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 저장 버튼 -->
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="handleCancel">
              취소
            </button>
            <button type="submit" class="btn-save" :disabled="saving">
              {{ saving ? '저장 중...' : '저장' }}
            </button>
          </div>
        </form>

        <!-- 로딩 상태 -->
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>설정을 불러오는 중...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vue3-toastify';
import PageTitle from '@/components/common/title/PageTitle.vue';
import InfoNotice from '@/components/common/notice/InfoNotice.vue';
import { fetchScheduleTypes } from '@/constants/infoConstants';
import ScheduleNotificationApi from '@/api/info/ScheduleNotificationApi';

const { t } = useI18n();

/**
 * 일정 유형 목록
 */
const scheduleTypes = ref([]);

/**
 * 로딩 상태
 */
const loading = ref(true);
const saving = ref(false);


/**
 * 폼 데이터
 */
const form = ref({
  timezone: 'Asia/Seoul',
  weekdayStartTime: '09:00',
  weekdayEndTime: '18:00',
  weekendEnabled: false,
  holidayEnabled: false,
  scheduleTypeNotifications: '{}',
  emailEnabled: true,
  smsEnabled: false,
  pushEnabled: false,
});

/**
 * 원본 데이터 (취소 시 복원용)
 */
const originalForm = ref(null);

/**
 * 일정 유형별 알림 설정 (JSON 파싱)
 */
const scheduleTypeNotifications = ref({});

/**
 * 일정 유형별 알림 활성화 여부 확인
 */
function isScheduleTypeEnabled(typeCode) {
  return scheduleTypeNotifications.value[typeCode] !== false;
}

/**
 * 일정 유형별 알림 토글
 */
function toggleScheduleType(typeCode, enabled) {
  scheduleTypeNotifications.value[typeCode] = enabled;
}

/**
 * 알림 설정 조회
 */
async function loadNotificationSettings() {
  try {
    loading.value = true;
    const response = await ScheduleNotificationApi.getNotificationSettings();
    const settings = response.data;

    // 일정 유형별 알림 설정 파싱
    if (settings.scheduleTypeNotifications) {
      try {
        scheduleTypeNotifications.value = JSON.parse(settings.scheduleTypeNotifications);
      } catch (e) {
        scheduleTypeNotifications.value = {};
      }
    }

    // 폼 데이터 설정
    form.value = {
      timezone: settings.timezone || 'Asia/Seoul',
      weekdayStartTime: settings.weekdayStartTime || '09:00',
      weekdayEndTime: settings.weekdayEndTime || '18:00',
      weekendEnabled: settings.weekendEnabled ?? false,
      holidayEnabled: settings.holidayEnabled ?? false,
      scheduleTypeNotifications: settings.scheduleTypeNotifications || '{}',
      emailEnabled: settings.emailEnabled ?? true,
      smsEnabled: settings.smsEnabled ?? false,
      pushEnabled: settings.pushEnabled ?? false,
    };

    // 원본 데이터 저장
    originalForm.value = JSON.parse(JSON.stringify(form.value));
  } catch (error) {
    console.error('알림 설정 조회 실패:', error);
    toast.error('알림 설정을 불러오는데 실패했습니다.');
  } finally {
    loading.value = false;
  }
}

/**
 * 저장 처리
 */
async function handleSave() {
  if (saving.value) return;

  try {
    saving.value = true;

    // 일정 유형별 알림 설정을 JSON 객체로 변환
    const scheduleTypeNotificationsJson = JSON.stringify(scheduleTypeNotifications.value);

    const settingsData = {
      notificationTimings: '[]', // 일정별 설정으로 이동했으므로 빈 배열로 설정
      timezone: form.value.timezone,
      weekdayStartTime: form.value.weekdayStartTime,
      weekdayEndTime: form.value.weekdayEndTime,
      weekendEnabled: form.value.weekendEnabled,
      holidayEnabled: form.value.holidayEnabled,
      scheduleTypeNotifications: scheduleTypeNotificationsJson,
      emailEnabled: form.value.emailEnabled,
      smsEnabled: form.value.smsEnabled,
      pushEnabled: form.value.pushEnabled,
    };

    await ScheduleNotificationApi.updateNotificationSettings(settingsData);
    toast.success('알림 설정이 저장되었습니다.');
    
    // 원본 데이터 업데이트
    originalForm.value = JSON.parse(JSON.stringify(form.value));
  } catch (error) {
    console.error('알림 설정 저장 실패:', error);
    toast.error('알림 설정 저장에 실패했습니다.');
  } finally {
    saving.value = false;
  }
}

/**
 * 취소 처리
 */
function handleCancel() {
  if (originalForm.value) {
    // 원본 데이터로 복원
    form.value = JSON.parse(JSON.stringify(originalForm.value));

    // 일정 유형별 알림 설정 복원
    try {
      scheduleTypeNotifications.value = JSON.parse(form.value.scheduleTypeNotifications);
    } catch (e) {
      scheduleTypeNotifications.value = {};
    }
  }
  toast.info('변경사항이 취소되었습니다.');
}

/**
 * 일정 유형 목록 조회
 */
async function loadScheduleTypes() {
  try {
    const types = await fetchScheduleTypes();
    scheduleTypes.value = types.map(st => ({
      value: st.code,
      label: st.label,
      color: st.color || '#9E9E9E',
    }));

    // 일정 유형별 알림 설정 초기화 (모든 유형 기본값: true)
    if (Object.keys(scheduleTypeNotifications.value).length === 0) {
      types.forEach(type => {
        if (scheduleTypeNotifications.value[type.code] === undefined) {
          scheduleTypeNotifications.value[type.code] = true;
        }
      });
    }
  } catch (error) {
    console.error('일정 유형 조회 실패:', error);
    // 기본값 설정 (에러 시)
    scheduleTypes.value = [
      { value: 'BUSINESS_TRIP', label: '외근', color: '#FF9800' },
      { value: 'VACATION', label: '휴가', color: '#4CAF50' },
      { value: 'MEETING', label: '회의', color: '#2196F3' },
      { value: 'EDUCATION', label: '교육', color: '#9C27B0' },
      { value: 'OTHER', label: '기타', color: '#9E9E9E' },
    ];
  }
}

/**
 * 초기화
 */
onMounted(async () => {
  await Promise.all([
    loadScheduleTypes(),
    loadNotificationSettings(),
  ]);
});
</script>

<style scoped>
.settings-container {
  max-width: 1000px;
  margin-bottom: 20px;
}

.feature-section {
  background: var(--theme-bg-main);
  border: 1px solid var(--theme-border);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.feature-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--theme-border-light);
}

.feature-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border-radius: 50%;
  font-weight: 700;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.feature-header h4 {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--theme-text-primary);
  margin: 0;
  flex: 1;
}

.required-badge {
  display: inline-block;
  padding: 2px 8px;
  background: #fee2e2;
  color: #dc2626;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 600;
  margin-left: 8px;
}

.feature-content {
  padding-left: 44px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  font-size: 0.9rem;
  color: var(--theme-text-primary);
}

.feature-item i {
  font-size: 1.2rem;
  color: var(--theme-text-muted);
  width: 24px;
  text-align: center;
}

.feature-options {
  margin: 16px 0 20px 36px;
  padding: 16px;
  background: var(--theme-bg-secondary);
  border-radius: 8px;
}

.option-group {
  margin-bottom: 12px;
}

.option-group:last-child {
  margin-bottom: 0;
}

.option-label {
  display: block;
  font-size: 0.8rem;
  color: var(--theme-text-secondary);
  margin-bottom: 8px;
  font-weight: 600;
}

.option-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.option-tag-checkbox {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: var(--theme-bg-main);
  border: 1px solid var(--theme-border);
  border-radius: 6px;
  font-size: 0.8rem;
  color: var(--theme-text-primary);
  cursor: pointer;
  transition: all 0.2s;
}

.option-tag-checkbox:hover {
  border-color: #3b82f6;
}

.option-tag-checkbox.active {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #2563eb;
  font-weight: 600;
}

.checkbox-input {
  margin-right: 6px;
  cursor: pointer;
}

.custom-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 8px 12px;
  background: var(--theme-bg-main);
  border: 1px dashed var(--theme-border);
  border-radius: 6px;
}

.custom-input {
  flex: 1;
  max-width: 120px;
  padding: 6px 10px;
  border: 1px solid var(--theme-border);
  border-radius: 4px;
  font-size: 0.85rem;
  background: var(--theme-bg-main);
  color: var(--theme-text-primary);
}

.custom-input:focus {
  outline: none;
  border-color: #3b82f6;
}

.custom-suffix {
  font-size: 0.85rem;
  color: var(--theme-text-secondary);
  white-space: nowrap;
}


.type-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.type-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: var(--theme-bg-secondary);
  border-radius: 8px;
  transition: background 0.2s;
}

.type-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.type-badge {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}

.type-label {
  font-size: 0.9rem;
  color: var(--theme-text-primary);
  font-weight: 500;
}

.type-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toggle-label {
  font-size: 0.8rem;
  color: var(--theme-text-secondary);
}

.toggle-switch {
  position: relative;
  width: 44px;
  height: 24px;
  background: #cbd5e1;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.toggle-switch.active {
  background: #3b82f6;
}

.toggle-switch.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.toggle-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  transition: transform 0.2s;
}

.toggle-switch.active .toggle-slider {
  transform: translateX(20px);
}

.time-setting-group {
  margin: 16px 0 20px 0px;
  padding: 16px;
  background: var(--theme-bg-secondary);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.time-setting-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-label {
  font-size: 0.85rem;
  color: var(--theme-text-secondary);
  font-weight: 500;
  min-width: 120px;
}

.time-input {
  padding: 8px 12px;
  border: 1px solid var(--theme-border);
  border-radius: 6px;
  font-size: 0.9rem;
  background: var(--theme-bg-main);
  color: var(--theme-text-primary);
  flex: 1;
  max-width: 150px;
}

.method-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--theme-bg-secondary);
  border-radius: 8px;
  font-size: 0.9rem;
  color: var(--theme-text-primary);
}

.method-item i {
  font-size: 1.3rem;
  color: var(--theme-text-muted);
  width: 24px;
  text-align: center;
}

.method-item.current {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.method-item.future {
  opacity: 0.6;
}

.method-item.disabled {
  cursor: not-allowed;
}

.method-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  margin-left: auto;
}

.method-badge.future {
  background: #f3f4f6;
  color: #6b7280;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--theme-border);
}

.btn-cancel,
.btn-save {
  padding: 10px 24px;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-cancel {
  background: var(--theme-bg-secondary);
  color: var(--theme-text-primary);
  border: 1px solid var(--theme-border);
}

.btn-cancel:hover {
  background: var(--theme-bg-tertiary);
}

.btn-save {
  background: #3b82f6;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #2563eb;
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--theme-border);
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 모바일 스타일 */
@media (max-width: 650px) {
  .feature-section {
    padding: 16px;
    margin-bottom: 16px;
    border-radius: 8px;
  }

  .feature-header {
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 16px;
    padding-bottom: 12px;
  }

  .feature-number {
    width: 24px;
    height: 24px;
    font-size: 0.7rem;
  }

  .feature-header h4 {
    font-size: 0.85rem !important;
  }

  .required-badge {
    font-size: 0.65rem;
    padding: 2px 6px;
  }

  .feature-content {
    padding-left: 0;
  }

  .feature-item {
    font-size: 0.7rem !important;
    padding: 10px 0;
  }

  .feature-item i {
  font-size: 1rem;
    width: 20px;
  }

  .feature-options {
    margin-left: 0;
    padding: 12px;
  }

  .option-label {
    font-size: 0.75rem;
  }

  .option-tag-checkbox {
    padding: 5px 10px;
    font-size: 0.75rem !important;
  }

  .custom-input-group {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
    padding: 8px;
  }

  .custom-input {
    max-width: 100%;
    width: 100%;
  }

  .time-setting-group {
    margin-left: 0;
    padding: 12px;
  }

  .time-setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .time-label {
    min-width: auto;
    font-size: 0.7rem !important;
  }

  .time-input {
    max-width: 100%;
    width: 100%;
    font-size: 0.75rem !important;
  }

  .type-item {
    padding: 12px;
  }

  .type-label {
    font-size: 0.7rem !important;
  }

  .toggle-label {
    font-size: 0.7rem !important;
  }

  .type-badge {
    width: 12px;
    height: 12px;
  }

  .method-item {
    padding: 12px;
    font-size: 0.7rem !important;
  }

  .method-item i {
    font-size: 1.1rem;
  }

  .form-actions {
    flex-direction: column-reverse;
    gap: 8px;
  }

  .btn-cancel,
  .btn-save {
    width: 100%;
    font-size: 0.8rem !important;
  }
}
</style>
