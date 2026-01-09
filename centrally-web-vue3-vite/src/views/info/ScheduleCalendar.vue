<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle
        :title="$t('info.schedule.title')"
        subtitle="Schedule Calendar"
        icon="ri-calendar-line"
        desktopMarginBottom="20px"
      />

      <!-- 캘린더 뷰 -->
      <div v-show="currentView === 'month'" class="calendar-wrapper">
        <!-- 상단 컨트롤 영역 -->
        <div class="calendar-header">
          <!-- 월 네비게이션 및 일정 등록 -->
          <div class="calendar-navigation">
            <button
              type="button"
              class="nav-btn prev-month"
              @click="prevMonth"
              :title="$t('info.schedule.navigation.prevMonth')"
            >
              <i class="ri-arrow-left-s-line"></i>
            </button>
            <div class="current-month">
              <span class="month-year">{{ currentMonthYear }}</span>
            </div>
            <button
              type="button"
              class="nav-btn next-month"
              @click="nextMonth"
              :title="$t('info.schedule.navigation.nextMonth')"
            >
              <i class="ri-arrow-right-s-line"></i>
            </button>
            <button
              type="button"
              class="nav-btn today-btn"
              @click="goToToday"
              :title="$t('info.schedule.navigation.today')"
            >
              {{ $t('info.schedule.navigation.today') }}
            </button>
            <div class="view-section">
              <button
                type="button"
                class="view-toggle-btn"
                :class="{ active: currentView === 'month' }"
                @click="switchView('month')"
                :title="$t('info.schedule.views.monthView')"
              >
                <i class="ri-calendar-line"></i>
              </button>
              <button
                type="button"
                class="view-toggle-btn"
                :class="{ active: currentView === 'list' }"
                @click="switchView('list')"
                :title="$t('info.schedule.views.listView')"
              >
                <i class="ri-list-check"></i>
              </button>
            </div>
            <button
              v-if="canCreate"
              type="button"
              class="create-btn"
              @click="showCreateModal"
            >
              <i class="ri-add-line"></i>
              <span>{{ $t('info.schedule.buttons.create') }}</span>
            </button>
          </div>

          <!-- 필터 및 뷰 선택 -->
          <div class="calendar-controls">
            <DefaultFormRow gap="10px" align="between">
              <DefaultSelect
                v-model="selectedDepartmentIdForSelect"
                :options="departmentOptions"
                @change="handleDepartmentChange"
                size="large"
              />
              <DefaultTextfield
                v-model="assigneeSearchKeyword"
                :placeholder="$t('info.schedule.filters.selectAssignee') || '담당자 검색'"
                :size="isMobile ? 'small' : 'large'"
              />
            </DefaultFormRow>
          </div>
        </div>

        <ScheduleMonthView
          ref="monthViewRef"
          :schedules="filteredSchedules"
          :currentDate="currentDate"
          :canEditOrDelete="canEditOrDelete"
          @update:currentDate="currentDate = $event"
          @monthChanged="handleMonthChanged"
          @create="showCreateModal"
          @edit="handleEditModal"
          @delete="showDeleteConfirm"
          @updateSchedule="loadSchedules"
          @reload="loadSchedules"
        />
      </div>

      <!-- 리스트 뷰 -->
      <div v-show="currentView === 'list'" class="list-view-wrapper">
        <!-- 상단 컨트롤 영역 -->
        <div class="calendar-header">
          <!-- 월 네비게이션 및 일정 등록 -->
          <div class="calendar-navigation">
            <button
              type="button"
              class="nav-btn prev-month"
              @click="prevMonth"
              :title="$t('info.schedule.navigation.prevMonth')"
            >
              <i class="ri-arrow-left-s-line"></i>
            </button>
            <div class="current-month">
              <span class="month-year">{{ currentMonthYear }}</span>
            </div>
            <button
              type="button"
              class="nav-btn next-month"
              @click="nextMonth"
              :title="$t('info.schedule.navigation.nextMonth')"
            >
              <i class="ri-arrow-right-s-line"></i>
            </button>
            <button
              type="button"
              class="nav-btn today-btn"
              @click="goToToday"
              :title="$t('info.schedule.navigation.today')"
            >
              {{ $t('info.schedule.navigation.today') }}
            </button>
            <div class="view-section">
              <button
                type="button"
                class="view-toggle-btn"
                :class="{ active: currentView === 'month' }"
                @click="switchView('month')"
                :title="$t('info.schedule.views.monthView')"
              >
                <i class="ri-calendar-line"></i>
              </button>
              <button
                type="button"
                class="view-toggle-btn"
                :class="{ active: currentView === 'list' }"
                @click="switchView('list')"
                :title="$t('info.schedule.views.listView')"
              >
                <i class="ri-list-check"></i>
              </button>
            </div>
            <button
              v-if="canCreate"
              type="button"
              class="create-btn"
              @click="showCreateModal"
            >
              <i class="ri-add-line"></i>
              <span>{{ $t('info.schedule.buttons.create') }}</span>
            </button>
          </div>

        <!-- 필터 및 뷰 선택 -->
        <div class="calendar-controls">
          <DefaultFormRow gap="10px" align="between">
            <DefaultSelect
              v-model="selectedDepartmentIdForSelect"
              :options="departmentOptions"
              @change="handleDepartmentChange"
              size="large"
            />
            <DefaultTextfield
              v-model="assigneeSearchKeyword"
              :placeholder="$t('info.schedule.filters.selectAssignee') || '담당자 검색'"
              :size="isMobile ? 'small' : 'large'"
            />
          </DefaultFormRow>
        </div>

          <DefaultTable
            :columns="listColumns"
            :data="scheduleList"
            :mobileCard="true"
            cardVariant="schedule"
            :rowClick="(item) => handleEditModal(item)"
            :noDataMessage="$t('info.schedule.noSchedules')"
            :usePagination="true"
            :currentPage="currentPage"
            :totalPages="totalPages"
            :visiblePageCount="5"
            :minRows="9"
            :noDataImageHeight="430"
            @pageChange="handlePageChange"
          />
        </div>

        <!-- 일정 생성/수정 모달 (데스크톱만) -->
        <ScheduleModal
          v-if="!isMobile"
          :isVisible="isModalVisible"
          :schedule="selectedSchedule"
          :isCreate="isCreate"
          :canDelete="selectedSchedule ? canEditOrDelete(selectedSchedule) : false"
          :canEdit="selectedSchedule ? canEditOrDelete(selectedSchedule) : true"
          @close="handleModalClose"
          @save="handleSave"
          @delete="showDeleteConfirm"
        />

        <!-- 삭제 확인 AlertModal -->
        <AlertModal
          :isVisible="deleteConfirmVisible"
          :disableBackgroundClose="true"
          :title="$t('info.schedule.confirm.deleteTitle')"
          :confirmText="$t('common.button.confirm')"
          :cancelText="$t('common.button.cancel')"
          @close="deleteConfirmVisible = false"
          @confirm="handleDeleteConfirm"
        >
          <template #body>
            <div>
              <div>[{{ scheduleToDelete?.title || '' }}]</div>
              <div style="margin-top: 8px">
                {{ $t('info.schedule.confirm.deleteMessage') }}
              </div>
            </div>
          </template>
        </AlertModal>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter, useRoute } from 'vue-router';
