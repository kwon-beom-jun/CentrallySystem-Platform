import { authApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  // 전체 사용자 목록 조회 (GET /users)
  async getUsers(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.USER_LIST) + "; path=/";
    return await authApi.get('/users', { params });
  },

  // 특정 사용자 조회 (GET /users/{id})
  async getUserById(id) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.USER_DETAIL) + "; path=/";
    return await authApi.get(`/users/${id}`);
  },

  // 이메일로 사용자 조회 (GET /users/search?email={email})
  async getUserByEmail(email) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.USER_BY_EMAIL) + "; path=/";
    const response = await authApi.get('/users/search', { params: { email } });
    return response.data;
  },
  
  // 특정 사용자 정보와 권한 목록 조회 (GET /users/{id}/with-roles)
  async getUserWithRoles(id) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.USER_WITH_ROLES) + "; path=/";
    return await authApi.get(`/users/${id}/with-roles`);
  },

  // 특정 사용자 및 소셜 목록 조회 (GET /users/${id}/social)
  async getUserSocialByEmail(email) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.USER_SOCIAL) + "; path=/";
    return await authApi.get(`/users/${email}/social`);
  },

  // 특정 사용자의 소셜 로그인 삭제 (DELETE /users/{id}/social/{socialName})
  async deleteUserSocial(userId, socialName) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.USER_SOCIAL_DELETE) + "; path=/";
    // 백엔드 실제 URL은 상황에 맞춰 변경 (예: "/users/{id}/social/{socialName}" 등)
    return await authApi.delete(`/users/${userId}/social/${socialName}`);
  },

  // ※ 임시 사용자에서 진행해서 사용안함 ※
  // 새 사용자 추가 (회원가입) - POST /users/temp/join
  async createUser(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.USER_CREATE) + "; path=/";
    return await authApi.post('/users/join', params);
  },

  // 특정 사용자 전체 업데이트 (PUT /users/{id})
  async putUser(id, userData) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.USER_UPDATE) + "; path=/";
    return await authApi.put(`/users/${id}`, userData);
  },

  // 특정 사용자 일부 업데이트 (PATCH /users/{id})
  async patchUser(id, patchData) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.USER_PATCH) + "; path=/";
    return await authApi.patch(`/users/${id}`, patchData);
  },

  // 특정 사용자 삭제 (DELETE /users/{id})
  async deleteUser(id) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.USER_DELETE) + "; path=/";
    return await authApi.delete(`/users/${id}`);
  },

  // 비밀번호 확인 (POST /auth/verify-password)
  async verifyPassword(password) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.PASSWORD_VERIFY) + "; path=/";
    return await authApi.post('/verify-password', { password });
  },
};
