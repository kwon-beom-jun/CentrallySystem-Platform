<template>
  <div class="mm-input-wrapper">
    <DefaultTextfield v-bind="$attrs" />
  </div>
</template>

<script setup>
/**
 * 모바일 민트 톤 텍스트필드
 * DefaultTextfield를 감싸 스타일만 보강합니다.
 */
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
</script>

<style scoped>
.mm-input-wrapper :deep(input),
.mm-input-wrapper :deep(select),
.mm-input-wrapper :deep(textarea) {
  /* background: #f9fcff !important; */
  border: 1px solid #e6f2ff !important;
  border-radius: 10px !important;
}
.mm-input-wrapper :deep(input[disabled]),
.mm-input-wrapper :deep(select[disabled]),
.mm-input-wrapper :deep(textarea[disabled]) {
  border: 1px dashed #cfe6ff !important;
  color: #0b162f !important;
  font-weight: 600 !important;
}
</style>
