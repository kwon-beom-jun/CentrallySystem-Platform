<template>
  <div>
    <div class="content content-wrapper">
      <MobileDetailTitle :title="$t('receipt.submission.title')" />
      <!-- <p class="content-sub-title">영수증 등록 페이지입니다</p> -->

      <!-- 모달과 동일한 본문 UI로 교체 -->
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
              :aria-label="receiptFile?.name"
            >
              <template v-if="receiptFile && receiptFileUrl">
                <img :src="receiptFileUrl" :alt="receiptFile.name || $t('receipt.common.receiptPhoto')" class="thumb__img" />
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
                <span class="file-name">{{ receiptFile.name }}</span>
                <button
                  type="button"
                  class="btn btn-danger square-btn"
                  @click.stop="removeFile"
                >
                  ×
                </button>
              </div>
              <div v-else class="file-name-display" @click.stop>
                <span class="file-name">{{ $t('receipt.common.receiptPhoto') }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 영수증 발행일 -->
        <div class="form-group cs-card--blue">
          <DefaultFormRow>
            <MobileMintLabel
              :text="$t('receipt.submission.dueDate')"
              forId="date"
              size="small"
              :required="true"
              marginBottom="5px"
            />
          <MobileMintSmallButton 
            variant="secondary" 
            @click="setToday"
            marginBottom="5px"
          >
            {{ $t('receipt.submission.now') }}
          </MobileMintSmallButton>
          </DefaultFormRow>
          <MobileMintTextfield
            type="date"
            id="date"
            v-model="formData.date"
            size="full"
            :required="true"
          />
        </div>

        <hr />
        <!-- 참여자 검색 + 외부 토글 + 참여자 목록 (컨테이너) -->
        <div class="form-group participants-group cs-card--blue">
          <div class="search-wrapper">
          <div class="d-flex align-items-center">
            <MobileMintLabel
              :text="$t('receipt.common.participants')"
              forId="participantSearch"
              size="small"
              marginBottom="5px"
            />
            <MobileMintSmallButton variant="secondary" marginBottom="5px" @click="showExternal = !showExternal">{{ $t('receipt.common.external') }}</MobileMintSmallButton>
          </div>
          <MobileMintUserSearchDropdown
            ref="participantSearchRef"
            :labelText="$t('receipt.submission.participantSearch')"
            inputId="participantSearch"
            inputSize="full"
            :keepSearchValue="false"
            :placeholder="$t('hrm.userManagement.namePlaceholder')"
            @userSelected="onParticipantSelected"
          />
          </div>

        <!-- 외부 참여자 인라인 추가 -->
        <transition name="slide-fast">
          <div v-if="showExternal" class="external-section cs-card--blue">
            <MobileMintLabel
              :text="$t('receipt.common.external')"
              size="small"
              marginBottom="5px"
              marginTop="10px"
              customHeight="20px"
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

        <!-- 추가된 참여자 목록 -->
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
              :text="$t('receipt.submission.category')"
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
            />
          </div>
          <div class="col">
            <MobileMintLabel
              class="amount-label"
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
              :disabled="!selectedCategory"
              :externalInvalid="!!amountError"
              :externalError="amountError"
              :reserveErrorSpace="true"
              :placeholder="$t('receipt.submission.amountPlaceholder')"
              @keydown="onAmountKeydown"
              @input="onAmountInput"
              @paste="onAmountPaste"
            />
          </div>
        </div>

        <hr class="search-wrapper-hr-1" />

        <!-- 결재(합의)자 검색 + 즐겨찾기 + 결재자 목록 (컨테이너) -->
        <div class="form-group approvers-group cs-card">
          <div class="search-wrapper">
          <div class="d-flex align-items-center">
            <MobileMintLabel
              :text="$t('receipt.submission.approverSearchWithRole')"
              forId="approverSearch"
              size="small"
              marginBottom="5px"
            />
            <MobileMintSmallButton
              variant="secondary"
              marginBottom="5px"
              @click="onToggleFavoritesClick"
            >
              {{ $t('receipt.submission.toggleFavorites') }}
            </MobileMintSmallButton>
          </div>
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

        <!-- 즐겨찾기 결재자 영역 -->
        <transition name="slide-fast">
          <div
            v-if="favoriteApprovers.length && showFavorites"
            class="favorite-approvers-area cs-card--blue"
          >
            <DefaultFormRow marginBottom="5px">
              <MobileMintLabel :text="$t('receipt.submission.favoriteApprovers')" size="small" />
            </DefaultFormRow>
            <draggable
              v-model="favoriteApprovers"
              item-key="favoriteUserId"
              tag="ul"
              class="list-group mt-2"
              handle=".drag-handle"
            >
              <template #item="{ element: fav }">
                <li
                  class="list-group-item d-flex align-items-center justify-content-between"
                >
                  <span class="d-flex align-items-center flex-wrap">
                    <span
                      class="drag-handle me-2"
                      :title="$t('receipt.submission.dragToReorder')"
                    >≡</span>
                    <div class="name-block">
                      <strong class="name-strong">{{ fav.favoriteUserName }}</strong>
                      <span
                        v-if="formatDeptTeam(fav.department, fav.team) !== '-'"
                        class="dept-team"
                      >
                        {{ formatDeptTeam(fav.department, fav.team) }}
                      </span>
                    </div>
                  </span>
                  <div>
                    <MobileMintSmallButton
                      variant="primary"
                      class="me-1"
                      @click="
                        onApproverSelected({
                          userId: fav.favoriteUserId,
                          name: fav.favoriteUserName,
                          email: fav.email,
                          department: fav.department,
                          team: fav.team,
                        })
                      "
                    >
                      {{ $t('common.button.add') }}
                    </MobileMintSmallButton>
                    <MobileMintSmallButton
                      variant="secondary"
                      @click="toggleFavorite({ userId: fav.favoriteUserId })"
                    >
                      {{ $t('receipt.submission.remove') }}
                    </MobileMintSmallButton>
                  </div>
                </li>
              </template>
            </draggable>
            <MobileMintButton
              color="blue"
              @click="saveFavoriteOrder"
              customHeight="25px"
              align="right"
              marginTop="5px"
            >
              {{ $t('receipt.submission.saveOrder') }}
            </MobileMintButton>
          </div>
        </transition>

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
            @end="onDragEnd"
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
                  <span
                    class="drag-handle me-2"
                    :title="$t('receipt.submission.dragToReorder')"
                  >≡</span>
                  <IconBadge
                    class="fixed-badge"
                    v-if="element.isDefault"
                    color="primary"
                  >{{ $t('receipt.common.fixed') }}</IconBadge>
                  <span
                    class="approval-option approval-decision"
                    :class="{
                      active: element.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER],
                      disabled: element.isDefault,
                    }"
                    @click="!element.isDefault && setApprovalType(element, APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER])"
                  >{{ $t('receipt.submission.approverToggle.approveShort') }}</span
                  >
                  <span
                    class="approval-option approval-concurrence approval-option-right"
                    :class="{
                      active: element.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE],
                      disabled: element.isDefault || isSameDepartment(element),
                    }"
                    @click="
                      !element.isDefault &&
                        !isSameDepartment(element) &&
                        setApprovalType(element, APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE])
                    "
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
                <div class="d-flex align-items-center ms-auto">
                  <button
                    type="button"
                    class="btn btn-sm btn-outline-secondary square-btn favorite-star"
                    :class="{ 'btn-warning': isFavorite(element) }"
                    :title="$t('receipt.submission.favoriteToggleTitle')"
                    @click.stop="toggleFavorite(element)"
                  >
                    ★
                  </button>
                  <button
                    v-if="!element.isDefault"
                    type="button"
                    class="btn btn-sm btn-outline-danger square-btn ms-1"
                    @click.stop="removeApprover(index)"
                  >
                    ×
                  </button>
                </div>
              </li>
            </template>
          </draggable>
        </div>
        <ul v-else class="list-group list-no-group mb-2">
          <li class="list-group-item text-center bg-light">
            {{ $t('receipt.submission.normalApproverEmpty') }}
          </li>
        </ul>
        </div>

        <div
          v-if="fixedApprovers.length"
          class="form-group fixed-approvers-group cs-card--blue"
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
              class="list-group-item d-flex align-items-center"
            >
              <span class="d-flex align-items-center flex-wrap">
                <IconBadge
                  class="fixed-badge"
                  color="primary"
                >{{ $t('receipt.common.fixed') }}</IconBadge>
                <span
                  :class="[
                    'approval-option',
                    'approval-decision',
                    { active: a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER], disabled: true },
                  ]"
                >{{ $t('receipt.submission.approverToggle.approveShort') }}</span>
                <span
                  :class="[
                    'approval-option',
                    'approval-concurrence',
                    'approval-option-right',
                    { active: a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE], disabled: true },
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
              </span>
            </li>
          </ul>
        </div>

        <hr class="search-wrapper-hr-2" />

        <!-- 사유 -->
        <div class="form-group reason cs-card--blue">
          <MobileMintLabel
            marginBottom="5px"
            :text="$t('receipt.common.reason')"
            forId="reason"
            size="small"
            :required="true"
          />
          <MobileMintTextfield
            type="text"
            id="reason"
            size="full"
            v-model="formData.reason"
            :placeholder="$t('receipt.submission.reasonPlaceholder')"
          />
        </div>

        <DefaultFormRow align="right" marginTop="5px">
          <MobileMintButton align="right" color="gray" marginRight="5px" @click="goBack">
            {{ $t('common.button.cancel') }}
          </MobileMintButton>
          <MobileMintButton type="submit" align="right">
            {{ $t('common.button.register') }}
          </MobileMintButton>
        </DefaultFormRow>
      </form>

      <AlertModal
        :isVisible="confirmationModalVisible"
        :disableBackgroundClose="true"
        :title="$t('receipt.submission.saveConfirm')"
        :confirmText="$t('common.button.confirm')"
        :cancelText="$t('common.button.cancel')"
        @close="confirmationModalVisible = false"
        @confirm="submitReceipt"
      >
        <template #body>
          {{ $t('receipt.submission.registerConfirm') }}
        </template>
      </AlertModal>

      <!-- 이미지 미리보기 모달 -->
      <ImagePreviewModal ref="imagePreviewRef" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { ROUTES } from '@/config/menuConfig';
