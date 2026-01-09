<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('system.activityLog.title')"
        :subtitle="$t('system.activityLog.list')"
        icon="ri-clipboard-line"
        desktopMarginBottom="30px"
      />

      <div class="search-controls">
        <!-- 시작, 종료일 그룹 -->
        <DefaultFormRow align="right">
          <DefaultLabel :text="$t('common.label.startDate')" forId="startDate" size="small" />
          <DefaultTextfield
            type="date"
            id="startDate"
            v-model="startDate"
            size="xsmall"
          />
          <DefaultLabel :text="$t('common.label.endDate')" forId="endDate" size="small" marginLeft="10px" />
          <DefaultTextfield
            type="date"
            id="endDate"
            v-model="endDate"
            size="xsmall"
          />
        </DefaultFormRow>
        <!-- 이름 검색 그룹 -->
        <DefaultFormRow align="right" marginTop="7px">
          <!-- <DefaultLabel text="이름(이메일) :" forId="searchData" size="small" /> -->
          <DefaultTextfield
            type="text"
            id="searchData"
            size="large"
            v-model="searchData"
            :placeholder="$t('system.activityLog.searchPlaceholder')"
          />
          <DefaultButton 
            @click="search"
            color="gray"
            size="small">
            {{ $t('common.button.search') }}
          </DefaultButton>
        </DefaultFormRow>
      </div>

      <div class="activity-log-table">
        <!-- DefaultTable 컴포넌트 (페이지네이션 통합) -->
        <DefaultTable
          :columns="columns"
          :data="displayData"
          :showTable="viewMode"
          :bodyFontSize="'0.7rem'"
          :rowClick="(item) => openDetailModal(item)"
          @rowClick="openDetailModal" 
          :fixMobileHeight="true"
          :minRows="pageSize"
          :usePagination="true"
          :currentPage="currentPage"
          :totalPages="totalPages"
          :visiblePageCount="visiblePageCount"
          @pageChange="onPageChange"
          :noDataImageHeight="474"
        />
      </div>
    </div>

    <!-- 테이블 클릭시 상세 모달 -->
    <PersonalHistoryDetail
      v-if="isDetailModalVisible"
      :historyData="selectedLog"
      :title="$t('system.activityLog.detail')"
      @close="isDetailModalVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import DefaultTable from '@/components/common/table/DefaultTable.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import ActivityLogApi from '@/api/system/ActivityLogApi';
import PersonalHistoryDetail from '@/components/system/PersonalHistoryDetail.vue';
import { useViewStateStore } from '@/store/viewState';
import { translateMenuKey, translateFunctionKey } from '@/utils/i18nDisplay';
// import { usePageSize10or8 } from '@/composables/usePageSize';

// import axios from 'axios'; // 실제 백엔드 호출 시 사용

// ================ 상태 정의 ================
const viewState = useViewStateStore();

/**
 * 해당 달의 첫 번째 날짜 반환 (yyyy-MM-dd 형식)
 */
function getFirstDayOfMonth() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}-01`;
}

/**
 * 해당 달의 마지막 날짜 반환 (yyyy-MM-dd 형식)
 */
function getLastDayOfMonth() {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1;
  const lastDay = new Date(year, month, 0).getDate();
  return `${year}-${String(month).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;
}

const startDate = ref(getFirstDayOfMonth());
const endDate = ref(getLastDayOfMonth());
const searchData = ref('');
const viewMode = ref(true);

// 모달 표시/숨김 제어
const isDetailModalVisible = ref(false);
const selectedLog = ref(null);

// 실제 서버에서 받아온 데이터 (원본 키만 저장)
const data = ref([]);

// 테이블 표시용 데이터 (실시간 번역)
const displayData = computed(() => {
  return data.value.map(item => ({
    ...item,
    menuTranslated: translateMenuKey(item.menu, t),
    functionTranslated: translateFunctionKey(item.function, t),
  }));
});

// 페이지네이션
const currentPage = ref(1);
const totalPages = ref(1);
const visiblePageCount = ref(5);  // 페이지네이션에서 보여줄 최대 페이지 버튼 수
// const pageSize = usePageSize10or8();  // 반응형 페이지 사이즈 (데스크탑: 10, 모바일: 6)
const pageSize = ref(10);

// 컬럼 정의
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

