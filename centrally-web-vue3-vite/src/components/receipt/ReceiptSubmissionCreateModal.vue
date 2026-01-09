<template>
  <!-- BootstrapVue 의 b-modal 사용 -->
  <b-modal
    v-model="innerVisible"
    :title="$t('receipt.submission.register')"
    no-close-on-backdrop
    scrollable
    centered
    fade
    hide-footer
    custom-class="receipt-modal"
    body-class="p-0"
    @shown="onModalShown"
  >
    <div class="modal-body">
      <!-- 모달 내부 컨텐츠 -->
      <form @submit.prevent="openConfirmationModal" @keydown.enter.prevent>
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
          <!-- 선택된 파일 이름 표시 및 삭제 버튼 -->
          <div v-if="receiptFile" class="mt-2">
            <ul class="list-group">
              <li class="list-group-item">
                {{ receiptFile.name }}
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

        <hr />
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
            <DefaultLabel :text="$t('receipt.common.external') + ' ' + $t('receipt.common.participants') + ' ' + $t('common.button.add')" size="small" marginBottom="5px" marginTop="10px" customHeight="20px"/>
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
            marginTop="5px"
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

        <!--카테고리 | 금액 -->
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
            />
          </div>
          <div class="col">
            <DefaultLabel
              class="amount-label"
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
              :disabled="!selectedCategory"
              :externalInvalid="!!amountError"
              :externalError="amountError"
              :reserveErrorSpace="true"
              :placeholder="$t('receipt.submission.category') + ' → ' + $t('receipt.common.amount')"
            />
          </div>
        </div>
        <hr class="search-wrapper-hr-1" />

        <!-- 결재자 검색 필드 -->
        <div class="search-wrapper">
          <div class="d-flex align-items-center">
            <DefaultLabel
              :text="$t('receipt.common.approversList') + ' ' + $t('common.label.search')"
              forId="approverSearch"
              size="small"
              marginBottom="5px"
              :required="true"
            />
            <DefaultSmallButton variant="secondary" marginBottom="5px" @click="onToggleFavoritesClick">{{ $t('hrm.favoriteMenu.title') }}</DefaultSmallButton>
          </div>
          
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

        <!-- 즐겨찾기 결재자 영역 -->
        <transition name="slide-fast">
        <div v-if="favoriteApprovers.length && showFavorites" class="favorite-approvers-area cs-card--blue">
          <DefaultFormRow marginBottom="5px">
            <DefaultLabel :text="$t('hrm.favoriteMenu.title')" size="small" />
          </DefaultFormRow>
          <draggable
            v-model="favoriteApprovers"
            item-key="favoriteUserId"
            tag="ul"
            class="list-group mt-2"
            handle=".drag-handle"
          >
            <template #item="{ element: fav }">
              <li class="list-group-item d-flex align-items-center justify-content-between">
                <span class="d-flex align-items-center flex-wrap">
                  <span class="drag-handle me-2" :title="$t('hrm.favoriteMenu.reorder')">≡</span>
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
                  <DefaultSmallButton variant="primary" class="me-1"
                    @click="onApproverSelected({ userId:fav.favoriteUserId, name:fav.favoriteUserName, email:fav.email, department:fav.department, team:fav.team })">
                    {{ $t('common.button.add') }}
                  </DefaultSmallButton>
                  <DefaultSmallButton variant="danger"
                    @click="toggleFavorite({ userId:fav.favoriteUserId })">
                    {{ $t('common.button.delete') }}
                  </DefaultSmallButton>
                </div>
              </li>
            </template>
          </draggable>
          <DefaultButton color="blue" @click="saveFavoriteOrder" customHeight="25px" align="right" marginTop="5px">{{ $t('receipt.submission.saveOrder') }}</DefaultButton>
        </div>
        </transition>

        <!-- 결재자 목록 -->
        <div v-if="normalApprovers.length > 0">
          <DefaultLabel
            :text="$t('receipt.common.approversList')"
            marginBottom="5px"
            marginTop="10px"
            size="small"
          />

          <!-- vuedraggable 적용 -->
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
                  <!-- 드래그 핸들 -->
                  <span class="drag-handle me-2" :title="$t('hrm.favoriteMenu.reorder')">≡</span>

                  <IconBadge
                    v-if="element.isDefault"
                    class="fixed-badge"
                    color="primary"
                  >{{ $t('receipt.common.fixed') }}</IconBadge>

                  <!-- [ 결재 | 합의 ] 글자 버튼 -->
                  <span
                    class="approval-option approval-decision"
                    :class="{
                            active  : element.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER],
                            disabled: element.isDefault
                          }"
                    @click="!element.isDefault && setApprovalType(element, APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER])"
                  >
                    {{ $t('receipt.submission.approverToggle.approveShort') }}
                  </span>
                  <span
                    class="approval-option approval-concurrence approval-option-right"
                    :class="{
                      active  : element.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE],
                      disabled: element.isDefault                /* 고정이면 무조건 회색 */
                            || isSameDepartment(element)        /* 같은 부서면 회색     */
                    }"
                    @click="!element.isDefault                  /* 고정은 못 바꿈        */
                          && !isSameDepartment(element)         /* 같은 부서 금지       */
                          && setApprovalType(element, APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE])"
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

                <!-- 오른쪽 컨트롤 컨테이너: 즐겨찾기 + 삭제 버튼을 오른쪽 끝 정렬 -->
                <div class="d-flex align-items-center ms-auto">
                  <button
                    type="button"
                    class="btn btn-sm btn-outline-secondary square-btn favorite-star"
                    :class="{ 'btn-warning': isFavorite(element) }"
                    title="즐겨찾기 토글"
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
        <!-- 일반 결재자가 없을 때 → 더미 안내만 출력 -->
        <ul v-else class="list-group list-no-group mb-2">
          <li class="list-group-item text-center text-muted bg-light">
            {{ $t('receipt.common.approversList') }}
          </li>
        </ul>


        <DefaultLabel v-if="fixedApprovers.length" :text="$t('receipt.common.fixed') + ' ' + $t('receipt.common.approversList')" size="small" marginTop="10px" />
        <!-- ── 고정 합의자(드래그·삭제 불가) ───────────────────────── -->
        <ul v-if="fixedApprovers.length" class="list-group mt-2">
          <li v-for="a in fixedApprovers" :key="a.userId"
              class="list-group-item d-flex align-items-center">
            <span class="d-flex align-items-center flex-wrap">
              <IconBadge
                class="fixed-badge"
                color="primary"
              >{{ $t('receipt.common.fixed') }}</IconBadge>
        
              <span :class="['approval-option','approval-decision',{active:a.approvalType===APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER],disabled:true}]">{{ $t('receipt.submission.approverToggle.approveShort') }}</span>
              <span :class="['approval-option','approval-concurrence','approval-option-right',{active:a.approvalType===APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE],disabled:true}]">{{ $t('receipt.submission.approverToggle.concurShort') }}</span>
        
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
            marginRight="5px"
            @click="closeModal"
          >
            {{ $t('common.button.cancel') }}
          </DefaultButton>
          <DefaultButton type="submit" align="right"> {{ $t('common.button.register') }} </DefaultButton>
        </div>
      </form>
    </div>
  </b-modal>

  <!-- AlertModal (확인 모달) -->
  <AlertModal
    :isVisible="confirmationModalVisible"
    :disableBackgroundClose="true"
    :title="$t('common.message.confirmSave')"
    :confirmText="$t('common.button.confirm')"
    :cancelText="$t('common.button.cancel')"
    @close="confirmationModalVisible = false"
    @confirm="submitReceipt"
  >
    <template #body>
      {{ $t('receipt.submission.applyMessage') }}
    </template>
  </AlertModal>
