<template>
  <div class="mm-textarea-wrapper">
    <DefaultTextarea v-bind="$attrs" />
  </div>
  
  <slot />
</template>

<script setup>
/**
 * 모바일 민트 톤 텍스트에어리어
 * DefaultTextarea를 감싸 스타일만 보강합니다.
 */
import DefaultTextarea from '@/components/common/textarea/DefaultTextarea.vue';
</script>

<style scoped>
.mm-textarea-wrapper :deep(textarea) {
  /* background: #f9fcff !important; */
  border: 1px solid #e6f2ff !important;
  border-radius: 10px !important;
}
.mm-textarea-wrapper :deep(textarea[disabled]) {
  background: #ffffff !important;
  border: 1px dashed #cfe6ff !important;
  color: #0b162f !important;
  font-weight: 600 !important;
}
</style>


