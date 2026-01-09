<template>
  <div>
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('receipt.common.delegateHistoryTitle')"
        :subtitle="`${$t('receipt.common.dateRange')}${startDate} ~ ${endDate}${$t('receipt.common.dateRangeEnd')}`"
        icon="ri-history-line"
        desktopMarginBottom="30px"
      />

      <div class="search-controls">
        <DefaultFormRow align="right">
          <DefaultLabel :text="$t('receipt.approvalSummary.startDate')" forId="startDate" size="small" />
          <DefaultTextfield type="date" id="startDate" v-model="startDate" size="xsmall" />
          <DefaultLabel :text="$t('receipt.approvalSummary.endDate')" forId="endDate" size="small" marginLeft="10px" />
          <DefaultTextfield type="date" id="endDate" v-model="endDate" size="xsmall" />
        </DefaultFormRow>
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
          <DefaultButton @click="search" size="small">{{ $t('receipt.approvalSummary.searchButton') }}</DefaultButton>
        </DefaultFormRow>
      </div>

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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DefaultFormRow from '@/components/common/DefaultFormRow.vue'
import DefaultLabel from '@/components/common/label/DefaultLabel.vue'
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue'
import DefaultTable from '@/components/common/table/DefaultTable.vue'
import DefaultButton from '@/components/common/button/DefaultButton.vue'
import UserSearchDropdown from '@/components/auth/UserSearchDropdown.vue'
import { useAuthStore } from '@/store/auth'
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi'
import { toDeptTeamDisplay } from '@/utils/blankFormat.js'
import { useViewStateStore } from '@/store/viewState'

const auth = useAuthStore()
const router = useRouter()
const viewState = useViewStateStore()

const isMobile = ref(false)
const startDate = ref('')
const endDate = ref('')
const searchUserId = ref('')
const selectedUserSearchValue = ref('')
const userSearchRef = ref(null)
const data = ref([])
const currentPage = ref(1)
const totalPages = ref(1)
const visiblePageCount = ref(5)

const columns = [
  { key:'userName',      label:'이름', width:150,
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

function enrich(rows){
  return rows.map(r => ({
    ...r,
    departmentName: toDeptTeamDisplay(r.departmentName),
    teamName:       toDeptTeamDisplay(r.teamName)
  }))
}

/**
 * 대리 결재/합의 요약 데이터 조회 (전체 상태)
 * - 기존 pending 전용 → 전체 상태 summary 엔드포인트
 */
async function fetchData(page=1){
  const pageSize = isMobile.value ? 4 : 10
  const { data: res } = await ReceiptsSearchApi.getSummaryForDelegate(auth.getUserId, {
    userId: searchUserId.value || null,
    startDate: startDate.value,
    endDate: endDate.value,
    statusCodes: null,
    page: page - 1,
    size: pageSize
  })
  data.value = enrich(res.content || [])
  totalPages.value = res.totalPages || 1
  if (isMobile.value) window.scrollTo(0,0)
}

/**
 * 조회 버튼 클릭 시 실행
 * - 저장된 상태를 초기화하여 새로운 조회 조건으로 검색
 */
function search(){ 
  currentPage.value = 1
  viewState.clearState('receiptDelegateHistory')  // 기존 저장 상태 초기화
  fetchData(1) 
}
function onPageChange(p){ currentPage.value = p; fetchData(p) }
function onUserSelected(u){ 
  searchUserId.value = u.userId 
  selectedUserSearchValue.value = u?.label ?? ''  // "이름 (이메일)" 형식
}
function goToDetail(row){
  // 상태 저장 후 이동
  viewState.saveState('receiptDelegateHistory', {
    startDate              : startDate.value,
    endDate                : endDate.value,
    searchUserId           : searchUserId.value,
    selectedUserSearchValue: selectedUserSearchValue.value,
    currentPage            : currentPage.value,
    scrollY                : window.scrollY,
  })
  router.push({
    name: 'ReceiptDelegateHistoryDetail',
    query: {
      userId: row.userId,
      userName: row.userName,
      startDate: startDate.value,
      endDate: endDate.value
    }
  })
}

function today(){
  const d=new Date();
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

function updateViewMode(){ isMobile.value = window.innerWidth <= 850 }

onMounted(async ()=>{
  startDate.value = endDate.value = today()
  
  // 화면 크기 반영
  updateViewMode()
  window.addEventListener('resize', updateViewMode)
  
  const saved = viewState.getState('receiptDelegateHistory')
  const restore = viewState.canRestore('receiptDelegateHistory')
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
viewState.allowRestoreFrom('receiptDelegateHistory', ['ReceiptDelegateHistoryDetail'])
</script>

<style scoped>
.search-controls { 
  margin-bottom: 10px; 
}
.align-items-center {
  margin-bottom: 0px !important;
}
</style>


