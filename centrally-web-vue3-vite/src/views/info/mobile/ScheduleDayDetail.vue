<template>
  <div>
    <div class="content content-wrapper">
      <MobileDetailTitle :title="formattedDate" :backTo="backToPath" />

      <!-- 일정 목록 -->
      <div class="schedule-list">
        <div v-if="daySchedules.length === 0" class="no-schedules">
          <div class="no-schedules-icon">
            <i class="ri-calendar-line"></i>
          </div>
          <p>{{ $t('info.schedule.noSchedules') }}</p>
        </div>

        <div
          v-for="schedule in daySchedules"
          :key="schedule.id || schedule.scheduleId"
          class="schedule-item"
          :style="{ borderLeftColor: (schedule.color || getScheduleTypeColorSync(schedule.scheduleTypeInfo?.code || '', scheduleTypes, schedule.scheduleTypeInfo)) + '60' }"
          @click="goToEdit(schedule)"
        >
          <!-- 왼쪽 색상 바 -->
          <div class="schedule-color-bar" :style="{ backgroundColor: schedule.color || getScheduleTypeColorSync(schedule.scheduleTypeInfo?.code || '', scheduleTypes, schedule.scheduleTypeInfo) }"></div>
          
          <!-- 시간 표시 영역 -->
          <div class="schedule-time-section" :style="{ borderRightColor: (schedule.color || getScheduleTypeColorSync(schedule.scheduleTypeInfo?.code || '', scheduleTypes, schedule.scheduleTypeInfo)) + '40' }">
            <div class="schedule-time">
              <span v-if="schedule.startTime || schedule.endTime" class="time-text time-specified" :style="{ color: schedule.color || getScheduleTypeColorSync(schedule.scheduleTypeInfo?.code || '', scheduleTypes, schedule.scheduleTypeInfo) }">
                {{ $t('info.schedule.timeSpecified') || '시간 지정' }}
              </span>
              <span v-else class="time-text all-day">{{ $t('info.schedule.allDay') || '종일' }}</span>
            </div>
          </div>

          <!-- 일정 정보 영역 -->
          <div class="schedule-content">
            <div class="schedule-title">{{ schedule.title }}</div>
            
            <!-- 일정 유형, 담당자, 부서 -->
            <div class="schedule-meta">
              <span class="schedule-type" :style="{ backgroundColor: schedule.color || getScheduleTypeColorSync(schedule.scheduleTypeInfo?.code || '', scheduleTypes, schedule.scheduleTypeInfo) }">
                {{ getScheduleTypeLabelSync(schedule.scheduleTypeInfo?.code || '', scheduleTypes, schedule.scheduleTypeInfo) }}
              </span>
              <span class="schedule-assignee">
                <i class="ri-user-line"></i>
                {{ schedule.assigneeName }}
              </span>
              <span v-if="schedule.departmentName" class="schedule-department">
                <i class="ri-building-line"></i>
                {{ schedule.departmentName }}
              </span>
            </div>
            
            <!-- 날짜/시간 정보 -->
            <div class="schedule-info">
              <div class="info-item">
                <i class="ri-calendar-line"></i>
                <span>{{ formatDate(schedule.startDate) }}</span>
                <span v-if="schedule.endDate && schedule.endDate !== schedule.startDate" class="date-range">
                  ~ {{ formatDate(schedule.endDate) }}
                </span>
              </div>
              <div v-if="schedule.startTime || schedule.endTime" class="info-item">
                <i class="ri-time-line"></i>
                <span v-if="schedule.startTime">{{ formatTime(schedule.startTime) }}</span>
                <span v-if="schedule.startTime && schedule.endTime" class="time-separator">-</span>
                <span v-if="schedule.endTime">{{ formatTime(schedule.endTime) }}</span>
              </div>
            </div>
          </div>

          <!-- 화살표 아이콘 -->
          <div class="schedule-arrow">
            <i class="ri-arrow-right-s-line"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/store/auth';
