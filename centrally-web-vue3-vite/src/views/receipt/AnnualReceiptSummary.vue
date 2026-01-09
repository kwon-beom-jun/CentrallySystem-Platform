<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('receipt.annualSummary.title')"
        :subtitle="$t('receipt.annualSummary.subtitle')"
        icon="ri-calendar-2-line"
      />

      <DefaultFormRow marginBottom="10px" align="left">
        <!-- 연도 입력: type="year" (실제로 내부에서 number 처리) -->
        <DefaultTextfield
        type="number"
        id="yearInput"
        v-model="selectedYear"
        size="small"
        @change="onYearChange"
        />
        <DefaultLabel :text="$t('receipt.annualSummary.yearSelect')" forId="yearInput" marginLeft="10px" size="medium" />
      </DefaultFormRow>

      <!-- 카드 그리드 레이아웃 -->
      <div class="annual-cards-container">
        <!-- 월별 카드 -->
        <div class="monthly-cards-grid">
          <div 
            v-for="monthData in tableData" 
            :key="monthData.monthLabel"
            class="month-card"
            @click="goToMonthDetail(monthData.monthLabel)"
          >
            <div class="month-header">
              <h3>{{ monthData.monthLabel }}</h3>
            </div>
            <div class="month-stats">
              <div class="stat-item">
                <span class="stat-label">{{ $t('receipt.annualSummary.request') }}</span>
                <span class="stat-value primary">{{ monthData.requestStr }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">{{ $t('receipt.annualSummary.approved') }}</span>
                <span class="stat-value success">{{ monthData.approvedStr }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">{{ $t('receipt.annualSummary.rejected') }}</span>
                <span class="stat-value danger">{{ monthData.rejectedStr }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">{{ $t('receipt.annualSummary.closed') }}</span>
                <span class="stat-value info">{{ monthData.closedStr }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">{{ $t('receipt.annualSummary.waiting') }}</span>
                <span class="stat-value warning">{{ monthData.waitingStr }}</span>
              </div>
              <div class="stat-total">
                <span class="total-label">{{ $t('receipt.annualSummary.sum') }}</span>
                <span class="total-value">{{ monthData.totalSumStr }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 연간 합계 카드 -->
        <div class="yearly-summary-card box-shadow-gray" v-if="footerData.length > 0">
          <div class="summary-header">
            <h2>{{ selectedYear }}{{ $t('receipt.annualSummary.annualTotal') }}</h2>
          </div>
          <div class="summary-stats">
            <div class="summary-item box-shadow-gray">
              <span class="summary-label">{{ $t('receipt.annualSummary.request') }}</span>
              <span class="summary-value primary">{{ footerData[2] }}</span>
            </div>
            <div class="summary-item box-shadow-gray">
              <span class="summary-label">{{ $t('receipt.annualSummary.approved') }}</span>
              <span class="summary-value success">{{ footerData[3] }}</span>
            </div>
            <div class="summary-item box-shadow-gray">
              <span class="summary-label">{{ $t('receipt.annualSummary.rejected') }}</span>
              <span class="summary-value danger">{{ footerData[4] }}</span>
            </div>
            <div class="summary-item box-shadow-gray">
              <span class="summary-label">{{ $t('receipt.annualSummary.closed') }}</span>
              <span class="summary-value info">{{ footerData[5] }}</span>
            </div>
            <div class="summary-item box-shadow-gray">
              <span class="summary-label">{{ $t('receipt.annualSummary.waiting') }}</span>
              <span class="summary-value warning">{{ footerData[1] }}</span>
            </div>
            <div class="summary-total">
              <span class="total-label">{{ $t('receipt.annualSummary.totalSum') }}</span>
              <span class="total-value">{{ footerData[6] }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/* ───────────────────────────── imports ───────────────────────────── */
import { ref, onMounted, computed }           from 'vue'
import { useRouter }                from 'vue-router'
import { useI18n } from 'vue-i18n'
import { toast }                    from 'vue3-toastify'

import DefaultTable                 from '@/components/common/table/DefaultTable.vue'
import DefaultTextfield             from '@/components/common/textfield/DefaultTextfield.vue'
import DefaultLabel                 from '@/components/common/label/DefaultLabel.vue'
import DefaultFormRow               from '@/components/common/DefaultFormRow.vue'

import ReceiptsApi                  from '@/api/receipt/ReceiptsApi'
import { useAuthStore }             from '@/store/auth'
import { useViewStateStore }        from '@/store/viewState'

/* ─────────────────────────── reactive state ─────────────────────── */
const { t } = useI18n()
const router         = useRouter()
const auth           = useAuthStore()
const isMobile       = ref(false)                       // 필요하면 resize 로직 추가
const now            = new Date()
const currentYear    = now.getFullYear()
const selectedYear   = ref(String(currentYear))

const tableData      = ref([])                          // 본문
const footerData     = ref([])                          // 푸터 합계 행
const viewState      = useViewStateStore()

/* ──────────────────────── table column schema ───────────────────── */
const columns = computed(() => [
  { key:'monthLabel',  label:t('receipt.annualSummary.month'),    width: 50, align: 'center' },
  { key:'waitingStr',  label:t('receipt.annualSummary.waiting'),  width: 80, align: 'right' , mobileDisable:true},
  { key:'requestStr',  label:t('receipt.annualSummary.request'),  width: 80, align: 'right' },
  { key:'approvedStr', label:t('receipt.annualSummary.approved'),  width: 80, align: 'right' , mobileDisable:true},
  { key:'rejectedStr', label:t('receipt.annualSummary.rejected'),  width: 80, align: 'right' },
  { key:'closedStr',   label:t('receipt.annualSummary.closed'),  width: 80, align: 'right' },
  { key:'totalSumStr', label:t('receipt.annualSummary.sum'),  width: 90, align: 'right' , mobileDisable:true}
])

/* ─────────────────────── helpers & formatters ───────────────────── */
const won = n => `${Number(n||0).toLocaleString()} 원`

/* ─────────────────────── data fetch from API ───────────────────── */
async function fetchYearlySummary () {
  try {
    const { data } =
      await ReceiptsApi.getReceiptByUserYearlySummary(auth.getUserId, {
        year: selectedYear.value          // query param ?year=YYYY
      })

    /* ① 본문 행 */
    tableData.value = (data.monthlyList || []).map(m => ({
      monthLabel : `${m.month}월`,
      waitingStr : won(m.waiting),
      requestStr : won(m.request),
      approvedStr: won(m.approved),
      rejectedStr: won(m.rejected),
      closedStr  : won(m.closed),
      totalSumStr: won(m.sum)
    }))

    /* ② 푸터 합계 */
    footerData.value = [
      '합계',
      won(data.totalWaiting),
      won(data.totalRequest),
      won(data.totalApproved),
      won(data.totalRejected),
      won(data.totalClosed),
      won(data.totalSum)
    ]
  } catch (err) {
    console.error(err)
    toast.error('연도별 영수증 요약 조회 중 오류가 발생했습니다')
  }
}

/**
 * 연도 입력 변경 시 유효성 검사
 * - 4자리 숫자가 아니면 현재 연도로 되돌리고 경고
 */
function onYearChange () {
  if (!/^\d{4}$/.test(selectedYear.value)) {
    toast.warning('년도를 4자리 숫자로 입력해주세요\n현재 연도로 변경합니다')
    selectedYear.value = String(currentYear)
  }
  viewState.saveState('annualReceiptSummary', { selectedYear: selectedYear.value, scrollY: window.scrollY })
  fetchYearlySummary()
}

/**
 * 월 카드 클릭 시 신청 내역 조회 페이지로 이동 (해당 년도 월로 필터링)
 */
function goToMonthDetail(monthLabel) {
  const month = parseInt(monthLabel.replace('월', ''))
  // 상태 저장 후 이동
  viewState.saveState('annualReceiptSummary', {
    selectedYear: selectedYear.value,
    scrollY: window.scrollY
  })
  router.push({
    path: '/receipt/personal-receipt-history',
    query: {
      year: selectedYear.value,
      month: month
    }
  })
}

/* ─────────────────────────── lifecycle ─────────────────────────── */
onMounted(async () => {
  const saved = viewState.getState('annualReceiptSummary')
  const restore = viewState.canRestore('annualReceiptSummary')
  if (restore && saved && saved.selectedYear) {
    selectedYear.value = String(saved.selectedYear)
  }
  await fetchYearlySummary()
  if (restore && saved && typeof saved.scrollY === 'number') {
    requestAnimationFrame(()=>{ window.scrollTo(0, saved.scrollY) })
  }
})

// PersonalReceiptHistory에서만 복원 허용
viewState.allowRestoreFrom('annualReceiptSummary', ['PersonalReceiptHistory'])
</script>

<style scoped>
.mb-3 {
  margin-bottom: 20px !important;
}

/* ───── 카드 컨테이너 ───── */
.annual-cards-container {
  margin-top: 20px;
}

/* ───── 월별 카드 그리드 ───── */
.monthly-cards-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

/* ───── 개별 월 카드 (심플 모던) ───── */
.month-card {
  background: #ffffff;
  border: 1px solid #dbdbdb;
  border-radius: 6px;
  padding: 16px 12px;
  transition: all 0.2s ease;
  min-height: 180px;
  cursor: pointer;
}

.month-card:hover {
  border-color: #c5d8ee;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  background: #e9f6ff;
}

/* ───── 자연스러운 월 헤더 ───── */
.month-header {
  margin: -16px -12px 16px -12px; /* 카드 패딩 무시하고 전체 너비 */
  padding: 14px 0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e5e7eb;
  border-radius: 6px 6px 0 0;
}

.month-header h3 {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: #383f49;
  text-align: center;
  letter-spacing: 0.02em;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* ───── 심플 통계 아이템들 ───── */
.month-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.stat-label {
  font-size: 0.75rem;
  font-weight: 500;
  color: #777c86;
}

.stat-value {
  font-size: 0.75rem; /* 조금 더 작게 */
  font-weight: 900; /* 웨이트 900 */
  color: #777c86; /* 연회색 */
}

/* ───── 심플 월별 합계 ───── */
.stat-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  margin-top: 8px;
  border-top: 1px solid #f3f4f6;
}

.total-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #777c86;
}

.total-value {
  font-size: 0.8rem; /* 조금 더 작게 */
  font-weight: 900; /* 웨이트 900 */
  color: #777c86; /* 연회색 */
}

/* ───── 심플 연간 합계 카드 ───── */
.yearly-summary-card {
  background: #ffffff;
  padding: 24px;
  margin-top: 8px;
  margin-bottom: 20px;
}

.summary-header h2 {
  margin: 0 0 20px 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #28303b;
  text-align: left;
}

/* ───── 심플 연간 통계 그리드 ───── */
.summary-stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  padding: 12px 8px;
  background: #f9fafb;
  border-radius: 4px;
  text-align: center;
}

.summary-label {
  font-size: 0.75rem;
  font-weight: 500;
  color: #6b7079;
}

.summary-value {
  font-size: 0.75rem; /* 조금 더 작게 */
  font-weight: 900; /* 웨이트 900 */
  color: #6b7079; /* 연회색 */
}

/* ───── 전체 합계 ───── */
.summary-total {
  grid-column: 1 / -1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  margin-top: 12px;
  background: #f3f4f6;
  border-radius: 6px;
  border-top: 2px solid #374151;
}

.summary-total .total-label {
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
}

.summary-total .total-value {
  font-size: 1.1rem; /* 조금 더 작게 */
  font-weight: 900; /* 웨이트 900 */
  color: #6b7079; /* 연회색 */
}

/* ───── 반응형 그리드 (6개 → 4개 → 3개 → 2개) ───── */
@media (min-width: 1200px) {
  .monthly-cards-grid {
    grid-template-columns: repeat(6, 1fr);
    gap: 16px;
  }
}

@media (min-width: 900px) and (max-width: 1199px) {
  .monthly-cards-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 14px;
  }
}

@media (min-width: 650px) and (max-width: 899px) {
  .monthly-cards-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
  
  .month-card {
    padding: 14px 10px;
  }
  
  .month-header {
    margin: -14px -10px 12px -10px;
    padding: 10px;
  }
  
  .month-header h3 {
    font-size: 0.85rem;
  }
}

@media (max-width: 649px) {
  .monthly-cards-grid {
    grid-template-columns: repeat(2, 1fr); /* 모바일에서도 최소 2개 */
    gap: 10px;
  }
  
  .month-card {
    min-height: 160px;
    padding: 12px 8px;
  }
  
  .month-header {
    margin: -12px -8px 10px -8px;
    padding: 10px 6px;
  }
  
  .month-header h3 {
    font-size: 0.8rem;
  }
  
  .stat-label {
    font-size: 0.7rem;
  }
  
  .stat-value {
    font-size: 0.7rem;
  }
  
  .total-label {
    font-size: 0.75rem !important;
  }
  
  .total-value {
    font-size: 0.75rem !important;
  }
  
  .yearly-summary-card {
    padding: 20px 16px;
  }
  
  .summary-header h2 {
    font-size: 1rem !important;
    margin-bottom: 16px;
  }
  
  .summary-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
  
  .summary-item {
    padding: 10px 8px;
  }
  
  .summary-label {
    font-size: 0.7rem;
  }
  
  .summary-value {
    font-size: 0.75rem;
  }
}
</style>
