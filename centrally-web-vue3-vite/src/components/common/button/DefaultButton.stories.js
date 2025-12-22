import DefaultButton from './DefaultButton.vue';

export default {
  // Sidebar 경로: Common/Button/Default
  title: 'Common/Button/Default',
  component: DefaultButton,
  args: {
    // 기본 prop 값 → Controls 패널 초기값
    type: 'button',
    size: 'medium',
    color: 'default',
    align: 'center',
    customClass: '',
    marginLeft: '0',
    marginRight: '0',
    marginTop: '0',
    marginBottom: '0',
    customHeight: '',
    customWidth: ''
  },
  argTypes: {
    /* === select 컨트롤 === */
    type: { control: { type: 'select', options: ['button', 'submit', 'reset'] } },
    size: { control: { type: 'select', options: [
      'xsmall','small','medium','large',
      'full','full-xsmall','full-small','full-medium','full-large'
    ]}},
    color: { control: { type: 'select', options: ['default','blue','red','green','yellow','gray'] } },
    align: { control: { type: 'select', options: ['left','center','right'] } },

    /* === text 입력 === */
    customClass:   { control: 'text' },
    marginLeft:    { control: 'text' },
    marginRight:   { control: 'text' },
    marginTop:     { control: 'text' },
    marginBottom:  { control: 'text' },
    customHeight:  { control: 'text' },
    customWidth:   { control: 'text' },

    /* === 이벤트 === */
    click: { action: 'clicked' }   // emit된 click 이벤트를 Actions 패널에 표시
  },
};

/* 기본 스토리 */
export const 기본 = (args) => ({
  components: { DefaultButton },
  setup() { return { args }; },
  template: '<DefaultButton v-bind="args">기본 버튼</DefaultButton>',
});

/* 색상만 바꿔가며 보여주기 */
export const 색상 = (args) => ({
  components: { DefaultButton },
  setup() { return { args }; },
  template: `
    <div style="display:flex; gap:8px">
      <DefaultButton v-bind="args" color="blue">Blue</DefaultButton>
      <DefaultButton v-bind="args" color="red">Red</DefaultButton>
      <DefaultButton v-bind="args" color="green">Green</DefaultButton>
      <DefaultButton v-bind="args" color="yellow">Yellow</DefaultButton>
      <DefaultButton v-bind="args" color="gray">Gray</DefaultButton>
    </div>
  `,
});

/* 크기별 보여주기 */
export const 크기 = (args) => ({
  components: { DefaultButton },
  setup() { return { args }; },
  template: `
    <div style="display:flex; flex-direction:column; gap:6px; width:220px">
      <DefaultButton v-bind="args" size="xsmall">xsmall</DefaultButton>
      <DefaultButton v-bind="args" size="small">small</DefaultButton>
      <DefaultButton v-bind="args" size="medium">medium</DefaultButton>
      <DefaultButton v-bind="args" size="large">large</DefaultButton>
      <DefaultButton v-bind="args" size="full">full (100%)</DefaultButton>
    </div>
  `,
});
