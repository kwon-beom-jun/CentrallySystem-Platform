<template>
  <b-modal
    v-model="innerVisible"
    :title="$t('receipt.submission.edit')"
    no-close-on-backdrop
    size="xl"
    scrollable
    centered
    fade
    hide-footer
    custom-class="receipt-modal"
    body-class="p-0" 
  >
    <div class="modal-body d-flex">
      <!-- ① 이미지 영역 -->
      <div
        class="preview-pane"
        :class="{ zoomed: isPaneZoomed  }"
        @dblclick="openZoom"
      >
        <img
          v-if="previewUrl"
          :src="previewUrl"
          class="preview-img"
          :class="{ zoomed: isPaneZoomed  }"
          :style="zoomStyle"
          draggable="false"
          @dragstart.prevent
          @mousedown="startDrag"
          @touchstart.passive="startDrag"
        />
        <div v-else class="preview-placeholder">
          {{ $t('receipt.common.receiptPhoto') }}
        </div>
      </div>

      <form
        class="form-pane"
        @submit.prevent="openConfirmationModal"
        @keydown.enter.prevent
      >
        <!-- 이미지 등록 -->
        <div class="form-group">
          <DefaultLabel
            :text="$t('receipt.common.receiptPhoto')"
            forId="receiptImage"
            size="small"
            marginBottom="5px"
            :required="true"
          />
          <DefaultTextfield
            type="file"
            id="receiptImage"
            size="full"
            style="width: 100%"
            @change="handleFileChange"
          />
          <!-- 기존 파일 표시 (새 파일을 아직 선택하지 않았을 때만) -->
          <div v-if="originalFile.name && !receiptFile" class="mt-2">
            <ul class="list-group">
              <li class="list-group-item">
                <a :href="originalFilePreviewUrl" @click.prevent="openPreviewModal(originalFilePreviewUrl)"
                  >{{ $t('receipt.submission.existingFilePrefix') }} {{ originalFile.name }}</a
                >
              </li>
            </ul>
          </div>

          <!-- 새 파일을 선택하면 기존 영역은 사라지고, 아래 v-if="receiptFile" 가 뜸 -->
          <div v-if="receiptFile" class="mt-2">
            <ul class="list-group">
              <li class="list-group-item">
                {{ $t('receipt.submission.newFilePrefix') }} {{ receiptFile.name }}
                <button
                  type="button"
                  class="btn btn-danger square-btn ms-2"
                  @click="removeFile"
                >
                  x
                </button>
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
            <DefaultSmallButton variant="secondary" @click="setToday">
              {{ $t('receipt.submission.now') }}
            </DefaultSmallButton>
          </DefaultFormRow>
          <DefaultTextfield
            type="date"
            id="date"
            v-model="formData.date"
            size="full"
            :required="true"
          />
        </div>

        <hr class="search-wrapper-hr-2" />
        <!-- 참여자 검색 필드 -->
        <div class="search-wrapper">
          <div class="d-flex align-items-center">
            <DefaultLabel
              :text="$t('receipt.common.participants') + ' ' + $t('common.label.search')"
              forId="participantSearch"
              size="small"
              marginBottom="5px"
            />
            <DefaultSmallButton marginBottom="5px" variant="secondary" @click="showExternal = !showExternal">{{ $t('receipt.common.external') }}</DefaultSmallButton>
          </div>
          <UserSearchDropdown
            ref="participantSearchRef"
            :labelText="$t('common.label.search')"
            inputId="participantSearch"
            inputSize="full"
            :keepSearchValue="false"
            :placeholder="$t('hrm.permissionsDetail.searchPlaceholder')"
            @userSelected="onParticipantSelected"
          />
        </div>

        <!-- 외부 참여자 인라인 추가 (토글 표시) -->
        <transition name="slide-fast">
          <div v-if="showExternal" class="external-section cs-card--blue">
            <DefaultLabel :text="$t('receipt.common.external') + ' ' + $t('receipt.common.participants') + ' ' + $t('common.button.add')" size="small" marginBottom="5px" marginTop="10px"/>
            <DefaultFormRow gap="5px" marginBottom="5px">
              <DefaultTextfield v-model="externalParticipant.name" :placeholder="$t('common.label.name') + '(*)'" size="full" style="width: 100%" />
              <DefaultTextfield v-model="externalParticipant.phone" :placeholder="$t('receipt.common.phone') + '(*)'" size="full" style="width: 100%" />
            </DefaultFormRow>
            <DefaultFormRow gap="5px">
              <DefaultTextfield v-model="externalParticipant.company" :placeholder="$t('receipt.common.company')" size="full" style="width: 100%" />
              <DefaultTextfield v-model="externalParticipant.position" :placeholder="$t('common.label.position')" size="full" style="width: 100%" />
            </DefaultFormRow>
            <DefaultFormRow align="right" marginTop="5px">
              <DefaultButton color="blue" @click="addExternalParticipant" customWidth="60px" customHeight="25px"> {{ $t('common.button.add') }} </DefaultButton>
            </DefaultFormRow>
          </div>
        </transition>

        <!-- 추가된 참여자 목록 -->
        <div v-if="formData.participants.length > 0">
          <DefaultLabel
            :text="$t('receipt.common.participantsList')"
            marginBottom="5px"
            marginTop="10px"
            size="small"
          />
          <ul class="list-group">
            <li
              v-for="(person, idx) in formData.participants"
              :key="idx"
              class="list-group-item d-flex align-items-center justify-content-between"
            >
              <div class="name-block" :class="{ 'external-block': person.type === 'EXTERNAL' }">
                <strong class="name-strong">
                  <template v-if="person.type === 'EXTERNAL'">
                    [{{ $t('receipt.common.external') }}] {{ person.name }}
                  </template>
                  <template v-else>
                    {{ person.name }}
                  </template>
                </strong>
                <span
                  v-if="person.type === 'EXTERNAL'"
                  class="dept-team"
                >
                  {{ person.phone || '-' }} / {{ person.company || '-' }} / {{ person.position || '-' }}
                </span>
                <span
                  v-else-if="formatDeptTeam(person.department, person.team) !== '-'"
                  class="dept-team"
                >
                  {{ formatDeptTeam(person.department, person.team) }}
                </span>
              </div>
              <!-- 삭제 버튼 -->
              <button
                type="button"
                class="btn btn-sm btn-outline-danger square-btn"
                @click="removeParticipant(idx)"
              >
                ×
              </button>
            </li>
          </ul>
        </div>
        <hr class="search-wrapper-hr-2" />

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
              size="full"
              style="width: 100%"
              :reserveErrorSpace="true"
              marginBottom="22px"
              :class="categorySelectClass"
            />
          </div>
          <div class="col">
            <DefaultLabel
              :text="amountLabel"
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
              validationType="money"
              :moneyMax="maxAllowed"
              :disabled="!selectedCategory"
              :reserveErrorSpace="true"
              placeholder="카테고리 → 금액(원) 입력해주세요"
            />
          </div>
        </div>
        <hr class="search-wrapper-hr-1" />

        <div v-if="isRejected && rejectedReason" class="form-group">
          <DefaultLabel :text="$t('receipt.common.rejectReason')" marginBottom="5px" size="small" />
          <DefaultTextfield
            type="text"
            :modelValue="rejectedReason"
            size="full"
            :disabled="true"
            bg-color="#fff0f0"
          />
        </div>

        <!-- 결재자 검색 필드 -->
        <div class="form-group search-wrapper">
          <DefaultLabel
            :text="$t('receipt.common.approversList') + ' ' + $t('common.label.search')"
            forId="approverSearch"
            size="small"
            marginBottom="5px"
            :required="true"
          />
          
          <!-- 
            영수증 서비스만 국한되게 조회됨 제거시 전체 서비스
            :filterBy="{ service: 'receipt',
                'ROLE_RECEIPT_APPROVER',
                ...
          -->
          <UserSearchDropdown
            ref="approverSearchRef"
            :labelText="$t('receipt.common.approversList') + ' ' + $t('common.label.search')"
            inputId="approverSearch"
            inputSize="full"
            :keepSearchValue="false"
            :filterBy="{
              roleDetails: [
                'ROLE_RECEIPT_APPROVER',
                'ROLE_RECEIPT_INSPECTOR',
                'ROLE_RECEIPT_MANAGER',
                'ROLE_GATE_SYSTEM'] 
            }"
            :placeholder="$t('hrm.permissionsDetail.searchPlaceholder')"
            @userSelected="onApproverSelected"
          />
        </div>

        <!-- 결재자 목록 -->
        <div v-if="formData.approvers.length > 0">
          <DefaultLabel
            :text="$t('receipt.common.approversList')"
            marginBottom="5px"
            size="small"
          />

          <!-- vuedraggable 적용 -->
          <draggable
            v-model="normalApprovers"
            item-key="userId"
            tag="ul"
            class="list-group"
            handle=".drag-handle"
          >
            <template #item="{ element, index }">
              <li
                class="list-group-item d-flex align-items-center justify-content-between"
              >
                <span class="d-flex align-items-center flex-wrap">
                  <!-- 드래그 핸들 -->
                  <span class="drag-handle me-2" :title="$t('hrm.favoriteMenu.reorder')"
                    >≡</span
                  >

                  <IconBadge
                    v-if="element.isDefault"
                    class="fixed-badge"
                    color="primary"
                  >{{ $t('receipt.common.fixed') }}</IconBadge>

                  <!-- [ 결재 | 합의 ] 글자 버튼 -->
                  <span
                    :class="[
                      'approval-option',
                      'approval-decision',
                      { active: element.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER], disabled: element.isDefault }
                    ]"
                    @click="!element.isDefault && setApprovalType(element, APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER])"
                  >
                    {{ $t('receipt.submission.approverToggle.approveShort') }}
                  </span>
                  <span
                    :class="[
                      'approval-option',
                      'approval-concurrence',
                      'approval-option-right',
                      {
                        active  : element.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE],
                        disabled: element.isDefault || isSameDepartment(element),
                      }
                    ]"
                    @click="!element.isDefault && onConcurrenceClick(element)"
                  >
                    {{ $t('receipt.submission.approverToggle.concurShort') }}
                  </span>

                  <div class="name-block approver-block">
                    <strong class="name-strong">{{ element.name }}</strong>
                    <span
                      v-if="formatDeptTeam(element.department, element.team) !== '-'"
                      class="dept-team"
                    >
                      {{ formatDeptTeam(element.department, element.team) }}
                    </span>
                  </div>
                </span>

                <!-- 삭제 버튼 -->
                <button
                  v-if="!element.isDefault"
                  type="button"
                  class="btn btn-sm btn-outline-danger square-btn"
                  @click.stop="removeApprover(index)"
                >
                  ×
                </button>
              </li>
            </template>
          </draggable>

          <!-- ── 2) 고정 합의자(맨 끝·드래그 X) ───────────────── -->
          <ul v-if="fixedApprovers.length" class="list-group mt-2">
            <li
              v-for="a in fixedApprovers"
              :key="a.userId"
              class="list-group-item d-flex align-items-center justify-content-between"
            >
              <span class="d-flex align-items-center flex-wrap">
                <IconBadge
                  class="fixed-badge"
                  color="primary"
                >{{ $t('receipt.common.fixed') }}</IconBadge>

                <!-- ✅ "결재 / 합의" 표시(비활성) -->
                <span
                  :class="[
                    'approval-option',
                    'approval-decision',
                    { active: a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER], disabled: true }
                  ]"
                >
                  {{ $t('receipt.submission.approverToggle.approveShort') }}
                </span>
                <span
                  :class="[
                    'approval-option',
                    'approval-concurrence',
                    'approval-option-right',
                    { active: a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE], disabled: true }
                  ]"
                >
                  {{ $t('receipt.submission.approverToggle.concurShort') }}
                </span>

                <div class="name-block approver-block">
                  <strong class="name-strong">{{ a.name }}</strong>
                  <span
                    v-if="formatDeptTeam(a.department, a.team) !== '-'"
                    class="dept-team"
                  >
                    {{ formatDeptTeam(a.department, a.team) }}
                  </span>
                </div>
              </span>
              <!-- 삭제‧타입 변경 버튼 없음 -->
            </li>
          </ul>
        </div>
        <hr class="search-wrapper-hr-2" />

        <!-- 사유 -->
        <div class="form-group reason">
          <DefaultLabel
            :text="$t('receipt.common.reason')"
            forId="reason"
            size="small"
            marginBottom="5px"
            :required="true"
          />
          <DefaultTextfield
            type="text"
            id="reason"
            size="full"
            v-model="formData.reason"
            :placeholder="$t('common.placeholder.content')"
          />
        </div>

        <!-- footer 영역 직접 구현 -->
        <div class="modal-footer">
          <DefaultButton
            align="right"
            color="gray"
            @click="closeModal"
          >
            {{ $t('common.button.cancel') }}
          </DefaultButton>
          <DefaultButton type="submit" align="right" marginLeft="5px" >
            {{ $t('common.button.modify') }}
          </DefaultButton>
          <DefaultButton
            color="blue"
            align="right"
            marginLeft="5px"
            @click="requestApply"
            :disabled="isApplyButtonDisabled"
            v-tooltip.bottom="applyButtonTooltip"
          >
            {{ $t('receipt.submission.apply') }}
          </DefaultButton>
          <DefaultButton 
            color="red" 
            align="right" 
            marginLeft="5px"
            @click="openDeleteModal"
          >
            {{ $t('common.button.delete') }}
          </DefaultButton>
        </div>
      </form>
    </div>
  </b-modal>

  <!-- 이미지 미리보기: 공통 컴포넌트 -->
  <ImagePreviewModal ref="imagePreviewRef" />
  
  <!-- 확인 모달 -->
  <AlertModal
    :isVisible="confirmationModalVisible"
    :title="$t('common.message.confirmSave')"
    @close="confirmationModalVisible = false"
    @confirm="updateReceipt"
    :cancelText="$t('common.button.cancel')"
    :confirmText="$t('common.button.confirm')"
  >
    <template #body>{{ $t('receipt.submission.edit') + '?' }}</template>
  </AlertModal>

  <!-- 삭제 확인 -->
  <AlertModal
    :isVisible="deleteModalVisible"
    :confirmText="$t('common.button.delete')"
    :cancelText="$t('common.button.cancel')"
    @close="deleteModalVisible=false"
    @confirm="confirmDelete"
  >
    <template #body>
        <div style="color: red;">
          {{ $t('receipt.management.deleteWarning') }} <br/><br/>
          {{ $t('receipt.management.deleteMessage') }} <br/>
          {{ $t('receipt.management.deleteProceed') }} <br/>
        </div>
    </template>
  </AlertModal>
