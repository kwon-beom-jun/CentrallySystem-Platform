import { infoApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

/**
 * INFO 게시판 API
 */
export default {
  /**
   * 게시판 목록 조회
   */
  async getBoards() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.BOARD_LIST) + "; path=/";
    return await infoApi.get('/boards');
  },

  /**
   * 게시판 단건 조회 (코드로)
   */
  async getBoardByCode(boardCode) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.BOARD_DETAIL) + "; path=/";
    return await infoApi.get(`/boards/${boardCode}`);
  },
};

