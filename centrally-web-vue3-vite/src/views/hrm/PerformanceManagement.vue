<template>
  <div>
    <!-- 메인 콘텐츠 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('hrm.performanceDetail.title')"
        :subtitle="(selectedUserLabel ? `${selectedUserLabel}${$t('hrm.performanceDetail.subtitle')}` : $t('hrm.performanceDetail.searchPlaceholder')) + $t('hrm.performanceDetail.refreshNote')"
        icon="ri-bar-chart-2-line"
      />

      <div class="d-flex justify-content-between align-items-start">
        <!-- 실적 추가 버튼 (관리자 + 사용자 선택 시) -->
        <DefaultButton 
          v-if="isAdmin && selectedUserLabel"
          align="left"
          @click="openCreateModal"
        >
          {{ $t('hrm.performanceDetail.add') }}
        </DefaultButton>

        <p></p>
        <!-- (1) 검색 영역: UserSearchDropdown 컴포넌트 사용 -->
        <UserSearchDropdown
          class="mb-3"
          :keepSearchValue="false"
          :includeCurrentUser="true"
          @userSelected="onUserSelected"
        />
      </div>

      <hr v-if="!selectedUserLabel" />

      <!-- 검색을 하지 않았을 때(사용자 선택 X) 이미지 표시 -->
      <div v-if="!selectedUserLabel" class="d-flex justify-content-center mt-5">
        <img
          src="/img/hrm/PerformanceManagement/004.png"
          :alt="$t('hrm.performanceDetail.noSearchImage')"
          class="placeholder-image"
        />
      </div>

      <!-- (2) 실적 목록: 사용자가 선택되었을 때만 -->
      <DefaultTable
        v-if="selectedUserLabel"
        :columns="columns"
        :data="data"
        :rowClick="(item) => openEditModal(item)"
        :bodyFontSize="'0.7rem'"
        :fixedHeader="true"
        :heightAdjust="50"
        dynamic-style
        :noDataImageHeight="514"
      />

      <!-- 실적 추가 모달 -->
      <PerformanceManagementCreateModal
        :visible="createModalVisible"
        :formData="formData"
        @close="createModalVisible = false"
        @submit="submitPerformance"
      />

      <!-- 실적 수정/삭제 모달 -->
      <PerformanceManagementDeleteModifyModal
        :visible="editModalVisible"
        :editDataProp="editData"
        @close="editModalVisible = false"
        @save="submitEdit"
        @delete="removeItem"
      />

      <!-- 검색 실패 AlertModal -->
      <AlertModal
        :isVisible="resultModalVisible"
        :disableBackgroundClose="true"
        :title="$t('hrm.performanceDetail.searchFailed')"
        :confirmText="$t('common.button.confirm')"
        @confirm="resultModalVisible = false"
      >
        <template #body>
          {{ $t('hrm.performanceDetail.loadFailed') }}
        </template>
      </AlertModal>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

// 모달 컴포넌트들
import PerformanceManagementCreateModal from '@/components/hrm/PerformanceManagementCreateModal.vue';
import PerformanceManagementDeleteModifyModal from '@/components/hrm/PerformanceManagementDeleteModifyModal.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';
// 버튼/테이블
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTable from '@/components/common/table/DefaultTable.vue';
// 새로운 검색 컴포넌트
import UserSearchDropdown from '@/components/auth/UserSearchDropdown.vue';

// API
import PerformanceApi from "@/api/hrm/PerformanceApi";
import { toast } from 'vue3-toastify';
import { useViewStateStore } from '@/store/viewState'

const { t } = useI18n();

// 상태 변수
const resultModalVisible = ref(false);
const viewState = useViewStateStore();

// 사용자 선택 결과 (UserSearchDropdown에서 넘어옴)
const selectedUserId = ref('');
const selectedUserLabel = ref('');  // "홍길동 (hong@...)" 등

