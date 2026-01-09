import { infoApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

/**
 * 일정 유형 API
 */
export default {
  /**
   * 활성화된 일정 유형 목록 조회
   */
  async getScheduleTypes() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_TYPE_LIST) + "; path=/";
    return await infoApi.get('/schedule-types');
  },

  /**
   * 캘린더용 일정 유형 목록 조회 (enabled=true인 모든 항목)
   */
  async getScheduleTypesForCalendar() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_TYPE_LIST) + "; path=/";
    return await infoApi.get('/schedule-types/for-calendar');
  },

  /**
   * 일정 유형 단건 조회
   */
  async getScheduleType(scheduleTypeId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_TYPE_DETAIL) + "; path=/";
    return await infoApi.get(`/schedule-types/${scheduleTypeId}`);
  },

  /**
   * 코드로 일정 유형 조회
   */
  async getScheduleTypeByCode(code) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_TYPE_DETAIL) + "; path=/";
    return await infoApi.get(`/schedule-types/code/${code}`);
  },

  /**
   * 일정 유형 생성
   */
  async createScheduleType(scheduleType) {
    // 이력 관리용 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_TYPE_CREATE) + "; path=/";
    return await infoApi.post('/schedule-types', scheduleType);
  },

  /**
   * 일정 유형 수정
   */
  async updateScheduleType(scheduleTypeId, scheduleType) {
    // 이력 관리용 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_TYPE_UPDATE) + "; path=/";
    return await infoApi.put(`/schedule-types/${scheduleTypeId}`, scheduleType);
  },

  /**
   * 일정 유형 삭제
   */
  async deleteScheduleType(scheduleTypeId) {
    // 이력 관리용 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_TYPE_DELETE) + "; path=/";
    return await infoApi.delete(`/schedule-types/${scheduleTypeId}`);
  },
};

