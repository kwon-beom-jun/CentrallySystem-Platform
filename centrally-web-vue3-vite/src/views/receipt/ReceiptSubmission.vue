<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper" :class="{ 'content-expanded': !isMobile }">
      <PageTitle 
        :title="$t('receipt.submission.title')"
        :subtitle="$t('receipt.submission.subtitle')"
        icon="ri-file-add-line"
      />

      <DefaultFormRow gap="10px" marginBottom="10px" align="between">
        <!-- 필터 -->
        <DefaultSelect
          id="serviceSearch"
          v-model="selectedService"
          :options="serviceOptions"
          @change="onStatusChange"
          size="small"
        />
        <!-- <select
          v-model="filterStatus"
          class="form-select form-select-sm"
          style="width:110px; height: 38px;"
        >
          <option value="">전체</option>
          <option :value="RECEIPT_STATUS.WAITING">{{ RECEIPT_STATUS_LABELS[RECEIPT_STATUS.WAITING] }}</option>
          <option :value="RECEIPT_STATUS.REJECTED">{{ RECEIPT_STATUS_LABELS[RECEIPT_STATUS.REJECTED] }}</option>
        </select> -->
        <DefaultButton 
          align="right" 
          style="" 
          @click="openModal"
        >
          {{ $t('receipt.submission.register') }}
        </DefaultButton>
      </DefaultFormRow>

      <!-- 테이블 보기 -->
      <DefaultTable
        class="receipt-table"
        :columns="columns"
        :data="data"
        :rowClick="openEditModal"
        :mobileCard="true"
        :buttonHeight="'30px'"
        @apply-row="handleApply"
        @delete-row="handleDelete"
        :minRows="8"
        :usePagination="true"
        :currentPage="currentPage"
        :totalPages="totalPages"
        :visiblePageCount="visiblePageCount"
        @pageChange="onPageChange"
        :noDataImageHeight="484"
      />
    </div>
  </div>
  
  <!-- 이미지 미리보기 -->
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

  <!-- 신청 확인 모달 -->
  <AlertModal
    :isVisible="applyModalVisible"
    :title="$t('receipt.submission.applyConfirm')"
    :confirmText="$t('receipt.submission.apply')"
    :cancelText="$t('common.button.cancel')"
    @close="applyModalVisible = false"
    @confirm="confirmApply"
  >
    <template #body>
      {{ $t('receipt.submission.applyMessage') }}
    </template>
  </AlertModal>

  <!-- 삭제 확인 모달 -->
  <AlertModal
    :isVisible="deleteModalVisible"
    :confirmText="$t('common.button.delete')"
    :cancelText="$t('common.button.cancel')"
    @close="deleteModalVisible = false"
    @confirm="confirmDelete"
  >
    <template #body>
        <div style="color: red;">
          {{ $t('receipt.management.deleteWarning') }} <br/><br/>
          {{ $t('receipt.submission.deleteMessage') }} <br/>
          {{ $t('receipt.submission.deleteProceed') }} <br/>
        </div>
    </template>
  </AlertModal>

  <!-- 영수증 등록 모달 (modalVisible 가 true 일 때만 마운트) -->
  <ReceiptSubmissionCreateModal
    v-if="!isMobile"
    :isVisible="modalVisible"
    :currentDeptId   ="currentDeptId"
    :currentDeptName ="currentDeptName"
    :currentTeamId   ="currentTeamId"
    :currentTeamName ="currentTeamName"
    @close="modalVisible = false"
    @confirm="() => receiptConfirm()"
  />

  <!-- 영수증 수정 모달 (editModalVisible 가 true 일 때만 마운트) -->
  <ReceiptSubmissionEditModal
    :isVisible="editModalVisible"
    :currentDeptName ="currentDeptName"
    :receiptId="editingReceiptId"
    @close="editModalVisible = false"
    @request-apply="handleApply"
    @confirm="() => fetchMetadata(currentPage)"
  />
</template>

<script setup>
/* ───────────────────────── IMPORTS ───────────────────────── */
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ROUTES } from '@/config/menuConfig'
import { toast }                    from 'vue3-toastify'
import { RECEIPT_STATUS, RECEIPT_STATUS_LABELS, getReceiptStatusLabel, getApprovalRoleLabel } from '@/constants/receiptConstants'

