<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('receipt.management.title')"
        :subtitle="$t('receipt.management.subtitle')"
        icon="ri-database-2-line"
        desktopMarginBottom="25px"
      />

      <!-- 검색 영역 -->
      <div class="search-controls">
        <!-- 시작, 종료일 그룹 -->
        <DefaultFormRow align="right">
          <DefaultLabel :text="$t('receipt.management.startDate')" forId="startDate" size="small" />
          <DefaultTextfield
            type="date"
            id="startDate"
            v-model="startDate"
            size="xsmall"
          />
          <DefaultLabel :text="$t('receipt.management.endDate')" forId="endDate" size="small" marginLeft="10px" />
          <DefaultTextfield
            type="date"
            id="endDate"
            v-model="endDate"
            size="xsmall"
          />
        </DefaultFormRow>
        <!-- 이름 검색 그룹 -->
        <DefaultFormRow align="right" marginTop="7px">
          <DefaultLabel :text="$t('receipt.management.nameLabel')" forId="nameSearch" size="small" />
          <UserSearchDropdown
            ref="userSearchRef"
            labelText="사용자 검색"
            inputId="nameSearch"
            inputSize="large"
            placeholder="사용자(이메일)을 검색해주세요"
            :includeCurrentUser="true"
            :initialValue="selectedUserSearchValue"
            @userSelected="onUserSelected"
          />
          <DefaultButton 
            @click="search"
            color="gray"
            size="small">
            {{ $t('receipt.management.searchButton') }}
          </DefaultButton>
        </DefaultFormRow>
      </div>

      
      <!-- 테이블: 사용자 ID/이메일 컬럼 추가됨 -->
      <!-- :scroll="true"
      :scrollHeight="420" -->
      <DefaultTable
        :columns="columns"
        :data="data"
        @delete-row="deleteReceipt"
        @row-updated="handleRowUpdated"
        :rowClick="onRowClick"
        :selectHeight="'28px'"
        :buttonHeight="'28px'"
        :fixMobileHeight="true"
        :minRows="pageSize"
        :usePagination="true"
        :currentPage="currentPage"
        :totalPages="totalPages"
        :visiblePageCount="visiblePageCount"
        :noDataImageHeight="468"
        @pageChange="onPageChange"
      />
    </div>
  </div>
  
  <!-- ─── 이미지 미리보기 모달 ─── -->
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
        class="preview-modal-image"
        :class="{ zoomed: isZoomed }"
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

  <ReceiptDetailViewModal
    :isVisible="detailVisible"
    :receiptId ="selectedReceiptId"
    @close="detailVisible = false"
    @updated="fetchDataFromServer(currentPage)"
  />

  <!-- 삭제 확인 AlertModal -->
  <AlertModal
    :isVisible="deleteConfirmVisible"
    :disableBackgroundClose="false"
    :confirmText="$t('common.button.confirm')"
    :cancelText="$t('common.button.cancel')"
    @close="deleteConfirmVisible = false"
    @confirm="deleteNotice"
  >
    <template #body>
      <p>{{ $t('receipt.management.deleteWarning') }}</p>
      <p> 
        {{ $t('receipt.management.deleteMessage') }} <br/>
        {{ $t('receipt.management.deleteProceed') }} <br/>
      </p>
      <div style="color: red;">
        {{ $t('receipt.management.deleteReceipt') }} #{{ receiptToDelete.receiptCode }} ]
      </div>
    </template>
  </AlertModal>

  <AlertModal
    :isVisible="statusUpdateConfirmVisible"
    :disableBackgroundClose="true"
    :title="$t('receipt.management.statusChangeConfirm')"
    :confirmText="$t('common.button.modify')"
    :cancelText="$t('common.button.cancel')"
    @close="cancelStatusUpdate"
    @confirm="confirmStatusUpdate"
  >
    <template #body>
      {{ $t('receipt.management.statusChangeMessage') }}
    </template>
  </AlertModal>
</template>