// 사용자 및 권한 조회

// 관리자 여부
const userRole = ref('admin');
const isAdmin = computed(() => userRole.value === 'admin');


// 실적 목록
const data = ref([]);
const editData = ref({});

// 실적 추가 모달
const createModalVisible = ref(false);
const formData = ref({
  fromDate: '',
  toDate: '',
  workType: '프로젝트',
  performanceTitle: '',
  performance: '',
  status: '',
});

// 실적 수정/삭제 모달
const editModalVisible = ref(false);

// (테이블 컬럼 정의)
const columns = computed(() => [
  { key: 'fromDate',        label: t('hrm.performanceDetail.startDate'),  width: 100,  align: 'center' , mobileDisable:true},
  { key: 'toDate',          label: t('hrm.performanceDetail.endDate'),  width: 100,  align: 'center' , mobileDisable:true},
  { key: 'workType',        label: t('hrm.performanceDetail.type'),    width: 100   , mobileDisable:true},
  { key: 'performanceTitle',label: t('hrm.performanceDetail.performanceTitle'), width: 'auto', minWidth: 100 },
  { key: 'performance',     label: t('hrm.performanceDetail.performance'),    width: 'auto', minWidth: 100  },
  {
    key: 'statusTableLabel',
    label: t('hrm.performanceDetail.status'),
    width: 80,
    align: 'center',
    customClass: (value) => {
      if (value === t('hrm.performanceDetail.statusInProgress')) return 'text-red';
      if (value === t('hrm.performanceDetail.statusComplete')) return 'text-green';
      if (value === t('hrm.performanceDetail.statusPending')) return 'text-blue';
      return '';
    }
  },
]);

// 상태값 매핑
const statusMapping = computed(() => ({
  '1': t('hrm.performanceDetail.statusInProgress'),
  '2': t('hrm.performanceDetail.statusComplete'),
  '3': t('hrm.performanceDetail.statusPending')
}));

// ====================== (1) UserSearchDropdown => 사용자 선택 콜백 ======================
function onUserSelected(option) {
  // option이 없거나 빈 문자열이면 => 선택 해제된 상황
  if (!option || !option.value) {
    selectedUserId.value = '';
    selectedUserLabel.value = '';
    data.value = [];
    return;
  }

  // 선택된 사용자 설정
  selectedUserId.value = option.value;
  selectedUserLabel.value = option.label;

  // 실적 목록 조회
  filterData();
  viewState.saveState('hrmPerformanceManagement', {
    selectedUserId: selectedUserId.value,
    selectedUserLabel: selectedUserLabel.value,
    scrollY: window.scrollY,
  })
  // 상세에서만 복원 허용 (상세 라우트가 없으면 저장만 하고 복원은 disabled)
  viewState.allowRestoreFrom('hrmPerformanceManagement', [])
}

// ====================== (2) 실적 목록 조회 ======================
async function filterData() {
  try {
    if (!selectedUserId.value) {
      data.value = [];
      return;
    }
    const response = await PerformanceApi.getPerformancesByUserId(selectedUserId.value);
    // 서버에러 처리
    if (response.data.error) {
      toast.error(response.data.error);
      return;
    }
    // 상태값 매핑
    data.value = response.data.map(item => ({
      ...item,
      statusTableLabel: statusMapping.value[item.status.toString()] || ''
    }));
  } catch (error) {
    toast.error(t('hrm.performanceDetail.loadFailed'));
    console.error(error);
  }
}

// ====================== (3) 실적 추가 모달 열기 / 저장 ======================
function openCreateModal() {
  formData.value = {
    fromDate: '',
    toDate: '',
    workType: '',
    performanceTitle: '',
    performance: '',
    status: '',
  };
  createModalVisible.value = true;
}

