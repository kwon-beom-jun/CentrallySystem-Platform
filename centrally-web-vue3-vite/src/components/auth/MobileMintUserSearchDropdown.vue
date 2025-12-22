<template>
  <div class="mm-search-wrapper">
    <UserSearchDropdown v-bind="$attrs" />
  </div>
</template>

<script setup>
/**
 * 모바일 민트 톤 사용자 검색 드롭다운
 */
import UserSearchDropdown from '@/components/auth/UserSearchDropdown.vue';
</script>

<style scoped>
.mm-search-wrapper :deep(input) {
  /* background: #f9fcff !important; */
  border: 1px solid #e6f2ff !important;
  border-radius: 10px !important;
}
.mm-search-wrapper :deep(.vs__dropdown-toggle) {
  background: #f9fcff !important;
  border: 1px solid #e6f2ff !important;
  border-radius: 10px !important;
}
</style>


