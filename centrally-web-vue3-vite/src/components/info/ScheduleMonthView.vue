<template>
  <div ref="calendarContainer" class="calendar-container"></div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { toast } from 'vue3-toastify';
import {
  fetchScheduleTypes,
  getScheduleTypeLabelSync,
  getScheduleTypeColorSync,
} from '@/constants/infoConstants';
import { hexToRgba } from '@/utils/info/scheduleUtils';
import { useScheduleUpdate } from '@/composables/info/useScheduleUpdate';
import { ROUTE_NAMES } from '@/config/routeConfig';

const props = defineProps({
  schedules: {
    type: Array,
    required: true,
  },
  currentDate: {
    type: Date,
    default: () => new Date(),
  },
  canEditOrDelete: {
    type: Function,
    default: () => false,
  },
});

const emit = defineEmits([
  'update:currentDate',
  'create',
  'edit',
  'delete',
  'updateSchedule',
  'reload',
  'monthChanged',
]);

const { t, locale } = useI18n();
const router = useRouter();

const calendarContainer = ref(null);
let calendarInstance = null;

// 일정 유형 목록
const scheduleTypes = ref([]);

// 모바일 감지
const isMobile = ref(window.innerWidth <= 650);

// 현재 클릭한 일정 저장 (상세 팝업에서 Edit 버튼 클릭 시 사용)
let currentClickedSchedule = null;

// Toast UI Calendar 동적 import
let Calendar = null;
let calendarCssLoaded = false;

/**
 * 캘린더 라이브러리 로드
 */
async function loadCalendar() {
  if (!Calendar) {
    try {
      const calendarModule = await import('@toast-ui/calendar');
      Calendar = calendarModule.default || calendarModule.Calendar || calendarModule;

      if (!calendarCssLoaded) {
        await import('@toast-ui/calendar/dist/toastui-calendar.min.css');
        calendarCssLoaded = true;
      }
    } catch (error) {
      toast.error(t('info.schedule.error.loadCalendarFailed'));
    }
  }
  return Calendar;
}

// schedules를 ref로 변환하여 useScheduleUpdate에 전달
const schedulesRef = computed(() => props.schedules);

// 일정 업데이트 composable 사용
const { handleScheduleUpdate } = useScheduleUpdate(
  schedulesRef,
  async () => {
    emit('reload');
  },
);

/**
 * 일정 ID 추출 (여러 형식 지원)
 * schedule 객체에서 scheduleId 또는 id를 추출하여 String으로 반환
 */
function extractScheduleId(schedule) {
  if (!schedule) {
    console.log('[extractScheduleId] schedule이 null 또는 undefined');
    return '';
  }
  
  // schedule.scheduleId || schedule.id 우선 체크 (일반적인 경우)
  if (schedule.scheduleId) {
    return String(schedule.scheduleId);
  }
  if (schedule.id) {
    return String(schedule.id);
  }
  // schedule.raw가 있는 경우 (Toast UI Calendar 이벤트 객체)
  if (schedule.raw) {
    if (schedule.raw.scheduleId) {
      return String(schedule.raw.scheduleId);
    }
    if (schedule.raw.id) {
      return String(schedule.raw.id);
    }
  }
  
  console.warn('[extractScheduleId] ID를 찾을 수 없음');
  return '';
}

/**
 * more 버튼을 + 아이콘으로 변경
 */
function convertMoreButtonsToPlusIcon() {
  if (!calendarContainer.value || window.innerWidth > 650) return;
  
  const moreButtons = calendarContainer.value.querySelectorAll(
    '.toastui-calendar-daygrid-more-events, .toastui-calendar-month-daygrid-more-events, .toastui-calendar-grid-cell-more-events, .toastui-calendar-weekday-grid-more-events'
  );
  
  moreButtons.forEach((button) => {
    // 이미 변환된 버튼도 top 값 업데이트
    if (button.classList.contains('converted-plus-icon')) {
      // 내부 span이 남아있으면 제거
      const spans = button.querySelectorAll('span');
      spans.forEach((span) => {
        span.remove();
      });
      // top 값을 일자 라인과 맞추기 (헤더 높이 31px 기준 중앙 정렬)
      button.style.setProperty('top', '6.5px', 'important');
      return;
    }
    
    // 내부 모든 요소 완전히 제거 (텍스트, 아이콘, span 등)
    while (button.firstChild) {
      button.removeChild(button.firstChild);
    }
    
    // 버튼 스타일 적용 (CSS의 ::after로 + 표시)
    // 헤더 높이 31px 기준으로 중앙 정렬: (31 - 14) / 2 = 8.5px
    button.style.cssText = `
      width: 14px !important;
      height: 14px !important;
      min-width: 14px !important;
      min-height: 14px !important;
      border-radius: 50% !important;
      background-color: #26a3ff !important;
      background: #26a3ff !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      position: absolute !important;
      right: 2px !important;
      top: 6.5px !important;
      z-index: 10 !important;
      padding: 0 !important;
      margin: 0 !important;
      border: none !important;
      cursor: pointer !important;
      font-size: 0 !important;
      line-height: 14px !important;
    `;
    
    button.classList.add('converted-plus-icon');
    
    // 주기적으로 span이 다시 생기면 제거
    setTimeout(() => {
      const spans = button.querySelectorAll('span');
      spans.forEach((span) => {
        span.remove();
      });
    }, 100);
  });
}

/**
 * 모바일에서 일자 셀 클릭 핸들러 설정
 */
