<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper" :class="{ 'content-expanded': !isMobile }">
      <PageTitle 
        :title="`${userName}${$t('receipt.approvalSummary.detailTitle')}`"
        :subtitle="`${$t('receipt.approvalSummary.dateRange')}${filteredStartDate} ~ ${filteredEndDate}${$t('receipt.approvalSummary.dateRangeEnd')}`"
        :show-back-button="true"
        @back="goBack"
      />
      
      <DefaultButton
        size="small"
        align="right"
        marginBottom="10px"
        @click="onDownloadExcel()">
        {{ $t('receipt.ceoReport.excelDownload') }}
      </DefaultButton>

      <!-- 테이블 보기 (큰 화면) -->
      <DefaultTable
        class="receipt-table"
        :mobileCard="true" 
        :columns="columns"
        :data="data"
        :rowClick="openDetailModal"
        :selectable="true"
        v-model:selectedRows="selectedRows"
        @selection-change="onSelection"
        :usePagination="true"
        :currentPage="currentPage"
        :totalPages="totalPages"
        :visiblePageCount="visiblePageCount"
        @pageChange="onPageChange"
        :noDataImageHeight="474"
      />
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
            transformOrigin: `${zoomOrigin.x}px ${zoomOrigin.y}px`
          }"
          @dblclick="toggleZoom"
          @touchstart="toggleZoom"
        />
      </div>
    </div>
  </div>

  <ReceiptDetailViewModal
    :isVisible="detailModalVisible"
    :receipt="editingReceipt"
    @close="detailModalVisible = false"
    @updated="fetchDataFromServer(currentPage)"
  />
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ROUTES } from '@/config/menuConfig';
import ReceiptDetailViewModal from '@/components/receipt/ReceiptDetailViewModal.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTable from '@/components/common/table/DefaultTable.vue'; 
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi';
import ReceiptsPrintApi from '@/api/receipt/ReceiptsPrintApi.js';
import HrmUserApi               from '@/api/hrm/UsersApi';
import { usePreviewModal } from '@/utils/preview-modal';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME } from '@/constants';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/store/auth'
import { toDeptTeamDisplay } from '@/utils/blankFormat.js'
import { toast } from "vue3-toastify";
import { 
  RECEIPT_STATUS,
  RECEIPT_STATUS_LABELS, 
  getReceiptStatusLabel,
  getApprovalRoleLabel
} from '@/constants/receiptConstants';

const route = useRoute();
const authStore = useAuthStore();
const approverId = ref(authStore.getUserId);

// =====================
// 1) 날짜 표시 
// =====================
const filteredStartDate = ref('');
const filteredEndDate = ref('');
const userId = ref('');
const userName = ref('');

// =====================
// 2) 상세 모달 / 모바일
// =====================
const detailModalVisible = ref(false);
const editingReceipt     = ref(null);
const isMobile = ref(window.innerWidth <= 650);
function updateIsMobile() {
  isMobile.value = window.innerWidth <= 650;
}


// =====================
// 3) 미리보기 모달
// =====================
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
  endDrag
} = usePreviewModal();
function closePreviewModalOnOutsideClick(event) {
  if (!event.target.classList.contains('preview-modal-image')) {
    isPreviewVisible.value = false;
  }
}

// =====================
// 4) 데이터 / 페이지네이션
// =====================
const data = ref([]);
const selectedRows = ref([]);
const currentPage = ref(1);
const totalPages = ref(1);
const visiblePageCount = ref(5);  // 페이지네이션에서 보여줄 최대 페이지 버튼 수
// 반응형 전환 시 재조회 및 페이징 리셋, 모달 닫기
watch(isMobile, (v) => {
  if (v) detailModalVisible.value = false;
  currentPage.value = 1;
  fetchDataFromServer(1);
});

