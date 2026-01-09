<template>
  <div class="employee-search-card">
    <div class="card-header">
      <i class="ri-search-line"></i>
      <span>{{ $t('common.sidebar.search.title') }}</span>
    </div>
    
    <!-- 사원 검색 입력 -->
    <div class="search-wrapper">
      <input
        ref="searchInputRef"
        v-model="searchQuery"
        @keydown="handleKeyDown"
        @focus="showResults = true"
        type="text"
        class="search-input"
        :placeholder="$t('common.sidebar.search.placeholder')"
        autocomplete="off"
      />
      
      <!-- 검색 결과 드롭다운 -->
      <ul
        v-show="showResults && searchQuery.trim().length >= 2"
        class="search-results"
      >
        <li v-if="searching" class="result-item loading">{{ $t('common.sidebar.search.loading') }}</li>
        <li v-else-if="searchResults.length === 0" class="result-item no-result">
          {{ $t('common.sidebar.search.noResults') }}
        </li>
        <li
          v-else
          v-for="(user, index) in searchResults"
          :key="user.userId"
          :class="['result-item', { active: index === selectedIndex }]"
          @click="handleSelectUser(user)"
          @mouseenter="selectedIndex = index"
        >
          <span class="user-name">{{ user.name }}</span>
          <span class="user-email">&nbsp;({{ user.email }})</span>
        </li>
      </ul>
    </div>
    
    <!-- 선택된 사원 카드 (검색창 아래에 표시) -->
    <div v-if="selectedEmployee" class="selected-employee-display">
      <button class="clear-selection" @click="clearSelection" :title="$t('common.sidebar.search.clearSelection')">
        <i class="ri-close-line"></i>
      </button>
      <div class="employee-card-content" @click="openDetail">
        <div class="employee-avatar">
          <img 
            v-if="selectedEmployee.profileImage" 
            :src="selectedEmployee.profileImage" 
            :alt="$t('common.sidebar.user.profileImageAlt')"
          />
          <i v-else class="ri-user-3-fill"></i>
        </div>
        <div class="employee-info">
          <div class="employee-name">{{ selectedEmployee.name }}</div>
          <div class="employee-meta">
            <span v-if="selectedEmployee.department">{{ selectedEmployee.department }}</span>
            <span v-if="selectedEmployee.position"> · {{ selectedEmployee.position }}</span>
          </div>
          <div class="employee-contact" v-if="selectedEmployee.email || selectedEmployee.phone">
            <div v-if="selectedEmployee.email" class="contact-item" :title="selectedEmployee.email">
              <i class="ri-mail-line"></i>
              <span class="ellipsis">{{ selectedEmployee.email }}</span>
            </div>
            <div v-if="selectedEmployee.phone" class="contact-item" :title="selectedEmployee.phone">
              <i class="ri-phone-line"></i>
              <span class="ellipsis">{{ selectedEmployee.phone }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 사원 상세 모달 -->
  <UserDetailModal
    :visible="isDetailModalVisible"
    :user="selectedUserForModal"
    @close="closeDetail"
  />
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';
import UserDetailModal from '@/components/hrm/UserDetailModal.vue';
import HrmUserApi from '@/api/hrm/UsersApi';
import { toast } from 'vue3-toastify';
import debounce from 'lodash/debounce';

const { t } = useI18n();

/**
 * 검색 관련 상태
 */
const searchQuery = ref('');
const searchResults = ref([]);
const showResults = ref(false);
const searching = ref(false);
const selectedIndex = ref(-1);
const searchInputRef = ref(null);

/**
 * 선택된 사원 정보
 */
const selectedEmployee = ref(null);
const isDetailModalVisible = ref(false);
const selectedUserForModal = ref(null);

/**
 * 백엔드 검색 (debounced)
 */
const performSearch = debounce(async (keyword) => {
  if (!keyword || keyword.trim().length < 2) {
    searchResults.value = [];
    selectedIndex.value = -1;
    return;
  }
  
  searching.value = true;
  try {
    const { data } = await HrmUserApi.searchUsers(keyword.trim(), {
      limit: 5,
      includeDisabled: false
    });
    
    searchResults.value = data.slice(0, 5); // 최대 5개
    selectedIndex.value = searchResults.value.length > 0 ? 0 : -1;
  } catch (error) {
    console.error('사원 검색 실패:', error);
    searchResults.value = [];
    selectedIndex.value = -1;
  } finally {
    searching.value = false;
  }
}, 300);

/**
 * 검색어 변경 감시
 */
watch(searchQuery, (newValue) => {
  performSearch(newValue);
});

/**
 * 키보드 네비게이션
 */
function handleKeyDown(event) {
  if (!showResults.value || searchResults.value.length === 0) return;
  
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault();
      selectedIndex.value = Math.min(selectedIndex.value + 1, searchResults.value.length - 1);
      break;
    case 'ArrowUp':
      event.preventDefault();
      selectedIndex.value = Math.max(selectedIndex.value - 1, 0);
      break;
    case 'Enter':
      event.preventDefault();
      if (selectedIndex.value >= 0 && selectedIndex.value < searchResults.value.length) {
        handleSelectUser(searchResults.value[selectedIndex.value]);
      }
      break;
    case 'Escape':
      event.preventDefault();
      showResults.value = false;
      break;
  }
}

