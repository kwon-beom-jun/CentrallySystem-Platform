<template>
  <div class="content content-wrapper">
    <!-- 모바일 스크롤 타이틀 (스크롤 시 서브타이틀 사라짐) -->
    <PageTitle 
      :title="$t('receipt.meta.title')"
      subtitle="Receipt Meta Management"
      icon="ri-settings-3-line"
    />

    <!-- ───────── 기본 합의자(고정) 관리 구역 헤더 ───────── -->
    <div class="hr-label"><span>{{ $t('receipt.meta.fixedApproverManagement') }}</span></div>

    <!-- 🔹 기본-합의자(고정) 관리 패널 -->
    <section v-if="isAdmin" class="soft-box">
      <DefaultFormRow align="between" marginBottom="5px">
        <DefaultLabel
          :text="$t('receipt.meta.fixedApproverManagement')"
          customClass="hr-label"
        />
        <!-- +고정 버튼 → 사용자 검색 드롭다운 열기 -->
        <DefaultButton
          class="btn-xs square me-2"
          @click="showSearch = !showSearch"
        >
          {{ $t('receipt.meta.fixedApproverRegister') }}
        </DefaultButton>
      </DefaultFormRow>

      <!-- 
        영수증 서비스만 국한되게 조회됨 제거시 전체 서비스
        :filterBy="{ service: 'receipt',
            'ROLE_RECEIPT_APPROVER',
            ...
      -->
      <!-- 검색 드롭다운 (toggle) -->
      <UserSearchDropdown
        v-if="showSearch"
        class="mt-2"
        :keepSearchValue="false"
        :placeholder="$t('receipt.meta.searchPlaceholder')"
        inputSize="full"
        :includeCurrentUser="true"
        @userSelected="addFixedUser"
        :filterBy="{
          roleDetails: [
            ...getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
            'ROLE_GATE_SYSTEM'] 
        }"
      />
      <hr />
      <ul
        v-if="fixedApprovers.length === 0" 
        class="list-group list-no-group mb-2"
      >
        <li class="list-group-item text-center text-muted bg-light no-default-fixed-approvers">
          {{ $t('receipt.meta.noFixedApprovers') }}
        </li>
      </ul>
      <!-- draggable 리스트 -->
      <draggable
        v-model="fixedApprovers"
        item-key="userId"
        tag="ul"
        class="list-group mt-2"
        handle=".drag-handle"
        @end="saveOrder"
      >
        <template #item="{ element, index }">
          <li
            class="list-group-item d-flex align-items-center justify-content-between draggable-item"
          >
            <DefaultFormRow>
              <span class="drag-handle me-2">≡</span>
              <div class="draggable-item-value">
                <div class="first-line">
                  <DefaultLabel
                    :text="`${index + 1}. ${element.userName} [${element.email}]`"
                    size="small"
                    marginLeft="10px"
                  />
                </div>
                <div class="second-line">
                  <DefaultLabel
                    :text="`${element.department} - ${element.team}`"
                    size="small"
                  />
                </div>
              </div>
            </DefaultFormRow>

            <button
              type="button"
              class="btn btn-sm btn-outline-danger square-btn"
              @click.stop="removeFixedUser(index)"
            >
              ×
            </button>
          </li>
        </template>
      </draggable>
    </section>



    <!-- ───────── 카테고리 관리 구역 헤더 ───────── -->
    <div class="hr-label"><span>{{ $t('receipt.meta.categoryManagement') }}</span></div>

    <section class="soft-box">
      <DefaultFormRow align="between" marginBottom="5px">
        <DefaultLabel
          :text="$t('receipt.meta.categoryManagement')"
          customClass="hr-label"
        />
        <DefaultButton
          v-if="isAdmin"
          @click="showModal"
        >
          {{ $t('receipt.meta.categoryRegister') }}
        </DefaultButton>
      </DefaultFormRow>

      <!-- 카테고리 테이블 -->
      <DefaultTable
        :columns="columns"
        :data="categoryData"
        @delete-row="handleDelete"
        :fixedHeader="true"
        :scroll="true"
        :scrollHeight="320"
        :rowClick="handleRowClick"
        :selectHeight="'28px'"
        :buttonHeight="'28px'"
        :minRows="5"
        :noDataImageHeight="309"
      />
    </section>

    <!-- 등록/수정 모달 -->
    <ReceiptsCategoryCreateModal
      v-model:isVisible="isModalVisible"
      :isCreate="isCreate"
      :form="form"
      @save="handleSave"
    />

    <ConfirmationModal
        :isVisible="isDeleteModalVisible"
        :title="$t('receipt.meta.deleteConfirmTitle')"
        :confirmText="$t('common.button.delete')"
        :cancelText="$t('common.button.cancel')"
        :disableBackgroundClose="true"
        @confirm="confirmDelete"
        @close="isDeleteModalVisible = false"
    >
      <template #body>
        <div v-if="deleteInfo.type === 'category'">
          <strong>{{ $t('receipt.meta.categoryLabel') }}</strong>
          <p class="mt-2">'{{ deleteInfo.target?.category }}' {{ $t('receipt.meta.deleteCategoryMessage') }}</p>
        </div>
        <div v-if="deleteInfo.type === 'approver'">
          <strong>{{ $t('receipt.meta.approverLabel') }}</strong>
          <p class="mt-2">'{{ deleteInfo.target?.userName }}' {{ $t('receipt.meta.deleteApproverMessage') }}</p>
        </div>
        <p class="text-danger mt-3" style="font-size: 0.8rem;">{{ $t('receipt.meta.deleteWarning') }}</p>
      </template>
    </ConfirmationModal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import draggable from 'vuedraggable'
