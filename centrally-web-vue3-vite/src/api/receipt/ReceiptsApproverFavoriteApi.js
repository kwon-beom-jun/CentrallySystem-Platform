import { receiptApi } from '@/api/apiConfig'
import { FUNCTIONS } from '@/config/activityConfig';

export default {

  async getApproverFavorites(ownerUserId) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.APPROVER_FAVORITE_LIST) + '; path=/'
    return await receiptApi.get(`/approver-favorites/${ownerUserId}`)
  },

  async createApproverFavorite(payload) {
    // payload: { ownerUserId, favoriteUserId, favoriteUserName, email, department, team }
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.APPROVER_FAVORITE_ADD) + '; path=/'
    return await receiptApi.post('/approver-favorites', payload)
  },

  async deleteApproverFavorite(ownerUserId, favoriteUserId) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.APPROVER_FAVORITE_REMOVE) + '; path=/'
    return await receiptApi.delete('/approver-favorites', { params: { ownerUserId, favoriteUserId } })
  },
  
  async updateApproverFavorite(ownerUserId, list) {
    // list: Array<{ favoriteUserId, stepNo }>
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.APPROVER_FAVORITE_REORDER) + '; path=/'
    return await receiptApi.patch(`/approver-favorites/order/${ownerUserId}`, list)
  },

  // 특정 사용자(favoriteUserId)가 즐겨찾기 대상인 모든 레코드 일괄 제거
  async deleteAllByFavoriteUser(favoriteUserId) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.APPROVER_FAVORITE_CLEAN) + '; path=/'
    return await receiptApi.delete(`/approver-favorites/internal/remove-by-fav/${favoriteUserId}`)
  }
}


