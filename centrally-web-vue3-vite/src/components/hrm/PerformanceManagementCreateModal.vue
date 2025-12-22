<template>
  <!-- BootstrapVue3 b-modal -->
  <b-modal
    ref="perfModal"
    v-model="innerVisible"
    :title="$t('hrm.performanceDetail.add')"
    hide-footer
    :keyboard="false"
    :backdrop="'static'"
    centered
    fade
    style="--bs-modal-width:450px"
    @hide="emits('close')"
  >
    <!-- ───── 바디 ───── -->
    <form @submit.prevent="submitForm">
      <DefaultFormRow gap="10px" marginBottom="5px">
        <!-- 시작일 / 종료일 -->
          <div class="col-wrapper">
            <DefaultLabel for="fromDate" :text="$t('hrm.performanceDetail.startDate')" size="small" :required="true" />
            <DefaultTextfield
              type="date"
              id="fromDate"
              v-model="localFormData.fromDate"
              size="full"
              style="width: 100%"
            />
          </div>
          <div class="col-wrapper">
            <DefaultLabel for="toDate" :text="$t('hrm.performanceDetail.endDate')" size="small" :required="true" />
            <DefaultTextfield
              type="date"
              id="toDate"
              v-model="localFormData.toDate"
              size="full"
              style="width: 100%"
            />
          </div>
      </DefaultFormRow>

      <!-- ② 유형 / 상태 -->
      <DefaultFormRow gap="10px" marginBottom="5px">
        <div class="col-wrapper">
          <DefaultLabel for="workType" :text="$t('hrm.performanceDetail.type')" size="small" :required="true" />
          <DefaultSelect
            id="workType"
            v-model="localFormData.workType"
            :options="workTypeOptions"
            size="full"
            style="width: 100%"
          />
        </div>
        <div class="col-wrapper">
          <DefaultLabel for="status" :text="$t('hrm.performanceDetail.status')" size="small" :required="true" />
          <DefaultSelect
            id="status"
            v-model="localFormData.status"
            :options="statusOptions"
            size="full"
            style="width: 100%"
          />
        </div>
      </DefaultFormRow>

      <!-- ③ 성과 주제 -->
      <DefaultFormRow marginBottom="5px">
        <div class="col-wrapper">
          <DefaultLabel
            for="performanceTitle"
            :text="$t('hrm.performanceDetail.performanceTitle')"
            size="small"
            :required="true"
          />
          <DefaultTextfield
            type="text"
            id="performanceTitle"
            v-model="localFormData.performanceTitle"
            size="full"
            :placeholder="$t('common.placeholder.input')"
          />
        </div>
      </DefaultFormRow>

      <!-- ④ 성과 내용 -->
      <DefaultFormRow marginBottom="10px">
        <div class="col-wrapper">
          <DefaultLabel for="performance" :text="$t('hrm.performanceDetail.performance')" size="small" />
          <DefaultTextarea
            id="performance"
            v-model="localFormData.performance"
            size="full"
            :placeholder="$t('common.placeholder.input')"
            :rows="4"
          />
        </div>
      </DefaultFormRow>

      <!-- ───── 푸터 버튼 ───── -->
      <DefaultFormRow align="right">
        <DefaultButton color="gray" marginRight="5px" size="small" @click="closeModal">
          {{ $t('common.button.cancel') }}
        </DefaultButton>
        <DefaultButton size="small" @click="submitForm">{{ $t('common.button.save') }}</DefaultButton>
      </DefaultFormRow>
    </form>
  </b-modal>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue';
import { useI18n } from 'vue-i18n';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultTextarea from '@/components/common/textarea/DefaultTextarea.vue';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import DefaultFormRow   from '@/components/common/DefaultFormRow.vue';
import { cloneDeep } from 'lodash';
import { toast } from 'vue3-toastify';

const props = defineProps({
  visible: { type: Boolean, default: false },
  formData: {
    type: Object,
    default: () => ({
      fromDate: '',
      toDate: '',
      workType: '',
      performanceTitle: '',
      performance: '',
      status: '',
    }),
  },
});
const emits = defineEmits(['close', 'submit']);
const { t } = useI18n();

/* ---------- b-modal 제어 변수 ---------- */
const innerVisible = ref(false);   // b-modal v-model 전용

/**
 * 모달 내부에서 쓸 지역 데이터
 * => 부모로부터 받은 formData를 로컬 복사
 */
const localFormData = ref({ ...props.formData });

/**
 * props.formData가 바뀔 때마다 localFormData를 다시 세팅
 */
watch(
  () => props.visible,
  (v) => {
    innerVisible.value = v;
    if (v) localFormData.value = cloneDeep(props.formData);   // 열릴 때 폼 초기화
  },
  { immediate: true }
);

// 자식 -> 부모  (b-modal 닫힘 → 부모에게 알려주기)
watch(innerVisible, (v) => {
  if (!v) emits('close');
});

/** DefaultSelect에 넣을 옵션들 */
const workTypeOptions = [
  { value: '프로젝트', label: t('hrm.performanceDetail.type') + ' 1' },
  { value: '회의', label: t('hrm.performanceDetail.type') + ' 2' },
  { value: '내부과제', label: t('hrm.performanceDetail.type') + ' 3' },
  { value: '보고서 작성', label: t('hrm.performanceDetail.type') + ' 4' },
];

const statusOptions = [
  { value: '1', label: t('hrm.performanceDetail.statusInProgress') },
  { value: '2', label: t('hrm.performanceDetail.statusComplete') },
  { value: '3', label: t('hrm.performanceDetail.statusPending') },
];

/* ---------- 모달 닫기 ---------- */
function closeModal() {
  innerVisible.value = false;      // b-modal 닫고
  // emits('close') 는 watch(innerVisible) 로 전달됨
}

/* ---------- 저장 ---------- */
function submitForm() {
  const f = localFormData.value;
  if (!f.fromDate || !f.toDate || !f.workType || !f.performanceTitle) {
    toast.warning(t('common.validation.required'));
    return;
  }
  emits('submit', cloneDeep(f));
  innerVisible.value = false;
}
</script>

<style scoped>
.col-wrapper {
  width: 100%;
}
</style>
