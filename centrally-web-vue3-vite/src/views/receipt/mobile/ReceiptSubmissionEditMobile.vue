<template>
  <div>
    <div class="content content-wrapper">
      <MobileDetailTitle :title="$t('receipt.submission.editTitle')" />
      <!-- <p class="content-mobile-detail-sub-title">모바일 전용 수정 페이지입니다</p> -->

      <form
        class="form-pane modal-body"
        @submit.prevent="openConfirmationModal"
        @keydown.enter.prevent
      >
        <!-- 이미지 등록 -->
        <div class="form-group cs-card--blue">
          <MobileMintLabel
            :text="$t('receipt.common.receiptPhoto')"
            forId="receiptImage"
            size="small"
            marginBottom="5px"
            :required="true"
          />
          <div class="thumb-inline">
            <div
              class="thumb"
              role="button"
              @click="onThumbClick"
              :aria-label="receiptFile?.name || originalFile.name"
            >
              <template v-if="receiptFile && receiptFileUrl">
                <img :src="receiptFileUrl" :alt="receiptFile.name || $t('receipt.common.receiptPhoto')" class="thumb__img" />
                <div class="thumb__overlay">
                  <i class="ri-search-eye-line thumb__icon"></i>
                </div>
              </template>
              <template v-else-if="originalFile && originalFile.url">
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
            <div class="file-select-area" @click="onSelectAreaClick">
              <div class="file-info-box">
                <div class="file-info-icon">
                  <i class="ri-image-add-line"></i>
                </div>
                <div class="file-info-text">
                  <span class="file-info-title">{{ $t('receipt.common.receiptPhoto') }}</span>
                  <span class="file-info-desc">JPG, PNG</span>
                </div>
              </div>
              <input
                ref="hiddenFileInput"
                type="file"
                id="receiptImage"
                accept="image/*"
                style="display:none"
                @change="handleFileChange"
              />
              <div v-if="receiptFile" class="file-name-display" @click.stop>
                <span class="file-name">{{ $t('receipt.submission.newFilePrefix') }} {{ receiptFile.name }}</span>
                <button
                  type="button"
                  class="btn btn-danger square-btn"
                  @click.stop="removeFile"
                >
                  ×
                </button>
              </div>
              <div v-else-if="originalFile.name" class="file-name-display" @click.stop>
                <span class="file-name">{{ $t('receipt.submission.existingFilePrefix') }} {{ originalFile.name }}</span>
              </div>
              <div v-else class="file-name-display" @click.stop>
                <span class="file-name">{{ $t('receipt.submission.noImageSelected') }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 발행일 -->
        <div class="form-group cs-card--blue">
          <DefaultFormRow marginBottom="5px">
            <MobileMintLabel
              :text="$t('receipt.submission.dueDate')"
              forId="date"
              size="small"
              :required="true"
            />
            <DefaultSmallButton variant="secondary" @click="setToday">
              {{ $t('receipt.submission.now') }}
            </DefaultSmallButton>
          </DefaultFormRow>
          <MobileMintTextfield
            type="date"
            id="date"
            v-model="formData.date"
            size="full"
            :required="true"
          />
        </div>

        <hr class="search-wrapper-hr-2" />

        <!-- 참여자 검색 + 외부 + 참여자 목록 (컨테이너) -->
        <div class="form-group participants-group cs-card--blue">
          <div class="search-wrapper">
          <div class="d-flex align-items-center">
            <MobileMintLabel
              :text="$t('receipt.submission.participantSearch')"
              forId="participantSearch"
              size="small"
              marginBottom="5px"
            />
            <MobileMintSmallButton variant="secondary" marginBottom="5px" @click="showExternal = !showExternal">{{ $t('receipt.common.external') }}</MobileMintSmallButton>
          </div>
          <MobileMintUserSearchDropdown
            ref="participantSearchRef"
            :labelText="$t('hrm.userManagement.search')"
            inputId="participantSearch"
            inputSize="full"
            :keepSearchValue="false"
            :placeholder="$t('hrm.userManagement.namePlaceholder')"
            @userSelected="onParticipantSelected"
          />
          </div>

        <transition name="slide-fast">
          <div v-if="showExternal" class="external-section cs-card--blue">
            <MobileMintLabel
              :text="$t('receipt.submission.externalParticipantAdd')"
              size="small"
              marginBottom="5px"
              marginTop="10px"
            />
            <DefaultFormRow gap="5px" marginBottom="5px">
              <MobileMintTextfield
                v-model="externalParticipant.name"
                :placeholder="$t('hrm.userManagement.namePlaceholder')"
                size="full"
                style="width: 100%"
              />
              <MobileMintTextfield
                v-model="externalParticipant.phone"
                :placeholder="$t('hrm.userInfo.mobile')"
                size="full"
                style="width: 100%"
              />
            </DefaultFormRow>
            <DefaultFormRow gap="5px">
              <MobileMintTextfield
                v-model="externalParticipant.company"
                :placeholder="$t('receipt.common.company')"
                size="full"
                style="width: 100%"
              />
              <MobileMintTextfield
                v-model="externalParticipant.position"
                :placeholder="$t('receipt.common.position')"
                size="full"
                style="width: 100%"
              />
            </DefaultFormRow>
            <DefaultFormRow align="right" marginTop="5px">
              <MobileMintButton
                color="blue"
                @click="addExternalParticipant"
                customWidth="60px"
                customHeight="25px"
              >
                {{ $t('receipt.meta.add') }}
              </MobileMintButton>
            </DefaultFormRow>
          </div>
        </transition>

        <!-- 참여자 목록 -->
        <div v-if="formData.participants.length > 0">
          <MobileMintLabel
            :text="$t('receipt.common.participantsList')"
            marginBottom="5px"
            marginTop="15px"
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
        </div>

        <hr class="search-wrapper-hr-2" />

        <!-- 카테고리 | 금액 -->
          <div class="form-row align-items-center">
          <div class="col">
            <MobileMintLabel
              class="category-label"
              :text="$t('receipt.meta.category')"
              forId="categorySelect"
              size="small"
              marginBottom="5px"
              :required="true"
            />
            <MobileMintSelect
              v-model="formData.categoryId"
              :options="categoryOptions"
              :placeholder="$t('receipt.submission.categorySelect')"
              size="full"
              style="width: 100%"
              :reserveErrorSpace="true"
              marginBottom="22px"
              :class="categorySelectClass"
            />
          </div>
          <div class="col">
            <MobileMintLabel
              :text="amountLabel"
              forId="amount"
              size="small"
              marginBottom="5px"
              :required="true"
            />
            <MobileMintTextfield
              type="text"
              id="amount"
              v-model="formData.amount"
              size="full"
              style="width: 100%"
              validationType="money"
              :moneyMax="maxAllowed"
              :disabled="!selectedCategory"
              :reserveErrorSpace="true"
              :placeholder="$t('receipt.submission.amountPlaceholder')"
            />
          </div>
        </div>

        <hr class="search-wrapper-hr-1" />

        <!-- 반려 사유 표시 -->
        <div v-if="isRejected && rejectedReason" class="form-group cs-card--blue cs-card--white">
          <MobileMintLabel marginBottom="5px" :text="$t('receipt.common.rejectReason')" size="small" />
          <MobileMintTextfield type="text" :modelValue="rejectedReason" size="full" :disabled="true" bg-color="#fff0f0" />
        </div>

        <!-- 결재(합의)자 검색 + 즐겨찾기 + 결재자 목록 (컨테이너) -->
        <div class="form-group approvers-group cs-card--blue">
          <div class="search-wrapper">
          <MobileMintLabel
            :text="$t('receipt.submission.approverSearchWithRole')"
            forId="approverSearch"
            size="small"
            marginBottom="5px"
          />
          <MobileMintUserSearchDropdown
            ref="approverSearchRef"
            :labelText="$t('receipt.submission.approverSearch')"
            inputId="approverSearch"
            inputSize="full"
            :keepSearchValue="false"
            :filterBy="{
              roleDetails: [
                'ROLE_RECEIPT_APPROVER',
                'ROLE_RECEIPT_INSPECTOR',
                'ROLE_RECEIPT_MANAGER',
                'ROLE_GATE_SYSTEM',
              ],
            }"
            :placeholder="$t('receipt.submission.approverSearchPlaceholder')"
            @userSelected="onApproverSelected"
          />
          </div>

        <!-- 결재자 목록 -->
        <div v-if="normalApprovers.length > 0">
          <MobileMintLabel
            :text="$t('receipt.submission.approversListReorder')"
            marginBottom="5px"
            marginTop="15px"
            size="small"
          />
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
                  <span class="drag-handle me-2" :title="$t('receipt.submission.dragToReorder')">≡</span>
                  <IconBadge
                    v-if="element.isDefault"
                    class="fixed-badge"
                    color="primary"
                  >{{ $t('receipt.common.fixed') }}</IconBadge>
                  <span
                    :class="[
                      'approval-option',
                      'approval-decision',
                      { active: element.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER], disabled: element.isDefault },
                    ]"
                    @click="!element.isDefault && setApprovalType(element, APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER])"
                  >{{ $t('receipt.submission.approverToggle.approveShort') }}</span
                  >
                  <span
                    :class="[
                      'approval-option',
                      'approval-concurrence',
                      'approval-option-right',
                      {
                        active: element.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE],
                        disabled: element.isDefault || isSameDepartment(element),
                      },
                    ]"
                    @click="!element.isDefault && onConcurrenceClick(element)"
                  >{{ $t('receipt.submission.approverToggle.concurShort') }}</span
                  >
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
              class="list-group-item d-flex align-items-center flex-wrap"
            >
              <span class="d-flex align-items-center flex-wrap">
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
              </span>
            </li>
          </ul>
        </div>
        </div>

        <hr class="search-wrapper-hr-2" />

        <!-- 사유 -->
        <div class="form-group reason cs-card--blue">
          <MobileMintLabel marginBottom="5px" :text="$t('receipt.common.reason')" forId="reason" size="small" :required="true" />
          <MobileMintTextfield
            type="text"
            id="reason"
            size="full"
            v-model="formData.reason"
            :placeholder="$t('receipt.submission.reasonPlaceholder')"
          />
        </div>

        <DefaultFormRow align="right" marginTop="5px">
          <MobileMintButton align="right" color="gray" marginRight="5px" @click="goBack">{{ $t('common.button.cancel') }}</MobileMintButton>
          <MobileMintButton type="submit" align="right">{{ $t('receipt.submission.modify') }}</MobileMintButton>
          <span v-tooltip.bottom="applyButtonTooltip">
            <MobileMintButton
              color="blue"
              align="right"
              marginLeft="5px"
              @click="requestApply"
              :disabled="isApplyButtonDisabled"
            >
              {{ $t('receipt.submission.apply') }}
            </MobileMintButton>
          </span>
          <MobileMintButton
            color="red"
            align="right"
            marginLeft="5px"
            @click="openDeleteModal"
          >
            {{ $t('common.button.delete') }}
          </MobileMintButton>
        </DefaultFormRow>
      </form>
    </div>
  </div>
  
  <!-- 이미지 미리보기: 공통 컴포넌트 -->
  <ImagePreviewModal ref="imagePreviewRef" />

  <!-- 확인 모달 -->
  <AlertModal
    :isVisible="confirmationModalVisible"
    :title="$t('receipt.submission.saveConfirm')"
    @close="confirmationModalVisible = false"
    @confirm="updateReceipt"
    :cancelText="$t('common.button.cancel')"
    :confirmText="$t('common.button.confirm')"
  >
    <template #body>
      {{ $t('receipt.submission.modifyConfirm') }}
    </template>
  </AlertModal>
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
  <AlertModal
    :isVisible="deleteModalVisible"
    :confirmText="$t('common.button.delete')"
    :cancelText="$t('common.button.cancel')"
    @close="deleteModalVisible = false"
    @confirm="confirmDelete"
  >
    <template #body>
      <div style="color: red">※ {{ $t('receipt.management.deleteMessage') }}</div>
    </template>
  </AlertModal>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ROUTES } from '@/config/menuConfig';
