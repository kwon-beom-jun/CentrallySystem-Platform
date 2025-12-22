<template>
  <DefaultSmallButton v-bind="$attrs" class="mm-sbtn">
    <slot />
  </DefaultSmallButton>
</template>

<script setup>
/**
 * 모바일 민트 톤 스몰 버튼
 */
import DefaultSmallButton from '@/components/common/button/DefaultSmallButton.vue';
</script>

<style scoped>
.mm-sbtn :deep(button) {
  border-radius: 999px !important;
  border: 1px solid #e6f2ff !important;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.12) !important;
  padding: 4px 10px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}
</style>


