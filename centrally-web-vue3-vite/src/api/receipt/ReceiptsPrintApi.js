import { receiptApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {
  
  /**
   * 영수증 ID 배열만 서버로 전송합니다.
   */
  async approvalSummaryExcelDownload(userId, params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.PRINT_APPROVAL_SUMMARY) + '; path=/';
    return await receiptApi.post(
      `/excel/${userId}/approval/summary`,
      params,
      { responseType: 'blob' }
    );
  },

  
  /**
   * CEO 보고용 엑셀 다운로드
   */
  async downloadCeoReport(params) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.PRINT_CEO_REPORT) + '; path=/';
    return await receiptApi.post(
      `/excel/ceo-report`, // 새 엔드포인트
      params, // { month, data }
      { responseType: 'blob' }
    );
  },

  
};