function setupMobileDayClickHandlers() {
  if (!calendarContainer.value || window.innerWidth > 650) return;
  
  // 기존 핸들러 제거를 위해 새 핸들러 추가
  const cells = calendarContainer.value.querySelectorAll('.toastui-calendar-daygrid-cell');
  cells.forEach((cell) => {
    // 기존 클릭 핸들러가 있으면 제거
    const newHandler = (e) => {
      // 일정 막대나 more 버튼 클릭은 무시
      if (
        e.target.closest('.toastui-calendar-weekday-event') ||
        e.target.closest('.toastui-calendar-daygrid-more-events') ||
        e.target.closest('.toastui-calendar-month-more-events')
      ) {
        return;
      }
      
      // 날짜 추출
      let dateStr = 
        cell.getAttribute('data-date') ||
        cell.getAttribute('data-ymd') ||
        cell.querySelector('[data-date]')?.getAttribute('data-date');
      
      if (!dateStr) {
        // 셀의 텍스트에서 날짜 추출
        const dateText = cell.textContent?.trim();
        const dayNumber = parseInt(dateText);
        if (isNaN(dayNumber) || dayNumber < 1 || dayNumber > 31) return;
        
        // 현재 캘린더 날짜 가져오기
        const currentDate = calendarInstance ? calendarInstance.getDate() : new Date();
        const currentYear = currentDate.getFullYear();
        const currentMonth = currentDate.getMonth();
        
        // 셀 인덱스로 날짜 계산
        const allCells = Array.from(
          calendarContainer.value.querySelectorAll('.toastui-calendar-daygrid-cell')
        );
        const cellIndex = allCells.indexOf(cell);
        const firstDayOfMonth = new Date(currentYear, currentMonth, 1);
        const dayOfWeek = firstDayOfMonth.getDay();
        const actualDay = cellIndex - dayOfWeek + 1;
        
        if (actualDay <= 0) {
          // 이전 달
          const prevMonth = currentMonth === 0 ? 11 : currentMonth - 1;
          const prevYear = currentMonth === 0 ? currentYear - 1 : currentYear;
          const daysInPrevMonth = new Date(prevYear, prevMonth + 1, 0).getDate();
          const prevMonthDay = daysInPrevMonth + actualDay;
          dateStr = `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-${String(prevMonthDay).padStart(2, '0')}`;
        } else {
          const daysInCurrentMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
          if (actualDay > daysInCurrentMonth) {
            // 다음 달
            const nextMonth = currentMonth === 11 ? 0 : currentMonth + 1;
            const nextYear = currentMonth === 11 ? currentYear + 1 : currentYear;
            const nextMonthDay = actualDay - daysInCurrentMonth;
            dateStr = `${nextYear}-${String(nextMonth + 1).padStart(2, '0')}-${String(nextMonthDay).padStart(2, '0')}`;
          } else {
            // 현재 달
            dateStr = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(actualDay).padStart(2, '0')}`;
          }
        }
      }
      
      if (dateStr) {
        // 해당 날짜에 일정이 있는지 확인
        const hasSchedule = props.schedules.some((schedule) => {
          const startDate = schedule.startDate || schedule.raw?.startDate;
          const endDate = schedule.endDate || schedule.raw?.endDate;
          
          if (!startDate) return false;
          
          // 날짜 문자열을 Date 객체로 변환 (YYYY-MM-DD 형식)
          const targetDate = new Date(dateStr);
          const scheduleStartDate = new Date(startDate);
          const scheduleEndDate = endDate ? new Date(endDate) : scheduleStartDate;
          
          // 날짜만 비교 (시간 제외)
          targetDate.setHours(0, 0, 0, 0);
          scheduleStartDate.setHours(0, 0, 0, 0);
          scheduleEndDate.setHours(0, 0, 0, 0);
          
          // 해당 날짜가 일정 기간 내에 있는지 확인
          return targetDate >= scheduleStartDate && targetDate <= scheduleEndDate;
        });
        
        if (!hasSchedule) {
          // 일정이 없으면 토스트 팝업 표시
          toast.info(t('info.schedule.noSchedulesForDate', { date: dateStr }));
          return;
        }
        
        // 일정이 있으면 라우터로 이동
        // 필터 상태는 ScheduleCalendar에서 viewState에 저장되어 있으므로
        // ScheduleDayDetail에서 읽어서 사용함
        router.push(`/info/schedule-day-detail/${dateStr}`);
      }
    };
    
    // 기존 핸들러 제거 후 새 핸들러 추가
    cell.removeEventListener('click', cell._mobileClickHandler);
    cell._mobileClickHandler = newHandler;
    cell.addEventListener('click', newHandler);
  });
}

/**
 * 일정 ID로 일정 찾기
 */
function findScheduleById(scheduleId) {
  if (!scheduleId) return null;
  return props.schedules.find(
    (s) => String(s.scheduleId || s.id) === String(scheduleId),
  );
}

/**
 * 일정 제목으로 일정 찾기
 */
function findScheduleByTitle(title) {
  if (!title) return null;
  return props.schedules.find((s) => s.title === title);
}

/**
 * 일정 ID 또는 제목으로 일정 찾기
 */
function findSchedule(scheduleId, title) {
  if (scheduleId) {
    const found = findScheduleById(scheduleId);
    if (found) return found;
  }
  if (title) {
    return findScheduleByTitle(title);
  }
  return null;
}

/**
 * 부서·팀 정보 조합 텍스트 생성
 */
function getDeptTeamText(departmentName, teamName) {
  if (!departmentName && !teamName) return '';
  const parts = [];
  if (departmentName) parts.push(departmentName);
  if (teamName) parts.push(teamName);
  return ` [${parts.join(' · ')}]`;
}

/**
 * more 팝업인지 확인
 */
function isMorePopup(element) {
  if (!element) return false;
  return (
    element.classList?.toString().includes('see-more') ||
    element.classList?.toString().includes('more') ||
    element.closest('.toastui-calendar-see-more-container') ||
    element.closest('.toastui-calendar-see-more') ||
    element.closest('[class*="see-more"]') ||
    element.closest('[class*="more-events"]')
  );
}

/**
 * 팝업 요소 찾기
 */
function findPopupElement() {
  return (
    document.querySelector('.toastui-calendar-popup') ||
    document.querySelector('.toastui-calendar-popup-container') ||
    document.querySelector('.toastui-calendar-detail-container') ||
    document.querySelector('[class*="popup-container"]')
  );
}

/**
 * 팝업 닫기 버튼 찾기
 */
function findCloseButton(element) {
  if (!element) return null;
  return (
    element.querySelector('.toastui-calendar-popup-close') ||
    element.querySelector('.toastui-calendar-icon-close') ||
    element.querySelector('[class*="close"]') ||
    element.querySelector('button[aria-label*="close" i]')
  );
}

/**
 * 팝업을 닫는 함수 (close 버튼 클릭 또는 직접 숨기기)
 */
function closePopupElement(element) {
  if (!element || !element.style) return;
  
  const closeBtn = findCloseButton(element);
  if (closeBtn) {
    closeBtn.click();
  } else {
    element.style.display = 'none';
    element.style.visibility = 'hidden';
    element.style.opacity = '0';
    if (calendarInstance && calendarInstance.hideDetailPopup) {
      calendarInstance.hideDetailPopup();
    }
  }
}

/**
 * 현재 날짜 업데이트
 */
function updateCurrentDate() {
  if (!calendarInstance) return;
  try {
    // Toast UI Calendar의 getDate() 메서드 사용
    if (typeof calendarInstance.getDate === 'function') {
      const date = calendarInstance.getDate();
      emit('update:currentDate', new Date(date));
    } else {
      // getDate()가 없으면 현재 날짜 사용
      emit('update:currentDate', new Date());
    }
  } catch (error) {
    emit('update:currentDate', new Date());
  }
}

/**
 * 이전 달로 이동
 */
function handlePrevMonth() {
  if (calendarInstance) {
    calendarInstance.prev();
    const newDate = calendarInstance.getDate();
    const dateObj = new Date(newDate);
    emit('update:currentDate', dateObj);
    emit('monthChanged', dateObj);
  }
}

/**
 * 다음 달로 이동
 */
function handleNextMonth() {
  if (calendarInstance) {
    calendarInstance.next();
    const newDate = calendarInstance.getDate();
    const dateObj = new Date(newDate);
    emit('update:currentDate', dateObj);
    emit('monthChanged', dateObj);
  }
}

/**
 * 오늘로 이동
 */
function handleGoToToday() {
  if (calendarInstance) {
    calendarInstance.today();
    const newDate = calendarInstance.getDate();
    const dateObj = new Date(newDate);
    emit('update:currentDate', dateObj);
    emit('monthChanged', dateObj);
  }
}

/**
 * Toast UI Calendar 상세 팝업 닫기 (more 팝업은 유지)
 */
function closeDetailPopup() {
  // 상세 팝업 찾기 및 닫기
  const popup = findPopupElement();
  
  if (popup && !isMorePopup(popup)) {
    closePopupElement(popup);
  }

  // 추가로 모든 상세 팝업 컨테이너 찾아서 닫기 (more 팝업 제외)
  const allPopups = document.querySelectorAll(
    '.toastui-calendar-popup, .toastui-calendar-popup-container, .toastui-calendar-detail-container, [class*="popup-container"]',
  );
  allPopups.forEach((p) => {
    if (p && p.style && !isMorePopup(p)) {
      p.style.display = 'none';
      p.style.visibility = 'hidden';
      p.style.opacity = '0';
    }
  });
}

/**
 * 모든 팝업 닫기 (상세 팝업 + more 팝업)
 */
function closeAllPopups() {
  // 상세 팝업 닫기
  closeDetailPopup();

  // Toast UI Calendar 인스턴스의 메서드 사용 시도 (우선)
  if (calendarInstance) {
    try {
      // hideMoreEventsView 메서드가 있으면 사용
      if (typeof calendarInstance.hideMoreEventsView === 'function') {
        calendarInstance.hideMoreEventsView();
      }
      // 또는 다른 메서드 시도
      if (typeof calendarInstance.hidePopup === 'function') {
        calendarInstance.hidePopup();
      }
    } catch (e) {
      // 메서드가 없거나 실패하면 DOM 조작으로 처리
    }
  }

  // More 이벤트 팝업 닫기 - body 전체에서 찾기
  const morePopupSelectors = [
    '.toastui-calendar-see-more-container',
    '.toastui-calendar-see-more',
    '.toastui-calendar-month-more-list',
    '[class*="see-more-container"]',
    '[class*="month-more-list"]',
  ];

  for (const selector of morePopupSelectors) {
    const elements = document.querySelectorAll(selector);
    elements.forEach((element) => {
      if (!element || !element.style) return;

      const style = window.getComputedStyle(element);
      const display = style.display;
      const visibility = style.visibility;
      const opacity = parseFloat(style.opacity);

      // 실제로 보이는 팝업 레이어인지 확인
      // display가 none이 아니고, visibility가 hidden이 아니며, opacity가 0이 아니면 보이는 팝업
      const isVisible =
        display !== 'none' && visibility !== 'hidden' && opacity > 0;

      // 또는 캘린더 컨테이너 밖에 있으면 팝업으로 간주
      const isOutsideContainer =
        calendarContainer.value && !calendarContainer.value.contains(element);

      if (isVisible || isOutsideContainer) {
        // 먼저 닫기 버튼 클릭 시도
        const closeBtn =
          element.querySelector('.toastui-calendar-popup-close') ||
          element.querySelector('.toastui-calendar-icon-close') ||
          element.querySelector('[class*="close"]') ||
          element.querySelector('button[aria-label*="close" i]');

        if (closeBtn) {
          closeBtn.click();
        } else {
          // 버튼이 없으면 직접 숨기기
          element.style.display = 'none';
          element.style.visibility = 'hidden';
          element.style.opacity = '0';
        }
      }
    });
  }

  // 모든 팝업 컨테이너 찾아서 닫기 (더 확실하게)
  const allPopups = document.querySelectorAll(
    '.toastui-calendar-popup, .toastui-calendar-popup-container, .toastui-calendar-detail-container, [class*="popup"]',
  );
  allPopups.forEach((p) => {
    if (!p || !p.style) return;

    const style = window.getComputedStyle(p);
    const display = style.display;
    const visibility = style.visibility;

    // 캘린더 컨테이너 내부에 있고, 실제로 보이지 않는 요소는 스킵
    // (캘린더 셀의 "더보기" 텍스트 등)
    if (calendarContainer.value && calendarContainer.value.contains(p)) {
      // 캘린더 내부 요소는 position을 확인하여 팝업 레이어인지 판단
      const position = style.position;
      const zIndex = parseInt(style.zIndex) || 0;
      const isPopupLayer =
        (position === 'fixed' || position === 'absolute') &&
        zIndex >= 100 &&
        display !== 'none';

      if (!isPopupLayer) {
        return; // 팝업 레이어가 아니면 스킵
      }
    }

    // 실제로 보이는 팝업만 닫기
    if (display !== 'none' && visibility !== 'hidden') {
      p.style.display = 'none';
      p.style.visibility = 'hidden';
      p.style.opacity = '0';
    }
  });
}

/**
 * 리사이즈 핸들 표시 함수
 */
function showResizeHandles() {
  if (!calendarContainer.value) return;

  try {
    const allResizeHandles = calendarContainer.value.querySelectorAll(
      '.toastui-calendar-icon-handle-y, .toastui-calendar-ic-handle-y, [class*="handle-y"], [class*="resize-handle"]',
    );
    allResizeHandles.forEach((handle) => {
      handle.style.display = 'block';
      handle.style.visibility = 'visible';
      handle.style.opacity = '1';
      handle.style.cursor = 'ew-resize';
    });

    // 모든 일정 막대에 색상 적용
    const allWeekdayEvents = calendarContainer.value.querySelectorAll(
      '.toastui-calendar-weekday-event',
    );
    allWeekdayEvents.forEach((eventElem) => {
      // 제목으로 일정 찾기
      const titleElement =
        eventElem.querySelector('.toastui-calendar-weekday-event-title') ||
        eventElem.querySelector('.toastui-calendar-template-allday span');
      const scheduleTitle = titleElement?.textContent?.trim();

      // 제목으로 일정 찾기
      let schedule = findScheduleByTitle(scheduleTitle);

      // data 속성으로 일정 찾기
      if (!schedule) {
        const scheduleId =
          eventElem.getAttribute('data-schedule-id') || eventElem.getAttribute('data-id');
        if (scheduleId) {
          schedule = findScheduleById(scheduleId);
        }
      }

      if (schedule) {
        // 권한 체크: 수정 권한이 없으면 리사이즈 핸들 숨기기
        const hasEditPermission = props.canEditOrDelete(schedule);
        
        if (!hasEditPermission) {
          // 권한이 없으면 리사이즈 핸들 숨기기
          const resizeHandles = eventElem.querySelectorAll(
            '.toastui-calendar-icon-handle-y, .toastui-calendar-ic-handle-y, [class*="handle-y"], [class*="resize-handle"]',
          );
          resizeHandles.forEach((handle) => {
            handle.style.display = 'none';
            handle.style.visibility = 'hidden';
            handle.style.opacity = '0';
          });
        }

        const scheduleTypeCode = schedule.scheduleTypeInfo?.code || '';
        const scheduleColor =
          schedule.color || getScheduleTypeColorSync(scheduleTypeCode, scheduleTypes.value, schedule.scheduleTypeInfo);
        if (scheduleColor) {
          // 배경색 강제 적용 (투명도 70% 적용)
          const rgbaColor = hexToRgba(scheduleColor, 0.7);
          eventElem.style.setProperty(
            'background-color',
            rgbaColor || scheduleColor,
            'important',
          );
          eventElem.style.setProperty('border-left-color', scheduleColor, 'important');
          eventElem.style.setProperty('border-color', scheduleColor, 'important');
          eventElem.style.setProperty('color', '#ffffff', 'important');

          // 모든 자식 요소에도 색상 적용
          const allChildren = eventElem.querySelectorAll('span, div, *');
          allChildren.forEach((child) => {
            if (
              child.tagName === 'SPAN' ||
              child.tagName === 'DIV' ||
              child.tagName === 'I'
            ) {
              child.style.setProperty('color', '#ffffff', 'important');
            }
          });
        }
      }
    });
  } catch (error) {
    // 리사이즈 핸들 표시 실패 시 무시
  }
}

/**
 * 캘린더 업데이트
 */
function updateCalendar() {
  if (!calendarInstance) return;

  // props.currentDate가 변경되었으면 캘린더 날짜도 업데이트
  if (props.currentDate) {
    try {
      const propsDate = new Date(props.currentDate);
      let calendarDate;
      try {
        calendarDate = calendarInstance.getDate();
        calendarDate = calendarDate instanceof Date ? calendarDate : new Date(calendarDate);
      } catch (e) {
        calendarDate = new Date();
      }
      
      // 년/월이 다르면 캘린더 날짜를 먼저 업데이트
      if (
        calendarDate.getFullYear() !== propsDate.getFullYear() ||
        calendarDate.getMonth() !== propsDate.getMonth()
      ) {
        // 날짜를 설정 (setDate는 자동으로 렌더링함)
        calendarInstance.setDate(propsDate);
        // 날짜 변경 후 일정 업데이트를 위해 약간의 지연
        setTimeout(() => {
          if (calendarInstance) {
            calendarInstance.clear();
            addSchedulesToCalendar();
          }
        }, 100);
        return;
      }
    } catch (error) {
      // 날짜 업데이트 실패 시 무시
    }
  }

  calendarInstance.clear();
  addSchedulesToCalendar();
}

/**
 * 일정을 캘린더에 추가
 */
function addSchedulesToCalendar() {
  if (!calendarInstance) return;

  props.schedules.forEach((schedule) => {
    try {
      let start, end;

      // 백엔드에서 startDate (LocalDate)와 startTime (LocalDateTime)을 반환
      // 하지만 캘린더 표시에서는 시간 정보를 무시하고 날짜만 사용
      if (!schedule.startDate) {
        return;
      }

      // 시간 정보는 무시하고 날짜만 사용 (항상 00:00:00으로 설정)
      start = new Date(schedule.startDate + 'T00:00:00');
      start.setHours(0, 0, 0, 0);

      if (!schedule.endDate) {
        return;
      }

      // 시간 정보는 무시하고 날짜만 사용 (항상 23:59:59로 설정)
      end = new Date(schedule.endDate + 'T23:59:59');
      end.setHours(23, 59, 59, 999);

      // 유효하지 않은 날짜 체크
      if (isNaN(start.getTime()) || isNaN(end.getTime())) {
        return;
      }

      // Toast UI Calendar에서 월간 뷰에서 가로막대로 표시하려면:
      // - allday: 월간 뷰에서 상단에 가로막대로 표시됨 (드래그 가능)
      // - time: 월간 뷰에서는 점으로만 표시될 수 있음
      // 모든 일정을 가로막대로 보이게 하려면 allday 사용
      // 드래그 앤 드롭을 위해 모든 일정을 allday로 설정
      // 일정 유형 라벨 가져오기 (삭제된 일정 유형도 표시하기 위해 scheduleTypeInfo 사용)
      const scheduleTypeInfo = schedule.scheduleTypeInfo || null;
      const scheduleType = scheduleTypeInfo?.code || 'OTHER';
      const scheduleTypeLabel = getScheduleTypeLabelSync(scheduleType, scheduleTypes.value, scheduleTypeInfo);

      // 권한 체크: 수정/삭제 권한이 없으면 읽기 전용
      const hasEditPermission = props.canEditOrDelete(schedule);

      const eventData = {
        id: extractScheduleId(schedule),
        calendarId: 'schedule',
        title: schedule.title,
        category: 'allday', // 모든 일정을 allday로 설정하여 드래그 앤 드롭 활성화
        start: start,
        end: end,
        color: schedule.color || getScheduleTypeColorSync(scheduleType, scheduleTypes.value, scheduleTypeInfo),
        bgColor: schedule.color || getScheduleTypeColorSync(scheduleType, scheduleTypes.value, scheduleTypeInfo),
        borderColor: schedule.color || getScheduleTypeColorSync(scheduleType, scheduleTypes.value, scheduleTypeInfo),
        dragBgColor: schedule.color || getScheduleTypeColorSync(scheduleType, scheduleTypes.value, scheduleTypeInfo),
        isReadOnly: !hasEditPermission, // 권한이 없으면 읽기 전용 (드래그 불가)
        isPrivate: false,
        state: scheduleTypeLabel, // 상세 팝업 세 번째 아이템(서류가방 아이콘)에 표시
        location: '', // location은 빈 문자열로 설정 (첫 번째에 표시되지 않도록)
        body: schedule.description || '', // 상세 팝업 네 번째 아이템에 표시
        // 상세 팝업에 표시될 추가 정보
        assigneeName: schedule.assigneeName,
        departmentName: schedule.departmentName,
        description: schedule.description,
        raw: schedule, // 원본 일정 데이터 저장
        goingDuration: 0,
        comingDuration: 0,
        attendees: (() => {
          if (!schedule.assigneeName) return [];

          // 부서·팀 정보 조합
          const deptTeamText = getDeptTeamText(
            schedule.departmentName || '',
            schedule.teamName || schedule.team || ''
          );

          // 이름 + 부서·팀 형식으로 반환
          return [schedule.assigneeName + deptTeamText];
        })(),
      };

      calendarInstance.createEvents([eventData]);

      // 이벤트 생성 후 DOM 요소에 data 속성 추가 및 리사이즈 핸들 표시
      // 각 일정을 개별적으로 처리하여 정확한 매칭 보장
      setTimeout(() => {
        try {
          const scheduleId = extractScheduleId(schedule);
          const scheduleTitle = schedule.title.trim();
          const eventId = String(eventData.id);

          // Toast UI Calendar가 생성한 이벤트 요소 찾기
          // 이미 ID가 설정되지 않은 요소만 찾아서 설정
          let targetElement = null;

          // 모든 일정 요소 찾기
          const allEventElements = calendarContainer.value.querySelectorAll(
            '.toastui-calendar-weekday-event',
          );

          // 같은 제목의 일정이 여러 개 있을 수 있으므로, 정확하게 매칭
          // 1. 먼저 이미 ID가 설정되지 않은 요소만 필터링
          const elementsWithoutId = Array.from(allEventElements).filter((elem) => {
            const existingId =
              elem.getAttribute('data-schedule-id') || elem.getAttribute('data-id');
            return !existingId;
          });

          // 2. 제목이 일치하는 요소 찾기
          const matchingElements = elementsWithoutId.filter((elem) => {
            const titleElem = elem.querySelector('.toastui-calendar-weekday-event-title');
            const titleText = titleElem?.textContent?.trim();
            return titleText && titleText === scheduleTitle;
          });

          // 3. 같은 제목이 여러 개 있을 경우, 날짜 범위를 확인하여 정확하게 매칭
          if (matchingElements.length > 0) {
            // 날짜 범위로 정확하게 매칭 시도
            const startDate = new Date(schedule.startDate + 'T00:00:00');
            const endDate = new Date(schedule.endDate + 'T23:59:59');

            // 각 요소가 속한 셀의 날짜를 확인하여 가장 일치하는 요소 선택
            let bestMatch = null;
            let bestMatchScore = -1;

            for (const elem of matchingElements) {
              const cell = elem.closest('.toastui-calendar-daygrid-cell');
              if (cell) {
                // 셀의 날짜 정보 추출
                const allCells = Array.from(
                  calendarContainer.value.querySelectorAll('.toastui-calendar-daygrid-cell'),
                );
                const cellIndex = allCells.indexOf(cell);

                if (cellIndex >= 0) {
                  const now = new Date();
                  const currentYear = now.getFullYear();
                  const currentMonth = now.getMonth();
                  const firstDayOfMonth = new Date(currentYear, currentMonth, 1);
                  const dayOfWeek = firstDayOfMonth.getDay();
                  const cellDate = new Date(
                    currentYear,
                    currentMonth,
                    cellIndex - dayOfWeek + 1,
                  );

                  // 날짜가 일정 범위 내에 있는지 확인
                  if (cellDate >= startDate && cellDate <= endDate) {
                    // 일치도 계산 (시작일과의 거리)
                    const distance = Math.abs(cellDate.getTime() - startDate.getTime());
                    if (bestMatch === null || distance < bestMatchScore) {
                      bestMatch = elem;
                      bestMatchScore = distance;
                    }
                  }
                }
              }
            }

            // 가장 일치하는 요소 선택, 없으면 첫 번째 요소
            targetElement = bestMatch || matchingElements[0];
          } else if (matchingElements.length === 1) {
            targetElement = matchingElements[0];
          }

          // 찾은 요소에 ID 설정
          if (targetElement) {
            targetElement.setAttribute('data-schedule-id', scheduleId);
            targetElement.setAttribute('data-id', scheduleId);

            // 부모 요소에도 추가
            let parent = targetElement.parentElement;
            let depth = 0;
            while (parent && depth < 3) {
              parent.setAttribute('data-schedule-id', scheduleId);
              parent.setAttribute('data-id', scheduleId);
              parent = parent.parentElement;
              depth++;
            }

            // 리사이즈 핸들 찾아서 표시
            const eventContainer =
              targetElement.closest('.toastui-calendar-weekday-event') ||
              targetElement.closest('.toastui-calendar-weekday-event-block') ||
              targetElement;
            if (eventContainer) {
              const resizeHandles = eventContainer.querySelectorAll(
                '.toastui-calendar-icon-handle-y, .toastui-calendar-ic-handle-y, [class*="handle-y"], [class*="resize-handle"]',
              );
              resizeHandles.forEach((handle) => {
                handle.style.display = 'block';
                handle.style.visibility = 'visible';
                handle.style.opacity = '1';
                handle.style.cursor = 'ew-resize';
              });
              
              // 일정 막대 클릭 시 일정 저장 (Edit 버튼 클릭 시 사용)
              const clickHandler = (e) => {
                const clickedScheduleId = 
                  eventContainer.getAttribute('data-schedule-id') ||
                  eventContainer.getAttribute('data-id') ||
                  targetElement.getAttribute('data-schedule-id') ||
                  targetElement.getAttribute('data-id');
                
                if (clickedScheduleId) {
                  const clickedSchedule = findScheduleById(clickedScheduleId);
                  if (clickedSchedule) {
                    currentClickedSchedule = clickedSchedule;
                  }
                }
              };
              
              // 기존 핸들러 제거 후 새 핸들러 추가
              eventContainer.removeEventListener('click', eventContainer._scheduleClickHandler);
              eventContainer._scheduleClickHandler = clickHandler;
              eventContainer.addEventListener('click', clickHandler, true);

              // 일정 막대 색상 강제 적용 (투명도 80% 적용)
              const scheduleTypeCode = schedule.scheduleTypeInfo?.code || '';
              const scheduleColor =
                schedule.color || getScheduleTypeColorSync(scheduleTypeCode, scheduleTypes.value, schedule.scheduleTypeInfo);
              if (scheduleColor) {
                const rgbaColor = hexToRgba(scheduleColor, 0.8);
                eventContainer.style.setProperty(
                  'background-color',
                  rgbaColor || scheduleColor,
                  'important',
                );
                eventContainer.style.setProperty(
                  'border-left-color',
                  scheduleColor,
                  'important',
                );
                eventContainer.style.setProperty('color', '#ffffff', 'important');

                // 모든 자식 요소에도 색상 적용
                const allChildren = eventContainer.querySelectorAll('*');
                allChildren.forEach((child) => {
                  if (child.tagName === 'SPAN' || child.tagName === 'DIV') {
                    child.style.setProperty('color', '#ffffff', 'important');
                  }
                });
              }
            }
          }
        } catch (error) {
          // 일정 DOM 요소 설정 실패 시 무시
        }
      }, 50); // 각 일정마다 짧은 지연 후 처리
    } catch (error) {
      // 일정 추가 실패 시 무시
    }
  });

  // 모든 일정 추가 후 전체 처리
  nextTick(() => {
    setTimeout(() => {
      // 모든 리사이즈 핸들 강제 표시 및 색상 적용
      showResizeHandles();
      // 일정 막대 높이 조정하여 Toast UI Calendar가 올바르게 인식하도록 함
      if (window.innerWidth <= 650) {
        adjustEventHeights();
      }
    }, 200);
  });
}

/**
 * 일정 막대 높이를 조정하여 Toast UI Calendar가 올바르게 인식하도록 함
 */
function adjustEventHeights() {
  if (!calendarContainer.value || window.innerWidth > 650) return;
  
  // 각 날짜 셀의 일정 이벤트 컨테이너 확인
  const dayCells = calendarContainer.value.querySelectorAll('.toastui-calendar-daygrid-cell');
  dayCells.forEach((cell) => {
    const eventsContainer = cell.querySelector('.toastui-calendar-weekday-events');
    if (!eventsContainer) return;
    
    const eventBlocks = Array.from(eventsContainer.querySelectorAll('.toastui-calendar-weekday-event-block'));
    
    // 각 일정 블록의 높이 조정 및 위치 재계산
    let currentTop = 15; // 헤더 높이 (15px) 아래부터 시작
    const eventHeight = 14;
    const eventMargin = 1;
    const eventSpacing = 1; // 일정 블록 간 간격
    
    eventBlocks.forEach((block, index) => {
      const events = block.querySelectorAll('.toastui-calendar-weekday-event');
      
      // 각 일정의 높이를 14px로 강제 설정
      events.forEach((event) => {
        event.style.setProperty('height', '14px', 'important');
        event.style.setProperty('line-height', '14px', 'important');
        event.style.setProperty('min-height', '14px', 'important');
        event.style.setProperty('max-height', '14px', 'important');
      });
      
      // 일정 블록의 높이 계산 (일정 개수 × 일정 높이 + 마진)
      const blockHeight = events.length * (eventHeight + eventMargin * 2);
      const finalBlockHeight = Math.max(14, blockHeight);
      
      // 일정 블록의 높이와 위치 설정
      block.style.setProperty('min-height', `${finalBlockHeight}px`, 'important');
      block.style.setProperty('height', `${finalBlockHeight}px`, 'important');
      block.style.setProperty('top', `${currentTop}px`, 'important');
      
      // 다음 일정 블록의 위치 계산
      currentTop += finalBlockHeight + eventSpacing;
    });
    
    // 이벤트 컨테이너의 높이도 조정 (모든 일정 블록을 포함할 수 있도록)
    if (eventBlocks.length > 0) {
      // CSS transform으로 간격이 줄어들었지만, 컨테이너 높이는 충분히 설정
      // 3개 블록 표시를 위해 최소 높이를 충분히 설정
      const totalHeight = currentTop + 10; // 여유 공간 추가
      // 최소 높이를 충분히 설정하여 3개 이상 표시 가능하도록 (각 블록 14px + 간격)
      const minRequiredHeight = 15 + (14 * 3) + (1 * 2); // 첫 블록 top + 3개 블록 높이 + 간격
      eventsContainer.style.setProperty('min-height', `${Math.max(minRequiredHeight, totalHeight)}px`, 'important');
      eventsContainer.style.setProperty('height', 'auto', 'important');
    }
  });
}

/**
 * 캘린더 초기화
 */
async function initCalendar() {
  if (!calendarContainer.value) return;

  // 기존 인스턴스가 있으면 날짜만 업데이트하고 재생성하지 않음
  if (calendarInstance) {
    if (props.currentDate) {
      const dateObj = new Date(props.currentDate);
      try {
        calendarInstance.setDate(dateObj);
        // 날짜 설정 후 일정 업데이트
        await nextTick();
        await new Promise(resolve => setTimeout(resolve, 200));
        updateCalendar();
        return;
      } catch (error) {
        // 날짜 설정 실패 시 재생성
      }
    } else {
      // currentDate가 없으면 재생성하지 않고 그대로 사용
      return;
    }
  }

  // 기존 인스턴스가 없거나 날짜 설정 실패 시에만 재생성
  if (calendarInstance) {
    try {
      calendarInstance.destroy();
    } catch (error) {
      // destroy 실패 시 무시
    }
    calendarInstance = null;
  }

  const CalendarClass = await loadCalendar();
  if (!CalendarClass) {
    return;
  }

  // 초기 날짜를 props에서 가져옴
  const initialDate = props.currentDate ? new Date(props.currentDate) : new Date();
  
  calendarInstance = new CalendarClass(calendarContainer.value, {
    defaultView: 'month',
    useCreationPopup: true, // 빈 공간 클릭 시 일정 생성 팝업 (이후 이벤트에서 커스텀 모달로 전환)
    useDetailPopup: true, // 일정 클릭 시 상세 팝업 사용
    isReadOnly: false, // 드래그 앤 드롭 활성화
    useFormPopup: false, // 기본 폼 팝업 비활성화
    calendars: [
      {
        id: 'schedule',
        name: t('info.schedule.title'),
        backgroundColor: '#9e9e9e',
        borderColor: '#9e9e9e',
        dragBackgroundColor: '#9e9e9e',
      },
    ],
    template: {
      milestone: (schedule) =>
        `<span style="color: #fff; cursor: pointer;">${schedule.title}</span>`,
      task: (schedule) =>
        `<span style="color: #fff; cursor: pointer;">${schedule.title}</span>`,
      allday: (schedule) =>
        `<span style="color: #fff; cursor: pointer;">${schedule.title}</span>`,
      time: (schedule) =>
        `<span style="color: #fff; cursor: pointer;">${schedule.title}</span>`,
      popupDetailLocation: (schedule) => {
        // location을 빈 문자열로 반환하여 첫 번째 항목에 표시되지 않도록
        return '';
      },
      popupDetailBody: (schedule) => {
        // body 템플릿에 설명 표시 (별도 아이템에 표시)
        return schedule.body || schedule.raw?.description || schedule.description || '';
      },
    },
    month: {
      dayNames:
        locale.value === 'ko'
          ? ['일', '월', '화', '수', '목', '금', '토']
          : ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
      moreEventsLabel: t('info.schedule.moreEvents'),
      narrowWeekend: false,
      startDayOfWeek: 0,
      workweek: false,
      isAlways6Weeks: false,
      visibleWeeksCount: 0,
      eventLimit: 4, // 일정 막대 최대 표시 개수 (더 많으면 more 버튼 표시)
    },
  });
  
  // 캘린더 생성 직후 초기 날짜 설정
  await nextTick();
  await new Promise(resolve => setTimeout(resolve, 100));

  // 월 변경 이벤트 감지
  calendarInstance.on('navigate', (event) => {
    const newDate = calendarInstance.getDate();
    const dateObj = new Date(newDate);
    emit('update:currentDate', dateObj);
    emit('monthChanged', dateObj);
      // 리사이즈 핸들 표시
      setTimeout(() => {
        showResizeHandles();
        if (isMobile.value) {
          adjustEventHeights();
        }
      }, 100);
  });
  
  // 초기 날짜 설정 (props.currentDate 사용)
  // 캘린더 생성 직후 날짜 설정
  if (props.currentDate) {
    await setDateToCalendar(props.currentDate);
  } else {
    updateCurrentDate();
  }

  // MutationObserver로 동적으로 생성되는 리사이즈 핸들 감지 및 모바일 클릭 핸들러 설정
  if (calendarContainer.value) {
    const observer = new MutationObserver(() => {
      showResizeHandles();
      if (isMobile.value) {
        setupMobileDayClickHandlers();
        convertMoreButtonsToPlusIcon();
        // 일정 막대 높이 조정
        adjustEventHeights();
        // span이 다시 생기면 제거
        setTimeout(() => {
          if (!calendarContainer.value) return;
          const moreButtons = calendarContainer.value.querySelectorAll(
            '.toastui-calendar-daygrid-more-events, .toastui-calendar-month-daygrid-more-events, .toastui-calendar-grid-cell-more-events, .toastui-calendar-weekday-grid-more-events'
          );
          moreButtons.forEach((button) => {
            const spans = button.querySelectorAll('span');
            spans.forEach((span) => {
              span.remove();
            });
          });
        }, 50);
      }
    });

    observer.observe(calendarContainer.value, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['class', 'style'],
    });

    // 초기 리사이즈 핸들 표시 및 모바일 클릭 핸들러 설정
    setTimeout(() => {
      showResizeHandles();
      if (isMobile.value) {
        setupMobileDayClickHandlers();
        convertMoreButtonsToPlusIcon();
        adjustEventHeights();
      }
    }, 200);

    // 주기적으로 span 제거 (Toast UI Calendar가 동적으로 생성할 수 있음)
    if (isMobile.value) {
      const spanRemovalInterval = setInterval(() => {
        if (!calendarContainer.value || window.innerWidth > 650) {
          clearInterval(spanRemovalInterval);
          return;
        }
        const moreButtons = calendarContainer.value.querySelectorAll(
          '.toastui-calendar-daygrid-more-events span, .toastui-calendar-month-daygrid-more-events span, .toastui-calendar-grid-cell-more-events span, .toastui-calendar-weekday-grid-more-events span'
        );
        moreButtons.forEach((span) => {
          span.remove();
        });
      }, 200);

      // 컴포넌트 언마운트 시 interval 정리
      const cleanup = () => {
        clearInterval(spanRemovalInterval);
      };
      // cleanup 함수를 전역에 저장하여 onUnmounted에서 호출할 수 있도록 함
      window._scheduleCalendarSpanRemovalCleanup = cleanup;
    }
    
    // 리사이즈 이벤트로 모바일 상태 업데이트
    window.addEventListener('resize', () => {
      isMobile.value = window.innerWidth <= 650;
      if (isMobile.value) {
        setupMobileDayClickHandlers();
        convertMoreButtonsToPlusIcon();
      }
    });
    
    // 주기적으로 more 버튼 변환 (Toast UI Calendar가 DOM을 재생성할 수 있음)
    setInterval(() => {
      if (isMobile.value) {
        convertMoreButtonsToPlusIcon();
      }
    }, 500);
  }

  // beforeCreateSchedule 이벤트 (빈 공간 클릭/드래그로 일정 생성 시)
  calendarInstance.on('beforeCreateSchedule', (event) => {
    // 기본 팝업 닫기
    if (event.guide && event.guide.clearGuideElement) {
      event.guide.clearGuideElement();
    }
    // 커스텀 모달 열기
    emit('create');
  });

  // beforeDeleteSchedule 이벤트 (상세 팝업에서 Delete 버튼 클릭 시)
  calendarInstance.on('beforeDeleteSchedule', async (event) => {
    // 기본 동작 방지
    if (event.stop) {
      event.stop();
    }

    const { schedule } = event;
    const scheduleId = extractScheduleId(schedule);

    // 일정 찾기
    const foundSchedule = findScheduleById(scheduleId);

    // 권한 체크: 삭제 권한이 없으면 차단
    if (!foundSchedule || !props.canEditOrDelete(foundSchedule)) {
      return;
    }

    // more 팝업과 상세 팝업 모두 닫기
    closeAllPopups();
    emit('delete', foundSchedule);
  });

  // clickSchedule 이벤트: 일정 클릭 시 상세 팝업이 열린 후 DOM 조작
  calendarInstance.on('clickSchedule', (event) => {
    const { schedule } = event;
    
    const scheduleId = extractScheduleId(schedule);
    const foundSchedule = findScheduleById(scheduleId);

    if (!foundSchedule) {
      console.log('[clickSchedule] 일정을 찾지 못함 - scheduleId:', scheduleId);
      return;
    }
    
    // 현재 클릭한 일정 저장 (Edit 버튼 클릭 시 사용)
    currentClickedSchedule = foundSchedule;

    const assigneeName = foundSchedule.assigneeName || '';
    const departmentName = foundSchedule.departmentName || '';
    const teamName = foundSchedule.teamName || foundSchedule.team || '';
    const description = foundSchedule.description || '';

    // 부서·팀 정보 조합
    const deptTeamText = getDeptTeamText(departmentName, teamName);

    // 동그라미 아이콘과 "일정" 텍스트가 있는 detail-item 전체를 제거하는 함수
    const removeScheduleItem = () => {
      const popup =
        document.querySelector('.toastui-calendar-detail-container') ||
        document.querySelector('.toastui-calendar-popup-container');

      if (!popup) return false;

      let removed = false;

      // 방법 1: 동그라미 아이콘을 가진 detail-item 찾기
      const calendarDotIcons = popup.querySelectorAll(
        '.toastui-calendar-icon.toastui-calendar-calendar-dot, .toastui-calendar-calendar-dot',
      );
      calendarDotIcons.forEach((icon) => {
        let parentItem = icon.closest('.toastui-calendar-detail-item');
        if (parentItem) {
          parentItem.remove();
          removed = true;
        }
      });

      // 방법 2: "일정" 텍스트를 포함하는 detail-item 찾기
      const detailItems = popup.querySelectorAll('.toastui-calendar-detail-item');
      detailItems.forEach((item) => {
        const textContent = item.textContent || '';
        const hasCalendarDot = item.querySelector(
          '.toastui-calendar-icon.toastui-calendar-calendar-dot, .toastui-calendar-calendar-dot',
        );
        const hasScheduleText =
          textContent.trim() === '일정' ||
          (textContent.includes('일정') &&
            !textContent.includes('관리자') &&
            !textContent.includes('교육'));

        if (hasCalendarDot || hasScheduleText) {
          item.remove();
          removed = true;
        }
      });

      // 방법 3: content 클래스를 가진 span에서 "일정" 텍스트 찾기
      const contentSpans = popup.querySelectorAll('.toastui-calendar-content');
      contentSpans.forEach((span) => {
        if (span.textContent && span.textContent.trim() === '일정') {
          const parentItem = span.closest('.toastui-calendar-detail-item');
          if (parentItem) {
            parentItem.remove();
            removed = true;
          }
        }
      });

      return removed;
    };

    // 담당자 정보 업데이트 함수
    const updateAssigneeInfo = () => {
      const popup =
        document.querySelector('.toastui-calendar-detail-container') ||
        document.querySelector('.toastui-calendar-popup-container');

      if (!popup || !assigneeName) return;

      const detailItems = popup.querySelectorAll(
        '.toastui-calendar-detail-item, .toastui-calendar-detail-item-indent',
      );

      detailItems.forEach((item) => {
        const userIcon = item.querySelector(
          '.toastui-calendar-icon.toastui-calendar-ic-user-b',
        );

        if (userIcon) {
          const allNodes = Array.from(item.childNodes);
          const iconIndex = allNodes.indexOf(userIcon);

          let targetNode = null;

          for (let i = iconIndex + 1; i < allNodes.length; i++) {
            const node = allNodes[i];
            if (node.nodeType === Node.TEXT_NODE) {
              const text = node.textContent.trim();
              if (text && text !== '') {
                targetNode = node;
                break;
              }
            } else if (node.nodeType === Node.ELEMENT_NODE && node.tagName === 'SPAN') {
              targetNode = node;
              break;
            }
          }

          if (!targetNode) {
            const allSpans = Array.from(item.querySelectorAll('span'));
            targetNode = allSpans.find((span) => {
              const text = span.textContent || '';
              return (
                text.includes(assigneeName) ||
                text.includes('[object Object]') ||
                text.includes('object Object') ||
                (text.trim() !== '' && !span.classList.contains('toastui-calendar-icon'))
              );
            });
          }

          if (!targetNode) {
            targetNode = document.createElement('span');
            if (userIcon.nextSibling) {
              item.insertBefore(targetNode, userIcon.nextSibling);
            } else {
              item.appendChild(targetNode);
            }
          }

          if (targetNode) {
            let currentText = targetNode.textContent || '';

            if (
              currentText.includes('[object Object]') ||
              currentText.includes('object Object')
            ) {
              currentText = currentText
                .replace(/\[object Object\]/g, '')
                .replace(/object Object/g, '')
                .trim();
            }

            if (
              currentText === assigneeName ||
              currentText.trim() === assigneeName.trim() ||
              (currentText.includes(assigneeName) && !currentText.includes('['))
            ) {
              targetNode.textContent = assigneeName + deptTeamText;
            } else if (!currentText.includes(assigneeName) && currentText.trim() === '') {
              targetNode.textContent = assigneeName + deptTeamText;
            } else if (!currentText.includes(assigneeName)) {
              targetNode.textContent = assigneeName + deptTeamText;
            }
          }
        }
      });
    };

    // Edit/Delete 버튼 숨기기 함수 (권한이 없는 경우)
    const hideEditDeleteButtons = () => {
      const popup =
        document.querySelector('.toastui-calendar-detail-container') ||
        document.querySelector('.toastui-calendar-popup-container');

      if (!popup) return;

      // 권한 체크
      const hasPermission = props.canEditOrDelete(foundSchedule);
      
      if (!hasPermission) {
        // Edit 버튼 숨기기
        const editButtons = popup.querySelectorAll(
          'button:has-text("Edit"), button:has-text("수정"), [class*="edit"], button[data-action="edit"]'
        );
        editButtons.forEach((btn) => {
          const btnText = btn.textContent?.trim() || '';
          if (
            btnText.includes('Edit') ||
            btnText.includes(t('info.schedule.buttons.edit')) ||
            btn.classList?.toString().includes('edit')
          ) {
            btn.style.display = 'none';
          }
        });

        // Delete 버튼 숨기기
        const deleteButtons = popup.querySelectorAll(
          'button:has-text("Delete"), button:has-text("삭제"), [class*="delete"], button[data-action="delete"]'
        );
        deleteButtons.forEach((btn) => {
          const btnText = btn.textContent?.trim() || '';
          if (
            btnText.includes('Delete') ||
            btnText.includes(t('info.schedule.buttons.delete')) ||
            btn.classList?.toString().includes('delete')
          ) {
            btn.style.display = 'none';
          }
        });

        // 모든 버튼을 순회하며 Edit/Delete 버튼 찾기
        const allButtons = popup.querySelectorAll('button');
        allButtons.forEach((btn) => {
          const btnText = btn.textContent?.trim() || '';
          const btnClass = btn.classList?.toString() || '';
          const isEditBtn =
            btnText.includes('Edit') ||
            btnText.includes(t('info.schedule.buttons.edit')) ||
            btnClass.includes('edit');
          const isDeleteBtn =
            btnText.includes('Delete') ||
            btnText.includes(t('info.schedule.buttons.delete')) ||
            btnClass.includes('delete');

          if (isEditBtn || isDeleteBtn) {
            btn.style.display = 'none';
          }
        });
      }
    };

    // 별도 아이템(body) 업데이트 함수
    const updateBodyItem = () => {
      const popup =
        document.querySelector('.toastui-calendar-detail-container') ||
        document.querySelector('.toastui-calendar-popup-container');

      if (!popup) return;

      const separateItems = popup.querySelectorAll(
        '.toastui-calendar-detail-item-separate',
      );
      separateItems.forEach((separateItem) => {
        const bodyTemplate = separateItem.querySelector(
          '.toastui-calendar-template-popupDetailBody',
        );
        if (bodyTemplate) {
          bodyTemplate.textContent = description || '';
        }
      });

      // 설명이 없으면 일정 유형 위의 실선 제거
      const stateItems = popup.querySelectorAll('.toastui-calendar-detail-item');
      stateItems.forEach((stateItem) => {
        const stateIcon = stateItem.querySelector('.toastui-calendar-ic-state-b');
        if (stateIcon) {
          // 다음 형제 요소 찾기
          let nextSibling = stateItem.nextElementSibling;
          let hasDescription = false;

          // 다음 형제가 separate 아이템인지 확인
          if (
            nextSibling &&
            nextSibling.classList.contains('toastui-calendar-detail-item-separate')
          ) {
            const bodyTemplate = nextSibling.querySelector(
              '.toastui-calendar-template-popupDetailBody',
            );
            if (bodyTemplate) {
              const bodyText = bodyTemplate.textContent || '';
              hasDescription = bodyText.trim() !== '';
            }
          }

          // 설명이 없으면 실선 제거
          if (!hasDescription) {
            stateItem.style.setProperty('border-bottom', 'none', 'important');
            stateItem.style.setProperty('padding-bottom', '0', 'important');
            stateItem.style.setProperty('margin-bottom', '0', 'important');
          } else {
            // 설명이 있으면 실선 표시
            stateItem.style.removeProperty('border-bottom');
            stateItem.style.removeProperty('padding-bottom');
            stateItem.style.removeProperty('margin-bottom');
          }
        }
      });
    };

    // 모든 업데이트 함수 실행
    const updateAll = () => {
      removeScheduleItem();
      updateAssigneeInfo();
      updateBodyItem();
      hideEditDeleteButtons();
    };

    // MutationObserver로 DOM 변경 감지하여 즉시 수정
    const observer = new MutationObserver(() => {
      removeScheduleItem();
      updateAssigneeInfo();
      updateBodyItem();
      hideEditDeleteButtons();
    });

    // setInterval로 지속적으로 체크 (Toast UI Calendar가 DOM을 다시 렌더링할 수 있음)
    let checkInterval = null;

    // 팝업 감지 및 observer/interval 시작
    const startObserving = () => {
      const popup =
        document.querySelector('.toastui-calendar-detail-container') ||
        document.querySelector('.toastui-calendar-popup-container');

      if (popup) {
        observer.observe(popup, {
          childList: true,
          subtree: true,
          characterData: true,
        });

        // setInterval로 30ms마다 체크 (더 자주 체크)
        checkInterval = setInterval(() => {
          removeScheduleItem();
          updateAssigneeInfo();
          updateBodyItem();
          hideEditDeleteButtons();
        }, 30);

        // 즉시 실행
        removeScheduleItem();
        updateAssigneeInfo();
        updateBodyItem();
        hideEditDeleteButtons();

        // 15초 후 observer와 interval 해제
        setTimeout(() => {
          observer.disconnect();
          if (checkInterval) {
            clearInterval(checkInterval);
            checkInterval = null;
          }
        }, 15000);
      } else {
        setTimeout(startObserving, 50);
      }
    };

    startObserving();

    // 추가로 여러 번 시도 (DOM이 완전히 렌더링될 때까지)
    const intervals = [
      10, 20, 50, 100, 200, 300, 500, 800, 1200, 2000, 3000, 5000, 8000, 10000,
    ];
    intervals.forEach((delay) => {
      setTimeout(() => {
        removeScheduleItem();
        updateAssigneeInfo();
        updateBodyItem();
        hideEditDeleteButtons();
      }, delay);
    });
  });

  // beforeUpdateSchedule 이벤트 (드래그/리사이즈/Edit 버튼으로 일정 수정 시)
  calendarInstance.on('beforeUpdateSchedule', async (event) => {
    const { schedule, changes } = event;
    
    // 일정 찾기: 우선순위 1) clickSchedule에서 저장한 일정, 2) scheduleId로 찾기
    const scheduleId = extractScheduleId(schedule);
    
    let foundSchedule = null;
    
    // 1. clickSchedule 이벤트에서 저장한 일정 사용 (가장 정확)
    if (currentClickedSchedule) {
      foundSchedule = currentClickedSchedule;
    }
    // 2. scheduleId로 찾기
    else if (scheduleId) {
      foundSchedule = findScheduleById(scheduleId);
      
      // 찾은 일정을 저장 (popupButtonHandler에서 사용할 수 있도록)
      if (foundSchedule) {
        currentClickedSchedule = foundSchedule;
      }
    }
    else {
      console.error('[beforeUpdateSchedule] scheduleId를 추출할 수 없음');
    }

    // 권한 체크: 수정 권한이 없으면 모든 동작 차단
    if (!foundSchedule || !props.canEditOrDelete(foundSchedule)) {
      if (event.stop) {
        event.stop();
      }
      return;
    }

    // Edit 버튼 클릭 시 (changes가 없거나 start/end가 없는 경우)
    if (
      !changes ||
      (!changes.start && !changes.end && !changes.startTime && !changes.endTime)
    ) {
      // 기본 동작 방지 (Edit 버튼 클릭 시에만)
      if (event.stop) {
        event.stop();
      }

      // 기본 팝업 닫기
      closeDetailPopup();

      // 추가로 모든 팝업 컨테이너 강제로 닫기
      setTimeout(() => {
        const allPopups = document.querySelectorAll(
          '.toastui-calendar-popup, .toastui-calendar-popup-container, .toastui-calendar-detail-container, [class*="popup-container"]',
        );
        allPopups.forEach((p) => {
          if (p) {
            p.style.display = 'none';
            p.style.visibility = 'hidden';
            p.style.opacity = '0';
            p.remove();
          }
        });
      }, 50);

      // 모달 열기
      setTimeout(() => {
        emit('edit', foundSchedule);
        // 사용 후 초기화
        currentClickedSchedule = null;
      }, 200);
      return;
    }

    // 드래그/리사이즈인 경우: event.stop()을 호출하지 않음
    // Toast UI Calendar가 내부적으로 이벤트를 업데이트하도록 허용
    // afterUpdateSchedule에서 백엔드 업데이트 처리
  });

  // afterUpdateSchedule: 드래그/리사이즈 완료 후 (캘린더 내부 업데이트 후 호출)
  calendarInstance.on('afterUpdateSchedule', async (event) => {
    const { schedule, changes } = event;

    // 마우스 이벤트 리스너에서 처리하지 않도록 플래그 설정
    window._scheduleCalendarUpdating = true;

    // 권한 체크
    const scheduleId = extractScheduleId(schedule);
    const originalSchedule = findScheduleById(scheduleId);
    
    if (!originalSchedule || !props.canEditOrDelete(originalSchedule)) {
      // 권한이 없으면 업데이트 취소 (원래 상태로 되돌리기)
      setTimeout(() => {
        window._scheduleCalendarUpdating = false;
      }, 500);
      return;
    }

    // changes가 있고 start/end가 변경된 경우에만 처리
    if (
      changes &&
      (changes.start || changes.end || changes.startTime || changes.endTime)
    ) {
      // 드래그와 리사이즈 구분
      // 리사이즈: end만 변경되고 start는 변경되지 않음 (뒷부분을 잡아서 늘림)
      // 드래그: start가 변경됨 (일정 전체를 이동)

      // 원본 일정 데이터 가져오기 (schedule.raw 또는 schedules에서 찾기)
      const scheduleId = extractScheduleId(schedule);
      const originalSchedule = findScheduleById(scheduleId);

      if (!originalSchedule) {
        return;
      }

      // 원본 일정의 날짜
      const originalStart = new Date(originalSchedule.startDate + 'T00:00:00');
      const originalEnd = new Date(originalSchedule.endDate + 'T23:59:59');

      // changes에서 새로운 날짜
      const newStart = changes.start ? new Date(changes.start) : null;
      const newEnd = changes.end ? new Date(changes.end) : null;

      // start가 변경되었는지 확인 (날짜 단위로 비교 - 로컬 날짜 사용)
      let startChanged = false;
      if (newStart) {
        const originalStartDate = `${originalStart.getFullYear()}-${String(
          originalStart.getMonth() + 1,
        ).padStart(2, '0')}-${String(originalStart.getDate()).padStart(2, '0')}`;
        const newStartDate = `${newStart.getFullYear()}-${String(
          newStart.getMonth() + 1,
        ).padStart(2, '0')}-${String(newStart.getDate()).padStart(2, '0')}`;
        startChanged = originalStartDate !== newStartDate;
      }

      // end가 변경되었는지 확인
      let endChanged = false;
      if (newEnd) {
        const originalEndDate = `${originalEnd.getFullYear()}-${String(
          originalEnd.getMonth() + 1,
        ).padStart(2, '0')}-${String(originalEnd.getDate()).padStart(2, '0')}`;
        const newEndDate = `${newEnd.getFullYear()}-${String(
          newEnd.getMonth() + 1,
        ).padStart(2, '0')}-${String(newEnd.getDate()).padStart(2, '0')}`;
        endChanged = originalEndDate !== newEndDate;
      }

      // 리사이즈: end만 변경되고 start는 변경되지 않음
      // 드래그: start가 변경됨
      const isResize = endChanged && !startChanged && !changes.startTime;
      const isDrag = startChanged || changes.startTime;

      if (isResize) {
        // 리사이즈: 원본 start 유지, end만 변경
        const newEnd = changes.end ? new Date(changes.end) : originalEnd;

        // 리사이즈 시 start는 원본 유지, end만 변경
        const resizeChanges = {
          end: newEnd,
        };

        try {
          await handleScheduleUpdate(schedule, resizeChanges);
          emit('reload');
        } catch (error) {
          console.error('ScheduleMonthView: resize update failed', error);
        }
      } else if (isDrag) {
        // 드래그: start와 end 모두 이동 (기간 유지)
        const duration = originalEnd.getTime() - originalStart.getTime();
        const newStart = changes.start ? new Date(changes.start) : originalStart;
        const newEnd = new Date(newStart.getTime() + duration);

        const dragChanges = {
          ...changes,
          start: newStart,
          end: newEnd,
        };

        try {
          await handleScheduleUpdate(schedule, dragChanges);
          emit('reload');
        } catch (error) {
          console.error('ScheduleMonthView: drag update failed', error);
        }
      }
    }

    setTimeout(() => {
      window._scheduleCalendarUpdating = false;
    }, 500);
  });

  // DOM 이벤트로 Edit/Delete 버튼 클릭 직접 감지
  nextTick(() => {
    const popupButtonHandler = async (e) => {
      const target = e.target;
      const button = target.closest('button');

      if (!button) return;

      // Toast UI Calendar 상세 팝업의 버튼 클래스 확인
      const buttonText = button.textContent?.trim() || '';
      const buttonClass = button.className || '';

      // 팝업 찾기
      const popup =
        button.closest('.toastui-calendar-popup') ||
        button.closest('.toastui-calendar-popup-container') ||
        button.closest('.toastui-calendar-detail-container') ||
        button.closest('[class*="popup"]');

      if (!popup) return;

      // 일정 ID 추출: 무조건 scheduleId로만 찾기 (가까운 요소 찾기 로직 제거)
      let scheduleId = null;

      // 1. 저장된 일정에서 ID 가져오기
      if (currentClickedSchedule) {
        scheduleId = String(currentClickedSchedule.scheduleId || currentClickedSchedule.id);
      }
      
      // 2. 팝업의 data 속성에서 찾기
      if (!scheduleId) {
        const eventId =
          popup.getAttribute('data-event-id') ||
          popup.getAttribute('data-schedule-id') ||
          button.getAttribute('data-event-id') ||
          button.getAttribute('data-schedule-id');

        if (eventId) {
          scheduleId = eventId;
        }
      }
      
      // 3. 팝업 내부의 모든 요소에서 data-schedule-id 또는 data-id 찾기
      if (!scheduleId) {
        const allElements = popup.querySelectorAll('[data-schedule-id], [data-id]');
        for (const elem of allElements) {
          const id = elem.getAttribute('data-schedule-id') || elem.getAttribute('data-id');
          if (id) {
            scheduleId = id;
            break;
          }
        }
      }

      // Edit 버튼 클릭
      if (
        buttonText.includes('Edit') ||
        buttonText.includes(t('info.schedule.buttons.edit')) ||
        buttonClass.includes('edit') ||
        button.getAttribute('data-action') === 'edit'
      ) {
        e.preventDefault();
        e.stopPropagation();

        // 일정 막대 클릭 이벤트가 발생했는지 확인 (최대 200ms 대기)
        if (!scheduleId && !currentClickedSchedule) {
          await new Promise((resolve) => {
            const startTime = Date.now();
            const checkInterval = setInterval(() => {
              if (currentClickedSchedule) {
                clearInterval(checkInterval);
                scheduleId = String(currentClickedSchedule.scheduleId || currentClickedSchedule.id);
                resolve();
              } else if (Date.now() - startTime > 200) {
                clearInterval(checkInterval);
                resolve();
              }
            }, 10);
          });
        }
        
        // 일정 찾기: 우선순위 1) clickSchedule에서 저장한 일정, 2) scheduleId로 찾기 (제목으로 찾지 않음)
        let foundSchedule = null;
        
        // 1. clickSchedule 이벤트에서 저장한 일정 사용 (가장 정확)
        if (currentClickedSchedule) {
          foundSchedule = currentClickedSchedule;
        }
        // 2. scheduleId로 찾기
        else if (scheduleId) {
          foundSchedule = findScheduleById(scheduleId);
        }
        // 3. scheduleId가 없고 currentClickedSchedule도 null이면 에러
        else {
          console.error('[popupButtonHandler] 일정 ID를 찾을 수 없음. scheduleId:', scheduleId);
          return;
        }

        // 권한 체크: 수정 권한이 없으면 차단
        if (!foundSchedule || !props.canEditOrDelete(foundSchedule)) {
          return;
        }
        
        if (foundSchedule) {
          // 기본 팝업 먼저 닫기
          closeDetailPopup();

          // 추가로 모든 팝업 컨테이너 강제로 닫기
          setTimeout(() => {
            const allPopups = document.querySelectorAll(
              '.toastui-calendar-popup, .toastui-calendar-popup-container, .toastui-calendar-detail-container, [class*="popup-container"]',
            );
            allPopups.forEach((p) => {
              if (p) {
                p.style.display = 'none';
                p.style.visibility = 'hidden';
                p.style.opacity = '0';
                p.remove();
              }
            });
          }, 50);

          // 모달 열기 (약간의 지연 후)
          setTimeout(() => {
            emit('edit', foundSchedule);
            // 사용 후 초기화
            currentClickedSchedule = null;
          }, 200);
        }
        return;
      }

      // Delete 버튼 클릭
      if (
        buttonText.includes('Delete') ||
        buttonText.includes(t('info.schedule.buttons.delete')) ||
        buttonClass.includes('delete') ||
        button.getAttribute('data-action') === 'delete'
      ) {
        e.preventDefault();
        e.stopPropagation();

        // 일정 찾기: 우선순위 1) clickSchedule에서 저장한 일정, 2) scheduleId로 찾기 (제목으로 찾지 않음)
        let foundSchedule = null;
        
        // 1. clickSchedule 이벤트에서 저장한 일정 사용 (가장 정확)
        if (currentClickedSchedule) {
          foundSchedule = currentClickedSchedule;
        }
        // 2. scheduleId로 찾기
        else if (scheduleId) {
          foundSchedule = findScheduleById(scheduleId);
        }
        // 3. scheduleId가 없고 currentClickedSchedule도 null이면 에러
        else {
          console.error('[popupButtonHandler] 일정 ID를 찾을 수 없음. scheduleId:', scheduleId);
          return;
        }

        // 권한 체크: 삭제 권한이 없으면 차단
        if (!foundSchedule || !props.canEditOrDelete(foundSchedule)) {
          return;
        }

        if (foundSchedule) {
          // more 팝업과 상세 팝업 모두 닫기
          closeAllPopups();
          emit('delete', foundSchedule);
          // 사용 후 초기화
          currentClickedSchedule = null;
        }
        return;
      }
    };

    // capture phase에서 이벤트 감지
    document.addEventListener('click', popupButtonHandler, true);

    // 컴포넌트 언마운트 시 리스너 제거를 위해 저장
    window._scheduleCalendarPopupHandler = popupButtonHandler;
  });

  // 캘린더 업데이트
  updateCalendar();

  // 드래그 완료 감지를 위한 마우스 이벤트 리스너
  nextTick(() => {
    const calendarElement = calendarContainer.value;
    if (!calendarElement) {
      return;
    }

    let dragStartSchedule = null;
    let isDragging = false;
    let isResizing = false; // 리사이즈 모드 플래그
    let dragStartPosition = null;

    // 상세 팝업이 열려있는지 확인하는 함수
    const isDetailPopupOpen = () => {
      const popup =
        document.querySelector('.toastui-calendar-detail-container') ||
        document.querySelector('.toastui-calendar-popup-container') ||
        document.querySelector('.toastui-calendar-popup');
      if (popup) {
        const style = window.getComputedStyle(popup);
        return (
          style.display !== 'none' &&
          style.visibility !== 'hidden' &&
          style.opacity !== '0'
        );
      }
      return false;
    };

    // 마우스 다운 이벤트 감지
    calendarElement.addEventListener(
      'mousedown',
      (e) => {
        // 상세 팝업이 열려있으면 드래그 비활성화
        if (isDetailPopupOpen()) {
          return;
        }

        const target = e.target;

        // 리사이즈 핸들 감지
        const isResizeHandle = target.closest(
          '.toastui-calendar-icon-handle-y, [class*="handle"]',
        );
        if (isResizeHandle) {
          isResizing = true;
        } else {
          isResizing = false;
        }

        const eventElement = target.closest(
          '.toastui-calendar-event, .toastui-calendar-allday-event, .toastui-calendar-weekday-event, [class*="event"]',
        );

        if (eventElement) {
          // 일정 ID 추출 시도
          let scheduleId =
            eventElement.getAttribute('data-schedule-id') ||
            eventElement.getAttribute('data-id') ||
            eventElement.closest('[data-schedule-id]')?.getAttribute('data-schedule-id');

          // ID를 찾지 못한 경우, 제목으로 일정 찾기
          // 같은 일자에 같은 제목이 여러 개 있을 수 있으므로, 클릭한 요소와 가장 가까운 일정 찾기
          if (!scheduleId) {
            const titleElement = eventElement.querySelector(
              '.toastui-calendar-weekday-event-title, .toastui-calendar-template-allday span, [class*="title"]',
            );
            const titleText =
              titleElement?.textContent?.trim() || eventElement.textContent?.trim();

            if (titleText) {
              // 같은 제목의 일정이 여러 개 있을 수 있으므로, 클릭한 요소와 가장 가까운 일정 찾기
              const allMatchingEvents = Array.from(
                calendarElement.querySelectorAll('.toastui-calendar-weekday-event'),
              ).filter((elem) => {
                const elemTitle = elem
                  .querySelector('.toastui-calendar-weekday-event-title')
                  ?.textContent?.trim();
                return elemTitle === titleText;
              });

              if (allMatchingEvents.length > 0) {
                // 클릭한 요소와 가장 가까운 일정 찾기
                let closestEvent = allMatchingEvents[0];
                let minDistance = Infinity;

                const clickRect = eventElement.getBoundingClientRect();
                const clickCenterX = clickRect.left + clickRect.width / 2;
                const clickCenterY = clickRect.top + clickRect.height / 2;

                allMatchingEvents.forEach((elem) => {
                  const elemRect = elem.getBoundingClientRect();
                  const elemCenterX = elemRect.left + elemRect.width / 2;
                  const elemCenterY = elemRect.top + elemRect.height / 2;
                  const distance = Math.sqrt(
                    Math.pow(clickCenterX - elemCenterX, 2) +
                      Math.pow(clickCenterY - elemCenterY, 2),
                  );

                  if (distance < minDistance) {
                    minDistance = distance;
                    closestEvent = elem;
                  }
                });

                // 가장 가까운 일정의 ID 찾기
                const closestScheduleId =
                  closestEvent.getAttribute('data-schedule-id') ||
                  closestEvent.getAttribute('data-id');

                if (closestScheduleId) {
                  scheduleId = String(closestScheduleId);
                } else {
                  // 여전히 ID를 찾지 못한 경우 첫 번째 일정 사용
                  const foundSchedule = findScheduleByTitle(titleText);
                  if (foundSchedule) {
                    scheduleId = extractScheduleId(foundSchedule);
                  }
                }
              } else {
                // 일치하는 일정이 없는 경우 첫 번째 일정 사용
                const foundSchedule = findScheduleByTitle(titleText);
                if (foundSchedule) {
                  scheduleId = extractScheduleId(foundSchedule);
                }
              }
            }
          }

          if (scheduleId) {
            const foundSchedule = findScheduleById(scheduleId);
            // 권한 체크: 수정 권한이 없으면 드래그 불가
            if (foundSchedule && props.canEditOrDelete(foundSchedule)) {
              dragStartSchedule = foundSchedule;
              dragStartPosition = { x: e.clientX, y: e.clientY };
              isDragging = false;
            }
          }
        }
      },
      true,
    );

    // 마우스 이동 이벤트 감지 (드래그 중인지 확인)
    calendarElement.addEventListener(
      'mousemove',
      (e) => {
        // 상세 팝업이 열려있으면 드래그 비활성화
        if (isDetailPopupOpen()) {
          if (dragStartSchedule) {
            dragStartSchedule = null;
            isDragging = false;
            isResizing = false;
            dragStartPosition = null;
          }
          return;
        }

        if (dragStartSchedule && e.buttons === 1) {
          if (!isDragging) {
            const moved =
              Math.abs(e.clientX - dragStartPosition.x) +
              Math.abs(e.clientY - dragStartPosition.y);
            if (moved > 5) {
              // 5px 이상 이동했을 때만 드래그로 간주
              isDragging = true;
            }
          }
        }
      },
      true,
    );

    // 마우스 업 이벤트 감지 (드래그 완료)
    calendarElement.addEventListener(
      'mouseup',
      async (e) => {
        // 상세 팝업이 열려있으면 드래그 비활성화
        if (isDetailPopupOpen()) {
          if (dragStartSchedule) {
            dragStartSchedule = null;
            isDragging = false;
            isResizing = false;
            dragStartPosition = null;
          }
          return;
        }

        // 드래그가 아닌 경우 즉시 초기화
        if (!dragStartSchedule || !isDragging) {
          dragStartSchedule = null;
          isDragging = false;
          isResizing = false;
          dragStartPosition = null;
          return;
        }

        // 드래그 중인 경우
        const currentDragSchedule = dragStartSchedule; // 현재 드래그 중인 일정 저장
        const currentIsResizing = isResizing; // 현재 리사이즈 모드 저장
        const mouseUpPosition = { x: e.clientX, y: e.clientY };

        // 권한 체크: 수정 권한이 없으면 드래그 불가
        if (!currentDragSchedule || !props.canEditOrDelete(currentDragSchedule)) {
          dragStartSchedule = null;
          isDragging = false;
          isResizing = false;
          dragStartPosition = null;
          return;
        }

        // 즉시 상태 초기화 (다음 드래그를 위해)
        dragStartSchedule = null;
        isDragging = false;
        isResizing = false;
        dragStartPosition = null;

        // afterUpdateSchedule 이벤트가 발생할 때까지 대기
        setTimeout(async () => {
          // afterUpdateSchedule 이벤트가 발생했는지 확인
          if (window._scheduleCalendarUpdating) {
            return;
          }

          try {
            // 업데이트된 일정 요소 찾기 - scheduleId로 정확하게 찾기
            const scheduleId = extractScheduleId(currentDragSchedule);
            let updatedEventElement = Array.from(
              calendarElement.querySelectorAll('.toastui-calendar-weekday-event'),
            ).find((elem) => {
              const elemScheduleId =
                elem.getAttribute('data-schedule-id') || elem.getAttribute('data-id');
              return elemScheduleId && String(elemScheduleId) === scheduleId;
            });

            // scheduleId로 찾지 못한 경우 제목으로 찾기 (fallback)
            if (!updatedEventElement) {
              const scheduleTitle = currentDragSchedule.title.trim();
              updatedEventElement = Array.from(
                calendarElement.querySelectorAll('.toastui-calendar-weekday-event'),
              ).find((elem) => {
                const titleElem = elem.querySelector(
                  '.toastui-calendar-weekday-event-title',
                );
                return titleElem && titleElem.textContent?.trim() === scheduleTitle;
              });
            }

            if (currentIsResizing) {
              // 리사이즈: end만 변경, start는 유지
              // 마우스 위치에서 날짜 셀 찾기 (막대 위에 있어도 찾을 수 있도록 개선)
              let dateCell = null;

              // 1. 먼저 마우스 위치의 요소에서 날짜 셀 찾기
              const elementAtPoint = document.elementFromPoint(
                mouseUpPosition.x,
                mouseUpPosition.y,
              );
              if (elementAtPoint) {
                // 막대 요소 위에 있어도 부모나 조상에서 셀 찾기
                dateCell = elementAtPoint.closest('.toastui-calendar-daygrid-cell');

                // 막대 요소 자체인 경우, 그 막대가 속한 셀 찾기
                if (
                  !dateCell &&
                  (elementAtPoint.classList.contains('toastui-calendar-weekday-event') ||
                    elementAtPoint.closest('.toastui-calendar-weekday-event'))
                ) {
                  const eventBar =
                    elementAtPoint.closest('.toastui-calendar-weekday-event') ||
                    elementAtPoint;
                  // 이벤트 막대의 위치를 기반으로 셀 찾기
                  const eventRect = eventBar.getBoundingClientRect();
                  const centerX = eventRect.left + eventRect.width / 2;
                  const centerY = eventRect.top + eventRect.height / 2;

                  // 막대의 중심점에서 날짜 셀 찾기
                  const centerElement = document.elementFromPoint(centerX, centerY);
                  dateCell = centerElement?.closest('.toastui-calendar-daygrid-cell');
                }

                // 여전히 찾지 못한 경우, 마우스 위치의 X 좌표를 기반으로 셀 찾기
                if (!dateCell) {
                  // 캘린더 컨테이너의 모든 날짜 셀을 가져와서 X 좌표로 찾기
                  const allCells = Array.from(
                    calendarElement.querySelectorAll('.toastui-calendar-daygrid-cell'),
                  );
                  for (const cell of allCells) {
                    const cellRect = cell.getBoundingClientRect();
                    // 마우스 X 좌표가 셀의 범위 내에 있는지 확인
                    if (
                      mouseUpPosition.x >= cellRect.left &&
                      mouseUpPosition.x <= cellRect.right &&
                      mouseUpPosition.y >= cellRect.top &&
                      mouseUpPosition.y <= cellRect.bottom
                    ) {
                      dateCell = cell;
                      break;
                    }
                  }
                }
              }

              // 리사이즈는 마우스 위치가 end 날짜이므로 마우스 위치의 셀을 직접 사용
              if (!dateCell) {
                return;
              }

              const targetCell = dateCell;

              // 날짜 정보 추출
              let dateAttr =
                targetCell.getAttribute('data-date') ||
                targetCell.getAttribute('data-ymd');

              // 날짜 계산
              let newEndDate = null;

              // 현재 표시 중인 월 정보 가져오기
              const currentDate = calendarInstance
                ? calendarInstance.getDate()
                : new Date();
              const currentYear = currentDate.getFullYear();
              const currentMonth = currentDate.getMonth();

              if (dateAttr) {
                // data-date 속성이 있으면 사용
                newEndDate = new Date(dateAttr + 'T00:00:00');
              } else {
                // 셀의 텍스트에서 날짜 숫자 추출
                const dateText = targetCell.textContent?.trim();
                const dayNumber = parseInt(dateText);

                // 셀의 인덱스를 기반으로 날짜 계산
                const allCells = Array.from(
                  calendarElement.querySelectorAll('.toastui-calendar-daygrid-cell'),
                );
                const cellIndex = allCells.indexOf(targetCell);

                if (cellIndex >= 0) {
                  // 첫 번째 날의 요일 계산
                  const firstDayOfMonth = new Date(currentYear, currentMonth, 1);
                  const dayOfWeek = firstDayOfMonth.getDay(); // 0=일요일
                  const actualDay = cellIndex - dayOfWeek + 1;

                  // actualDay가 0 이하이면 이전 달
                  if (actualDay <= 0) {
                    // 이전 달의 마지막 날들
                    const prevMonth = currentMonth === 0 ? 11 : currentMonth - 1;
                    const prevYear = currentMonth === 0 ? currentYear - 1 : currentYear;
                    const daysInPrevMonth = new Date(
                      prevYear,
                      prevMonth + 1,
                      0,
                    ).getDate();
                    const prevMonthDay = daysInPrevMonth + actualDay;
                    newEndDate = new Date(prevYear, prevMonth, prevMonthDay);
                  }
                  // actualDay가 현재 달의 마지막 날보다 크면 다음 달
                  else {
                    const daysInCurrentMonth = new Date(
                      currentYear,
                      currentMonth + 1,
                      0,
                    ).getDate();
                    if (actualDay > daysInCurrentMonth) {
                      // 다음 달의 날짜들
                      const nextMonth = currentMonth === 11 ? 0 : currentMonth + 1;
                      const nextYear =
                        currentMonth === 11 ? currentYear + 1 : currentYear;
                      const nextMonthDay = actualDay - daysInCurrentMonth;
                      newEndDate = new Date(nextYear, nextMonth, nextMonthDay);
                    } else {
                      // 현재 달의 날짜
                      newEndDate = new Date(currentYear, currentMonth, actualDay);
                    }
                  }

                  // 텍스트에서 추출한 날짜와 계산된 날짜의 일자가 일치하는지 확인
                  if (!isNaN(dayNumber) && dayNumber > 0 && dayNumber <= 31) {
                    const calculatedDay = newEndDate.getDate();
                    // 일자가 일치하지 않으면 텍스트 기반으로 재계산
                    if (calculatedDay !== dayNumber) {
                      // 셀의 위치를 고려하여 이전/다음 달인지 판단
                      if (cellIndex < dayOfWeek) {
                        // 이전 달
                        const prevMonth = currentMonth === 0 ? 11 : currentMonth - 1;
                        const prevYear =
                          currentMonth === 0 ? currentYear - 1 : currentYear;
                        newEndDate = new Date(prevYear, prevMonth, dayNumber);
                      } else {
                        // 현재 달 또는 다음 달 (셀 인덱스로 판단)
                        const daysInCurrentMonth = new Date(
                          currentYear,
                          currentMonth + 1,
                          0,
                        ).getDate();
                        if (actualDay > daysInCurrentMonth) {
                          // 다음 달
                          const nextMonth = currentMonth === 11 ? 0 : currentMonth + 1;
                          const nextYear =
                            currentMonth === 11 ? currentYear + 1 : currentYear;
                          newEndDate = new Date(nextYear, nextMonth, dayNumber);
                        } else {
                          // 현재 달
                          newEndDate = new Date(currentYear, currentMonth, dayNumber);
                        }
                      }
                    }
                  }
                } else if (!isNaN(dayNumber) && dayNumber > 0 && dayNumber <= 31) {
                  // 셀 인덱스를 찾지 못한 경우 텍스트만으로 계산 (현재 달로 가정)
                  newEndDate = new Date(currentYear, currentMonth, dayNumber);
                }
              }

              if (newEndDate && !isNaN(newEndDate.getTime())) {
                const originalStart = new Date(
                  currentDragSchedule.startDate + 'T00:00:00',
                );
                const originalEnd = new Date(currentDragSchedule.endDate + 'T23:59:59');

                // 리사이즈: start는 유지, end만 변경
                const newEnd = new Date(newEndDate);
                newEnd.setHours(23, 59, 59, 999);

                // end가 start보다 앞서면 안됨
                if (newEnd.getTime() < originalStart.getTime()) {
                  return;
                }

                // end가 변경되었는지 확인
                const endChanged =
                  Math.abs(originalEnd.getTime() - newEnd.getTime()) >
                  1000 * 60 * 60 * 12; // 12시간 이상 차이

                if (endChanged) {
                  // 권한 재확인
                  if (!props.canEditOrDelete(currentDragSchedule)) {
                    return;
                  }

                  const changes = {
                    end: newEnd,
                    // start는 포함하지 않음 (원본 유지)
                  };

                  const updatedEvent = {
                    id: extractScheduleId(currentDragSchedule),
                    raw: currentDragSchedule,
                  };

                  try {
                    await handleScheduleUpdate(updatedEvent, changes);
                    emit('reload');
                  } catch (error) {
                    console.error('ScheduleMonthView: resize update failed (mouse)', error);
                  }
                }
              }
            } else {
              // 드래그 처리
              // 마우스 위치에서 날짜 셀 찾기 (막대 위에 있어도 찾을 수 있도록 개선)
              let dateCell = null;

              // 1. 먼저 마우스 위치의 요소에서 날짜 셀 찾기
              const elementAtPoint = document.elementFromPoint(
                mouseUpPosition.x,
                mouseUpPosition.y,
              );
              if (elementAtPoint) {
                // 막대 요소 위에 있어도 부모나 조상에서 셀 찾기
                dateCell = elementAtPoint.closest('.toastui-calendar-daygrid-cell');

                // 막대 요소 자체인 경우, 그 막대가 속한 셀 찾기
                if (
                  !dateCell &&
                  (elementAtPoint.classList.contains('toastui-calendar-weekday-event') ||
                    elementAtPoint.closest('.toastui-calendar-weekday-event'))
                ) {
                  const eventBar =
                    elementAtPoint.closest('.toastui-calendar-weekday-event') ||
                    elementAtPoint;
                  // 이벤트 막대의 위치를 기반으로 셀 찾기
                  const eventRect = eventBar.getBoundingClientRect();
                  const centerX = eventRect.left + eventRect.width / 2;
                  const centerY = eventRect.top + eventRect.height / 2;

                  // 막대의 중심점에서 날짜 셀 찾기
                  const centerElement = document.elementFromPoint(centerX, centerY);
                  dateCell = centerElement?.closest('.toastui-calendar-daygrid-cell');
                }

                // 여전히 찾지 못한 경우, 마우스 위치의 X 좌표를 기반으로 셀 찾기
                if (!dateCell) {
                  // 캘린더 컨테이너의 모든 날짜 셀을 가져와서 X 좌표로 찾기
                  const allCells = Array.from(
                    calendarElement.querySelectorAll('.toastui-calendar-daygrid-cell'),
                  );
                  for (const cell of allCells) {
                    const cellRect = cell.getBoundingClientRect();
                    // 마우스 X 좌표가 셀의 범위 내에 있는지 확인
                    if (
                      mouseUpPosition.x >= cellRect.left &&
                      mouseUpPosition.x <= cellRect.right &&
                      mouseUpPosition.y >= cellRect.top &&
                      mouseUpPosition.y <= cellRect.bottom
                    ) {
                      dateCell = cell;
                      break;
                    }
                  }
                }
              }

              // 이벤트 요소가 있는 셀 찾기 - 여러 방법 시도
              let eventCell = null;

              if (updatedEventElement) {
                // 방법 1: closest로 찾기
                eventCell = updatedEventElement.closest('.toastui-calendar-daygrid-cell');

                // 방법 2: 부모 요소들을 순회하며 찾기
                if (!eventCell) {
                  let parent = updatedEventElement.parentElement;
                  let depth = 0;
                  while (parent && depth < 5) {
                    if (parent.classList?.contains('toastui-calendar-daygrid-cell')) {
                      eventCell = parent;
                      break;
                    }
                    parent = parent.parentElement;
                    depth++;
                  }
                }

                // 방법 3: 이벤트 요소의 위치를 기반으로 셀 찾기
                if (!eventCell) {
                  const eventRect = updatedEventElement.getBoundingClientRect();
                  const eventCenterX = eventRect.left + eventRect.width / 2;
                  const eventCenterY = eventRect.top + eventRect.height / 2;
                  const elementAtCenter = document.elementFromPoint(
                    eventCenterX,
                    eventCenterY,
                  );
                  eventCell = elementAtCenter?.closest('.toastui-calendar-daygrid-cell');
                }
              }

              // 방법 4: 마우스 위치에서 찾은 셀 사용
              if (!eventCell && dateCell) {
                eventCell = dateCell;
              }

              if (eventCell || dateCell) {
                const targetCell = eventCell || dateCell;
                // 날짜 정보 추출 - 여러 방법 시도
                let dateAttr =
                  targetCell.getAttribute('data-date') ||
                  targetCell.getAttribute('data-ymd') ||
                  targetCell.querySelector('[data-date]')?.getAttribute('data-date');

                // 부모 요소에서 찾기
                if (!dateAttr) {
                  let parent = targetCell.parentElement;
                  let depth = 0;
                  while (parent && depth < 3) {
                    dateAttr =
                      parent.getAttribute('data-date') || parent.getAttribute('data-ymd');
                    if (dateAttr) break;
                    parent = parent.parentElement;
                    depth++;
                  }
                }

                // 셀의 텍스트에서 날짜 추출 (예: "11")
                const dateText = targetCell.textContent?.trim();
                const dayNumber = parseInt(dateText);

                // 현재 표시 중인 월 정보 가져오기
                const currentDate = calendarInstance
                  ? calendarInstance.getDate()
                  : new Date();
                const currentYear = currentDate.getFullYear();
                const currentMonth = currentDate.getMonth();

                // 날짜 셀의 위치를 기반으로 날짜 계산
                // 또는 셀의 인덱스를 기반으로 계산
                const allCells = Array.from(
                  calendarElement.querySelectorAll('.toastui-calendar-daygrid-cell'),
                );
                const cellIndex = allCells.indexOf(targetCell);

                // 날짜 계산: 셀의 인덱스나 텍스트를 기반으로
                let newDate = null;

                if (dateAttr) {
                  // data-date 속성이 있으면 사용
                  newDate = new Date(dateAttr + 'T00:00:00');
                } else if (cellIndex >= 0) {
                  // 셀의 인덱스를 기반으로 날짜 계산
                  const firstDayOfMonth = new Date(currentYear, currentMonth, 1);
                  const dayOfWeek = firstDayOfMonth.getDay(); // 0=일요일
                  const actualDay = cellIndex - dayOfWeek + 1;

                  // actualDay가 0 이하이면 이전 달
                  if (actualDay <= 0) {
                    // 이전 달의 마지막 날들
                    const prevMonth = currentMonth === 0 ? 11 : currentMonth - 1;
                    const prevYear = currentMonth === 0 ? currentYear - 1 : currentYear;
                    const daysInPrevMonth = new Date(
                      prevYear,
                      prevMonth + 1,
                      0,
                    ).getDate();
                    const prevMonthDay = daysInPrevMonth + actualDay;
                    newDate = new Date(prevYear, prevMonth, prevMonthDay);
                  }
                  // actualDay가 현재 달의 마지막 날보다 크면 다음 달
                  else {
                    const daysInCurrentMonth = new Date(
                      currentYear,
                      currentMonth + 1,
                      0,
                    ).getDate();
                    if (actualDay > daysInCurrentMonth) {
                      // 다음 달의 날짜들
                      const nextMonth = currentMonth === 11 ? 0 : currentMonth + 1;
                      const nextYear =
                        currentMonth === 11 ? currentYear + 1 : currentYear;
                      const nextMonthDay = actualDay - daysInCurrentMonth;
                      newDate = new Date(nextYear, nextMonth, nextMonthDay);
                    } else {
                      // 현재 달의 날짜
                      newDate = new Date(currentYear, currentMonth, actualDay);
                    }
                  }

                  // 텍스트에서 추출한 날짜와 계산된 날짜의 일자가 일치하는지 확인
                  if (!isNaN(dayNumber) && dayNumber > 0 && dayNumber <= 31) {
                    const calculatedDay = newDate.getDate();
                    // 일자가 일치하지 않으면 텍스트 기반으로 재계산
                    if (calculatedDay !== dayNumber) {
                      // 셀의 위치를 고려하여 이전/다음 달인지 판단
                      if (cellIndex < dayOfWeek) {
                        // 이전 달
                        const prevMonth = currentMonth === 0 ? 11 : currentMonth - 1;
                        const prevYear =
                          currentMonth === 0 ? currentYear - 1 : currentYear;
                        newDate = new Date(prevYear, prevMonth, dayNumber);
                      } else {
                        // 현재 달 또는 다음 달 (셀 인덱스로 판단)
                        const daysInCurrentMonth = new Date(
                          currentYear,
                          currentMonth + 1,
                          0,
                        ).getDate();
                        const actualDayForText = cellIndex - dayOfWeek + 1;
                        if (actualDayForText > daysInCurrentMonth) {
                          // 다음 달
                          const nextMonth = currentMonth === 11 ? 0 : currentMonth + 1;
                          const nextYear =
                            currentMonth === 11 ? currentYear + 1 : currentYear;
                          newDate = new Date(nextYear, nextMonth, dayNumber);
                        } else {
                          // 현재 달
                          newDate = new Date(currentYear, currentMonth, dayNumber);
                        }
                      }
                    }
                  }
                } else if (!isNaN(dayNumber) && dayNumber > 0 && dayNumber <= 31) {
                  // 셀 인덱스를 찾지 못한 경우 텍스트만으로 계산 (현재 달로 가정)
                  newDate = new Date(currentYear, currentMonth, dayNumber);
                }

                if (newDate && !isNaN(newDate.getTime())) {
                  const originalStart = new Date(
                    currentDragSchedule.startDate + 'T00:00:00',
                  );
                  const originalEnd = new Date(currentDragSchedule.endDate + 'T23:59:59');

                  // 기간 계산 (원본 기간 유지)
                  const duration = originalEnd.getTime() - originalStart.getTime();
                  const newStart = new Date(newDate);
                  newStart.setHours(0, 0, 0, 0);
                  const newEnd = new Date(newStart.getTime() + duration);

                  // 날짜가 변경되었는지 확인
                  const startChanged =
                    Math.abs(originalStart.getTime() - newStart.getTime()) >
                    1000 * 60 * 60; // 1시간 이상 차이
                  const endChanged =
                    Math.abs(originalEnd.getTime() - newEnd.getTime()) > 1000 * 60 * 60;

                  if (startChanged || endChanged) {
                    const changes = {
                      start: newStart,
                      end: newEnd,
                    };

                    // 권한 재확인
                    if (!props.canEditOrDelete(currentDragSchedule)) {
                      return;
                    }

                    // 임시 이벤트 객체 생성
                    const updatedEvent = {
                      id: extractScheduleId(currentDragSchedule),
                      raw: currentDragSchedule,
                    };

                    try {
                      await handleScheduleUpdate(updatedEvent, changes);
                      emit('reload');
                    } catch (error) {
                      console.error('ScheduleMonthView: drag update failed (mouse)', error);
                    }
                  }
                }
              }
            }
          } catch (error) {
            // 드래그 완료 처리 중 오류 발생 시 무시
            console.error('ScheduleMonthView: mouse event handler error', error);
          }
        }, 500);
      },
      true,
    );
  });
}

/**
 * 캘린더에 날짜 설정 (내부 함수)
 */
async function setDateToCalendar(date) {
  if (!calendarInstance || !date) return;
  
  const targetDate = new Date(date);
  const targetYear = targetDate.getFullYear();
  const targetMonth = targetDate.getMonth();
  
  // 현재 캘린더 날짜 확인
  let currentCalendarDate;
  try {
    currentCalendarDate = calendarInstance.getDate();
    currentCalendarDate = currentCalendarDate instanceof Date ? currentCalendarDate : new Date(currentCalendarDate);
  } catch (e) {
    currentCalendarDate = new Date();
  }
  
  const currentYear = currentCalendarDate.getFullYear();
  const currentMonth = currentCalendarDate.getMonth();
  
  // 년/월이 다르면 날짜 변경
  if (targetYear !== currentYear || targetMonth !== currentMonth) {
    // prev/next를 직접 사용하여 날짜 변경
    const monthDiff = (targetYear - currentYear) * 12 + (targetMonth - currentMonth);
    if (monthDiff !== 0) {
      const absDiff = Math.abs(monthDiff);
      for (let i = 0; i < absDiff; i++) {
        if (monthDiff > 0) {
          calendarInstance.next();
        } else {
          calendarInstance.prev();
        }
      }
    }
    
    // 날짜 변경 후 일정 업데이트
    await nextTick();
    updateCalendar();
  } else {
    // 날짜가 같아도 일정은 업데이트
    updateCalendar();
  }
}

/**
 * 캘린더 날짜 설정 (외부 호출용)
 */
async function setDate(date) {
  if (!calendarInstance || !date) return;
  
  try {
    const targetDate = date instanceof Date ? new Date(date) : new Date(date);
    const targetYear = targetDate.getFullYear();
    const targetMonth = targetDate.getMonth();
    
    // 현재 캘린더 날짜 가져오기
    let currentCalendarDate;
    try {
      currentCalendarDate = calendarInstance.getDate();
      currentCalendarDate = currentCalendarDate instanceof Date ? currentCalendarDate : new Date(currentCalendarDate);
    } catch (e) {
      currentCalendarDate = new Date();
    }
    
    const currentYear = currentCalendarDate.getFullYear();
    const currentMonth = currentCalendarDate.getMonth();
    
    // 년/월이 같으면 일정만 업데이트
    if (currentYear === targetYear && currentMonth === targetMonth) {
      calendarInstance.clear();
      addSchedulesToCalendar();
      return;
    }
    
    // 목표 날짜까지 prev() 또는 next()를 사용하여 이동
    const monthDiff = (targetYear - currentYear) * 12 + (targetMonth - currentMonth);
    
    if (monthDiff !== 0) {
      // 먼저 setDate로 시도
      calendarInstance.setDate(targetDate);
      await nextTick();
      await new Promise(resolve => setTimeout(resolve, 300));
      
      // 날짜가 제대로 설정되었는지 확인
      let checkDate;
      try {
        checkDate = calendarInstance.getDate();
        checkDate = checkDate instanceof Date ? checkDate : new Date(checkDate);
      } catch (e) {
        checkDate = new Date();
      }
      
      // 날짜가 제대로 설정되지 않았으면 prev/next로 이동
      if (checkDate.getFullYear() !== targetYear || checkDate.getMonth() !== targetMonth) {
        const absDiff = Math.abs(monthDiff);
        for (let i = 0; i < absDiff; i++) {
          if (monthDiff > 0) {
            calendarInstance.next();
          } else {
            calendarInstance.prev();
          }
          await new Promise(resolve => setTimeout(resolve, 50));
        }
      }
    }
    
    // 일정 업데이트
    await nextTick();
    await new Promise(resolve => setTimeout(resolve, 100));
    if (calendarInstance) {
      calendarInstance.clear();
      addSchedulesToCalendar();
    }
  } catch (error) {
    // 날짜 설정 실패 시 무시
  }
}

/**
 * 캘린더 인스턴스 존재 여부 확인
 */
function hasCalendarInstance() {
  return calendarInstance !== null;
}

// 외부에서 호출 가능하도록 expose
defineExpose({
  initCalendar,
  updateCalendar,
  setDate,
  prevMonth: handlePrevMonth,
  nextMonth: handleNextMonth,
  goToToday: handleGoToToday,
  hasCalendarInstance,
  closeAllPopups,
});

// currentDate 변경 감지 - 날짜가 변경되면 캘린더 날짜도 업데이트
watch(
  () => props.currentDate,
  async (newDate) => {
    if (calendarInstance && newDate) {
      await setDateToCalendar(newDate);
    }
  },
  { deep: false, immediate: false },
);

// schedules 변경 감지
watch(
  () => props.schedules,
  () => {
    if (calendarInstance) {
      updateCalendar();
    }
  },
  { deep: true },
);

// 언어 변경 감지 및 캘린더 업데이트
watch(
  locale,
  async () => {
    if (calendarInstance) {
      // 캘린더의 dayNames 업데이트
      calendarInstance.setOptions({
        month: {
          dayNames:
            locale.value === 'ko'
              ? ['일', '월', '화', '수', '목', '금', '토']
              : ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
          moreEventsLabel: t('info.schedule.moreEvents'),
          eventLimit: 4, // 일정 막대 최대 표시 개수 (더 많으면 more 버튼 표시)
        },
        calendars: [
          {
            id: 'schedule',
            name: t('info.schedule.title'),
          },
        ],
      });

      // 캘린더 다시 렌더링
      await nextTick();
      calendarInstance.render();
    }
  },
  { immediate: false },
);

onMounted(async () => {
  scheduleTypes.value = await fetchScheduleTypes();
  await nextTick();
  await initCalendar();
});

onUnmounted(() => {
  if (calendarInstance) {
    calendarInstance.destroy();
  }

  // Edit 버튼 클릭 리스너 제거
  if (window._scheduleCalendarPopupHandler) {
    document.removeEventListener('click', window._scheduleCalendarPopupHandler, true);
    delete window._scheduleCalendarPopupHandler;
  }
  
  // span 제거 interval 정리
  if (window._scheduleCalendarSpanRemovalCleanup) {
    window._scheduleCalendarSpanRemovalCleanup();
    delete window._scheduleCalendarSpanRemovalCleanup;
  }
});
</script>

