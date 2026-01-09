<template>
  <div>
    <div class="content content-wrapper">
      <MobileDetailTitle :title="$t('receipt.common.receiptDetail')" />
      <!-- <p class="content-sub-title">모바일 전용 상세 페이지입니다</p> -->

      <div class="modal-body">
        <div class="form-pane">
          <!-- 이미지 영역 (읽기 전용) -->
          <div class="form-group cs-card--blue">
            <DefaultLabel
              :text="$t('receipt.common.receiptPhoto')"
              forId="receiptImage"
              size="small"
              :required="true"
            />
            <div class="thumb-inline">
              <div
                class="thumb"
                role="button"
                @click="openPreviewModal(originalFile.url)"
                :aria-label="originalFile.name"
              >
                <template v-if="originalFile && originalFile.url">
                  <img :src="originalFile.url" :alt="originalFile.name || $t('receipt.common.receiptPhoto')" class="thumb__img" />
                  <div class="thumb__overlay">
                    <i class="ri-search-eye-line thumb__icon"></i>
                  </div>
                </template>
                <template v-else>
                  <div class="thumb__placeholder">
                    <i class="ri-image-line"></i>
                  </div>
                </template>
              </div>
              <div class="meta-col">
                <div class="meta-row">
                  <i class="ri-calendar-2-line meta-icon"></i>
                  <span class="meta-text">{{ formData.date || '-' }}</span>
                </div>
                <div class="meta-chips">
                  <span class="chip chip--mint">{{ categoryText || '-' }}</span>
                  <span class="meta-amount">{{ formData.amount || '0원' }}</span>
                </div>
                <div class="file-name-display" @click.stop>
                  <span class="file-name">{{ originalFile.name || $t('receipt.common.receiptPhoto') }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 사유 (읽기 전용) - 공통 컴포넌트 제거, 커스텀 표시 -->
          <div class="form-group cs-card--blue">
            <div class="reason-head">
              <i class="ri-chat-1-line"></i>
              <span>{{ $t('receipt.common.reason') }}</span>
            </div>
            <div class="reason-display">{{ formData.reason || '-' }}</div>
          </div>

          <!-- 참여자 목록 -->
          <div class="form-group" v-if="formData.participants.length">
            <MobileMintLabel :text="$t('receipt.common.participantsList')" size="small" marginBottom="5px" />
            <ul class="list-group">
              <li
                v-for="(p, idx) in formData.participants"
                :key="idx"
                class="list-group-item"
              >
                <template v-if="(p.type || p.participantType) === 'EXTERNAL'">
                  <div class="name-block external-block">
                    <strong class="name-strong">[{{ $t('receipt.common.external') }}] {{ p.name }}</strong>
                    <span class="dept-team">
                      {{ p.phone || '-' }} /
                      {{ p.company || '-' }} /
                      {{ p.position || '-' }}
                    </span>
                  </div>
                </template>
                <template v-else>
                  <div class="name-block">
                    <strong class="name-strong">{{ p.name }}</strong>
                    <span
                      v-if="formatDeptTeam(p.department, p.team) !== '-'"
                      class="dept-team"
                    >
                      {{ formatDeptTeam(p.department, p.team) }}
                    </span>
                  </div>
                </template>
              </li>
            </ul>
          </div>

          <!-- 반려 사유 (컨테이너) -->
          <div v-if="rejectReason" class="form-group cs-card--blue cs-card--white">
            <MobileMintLabel :text="$t('receipt.common.rejectReason')" size="small" />
            <MobileMintTextfield type="text" v-model="rejectReason" size="full" :disabled="true" bg-color="#fff0f0" />
          </div>

          <!-- 결재자 목록 (상태 표시) -->
          <div v-if="normalApprovers.length" class="form-group cs-card--blue">
            <MobileMintLabel :text="$t('receipt.common.approversList')" size="small" marginBottom="5px" />
            <ul class="list-group">
              <li
                v-for="a in normalApprovers"
                :key="a.userId"
                :class="[
                  'list-group-item d-flex align-items-center flex-wrap',
                  {
                    'done-approver': a.stateText && a.stateText !== RECEIPT_STATUS_LABELS.WAITING,
                    'approved-row': a.stateText === RECEIPT_STATUS_LABELS.APPROVED,
                    'rejected-row': a.stateText === RECEIPT_STATUS_LABELS.REJECTED
                  },
                ]"
              >
                <div class="d-flex align-items-center flex-wrap">
                  <span
                    class="approval-option approval-decision disabled"
                    :class="{ active: a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER] || a.approvalRole === APPROVAL_ROLE.APPROVER }"
                  >{{ $t('receipt.submission.approverToggle.approveShort') }}</span>
                  <span
                    class="approval-option approval-concurrence approval-option-right disabled"
                    :class="{ active: a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE] || a.approvalRole === APPROVAL_ROLE.CONCURRENCE }"
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
                <span
                  class="state-badge ms-auto text-end"
                  :class="{
                    'text-success': a.stateText === RECEIPT_STATUS_LABELS.APPROVED,
                    'text-danger': a.stateText === RECEIPT_STATUS_LABELS.REJECTED,
                    'text-secondary': a.stateText === RECEIPT_STATUS_LABELS.WAITING,
                  }"
                >
                  {{ a.stateText || RECEIPT_STATUS_LABELS.WAITING }}
                </span>
              </li>
            </ul>
          </div>

          <div
            v-if="fixedApprovers.length"
            class="form-group cs-card--blue fixed-approvers-group"
          >
            <MobileMintLabel
              :text="$t('receipt.submission.fixedApproverNotice')"
              size="small"
              marginBottom="5px"
            />
            <ul class="list-group">
              <li
                v-for="a in fixedApprovers"
                :key="a.userId"
                :class="[
                  'list-group-item d-flex align-items-center flex-wrap',
                  {
                    'done-approver': a.stateText && a.stateText !== RECEIPT_STATUS_LABELS.WAITING,
                    'approved-row': a.stateText === RECEIPT_STATUS_LABELS.APPROVED,
                    'rejected-row': a.stateText === RECEIPT_STATUS_LABELS.REJECTED
                  },
                ]"
              >
                <div class="d-flex align-items-center flex-wrap">
                  <IconBadge
                    class="fixed-badge"
                    color="primary"
                  >{{ $t('receipt.common.fixed') }}</IconBadge>
                  <span
                    class="approval-option approval-decision disabled"
                    :class="{ active: a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER] || a.approvalRole === APPROVAL_ROLE.APPROVER }"
                  >{{ $t('receipt.submission.approverToggle.approveShort') }}</span>
                  <span
                    class="approval-option approval-concurrence approval-option-right disabled"
                    :class="{ active: a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE] || a.approvalRole === APPROVAL_ROLE.CONCURRENCE }"
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
                <span
                  class="state-badge ms-auto text-end"
                  :class="{
                    'text-success': a.stateText === RECEIPT_STATUS_LABELS.APPROVED,
                    'text-danger': a.stateText === RECEIPT_STATUS_LABELS.REJECTED,
                    'text-secondary': a.stateText === RECEIPT_STATUS_LABELS.WAITING,
                  }"
                >
                  {{ a.stateText || RECEIPT_STATUS_LABELS.WAITING }}
                </span>
              </li>
            </ul>
          </div>

          <DefaultFormRow align="right" marginTop="10px">
            <MobileMintButton color="gray" @click="goBack">{{ $t('receipt.common.back') }}</MobileMintButton>
          </DefaultFormRow>
        </div>
      </div>

      <!-- 이미지 미리보기: 공통 컴포넌트 -->
      <ImagePreviewModal ref="imagePreviewRef" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import ImagePreviewModal from '@/components/common/image/ImagePreviewModal.vue';