import { toast } from 'vue3-toastify';
import { useToastStore } from '@/store/toast';
import { useAuthStore } from '@/store/auth';

const { t } = useI18n();
import UsersApi from '@/api/hrm/UsersApi';
import CategoryApi from '@/api/receipt/ReceiptsCategoryApi.js';
import ReceiptApi from '@/api/receipt/ReceiptsApi.js';
import ReceiptsApi from '@/api/receipt/ReceiptsApi.js';
import ReceiptsRequestApi from '@/api/receipt/ReceiptsRequestApi';
import draggable from 'vuedraggable';

// 공통 컴포넌트
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME } from '@/constants';
import DefaultSmallButton       from '@/components/common/button/DefaultSmallButton.vue'
import MobileDetailTitle from '@/components/common/title/MobileDetailTitle.vue';
import MobileMintButton from '@/components/common/button/MobileMintButton.vue';
import MobileMintLabel from '@/components/common/label/MobileMintLabel.vue';
import MobileMintTextfield from '@/components/common/textfield/MobileMintTextfield.vue';
import MobileMintSelect from '@/components/common/select/MobileMintSelect.vue';
import MobileMintSmallButton from '@/components/common/button/MobileMintSmallButton.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import IconBadge from '@/components/common/badge/IconBadge.vue';
import MobileMintUserSearchDropdown from '@/components/auth/MobileMintUserSearchDropdown.vue';

