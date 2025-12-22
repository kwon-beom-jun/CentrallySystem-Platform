<template>
  <!-- BootstrapVue3 b-modal -->
  <b-modal
    ref="perfEditModal"
    v-model="innerVisible"
    :title="$t('hrm.performanceDetail.updateSuccess').replace('완료','') || $t('hrm.performanceDetail.edit')"
    hide-footer
    :keyboard="false"
    :backdrop="'static'"
    centered
    fade
    style="--bs-modal-width:450px"
    @hide="emits('close')"
  >
    <form @submit.prevent="submitEdit">
      <!-- ① 날짜 -->
      <DefaultFormRow gap="8px" marginBottom="6px">
        <div class="col-wrapper">
          <DefaultLabel for="editFromDate" :text="$t('hrm.performanceDetail.startDate')" size="small" :required="true" />
          <DefaultTextfield
            type="date"
            id="editFromDate"
            v-model="editData.fromDate"
            size="full"
            style="width: 100%"
          />
        </div>
        <div class="col-wrapper">
          <DefaultLabel for="editToDate" :text="$t('hrm.performanceDetail.endDate')" size="small" :required="true" />
          <DefaultTextfield
            type="date"
            id="editToDate"
            v-model="editData.toDate"
            size="full"
            style="width: 100%"
          />
        </div>
      </DefaultFormRow>

      <!-- ② 유형 / 상태 -->
      <DefaultFormRow gap="8px" marginBottom="6px">
        <div class="col-wrapper">
          <DefaultLabel for="editWorkType" :text="$t('hrm.performanceDetail.type')" size="small" :required="true" />
          <DefaultSelect
            id="editWorkType"
            v-model="editData.workType"
            :options="workTypeOptions"
            size="full"
            style="width: 100%"
          />
        </div>
        <div class="col-wrapper">
          <DefaultLabel for="editStatus" :text="$t('hrm.performanceDetail.status')" size="small" :required="true" />
          <DefaultSelect
            id="editStatus"
            v-model="editData.status"
            :options="statusOptions"
            size="full"
            style="width: 100%"
          />
        </div>
      </DefaultFormRow>

      <!-- ③ 성과 주제 -->
      <DefaultFormRow marginBottom="6px">
        <div class="col-wrapper">
          <DefaultLabel for="editPerformanceTitle" :text="$t('hrm.performanceDetail.performanceTitle')" size="small" :required="true" />
          <DefaultTextfield
            type="text"
            id="editPerformanceTitle"
            v-model="editData.performanceTitle"
            size="full"
            :placeholder="$t('common.placeholder.input')"
          />
        </div>
      </DefaultFormRow>

      <!-- ④ 성과 내용 -->
      <DefaultFormRow marginBottom="10px">
        <div class="col-wrapper">
          <DefaultLabel for="editPerformance" :text="$t('hrm.performanceDetail.performance')" size="small" />
          <DefaultTextarea
            id="editPerformance"
            v-model="editData.performance"
            size="full"
            :placeholder="$t('common.placeholder.input')"
            :rows="4"
          />
        </div>
      </DefaultFormRow>

      <!-- ─── 푸터 버튼 ─── -->
      <DefaultFormRow align="right">
        <DefaultButton size="small" color="gray" marginRight="5px" @click="closeModal">
          {{ $t('common.button.cancel') }}
        </DefaultButton>
        <DefaultButton size="small" color="red" marginRight="5px" @click="showDeleteAlert">
          {{ $t('common.button.delete') }}
        </DefaultButton>
        <DefaultButton size="small" @click="submitEdit">{{ $t('common.button.edit') }}</DefaultButton>
      </DefaultFormRow>
    </form>
  </b-modal>

  <!-- 삭제 확인 AlertModal -->
  <AlertModal
    :isVisible="deleteConfirmVisible"
    :disableBackgroundClose="true"
    :title="$t('common.message.confirmDelete')"
    :confirmText="$t('common.button.confirm')"
    :cancelText="$t('common.button.cancel')"
    @close="deleteConfirmVisible = false"
    @confirm="deleteNotice"
  >
    <template #body>
      {{ $t('common.message.confirmDelete') }}
    </template>
  </AlertModal>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue';
import { useI18n } from 'vue-i18n';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultTextarea from '@/components/common/textarea/DefaultTextarea.vue';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
 import DefaultFormRow   from '@/components/common/DefaultFormRow.vue';
import { cloneDeep } from 'lodash';
import { toast } from 'vue3-toastify';

const props = defineProps({
  visible: { type: Boolean, default: false },
  editDataProp: {
    type: Object,
    default: () => ({
      fromDate: '',
      toDate: '',
      workType: '프로젝트',
      performanceTitle: '',
      performance: '',
      status: '',
    }),
  },
});
const emits = defineEmits(['close', 'save', 'delete']);
const innerVisible  = ref(false);          // b-modal v-model

/**
 * 로컬 데이터
 */
const editData = ref({ ...props.editDataProp });


/* props → 내부 동기화 */
watch(
  () => props.visible,
  (v) => {
  // 기존: 열기/닫기 직접 control
  innerVisible.value = v;
    if (v) editData.value = cloneDeep(props.editDataProp);
  },
  { immediate: true }
);

/* b-modal 닫힘 → 부모 알림 */
watch(innerVisible, (v) => {
  if (!v) emits('close');
});

/** 옵션 목록: DefaultSelect에 넘길 데이터 */
const workTypeOptions = [
  { value: '프로젝트', label: '프로젝트' },
  { value: '회의', label: '회의' },
  { value: '내부과제', label: '내부과제' },
  { value: '보고서 작성', label: '보고서 작성' },
];

const { t } = useI18n();
const statusOptions = [
  { value: '1', label: t('hrm.performanceDetail.statusInProgress') },
  { value: '2', label: t('hrm.performanceDetail.statusComplete') },
  { value: '3', label: t('hrm.performanceDetail.statusPending') },
];

/** 모달 닫기 */
function closeModal() {
  innerVisible.value = false;
}

/** 수정 */
function submitEdit() {
  if (!editData.value.fromDate || !editData.value.toDate ||
      !editData.value.workType || !editData.value.performanceTitle) {
    toast.warning(t('common.validation.required'));
    return;
  }
  emits('save', cloneDeep(editData.value));
  innerVisible.value = false;
}

const deleteConfirmVisible = ref(false);

/** 삭제 버튼 클릭 => AlertModal 열기 */
function showDeleteAlert() {
  deleteConfirmVisible.value = true;
}

/** AlertModal에서 확인 시 => 부모에 삭제 이벤트 알림 */
function deleteNotice() {
  // 현재 항목 ID를 부모에 전달
  deleteConfirmVisible.value = false;
  // (부모에서 AlertModal 등으로 재확인할 수도 있음)
  emits('delete', { ...editData.value });
}
</script>

<style scoped>
.col-wrapper {
  width: 100%;
}
</style>