import { useAuthStore } from '@/store/auth';
import UsersApi from '@/api/hrm/UsersApi';
import ReceiptApi from '@/api/receipt/ReceiptsApi.js';
import CategoryApi from '@/api/receipt/ReceiptsCategoryApi.js';
import DefaultApproverApi from '@/api/receipt/ReceiptsDefaultApproverApi';
import ApproverFavoriteApi from '@/api/receipt/ReceiptsApproverFavoriteApi';
import { toDeptTeamPayload } from '@/utils/blankFormat.js';
import { toDeptTeamDisplay } from '@/utils/blankFormat.js';
import { processImageFile } from '@/utils/imageProcessor.js';
import { computeTotalLimit, parseAmountNumber, formatAndClampAmount, enforceMoneyKeydown, formatClampFromClipboard } from '@/utils/receipt/receiptUtils.js';
import { toast } from 'vue3-toastify';
import { APPROVAL_ROLE, APPROVAL_ROLE_LABELS } from '@/constants/receiptConstants';

// 공통 컴포넌트
import MobileDetailTitle from '@/components/common/title/MobileDetailTitle.vue';
import MobileMintButton from '@/components/common/button/MobileMintButton.vue';
import MobileMintSmallButton from '@/components/common/button/MobileMintSmallButton.vue';
import MobileMintTextfield from '@/components/common/textfield/MobileMintTextfield.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import MobileMintLabel from '@/components/common/label/MobileMintLabel.vue';
import MobileMintSelect from '@/components/common/select/MobileMintSelect.vue';
import MobileMintUserSearchDropdown from '@/components/auth/MobileMintUserSearchDropdown.vue';
import IconBadge from '@/components/common/badge/IconBadge.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import ImagePreviewModal from '@/components/common/image/ImagePreviewModal.vue';
import draggable from 'vuedraggable';