import { useHrmStore } from '@/store/hrm';
import { useAuthStore } from '@/store/auth';
import ScheduleApi from '@/api/info/ScheduleApi';
import PageTitle from '@/components/common/title/PageTitle.vue';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import DefaultTable from '@/components/common/table/DefaultTable.vue';
import ScheduleModal from '@/components/info/ScheduleModal.vue';
import ScheduleMonthView from '@/components/info/ScheduleMonthView.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import { fetchScheduleTypes, getScheduleTypeLabelSync } from '@/constants/infoConstants';
import { toast } from 'vue3-toastify';
import { useScheduleFilter } from '@/composables/info/useScheduleFilter';
import { useScheduleModal } from '@/composables/info/useScheduleModal';
import { useViewStateStore } from '@/store/viewState';
import { ROUTES } from '@/config/menuConfig';

const { t, locale } = useI18n();
const router = useRouter();
const route = useRoute();
const hrmStore = useHrmStore();
const authStore = useAuthStore();
const viewState = useViewStateStore();

// Composables 사용
const {
  selectedDepartmentId,
  selectedAssigneeId,
  departments,
  schedules,
  loadDepartments,
  loadSchedules: loadSchedulesFromFilter,
  onDepartmentChange,
  onUserSelected,
  resetFilter,
} = useScheduleFilter();

