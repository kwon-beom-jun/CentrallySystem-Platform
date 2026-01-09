<template>
  <div class="content content-wrapper">
    <!-- 페이지 타이틀 -->
    <PageTitle 
      :title="$t('hrm.favoriteMenu.title')"
      :subtitle="$t('hrm.favoriteMenu.subtitle')"
      icon="ri-star-line"
    />

    <!-- 안내 문구 -->
    <InfoNotice 
      color="blue"
      icon="ri-information-line"
      :text="$t('hrm.favoriteMenu.info')"
      marginBottom="20px"
    />

    <!-- 메뉴 검색 섹션 -->
    <div class="search-section">
      <h3 class="section-title">
        <i class="ri-search-line"></i>
        {{ $t('hrm.favoriteMenu.menuSearch') }}
      </h3>
      <div class="search-box">
        <input 
          type="text"
          v-model="searchQuery"
          :placeholder="$t('hrm.favoriteMenu.searchPlaceholder')"
          class="search-input"
          @input="handleSearch"
        />
        <i class="ri-search-line search-icon"></i>
      </div>

      <!-- 검색 결과 -->
      <div v-if="searchQuery.trim()" class="search-results">
        <div v-if="filteredMenus.length > 0">
          <div class="search-result-info">
            <i class="ri-information-line"></i>
            <span>{{ $t('hrm.favoriteMenu.searchResultInfo') }}</span>
          </div>
          <div class="menu-chips">
          <div
            v-for="menu in filteredMenus"
            :key="menu.path"
            class="menu-chip"
            :class="{ 'is-favorite': isFavorite(menu.path) }"
          >
            <div class="chip-icon">
              <i :class="menu.icon"></i>
            </div>
            <div class="chip-info">
              <div class="chip-label" v-html="highlightText($t(menu.i18nKey || menu.label), searchQuery.trim())"></div>
              <div class="chip-category" v-html="highlightText($t(menu.categoryI18nKey || menu.category), searchQuery.trim())"></div>
            </div>
            <button 
              class="chip-favorite-btn"
              :class="{ 'active': isFavorite(menu.path) }"
              @click="toggleMenuFavorite(menu)"
              :title="isFavorite(menu.path) ? $t('hrm.favoriteMenu.removeFavorite') : $t('hrm.favoriteMenu.addFavorite')"
            >
              <i :class="isFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
            </button>
          </div>
          </div>
        </div>
        <div v-else class="no-results box-shadow-gray">
          <i class="ri-inbox-line"></i>
          <p>{{ $t('hrm.favoriteMenu.noSearchResults') }}</p>
        </div>
      </div>
    </div>

    <!-- 즐겨찾기 목록 -->
    <h3 class="section-title">
      <i class="ri-star-fill"></i>
      {{ $t('hrm.favoriteMenu.myFavorites') }}
    </h3>
    <div v-if="favoriteMenus.length > 0" class="favorite-list-section">
      <draggable
        v-model="favoriteMenus"
        @start="onDragStart"
        @end="onDragEnd"
        item-key="favoriteId"
        tag="div"
        class="favorite-list"
        handle=".drag-handle"
        ghost-class="sortable-ghost"
        drag-class="sortable-drag"
      >
        <template #item="{ element, index }">
          <div class="favorite-item" :class="`color-${element.color || 'gray'}`">
            <!-- 드래그 핸들 -->
            <div class="drag-handle">
              <i class="ri-draggable"></i>
            </div>

            <!-- 메뉴 아이콘 -->
            <div class="menu-icon">
              <i :class="element.menuIcon"></i>
            </div>

            <!-- 메뉴 정보 -->
            <div class="menu-info">
              <div class="menu-label">{{ $t(element.menuLabel) }}</div>
              <div class="menu-category">{{ $t(element.category) }}</div>
            </div>

            <!-- 색상 선택 버튼 -->
            <button 
              class="color-button"
              @click="openColorPicker(element)"
              :title="$t('hrm.favoriteMenu.colorSelect')"
            >
              <i class="ri-palette-line"></i>
            </button>

            <!-- 제거 버튼 -->
            <button 
              class="delete-button"
              @click="openDeleteConfirm(element)"
              :title="$t('hrm.favoriteMenu.remove')"
            >
              <i class="ri-delete-bin-line"></i>
            </button>
          </div>
        </template>
      </draggable>
    </div>

    <!-- 즐겨찾기 없을 때 -->
    <div v-else class="empty-state box-shadow-gray">
      <i class="ri-star-line"></i>
      <p>{{ $t('hrm.favoriteMenu.noFavorites') }}</p>
      <p class="sub-text">{{ $t('hrm.favoriteMenu.noFavoritesSub') }}</p>
    </div>

    <!-- 카드 디자인 선택 섹션 -->
    <h3 class="section-title">
      <i class="ri-layout-line"></i>
      {{ $t('hrm.favoriteMenu.mobileCardDesign') }}
    </h3>
    <div class="card-style-section">
      <div class="current-style-info">
        <span class="info-label">{{ $t('hrm.favoriteMenu.currentStyle') }}</span>
        <span class="info-value">{{ currentStyleLabel }}</span>
      </div>
      <button class="btn-change-style" @click="openStyleModal">
        <i class="ri-palette-line"></i>
        {{ $t('hrm.favoriteMenu.changeDesign') }}
      </button>
    </div>

  </div>

  <!-- 색상 선택 모달 -->
  <AlertModal
    :isVisible="colorPickerVisible"
    :disableBackgroundClose="false"
    :title="$t('hrm.favoriteMenu.colorSelect')"
    :confirmText="$t('hrm.favoriteMenu.select')"
    :cancelText="$t('common.button.cancel')"
    @close="colorPickerVisible = false"
    @confirm="applyColor"
  >
    <template #body>
      <div class="color-picker-grid">
        <div
          v-for="color in colorOptions"
          :key="color.name"
          class="color-option"
          :class="{ active: selectedColor === color.name }"
          @click="selectedColor = color.name"
        >
          <div 
            class="color-preview"
            :style="{ 
              background: `linear-gradient(135deg, ${color.light} 0%, ${color.dark} 100%)`,
              border: `1px solid ${color.border}`
            }"
          >
            <i v-if="selectedColor === color.name" class="ri-check-line"></i>
          </div>
          <span class="color-name">{{ color.label }}</span>
        </div>
      </div>
    </template>
  </AlertModal>

  <!-- 삭제 확인 모달 -->
  <AlertModal
    :isVisible="deleteConfirmVisible"
    :disableBackgroundClose="true"
    :title="$t('hrm.favoriteMenu.removeFavorite')"
    :confirmText="$t('hrm.favoriteMenu.remove')"
    :cancelText="$t('common.button.cancel')"
    @close="deleteConfirmVisible = false"
    @confirm="deleteFavorite"
  >
    <template #body>
      <strong>{{ $t(itemToDelete?.menuLabel) }}</strong>{{ $t('hrm.favoriteMenu.removeConfirm') }}
    </template>
  </AlertModal>

  <!-- 카드 스타일 선택 모달 -->
  <CardStyleSelectorModal
    :visible="styleModalVisible"
    :currentStyle="authStore.cardStyle || 'default'"
    @close="closeStyleModal"
    @confirm="confirmStyleChange"
  />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/store/auth';
