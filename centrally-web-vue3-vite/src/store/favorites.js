import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useAuthStore } from './auth';
import FavoriteMenuApi from '@/api/hrm/FavoriteMenuApi';
import UsersApi from '@/api/hrm/UsersApi';
import StylesApi from '@/api/hrm/StylesApi';
import { STYLE_CATEGORY } from '@/constants';

/**
 * 즐겨찾기 상태 관리 Store (백엔드 API 연동)
 */
export const useFavoritesStore = defineStore('favorites', () => {
  // 즐겨찾기 목록
  const favoriteMenus = ref([]);
  
  /**
   * 백엔드에서 즐겨찾기 불러오기
   */
  const loadFavorites = async () => {
    const authStore = useAuthStore();
    const userId = authStore.userId;
    
    if (!userId) {
      favoriteMenus.value = [];
      return;
    }

    try {
      const response = await FavoriteMenuApi.getFavoriteMenus(userId);
      favoriteMenus.value = response.data.map(item => ({
        id: item.favoriteId.toString(),
        path: item.menuPath,
        label: item.menuLabel, // i18n 키 (예: 'menu.info.notice')
        icon: item.menuIcon,
        workspace: item.workspace,
        category: item.category, // i18n 키 (예: 'menu.category.board')
        color: item.color,
        addedAt: item.createdAt
      }));
    } catch (error) {
      console.error('즐겨찾기 불러오기 실패:', error);
      favoriteMenus.value = [];
    }
  };

  /**
   * 즐겨찾기 추가
   */
  const addFavorite = async (menu) => {
    const authStore = useAuthStore();
    const userId = authStore.userId;
    
    if (!userId) return;

    // 중복 체크
    const exists = favoriteMenus.value.some(item => item.path === menu.path);
    if (exists) return;

    try {
      const response = await FavoriteMenuApi.createFavoriteMenu(userId, {
        menuPath: menu.path,
        menuLabel: menu.i18nKey || menu.label, // i18n 키 우선 사용, 없으면 기존 label
        menuIcon: menu.icon,
        workspace: menu.workspace,
        category: menu.categoryI18nKey || menu.category // i18n 키 우선 사용, 없으면 기존 category
      });
      
      // 로컬 상태 업데이트
      const newItem = response.data;
      favoriteMenus.value.push({
        id: newItem.favoriteId.toString(),
        path: newItem.menuPath,
        label: newItem.menuLabel, // i18n 키
        icon: newItem.menuIcon,
        workspace: newItem.workspace,
        category: newItem.category, // i18n 키
        addedAt: newItem.createdAt
      });
    } catch (error) {
      console.error('즐겨찾기 추가 실패:', error);
    }
  };

  /**
   * 즐겨찾기 제거
   */
  const removeFavorite = async (path) => {
    const authStore = useAuthStore();
    const userId = authStore.userId;
    
    if (!userId) return;

    try {
      await FavoriteMenuApi.deleteFavoriteMenu(userId, path);
      
      // 로컬 상태 업데이트
      const index = favoriteMenus.value.findIndex(item => item.path === path);
      if (index !== -1) {
        favoriteMenus.value.splice(index, 1);
      }
    } catch (error) {
      console.error('즐겨찾기 제거 실패:', error);
    }
  };

  /**
   * 즐겨찾기 토글
   */
  const toggleFavorite = async (menu) => {
    const exists = favoriteMenus.value.some(item => item.path === menu.path);
    if (exists) {
      await removeFavorite(menu.path);
    } else {
      await addFavorite(menu);
    }
  };

  /**
   * 특정 경로가 즐겨찾기인지 확인
   */
  const isFavorite = (path) => {
    return favoriteMenus.value.some(item => item.path === path);
  };

  /**
   * 즐겨찾기 전체 삭제
   */
  const clearFavorites = async () => {
    const authStore = useAuthStore();
    const userId = authStore.userId;
    
    if (!userId) return;

    try {
      await FavoriteMenuApi.clearFavoriteMenus(userId);
      favoriteMenus.value = [];
    } catch (error) {
      console.error('즐겨찾기 전체 삭제 실패:', error);
    }
  };

  /**
   * 즐겨찾기 개수
   */
  const favoritesCount = computed(() => favoriteMenus.value.length);

  /**
   * 워크스페이스별로 그룹화된 즐겨찾기
   */
  const favoritesByWorkspace = computed(() => {
    const grouped = {};
    favoriteMenus.value.forEach(item => {
      if (!grouped[item.workspace]) {
        grouped[item.workspace] = [];
      }
      grouped[item.workspace].push(item);
    });
    return grouped;
  });

  /**
   * 즐겨찾기 순서 변경
   */
  const reorderFavorites = async (newOrder) => {
    const authStore = useAuthStore();
    const userId = authStore.userId;
    
    if (!userId) return;

    try {
      // favoriteId 배열 생성
      const favoriteIds = newOrder.map(item => parseInt(item.id));
      await FavoriteMenuApi.reorderFavoriteMenus(userId, favoriteIds);
      
      // 로컬 상태 업데이트
      favoriteMenus.value = newOrder;
    } catch (error) {
      console.error('즐겨찾기 순서 변경 실패:', error);
    }
  };

  /**
   * 즐겨찾기 색상 업데이트
   */
  const updateFavoriteColor = (favoriteId, color) => {
    const favorite = favoriteMenus.value.find(item => item.id === favoriteId.toString());
    if (favorite) {
      favorite.color = color;
    }
  };

  /**
   * 카드 스타일 업데이트
   */
  const updateCardStyle = async (cardStyle) => {
    const authStore = useAuthStore();
    const userId = authStore.userId;
    
    if (!userId) return;

    try {
      // 새로운 StylesApi 사용
      await StylesApi.updateUserStyle(userId, STYLE_CATEGORY.MAIN_CARD, cardStyle);
      
      // AuthStore의 cardStyle 업데이트
      authStore.updateCardStyle(cardStyle);
    } catch (error) {
      console.error('카드 스타일 업데이트 실패:', error);
      throw error;
    }
  };

  return {
    favoriteMenus,
    favoritesCount,
    favoritesByWorkspace,
    addFavorite,
    removeFavorite,
    toggleFavorite,
    isFavorite,
    clearFavorites,
    loadFavorites,
    reorderFavorites,
    updateFavoriteColor,
    updateCardStyle
  };
});