import UserSearchDropdown from '@/components/auth/UserSearchDropdown.vue'
import DefaultApproverApi from '@/api/receipt/ReceiptsDefaultApproverApi'
import DefaultFormRow from "@/components/common/DefaultFormRow.vue";
import DefaultLabel from "@/components/common/label/DefaultLabel.vue";
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTable  from '@/components/common/table/DefaultTable.vue';
import ConfirmationModal from '@/components/common/modal/AlertModal.vue';
import ReceiptsCategoryCreateModal from '@/components/receipt/ReceiptsCategoryCreateModal.vue';
import ReceiptsCategoryApi from '@/api/receipt/ReceiptsCategoryApi';
import { useHrmStore } from '@/store/hrm';
import { toast } from 'vue3-toastify';
import { getRolesFrom } from '@/utils/roleUtils';
import { ROLE_GROUPS } from '@/config/roleConfig';
import { toDeptTeamDisplay } from '@/utils/blankFormat.js'
import { useViewStateStore } from '@/store/viewState'

/* ───── 상태 ───── */
const { t } = useI18n();
const hrmStore         = useHrmStore();
const viewState        = useViewStateStore();
const isAdmin          = ref(false);
const isModalVisible   = ref(false);
const isCreate         = ref(true);
const categoryData     = ref([]);
const form             = ref({ id:null, category:'', maxAmount:0 });
// 삭제 확인 모달 관련 상태 변수
const isDeleteModalVisible = ref(false);
const deleteInfo = ref({ target: null, type: '' }); // 삭제할 대상의 정보 (데이터, 타입)


/* ───── 테이블 컬럼 ───── */
const columns = computed(() => [
  { key:'category',   label:t('receipt.meta.category'),          width:'auto', minWidth: 100,  align: 'center' },
  { key:'maxAmount',  label:t('receipt.meta.maxAmount'),  width:'auto', minWidth: 100,  align: 'center' },
  {
    key:'delete',     label:'', width:85, type:'button',
    buttonText:t('common.button.delete'), buttonColor:'red', buttonSize:'full-small',
    emit:'delete-row', vIf: () => isAdmin.value               // ← 관리자인 경우만 렌더
  }
]);