</template>

<script setup>
/* ────────────────────────────── ①  Imports ───────────────────────────── */
import { ref, reactive, computed, watch, onMounted, defineProps, defineEmits } from 'vue';
import { useI18n } from 'vue-i18n';
import { BModal }          from 'bootstrap-vue-3';
import draggable           from 'vuedraggable';
import { processImageFile } from '@/utils/imageProcessor.js';
import { extractFirstRejectedReason } from '@/utils/receipt/receiptUtils.js';

import DefaultLabel        from '@/components/common/label/DefaultLabel.vue';
import DefaultTextfield    from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultFormRow      from '@/components/common/DefaultFormRow.vue';
import DefaultButton       from '@/components/common/button/DefaultButton.vue';
import DefaultSelect       from '@/components/common/select/DefaultSelect.vue';
import DefaultSmallButton       from '@/components/common/button/DefaultSmallButton.vue'
import UserSearchDropdown  from '@/components/auth/UserSearchDropdown.vue';
import AlertModal          from '@/components/common/modal/AlertModal.vue';
import IconBadge                from '@/components/common/badge/IconBadge.vue'

import CategoryApi         from '@/api/receipt/ReceiptsCategoryApi.js';
import ReceiptApi          from '@/api/receipt/ReceiptsApi.js';
import ReceiptsRequestApi  from '@/api/receipt/ReceiptsRequestApi.js';

