import { receiptApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';
import qs from 'qs';

export default {
  // 영수증 조회
  async getReceipts(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.LIST) + "; path=/";
    return await receiptApi.get('/receipts', { params });
  },

  // 전체(개별) 사용자 영수증 상태포함 조회
  async getReceiptsWithStatus(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.LIST_WITH_STATUS) + "; path=/";
    return await receiptApi.get(`/receipts/with-status/all`, { params });
  },

  /* ─────────────────────────── ① 통합 검색 ───────────────────────────
     - statusCodes(다중) | startDate/endDate | yearMonth 를 모두 지원
     - indices:false → statusCodes=3&statusCodes=4 직렬화
  -------------------------------------------------------------------*/
  async searchUserReceipts(userId, params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.SEARCH_USER) + "; path=/";
    return await receiptApi.get(
      `/receipts/user/${userId}/search`,
      { params, paramsSerializer: p => qs.stringify(p, { indices:false }) }
    );
  },

  // 사용자 영수증 – 상태코드로 필터링
  //   params 예시: { statusCode: 1, page: 0, size: 10 }
  async getReceiptsByUserIdAndStatus(userId, params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.USER_LIST_STATUS) + "; path=/";
    return await receiptApi.get(`/receipts/user/${userId}/status`, { params });
  },

  // 사용자 영수증 조회
  async getReceiptsByUserId(userId, params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.USER_LIST) + "; path=/";
    return await receiptApi.get(`/receipts/user/${userId}`, { params });
  },

  // 사용자 영수증 추가
  async createReceipt(userId, formData) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.CREATE) + "; path=/";
    return await receiptApi.post(`/receipts/user/${userId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  // 사용자 영수증 수정
  async patchReceipt(userId, receiptId, formData) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.UPDATE) + "; path=/";
    return await receiptApi.patch(`/receipts/user/${userId}/${receiptId}`, formData, { 
      headers: { "Content-Type": "multipart/form-data" } 
    });
  },

  // 사용자 영수증 연도별 요약 조회
  // GET /receipts/user/{userId}/year-summary?year=2025
  async getReceiptByUserYearlySummary(userId, params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.YEAR_SUMMARY) + "; path=/";
    return await receiptApi.get(`/receipts/user/${userId}/year-summary`, { params });
  },

  // 특정 사용자 + 기간별 조회
  // GET /receipts/date-range?userId=...&startDate=...&endDate=...&page=...&size=...
  async getReceiptsBySearchUserDateRange(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.DATE_RANGE) + "; path=/";
    return await receiptApi.get(`/receipts/date-range`, { params });
  },

  // 사용자 영수증 삭제
  // DELETE /receipts/{receiptId}
  async deleteReceipt(receiptId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.DELETE) + "; path=/";
    return await receiptApi.delete(`/receipts/${receiptId}`);
  },

  /**
   * 단일 영수증 조회
   * GET /receipts/{receiptId}
   * @param {number|string} receiptId
   */
  async getReceiptById(receiptId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.DETAIL) + "; path=/";
    return await receiptApi.get(`/receipts/${receiptId}`);
  },

};