const {
  isModalVisible,
  selectedSchedule,
  isCreate,
  deleteConfirmVisible,
  scheduleToDelete,
  showCreateModal: showCreateModalOriginal,
  showEditModal,
  closeModal,
  showDeleteConfirm,
  closeDeleteConfirm,
} = useScheduleModal();

/**
 * 일정 생성 모달 열기 (모바일 체크 포함)
 */
function showCreateModal() {
  // 모바일일 때는 모바일 페이지로 이동
  if (isMobile.value) {
    // 히스토리 저장
    viewState.saveState('scheduleCalendar', {
      selectedDepartmentId: selectedDepartmentId.value,
      assigneeSearchKeyword: assigneeSearchKeyword.value,
      currentPage: currentPage.value,
      currentDate: currentDate.value.toISOString(),
      currentView: currentView.value,
      scrollY: window.scrollY,
    });
    
    router.push(ROUTES.INFO.SCHEDULE_CREATE_MOBILE);
    return;
  }
  
  // 데스크톱일 때는 모달 표시
  showCreateModalOriginal();
}

const monthViewRef = ref(null);
const scheduleTypes = ref([]);

const currentView = ref('month');
const currentDate = ref(new Date());

// 리스트 뷰 페이징 관련
const MOBILE_BP = 650;
const MOBILE_PAGE_SIZE = 4;
const DESKTOP_PAGE_SIZE = 9;

const isMobile = ref(window.innerWidth <= MOBILE_BP);
const currentPage = ref(1);
const itemsPerPage = computed(() =>
  isMobile.value ? MOBILE_PAGE_SIZE : DESKTOP_PAGE_SIZE,
);

function updateIsMobile() {
  isMobile.value = window.innerWidth <= MOBILE_BP;
}

/**
 * 모바일/데스크톱 전환 시 페이지 리셋
 */
watch(isMobile, (v, prev) => {
  // 화면 모드가 실제로 변한 경우: 리스트 뷰일 때 페이지 초기화
  if (v !== prev && currentView.value === 'list') {
    currentPage.value = 1;
    loadSchedules();
  }
});

// 담당자 검색어
const assigneeSearchKeyword = ref('');

// 부서 선택을 위한 computed (null을 빈 문자열로 변환)
const selectedDepartmentIdForSelect = computed({
  get: () => {
    return selectedDepartmentId.value === null ? '' : selectedDepartmentId.value;
  },
  set: (value) => {
    selectedDepartmentId.value = value === '' ? null : value;
  },
});

/**
 * 현재 월/년도 표시
 */
const currentMonthYear = computed(() => {
  const year = currentDate.value.getFullYear();
  const month = currentDate.value.getMonth() + 1;
  return t('info.schedule.monthYearFormat', { year, month });
});

/**
 * 이전 달로 이동
 */
async function prevMonth() {
  if (currentView.value === 'month' && monthViewRef.value) {
    monthViewRef.value.prevMonth();
  } else {
    // 리스트 모드일 때는 currentDate를 직접 업데이트
    const newDate = new Date(currentDate.value);
    newDate.setMonth(newDate.getMonth() - 1);
    const normalizedDate = new Date(newDate.getFullYear(), newDate.getMonth(), 1);
    currentDate.value = normalizedDate;
    // 페이징 리셋
    resetPagination();
    // 변경된 날짜로 데이터 재조회
    console.log('prevMonth - calling loadSchedulesFromFilter with date:', normalizedDate);
    try {
      await loadSchedulesFromFilter(() => {
        // 리스트 뷰에서는 별도 처리 불필요
      }, normalizedDate);
    } catch (error) {
      console.error('prevMonth loadSchedulesFromFilter error:', error);
    }
  }
}

