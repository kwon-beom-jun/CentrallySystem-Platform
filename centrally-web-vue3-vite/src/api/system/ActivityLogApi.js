import { systemApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  /**
   * 활동 이력 페이지 조회 (관리자용)
   */
  async getActivityLogs(params) {
    // 로그인 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.SYSTEM.ACTIVITY_LOG_LIST) + "; path=/";
    return await systemApi.get('/activity-log', { params });
  },

  /**
   * 현재 사용자의 최신 활동 이력 조회
   */
  async getRecentActivities(limit = 5) {
    return await systemApi.get('/activity-log/recent', { 
      params: { limit } 
    });
  },
};