const router = useRouter();
const auth = useAuthStore();
const { t } = useI18n();

const currentDeptId = ref(null);
const currentDeptName = ref('');
const currentTeamId = ref(null);
const currentTeamName = ref('');

const confirmationModalVisible = ref(false);

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
const imagePreviewRef = ref(null);

const categories = ref([]);
const categoryOptions = computed(() =>
  (categories.value || [])
    .filter((c) => c.enabled)
    .map((c) => ({ value: c.categoryId, label: c.categoryName })),
);
const selectedCategory = computed(() => {
  const id = Number(formData.value.categoryId || NaN);
  return categories.value.find((c) => c.categoryId === id);
});
const maxAllowed = computed(() => {
  if (!selectedCategory.value) return Infinity;
  return computeTotalLimit(selectedCategory.value.limitPrice, formData.value.participants.length);
});
const amountError = computed(() => {
  if (!selectedCategory.value || maxAllowed.value === Infinity) return null;
  const entered = parseAmountNumber(formData.value.amount);
  if (entered <= maxAllowed.value) return null;
  return t('receipt.submission.amountExceeded', {
    max: maxAllowed.value.toLocaleString(),
  });
});
const amountLabel = computed(() => {
  if (!selectedCategory.value) return t('receipt.common.amount');
  const persons = formData.value.participants.length + 1;
  const total = selectedCategory.value.limitPrice * persons;
  return t('receipt.submission.amountWithLimit', { limit: total.toLocaleString() });
});
const externalParticipant = ref({ company: '', name: '', position: '', phone: '' });
const showExternal = ref(false);
const participantSearchRef = ref(null);
const approverSearchRef = ref(null);
const favoriteApprovers = ref([]);
const showFavorites = ref(false);

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
 * 부서와 팀 텍스트를 한 줄로 요약한다.
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