/**
 * 다음 달로 이동
 */
async function nextMonth() {
  if (currentView.value === 'month' && monthViewRef.value) {
    monthViewRef.value.nextMonth();
  } else {
    // 리스트 모드일 때는 currentDate를 직접 업데이트
    const newDate = new Date(currentDate.value);
    newDate.setMonth(newDate.getMonth() + 1);
    const normalizedDate = new Date(newDate.getFullYear(), newDate.getMonth(), 1);
    currentDate.value = normalizedDate;
    // 페이징 리셋
    resetPagination();
    // 변경된 날짜로 데이터 재조회
    console.log('nextMonth - calling loadSchedulesFromFilter with date:', normalizedDate);
    try {
      await loadSchedulesFromFilter(() => {
        // 리스트 뷰에서는 별도 처리 불필요
      }, normalizedDate);
    } catch (error) {
      console.error('nextMonth loadSchedulesFromFilter error:', error);
    }
  }
}

/**
 * 오늘로 이동
 */
async function goToToday() {
  if (currentView.value === 'month' && monthViewRef.value) {
    monthViewRef.value.goToToday();
  } else {
    // 리스트 모드일 때는 currentDate를 직접 업데이트
    const today = new Date();
    const normalizedDate = new Date(today.getFullYear(), today.getMonth(), 1);
    currentDate.value = normalizedDate;
    // 페이징 리셋
    resetPagination();
    // 변경된 날짜로 데이터 재조회
    console.log('goToToday - calling loadSchedulesFromFilter with date:', normalizedDate);
    try {
      await loadSchedulesFromFilter(() => {
        // 리스트 뷰에서는 별도 처리 불필요
      }, normalizedDate);
    } catch (error) {
      console.error('goToToday loadSchedulesFromFilter error:', error);
    }
  }
}
// 상태는 composables에서 관리됨

/**
 * 일정 생성 권한 체크 (INFO_USER 이상)
 */
const canCreate = computed(() => {
  const roles = authStore.getRoles || [];
  return (
    roles.includes('ROLE_INFO_USER') ||
    roles.includes('ROLE_INFO_MANAGER') ||
    roles.includes('ROLE_INFO_ADMIN') ||
    roles.includes('ROLE_GATE_SYSTEM')
  );
});

/**
 * 일정 수정/삭제 권한 체크
 * INFO_MANAGER 또는 INFO_ADMIN 권한이 있거나, 내 소유인 경우에만 수정/삭제 가능
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

  return (
    myUserId && scheduleAssigneeId && String(myUserId) === String(scheduleAssigneeId)
  );
}

// 모달 관련 함수는 composables/info/useScheduleModal.js에서 import됨

// hexToRgba는 utils/scheduleUtils.js에서 import됨

/**
 * 부서 옵션
 */
const departmentOptions = computed(() => {
  const options = [{ value: '', label: t('info.schedule.filters.all') || '전체' }];

  if (hrmStore.myProfile?.departmentId) {
    options.push({
      value: hrmStore.myProfile.departmentId,
      label: `${hrmStore.myProfile.departmentName || ''}  ⭐`,
    });
  }

  departments.value.forEach((dept) => {
    if (dept.departmentId !== hrmStore.myProfile?.departmentId) {
      options.push({
        value: dept.departmentId,
        label: dept.departmentName,
      });
    }
  });

  return options;
});

/**
 * 리스트 뷰용 컬럼 정의
 */
