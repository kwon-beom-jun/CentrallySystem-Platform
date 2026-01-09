<template>
  <div class="mm-label-wrapper">
    <DefaultLabel v-bind="forwarded" />
  </div>
</template>

<script setup>
/**
 * 모바일 민트 톤 라벨 (UserInformation 모바일 톤 기반)
 * DefaultLabel을 감싸 스타일만 보강합니다.
 */
import { computed, useAttrs } from 'vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
defineOptions({ inheritAttrs: false });

const props = defineProps({
  text: { type: String, default: '' },
  forId: { type: String, default: '' },
  alignment: { type: String, default: 'left' },
  size: { type: String, default: 'small' },
  marginBottom: { type: String, default: '' },
  marginTop: { type: String, default: '' },
  customHeight: { type: String, default: '' },
});

const attrs = useAttrs();
const forwarded = computed(() => ({
  ...attrs,
  text: props.text,
  forId: props.forId,
  alignment: props.alignment,
  size: props.size,
  marginBottom: props.marginBottom,
  marginTop: props.marginTop,
  customHeight: props.customHeight,
}));
</script>

<style scoped>
.mm-label-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* 왼쪽 포인트 바 */
.mm-label-wrapper::before {
  /* content: '';
  display: inline-block;
  width: 6px;
  height: 10px;
  border-radius: 999px;
  background: #0b8cff;
  flex-shrink: 0; */
}

/* DefaultLabel 내부 스타일 덮어쓰기 */
.mm-label-wrapper :deep(label) {
  display: flex;
  align-items: center;
  gap: 3px;
  color: #334155;
  font-weight: 800;
  font-size: 0.75rem;
}

/* 작은 사이즈에 맞게 포인트 바 살짝 축소 */
@media (max-width: 650px) {
  .mm-label-wrapper :deep(label) {
    font-size: 0.76rem !important;
  }
  .mm-label-wrapper::before {
    width: 5px;
    height: 9px;
  }
}
</style>
