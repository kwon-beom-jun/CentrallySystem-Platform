<template><!-- ───────── 재직 인원 차트 ───────── -->
    <v-col cols="12" md="6">
      <v-card>
        <v-card-title class="d-flex justify-space-between align-center v-chart-title-custom">
          <DefaultLabel :text="$t('system.headcount.title')" />

          <v-select
            v-model="granularity"
            :items="granularities"
            density="compact"
            hide-details
            variant="outlined"
            class="employee-select"
          >
            <template v-slot:selection="{ item }">
              <span class="select-small">{{ item.title ?? item }}</span>
            </template>
            <template v-slot:item="{ props, item }">
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
          <apexchart
            type="bar"
            height="300"
            :options="chartOpt"
            :series="chartSeries"
          />
        </v-card-text>
      </v-card>
    </v-col>

    <!-- ───────── 재직 인원 통계 데이터 (테이블) ───────── -->
    <v-col cols="12" md="6">
      <div class="employees-data">

        <DefaultFormRow marginTop="20px">
          <DefaultLabel :text="$t('system.headcount.tableTitle')" size="large" customClass="hr-label"/>
        </DefaultFormRow>

        <DefaultLabel :text="$t('system.headcount.tableDesc')" color="gray" size="small" marginBottom="20px" />

        <DefaultTable
          :columns="tableColumns"
          :data="tableData"
          :bodyFontSize="'0.8rem'"
          :fixedHeader="true"
          :scrollHeight="310"
          scroll
          :minRows="6"
          :noDataImageHeight="302"
        />
      </div>
    </v-col>
</template>

<script setup>
// HRM 서비스와 연동하여 실시간 직원 통계를 표시합니다.
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import UsersApi from '@/api/hrm/UsersApi.js';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultTable from '@/components/common/table/DefaultTable.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
const { t } = useI18n();

/* ──────────────────────────────────────────
 * ① 상태 & 초기 fetch
 * ────────────────────────────────────────── */
const users = ref([]);                    // 전체 사용자 목록

async function fetchUsers() {
  try {
    const res = await UsersApi.getUsers(); // 활성 사용자만 조회
    users.value = res.data ?? [];
  } catch (err) {
    console.error('사용자 목록 조회 실패', err);
  }
}

onMounted(fetchUsers);

/* Granularity(기존 UI 유지용, 실제로는 의미 無) */
const granularities = computed(() => [t('system.headcount.department'), t('system.headcount.team')]);
const granularity   = ref(t('system.headcount.department'));

/* ──────────────────────────────────────────
 * ② 부서·팀별 집계
 * ────────────────────────────────────────── */
// 부서별 인원
const deptMap = computed(() => {
  const map = {};
  users.value.forEach(u => {
    const dept = u?.team?.department?.departmentName   // 팀→부서 경로
              ?? u?.department?.departmentName
              ?? u.department                          // basic DTO 용
              ?? t('system.common.unassigned');

    const team = u?.team?.teamName ?? u.team ?? t('system.common.unassigned');

    if (!map[dept]) {
      map[dept] = { total: 0, teams: {} };
    }
    map[dept].total += 1;
    map[dept].teams[team] = (map[dept].teams[team] || 0) + 1;
  });
  return map;
});

// 팀별 인원 (전사 기준)
const teamMap = computed(() => {
  const map = {};
  users.value.forEach(u => {
    const team = u?.team?.teamName ?? u.team ?? '미지정';
    map[team] = (map[team] || 0) + 1;
  });
  return map;
});

/* ──────────────────────────────────────────
 * ③ 차트 데이터
 * ────────────────────────────────────────── */
const chartCategories = computed(() =>
  granularity.value === t('system.headcount.department')
    ? Object.keys(deptMap.value)
    : Object.keys(teamMap.value)
);

const chartSeries = computed(() => [{
  name: t('system.headcount.seriesName'),
  data: granularity.value === t('system.headcount.department')
          ? chartCategories.value.map(cat => deptMap.value[cat].total)
          : chartCategories.value.map(team => teamMap.value[team]),
}]);

const chartOpt = computed(() => ({
  chart: { toolbar: { show: false } },
  xaxis: { categories: chartCategories.value },
  tooltip: { y: { formatter: v => `${v} ${t('system.common.countSuffix') || '명'}` } },
  colors: ['#4e73df'],
}));

/* ──────────────────────────────────────────
 * ④ 테이블 데이터
 * ────────────────────────────────────────── */
const deptColumns = [
  { key: 'dept',      label: t('system.headcount.department'),      width: 'auto', minWidth: 100 },
  { key: 'deptTotal', label: t('system.headcount.deptCount'), width: 'auto', minWidth: 100  },
  /* { key: 'team',      label: '팀',        width: 'auto', minWidth: 100 },
  { key: 'teamTotal', label: '팀 인원',   width: 80  }, */
];

const teamColumns = [
  { key: 'team',      label: t('system.headcount.team'),      width: 'auto', minWidth: 100 },
  { key: 'teamTotal', label: t('system.headcount.teamCount'), width: 'auto', minWidth: 100 },
];

const tableColumns = computed(() => (
  granularity.value === t('system.headcount.department') ? deptColumns : teamColumns
));

const tableData = computed(() => {
  if (granularity.value === '부서') {
    const rows = [];
    Object.entries(deptMap.value).forEach(([deptName, { total, teams }]) => {
      const teamEntries = Object.entries(teams);
      if (teamEntries.length === 0) {
        rows.push({ dept: deptName, deptTotal: `${total}${t('system.common.countSuffix') || '명'}`, team: '-', teamTotal: '-' });
      } else {
        teamEntries.forEach(([teamName, cnt]) => {
          rows.push({
            dept: deptName,
            deptTotal: `${total}${t('system.common.countSuffix') || '명'}`,
            team: teamName,
            teamTotal: `${cnt}${t('system.common.countSuffix') || '명'}`,
          });
        });
      }
    });
    return rows;
  }
  // 팀별 모드
  return Object.entries(teamMap.value).map(([team, cnt]) => ({
    team,
    teamTotal: `${cnt}${t('system.common.countSuffix') || '명'}`,
  }));
});
</script>

<style scoped>
@media (max-width: 650px) {
  .hr-label {
    font-size: 0.95rem !important;
  }
}
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