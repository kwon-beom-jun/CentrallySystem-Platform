<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('receipt.common.historyTitle')"
        :subtitle="`${$t('receipt.common.dateRange')}${computedStartDate} ~ ${computedEndDate}${$t('receipt.common.dateRangeEnd')}`"
        icon="ri-history-line"
        desktopMarginBottom="30px"
      />

      <!-- 검색 영역 -->
      <div class="search-controls">
        <!-- 시작, 종료일 그룹 -->
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
        <!-- 이름 검색 그룹 -->
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
            @click="search"
            size="small">
            {{ $t('receipt.approvalSummary.searchButton') }}
          </DefaultButton>
        </DefaultFormRow>
      </div>

      <!-- DefaultTable 컴포넌트 (큰 화면) -->
      <DefaultTable
        :columns="columns"
        :data="data"
        :mobileCard="true"
        :bodyFontSize="'0.7rem'"
        :rowClick="goToDetail"
        :minRows="10"
        :usePagination="true"
        :currentPage="currentPage"
        :totalPages="totalPages"
        :visiblePageCount="visiblePageCount"
        @pageChange="onPageChange"
        :noDataImageHeight="474"
      />
    </div>
  </div>
</template>

<script setup>
/* ───────────────────────────── imports ───────────────────────────── */
import { ref, watch, onMounted, computed, nextTick }        from 'vue'
import { useRouter, useRoute }          from 'vue-router'

import DefaultTable                     from '@/components/common/table/DefaultTable.vue'
import DefaultButton                    from '@/components/common/button/DefaultButton.vue'
import DefaultTextfield                 from '@/components/common/textfield/DefaultTextfield.vue'
import DefaultLabel                     from '@/components/common/label/DefaultLabel.vue'
import DefaultFormRow                   from '@/components/common/DefaultFormRow.vue'
import UserSearchDropdown               from '@/components/auth/UserSearchDropdown.vue'
import { toDeptTeamDisplay }            from '@/utils/blankFormat.js'

import { useAuthStore }                 from '@/store/auth'

import ReceiptsSearchApi                from '@/api/receipt/ReceiptsSearchApi'
import { useViewStateStore }            from '@/store/viewState'

/* ──────────────────────────── stores & router ────────────────────── */
const auth        = useAuthStore()
const router      = useRouter()
const route       = useRoute()
const viewState   = useViewStateStore()

/* ─────────────────────────── reactive states ─────────────────────── */
const isMobile           = ref(false)
const startDate          = ref('')
const endDate            = ref('')
const searchUserId       = ref('')
const selectedUserSearchValue = ref('')
const userSearchRef      = ref(null)
const computedStartDate  = computed(() => startDate.value)
const computedEndDate    = computed(() => endDate.value)

const data               = ref([])

const currentPage        = ref(1)
const totalPages         = ref(1)
const visiblePageCount   = ref(5)

/* ───────────────────────────── table meta ───────────────────────── */
const columns = [
  { key:'userName',      label:'이름', width:100,
    mobile:{ line:1, inline:true,  prefix:'이름\u00a0:\u00a0',  bold:true } },

  { key:'userEmail', label:'이메일', width:'auto', minWidth: 100,
    mobile:{ line:1, inline:true,  prefix:'', align:'right' } },

  { key:'departmentName',label:'부서', width:150,
    mobile:{ dividerTop:true, dividerTopGapAbove:'5px', dividerTopGapBelow:'15px', line:2, inline:false, prefix:'부서\u00a0:\u00a0' } },

  { key:'teamName',  label:'팀', width:150,
    mobile:{ line:3, inline:false, prefix:'팀\u00a0:\u00a0' } },

  { key:'count',     label:'건수', width:50,  align:'center',
    mobile:{ line:4, inline:false, prefix:'건수\u00a0:\u00a0' } },

  { key:'waiting',   label:'대기', width:100, align:'right',
    mobile:{ line:5, inline:false, prefix:'대기\u00a0:\u00a0' } },

  { key:'requested', label:'신청', width:100, align:'right',
    mobile:{ line:6, inline:false, prefix:'신청\u00a0:\u00a0' } },

  { key:'approved',  label:'승인', width:100, align:'right',
    mobile:{ line:7, inline:false, prefix:'승인\u00a0:\u00a0' } },

  { key:'rejected',  label:'반려', width:100, align:'right',
    mobile:{ line:8, inline:false, prefix:'반려\u00a0:\u00a0' } },

  { key:'closed',    label:'마감', width:100, align:'right',
    mobile:{ line:9, inline:false, prefix:'마감\u00a0:\u00a0' } }
]