import { processImageFile } from '@/utils/imageProcessor.js';
import { extractFirstRejectedReason, computeTotalLimit, parseAmountNumber, formatAndClampAmount, enforceMoneyKeydown, formatClampFromClipboard } from '@/utils/receipt/receiptUtils.js';
import { toDeptTeamDisplay, toDeptTeamPayload } from '@/utils/blankFormat.js';
import ImagePreviewModal from '@/components/common/image/ImagePreviewModal.vue';
import { APPROVAL_ROLE, APPROVAL_ROLE_LABELS, RECEIPT_STATUS_LABELS, getApprovalRoleLabel } from '@/constants/receiptConstants';

const router = useRouter();
const auth = useAuthStore();
const toastStore = useToastStore();

const currentDeptId = ref(null);
const currentDeptName = ref('');
const currentTeamId = ref(null);
const currentTeamName = ref('');

const confirmationModalVisible = ref(false);
const applyModalVisible = ref(false);
const deleteModalVisible = ref(false);
const formData = ref({
  date: '',
  categoryId: '',
  amount: '',
  reason: '',
  participants: [],
  approvers: [],
});
const receiptFile = ref(null);
const receiptFileUrl = ref(null);
const hiddenFileInput = ref(null);
const originalFile = ref({ name: '', url: '' });
const originalFormData = ref(null);