/**
 * 사원 선택 핸들러
 */
async function handleSelectUser(user) {
  if (!user || !user.userId) return;
  
  showResults.value = false;
  searchQuery.value = '';
  
  try {
    // 전체 정보 다시 조회
    const response = await HrmUserApi.getUserById(user.userId);
    const userData = response.data;
    
    // department와 position 객체 처리
    let departmentName = '';
    if (userData.department) {
      if (typeof userData.department === 'string') {
        departmentName = userData.department;
      } else if (userData.department.departmentName) {
        departmentName = userData.department.departmentName;
      }
    }
    
    let positionName = '';
    if (userData.position) {
      if (typeof userData.position === 'string') {
        positionName = userData.position;
      } else if (userData.position.positionName) {
        positionName = userData.position.positionName;
      }
    }
    
    selectedEmployee.value = {
      userId: userData.userId,
      name: userData.name || user.name || '',
      department: departmentName,
      position: positionName,
      email: userData.email || user.email || '',
      phone: userData.phone || '',
      profileImage: userData.profileImg?.fileUrl || userData.profileImageUrl || ''
    };
  } catch (error) {
    console.error('사원 정보 조회 실패:', error);
    toast.error('사원 정보를 불러오지 못했습니다.');
  }
}

/**
 * 외부 클릭 시 검색 결과 닫기
 */
function handleClickOutside(event) {
  if (!searchInputRef.value) return;
  const searchCard = event.target.closest('.employee-search-card');
  if (!searchCard) {
    showResults.value = false;
  }
}

/**
 * 마운트 시 외부 클릭 이벤트 등록
 */
onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

/**
 * 언마운트 시 이벤트 제거
 */
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});

/**
 * 선택 취소
 */
function clearSelection() {
  selectedEmployee.value = null;
}

/**
 * 상세 정보 열기
 */
async function openDetail() {
  if (!selectedEmployee.value) return;
  
  try {
    const response = await HrmUserApi.getUserById(selectedEmployee.value.userId);
    selectedUserForModal.value = response.data;
    isDetailModalVisible.value = true;
  } catch (error) {
    console.error('사원 정보 조회 실패:', error);
    toast.error('사원 정보를 불러오지 못했습니다.');
  }
}

/**
 * 상세 정보 닫기
 */
function closeDetail() {
  isDetailModalVisible.value = false;
  selectedUserForModal.value = null;
}
</script>

