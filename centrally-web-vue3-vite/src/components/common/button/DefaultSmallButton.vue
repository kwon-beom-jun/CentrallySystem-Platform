<template>
  <button
    :type="type"
    class="btn external-toggle-btn"
    :class="variantClass"
    :style="buttonStyle"
    @click="emit('click', $event)"
  >
    <slot />
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: { type: String, default: 'button' },
  variant: { type: String, default: 'secondary' }, // 'primary' | 'secondary' | 'danger'
  marginBottom: { type: String, default: '' },
  marginTop: { type: String, default: '' },
  marginLeft: { type: String, default: '' },
  marginRight: { type: String, default: '' }
})

const emit = defineEmits(['click'])

const variantClass = computed(() => {
  switch (props.variant) {
    case 'primary': return 'btn-outline-primary'
    case 'danger' : return 'btn-outline-danger'
    default       : return 'btn-outline-secondary'
  }
})

/**
 * 버튼 스타일 계산
 */
const buttonStyle = computed(() => {
  const style = {}
  if (props.marginBottom) style.marginBottom = props.marginBottom
  if (props.marginTop) style.marginTop = props.marginTop
  if (props.marginLeft) style.marginLeft = props.marginLeft
  if (props.marginRight) style.marginRight = props.marginRight
  return style
})
</script>

<style scoped>
.external-toggle-btn {
  padding: 0px 3px;
  height: 20px;
  line-height: 18px;
  font-size: 0.65rem;
  font-weight: 500;
}
</style>


