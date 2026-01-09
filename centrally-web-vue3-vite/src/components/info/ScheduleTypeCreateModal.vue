<!-- src/components/info/ScheduleTypeCreateModal.vue -->
<template>
  <b-modal
    ref="scheduleTypeModal"
    style="--bs-modal-width: 400px"
    v-model="innerVisible"
    :title="modalTitle"
    :keyboard="false"
    :backdrop="'static'"
    centered
    hide-footer
  >
    <form @submit.prevent="saveScheduleType">
      <!-- 코드 -->
      <DefaultLabel
        :text="$t('info.scheduleType.code')"
        forId="code"
        size="small"
        required
      />
      <DefaultFormRow align="between" marginBottom="15px">
        <DefaultTextfield
          id="code"
          v-model="localForm.code"
          :placeholder="$t('info.scheduleType.code')"
          style="width: 100%"
          size="full"
        />
      </DefaultFormRow>

      <!-- 라벨 -->
      <DefaultLabel
        :text="$t('info.scheduleType.label')"
        forId="label"
        size="small"
        required
      />
      <DefaultFormRow align="between" marginBottom="15px">
        <DefaultTextfield
          id="label"
          v-model="localForm.label"
          :placeholder="$t('info.scheduleType.label')"
          style="width: 100%"
          size="full"
        />
      </DefaultFormRow>

      <!-- 색상 -->
      <DefaultLabel
        :text="$t('info.scheduleType.color')"
        forId="color"
        size="small"
        required
      />
      <DefaultFormRow align="between" marginBottom="15px">
        <DefaultTextfield
          id="color"
          type="color"
          v-model="localForm.color"
          style="width: 80px; height: 35px;"
          size="full"
        />
        <span style="font-size: 0.8rem; color: #666;">{{ localForm.color }}</span>
      </DefaultFormRow>

      <!-- 표시 순서 -->
      <DefaultLabel
        :text="$t('info.scheduleType.displayOrder')"
        forId="displayOrder"
        size="small"
        required
      />
      <DefaultTextfield
        id="displayOrder"
        type="number"
        v-model="localForm.displayOrder"
        :placeholder="$t('info.scheduleType.displayOrder')"
        style="width: 100%"
        size="full"
        validationType="number"
        :min="(!isCreate && localForm.isActive === false) ? -1 : 1"
        :max="999"
        :disabled="!isCreate && localForm.isActive === false"
        reserveErrorSpace
      />

      <!-- 활성 여부 (수정 모달에서만 표시) -->
      <DefaultFormRow v-if="!isCreate" align="between" marginTop="15px" marginBottom="15px">
        <DefaultLabel
          :text="$t('info.scheduleType.isActive')"
          forId="isActive"
          size="small"
        />
        <input
          id="isActive"
          type="checkbox"
          v-model="localForm.isActive"
          style="width: 20px; height: 20px;"
        />
      </DefaultFormRow>

      <!-- 버튼 -->
      <DefaultFormRow align="right" marginTop="20px">
        <DefaultButton
          color="gray"
          size="small"
          marginRight="5px"
          @click="closeModal"
        >
          {{ $t('common.button.cancel') }}
        </DefaultButton>
        <DefaultButton size="small" @click="saveScheduleType">
          {{ isCreate ? $t('common.button.register') : $t('common.button.modify') }}
        </DefaultButton>
      </DefaultFormRow>
    </form>
  </b-modal>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { cloneDeep } from 'lodash';
import { toast } from 'vue3-toastify';

import DefaultLabel     from '@/components/common/label/DefaultLabel.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultButton    from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow   from '@/components/common/DefaultFormRow.vue';

import ScheduleTypeApi from '@/api/info/ScheduleTypeApi';

/* ─── Props / Emits ─── */
const props = defineProps({
  isVisible: {
    type   : Boolean,
    default: false
  },
  isCreate : { type: Boolean, default: true },
  form     : { 
    type: Object,  
    default: () => ({ 
      scheduleTypeId: null, 
      code: '', 
      label: '', 
      color: '#3b82f6', 
      displayOrder: 0,
      isActive: true 
    }) 
  }
});
const emit = defineEmits(['update:isVisible', 'save']);
const { t } = useI18n();