async function submitPerformance(newForm) {
  createModalVisible.value = false;
  if (!selectedUserId.value) return;

  try {
    await PerformanceApi.createPerformance(selectedUserId.value, newForm);
    toast.success(t('hrm.performanceDetail.addSuccess'));
    await filterData();
  } catch (err) {
    toast.error(t('hrm.performanceDetail.addError'));
    console.error(err);
  }
}

// ====================== (4) 실적 수정/삭제 모달 열기 / 저장 / 삭제 ======================
function openEditModal(item) {
  editData.value = { ...item };
  editModalVisible.value = true;
}

async function submitEdit(updatedItem) {
  editModalVisible.value = false;
  if (!selectedUserId.value) return;

  try {
    await PerformanceApi.patchPerformance(selectedUserId.value, updatedItem.performanceId, updatedItem);
    toast.success(t('hrm.performanceDetail.updateSuccess'));
    await filterData();
  } catch (err) {
    toast.error(t('hrm.performanceDetail.updateError'));
    console.error(err);
  }
}

async function removeItem(deletedItem) {
  editModalVisible.value = false;
  if (!selectedUserId.value) return;

  try {
    await PerformanceApi.deletePerformance(selectedUserId.value, deletedItem.performanceId);
    toast.success(t('hrm.performanceDetail.deleteSuccess'));
    await filterData();
  } catch (err) {
    toast.error(t('hrm.performanceDetail.deleteError'));
    console.error(err);
  }
}

// 복원
const bootstrap = () => {
  const saved = viewState.getState('hrmPerformanceManagement')
  const restore = viewState.canRestore('hrmPerformanceManagement')
  if (restore && saved) {
    if (saved.selectedUserId && saved.selectedUserLabel) {
      selectedUserId.value = saved.selectedUserId
      selectedUserLabel.value = saved.selectedUserLabel
      filterData()
    }
    requestAnimationFrame(()=>{ window.scrollTo(0, saved.scrollY ?? 0) })
  }
}
bootstrap()
</script>

<style scoped>
hr {
  margin: 0px
}

/** ---------------- 사용자 검색 관련 css START ---------------- */
.search-wrapper {
  position: relative;
}

.search-result {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #ccc;
  border-top: none;
  border-radius: 0 0 4px 4px;
  max-height: 150px;
  overflow-y: auto;
  list-style: none;
  margin: 0;
  padding: 0;
  z-index: 1000;
}

/* 검색 결과 목록 항목 스타일 */
.search-result li {
  padding: 8px 10px;
  cursor: pointer;
  font-size: 0.7rem;
}

.search-result li:hover,
.search-result li.active {
  background-color: #f2f2f2;
  color: black;
}
/** ---------------- 사용자 검색 관련 css END ---------------- */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.close {
  font-size: 1.5rem;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}
.performance-save-button {
  background-color: #375a7f;
  font-size: 0.875rem;
  padding: 0.4rem 0.75rem;
}
.bnt-search-name {
  background-color: #375a7f;
}
#dataTable tbody tr:hover td {
  background-color: #e7f1ff !important;
}
.placeholder-image {
  max-width: 700px;
  margin-top: 30px;
  width: 100%;
  height: auto;
  opacity: 0.8;
}
@media (max-width: 650px) {
  .close {
    font-size: 1rem !important;
  }
  label {
    font-size: 0.85rem;
  }
  .performance-save-button {
    margin-bottom: 0;
    font-size: 0.63rem;
    padding: 0.3rem 0.6rem;
  }
  .align-items-center {
    margin-bottom: 10px;
  }
  .text-muted {
    font-size: 0.75rem;
    margin-bottom: 7px;
  }
  .input-search-name {
    font-size: 0.75rem;
    padding: 0.3rem 0.6rem;
    width: 150px !important;
  }
  .search-result li {
    padding: 5px 7px;
    cursor: pointer;
    font-size: 0.5rem;
  }
}

@media (max-width: 500px) {
  .placeholder-image {
    margin-top: 10px;
  }
}
</style>
