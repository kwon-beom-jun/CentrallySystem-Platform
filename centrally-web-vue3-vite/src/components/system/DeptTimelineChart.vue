<template>
  <v-card class="dept-timeline-chart" v-bind="$attrs">
    <v-card-title class="text-subtitle-2 d-flex justify-space-between v-chart-title-custom align-center">
      <DefaultLabel :text="$t('system.deptTimeline.title')" />

      <v-select
        v-model="gran"
        :items="gItems"
        density="compact"
        hide-details
        variant="outlined"
        class="employee-select"
        style="max-width: 60px"
      >
        <!-- 선택돼 있는 값 -->
        <template #selection="{ item }">
          <span class="select-small">{{ item.title ?? item }}</span>
        </template>

        <!-- 드롭다운 항목 -->
        <template #item="{ props, item }">
          <v-list-item v-bind="props" title="">
            <v-list-item-title class="select-small-option">
              {{ item.title ?? item }}
            </v-list-item-title>
          </v-list-item>
        </template>
      </v-select>
    </v-card-title>
    
    <hr class="divider-with-gap" />

      <v-card-text class="v-chart-text-custom">
        <!-- 꺾은선 그래프 -->
        <apexchart type="line" height="350" :options="optSel" :series="serSel" />

        <DefaultFormRow align="right">
          <v-btn icon @click="zoomIn" size="x-small" class="mx-1 mb-3">
            <v-icon size="18">mdi-magnify-plus-outline</v-icon>
          </v-btn>
          <v-btn icon @click="zoomOut" size="x-small" class="mx-1 mb-3">
            <v-icon size="18">mdi-magnify-minus-outline</v-icon>
          </v-btn>
        </DefaultFormRow>
      </v-card-text>
  </v-card>
  <hr/>

  <!-- 📊 테이블 영역 -->
  <div class="mt-6">
    <DefaultFormRow marginBottom="15px">
      <DefaultLabel :text="$t('system.deptTimeline.deptTableTitle')" size="large" customClass="hr-label" />
    </DefaultFormRow>

    <DefaultTable
      :columns="tblColumns"
      :data="tblData"
      scroll
      :scrollHeight="300"
      :bodyFontSize="'0.8rem'"
      :fixedHeader="true"
      :minRows="3"
      :noDataImageHeight="173"
    />
  </div>
  <hr/>

  <!-- 📊 테이블 영역 -->
  <!-- 팀별 지출 영역 -->
  <div class="mt-6">
    <DefaultFormRow marginBottom="15px">
      <DefaultLabel :text="$t('system.deptTimeline.teamTableTitle')" size="large" customClass="hr-label" />
    </DefaultFormRow>

    <DefaultTable
      :columns="teamColumns"
      :data="teamTblData"
      scroll
      :scrollHeight="300"
      :bodyFontSize="'0.8rem'"
      :fixedHeader="true"
      :minRows="3"
      :noDataImageHeight="173"
    />
  </div>
  <hr/>
</template>

<script setup>
import "@/assets/styles/pages/cart.css";
import { ref, computed, watch, onMounted } from "vue";
import { useI18n } from 'vue-i18n';
import ReceiptsApi from '@/api/receipt/ReceiptsApi.js';

defineOptions({
  inheritAttrs: false
});
import DefaultFormRow from "@/components/common/DefaultFormRow.vue";
import DefaultLabel   from "@/components/common/label/DefaultLabel.vue";
import DefaultTable   from "@/components/common/table/DefaultTable.vue";
import ApexCharts from "apexcharts";

const { t } = useI18n();

/* ── 월 선택(일 단위는 추후 구현) ─────── */
const gItems = computed(() => [{ title: t('system.common.month'), value: 'M' }]);
const gran   = ref('M');
/* ── 축 레이블 ──────────────── */
const months = computed(() => Array.from({ length: 12 }, (_, i) => `${i+1}${t('system.common.monthShort')}`));
/* 일 단위는 차후 구현 */
const days   = computed(() => Array.from({ length: 31 }, (_, i) => `${i + 1}`));

/* ── 영수증 데이터 로딩 ───────────── */
const receipts = ref([]);

