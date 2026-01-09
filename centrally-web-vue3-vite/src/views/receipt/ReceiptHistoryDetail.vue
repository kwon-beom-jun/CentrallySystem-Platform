<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper" :class="{ 'content-expanded': !isMobile }">
      <PageTitle 
        :title="`${userName}${$t('receipt.common.detailTitle')}`"
        :subtitle="`${$t('receipt.common.dateRange')}${filteredStartDate} ~ ${filteredEndDate}${$t('receipt.common.dateRangeEnd')}`"
        :show-back-button="true"
        @back="goBack"
      />

      <div class="padding-h-30" v-if="!isMobile" />

      <!-- 테이블 보기 (큰 화면) -->
      <DefaultTable
        class="receipt-table"
        :mobileCard="true"
        :columns="columns"
        :data="data"
        :rowClick="openDetailModal"
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

    <!-- 상세 모달 -->
    <ReceiptDetailViewModal
      :isVisible="detailModalVisible"
      :receiptId="editingReceiptId"
      @close="detailModalVisible = false"
      @updated="fetchDataFromServer(currentPage)"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ROUTES } from '@/config/menuConfig';
import ReceiptDetailViewModal from '@/components/receipt/ReceiptDetailViewModal.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTable from '@/components/common/table/DefaultTable.vue'; 
import DefaultFormRow from '@/components/common/DefaultFormRow.vue'
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi';
import { usePreviewModal } from '@/utils/preview-modal';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME } from '@/constants';
import { useAuthStore } from '@/store/auth'
import { toDeptTeamDisplay } from '@/utils/blankFormat.js'
import { useViewStateStore } from '@/store/viewState'
import { 
  RECEIPT_STATUS_LABELS, 
  getReceiptStatusLabel,
  APPROVAL_ROLE,
  getApprovalRoleLabel
} from '@/constants/receiptConstants';

const route = useRoute();
const authStore = useAuthStore();
const viewState = useViewStateStore();
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
const editingReceiptId   = ref(null);
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
const currentPage = ref(1);
const totalPages = ref(1);
const visiblePageCount = ref(5);  // 페이지네이션에서 보여줄 최대 페이지 버튼 수
// 반응형 전환 시 재조회 및 페이징 리셋
watch(isMobile, () => {
  currentPage.value = 1;
  fetchDataFromServer(1);
});

// **테이블 컬럼** 정의
// 각각의 'key'가 item[key]에 대응
// 필요 시 customClass, customValue 등 추가 가능
const columns = [
  { key:'receiptCode',  label:'식별 번호', width:100,  align:'center',
    mobile:{ line:1, inline:true,  prefix:'📝 글번호\u00a0:\u00a0', bold:true } },

  { key:'date',         label:'발행일',   width:120, align:'center',
    mobile:{ line:1, inline:true,  align:'right' } },

  { key:'peopleCount',  label:'총 인원',  width:60,  align:'center',
    mobile:{ dividerTop: true, line:2, inline:false, prefix:'총 인원\u00a0:\u00a0' } },

  { key:'type',         label:'구분',     width:200,
    mobile:{ line:3, inline:false, prefix:'구분\u00a0:\u00a0' } },

  { key:'reason',       label:'사유',     width:'auto', minWidth: 100,
    mobile:{ line:4, inline:false, prefix:'사유\u00a0:\u00a0' } },

  { key:'amount',       label:'금액',     width:150, align:'right',
    mobile:{ line:5, inline:false, prefix:'금액\u00a0:\u00a0' } },

  { key:'amountPerPerson', label:'금액/인원수', width:150, align:'right',
    mobile:{ dividerBottom: true, line:6, inline:false, prefix:'금액/인원\u00a0:\u00a0' },
    customValue: (item) => {
      const amount      = parseInt(item.amount.replace(/[^0-9]/g, ''), 10);
      return calculateAmountPerPerson(amount, item.peopleCount);   // ✅ 수정
    }
  },

  { key:'status',       label:'결제 상황', width:100, align:'center',
    mobile:{ line:7, align: 'right', bold: true, inline:false, prefix:'결제 상황\u00a0:\u00a0' },
    // 동적 클래스 지정 (DefaultTable.vue에서 customClass 사용)
    customClass: (value) => {
      if (value === RECEIPT_STATUS_LABELS.WAITING) return 'text-gray';
      if (value === RECEIPT_STATUS_LABELS.REQUEST) return 'text-primary';
      if (value === RECEIPT_STATUS_LABELS.APPROVED) return 'text-success';
      if (value === RECEIPT_STATUS_LABELS.REJECTED) return 'text-danger';
      if (value === RECEIPT_STATUS_LABELS.CLOSED) return 'text-gray';
      return '';
    }
  },
];