import { useHrmStore } from '@/store/hrm';
import ScheduleApi from '@/api/info/ScheduleApi';
import MobileDetailTitle from '@/components/common/title/MobileDetailTitle.vue';
import { fetchScheduleTypes, getScheduleTypeLabelSync, getScheduleTypeColorSync } from '@/constants/infoConstants';
import { toast } from 'vue3-toastify';
import { ROUTES } from '@/config/menuConfig';
import { useViewStateStore } from '@/store/viewState';

const router = useRouter();
const route = useRoute();
const { t, locale } = useI18n();
const authStore = useAuthStore();
const hrmStore = useHrmStore();
const viewState = useViewStateStore();

const selectedDate = ref(null);
const schedules = ref([]);
const scheduleTypes = ref([]);

/**
 * 뒤로가기 경로 결정
 */
const backToPath = computed(() => {
  const savedState = viewState.getState('scheduleCalendar');
  // 리스트 뷰에서 왔으면 리스트 뷰로, 아니면 캘린더로
  if (savedState?.currentView === 'list') {
    return `${ROUTES.INFO.SCHEDULE}?view=list`;
  }
  return ROUTES.INFO.SCHEDULE;
});

/**
 * 날짜 파싱 및 포맷팅
 */
const formattedDate = computed(() => {
  if (!selectedDate.value) return '';
  
  let dateStr = selectedDate.value;
  if (dateStr.includes('T')) {
    dateStr = dateStr.split('T')[0];
  }
  
  const date = new Date(dateStr + 'T00:00:00');
  const weekdays = locale.value === 'ko' ? ['일', '월', '화', '수', '목', '금', '토'] : ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const weekday = weekdays[date.getDay()];
  
  if (locale.value === 'ko') {
    return `${year}년 ${month}월 ${day}일 (${weekday})`;
  } else {
    return `${weekday}, ${month}/${day}/${year}`;
  }
});

/**
 * 해당 날짜의 일정 필터링 및 시간순 정렬
 */
const daySchedules = computed(() => {
  if (!selectedDate.value) {
    return [];
  }
  
  let targetDate = selectedDate.value;
  if (targetDate.includes('T')) {
    targetDate = targetDate.split('T')[0];
  }
  
  const dateMatch = targetDate.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!dateMatch) {
    return [];
  }
  
  const targetYear = parseInt(dateMatch[1]);
  const targetMonth = parseInt(dateMatch[2]);
  const targetDay = parseInt(dateMatch[3]);
  
  const filtered = schedules.value.filter((schedule) => {
    if (!schedule.startDate) {
      return false;
    }
    
    let startDate = schedule.startDate;
    let endDate = schedule.endDate || schedule.startDate;
    
    if (startDate.includes('T')) {
      startDate = startDate.split('T')[0];
    }
    if (endDate.includes('T')) {
      endDate = endDate.split('T')[0];
    }
    
    const targetDateObj = new Date(targetYear, targetMonth - 1, targetDay);
    const startDateObj = new Date(startDate + 'T00:00:00');
    const endDateObj = new Date(endDate + 'T23:59:59');
    
    return targetDateObj >= startDateObj && targetDateObj <= endDateObj;
  });

  return filtered.sort((a, b) => {
    if (!a.startTime && !b.startTime) {
      return (a.title || '').localeCompare(b.title || '');
    }
    if (!a.startTime) return 1;
    if (!b.startTime) return -1;
    return a.startTime.localeCompare(b.startTime);
  });
});

/**
 * 날짜 포맷팅 (MM/DD 형식)
 */
function formatDate(dateStr) {
  if (!dateStr) return '';
  
  let date = dateStr;
  if (date.includes('T')) {
    date = date.split('T')[0];
  }
  
  const dateObj = new Date(date + 'T00:00:00');
  const month = dateObj.getMonth() + 1;
  const day = dateObj.getDate();
  return `${month}/${day}`;
}

/**
 * 시간 포맷팅
 */
function formatTime(timeStr) {
  if (!timeStr) return '';
  
  if (timeStr.includes('T')) {
    const date = new Date(timeStr);
    const hours = date.getHours();
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const ampm = hours >= 12 ? 'PM' : 'AM';
    const displayHour = hours % 12 || 12;
    return `${ampm} ${displayHour}:${minutes}`;
  }
  
  const [hours, minutes] = timeStr.split(':');
  const hour = parseInt(hours);
  const ampm = hour >= 12 ? 'PM' : 'AM';
  const displayHour = hour % 12 || 12;
  return `${ampm} ${displayHour}:${minutes}`;
}