const listColumns = computed(() => [
  {
    key: 'assigneeName',
    label: t('info.schedule.labels.assignee'),
    width: 150,
    mobile: {
      line: 1,
      inline: true,
      prefix: '',
      suffix: '',
      bold: true,
    },
  },
  {
    key: 'departmentName',
    label: t('info.schedule.labels.department'),
    width: 150,
    mobile: {
      line: 1,
      inline: true,
      prefix: '\u00a0[',
      suffix: ']',
      bold: false,
      align: 'right',
    },
  },
  {
    key: 'title',
    label: t('info.schedule.labels.title'),
    width: 'auto',
    minWidth: 150,
    mobile: {
      dividerTop: true,
      dividerTopGapAbove: '5px',
      dividerTopGapBelow: '15px',
      line: 2,
      inline: false,
      prefix: `📝\u00a0${t('info.schedule.labels.title')}\u00a0:\u00a0`,
      suffix: '',
      bold: false,
    },
  },
  {
    key: 'scheduleType',
    label: t('info.schedule.labels.scheduleType'),
    width: 150,
    customValue: (item) => getScheduleTypeLabelSync(item.scheduleTypeInfo?.code || '', scheduleTypes.value, item.scheduleTypeInfo),
    mobile: {
      line: 3,
      inline: false,
      prefix: `📅\u00a0일정\u00a0:\u00a0`,
      suffix: '',
      bold: false,
    },
  },
  {
    key: 'startDate',
    label: t('info.schedule.labels.startDate'),
    width: 110,
    align: 'center',
    customValue: (item) => {
      if (!item.startDate) return '';
      return new Date(item.startDate + 'T00:00:00').toLocaleDateString('ko-KR').replace(/\.$/, '');
    },
    mobile: {
      line: 4,
      inline: false,
      prefix: `📆\u00a0기간\u00a0:\u00a0`,
      suffix: '',
      bold: false,
    },
  },
  {
    key: 'endDate',
    label: t('info.schedule.labels.endDate'),
    width: 110,
    align: 'center',
    customValue: (item) => {
      if (!item.endDate) return '';
      return new Date(item.endDate + 'T00:00:00').toLocaleDateString('ko-KR').replace(/\.$/, '');
    },
    mobile: {
      hidden: true,
    },
  },
  {
    key: 'regTime',
    label: t('info.schedule.labels.createdDate'),
    width: 110,
    align: 'center',
    customValue: (item) => {
      if (!item.regTime) return '';
      const date = new Date(item.regTime);
      return date.toLocaleDateString('ko-KR').replace(/\.$/, '');
    },
    mobile: {
      hidden: true,
    },
  },
]);

/**
 * 담당자 필터가 적용된 일정 목록 (캘린더 및 리스트 공통)
 */
const filteredSchedules = computed(() => {
  let filtered = schedules.value;

  // 담당자 검색어로 필터링 (실시간 검색)
  if (assigneeSearchKeyword.value && assigneeSearchKeyword.value.trim()) {
    const keyword = assigneeSearchKeyword.value.trim().toLowerCase();
    filtered = filtered.filter((schedule) => {
      const assigneeName = (
        schedule.assigneeName ||
        schedule.assignee?.name ||
        schedule.assignee?.userName ||
        ''
      ).toLowerCase();
      return assigneeName.includes(keyword);
    });
  }

  return filtered;
});

/**
 * 리스트 뷰용 데이터 변환 (페이징 적용)
 */
const scheduleList = computed(() => {
  const allSchedules = filteredSchedules.value.map((schedule) => ({
    ...schedule,
    scheduleType: schedule.scheduleTypeInfo?.code || 'OTHER',
  }));

  // 페이징 처리
  const startIndex = (currentPage.value - 1) * itemsPerPage.value;
  const endIndex = startIndex + itemsPerPage.value;
  return allSchedules.slice(startIndex, endIndex);
});

/**
 * 전체 페이지 수 계산 (담당자 필터 적용)
 */
const totalPages = computed(() => {
  return Math.ceil(filteredSchedules.value.length / itemsPerPage.value);
});

/**
 * 페이지 변경 핸들러
 */
function handlePageChange(page) {
  currentPage.value = page;
  // 모바일일 때 페이지 변경 시 상단으로 스크롤
  if (isMobile.value) {
    window.scrollTo(0, 0);
  }
}

/**
 * 월 변경 시 첫 페이지로 리셋
 */
function resetPagination() {
  currentPage.value = 1;
}

