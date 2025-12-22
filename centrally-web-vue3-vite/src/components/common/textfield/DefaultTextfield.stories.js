import { ref } from 'vue';
import DefaultTextfield from './DefaultTextfield.vue';

export default {
  title: 'Common/Textfield/DefaultTextfield',
  component: DefaultTextfield,
  args: {
    modelValue: '',
    placeholder: '입력하세요',
    type: 'text',
    size: 'medium',
    required: false,
    disabled: false,
    validationType: '',
    reserveErrorSpace: true,
    externalError: '',
    externalInvalid: false,
    bgColor: ''
  },
  argTypes: {
    modelValue: { control: 'text' },
    placeholder:{ control: 'text' },
    type:       { control: { type: 'select', options: ['text','number','date','month','radio'] }},
    size:       { control: { type: 'select', options: ['xsmall','small','medium','large','full'] }},
    required:   { control: 'boolean' },
    disabled:   { control: 'boolean' },
    validationType:{ control: { type: 'select', options: ['', 'english', 'number', 'korean', 'email'] }},
    reserveErrorSpace:{ control: 'boolean' },
    externalError:{ control: 'text' },
    externalInvalid:{ control: 'boolean' },
    bgColor:    { control: 'color' },
    change:     { action: 'change' },
    keydown:    { action: 'keydown' },
    'update:modelValue': { action: 'update:modelValue' }
  }
};

/* 기본 */
export const 기본 = (args) => ({
  components: { DefaultTextfield },
  setup() { const v = ref(args.modelValue); return { args, v }; },
  template: `
    <DefaultTextfield v-bind="args" v-model="v"
      @change="$emit('change',$event)" @keydown="$emit('keydown',$event)" />
    <p style="margin-top:8px">현재 값: {{ v }}</p>
  `
});

/* 검증 & 외부 에러 */
export const 검증 = () => ({
  components: { DefaultTextfield },
  setup() { const v = ref('abc'); return { v }; },
  template: `
    <DefaultTextfield v-model="v"
      validationType="email"
      placeholder="이메일 형식"
      :externalError="'서버-측 에러 예시'"
      :externalInvalid="v==='error'"
      reserveErrorSpace
    />
  `
});

/* 타입 & 사이즈 모음 */
export const 타입_사이즈 = () => ({
  components: { DefaultTextfield },
  template: `
    <div style="display:flex;flex-direction:column;gap:10px">
      <DefaultTextfield type="text"   size="small"  placeholder="text/small"/>
      <DefaultTextfield type="number" size="medium" placeholder="number/medium"/>
      <DefaultTextfield type="date"   size="large"  placeholder="date/large"/>
      <DefaultTextfield type="month"  size="full"   placeholder="month/full"/>
    </div>
  `
});
