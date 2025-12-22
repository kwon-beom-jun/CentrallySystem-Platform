import { hrmApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

const receiptFlag = () =>
  import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true' ? '?syncReceipt=true' : '';

export default {
  // 전체 사용자 목록 조회 (GET /users)
  async getUsers(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.USER_LIST) + "; path=/";
    return await hrmApi.get('/users', { params });
  },

  // 전체 사용자(비활성 포함) 목록 조회 (GET /users)
  async getUsersAll(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.USER_LIST_WITH_INACTIVE) + "; path=/";
    return await hrmApi.get('/users?includeDisabled=true', { params });
  },

  // 특정 사용자 조회 (GET /users/{id})
  async getUserById(id) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.USER_DETAIL) + "; path=/";
    return await hrmApi.get(`/users/${id}`);
  },

  // 전체 사용자 목록 조회 Basic (GET /users)
  async getUsersBasic() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.USER_LIST) + "; path=/";
    return await hrmApi.get('/users/basic');
  },

  // 여러 사용자 ID를 받아 한 번에 조회(비활성화 포함) (POST /users/bulk-lookup)
  async getUsersByIds(ids) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.USER_BULK) + "; path=/";
    const response = await hrmApi.post('/users/bulk-lookup', ids, { skipGlobalLoading: true });
    return response.data;
  },
  
  // 여러 사용자 ID를 받아 한 번에 조회(비활성화 및 권한 포함) (POST /users/bulk-lookup)
  async getUsersByIdsAndRole(ids) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.USER_BULK_WITH_ROLES) + "; path=/";
    const response = await hrmApi.post('/users/bulk-lookup/role', ids);
    return response.data;
  },

  // 여러 사용자 ID를 받아 활성 사용자만 권한과 함께 조회 (POST /users/bulk-lookup/active/role)
  async getActiveUsersWithRoles(ids) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.USER_ACTIVE_WITH_ROLES) + "; path=/";
    const response = await hrmApi.post('/users/bulk-lookup/active-role', ids);
    return response.data;
  },

  /**
   * 여러 사용자 ID를 받아 비활성 상태인 사용자 목록을 조회합니다.
   * @param {number[]} ids - 확인할 사용자 ID 배열
   * @returns {Promise<AxiosResponse<any>>} - 비활성화된 사용자 정보 배열을 포함하는 Promise
   */
  async getUsersDisabled(ids) {
      document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.USER_INACTIVE_CHECK) + "; path=/";
      return await hrmApi.post('/users/check-disabled', ids);
  },

  // 특정 사용자 일부 필드 업데이트 (PATCH /users/{id})
  async patchUser(id, updates) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.USER_PATCH) + "; path=/";
    return await hrmApi.patch(`/users/${id}`, updates, { params:{ syncReceipt:true }});
  },

  // 프로필 이미지 업로드 (POST /users/{id}/profile-img)
  async uploadProfileImage(id, file) {
    const formData = new FormData();
    formData.append('file', file);
    return await hrmApi.post(`/users/${id}/profile-img`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  // ────────────────────────────────────────
  //  실시간 검색
  //  kw   : 검색어
  //  roles: ['ROLE_A', 'ROLE_B']  <-- 배열 그대로 넘김
  // ────────────────────────────────────────
  async searchUsers(keyword, opts = {}) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.HRM.USER_SEARCH) + '; path=/';
    const params = {
      kw:              keyword,
      limit:           opts.limit ?? 20,
      includeDisabled: opts.includeDisabled ?? false,
    }
    // service가 "실제 값"일 때만 추가
    if (opts.service)
      params.service = opts.service
    // roles가 빈 배열이 아닐 때만 추가
    if (Array.isArray(opts.roles) && opts.roles.length > 0)
      params.roles = opts.roles
    return await hrmApi.get('/users/search', {
      params,
      skipGlobalLoading: true,
    })
  },

  /**
   * 사용자 카드 스타일 변경
   */
  async updateCardStyle(userId, cardStyle) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.USER_PATCH) + "; path=/";
    return await hrmApi.patch(`/users/${userId}/card-style`, { cardStyle });
  },
};