/**
 * 이미지 파일 선택 처리
 */
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
  } catch (err) {
    if (err.message === 'FILE_SIZE_EXCEEDED') {
      toast.error(t('receipt.submission.imageSizeExceeded'));
    } else {
      toast.error(t('receipt.submission.imageProcessError'));
      console.error(err);
    }
    receiptFile.value = null;
    receiptFileUrl.value = null;
    e.target.value = '';
    return;
  }
  e.target.value = '';
}

/**
 * 썸네일 클릭 시 미리보기 모달 오픈 (이미지 있을 때만)
 */
function onThumbClick() {
  if (receiptFileUrl.value) {
    openPreviewModal(receiptFileUrl.value);
  }
}

/**
 * 파일 선택 영역 클릭 시 파일 선택창 오픈
 */
function onSelectAreaClick() {
  if (hiddenFileInput.value) {
    hiddenFileInput.value.click();
    return;
  }
  const el = document.getElementById('receiptImage');
  if (el && el.click) el.click();
}

/**
 * 선택한 이미지 제거
 */
function removeFile() {
  if (receiptFileUrl.value) {
    URL.revokeObjectURL(receiptFileUrl.value);
  }
  receiptFile.value = null;
  receiptFileUrl.value = null;
}

/** 금액 입력 paste/IME 등 모든 입력 경로 정화 + 즉시 클램프 */
function onAmountInput(e) {
  const raw = e?.target?.value ?? formData.value.amount;
  const { formatted } = formatAndClampAmount(raw, maxAllowed.value);
  if (formData.value.amount !== formatted) formData.value.amount = formatted;
}

/** 금액 붙여넣기 전용 처리: 클립보드 값을 직접 정규화하여 주입 */
function onAmountPaste(e) {
  formData.value.amount = formatClampFromClipboard(e, maxAllowed.value);
}

/**
 * 이미지 미리보기 모달 열기
 */
function openPreviewModal(url) {
  if (!url) return;
  imagePreviewRef?.value?.open(url);
}

/**
 * 금액 입력: 숫자만 허용 + 한도 초과 방지(기본)
 */
function onAmountKeydown(e) {
  enforceMoneyKeydown(e, maxAllowed.value, formData.value.amount);
}

/**
 * 외부 참여자 입력 토글
 */
function onToggleFavoritesClick() {
  if (!showFavorites.value) {
    if (!favoriteApprovers.value.length) {
      toast.info(t('receipt.submission.favoriteEmpty'));
      return;
    }
  }
  showFavorites.value = !showFavorites.value;
}

/**
 * 외부 참여자 추가
 */
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

/**
 * 내부 참여자 선택 시 추가
 */
function onParticipantSelected(user) {
  if (formData.value.participants.some((p) => p.userId === user.userId))
    return toast.warning(t('receipt.submission.participantAlreadyAdded'));
  formData.value.participants.push({
    userId: user.userId,
    name: user.name,
    department: user.department,
    team: user.team,
  });
  participantSearchRef.value?.resetSearch?.();
}

/**
 * 참여자 삭제
 */
function removeParticipant(idx) {
  formData.value.participants.splice(idx, 1);
}

/**
 * 결재/합의자 선택 시 추가 (결재 최대 3명)
 */
