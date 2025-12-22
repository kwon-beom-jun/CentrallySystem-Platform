import { hrmApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  // 전체 팀 정보 조회 (GET /teams)
  async getTeams() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.TEAM_LIST) + "; path=/";
    return await hrmApi.get('/teams');
  },
};