const externalParticipant = ref({ company: '', name: '', position: '', phone: '' });
const showExternal = ref(false);
const participantSearchRef = ref(null);
const approverSearchRef = ref(null);

/**
 * 조직 텍스트를 모바일 표시에 맞춰 정제한다.
 * @param {string} value
 * @returns {string}
 */
function sanitizeOrgText(value) {
  const text = (value ?? '').toString().trim();
  if (!text || text === '-' || text === '--') return '';
  return text;
}

/**
 * 부서와 팀 정보를 한 줄로 요약한다.
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

const categories = ref([]);
const categoryOptions = computed(() => {
  const list = categories.value || [];
  // 기본: 활성 카테고리만 노출
  const opts = list
    .filter((c) => c.enabled)
    .map((c) => ({ value: c.categoryId, label: c.categoryName }));
  // 현재 선택된 카테고리가 비활성이라면, 옵션 목록에 [비활성] 라벨로 강제 포함
  const currentId = Number(formData.value.categoryId || NaN);
  const current = list.find((c) => Number(c.categoryId) === currentId);
  if (current && current.enabled === false && !opts.some(o => Number(o.value) === Number(current.categoryId))) {
    opts.unshift({ value: current.categoryId, label: `[비활성] ${current.categoryName}` });
  }
  return opts;
});
const selectedCategory = computed(() => {
  const id = Number(formData.value.categoryId || NaN);
  return categories.value.find((c) => Number(c.categoryId) === id);
});
const maxAllowed = computed(() => !selectedCategory.value
  ? Infinity
  : computeTotalLimit(selectedCategory.value.limitPrice, formData.value.participants.length)
);
const amountLabel = computed(() => {
  if (!selectedCategory.value) return t('receipt.common.amount');
  const total =
    selectedCategory.value.limitPrice * (formData.value.participants.length + 1);
  return t('receipt.submission.amountWithLimit', {
    limit: total.toLocaleString(),
  });
});

const isRejected = computed(() => {
  const st = (receipt.value?.status || receipt.value?.statusText || '')
    .toString()
    .toUpperCase();
  if (st === 'REJECTED') return true;
  if (receipt.value?.lastRejectionDate) return true;
  const lines = receipt.value?.approvalLines || [];
  return Array.isArray(lines) && lines.some(l => l?.rejectedAt || l?.rejectedReason);
});
const receipt = ref(null);
const rejectedReason = ref('');

/**
 * 단건 조회 결과를 편집 화면 폼 구조로 매핑
 * @param {object} r 백엔드 영수증 원본
 */