/* ───── 데이터 로딩 ───── */
async function fetchCategories() {
  const { data } = await ReceiptsCategoryApi.getCategories();
  categoryData.value = data.map(c => ({
    id:         c.categoryId,
    category:   c.categoryName,
    maxAmount:  c.limitPrice
  }));
}

/* ───── 버튼 & 콜백 ───── */
function showModal() {
  isCreate.value = true;
  form.value = { id:null, category:'', maxAmount:0 };
  isModalVisible.value = true;
}

/* 행 클릭 → 수정 모달 */
function handleRowClick(row) {
  if (!isAdmin.value) return;               // (선택) 관리자만 수정
  isCreate.value = false;                   // 수정 모드
  form.value = {                            // 선택 행 데이터 주입
    id        : row.id,
    category  : row.category,
    maxAmount : row.maxAmount
  };
  isModalVisible.value = true;              // 모달 열기
  // 현재 스크롤 저장 (뒤로가기로 돌아올 때 복원)
  viewState.saveState('receiptMetaManagement', { scrollY: window.scrollY })
}

/* 모달 저장 완료 */
async function handleSave() {
  await fetchCategories();
  isModalVisible.value = false;
}

/* 카테고리 삭제 버튼 클릭 시 모달 오픈 */
function handleDelete(row) {
  deleteInfo.value = { target: row, type: 'category' };
  isDeleteModalVisible.value = true;
}

/* ───────────────────────── 고정 합의자(Pinned Approver) 상태 ───────────────────────── */
const fixedApprovers = ref([])          // [{userId, name, …, order}, …]
const showSearch     = ref(false)       // ‘+고정’ 드롭다운 토글

/* 최초로드 */
async function fetchFixedApprovers () {
  const { data } = await DefaultApproverApi.getDefaultApprovers({size:1000})
  fixedApprovers.value = (data.content ?? data)
    .map(u=>({
      ...u,
      department: toDeptTeamDisplay(u.department),
      team      : toDeptTeamDisplay(u.team)
    }))
    .sort((a,b)=>a.stepNo-b.stepNo)
}

/* +고정 버튼으로 선택 */
async function addFixedUser (user) {
  const exists = fixedApprovers.value.some(u=>u.userId===user.userId)
  if (exists) return toast.warning('이미 고정돼 있습니다')

  // ① 서버 저장
  await DefaultApproverApi.createDefaultApprovers({
    userId: user.userId,
    userName: user.name,
    email: user.email,
    department: toDeptTeamDisplay(user.department),
    team: toDeptTeamDisplay(user.team),
    stepNo: fixedApprovers.value.length + 1
  })

  // ② 화면 추가
  fixedApprovers.value.push({
    userId: user.userId,
    userName: user.name,
    email: user.email,
    department: toDeptTeamDisplay(user.department),
    team      : toDeptTeamDisplay(user.team),
    stepNo: fixedApprovers.value.length + 1
  })
  showSearch.value = false
  toast.success('고정 목록에 추가되었습니다')
}

/* 고정 합의자 삭제 버튼 클릭 시 모달 오픈 */
function removeFixedUser (idx) {
  const target = fixedApprovers.value[idx];
  deleteInfo.value = { target, type: 'approver' };
  isDeleteModalVisible.value = true;
}

/* 드래그 후 순서 저장 */
async function saveOrder () {
  // 화면 배열 순서대로 order 재계산
  fixedApprovers.value.forEach((u,i)=>u.stepNo=i+1)

  // PATCH → [{userId, order}, …] 형태로 전송한다고 가정
  await DefaultApproverApi.reorderDefaultApprovers(
    fixedApprovers.value.map(({userId,stepNo})=>({userId,stepNo}))
  )
  toast.success('순서가 저장되었습니다')
}

