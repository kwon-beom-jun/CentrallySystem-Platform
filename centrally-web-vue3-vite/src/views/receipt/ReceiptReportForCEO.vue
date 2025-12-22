<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('receipt.ceoReport.title')"
        :subtitle="$t('receipt.ceoReport.subtitle')"
        icon="ri-file-chart-line"
        desktopMarginBottom="30px"
      />
      <DefaultFormRow align="between">
        <DefaultLabel
          :text="pastApprovedMonths.length > 0 ? ($t('receipt.closure.unsettledMonths') + ' ' + pastApprovedMonths.join(', ')) : ''"
          size="small"
          color="red"
        />
        <DefaultButton
          size="small"
          customHeight="30px"
          @click="onDownloadExcel()">
          {{ $t('receipt.ceoReport.excelDownload') }}
        </DefaultButton>
      </DefaultFormRow>

      <!-- ───── 검색 영역 ───── -->
      <div class="search-controls">
        <DefaultFormRow align="right" marginTop="7px">
          <DefaultSelect
            id="department"
            v-model="selectedDept"
            :options="deptOptions"
            :placeholder="$t('receipt.ceoReport.departmentAll')"
            size="full"
            style="width: 100%"
            marginRight="10px"
          />
          <DefaultSelect
            id="team"
            v-model="selectedTeam"
            :options="teamOptions"
            :placeholder="$t('receipt.ceoReport.teamAll')"
            size="full"
            style="width: 100%"
            :disabled="isTeamDisabled"
          />
        </DefaultFormRow>

        <DefaultFormRow marginTop="7px" gap="10px">
          <DefaultFormRow>
            <DefaultTextfield
              id="monthInput"
              type="month"
              size="full"
              style="width: 100%"
              v-model="monthInput"
            />
          </DefaultFormRow>
          <DefaultFormRow :growFirst="true" align="right">
            <DefaultTextfield
              id="nameSearch"
              v-model="keyword"
              :placeholder="$t('receipt.ceoReport.namePlaceholder')"
              size="full"
              style="width: 100%"
            />
          </DefaultFormRow>
        </DefaultFormRow>
      </div>

      <!-- 체크된 행 수 -->
      <DefaultLabel
        :text="`☑️ ${selectedCount} ${$t('receipt.common.count')}`"
        size="small"
        customClass="nowrap"
        marginBottom="5px"
      />
      <DefaultTable
        :columns="columns"
        :data="filteredData"
        :selectable="true"
        v-model:selectedRows="selectedRows"
        :mobileCard="false"
        :fixedHeader="true"
        :bodyFontSize="'0.7rem'"
        :rowClick="openDetailModal"
        :heightAdjust="-60"
        :noDataImageHeight="424"
        dynamic-style="424px"
      />
      <!-- 승인 금액 합계 -->
      <div class="summary-box">
        <DefaultFormRow align="right" gap="10px">
          <DefaultLabel :text="$t('receipt.ceoReport.totalApprovedAmount')"/>
          <DefaultLabel :text="formatCurrency(totalAppr)"/>
          <DefaultLabel :text="$t('receipt.ceoReport.won')"/>
        </DefaultFormRow>
      </div>
    </div>

    <!-- 모바일 상세 모달 -->
    <ReceiptReportForCEODetail
      v-if="isDetailModalVisible"
      :rowData="selectedRow"
      @close="isDetailModalVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

/* ─── 공용 컴포넌트 ─── */
import DefaultTable       from '@/components/common/table/DefaultTable.vue';
import DefaultFormRow     from '@/components/common/DefaultFormRow.vue';
import DefaultLabel       from '@/components/common/label/DefaultLabel.vue';
import DefaultTextfield   from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultButton      from '@/components/common/button/DefaultButton.vue';
import DefaultSelect      from '@/components/common/select/DefaultSelect.vue';
import UserSearchDropdown from '@/components/auth/UserSearchDropdown.vue';
import { toast } from "vue3-toastify";
import { toDeptTeamDisplay } from '@/utils/blankFormat.js'
import { useAuthStore } from '@/store/auth'
import ReceiptReportForCEODetail from '@/components/receipt/ReceiptReportForCEODetail.vue';
import { RECEIPT_STATUS } from '@/constants/receiptConstants';
import { useViewStateStore } from '@/store/viewState'

