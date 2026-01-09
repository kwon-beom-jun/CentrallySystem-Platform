import { ref } from 'vue';
import DefaultTextarea from './DefaultTextarea.vue';

export default {
  title: 'Common/Textarea/DefaultTextarea',
  component: DefaultTextarea,
  args: {
    modelValue: '',
    placeholder: '내용을 입력하세요',
    size: 'medium',
    rows: 4,
    required: false,
    disabled: false,
    reserveErrorSpace: true,
    validationType: '',
    errorMessage: '입력 형식이 올바르지 않습니다.'
  },
  argTypes: {
    modelValue:      { control: 'text' },
    placeholder:     { control: 'text' },
    size:            { control: { type: 'select', options: ['xsmall','small','medium','large','full'] }},
    rows:            { control: { type: 'number', min: 1 }},
    required:        { control: 'boolean' },
    disabled:        { control: 'boolean' },
    reserveErrorSpace:{ control: 'boolean' },
    validationType:  { control: { type: 'select', options: ['', 'english', 'number', 'korean', 'email'] }},
    change:          { action: 'change' },
    'update:modelValue': { action: 'update:modelValue' }
  }
};

/* 기본 */
export const 기본 = (args) => ({
  components: { DefaultTextarea },
  setup() { const v = ref(args.modelValue); return { args, v }; },
  template: `
    <DefaultTextarea v-bind="args" v-model="v" @change="$emit('change',$event)" />
    <p style="margin-top:8px">현재 값: {{ v }}</p>
  `
});

/* 검증 */
export const 검증 = () => ({
  components: { DefaultTextarea },
  setup() { const v = ref(''); return { v }; },
  template: `
    <DefaultTextarea v-model="v"
      placeholder="숫자만 입력"
      validationType="number"
      errorMessage="숫자만 입력 가능합니다."
      reserveErrorSpace
    />
  `
});

/* 사이즈 비교 */
export const 사이즈 = () => ({
  components: { DefaultTextarea },
  template: `
    <div style="display:flex;flex-direction:column;gap:10px">
      <DefaultTextarea size="xsmall" placeholder="xsmall"/>
      <DefaultTextarea size="small"  placeholder="small"/>
      <DefaultTextarea size="medium" placeholder="medium"/>
      <DefaultTextarea size="large"  placeholder="large"/>
      <DefaultTextarea size="full"   placeholder="full"/>
    </div>
  `
});
