import { ref } from 'vue';
import DefaultSelect from './DefaultSelect.vue';

export default {
  title: 'Common/Select/Default',
  component: DefaultSelect,
  args: {
    modelValue: '',
    options: [
      { value: '', label: '선택하세요' },
      { value: 'A', label: '옵션 A' },
      { value: 'B', label: '옵션 B' }
    ],
    placeholder: '옵션을 선택하세요',
    size: 'medium',
    disabled: false,
    customHeight: ''
  },
  argTypes: {
    modelValue: { control: 'text' },
    options: { control: 'object' },
    placeholder: { control: 'text' },
    size: { control: { type: 'select', options: [
      'xsmall','small','medium','large',
      'full','full-xsmall','full-small','full-medium','full-large'
    ]}},
    disabled: { control: 'boolean' },
    change: { action: 'change' }
  }
};

/* 기본 스토리 */
export const 기본 = (args) => ({
  components: { DefaultSelect },
  setup() {
    const value = ref(args.modelValue);
    return { args, value };
  },
  template: `
    <DefaultSelect
      v-bind="args"
      v-model="value"
      @change="$emit('change', $event)"
    />
    <p style="margin-top:8px">선택 값: {{ value }}</p>
  `
});