import { useRouter } from 'vue-router';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import MobileDetailTitle from '@/components/common/title/MobileDetailTitle.vue';
import MobileMintLabel from '@/components/common/label/MobileMintLabel.vue';
import MobileMintTextfield from '@/components/common/textfield/MobileMintTextfield.vue';
import MobileMintSelect from '@/components/common/select/MobileMintSelect.vue';
import MobileMintButton from '@/components/common/button/MobileMintButton.vue';
import CategoryApi from '@/api/receipt/ReceiptsCategoryApi.js';
import ReceiptsApi from '@/api/receipt/ReceiptsApi.js';
import { toDeptTeamDisplay } from '@/utils/blankFormat.js';
import { extractFirstRejectedReason } from '@/utils/receipt/receiptUtils.js';
import { RECEIPT_STATUS_LABELS, APPROVAL_ROLE_LABELS, APPROVAL_ROLE, getApprovalRoleLabel } from '@/constants/receiptConstants';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME } from '@/constants';
import IconBadge from '@/components/common/badge/IconBadge.vue';

const router = useRouter();
const receipt = ref(null);
const formData = ref({
  date: '',
  categoryId: null,
  amount: '',
  reason: '',
  participants: [],
  approvers: [],
});
const fixedApprovers = computed(() =>
  (formData.value.approvers || []).filter((a) => a?.isDefault || a?.approvalRole === 3),
);
const normalApprovers = computed(() =>
  (formData.value.approvers || []).filter((a) => !(a?.isDefault || a?.approvalRole === 3)),
);
const originalFile = ref({ name: '', url: '' });
const imagePreviewRef = ref(null);
/**
 * 이미지 미리보기 모달 열기
 * @param {string} url 이미지 URL
 */
