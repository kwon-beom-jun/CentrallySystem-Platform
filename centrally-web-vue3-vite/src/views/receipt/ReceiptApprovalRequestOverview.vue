<template>
  <div class="content content-wrapper">
    <PageTitle 
      :title="isDelegate ? $t('receipt.common.delegateApprovalTitle') : $t('receipt.common.receiptList')"
      :subtitle="$t('receipt.common.receiptList')"
      :icon="isDelegate ? 'ri-user-shared-line' : 'ri-survey-line'"
      :show-action-button="tableData.length > 0"
      :action-button-active="showKpi"
      @action-button-click="toggleKpi"
    >
      <template #actionButton>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M3 3v18h18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M7 16l4-4 3 3 5-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        {{ $t('receipt.common.statistics') }}
      </template>
    </PageTitle>

    <DefaultFormRow 
      v-if="canBulkProcess" 
      align="right" 
      :marginTop="isMobile ? '0px' : '0px'"
      :marginBottom="isMobile ? '5px' : '5px'"
    >
      <DefaultButton
        size="small"
        @click="openConfirmModal('approval')">
        {{ $t('receipt.common.bulkApprove') }}
      </DefaultButton>
      <DefaultButton
        size="small"
        marginLeft="5px"
        color="red"
        @click="openConfirmModal('rejected')">
        {{ $t('receipt.common.bulkReject') }}
      </DefaultButton>
    </DefaultFormRow>

    <!-- ───── 검색 영역 ───── -->
    <div class="search-controls">
      <!-- ① 날짜 범위만 -->
      <!-- <DefaultFormRow align="right">
        <DefaultTextfield
          id="monthDate"
          type="month"
          size="full"
          style="width: 50%"
          v-model="monthDate"
        />
      </DefaultFormRow> -->

      <DefaultFormRow align="right" marginTop="7px">
        <!-- <DefaultLabel text="부서 :" forId="department" size="small" /> -->
        <DefaultSelect
          id="department"
          v-model="selectedDeptId"
          :options="deptOptions"
          :placeholder="$t('receipt.ceoReport.departmentAll')"
          size="full"
          style="width: 100%"
          marginRight="10px"
        />
        <!-- <DefaultLabel text="팀 :" forId="team" size="small" /> -->
        <DefaultSelect
          id="team"
          v-model="selectedTeamId"
          :options="teamsOptions"
          :placeholder="$t('receipt.ceoReport.teamAll')"
          size="full"
          style="width: 100%"
          :disabled="isTeamDisabled"
        />
      </DefaultFormRow>


      <!-- ② 이름 + 조회 버튼 (추가) -->
      <DefaultFormRow marginTop="7px" gap="10px">
        <DefaultFormRow>
          <DefaultTextfield
            id="monthDate"
            type="month"
            size="full"
            style="width: 100%"
            v-model="monthDate"
          />
        </DefaultFormRow>
        <DefaultFormRow :growFirst="true" align="right">
          <!-- <DefaultLabel text="이름 :" forId="nameSearch" size="small" /> -->
          <UserSearchDropdown
            ref="userSearchRef"
            :labelText="$t('hrm.userManagement.search')"
            inputId="nameSearch"
            inputSize="full"
            :placeholder="$t('hrm.userManagement.namePlaceholder')"
            :includeCurrentUser="true"
            :initialValue="selectedUserSearchValue"
            @userSelected="onUserSelected"
          />
          <DefaultButton size="small" @click="search" color="gray">{{ $t('common.button.search') }}</DefaultButton>
        </DefaultFormRow>
      </DefaultFormRow>
    </div>

    <!-- KPI 카드 (행이 3개 이하일 때만 노출) -->
    <div v-if="tableData.length && showKpi && summary.cnt > 0" class="kpi-wrap">
      <div class="kpi-card">
        <div class="kpi-title">{{ $t('receipt.common.totalReceiptCount') }}</div>
        <div class="kpi-value">{{ summary.cnt }}{{ $t('receipt.common.count') }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-title">{{ $t('receipt.common.totalAmount') }}</div>
        <div class="kpi-value">{{ formatNumber(summary.total) }}{{ $t('receipt.ceoReport.won') }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-title">{{ $t('receipt.common.averageAmount') }}</div>
        <div class="kpi-value">{{ formatNumber(summary.avg) }}{{ $t('receipt.ceoReport.won') }}</div>
      </div>
    </div>

    <!-- ───── 결과 테이블 / 카드 ───── -->
    <DefaultTable 
      :columns="columns" 
      :data="tableData" 
      :rowClick="goToDetail" 
      :selectable="canBulkProcess"
      v-model:selectedRows="selectedRows"
      :mobileCard="true"
      :minRows="DESKTOP_PAGE_SIZE"
      :usePagination="true"
      :currentPage="currentPage"
      :totalPages="totalPages"
      :visiblePageCount="visiblePageCount"
      @pageChange="onPageChange"
      :noDataImageHeight="388"
    />
  </div>

  <!-- 승인 확인 모달 -->
  <AlertModal
    :isVisible="isConfirmModalVisible"
    :title="modalTitle"
    :confirmText="$t('receipt.common.approve')"
    :cancelText="$t('common.button.cancel')"
    :disableBackgroundClose="true"
    @confirm="handleConfirm"
    @close="isConfirmModalVisible = false"
  >
    <template #body>
      {{ modalBodyText }}
    </template>
  </AlertModal>

  <!-- 반려 사유 입력 모달 -->
  <EditModal
    :isVisible="isRejectReasonModalVisible"
    :title="$t('receipt.common.rejectReason')"
    :confirmText="$t('receipt.common.reject')"
    :cancelText="$t('common.button.cancel')"
    v-model:value="rejectionReason"
    @confirm="handleRejectConfirm"
    @close="isRejectReasonModalVisible = false"
  />
</template>

<script setup>
/* ────────────────────────────────
 *  공통 import
 * ──────────────────────────────── */
import { ref, computed, watch, onMounted, defineProps } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import DepartmentApi from '@/api/hrm/DepartmentApi'
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi'
import ReceiptsRequestApi from '@/api/receipt/ReceiptsRequestApi.js';
import { getRolesFrom, hasPermission } from '@/utils/roleUtils.js'
import { ROLE_GROUPS } from '@/config/roleConfig';

/* 로컬 컴포넌트 (템플릿에서 쓰이므로 import 필요) */
import DefaultFormRow from '@/components/common/DefaultFormRow.vue'
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue'
import DefaultLabel from '@/components/common/label/DefaultLabel.vue'
import DefaultButton from '@/components/common/button/DefaultButton.vue'
import DefaultTable from '@/components/common/table/DefaultTable.vue'
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import EditModal from '@/components/common/modal/EditModal.vue';
import UserSearchDropdown from '@/components/auth/UserSearchDropdown.vue'
import { useToastStore } from '@/store/toast'
import { toDeptTeamDisplay } from '@/utils/blankFormat.js'
import { getCurrentMonthKST, getMonthDateRange } from '@/utils/dateUtils.js'
import { useViewStateStore } from '@/store/viewState'

// toast store 사용
const toastStore = useToastStore();

/* ────────────────────────────────
 *  상수·헬퍼
 * ──────────────────────────────── */
const MOBILE_BP = 850
const MOBILE_PAGE_SIZE = 4
const DESKTOP_PAGE_SIZE = 8
const fmt = n => (+n || 0).toLocaleString()

/* ───── 상태 ───── */
const selectedRows = ref([]);    // 테이블 체크박스 클릭
const departments = ref([])      // 부서+팀 전체 원본 데이터
const selectedDeptId = ref(null) // 선택된 부서 ID
const selectedTeamId = ref(null) // 선택된 팀 ID
// '사용자 관리' 페이지의 로직을 적용하여 모든 경우를 처리
const isTeamDisabled = computed(() => {
  // 1. '부서 전체'가 선택되면 비활성화
  if (selectedDeptId.value === null) return true;
  // 2. 선택된 부서에 팀이 있는지 확인
  const dept = departments.value.find(
    d => d.departmentId === Number(selectedDeptId.value)
  );
  // 3. 해당 부서가 없거나, 팀 목록이 비어있으면 비활성화
  return !dept || !dept.teams || dept.teams.length === 0;
});
// 모달 관련 상태
const isConfirmModalVisible = ref(false);
const isRejectReasonModalVisible = ref(false);
const confirmAction = ref('');
const rejectionReason = ref('');

/* ───────── 반응형 상태 ───────── */
const isMobile = ref(window.innerWidth < MOBILE_BP)
const pageSize = computed(() => isMobile.value ? MOBILE_PAGE_SIZE : DESKTOP_PAGE_SIZE)

/* ───────── 조회·뷰 상태 ───────── */
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore();
const viewState = useViewStateStore()
const myId = authStore.getUserId
const props = defineProps({ forceDelegate: { type: Boolean, default: false } })
const isDelegate = computed(() => props.forceDelegate || route?.meta?.delegate === true)

// ▶️ 일괄 승인/반려 사용 가능 여부 (확정자 이상 또는 시스템 권한) - roleUtils.js 사용
const canBulkProcess = computed(() => {
  const roles = authStore.getRoles || [];
  return hasPermission(roles, getRolesFrom(ROLE_GROUPS.RECEIPT_FINALIZER));
});
const currentPage = ref(1)
const totalPages = ref(1)
const visiblePageCount = ref(5)
const tableData = ref([])

/* ───── ‘월’ 입력 값 ───── */
const monthDate = ref('') 

/* ───── 초기 진입 중복 조회 방지 가드 ───── */
const isBootstrapping = ref(true)
/* ───── 상태 복원 중 플래그 (watch 무시용) ───── */
const isRestoring = ref(false)

/* ───── 시작·종료일 계산 ───── */
const dateRange = computed(() => {
  if (!monthDate.value) return { startDate: '', endDate: '' };
  try {
    return getMonthDateRange(monthDate.value);
  } catch (error) {
    console.error('날짜 범위 계산 오류:', error);
    return { startDate: '', endDate: '' };
  }
});

const startDate = computed(() => dateRange.value.startDate);
const endDate = computed(() => dateRange.value.endDate);

/* ───────── 컬럼 정의 ───────── */
const columns = [
 { key:'userName', label:'이름',      width:150,
   mobile:{ line:1, inline:true, prefix:'📝 이름 : ', bold:true }},
 { key:'userEmail',label:'이메일',    width:'auto', minWidth: 100,
   mobile:{ line:1, inline:true, prefix:'이메일 : ', bold:false, align:"right"}},
 { key:'department',label:'부서',     width:200,
   mobile:{ dividerTop:true, dividerTopGapAbove:'5px', dividerTopGapBelow:'15px', line:2, inline:false, prefix:'부서 : ', bold:false }},
 { key:'team',      label:'팀',       width:200,
   mobile:{ line:3, inline:false, prefix:'팀 : ', bold:false }},
 { key:'count',     label:'건수',     width:60, align:'center',
   mobile:{ line:4, inline:false, prefix:'건수 : ', bold:false }},
 { key:'amount',    label:'총금액',   width:150, align:'right', customValue: r => fmt(r.amount),
   mobile:{ line:5, inline:false, prefix:'총금액 : ', bold:false }},
]
/* ─── KPI 토글 상태 ─── */
const showKpi = ref(false)
const isSummaryLoaded = ref(false)
async function toggleKpi () {
  showKpi.value = !showKpi.value
  if (showKpi.value && !isSummaryLoaded.value && tableData.value.length) {
    await fetchSummary()
  }
}

/* ───────── KPI 요약 ───────── */
const summary = ref({ cnt: 0, total: 0, avg: 0 });
const selectedUserId = ref(null);
/**
 * KPI 요약 데이터 조회
 */
async function fetchSummary() {
  const summaryParams = {
    userId   : selectedUserId.value ?? null,
    startDate: startDate.value,
    endDate  : endDate.value,
    roles    : [1],
  }
  // 팀/부서 ID가 유효한 경우에만 params 추가
  if (selectedTeamId.value != null) {
    summaryParams.teamId = selectedTeamId.value
  } else if (selectedDeptId.value != null) {
    summaryParams.departmentId = selectedDeptId.value
  }

  const { data } = isDelegate.value
    ? await ReceiptsSearchApi.getMyPendingSummaryByUserAsDelegate(myId, summaryParams)
    : await ReceiptsSearchApi.getMyPendingSummaryByRoles(myId, summaryParams)
  summary.value = data;
  isSummaryLoaded.value = true
}

/* ───────── 데이터 로드 ───────── */
async function fetchData(page = 1) {
  const params = {
    userId   : selectedUserId.value ?? null,
    startDate: startDate.value,
    endDate  : endDate.value,
    roles    : [1],
    page     : page - 1,
    size     : pageSize.value,
  }
  // 팀/부서 ID가 유효한 경우에만 params 추가
  if (selectedTeamId.value != null) {
    params.teamId = selectedTeamId.value
  } else if (selectedDeptId.value != null) {
    params.departmentId = selectedDeptId.value
  }

  const { data } = isDelegate.value
    ? await ReceiptsSearchApi.getMyPendingSummaryByUserAsDelegate(myId, params)
    : await ReceiptsSearchApi.getMyPendingSummaryRows(myId, params)

  /* 행 가공 */
  tableData.value = (data.content || []).map(r => {
    return {
      receiptId     : r.receiptId,
      userId      : r.userId,
      userName    : r.userName,
      userEmail   : r.userEmail,
      department  : toDeptTeamDisplay(r.departmentName),
      team        : toDeptTeamDisplay(r.teamName),
      count       : r.count,
      amount      : r.requested,
    }
  })

  totalPages.value = data.totalPages || 1
  // KPI 요약은 기본 로드 시 호출하지 않음(토글 시 지연 호출)
  if (isMobile.value) window.scrollTo(0, 0)
}

/* ───────── 이벤트 ───────── */
/**
 * 조회 버튼 클릭 시 실행
 * - 저장된 상태를 초기화하여 새로운 조회 조건으로 검색
 */
function search() {
  currentPage.value = 1;
  isSummaryLoaded.value = false;
  viewState.clearState('receiptApprovalOverview')  // 기존 저장 상태 초기화
  fetchData(1)
}
function onPageChange(p) { currentPage.value = p; fetchData(p) }
function goToDetail(row) {
  viewState.saveState('receiptApprovalOverview', {
    selectedDeptId     : selectedDeptId.value,
    selectedTeamId     : selectedTeamId.value,
    selectedUserId     : selectedUserId.value,
    selectedUserSearchValue : selectedUserSearchValue.value,
    monthDate          : monthDate.value,
    currentPage        : currentPage.value,
    scrollY            : window.scrollY,
  })
  router.push({
    name: isDelegate.value ? 'ReceiptDelegateApprovalRequestDetails' : 'ReceiptApprovalRequestDetails',
    query: {
      startDate: startDate.value,
      endDate: endDate.value,
      userId: row.userId,
      userName: row.userName
    }
  })
}

/* ───────── 이름 선택 이벤트 ───────── */
const userSearchRef = ref(null)
const selectedUserSearchValue = ref('')
function onUserSelected(user) {
  selectedUserId.value = user?.userId ?? null
  selectedUserSearchValue.value = user?.label ?? ''  // "이름 (이메일)" 형식
}

/* 화면 폭 변경 → pageSize 재적용 */
function handleResize() {
  // const prev = isMobile.value
  // isMobile.value = window.innerWidth < MOBILE_BP
  // if (prev !== isMobile.value) {
  //   currentPage.value = 1
  //   fetchData(1)
  // }
  isMobile.value = window.innerWidth < MOBILE_BP
}


/* ───────── 일괄 승인, 일괄 반려 선택 이벤트 ───────── */
// 모달 제목과 본문 내용을 동적으로 생성
const modalTitle = computed(() => {
  return confirmAction.value === 'approval' ? '일괄 승인 확인' : '일괄 반려 확인';
});

const modalBodyText = computed(() => {
  const count = selectedRows.value.length;
  const actionText = confirmAction.value === 'approval' ? '일괄 승인' : '일괄 반려';
  return `선택한 ${count}개의 항목을 정말 ${actionText} 처리하시겠습니까?`;
});

// 버튼 클릭 시 모달 열기 로직 수정
function openConfirmModal(action) {
  if (selectedRows.value.length === 0) {
    const actionText = action === 'approval' ? '일괄 승인' : '일괄 반려';
    return toastStore.warn(`${actionText}할 항목을 선택해주세요.`);
  }
  confirmAction.value = action;

  if (action === 'rejected') { // '일괄 반려'일 경우
    rejectionReason.value = ''; // 일괄 반려 사유 초기화
    isRejectReasonModalVisible.value = true; // 사유 입력 모달 열기
  } else { // '승인'일 경우
    isConfirmModalVisible.value = true; // 기존 확인 모달 열기
  }
}

// '일괄 승인' 처리 함수
async function handleConfirm() {
  isConfirmModalVisible.value = false;
  
  const payload = {
    userIds   : selectedRows.value.map(r => r.userId), // ✔️ 신청자 목록
    startDate : startDate.value,                       // ✔️ YYYY‑MM‑01
    endDate   : endDate.value,                         // ✔️ 말일
    roles     : [1]
  };

  await ReceiptsRequestApi.approvalUserReceipts(payload);

  toastStore.success(`${selectedRows.value.length}건이 승인 처리되었습니다.`);
  selectedRows.value = []; // 처리 후 선택 해제
  await fetchData(currentPage.value); // 현재 페이지 데이터 다시 로드
}

// '일괄 반려' 처리 함수
async function handleRejectConfirm() {
  isRejectReasonModalVisible.value = false;
  if (!rejectionReason.value.trim()) {
    return toastStore.warn('반려 사유를 입력해주세요.');
  }
  
  const payload = {
    userIds   : selectedRows.value.map(r => r.userId),
    startDate : startDate.value,
    endDate   : endDate.value,
    reason    : rejectionReason.value,
    roles     : [1]
  };

  await ReceiptsRequestApi.rejectUserReceipts(payload);
  
  toastStore.success(`${selectedRows.value.length}건이 반려 처리되었습니다.`);
  selectedRows.value = []; // 처리 후 선택 해제
  await fetchData(currentPage.value); // 현재 페이지 데이터 다시 로드
}

// PC ↔ 모바일 모드가 실제로 변한 순간에만 1회 재조회
watch(isMobile, (v, prev) => {
  if (v !== prev) {
    currentPage.value = 1
    fetchData(1)
  }
})

/* ───────── 라우터 쿼리(monthDate) 변경 감지 ───────── */
watch(
  () => route.query.monthDate,
  (newVal, oldVal) => {
    if (isBootstrapping.value) return; // 초기 URL 교정 시 중복 조회 방지
    if (newVal && newVal !== oldVal && typeof newVal === 'string') {
      const isValid = /^\d{4}-\d{2}$/.test(newVal);
      if (isValid) {
        monthDate.value = newVal;
        search();
      } else {
        toastStore.setNextPageMessage('잘못된 월 형식입니다. (YYYY-MM)', 'error');

        const currentMonth = getCurrentMonthKST();
        monthDate.value = currentMonth;
        router.replace({
          query: {
            ...route.query,
            monthDate: currentMonth,
          },
        });
      }
    }
  }
)

/* ───────── 부서/팀 선택 로직 ───────── */
// 부서 드롭다운 옵션
const deptOptions = computed(() => [
  { value: '', label: '부서 전체' },
  ...departments.value.map(d => ({
    value: d.departmentId,
    label: d.departmentName
  }))
]);

// 선택된 부서에 따라 동적으로 팀 드롭다운 옵션 생성
const teamsOptions = computed(() => {
  if (selectedDeptId.value === null) {
    return [];
  }
  const dept = departments.value.find(d => d.departmentId === Number(selectedDeptId.value));
  // 팀이 없는 경우, 빈 배열을 반환
  if (!dept || !dept.teams || dept.teams.length === 0) {
    return [];
  }
  // 실제 팀 목록만 반환
  return dept.teams.map(t => ({
      value: t.teamId,
      label: t.teamName
  }));
});

/**
 * 부서 선택 시 팀 선택 초기화
 * (단, 상태 복원 중에는 무시)
 */
watch(selectedDeptId, () => {
  if (isRestoring.value) return;
  selectedTeamId.value = null;
});


/* ───────── 최초 실행 ───────── */
onMounted(async () => {
  const today = new Date()
  const queryMonth = route.query?.monthDate;
  const regex = /^\d{4}-\d{2}$/;
  if (typeof queryMonth === 'string' && regex.test(queryMonth)) {
    monthDate.value = queryMonth;
  } else {
    if (queryMonth) toastStore.setNextPageMessage('잘못된 월 형식입니다. (YYYY-MM)', 'error');

    // 현재 월 문자열 (YYYY-MM) - 한국 시간 기준
    const currentMonth = getCurrentMonthKST();
    monthDate.value = currentMonth;

    // 잘못된 쿼리를 교정하여 갱신 (새로고침 시 팝업 재발생 방지)
    router.replace({
      query: {
        ...route.query,
        monthDate: currentMonth,
      },
    });
  }
  window.addEventListener('resize', handleResize)
  
  /* 부서/팀 데이터 로드 방식 수정 */
  const { data } = await DepartmentApi.getDepartments();
  departments.value = data || []; // departments에 전체 데이터 저장

  // 초기 1회만 조회하고, 이후 쿼리 감시는 활성화
  const saved = viewState.getState('receiptApprovalOverview')
  const restore = viewState.canRestore('receiptApprovalOverview')
  if (restore && saved) {
    isRestoring.value = true  // 복원 중 플래그 활성화
    if (typeof saved.monthDate === 'string') monthDate.value = saved.monthDate
    selectedDeptId.value = saved.selectedDeptId ?? null
    selectedTeamId.value = saved.selectedTeamId ?? null
    selectedUserId.value = saved.selectedUserId ?? null
    selectedUserSearchValue.value = saved.selectedUserSearchValue ?? ''
    currentPage.value    = saved.currentPage ?? 1
    await fetchData(currentPage.value)
    requestAnimationFrame(()=>{ window.scrollTo(0, saved.scrollY ?? 0) })
    isRestoring.value = false  // 복원 완료
  } else {
    viewState.clearState('receiptApprovalOverview')
    await fetchData(1)
  }
  isBootstrapping.value = false
})

// 상세에서만 복원 허용
viewState.allowRestoreFrom('receiptApprovalOverview', ['ReceiptApprovalRequestDetails','ReceiptDelegateApprovalRequestDetails'])

/* 템플릿 헬퍼 */
function formatNumber(n) { return fmt(n) }
</script>


<style scoped>
#dataTable tbody tr:hover td {
  background-color: #e7f1ff !important;
}

/* 상태별 색상 설정 */
.status-complete {
  color: blue;
  /* 완료 상태는 파란색 */
}

.status-in-progress {
  color: red;
  /* 진행중 상태는 빨간색 */
}

.align-items-center {
  margin-bottom: 0px !important;
}

.bnt-search {
  margin: 0 !important;
}

.search-controls {
  margin-bottom: 10px;
}

.kpi-wrap {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}

.kpi-card {
  flex: 1;
  background: linear-gradient(135deg, #f0f7ff 0%, #e3f2fd 100%);
  border: 1px solid #bbdefb;
  border-radius: 8px;
  padding: 8px 14px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.08);
  transition: all 0.2s ease;
}

.kpi-card:hover {
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.15);
  transform: translateY(-1px);
}

.kpi-title {
  font-size: .8rem;
  color: #1565c0;
  margin-bottom: 4px;
  font-weight: 500;
}

.kpi-value {
  font-size: 0.875rem;
  font-weight: 700;
  color: #0d47a1;
}

@media (min-width: 851px) {
  #nameSearch {
    font-size: 1rem;
  }
}

@media (max-width: 850px) {
  .btn-primary {
    font-size: 0.75rem;
    /* 모바일 버튼 글자 크기 줄이기 */
    padding: 0.3rem 0.6rem;
    /* 모바일 버튼 패딩 줄이기 */
    margin-bottom: 10px;
  }
  .bnt-print {
    margin-bottom: 40px;
  }

  .kpi-wrap {
    gap: 8px;
  }

  .kpi-card {
    padding: 8px 10px;
  }

  .kpi-title {
    font-size: .6rem;
  }

  .kpi-value {
    font-size: .8rem;
  }
}

@media (max-width: 650px) {
  /* 📱 모바일: 서브타이틀(날짜+토글 라인) 중앙 정렬 */
  .date-toggle-line {
    justify-content: center;
  }
}
</style>
