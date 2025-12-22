<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper" :class="{ 'content-expanded': sidebarVisible && !isMobile }">
      <PageTitle
        :title="`${userName}${$t('receipt.common.detailTitleApproval')}`"
        :subtitle="`${$t('receipt.common.dateRange')}${filteredStartDate} ~ ${filteredEndDate}${$t('receipt.common.dateRangeEnd')}`"
        :show-back-button="true"
        @back="goBack"
      />

      <!-- 조회 결과 없을 때 -->
      <div v-if="noData" class="no-data-container">
        <v-img
          class="no-results-found"
          src="/img/common/state/001.png"
        ></v-img>
      </div>

      <!-- 데이터 있을 때만 테이블 표시 -->
      <div v-else-if="data.length > 0">
        <div class="d-flex align-items-center justify-content-end content-body-header">
          <span class="mr-2 all-click" @click="onSelectAllClick('2', $event)">{{ $t('receipt.common.allApprove') }}</span>
          <DefaultTextfield
            type="radio"
            name="approveAll" 
            class="approveAll" 
            id="approveAll"
            @change="toggleApproveAll"
            @click="onSelectAllClick('2', $event)"
            v-model="selectAllDecision"
            value="2" 
            size="small"
          />
          <span class="mr-2 all-click" @click="onSelectAllClick('3', $event)">{{ $t('receipt.common.allReject') || $t('receipt.common.allReject') }}</span>
          <DefaultTextfield
            type="radio"
            name="rejectAll" 
            class="rejectAll" 
            id="rejectAll"
            @change="toggleRejectAll"
            @click="onSelectAllClick('3', $event)"
            v-model="selectAllDecision"
            value="3" 
            size="small"
          />
        </div>
        <hr />

        <!-- 테이블 보기 (큰 화면에서만 보임) -->
        <table class="table mt-3 receipt-table" id="dataTable" v-if="!isMobile">
          <thead>
            <tr>
              <th>{{ $t('receipt.common.receiptCode') }}</th>
              <th>{{ $t('receipt.common.issueDate') }}</th>
              <th>{{ $t('receipt.common.totalPeople') }}</th>
              <th>{{ $t('receipt.common.type') }}</th>
              <th>{{ $t('receipt.common.reason') }}</th>
              <th>{{ $t('receipt.common.amount') }}</th>
              <th>{{ $t('receipt.common.receiptPhoto') }}</th>
              <th>{{ $t('receipt.common.approve') }}</th>
              <th>{{ $t('receipt.common.reject') }}</th>
              <th>{{ $t('receipt.common.rejectReason') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in data"
              :key="index"
              @click="(e) => {
                const t=e.target.tagName;
                if(t!=='A' && t!=='INPUT') openEditModal(item);
              }"
            >
              <td class="text-ellipsis align-center">{{ item.receiptCode }}</td>
              <td class="date-cell align-center">{{ item.date }}</td>
              <td class="align-center">{{ item.peopleCount }}</td>
              <td class="text-ellipsis">{{ item.type }}</td>
              <td class="text-ellipsis">{{ item.reason }}</td>
              <td class="text-ellipsis align-right">{{ formatAmount(item.amount) }}{{ $t('receipt.ceoReport.won') }}</td>
              <td class="text-ellipsis-picture" @click.stop>
                <a
                  href="#"
                  @click.stop.prevent="openPreviewModal(item.receipt)"
                  class="text-primary"
                >
                  {{ item.receiptName }}
                </a>
              </td>
              <td @click.stop>
                <input
                  type="radio"
                  :name="'decision' + index"
                  class="approveRadio"
                  v-model="item.decision"
                  value="2"
                  @change="updateDecision()"
                  @click.stop="onDecisionRadioClick(item, '2', $event)"
                />
              </td>
              <td @click.stop>
                <input
                  type="radio"
                  :name="'decision' + index"
                  class="rejectRadio"
                  v-model="item.decision"
                  value="3"
                  @change="updateDecision()"
                  @click.stop="onDecisionRadioClick(item, '3', $event)"
                />
              </td>
              <td @click.stop>
                <DefaultTextfield
                  size="full"
                  v-model="item.rejectReason"
                  :disabled="item.decision !== '3' && item.decision !== 3"
                />
              </td>
            </tr>
            <!-- 데이터 행이 8 개보다 적을 때 채워줄 Placeholder -->
            <tr
              v-for="n in fillerRows"
              :key="`ph-${n}`"
              class="placeholder-row"
              aria-hidden="true"
              :style="{ height: rowH + 'px' }"
            >
              <td v-for="i in 10" :key="i">&nbsp;</td>
            </tr>
          </tbody>
        </table>

        <!-- 카드 레이아웃 보기 (작은 화면에서만 보임) -->
        <div class="card-layout" v-if="isMobile">
          <div
            class="receipt-card"
            v-for="(item, index) in data"
            :key="index"
          >
            <header class="receipt-card__header">
              <p class="card-title">
                📝 {{ $t('receipt.common.postNumber') }} {{ item.receiptCode }} <br />
              </p>
              <div class="card-actions">
                <DefaultButton
                  size="small"
                  color="gray"
                  customHeight="24px"
                  @click.stop="openEditModal(item)"
                >
                  보기
                </DefaultButton>
              </div>
            </header>
            <div class="card-body">
              <p class="card-text">
                <strong class="label">{{ $t('receipt.common.peopleCount') }}</strong>
                <span  class="value" @click="togglePeopleList(item)">
                  {{ item.peopleCount }}명
                </span>
              </p>

              <p class="card-text">
                <strong class="label">{{ $t('receipt.common.typeLabel') }}</strong>
                <span  class="value">{{ item.type }}</span>
              </p>

              <p class="card-text">
                <strong class="label">{{ $t('receipt.common.reasonLabel') }}</strong>
                <span  class="value">{{ item.reason }}</span>
              </p>

              <p class="card-text">
                <strong class="label">{{ $t('receipt.common.dateLabel') }}</strong>
                <span  class="value">{{ item.date }}</span>
              </p>

              <p class="card-text">
                <strong class="label">{{ $t('receipt.common.amountLabel') }}</strong>
                <span  class="value amount">{{ formatAmount(item.amount) }}</span>
              </p>

              <p class="card-text">
                <strong class="label">{{ $t('receipt.common.amountPerPerson') }}</strong>
                <span  class="value">{{ calculateAmountPerPerson(item) }}</span>
              </p>
              <p class="card-text">
                <strong class="label">{{ $t('receipt.common.receiptPhotoLabel') }} </strong>
                <a class="card-text"
                  @click.prevent="openPreviewModal(item.receipt)"
                  style="cursor: pointer; color: blue;"
                >
                  {{ item.receiptName }}
                </a>
              </p>
              <p class="card-text">
                <strong class="label">{{ $t('receipt.common.decision') }} </strong>
                &nbsp;{{ $t('receipt.common.approve') }}
                <input
                  type="radio"
                  :name="'decision' + index"
                  class="approveRadio"
                  v-model="item.decision"
                  value="2"
                  @change="updateDecision()"
                  @click.stop="onDecisionRadioClick(item, '2', $event)"
                />
                {{ $t('receipt.common.reject') }}
                <input
                  type="radio"
                  :name="'decision' + index"
                  class="rejectRadio"
                  v-model="item.decision"
                  value="3"
                  @change="updateDecision()"
                  @click.stop="onDecisionRadioClick(item, '3', $event)"
                />
                <br>
              </p>
              <p class="card-text d-flex align-items-center">
                <strong class="label">{{ $t('receipt.common.rejectReason') }} </strong>
                <DefaultTextfield
                  v-model="item.rejectReason"
                  marginLeft="10px"
                  customHeight="32px"
                  size="full"
                  class="rejectReason ml-2"
                  :disabled="item.decision !== '3' && item.decision !== 3"
                  placeholder="사유 입력"
                  style="flex:1;"
                />
              </p>
            </div>
          </div>
        </div>

        <!-- 저장 버튼 -->
        <DefaultButton align="right" @click="save">
          저장
        </DefaultButton>

        <!-- 페이지네이션 (공통 컴포넌트) -->
        <DefaultPagination
          :currentPage="currentPage"
          :totalPages="totalPages"
          :visiblePageCount="visiblePageCount"
          @pageChange="onPageChange"
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

    <!-- 🆕 영수증 상세 모달 -->
    <ReceiptDetailViewModal
      :isVisible="historyModalVisible"
      :receiptId="editingReceiptId"
      @close="historyModalVisible = false"
      @updated="fetchDataFromServer(currentPage)" />
  </div>
</template>

<script setup>
/* ──────────────────────────────── import ──────────────────────────────── */
import { ref, computed, onMounted, watch, nextTick, defineProps } from 'vue'
import { useRouter, useRoute }             from 'vue-router'
import { ROUTES }                          from '@/config/menuConfig'
import { toast }                           from 'vue3-toastify'

import DefaultTextfield       from '@/components/common/textfield/DefaultTextfield.vue'
import DefaultButton          from '@/components/common/button/DefaultButton.vue'
import DefaultPagination      from '@/components/common/pagination/DefaultPagination.vue'
import DefaultFormRow from '@/components/common/DefaultFormRow.vue'
import ReceiptSidebar         from './ReceiptSidebar.vue'
import ReceiptDetailViewModal from '@/components/receipt/ReceiptDetailViewModal.vue'

import { usePreviewModal }    from '@/utils/preview-modal'
import ReceiptsSearchApi      from '@/api/receipt/ReceiptsSearchApi'
import ReceiptsRequestApi     from '@/api/receipt/ReceiptsRequestApi'
import { useAuthStore }       from '@/store/auth'
import { toDeptTeamDisplay }  from '@/utils/blankFormat.js'
import { APPROVAL_ROLE_LABELS, APPROVAL_ROLE, getApprovalRoleLabel, RECEIPT_STATUS_LABELS } from '@/constants/receiptConstants';

/* ──────────────────────────────── 상수 ──────────────────────────────── */
const MOBILE_BP         = 650          /* 화면폭 기준(px)                */
const MOBILE_PAGE_SIZE  = 4
const DESKTOP_PAGE_SIZE = 7

/* ──────────────────────────────── 반응형 상태 ──────────────────────────────── */
const isMobile = ref(window.innerWidth <= MOBILE_BP)
const pageSize = computed(() => (isMobile.value ? MOBILE_PAGE_SIZE : DESKTOP_PAGE_SIZE))
window.addEventListener('resize', () => { isMobile.value = window.innerWidth <= MOBILE_BP })

/* ──────────────────────────────── 라우터 & 스토어 ──────────────────────────────── */
const router = useRouter()
const route  = useRoute()
const auth   = useAuthStore()
const props = defineProps({ forceDelegate: { type: Boolean, default: false } })
const isDelegate = computed(() => props.forceDelegate || route?.meta?.delegate === true)

/* ──────────────────────────────── 파라미터 ──────────────────────────────── */
const filteredStartDate = ref('')
const filteredEndDate   = ref('')
const userId   = ref('')
const userName = ref('')

/* ──────────────────────────────── 화면 상태 ──────────────────────────────── */
const data             = ref([])
const noData           = ref(false)
const currentPage      = ref(1)
const totalPages       = ref(1)
const visiblePageCount = ref(5)

/* ──────────────────────────────── 모달/사이드바 ──────────────────────────────── */
const sidebarVisible      = ref(false)
const selectedItem        = ref(null)
const editingReceipt      = ref(null)
const editingReceiptId    = ref(null)
const historyModalVisible = ref(false)

/* ──────────────────────────────── 전체 승인/반려 ──────────────────────────────── */
const selectAllDecision = ref('')

/* ──────────────────────────────── 프리뷰 모달 util ──────────────────────────────── */
const {
  isPreviewVisible, previewImage,
  isZoomed, zoomedPosition, zoomOrigin,
  openPreviewModal, toggleZoom,
  startDrag, onDrag, endDrag
} = usePreviewModal()

/* ──────────────────────────────── 포매터 & 헬퍼 ──────────────────────────────── */
const formatAmount = n => (+n || 0).toLocaleString()
const calculateAmountPerPerson = item =>
  item.peopleCount
    ? `${Math.floor(item.amountRaw / item.peopleCount).toLocaleString()} 원`
    : 'N/A'

/* ──────────────────────────────── watch (날짜 검증) ──────────────────────────────── */
watch(filteredStartDate, v => { if (v > filteredEndDate.value) filteredEndDate.value = v })
watch(filteredEndDate,   v => { if (v < filteredStartDate.value) filteredStartDate.value = v })

/* ──────────────────────────────── 데이터 매핑 ──────────────────────────────── */
function mapRow(raw) {
  const peopleArr = raw.participantsList || []
  const peopleCnt = peopleArr.length + 1
  const amountRaw = +(`${raw.amount}`.replace(/[^0-9]/g, '')) || 0

  return {
    /* 표 본문 */
    receiptId : raw.receiptId,
    receiptCode : raw.receiptCode,
    date      : raw.submissionDate,
    type      : raw.category?.categoryName ?? '',
    categoryId: raw.category?.categoryId ?? null,
    reason    : raw.reason,
    amount    : amountRaw.toString(),
    amountRaw,
    receiptName: raw.attachment?.fileName || '영수증',
    receipt     : raw.attachment?.fileUrl  || '',
    participants: peopleArr.map(p => ({
      userId: p.participantUserId, // ID도 함께 넘겨주면 좋습니다.
      name: p.participantName,
      department: toDeptTeamDisplay(p.department),
      team      : toDeptTeamDisplay(p.team),
      participantType: p.participantType || p.type || null,
      company   : p.company || null,
      position  : p.position || null,
      phone     : p.phone || null
    })),
    peopleCount     : peopleCnt,
    amountPerPerson : peopleCnt ? Math.floor(amountRaw / peopleCnt) : 0,
    /* 결재라인 */
    approvers: (raw.approvalLines || []).map(a => ({
      userId   : a.approverUserId,
      name     : (a.delegateMapping ? ('[대리] ' + (a.delegateMapping.delegateName || a.approverName)) : a.approverName),
      department: toDeptTeamDisplay(a.delegateMapping ? a.delegateMapping.delegateDepartment : a.department),
      team      : toDeptTeamDisplay(a.delegateMapping ? a.delegateMapping.delegateTeam : a.team),
      approvalRole : a.approvalRole,          // ← 추가
      isDefault    : a.approvalRole === 3,    // ← 추가(선택)  
      approvalType : getApprovalRoleLabel(a.approvalRole),   // 1=결재, 2·3=합의
      stateText    : a.rejectedAt      ? RECEIPT_STATUS_LABELS.REJECTED
                   : a.approvalStatus  ? RECEIPT_STATUS_LABELS.APPROVED
                                       : RECEIPT_STATUS_LABELS.WAITING,
      rejectedReason: a.rejectedReason
    })),
    statusText    : raw.status ?? '',
    rejectedReason: '',
    /* 결재(저장용) */
    decision     : '',
    rejectReason : ''
  }
}

/* ──────────────────────────────── 서버 통신 ──────────────────────────────── */
async function fetchDataFromServer(page = 1) {
  sidebarVisible.value = false

  const params = {
      userId     : userId.value,
      startDate  : filteredStartDate.value,
      endDate    : filteredEndDate.value,
      // statusCodes: ['REQUEST'],
      roles: [1],
      page       : page - 1,
      size       : pageSize.value
    }

  const { data: dto } = isDelegate.value
    ? await ReceiptsSearchApi.getMyApprovalPendingByDateAsDelegate(auth.getUserId, params)
    : await ReceiptsSearchApi.getMyApprovalPendingByDate(auth.getUserId, params)

  data.value       = (dto.content || []).map(mapRow)
  totalPages.value = dto.totalPages || 1
  noData.value     = data.value.length === 0

  if (isMobile.value) window.scrollTo(0, 0)
}

/* ──────────────────────────────── 페이지 이동 ──────────────────────────────── */
function onPageChange(page) {
  currentPage.value = page
  fetchDataFromServer(page)
}

/* ──────────────────────────────── 승인/반려 제어 ──────────────────────────────── */
function toggleApproveAll() {
  data.value.forEach(r => {
    r.decision = selectAllDecision.value === '2' ? '2' : ''
    if (r.decision !== '3') r.rejectReason = ''     // 사유 초기화
  })
}
function toggleRejectAll() {
  data.value.forEach(r => {
    r.decision = selectAllDecision.value === '3' ? '3' : ''
    /* 반려 ALL 선택일 때는 그대로 두고, 해제되면 사유도 제거 */
    if (r.decision !== '3') r.rejectReason = ''
  })
}
function updateDecision() {
  /* 선택이 ‘승인’(2) 으로 바뀌면 반려 사유 제거 */
  data.value.forEach(r => {
    if (r.decision === '2' || r.decision == 2) r.rejectReason = ''
  })
  const allA = data.value.length && data.value.every(r => r.decision === '2')
  const allR = data.value.length && data.value.every(r => r.decision === '3')
  selectAllDecision.value = allA ? '2' : allR ? '3' : ''
}

/* 라디오: 같은 값을 다시 클릭하면 해제되도록 처리 */
function onDecisionRadioClick(row, value, e) {
  const targetValue = String(value)
  const current = row.decision != null ? String(row.decision) : ''
  if (current === targetValue) {
    row.decision = ''
    if (targetValue === '3') {
      row.rejectReason = ''
    }
  } else {
    row.decision = targetValue
    if (targetValue === '2') {
      row.rejectReason = ''
    }
  }
  updateDecision()
}

/* 전체 승인/반려 라디오도 같은 값 재클릭시 해제 */
function onSelectAllClick(value, e) {
  const v = String(value)
  const isSame = selectAllDecision.value === v
  selectAllDecision.value = isSame ? '' : v

  if (isSame) {
    /* 해제 시, 해당 값으로 선택된 항목만 해제 */
    if (v === '2') {
      data.value.forEach(r => {
        if (r.decision == 2 || r.decision === '2') r.decision = ''
      })
    } else if (v === '3') {
      data.value.forEach(r => {
        if (r.decision == 3 || r.decision === '3') {
          r.decision = ''
          r.rejectReason = ''
        }
      })
    }
    updateDecision()
  } else {
    /* 선택 시 기존 로직 재사용 */
    if (v === '2') toggleApproveAll()
    else if (v === '3') toggleRejectAll()
  }
}

/* ──────────────────────────────── 저장 ──────────────────────────────── */
async function save() {
  /* ── ‘반려’ 사유 입력 여부 검사 ───────────────── */
  const missing = data.value.find(r =>
      (r.decision === '3' || r.decision == 3) &&
      !(r.rejectReason?.trim())
  )
  if (missing) {
    toast.warning('반려 사유를 입력해주세요.')
    return
  }

  const payload = data.value
    .filter(r => r.decision == 2 || r.decision == 3   // 🔸 느슨비교(==)
         || r.decision === '2' || r.decision === '3')
    .map(r => ({
      receiptId   : r.receiptId,
      statusCode  : r.decision,
      rejectReason: r.rejectReason,
      approverId  : auth.getUserId,
      approverName: auth.getUser
    }))

  if (!payload.length) return toast.warning('승인 또는 반려된 항목이 없습니다.')

  await ReceiptsRequestApi.saveReceiptDecisions(userId.value, payload)
  toast.success('저장되었습니다.');
  selectAllDecision.value = '';
  fetchDataFromServer(currentPage.value)
}

/* ──────────────────────────────── 상세 모달 / 모바일 상세 ──────────────────────────────── */
watch(isMobile, v => { if (v) historyModalVisible.value = false })
function openEditModal(row) {
  const id = row.receiptId
  if (isMobile.value) {
    try { sessionStorage.setItem('receiptDetail', JSON.stringify({ id })) } catch (e) {}
    router.push(ROUTES.RECEIPT.DETAIL_MOBILE)
    return
  }
  editingReceiptId.value    = id
  historyModalVisible.value = true
}

/* ──────────────────────────────── 카드 인원 토글 ──────────────────────────────── */
const openedIndex = ref(null)
function togglePeopleList(row) { openedIndex.value = openedIndex.value === row ? null : row }
function showPeopleList(row)   { return openedIndex.value === row }

/* ──────────────────────────────── 이미지 미리보기 ──────────────────────────────── */
function closePreviewModalOnOutsideClick(e) {
  if (!e.target.classList.contains('preview-modal-image')) isPreviewVisible.value = false
}

/* ──────────────────────────────── 네비게이션 ──────────────────────────────── */
function goBack() {
  /* 뒤로가기 전에 모달/사이드바 확실히 닫기 */
  historyModalVisible.value = false
  sidebarVisible.value      = false
  isPreviewVisible.value    = false
  router.back()
}

/* ──────────────────────── 8개 이하 테이블 더미데이터 ──────────────────────── */
const rowH = ref(0)

/* 데스크톱 테이블이 8 행보다 적으면 빈 행을 채우기 위한 계산 */
const fillerRows = computed(() => {
  /* 모바일 카드 레이아웃일 때는 필요 없음 */
  if (isMobile.value) return 0
  const diff = DESKTOP_PAGE_SIZE - data.value.length
  return diff > 0 ? diff : 0
})
async function syncRowHeight () {
  await nextTick()                        // DOM 렌더 완료 후
  const tr = document.querySelector('#dataTable tbody tr:not(.placeholder-row)')
  rowH.value = tr ? tr.offsetHeight : 0
}
/* 데이터·창크기 변화마다 재계산 */
watch(data,    syncRowHeight, { deep:true })
watch(isMobile,syncRowHeight)
onMounted(     syncRowHeight)
window.addEventListener('resize', syncRowHeight)

/* ──────────────────────────────── 초기화 ──────────────────────────────── */
onMounted(() => {
  filteredStartDate.value = route.query.startDate || ''
  filteredEndDate.value   = route.query.endDate   || ''
  userId.value            = route.query.userId    || ''
  userName.value          = route.query.userName  || ''

  if (!userId.value) return goBack()              // 파라미터 이상 → 이전 페이지

  fetchDataFromServer()
})
</script>


<style scoped>
/* .mt-3 {
  margin-top: 0px !important;
} */
#dataTable tbody tr:hover td {
  background-color: #e7f1ff !important;
}
#dataTable td {
  vertical-align: middle;
  text-align: left;
}