function openPreviewModal(url) {
  if (!url) return;
  imagePreviewRef?.value?.open(url);
}
const rejectReason = ref('');

/**
 * 뒤로가기 네비게이션
 */
function goBack() {
  router.back();
}
/**
 * 모바일 해상도에서만 유지. 데스크탑으로 전환 시 뒤로가기
 */
function handleResizeRedirect() {
  if (window.innerWidth > 650) router.back();
}

/**
 * 조직 텍스트를 모바일 표시용으로 정제한다.
 * @param {string} value
 * @returns {string}
 */
function sanitizeOrgText(value) {
  const text = (value ?? '').toString().trim();
  if (!text || text === '-' || text === '--') return '';
  return text;
}

/**
 * 부서·팀 텍스트를 축약 포맷으로 결합한다.
 * @param {string} department
 * @param {string} team
 * @returns {string}
 */
function formatDeptTeam(department, team) {
  const parts = [sanitizeOrgText(department), sanitizeOrgText(team)].filter(Boolean);
  return parts.length ? parts.join(' · ') : '-';
}

const categories = ref([]);
const categoryOptions = ref([]);
const categoryText = ref('');
/**
 * 카테고리 옵션 조회 (표시용)
 */
async function loadCategories() {
  if (categories.value.length) return;
  const res =
    (await CategoryApi.getCategoriesWithDisabled?.()) ??
    (await CategoryApi.getCategories());
  categories.value = res.data || [];
  categoryOptions.value = categories.value.map((c) => ({
    value: c.categoryId,
    label: c.categoryName,
  }));
}

/**
 * 단건 조회 결과를 화면 표시용으로 매핑
 * @param {object} r 백엔드 영수증 원본
 */
