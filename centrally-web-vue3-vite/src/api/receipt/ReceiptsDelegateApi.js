import { receiptApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  /** 활성 대리결재자 조회 (204 No Content 가능) */
  async getActive(principalUserId) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DELEGATE_ACTIVE) + '; path=/';
    return await receiptApi.get(`/delegate-approvals/${principalUserId}`, {
      // 필요 시 글로벌 로딩 스킵 옵션 사용 가능
      skipGlobalLoading: true,
      validateStatus: status => status >= 200 && status < 500,
    });
  },

  /** 나를 대리자로 지정한 활성 원결재자 목록 */
  async getPrincipalsByDelegate(delegateUserId) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DELEGATE_PRINCIPAL_LIST) + '; path=/';
    return await receiptApi.get(`/delegate-approvals/by-delegate/${delegateUserId}`);
  },

  /** 대리결재 매핑 저장 */
  async saveMapping(payload) {
    // payload: { principalUserId, delegateUserId, effectiveFrom?, effectiveTo? }
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DELEGATE_SAVE) + '; path=/';
    return await receiptApi.post('/delegate-approvals', payload);
  },

  /** 대리결재 매핑 삭제 */
  async deleteMapping(id) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DELEGATE_DELETE) + '; path=/';
    return await receiptApi.delete(`/delegate-approvals/${id}`);
  },

  /** 특정 사용자 관련 모든 대리결재 매핑 소프트 삭제 (원/대리 모두) */
  async softDeleteAllByUser(userId) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DELEGATE_DELETE_ALL) + '; path=/';
    return await receiptApi.delete(`/delegate-approvals/by-user/${userId}`);
  },
};