/**
 * 수정 페이지로 이동
 */
function goToEdit(schedule) {
  if (!schedule) return;
  
  viewState.saveState('scheduleCalendar', {
    selectedDepartmentId: null,
    assigneeSearchKeyword: '',
    currentPage: 1,
    currentDate: selectedDate.value,
    scrollY: 0,
  });
  
  const scheduleIdValue = schedule.scheduleId || schedule.id;
  if (!scheduleIdValue) {
    toast.error('일정 정보를 찾을 수 없습니다.');
    return;
  }
  
  sessionStorage.setItem('scheduleEdit', JSON.stringify({
    scheduleId: scheduleIdValue,
    canEdit: canEditOrDelete(schedule),
    canDelete: canEditOrDelete(schedule),
  }));
  
  // 이전 경로 저장 (일자 상세 페이지에서 왔을 경우)
  sessionStorage.setItem('scheduleEditReturnPath', route.fullPath);
  
  router.push(ROUTES.INFO.SCHEDULE_EDIT_MOBILE);
}

/**
 * 권한 체크
 */
function canEditOrDelete(schedule) {
  if (!schedule) return false;

  const roles = authStore.getRoles || [];
  const isManagerOrAdmin =
    roles.includes('ROLE_INFO_MANAGER') || roles.includes('ROLE_INFO_ADMIN');

  if (isManagerOrAdmin) return true;

  // 내 소유인지 확인
  const myUserId = hrmStore.myProfile?.userId;
  const scheduleAssigneeId = schedule.assigneeId;

  return myUserId === scheduleAssigneeId;
}

/**
 * 일정 목록 로드
 */