// **테이블 컬럼** 정의
// 각각의 'key'가 item[key]에 대응
// 필요 시 customClass, customValue 등 추가 가능
const columns = [
  { key:'receiptCode', label:'식별 번호', width:85, align:'center',
    mobile:{ line:1, inline:true, bold:true, prefix:'🧾 코드\u00a0:\u00a0' } },

  { key:'date', label:'발행일', width:100, align:'center',
    mobile:{ line:1, inline:true, align:'right' } },

  { key:'peopleCount', label:'총 인원', width:60, align:'center',
    mobile:{ line:2, inline:false, prefix:'인원\u00a0:\u00a0' } },

  { key:'type', label:'구분', width:100,
    mobile:{ line:3, inline:false, prefix:'구분\u00a0:\u00a0' } },

  { key:'reason', label:'사유', width:'auto', minWidth: 100,
    mobile:{ line:4, inline:false, prefix:'사유\u00a0:\u00a0' } },

  { key: 'amount', label: '금액', width: 100, align: 'right',
    mobile:{ line:5, inline:false, prefix:'금액\u00a0:\u00a0' } },

  {
    key: 'amountPerPerson',
    label: '금액/인원수',
    width: 100,
    align: 'right',
    mobile:{ line:6, inline:false, prefix:'1인당\u00a0:\u00a0' },
    customValue: (item) => {
      const amount      = parseInt(item.amount.replace(/[^0-9]/g, ''), 10);
      return calculateAmountPerPerson(amount, item.peopleCount);   // ✅ 수정
    }
  },
  {
    key: 'status',
    label: '결제 상황',
    width: 75,
    align: 'center',
    mobile:{ line:7, inline:false, prefix:'상태\u00a0:\u00a0' },
    // 동적 클래스 지정 (DefaultTable.vue에서 customClass 사용)
    customClass: (value) => {
      if (value === RECEIPT_STATUS_LABELS.REQUEST) return 'text-blue';
      if (value === RECEIPT_STATUS_LABELS.APPROVED) return 'text-green';
      if (value === RECEIPT_STATUS_LABELS.REJECTED) return 'text-red';
      return '';
    }
  },
];

/* (선택) 체크 변경 시 호출 */
function onSelection(rows) {
  // rows = 현재 체크된 행 배열
  console.log('선택된 행:', rows);
  // 예: 총 합계, 엑셀 다운로드 대상 등으로 활용
}

// =========== 서버 연동: 데이터 가져오기 ===========
// [변경] 실제 백엔드 호출로직
async function fetchDataFromServer(page = 1) {
  const pageSize = isMobile.value ? 4 : 10;

  // 백엔드: GET /receipts/user/{userId}/with-status?yearMonth=2025-04&page=...

  const response = await ReceiptsSearchApi.getReceiptsByUserAndDateRange(
    userId.value,
    {
      page     : page - 1,
      size     : pageSize,
      startDate: filteredStartDate.value,
      endDate  : filteredEndDate.value,
      statusCodes: [RECEIPT_STATUS.APPROVED, RECEIPT_STATUS.CLOSED]          // 승인·마감만
    }
  );

  // (3) 응답 데이터 구조 
  // { receiptPage: {...}, statusList: [...] }
  const receiptPage = response.data;
  // const statusList = response.data.statusList; 
  // statusList를 별도로 활용할 수도 있음 (ex. 드롭다운, 상태 변경 등)

  // receiptPage.content -> 영수증 배열
  const content = receiptPage.content;

  // (4) 각 영수증 데이터를 프론트에서 쓰기 편한 형태로 가공
  const transformed = content.map(r => {
    const peopleArr = r.participantsList?.map(p => ({
      name       : p.participantName,
      department : toDeptTeamDisplay(p.department),
      team       : toDeptTeamDisplay(p.team),
      participantType: p.participantType || p.type || null,
      company    : p.company || null,
      position   : p.position || null,
      phone      : p.phone || null
    })) || [];
    const peopleCount = peopleArr.length + 1;

    // 영수증 첨부파일
    const attachment = r.attachment;
    const receiptName = attachment ? attachment.fileName : '영수증 미등록';
    const receiptUrl = attachment && attachment.fileUrl 
      ? getImagePreviewUrl(attachment.fileUrl, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) 
      : '';

    /* ② 결재선 → 모달에서도 쓰므로 포함 */
    const approverArr = (r.approvalLines ?? []).map(al => ({
      userId:         al.approverUserId,
      name:           al.approverName,
      department:     toDeptTeamDisplay(al.department),
      team:           toDeptTeamDisplay(al.team),
      approvalRole:   al.approvalRole,                 // 1=결재, 2=합의
      approvalStatus: al.approvalStatus,
      approvalType:   getApprovalRoleLabel(al.approvalRole),
      stateText:      al.rejectedAt
                        ? RECEIPT_STATUS_LABELS.REJECTED
                        : (al.approvalStatus ? RECEIPT_STATUS_LABELS.APPROVED : RECEIPT_STATUS_LABELS.WAITING),
      rejectedReason: al.rejectedReason,
    }));

    // 금액 변환
    const amountVal = r.amount ? parseInt(r.amount, 10) : 0;
    const amountStr = amountVal.toLocaleString() + '원';

    /* 상태 (신청, 승인, 반려, 마감 …) */
    const statusCode =
      typeof r.status === 'string'
        ? r.status                     // 'REQUEST' | 'APPROVED' | 'REJECTED' | 'CLOSED'
        : r.status?.statusCode ?? 'REQUEST';

    const statusDesc = getReceiptStatusLabel(statusCode);
    
    // 반려 사유는 예시로 reason이나 별도 필드를 사용
    const rejectionReason = r.reason ? r.reason : ''; // 예시

    // 스크롤 맨 위로 이동(모바일 카드형식일때)
    if (isMobile.value) {
      window.scrollTo(0, 0)
    }

    return {
      receiptId: r.receiptId,
      receiptCode: r.receiptCode,
      date: r.submissionDate,
      type: r.category?.categoryName || '',
      categoryId: r.category?.categoryId ?? null,
      reason: r.reason,                // 예: '야근'
      amount: amountStr,               // '80,000원'
      amountRaw: amountVal,
      people: peopleArr,               // [{ name, dept, team }]
      peopleCount,
      approvers: approverArr,
      amountPerPerson: calculateAmountPerPerson(amountVal, peopleCount),
      status: statusDesc,              // '신청'|'승인'|'반려'...
      rejectionReason,
      receiptName,
      receipt: receiptUrl
    };
  });

  // (5) 결과 할당
  data.value = transformed;
  totalPages.value = receiptPage.totalPages;
}

