import { infoApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

/**
 * 일정 알림 설정 API
 */
export default {
  /**
   * 현재 사용자의 알림 설정 조회
   */
  async getNotificationSettings() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_NOTIFICATION_GET) + "; path=/";
    return await infoApi.get('/schedule-notifications/me');
  },

  /**
   * 특정 사용자의 알림 설정 조회 (관리자용)
   */
  async getNotificationSettingsByUserId(userId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_NOTIFICATION_GET) + "; path=/";
    return await infoApi.get(`/schedule-notifications/user/${userId}`);
  },

  /**
   * 알림 설정 저장/수정
   */
  async updateNotificationSettings(settings) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_NOTIFICATION_UPDATE) + "; path=/";
    return await infoApi.put('/schedule-notifications/me', settings);
  },

  /**
   * 알림 설정 삭제
   */
  async deleteNotificationSettings() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.SCHEDULE_NOTIFICATION_DELETE) + "; path=/";
    return await infoApi.delete('/schedule-notifications/me');
  },
};