</template>

<script setup>
/* -------------------------------------------------------------------
 *  Imports
 * ------------------------------------------------------------------*/
import { ref, computed, watch, onMounted, onBeforeUnmount, defineProps, defineEmits } from 'vue'
import { useI18n } from 'vue-i18n'
import draggable                from 'vuedraggable'
import { processImageFile } from '@/utils/imageProcessor.js';
import AlertModal               from '@/components/common/modal/AlertModal.vue'
import DefaultButton            from '@/components/common/button/DefaultButton.vue'
import DefaultTextfield         from '@/components/common/textfield/DefaultTextfield.vue'
import DefaultFormRow           from '@/components/common/DefaultFormRow.vue'
import DefaultLabel             from '@/components/common/label/DefaultLabel.vue'
import DefaultSelect            from '@/components/common/select/DefaultSelect.vue'
import UserSearchDropdown       from '@/components/auth/UserSearchDropdown.vue'
import IconBadge                from '@/components/common/badge/IconBadge.vue'
import DefaultSmallButton       from '@/components/common/button/DefaultSmallButton.vue'
import { useAuthStore }         from '@/store/auth'
import { toast }                from 'vue3-toastify'
import ReceiptApi               from '@/api/receipt/ReceiptsApi.js'
import CategoryApi              from '@/api/receipt/ReceiptsCategoryApi.js'
import DefaultApproverApi       from '@/api/receipt/ReceiptsDefaultApproverApi'
import ApproverFavoriteApi      from '@/api/receipt/ReceiptsApproverFavoriteApi'
import { toDeptTeamDisplay, toDeptTeamPayload } from '@/utils/blankFormat.js'
import { APPROVAL_ROLE, APPROVAL_ROLE_LABELS } from '@/constants/receiptConstants';

