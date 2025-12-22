<template>
  <!-- ───────── 영수증 차트 데이터 ───────── -->
  <v-col cols="12" md="6" class="chart-card-wrapper">
    <div class="expenseStats-data">
      <DefaultFormRow>
        <DefaultLabel :text="$t('system.expenseChart.summaryTitle')" />
      </DefaultFormRow>
      <DefaultLabel :text="$t('system.expenseChart.summaryDesc')" color="gray" size="small" marginBottom="10px" />
      <hr class="hr-custom" />
      <DefaultFormRow marginTop="10px" marginBottom="10px" align="between">
        <DefaultLabel marginRight="30px" :text="$t('system.expenseChart.period')" />
        <DefaultLabel :text="expenseStats.period" color="#777777" size="small" />
      </DefaultFormRow>
      <hr class="hr-custom" />
      <DefaultFormRow marginTop="10px" marginBottom="10px" align="between">
        <DefaultLabel marginRight="30px" :text="$t('system.expenseChart.avgPerDay')" />
        <DefaultLabel :text="expenseStats.avg" color="#777777" size="small" />
      </DefaultFormRow>
      <hr class="hr-custom" />
      <DefaultFormRow marginTop="10px" marginBottom="10px" align="between">
        <DefaultLabel marginRight="30px" :text="$t('system.expenseChart.max')" />
        <DefaultLabel :text="expenseStats.max" color="#777777" size="small" />
      </DefaultFormRow>
      <hr class="hr-custom" />
      <DefaultFormRow marginTop="10px" marginBottom="10px" align="between">
        <DefaultLabel marginRight="30px" :text="$t('system.expenseChart.min')" />
        <DefaultLabel :text="expenseStats.min" color="#777777" size="small" />
      </DefaultFormRow>
      <hr class="hr-custom" />
      <DefaultFormRow marginTop="10px" marginBottom="10px" align="between">
        <DefaultLabel marginRight="30px" :text="$t('system.expenseChart.total')" />
        <DefaultLabel :text="expenseStats.total" color="#777777" size="small" />
      </DefaultFormRow>
      <hr class="hr-custom" />
    </div>
  </v-col>

  <!-- ───────── 영수증 지출 차트 ───────── -->
  <v-col cols="12" md="6">
    <v-card>
      <!-- v-row · v-col 제거, spacer 사용 -->
      <v-card-title class="v-chart-title-custom d-flex align-center">
        <DefaultLabel :text="$t('system.expenseChart.chartTitle')" />

        <v-spacer />

        <!-- 시작 날짜 -->
        <DefaultTextfield
          v-model="start"
          id="startDate"
          type="date"
          size="xsmall"
          marginRight="8px"
          customHeight="32"
          class="date-input"
          @change="validateRange"
        />

        <!-- 종료 날짜 -->
        <DefaultTextfield
          v-model="end"
          id="endDate"
          type="date"
          size="xsmall"
          customHeight="32"
          class="date-input"
          @change="validateRange"
        />
      </v-card-title>

      <hr class="divider-with-gap" />

      <v-card-text class="v-chart-text-custom">
        <apexchart type="area" height="300" :options="opt" :series="ser" />
      </v-card-text>
    </v-card>
  </v-col>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useI18n } from 'vue-i18n';
import ReceiptsApi from '@/api/receipt/ReceiptsApi.js';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultFormRow from "@/components/common/DefaultFormRow.vue";
import DefaultLabel from "@/components/common/label/DefaultLabel.vue";
import { useToastStore } from '@/store/toast'

const { t } = useI18n();
const toISO = (d) => d.toISOString().substring(0, 10);
const today = new Date();
const start = ref(toISO(new Date(today.getTime() - 6 * 24 * 60 * 60 * 1000)));
const end = ref(toISO(today));

/* 통계 객체 */
const expenseStats = ref({
  period: "-",
  total: "-",
  avg: "-",
  max: "-",
  min: "-",
});

/* ──────────────────────────────────────────
 * ① 데이터 로딩 – 영수증 목록
 * ────────────────────────────────────────── */
const receipts = ref([]);

async function fetchReceipts () {
  try {
    const res = await ReceiptsApi.getReceiptsBySearchUserDateRange({
      startDate: start.value,
      endDate:   end.value,
      page: 0,
      size: 1000,
    });
    receipts.value = res.data?.content ?? [];
  } catch (err) {
    console.error('영수증 조회 실패', err);
  }
}

onMounted(fetchReceipts);
watch([start, end], () => {
  if (start.value <= end.value) fetchReceipts();
});

/* ──────────────────────────────────────────
 * ② 일별 합계 계산
 * ────────────────────────────────────────── */
const dailyMap = computed(() => {
  const map = {};
  receipts.value.forEach(r => {
    const d = (r.submissionDate ?? r.date ?? '').substring(0, 10);
    if (!d) return;
    const amt = Number(r.amount) || 0;
    map[d] = (map[d] || 0) + amt;
  });
  return map;
});

/* 날짜 범위 내 일자 배열 */
const dateRange = computed(() => {
  const arr = [];
  let cur = new Date(start.value);
  const endDate = new Date(end.value);
  while (cur <= endDate) {
    arr.push(toISO(cur));
    cur.setDate(cur.getDate() + 1);
  }
  return arr;
});

/* 차트 시리즈 */
const ser = computed(() => [{
  name: t('system.expenseChart.total'),
  data: dateRange.value.map(d => [d, dailyMap.value[d] || 0]),
}]);

/* 통계 계산을 위한 배열 */
const numsComputed = computed(() => dateRange.value.map(d => dailyMap.value[d] || 0));

const opt = {
  chart: { type: "area", toolbar: { show: false }, zoom: { enabled: false } },
  stroke: { curve: "smooth" },
  xaxis: { type: "datetime" },
  tooltip: {
    x: { format: "yyyy-MM-dd" },
    y: { formatter: (v) => v.toLocaleString() + t('system.common.wonSymbol') },
  },
  colors: ["#1cc88a"],
};

function validateRange () {
  if (start.value > end.value) {
    useToastStore().warning(t('system.expenseChart.invalidRange'))
    // 가장 최근 값으로 되돌리기
    const tmp = start.value;
    start.value = end.value;
    end.value   = tmp;
  }
}

/* 통계 계산 → expenseStats 갱신 */
watch(
  numsComputed,
  () => {
    const total = numsComputed.value.reduce((a, b) => a + b, 0);
    const avg = Math.round(total / (numsComputed.value.length || 1));
    const max = Math.max(...numsComputed.value);
    const min = Math.min(...numsComputed.value);

    expenseStats.value = {
      period: `${start.value} ~ ${end.value}`,
      total: total.toLocaleString() + t('system.common.wonSymbol'),
      avg: avg.toLocaleString() + t('system.common.wonSymbol'),
      max: max.toLocaleString() + t('system.common.wonSymbol'),
      min: min.toLocaleString() + t('system.common.wonSymbol'),
    };
  },
  { immediate: true }
);
</script>

<style scoped>
@media (max-width: 500px) {
  /* DefaultTextfield 내부 input(.xsmall-input)까지 스타일이 전달되도록 :deep 사용 */
  .date-input :deep(.xsmall-input) {
    width: 94px !important;  /* 시작·종료 날짜 전용 폭 조정 */
    min-width: 94px !important;
    font-size: 0.52rem !important;
  }
  /* date 타입 전용 더 세밀한 설정 (브라우저 기본 스타일 우선순위 대응) */
  .date-input :deep(input[type='date']) {
    width: 94px !important;
    min-width: 94px !important;
    font-size: 0.52rem !important;
  }
} 
</style>