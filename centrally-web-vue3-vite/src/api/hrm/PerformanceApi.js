import { hrmApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  // 특정 사용자 실적 조회 (GET /performances/{id})
  async getPerformancesByUserId(userId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.PERFORMANCE_LIST) + "; path=/";
    return await hrmApi.get(`/performances/${userId}`);
  },

  // 특정 사용자 실적 추가 (POST /performances/{userId})
  async createPerformance(userId, performance) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.PERFORMANCE_CREATE) + "; path=/";
    return await hrmApi.post(`/performances/${userId}`, performance);
  },

  // 특정 사용자 실적 삭제 (DELETE /performances/{userId}/{performanceId})
  async deletePerformance(userId, performanceId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.PERFORMANCE_DELETE) + "; path=/";
    return await hrmApi.delete(`/performances/${userId}/${performanceId}`);
  },

  // 특정 사용자 실적 수정 (PUT /performances/{userId}/{performanceId})
  async patchPerformance(userId, performanceId, performance) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.PERFORMANCE_UPDATE) + "; path=/";
    return await hrmApi.patch(`/performances/${userId}/${performanceId}`, performance);
  },

};