import { useAuthStore }        from '@/store/auth';
import ImagePreviewModal from '@/components/common/image/ImagePreviewModal.vue';
import { toast }               from 'vue3-toastify';
import { VTooltip } from 'floating-vue';
import { toDeptTeamDisplay, toDeptTeamPayload } from '@/utils/blankFormat.js'
import { 
  APPROVAL_ROLE_LABELS, 
  APPROVAL_ROLE, 
  getApprovalRoleLabel,
  RECEIPT_STATUS_LABELS
} from '@/constants/receiptConstants';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME } from '@/constants';

/* ────────────────────────────── ②  Props & Emits ─────────────────────── */
const props = defineProps({
  isVisible: Boolean,
  receiptId: [Number, String],
  currentDeptName: String,
});
const emit = defineEmits(['close', 'confirm', 'request-apply']);

/* ────────────────────────────── ③  Stores ────────────────────────────── */
const authStore     = useAuthStore();
const { t } = useI18n();

/* ────────────────────────────── ④  모달 on/off ───────────────────────── */
const innerVisible             = ref(props.isVisible);
const confirmationModalVisible = ref(false);
const deleteModalVisible       = ref(false);   // 삭제 확인

/* ────────────────────────────── ⑤  미리보기 모달(공통) ─────────────────── */
const imagePreviewRef = ref(null);
/**
 * 이미지 미리보기 열기
 * @param {string} url 이미지 URL
 */
