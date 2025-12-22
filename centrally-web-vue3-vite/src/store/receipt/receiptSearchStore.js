import { defineStore } from 'pinia';

export const useReceiptSearchStore = defineStore('receiptSearchStore', {
  state: () => ({
    searchUserId: '',
    searchUserLabel: '',
    startDate: '',
    endDate: '',
    currentPage: 1,
    data: [],
    totalPages: 1,
  }),
  actions: {
    setSearchCondition({ userId, startDate, endDate, currentPage, userLabel }) {
      this.searchUserId = userId;
      this.searchUserLabel = userLabel;
      this.startDate = startDate;
      this.endDate = endDate;
      this.currentPage = currentPage;
    },
    setData({ data, totalPages }) {
      this.data = data;
      this.totalPages = totalPages;
    },
    clearData() {
      this.data = [];
      this.totalPages = 1;
      this.searchUserId = '';
      this.searchUserLabel = '';
      this.startDate = '';
      this.endDate = '';
      this.currentPage = 1;
    }
  }
});
