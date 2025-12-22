<template>
  <DefaultButton v-bind="$attrs" class="mm-btn">
    <slot />
  </DefaultButton>
 </template>

<script setup>
/**
 * 모바일 민트 톤 기본 버튼
 */
import DefaultButton from '@/components/common/button/DefaultButton.vue';
</script>

<style scoped>
.mm-btn :deep(button) {
  border-radius: 999px !important;
  border: 1px solid #e6f2ff !important;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.12) !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}
</style>