function populateFromRaw(r) {
  if (!r) return;
  const peopleArr = (r.participantsList ?? []).map((p) => ({
    name: p.participantName,
    department: toDeptTeamDisplay(p.department),
    team: toDeptTeamDisplay(p.team),
    participantType: p.participantType || p.type || null,
    company: p.company || null,
    position: p.position || null,
    phone: p.phone || null,
  }));

  const approverArr = (r.approvalLines ?? []).map((al) => ({
    userId: al.approverUserId,
    name: al.delegateMapping
      ? '[대리] ' + (al.delegateMapping.delegateName || al.approverName)
      : al.approverName,
    department: toDeptTeamDisplay(
      al.delegateMapping ? al.delegateMapping.delegateDepartment : al.department,
    ),
    team: toDeptTeamDisplay(
      al.delegateMapping ? al.delegateMapping.delegateTeam : al.team,
    ),
    approvalRole: al.approvalRole,
    approvalStatus: al.approvalStatus,
    approvalType: getApprovalRoleLabel(al.approvalRole),
    stateText: al.rejectedAt ? RECEIPT_STATUS_LABELS.REJECTED : (al.approvalStatus ? RECEIPT_STATUS_LABELS.APPROVED : RECEIPT_STATUS_LABELS.WAITING),
    rejectedReason: al.rejectedReason,
    isDefault: al.approvalRole === 3,
  }));

  const amountVal = r.amount ? parseInt(r.amount, 10) : 0;
  originalFile.value = {
    name: r.attachment?.fileName || '',
    url: r.attachment?.fileUrl ? getImagePreviewUrl(r.attachment.fileUrl, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : '',
  };
  formData.value = {
    date: r.submissionDate,
    categoryId: r.category?.categoryId ?? null,
    amount: amountVal.toLocaleString() + '원',
    reason: r.reason,
    participants: peopleArr,
    approvers: approverArr,
  };
  categoryText.value = r.category?.categoryName || '';
  // 반려 사유: 결재선에서 첫 반려 사유를 간결하게 추출
  rejectReason.value = extractFirstRejectedReason(r.approvalLines);
}

/**
 * receiptId 기준 단건 조회 실행
 * @param {number|string} id 영수증 ID
 */
async function loadById(id) {
  const { data } = await ReceiptsApi.getReceiptById(id);
  populateFromRaw(data);
}

onMounted(async () => {
  // 초기 진입 시 상태 초기화
  formData.value = {
    date: '',
    categoryId: null,
    amount: '',
    reason: '',
    participants: [],
    approvers: [],
  };
  originalFile.value = { name: '', url: '' };
  rejectReason.value = '';

  handleResizeRedirect();
  window.addEventListener('resize', handleResizeRedirect);
  await loadCategories();
  try {
    const s = sessionStorage.getItem('receiptDetail');
    const parsed = s ? JSON.parse(s) : null;
    const id = parsed?.id;
    if (!id) return goBack();
    // 데이터 선 로드 후 페이지 렌더링 (약간의 티 안 나게 처리)
    await loadById(id);
  } catch {
    return goBack();
  }
});

onBeforeUnmount(() => window.removeEventListener('resize', handleResizeRedirect));
</script>

<style scoped>
:deep(.modal) {
  display: block !important;
}

:deep(.modal-backdrop) {
  display: none !important;
}

:deep(.modal-dialog) {
  margin: 0;
  max-width: 100%;
  width: 100vw;
  height: calc(100vh - 130px);
}

:deep(.modal-content) {
  height: 100%;
  border-radius: 0;
}

/* 모달 본문과 동일한 스타일 복제 */
hr {
  /* margin: 0 0 5px 0; */
  margin: 20px 0 20px 0 !important;
  border: none;
  height: 1px;
  background: #000000;
}

.content-sub-title {
  margin-bottom: 10px !important;
}

.search-wrapper-hr {
  margin: 20px 0 20px 0 !important;
}

.search-wrapper-hr-1 {
  margin: 20px 0 20px 0 !important;
}

.search-wrapper-hr-2 {
  margin: 20px 0 20px 0 !important;
}

.modal-body {
  padding-top: 15px;
  max-height: none !important;
  overflow-y: visible !important;
  background-color: #f9fafb !important;
}

.form-row .col {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.reason {
  margin-top: 10px;
}

.form-group {
  margin-bottom: 20px;
  background: #ffffff;
  border: 1px solid #e6f2ff;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.06);
}

.form-row {
  display: flex;
  gap: 10px;
}
.form-row.align-items-center {
  background: #ffffff;
  border: 1px solid #e6f2ff;
  border-radius: 12px;
  padding: 8px 10px;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.06);
  overflow: visible;
}

.list-group-item,
.list-group {
  font-size: 0.65rem !important;
}
.list-group {
  background: #ffffff;
  border: 1px solid #e6f2ff;
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.06);
  overflow: hidden;
}
.list-group-item {
  border: none !important;
  border-top: 1px dashed #e6efff !important;
  padding: 10px 12px !important;
}
.name-block {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 4px;
  font-size: 0.7rem;
}
.name-strong {
  font-weight: 800;
}
.dept-team {
  font-weight: 600;
  font-size: 0.62rem;
  color: #4b5563;
}
.external-block .dept-team {
  font-weight: 500;
  color: #4b5563;
}
.list-group-item:first-of-type {
  border-top: none !important;
}