hr {
  margin: 10px 0px 10px 0px;
}

/* 테이블 본문 폰트 사이즈 */
tbody {
  font-size: var(--table-body-font-size, 0.75rem);
}

.content {
  transition: margin-right 0.3s ease;
}

.content-sub-title {
  margin-bottom: 10px !important;
}

.content-expanded {
  margin-right: 300px;
}

.content-body-header {
  margin-top: 0px;
}

.text-primary {
  color: #c7573e;
  /* 원하는 하이퍼링크 색상으로 변경 */
  text-decoration: none;
  /* 하이퍼링크 밑줄 제거 */
}

/* 전체 승인/반려 */
.all-click {
  font-weight: bold;
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

.approveRadio,
.rejectRadio {
  /* 축소 기준점을 중앙으로 설정 */
  transform-origin: center center;
  /* 수직 정렬 (inline 요소가 text-baseline이 아니라 middle에 맞춰지도록 함) */
  vertical-align: middle;
}

/* 노데이터 이미지 컨테이너 */
.no-data-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px; /* 모바일 기본값 */
  min-height: 300px; /* 모바일 기본값 */
}

.no-results-found {
  max-width: 400px; /* 모바일 기본값 */
  width: 80%;
  height: auto;
  object-fit: contain;
}

/* 데스크탑에서 노데이터 이미지 영역 설정 */
@media (min-width: 651px) {
  .no-data-container {
    height: 500px;
    width: 100%;
    margin: 80px 0 0 0;
    padding: 0;
    background: #ffffff;
    border: 1px solid #e5e7eb;
    border-radius: 0;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
    position: relative;
  }
  
  .no-results-found {
    max-width: 500px;
    width: 50%;
    opacity: 0.8;
  }
}

