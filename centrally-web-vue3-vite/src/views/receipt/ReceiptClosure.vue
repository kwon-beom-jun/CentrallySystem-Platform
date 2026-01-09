<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('receipt.closure.title')"
        :subtitle="$t('receipt.closure.subtitle')"
        icon="ri-lock-line"
        desktopMarginBottom="20px"
      />
      <DefaultFormRow align="right" :marginBottom="isMobile ? '5px' : '5px'">
        <DefaultLabel
          v-if="pastApprovedMonths.length > 0"
          :text="$t('receipt.closure.unsettledMonths') + ' ' + pastApprovedMonths.join(', ')"
          size="small"
          color="red"
          marginLeft="10px" />
      </DefaultFormRow>

      <!-- ───── 검색 영역 ───── -->
      <div class="search-controls">
        <DefaultFormRow align="right">
          <DefaultLabel :text="$t('receipt.closure.monthSearch')" forId="endDate" size="small" marginLeft="10px" />
          <DefaultTextfield
            type="month"
            id="monthInput"
            v-model="monthInput"
            size="xsmall"
            marginBottom="5px" 
          />
        </DefaultFormRow>
        <DefaultFormRow align="right">
          <DefaultLabel :text="$t('receipt.closure.nameLabel')" forId="nameSearch" size="small" />
          <DefaultTextfield
            id="nameSearch"
            v-model="keyword"
            :placeholder="$t('receipt.closure.namePlaceholder')"
            size="large"
          />
        </DefaultFormRow>
      </div>

      <!-- DefaultTable 컴포넌트 (큰 화면) -->
      <!-- :footerData="footerData" -->
      <DefaultTable
        :columns="columns"
        :data="filteredData"
        :selectable="true"
        v-model:selectedRows="selectedRows"
        :mobileCard="false"
        :fixedHeader="true"
        :bodyFontSize="'0.7rem'"
        :rowClick="openDetailModal"
        :heightAdjust="0"
        :noDataImageHeight="474"
        dynamic-style="474px"
      />
      <DefaultFormRow align="right" marginTop="10px">
        <DefaultButton
          size="small"
          @click="openConfirmModal('closed')">
          {{ $t('receipt.closure.close') }}
        </DefaultButton>
        <DefaultButton
          size="small"
          marginLeft="5px"
          color="red"
          @click="openConfirmModal('rejected')">
          {{ $t('receipt.common.reject') }}
        </DefaultButton>
      </DefaultFormRow>
    </div>
  </div>

  <!-- 마감 확인 모달 -->
  <AlertModal
    :isVisible="isConfirmModalVisible"
    :title="modalTitle"
    :confirmText="$t('receipt.closure.close')"
    :cancelText="$t('common.button.cancel')"
    :disableBackgroundClose="true"
    @confirm="handleConfirm"
    @close="isConfirmModalVisible = false"
  >
    <template #body>
      {{ modalBodyText }}
    </template>
  </AlertModal>

  <!-- 개인 영수증 상세 모달 -->
  <ReceiptDetailViewModal
    :isVisible="detailModalVisible"
    :receiptId="editingReceiptId"
    @close="detailModalVisible = false"
  />

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
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { ROUTES } from '@/config/menuConfig';
import { toast } from "vue3-toastify";
import { toDeptTeamDisplay } from '@/utils/blankFormat.js'
import { useAuthStore } from '@/store/auth';
import { APPROVAL_ROLE, getApprovalRoleLabel, RECEIPT_STATUS_LABELS, getReceiptStatusLabel } from '@/constants/receiptConstants';

/* ─── 공용 컴포넌트 ─── */
import DefaultTable from '@/components/common/table/DefaultTable.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import EditModal from '@/components/common/modal/EditModal.vue';
import ReceiptDetailViewModal from '@/components/receipt/ReceiptDetailViewModal.vue';

// API
import ReceiptsApi from '@/api/receipt/ReceiptsApi.js';
import ReceiptsRequestApi from '@/api/receipt/ReceiptsRequestApi.js';
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi.js';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME } from '@/constants';

// 상태 변수
const authStore = useAuthStore();
const data = ref([]);
const userList = ref([]);
const totalPages = ref(1);
const selectedRows = ref([]);
const keyword = ref('');
const monthInput = ref('');

// 모달 관련 상태
const isConfirmModalVisible = ref(false);
const isRejectReasonModalVisible = ref(false);
const confirmAction = ref('');
const rejectionReason = ref('');