function populateFromRaw(r) {
  if (!r) return;
  const peopleArr = (r.participantsList ?? []).map((p) => ({
    // 외부 참여자만 명시적으로 표시, 내부자는 undefined 유지
    type: p.participantType === 'EXTERNAL' ? 'EXTERNAL' : undefined,
    userId: p.userId ?? p.participantUserId ?? null,
    name: p.participantName,
    company: p.company ?? null,
    position: p.position ?? null,
    phone: p.phone ?? null,
    department: toDeptTeamDisplay(p.department),
    team: toDeptTeamDisplay(p.team),
  }));

  const approverArr = (r.approvalLines ?? []).map((al) => ({
    userId: al.approverUserId ?? al.userId,
    name: al.delegateMapping
      ? '[대리] ' + (al.delegateMapping.delegateName || al.approverName || al.name)
      : (al.approverName || al.name),
    department: toDeptTeamDisplay(
      al.delegateMapping ? al.delegateMapping.delegateDepartment : al.department,
    ),
    team: toDeptTeamDisplay(
      al.delegateMapping ? al.delegateMapping.delegateTeam : al.team,
    ),
    approvalRole: al.approvalRole,
    approvalType: getApprovalRoleLabel(al.approvalRole),
    isDefault: al.approvalRole === 3,
    email: al.approverEmail ?? al.email ?? '',
  }));

  const rawAmountNum = Number(String(r.amount ?? '').replace(/\D/g, '')) || 0;
  originalFile.value = {
    name: r.attachment?.fileName || '',
    url: r.attachment?.fileUrl ? getImagePreviewUrl(r.attachment.fileUrl, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : '',
  };
  formData.value = {
    date: r.submissionDate ?? r.date ?? '',
    categoryId: r.category?.categoryId ?? r.categoryId ?? '',
    amount: rawAmountNum ? String(rawAmountNum) : '',
    reason: r.reason ?? '',
    participants: peopleArr,
    approvers: approverArr,
  };
  // 반려 사유
  rejectedReason.value = extractFirstRejectedReason(r.approvalLines);
}

/**
 * receiptId 기준 단건 조회 실행 후 상태 반영
 * @param {number|string} id 영수증 ID
 */
async function loadById(id) {
  const { data } = await ReceiptsApi.getReceiptById(id);
  receipt.value = data;
  populateFromRaw(data);
  // 현재 값을 스냅샷하여 변경 여부 비교용으로 저장
  originalFormData.value = JSON.parse(JSON.stringify(formData.value));
  // 선택 이미지 초기화
  receiptFile.value = null;
  receiptFileUrl.value = null;
}

// 이미지 미리보기 공통 컴포넌트 참조
const imagePreviewRef = ref(null);
function openPreviewModal(url) {
  if (!url) return;
  imagePreviewRef?.value?.open(url);
}

/** 이미지 파일 선택 처리 */
async function handleFileChange(e) {
  const file = e.target.files[0] || null;
  if (!file) {
    receiptFile.value = null;
    receiptFileUrl.value = null;
    return;
  }
  try {
    receiptFile.value = await processImageFile(file, {
      maxSizeMB: 2,
      maxWidthOrHeight: 2048,
    });
    // 미리보기 URL 생성
    receiptFileUrl.value = URL.createObjectURL(receiptFile.value);
    // 동일 파일 재선택 허용을 위해 입력값 초기화
    if (e && e.target) e.target.value = '';
  } catch (err) {
    if (err.message === 'FILE_SIZE_EXCEEDED')
      toast.error(t('receipt.submission.imageSizeExceeded'));
    else toast.error(t('receipt.submission.imageProcessError'));
    receiptFile.value = null;
    receiptFileUrl.value = null;
    if (e && e.target) e.target.value = '';
    return;
  }
}
/** 선택 이미지 제거 */
function removeFile() {
  if (receiptFileUrl.value) {
    URL.revokeObjectURL(receiptFileUrl.value);
  }
  receiptFile.value = null;
  receiptFileUrl.value = null;
  // 동일 파일을 다시 선택할 수 있도록 파일 입력값을 비웁니다
  if (hiddenFileInput.value) hiddenFileInput.value.value = '';
}

/**
 * 썸네일 클릭 → 미리보기 모달 오픈
 */
function onThumbClick() {
  const url = receiptFileUrl.value || (originalFile.value?.url ? getImagePreviewUrl(originalFile.value.url, RECEIPT_API_BASE_URL, RECEIPT_SERVICE_NAME) : null);
  if (url) openPreviewModal(url);
}

/**
 * 파일 선택 영역 클릭 → 파일 선택창 오픈
 */
function onSelectAreaClick() {
  if (hiddenFileInput.value) {
    hiddenFileInput.value.click();
    return;
  }
  const el = document.getElementById('receiptImage');
  if (el && el.click) el.click();
}
/** 오늘 날짜 설정 */
function setToday() {
  const t = new Date();
  formData.value.date = `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(
    2,
    '0',
  )}-${String(t.getDate()).padStart(2, '0')}`;
}

/** 내부 참여자 선택 */
function onParticipantSelected(u) {
  if (formData.value.participants.some((p) => p.userId === u.userId))
    return toast.warning(t('receipt.submission.participantAlreadyAdded'));
  formData.value.participants.push({
    userId: u.userId,
    name: u.name,
    department: u.department,
    team: u.team,
  });
}
/** 외부 참여자 추가 */
function addExternalParticipant() {
  const p = externalParticipant.value;
  if (!p.name || !p.phone)
    return toast.warning(t('receipt.submission.externalParticipantRequired'));
  const dup = formData.value.participants.some(
    (x) => x.type === 'EXTERNAL' && x.name === p.name && x.phone === p.phone,
  );
  if (dup) return toast.warning(t('receipt.submission.externalParticipantAlreadyAdded'));
  formData.value.participants.push({
    type: 'EXTERNAL',
    userId: null,
    name: p.name,
    company: p.company,
    position: p.position,
    phone: p.phone,
    department: '',
    team: '',
  });
  externalParticipant.value = { company: '', name: '', position: '', phone: '' };
}
/** 참여자 삭제 */
function removeParticipant(idx) {
  formData.value.participants.splice(idx, 1);
}

/** 결재자 선택 */
function onApproverSelected(u) {
  if (formData.value.approvers.some((a) => a.userId === u.userId))
    return toast.warning(t('receipt.submission.approverAlreadyAdded'));
  const decisionCnt = formData.value.approvers.filter(
    (a) => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER],
  ).length;
  if (decisionCnt >= 3) return toast.warning(t('receipt.submission.approverMaxLimit'));
  formData.value.approvers.push({
    userId: u.userId,
    name: u.name,
    email: u.email,
    department: u.department,
    team: u.team,
    approvalType: APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER],
    approvalRole: APPROVAL_ROLE.APPROVER,
  });
}
/** 결재/합의 전환 */
function setApprovalType(u, type) {
  if (u.isDefault) return;
  if (type === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE] && isSameDepartment(u)) return;
  if (type === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]) {
    const cnt = formData.value.approvers.filter(
      (a) => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER] && a !== u,
    ).length;
    if (cnt >= 3) return toast.warning(t('receipt.submission.approverMaxLimitSet'));
  }
  u.approvalType = type;
  u.approvalRole = u.isDefault ? 3 : (type === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE] ? APPROVAL_ROLE.CONCURRENCE : APPROVAL_ROLE.APPROVER);
}
function onConcurrenceClick(u) {
  if (u.isDefault || isSameDepartment(u)) return;
  setApprovalType(u, APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE]);
}
/** 결재자 삭제 */
function removeApprover(idx) {
  formData.value.approvers.splice(idx, 1);
}

