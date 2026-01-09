<template>
  <div class="card-list-container">
    <div
      v-for="(item, itemIndex) in data"
      :key="itemIndex"
      class="card-item"
      @click="itemClick(item)" 
    >
      <div
        v-for="column in columns"
        :key="column.key"
        :class="['card-field', ...getFieldClasses(column, item)]"
        :style="getFieldStyles(column)"
      >
        <span v-if="column.prefix" class="prefix">{{ column.prefix }}</span>
        {{ getFieldValue(item, column) }}
        <span v-if="column.suffix" class="suffix">{{ column.suffix }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

const props = defineProps({
  data: { type: Array, required: true },
  columns: { type: Array, required: true },
  /**
   * ▼▼▼ 부모 컴포넌트로부터 클릭 이벤트를 처리할 함수를 받습니다. ▼▼▼
   */
  itemClick: { type: Function, default: () => {} },
});

const getFieldValue = (item, column) => {
  if (column.customValue && typeof column.customValue === 'function') {
    return column.customValue(item);
  }
  return item[column.key];
};

const getFieldClasses = (column, item) => {
  const classes = [];
  if (column.inline) classes.push('inline');
  if (column.align) classes.push(`align-${column.align}`);
  if (column.customClass) {
    const custom = typeof column.customClass === 'function' ? column.customClass(item) : column.customClass;
    if (custom) classes.push(...custom.split(' '));
  }
  return classes;
};

const getFieldStyles = (column) => ({
  'order': column.line || 99,
  'font-weight': column.bold ? '700' : 'normal',
});
</script>

<style scoped>
.card-item {
  cursor: pointer;
  box-sizing: border-box;
  display: flex;
  flex-wrap: wrap;
  padding: 16px;
  margin-bottom: 12px;
  border-radius: 8px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.card-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.card-field {
  box-sizing: border-box;
  width: 100%;
  padding: 4px 0;
  line-height: 1.5;
  font-size: 0.9rem;
  color: #333;
}
.card-field.inline {
  width: auto;
  flex-grow: 0;
  margin-right: 0.75em;
}
.card-field.inline:last-of-type {
  margin-right: 0;
}
.prefix, .suffix {
  display: inline;
  white-space: pre;
}
.align-left { text-align: left; }
.align-center { text-align: center; }
.align-right { text-align: right; margin-left: auto; }
.text-meta {
  color: #666;
  font-size: 0.8rem;
}
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>