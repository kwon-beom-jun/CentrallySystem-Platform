<template>
  <b-modal
    v-model="innerVisible"
    :title="$t('receipt.common.detail')"
    no-close-on-backdrop
    size="xl"
    scrollable
    centered
    fade
    hide-footer
    custom-class="receipt-modal"
    body-class="p-0"
    @show="resetPane"
  >
    <div class="modal-body d-flex">
      <!-- ① 이미지 영역 -->
      <div
        class="preview-pane"
        :class="{ zoomed: isPaneZoomed }"
        @dblclick="openZoom"
      >
        <img
          v-if="previewUrl"
          :src="previewUrl"
          class="preview-img"
          :class="{ zoomed: isPaneZoomed }"
          :style="zoomStyle"
          draggable="false"
          @mousedown="startDrag"
          @touchstart.passive="startDrag"
        />
        <div v-else class="preview-placeholder">{{ $t('receipt.common.receiptPhoto') }}</div>
      </div>

      <!-- ② 상세 정보 (전체 읽기 전용) -->
      <div class="form-pane">
        <!-- 이미지 등록 -->
        <div class="form-group">
          <DefaultLabel
            :text="$t('receipt.common.receiptPhoto')"
            forId="receiptImage"
            size="small"
            :required="true"
            marginBottom="5px"
          />
          <!-- 기존 파일 표시 (새 파일을 아직 선택하지 않았을 때만) -->
          <div v-if="originalFile.name">
            <ul class="list-group">
              <li class="list-group-item">
                <a
                  :href="originalFilePreviewUrl"
                  @click.prevent="openPreviewModal(originalFilePreviewUrl)"
                  >{{ $t('receipt.submission.existingFilePrefix') }} {{ originalFile.name }}</a
                >
              </li>
            </ul>
          </div>
        </div>

        <!-- 영수증 발행일 -->
        <div class="form-group">
          <DefaultFormRow marginBottom="5px">
            <DefaultLabel
              :text="$t('receipt.common.issueDate')"
              forId="date"
              size="small"
              :required="true"
            />
          </DefaultFormRow>
          <DefaultTextfield
            type="date"
            id="date"
            v-model="formData.date"
            size="full"
            :required="true"
            :disabled="true"
          />
        </div>

        <!-- 구분 | 금액 -->
        <!-- 카테고리 | 금액 (한도 적용) -->
        <div class="form-row align-items-center">
          <div class="col">
            <DefaultLabel
              class="category-label"
              :text="$t('receipt.submission.category')"
              forId="categorySelect"
              size="small"
              marginBottom="5px"
              :required="true"
            />
            <DefaultSelect
              v-model="formData.categoryId"
              :options="categoryOptions"
              :placeholder="$t('receipt.meta.category') + ' ' + $t('common.label.select')"
              :disabled="true"
              size="full"
              style="width: 100%"
              marginBottom="10px"
            />
          </div>
          <div class="col">
            <DefaultLabel
              :text="$t('receipt.common.amount')"
              forId="amount"
              size="small"
              marginBottom="5px"
              :required="true"
            />
            <DefaultTextfield
              type="text"
              id="amount"
              v-model="formData.amount"
              size="full"
              style="width: 100%"
              validationType="number"
              :disabled="true"
              marginBottom="10px"
            />
          </div>
        </div>

        <!-- 사유 -->
        <div class="mb-3">
          <DefaultLabel :text="$t('receipt.common.reason')" size="small" marginBottom="5px" />
          <DefaultTextfield
            type="text"
            v-model="formData.reason"
            size="full"
            :disabled="true"
            :required="true"
          />
        </div>

        <!-- 참여자 목록 -->
        <div v-if="formData.participants.length">
          <DefaultLabel :text="$t('receipt.common.participantsList')" size="small" marginBottom="5px" />
          <ul class="list-group">
            <li
              v-for="(p, idx) in formData.participants"
              :key="idx"
              class="list-group-item"
            >
              <div class="name-block" :class="{ 'external-block': (p.type || p.participantType) === 'EXTERNAL' }">
                <strong class="name-strong">
                  <template v-if="(p.type || p.participantType) === 'EXTERNAL'">
                    [{{ $t('receipt.common.external') }}] {{ p.name }}
                  </template>
                  <template v-else>
                    {{ p.name }}
                  </template>
                </strong>
                <span
                  v-if="(p.type || p.participantType) === 'EXTERNAL'"
                  class="dept-team"
                >
                  {{ p.phone || '-' }} / {{ p.company || '-' }} / {{ p.position || '-' }}
                </span>
                <span
                  v-else-if="formatDeptTeam(p.department, p.team) !== '-'"
                  class="dept-team"
                >
                  {{ formatDeptTeam(p.department, p.team) }}
                </span>
              </div>
            </li>
          </ul>
        </div>

        <div v-if="rejectReason" class="mt-3">
          <DefaultLabel :text="$t('receipt.common.rejectReason')" size="small" />
          <DefaultTextfield
            type="text"
            v-model="rejectReason"
            size="full"
            :disabled="true"
            bg-color="#ffe6e6"
          />
        </div>

        <!-- 결재자 목록 -->
        <div v-if="formData.approvers.length" class="mt-3">
          <DefaultLabel :text="$t('receipt.common.approversList')" size="small" marginBottom="5px" />
          <ul class="list-group">
            <li
              v-for="a in formData.approvers"
              :key="a.userId"
              :class="[
                'list-group-item d-flex align-items-center flex-wrap',
                { 'done-approver': a.stateText !== RECEIPT_STATUS_LABELS.WAITING }
              ]"
            >
              <!-- ── 왼쪽 영역 ───────────────────────────── -->
              <div class="d-flex align-items-center flex-wrap">

                <IconBadge
                  class="fixed-badge"
                  v-if="a.isDefault"
                  color="primary"
                >{{ $t('receipt.common.fixed') }}</IconBadge>

                <span
                  :class="[
                    'approval-option',
                    'approval-decision',
                    { active: a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER], disabled: true }
                  ]"
                >{{ $t('receipt.submission.approverToggle.approveShort') }}</span>
                <span
                  :class="[
                    'approval-option',
                    'approval-concurrence',
                    'approval-option-right',
                    { active: a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE], disabled: true }
                  ]"
                >{{ $t('receipt.submission.approverToggle.concurShort') }}</span>
                <div class="name-block approver-block">
                  <strong class="name-strong">{{ a.name }}</strong>
                  <span
                    v-if="formatDeptTeam(a.department, a.team) !== '-'"
                    class="dept-team"
                  >
                    {{ formatDeptTeam(a.department, a.team) }}
                  </span>
                </div>
              </div>

              <!-- ── 상태 표시(오른쪽) ────────────────────── -->
              <span
                class="state-badge ms-auto text-end"
                :class="{
                  'text-success': a.stateText === RECEIPT_STATUS_LABELS.APPROVED,
                  'text-danger': a.stateText === RECEIPT_STATUS_LABELS.REJECTED,
                  'text-secondary': a.stateText === RECEIPT_STATUS_LABELS.WAITING,
                }"
              >
                {{ a.stateText }}
              </span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </b-modal>
  
  <ImagePreviewModal ref="imagePreviewRef" />
