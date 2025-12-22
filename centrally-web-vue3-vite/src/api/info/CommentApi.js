import { infoApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

/**
 * INFO 댓글 API
 */
export default {
  /**
   * 댓글 목록 조회
   */
  async getComments(postId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.COMMENT_LIST) + "; path=/";
    return await infoApi.get(`/posts/${postId}/comments`, { skipGlobalLoading: true });
  },

  /**
   * 댓글 등록 (parentCommentId는 선택)
   */
  async addComment({ postId, content, parentCommentId = null }) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.COMMENT_CREATE) + "; path=/";
    return await infoApi.post(`/posts/${postId}/comments`, { content, parentCommentId }, { skipGlobalLoading: true });
  },

  /**
   * 댓글 삭제
   */
  async deleteComment(commentId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.COMMENT_DELETE) + "; path=/";
    return await infoApi.delete(`/comments/${commentId}`, { skipGlobalLoading: true });
  },

  /**
   * 댓글 수정
   */
  async updateComment(commentId, { content }) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.COMMENT_UPDATE) + "; path=/";
    return await infoApi.patch(`/comments/${commentId}`, { content }, { skipGlobalLoading: true });
  },
};

