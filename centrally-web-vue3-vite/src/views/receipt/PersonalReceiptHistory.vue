<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper" :class="{ 'content-expanded': !isMobile }">
      <PageTitle 
        :title="$t('receipt.history.title') || $t('receipt.personal.title')"
        :subtitle="$t('receipt.personal.submitted')"
        icon="ri-file-list-3-line"
      />

      <!-- 월 선택 셀렉트 -->
      <DefaultFormRow marginBottom="10px" align="between" gap="10px">
        <!-- 상태 선택 드롭다운 -->
        <DefaultSelect
          id="statusSearch"
          v-model="selectedStatus"
          :options="statusOptions"
          size="small"
          @change="onStatusChange"
        />
        <DefaultFormRow align="right" gap="0px">
          <DefaultLabel :text="$t('receipt.annualSummary.month')" forId="monthSearch" size="small" />
          <DefaultTextfield
            type="month"
            id="monthSearch"
            v-model="selectedMonth"
            size="small"
            @change="filterByMonth"
          />
        </DefaultFormRow>
      </DefaultFormRow>

      <!-- ─── 테이블 보기 ─── -->
      <DefaultTable
        class="receipt-table"
        :columns="columns"
        :data="data"
        :rowClick="openEditModal"
        :mobileCard="true"
        :usePagination="true"
        :currentPage="currentPage"
        :totalPages="totalPages"
        :visiblePageCount="visiblePageCount"
        @pageChange="onPageChange"
        :noDataImageHeight="474"
      />
    </div>
  </div>

  <!-- 이미지 미리보기 모달 -->
  <div
    v-if="isPreviewVisible"
    class="modal preview-modal"
    @click="closePreviewModalOnOutsideClick"
  >
    <div
      class="preview-modal-content"
      @mousedown="startDrag"
      @mousemove="onDrag"
      @mouseup="endDrag"
      @mouseleave="endDrag"
      @touchstart="startDrag"
      @touchmove="onDrag"
      @touchend="endDrag"
    >
      <img
        :src="previewImage"
        :class="{ zoomed: isZoomed }"
        class="preview-modal-image"
        :style="{
          transform: isZoomed
            ? `translate(${zoomedPosition.x}px, ${zoomedPosition.y}px) scale(1.5)`
            : 'none',
          transformOrigin: `${zoomOrigin.x}px ${zoomOrigin.y}px`,
        }"
        @dblclick="toggleZoom"
        @touchstart="toggleZoom"
      />
    </div>
  </div>
  <!-- 개인 영수증 히스토리 모달 -->
  <ReceiptDetailViewModal
    :isVisible="historyModalVisible"
    :receiptId="editingReceiptId"
    @close="historyModalVisible = false"
    @updated="fetchMetadata(currentPage)"
  />
</template>

<script setup>
/* ───────────────────────────── imports ───────────────────────────── */
import { ref, onMounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ROUTES } from '@/config/menuConfig';
import { toast } from 'vue3-toastify';

import DefaultTable from '@/components/common/table/DefaultTable.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import ReceiptDetailViewModal from '@/components/receipt/ReceiptDetailViewModal.vue';
import { useViewStateStore } from '@/store/viewState';
import { 
  RECEIPT_STATUS_LABELS, 
  getReceiptStatusLabel,
  APPROVAL_ROLE,
  getApprovalRoleLabel
} from '@/constants/receiptConstants';

import ReceiptsApi from '@/api/receipt/ReceiptsApi';
import { useAuthStore } from '@/store/auth';
import { usePreviewModal } from '@/utils/preview-modal';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME } from '@/constants';
import { toDeptTeamDisplay } from '@/utils/blankFormat.js';
import useBreakPoint from '@/composables/useBreakPoint';
import { usePageSize10or4 } from '@/composables/usePageSize';

/* status → 색상 클래스 */
const STATUS_CLASS = {
  [RECEIPT_STATUS_LABELS.APPROVED]: 'text-success',
  [RECEIPT_STATUS_LABELS.REJECTED]: 'text-danger',
  [RECEIPT_STATUS_LABELS.REQUEST]: 'text-primary'
}