</template>

<script setup>
import { ref, computed, watch, defineProps, defineEmits } from "vue";
import { useI18n } from 'vue-i18n';
import { BModal } from "bootstrap-vue-3";

import DefaultLabel from "@/components/common/label/DefaultLabel.vue";
import DefaultTextfield from "@/components/common/textfield/DefaultTextfield.vue";
import DefaultSelect from "@/components/common/select/DefaultSelect.vue";
import DefaultFormRow from '@/components/common/DefaultFormRow.vue'
import IconBadge from '@/components/common/badge/IconBadge.vue';

import CategoryApi from "@/api/receipt/ReceiptsCategoryApi.js";
import ReceiptsApi from "@/api/receipt/ReceiptsApi.js";
import ImagePreviewModal from '@/components/common/image/ImagePreviewModal.vue';
import { toDeptTeamDisplay } from '@/utils/blankFormat.js'
import { RECEIPT_STATUS_LABELS, APPROVAL_ROLE_LABELS, APPROVAL_ROLE, getApprovalRoleLabel } from '@/constants/receiptConstants';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME } from '@/constants';

/**
 * 조직 텍스트를 상세 모달 표시용으로 정제한다.
 * @param {string} value
 * @returns {string}
 */
function sanitizeOrgText(value) {
  const text = (value ?? '').toString().trim();
  if (!text || text === '-' || text === '--') return '';
  return text;
}

/**
 * 부서와 팀 텍스트를 하나의 라인으로 축약한다.
 * @param {string} department
 * @param {string} team
 * @returns {string}
 */
function formatDeptTeam(department, team) {
  const parts = [
    sanitizeOrgText(toDeptTeamDisplay(department)),
    sanitizeOrgText(toDeptTeamDisplay(team)),
  ].filter(Boolean);
  return parts.length ? parts.join(' · ') : '-';
}