import DefaultTable                 from '@/components/common/table/DefaultTable.vue'
import DefaultButton                from '@/components/common/button/DefaultButton.vue'
import DefaultFormRow               from '@/components/common/DefaultFormRow.vue'
import DefaultSelect                from "@/components/common/select/DefaultSelect.vue";
import AlertModal                   from '@/components/common/modal/AlertModal.vue'
import ReceiptSubmissionCreateModal from '@/components/receipt/ReceiptSubmissionCreateModal.vue'
import ReceiptSubmissionEditModal   from '@/components/receipt/ReceiptSubmissionEditModal.vue'

import UsersApi             from '@/api/hrm/UsersApi'
import HrmUserApi           from '@/api/hrm/UsersApi';
import ReceiptsApi          from '@/api/receipt/ReceiptsApi'
import ReceiptsRequestApi   from '@/api/receipt/ReceiptsRequestApi'
import { useAuthStore }     from '@/store/auth'
import { useToastStore }    from '@/store/toast'
import { usePreviewModal }  from '@/utils/preview-modal'
import { useViewStateStore } from '@/store/viewState'
import { getRolesFrom } from '@/utils/roleUtils';
import { ROLE_GROUPS } from '@/config/roleConfig';

/* ───────────────────────── CONSTANTS ─────────────────────── */
const MOBILE_BP         = 650
const MOBILE_PAGE_SIZE  = 4
const DESKTOP_PAGE_SIZE = 8

/* ───────────────────────── 사용자 부서(팀) ─────────────────────── */
const currentDeptId = ref('')
const currentDeptName = ref('')
const currentTeamId = ref('')
const currentTeamName = ref('')

/* ───────────────────────── RESPONSIVE ────────────────────── */
const isMobile = ref(window.innerWidth <= MOBILE_BP)
const pageSize = computed(() => isMobile.value ? MOBILE_PAGE_SIZE : DESKTOP_PAGE_SIZE)
function updateIsMobile () { isMobile.value = window.innerWidth <= MOBILE_BP }
window.addEventListener('resize', updateIsMobile)
// 모바일로 전환되는 순간 부모에서 모달을 닫는다
watch(isMobile, (v, prev) => {
  if (v) modalVisible.value = false
  if (v) editModalVisible.value = false
  // 화면 모드가 실제로 변한 경우: 페이지/상태 초기화 및 저장 상태 제거
  if (v !== prev) {
    currentPage.value = 1
    fetchMetadata(1)
  }
})

/* ───────────────────────── STORES ────────────────────────── */
const { t } = useI18n()
const authStore = useAuthStore()
const toastStore = useToastStore()
const router    = useRouter()
const viewState = useViewStateStore()

/* ───────────────────────── DATA / TABLE ──────────────────── */
const data            = ref([])
const currentPage     = ref(1)
const totalPages      = ref(1)
const visiblePageCount= ref(5)
const serviceOptions = computed(() => [
  { value: '',          label: t('common.label.all')  },
  { value: RECEIPT_STATUS.WAITING,   label: RECEIPT_STATUS_LABELS[RECEIPT_STATUS.WAITING]  },
  { value: RECEIPT_STATUS.REJECTED,  label: RECEIPT_STATUS_LABELS[RECEIPT_STATUS.REJECTED] }
])
const selectedService = ref('')        // v-model 값
const filterStatus    = ref('')        // 실제 API 파라미터

