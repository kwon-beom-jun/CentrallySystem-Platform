import { hrmApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  // 부서(팀), 고용형태, 직책 정보 조회 (GET /metadata)
  async getMetadatas() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.METADATA_LIST) + "; path=/";
    return await hrmApi.get('/metadata');
  },
};
