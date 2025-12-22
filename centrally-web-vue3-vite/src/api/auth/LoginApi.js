import { authApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  async login(userEmail, password, encryptData) {
    // 로그인 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.LOGIN) + "; path=/";
    // const password = EncryptionUtil.encryptPassword(rawPassword, loginKey);
    return await authApi.post('/login', {
      userEmail,
      password,
      encryptData
    });
  },
  
  async logout() {
    // 로그아웃 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.LOGOUT) + "; path=/";
    return await authApi.post('/logout');
  },

  async logoutAll() {
    // 모든 기기 로그아웃 기능 쿠키 설정
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.LOGOUT) + "; path=/";
    return await authApi.post('/logout-all');
  },

  /**
   * Refresh Token으로 Access Token 갱신
   * - 401 에러 시 Axios Interceptor에서 자동 호출
   */
  async refreshToken() {
    return await authApi.post('/refresh', {}, {
      skipErrorHandling: true,
      skipGlobalLoading: true
    });
  },

  async getMe() {
    // 서버에 GET /me 요청 -> JWT 쿠키 사용
    return await authApi.get('/me');
  },

  async getMeWithoutErrorHandling() {
    // 에러 처리 건너뛰기 옵션으로 JWT 유효성만 체크
    return await authApi.get('/me', { 
      skipErrorHandling: true,
      skipGlobalLoading: true 
    });
  },
  
  /**
   * RSA 공개키 조회
   */
  async getPublicKey() {
    return await authApi.get('/public-key', {
      skipErrorHandling: false,
      skipGlobalLoading: true
    });
  },
};