function openPreviewModal(url) {
  if (!url) return;
  imagePreviewRef?.value?.open(url);
}

/**
 * 기존 파일 URL을 게이트웨이를 거쳐 변환
 */
const originalFilePreviewUrl = computed(() => {
  return originalFile.value.url ? getImagePreviewUrl(originalFile.value.url, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : '';
});

/* ────────────────────────────── ⑥  Form 데이터 ───────────────────────── */
const formData = ref({
  date: '', type: '', amount: '', reason: '',
  participants: [], approvers: [],
});
const externalParticipant = ref({ company:'', name:'', position:'', phone:'' });
const showExternal = ref(false);
const originalFormData = ref(null);
// 비활성 여부 상태: 로드된 원본 기준
const loadedReceipt = ref(null);
const currentReceiptId = ref(null);
const rejectedReason = ref('');

/**
 * 조직 텍스트를 모달 표시에 맞춰 정제한다.
 * @param {string} value
 * @returns {string}
 */
function sanitizeOrgText(value) {
  const text = (value ?? '').toString().trim();
  if (!text || text === '-' || text === '--') return '';
  return text;
}

/**
 * 부서와 팀 텍스트를 단일 표시 문자열로 결합한다.
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

/* ---------- 파일 ---------- */
const receiptFile  = ref(null);
const previewUrl   = ref(null);
const originalFile = ref({ name: '', url: '' });

// 이미지 파일 처리(압축·리사이즈) util 사용
async function handleFileChange (e) {
  const file = e.target.files[0] || null
  if (!file) return

  let processedFile
  try {
    processedFile = await processImageFile(file, { maxSizeMB: 2, maxWidthOrHeight: 2048 })
  } catch (err) {
    if (err.message === 'FILE_SIZE_EXCEEDED') {
      toast.error(t('common.message.serverError'))
    } else {
      toast.error(t('common.message.serverError'))
      console.error(err)
    }
    return
  }
  receiptFile.value = processedFile

  // ── 미리보기 URL 갱신 ───────────────────────────────────────────
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = processedFile
    ? URL.createObjectURL(processedFile)
    : (originalFile.value.url ? getImagePreviewUrl(originalFile.value.url, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : null)

  resetZoomAndPos()
  e.target.value = ''
}
function removeFile () {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value);
  receiptFile.value = null;
  previewUrl.value  = originalFile.value.url ? getImagePreviewUrl(originalFile.value.url, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : null;
  resetZoomAndPos();
}

/* ---------- 오늘 날짜 ---------- */
function setToday () {
  const d = new Date();
  formData.value.date = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
}

/* ---------- 참여자 / 결재자 ---------- */
function onParticipantSelected (u) {
  if (formData.value.participants.some(p => p.userId === u.userId))
    return toast.warning(t('receipt.common.participants') + ' ' + t('common.message.required'));
  formData.value.participants.push({ userId:u.userId, name:u.name, department:u.department, team:u.team });
}
function addExternalParticipant () {
  const p = externalParticipant.value;
  if (!p.name || !p.phone) return toast.warning(t('receipt.common.external') + ' ' + t('receipt.common.participants') + ' ' + t('common.message.required'));
  const dup = formData.value.participants.some(x => x.type==='EXTERNAL' && x.name===p.name && x.phone===p.phone);
  if (dup) return toast.warning(t('receipt.common.external') + ' ' + t('receipt.common.participants') + ' ' + t('common.message.required'));
  formData.value.participants.push({ type:'EXTERNAL', userId:null, name:p.name, company:p.company, position:p.position, phone:p.phone, department:'', team:'' });
  externalParticipant.value = { company:'', name:'', position:'', phone:'' };
}

function onApproverSelected (u) {
  if (formData.value.approvers.some(a => a.userId === u.userId))
    return toast.warning(t('receipt.common.approversList') + ' ' + t('common.message.required'));
  /* ── 현재 '결재'(approvalType === "결재") 인원 수 검사 ── */
  const decisionCnt = formData.value.approvers
    .filter(a => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]).length
  if (decisionCnt >= 3)
    return toast.warning(t('receipt.common.approversList') + ' ' + t('common.message.required'))
  const obj = { userId:u.userId, name:u.name, email:u.email, department:u.department, team:u.team,
                approvalType:APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER], isDefault:false, approvalRole:APPROVAL_ROLE.APPROVER };
  formData.value.approvers.splice(normalApprovers.value.length, 0, obj);
}

function removeParticipant (idx) { formData.value.participants.splice(idx,1); }
function removeApprover    (idx) { 
  if (formData.value.approvers[idx].isDefault) return toast.info(t('receipt.common.fixed') + ' ' + t('common.message.required'));
  formData.value.approvers.splice(idx,1); 
}

/* ────────────────────────────── ⑦  Drag / Zoom ──────────────────────── */
const isPaneZoomed = ref(false);
const pos          = reactive({ x:0, y:0 });
let   start        = { x:0, y:0 };
let   dragging     = false;

const zoomStyle = computed(() =>
  isPaneZoomed.value
    ? { transform:`translate(${pos.x}px,${pos.y}px) scale(1.5)`,
        cursor: dragging?'grabbing':'zoom-out' }
    : { cursor:'zoom-in' });