import { useFavoritesStore } from '@/store/favorites';
import { canShow as checkPermission } from '@/utils/roleUtils';
import { getAllMenus } from '@/config/menuConfig';
import { toast } from 'vue3-toastify';
import draggable from 'vuedraggable';
import PageTitle from '@/components/common/title/PageTitle.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import CardStyleSelectorModal from '@/components/hrm/CardStyleSelectorModal.vue';
import InfoNotice from '@/components/common/notice/InfoNotice.vue';
import FavoriteMenuApi from '@/api/hrm/FavoriteMenuApi';
import StylesApi from '@/api/hrm/StylesApi';
import { useHrmStore } from '@/store/hrm';
import { STYLE_CATEGORY } from '@/constants';

const router = useRouter();
const authStore = useAuthStore();
const hrmStore = useHrmStore();
const favoritesStore = useFavoritesStore();
const { t } = useI18n();

/* ================= 데이터 ================= */
const favoriteMenus = ref([]);
const originalOrder = ref([]); // 원본 순서 저장
const colorPickerVisible = ref(false);
const deleteConfirmVisible = ref(false);
const selectedColor = ref('gray');
const itemToEdit = ref(null);
const itemToDelete = ref(null);
const searchQuery = ref('');
const styleModalVisible = ref(false);