.list-no-group {
  margin-top: 2px;
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

/* ==== 모바일 카드 톤: 템플릿 변경 없이 .form-group/.list-group를 카드처럼 보이게 ==== */
.form-group {
  background: #ffffff;
  border: 1px solid #e6f2ff;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.06);
}
.form-row.align-items-center {
  background: #ffffff;
  border: 1px solid #e6f2ff;
  border-radius: 12px;
  padding: 8px 10px;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.06);
  overflow: visible;
}
.list-group {
  background: #ffffff;
  border: 1px solid #e6f2ff;
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.06);
  overflow: hidden;
}
.list-group-item {
  border: none !important;
  border-top: 1px dashed #e6efff !important;
  padding: 10px 12px !important;
}
.list-group-item:first-of-type {
  border-top: none !important;
}

.approval-option {
  cursor: pointer;
  padding: 2px 6px;
  color: #39393a;
  border: 1px solid #8e8e8f;
  user-select: none;
  margin: 0;
  font-weight: 900 !important;
}

.approval-option + .approval-option {
  margin-left: -1px;
}

.approval-option-right {
  margin-right: 7px;
}
.fixed-badge {
  margin-right: 5px;
}
.fixed-approvers-group {
  margin-top: 20px;
}
.fixed-approvers-group .list-group {
  margin-top: 10px;
}

.approval-decision.active {
  color: #dc3545 !important;
  background: #dc354510 !important;
}

.approval-concurrence.active {
  color: #198754 !important;
  background: #1987542b !important;
}

.approval-option.disabled {
  pointer-events: none;
  opacity: 0.4;
}

/* 텍스트 입력/셀렉트 비활성 배경 톤 보정 */
.form-group input[disabled],
.form-group select[disabled] {
  background: #f9fcff !important;
  border: 1px solid #e6f2ff !important;
}

/* 사유 카드 강조 */
.reason-card {
  border-color: #eaf4ff !important;
  background: #fbfdff !important;
}
.reason-card input[disabled] {
  background: #ffffff !important;
  border: 1px dashed #cfe6ff !important;
  color: #0b162f !important;
  font-weight: 700;
}
.reason-head {
  display: flex;
  align-items: center;
  gap: 3px;
  color: #334155;
  font-weight: 800;
  margin-bottom: 4px;
  font-size: 0.75rem;
}
.reason-head i {
  font-size: 0.8rem;
}
.reason-display {
  background: #ffffff;
  border: 1px dashed #cfe6ff;
  border-radius: 10px;
  padding: 8px 10px;
  color: #111827;
  font-weight: 600;
  line-height: 1.35;
  font-size: 0.65rem;
  word-break: break-word;
}