function openZoom () {
  if (!previewUrl.value) return;
  isPaneZoomed.value = !isPaneZoomed.value;
  if (!isPaneZoomed.value) pos.x = pos.y = 0;
}
function startDrag (e) {
  if (!isPaneZoomed.value) return;
  e.preventDefault();
  dragging = true;
  const p  = e.touches?.[0] ?? e;
  start    = { x:p.clientX-pos.x, y:p.clientY-pos.y };
  addDragListeners();
}
function onMove (e) {
  if (!dragging) return;
  const p  = e.touches?.[0] ?? e;
  pos.x    = p.clientX - start.x;
  pos.y    = p.clientY - start.y;
  e.preventDefault();
}
function endDrag () { dragging=false; removeDragListeners(); }

function addDragListeners () {
  window.addEventListener('mousemove', onMove);
  window.addEventListener('mouseup',   endDrag);
  window.addEventListener('touchmove', onMove, { passive:false });
  window.addEventListener('touchend',  endDrag);
}
function removeDragListeners () {
  window.removeEventListener('mousemove', onMove);
  window.removeEventListener('mouseup',   endDrag);
  window.removeEventListener('touchmove', onMove);
  window.removeEventListener('touchend',  endDrag);
}
function resetZoomAndPos () { isPaneZoomed.value=false; dragging=false; pos.x=pos.y=0; }

/* ────────────────────────────── ⑧  한도 / 카테고리 ──────────────────── */
const categories      = ref([]);
const categoryOptions = computed(() => {
  const enabledOptions = categories.value
    .filter(c => c.enabled)
    .map(c => ({ value: c.categoryId, label: c.categoryName }));

  const r = loadedReceipt.value;
  const disabledCategoryId = r?.category?.categoryId;
  const disabledCategoryName = r?.category?.categoryName;
  const isDisabled = r?.category && r.category.enabled === false;
  if (isDisabled && disabledCategoryId != null) {
    if (!enabledOptions.some(opt => opt.value === disabledCategoryId)) {
      enabledOptions.unshift({ value: disabledCategoryId, label: `[비활성] ${disabledCategoryName || '카테고리'}` });
    }
  }
  return enabledOptions;
});
async function loadCategories() {
  if (!categories.value.length) categories.value = (await CategoryApi.getCategoriesWithDisabled()).data;
}
const selectedCategory = computed(()=>{
  const id = Number(formData.value.categoryId || NaN);
  return categories.value.find(c => Number(c.categoryId) === id);
});
const maxAllowed = computed(()=>{
  if (!selectedCategory.value) return Infinity;
  return Math.floor(selectedCategory.value.limitPrice * (formData.value.participants.length+1));
});
const amountLabel = computed(()=>{
  if (!selectedCategory.value) return t('receipt.common.amount');
  const tot = selectedCategory.value.limitPrice*(formData.value.participants.length+1);
  return t('receipt.submission.amountWithLimit', { limit: tot.toLocaleString() });
});

/* ────────────────────────────── ⑨  부서 / 승인타입 ─────────────────── */
const myDeptUpper = computed(() =>
  (props.currentDeptName || '').trim().toUpperCase()
);
function isSameDepartment (u) {
  return (u?.department ?? '').trim().toUpperCase() === myDeptUpper.value;
}
// 고정 + 같은 부서 제거
function sanitizeApprovers(list) {
  return list.filter(a => !(a.approvalRole === 3 && isSameDepartment(a)))
}
function setApprovalType (u, type) {
  if (u.isDefault || (type===APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE] && isSameDepartment(u))) return;
  /* 3 명 제한: 합의 → 결재 전환 시에도 검사 */
  if (type === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]) {
    const decisionCnt = formData.value.approvers
        .filter(a => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]).length
    /* 이미 3명인데, 본인도 결재로 바꾸려 하면 차단 */
    if (decisionCnt >= 3 && u.approvalType !== APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER])
      return toast.warning(t('receipt.common.approversList') + ' ' + t('common.message.required'))
  }
  u.approvalType = type;
  u.approvalRole = u.isDefault
    ? 3 : (type===APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE] ? APPROVAL_ROLE.CONCURRENCE : APPROVAL_ROLE.APPROVER);
}
function onConcurrenceClick (u) {
  if (u.isDefault || isSameDepartment(u)) {
    if (isSameDepartment(u)) toast.info(t('receipt.common.agree') + ' ' + t('common.message.required'));
    return;
  }
  setApprovalType(u, APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE]);
}

/* ---------- 고정 / 일반 결재자 ---------- */
const fixedApprovers   = computed(()=> formData.value.approvers.filter(a=>a.isDefault));
const normalApprovers  = computed({
  get:()=>formData.value.approvers.filter(a=>!a.isDefault),
  set:list=>{ formData.value.approvers=[...list,...fixedApprovers.value]; }
});

/* ────────────────────────────── ⑩  PATCH 용 FormData ────────────────── */
function buildDiffFD () {
  const fd = new FormData(), cur=formData.value, orig=originalFormData.value || {};

  /* Primitive */
  if (cur.date       && cur.date       !== orig.date)       fd.append('date',cur.date);
  if (cur.categoryId && cur.categoryId !== orig.categoryId) fd.append('categoryId',cur.categoryId);
  if (cur.reason     && cur.reason     !== orig.reason)     fd.append('reason',cur.reason);
  // amount 처리 로직
  const origAmount = String((orig.amountRaw ?? orig.amount) || '').replace(/\D/g, '');
  const currAmount = String(cur.amount || '').replace(/\D/g, '');
  if (currAmount && currAmount !== origAmount) {
    // 쉼표가 제거된 순수 숫자 문자열을 전송
    fd.append('amount', currAmount);
  }


  /* Participants */
  const stripP = p=>({ 
    type:p.type||'INTERNAL', 
    userId:p.userId, 
    name:p.name, 
    company:p.company, 
    position:p.position, 
    phone:p.phone, 
    department:toEmpty(p.department), 
    team:toEmpty(p.team) 
  });
  const newP   = JSON.stringify(cur.participants.map(stripP));
  const oldP   = JSON.stringify((orig.participants??[]).map(stripP));
  if (newP!==oldP) fd.append('participants',newP);

  /* Approvers */
  const stripA = a=>({
    userId       : a.userId,
    email        : a.email ?? '',
    approvalType : a.approvalType,
    approvalRole : a.isDefault ? 3 : (
      a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE] ? APPROVAL_ROLE.CONCURRENCE : APPROVAL_ROLE.APPROVER
    ),
    isDefault    : a.isDefault,
    name         : a.name,
    department   : toEmpty(a.department),
    team         : toEmpty(a.team)
  });
  const newA = JSON.stringify(cur.approvers.map(stripA));
  const oldA = JSON.stringify((orig.approvers??[]).map(stripA));
  if (newA!==oldA) fd.append('approvers',newA);

  /* File */
  if (receiptFile.value)              fd.append('receiptFile',receiptFile.value);
  else if (!previewUrl.value && (loadedReceipt.value?.attachment?.fileUrl)) fd.append('deleteFile',true);

  return fd;
}