// 필터링 관련 함수는 composables/useScheduleFilter.js에서 import됨
// loadSchedules는 updateCalendar 콜백을 포함하도록 래핑
async function loadSchedules() {
  await loadSchedulesFromFilter(() => {
    if (currentView.value === 'month' && monthViewRef.value) {
      // 데이터 로드 후 캘린더 업데이트
      nextTick(() => {
        if (monthViewRef.value) {
          monthViewRef.value.updateCalendar();
        }
      });
    }
  }, currentDate.value);
}

/**
 * 달력 월 변경 핸들러
 */
async function handleMonthChanged(newDate) {
  if (currentView.value === 'month') {
    // newDate가 Date 객체인지 확인하고, 해당 월의 1일로 정규화
    const dateObj = newDate instanceof Date ? new Date(newDate) : new Date(newDate);
    const normalizedDate = new Date(dateObj.getFullYear(), dateObj.getMonth(), 1);

    // currentDate 업데이트
    currentDate.value = normalizedDate;

    // 해당 월의 데이터 로드
    await loadSchedulesFromFilter(async () => {
      // 데이터 로드 완료 후 캘린더 업데이트
      // schedules watch가 자동으로 updateCalendar를 호출하지만,
      // 확실하게 하기 위해 명시적으로 호출
      await nextTick();
      if (monthViewRef.value) {
        // 캘린더 날짜 업데이트 (요일 표시를 위해)
        if (monthViewRef.value.setDate) {
          monthViewRef.value.setDate(normalizedDate);
        }
        // 데이터 업데이트
        if (monthViewRef.value.updateCalendar) {
          monthViewRef.value.updateCalendar();
        }
      }
    }, normalizedDate);

    // 데이터가 완전히 반영될 때까지 대기 후 캘린더 업데이트
    // watch가 트리거되지만, 명시적으로 한 번 더 호출하여 확실하게 함
    await nextTick();
    setTimeout(() => {
      if (monthViewRef.value) {
        // 캘린더 날짜 업데이트
        if (monthViewRef.value.setDate) {
          monthViewRef.value.setDate(normalizedDate);
        }
        // 데이터 업데이트
        if (monthViewRef.value.updateCalendar) {
          monthViewRef.value.updateCalendar();
        }
      }
    }, 100);
  }
}

// currentDate가 변경될 때 일정 데이터 다시 로드 (초기 로드 시에만)
watch(
  currentDate,
  async (newDate) => {
    // monthChanged 이벤트로 처리되므로 여기서는 초기 로드만 처리
  },
  { deep: false },
);

// 캘린더 관련 함수들은 ScheduleMonthView 컴포넌트로 이동됨

/**
 * 뷰 전환
 */
async function switchView(view) {
  currentView.value = view;
  if (view === 'month') {
    await nextTick();
    if (monthViewRef.value) {
      // 캘린더 인스턴스가 없으면 초기화
      if (
        !monthViewRef.value.hasCalendarInstance ||
        !monthViewRef.value.hasCalendarInstance()
      ) {
        await monthViewRef.value.initCalendar();
      }
      // 항상 날짜를 설정하여 동기화 보장
      if (monthViewRef.value.setDate && currentDate.value) {
        await monthViewRef.value.setDate(currentDate.value);
      }
      // 해당 월의 데이터 로드
      await loadSchedules();
    }
  } else if (view === 'list') {
    // 리스트 뷰로 전환 시 테이블이 완전히 렌더링된 후 리사이즈 이벤트 트리거
    await nextTick();
    await nextTick();
    window.dispatchEvent(new Event('resize'));
  }
}

/**
 * 부서 필터 변경
 */
// 필터 관련 함수는 composables/info/useScheduleFilter.js에서 import됨
// onDepartmentChange는 래핑 필요
async function handleDepartmentChange(value) {
  // null이거나 undefined면 null로 설정, 그 외에는 숫자로 변환
  if (value === null || value === undefined || value === '') {
    onDepartmentChange(null);
  } else if (
    typeof value === 'string' &&
    (value === '전체' || value === t('info.schedule.filters.all'))
  ) {
    onDepartmentChange(null);
  } else {
    onDepartmentChange(typeof value === 'number' ? value : parseInt(value));
  }
  // 필터 변경 시 상태 저장
  viewState.saveState('scheduleCalendar', {
    selectedDepartmentId: selectedDepartmentId.value,
    assigneeSearchKeyword: assigneeSearchKeyword.value,
    currentPage: currentPage.value,
    currentDate: currentDate.value.toISOString(),
    scrollY: window.scrollY,
  });
  // 필터 변경 시 데이터 재조회 및 페이징 리셋
  resetPagination();
  await loadSchedules();
}

