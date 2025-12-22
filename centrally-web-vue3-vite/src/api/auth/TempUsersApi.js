// src/api/auth/TempUsersApi.js
import { authApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  /* ───────── 임시 사용자 조회 ───────── */
  async getUsers(params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.AUTH.TEMP_USER_LIST) + '; path=/';
    return await authApi.get('/users/temp', { params });
  },

  /* ───────── 임시 회원가입(사용자 스스로) ───────── */
  async createTempUser(params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.AUTH.TEMP_USER_CREATE) + '; path=/';
    return await authApi.post('/users/temp/join', params);
  },

  /* ───────── 임시 사용자 승인 ───────── */
  async approveUser(id) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.AUTH.TEMP_USER_APPROVE) + '; path=/';
    return await authApi.post(`/users/temp/${id}/approve`);
  },

  /* ───────── 임시 사용자 삭제 ───────── */
  async deleteUser(id) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.AUTH.TEMP_USER_DELETE) + '; path=/';
    return await authApi.delete(`/users/temp/${id}`);
  },
};