/* ─────────────────────────── helper utils ───────────────────────── */
const won = n => (Number(n||0)).toLocaleString()

function formatCurrency(n){ return won(n) }

/* ──────────────────────── user list mapping  ────────────────────── */
const userList = ref([])                      // [{userId, name, email, dept, team}]

/* ───────────────────────── data enrichment ───────────────────────── */
function enrichSummaryData (rows){
  return rows.map(r => ({
    ...r,
    departmentName: toDeptTeamDisplay(r.departmentName),
    teamName      : toDeptTeamDisplay(r.teamName)
  }))
}

/* ──────────────────────── server interaction ────────────────────── */
async function fetchData (page=1){
  const pageSize = isMobile.value ? 4 : 10
  const { data: res } = await ReceiptsSearchApi.getReceiptSummaryByNameAndDate({
    startDate : startDate.value,
    endDate   : endDate.value,
    userId    : searchUserId.value,
    approverId: auth.getUserId,
    page      : page-1,
    size      : pageSize
  })
  data.value          = enrichSummaryData(res.content || [])
  totalPages.value    = res.totalPages || 1
  // computedStartDate.value = startDate.value
  // computedEndDate.value   = endDate.value
  if (isMobile.value) window.scrollTo(0,0)
}

/* ─────────────────────────── search / nav ───────────────────────── */
/**
 * 조회 버튼 클릭 시 실행
 * - 저장된 상태를 초기화하여 새로운 조회 조건으로 검색
 */
function search () {
  currentPage.value = 1
  viewState.clearState('receiptHistory')  // 기존 저장 상태 초기화
  fetchData(1)
}
function onPageChange (p){ currentPage.value = p; fetchData(p) }
function onUserSelected (u){ 
  searchUserId.value = u.userId 
  selectedUserSearchValue.value = u?.label ?? ''  // "이름 (이메일)" 형식
}
function goToDetail (row){
  // 상태 저장 후 이동
  viewState.saveState('receiptHistory', {
    startDate              : startDate.value,
    endDate                : endDate.value,
    searchUserId           : searchUserId.value,
    selectedUserSearchValue: selectedUserSearchValue.value,
    currentPage            : currentPage.value,
    scrollY                : window.scrollY,
  })
  router.push({
    name : 'ReceiptHistoryDetail',
    query: {
      userId   : row.userId,
      userName : row.userName,
      startDate: startDate.value,
      endDate  : endDate.value
    }
  })
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
  const saved = viewState.getState('receiptHistory')
  const restore = viewState.canRestore('receiptHistory')
  if (restore && saved) {
    startDate.value              = saved.startDate   || startDate.value
    endDate.value                = saved.endDate     || endDate.value
    searchUserId.value           = saved.searchUserId|| ''
    selectedUserSearchValue.value = saved.selectedUserSearchValue ?? ''
    currentPage.value            = saved.currentPage || 1
    await fetchData(currentPage.value)
    requestAnimationFrame(()=>{ window.scrollTo(0, saved.scrollY ?? 0) })
  } else {
    search()
  }
})

// 상세에서만 복원 허용
viewState.allowRestoreFrom('receiptHistory', ['ReceiptHistoryDetail'])
</script>

<style scoped>
.search-controls {
  margin-bottom: 10px;
}
.align-items-center {
  margin-bottom: 0px !important;
}
</style>