/* 상태 텍스트를 배지처럼 보이게 */
.state-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 22px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.6rem;
  font-weight: 800;
  border: 1px solid transparent;
}
.state-badge.text-success {
  background: rgba(10, 122, 85, 0.36);
  color: #ffffff !important;
  border-color: rgba(10, 122, 85, 0.5);
}
.state-badge.text-danger {
  background: rgba(198, 40, 40, 0.36);
  color: #ffffff !important;
  border-color: rgba(198, 40, 40, 0.5);
}
.state-badge.text-secondary {
  background: rgba(51, 65, 85, 0.36);
  color: #ffffff !important;
  border-color: rgba(51, 65, 85, 0.5);
}

/* 결재자 목록 상태별 배경색 */
.list-group-item.approved-row {
  background: rgba(16, 185, 129, 0.08) !important; /* 아주 연한 초록색 */
}
.list-group-item.rejected-row {
  background: rgba(239, 68, 68, 0.08) !important; /* 아주 연한 빨간색 */
}

.slide-fast-enter-active,
.slide-fast-leave-active {
  transition: transform 160ms ease-out, opacity 160ms ease-out, max-height 160ms ease-out;
  overflow: hidden;
}

.slide-fast-enter-from,
.slide-fast-leave-to {
  opacity: 0;
  transform: translateY(-6px);
  max-height: 0;
}

.slide-fast-enter-to,
.slide-fast-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 500px;
}

.favorite-approvers-area {
  margin-top: 10px;
  background: #f7f7f7;
  border-radius: 6px;
  padding: 8px 10px;
}

.external-section {
  margin-top: 10px;
  background: #f7f7f7;
  border-radius: 6px;
  padding: 8px 10px;
}

.favorite-star.btn-warning {
  color: #ffffff !important;
}

/* ── 이미지 미리보기 오버레이: 중앙 정렬/최상위 표시 ── */
.thumb-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.thumb-inline {
  display: grid;
  grid-template-columns: 92px 1fr;
  gap: 12px;
  align-items: center;
}
.thumb {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  border: 1px solid #e6f2ff;
  border-radius: 10px;
  background: #f7fbff;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.08);
}
.thumb__img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb__overlay {
  position: absolute;
  inset: 0;
  background: radial-gradient(rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0.25));
  opacity: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s ease;
}
.thumb:hover .thumb__overlay {
  opacity: 1;
}
.thumb__icon {
  font-size: 1.2rem;
  color: #ffffff;
}
.meta-col {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin: 0px 0px 10px 10px;
}
.file-name-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: #f9fcff;
  border: 1px solid #e6f2ff;
  border-radius: 8px;
  font-size: 0.7rem;
  width: 100%;
  min-width: 0;
  overflow: hidden;
}
.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #334155;
  cursor: default;
  min-width: 0;
}
.meta-chips {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.7rem;
  line-height: 1;
  border: 1px solid transparent;
}
.chip--mint {
  background: rgba(11, 107, 203, 0.36);
  color: #ffffff;
  border-color: rgba(11, 107, 203, 0.5);
  font-weight: 700;
}
.meta-col .form-row.align-items-center {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.meta-col .form-row.align-items-center .col {
  width: 100%;
  min-width: 0;
}

/* 리스트 배지(고정) 톤 보정 - 사용자 정보 배지 톤과 일치 */
.list-group .badge.text-bg-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.68rem;
  font-weight: 800;
  background: rgba(11, 107, 203, 0.36) !important;
  color: #ffffff !important;
  border: 1px solid rgba(11, 107, 203, 0.5) !important;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #334155;
}
.meta-icon {
  font-size: 0.95rem;
  color: #0b6bcb;
}
.meta-text {
  font-size: 0.8rem;
  font-weight: 900;
}
.meta-amount {
  font-size: 0.8rem;
  font-weight: 800;
  color: #0b6bcb;
}
.sep {
  opacity: 0.5;
}
.hide-when-inline {
  display: none;
}
.thumb-empty {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6b7a90;
  font-size: 0.8rem;
}
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

/* 다크모드: theme/pages/receipt/detail/mobile/receipt-detail-view-dark.css */
</style>