.text-ellipsis {
  max-width: 60px;        /* 원하는 최대 폭 – 상황에 맞게 조절 */
  white-space: nowrap;     /* 줄바꿈 금지 */
  overflow: hidden;        /* 넘치는 글자 숨김  */
  text-overflow: ellipsis; /* ‘…’ 표시       */
}
.text-ellipsis-picture {
  max-width: 100px;        /* 원하는 최대 폭 – 상황에 맞게 조절 */
  white-space: nowrap;     /* 줄바꿈 금지 */
  overflow: hidden;        /* 넘치는 글자 숨김  */
  text-overflow: ellipsis; /* ‘…’ 표시       */
}

.date-cell {
  min-width: 80px;      /* 80px 확보 */
  white-space: nowrap;  /* 줄바꿈 방지 */
}

#dataTable th {
  text-align: center;          /* 헤더 전체 중앙정렬 */
}

#dataTable th:nth-child(1),
#dataTable td.align-center {
  text-align: center;
}

#dataTable td input.approveRadio,
#dataTable td input.rejectRadio {
  display: block;
  margin: 0 auto;           /* 좌우 중앙 */
}

/* 우측 정렬 보조 클래스 ─ 금액 열에 사용 */
#dataTable td.align-right {
  text-align: right;
}

/* 금액 헤더도 우측 정렬 (6번째 컬럼) */
/* #dataTable th:nth-child(6) {
  text-align: right;
} */