/**
 * 단건 조회 응답을 편집 폼 구조로 변환
 * @param {object} r 백엔드 원본 영수증
 * @returns {{ 
 *  form: {
 *    date:string,
 *    categoryId:number|null,
 *    amount:string,
 *    reason:string,
 *    participants:Array,
 *    approvers:Array
 *  },
 *  originalFile: {name:string, url:string} 
 * }}
 */
function mapRawToForm(r) {
  const participants = (r?.participantsList ?? []).map((p) => ({
    type: p.participantType === 'EXTERNAL' ? 'EXTERNAL' : 'INTERNAL',
    userId: p.participantUserId ?? null,
    name: p.participantName,
    company: p.company ?? null,
    position: p.position ?? null,
    phone: p.phone ?? null,
    department: toDeptTeamDisplay(p.department),
    team: toDeptTeamDisplay(p.team),
  }));

  const approvers = (r?.approvalLines ?? []).map((al) => ({
    userId: al.approverUserId,
    name: al.approverName,
    email: al.approverEmail ?? '',
    department: toDeptTeamDisplay(al.department),
    team: toDeptTeamDisplay(al.team),
    approvalRole: al.approvalRole,
    approvalType: getApprovalRoleLabel(al.approvalRole),
    isDefault: al.approvalRole === 3,
    approvalStatus: al.approvalStatus,
    rejected: !!al.rejectedAt,
    stateText: al.rejectedAt ? 
      RECEIPT_STATUS_LABELS.REJECTED : (al.approvalStatus ? RECEIPT_STATUS_LABELS.APPROVED : RECEIPT_STATUS_LABELS.WAITING),
    rejectedReason: al.rejectedReason ?? '',
  }));

  const amountNum = Number(String(r?.amount ?? 0).replace(/\D/g, '')) || 0;
  const rr = extractFirstRejectedReason(r?.approvalLines);
  const form = {
    date: r?.submissionDate ?? '',
    categoryId: r?.category?.categoryId ?? null,
    amount: amountNum ? String(amountNum) : '',
    reason: r?.reason ?? '',
    participants,
    approvers,
  };
  const originalFile = {
    name: r?.attachment?.fileName || r?.attachment?.fileOriginName || '',
    url: r?.attachment?.fileUrl || '',
  };
  return { form, originalFile, rejectedReason: rr };
}

/* -------------------------------------------------------------------
 *  Utility: convert display '미지정' -> null for API
 * ------------------------------------------------------------------*/
const toEmpty = toDeptTeamPayload;

/* ────────────────────────────── ⑪  저장 / 취소 ──────────────────────── */
const diffForPatch = ref(null);

function openConfirmationModal () {
  const diffFD = buildDiffFD();
  if (![...diffFD.keys()].length) return toast.info(t('common.message.noData'));

  const approverCnt = formData.value.approvers
    .filter(a => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]).length
  if (approverCnt === 0)
    return toast.warning(t('receipt.common.approversList') + ' ' + t('common.message.required'))
  if (approverCnt > 3)
    return toast.warning(t('receipt.common.approversList') + ' ' + t('common.message.required'))

  if (diffFD.has('date')       && !formData.value.date)       return toast.warning(t('receipt.common.issueDate') + ' ' + t('common.message.required'));
  if (diffFD.has('categoryId') && !formData.value.categoryId) return toast.warning(t('receipt.submission.category') + ' ' + t('common.message.required'));
  if (diffFD.has('amount')     && !formData.value.amount)     return toast.warning(t('receipt.common.amount') + ' ' + t('common.message.required'));
  if (diffFD.has('reason')     && !formData.value.reason)     return toast.warning(t('receipt.common.reason') + ' ' + t('common.message.required'));
  if (diffFD.has('receiptFile') &&
      !(receiptFile.value.type||'').startsWith('image/'))     return toast.warning(t('receipt.common.receiptPhoto') + ' ' + t('common.message.required'));

  diffForPatch.value = diffFD;
  confirmationModalVisible.value = true;
}

async function updateReceipt () {
  confirmationModalVisible.value=false;
  if (!diffForPatch.value) { innerVisible.value=false; return; }

  try {
    await ReceiptApi.patchReceipt(
      authStore.getUserId, 
      (loadedReceipt.value?.receiptId ?? props.receiptId),
      diffForPatch.value
    );
    toast.success(t('common.message.updated'));
    emit('confirm');
  } catch (e) {
    console.error(e); toast.error(t('common.message.serverError'));
  } finally {
    diffForPatch.value=null; innerVisible.value=false;
  }
}
function closeModal () { innerVisible.value=false; if (history.state?.modal) history.back(); }