/* -------------------------------------------------------------------
 *  Basic setup
 * ------------------------------------------------------------------*/
const props = defineProps({
  isVisible: Boolean,
  currentDeptId   : [String, Number],
  currentDeptName : String,
  currentTeamId   : [String, Number],
  currentTeamName : String
})
const emit  = defineEmits(['close', 'confirm'])
const authStore      = useAuthStore()
const myUserId       = authStore.getUserId
const myUser         = authStore.getUser
const myUserEmail         = authStore.getUserEmail
const innerVisible   = ref(props.isVisible)
const confirmationModalVisible = ref(false)
const { t } = useI18n()

/* -------------------------------------------------------------------
 *  Reactive form state
 * ------------------------------------------------------------------*/
const formData = ref({
  date        : '',
  categoryId  : '',
  amount      : '',
  reason      : '',
  participants: [],
  approvers   : []
})
const receiptFile = ref(null)
const externalParticipant = ref({ company:'', name:'', position:'', phone:'' })
const showExternal = ref(false)

/* -------------------------------------------------------------------
 *  Categories (카테고리)
 * ------------------------------------------------------------------*/
const categories = ref([])
const categoryOptions = computed(() =>
  categories.value
    .filter(c => c.enabled) // ✅ 'enabled' 필드로 정확하게 수정
    .map(c => ({ value: c.categoryId, label: c.categoryName }))
)
async function loadCategories () {
  if (categories.value.length) return
  const { data } = await CategoryApi.getCategories()
  categories.value = data
}

/* -------------------------------------------------------------------
 *  Participants & approvers helpers
 * ------------------------------------------------------------------*/
function keepDefaultAtBottom () {
  const nonDef = formData.value.approvers.filter(a => !a.isDefault)
  const def    = formData.value.approvers.filter(a =>  a.isDefault)
  formData.value.approvers = [...nonDef, ...def]
}
const participantSearchRef = ref(null)
const approverSearchRef    = ref(null)
const favoriteApprovers    = ref([])
const showFavorites        = ref(false)

/**
 * 조직 텍스트를 모바일/모달 표시에 맞춰 정제한다.
 * @param {string} value
 * @returns {string}
 */
function sanitizeOrgText(value) {
  const text = (value ?? '').toString().trim();
  if (!text || text === '-' || text === '--') return '';
  return text;
}

