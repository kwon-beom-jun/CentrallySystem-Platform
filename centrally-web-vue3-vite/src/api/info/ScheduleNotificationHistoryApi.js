import { infoApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';
import qs from 'qs';

/**
 * 일정 알림 발송 이력 API
 */
export default {
  /**
   * 알림 발송 이력 조회 (페이징)
   * @param {Object} params - 조회 파라미터
   * @param {string} params.startDate - 시작 날짜 (yyyy-MM-dd)
   * @param {string} params.endDate - 종료 날짜 (yyyy-MM-dd)
   * @param {string} params.searchKeyword - 검색어 (사용자명, 이메일) (선택)
   * @param {number} params.page - 페이지 번호 (0부터 시작)
   * @param {number} params.size - 페이지 크기
   */
  async getNotificationHistory(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_NOTIFICATION_HISTORY) + "; path=/";
    return await infoApi.get('/schedule-notification-logs', {
      params,
      paramsSerializer: p => qs.stringify(p, { arrayFormat: 'repeat', skipNulls: true })
    });
  },
};