/* 실제 데이터 행만 hover (placeholder-row 제외) */
#dataTable tbody tr:not(.placeholder-row):hover td {
  background-color: #e7f1ff !important;
}
/* (공통) 라벨 & 값 */
.label{               /* ‘총 인원 :’ 같은 앞부분 */
  font-weight: 700;
  color:#6d7575;      /* Charcoal ▸ 살짝 진함 */
  font-size: .78rem;
  line-height: 1.42;
  opacity: .8;
}
.value{               /* 실제 데이터 값 */
  font-weight: 400;
  color:#525a5a;      /* Charcoal-50 ▸ 연한 회색 */
}

/* 금액 전용 색 */
.value.amount{
  color:#c7573e;      /* 브랜드 오렌지(기존 text-primary) */
}

/* 인라인 배치 시 라벨·값 사이 간격 살짝 */
.label + .value{ margin-left:.25rem; }

/* ───────── style scoped – 더미 행 모양만 살짝 ───────── */
.placeholder-row td{
  background:#fafafa;
  /* border-left:1px solid #e0e0e0; */
  padding:8px;
}
.placeholder-row td:first-child{border-left:none;}

@media (max-width: 700px) {
  .approveRadio,
  .rejectRadio {
    /* 라디오 크기 축소 */
    transform: scale(0.7);
    /* 라디오가 작아지는 만큼 위치 조정이 필요하다면 여기에 마진 설정 가능 */
    margin: 0 2px;
  }
}

