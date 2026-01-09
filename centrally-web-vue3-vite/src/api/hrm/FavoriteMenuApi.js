import { hrmApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

/**
 * 즐겨찾기 메뉴 API
 */
export default {
  /**
   * 즐겨찾기 목록 조회
   */
  async getFavoriteMenus(userId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.FAVORITE_LIST) + "; path=/";
    return await hrmApi.get(`/users/${userId}/favorite-menus`);
  },

  /**
   * 즐겨찾기 생성
   */
  async createFavoriteMenu(userId, menuData) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.FAVORITE_CREATE) + "; path=/";
    return await hrmApi.post(`/users/${userId}/favorite-menus`, menuData);
  },

  /**
   * 즐겨찾기 삭제
   */
  async deleteFavoriteMenu(userId, menuPath) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.FAVORITE_DELETE) + "; path=/";
    return await hrmApi.delete(`/users/${userId}/favorite-menus`, {
      params: { menuPath }
    });
  },

  /**
   * 모든 즐겨찾기 삭제
   */
  async clearFavoriteMenus(userId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.FAVORITE_DELETE_ALL) + "; path=/";
    return await hrmApi.delete(`/users/${userId}/favorite-menus/all`);
  },

  /**
   * 즐겨찾기 여부 확인
   */
  async isFavorite(userId, menuPath) {
    const response = await hrmApi.get(`/users/${userId}/favorite-menus/check`, {
      params: { menuPath },
      skipGlobalLoading: true
    });
    return response.data;
  },

  /**
   * 즐겨찾기 순서 변경
   */
  async reorderFavoriteMenus(userId, favoriteIds) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.FAVORITE_REORDER) + "; path=/";
    return await hrmApi.put(`/users/${userId}/favorite-menus/reorder`, favoriteIds);
  },

  /**
   * 즐겨찾기 색상 변경
   */
  async updateFavoriteMenuColor(userId, favoriteId, color) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.FAVORITE_COLOR) + "; path=/";
    return await hrmApi.patch(`/users/${userId}/favorite-menus/${favoriteId}/color`, { color });
  }
};

