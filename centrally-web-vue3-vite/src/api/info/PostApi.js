import { infoApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

/**
 * INFO 게시글 API
 */
export default {
  /**
   * 게시글 목록 조회
   */
  async getPosts(boardId, params) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.POST_LIST) + "; path=/";
    return await infoApi.get(`/boards/${boardId}/posts`, { params });
  },

  /**
   * 게시글 단건 조회
   */
  async getPost(postId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.POST_DETAIL) + "; path=/";
    return await infoApi.get(`/posts/${postId}`, { skipGlobalLoading: true });
  },

  /**
   * 게시글 등록 (파일 포함)
   */
  async createPost(boardId, formData) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.POST_CREATE) + "; path=/";
    return await infoApi.post(`/boards/${boardId}/posts`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  /**
   * 게시글 수정 (파일 추가/삭제 포함)
   */
  async updatePost({ postId, formData }) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.POST_UPDATE) + "; path=/";
    return await infoApi.patch(`/posts/${postId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  /**
   * 게시글 삭제
   */
  async deletePost(postId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.POST_DELETE) + "; path=/";
    return await infoApi.delete(`/posts/${postId}`);
  },

  /**
   * 게시글 고정/해제
   */
  async pinPost({ postId, pinned }) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.INFO.POST_PIN) + "; path=/";
    return await infoApi.post(`/posts/${postId}/pin`, pinned);
  },
};