<script setup>
/* ──────────────── imports ──────────────── */
import { ref, watch, onMounted, onBeforeUnmount, computed }       from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ROUTES } from '@/config/menuConfig'
import AlertModal                      from '@/components/common/modal/AlertModal.vue'
import DefaultTable                    from '@/components/common/table/DefaultTable.vue'
import DefaultButton                   from '@/components/common/button/DefaultButton.vue'
import DefaultTextfield                from '@/components/common/textfield/DefaultTextfield.vue'
import DefaultLabel                    from '@/components/common/label/DefaultLabel.vue'
import DefaultFormRow                  from '@/components/common/DefaultFormRow.vue'
import UserSearchDropdown              from '@/components/auth/UserSearchDropdown.vue'
import ReceiptDetailViewModal          from '@/components/receipt/ReceiptDetailViewModal.vue'
import { usePreviewModal }             from '@/utils/preview-modal'
import { getImagePreviewUrl }           from '@/utils/fileUtils'
import { RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME } from '@/constants'

/**
 * 영수증 이미지 미리보기 (게이트웨이를 거쳐 정적 리소스 접근)
 */
function openReceiptPreview(fileUrl) {
  const previewUrl = getImagePreviewUrl(fileUrl, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME);
  openPreviewModal(previewUrl);
}
import { 
  RECEIPT_STATUS_LABELS, 
  getReceiptStatusLabel,
  getApprovalRoleLabel
} from '@/constants/receiptConstants'

import UsersApi                        from '@/api/hrm/UsersApi'
import ReceiptsApi                     from '@/api/receipt/ReceiptsApi'
import ReceiptsRequestApi              from '@/api/receipt/ReceiptsRequestApi'
import { toast }                       from 'vue3-toastify'
import { getRolesFrom } from '@/utils/roleUtils';
import { toDeptTeamDisplay } from '@/utils/blankFormat.js'
import { validateApprovers } from '@/utils/receipt/receiptUtils'
// import { usePageSize8or6 } from '@/composables/usePageSize'
import { useViewStateStore } from '@/store/viewState'
import { adjustPageAndRefetch } from '@/utils/paginationUtils'

/* ──────────────── reactive state ──────────────── */
const { t } = useI18n()
const isMobile = ref(window.innerWidth <= 650)
function updateIsMobile(){ isMobile.value = window.innerWidth <= 650 }
const startDate      = ref('')
const endDate        = ref('')
const searchUserId   = ref('')
const selectedUserSearchValue = ref('')
const userSearchRef  = ref(null)

const data           = ref([])

const currentPage    = ref(1)
const totalPages     = ref(1)
const visiblePageCount = ref(5)
// const pageSize       = usePageSize8or6()  // 반응형 페이지 사이즈 (데스크탑: 10, 모바일: 6)
const pageSize       = ref(8)
const viewState = useViewStateStore()

/* 사용자 캐시 : { [userId]: { userName, userEmail, department, team } } */
const userMap        = ref({})

/* 상세/삭제 모달 */
const detailVisible        = ref(false)
const selectedReceiptId    = ref(null)
const deleteConfirmVisible = ref(false)
const receiptToDelete      = ref(null)

// 상태 변경 확인 모달용 상태 변수
const statusUpdateConfirmVisible = ref(false);
const rowToUpdateStatus = ref(null);

/* preview-modal */
const {
  isPreviewVisible, previewImage, isZoomed,
  zoomedPosition, zoomOrigin,
  toggleZoom, startDrag, onDrag, endDrag
} = usePreviewModal()
const closePreviewModalOnOutsideClick = e => {
  if (!e.target.classList.contains('preview-modal-image'))
    isPreviewVisible.value = false
}

/* ──────────────── helpers ──────────────── */
const perPerson    = (amt,cnt)=>`${Math.floor(amt/cnt).toLocaleString()}원`
const today        = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