// 페이지 전환 (DefaultPagination → @pageChange)
function onPageChange(newPage) {
  currentPage.value = newPage;
  fetchDataFromServer(newPage);
}

// =====================
// 5) 기타 로직
// =====================
// 금액/인원수 계산
function calculateAmountPerPerson(amountVal, peopleCount) {
  if (!peopleCount) return '0원';
  return Math.floor(amountVal / peopleCount).toLocaleString() + '원';
}

// 결제 상황별 CSS 클래스
function getStatusClass(status) {
  if (status === RECEIPT_STATUS_LABELS.REQUEST) return 'status-pending';
  if (status === RECEIPT_STATUS_LABELS.APPROVED)   return 'status-approved';
  if (status === RECEIPT_STATUS_LABELS.REJECTED)   return 'status-rejected';
  return '';
}

// 모바일 인원 목록 토글
const openedIndex = ref(null);
function togglePeopleList(idx) {
  openedIndex.value = openedIndex.value === idx ? null : idx;
}
function showPeopleList(index) {
  return openedIndex.value === index;
}

function openDetailModal(row) {
  const payload = {
    id:            row.receiptId,
    date:          row.date,
    type:          row.type,
    categoryId:    row.categoryId,
    amount:        String(row.amountRaw ?? ''), 
    reason:        row.reason,
    receiptName:   row.receiptName,
    receipt:       row.receipt,
    participants:  [...row.people],
    approvers:     [...row.approvers],
    statusText:    row.status,
    rejectedReason:row.rejectionReason
  };
  if (isMobile.value) {
    try { sessionStorage.setItem('receiptDetail', JSON.stringify(payload)) } catch (e) {}
    router.push(ROUTES.RECEIPT.DETAIL_MOBILE)
    return
  }
  editingReceipt.value = payload;
  detailModalVisible.value = true;
}

// 뒤로가기
const router = useRouter();
function goBack() {
  router.back();
}

/**
 * 선택된 영수증 목록을 서버로 전송해
 * 엑셀 파일을 받아 다운로드 처리합니다.
 */