/* ================= 권한 및 로컬 테스트 ================= */
const roles = computed(() => authStore.roles);

/**
 * 권한 체크 (roleUtils.js의 canShow 사용)
 */
const canShow = (roleArr = []) => {
  return checkPermission(roles.value, roleArr);
};

/* ================= 전체 메뉴 목록 (menuConfig.js에서 import) ================= */
const allMenus = getAllMenus();

/**
 * 권한에 맞게 필터링된 메뉴 목록
 */
const availableMenus = computed(() => {
  return allMenus.filter(menu => canShow(menu.roles));
});

/**
 * 검색 필터링된 메뉴 (LIKE 검색, 앞글자 우선순위, 최대 6개)
 */
const filteredMenus = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();
  if (!query) return [];
  
  // LIKE 검색: 번역된 메뉴명 또는 카테고리에 포함
  const filtered = availableMenus.value.filter(menu => {
    const menuLabel = t(menu.i18nKey || menu.label).toLowerCase();
    const menuCategory = t(menu.categoryI18nKey || menu.category).toLowerCase();
    return menuLabel.includes(query) || menuCategory.includes(query);
  });
  
  // 앞글자 우선순위로 정렬
  const sorted = filtered.sort((a, b) => {
    const aLabel = t(a.i18nKey || a.label).toLowerCase();
    const bLabel = t(b.i18nKey || b.label).toLowerCase();
    const aCategory = t(a.categoryI18nKey || a.category).toLowerCase();
    const bCategory = t(b.categoryI18nKey || b.category).toLowerCase();
    
    // 메뉴명이 검색어로 시작하는 경우 우선
    const aLabelStarts = aLabel.startsWith(query);
    const bLabelStarts = bLabel.startsWith(query);
    
    if (aLabelStarts && !bLabelStarts) return -1;
    if (!aLabelStarts && bLabelStarts) return 1;
    
    // 카테고리가 검색어로 시작하는 경우 그 다음 우선
    const aCategoryStarts = aCategory.startsWith(query);
    const bCategoryStarts = bCategory.startsWith(query);
    
    if (aCategoryStarts && !bCategoryStarts) return -1;
    if (!aCategoryStarts && bCategoryStarts) return 1;
    
    // 나머지는 메뉴명 기준 알파벳 순
    return aLabel.localeCompare(bLabel);
  });
  
  // 최대 6개까지만 반환
  return sorted.slice(0, 6);
});

/**
 * 해당 경로가 즐겨찾기에 있는지 확인
 */
const isFavorite = (path) => {
  return favoriteMenus.value.some(menu => menu.menuPath === path);
};

/**
 * 검색 입력 처리
 */
const handleSearch = () => {
  // 검색어가 입력되면 자동으로 filteredMenus가 업데이트됨
};

/**
 * 검색어 하이라이트 (파란색 강조)
 */
const highlightText = (text, query) => {
  if (!query || !text) return text;
  
  const regex = new RegExp(`(${query})`, 'gi');
  return text.replace(regex, '<span class="highlight-text">$1</span>');
};

/**
 * 메뉴 즐겨찾기 토글
 */
const toggleMenuFavorite = async (menu) => {
  const userId = authStore.getUserId;
  
  if (isFavorite(menu.path)) {
    // 즐겨찾기 제거
    try {
      await FavoriteMenuApi.deleteFavoriteMenu(userId, menu.path);
      favoriteMenus.value = favoriteMenus.value.filter(
        (fav) => fav.menuPath !== menu.path
      );
      // 스토어 업데이트
      favoritesStore.loadFavorites();
      toast.success(t('hrm.favoriteMenu.removeSuccess'));
    } catch (e) {
      console.error(e);
      toast.error(t('hrm.favoriteMenu.deleteFailed'));
    }
  } else {
    // 즐겨찾기 추가
    try {
      await FavoriteMenuApi.createFavoriteMenu(userId, {
        menuPath: menu.path,
        menuLabel: menu.i18nKey || menu.label,
        menuIcon: menu.icon,
        workspace: menu.workspace,
        category: menu.categoryI18nKey || menu.category,
        color: 'gray'
      });
      // 목록 다시 로드
      await fetchFavoriteMenus();
      // 스토어 업데이트
      favoritesStore.loadFavorites();
      toast.success(t('hrm.favoriteMenu.addSuccess'));
    } catch (e) {
      console.error(e);
      toast.error(t('hrm.favoriteMenu.maxReached'));
    }
  }
};

