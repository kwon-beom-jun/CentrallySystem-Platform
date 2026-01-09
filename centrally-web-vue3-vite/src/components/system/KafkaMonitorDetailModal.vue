<template>
  <b-modal
    ref="detailModal"
    v-model="show"
    :title="$t('system.common.detail')"
    :keyboard="false"
    :backdrop="'static'"
    centered
    hide-footer
    dialog-class="detail-modal-dialog"
    @hidden="notifyParent"
  >
    <!-- 1) ID 한 줄 -->
    <DefaultFormRow marginBottom="12px">
      <div style="flex: 1 1 0; min-width: 0">
        <DefaultLabel text="ID" size="small" />
        <DefaultTextfield type="text" :modelValue="row?.id || ''" size="full" disabled />
      </div>
    </DefaultFormRow>

    <!-- 2) 서비스 | Aggregate 한 줄 -->
    <DefaultFormRow align="betweenEqual" gap="10px" marginBottom="12px">
      <div>
        <DefaultLabel :text="$t('system.kafka.service')" size="small" />
        <DefaultTextfield type="text" :modelValue="serviceLabel" size="full" disabled />
      </div>
      <div>
        <DefaultLabel :text="$t('system.kafka.aggregate')" size="small" />
        <DefaultTextfield type="text" :modelValue="row?.aggregateType || ''" size="full" disabled />
      </div>
    </DefaultFormRow>

    <!-- 3) 기능 | 상태 한 줄 -->
    <DefaultFormRow align="betweenEqual" gap="10px" marginBottom="12px">
      <div>
        <DefaultLabel :text="$t('system.kafka.function')" size="small" />
        <DefaultTextfield type="text" :modelValue="eventTypeLabel" size="full" disabled />
      </div>
      <div>
        <DefaultLabel :text="$t('system.kafka.status')" size="small" />
        <DefaultTextfield type="text" :modelValue="row?.status || ''" size="full" disabled />
      </div>
    </DefaultFormRow>

    <!-- 4) 이벤트 타입 -->
    <DefaultFormRow marginBottom="12px">
      <div style="flex: 1 1 0; min-width: 0">
        <DefaultLabel :text="$t('system.kafka.event')" size="small" />
        <DefaultTextfield type="text" :modelValue="row?.eventType || ''" size="full" disabled />
      </div>
    </DefaultFormRow>

    <!-- 5) 생성일 | 최종시도 한 줄 -->
    <DefaultFormRow align="betweenEqual" gap="10px" marginBottom="12px">
      <div>
        <DefaultLabel :text="$t('system.kafka.createdAt')" size="small" />
        <DefaultTextfield type="text" :modelValue="fmt(row?.createdAt)" size="full" disabled />
      </div>
      <div>
        <DefaultLabel :text="$t('system.kafka.lastTriedAt')" size="small" />
        <DefaultTextfield type="text" :modelValue="fmt(row?.lastTriedAt)" size="full" disabled />
      </div>
    </DefaultFormRow>

    <!-- 6) Payload (JSON) -->
    <DefaultFormRow marginBottom="12px" v-if="row?.payload">
      <div style="flex: 1 1 0; min-width: 0">
        <DefaultLabel :text="$t('system.kafka.payload')" size="small" />
        <DefaultTextarea :modelValue="formattedPayload" size="full" rows="8" disabled style="font-family: monospace; font-size: 12px;" />
      </div>
    </DefaultFormRow>

    <!-- 7) 에러 메시지 -->
    <DefaultFormRow marginBottom="12px" v-if="row?.errorMessage">
      <div style="flex: 1 1 0; min-width: 0">
        <DefaultLabel :text="$t('system.kafka.errorMessage')" size="small" />
        <DefaultTextarea :modelValue="row?.errorMessage" size="full" rows="5" disabled />
      </div>
    </DefaultFormRow>

    <DefaultFormRow align="right" marginTop="20px">
      <DefaultButton @click="show = false" color="gray" size="small">{{ $t('system.common.close') }}</DefaultButton>
    </DefaultFormRow>
  </b-modal>
</template>

<script setup>
import { defineProps, defineEmits, ref, computed, onMounted, nextTick } from "vue";
import { useI18n } from 'vue-i18n';
const { t, messages, locale } = useI18n();
import DefaultFormRow from "@/components/common/DefaultFormRow.vue";
import DefaultLabel from "@/components/common/label/DefaultLabel.vue";
import DefaultTextfield from "@/components/common/textfield/DefaultTextfield.vue";
import DefaultTextarea from "@/components/common/textarea/DefaultTextarea.vue";
import DefaultButton from "@/components/common/button/DefaultButton.vue";

const props = defineProps({
  row: { type: Object, default: null },
  service: { type: String, default: "" },
});
const emit = defineEmits(["close"]);

const serviceLabel = computed(() => (props.service || "").toUpperCase());

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
const eventTypeLabel = computed(() => getEventTypeLabel(props.row?.eventType));

const formattedPayload = computed(() => {
  if (!props.row?.payload) return "";
  try {
    const parsed = JSON.parse(props.row.payload);
    return JSON.stringify(parsed, null, 2);
  } catch (e) {
    return props.row.payload;
  }
});

function fmt(value) {
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

const show = ref(false);
// function closeModal () { show.value = false }
function notifyParent() {
  emit("close");
}
onMounted(async () => {
  await nextTick();
  show.value = true;
});
</script>

<style scoped>
.modal-header {
  display: flex;
  justify-content: space-between;
  font-size: 1.1rem;
}
/* 데스크탑(>=992px)에서 모달 최대 너비 600px로 제한 */
@media (min-width: 992px) {
  .detail-modal-dialog {
    --bs-modal-width: 600px;
  }
}
</style>