/**
 * 부서와 팀 텍스트를 하나의 줄로 축약한다.
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

function onToggleFavoritesClick () {
  // 열려고 할 때 비어있으면 토스트만 띄우고 펼치지 않음
  if (!showFavorites.value) {
    if (!favoriteApprovers.value.length) {
      toast.info(t('receipt.submission.favoriteEmpty'))
      return
    }
  }
  showFavorites.value = !showFavorites.value
}

// 이미지 파일 처리(압축·리사이즈) util 사용
async function handleFileChange (e) {
  const file = e.target.files[0] || null
  if (!file) {
    receiptFile.value = null
    return
  }
  try {
    receiptFile.value = await processImageFile(file, { maxSizeMB: 2, maxWidthOrHeight: 2048 })
  } catch (err) {
    if (err.message === 'FILE_SIZE_EXCEEDED') {
      toast.error(t('receipt.submission.imageSizeExceeded'))
    } else {
      toast.error(t('receipt.submission.imageProcessError'))
      console.error(err)
    }
    receiptFile.value = null
    e.target.value = ''
    return
  }
  e.target.value = ''
}
const removeFile = () => { receiptFile.value = null }

/* 모달 표시 후 레이아웃 강제 리플로우로 초기 버벅임 방지 */
function onModalShown () {
  // 카테고리/결재자/참여자 섹션이 비동기 로딩될 때 레이아웃 점프를 줄이기 위해 강제 reflow
  try {
    const el = document.querySelector('.receipt-modal .modal-content > .modal-body')
    if (el) {
      // 읽기만 해도 리플로우 유도됨
      void el.offsetHeight
    }
  } catch (e) {}
}

/* -------------------------------------------------------------------
 *  Select-box / validation helpers
 * ------------------------------------------------------------------*/
const selectedCategory = computed(() => {
  const id = Number(formData.value.categoryId || NaN)
  return categories.value.find(c => c.categoryId === id)
})
const maxAllowed = computed(() => {
  if (!selectedCategory.value) return Infinity
  const persons = formData.value.participants.length + 1
  return Math.floor(selectedCategory.value.limitPrice * persons)
})
const amountError = computed(() => {
  const entered = Number(String(formData.value.amount).replace(/\D/g, '')) || 0
  if (!selectedCategory.value || maxAllowed.value === Infinity) return null
  return entered > maxAllowed.value
    ? t('receipt.submission.amountExceeded', { max: maxAllowed.value.toLocaleString() })
    : null
})
const amountLabel = computed(() => {
  if (!selectedCategory.value) return t('receipt.common.amount')
  const persons = formData.value.participants.length + 1
  const total   = selectedCategory.value.limitPrice * persons
  return t('receipt.submission.amountWithLimit', { limit: total.toLocaleString() })
})

/* -------------------------------------------------------------------
 *  금액 입력 필터링 및 포맷팅
 * ------------------------------------------------------------------*/
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

/* -------------------------------------------------------------------
 *  User / role helpers
 * ------------------------------------------------------------------*/
// const currentDeptName = computed(() => {
//   const me = HrmUserApi.getUserById(authStore.getUserId)
//   return me?.department?.trim().toUpperCase() ?? ''
// })
const isSameDepartment = target => {
  const theirs = (target?.department ?? '').trim().toUpperCase()
  const mine   = (props.currentDeptName ?? '').trim().toUpperCase()
  return theirs && theirs === mine
}
function setApprovalType (user, type) {
  if (user.isDefault && type === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]) return
  if (type === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE] && isSameDepartment(user)) return
  if (type === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]) {
    /* 자신을 제외하고 결재자 수 계산 */
    const otherCnt = formData.value.approvers
      .filter(a => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER] && a !== user).length
    if (otherCnt >= 3)
      return toast.warning(t('receipt.submission.approverMaxLimitSet'))
  }
  user.approvalType = type
}

/* ------------ 고정 / 일반 결재자 분리 ------------------------------ */
const fixedApprovers  = computed(()=> formData.value.approvers.filter(a=>a.isDefault))
const normalApprovers = computed({
  get : ()   => formData.value.approvers.filter(a=>!a.isDefault),
  set : list => {              // 드래그 후 setter 로 다시 병합
    formData.value.approvers = [...list, ...fixedApprovers.value]
  }
})

/* -------------------------------------------------------------------
 *  Participant / approver UI actions
 * ------------------------------------------------------------------*/