/* ================= 색상 옵션 ================= */
const colorOptions = [
  { name: 'gray', label: t('hrm.favoriteMenu.colorGray'), light: '#f8fafc', dark: '#f1f5f9', border: '#e2e8f0' },
  { name: 'blue', label: t('hrm.favoriteMenu.colorBlue'), light: '#eff6ff', dark: '#e0f2fe', border: '#bfdbfe' },
  {
    name: 'purple',
    label: t('hrm.favoriteMenu.colorPurple'),
    light: '#f5f3ff',
    dark: '#ede9fe',
    border: '#ddd6fe',
  },
  { name: 'pink', label: t('hrm.favoriteMenu.colorPink'), light: '#fdf2f8', dark: '#fce7f3', border: '#fbcfe8' },
  {
    name: 'yellow',
    label: t('hrm.favoriteMenu.colorYellow'),
    light: '#fffbeb',
    dark: '#fef3c7',
    border: '#fde68a',
  },
  {
    name: 'green',
    label: t('hrm.favoriteMenu.colorGreen'),
    light: '#f0fdf4',
    dark: '#dcfce7',
    border: '#bbf7d0',
  },
  { name: 'cyan', label: t('hrm.favoriteMenu.colorCyan'), light: '#f0fdfa', dark: '#ccfbf1', border: '#99f6e4' },
  { name: 'red', label: t('hrm.favoriteMenu.colorRed'), light: '#fef2f2', dark: '#fee2e2', border: '#fecaca' },
];

/**
 * 즐겨찾기 목록 조회
 */
async function fetchFavoriteMenus() {
  try {
    const userId = authStore.getUserId;
    const res = await FavoriteMenuApi.getFavoriteMenus(userId);
    favoriteMenus.value = res.data || [];
    // 원본 순서 저장
    originalOrder.value = favoriteMenus.value.map((menu) => menu.favoriteId);
  } catch (e) {
    console.error(e);
    toast.error(t('hrm.favoriteMenu.loadFailed'));
  }
}

/**
 * 드래그 시작 시 원본 순서 저장
 */
function onDragStart() {
  originalOrder.value = favoriteMenus.value.map((menu) => menu.favoriteId);
}

/**
 * 드래그 종료 시 순서가 변경됐으면 즉시 저장
 */
async function onDragEnd() {
  const currentOrder = favoriteMenus.value.map((menu) => menu.favoriteId);
  
  // 순서가 변경되지 않았으면 저장하지 않음
  const isOrderChanged = originalOrder.value.some((id, index) => id !== currentOrder[index]);
  
  if (!isOrderChanged) {
    return;
  }
  
  try {
    const userId = authStore.getUserId;
    await FavoriteMenuApi.reorderFavoriteMenus(userId, currentOrder);
    
    // 원본 순서 업데이트
    originalOrder.value = [...currentOrder];
    
    // 스토어 업데이트 (SecondSidebar, MainPage 즉시 반영)
    favoritesStore.loadFavorites();
    
    toast.success(t('hrm.favoriteMenu.orderSaveSuccess'));
  } catch (e) {
    console.error(e);
    toast.error(t('hrm.favoriteMenu.orderSaveFailed'));
  }
}

/**
 * 색상 선택 모달 열기
 */
function openColorPicker(item) {
  itemToEdit.value = item;
  selectedColor.value = item.color || 'gray';
  colorPickerVisible.value = true;
}

/**
 * 색상 적용
 */
async function applyColor() {
  if (!itemToEdit.value) return;

  try {
    const userId = authStore.getUserId;
    await FavoriteMenuApi.updateFavoriteMenuColor(
      userId,
      itemToEdit.value.favoriteId,
      selectedColor.value,
    );

    // 로컬 데이터 업데이트
    itemToEdit.value.color = selectedColor.value;
    
    // 스토어 업데이트 (SecondSidebar, MainPage 즉시 반영)
    favoritesStore.updateFavoriteColor(itemToEdit.value.favoriteId, selectedColor.value);
    
    colorPickerVisible.value = false;
    toast.success(t('hrm.favoriteMenu.colorChangeSuccess'));
  } catch (e) {
    console.error(e);
    toast.error(t('hrm.favoriteMenu.colorChangeFailed'));
  }
}