/* ─────── columns: 템플릿에서 그대로 쓰므로 변경 ❌ ─────── */
const columns = computed(() => [
  {
    key: "receiptCode",
    label: t('receipt.common.receiptCode'),
    width: 85,
    align: "center",
    mobile: {
      line: 1,
      inline: true,
      prefix: `📝\u00a0${t('receipt.common.postNumber')}\u00a0`,
      suffix: "",
      bold: true,
    },
  },

  {
    key: "date",
    label: t('receipt.common.issueDate'),
    width: 110,
    align: "center",
    mobile: { line: 1, inline: true, prefix: "", suffix: "", bold: false, align: "right" },
  },

  {
    key: "type",
    label: t('receipt.common.type'),
    width: 150,
    align: "center",
    mobile: { dividerTop:true, dividerTopGapAbove:'5px', dividerTopGapBelow:'15px', line: 2, inline: false, prefix: `${t('receipt.common.typeLabel')}\u00a0`, suffix: "", bold: false },
  },

  {
    key: "peopleCount",
    label: t('receipt.common.totalPeople'),
    width: 60,
    align: "center",
    mobile: { hidden: true, line: 3, inline: false, prefix: `${t('receipt.common.peopleCount')}\u00a0`, suffix: "", bold: false },
  },

  {
    key: "reason",
    label: t('receipt.common.reason'),
    width: 'auto',
    minWidth: 100,
    align: "left",
    mobile: { line: 4, inline: false, prefix: `${t('receipt.common.reasonLabel')}\u00a0`, suffix: "", bold: false },
  },

  {
    key: "amount",
    label: t('receipt.common.amount'),
    width: 80,
    align: "right",
    mobile: { line: 5, inline: false, prefix: `${t('receipt.common.amountLabel')}\u00a0`, suffix: "", bold: false },
  },

  {
    key: "amountPerPerson",
    label: t('receipt.common.amountPerPerson'),
    width: 80,
    align: "right",
    mobile: { hidden: true, line: 6, inline: false, prefix: `${t('receipt.common.amountPerPersonLabel')}\u00a0`, suffix: "", bold: false },
  },

  {
    key: "statusText",
    label: t('receipt.common.paymentStatus'),
    width: 75,
    align: "center",
    customClass: (v) => (v === t('receipt.approvalRequest.rejected') ? "text-red" : ""),
    mobile: { dividerBottom:true, line: 7, inline: false, prefix: `${t('receipt.common.paymentStatusLabel')}\u00a0`, suffix: "", bold: false },
  },

  {
    key: "apply",
    type: "button",
    label: "",
    width: 80,
    buttonText: t('receipt.submission.apply'),
    buttonColor: "blue",
    buttonSize: "full-small",
    emit: "apply-row",
    mobile: { line: 8, inline: true, prefix: "", suffix: "\u00a0", bold: true, align: "right" },
  },
  {
    key: "delete",
    type: "button",
    label: "",
    width: 80,
    buttonText: t('common.button.delete'),
    buttonColor: "red",
    buttonSize: "full-small",
    emit: "delete-row",
    mobile: { line: 8, inline: true, prefix: "", suffix: "", bold: true },
  },
]);

/* ───────────────────────── PREVIEW MODAL ─────────────────── */
const { isPreviewVisible, previewImage,
        isZoomed, zoomedPosition, zoomOrigin,
        openPreviewModal, toggleZoom,
        startDrag, onDrag, endDrag } = usePreviewModal()

function closePreviewModalOnOutsideClick (e) {
  if (!e.target.classList.contains('preview-modal-image'))
    isPreviewVisible.value = false
}

/* ───────────────────────── SUPPORT FNS ──────────────────── */
const calculateAmountPerPerson = item => {
  const total = parseInt((item.amount||'0').replace(/[^0-9]/g,''),10)||0
  return item.peopleCount
    ? Math.floor(total/item.peopleCount).toLocaleString()
    : '0'
}
const statusColor = txt =>
  txt===RECEIPT_STATUS_LABELS.APPROVED ? 'text-success'
: txt===RECEIPT_STATUS_LABELS.REJECTED  ? 'text-danger'
: txt===RECEIPT_STATUS_LABELS.WAITING  ? 'text-primary'
:                 'text-secondary'

/* ───────────────────────── MODAL FLAGS ───────────────────── */
const modalVisible       = ref(false)
const editModalVisible   = ref(false)
const editingReceiptId   = ref(null)

const applyModalVisible  = ref(false)
const deleteModalVisible = ref(false)
const applyTarget        = ref(null)
const deleteTarget       = ref(null)

/* ───────────────────────── FETCH LIST ────────────────────── */
async function fetchMetadata (page=1){
  const { data: pageDto } =
    await ReceiptsApi.searchUserReceipts(authStore.getUserId,{
      statusCodes:
        filterStatus.value==='WAITING' ? [4] :
        filterStatus.value==='REJECTED'? [3] : [3,4],
      page:page-1,
      size:pageSize.value
    })

  data.value = (pageDto.content||[]).map(mapRow)
  totalPages.value = pageDto.totalPages || 1
  if(isMobile.value) window.scrollTo(0,0)
}

