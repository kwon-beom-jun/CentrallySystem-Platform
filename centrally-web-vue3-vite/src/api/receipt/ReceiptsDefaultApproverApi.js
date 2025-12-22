import { receiptApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  /* 목록 조회 – GET /default-approvers */
  async getDefaultApprovers(params = {}) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DEFAULT_APPROVER_LIST) + '; path=/'
    // stepNo 오름차순으로 정렬해서 가져오도록 쿼리 파라미터 강제 주입
    return await receiptApi.get('/default-approvers', { params: { sort: 'stepNo,asc', ...params }})
  },

  /* 특정 사용자가 고정 합의자인지 확인 */
  async checkIfDefaultApprover(userId) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DEFAULT_APPROVER_CHECK) + '; path=/';
    return await receiptApi.get(`/default-approvers/check/${userId}`);
  },

  /* 등록 – POST /default-approvers  */
  async createDefaultApprovers(payload) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DEFAULT_APPROVER_CREATE) + '; path=/'
    return await receiptApi.post('/default-approvers', payload)
  },

  /* 삭제 – DELETE /default-approvers/{id} */
  async deleteDefaultApprovers(userId) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DEFAULT_APPROVER_DELETE) + '; path=/'
    return await receiptApi.delete(`/default-approvers/${userId}`)
  },

  /* 순서(배열) 한번에 저장 – PATCH /default-approvers/order */
  async reorderDefaultApprovers(orderList) {
    // orderList: Array<{userId, stepNo}>
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DEFAULT_APPROVER_REORDER) + '; path=/'
    return await receiptApi.patch('/default-approvers/order', orderList)
  },
}