async function fetchYearReceipts() {
  const today = new Date();
  const start = `${today.getFullYear()}-01-01`;
  const end   = `${today.getFullYear()}-12-31`;
  try {
    const res = await ReceiptsApi.getReceiptsBySearchUserDateRange({
      startDate: start,
      endDate:   end,
      page: 0,
      size: 2000,
    });
    receipts.value = res.data?.content ?? [];
  } catch (err) {
    console.error('부서별 영수증 조회 실패', err);
  }
}

onMounted(fetchYearReceipts);

/* ── 부서별 월 합계 계산 ─────────── */
const serMon = computed(() => {
  const map = {};
  receipts.value.forEach(r => {
    const dept = r.departmentName ?? t('system.common.unassigned');
    const date = new Date(r.submissionDate ?? r.date ?? '');
    if (isNaN(date)) return;
    const mIdx = date.getMonth();
    const amt  = (Number(r.amount) || 0) / 10000; // 만 단위
    if (!map[dept]) map[dept] = Array(12).fill(0);
    map[dept][mIdx] += amt;
  });
  return Object.entries(map).map(([dept, arr]) => ({
    name: dept,
    data: arr.map(v => Math.round(v)),
  }));
});

/* 일 단위 시리즈(미구현) */
const serDay = computed(() => []);

/* ── 차트 옵션 ──────────────── */
const baseOpt = {
  chart:{
    id: "dept-line",
    toolbar:{ show:false },
    zoom:{ enabled:true },
    events:{ mounted: onChartMounted }
  },
  stroke:{ curve:"smooth", width:2 },
  markers:{ size:3 },
  yaxis:{ labels:{ formatter:v=>v.toLocaleString()+'('+t('system.common.expenseTenThousand').replace(/.*\((.*)\).*/, '$1')+')' } },
  tooltip:{ y:{ formatter:v=>v.toLocaleString()+'('+t('system.common.expenseTenThousand').replace(/.*\((.*)\).*/, '$1')+')' } },
  legend:{ position:"top" },
  colors:["#4e73df","#1cc88a","#f6c23e"]
};

const serSel = computed(() => gran.value === 'M' ? serMon.value : serDay.value);
const optSel = computed(() => ({
  ...baseOpt,
  xaxis:{ categories: gran.value === 'M' ? months.value : days.value }
}));

function onChartMounted(ctx) {
  xRange.value = { min: ctx.w.globals.minX, max: ctx.w.globals.maxX };
}

// 현재 보여 주는 x축 구간 기억
const xRange   = ref({ min: 0, max: months.value.length - 1 });
const zoomStack = [];

function zoomIn () {
  const { min, max } = xRange.value;
  if (max - min <= 1) return;             // 더 이상 못 줄이면 끝

  // 현재 범위를 기록
  zoomStack.push({ min, max });           // ★ push

  // 25 %씩 잘라내기 (한쪽 12.5 %)
  const span   = max - min + 1;
  const shrink = Math.ceil(span * 0.125);
  const newMin = min + shrink;
  const newMax = max - shrink;
  if (newMin >= newMax) return;           // 안전장치

  ApexCharts.exec("dept-line", "zoomX", newMin, newMax);
  xRange.value = { min: newMin, max: newMax };
}

function zoomOut () {
  const fullMax = (gran.value === 'M' ? months.value : days.value).length - 1;

  // 직전 범위로 복귀
  if (zoomStack.length) {
    const { min, max } = zoomStack.pop();    // ★ pop
    ApexCharts.exec("dept-line", "zoomX", min, max);
    xRange.value = { min, max };
    return;
  }

  // 스택이 비었다 = 이미 최상위. 전체로 리셋
  ApexCharts.exec("dept-line", "resetZoom");
  xRange.value = { min: 0, max: fullMax };
}

/* ── 📑 테이블 컬럼 ───────────── */
const monthCols = computed(() => months.value.map((m, i) => ({
  key: `m${i + 1}`, label: m, width: 60
})));
const tblColumns = computed(() => (
  gran.value === 'M'
    ? [
        { key: 'dept', label: t('system.deptTimeline.department'), width: 'auto', minWidth: 100 },
        ...monthCols.value,
        { key: 'sum',  label: t('system.common.sumTenThousand'), width: 100 }
      ]
    : [
        { key: 'dept',  label: t('system.deptTimeline.department'), width: 'auto', minWidth: 100 },
        { key: 'label', label: t('system.common.month'), width: 80 },
        { key: 'value', label: t('system.common.expenseTenThousand'), width: 100 }
      ]
));