/* ───────────── 신청 / 삭제 helpers ───────────── */
function requestApply() {
    // 폼에 수정된 내용이 있거나 카테고리가 비활성이면 신청을 막는 로직은 그대로 유지합니다.
    if (isApplyButtonDisabled.value) {
        toast.warning(applyButtonTooltip.value);
        return;
    }
    // 부모에게 'request-apply' 이벤트를 발생시키고, 최소 데이터 전달
    emit('request-apply', {
      receiptId: (loadedReceipt.value?.receiptId ?? props.receiptId),
      approvers: formData.value.approvers,
      isCategoryEnabled: !!selectedCategory.value?.enabled,
    });
}
function openDeleteModal () { deleteModalVisible.value = true }

async function confirmDelete () {
  try {
    await ReceiptApi.deleteReceipt(loadedReceipt.value?.receiptId ?? props.receiptId)
    toast.success(t('common.message.deleted'))
    emit('confirm')
    innerVisible.value = false
  } catch (e) {
    console.error(e)
    toast.error(t('common.message.serverError'))
  } finally {
    deleteModalVisible.value = false
  }
}

/* ──────────────────────────────── 금액 입력 필터링 및 포맷팅 ─────────────────── */
watch(() => formData.value.amount, (newValue, oldValue) => {
    // 값이 없거나 문자열이 아니면 중단
    if (typeof newValue !== 'string' || !newValue) {
        return;
    }
    // 1. 숫자 이외의 모든 문자(쉼표 포함)를 제거합니다.
    const numericValue = newValue.replace(/\D/g, '');

    // 2. 숫자가 아닌 값을 모두 지워서 빈 문자열이 되었다면, formData.amount를 ''로 설정합니다.
    if (numericValue === '') {
        if (formData.value.amount !== '') {
            formData.value.amount = '';
        }
        return;
    }
    // 3. 숫자로 변환 후, 세 자리마다 쉼표(,)를 포함한 문자열로 포맷팅합니다.
    const formattedValue = Number(numericValue).toLocaleString('ko-KR');

    // 4. 현재 입력 값과 포맷팅된 값이 다를 경우에만 값을 업데이트합니다. (무한 루프 방지)
    if (formattedValue !== newValue) {
        formData.value.amount = formattedValue;
    }
});

/* ────────────────────────────── ⑫  Lifecycle & Watch ────────────────── */
onMounted(async () => {
  // 카테고리 로딩
  await loadCategories()
})

