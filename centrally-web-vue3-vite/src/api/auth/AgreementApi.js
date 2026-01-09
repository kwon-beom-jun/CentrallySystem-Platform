import { authApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  /* 현재 적용 중인 약관 목록 조회 */
  async getAgreements(languageCode) {
    // (헤더에 X-Func-Vue 삽입 – 선택)
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.AUTH.AGREEMENT_LIST) + '; path=/';
    const config = languageCode ? { params: { lang: languageCode } } : {};
    return await authApi.get('/agreements', config);
  },
};