function onParticipantSelected (user) {
  if (formData.value.participants.some(p => p.userId === user.userId))
    return toast.warning(t('receipt.submission.participantAlreadyAdded'))
  formData.value.participants.push({
    userId: user.userId, name: user.name,
    department: user.department, team: user.team
  })
}
function addExternalParticipant () {
  const p = externalParticipant.value
  if (!p.name || !p.phone) return toast.warning(t('receipt.submission.externalParticipantRequired'))
  const dup = formData.value.participants.some(x => x.type==='EXTERNAL' && x.name===p.name && x.phone===p.phone)
  if (dup) return toast.warning(t('receipt.submission.externalParticipantAlreadyAdded'))
  formData.value.participants.push({
    type:'EXTERNAL', userId:null,
    name:p.name, company:p.company, position:p.position, phone:p.phone,
    department:'', team:''
  })
  externalParticipant.value = { company:'', name:'', position:'', phone:'' }
}
function onApproverSelected (user) {
  const dup = formData.value.approvers.some(a =>
    (a.userId && a.userId === user.userId) ||
    (!a.userId && a.email === user.email)
  )
  if (dup) return toast.warning(t('receipt.submission.approverAlreadyAdded'))
  /* 현재 결재자(approvalType === '결재') 수 확인 */
  const decisionCnt = formData.value.approvers
    .filter(a => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]).length
  if (decisionCnt >= 3) {
    return toast.warning(t('receipt.submission.approverMaxLimit'))
  }
  formData.value.approvers.push({
    userId: user.userId, name: user.name, email: user.email,
    department: user.department, team: user.team,
    approvalType: APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]
  })
  keepDefaultAtBottom()
}
function removeParticipant (idx) { formData.value.participants.splice(idx, 1) }
function removeApprover (idx) {
  const target = normalApprovers.value[idx]
  if (!target) return
  const realIdx = formData.value.approvers.indexOf(target)
  if (realIdx !== -1) formData.value.approvers.splice(realIdx, 1)
}
function onDragEnd(evt) {
  // 드래그가 끝난 후 특별한 로직이 필요 없다면 빈 함수로 둬도 됨
  // 만약 순서 변경 후 뭔가 처리할 게 있다면 이 안에 작성
}
/* -------------------------------------------------------------------
 *  Fetch default approvers
 * ------------------------------------------------------------------*/
async function fetchDefaultApprovers () {
  try {
    const { data } = await DefaultApproverApi.getDefaultApprovers({ size: 1000 });
      // 본인 + 같은 부서 전부 제외
      (data.content ?? data)
      .filter(d =>
        // 본인 제외
        d.userId !== myUserId &&
        // 같은 부서 전부 제외
        (d.department ?? '').trim().toUpperCase() !== props.currentDeptName.trim().toUpperCase()
      )
      .forEach(d => {
        formData.value.approvers.push({
          userId: d.userId, name: d.userName,
          email: d.email, department: d.department, team: d.team,
          approvalType: APPROVAL_ROLE_LABELS[APPROVAL_ROLE.CONCURRENCE], isDefault: true
        })
      })
    keepDefaultAtBottom()
  } catch {
    toast.error(t('receipt.submission.defaultApproverLoadError'))
  }
}

/* -------------------------------------------------------------------
 *  Fetch favorites (owner: myUserId)
 * ------------------------------------------------------------------*/
async function fetchApproverFavorites () {
  try {
    const { data } = await ApproverFavoriteApi.getApproverFavorites(myUserId)
    favoriteApprovers.value = data || []
  } catch {
    favoriteApprovers.value = []
  }
}