/* ── 테이블 데이터 생성 ──────── */
const tblData = computed(() => {
  if (gran.value === 'M') {
    // 월별 피벗 테이블
    return serMon.value.map(dept => {
      const row = { dept: dept.name };
      let sum = 0;
      dept.data.forEach((v, idx) => {
        row[`m${idx + 1}`] = v.toLocaleString();
        sum += v;
      });
      row.sum = sum.toLocaleString();
      return row;
    });
  }

  // '일' 모드 – 기존 방식
  const labels = days;
  const rows = [];
  serDay.value.forEach(dept => {
    labels.forEach((lab, idx) => {
      rows.push({
        dept : dept.name,
        label: lab,
        value: dept.data[idx].toLocaleString()
      });
    });
  });
  return rows;
});


/* ── 1) ‘팀별’ 월 합계 ───────────── */
const teamMon = computed(() => {
  const map = {};
  receipts.value.forEach(r => {
    const team = r.teamName ?? t('system.common.unassigned');
    const date = new Date(r.submissionDate ?? r.date ?? '');
    if (isNaN(date)) return;
    const idx = date.getMonth();
    const amt = (Number(r.amount) || 0) / 10000; // 만 단위
    if (!map[team]) map[team] = Array(12).fill(0);
    map[team][idx] += amt;
  });
  return Object.entries(map).map(([team, arr]) => ({
    name: team,
    data: arr.map(v => Math.round(v)),
  }));
});

/* ── 2) 팀별 테이블 컬럼(고정) ─────────── */
const teamColumns = computed(() => ([
  { key:'team',  label: t('system.deptTimeline.team'),  width:'auto', minWidth: 100 },
  ...monthCols.value,                        // 1~12월 열 재사용
  { key:'sum',  label: t('system.common.sumTenThousand'), width:80 }
]));

/* ── 3) 팀별 테이블 데이터 생성 ───────── */
const teamTblData = computed(() => {
  return teamMon.value.map(team => {
    const row = { team: team.name };
    let sum = 0;
    team.data.forEach((v, idx) => {
      row[`m${idx+1}`] = v.toLocaleString();
      sum += v;
    });
    row.sum = sum.toLocaleString();
    return row;
  });
});

watch(gran, val => {
  const fullMax = (val === 'M' ? months.value : days.value).length - 1;
  xRange.value = { min: 0, max: fullMax };
  zoomStack.length = 0;                      // ★ 스택 비우기
  ApexCharts.exec("dept-line", "resetZoom");
 });
</script>

<style scoped>
@media (max-width: 650px) {
  .hr-label {
    font-size: 0.9rem !important;
  }
  .v-field__input {
    max-width: 110px !important;
    padding-right: 5px !important;
    padding-left: 10px !important;
    font-size: 0.7rem !important;
  }
}
/* 필요 시 추가 커스텀 */
@media (max-width: 500px) { 
  .v-chart-text-custom {
    padding: 0 10px 10px 0 !important;
  }
  /* ▼ 모바일 환경에서 select 박스(부서/팀) 높이 강제 축소 */
  .employee-select:deep(.v-input__control),
  .employee-select:deep(.v-field),
  .employee-select:deep(.v-field__overlay) {
    min-height: 32px !important;
    height: 32px !important;
  }
  /* ▼ 모바일 환경에서 select 박스(부서/팀) 크기 축소 */
  .employee-select:deep(.v-field) {
    min-height: 32px !important; /* 높이 감소 */
    height: 32px !important;
    display: flex;
    align-items: center;   /* 세로 중앙 */
    justify-content: center; /* 가로 중앙 */
    line-height: 1;        /* 불필요한 여백 제거 */
    text-align: center;
    width: 100%;
  }
  .employee-select:deep(.v-select__selection-text),
  .select-small,
  .select-small-option {
    font-size: 0.7rem !important; /* 폰트 축소 */
  }
}
</style>