function mapRow(r){
  const people     = (r.participantsList||[]).map(p=>({
    userId           : p.participantUserId ?? null,
    name             : p.participantName,
    department       : p.department,
    team             : p.team,
    participantType  : p.participantType || p.type || null,
    company          : p.company || null,
    position         : p.position || null,
    phone            : p.phone || null
  }))
  const amountRaw  = +r.amount || 0
  const approvers  = (r.approvalLines||[]).map(al=>({
    userId:al.approverUserId,
    name:al.approverName,
    email: al.approverEmail ?? '',
    department:al.department,
    team:al.team,
    approvalRole:al.approvalRole,
    approvalType:getApprovalRoleLabel(al.approvalRole),
    approvalStatus:al.approvalStatus,
    rejected:!!al.rejectedAt,
    stateText: al.rejectedAt ? RECEIPT_STATUS_LABELS.REJECTED : (al.approvalStatus ? RECEIPT_STATUS_LABELS.APPROVED : RECEIPT_STATUS_LABELS.WAITING),
    rejectedReason:al.rejectedReason??''
  }))
  const rejectedReason = approvers.find(a=>a.rejectedReason)?.rejectedReason || ''
  // category가 없거나(null), 있더라도 enabled가 false이면 비활성으로 처리
  const isCategoryEnabled = r.category?.enabled ?? false;
  return {
    /* table/card 필드 */
    receiptId:r.receiptId,
    receiptCode:r.receiptCode,
    date:r.submissionDate,
    type:r.category?.categoryName ?? '',
    categoryId:r.category?.categoryId ?? null,
    reason:r.reason,
    amount:amountRaw.toLocaleString(),
    amountRaw,
    receiptName:r.attachment?.fileName || t('receipt.common.receiptPhoto'),
    receipt:r.attachment?.fileUrl || '',
    people,
    peopleCount:people.length+1,
    amountPerPerson: Math.floor(amountRaw/(people.length+1)).toLocaleString(),
    approvers,
    status:r.status,
    statusText: getReceiptStatusLabel(r.status),
    rejectedReason,
    isCategoryEnabled
  }
}

/* ───────────────────────── 사용자 부서(팀) ───────────────────── */
async function getUserDepartment () {
  const { data: me } = await HrmUserApi.getUserById(authStore.getUserId)

  const deptName = me.team?.department?.departmentName ?? ''
  currentDeptId.value = me.team?.department?.departmentId
  currentDeptName.value = deptName.trim().toUpperCase()
  
  const teamName = me.team?.teamName ?? ''
  currentTeamId.value = me.team?.teamId
  currentTeamName.value = teamName.trim().toUpperCase()
}

/* ───────────────────────── PAGINATION ───────────────────── */
function onPageChange(p){ currentPage.value=p; fetchMetadata(p) }


/* ───────────────────────── 필터 ───────────────────── */
function onStatusChange(val){
  filterStatus.value = val
  currentPage.value  = 1
  fetchMetadata(1)
}

/* ───────────────────────── APPLY / DELETE ───────────────── */

/**
 * 신청 버튼 클릭 시, 결재선에 비활성 사용자가 있는지 확인합니다.
 */
async function handleApply(row) {
  // 결재선 존재 여부 확인
  const approvers = row.approvers || [];
  if (approvers.length === 0) {
      toast.error(t('receipt.submission.requiredFields'));
      return;
  }
  const originalIds = approvers.map(a => a.userId);

  // '활성화된' 사용자 정보 및 권한
  const enabledUsers = await UsersApi.getActiveUsersWithRoles(originalIds);
  const enabledIdSet = new Set(enabledUsers.map(u => u.userId));
  const disabledIds = originalIds.filter(id => !enabledIdSet.has(id));
  // 기존과 다르게 존재하지 않는 사용자가 있으면, 원래 결재선 정보에서 이름을 찾아 에러 메시지를 표시
  if (disabledIds.length > 0) {
      const disabledNames = approvers
          .filter(a => disabledIds.includes(a.userId))
          .map(a => a.name)
          .join(', ');
      toast.error(`${t('receipt.submission.requiredFields')}: [${disabledNames}]\n${t('receipt.submission.edit')}`);
      return; // 신청 절차 중단
  }

  // 결재 권한
  const requiredRoles = [
    ...getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
    "ROLE_GATE_SYSTEM"
  ];
  // 활성 결재자 목록을 순회하며, 각자가 필요한 권한 중 하나라도 가졌는지 확인
  const unauthorizedApprovers = enabledUsers.filter(approver => 
    !approver.roles || !approver.roles.some(userRole => requiredRoles.includes(userRole))
  );
  // 권한 없는 사용자가 있으면 에러 메시지 표시
  if (unauthorizedApprovers.length > 0) {
    const unauthorizedNames = unauthorizedApprovers.map(u => u.name).join(', ');
    toast.error(`${t('receipt.submission.requiredFields')}: [${unauthorizedNames}]\n${t('receipt.submission.edit')}`);
    return;
  }

  // 카테고리 비활성 확인 후, 기존 신청 절차를 계속 진행합니다.
  if (row.isCategoryEnabled) {
      applyTarget.value = row;
      applyModalVisible.value = true;
  } else {
      openEditModal(row);
  }
}