/* ────── props / emit ────── */
const props = defineProps({
  /**
   * 모달 표시 여부
   */
  isVisible: Boolean,
  /**
   * 기존: 부모에서 가공된 객체 전체 전달
   * 변경: id(또는 code)를 넘기면, 모달 내부에서 단건 조회하여 뿌림
   * 호환을 위해 기존 receipt도 허용
   */
  receipt: Object,
  /**
   * 단건 조회용 id. 지정되면 이 값을 우선 사용
   */
  receiptId: [Number, String],
});
const emit = defineEmits(["close", "updated"]);
const { t } = useI18n();

/* ────── 모달 on/off ────── */
const innerVisible = ref(false);
watch(
  () => props.isVisible,
  async (v) => {
    if (!v) {
      innerVisible.value = false;
      return;
    }
    // 지연 오픈: 데이터 로드 후 모달 표시
    await openModalFlow();
  }
);
watch(innerVisible, (v) => {
  if (!v) emit("close");
});

/* ────── 미리보기 모달(공통 컴포넌트) ────── */
const imagePreviewRef = ref(null);
function openPreviewModal(url){ if(!url) return; imagePreviewRef?.value?.open(url); }

/**
 * 기존 파일 URL을 게이트웨이를 거쳐 변환
 */
const originalFilePreviewUrl = computed(() => {
  return originalFile.value.url ? getImagePreviewUrl(originalFile.value.url, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : '';
});

/* ────── 좌측 패널 상태 초기화용 함수 ────── */
const isPaneZoomed = ref(false);
const pos = ref({ x:0, y:0 });
function resetPane() {            // ← 새로 추가
  isPaneZoomed.value = false;
  pos.value = { x:0, y:0 };
}

// 전체 상태 초기화 (초진입 시 잔여 데이터 제거)
function resetAll() {
  formData.value = {
    date: "",
    categoryId: null,
    amount: "",
    reason: "",
    participants: [],
    approvers: [],
  };
  originalFile.value = { name: "", url: "" };
  previewUrl.value = null;
  rejectReason.value = "";
  resetPane();
}

async function openModalFlow() {
  resetAll();
  if (props.receiptId) {
    const { data } = await ReceiptsApi.getReceiptById(props.receiptId);
    populateFromRaw(data);
    innerVisible.value = true;
    return;
  }
  if (props.receipt) {
    // 하위 호환: 기존 가공 객체를 그대로 반영 후 오픈
    const r = props.receipt;
    formData.value = {
      date: r.date,
      categoryId: r.categoryId,
      amount: r.amount,
      reason: r.reason,
      participants: (r.participants ?? []).map(p=>({
        ...p,
        department: toDeptTeamDisplay(p.department),
        team      : toDeptTeamDisplay(p.team)
      })),
      approvers: (r.approvers ?? []).map(a => ({
        ...a,
        department: toDeptTeamDisplay(a.department),
        team      : toDeptTeamDisplay(a.team),
        isDefault: a.isDefault ?? a.approvalRole === 3
      })),
    };
    rejectReason.value = (r.approvers ?? []).find(a => a.rejectedReason)?.rejectedReason || '';
    originalFile.value = { name: r.receiptName ?? '', url: r.receipt ?? '' };
    previewUrl.value = originalFile.value.url ? getImagePreviewUrl(originalFile.value.url, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : null;
    resetPane();
    innerVisible.value = true;
  }
}

/* ────── 이미지 확대/드래그 (좌측 pane) ────── */
let start = { x: 0, y: 0 };
let dragging = false;

const zoomStyle = computed(() =>
  isPaneZoomed.value
    ? {
        transform: `translate(${pos.value.x}px, ${pos.value.y}px) scale(1.5)`,
        cursor: dragging ? "grabbing" : "zoom-out",
      }
    : { cursor: "zoom-in" }
);

function openZoom() {
  if (!previewUrl.value) return;
  isPaneZoomed.value = !isPaneZoomed.value;
  if (!isPaneZoomed.value) pos.value = { x: 0, y: 0 };
}
function startDrag(e) {
  if (!isPaneZoomed.value) return;
  dragging = true;
  const p = e.touches?.[0] ?? e;
  start = { x: p.clientX - pos.value.x, y: p.clientY - pos.value.y };
  window.addEventListener("mousemove", onMove);
  window.addEventListener("mouseup", endDrag);
  window.addEventListener("touchmove", onMove, { passive: false });
  window.addEventListener("touchend", endDrag);
}
function onMove(e) {
  if (!dragging) return;
  const p = e.touches?.[0] ?? e;
  pos.value = { x: p.clientX - start.x, y: p.clientY - start.y };
  e.preventDefault();
}
function endDrag() {
  dragging = false;
  window.removeEventListener("mousemove", onMove);
  window.removeEventListener("mouseup", endDrag);
  window.removeEventListener("touchmove", onMove);
  window.removeEventListener("touchend", endDrag);
}

/* ────── 카테고리 옵션 (읽기 전용이라도 표시용으로 필요) ────── */
const rejectReason = ref(""); // 반려사유·좌측 초기화용 상태
const categories = ref([]);
const categoryOptions = computed(() =>
  categories.value.map((c) => ({ value: c.categoryId, label: c.categoryName }))
);
async function loadCategories() {
  if (categories.value.length) return;
  const res = await CategoryApi.getCategoriesWithDisabled();
  categories.value = res.data;
}
loadCategories();

/* ────── 표시용 데이터 ────── */
const formData = ref({
  date: "",
  categoryId: null,
  amount: "",
  reason: "",
  participants: [],
  approvers: [],
});
const originalFile = ref({ name: "", url: "" });
const previewUrl = ref(null);

/**
 * 단건 조회 결과를 모달 formData에 매핑
 */
function populateFromRaw(receiptRaw) {
  if (!receiptRaw) return;
  const r = receiptRaw;
  const peopleArr = (r.participantsList ?? []).map(p => ({
    name: p.participantName,
    department: toDeptTeamDisplay(p.department),
    team: toDeptTeamDisplay(p.team),
    participantType: p.participantType || p.type || null,
    company: p.company || null,
    position: p.position || null,
    phone: p.phone || null,
  }));
  const approverArr = (r.approvalLines ?? []).map(al => ({
    userId:         al.approverUserId,
    name:           al.delegateMapping ? ('[대리] ' + (al.delegateMapping.delegateName || al.approverName)) : al.approverName,
    department:     toDeptTeamDisplay(al.delegateMapping ? al.delegateMapping.delegateDepartment : al.department),
    team:           toDeptTeamDisplay(al.delegateMapping ? al.delegateMapping.delegateTeam       : al.team),
    approvalRole:   al.approvalRole,
    approvalStatus: al.approvalStatus,
    approvalType:   getApprovalRoleLabel(al.approvalRole),
    stateText:      al.rejectedAt ? RECEIPT_STATUS_LABELS.REJECTED : (al.approvalStatus ? RECEIPT_STATUS_LABELS.APPROVED : RECEIPT_STATUS_LABELS.WAITING),
    rejectedReason: al.rejectedReason,
  }));

  const amountVal = r.amount ? parseInt(r.amount, 10) : 0;
  formData.value = {
    date: r.submissionDate,
    categoryId: r.category?.categoryId ?? null,
    amount: amountVal.toLocaleString() + '원',
    reason: r.reason,
    participants: peopleArr,
    approvers: approverArr,
  };
  rejectReason.value = approverArr.find(a => a.rejectedReason)?.rejectedReason || '';
  const attachment = r.attachment;
  originalFile.value = { name: attachment?.fileName ?? '', url: attachment?.fileUrl ?? '' };
  previewUrl.value = originalFile.value.url ? getImagePreviewUrl(originalFile.value.url, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : null;
  resetPane();
}
</script>

<style scoped>
hr {
  margin: 0 0 7px;
  border: none; /* ← 기본 border 제거 */
  height: 2px; /* 실제 두께 */
  background: #000000;
}
.search-wrapper-hr {
  margin: 15px 0 10px 0;
}
.form-row .col {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.modal-body {
  padding: 10px 0px 10px 20px !important;
  overflow-y: none;
}

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

.name-block {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 6px;
  font-size: 0.7rem;
}
.name-strong {
  font-weight: 800;
}
.dept-team {
  font-weight: 600;
  font-size: 0.65rem;
  color: #4b5563;
}
.external-block .dept-team {
  font-weight: 500;
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
  font-weight: 900 !important;
}

.approval-option + .approval-option {
  margin-left: -1px; /* 경계선 겹침 처리 */
}

.approval-option-right {
  margin-right: 7px;
}
.fixed-badge {
  margin-right: 5px;
}
/* 결재 (active) : 빨강 */
.approval-decision.active {
  color: #ff0019 !important;
  background: #dc35466c !important;
}

/* 합의 (active) : 초록 */
.approval-concurrence.active {
  color: #00914d !important;
  background: #19875469 !important;
}

.approval-option.disabled {
  pointer-events: none; /* 클릭 차단 */
  opacity: 0.4; /* 흐리게 표시 */
}
/* 컨테이너는 이미 .modal-body d-flex 가 있음 -------------- */
.form-pane {
  flex: 0 0 60%;
  max-height: 80vh; /* preview-pane 과 동일한 세로 제한 */
  padding-right: 80px;
	padding-left: 40px;
  padding-top: 20px;
  margin: 0 auto;
  margin-bottom: 10px;
  overflow-y: auto; /* 넘치는 부분만 내부 스크롤 */
}

/* 미리보기 박스 세부 */
.preview-pane {
  margin-top: 25px;
  margin-bottom: 25px;
  flex: 0 0 40%; /* 4  */
  max-width: 40%;
  display: flex;
  align-items: center;
  justify-content: center;
  max-height: 75vh; /* 모달 높이에 맞춰서 */
  overflow: hidden; /* 너무 큰 이미지는 잘라냄 */
  /* border-right: 1px solid #dee2e6; */
}
.preview-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain; /* 비율 유지하며 내부에 맞춤 */
  border: 2px solid #dee2e6;
  border-radius: 4px;
  user-select: none; /* 글자 선택 방지 */
  -webkit-user-drag: none; /* Safari, Chrome */
}
.preview-placeholder {
  font-size: 0.8rem;
  color: #777;
  text-align: center;
}
/* 기본(축소) */
.preview-pane {
  flex: 0 0 40%;
  max-width: 40%;
  display: flex;
  align-items: center;
  justify-content: center;
  max-height: 75vh;
  overflow: hidden;
  cursor: zoom-in; /* 확대 가능하다는 힌트 */
}
.preview-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  transition: transform 0.2s ease;
}

/* 확대 상태 */
.preview-pane.zoomed {
  cursor: zoom-out;
  /* overflow: auto; */ /* 스크롤 가능 */
  overflow: hidden;
}

/* ── 미리보기 모달 ─────────────────────── */
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

/* scoped style 맨 아래에 추가 */
.state-badge {
  font-weight: 900;
  min-width: 48px; /* 넓이 통일 */
  text-align: right;
}

/* 승인·반려 완료된 결재자 행 */
.done-approver {
  background: #f1f1f1 !important;   /* 원하는 회색 계열로 조정 */
}
@media (max-width: 991px) {
  .modal-body {
    padding: 20px 25px 30px 25px !important;
  }
  .preview-pane {
    /* 왼쪽 이미지 영역 숨김 */
    display: none;
  }
  .v-divider {
    /* 가운데 세로선 제거 */
    display: none;
  }
  .form-pane {
    /* 오른쪽(폼) 영역을 100%로 확장 */
    flex: 1 1 100%;
    max-width: 100%;
    padding: 0px;
  }
}

@media (max-width: 650px) {
  .modal-body {
    flex-direction: column;
    max-height: 70vh;
    overflow-y: auto;
  }
  .preview-pane,
  .form-pane {
    max-width: 100%;
    flex: 0 0 auto;
  }
  .form-pane {
    margin: 0;
  }
  .v-divider {
    display: none;
  }
  .preview-pane {
    height: 240px;
    margin-bottom: 1rem;
  }
  hr {
    margin: 0 0 5px 0;
  }

  .search-wrapper-hr {
    margin: 10px 0 5px 0;
  }

  .list-group-item,
  .list-group {
    font-size: 0.8em !important;
  }

  .form-group {
    margin-bottom: 10px;
  }
}

/* 모달 내부 스크롤 상한 및 정책 */
.receipt-modal .modal-content > .modal-body { max-height: 80vh; }

/* Desktop(>=992px): 우측 form-pane 스크롤, 모달 본문 스크롤 숨김 */
@media (min-width: 992px) {
  .receipt-modal .modal-content > .modal-body { overflow-y: hidden; }
  .form-pane { overflow-y: auto; max-height: 80vh; }
}

/* Tablet/Mobile(<=991px): 왼쪽 패널 사라지면 모달 본문 스크롤 */
@media (max-width: 991px) {
  .receipt-modal .modal-content > .modal-body { overflow-y: scroll; }
  .preview-pane { display: none; }
  .form-pane { flex: 1 1 100%; max-width: 100%; overflow: visible; max-height: none; padding: 0; }
}
</style>