/** 동일 부서 여부 */
function isSameDepartment(u) {
  return (
    (u?.department ?? '').trim().toUpperCase() ===
    currentDeptName.value.trim().toUpperCase()
  );
}

/** 저장 전 확인 */
function openConfirmationModal() {
  const approverCnt = formData.value.approvers.filter(
    (a) => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER],
  ).length;
  if (approverCnt === 0) return toast.warning(t('receipt.submission.approverMinRequired'));
  if (approverCnt > 3)
    return toast.warning(t('receipt.submission.approverMaxLimitSet'));
  if (
    !formData.value.date ||
    !formData.value.categoryId ||
    !formData.value.amount ||
    !formData.value.reason
  )
    return toast.warning(t('receipt.submission.requiredFieldsWarning'));
  if (receiptFile.value && !receiptFile.value.type?.startsWith?.('image/'))
    return toast.warning(t('receipt.submission.imageOnlyWarning'));
  confirmationModalVisible.value = true;
}

/** 수정 요청 */
async function updateReceipt() {
  confirmationModalVisible.value = false;
  const fd = new FormData();
  fd.append('date', formData.value.date);
  fd.append('categoryId', formData.value.categoryId);
  fd.append('amount', String(formData.value.amount).replace(/\D/g, ''));
  fd.append('reason', formData.value.reason);
  fd.append(
    'participants',
    JSON.stringify(
      formData.value.participants.map((p) => ({
        type: p.type || 'INTERNAL',
        userId: p.userId,
        name: p.name,
        company: p.company,
        position: p.position,
        phone: p.phone,
        department: toDeptTeamPayload(p.department),
        team: toDeptTeamPayload(p.team),
      })),
    ),
  );
  fd.append(
    'approvers',
    JSON.stringify(
      formData.value.approvers.map((a) => ({
        userId: a.userId,
        name: a.name,
        email: a.email,
        approvalType: a.approvalType,
        approvalRole: a && a.isDefault ? 3 : (a && a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE] ? APPROVAL_ROLE.CONCURRENCE : APPROVAL_ROLE.APPROVER),
        isDefault: a.isDefault,
        department: toDeptTeamPayload(a.department),
        team: toDeptTeamPayload(a.team),
      })),
    ),
  );
  if (receiptFile.value) fd.append('receiptFile', receiptFile.value);
  await ReceiptApi.patchReceipt(auth.getUserId, receipt.value.receiptId, fd);
  // 공통 토스트: 다음 페이지에서 노출
  toastStore.setNextPageMessage(t('receipt.submission.modifyComplete'), 'success');
  router.replace('/receipt/receipt-submission');
}