/* ─── 로컬 상태 ─── */
const innerVisible = ref(false);
const localForm    = ref(cloneDeep(props.form));

/* 모달 제목 */
const modalTitle = computed(() => (props.isCreate ? t('info.scheduleType.typeRegister') : t('info.scheduleType.typeModify')));

/* 모달 닫기 */
function closeModal () {
  innerVisible.value = false
  emit('update:isVisible', false)
}

/* ─── 저장(등록·수정) ─── */
async function saveScheduleType () {
  /* ① 검증 */
  if (!localForm.value.code.trim()) {
    toast.warning(t('info.scheduleType.code') + ' ' + t('common.message.required'));
    return;
  }
  if (!localForm.value.label.trim()) {
    toast.warning(t('info.scheduleType.label') + ' ' + t('common.message.required'));
    return;
  }
  if (!localForm.value.color) {
    toast.warning(t('info.scheduleType.color') + ' ' + t('common.message.required'));
    return;
  }
  
  const displayOrder = Number(localForm.value.displayOrder);
  const isInactive = localForm.value.isActive === false;
  
  // 비활성 상태일 때는 -1 허용, 활성 상태일 때는 1~999만 허용
  if (isInactive) {
    if (displayOrder !== -1 && (displayOrder < 1 || displayOrder > 999)) {
      toast.warning(t('info.scheduleType.displayOrderRange'));
      return;
    }
  } else {
    if (displayOrder < 1 || displayOrder > 999) {
      toast.warning(t('info.scheduleType.displayOrderRange'));
      return;
    }
  }

  /* ② 서버 호출용 Payload */
  const payload = {
    code:         localForm.value.code.trim(),
    label:        localForm.value.label.trim(),
    color:        localForm.value.color,
    displayOrder: displayOrder,
    isActive:     localForm.value.isActive !== undefined ? localForm.value.isActive : true
  };

  try {
    let res;
    if (props.isCreate) {
      res = await ScheduleTypeApi.createScheduleType(payload);
      toast.success(t('info.scheduleType.createSuccess'));
    } else {
      // 수정 시: isActive=false로 변경하려고 할 때 최소 1개 활성화 유지 체크
      // 현재 항목이 활성화되어 있고, 변경하려는 값이 false인 경우에만 체크
      const currentTypes = await ScheduleTypeApi.getScheduleTypes();
      const currentItem = currentTypes.data.find(st => st.scheduleTypeId === localForm.value.scheduleTypeId);
      const wasActive = currentItem?.isActive !== false;
      const changingToInactive = !payload.isActive;
      
      if (wasActive && changingToInactive) {
        const activeCount = currentTypes.data.filter(st => st.isActive && st.scheduleTypeId !== localForm.value.scheduleTypeId).length;
        if (activeCount < 1) {
          toast.warning(t('info.scheduleType.minActiveRequired'));
          return;
        }
      }
      
      res = await ScheduleTypeApi.updateScheduleType(localForm.value.scheduleTypeId, payload);
      toast.success(t('info.scheduleType.updateSuccess'));
    }

    /* ③ 부모에게 저장 완료 알림 */
    emit('save', res.data);
    closeModal();
  } catch (e) {
    if (e.response?.data?.message?.includes('최소 하나 이상')) {
      toast.error(t('info.scheduleType.minActiveRequired'));
    } else {
      toast.error(t('common.message.serverError'));
    }
  }
}

/* 표시 상태 동기화 */
watch(
  () => props.isVisible,
  v => {
    innerVisible.value = v
    if (v) localForm.value = cloneDeep(props.form)
  },
  { immediate: true }
)

watch(innerVisible, v => emit('update:isVisible', v))
</script>

<style scoped>
:deep(.form-control) {
  height: 28px;
  font-size: 0.8rem;
}
</style>

