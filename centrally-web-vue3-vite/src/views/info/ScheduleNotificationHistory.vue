<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('info.scheduleNotificationHistory.title')"
        :subtitle="`${$t('info.scheduleNotificationHistory.dateRange')}${computedStartDate} ~ ${computedEndDate}${$t('info.scheduleNotificationHistory.dateRangeEnd')}`"
        icon="ri-history-line"
        desktopMarginBottom="30px"
      />

      <!-- 검색 영역 -->
      <div class="search-controls">
        <!-- 시작, 종료일 그룹 -->
        <DefaultFormRow align="right">
          <DefaultLabel :text="$t('info.scheduleNotificationHistory.startDate')" forId="startDate" size="small" />
          <DefaultTextfield
            type="date"
            id="startDate"
            v-model="startDate"
            size="xsmall"
          />
          <DefaultLabel :text="$t('info.scheduleNotificationHistory.endDate')" forId="endDate" size="small" marginLeft="10px" />
          <DefaultTextfield
            type="date"
            id="endDate"
            v-model="endDate"
            size="xsmall"
          />
        </DefaultFormRow>
        <!-- 사용자 검색 그룹 -->
        <DefaultFormRow align="right" marginTop="7px">
          <DefaultTextfield
            type="text"
            id="searchKeyword"
            size="large"
            v-model="searchKeyword"
            :placeholder="$t('info.scheduleNotificationHistory.searchPlaceholder')"
          />
          <DefaultButton 
            @click="search"
            color="gray"
            size="small">
            {{ $t('common.button.search') }}
          </DefaultButton>
        </DefaultFormRow>
      </div>

      <!-- DefaultTable 컴포넌트 -->
      <DefaultTable
        :columns="columns"
        :data="data"
        :mobileCard="false"
        :bodyFontSize="'0.7rem'"
        :minRows="10"
        :usePagination="true"
        :currentPage="currentPage"
        :totalPages="totalPages"
        :visiblePageCount="visiblePageCount"
        @pageChange="onPageChange"
        :noDataImageHeight="474"
        :fixMobileHeight="true"
        :rowClick="handleRowClick"
      />
    </div>

    <!-- 테이블 클릭시 상세 모달 (데스크탑) -->
    <ScheduleNotificationHistoryDetail
      v-if="isDetailModalVisible"
      :historyData="selectedHistory"
      @close="isDetailModalVisible = false"
    />
  </div>
</template>