function isFavorite(user) {
  if (!user?.userId) return false
  return favoriteApprovers.value.some(f => f.favoriteUserId === user.userId)
}
async function toggleFavorite(user) {
  if (!user?.userId) return
  try {
    if (isFavorite(user)) {
      await ApproverFavoriteApi.deleteApproverFavorite(myUserId, user.userId)
      favoriteApprovers.value = favoriteApprovers.value.filter(f=>f.favoriteUserId!==user.userId)
      toast.info(t('receipt.submission.favoriteRemoved'))
    } else {
      await ApproverFavoriteApi.createApproverFavorite({
        ownerUserId: myUserId,
        favoriteUserId: user.userId,
        favoriteUserName: user.name,
        email: user.email,
        department: user.department,
        team: user.team
      })
      await fetchApproverFavorites()
      toast.success(t('receipt.submission.favoriteAdded'))
    }
  } catch {
    toast.error(t('receipt.submission.favoriteError'))
  }
}

/* -------------------------------------------------------------------
 *  Form submit & reset
 * ------------------------------------------------------------------*/
function resetFormData () {
  formData.value = {
    date: '', type: '', amount: '', reason: '',
    // participants: [], approvers: formData.value.approvers.filter(a => a.isDefault)
    participants: [], approvers: []
  }
  receiptFile.value = null
  participantSearchRef.value?.resetSearch?.()
  approverSearchRef.value?.resetSearch?.()
}
/**
 * 필수 항목을 순차적으로 체크하여 확인 모달을 연다
 */
function openConfirmationModal () {
  // 1. 영수증 사진
  if (!receiptFile.value) {
    return toast.warning(t('receipt.submission.attachmentRequired'))
  }
  if (!receiptFile.value.type.startsWith('image/')) {
    return toast.warning(t('receipt.submission.imageOnlyWarning'))
  }
  // 2. 영수증 발행일
  if (!formData.value.date) {
    return toast.warning(t('receipt.submission.dueDateRequired'))
  }
  // 3. 카테고리
  if (!formData.value.categoryId) {
    return toast.warning(t('receipt.submission.categorySelectRequired'))
  }
  // 4. 금액
  if (!formData.value.amount) {
    return toast.warning(t('receipt.submission.amountRequired'))
  }
  if (amountError.value) {
    return toast.warning(t('receipt.submission.amountLimitCheck'))
  }
  // 5. 결재자 (최소 1명)
  const approverCnt = formData.value.approvers
    .filter(a => a.approvalType === APPROVAL_ROLE_LABELS[APPROVAL_ROLE.APPROVER]).length
  if (approverCnt === 0) {
    return toast.warning(t('receipt.submission.approverMinRequired'))
  }
  if (approverCnt > 3) {
    return toast.warning(t('receipt.submission.approverMaxLimitSet'))
  }
  // 6. 사유
  if (!formData.value.reason) {
    return toast.warning(t('receipt.submission.reasonRequired'))
  }
  // 모든 검증 통과 시 확인 모달 표시
  confirmationModalVisible.value = true
}
async function saveFavoriteOrder () {
  favoriteApprovers.value.forEach((f,i)=> f.stepNo = i+1)
  try {
    await ApproverFavoriteApi.updateApproverFavorite(myUserId, favoriteApprovers.value.map(f=>({ favoriteUserId:f.favoriteUserId, stepNo:f.stepNo })))
    toast.success(t('receipt.submission.favoriteOrderSaved'))
  } catch {
    toast.error(t('receipt.submission.favoriteOrderSaveFailed'))
  }
}
async function submitReceipt () {
  confirmationModalVisible.value = false
  const payload = new FormData()
  payload.append('userName'   , myUser)
  payload.append('userEmail'  , myUserEmail)
  payload.append('date'       , formData.value.date)
  payload.append('categoryId' , formData.value.categoryId)
  payload.append('amount'     , String(formData.value.amount).replace(/\D/g, ''))
  payload.append('reason'     , formData.value.reason)
  payload.append('participants', JSON.stringify(formData.value.participants.map(p=>({
    type: p.type||'INTERNAL',
    userId: p.userId,
    name: p.name,
    company: p.company,
    position: p.position,
    phone: p.phone,
    department: toDeptTeamPayload(p.department),
    team: toDeptTeamPayload(p.team)
  }))) )

  payload.append('approvers', JSON.stringify(formData.value.approvers.map(a=>({
    userId: a.userId,
    name: a.name,
    email: a.email,
    approvalType: a.approvalType,
    approvalRole: a.approvalRole,
    isDefault: a.isDefault,
    department: toDeptTeamPayload(a.department),
    team: toDeptTeamPayload(a.team)
  }))) )
  payload.append('departmentId',   props.currentDeptId ?? '')
  payload.append('departmentName', props.currentDeptName ?? '')
  payload.append('teamId',         props.currentTeamId ?? '')
  payload.append('teamName',       props.currentTeamName ?? '')
  payload.append('receiptFile' , receiptFile.value)
  // 포트폴리오용: 기능 비활성화
  console.error('포트폴리오용: 영수증 생성 기능이 비활성화되어 있습니다.')
}

