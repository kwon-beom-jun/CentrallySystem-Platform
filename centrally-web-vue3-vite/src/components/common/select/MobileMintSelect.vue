<template>
  <div class="mm-select-wrapper">
    <DefaultSelect v-bind="$attrs" />
  </div>
</template>

<script setup>
/**
 * 모바일 민트 톤 셀렉트박스
 * DefaultSelect를 감싸 스타일만 보강합니다.
 */
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
</script>

<style scoped>
.mm-select-wrapper :deep(select),
.mm-select-wrapper :deep(.vue-select),
.mm-select-wrapper :deep(.vs__dropdown-toggle) {
  /* background: #f9fcff !important; */
  border: 1px solid #e6f2ff !important;
  border-radius: 10px !important;
}
.mm-select-wrapper :deep(select[disabled]),
.mm-select-wrapper :deep(.vs--disabled .vs__dropdown-toggle) {
  background: #ffffff !important;
  border: 1px dashed #cfe6ff !important;
  color: #0b162f !important;
}
</style>