// 과거 승인 월 목록을 저장할 상태 변수 추가
const pastApprovedMonths = ref([]);

// ─── 상세 모달 상태 ───
const detailModalVisible = ref(false);
const editingReceiptId = ref(null);
const isMobile = ref(window.innerWidth <= 650);
function updateIsMobile() { isMobile.value = window.innerWidth <= 650 }

// 반응형 전환: 모바일 전환 시 모달 닫기 + 재조회
watch(isMobile, (v) => {
  if (v) detailModalVisible.value = false;
  // 월 기반 조회이므로 현재 월 기준으로 다시 조회
  refreshData();
});

const router = useRouter();

function openDetailModal (row) {
  const id = row.receiptId
  if (isMobile.value) {
    try { sessionStorage.setItem('receiptDetail', JSON.stringify({ id })) } catch (e) {}
    router.push(ROUTES.RECEIPT.DETAIL_MOBILE);
    return;
  }
  editingReceiptId.value = id;
  detailModalVisible.value = true
}


// 테이블 컬럼 정의
const columns = ref([
  { key: 'receiptCode', label: '식별 번호', width: 85,  align: "center" },
  { key: 'date',        label: '발행일',    width: 100, align: "center" , mobileDisable:true },
  { key: 'name',        label: '이름',      width: 'auto', minWidth: 100 },
  { key: 'department',  label: '부서',      width: 120 , mobileDisable:true },
  { key: 'team',        label: '팀',        width: 120 , mobileDisable:true },
  { key: 'email',       label: '이메일',    width: 'auto', minWidth: 100 , mobileDisable:true },
  { key: 'type',        label: '구분',      width: 80,  align: 'center' , mobileDisable:true },
  { key: 'reason',      label: '사유',      width: 120, align: 'left' , mobileDisable:true },
  { key: 'amount',      label: '금액',      width: 80,  align: 'right' },
]);

// 영수증 데이터 → 테이블/모달에 사용할 형태로 변환
function mapRow (r) {
  /* ① 참여자 */
  const participants = (r.participantsList||[]).map(p=>({
    name       : p.participantName,
    department : toDeptTeamDisplay(p.department),
    team       : toDeptTeamDisplay(p.team),
    participantType: p.participantType || p.type || null,
    company    : p.company || null,
    position   : p.position || null,
    phone      : p.phone || null
  }))

  /* ② 결재선 */
  const approvers = (r.approvalLines||[]).map(a=>({
    userId        : a.approverUserId,
    name          : a.approverName,
    department    : toDeptTeamDisplay(a.department),
    team          : toDeptTeamDisplay(a.team),
    approvalRole  : a.approvalRole,
    approvalStatus: a.approvalStatus,
    approvalType  : getApprovalRoleLabel(a.approvalRole),
    stateText     : a.rejectedAt ? RECEIPT_STATUS_LABELS.REJECTED : (a.approvalStatus ? RECEIPT_STATUS_LABELS.APPROVED : RECEIPT_STATUS_LABELS.WAITING),
    rejectedReason: a.rejectedReason
  }))

  /* ③ 반려 사유 (결재선 중 존재하면) */
  const rejectedReason = approvers.find(a=>a.rejectedReason)?.rejectedReason || ''

  /* ④ 상태 텍스트 */
  const statusText = getReceiptStatusLabel(r.status)

  const amountRaw = +r.amount || 0

  return {
    /* 테이블 표시용 */
    receiptId  : r.receiptId,
    receiptCode: r.receiptCode,
    date       : r.submissionDate,
    name       : r.userName,
    department : toDeptTeamDisplay(r.departmentName),
    team       : toDeptTeamDisplay(r.teamName),
    email      : r.userEmail,
    type       : r.category?.categoryName || 'N/A',
    reason     : r.reason,
    amount     : amountRaw.toLocaleString(),
    amountRaw,

    /* 모달 표시용 */
    categoryId : r.category?.categoryId ?? null,
    receiptName: r.attachment?.fileName || r.attachment?.fileOriginName || '영수증 미등록',
    receipt    : r.attachment?.fileUrl ? getImagePreviewUrl(r.attachment.fileUrl, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : '',
    participants,
    approvers,
    statusText,
    rejectedReason
  }
}

// 서버에서 영수증 데이터 가져오기
async function fetchDataFromServer(startDate, endDate) {

  // 미정산 월 목록을 가져오는 API 호출
  const summaryResponse = await ReceiptsSearchApi.getApprClosedSummaryAll({ startDate, endDate });
  pastApprovedMonths.value = summaryResponse.data.pastApprovedMonths || [];

  const params = {
    page: 0,
    size: 100000,
    startDate,
    endDate,
    statusCode: '2' // APPROVED(2, "승인"),
  };
  const { data: pageDto } = await ReceiptsApi.getReceiptsBySearchUserDateRange(params);
  // 사용자 목록과 매핑하여 최종 데이터 생성
  data.value = (pageDto.content || []).map(mapRow);
  totalPages.value = pageDto.totalPages || 1;
}

// 로컬 검색 필터
const filteredData = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  if (!kw) {
    return data.value;
  }
  return data.value.filter(r =>
    (r.name && r.name.toLowerCase().includes(kw)) ||
    (r.email && r.email.toLowerCase().includes(kw))
  );
});