/**
 * 제거 확인 모달 열기
 */
function openDeleteConfirm(item) {
  itemToDelete.value = item;
  deleteConfirmVisible.value = true;
}

/**
 * 즐겨찾기 제거
 */
async function deleteFavorite() {
  if (!itemToDelete.value) return;

  try {
    const userId = authStore.getUserId;
    await FavoriteMenuApi.deleteFavoriteMenu(userId, itemToDelete.value.menuPath);

    // 목록에서 제거
    favoriteMenus.value = favoriteMenus.value.filter(
      (menu) => menu.favoriteId !== itemToDelete.value.favoriteId,
    );

    // 스토어 업데이트 (SecondSidebar, MainPage 즉시 반영)
    favoritesStore.loadFavorites();

    deleteConfirmVisible.value = false;
    itemToDelete.value = null;
    toast.success(t('hrm.favoriteMenu.deleteSuccess'));
  } catch (e) {
    console.error(e);
    toast.error(t('hrm.favoriteMenu.deleteFailed'));
  }
}

/**
 * 현재 카드 스타일 라벨
 */
const currentStyleLabel = computed(() => {
  const cardStyle = authStore.cardStyle || 'default';
  const styleLabels = {
    'default': t('common.label.default'),
    'ver1': t('common.label.style1'),
    'ver2': t('common.label.style2'),
    'ver3': t('common.label.style3'),
    'ver4': t('common.label.style4')
  };
  return styleLabels[cardStyle] || t('common.label.default');
});

/**
 * 스타일 선택 모달 열기
 */
function openStyleModal() {
  styleModalVisible.value = true;
}

/**
 * 스타일 선택 모달 닫기
 */
function closeStyleModal() {
  styleModalVisible.value = false;
}

/**
 * 카드 스타일 변경 확인
 */
async function confirmStyleChange(newStyle) {
  try {
    await favoritesStore.updateCardStyle(newStyle);
    toast.success(t('hrm.favoriteMenu.cardDesignChangeSuccess'));
    styleModalVisible.value = false;
  } catch (error) {
    console.error('카드 스타일 변경 실패:', error);
    toast.error(t('hrm.favoriteMenu.cardDesignChangeFailed'));
  }
}

onMounted(() => {
  fetchFavoriteMenus();
});
</script>

<style scoped>

/* 섹션 타이틀 */
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.95rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 12px;
  margin-top: 30px;
}

.section-title:first-of-type {
  margin-top: 0;
}

.section-title i {
  font-size: 1rem;
  color: #3b82f6;
}

/* 카드 스타일 섹션 */
.card-style-section {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px 18px;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.btn-change-style {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 14px;
  background: #3b82f6;
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-change-style:hover {
  background: #2563eb;
}

.btn-change-style i {
  font-size: 0.95rem;
}

.current-style-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  flex: 1;
}

.info-label {
  color: #64748b;
  font-weight: 500;
}

.info-value {
  color: #ffffff;
  font-weight: 700;
  background: #8f8f8f;
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

/* 검색 섹션 */
.search-section {
  margin-bottom: 30px;
}

/* 검색 박스 */
.search-box {
  position: relative;
  margin-bottom: 7px;
}

.search-input {
  width: 100%;
  padding: 10px 40px 10px 14px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 0.85rem;
  transition: all 0.2s;
  background: #fff;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-icon {
  position: absolute;
  right: 13px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.05rem;
  color: #9ca3af;
  pointer-events: none;
}

/* 검색 결과 */
.search-results {
  margin-top: 7px;
}

/* 검색 결과 안내 문구 */
.search-result-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 6px;
  font-size: 0.78rem;
  color: #0369a1;
  margin-bottom: 8px;
}

.search-result-info i {
  font-size: 0.9rem;
  flex-shrink: 0;
}

/* 메뉴 칩 그리드 - 데스크톱: 3열 x 2행 = 6개 */
.menu-chips {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 7px;
}

/* 데스크톱: 7번째 아이템부터 숨김 (6개만 표시, 3열 x 2행) */
.menu-chips > .menu-chip:nth-child(n+7) {
  display: none;
}

/* 메뉴 칩 */
.menu-chip {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s;
  cursor: pointer;
}

.menu-chip:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
  transform: translateY(-1px);
}