const columns = ref([
  { key: 'username', label: t('common.label.name'), width: 85, align: 'center'  },
  { key: 'userEmail', label: t('common.label.email'), width: 220 , mobileDisable:true },
  { key: 'menuTranslated', label: t('system.activityLog.menu'), width: 120 , mobileDisable:true },
  { key: 'functionTranslated', label: t('system.activityLog.function'), width: 'auto', minWidth: 80 },
  { key: 'ip', label: t('system.activityLog.uri'), width: 120 , mobileDisable:true },
  { key: 'httpMethod', label: t('system.activityLog.method'), width: 80 , mobileDisable:true },
  { key: 'browser', label: t('system.activityLog.browser'), width: 100 , mobileDisable:true },
  { key: 'os', label: t('system.activityLog.os'), width: 100 , mobileDisable:true },
  { key: 'timestamp', label: t('system.activityLog.timestamp'), width: 155, align: 'center' },
]);

function openDetailModal(rowData) {
  // 상태 저장 후 모달 열기
  viewState.saveState('activityLog', {
    startDate   : startDate.value,
    endDate     : endDate.value,
    searchData  : searchData.value,
    currentPage : currentPage.value,
    scrollY     : window.scrollY,
  })
  selectedLog.value = rowData; // 클릭한 행의 데이터 보관
  isDetailModalVisible.value = true; // 모달 열기
}

/**
 * 서버에서 데이터 가져오기
 * 실제로는 axios 등을 사용하여 파라미터(startDate, endDate, searchData, currentPage 등)를 전송 후,
 * 응답에서 data, totalPages 등을 받아 세팅하시면 됩니다.
 */
async function fetchDataFromServer() {
  const response = await ActivityLogApi.getActivityLogs(
    {
      startDate: startDate.value,
      endDate: endDate.value,
      searchData: searchData.value,
      currentPage: currentPage.value,
      pageSize: pageSize.value,
    }, 
  );

  // 서버에서 받은 데이터를 모달용 fullData 형식으로 변환
  // 원본 키만 저장 (번역은 computed에서 실시간 처리)
  data.value = response.data.content.map(item => ({
    ...item,
    fullData: {
      userId: item.userEmail,
      userName: item.username,
      ip: item.ip,
      browser: item.browser,
      os: item.os,
    }
  }));
  
  totalPages.value = response.data.totalPages;
}

/**
 * 검색 버튼 클릭 시 실행
 * - 저장된 상태를 초기화하여 새로운 조회 조건으로 검색
 */
function search() {
  currentPage.value = 1;
  viewState.clearState('activityLog');  // 기존 저장 상태 초기화
  fetchDataFromServer(currentPage.value);
}

/**
 * 페이지 변경 시 (DefaultPagination에서 방출)
 * - 새 페이지를 받아서 currentPage를 갱신
 * - 백엔드 재조회
 */
function onPageChange(newPage) {
  currentPage.value = newPage;
  fetchDataFromServer(currentPage.value);
}

// 사용자가 시작일을 수정했을 때, 종료일보다 큰 경우 → 종료일을 시작일과 동일하게 맞춤
watch(startDate, (newStart) => {
  if (newStart > endDate.value) {
    endDate.value = newStart;
  }
});
// 사용자가 종료일을 수정했을 때, 시작일보다 작은 경우 → 시작일을 종료일과 동일하게 맞춤
watch(endDate, (newEnd) => {
  if (newEnd < startDate.value) {
    startDate.value = newEnd;
  }
});

onMounted(async () => {
  const saved = viewState.getState('activityLog')
  const restore = viewState.canRestore('activityLog')
  if (restore && saved) {
    startDate.value   = saved.startDate   || startDate.value
    endDate.value     = saved.endDate     || endDate.value
    searchData.value  = saved.searchData  || ''
    currentPage.value = saved.currentPage || 1
    await fetchDataFromServer(currentPage.value)
    requestAnimationFrame(()=>{ window.scrollTo(0, saved.scrollY ?? 0) })
  } else {
    // 컴포넌트 초기 로드시 첫 페이지 조회
    fetchDataFromServer(currentPage.value);
  }
});

// 상세 모달에서만 복원 허용 (모달이므로 라우트 이름은 동일)
viewState.allowRestoreFrom('activityLog', ['ActivityLog']);
</script>

<style scoped>
.search-controls {
  margin-bottom: 10px;
}
</style>