// API
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi'; 
import ReceiptsPrintApi from '@/api/receipt/ReceiptsPrintApi.js';

// Vue Router
const authStore = useAuthStore();
const viewState = useViewStateStore();

// 상태 변수
const data = ref([]);  // 최종 테이블/카드에 표시할 목록
const footerData = ref([]) // 체크박스 선택 시 계산하여 합계 도축
const isMobile = ref(false);
const keyword = ref('')        // ① ✨ 이름/메일 키워드
const selectedDept = ref('')
const selectedTeam = ref('')
// 선택된 행(체크박스)
const selectedRows = ref([]);

/* ─── 합계 계산 ─── */
const selectedCount = computed(() => selectedRows.value.length);
const totalAppr = ref(0);


// 과거 승인 월 목록을 저장할 상태 변수 추가
const pastApprovedMonths = ref([]);

// 검색 조건
const startDate = ref('');
const endDate = ref('');
const monthInput = ref('');


// 조회된 날짜 범위(출력용)
const computedStartDate = ref('');
const computedEndDate = ref('');

// 모든 사용자 목록
const userList = ref([]);

// 테이블 컬럼 정의
const columns = ref([
  { key:'userName',       label:'이름',       width:'auto', minWidth: 100,
    mobile:{ line:1, inline:true,  bold:true, prefix:'👤\u00a0:\u00a0' } },
  
  { key:'userEmail',      label:'이메일',     width:'auto', minWidth: 100, mobileDisable:true ,
    mobile:{ line:1, inline:true,  align:'right' } },

  { key:'departmentName', label:'부서',       width:100, mobileDisable:true ,
    mobile:{ line:2, inline:false, prefix:'부서\u00a0:\u00a0' } },

  { key:'teamName',       label:'팀',         width:100,
    mobile:{ line:2, inline:false, prefix:'팀\u00a0:\u00a0' } },

  { key:'count',      label:'총 건수',    width:80,  align:'center', mobileDisable:true ,
    mobile:{ line:3, inline:false, prefix:'총 건수\u00a0:\u00a0' } },

  { key:'approved',   label:'합계 금액',  width:80, align:'right', 
    customValue: (row) => formatCurrency(row.approved),
    mobile:{ line:4, inline:false, prefix:'합계 금액\u00a0:\u00a0' } },

  // { key:'closed',     label:'마감 금액',  width:100, align:'right',
  //   mobile:{ line:4, inline:false, prefix:'마감\u00a0:\u00a0' } },
]);

/* 부서 옵션: “전체” + 부서명 중복 제거 */
// 부서 옵션
const deptOptions = computed(() => {
  const names = new Set(data.value.map(u => u.departmentName).filter(Boolean))
  return [{ value: '', label: '부서 전체' }, ...Array.from(names).map(n => ({ value: n, label: n }))]
})

// 선택된 부서에 따른 팀 옵션 (ReceiptApprovalRequestOverview 로직 동일)
const teamOptions = computed(() => {
  if (!selectedDept.value) return []
  const names = new Set(
    data.value
      .filter(u => u.departmentName === selectedDept.value)
      .map(u => u.teamName)
      .filter(Boolean)
  )
  return Array.from(names).map(n => ({ value: n, label: n }))
})

// 팀 셀렉트 disabled 여부
const isTeamDisabled = computed(() => !selectedDept.value)
// 반응형 전환 시 초기화
const isMobileBp = ref(window.innerWidth <= 650)
window.addEventListener('resize', ()=>{ isMobileBp.value = window.innerWidth <= 650 })
watch(isMobileBp, (v, prev) => {
  if (v !== prev) {
    // 전역 초기화에 맡기고, 로컬은 UI만 리셋
    selectedDept.value = ''
    selectedTeam.value = ''
    keyword.value = ''
    window.scrollTo(0,0)
  }
})