/* ─────────────────────── responsive state / helpers ─────────────── */
const { isMobile } = useBreakPoint();
const pageSize = usePageSize10or4();
// 뷰포트 크기가 변해 isMobile 값이 바뀌면 페이지 크기 재적용을 위해 재조회
watch(isMobile, (v, prev) => {
  if (v !== prev) {
    currentPage.value = 1;
    fetchMetadata(1);
  }
});
// 모바일로 전환될 때 상세 모달이 열려 있다면 닫는다
watch(isMobile, (v) => {
  if (v) historyModalVisible.value = false;
});

/* ───────────────────────────── stores ───────────────────────────── */
const auth   = useAuthStore()
const viewState = useViewStateStore()

/* ─────────────────────── pagination & list data ─────────────────── */
const currentPage      = ref(1)
const totalPages       = ref(1)
const visiblePageCount = ref(5)

const data   = ref([])

/* ─────────────────────────── month filter ───────────────────────── */
const selectedMonth = ref(getCurrentYearMonth())
function getCurrentYearMonth () {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}`
}
function filterByMonth () {
  currentPage.value = 1
  fetchMetadata(1)
}

/* ─────────────────────────── status filter ──────────────────────── */
/*   value 는 백엔드 enum 그대로 사용 (REQUEST, APPROVED …)              */
const statusOptions = [
  { value: '',         label:'전체' },
  { value: 'REQUEST',  label:'신청' },
  { value: 'APPROVED', label:'승인' },
  { value: 'REJECTED', label:'반려' },
  { value: 'WAITING',  label:'대기' },
  { value: 'CLOSED',   label:'마감' }
]
const selectedStatus = ref('')   // v-model 바인딩

function onStatusChange () {
  currentPage.value = 1
  fetchMetadata(1)
}

/* ─────────────────────── preview-modal helpers ──────────────────── */
const {
  isPreviewVisible,
  previewImage,
  isZoomed,
  zoomedPosition,
  zoomOrigin,
  openPreviewModal,
  toggleZoom,
  startDrag,
  onDrag,
  endDrag,
} = usePreviewModal();

function closePreviewModalOnOutsideClick(e) {
  if (!e.target.classList.contains('preview-modal-image')) isPreviewVisible.value = false;
}

/* ─────────────────────── table column schema ───────────────────── */
const columns = [
  {
    key: 'receiptCode',
    label: '식별 번호',
    width: 85,
    align: 'center',
    mobile: {
      line: 1,
      inline: true,
      prefix: '📝\u00a0글번호\u00a0:\u00a0',
      suffix: '',
      bold: true,
    },
  },
  {
    key: 'date',
    label: '발행일',
    width: 100,
    align: 'center',
    mobile: {
      line: 1, // 몇 번째 줄?
      inline: true, // 같은 줄에 나란히?
      prefix: '', // 앞 붙일 문자
      suffix: '', // 뒤 붙일 문자
      bold: false,
      align: 'right',
    },
  },
  {
    key: 'type',
    label: '구분',
    width: 150,
    mobile: {
      dividerTop: true,
      line: 2,
      inline: false,
      prefix: '구분\u00a0:\u00a0',
      suffix: '',
      bold: false,
    },
  },
  {
    key: 'reason',
    label: '사유',
    width: 'auto',
    minWidth: 100,
    mobile: {
      line: 3,
      inline: false,
      prefix: '사유\u00a0:\u00a0',
      suffix: '',
      bold: false,
    },
  },
  {
    key: 'peopleCount',
    label: '총 인원',
    width: 80,
    align: 'center',
    mobile: {
      hidden: true,
      line: 4,
      inline: false,
      prefix: '총 인원\u00a0:\u00a0',
      suffix: '',
      bold: false,
    },
  },
  {
    key: 'amount',
    label: '금액',
    width: 80,
    align: 'right',
    mobile: {
      hidden: true,
      line: 5,
      inline: false,
      prefix: '금액\u00a0:\u00a0',
      suffix: '',
      bold: false,
    },
  },
  {
    key: 'amountPerPerson',
    label: '금액/인원',
    width: 80,
    align: 'right',
    mobile: {
      dividerBottom: true,
      line: 6,
      inline: false,
      prefix: '금액/인원\u00a0:\u00a0',
      suffix: '',
      bold: false,
    },
  },
  {
    key: 'statusText',
    label: '결제 상황',
    width: 80,
    align: 'center',
    customClass: (v) => STATUS_CLASS[v] ?? '',
    mobile: {
      line: 7,
      inline: false,
      prefix: '결제 상황\u00a0:\u00a0',
      suffix: '',
      bold: true,
      align: 'right',
    },
  },
];

/* ─────────────────────── card toggle states ────────────────────── */
const openedIndex = ref(null); // 참가자
const openedApproverIdx = ref(null); // 결재자
function togglePeopleList(i) {
  openedIndex.value = openedIndex.value === i ? null : i;
}
function showPeopleList(i) {
  return openedIndex.value === i;
}
function toggleApproverList(i) {
  openedApproverIdx.value = openedApproverIdx.value === i ? null : i;
}
function showApproverList(i) {
  return openedApproverIdx.value === i;
}

/* ─────────────────────── formatting helpers ───────────────────── */
function calculateAmountPerPerson(row) {
  const amt = Number(row.amountRaw || 0);
  return row.peopleCount ? Math.floor(amt / row.peopleCount).toLocaleString() : '0';
}
function statusColor(txt) {
  return STATUS_CLASS[txt] ?? 'text-secondary';
}

/* ─────────────────────── fetch list from API ───────────────────── */
async function fetchMetadata(page = 1) {
  const params = {
    page: page - 1,
    size: pageSize.value,
    yearMonth: selectedMonth.value, // "YYYY-MM"
    /* statusCodes 파라미터(배열) – 선택값이 있으면 단일 원소 배열로 전달 */
    ...(selectedStatus.value ? { statusCodes: [selectedStatus.value] } : {}),
  };
  try {
    const { data: pageDto } = await ReceiptsApi.searchUserReceipts(
      auth.getUserId,
      params,
    );

    data.value = (pageDto.content || []).map(mapRow);
    totalPages.value = pageDto.totalPages || 1;
    if (isMobile.value) window.scrollTo(0, 0);
  } catch (err) {
    console.error(err);
    toast.error('영수증 조회 중 오류가 발생했습니다');
  }
}

/* ─────────────────────── row mapper ────────────────────────────── */
function mapRow(r) {
  const people = (r.participantsList || []).map((p) => ({
    name: p.participantName,
    department: toDeptTeamDisplay(p.department),
    team: toDeptTeamDisplay(p.team),
    participantType: p.participantType || p.type || null,
    company: p.company || null,
    position: p.position || null,
    phone: p.phone || null,
  }));
  const approvers = (r.approvalLines || []).map((a) => ({
    userId: a.approverUserId,
    name: a.approverName,
    department: toDeptTeamDisplay(a.department),
    team: toDeptTeamDisplay(a.team),
    approvalRole: a.approvalRole,
    approvalStatus: a.approvalStatus,
    approvalType: getApprovalRoleLabel(a.approvalRole),
    stateText: a.rejectedAt ? RECEIPT_STATUS_LABELS.REJECTED : a.approvalStatus ? RECEIPT_STATUS_LABELS.APPROVED : RECEIPT_STATUS_LABELS.WAITING,
    rejectedReason: a.rejectedReason,
  }));
  const rejectedReason = approvers.find((ap) => ap.rejectedReason)?.rejectedReason || '';

  const amountRaw = +r.amount || 0;

  return {
    receiptId: r.receiptId,
    receiptCode: r.receiptCode,
    date: r.submissionDate,
    type: r.category?.categoryName || '',
    categoryId: r.category?.categoryId || null,
    reason: r.reason,
    amount: amountRaw.toLocaleString(),
    amountRaw,
    receiptName: r.attachment?.fileName || '영수증 미등록',
    receipt: r.attachment?.fileUrl ? getImagePreviewUrl(r.attachment.fileUrl, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : '',
    people,
    peopleCount: people.length + 1,
    amountPerPerson: (people.length + 1) > 0
      ? Math.floor(amountRaw / (people.length + 1)).toLocaleString()
      : '0',
    approvers,
    statusText: getReceiptStatusLabel(r.status),
    rejectedReason,
  };
}

/* ─────────────────────── page change handler ─────────────────── */
function onPageChange(p) {
  currentPage.value = p;
  fetchMetadata(p);
}

/* ─────────────────────── open detail modal ───────────────────── */
const historyModalVisible = ref(false);
const editingReceiptId = ref(null);
const router = useRouter();

function openEditModal(row) {
  const id = row.receiptId;
  if (isMobile.value) {
    viewState.saveState('personalReceiptHistory', {
      selectedStatus: selectedStatus.value,
      selectedMonth : selectedMonth.value,
      currentPage   : currentPage.value,
      scrollY       : window.scrollY,
    })
    try { sessionStorage.setItem('receiptDetail', JSON.stringify({ id })) } catch (e) {}
    router.push(ROUTES.RECEIPT.DETAIL_MOBILE);
    return;
  }
  editingReceiptId.value = id;
  historyModalVisible.value = true;
}

/* ─────────────────────── lifecycle ───────────────────────────── */
onMounted(async () => {
  // 라우터 쿼리 파라미터 확인 (년도별 요약에서 넘어온 경우)
  const route = useRoute()
  const queryYear = route.query.year
  const queryMonth = route.query.month
  
  // 쿼리 파라미터가 있으면 우선 적용 (한 번만 사용)
  if (queryYear && queryMonth) {
    const formattedMonth = `${queryYear}-${String(queryMonth).padStart(2, '0')}`
    selectedMonth.value = formattedMonth
    currentPage.value = 1
    await fetchMetadata(1)
    return
  }
  
  // 쿼리 파라미터 없으면 기존 로직 (viewState 복원)
  const saved = viewState.getState('personalReceiptHistory')
  const restore = viewState.canRestore('personalReceiptHistory')
  if (restore && saved) {
    selectedStatus.value = saved.selectedStatus ?? ''
    selectedMonth.value  = saved.selectedMonth  ?? getCurrentYearMonth()
    currentPage.value    = saved.currentPage    ?? 1
    await fetchMetadata(currentPage.value)
    await nextTick()
    requestAnimationFrame(() => { window.scrollTo(0, saved.scrollY ?? 0) })
  } else {
    // 일반 입장: 저장 상태 무시하고 초기화 후 첫 페이지 조회
    viewState.clearState('personalReceiptHistory')
    await fetchMetadata()
  }
  // 상세(모바일 영수증 상세)에서만 복원 허용
  viewState.allowRestoreFrom('personalReceiptHistory', ['ReceiptDetailViewMobile'])
});
</script>

<style scoped>
.modal-header {
  display: flex;
  justify-content: space-between;
  font-size: 1.5rem;
}

.reason {
  margin-top: 10px;
}

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 10px;
}

.list-group-item,
.list-group {
  font-size: 0.875em !important;
}

.square-btn {
  width: 15px;
  height: 15px;
  padding: 0;
  text-align: center;
  border-radius: 4px;
  font-size: 0.55rem;
}

.now-label {
  cursor: pointer;
  color: #0d6efd;
}

.now-label:hover {
  text-decoration: underline;
}

.drag-handle {
  cursor: grab;
  user-select: none;
}

.drag-handle:active {
  cursor: grabbing;
}

.align-items-center {
  margin-bottom: 0px !important;
}

/* 선택 토글용 글자 버튼 */
.approval-option {
  cursor: pointer;
  padding: 2px 6px;
  /* border-radius: 4px; */
  color: #39393a; /* 기본 회색 */
  border: 1px solid #8e8e8f; /* 기본 얇은 회색 테두리 */
  user-select: none;
  margin: 0;
}

.approval-option + .approval-option {
  margin-left: -1px; /* 경계선 겹침 처리 */
}

.approval-option-right {
  margin-right: 7px;
}
.approval-option.active {
  color: #0d6efd; /* 선택 시 파란색 */
  font-weight: 900;
  background: #0d6dfd25;
}
/* ── 미리보기 모달 ───────────────────────────── */
.preview-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
.preview-modal-content {
  position: relative;
}
.preview-modal-image {
  max-width: 80vw;
  max-height: 80vh;
  transition: transform 0.2s ease;
}

@media (max-width: 650px) {
  .list-group-item,
  .list-group {
    font-size: 0.8em !important;
  }

  .form-group {
    margin-bottom: 10px;
  }
}
</style>