function onApproverSelected(user) {
  const dup = formData.value.approvers.some(
    (a) =>
      (a.userId && a.userId === user.userId) || (!a.userId && a.email === user.email),
  );
  if (dup) return toast.warning(t('receipt.submission.approverAlreadyAdded'));
  const decisionCnt = formData.value.approvers.filter(
    (a) => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER],
  ).length;
  if (decisionCnt >= 3) return toast.warning(t('receipt.submission.approverMaxLimit'));
  formData.value.approvers.push({
    userId: user.userId,
    name: user.name,
    email: user.email,
    department: user.department,
    team: user.team,
    approvalType: APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER],
  });
  approverSearchRef.value?.resetSearch?.();
}

/**
 * 결재/합의 전환 토글
 */
function setApprovalType(user, type) {
  if (user.isDefault && type === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]) return;
  if (type === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE] && isSameDepartment(user)) return;
  if (type === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]) {
    const otherCnt = formData.value.approvers.filter(
      (a) => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER] && a !== user,
    ).length;
    if (otherCnt >= 3) return toast.warning(t('receipt.submission.approverMaxLimitSet'));
  }
  user.approvalType = type;
}

/**
 * 결재선 사용자 삭제
 */
function removeApprover(idx) {
  const target = normalApprovers.value[idx];
  if (!target) return;
  const realIdx = formData.value.approvers.indexOf(target);
  if (realIdx !== -1) formData.value.approvers.splice(realIdx, 1);
}

/**
 * 저장 전 검증 및 확인 모달 표시
 */
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
  if (!receiptFile.value) return toast.warning(t('receipt.submission.attachmentRequired'));
  if (!receiptFile.value.type?.startsWith?.('image/'))
    return toast.warning(t('receipt.submission.imageOnlyWarning'));
  if (amountError.value) return toast.warning(t('receipt.submission.amountLimitCheck'));
  confirmationModalVisible.value = true;
}

/**
 * 영수증 저장 요청
 */
async function submitReceipt() {
  confirmationModalVisible.value = false;
  const payload = new FormData();
  payload.append('userName', auth.getUser);
  payload.append('userEmail', auth.getUserEmail);
  payload.append('date', formData.value.date);
  payload.append('categoryId', formData.value.categoryId);
  payload.append('amount', String(formData.value.amount).replace(/\D/g, ''));
  payload.append('reason', formData.value.reason);
  payload.append(
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
  payload.append(
    'approvers',
    JSON.stringify(
      formData.value.approvers.map((a) => ({
        userId: a.userId,
        name: a.name,
        email: a.email,
        approvalType: a.approvalType,
        approvalRole: a.approvalRole,
        isDefault: a.isDefault,
        department: toDeptTeamPayload(a.department),
        team: toDeptTeamPayload(a.team),
      })),
    ),
  );
  payload.append('departmentId', currentDeptId.value ?? '');
  payload.append('departmentName', currentDeptName.value ?? '');
  payload.append('teamId', currentTeamId.value ?? '');
  payload.append('teamName', currentTeamName.value ?? '');
  payload.append('receiptFile', receiptFile.value);
  await ReceiptApi.createReceipt(auth.getUserId, payload);
  toast.success(t('receipt.submission.registerSuccess'));
  router.push(ROUTES.RECEIPT.SUBMISSION);
}

/**
 * 카테고리 목록 조회
 */
async function fetchCategories() {
  const { data } = await CategoryApi.getCategories();
  categories.value = data || [];
}

/**
 * 기본 결재선 조회
 */
async function fetchDefaultApprovers() {
  const { data } = await DefaultApproverApi.getDefaultApprovers({ size: 1000 });
  const rows = (data?.content ?? data) || [];
  rows
    .filter(
      (d) =>
        d.userId !== auth.getUserId &&
        (d.department ?? '').trim().toUpperCase() !==
          currentDeptName.value.trim().toUpperCase(),
    )
    .forEach((d) => {
      formData.value.approvers.push({
        userId: d.userId,
        name: d.userName,
        email: d.email,
        department: d.department,
        team: d.team,
        approvalType: APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE],
        isDefault: true,
      });
    });
  keepDefaultAtBottom();
}
function keepDefaultAtBottom() {
  const nonDef = formData.value.approvers.filter((a) => !a.isDefault);
  const def = formData.value.approvers.filter((a) => a.isDefault);
  formData.value.approvers = [...nonDef, ...def];
}
function isFavorite(user) {
  if (!user?.userId) return false;
  return favoriteApprovers.value.some((f) => f.favoriteUserId === user.userId);
}
async function toggleFavorite(user) {
  if (!user?.userId) return;
  try {
    if (isFavorite(user)) {
      await ApproverFavoriteApi.deleteApproverFavorite(auth.getUserId, user.userId);
      favoriteApprovers.value = favoriteApprovers.value.filter(
        (f) => f.favoriteUserId !== user.userId,
      );
      toast.info(t('receipt.submission.favoriteRemoved'));
    } else {
      await ApproverFavoriteApi.createApproverFavorite({
        ownerUserId: auth.getUserId,
        favoriteUserId: user.userId,
        favoriteUserName: user.name,
        email: user.email,
        department: user.department,
        team: user.team,
      });
      await fetchApproverFavorites();
      toast.success(t('receipt.submission.favoriteAdded'));
    }
  } catch {
    toast.error(t('receipt.submission.favoriteError'));
  }
}
async function fetchApproverFavorites() {
  try {
    const { data } = await ApproverFavoriteApi.getApproverFavorites(auth.getUserId);
    favoriteApprovers.value = data || [];
  } catch {
    favoriteApprovers.value = [];
  }
}
/** 즐겨찾기 순서 저장 */
async function saveFavoriteOrder() {
  favoriteApprovers.value.forEach((f, i) => (f.stepNo = i + 1));
  try {
    await ApproverFavoriteApi.updateApproverFavorite(
      auth.getUserId,
      favoriteApprovers.value.map((f) => ({
        favoriteUserId: f.favoriteUserId,
        stepNo: f.stepNo,
      })),
    );
    toast.success(t('receipt.submission.favoriteOrderSaved'));
  } catch {
    toast.error(t('receipt.submission.favoriteOrderSaveFailed'));
  }
}
function onDragEnd(evt) {}
const fixedApprovers = computed(() =>
  formData.value.approvers.filter((a) => a.isDefault),
);
const normalApprovers = computed({
  get: () => formData.value.approvers.filter((a) => !a.isDefault),
  set: (list) => {
    formData.value.approvers = [...list, ...fixedApprovers.value];
  },
});
function isSameDepartment(target) {
  const theirs = (target?.department ?? '').trim().toUpperCase();
  const mine = (currentDeptName.value ?? '').trim().toUpperCase();
  return theirs && theirs === mine;
}
/** 오늘 날짜로 설정 */
function setToday() {
  const t = new Date();
  formData.value.date = `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(
    2,
    '0',
  )}-${String(t.getDate()).padStart(2, '0')}`;
}

