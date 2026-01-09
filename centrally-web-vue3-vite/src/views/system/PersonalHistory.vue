<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('system.personalHistory.title')"
        :subtitle="$t('system.personalHistory.list')"
        icon="ri-file-user-line"
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
        
        <!-- 검색 그룹 -->
        <DefaultFormRow align="right" marginTop="7px">
          <DefaultTextfield
            type="text"
            id="searchKeyword"
            size="large"
            v-model="searchKeyword"
            :placeholder="$t('system.personalHistory.searchPlaceholder')"
          />
          <DefaultButton 
            @click="search"
            color="gray"
            size="small">
            {{ $t('common.button.search') }}
          </DefaultButton>
        </DefaultFormRow>
      </div>

      <div class="personal-history-table">
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
      :historyData="selectedHistory"
      :showBody="false"
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
import PageTitle from '@/components/common/title/PageTitle.vue';
import PersonalHistoryDetail from '@/components/system/PersonalHistoryDetail.vue';
import PersonalHistoryApi from '@/api/system/PersonalHistoryApi';
import { useViewStateStore } from '@/store/viewState';
import { useAuthStore } from '@/store/auth';
import { getEnvironmentInfo } from '@/utils/browserUtils';
import { translateMenuKey, translateFunctionKey } from '@/utils/i18nDisplay';

// ================ 상태 정의 ================
const viewState = useViewStateStore();
const authStore = useAuthStore();

// 기본값: 최근 1개월
const getDefaultStartDate = () => {
  const date = new Date();
  date.setMonth(date.getMonth() - 1);
  return date.toISOString().slice(0, 10);
};
const getDefaultEndDate = () => {
  return new Date().toISOString().slice(0, 10);
};

const startDate = ref(getDefaultStartDate());
const endDate = ref(getDefaultEndDate());
const searchKeyword = ref('');

// 모달 표시/숨김 제어
const isDetailModalVisible = ref(false);
const selectedHistory = ref(null);

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
const visiblePageCount = ref(5);
const pageSize = ref(10);

// viewMode (데스크톱: true, 모바일: false)
const viewMode = ref(true);

// 환경 정보 수집
const envInfo = getEnvironmentInfo();

// 컬럼 정의
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

const columns = ref([
  { key: 'timestamp', label: t('system.activityLog.timestamp'), width: 160, align: 'center' },
  { key: 'menuTranslated', label: t('system.activityLog.menu'), width: 120, mobileDisable: true },
  { key: 'functionTranslated', label: t('system.activityLog.function'), width: 'auto', minWidth: 150 },
  { key: 'ip', label: t('system.activityLog.ipAddress'), width: 250, mobileDisable: true },
  { key: 'httpMethod', label: t('system.activityLog.method'), width: 80, mobileDisable: true },
]);

/**
 * 상세 모달 열기
 */
function openDetailModal(rowData) {
  viewState.saveState('personalHistory', {
    startDate: startDate.value,
    endDate: endDate.value,
    searchKeyword: searchKeyword.value,
    currentPage: currentPage.value,
    scrollY: window.scrollY,
  });
  selectedHistory.value = rowData;
  isDetailModalVisible.value = true;
}

/**
 * 서버에서 데이터 가져오기
 */
async function fetchDataFromServer() {
  try {
    const response = await PersonalHistoryApi.getPersonalHistory({
      startDate: startDate.value,
      endDate: endDate.value,
      searchData: searchKeyword.value,
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      browser: envInfo.browser,
      os: envInfo.os,
    });

    // 서버에서 받은 데이터를 화면에 맞게 변환
    // 원본 키만 저장 (번역은 computed에서 실시간 처리)
    data.value = response.data.content.map(item => ({
      ...item,
      fullData: {
        userId: authStore.userId,
        userName: authStore.userName,
        ip: item.ip,
        browser: item.browser || '',  // 서버에서 받은 브라우저 정보 사용
        os: item.os || '',  // 서버에서 받은 OS 정보 사용
        userAgent: envInfo.userAgent,
      }
    }));
    
    totalPages.value = response.data.totalPages;
  } catch (error) {
    console.error('개인 이력 조회 실패:', error);
    data.value = [];
    totalPages.value = 1;
  }
}

/**
 * 검색 버튼 클릭 시 실행
 */
function search() {
  currentPage.value = 1;
  viewState.clearState('personalHistory');
  fetchDataFromServer();
}

/**
 * 페이지 변경 시
 */
function onPageChange(newPage) {
  currentPage.value = newPage;
  fetchDataFromServer();
}

/**
 * 날짜 유효성 검사
 */
watch(startDate, (newStart) => {
  if (newStart > endDate.value) {
    endDate.value = newStart;
  }
});

watch(endDate, (newEnd) => {
  if (newEnd < startDate.value) {
    startDate.value = newEnd;
  }
});

/**
 * 컴포넌트 마운트 시 초기화
 */
onMounted(async () => {
  const saved = viewState.getState('personalHistory');
  const restore = viewState.canRestore('personalHistory');
  
  if (restore && saved) {
    startDate.value = saved.startDate || startDate.value;
    endDate.value = saved.endDate || endDate.value;
    searchKeyword.value = saved.searchKeyword || '';
    currentPage.value = saved.currentPage || 1;
    await fetchDataFromServer();
    requestAnimationFrame(() => { 
      window.scrollTo(0, saved.scrollY ?? 0); 
    });
  } else {
    await fetchDataFromServer();
  }
});

// 상세 모달에서만 복원 허용
viewState.allowRestoreFrom('personalHistory', ['PersonalHistory']);
</script>

<style scoped>
.search-controls {
  margin-bottom: 10px;
}

.personal-history-table {
  margin-top: 15px;
}

.mx-width-700 {
  max-width: 700px;
  margin: 40px auto;
}

.no-results-found {
  opacity: 0.7;
}
</style>

