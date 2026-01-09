import { authApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  async mailSend(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.EMAIL_SEND) + "; path=/";
    return await authApi.post('/email/send', params);
  },

  async mailVerify(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.EMAIL_VERIFY) + "; path=/";
    return await authApi.post('/email/verify', params);
  },
  
  // 비밀번호 찾기용
  async mailResetSend(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.EMAIL_RESET_SEND) + "; path=/";
    return await authApi.post('/email/reset/send', params);
  },

  async mailResetVerify(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.EMAIL_RESET_VERIFY) + "; path=/";
    return await authApi.post('/email/reset/confirm', params);
  },
};