// 월 선택이 변경될 때마다 데이터 다시 조회
watch(monthInput, async (val) => {
  if (!val) return;
  const [year, month] = val.split('-');
  const startDate = `${val}-01`;
  const lastDay = new Date(year, month, 0).getDate();
  const endDate = `${val}-${String(lastDay).padStart(2, '0')}`;
  selectedRows.value = [];

  await fetchDataFromServer(startDate, endDate);
}, { immediate: true });

// 이름 검색어가 변경될 때마다 체크박스 초기화
watch(keyword, () => {
    selectedRows.value = []; // 체크박스 선택 초기화
});

// 컴포넌트 마운트 시 초기 데이터 로드
onMounted(async () => {
  // 현재 '월'로 `monthInput`을 초기화 (watch가 자동으로 데이터 조회를 트리거)
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  monthInput.value = `${yyyy}-${mm}`;
  window.addEventListener('resize', updateIsMobile);
  updateIsMobile();
});
onBeforeUnmount(() => { window.removeEventListener('resize', updateIsMobile) });

// 모달 제목과 본문 내용을 동적으로 생성
const modalTitle = computed(() => {
  return confirmAction.value === 'closed' ? '마감 확인' : '반려 확인';
});

const modalBodyText = computed(() => {
  const count = selectedRows.value.length;
  const actionText = confirmAction.value === 'closed' ? '마감' : '반려';
  return `선택한 ${count}개의 항목을 정말 ${actionText} 처리하시겠습니까?`;
});

// 버튼 클릭 시 모달 열기 로직 수정
function openConfirmModal(action) {
  if (selectedRows.value.length === 0) {
    const actionText = action === 'closed' ? '마감' : '반려';
    return toast.warn(`${actionText}할 항목을 선택해주세요.`);
  }
  confirmAction.value = action;

  if (action === 'rejected') { // '반려'일 경우
    rejectionReason.value = ''; // 반려 사유 초기화
    isRejectReasonModalVisible.value = true; // 사유 입력 모달 열기
  } else { // '마감'일 경우
    isConfirmModalVisible.value = true; // 기존 확인 모달 열기
  }
}

// '마감' 처리 함수
async function handleConfirm() {
  isConfirmModalVisible.value = false;
  const ids = selectedRows.value.map(row => row.receiptId);
  await ReceiptsRequestApi.closeReceipts(ids);
  toast.success(`${ids.length}건이 마감 처리되었습니다.`);
  await refreshData();
}

// '반려' 처리 함수
async function handleRejectConfirm() {
  isRejectReasonModalVisible.value = false;
  if (!rejectionReason.value.trim()) {
    return toast.warn('반려 사유를 입력해주세요.');
  }
  const ids = selectedRows.value.map(row => row.receiptId);
  await ReceiptsRequestApi.rejectReceipts(ids, rejectionReason.value);
  toast.success(`${ids.length}건이 반려 처리되었습니다.`);
  await refreshData();
}

// 데이터 새로고침 및 선택 초기화 함수
async function refreshData() {
  const [year, month] = monthInput.value.split('-');
  const startDate = `${monthInput.value}-01`;
  const lastDay = new Date(year, month, 0).getDate();
  const endDate = `${monthInput.value}-${String(lastDay).padStart(2, '0')}`;
  await fetchDataFromServer(startDate, endDate);
  selectedRows.value = [];
}
</script>

<style scoped>
/* .content-sub-title {
  margin-bottom: 20px !important;
  line-height: 1.6;
} */
.content-sub-title {
  margin-bottom: 10px !important;
}
.search-controls {
  margin-top: 0px;
  margin-bottom: 10px;
}

@media (max-width: 650px) {
  .search-controls {
    /* margin-top: 10px; */
  }
}
</style>