import { hrmApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

/**
 * HRM 스타일 API
 */
export default {
  /**
   * 모든 스타일 조회
   */
  async getAllStyles() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.STYLE_LIST) + "; path=/";
    const response = await hrmApi.get('/styles');
    return response.data;
  },

  /**
   * 카테고리별 스타일 조회
   */
  async getStylesByCategory(category) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.STYLE_LIST_BY_CATEGORY) + "; path=/";
    const response = await hrmApi.get(`/styles/category/${category}`);
    return response.data;
  },

  /**
   * 사용자 스타일 설정 조회
   */
  async getUserStyles(userId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.STYLE_USER_GET) + "; path=/";
    const response = await hrmApi.get(`/styles/user/${userId}`);
    return response.data;
  },

  /**
   * 사용자 스타일 업데이트
   */
  async updateUserStyle(userId, category, styleCode) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.STYLE_USER_UPDATE) + "; path=/";
    return await hrmApi.patch(`/styles/user/${userId}`, {
      category,
      styleCode
    });
  }
}