<style scoped>
.employee-search-card {
  position: relative;
  margin: 12px 5px 12px 15px;
  padding: 16px;
  background: var(--theme-card-bg);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--theme-border);
  will-change: opacity, transform;
  transform: translate3d(0, 0, 0);
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s, 
              visibility 0s linear,
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: -16px -16px 15px -16px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--theme-border);
  border-top-left-radius: 12px !important;
  border-top-right-radius: 12px !important;
}

.card-header i {
  font-size: 14px;
  color: #667eea;
}

.card-header span {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--theme-text-primary);
}

/* 검색 입력 영역 */
.search-wrapper {
  position: relative;
  width: 100%;
}

.search-input {
  width: 100%;
  padding: 8px 10px;
  border: 2px solid #e1e8ed;
  border-radius: 8px;
  font-size: 0.75rem;
  transition: all 0.3s ease;
  background: var(--theme-input-bg);
}

.search-input:focus {
  outline: none;
  /* border-color: #667eea; */
  border-color: #575757;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-input::placeholder {
  color: #a0aec0;
}

/* 검색 결과 드롭다운 */
.search-results {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
  border: 1px solid #e1e8ed;
  list-style: none;
  padding: 0;
  margin: 0;
}

.result-item {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f3f5;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.75rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item:hover {
  background: #f8f9fa;
}

.result-item.active {
  background: #6b6b6b;
  color: white;
}

.result-item.loading,
.result-item.no-result {
  text-align: center;
  color: #718096;
  cursor: default;
  font-size: 0.7rem;
}

.result-item.loading:hover,
.result-item.no-result:hover {
  background: white;
}

.user-name {
  font-weight: 700;
  color: var(--theme-text-primary);
}

.result-item.active .user-name {
  color: white;
}

.user-email {
  font-size: 0.65rem;
  color: var(--theme-text-secondary);
}

.result-item.active .user-email {
  color: rgba(255, 255, 255, 0.8);
}

/* 선택된 사원 표시 영역 */
.selected-employee-display {
  position: relative;
  margin-top: 12px;
  padding: 12px;
  background: var(--theme-card-bg);
  border-radius: 10px;
  border: 1px solid var(--theme-border);
  transition: all 0.3s ease;
}

.selected-employee-display:hover {
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
  /* background: linear-gradient(90deg, #5d9bff 0%, #0063db 100%); */
  background: linear-gradient(135deg, #99a0ad 0%, #58606D 100%);
  transform: scale(1.02);
}

.selected-employee-display:hover .employee-name,
.selected-employee-display:hover .employee-meta,
.selected-employee-display:hover .contact-item {
  color: white;
}

.selected-employee-display:hover .contact-item i {
  color: rgba(255, 255, 255, 0.8);
}

.clear-selection {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.329);
  /* border: none; */
  /* border: 1px solid #a3a3a3; */
  border-radius: 50%;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10;
  padding: 0;
}

.clear-selection:hover {
  background: rgba(239, 68, 68, 0.9);
  color: white;
  transform: scale(1.1);
}

.clear-selection i {
  font-size: 14px;
}

.ri-user-3-fill {
  color: #9b9b9bcc !important;
}

.employee-card-content {
  display: flex;
  align-items: flex-start;
  gap: 13px;
  cursor: pointer;
}

.employee-avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  overflow: hidden;
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
  border: 1px solid #cacaca;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.employee-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.employee-avatar i {
  font-size: 22px;
  color: white;
}

.employee-info {
  flex: 1;
  min-width: 0;
}

.employee-name {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--theme-text-primary);
  margin-bottom: 3px;
  transition: color 0.3s ease;
}

.employee-meta {
  font-size: 0.7rem;
  color: var(--theme-text-secondary);
  line-height: 1.3;
  margin-bottom: 6px;
  transition: color 0.3s ease;
}

.employee-contact {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.65rem;
  color: var(--theme-text-secondary);
  transition: color 0.3s ease;
}

.contact-item i {
  font-size: 11px;
  color: #a0aec0;
  flex-shrink: 0;
  transition: color 0.3s ease;
}

.contact-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.ellipsis {
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  white-space: nowrap !important;
  display: block !important;
}
</style>