// ========== 서버에서 요약 데이터 가져오기 ==========
async function fetchDataFromServer() {
    const pageSize = 100000;
    const response = await ReceiptsSearchApi.getApprClosedSummaryAll({
        startDate: startDate.value,
        endDate:   endDate.value,
        statusCodes: [RECEIPT_STATUS.APPROVED],
        page:        0,
        size:        pageSize
    });

    // 새로운 응답 DTO에서 데이터 분리
    const pageData = response.data.pageData;
    pastApprovedMonths.value = response.data.pastApprovedMonths;
    
    // pageData.content를 기반으로 userList 정보와 매핑
    data.value = enrichSummaryData(pageData.content);

    // 조회가 완료되면 부서 셀렉트박스를 기본값(전체)으로 초기화
    selectedDept.value = '';

    // 더미 데이터
    // addDummyRows(500);

    // 스크롤 맨 위로 이동(모바일 카드형식일때)
    if (isMobile.value) {
        window.scrollTo(0, 0)
    }
    selectedRows.value = [];
    updateFooter();
}

/* 더미 행 생성/삽입 함수 --------------------------- */
function addDummyRows(num) {
  if (!num) return;
  const dummy = Array.from({ length: num }).map((_, i) => {
    // 건수 1 ~ 5건
    const cnt = Math.floor(Math.random() * 5) + 1;
    // 승인 금액 1 ~ 9만 원 단위 × 건수 (ex. 30 000, 70 000 …)
    const apprUnit = (Math.floor(Math.random() * 9) + 1) * 10_000;
    const appr = cnt * apprUnit;
    return {
      userId    : -1 * (i + 1),            // 실제 ID와 겹치지 않도록 음수
      userName      : `더미${i + 1}`,
      userEmail     : `dummy${i + 1}@example.com`,
      departmentName: '더미부서',
      teamName      : '더미팀',
      count     : cnt,
      approved  : appr,                    // ✅ 승인 금액
    };
  });
  data.value.push(...dummy);
}
/* ------------------------------------------------------- */

/* ───────── 로컬 필터 ───────── */
const filteredData = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return data.value
    .filter(r => !selectedDept.value || r.departmentName === selectedDept.value) // 🔹 부서 필터
    .filter(r => !selectedTeam.value || r.teamName === selectedTeam.value)       // 🔹 팀 필터

    .filter(r =>
      (!kw) ||
      (r.userName  && r.userName.toLowerCase().includes(kw)) ||
      (r.userEmail && r.userEmail.toLowerCase().includes(kw))
    )
})

/* ───── 선택 행 ‧ 푸터 초기화 ───── */
function clearSelection() {
  selectedRows.value = [];
  updateFooter();
}

/* ─── 합계 계산 ─── */
watch(selectedRows, () => {
  totalAppr.value = selectedRows.value
    .reduce((s,r) => s + Number(r.approved||0), 0);
});


// 요약 데이터에 사용자 정보(이름, 이메일, 부서, 팀) 매핑
function enrichSummaryData(summaryList) {
  return summaryList.map(item => ({
    ...item,
    departmentName: toDeptTeamDisplay(item.departmentName),
    teamName      : toDeptTeamDisplay(item.teamName)
  }));
}

// 반응형
function updateViewMode() {
  isMobile.value = window.innerWidth <= 650;
}

// 금액 표시
function formatCurrency(amount) {
  if (!amount) return '0';
  return amount.toLocaleString();
}

/* 합계 계산 함수 */
function updateFooter() {
  const cnt   = selectedRows.value
                 .reduce((s,r) => s + Number(r.count   || 0), 0);
  const appr  = selectedRows.value
                 .reduce((s,r) => s + Number(r.approved|| 0), 0);

  footerData.value = [
    '총 합계', '', '', '',          // name·dept·team·email 컬럼용 빈칸
    // cnt.toLocaleString(),
    '',
    formatCurrency(appr)
  ];
}

/* 선택 변경될 때마다 합계 갱신 */
watch(selectedRows, updateFooter, { deep:true });