/** 신청 버튼 클릭 */
function requestApply() {
  if (isApplyButtonDisabled.value) {
    toast.warning(t('receipt.submission.applyDisabledWarning'));
    return;
  }
  applyModalVisible.value = true;
}

/** 신청 확정 */
async function confirmApply() {
  applyModalVisible.value = false;
  const payload = {
    departmentId: currentDeptId.value,
    departmentName: currentDeptName.value,
    teamId: currentTeamId.value,
    teamName: currentTeamName.value,
  };
  await ReceiptsRequestApi.requestReceipt(receipt.value.receiptId, payload);
  toast.success(t('receipt.submission.applyComplete'));
  router.replace('/receipt/receipt-submission');
}

/** 삭제 모달 열기 */
function openDeleteModal() {
  deleteModalVisible.value = true;
}
/** 삭제 확정 */
async function confirmDelete() {
  deleteModalVisible.value = false;
  await ReceiptApi.deleteReceipt(receipt.value.receiptId);
  toast.success(t('receipt.submission.deleteComplete'));
  router.replace('/receipt/receipt-submission');
}

/** 신청 가능 여부(모달과 동일 로직) */
const isFormDirty = computed(() => {
  if (!originalFormData.value) return false;
  const orig = originalFormData.value;
  const curr = formData.value;
  const origAmount = String(orig.amount || '').replace(/\D/g, '');
  const currAmount = String(curr.amount || '').replace(/\D/g, '');
  if (origAmount !== currAmount) return true;
  if (
    orig.date !== curr.date ||
    Number(orig.categoryId) !== Number(curr.categoryId) ||
    orig.reason !== curr.reason
  )
    return true;
  const origP = (orig.participants || []).map((p) => p.userId).sort();
  const curP = (curr.participants || []).map((p) => p.userId).sort();
  if (JSON.stringify(origP) !== JSON.stringify(curP)) return true;
  const origA = (orig.approvers || [])
    .map((a) => `${a.userId}-${a.approvalType}`)
    .join(',');
  const curA = (curr.approvers || [])
    .map((a) => `${a.userId}-${a.approvalType}`)
    .join(',');
  if (origA !== curA) return true;
  return false;
});
const isApplyButtonDisabled = computed(() => {
  const currentCategory = categories.value.find(
    (c) => Number(c.categoryId) === Number(formData.value.categoryId),
  );
  if (!currentCategory || !currentCategory.enabled) return true;
  if (isFormDirty.value) return true;
  // 이미지가 변경되었거나 새 이미지가 선택된 경우 신청 비활성화
  if (receiptFile.value) return true;
  return false;
});

/**
 * 뒤로가기
 */
function goBack() {
  router.push(ROUTES.RECEIPT.SUBMISSION);
}

// 금액 입력 서식/한도 클램프 통합
const clampToastGuard = ref(false);
watch([() => formData.value.amount, maxAllowed], ([raw, max]) => {
  const { formatted, wasClamped } = formatAndClampAmount(raw, max);
  if (formData.value.amount !== formatted) formData.value.amount = formatted;
});

/** 650px 초과 시 목록으로 이동 */
function handleResizeRedirect() {
  if (window.innerWidth > 650) router.replace('/receipt/receipt-submission');
}

onMounted(async () => {
  handleResizeRedirect();
  window.addEventListener('resize', handleResizeRedirect);
  // 부서명 로드
  const { data: me } = await UsersApi.getUserById(auth.getUserId);
  currentDeptId.value = me.team?.department?.departmentId ?? null;
  currentDeptName.value = (me.team?.department?.departmentName ?? '')
    .trim()
    .toUpperCase();
  currentTeamId.value = me.team?.teamId ?? null;
  currentTeamName.value = (me.team?.teamName ?? '').trim().toUpperCase();
  // 카테고리 로드
  const { data } =
    (await CategoryApi.getCategoriesWithDisabled?.()) ??
    (await CategoryApi.getCategories());
  categories.value = data || [];
  // 편집 대상 로드
  try {
    const s = sessionStorage.getItem('receiptEditing');
    const parsed = s ? JSON.parse(s) : null;
    const id = parsed?.id ?? parsed?.receiptId;
    if (!id) {
      toast.error(t('receipt.submission.editTargetNotFound'));
      return router.replace('/receipt/receipt-submission');
    }
    await loadById(id);
  } catch {
    toast.error(t('receipt.submission.editDataLoadError'));
    router.replace('/receipt/receipt-submission');
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResizeRedirect);
});