/* -------------------------------------------------------------------
 *  Misc helpers
 * ------------------------------------------------------------------*/
const setToday = () => {
  const t = new Date()
  formData.value.date = `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')}`
}
const openModal = () => { history.pushState({ modal: true }, '') }
function closeModal () {
  innerVisible.value = false
  if (history.state?.modal) history.back()
}
function handlePopState () {
  if (innerVisible.value) {
    innerVisible.value = false
    emit('close')
  }
}

watch(innerVisible, v => {
  if (!v) {
    showExternal.value = false
    showFavorites.value = false
    externalParticipant.value = { company:'', name:'', position:'', phone:'' }
    emit('close')
  }
})
watch([() => formData.value.amount, maxAllowed], ([raw, max]) => {
  if (max === Infinity) return
  const entered = Number(String(raw).replace(/\D/g, '')) || 0
  if (entered > max) {
    formData.value.amount = String(max)
    toast.info(t('receipt.submission.amountAdjusted', { limit: max.toLocaleString() }))
  }
})

const showModalWatcher = watch(
  () => props.isVisible,               // getter: 동기 함수
  async (visible) => {                  // effect: prop 변경 시 실행
    innerVisible.value = visible
    if (!visible) return

    // 모달이 열릴 때만 초기화 + API 호출
    resetFormData()
    showExternal.value = false
    showFavorites.value = false
    await loadCategories()
    await fetchDefaultApprovers()
    await fetchApproverFavorites()
    // v-model="innerVisible" 에 의해 <b-modal> 이 열립니다
  }
)

/* -------------------------------------------------------------------
 *  Lifecycle
 * ------------------------------------------------------------------*/
onMounted(() => {
  window.addEventListener('popstate', handlePopState)
})
onBeforeUnmount(() => {
  window.removeEventListener('popstate', handlePopState)
  showModalWatcher() // stop
})
</script>


<style scoped>
hr {
  margin: 0 0 15px;
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

.receipt-modal .modal-content > .modal-body {
  padding: 0 !important;
  max-height: 80vh;
  overflow-y: scroll; /* 모달 내부 스크롤 고정 */
  contain: content; /* 초기 페인트 분리로 애니메이션 떨림 완화 */
}

.modal-body {
  padding: 15px 20px 10px 20px;
  overflow-y: scroll; /* 데스크탑/모바일 모두 스크롤 영역 항상 표시 */
  scrollbar-gutter: stable both-edges; /* 좌우 대칭 여백 확보 */
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
}

/* 합의 (active) : 초록 */
.approval-concurrence.active {
  color: #198754 !important;
  background: #1987542b !important;
}

.approval-option.disabled {
  pointer-events: none; /* 클릭 차단 */
  opacity: 0.4; /* 흐리게 표시 */
}
/* slide-fast transition */
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

/* 외부 참여자 영역도 동일한 연회색 배경 적용 */
.external-section { margin-top: 10px; }

/* 즐겨찾기(별) 버튼이 활성화(노란색)일 때 별 아이콘을 흰색으로 */
.favorite-star.btn-warning {
  color: #ffffff !important;
}

@media (max-width: 650px) {
  hr {
    margin: 0 0 5px 0;
  }

  .modal-body {
    padding: 15px 20px;
  }

  .search-wrapper-hr {
    margin: 10px 0 5px 0;
  }
  
  .modal-body {
    max-height: 70vh;
    overflow-y: scroll;
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