function handleDelete(row){ deleteTarget.value=row; deleteModalVisible.value=true }

async function confirmApply(){
  if(!applyTarget.value) return
  applyModalVisible.value = false;

  // 부서/팀 정보를 담을 payload 객체 생성
  const payload = {
    departmentId: currentDeptId.value,
    departmentName: currentDeptName.value,
    teamId: currentTeamId.value,
    teamName: currentTeamName.value,
  };

  await ReceiptsRequestApi.requestReceipt(applyTarget.value.receiptId, payload )

  toast.success(t('receipt.submission.submitSuccess'))
  fetchMetadata(currentPage.value)

  editModalVisible.value = false 
  applyTarget.value = null
}

async function confirmDelete(){
  if(!deleteTarget.value) return
  deleteModalVisible.value=false;

  await ReceiptsApi.deleteReceipt(deleteTarget.value.receiptId)
  
  toast.success(t('receipt.submission.deleteSuccess'))
  fetchMetadata(currentPage.value)
  deleteTarget.value=null
}

/* ───────────────────────── REG / EDIT ───────────────────── */
function receiptConfirm(){ toast.success(t('receipt.submission.saveSuccess')); fetchMetadata(currentPage.value) }
function openModal(){
  if (isMobile.value) {
    viewState.saveState('receiptSubmission', {
      selectedService: selectedService.value,
      filterStatus: filterStatus.value,
      currentPage: currentPage.value,
      scrollY: window.scrollY,
    })
    router.push(ROUTES.RECEIPT.SUBMISSION_CREATE_MOBILE)
    return
  }
  modalVisible.value=true
}
function openEditModal(row){
  const src = data.value.find(d=>d.receiptId===row.receiptId) || row
  if (isMobile.value) {
    viewState.saveState('receiptSubmission', {
      selectedService: selectedService.value,
      filterStatus: filterStatus.value,
      currentPage: currentPage.value,
      scrollY: window.scrollY,
    })
    // 상세와 동일: URL 노출 없이 세션으로만 전달
    sessionStorage.setItem('receiptEditing', JSON.stringify({ id: src.receiptId }))
    // 다음 페이지 토스트: 카테고리가 비활성인 경우 안내 메시지 표시
    if (!src.isCategoryEnabled) {
      toastStore.setNextPageMessage(t('receipt.meta.deleteWarning'), 'error')
    }
    router.push(ROUTES.RECEIPT.SUBMISSION_EDIT_MOBILE)
    return
  }
  editingReceiptId.value = src.receiptId
  editModalVisible.value = true
}

/* ───────────────────────── LIFECYCLE ────────────────────── */
onMounted(async () => {
  await getUserDepartment()
  const saved = viewState.getState('receiptSubmission')
  const restore = viewState.canRestore('receiptSubmission')
  if (restore && saved) {
    selectedService.value = saved.selectedService ?? ''
    filterStatus.value    = saved.filterStatus ?? ''
    currentPage.value     = saved.currentPage ?? 1
    await fetchMetadata(currentPage.value)
    requestAnimationFrame(() => { window.scrollTo(0, saved.scrollY ?? 0) })
  } else {
    viewState.clearState('receiptSubmission')
    await fetchMetadata()
  }
  // 상세(모바일 등록/수정)에서만 복원 허용
  viewState.allowRestoreFrom('receiptSubmission', ['ReceiptSubmissionCreateMobile','ReceiptSubmissionEditMobile'])
})
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