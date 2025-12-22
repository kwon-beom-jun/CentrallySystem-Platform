import { hrmApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  // 전체 직책 조회 (GET /positions)
  async getPositions() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.POSITION_LIST) + "; path=/";
    return await hrmApi.get('/positions');
  },
  // 유저 직책 조회 (GET /positions)
  async getPositionsByPositionId(positionId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.POSITION_BY_USER) + "; path=/";
    return await hrmApi.get(`/positions/${positionId}`);
  },
};
