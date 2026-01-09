<template>
  <div>
    <!-- Main Content Area -->
    <div class="content content-wrapper">
      <h2 class="content-title">{{ $t('hrm.performanceHistory.title') }}</h2>
      <p class="content-sub-title">{{ $t('hrm.performanceHistory.subtitle') }}</p>

      <!-- Table View (Visible on larger screens) -->
      <table class="table mt-3" id="dataTable" v-if="!isMobile">
        <thead>
          <tr>
            <th>{{ $t('hrm.performanceHistory.performance') }}</th>
            <th>{{ $t('hrm.performanceHistory.startDate') }}</th>
            <th>{{ $t('hrm.performanceHistory.endDate') }}</th>
            <th>{{ $t('hrm.performanceHistory.content') }}</th>
            <th>{{ $t('hrm.performanceHistory.achievement') }}</th>
            <th>{{ $t('hrm.performanceHistory.status') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in paginatedData" :key="item.date">
            <td>{{ item.performanceName }}</td>
            <td>{{ item.beforeDate }}</td>
            <td>{{ item.afterDate }}</td>
            <td>{{ item.content }}</td>
            <td>{{ item.performance }}</td>
            <td :class="getStatusClass(item.status)">{{ getStatusLabel(item.status) }}</td>
          </tr>
        </tbody>
      </table>

      <!-- Card Layout View (Visible on smaller screens) -->
      <div class="card-layout" v-if="isMobile">
        <div class="card" v-for="item in paginatedData" :key="item.date">
          <div class="card-header">
            <p class="card-title">{{ item.performanceName }}</p>
          </div>
          <div class="card-body">
            <p class="card-text"><strong>{{ $t('hrm.performanceHistory.startDate') }} : </strong> {{ item.beforeDate }} ~ <strong> {{ $t('hrm.performanceHistory.endDate') }} : </strong>{{
        item.afterDate }}</p>
            <p class="card-text"><strong>{{ $t('hrm.performanceHistory.content') }}:</strong> {{ item.content }}</p>
            <p class="card-text"><strong>{{ $t('hrm.performanceHistory.achievement') }}:</strong> {{ item.performance }}</p>
            <p class="card-text">
              <strong>{{ $t('hrm.performanceHistory.status') }}:</strong> <span :class="getStatusClass(item.status)">{{ getStatusLabel(item.status) }}</span>
            </p>
          </div>
        </div>
      </div>

      <!-- Pagination Buttons -->
      <nav>
        <ul class="pagination justify-content-center" id="pagination">
          <li class="page-item" :class="{ active: page === currentPage }" v-for="page in totalPages" :key="page">
            <a class="page-link" href="#" @click.prevent="goToPage(page)">{{ page }}</a>
          </li>
        </ul>
      </nav>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useViewStateStore } from '@/store/viewState'

const { t } = useI18n();

const data = ref([
  { beforeDate: '2024-10-03', afterDate: '2024-11-01', performanceName: 'CENTRALLY 포탈 개발', content: '분기 보고서 작성', performance: '대기 중', status: '대기 중' },
  { beforeDate: '2024-08-01', afterDate: '2024-09-01', performanceName: '○○카드 프로젝트', content: '프로젝트 A 진행', performance: '완료', status: '완료' },
  { beforeDate: '2024-09-02', afterDate: '2024-10-01', performanceName: '○○은행 프로젝트 계약', content: '팀 미팅', performance: '진행 중', status: '진행 중' },
  // Additional data...
]);

const currentPage = ref(1);
const itemsPerPage = ref(5);
const isMobile = ref(window.innerWidth <= 650);
const viewState = useViewStateStore();

const totalPages = computed(() => Math.ceil(data.value.length / itemsPerPage.value));

const paginatedData = computed(() => {
  const startIndex = (currentPage.value - 1) * itemsPerPage.value;
  return data.value.slice(startIndex, startIndex + itemsPerPage.value);
});

const goToPage = (page) => {
  currentPage.value = page;
  viewState.saveState('hrmPerformanceHistory', {
    currentPage: currentPage.value,
    itemsPerPage: itemsPerPage.value,
    scrollY: window.scrollY,
  })
};

const updateIsMobile = () => {
  isMobile.value = window.innerWidth <= 650;
};

const getStatusClass = (status) => {
  if (status === '진행 중' || status === t('hrm.performanceHistory.statusInProgress')) return 'text-primary'; // Blue
  if (status === '완료' || status === t('hrm.performanceHistory.statusComplete')) return 'text-success'; // Green
  if (status === '대기 중' || status === t('hrm.performanceHistory.statusPending')) return 'text-warning'; // Orange
  return '';
};

const getStatusLabel = (status) => {
  if (status === '진행 중') return t('hrm.performanceHistory.statusInProgress');
  if (status === '완료') return t('hrm.performanceHistory.statusComplete');
  if (status === '대기 중') return t('hrm.performanceHistory.statusPending');
  return status;
};

onMounted(() => {
  window.addEventListener('resize', updateIsMobile);
  const saved = viewState.getState('hrmPerformanceHistory')
  const restore = viewState.canRestore('hrmPerformanceHistory')
  if (restore && saved) {
    currentPage.value = saved.currentPage ?? 1
    itemsPerPage.value = saved.itemsPerPage ?? itemsPerPage.value
    requestAnimationFrame(()=>{ window.scrollTo(0, saved.scrollY ?? 0) })
  } else {
    viewState.clearState('hrmPerformanceHistory')
  }
});
</script>

<style scoped>
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#dataTable tbody tr:hover td {
  background-color: #e7f1ff !important;
}

.text-muted {
  margin-bottom: 10px;
}
</style>