/* ───────────────────────── 고정 합의자, 카테고라 삭제 ───────────────────────── */
// 모달에서 '삭제' 버튼 클릭 시 실행될 최종 삭제 함수
async function confirmDelete() {
  const { target, type } = deleteInfo.value;
  if (!target) return;

  try {
    if (type === 'category') {
      await ReceiptsCategoryApi.deleteCategory(target.id);
      toast.success('카테고리를 삭제했습니다');
      await fetchCategories(); // 목록 새로고침
    } else if (type === 'approver') {
      await DefaultApproverApi.deleteDefaultApprovers(target.userId);
      toast.success('고정 합의자를 삭제했습니다');
      await fetchFixedApprovers(); // 목록 새로고침
    }
  } catch (e) {
    toast.error('삭제 중 오류가 발생했습니다.');
    console.error(e);
  } finally {
    // 모달 닫기 및 상태 초기화
    isDeleteModalVisible.value = false;
    deleteInfo.value = { target: null, type: '' };
  }
}

/* 페이지 진입 시 불러오기 */
onMounted(async ()=>{
  isAdmin.value = hrmStore.isHrmAdmin
  const saved = viewState.getState('receiptMetaManagement')
  const restore = viewState.canRestore('receiptMetaManagement')
  await Promise.all([ fetchCategories(), fetchFixedApprovers() ])
  // 복원 가능한 경우에만 스크롤 복원
  if (restore && saved && typeof saved.scrollY === 'number') {
    setTimeout(()=>{ window.scrollTo(0, saved.scrollY) }, 100)
  } else {
    // 새로 진입한 경우 맨 위로
    window.scrollTo(0, 0)
  }
})

/* ───── 초기화 ───── */
// onMounted(async () => {
//   isAdmin.value = hrmStore.isHrmAdmin;
//   await fetchCategories();
// });
</script>

<style scoped>
.hr-label{
  display:flex;
  align-items:center;
  margin:28px 0 14px;          /* 위·아래 간격 */
  font-weight:600;
  font-size:1rem;
  color:#333;
}
.hr-label::before,
.hr-label::after{
  content:"";
  flex:1;
  border-top:1px solid #d7d7d7;/* 가로줄 */
}
.hr-label span{                /* 가운데 글자 */
  white-space:nowrap;
  margin:0 12px;
}
.soft-box{
  background:#f9f9f9;     /* 흐린 회색 (원하면 더 흐리게 #fcfcfc) */
  border:1px solid #ededed;
  border-radius:6px;
  padding:14px 16px 30px 16px;
  margin-bottom: 50px;     /* 뒤 구역과 간격 */
}
.section-title {
  margin: 0;
  font-size: 1rem;
}
.drag-handle {
  cursor: grab;
  user-select: none;
}
.drag-handle:active {
  cursor: grabbing;
}
.square-btn {
  width: 15px;
  height: 15px;
  font-size: 0.6rem;
  padding: 0;
  border-radius: 3px;
}
.btn-xs.square {
  border-radius: 3px;
  padding: 1px 6px;
  font-size: 0.7rem;
}

.draggable-item {
  margin-bottom: 0px !important;
  padding: 10px 16px 10px 16px;
}

.draggable-item-value .first-line,
.draggable-item-value .second-line {
  display: inline-block;        /* ✅ 기본 한 줄로 이어붙임 */
}

@media (max-width: 650px) {
  .hr-label{
    font-size: 0.8rem !important;
  }
  .no-default-fixed-approvers {
    font-size: 0.7rem !important;
    padding: 21.5px;
  }
  .soft-box{
    margin-bottom: 30px;
  }
  .draggable-item-value {
    display: flex;
    flex-direction: column;
  }

  .draggable-item-value .first-line {
    font-weight: 600;
    font-size: 0.8rem;
  }

  .draggable-item-value .second-line {
    color: #666;
    font-size: 0.75rem;
    margin-top: 2px;
    margin-left: 22px;    /* ← 두 번째 줄 왼쪽 간격 띄우기 */
  }
}

</style>