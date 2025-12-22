<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('receipt.approvalSummary.title')"
        :subtitle="$t('receipt.management.subtitle')"
        icon="ri-checkbox-circle-line"
        desktopMarginBottom="20px"
        mobileMarginBottom="0px"
      />

      <!-- ───── 검색 영역 ───── -->
      <div class="search-controls">
        <!-- 시작/종료일 -->
        <DefaultFormRow align="right">
          <DefaultLabel :text="$t('receipt.approvalSummary.startDate')" forId="startDate" size="small" />
          <DefaultTextfield
            type="date"
            id="startDate"
            v-model="startDate"
            size="xsmall"
          />
          <DefaultLabel :text="$t('receipt.approvalSummary.endDate')" forId="endDate" size="small" marginLeft="10px" />
          <DefaultTextfield
            type="date"
            id="endDate"
            v-model="endDate"
            size="xsmall"
          />
        </DefaultFormRow>

        <!-- 이름 검색 -->
        <DefaultFormRow align="right" marginTop="7px">
          <DefaultLabel :text="$t('receipt.approvalSummary.nameLabel')" forId="nameSearch" size="small" />
          <UserSearchDropdown
            ref="userSearchRef"
            :labelText="$t('hrm.userManagement.search')"
            inputId="nameSearch"
            inputSize="large"
            :placeholder="$t('hrm.userManagement.namePlaceholder')"
            :includeCurrentUser="true"
            :initialValue="selectedUserSearchValue"
            @userSelected="onUserSelected"
          />
          <DefaultButton
            size="small"
            @click="search"
            color="gray"
          >
            {{ $t('receipt.approvalSummary.searchButton') }}
          </DefaultButton>
        </DefaultFormRow>
      </div>

      <!-- DefaultTable 컴포넌트 (페이지네이션 통합) -->
      <DefaultTable
        :columns="columns"
        :data="data"
        :mobileCard="true"
        :bodyFontSize="'0.7rem'"
        :rowClick="goToDetail"
        :usePagination="true"
        :currentPage="currentPage"
        :totalPages="totalPages"
        :visiblePageCount="visiblePageCount"
        @pageChange="onPageChange"
        :noDataImageHeight="476"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';

/* ─── 공용 컴포넌트 ─── */
import DefaultTable       from '@/components/common/table/DefaultTable.vue';
import DefaultFormRow     from '@/components/common/DefaultFormRow.vue';
import DefaultLabel       from '@/components/common/label/DefaultLabel.vue';
import DefaultTextfield   from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultButton      from '@/components/common/button/DefaultButton.vue';
import UserSearchDropdown from '@/components/auth/UserSearchDropdown.vue';
import { useAuthStore } from '@/store/auth'
import { toDeptTeamDisplay } from '@/utils/blankFormat.js'
import { useViewStateStore } from '@/store/viewState'

// API
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi'; 

// Vue Router
const router = useRouter();
const authStore = useAuthStore();
const viewState = useViewStateStore();

// 상태 변수
const data = ref([]);  // 최종 테이블/카드에 표시할 목록
const currentPage = ref(1);
const totalPages = ref(1);
const visiblePageCount = ref(5);
const isMobile = ref(false);

// 검색 조건
const startDate = ref('');
const endDate = ref('');
const searchUserId = ref(''); // UserSearchDropdown에서 선택된 유저ID
const selectedUserSearchValue = ref('');
const userSearchRef = ref(null);

// 조회된 날짜 범위(출력용)
const computedStartDate = ref('');
const computedEndDate = ref('');

// 모든 사용자 목록
const userList = ref([]);

// 테이블 컬럼 정의
const columns = ref([
  { key:'userName',       label:'이름',       width:150,
    mobile:{ line:1, inline:true,  bold:true, prefix:'👤\u00a0:\u00a0' } },

  { key:'departmentName', label:'부서',       width:150,
    mobile:{ dividerTop:true, dividerTopGapAbove:'5px', dividerTopGapBelow:'15px', line:2, inline:false, prefix:'부서\u00a0:\u00a0' } },

  { key:'teamName',       label:'팀',         width:150,
    mobile:{ line:2, inline:false, prefix:'팀\u00a0:\u00a0' } },

  { key:'userEmail',      label:'이메일',     width:'auto', minWidth: 100,
    mobile:{ line:1, inline:true,  align:'right' } },

  { key:'count',      label:'총 건수',    width:80,  align:'center',
    mobile:{ line:3, inline:false, prefix:'총 건수\u00a0:\u00a0' } },

  { key:'approved',   label:'승인 금액',  width:150, align:'right',
    mobile:{ line:4, inline:false, prefix:'승인\u00a0:\u00a0' } },

  { key:'closed',     label:'마감 금액',  width:150, align:'right',
    mobile:{ line:4, inline:false, prefix:'마감\u00a0:\u00a0' } },
]);