.menu-chip.is-favorite {
  background: linear-gradient(135deg, #eff6ff 0%, #e0f2fe 100%);
  border-color: #bfdbfe;
}

/* 칩 아이콘 */
.chip-icon {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 7px;
  font-size: 1.05rem;
  color: #3b82f6;
  flex-shrink: 0;
}

/* 칩 정보 */
.chip-info {
  flex: 1;
  min-width: 0;
}

.chip-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chip-category {
  font-size: 0.72rem;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 검색어 하이라이트 */
.chip-label :deep(.highlight-text),
.chip-category :deep(.highlight-text) {
  color: #3b82f6;
  font-weight: 700;
  background: rgba(59, 130, 246, 0.1);
  padding: 1px 2px;
  border-radius: 2px;
}

/* 칩 즐겨찾기 버튼 */
.chip-favorite-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  font-size: 1rem;
  color: #9ca3af;
}

.chip-favorite-btn:hover {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  transform: scale(1.1);
}

.chip-favorite-btn.active {
  color: #f59e0b;
}

.chip-favorite-btn.active:hover {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

/* 검색 결과 없음 */
.no-results {
  text-align: center;
  padding: 10px 20px;
  color: #9ca3af;
  min-height: 178px;
  justify-content: center;
  display: flex;
  flex-direction: column;
}

.no-results i {
  font-size: 3rem;
  color: #d1d5db;
}

.no-results p {
  font-size: 0.88rem;
  color: #6b7280;
}

/* 즐겨찾기 목록 */
.favorite-list-section {
  margin-top: 15px;
  margin-bottom: 20px;
}

.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.favorite-item {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 14px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

/* 드래그 핸들 */
.drag-handle {
  cursor: grab;
  font-size: 1.15rem;
  color: #9ca3af;
  display: flex;
  align-items: center;
  transition: color 0.2s;
}

.drag-handle:hover {
  color: #718096;
}

.drag-handle:active {
  cursor: grabbing;
}

.favorite-item:hover .drag-handle {
  color: #4a5568;
}

/* 메뉴 아이콘 */
.menu-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 9px;
  font-size: 1.25rem;
  color: #3b82f6;
  flex-shrink: 0;
}

/* 메뉴 정보 */
.menu-info {
  flex: 1;
  min-width: 0;
}

.menu-label {
  font-size: 0.88rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.menu-category {
  font-size: 0.75rem;
  font-weight: 800;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 버튼들 */
.color-button,
.delete-button {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 7px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  font-size: 1.05rem;
}

.color-button {
  background: #fff;
  color: #6366f1;
  border: 1px solid #e5e7eb;
}

.color-button:hover {
  background: #f9fafb;
}

.color-button:active {
  transform: scale(0.95);
  background: #f3f4f6;
}

.delete-button {
  background: #fff;
  color: #ef4444;
  border: 1px solid #e5e7eb;
}

.delete-button:hover {
  background: #fef2f2;
}

.delete-button:active {
  transform: scale(0.95);
  background: #fee2e2;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 20px 20px 40px 20px;
  color: #9ca3af;
}

.empty-state i {
  font-size: 3.5rem;
  margin-bottom: 16px;
  color: #d1d5db;
}

.empty-state p {
  font-size: 0.92rem;
  margin-bottom: 6px;
  color: #6b7280;
}

.empty-state .sub-text {
  font-size: 0.8rem;
  color: #9ca3af;
}

/* 색상 선택 그리드 */
.color-picker-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  padding: 15px 0;
}

.color-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.color-preview {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.color-option.active .color-preview {
  transform: scale(1.15);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.25);
}

.color-preview i {
  font-size: 1.8rem;
  color: #fff;
  filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.3));
}

.color-name {
  font-size: 0.8rem;
  color: #6b7280;
  font-weight: 500;
}

/* 드래그 중인 요소 스타일 - 마우스 포인터를 따라다니는 요소 */
/* .sortable-drag {
  opacity: 1 !important;
  cursor: grabbing !important;
}
.sortable-drag .favorite-item {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2) !important;
  transform: scale(1.05) rotate(2deg);
  border: 2px solid #000000 !important;
  background: #fff !important;
}
.sortable-ghost {
  opacity: 0.4;
}
.sortable-ghost .favorite-item {
  background: #f1f5f9 !important;
  border: 2px dashed #cbd5e1 !important;
  box-shadow: none !important;
} */

/* 모바일 대응 */
@media (max-width: 650px) {

  .section-title {
    font-size: 0.85rem;
    gap: 5px;
    margin-bottom: 10px;
  }

  .section-title i {
    font-size: 0.9rem;
  }

  .card-style-section {
    padding: 12px 14px;
    flex-wrap: wrap;
  }

  .current-style-info {
    font-size: 0.7rem;
    flex: 1;
    min-width: 120px;
  }

  .info-label {
    font-size: 0.7rem;
  }

  .info-value {
    font-size: 0.7rem;
    padding: 3px 8px;
  }

  .btn-change-style {
    font-size: 0.6rem;
    padding: 5px 8px;
    gap: 3px;
    flex-shrink: 0;
  }

  .btn-change-style i {
    font-size: 0.7rem;
  }

  .search-input {
    padding: 9px 38px 9px 12px;
    font-size: 0.8rem;
  }

  .search-input::placeholder {
    font-size: 0.7rem;
  }

  .search-icon {
    right: 11px;
    font-size: 0.95rem;
  }

  .search-results {
    min-height: 110px;
  }

  .search-result-info {
    padding: 7px 10px;
    font-size: 0.7rem;
    margin-bottom: 7px;
    gap: 5px;
  }

  .search-result-info i {
    font-size: 0.8rem;
  }

  .menu-chips {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  /* 모바일: 5번째 아이템부터 숨김 (4개만 표시, 1열 x 4행) */
  .menu-chips > .menu-chip:nth-child(n+5) {
    display: none;
  }

  .menu-chip {
    padding: 9px 11px;
    gap: 7px;
  }

  .chip-icon {
    width: 30px;
    height: 30px;
    font-size: 0.95rem;
    border-radius: 6px;
  }

  .chip-label {
    font-size: 0.8rem;
  }

  .chip-category {
    font-size: 0.68rem;
  }

  .chip-favorite-btn {
    width: 30px;
    height: 30px;
    font-size: 0.9rem;
  }

  .no-results {
    min-height: 110px;
  }

  .no-results i {
    font-size: 2.2rem;
  }

  .no-results p {
    font-size: 0.8rem;
  }

  .favorite-item {
    padding: 11px;
    gap: 9px;
  }

  .drag-handle {
    font-size: 1rem;
  }

  .menu-icon {
    width: 34px;
    height: 34px;
    font-size: 1.1rem;
    border-radius: 8px;
  }

  .menu-label {
    font-size: 0.78rem;
    margin-bottom: 2px;
  }

  .menu-category {
    font-size: 0.68rem;
  }

  .color-button,
  .delete-button {
    width: 32px;
    height: 32px;
    font-size: 0.95rem;
    border-radius: 6px;
  }

  .empty-state {
    padding: 10px 20px 25px 20px;
  }

  .empty-state i {
    font-size: 2.2rem;
    margin-bottom: 12px;
  }

  .empty-state p {
    font-size: 0.85rem;
    margin-bottom: 5px;
  }

  .empty-state .sub-text {
    font-size: 0.72rem;
  }

  .color-picker-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
    padding: 10px 0;
  }

  .color-preview {
    width: 50px;
    height: 50px;
  }

  .color-preview i {
    font-size: 1.5rem;
  }

  .color-name {
    font-size: 0.7rem;
  }
}

/* 다크모드 스타일 */
body[data-theme="dark"] .menu-label {
  color: #e2e2e2 !important;
}

body[data-theme="dark"] .chip-label {
  color: #e2e2e2 !important;
}

/* 매우 작은 화면 (400px 이하) */
@media (max-width: 400px) {
  .color-picker-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 6px;
    padding: 8px 0;
  }

  .color-preview {
    width: 42px;
    height: 42px;
    border-radius: 10px;
  }

  .color-preview i {
    font-size: 1.2rem;
  }

  .color-name {
    font-size: 0.62rem;
  }
}
</style>

