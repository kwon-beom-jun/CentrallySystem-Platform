// src/api/receipt/ReceiptsCategoryApi.js
import { receiptApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

export default {

  /* 카테고리 전체 조회(활성 목록) */
  async getCategories() {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.CATEGORY_LIST) + '; path=/';
    return receiptApi.get('/categories');
  },
  
  /* 카테고리 전체 조회(비활성 포함 목록) */
  async getCategoriesWithDisabled() {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.CATEGORY_LIST_ALL) + '; path=/';
    return receiptApi.get('/categories/all');
  },

  /* 카테고리 삭제(비활성화) */
  async deleteCategory(id) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.CATEGORY_DELETE) + '; path=/';
    return receiptApi.delete(`/categories/${id}/delete`);
  },

  /* 신규 등록 */
  async createCategory(payload) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.CATEGORY_CREATE) + '; path=/';
    return receiptApi.post('/categories', payload);          // POST /categories
  },

  /* 수정 */
  async updateCategory(id, payload) {
    document.cookie = 'X-Func-Vue=' + encodeURIComponent(FUNCTIONS.RECEIPT.CATEGORY_UPDATE) + '; path=/';
    return receiptApi.put(`/categories/${id}`, payload);     // PUT /categories/{id}
  }
};