@media (max-width: 650px) {
  /* ─────────────────── 카드 자체 ─────────────────── */
  .receipt-card__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 6px;
    padding-bottom: 10px;             /* 실선 위 간격 */
    margin-bottom: 10px;             /* 실선 아래 간격 */
    border-bottom: 1px solid #e9eef3;/* 글번호/보기 ↔ 첫 줄 사이 실선 */
  }
  .receipt-card__header .card-title {
    font-size: 0.8rem;
    font-weight: 900;
    color: #203433;
    line-height: 1.35;
    opacity: .9;
  }
  .card-text {
    margin-bottom: 0;
    padding: 4px 0;
    font-size: 0.82rem;
    width: 100% !important;
    min-width: 0 !important;
    max-width: 100% !important;
  }
  /* 첫~둘째 구간(헤더 보더 ↔ 결정 보더) 데이터 폰트 축소 */
  .receipt-card .card-body .card-text:nth-of-type(-n+7):nth-of-type(n+2){
    font-size: 0.785rem;
  }
  /* 총 인원 라인(1번째)은 모바일에서 숨김 */
  .receipt-card .card-body > p.card-text:nth-of-type(1){
    display: none;
  }
  /* 결정(승인/반려) ↔ 영수증 사진 사이 실선 */
  .receipt-card .card-body .card-text:nth-of-type(8){
    border-top: 1px solid #e9eef3;
    margin-top: 10px;
    padding-top: 15px;
  }
  /* (삭제) 첫 줄에 실선 적용하던 규칙은 헤더 보더로 대체 */
  /* 1. .receipt-card = 심플 카드 (Overview와 동일 톤) */
  .receipt-card {
    display: block;
    position: relative;
    background: #fff;
    border: 1px solid #EEF0F3;
    box-shadow: 0 2px 8px rgba(25, 28, 33, 0.06);
    border-radius: 12px;
    padding: 16px;
    margin: 10px 0;
    transition: box-shadow .2s ease, transform .2s ease, border-color .2s ease, background-color .2s ease;
  }
  .receipt-card:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 14px rgba(25, 28, 33, 0.10);
    border-color: #E2E6EA;
    background-color: #FAFCFF;
  }
  /* 2. 왼쪽 스텁 제거 */
  .receipt-card::before {
    content: none !important;
  }
  /* 3. 오른쪽 ::after는 필요 없으므로 제거 */
  .receipt-card::after {
      display: none;
  }
  .rejectReason {
    width: 100%;
  }
  
  .content-body-header {
    margin-top: 0px;
  }
  /* -- 카드 컨테이너 자체가 숨겨져 있을 수도 있으니 한꺼번에 복원 */
  :where(.card-layout){
    display:block !important;   /* 혹시라도 display:none 이면 살리기 */
  }
  :where(.card-layout) :where(.card){
    display:flex  !important;   /* flex 로 복원 – block 으로 해도 무방 */
  }
}
</style>