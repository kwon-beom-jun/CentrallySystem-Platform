import { authApi, hrmApi, infoApi, receiptApi } from '@/api/apiConfig';

/**
 * Kafka Outbox 조회 API
 * - 각 서비스(auth/hrm/info/receipt)의 베이스 URL을 사용합니다.
 * - 엔드포인트 경로: '/outbox'
 */

function pickApi(service) {
  switch (service) {
    case 'auth':    return authApi;
    case 'hrm':     return hrmApi;
    case 'info':    return infoApi;
    case 'receipt':
    case 'rcpt':    return receiptApi;
    default:        return authApi;
  }
}

export default {
  /**
   * Outbox 목록 조회
   * @param {'auth'|'hrm'|'info'|'receipt'} service
   * @param {object} params { page, size, status, fromDate, toDate, aggregateType, eventType }
   */
  async getOutbox(service, params) {
    const api = pickApi(service);
    return await api.get('/outbox', { params });
  },
};


