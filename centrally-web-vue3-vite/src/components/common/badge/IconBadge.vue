<template>
  <span :class="['badge', colorClass, { 'badge--stacked': stacked }]">
    <i v-if="icon && !stacked" :class="[icon]"></i>
    <span v-if="stacked" class="badge__stack">
      <span class="badge__title"><slot /></span>
      <span v-if="subtitle" class="badge__subtitle">{{ subtitle }}</span>
    </span>
    <template v-else>
      <slot />
    </template>
  </span>
</template>

<script setup>
import { computed } from 'vue';
const props = defineProps({
  color: { type: String, default: 'primary' }, // primary | success | danger | warning | info | secondary | dark
  icon: { type: String, default: '' }, // e.g. 'ri-time-line'
  stacked: { type: Boolean, default: false }, // 모바일 2줄 스택 배지 여부
  subtitle: { type: String, default: '' }, // 스택 배지 하단 서브텍스트(이메일 등)
});

const colorClass = computed(() => {
  switch (props.color) {
    case 'success':
      return 'bg-success';
    case 'danger':
      return 'bg-danger';
    case 'warning':
      return 'bg-warning';
    case 'info':
      return 'bg-info';
    case 'secondary':
      return 'bg-secondary';
    case 'dark':
      return 'bg-dark';
    default:
      return 'bg-primary';
  }
});
</script>

<style scoped>
/* 배지 컨텐츠 중앙 정렬 및 라운드 형태 고정 */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  vertical-align: middle;
  line-height: 1;
  padding: 4px 12px 4px 10px;
  border-radius: 999px;
  font-weight: 700;
  border: 1px solid transparent;
}
.badge i {
  line-height: 1;
  display: inline-block;
}
.badge--stacked {
  align-items: flex-start;
  padding: 6px 10px;
}
.badge__stack {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}
.badge__title {
  font-weight: 800;
}
.badge__subtitle {
  font-size: 0.72rem;
  opacity: 0.9;
}

/* ── 연한 배경 + 외곽선: 모바일 칩 스타일과 톤 맞춤 ── */
.badge.bg-primary {
  background: rgba(11, 107, 203, 0.80) !important;
  color: #ffffff !important;
  border-color: rgba(11, 107, 203, 0.5) !important;
}
.badge.bg-success {
  background: rgba(10, 122, 85, 0.80) !important;
  color: #ffffff !important;
  border-color: rgba(10, 122, 85, 0.5) !important;
}
.badge.bg-danger {
  background: rgba(198, 40, 40, 0.80) !important;
  color: #ffffff !important;
  border-color: rgba(198, 40, 40, 0.5) !important;
}
.badge.bg-secondary {
  background: rgba(51, 65, 85, 0.80) !important;
  color: #ffffff !important;
  border-color: rgba(51, 65, 85, 0.5) !important;
}
.badge.bg-dark {
  background: rgba(17, 24, 39, 0.80) !important;
  color: #ffffff !important;
  border-color: rgba(17, 24, 39, 0.5) !important;
}
.badge.bg-warning {
  background: rgba(180, 83, 9, 0.80) !important;
  color: #ffffff !important;
  border-color: rgba(180, 83, 9, 0.5) !important;
}
.badge.bg-info {
  background: rgba(3, 105, 161, 0.80) !important;
  color: #ffffff !important;
  border-color: rgba(3, 105, 161, 0.5) !important;
}
</style>