/** 월이 바뀔 때마다 → 날짜 범위 세팅 + 서버 조회 */
watch(
  monthInput,
  async (val) => {
    if (!val) return;
    // ① 날짜 범위 계산
    startDate.value = `${val}-01`;
    const [year, month] = val.split('-');
    const lastDay = new Date(year, month, 0).getDate();
    endDate.value = `${val}-${String(lastDay).padStart(2, '0')}`;
    // ② 바로 조회
    await fetchDataFromServer();
  },
  { immediate: true }          // ← 컴포넌트 처음 마운트될 때도 즉시 실행
);

// 날짜 자동 설정: YYYY-MM-DD 형태
function getTodayString() {
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}



/**
 * onDownloadExcel 함수를 백엔드 API 호출 방식으로 수정
 */
async function onDownloadExcel() {
  // dataToExport의 대상을 selectedRows.value로 변경
  const dataToExport = selectedRows.value.map(r => ({
    name      : r.userName,
    department: toDeptTeamDisplay(r.departmentName),
    team      : toDeptTeamDisplay(r.teamName),
    email     : r.userEmail,
    count     : r.count,
    approved  : r.approved
  }));
  if (dataToExport.length === 0) {
    return toast.warning("다운로드할 데이터가 없습니다.");
  }

  const response = await ReceiptsPrintApi.downloadCeoReport({
      month: monthInput.value,
      data: dataToExport
  });

  // 파일 다운로드 처리
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');

  // Content-Disposition 헤더에서 파일 이름 추출
  const contentDisposition = response.headers['content-disposition'];
  let fileName = `보고용_영수증_정산_${monthInput.value}.xlsx`; // 기본값
  if (contentDisposition) {
      // RFC 6266 표준을 더 잘 준수하는 정규식으로 교체
      const fileNameMatch = contentDisposition.match(/filename\*?=(?:UTF-8'')?([^;]+)/);
      if (fileNameMatch && fileNameMatch[1]) {
        // 추출된 파일명을 디코딩하고, 양쪽의 큰따옴표(")를 제거
        fileName = decodeURIComponent(fileNameMatch[1].replace(/"/g, ''));
      }
    }

  link.href = url;
  link.setAttribute('download', fileName);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

/* 이름 키워드가 바뀔 때 셀렉트박스 초기화 */
watch(keyword,  clearSelection);

/* 부서 셀렉트가 바뀔 때 셀렉트박스 초기화 */
watch(selectedDept, () => {
  selectedTeam.value = '';
  clearSelection();
  viewState.saveState('receiptReportForCEO', {
    keyword     : keyword.value,
    selectedDept: selectedDept.value,
    selectedTeam: selectedTeam.value,
    scrollY     : window.scrollY,
  })
});

watch(selectedTeam, clearSelection);


const isDetailModalVisible = ref(false);
const selectedRow = ref(null);

function openDetailModal(rowData) {
  // 모바일(650px 이하)에서만 모달 표시
  if (!isMobile.value) return;
  selectedRow.value = rowData;
  isDetailModalVisible.value = true;
}

onMounted(async () => {
  // 월 값만 초기화 → watch(monthInput)에서 자동 조회
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  monthInput.value = `${yyyy}-${mm}`;

  updateViewMode();
  window.addEventListener('resize', updateViewMode);
  const saved = viewState.getState('receiptReportForCEO')
  if (saved) {
    keyword.value      = saved.keyword ?? ''
    selectedDept.value = saved.selectedDept ?? ''
    selectedTeam.value = saved.selectedTeam ?? ''
    requestAnimationFrame(()=>{ window.scrollTo(0, saved.scrollY ?? 0) })
  }
});

</script>

<style scoped>
.content-sub-title {
  margin-bottom: 10px !important;
}
.search-controls {
  margin-bottom: 10px;
}
.align-items-center {
  margin-bottom: 0px !important;
}
.nowrap {
  white-space:nowrap;
}
.row-between-btm{
  align-items:flex-end !important;
}
.summary-box {
  margin-top: 15px;
}
/* 반응형 테이블/카드 */
@media (min-width: 851px) {
  #nameSearch {
    font-size: 1rem;
  }
}

@media (max-width: 650px) {
  .btn-primary {
    font-size: 0.75rem;
    padding: 0.3rem 0.6rem;
    margin-bottom: 10px;
  }
}
</style>