<script setup>
/* ───────────────────────────── imports ───────────────────────────── */
import { ref, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import PageTitle                         from '@/components/common/title/PageTitle.vue'
import DefaultTable                     from '@/components/common/table/DefaultTable.vue'
import DefaultButton                    from '@/components/common/button/DefaultButton.vue'
import DefaultTextfield                 from '@/components/common/textfield/DefaultTextfield.vue'
import DefaultLabel                     from '@/components/common/label/DefaultLabel.vue'
import DefaultFormRow                   from '@/components/common/DefaultFormRow.vue'
import ScheduleNotificationHistoryDetail from '@/components/info/ScheduleNotificationHistoryDetail.vue'

import ScheduleNotificationHistoryApi   from '@/api/info/ScheduleNotificationHistoryApi'
import { useViewStateStore }            from '@/store/viewState'

/* ──────────────────────────── stores & router ────────────────────── */
const route       = useRoute()
const router      = useRouter()
const viewState   = useViewStateStore()

/* ─────────────────────────── reactive states ─────────────────────── */
const isMobile           = ref(false)
const startDate          = ref('')
const endDate            = ref('')
const searchKeyword      = ref('')
const computedStartDate  = computed(() => startDate.value)
const computedEndDate    = computed(() => endDate.value)

const data               = ref([])

const currentPage        = ref(1)
const totalPages         = ref(1)
const visiblePageCount   = ref(5)

// 모달 표시/숨김 제어
const isDetailModalVisible = ref(false)
const selectedHistory = ref(null)

/* ───────────────────────────── table meta ───────────────────────── */
const columns = [
  { key:'logId',      label:'로그 ID', width:70, align:'center', mobileDisable: true },

  { key:'userName',        label:'사용자명', width:100, mobileDisable: true },

  { key:'userEmail',       label:'이메일', width:'auto', minWidth: 180 },

  { key:'notificationType',label:'알림 유형', width:120, align:'center', mobileDisable: true },

  { key:'notificationValue',label:'알림 설정 시간', width:150, align:'center', mobileDisable: true },

  { key:'scheduledTime',   label:'예정 시간', width:150, align:'center', mobileDisable: true },

  { key:'sentTime',        label:'발송 시간', width:150, align:'center', mobileDisable: true },

  { key:'channel',         label:'채널', width:80, align:'center', mobileDisable: true },

  { key:'sent',            label:'발송 성공', width:100, align:'center',
    customValue: (row) => row.sent ? '성공' : '실패',
    customClass: (value) => {
      // value는 원본 sent 값 (true/false)
      return value === true ? 'text-green' : 'text-red';
    } },

  { key:'failureReason',   label:'실패 사유', width:'auto', minWidth: 150, mobileDisable: true }
]

/* ──────────────────────── server interaction ────────────────────── */
async function fetchData (page=1){
  const pageSize = isMobile.value ? 4 : 10
  
  const { data: res } = await ScheduleNotificationHistoryApi.getNotificationHistory({
    startDate     : startDate.value,
    endDate       : endDate.value,
    searchKeyword : searchKeyword.value.trim() || undefined,
    page          : page-1,
    size          : pageSize
  })
  
  data.value          = res.content || []
  totalPages.value    = res.totalPages || 1
  if (isMobile.value) window.scrollTo(0,0)
}

/* ─────────────────────────── search / nav ───────────────────────── */
/**
 * 조회 버튼 클릭 시 실행
 */
function search () {
  currentPage.value = 1
  viewState.saveState('scheduleNotificationHistory', {
    startDate: startDate.value,
    endDate: endDate.value,
    searchKeyword: searchKeyword.value,
    currentPage: currentPage.value,
    scrollY: window.scrollY,
  })
  fetchData(1)
}
function onPageChange (p){ currentPage.value = p; fetchData(p) }

/**
 * 테이블 행 클릭 핸들러
 * @param {object} item 클릭한 행 데이터
 */
function handleRowClick(item) {
  if (isMobile.value) {
    // 상태 저장 후 이동
    viewState.saveState('scheduleNotificationHistory', {
      startDate: startDate.value,
      endDate: endDate.value,
      searchKeyword: searchKeyword.value,
      currentPage: currentPage.value,
      scrollY: window.scrollY,
    });
    // 모바일일 경우 상세 페이지로 이동
    sessionStorage.setItem('scheduleNotificationHistoryDetail', JSON.stringify(item));
    router.push({
      name: 'ScheduleNotificationHistoryDetailMobile'
    });
  } else {
    // 데스크탑일 경우 모달 표시
    selectedHistory.value = item;
    isDetailModalVisible.value = true;
  }
}

/* ───────────────────────────── watchers ─────────────────────────── */
watch(startDate,v=>{ if(v> endDate.value) endDate.value=v })
watch(endDate  ,v=>{ if(v< startDate.value) startDate.value=v })

/* ───────────────────────────── resize  ──────────────────────────── */
function updateViewMode (){ isMobile.value = window.innerWidth <= 850 }

/* ──────────────────────────── init helpers ───────────────────────── */
function todayString (){
  const d=new Date()
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

/* ───────────────────────────── mounted ──────────────────────────── */
onMounted(async ()=>{
  // 1) 쿼리 우선 반영, 없으면 오늘로 셋팅
  const qsStart = String(route.query.startDate || '')
  const qsEnd   = String(route.query.endDate   || '')
  if (qsStart && qsEnd) {
    startDate.value = qsStart
    endDate.value   = qsEnd
  } else {
    startDate.value = endDate.value = todayString()
  }
  // 2) 화면 크기 반영
  updateViewMode()
  window.addEventListener('resize', updateViewMode)
  const saved = viewState.getState('scheduleNotificationHistory')
  const restore = viewState.canRestore('scheduleNotificationHistory')
  if (restore && saved) {
    startDate.value              = saved.startDate   || startDate.value
    endDate.value                = saved.endDate     || endDate.value
    searchKeyword.value          = saved.searchKeyword || ''
    currentPage.value            = saved.currentPage || 1
    await fetchData(currentPage.value)
    requestAnimationFrame(()=>{ window.scrollTo(0, saved.scrollY ?? 0) })
  } else {
    search()
  }
  // 상세(모바일 상세)에서만 복원 허용
  viewState.allowRestoreFrom('scheduleNotificationHistory', ['ScheduleNotificationHistoryDetailMobile'])
})
</script>

<style scoped>
.search-controls {
  margin-bottom: 10px;
}
.align-items-center {
  margin-bottom: 0px !important;
}
</style>

