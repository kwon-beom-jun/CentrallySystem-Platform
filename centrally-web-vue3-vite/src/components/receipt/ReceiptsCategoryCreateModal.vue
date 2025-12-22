<!-- src/components/receipt/ReceiptsCategoryCreateModal.vue -->
<template>
  <b-modal
    ref="categoryModal"
    style="--bs-modal-width: 350px"
    v-model="innerVisible"
    :title="modalTitle"
    :keyboard="false"
    :backdrop="'static'"
    centered
    hide-footer
  >
    <form @submit.prevent="saveCategory">
      <!-- 카테고리 이름 -->
        <DefaultLabel
          :text="$t('receipt.meta.category') + ' ' + $t('common.label.name')"
          forId="catName"
          size="small"
          required
        />
        <DefaultTextfield
          id="catName"
          v-model="localForm.category"
          :placeholder="$t('receipt.meta.category')"
          style="width: 100%"
          size="full"
          reserveErrorSpace
        />

      <!-- 최대 금액 -->
      <DefaultLabel
        :text="$t('receipt.meta.maxAmount')"
        forId="catLimit"
        size="small"
        required
      />
      <DefaultTextfield
        id="catLimit"
        type="number"
        v-model="localForm.maxAmount"
        :placeholder="$t('receipt.meta.maxAmount')"
        style="width: 100%"
        size="full"
        validationType="number"
        reserveErrorSpace
      />

      <!-- 버튼 -->
      <DefaultFormRow align="right">
        <DefaultButton
          color="gray"
          size="small"
          marginRight="5px"
          @click="closeModal"
        >
          {{ $t('common.button.cancel') }}
        </DefaultButton>
        <DefaultButton size="small" @click="saveCategory">
          {{ isCreate ? $t('common.button.register') : $t('common.button.modify') }}
        </DefaultButton>
      </DefaultFormRow>
    </form>
  </b-modal>
</template>

<script setup>
import { ref, watch, computed, defineProps, defineEmits } from 'vue';
import { useI18n } from 'vue-i18n';
import { cloneDeep } from 'lodash';
import { toast } from 'vue3-toastify';

import DefaultLabel     from '@/components/common/label/DefaultLabel.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultButton    from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow   from '@/components/common/DefaultFormRow.vue';

import ReceiptsCategoryApi from '@/api/receipt/ReceiptsCategoryApi';

/* ─── Props / Emits ─── */
const props = defineProps({
  isVisible: {
    type   : Boolean,
    default: false
  },
  isCreate : { type: Boolean, default: true },
  form     : { type: Object,  default: () => ({ id:null, category:'', maxAmount:0 }) }
});
const emit = defineEmits(['update:isVisible', 'save']);
const { t } = useI18n();

/* ─── 로컬 상태 ─── */
const innerVisible = ref(false);
const localForm    = ref(cloneDeep(props.form));

/* 모달 제목 */
const modalTitle = computed(() => (props.isCreate ? t('receipt.meta.categoryRegister') : t('receipt.meta.category') + ' ' + t('common.button.modify')));

/* 모달 닫기 */
function closeModal () {
  innerVisible.value = false
  emit('update:isVisible', false)
}

/* ─── 저장(등록·수정) ─── */
async function saveCategory () {
  /* ① 검증 */
  if (!localForm.value.category.trim()) {
    toast.warning(t('receipt.meta.category') + ' ' + t('common.message.required'));
    return;
  }
  if (!localForm.value.maxAmount || localForm.value.maxAmount <= 0) {
    toast.warning(t('receipt.meta.maxAmount') + ' ' + t('common.message.required'));
    return;
  }

  /* ② 서버 호출용 Payload */
  const payload = {
    categoryName: localForm.value.category.trim(),
    limitPrice  : Number(localForm.value.maxAmount)
  };

  try {
    let res;
    if (props.isCreate) {
      res = await ReceiptsCategoryApi.createCategory(payload);
      toast.success(t('receipt.meta.category') + ' ' + t('common.message.created'));
    } else {
      res = await ReceiptsCategoryApi.updateCategory(localForm.value.id, payload);
      toast.success(t('receipt.meta.category') + ' ' + t('common.message.updated'));
    }

    /* ③ 부모에게 저장 완료 알림 */
    emit('save', res.data);      // → 부모에서 목록 재조회
    closeModal();
  } catch (e) {
    toast.error(t('common.message.serverError'));
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