/**
 * 담당자 검색어 변경 시 페이징 리셋 및 상태 저장
 */
watch(assigneeSearchKeyword, () => {
  resetPagination();
  // 필터 변경 시 상태 저장
  viewState.saveState('scheduleCalendar', {
    selectedDepartmentId: selectedDepartmentId.value,
    assigneeSearchKeyword: assigneeSearchKeyword.value,
    currentPage: currentPage.value,
    currentDate: currentDate.value.toISOString(),
    scrollY: window.scrollY,
  });
});

/**
 * 일정 수정 모달 열기 (히스토리 저장 포함)
 */
function handleEditModal(item) {
  // 모바일일 때는 모바일 페이지로 이동
  if (isMobile.value) {
    // 히스토리 저장
    viewState.saveState('scheduleCalendar', {
      selectedDepartmentId: selectedDepartmentId.value,
      assigneeSearchKeyword: assigneeSearchKeyword.value,
      currentPage: currentPage.value,
      currentDate: currentDate.value.toISOString(),
      currentView: currentView.value,
      scrollY: window.scrollY,
    });
    
    // 일정 정보를 sessionStorage에 저장
    sessionStorage.setItem('scheduleEdit', JSON.stringify({
      scheduleId: item.scheduleId || item.id,
      canEdit: canEditOrDelete(item),
      canDelete: canEditOrDelete(item),
    }));
    
    router.push(ROUTES.INFO.SCHEDULE_EDIT_MOBILE);
    return;
  }
  
  // 히스토리 저장
  viewState.saveState('scheduleCalendar', {
    selectedDepartmentId: selectedDepartmentId.value,
    assigneeSearchKeyword: assigneeSearchKeyword.value,
    currentPage: currentPage.value,
    currentDate: currentDate.value.toISOString(),
    scrollY: window.scrollY,
  });

  showEditModal(item);
}

/**
 * 모달 닫기 처리 (more 팝업도 함께 닫기)
 */
function handleModalClose() {
  // more 팝업과 상세 팝업 모두 닫기
  if (monthViewRef.value?.closeAllPopups) {
    monthViewRef.value.closeAllPopups();
  }

  closeModal();
}

/**
 * 일정 저장 처리
 */
async function handleSave() {
  await loadSchedules();

  // 수정 완료 시 more 팝업과 상세 팝업 모두 닫기
  if (monthViewRef.value?.closeAllPopups) {
    monthViewRef.value.closeAllPopups();
  }

  closeModal();
}

/**
 * 삭제 확인 처리
 */
async function handleDeleteConfirm() {
  if (!scheduleToDelete.value) return;

  try {
    const scheduleId = scheduleToDelete.value.scheduleId || scheduleToDelete.value.id;
    await ScheduleApi.deleteSchedule(scheduleId);
    toast.success(t('info.schedule.success.delete'));

    // 상세 팝업과 more 팝업 모두 닫기
    if (monthViewRef.value?.closeAllPopups) {
      monthViewRef.value.closeAllPopups();
    }

    // 모달 닫기
    closeModal();

    await loadSchedules();

    // 삭제 후 현재 페이지가 총 페이지 수보다 크면 마지막 페이지로 이동
    if (currentView.value === 'list' && totalPages.value > 0 && currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value;
    }
  } catch (error) {
    toast.error(t('info.schedule.error.deleteFailed'));
  } finally {
    deleteConfirmVisible.value = false;
    scheduleToDelete.value = null;
  }
}

// 캘린더 관련 함수들은 ScheduleMonthView 컴포넌트로 이동됨

/**
 * 컴포넌트 마운트
 */
