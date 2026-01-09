import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useLoadingStore = defineStore('loading', () => {
  // 현재 로딩 중인 요청(또는 동작)의 개수
  const requestCount = ref(0);

  // 로딩 상태: requestCount가 1 이상이면 로딩 중
  const isLoading = computed(() => requestCount.value > 0);

  // (1) 요청 시작 시 사용
  function startLoading() {
    requestCount.value++;
  }

  // (2) 요청 종료 시 사용
  function stopLoading() {
    if (requestCount.value > 0) {
      requestCount.value--;
    }
  }

  // (3) 로딩 강제 종료 (에러 등으로 꼬였을 때)
  function resetLoading() {
    requestCount.value = 0;
  }

  return {
    isLoading,
    startLoading,
    stopLoading,
    resetLoading,
  };
});
