import { authApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  // 유저 + 권한 정보를 한 번에 조회 (GET /user-permissions)
  async getUsersWithRoles() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.PERMISSION_LIST) + "; path=/";
    return await authApi.get('/user-permissions');
  },
  // 유저 + 권한 정보를 한 번에 조회 (PATCH /user-permissions)
  async patchUserWithRole(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.PERMISSION_UPDATE) + "; path=/";
    return await authApi.patch('/user-permissions', params);
  },
  // 유저 + 권한 추가 (POST /user-permissions)
  async createUserWithRole(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.PERMISSION_CREATE) + "; path=/";
    return await authApi.post('/user-permissions', params);
  },
  // 유저 + 권한 추가 (delete /user-permissions)
  // ※ Axios 등에서 DELETE 요청 시 본문을 보내려면 두 번째 인자로 객체를 넘겨야 함 ※
  //    DELETE 요청 시 { data: params }로 보내야 본문이
  //    { userId: 1, serviceName: 'receipt', roleNameDetail: '관리자' } 이런식으로 전달됨
  async deleteUserWithRole(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.AUTH.PERMISSION_DELETE) + "; path=/";
    return await authApi.delete('/user-permissions', { data: params });
  },
};