// =========== 서버 연동: 데이터 가져오기 ===========
// [변경] 실제 백엔드 호출로직
async function fetchDataFromServer(page = 1) {
  const pageSize = isMobile.value ? 4 : 10;

  // 백엔드: GET /receipts/user/{userId}/with-status?yearMonth=2025-04&page=...
  const response = await ReceiptsSearchApi.getPendingReceiptsByApprover(
    approverId.value,
    {
      page: page - 1,
      size: pageSize,
      userId: userId.value,
      startDate: filteredStartDate.value,
      endDate:   filteredEndDate.value,
    }
  )

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
      name: p.participantName,
      department: toDeptTeamDisplay(p.department),
      team      : toDeptTeamDisplay(p.team),
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

    /* ② 결재선 → [대리] 표기 및 대리자 부서/팀 반영 */
    const approverArr = (r.approvalLines ?? []).map(al => ({
      userId:         al.approverUserId,
      name:           al.delegateMapping ? ('[대리] ' + (al.delegateMapping.delegateName || al.approverName)) : al.approverName,
      department:     toDeptTeamDisplay(al.delegateMapping ? al.delegateMapping.delegateDepartment : al.department),
      team:           toDeptTeamDisplay(al.delegateMapping ? al.delegateMapping.delegateTeam       : al.team),
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

    // 상태 (신청, 승인, 반려, etc.)
    const statusCode =
      typeof r.status === "string"
        ? r.status // 'REQUEST' | 'APPROVED' | 'REJECTED'
        : r.status?.statusCode ?? "REQUEST";

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
   if (status === '대기')   return 'status-waiting';
   if (status === '신청')   return 'status-pending';
   if (status === '승인')   return 'status-approved';
   if (status === '반려')   return 'status-rejected';
   if (status === '마감')   return 'status-closed';
  return '';
}

watch(isMobile, v => { if (v) detailModalVisible.value = false })
function openDetailModal(row) {
  if (isMobile.value) {
    // 모바일은 기존 동작 유지 (별도 모바일 상세 페이지)
    // 상태 저장 후 이동
    viewState.saveState('receiptHistoryDetail', {
      currentPage: currentPage.value,
      scrollY: window.scrollY,
    })
    try { sessionStorage.setItem('receiptDetail', JSON.stringify({ id: row.receiptId })) } catch (e) {}
    router.push(ROUTES.RECEIPT.DETAIL_MOBILE)
    return
  }
  editingReceiptId.value = row.receiptId;
  detailModalVisible.value = true;
}

// 뒤로가기
const router = useRouter();
function goBack() {
  router.back();
}

// =====================
// 6) onMounted
// =====================
onMounted(async () => {
  filteredStartDate.value = route.query.startDate || '';
  filteredEndDate.value = route.query.endDate || '';
  userId.value = route.query.userId || '';
  userName.value = route.query.userName || '';

  if (!userId.value || !filteredStartDate.value || !filteredEndDate.value) {
    router.back();
    return;
  }

  window.addEventListener('resize', updateIsMobile);
  updateIsMobile();

  // viewState 복원
  const saved = viewState.getState('receiptHistoryDetail')
  const restore = viewState.canRestore('receiptHistoryDetail')
  if (restore && saved) {
    currentPage.value = saved.currentPage || 1
    await fetchDataFromServer(currentPage.value)
    requestAnimationFrame(() => { window.scrollTo(0, saved.scrollY ?? 0) })
  } else {
    await fetchDataFromServer()
  }
});

// 모바일 상세에서만 복원 허용
viewState.allowRestoreFrom('receiptHistoryDetail', ['ReceiptDetailViewMobile'])
</script>

<style scoped>
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
.status-waiting { color: gray;   font-weight: bold; }
.status-closed  { color: gray;   font-weight: bold; }

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
</style>
