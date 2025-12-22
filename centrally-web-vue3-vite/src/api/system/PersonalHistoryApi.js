import { systemApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

/**
 * 개인 이력 API
 */
export default {
  /**
   * 개인 이력 조회 (현재 로그인한 사용자의 활동 이력)
   */
  async getPersonalHistory(params) {
    // 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.SYSTEM.PERSONAL_HISTORY_LIST) + "; path=/";
    return await systemApi.get('/activity-log/personal', { params });
  },
};

