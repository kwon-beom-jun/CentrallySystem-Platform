<template>
  <div class="content content-wrapper">
    <PageTitle 
      :title="$t('system.kafkaOutbox.title')"
      :subtitle="$t('system.kafkaOutbox.list')"
      icon="ri-stack-line"
    />

    <DefaultFormRow marginBottom="10px" align="right">
      <div class="filter-grid">
        <div class="service-left">
          <DefaultLabel :text="$t('system.status.services')" size="small"/>
          <DefaultSelect
            v-model="service"
            :options="serviceOptions"
            size="full"
            style="width: 80%"
          />
        </div>
        <div>
          <DefaultLabel :text="$t('common.label.startDate')" size="small"/>
          <DefaultTextfield type="date" v-model="fromDate" size="xsmall" />
        </div>
        <div>
          <DefaultLabel :text="$t('common.label.endDate')" size="small"/>
          <DefaultTextfield type="date" v-model="toDate" size="xsmall" />
        </div>
        <div>
          <DefaultLabel text="" size="small"/>
          <DefaultButton color="gray" size="small" @click="onSearch">{{ $t('common.button.search') }}</DefaultButton>
        </div>
      </div>
    </DefaultFormRow>
    

    <DefaultTable
      :columns="outboxColumns"
      :data="rows"
      :minRows="pageSize"
      :rowClick="openDetail"
      :usePagination="true"
      :currentPage="currentPage"
      :totalPages="totalPages"
      :visiblePageCount="5"
      @pageChange="onPageChange"
      :noDataImageHeight="474"
    />

    <KafkaMonitorDetailModal
      v-if="detailOpen"
      :row="detailRow"
      :service="service"
      @close="detailOpen = false"
    />
  </div>
</template>

<script setup>
import { ref, watch, computed } from "vue";
import DefaultFormRow from "@/components/common/DefaultFormRow.vue";
import DefaultLabel from "@/components/common/label/DefaultLabel.vue";
import DefaultTextfield from "@/components/common/textfield/DefaultTextfield.vue";
import DefaultSelect from "@/components/common/select/DefaultSelect.vue";
import DefaultTable from "@/components/common/table/DefaultTable.vue";
import KafkaMonitorApi from "@/api/system/KafkaMonitorApi";
import { getCurrentDateKST } from "@/utils/dateUtils";
import KafkaMonitorDetailModal from "@/components/system/KafkaMonitorDetailModal.vue";
import { usePageSize10or8 } from "@/composables/usePageSize";
import DefaultButton from "@/components/common/button/DefaultButton.vue";

const service = ref("auth");
const pageSize = usePageSize10or8();
const currentPage = ref(1);
const totalPages = ref(1);
const fromDate = ref(getCurrentDateKST());
const toDate = ref(getCurrentDateKST());

const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === "true";
const serviceOptions = computed(() => {
  const opts = [
    { value: "auth", label: "AUTH" },
    { value: "hrm", label: "HRM" },
    { value: "info", label: "INFO" },
  ];
  if (isReceiptEnabled) opts.push({ value: "receipt", label: "RECEIPT" });
  return opts;
});

function toYYMMDD_HH_MM_SS(value) {
  if (!value) return "";
  try {
    const d = new Date(value);
    const k = new Date(d.getTime() + 9 * 60 * 60 * 1000);
    const yy = String(k.getUTCFullYear()).slice(-2);
    const MM = String(k.getUTCMonth() + 1).padStart(2, "0");
    const dd = String(k.getUTCDate()).padStart(2, "0");
    const hh = String(k.getUTCHours()).padStart(2, "0");
    const mm = String(k.getUTCMinutes()).padStart(2, "0");
    const ss = String(k.getUTCSeconds()).padStart(2, "0");
    return `${yy}-${MM}-${dd} ${hh}:${mm}:${ss}`;
  } catch (e) {
    return String(value);
  }
}

import { useI18n } from 'vue-i18n';
const { t, messages, locale } = useI18n();

/**
 * eventType을 한글로 변환하는 헬퍼 함수
 * 점(.)이 포함된 키는 t() 함수로 접근 불가하므로 messages 객체에 직접 접근
 */
function getEventTypeLabel(eventType) {
  if (!eventType) return '';
  try {
    const currentLocale = locale.value || 'ko';
    const eventTypes = messages.value?.[currentLocale]?.system?.kafka?.eventTypes;
    return eventTypes?.[eventType] || eventType;
  } catch (e) {
    return eventType;
  }
}

const outboxColumns = [
  { 
    key: "function", 
    label: t('system.kafka.function'), 
    width: 'auto',
    customValue: (row) => getEventTypeLabel(row.eventType)
  },
  { key: "status", label: t('system.kafkaOutbox.status'), width: 100, align: 'center'},
  { key: "id", label: "ID", width: 'auto', minWidth: 330, mobileDisable: true },
  { key: "eventType", label: t('system.kafkaOutbox.eventType'), width: '200', mobileDisable: true },
  { key: "aggregateType", label: t('system.kafkaOutbox.aggregateType'), width: 100, mobileDisable: true },
  { key: "aggregateId", label: t('system.kafkaOutbox.aggregateId'), width: 80, mobileDisable: true },
  {
    key: "createdAt",
    label: t('system.kafkaOutbox.createdAt'),
    width: 140,
    customValue: (row) => toYYMMDD_HH_MM_SS(row.createdAt),
    mobileDisable: true,
  },
  {
    key: "lastTriedAt",
    label: t('system.kafkaOutbox.lastTriedAt'),
    width: 140,
    customValue: (row) => toYYMMDD_HH_MM_SS(row.lastTriedAt),
    mobileDisable: true,
  },
];

const rows = ref([]);
const detailOpen = ref(false);
const detailRow = ref(null);

async function fetch(page = 1) {
  const params = {
    page: page - 1,
    size: pageSize.value,
    fromDate: fromDate.value,
    toDate: toDate.value,
  };
  const res = await KafkaMonitorApi.getOutbox(service.value, params);
  const body = res.data;
  rows.value = body.content ?? body.items ?? [];
  totalPages.value = body.totalPages ?? 1;
}

function onPageChange(p) {
  currentPage.value = p;
  fetch(p);
}

// 초기 로드
fetch(1);

// 명시적 조회 버튼 사용
function onSearch() {
  currentPage.value = 1;
  fetch(1);
}

function openDetail(row) {
  detailRow.value = row;
  detailOpen.value = true;
}

// 서비스 변경 시 자동 조회
watch(service, () => {
  currentPage.value = 1;
  fetch(1);
});

// 플래그 변동 등으로 현재 선택 서비스가 옵션에서 사라진 경우 보정
watch(serviceOptions, (opts) => {
  if (!opts.some((o) => o.value === service.value)) {
    service.value = opts[0]?.value || "auth";
  }
});
</script>

<style scoped>
.service-left { margin-right: auto; }
.filter-grid {
  display: flex;
  gap: 2px;
  width: 100%;
  align-items: end;
}
</style>