/* ──────────────── table schema ──────────────── */
const columns = computed(() => [
  { key:'receiptCode',label:t('receipt.common.receiptCode'),  width:85,     align: 'center' },
  { key:'userName',   label:t('receipt.common.name'),      width:'auto', minWidth: 60 ,     align: 'center' },
  { key:'userEmail',  label:t('hrm.userInfo.email'),     width:'auto', minWidth: 100 , mobileDisable:true },
  { key:'date',       label:t('receipt.common.issueDate'),     width:100 , mobileDisable:true },
  { key:'type',       label:t('receipt.common.type'),       width:90  , mobileDisable:true },
  { key:'peopleCount',label:t('receipt.common.totalPeople'),    width:70,     align: 'center' , mobileDisable:true },
  { key:'reason',     label:t('receipt.common.reason'),       width:120 , mobileDisable:true },
  { key:'amount',     label:t('receipt.common.amount'),       width:75,    align: 'right' , mobileDisable:true },
  { key:'amountPerPerson', label:t('receipt.common.amountPerPerson'), width:90, align: 'right' , mobileDisable:true },
  {
    key:'status',
    label:t('receipt.history.status'),
    width:95,
    type:'select',
    getOptions:() => Object.entries(RECEIPT_STATUS_LABELS).map(([v,l])=>({ value:v, label:l }))
  },
  { key:'delete',
    label:'',
    width:80,
    type:'button',
    buttonText:t('common.button.delete'),
    buttonColor:'red',
    buttonSize:'full-small',
    emit:'delete-row'
  }
])