async function onDownloadExcel() {
  if (selectedRows.value.length === 0) {
    return toast.warning("다운로드할 영수증을 하나 이상 선택해주세요.");
  }
  // 선택된 행에서 ID만 추출
  const receiptIds = selectedRows.value.map(r => r.receiptId);
  
  userId.value
  userName.value
  const { data: me } = await HrmUserApi.getUserById(authStore.getUserId)
  const teamName = (me.team.teamName ?? '미지정').trim().toUpperCase();
  const deptName = (me.team?.department?.departmentName ?? '미지정').trim().toUpperCase()
  const position = (me.position.positionName ?? '미지정').trim().toUpperCase();

  // 백엔드에서 Blob(EXCEL) 반환
  const response = await ReceiptsPrintApi.approvalSummaryExcelDownload(
    userId.value, 
    {
      userName : userName.value,
      deptName : deptName,
      teamName : teamName,
      position : position,
      receiptIds : receiptIds
    }
  );

  // 1. 응답 헤더에서 Content-Disposition 값 가져오기
  const contentDisposition = response.headers['content-disposition'];
  let fileName = '영수증_승인(마감)_상세.xlsx'; // 기본 파일명

  if (contentDisposition) {
    // 2. 정규식을 사용하여 filename* 또는 filename 값 추출
    const fileNameMatch = contentDisposition.match(/filename\*?=(?:UTF-8'')?([^;]+)/);
    if (fileNameMatch && fileNameMatch[1]) {
      // 3. URL 디코딩하여 실제 파일명 복원
      fileName = decodeURIComponent(fileNameMatch[1].replace(/"/g, ''));
    }
  }

  // 4. Blob 데이터로 다운로드 링크 생성
  const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', fileName); // 서버에서 받은 파일명으로 설정
  document.body.appendChild(link);
  link.click();

  // 5. 생성된 링크와 URL 정리
  link.remove();
  window.URL.revokeObjectURL(url);
}

// =====================
// 6) onMounted
// =====================
onMounted(() => {
  filteredStartDate.value = route.query.startDate || '';
  filteredEndDate.value = route.query.endDate || '';
  userId.value = route.query.userId || '';
  userName.value = route.query.userName || '';

  if (!userId.value || !filteredStartDate.value || !filteredEndDate.value) {
    router.back();
    return;
  }

  fetchDataFromServer();
  window.addEventListener('resize', updateIsMobile);
  updateIsMobile();
});
</script>

<style scoped>
/* 반응형 카드 레이아웃 */
.card-layout {
  margin-top: 40px;
}

/* 상태별 색상 */
.status-approved {
  color: green;
  font-weight: bold;
}
.status-pending {
  color: blue;
  font-weight: bold;
}
.status-rejected {
  color: red;
  font-weight: bold;
}

/* 인원 목록 */
.people-list {
  font-size: 0.7rem;
  max-height: 150px;
  overflow-y: auto;
  margin-top: 10px;
  border-top: 1px solid #ddd;
  padding-top: 10px;
}
.people-list-header {
  font-weight: bold;
  margin-bottom: 5px;
}
.people-list-item {
  padding: 1px 0;
  margin: 0;
  margin-left: 10px;
}

/* 이미지 미리보기 모달 */
.preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.7);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.preview-modal-content {
  position: relative;
}
.preview-modal-image {
  max-width: 80vw;
  max-height: 80vh;
  transition: transform 0.2s ease;
}
.zoomed {
  cursor: move; /* 확대된 상태에서는 드래그 가능 */
}

/* ─── 카드 헤더 버튼 일렬 배치 ─────────────────── */
.card-actions {
  display: flex; /* 가로 한 줄 */
  flex-wrap: nowrap; /* 줄바꿈 방지 */
  gap: 4px; /* 버튼 간격 */
  margin-left: auto;
}

/* DefaultButton 내부 <button> 폭 덮어쓰기 */
.card-actions :deep(button) {
  width: auto !important; /* 100% → auto */
  min-width: 50px; /* 필요 시 최소 너비 지정 */
  padding: 2px 6px; /* XS 버튼 느낌 */
  line-height: 1.3;
}
.card-header {
  display: flex;
  align-items: center;
}

@media (max-width: 650px) {
}
</style>
