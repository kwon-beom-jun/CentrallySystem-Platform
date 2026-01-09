import { receiptApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {

  /**
   * 영수증을 신청 상태로 변경합니다. (부서/팀 정보 포함)
   * @param {number} receiptId 영수증 ID
   * @param {object} payload { departmentId, departmentName, teamId, teamName }
   */
  async requestReceipt(receiptId, payload) { //
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.REQUEST) + '; path=/';
    // POST /request/{id} 요청 시, 본문에 payload를 담아 전송
    return await receiptApi.post(`/request/${receiptId}`, payload);
  },

  // 사용자 영수증 상태 저장
  // POST /receipts/user/{userId}/decisions
  // body: [{ receiptId, decision, rejectReason }]
  async saveReceiptDecisions(userId, decisionList) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.STATUS_SAVE) + "; path=/";
    return await receiptApi.post(`/request/user/${userId}/decisions`, decisionList);
  },





  
  /**
   * 여러 유저들의 신청 영수증을 일괄 '승인' 처리 (날짜 필터)
   */
  async approvalUserReceipts(payload) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.BATCH_APPROVE) + "; path=/";
    // POST 요청으로 ID 목록을 request body에 담아 전송
    return await receiptApi.post('/request/bulk-users-approval', payload);
  },

  /**
   * 여러 유저들의 신청 영수증을 일괄 '반려' 처리 (날짜 필터)
   */
  async rejectUserReceipts(payload) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.BATCH_REJECT) + "; path=/";
    // POST 요청으로 ID 목록을 request body에 담아 전송
    return await receiptApi.post('/request/bulk-users-rejected', payload);
  },







  /**
   * 여러 영수증을 일괄 '마감' 처리합니다.
   * @param {number[]} receiptIds 영수증 ID 배열
   */
  async closeReceipts(receiptIds) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.BATCH_CLOSE) + "; path=/";
    // POST 요청으로 ID 목록을 request body에 담아 전송
    return await receiptApi.post('/request/bulk-receipts-close', receiptIds);
  },

  /**
   * 여러 영수증을 일괄 '반려' 처리합니다. (반려 사유 포함)
   * @param {number[]} receiptIds 영수증 ID 배열
   * @param {string} reason 반려 사유
   */
  async rejectReceipts(receiptIds, reason) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.BATCH_REJECT) + "; path=/";
    // 백엔드가 @RequestParam으로 reason을 받으므로 URL에 쿼리 파라미터로 추가
    const url = `/request/bulk-receipts-reject?reason=${encodeURIComponent(reason)}`;
    return await receiptApi.post(url, receiptIds);
  },

  /**
   * 사용자 비활성화에 따른 영수증 후속 처리를 수행
   * @param {object} data { receiptIds: number[], userToRemoveId: number }
   * @param {string} reason 반려 사유
   */
  async rejectAndRemoveApprover(data, reason) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.RECEIPT.USER_DEACTIVATE_PROCESS) + "; path=/";
    const url = `/request/bulk-reject-and-remove?reason=${encodeURIComponent(reason)}`;
    // POST 요청으로 data 객체({ receiptIds, userToRemoveId })를 request body에 담아 전송
    return await receiptApi.post(url, data);
  },

  /**
   * 영수증 상태를 관리자 권한으로 강제 변경
   * @param {number} receiptId  영수증 PK
   * @param {'WAITING'|'REQUEST'|'REJECTED'|'APPROVED'|'CLOSED'} newStatus
   */
  async forceChangeStatus(receiptId, newStatus, deptTeamDto) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.FORCE_STATUS_CHANGE) + '; path=/';
    return await receiptApi.put(`/request/${receiptId}/status/${newStatus}`, deptTeamDto);
  },

  // /**
  //  * 영수증 부서(팀) 업데이트
  //  * @param {number} receiptId 영수증 ID
  //  * @param {object} payload { 
  //  *  oldDepartmentId, oldDepartmentName, oldTeamId, oldTeamName 
  //  *  newDepartmentId, newDepartmentName, newTeamId, newTeamName 
  //  * }
  //  */
  // async updateReceiptsDeptTeam(receiptId, payload) { //
  //   document.cookie = 'X-Func-Vue=' + encodeURIComponent('영수증 부서(팀) 업데이트') + '; path=/';
  //   // POST /request/{id} 요청 시, 본문에 payload를 담아 전송
  //   return await receiptApi.post(`/request/${receiptId}/update/deptteam`, payload);
  // },
};