async function fetchReceipts (page = 1) {
  const { data: pageDto } = await ReceiptsApi.getReceiptsWithStatus({
    startDate: startDate.value,
    endDate  : endDate.value,
    userId   : searchUserId.value,
    page     : page - 1,
    size     : pageSize.value
  })

  data.value = (pageDto.content || []).map(r => {

    /* participants: 이름 누락 시 보완 */
    const participants = (r.participantsList || []).map(p => {
      const fallback = userMap.value[p.participantUserId] ?? {}
      return {
        name      : p.participantName || fallback.userName || t('common.unknown'),
        department: toDeptTeamDisplay(p.department || fallback.department),
        team      : toDeptTeamDisplay(p.team || fallback.team),
        participantType: p.participantType || p.type || null,
        company   : p.company || null,
        position  : p.position || null,
        phone     : p.phone || null
      }
    })
    const peopleCount = participants.length + 1

    /* approvers - 대리 표기 및 대리자 부서/팀 반영 */
    const approvers = (r.approvalLines || []).map(al => ({
      userId        : al.approverUserId,
      name          : al.delegateMapping ? ('[대리] ' + (al.delegateMapping.delegateName || al.approverName)) : al.approverName,
      department    : toDeptTeamDisplay(al.delegateMapping ? al.delegateMapping.delegateDepartment : al.department),
      team          : toDeptTeamDisplay(al.delegateMapping ? al.delegateMapping.delegateTeam       : al.team),
      approvalRole  : al.approvalRole,
      approvalType  : getApprovalRoleLabel(al.approvalRole),
      approvalStatus: al.approvalStatus,
      stateText     : al.rejectedAt ? RECEIPT_STATUS_LABELS.REJECTED : al.approvalStatus ? RECEIPT_STATUS_LABELS.APPROVED : RECEIPT_STATUS_LABELS.WAITING,
      rejectedReason: al.rejectedReason
    }))

    const amountNum = +r.amount || 0

    return {
      /* 사용자 */
      userId   : r.userId,
      userName : r.userName,
      userEmail: r.userEmail,

      /* 영수증 */
      receiptId  : r.receiptId,
      receiptCode: r.receiptCode,
      date       : r.submissionDate,
      type       : r.category?.categoryName ?? t('receipt.meta.category'),
      categoryId : r.category?.categoryId   ?? null,
      limitPrice : r.category?.limitPrice   ?? null,
      reason     : r.reason,
      amount     : `${amountNum.toLocaleString()}원`,
      amountRaw  : amountNum,
      amountPerPerson: perPerson(amountNum, peopleCount),
      peopleCount,
      participants,
      approvers,
      rejectedReason: approvers.find(a => a.rejectedReason)?.rejectedReason || '',

      status    : (r.status || 'REQUEST').toUpperCase(),
      statusText: getReceiptStatusLabel((r.status || 'REQUEST').toUpperCase()),

      receiptName : r.attachment?.fileName || '영수증 미등록',
      receipt     : r.attachment?.fileUrl ? getImagePreviewUrl(r.attachment.fileUrl, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : '',
      approverId  : r.approverId   ?? '',
      approverName: r.approverName ?? '',
      department:   toDeptTeamDisplay(r.department),
      team:         toDeptTeamDisplay(r.team),
    }
  })

  totalPages.value = pageDto.totalPages || 1
}

/* ──────────────── UI handlers ──────────────── */
const router = useRouter()
/**
 * 조회 버튼 클릭 시 실행
 * - 저장된 상태를 초기화하여 새로운 조회 조건으로 검색
 */
function search () {
  currentPage.value = 1
  viewState.clearState('receiptManagement')  // 기존 저장 상태 초기화
  fetchReceipts(1)
}
function onPageChange (p) {
  currentPage.value = p
  fetchReceipts(p)
}
function onUserSelected (u) { 
  searchUserId.value = u.userId 
  selectedUserSearchValue.value = u?.label ?? ''  // "이름 (이메일)" 형식
}
function onRowClick (row)  {
  const id = row.receiptId
  if (isMobile.value) {
    // 상태 저장 후 이동
    viewState.saveState('receiptManagement', {
      startDate              : startDate.value,
      endDate                : endDate.value,
      searchUserId           : searchUserId.value,
      selectedUserSearchValue: selectedUserSearchValue.value,
      currentPage            : currentPage.value,
      scrollY                : window.scrollY,
    })
    try { sessionStorage.setItem('receiptDetail', JSON.stringify({ id })) } catch (e) {}
    router.push(ROUTES.RECEIPT.DETAIL_MOBILE)
    return
  }
  selectedReceiptId.value = id
  detailVisible.value = true
}

/**
 * 테이블 내에서 상태 변경 시 1차 확인 모달을 띄우는 역할
 */
function handleRowUpdated (row) {
  statusUpdateConfirmVisible.value = true;
  // 변경된 상태를 포함한 행 정보를 임시 저장
  rowToUpdateStatus.value = { ...row }; 
}

/**
 * 상태 변경 모달에서 '변경' 클릭 시 실행
 */
async function confirmStatusUpdate() {
  // 모달을 닫고, 임시 저장된 정보를 가져옴
  statusUpdateConfirmVisible.value = false;
  const row = rowToUpdateStatus.value;
  if (!row) return;

  // --- 결재자 권한 검증 (receiptUtils.js의 validateApprovers 사용) ---
  if (row.status === 'REQUEST') {
    const approvers = row.approvers || [];
    const approverIds = approvers.map(a => a.userId);

    // 공통 함수로 검증
    const validation = await validateApprovers(approverIds, UsersApi, getRolesFrom);

    if (!validation.valid) {
      switch (validation.error) {
        case 'NO_APPROVERS':
          toast.error('결재선이 지정되지 않아 신청 상태로 변경할 수 없습니다.');
          break;
        case 'NO_REQUIRED_APPROVER':
          toast.error('한명 이상의 결재자가 지정되지 않아 신청 상태로 변경할 수 없습니다.');
          break;
        case 'DISABLED_USERS':
          const disabledNames = approvers
            .filter(a => validation.unauthorizedUsers.includes(a.userId))
            .map(a => a.name)
            .join(', ');
          toast.error(`결재선에 비활성 사용자가 포함되어 신청 상태로 변경할 수 없습니다: [${disabledNames}]`);
          break;
        case 'UNAUTHORIZED_ROLES':
          const unauthorizedNames = validation.unauthorizedUsers.map(u => u.name).join(', ');
          toast.error(`결재 권한이 없는 사용자가 포함되어 있습니다: [${unauthorizedNames}]\n영수증을 수정해주세요.`);
          break;
      }
      await fetchReceipts(currentPage.value);
      return;
    }
  }

  // 유효성 검사를 통과했거나 '신청' 이외의 상태로 변경하는 경우, API를 호출합니다.
  if (row.status === 'REQUEST') {
    /* HRM 에서 최신 부서‧팀 조회 */
    const user = (await UsersApi.getUserById(row.userId)).data;
    const dept = user.team?.department ?? {};
    const team = user.team ?? {};

    /* payload 구성 (0 → null 변환) */
    const payload = {
      departmentId  : dept.departmentId  || null,
      departmentName: dept.departmentName|| null,
      teamId        : team.teamId        || null,
      teamName      : team.teamName      || null,
    };
    await ReceiptsRequestApi.forceChangeStatus(row.receiptId, row.status, payload);
  } else {
    /* 그 외 상태는 기존 강제변경 API 유지 */
    await ReceiptsRequestApi.forceChangeStatus(row.receiptId, row.status);
  }

  toast.success('상태가 성공적으로 수정되었습니다.');
  await fetchReceipts(currentPage.value);
}

/**
 * 상태 변경 모달에서 '취소' 클릭 시 실행
 */
async function cancelStatusUpdate() {
  statusUpdateConfirmVisible.value = false;
  rowToUpdateStatus.value = null;
  toast.info('상태 변경을 취소하였습니다.');
  // 변경 시도했던 내용을 원상복구하기 위해 목록 새로고침
  await fetchReceipts(currentPage.value);
}

/* 삭제 플로우 */
function deleteReceipt (row) {
  receiptToDelete.value      = row
  deleteConfirmVisible.value = true
}
async function deleteNotice () {
  if (!receiptToDelete.value) return
  await ReceiptsApi.deleteReceipt(receiptToDelete.value.receiptId)
  toast.success('영수증이 삭제되었습니다.')
  deleteConfirmVisible.value = false
  receiptToDelete.value      = null
  
  // 페이지 조정 후 재조회
  await adjustPageAndRefetch({
    currentPageRef: currentPage,
    dataRef: data,
    fetchFunction: fetchReceipts,
  })
}

/* 날짜 보정 */
watch(startDate, v => { if (v > endDate.value) endDate.value = v })
watch(endDate  , v => { if (v < startDate.value) startDate.value = v })

/* 반응형 전환: 모바일 전환 시 모달 닫기, 페이징 리셋 후 재조회 */
watch(isMobile, (v, prev) => {
  if (v) detailVisible.value = false
  if (v !== prev) {
    currentPage.value = 1
    fetchReceipts(1)
  }
})

/* ──────────────── mounted ──────────────── */
onMounted(async () => {
  const saved = viewState.getState('receiptManagement')
  const restore = viewState.canRestore('receiptManagement')
  if (restore && saved) {
    startDate.value              = saved.startDate || today()
    endDate.value                = saved.endDate   || startDate.value
    searchUserId.value           = saved.searchUserId || ''
    selectedUserSearchValue.value = saved.selectedUserSearchValue ?? ''
    currentPage.value            = saved.currentPage || 1
    await fetchReceipts(currentPage.value)
    requestAnimationFrame(() => { window.scrollTo(0, saved.scrollY || 0) })
  } else {
    viewState.clearState('receiptManagement')
    startDate.value = endDate.value = today()
    fetchReceipts(currentPage.value)
  }
  window.addEventListener('resize', updateIsMobile)
  // 상세(모바일 영수증 상세)에서만 복원 허용
  viewState.allowRestoreFrom('receiptManagement', ['ReceiptDetailViewMobile'])
})
onBeforeUnmount(() => { window.removeEventListener('resize', updateIsMobile) })
</script>

<style scoped>
.content {
  transition: margin-right 0.3s ease;
}
.search-controls {
  margin-bottom: 10px;
}

/* 반응형 테이블/카드 (원본 그대로 유지) */
@media (min-width: 851px) {
  #nameSearch {
    font-size: 1rem;
  }
}

@media (max-width: 850px) {
  .btn-primary {
    font-size: 0.75rem;
    padding: 0.3rem 0.6rem;
    margin-bottom: 10px;
  }
}

@media (min-width: 1920px) {
  .navbar-text {
    font-size: 1.5rem; /* 더 큰 텍스트 크기 */
  }
}
</style>