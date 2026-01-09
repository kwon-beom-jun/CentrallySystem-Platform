<!--
  사용법:
  <DefaultFormRow 
    align="center" 
    customClass="추가클래스"
    marginLeft="10px"
    marginRight="10px"
    marginTop="5px"
    marginBottom="5px"
  >
  </DefaultFormRow>

  - align: 'left'(기본), 'center', 'right' 중 선택
  - customClass: 추가로 적용할 CSS 클래스
  - marginLeft, marginRight, marginTop, marginBottom: 각 방향의 마진 값 (예: "10px", "1rem" 등)
-->
<template>
  <div :class="rowClass" :style="marginStyle">
    <slot></slot>
  </div>
</template>

<script setup>
import { computed, defineProps } from 'vue';

const props = defineProps({
  // 정렬 옵션: 'left'(기본), 'center', 'right'
  align: {
    type: String,
    default: 'left',
    validator: value => ['left', 'center', 'right', 'between', 'betweenEqual'].includes(value)
  },
  // 추가적으로 적용할 클래스
  customClass: {
    type: String,
    default: ''
  },
  // 마진 관련 프롭스
  marginLeft: {
    type: String,
    default: '0'
  },
  marginRight: {
    type: String,
    default: '0'
  },
  marginTop: {
    type: String,
    default: '0'
  },
  marginBottom: {
    type: String,
    default: '0'
  },
  gap: {
    type: String,
    default: '0'
  },
  growFirst: {
    type: Boolean,
    default: false
  }
});

// align prop에 따라 정렬 클래스를 결정합니다.
const rowClass = computed(() => {
  let alignmentClass = '';
  switch (props.align) {
    case 'center':
      alignmentClass = 'justify-content-center';
      break;
    case 'right':
      alignmentClass = 'justify-content-end';
      break;
    case 'between':
      alignmentClass = 'justify-content-between';
      break;
    case 'betweenEqual':
      alignmentClass = 'justify-content-between equal-distribute'
      break;
    case 'left':
    default:
      alignmentClass = 'justify-content-start';
  }
  const growCls = props.growFirst ? 'grow-first' : '';
  // 기본 클래스: d-flex, align-items-center 등을 포함합니다.
  return [
    'default-form-row',
    'd-flex',
    'align-items-center',
    alignmentClass,
    growCls,
    props.customClass
  ].join(' ').trim();
});

// margin 프롭스를 이용한 스타일 객체
const marginStyle = computed(() => {
  return {
    marginLeft: `${props.marginLeft} !important`,
    marginRight: `${props.marginRight} !important`,
    marginTop: `${props.marginTop} !important`,
    marginBottom: `${props.marginBottom} !important`,
    gap : `${props.gap} !important`,
  };
});
</script>

<style scoped>
/* 첫 번째만 늘리고 나머지는 고정 */
.default-form-row.grow-first > *:first-child {
  flex: 1 1 0;
  min-width: 0;
}
/* betweenEqual 전용 : 자식들을 1:1(또는 N:N)로 확장 */
.default-form-row.equal-distribute > * {
  flex: 1 1 0;
  min-width: 0;
}
.default-form-row {
  width: 100%;
}
</style>