// 금액 입력 서식/한도 클램프 통합
const clampToastGuard = ref(false);
watch([() => formData.value.amount, maxAllowed], ([raw, max]) => {
  const { formatted, wasClamped } = formatAndClampAmount(raw, max);
  if (formData.value.amount !== formatted) formData.value.amount = formatted;
  if (wasClamped && !clampToastGuard.value) {
    clampToastGuard.value = true;
    toast.info(t('receipt.submission.amountAdjusted', { limit: max.toLocaleString() }));
    setTimeout(() => (clampToastGuard.value = false), 600);
  }
});

/**
 * 사용자 부서/팀 정보 조회
 */
async function fetchMyDeptTeamInfo() {
  const { data: me } = await UsersApi.getUserById(auth.getUserId);
  currentDeptId.value = me.team?.department?.departmentId ?? null;
  currentDeptName.value = (me.team?.department?.departmentName ?? '')
    .trim()
    .toUpperCase();
  currentTeamId.value = me.team?.teamId ?? null;
  currentTeamName.value = (me.team?.teamName ?? '').trim().toUpperCase();
}

/**
 * 목록으로 이동
 */
/**
 * 뒤로가기
 */
function goBack() {
  router.push(ROUTES.RECEIPT.SUBMISSION);
}

/**
 * 화면 폭이 650px 초과 시 데스크톱 리스트 페이지로 리다이렉트
 */
function handleResizeRedirect() {
  if (window.innerWidth > 650) {
    router.replace('/receipt/receipt-submission');
  }
}

onMounted(async () => {
  // 초기 진입 시에도 즉시 검사
  handleResizeRedirect();
  window.addEventListener('resize', handleResizeRedirect);
  await fetchMyDeptTeamInfo();
  await fetchCategories();
  await fetchDefaultApprovers();
  await fetchApproverFavorites();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResizeRedirect);
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
  margin-bottom: 20px;
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

.favorite-approvers-area { margin-top: 10px; }

.external-section { margin-top: 10px; }

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

/* 다크모드: theme/pages/receipt/submission/mobile/receipt-submission-create-dark.css */
</style>