onMounted(async () => {
  scheduleTypes.value = await fetchScheduleTypes();
  window.addEventListener('resize', updateIsMobile);
  updateIsMobile();

  // URL 쿼리 파라미터에서 뷰 확인
  if (route.query.view === 'list') {
    currentView.value = 'list';
  }

  // 히스토리 복원
  const saved = viewState.getState('scheduleCalendar');
  const restore = viewState.canRestore('scheduleCalendar');

  if (restore && saved) {
    // 부서 선택 복원 (null 포함하여 명시적으로 복원)
    if ('selectedDepartmentId' in saved) {
      selectedDepartmentId.value = saved.selectedDepartmentId;
    }
    // 담당자 검색어 복원
    if (saved.assigneeSearchKeyword !== undefined) {
      assigneeSearchKeyword.value = saved.assigneeSearchKeyword;
    }
    // 현재 페이지 복원
    if (saved.currentPage !== undefined) {
      currentPage.value = saved.currentPage;
    }
    // 현재 날짜 복원
    if (saved.currentDate) {
      currentDate.value = new Date(saved.currentDate);
    }
    // 현재 뷰 복원
    if (saved.currentView) {
      currentView.value = saved.currentView;
    }

    await loadDepartments();
    // loadDepartments 후에도 복원된 값 유지 (null인 경우 "전체" 유지)
    if ('selectedDepartmentId' in saved) {
      selectedDepartmentId.value = saved.selectedDepartmentId;
    }
    await loadSchedules();

    // 삭제 후 복원 시 페이지 조정 (리스트 뷰일 경우)
    await nextTick();
    if (currentView.value === 'list' && totalPages.value > 0 && currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value;
      viewState.saveState('scheduleCalendar', {
        selectedDepartmentId: selectedDepartmentId.value,
        assigneeSearchKeyword: assigneeSearchKeyword.value,
        currentPage: currentPage.value,
        currentDate: currentDate.value.toISOString(),
        currentView: currentView.value,
        scrollY: saved.scrollY ?? 0,
      });
    }

    // 스크롤 위치 복원
    requestAnimationFrame(() => {
      window.scrollTo(0, saved.scrollY ?? 0);
    });

    // 데이터 로드 후 캘린더가 자동으로 업데이트됨 (ScheduleMonthView의 watch에서 처리)
    // 하지만 확실하게 하기 위해 업데이트 호출
    if (currentView.value === 'month') {
      await nextTick();
      if (monthViewRef.value && monthViewRef.value.updateCalendar) {
        monthViewRef.value.updateCalendar();
      }
    }
  } else {
    // 일반 입장: 저장 상태 무시하고 초기화 후 첫 페이지 조회
    viewState.clearState('scheduleCalendar');

    await loadDepartments();
    await loadSchedules();

    // 데이터 로드 후 캘린더가 자동으로 업데이트됨 (ScheduleMonthView의 watch에서 처리)
    // 하지만 확실하게 하기 위해 업데이트 호출
    if (currentView.value === 'month') {
      await nextTick();
      if (monthViewRef.value && monthViewRef.value.updateCalendar) {
        monthViewRef.value.updateCalendar();
      }
    }
  }

  // 상세 모달 및 일자 상세 페이지에서 복원 허용
  viewState.allowRestoreFrom('scheduleCalendar', ['ScheduleModal', 'ScheduleDayDetail', 'ScheduleEditMobile', 'ScheduleCreateMobile']);
});

/**
 * 컴포넌트 언마운트
 */
onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile);
  // 캘린더 인스턴스 정리는 ScheduleMonthView에서 처리됨
});
</script>

<style scoped>
.today-btn {
  height: 32px;
}

/* 모바일에서 필터 섹션 한 줄로 표시 */
@media (max-width: 650px) {
  .calendar-controls .default-form-row {
    flex-wrap: nowrap !important;
  }
  
  .calendar-controls .default-form-row > *:first-child {
    flex: 0 0 auto;
    min-width: 120px;
  }
  
  .calendar-controls .default-form-row > *:last-child {
    flex: 0 0 auto;
    min-width: 0;
    margin-left: auto;
  }
}
</style>
