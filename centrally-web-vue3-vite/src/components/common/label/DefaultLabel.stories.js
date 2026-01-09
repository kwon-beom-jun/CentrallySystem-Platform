/* DefaultLabel.stories.js -------------------------------------------------- */
import DefaultLabel from './DefaultLabel.vue';

export default {
  // 사이드바 경로  ─▶  Common / Label / Default
  title: 'Common/Label/Default',
  component: DefaultLabel,

  // Controls 패널 기본값
  args: {
    text: '기본 라벨',
    forId: '',
    required: false,
    size: 'medium',
    color: '#333',
    alignment: 'center',
    marginLeft: '0',
    marginRight: '0',
    marginTop: '0',
    marginBottom: '0'
  },

  // 각 props → 어떤 컨트롤을 보여줄지 정의
  argTypes: {
    text:        { control: 'text',       description: '라벨 내용' },
    forId:       { control: 'text',       description: '`for` 속성(id)' },
    required:    { control: 'boolean',    description: '필수 * 표시' },
    size:        { control: { type: 'select', options: ['small','medium','large'] }},
    color:       { control: 'color' },
    alignment:   { control: { type: 'select', options: ['left','center','right'] }},
    marginLeft:  { control: 'text' },
    marginRight: { control: 'text' },
    marginTop:   { control: 'text' },
    marginBottom:{ control: 'text' }
  }
};

/* 기본 형태 --------------------------------------------------------------- */
export const 기본 = (args) => ({
  components: { DefaultLabel },
  setup() { return { args }; },
  template: '<DefaultLabel v-bind="args" />'
});

/* 사이즈별 예시 ----------------------------------------------------------- */
export const 사이즈 = (args) => ({
  components: { DefaultLabel },
  setup() { return { args }; },
  template: `
    <div style="display:flex; flex-direction:column; gap:8px">
      <DefaultLabel v-bind="args" size="small"  text="small"  />
      <DefaultLabel v-bind="args" size="medium" text="medium" />
      <DefaultLabel v-bind="args" size="large"  text="large"  />
    </div>
  `
});

/* 정렬 예시 --------------------------------------------------------------- */
export const 정렬 = (args) => ({
  components: { DefaultLabel },
  setup() { return { args }; },
  template: `
    <div style="width:300px; border:1px dashed #aaa; padding:8px">
      <DefaultLabel v-bind="args" alignment="left"   text="left"   /><br/>
      <DefaultLabel v-bind="args" alignment="center" text="center" /><br/>
      <DefaultLabel v-bind="args" alignment="right"  text="right"  />
    </div>
  `
});