async function loadSchedules() {
  if (!selectedDate.value) return;
  
  try {
    const date = new Date(selectedDate.value + 'T00:00:00');
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    
    // viewState에서 필터 상태 가져오기
    const savedState = viewState.getState('scheduleCalendar');
    // 필터가 저장되어 있지 않으면(undefined) 사용자의 부서로 기본 설정
    // null이면 "전체"를 선택한 것이므로 null 유지
    let selectedDepartmentId = savedState?.selectedDepartmentId;
    if (selectedDepartmentId === undefined) {
      selectedDepartmentId = hrmStore.myProfile?.departmentId || null;
    }
    const assigneeSearchKeyword = savedState?.assigneeSearchKeyword || '';
    
    // 날짜 범위 계산
    const startDate = new Date(year, month - 1, 1);
    const endDate = new Date(year, month, 0);
    const startDateStr = `${startDate.getFullYear()}-${String(startDate.getMonth() + 1).padStart(2, '0')}-${String(startDate.getDate()).padStart(2, '0')}`;
    const endDateStr = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`;
    
    const params = {
      startDate: startDateStr,
      endDate: endDateStr,
    };
    
    // 부서 필터 적용 (null이 아니고 숫자인 경우에만)
    if (selectedDepartmentId !== null && typeof selectedDepartmentId === 'number') {
      params.departmentId = selectedDepartmentId;
    }
    
    const response = await ScheduleApi.getSchedules(params);
    
    let filteredSchedules = response.data || [];
    
    // 담당자 검색어 필터링 (프론트엔드에서 필터링)
    if (assigneeSearchKeyword && assigneeSearchKeyword.trim() !== '') {
      const keyword = assigneeSearchKeyword.trim().toLowerCase();
      filteredSchedules = filteredSchedules.filter((schedule) => {
        const assigneeName = (schedule.assigneeName || '').toLowerCase();
        return assigneeName.includes(keyword);
      });
    }
    
    schedules.value = filteredSchedules;
  } catch (error) {
    console.error('일정 목록 로드 실패:', error);
    toast.error(t('info.schedule.error.loadSchedulesFailed'));
  }
}

/**
 * URL 파라미터에서 날짜 가져오기
 */
function setDateFromRoute() {
  const dateParam = route.params.date;
  
  if (dateParam) {
    selectedDate.value = dateParam;
  } else {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    selectedDate.value = `${year}-${month}-${day}`;
  }
}

onMounted(async () => {
  scheduleTypes.value = await fetchScheduleTypes();
  setDateFromRoute();
  await loadSchedules();
});

watch(() => route.params.date, async (newDate, oldDate) => {
  if (newDate && newDate !== oldDate) {
    selectedDate.value = newDate;
    await loadSchedules();
  }
}, { immediate: false });

watch(() => route.params, async (newParams) => {
  if (newParams.date && newParams.date !== selectedDate.value) {
    selectedDate.value = newParams.date;
    await loadSchedules();
  }
}, { deep: true });

watch(selectedDate, async (newDate) => {
  if (newDate) {
    await loadSchedules();
  }
});
</script>

<style scoped>
.schedule-list {
  padding: 0;
  margin-top: 30px;
}

.no-schedules {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.no-schedules-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e6f2ff 0%, #cfe6ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 140, 255, 0.1);
}

.no-schedules-icon i {
  font-size: 36px;
  color: var(--theme-primary);
}

.no-schedules p {
  font-size: 0.9rem;
  margin: 0;
  color: var(--theme-text-secondary);
  font-weight: 500;
}

.schedule-item {
  position: relative;
  display: flex;
  align-items: stretch;
  padding: 0;
  margin-bottom: 12px;
  background: #ffffff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  gap: 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  border: 1px solid #e6f2ff;
  border-left-width: 3px;
}

.schedule-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--schedule-color, var(--theme-primary)) 0%, rgba(0, 140, 255, 0.3) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.schedule-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-color: #cfe6ff;
}

.schedule-item:active {
  transform: translateY(0) scale(0.99);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.schedule-color-bar {
  width: 5px;
  flex-shrink: 0;
  border-radius: 0;
}

.schedule-time-section {
  flex-shrink: 0;
  border-right: 2px solid #e6f2ff;
  background: #f9fcff;
}

.schedule-time {
  min-width: 60px;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.time-text {
  font-size: 0.7rem;
  font-weight: 700;
  color: var(--theme-primary);
  white-space: nowrap;
  line-height: 1.3;
  text-align: center;
}

.time-separator {
  margin: 0 2px;
  opacity: 0.5;
}

.time-text.all-day {
  color: var(--theme-text-secondary);
  font-size: 0.65rem;
  font-weight: 600;
}

.time-text.time-specified {
  color: var(--theme-primary);
  font-size: 0.65rem;
  font-weight: 600;
}

.schedule-content {
  flex: 1;
  min-width: 0;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.schedule-title {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--theme-text-primary);
  line-height: 1.4;
  letter-spacing: -0.1px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 0;
}

.schedule-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: var(--theme-text-primary);
  font-weight: 600;
}

.info-item i {
  font-size: 0.85rem;
  color: var(--theme-primary);
  opacity: 0.7;
}

.date-range {
  color: var(--theme-text-secondary);
  font-weight: 500;
}

.time-separator {
  margin: 0 4px;
  opacity: 0.5;
}

.schedule-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.schedule-type {
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 0.65rem;
  font-weight: 600;
  color: white;
  white-space: nowrap;
  line-height: 1.2;
}

.schedule-assignee,
.schedule-department {
  font-size: 0.7rem;
  color: var(--theme-text-secondary);
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.schedule-assignee i,
.schedule-department i {
  font-size: 0.75rem;
  opacity: 0.6;
}

.schedule-description {
  font-size: 0.75rem;
  color: var(--theme-text-secondary);
  line-height: 1.5;
  margin-top: 2px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.schedule-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 12px 12px 8px;
  color: var(--theme-text-muted);
  font-size: 18px;
  flex-shrink: 0;
  transition: transform 0.2s ease, color 0.2s ease;
}

.schedule-item:hover .schedule-arrow {
  transform: translateX(4px);
  color: var(--theme-primary);
}

@media (min-width: 651px) {
  .schedule-day-detail {
    max-width: 600px;
    margin: 0 auto;
  }
}
</style>