const fixedApprovers = computed(() =>
  formData.value.approvers.filter((a) => a.isDefault),
);
const normalApprovers = computed({
  get: () => formData.value.approvers.filter((a) => !a.isDefault),
  set: (list) => {
    formData.value.approvers = [...list, ...fixedApprovers.value];
  },
});
const categorySelectClass = computed(() => {
  const c = selectedCategory.value;
  return c && c.enabled === false ? 'inactive-category-select' : '';
});

const applyButtonTooltip = computed(() => {
  const currentCategory = categories.value.find(
    (c) => Number(c.categoryId) === Number(formData.value.categoryId),
  );
  if (!currentCategory) {
    return t('receipt.submission.categorySelectRequired');
  }
  if (currentCategory && currentCategory.enabled === false) {
    return t('receipt.submission.inactiveCategoryWarning');
  }
  if (isFormDirty.value) {
    return t('receipt.submission.changeDetectedWarning');
  }
  return t('receipt.submission.applyAvailable');
});
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
hr { display: none !important; }

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
  font-size: 0.62rem;
  color: #4b5563;
}
.external-block .dept-team {
  font-weight: 500;
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
  flex-shrink: 0;
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
  background: #f9fcff;
  border: 1px solid #e6f2ff;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 2px 8px rgba(0, 140, 255, 0.04);
}

.external-section { margin-top: 10px; }

.rejected-reason-card {
  background: #ffffff !important; /* 컨테이너는 흰색 */
  border: 1px solid #e6f2ff !important;
}

.favorite-star.btn-warning {
  color: #ffffff !important;
}

/* ── 이미지 미리보기 썸네일 ── */
.thumb-inline {
  display: grid;
  grid-template-columns: 92px 1fr;
  gap: 12px;
  align-items: flex-start;
}
.thumb-inline > .file-select-area { min-width: 0; }
.thumb {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  border: 1px solid #e6f2ff;
  border-radius: 10px;
  background: #f7fbff;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.08);
  cursor: pointer;
  transition: box-shadow 0.15s ease, transform 0.15s ease;
}
.thumb:hover {
  box-shadow: 0 6px 18px rgba(0, 140, 255, 0.12);
}
.thumb:active {
  transform: scale(0.985);
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
.thumb__placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #cbd5e1;
}
.file-select-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.file-info-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #f9fcff;
  border: 1px dashed #b3d9ff;
  border-radius: 10px;
  cursor: pointer;
}
.file-info-box:hover {
  box-shadow: 0 4px 12px rgba(0, 140, 255, 0.08);
}
.file-info-box:active {
  transform: scale(0.995);
}
.file-info-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e6f2ff 0%, #cfe6ff 100%);
  border-radius: 8px;
  color: #0b8cff;
  font-size: 1.2rem;
}
.file-info-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.file-info-title {
  font-size: 0.75rem;
  font-weight: 700;
  color: #334155;
  line-height: 1.2;
}
.file-info-desc {
  font-size: 0.65rem;
  color: #64748b;
  line-height: 1.2;
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
  width: 100% !important;
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
  min-width: 0 !important;
}

/* ==== 상세 모바일 카드 톤과 정렬: 그룹/리스트 카드화 ==== */
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
  padding: 12px 14px;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.06);
  overflow: visible;
  margin-bottom: 20px !important;
}
.list-group {
  background: #ffffff;
  border: 1px solid #e6f2ff;
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.06);
  overflow: hidden;
}
.list-group-item { border: none !important; padding: 10px 12px !important; }
.list-group-item:first-of-type { border-top: none !important; }

/* ── 이미지 미리보기 오버레이 ── */
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

/* 비활성 카테고리 선택 스타일 (MobileMintSelect 루트에 class 적용시 내부 select에 반영) */
:deep(.inactive-category-select select),
:deep(.inactive-category-select .vs__dropdown-toggle) {
  background-color: #fff2f2 !important;
  border-color: #ffc2c2 !important;
  color: #d9534f !important;
  font-weight: bold;
}

/* 다크모드: theme/pages/receipt/submission/mobile/receipt-submission-edit-dark.css */
</style>
