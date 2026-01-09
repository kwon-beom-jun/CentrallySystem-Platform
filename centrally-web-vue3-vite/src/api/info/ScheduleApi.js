import { infoApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

/**
 * 일정 API
 */
export default {
  /**
   * 일정 목록 조회
   */
  async getSchedules(params) {
    return await infoApi.get('/schedules', { params });
  },

  /**
   * 일정 단건 조회
   */
  async getSchedule(scheduleId) {
    return await infoApi.get(`/schedules/${scheduleId}`);
  },

  /**
   * 일정 생성
   */
  async createSchedule(schedule) {
    // 이력 관리용 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_CREATE) + "; path=/";
    return await infoApi.post('/schedules', schedule);
  },

  /**
   * 일정 수정
   */
  async updateSchedule(scheduleId, schedule) {
    // 이력 관리용 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_UPDATE) + "; path=/";
    return await infoApi.put(`/schedules/${scheduleId}`, schedule);
  },

  /**
   * 일정 삭제
   */
  async deleteSchedule(scheduleId) {
    // 이력 관리용 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_DELETE) + "; path=/";
    return await infoApi.delete(`/schedules/${scheduleId}`);
  },
};

