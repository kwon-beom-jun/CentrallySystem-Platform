import { h, ref } from 'vue';
import EditModal from './EditModal.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';

export default {
  title: 'Common/Modal/Edit',
  component: EditModal,
  args: {
    isVisible: true,
    title: '이름 수정',
    value: '홍길동',
    confirmText: '저장',
    cancelText: '취소',
    disableBackgroundClose: false,
    zIndex: 0
  },
  argTypes: {
    isVisible: { control: 'boolean' },
    title: { control: 'text' },
    value: { control: 'text' },
    confirmText: { control: 'text' },
    cancelText: { control: 'text' },
    disableBackgroundClose: { control: 'boolean' },
    zIndex: { control: 'number' },
    confirm: { action: 'confirm' },
    close: { action: 'close' },
    'update:value': { action: 'update:value' }
  }
};

/* 기본 스토리 ─ 입력값·확인 로깅 */
export const 기본 = (args) => ({
  components: { EditModal },
  setup() {
    const show = ref(args.isVisible);
    const val  = ref(args.value);
    return { args, show, val };
  },
  template: `
    <EditModal
      v-bind="args"
      :isVisible="show"
      :value="val"
      @close="show = false"
      @confirm="show = false"
      @update:value="val = $event"
    />
    <p style="margin-top:12px">입력값 미러링: {{ val }}</p>
  `
});

/* 토글 버튼 + 초기값 변경 */
export const 토글 = (args) => ({
  components: { EditModal, DefaultButton },
  setup() {
    const show = ref(false);
    const val  = ref('');
    return { args, show, val };
  },
  template: `
    <DefaultButton @click="show = true">모달 열기</DefaultButton>

    <EditModal
      v-bind="args"
      :isVisible="show"
      :value="val"
      @close="show = false"
      @confirm="show = false"
      @update:value="val = $event"
    />
  `
});
