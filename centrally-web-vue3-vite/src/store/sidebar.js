import { defineStore } from 'pinia';
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';

/**
 * 사이드바 상태 관리 스토어
 */
export const useSidebarStore = defineStore('sidebar', () => {
  const route = useRoute();
  
  // 현재 선택된 워크스페이스 키
  const selectedWorkspace = ref('home');

  /**
   * 워크스페이스 선택
   */
  const selectWorkspace = (key) => {
    selectedWorkspace.value = key;
  };

  /**
   * 현재 경로에서 워크스페이스 자동 결정
   */
  const updateWorkspaceFromRoute = (path) => {
    if (!path) return;
    
    // 라우터 메타에 workspace가 있으면 우선 사용
    if (route.meta?.workspace) {
      selectedWorkspace.value = route.meta.workspace;
      return;
    }
    
    if (path.startsWith('/receipt')) {
      selectedWorkspace.value = 'receipt';
    } else if (path.startsWith('/hrm/notice') || path.startsWith('/guide')) {
      selectedWorkspace.value = 'notice';
    } else if (path.startsWith('/hrm') || path.startsWith('/auth/temp-user-approvals')) {
      selectedWorkspace.value = 'management';
    } else if (path.startsWith('/system')) {
      selectedWorkspace.value = 'system';
    } else if (path.startsWith('/main') || path === '/') {
      selectedWorkspace.value = 'home';
    }
  };

  // 라우트 변경 감지
  watch(() => route.path, (newPath) => {
    updateWorkspaceFromRoute(newPath);
  }, { immediate: true });
  
  // 라우트 메타 변경도 감지
  watch(() => route.meta, () => {
    updateWorkspaceFromRoute(route.path);
  });

  /**
   * 사이드바 닫기 (index.js에서 사용하는 sidebarStore.closeSidebar() 라우터 호환용)
   */
  const closeSidebar = () => {
    // 현재는 워크스페이스 스토어이므로 별도 동작 없음
    // 필요시 여기에 사이드바 닫기 로직 추가
  };

  return {
    selectedWorkspace,
    selectWorkspace,
    updateWorkspaceFromRoute,
    closeSidebar,
  };
});
