/**
 * 일정 필터링 관련 composable
 */
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useHrmStore } from '@/store/hrm';
import DepartmentApi from '@/api/hrm/DepartmentApi';
import ScheduleApi from '@/api/info/ScheduleApi';
import { toast } from 'vue3-toastify';

export function useScheduleFilter() {
  const { t } = useI18n();
  const hrmStore = useHrmStore();
  
  const selectedDepartmentId = ref(null);
  const selectedAssigneeId = ref(null);
  const departments = ref([]);
  const schedules = ref([]);
  
  /**
   * 부서 목록 조회
   */
  async function loadDepartments() {
    try {
      const res = await DepartmentApi.getDepartments();
      departments.value = res.data || [];
      
      // 기본값: 사용자 부서 (처음 진입 시에만 설정, 이미 값이 있으면 변경하지 않음)
      if (selectedDepartmentId.value === null && hrmStore.myProfile?.departmentId) {
        selectedDepartmentId.value = hrmStore.myProfile.departmentId;
      }
    } catch (error) {
      // 부서 목록 조회 실패 시 무시
    }
  }
  
  /**
   * 일정 목록 조회
   */
  async function loadSchedules(onUpdateCalendar, targetDate = null) {
    try {
      // targetDate가 제공되면 해당 날짜를 기준으로, 없으면 현재 날짜를 기준으로
      const baseDate = targetDate ? new Date(targetDate) : new Date();
      
      // 현재 월의 첫날
      const year = baseDate.getFullYear();
      const month = baseDate.getMonth(); // 0-11
      const startDate = new Date(year, month, 1);
      
      // 현재 월의 마지막날
      const endDate = new Date(year, month + 1, 0);
      
      // 날짜를 로컬 시간대로 변환하여 YYYY-MM-DD 형식으로 변환
      const startDateStr = `${startDate.getFullYear()}-${String(startDate.getMonth() + 1).padStart(2, '0')}-${String(startDate.getDate()).padStart(2, '0')}`;
      const endDateStr = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`;
      
      const params = {
        startDate: startDateStr,
        endDate: endDateStr,
      };
      
      // departmentId가 null이 아니고 숫자인 경우에만 추가
      if (selectedDepartmentId.value !== null && typeof selectedDepartmentId.value === 'number') {
        params.departmentId = selectedDepartmentId.value;
      }
      
      // assigneeId는 프론트엔드에서 필터링하므로 백엔드 파라미터에서 제외
      // if (selectedAssigneeId.value !== null && typeof selectedAssigneeId.value === 'number') {
      //   params.assigneeId = selectedAssigneeId.value;
      // }
      
      const res = await ScheduleApi.getSchedules(params);
      schedules.value = res.data || [];
      
      // 캘린더 업데이트 콜백 호출
      if (onUpdateCalendar) {
        onUpdateCalendar();
      }
    } catch (error) {
      console.error('loadSchedules error:', error);
      toast.error(t('info.schedule.error.loadSchedulesFailed'));
    }
  }
  
  /**
   * 부서 변경 핸들러
   */
  function onDepartmentChange(value) {
    selectedDepartmentId.value = value;
    // 부서 변경 시 자동으로 데이터 재조회하지 않음 (호출하는 쪽에서 처리)
  }
  
  /**
   * 사용자 선택 핸들러
   */
  function onUserSelected(user) {
    selectedAssigneeId.value = user ? (user.value || user.userId) : null;
    // 사용자 변경 시 자동으로 데이터 재조회하지 않음 (호출하는 쪽에서 처리)
  }
  
  /**
   * 필터 초기화
   */
  function resetFilter() {
    selectedDepartmentId.value = null; // 전체로 초기화
    selectedAssigneeId.value = null;
  }
  
  return {
    selectedDepartmentId,
    selectedAssigneeId,
    departments,
    schedules,
    loadDepartments,
    loadSchedules,
    onDepartmentChange,
    onUserSelected,
    resetFilter,
  };
}

