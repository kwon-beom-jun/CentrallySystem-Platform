/**
 * 일정 업데이트 관련 composable
 */
import { useI18n } from 'vue-i18n';
import ScheduleApi from '@/api/info/ScheduleApi';
import { toast } from 'vue3-toastify';

export function useScheduleUpdate(schedules, loadSchedules) {
  const { t } = useI18n();
  
  /**
   * 일정 업데이트 처리 (드래그 앤 드롭)
   */
  async function handleScheduleUpdate(schedule, changes) {
    try {
      // schedule.id 또는 schedule.raw.id로 원본 일정 찾기
      const scheduleId = schedule.id || schedule.raw?.scheduleId || schedule.raw?.id;
      
      // schedules가 ref인지 확인하고 .value로 접근
      const schedulesArray = schedules.value || schedules;
      
      if (!Array.isArray(schedulesArray)) {
        console.error('useScheduleUpdate: schedules is not an array', schedules);
        toast.error(t('info.schedule.error.updateFailed'));
        return;
      }
      
      const originalSchedule = schedulesArray.find(s => 
        String(s.scheduleId || s.id) === String(scheduleId)
      );
      
      if (!originalSchedule) {
        console.error('useScheduleUpdate: schedule not found', { scheduleId, schedulesArray });
        return;
      }
      
      // changes 객체에서 start, end, startTime, endTime 추출
      const startDate = changes.start ? new Date(changes.start) : null;
      const endDate = changes.end ? new Date(changes.end) : null;
      const startTime = changes.startTime ? new Date(changes.startTime) : null;
      const endTime = changes.endTime ? new Date(changes.endTime) : null;
      
      if (!startDate && !endDate && !startTime && !endTime) {
        return;
      }
      
      // 날짜만 추출 (YYYY-MM-DD) - 로컬 날짜 사용 (시간대 문제 방지)
      const startDateStr = startDate ? 
        `${startDate.getFullYear()}-${String(startDate.getMonth() + 1).padStart(2, '0')}-${String(startDate.getDate()).padStart(2, '0')}` : 
        originalSchedule.startDate;
      const endDateStr = endDate ? 
        `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}` : 
        originalSchedule.endDate;
      
      // 시간 추출 (HH:mm:ss 또는 전체 ISO 문자열)
      let startTimeStr = originalSchedule.startTime;
      let endTimeStr = originalSchedule.endTime;
      
      // startTime이 있으면 사용, 없으면 기존 시간 유지
      if (startTime) {
        startTimeStr = startTime.toISOString();
      } else if (startDate && originalSchedule.startTime) {
        // 날짜만 변경된 경우, 기존 시간 유지
        const existingStartTime = new Date(originalSchedule.startTime);
        const newStartTime = new Date(startDate);
        newStartTime.setHours(existingStartTime.getHours(), existingStartTime.getMinutes(), existingStartTime.getSeconds());
        startTimeStr = newStartTime.toISOString();
      } else if (startDate && !originalSchedule.startTime) {
        // 날짜만 변경되고 기존 시간이 없는 경우
        startTimeStr = null;
      }
      
      // endTime이 있으면 사용, 없으면 기존 시간 유지
      if (endTime) {
        endTimeStr = endTime.toISOString();
      } else if (endDate && originalSchedule.endTime) {
        // 날짜만 변경된 경우, 기존 시간 유지
        const existingEndTime = new Date(originalSchedule.endTime);
        const newEndTime = new Date(endDate);
        newEndTime.setHours(existingEndTime.getHours(), existingEndTime.getMinutes(), existingEndTime.getSeconds());
        endTimeStr = newEndTime.toISOString();
      } else if (endDate && !originalSchedule.endTime) {
        // 날짜만 변경되고 기존 시간이 없는 경우
        endTimeStr = null;
      }
      
      const updatedSchedule = {
        ...originalSchedule,
        startDate: startDateStr,
        endDate: endDateStr,
        startTime: startTimeStr,
        endTime: endTimeStr,
      };
      
      await ScheduleApi.updateSchedule(originalSchedule.scheduleId, updatedSchedule);
      toast.success(t('info.schedule.success.update'));
      await loadSchedules();
    } catch (error) {
      console.error('useScheduleUpdate: update failed', error);
      console.error('Error details:', {
        message: error.message,
        response: error.response?.data,
        schedule: schedule,
        changes: changes,
      });
      toast.error(t('info.schedule.error.updateFailed'));
      await loadSchedules(); // 원래 상태로 복구
    }
  }
  
  return {
    handleScheduleUpdate,
  };
}