// ========== 서버에서 요약 데이터 가져오기 ==========
async function fetchDataFromServer(page = 1) {
  const pageSize = isMobile.value ? 4 : 10;
  const response = await ReceiptsSearchApi.getApprClosedSummaryAll({
    startDate: startDate.value,
    endDate:   endDate.value,
    userId:    searchUserId.value || undefined,
    page:      currentPage.value - 1,
    size:      pageSize
  });

  const pageData = response.data.pageData;
  
  data.value       = enrichSummaryData(pageData.content);
  totalPages.value = pageData.totalPages;

  // 조회된 날짜 범위 저장
  computedStartDate.value = startDate.value;
  computedEndDate.value   = endDate.value;

  // 스크롤 맨 위로 이동(모바일 카드형식일때)
  if (isMobile.value) {
    window.scrollTo(0, 0)
  }
}

// 요약 데이터에 사용자 정보(이름, 이메일, 부서, 팀) 매핑
function enrichSummaryData(summaryList) {
  return summaryList.map(item => ({
    ...item,
    departmentName: toDeptTeamDisplay(item.departmentName),
    teamName      : toDeptTeamDisplay(item.teamName)
  }));
}

// 페이지네이션
function onPageChange(newPage) {
  currentPage.value = newPage;
  fetchDataFromServer(newPage);
}

// 반응형
function updateViewMode() {
  isMobile.value = window.innerWidth <= 850;
}

// 금액 표시
function formatCurrency(amount) {
  if (!amount) return '0';
  return amount.toLocaleString();
}

// 날짜 제한 로직
watch(startDate, (newVal) => {
  if (newVal > endDate.value) {
    endDate.value = newVal;
  }
});
watch(endDate, (newVal) => {
  if (newVal < startDate.value) {
    startDate.value = newVal;
  }
});

// 상세 페이지 이동
function goToDetail(item) {
  // 상태 저장 후 이동
  viewState.saveState('receiptApprovalSummary', {
    startDate              : startDate.value,
    endDate                : endDate.value,
    searchUserId           : searchUserId.value,
    selectedUserSearchValue: selectedUserSearchValue.value,
    currentPage            : currentPage.value,
    scrollY                : window.scrollY,
  })
  router.push({
    name: 'ReceiptApprovalSummaryDetail',
    query: {
      userId: item.userId,
      userName: item.userName,
      startDate: startDate.value,
      endDate: endDate.value
    }
  });
}

// 날짜 자동 설정: YYYY-MM-DD 형태
function getTodayString() {
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

// UserSearchDropdown 선택 이벤트
function onUserSelected(user) {
  // user = { userId, email, name, … }
  searchUserId.value = user?.userId ?? '';
  selectedUserSearchValue.value = user?.label ?? '';  // "이름 (이메일)" 형식
}

/**
 * 조회 버튼 클릭 시 실행
 * - 저장된 상태를 초기화하여 새로운 조회 조건으로 검색
 */
function search() {
  currentPage.value = 1;          // 항상 1페이지부터
  viewState.clearState('receiptApprovalSummary');  // 기존 저장 상태 초기화
  fetchDataFromServer(1);
}

// 날짜 제한 로직(자동 조회 X)
watch(startDate, newVal => {
  if (newVal > endDate.value) endDate.value = newVal;
});
watch(endDate, newVal => {
  if (newVal < startDate.value) startDate.value = newVal;
});

// onMounted
onMounted(async () => {
  // 1) 기본 날짜(오늘)
  startDate.value = getTodayString();
  endDate.value = getTodayString();

  const saved = viewState.getState('receiptApprovalSummary')
  const restore = viewState.canRestore('receiptApprovalSummary')
  if (restore && saved) {
    startDate.value              = saved.startDate   || startDate.value
    endDate.value                = saved.endDate     || endDate.value
    searchUserId.value           = saved.searchUserId|| ''
    selectedUserSearchValue.value = saved.selectedUserSearchValue ?? ''
    currentPage.value            = saved.currentPage || 1
    await fetchDataFromServer(currentPage.value)
    requestAnimationFrame(()=>{ window.scrollTo(0, saved.scrollY ?? 0) })
  } else {
    // 2) 첫 조회
    fetchDataFromServer(currentPage.value);
  }

  // 3) 반응형 모드 설정
  updateViewMode();
  window.addEventListener('resize', updateViewMode);
});

// 상세에서만 복원 허용
viewState.allowRestoreFrom('receiptApprovalSummary', ['ReceiptApprovalSummaryDetail']);
</script>

<style scoped>
.search-controls {
  margin-top: 0px;
  margin-bottom: 10px;
}
.align-items-center {
  margin-bottom: 0px !important;
}

/* 반응형 테이블/카드 */
@media (min-width: 851px) {
  #nameSearch {
    font-size: 1rem;
  }
}

@media (max-width: 650px) {
  .content-sub-title {
    margin-bottom: 40px !important;
  }
  .btn-primary {
    font-size: 0.75rem;
    padding: 0.3rem 0.6rem;
    margin-bottom: 10px;
  }
}
</style>
