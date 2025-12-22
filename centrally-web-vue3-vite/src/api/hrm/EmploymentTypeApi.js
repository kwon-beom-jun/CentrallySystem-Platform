import { hrmApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  // 전체 고용 타입 정보 조회 (GET /employment-types)
  async getEmploymentTypes() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.EMPLOYMENT_TYPE_LIST) + "; path=/";
    return await hrmApi.get('/employment-types');
  },
};
