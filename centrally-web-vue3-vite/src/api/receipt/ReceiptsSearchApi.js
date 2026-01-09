import { receiptApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';
import qs from 'qs';

export default {
  
  /**
   * 결재자 관점 히스토리 개요(기간 요약 + 내 차례 건수 + 최근 활동)
   * GET /receipts-search/approver/{approverId}/history-overview
   */
  async getHistoryOverview(approverId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.HISTORY_OVERVIEW) + '; path=/';
    return await receiptApi.get(`/receipts-search/approver/${approverId}/history-overview`, { params });
  },
  
  /**
   * 🔍 [집계] 결재자(approverId) 관점 ­– 신청자별 금액/건수 요약
   *
   * GET /receipts-search
   * ├─ 필수: approverId, startDate, endDate
   * └─ 선택: userId(특정 신청자), statusCodes[]=WAITING | REQUEST …
   *
   * @param {object} params
   *   {Number}  approverId  결재자 본인 ID
   *   {String}  startDate   yyyy-MM-dd
   *   {String}  endDate     yyyy-MM-dd
   *   {Number?} userId      특정 신청자(선택)
   *   {Array?}  statusCodes 상태코드 배열 (반복 쿼리스트링)
   */
  async getReceiptSummaryByNameAndDate(params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.SUMMARY_BY_NAME_DATE) + "; path=/";
    return await receiptApi.get('/receipts-search', {
      params,
      /* 배열 → statusCodes=APPROVED&statusCodes=CLOSED */
      paramsSerializer: p => qs.stringify(p, { arrayFormat: 'repeat' })
    });
  },

  /**
   * 🔍 [목록] 내 결재선에 포함된 영수증 목록 조회
   *
   * GET /receipts-search/approver/{approverId}/date-range
   * ├─ 필수: startDate, endDate
   * └─ 선택: userId, statusCode(단일)
   *
   * @param {Number} approverId 결재자 본인 ID
   * @param {object} params     page·size·userId·startDate·endDate·statusCode
   */
  async getPendingReceiptsByApprover(approverId, params) {
    // →  GET /receipts/approver/{approverId}/date-range?userId&startDate&endDate&page&size
    document.cookie =
      'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.MY_APPROVAL_DETAIL) + '; path=/';
    return await receiptApi.get(`/receipts-search/approver/${approverId}/date-range`, {
      params,
      // 배열 ⇒  statusCodes=APPROVED&statusCodes=CLOSED
      paramsSerializer: p => qs.stringify(p, { arrayFormat:'repeat' })
    });
  },

  async getReceiptsByUserAndDateRange(userId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.USER_APPROVED_LIST) + '; path=/';
    return await receiptApi.get(`/receipts-search/user/${userId}/date-range`, {
      params,
      paramsSerializer: p => qs.stringify(p, { arrayFormat:'repeat' })
    });
  },

  /**
   * 🔍 [목록] (단축) ‘내 차례’인 영수증만 조회
   *    – controller에서 statusCode=REQUEST 로 필터링됨
   */
  async getMyPendingByDate(approverId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.MY_PENDING) + '; path=/';
    return await receiptApi.get(
      `/receipts-search/approver/${approverId}/date-range`,
      {
        params,
        /* 배열 → statusCodes=REQUEST&statusCodes=APPROVED */
        paramsSerializer: p => qs.stringify(p, { arrayFormat: 'repeat' })
      }
    );
  },

  /**
   * 🔍 [목록] (단축) ‘내 차례’인 [결재 or 합의] 영수증 조회
   *    – controller에서 [결재 or 합의] 로 필터링됨
   */
  async getMyApprovalPendingByDate(approverId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.MY_PENDING) + '; path=/';
    return await receiptApi.get(
      `/receipts-search/approver/${approverId}/date-range/pending`,
      {
        params,
        /* 배열 → statusCodes=REQUEST&statusCodes=APPROVED */
        paramsSerializer: p => qs.stringify(p, { arrayFormat: 'repeat' })
      }
    );
  },

  /**
   * 🔍 [통계] 내 결재선 금액·건수 요약(평균 포함)
   *
   * GET /receipts-search/approver/{approverId}/date-range/summary
   */
  async getMyPendingSummary(approverId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.MY_PENDING_SUMMARY) + '; path=/';
    return await receiptApi.get(`/receipts-search/approver/${approverId}/date-range/summary`, { params });
  },

  /* ‘내 차례 – 신청자별 요약’ */
  async getMyPendingSummaryRows(approverId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.MY_PENDING_SUMMARY_ROWS) + '; path=/';
    return await receiptApi.get(`/receipts-search/approver/${approverId}/pending-summary`, 
      // skipNulls:true 옵션을 주면 null 값 파라미터가 자동으로 제외
      { params, 
        paramsSerializer: p => qs.stringify(p, { arrayFormat:'repeat', skipNulls:true }) }
    );
  },

  /**
   * 🔍 [통계] 내 결재선(결재 or 합의) 금액·건수 요약(평균 포함)
   *
   * GET /receipts-search/approver/{approverId}/date-range/summary/pending
   */
  async getMyPendingSummaryByRoles(approverId, params) {
    document.cookie =
      'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.MY_PENDING_BY_ROLE) + '; path=/';
    return await receiptApi.get(
      `/receipts-search/approver/${approverId}/date-range/summary/pending`,
      { params,
        paramsSerializer: p => qs.stringify(p, { arrayFormat:'repeat', skipNulls:true }) }
    );
  },

  /** 대리결재자 관점 ‘내 차례’ 목록 */
  async getMyApprovalPendingByDateAsDelegate(delegateId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DELEGATE_APPROVAL_PENDING) + '; path=/';
    return await receiptApi.get(
      `/receipts-search/delegate/${delegateId}/date-range/pending`,
      { params, paramsSerializer: p => qs.stringify(p, { arrayFormat: 'repeat' }) }
    );
  },

  /** 대리결재자 관점 전체 상태 목록 */
  async getByDelegateAndDateRange(delegateId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DELEGATE_DATE_RANGE) + '; path=/';
    return await receiptApi.get(
      `/receipts-search/delegate/${delegateId}/date-range`,
      { params, paramsSerializer: p => qs.stringify(p, { arrayFormat: 'repeat', skipNulls:true }) }
    );
  },

  /** 대리결재자 관점 신청자별 요약 (전체 상태) */
  async getSummaryForDelegate(delegateId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DELEGATE_SUMMARY_ALL) + '; path=/';
    return await receiptApi.get(
      `/receipts-search/delegate/${delegateId}/summary`,
      { params, paramsSerializer: p => qs.stringify(p, { arrayFormat:'repeat', skipNulls:true }) }
    );
  },

  /** 대리결재자 관점 신청자별 요약 */
  async getMyPendingSummaryByUserAsDelegate(delegateId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.DELEGATE_SUMMARY_PENDING) + '; path=/';
    return await receiptApi.get(
      `/receipts-search/delegate/${delegateId}/pending-summary`,
      { params, paramsSerializer: p => qs.stringify(p, { arrayFormat:'repeat', skipNulls:true }) }
    );
  },


  /** 🔍 (전사) 승인·마감 요약 */
  async getApprClosedSummaryAll(params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.APPROVAL_CLOSED_SUMMARY) + '; path=/';
    return await receiptApi.get('/receipts-search/appr-closed', { 
      params,
      paramsSerializer: p => qs.stringify(p, { arrayFormat: 'repeat' })
    });
  },


  /**
   * 🔍 [통계] 전사(또는 필터조건) 기준 전체 합계
   *
   * GET /receipts-search/totals
   */
  async getTotals(params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.ALL_STATISTICS) + '; path=/';
    return await receiptApi.get(`/receipts-search/totals`, { params });
  },


  /**
   * 🔍 [목록] 내가 포함된 결재선 조회 (영수증 상태 대기, 신청, 반려)
   *
   * GET /receipts-search/approver/{approverId}/pending-list
   *
   * @param {Number} approverId 결재자 본인 ID
   * @param {object} params     { page, size }
   */
  async getMyPendingApprovals(approverId, params) {
      document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.MY_PENDING_LIST) + '; path=/';
      return await receiptApi.get(`/receipts-search/approver/${approverId}/pending-list`, {
          params // { page: 0, size: 10 }
      });
  },

  /**
   * 🔔 [알림] 내 차례 결재·합의 건수
   * GET /receipts-search/approver/{approverId}/pending-counts
   */
  async getMyPendingCounts(approverId) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.MY_PENDING_COUNT) + '; path=/';
    return await receiptApi.get(`/receipts-search/approver/${approverId}/pending-counts`);
  },
};
