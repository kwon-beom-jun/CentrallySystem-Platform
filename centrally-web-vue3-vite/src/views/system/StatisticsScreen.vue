<template>
  <div class="content content-wrapper">
    <!-- 헤더 -->
    <h2 class="content-title">{{ $t('system.statistics.title') }}</h2>
    <p class="content-sub-title">
      {{ $t('system.statistics.overview') }}
    </p>

    <DefaultLabel id="headcount" :text="'📊 ' + $t('system.statistics.users')" size="large" />
    <v-row class="mt-6 v-row-chart" dense>
      <HeadcountChart />
    </v-row>

    <template v-if="isReceiptEnabled">
      <hr class="hr-chart-gap" />

      <DefaultLabel id="receipt" :text="'🧾 ' + $t('system.statistics.receipts')" size="large" />
      <v-row class="mt-6 v-row-chart" dense>
        <ExpenseChart />
      </v-row>

      <hr class="hr-chart-gap" />

      <DefaultLabel
        id="deptteam"
        :text="'📈 ' + $t('system.statistics.approvals')"
        size="large"
      />
      <div class="v-row-chart">
        <DeptTimelineChart class="mt-6 v-row-chart" />
      </div>
    </template>
    
  </div>

  <!-- 네비게이션 컴포넌트 -->
  <div class="floating-nav" :class="{ collapsed: isCollapsed }">
    <DefaultFormRow align="center" class="nav-header">
      <DefaultLabel :text="$t('common.button.list')" class="nav-title-label" v-if="showNavLabel"/>
      <v-btn
        icon
        size="x-small"
        variant="text"
        class="ml-2"
        @click="isCollapsed = !isCollapsed"
      >
        <v-icon>{{ isCollapsed ? 'mdi-chevron-right' : 'mdi-chevron-left' }}</v-icon>
      </v-btn>
    </DefaultFormRow>

    <ul v-show="!isCollapsed">
      <hr class="hr-custom"/>
      <li>
        <a href="#" @click.prevent="scrollTo('headcount')">
          <DefaultLabel :text="'📊 ' + $t('system.statistics.users')" class="nav-label"/>
        </a>
      </li>
      <li>
        <a href="#" @click.prevent="scrollTo('receipt')">
          <DefaultLabel :text="'🧾 ' + $t('system.statistics.receipts')" class="nav-label"/>
        </a>
      </li>
      <li>
        <a href="#" @click.prevent="scrollTo('deptteam')">
          <DefaultLabel :text="'📈 ' + $t('system.statistics.approvals')" class="nav-label"/>
        </a>
      </li>
    </ul>
  </div>

</template>
<script setup>
import { ref, computed, onMounted } from "vue";
import HeadcountChart from "@/components/system/HeadcountChart.vue";
import ExpenseChart from "@/components/system/ExpenseChart.vue";
import DeptTimelineChart from "@/components/system/DeptTimelineChart.vue";
import DefaultLabel from "@/components/common/label/DefaultLabel.vue";
import DefaultFormRow from "@/components/common/DefaultFormRow.vue";

const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';

const isCollapsed = ref(false);
const windowWidth = ref(window.innerWidth);

// 반응형 바로가기 텍스트 노출 조건
const showNavLabel = computed(() => {
  return windowWidth.value >= 500 || (!isCollapsed.value && windowWidth.value < 500);
});

/* 자동 접힘 (1500px 이하) */
function handleResize() {
  const width = window.innerWidth;
  windowWidth.value = width;
  isCollapsed.value = width < 1500;
}

function scrollTo(id) {
  const el = document.getElementById(id);
  if (el) {
    el.scrollIntoView({ behavior: "smooth", block: "start" });
  }
}

onMounted(() => {
  handleResize(); // 초기 상태 설정
  window.addEventListener("resize", handleResize);
});
</script>

<style scoped>
.mt-6 {
  margin-top: 0px !important;
}
.hr-custom {
  margin: 10px 0 20px 0;
}
.v-row-chart {
  background-color: var(--theme-bg-secondary);
  align-items: center;
  padding-inline: 24px !important; /* = 24px 좌우 */
  padding-block: 16px !important;
  margin-top: 20px !important;
}

.floating-nav {
  min-width: 150px;
  position: fixed;
  top: 130px;
  /* 반응형 오른쪽 여백 처리 */
  right: clamp(10px, 3vw, 100px); /* 최소 10px, 최대 100px, 뷰포트 5% 기준 */
  background-color: var(--theme-bg-main);
  border: 1px solid var(--theme-border);
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  font-size: 0.875rem;
}

.floating-nav ul {
  list-style: none;
  padding: 0;
  margin-top: 0px;
}

.floating-nav li {
  margin-bottom: 20px;
}

.floating-nav a {
  color: var(--theme-text-primary);
  text-decoration: none;
}

.floating-nav a:hover {
  text-decoration: underline;
}
.floating-nav.collapsed {
  padding: 8px 10px;
  width: auto;
}

.floating-nav.collapsed .nav-header {
  justify-content: space-between;
}

.floating-nav.collapsed ul {
  display: none;
}

.floating-nav .nav-label {
  transition: all 0.1s !important;
}

.floating-nav a:hover .nav-label {
  color: #e0ca00 !important;
  font-weight: bold !important;
  cursor: pointer !important;
}

#headcount,
#receipt,
#deptteam {
  scroll-margin-top: 100px; /* 상단 고정 툴바나 여백만큼 확보 */
}

@media (max-width: 650px) {
  .v-row-chart {
    margin-top: 0px !important;
  }

  .hr-custom {
    margin: 0 0 20px 0;
  }

  .floating-nav {
    min-width: 140px;
    right: 10px;
    padding: 12px 16px;
    top: 100px;
  }

  .floating-nav li {
    margin-bottom: 15px;
  }

  .floating-nav.collapsed {
    padding: 8px 10px;
    width: auto;
  }

  .floating-nav.collapsed ul {
    display: none;
  }

  #headcount,
  #receipt,
  #deptteam {
    scroll-margin-top: 100px; /* 상단 고정 툴바나 여백만큼 확보 */
  }
}

@media (max-width: 500px) {
  .floating-nav {
    min-width: 0px;
    right: 10px;
    padding: 12px 16px;
    top: 80px !important;
  }
  .ml-2{
    margin: 0px !important;
  }
  .v-row-chart {
    background-color: #f9f9f9;
    align-items: center;
    padding-inline: 5px !important; /* = 24px 좌우 */
    padding-block: 16px !important;
  }
}
</style>