// 모달 표시 시점에 ID가 있으면 단건 조회하여 폼 바인딩
watch(() => props.isVisible, async (v) => {
  innerVisible.value = v;
  if (!v) return;
  try {
    await loadCategories();
    if (props.receiptId) {
      const { data } = await ReceiptApi.getReceiptById(props.receiptId);
      loadedReceipt.value = data;
      currentReceiptId.value = data?.receiptId ?? props.receiptId;
      // 단건 원본 -> 편집 폼 매핑
      const mapped = mapRawToForm(data);
      formData.value = mapped.form;
      originalFormData.value = JSON.parse(JSON.stringify(formData.value));
      originalFile.value = mapped.originalFile;
      previewUrl.value = originalFile.value.url ? getImagePreviewUrl(originalFile.value.url, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : null;
      rejectedReason.value = mapped.rejectedReason || extractFirstRejectedReason(data?.approvalLines);
      // 고정 상태 초기화
      receiptFile.value = null;
    }
  } catch (e) {
    console.error(e);
  }
});

watch(()=>props.isVisible, v=> innerVisible.value=v);
watch(innerVisible, v=>{
  if (!v) {
    showExternal.value = false;
    externalParticipant.value = { company:'', name:'', position:'', phone:'' };
    emit('close');
  }
});

/* ─────────────────────── 변경 여부 감지 (isFormDirty) ───────────────────── */
const isFormDirty = computed(() => {
  // 초기 데이터가 없으면 변경된 것으로 간주하지 않음
  if (!originalFormData.value) {
    return false;
  }
  const orig = originalFormData.value;
  const curr = formData.value;

  // 1. 단순 값 비교 (날짜, 카테고리, 금액, 사유)
  // 금액 비교
  const origAmount = String(orig.amount || '').replace(/\D/g, '');
  const currAmount = String(curr.amount || '').replace(/\D/g, '');
  if (origAmount !== currAmount) return true;
  // 금액을 제외한 나머지 항목 비교
  if (
    orig.date !== curr.date ||
    Number(orig.categoryId) !== Number(curr.categoryId) ||
    orig.reason !== curr.reason
  ) {
    return true;
  }

  // 2. 참여자 목록 비교 (순서 무관)
  // - 참여자는 순서가 중요하지 않으므로, ID만 뽑아 정렬 후 비교
  const origParticipantIds = (orig.participants || []).map(p => p.userId).sort();
  const currParticipantIds = (curr.participants || []).map(p => p.userId).sort();
  if (JSON.stringify(origParticipantIds) !== JSON.stringify(currParticipantIds)) {
    return true;
  }
  // 3. 결재자 목록 비교 (순서 중요!)
  // - 결재자는 순서가 중요하므로, 각 항목의 핵심 데이터(userId, approvalType)를 문자열로 합쳐서 전체를 비교
  const origApproverSignature = (orig.approvers || []).map(a => `${a.userId}-${a.approvalType}`).join(',');
  const currApproverSignature = (curr.approvers || []).map(a => `${a.userId}-${a.approvalType}`).join(',');
  if (origApproverSignature !== currApproverSignature) {
    return true;
  }
  // 4. 파일 변경 여부 확인
  if (receiptFile.value !== null) {
    return true;
  }
  // 위 모든 검사를 통과하면 변경되지 않은 것
  return false;
});

/* ────────────────────────────── ⑬  기타 Helpers ─────────────────────── */
const isRejected = computed(() => {
  const st = (loadedReceipt.value?.status || loadedReceipt.value?.statusText || '')
    .toString()
    .toUpperCase();
  if (st === 'REJECTED') return true;
  if (loadedReceipt.value?.lastRejectionDate) return true;
  const lines = loadedReceipt.value?.approvalLines || [];
  return Array.isArray(lines) && lines.some(l => l?.rejectedAt || l?.rejectedReason);
});

// '신청' 버튼의 비활성화 상태를 결정하는 computed 속성
const isApplyButtonDisabled = computed(() => {
  // Number()를 사용해 타입을 통일합니다.
  const currentCategory = categories.value.find(
    (c) => Number(c.categoryId) === Number(formData.value.categoryId)
  );
  // 선택된 카테고리가 없거나 비활성 상태이면 버튼을 비활성화합니다.
  if (!currentCategory || !currentCategory.enabled) {
    return true;
  }
  // 폼 내용이 수정되었다면 버튼을 비활성화합니다. (수정 후 저장 필요)
  if (isFormDirty.value) {
    return true;
  }
  // 위의 모든 조건에 해당하지 않으면 버튼을 활성화합니다.
  return false;
});

// '신청' 버튼의 툴팁 메시지를 결정하는 computed 속성
const applyButtonTooltip = computed(() => {
  // Number()를 사용해 타입을 통일합니다.
  const currentCategory = categories.value.find(
    (c) => Number(c.categoryId) === Number(formData.value.categoryId)
  );
  if (!currentCategory || !currentCategory.enabled) {
    return t('receipt.submission.category') + ' ' + t('common.message.required');
  }
  if (isFormDirty.value) {
    return t('common.message.required');
  }
  return t('receipt.submission.apply');
});

// 카테고리 Select 박스에 적용할 동적 클래스
const categorySelectClass = computed(() => {
  // selectedCategory가 존재하고, 비활성화 상태일 때 CSS 클래스를 반환합니다.
  if (selectedCategory.value && !selectedCategory.value.enabled) {
    return 'inactive-category-select';
  }
  return ''; // 그 외의 경우에는 아무 클래스도 적용하지 않습니다.
});
</script>

<style scoped>
hr {
  margin: 0 0 7px;
  border: none; /* ← 기본 border 제거 */
  height: 1px; /* 실제 두께 */
  background: #000000;
}
.search-wrapper-hr-1 {
  margin: 0px 0 10px 0;
}
.search-wrapper-hr-2 {
  margin: 15px 0 10px 0;
}
.form-row .col {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.modal-body {
  padding: 15px 0px 10px 20px !important;
  overflow-y: none; /* 데스크탑/모바일 모두 스크롤 영역 항상 표시 */
}

.receipt-modal .modal-content > .modal-body {
  max-height: 80vh;
  overflow-y: scroll; /* 모달 내부 스크롤 고정 */
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

/* 이미지 제거 버튼 텍스트 흰색, 배경 빨강 (다크 테마 포함) */
.btn-danger.square-btn {
  color: #ffffff !important;
  font-weight: 500 !important;
  font-size: 0.6rem !important;
  line-height: 1 !important;
  background-color: #dc3545 !important;
  border-color: #dc3545 !important;
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
  color: #dc3545 !important;
  background: #dc354510 !important;
  font-weight: 900;
}

/* 합의 (active) : 초록 */
.approval-concurrence.active {
  color: #198754 !important;
  background: #1987542b !important;
  font-weight: 900;
}

.approval-option.disabled {
  pointer-events: none; /* 클릭 차단 */
  opacity: 0.4; /* 흐리게 표시 */
}
/* 외부 참여자 영역 연회색 배경 적용 */
.external-section { margin-top: 10px; }
/* slide-fast transition */
.slide-fast-enter-active,
.slide-fast-leave-active {
  transition: all 160ms ease-out;
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
.preview-modal{
  position: fixed; inset: 0;
  background: rgba(0,0,0,.7);
  display: flex; justify-content: center; align-items: center;
  z-index: 9999;
}
.preview-modal-content{ position: relative; }
.preview-modal-image{
  max-width: 80vw; max-height: 80vh;
  transition: transform .2s ease;
}

:deep(.inactive-category-select select) {
  background-color: #fff2f2 !important; /* 연한 빨강 배경 */
  border-color: #ffc2c2 !important;    /* 살짝 더 진한 빨강 테두리 */
  color: #d9534f !important;           /* 텍스트도 붉은 계열로 변경 */
  font-weight: bold;
}
@media (max-width: 991px) {
  .modal-body {
    padding: 20px 25px 30px 25px !important;
  }
  .preview-pane {          /* 왼쪽 이미지 영역 숨김 */
    display: none;
  }
  .form-pane {
    padding-top: 0;
  }
  .v-divider {             /* 가운데 세로선 제거 */
    display: none;
  }
  .form-pane {             /* 오른쪽(폼) 영역을 100%로 확장 */
    flex: 1 1 100%;
    max-width: 100%;
    padding: 0px;
  }
}

/* -------- Desktop(>1300px): 우측 폼 패널만 스크롤, 모달 본문은 고정 -------- */
@media (min-width: 992px) {
  .receipt-modal .modal-content > .modal-body { overflow-y: hidden; }
  .form-pane { overflow-y: auto; max-height: 80vh; }
}

/* -------- Tablet/Mobile(<=991px): 왼쪽 패널 사라지면 모달 본문 스크롤 -------- */
@media (max-width: 991px) {
  .receipt-modal .modal-content > .modal-body { overflow-y: scroll; }
  .form-pane { overflow: visible; max-height: none; }
}

@media (max-width: 650px) {
  .modal-body {
    flex-direction: column;
    max-height: 70vh;
    overflow-y: scroll;
    padding: 15px 20px !important;
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

@media (max-width: 400px) {
  .list-group-item {
    padding: 8px 10px;
  }
}
</style>
