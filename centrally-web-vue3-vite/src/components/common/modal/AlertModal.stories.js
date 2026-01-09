import { h, ref } from 'vue';
import AlertModal from './AlertModal.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';

/*
  ──────────────────────────────────────────────────────────────────────
  ⚙️ 메타 정의
*/
export default {
  title: 'Common/Modal/Alert',
  component: AlertModal,

  args: {
    isVisible: true,             // Controls 패널에서 켜고 끌 수 있게
    title: '알림',
    confirmText: '확인',
    cancelText: '취소',
    disableBackgroundClose: false,
    zIndex: 0
  },

  argTypes: {
    isVisible: { control: 'boolean' },
    title: { control: 'text' },
    confirmText: { control: 'text' },
    cancelText: { control: 'text' },
    disableBackgroundClose: { control: 'boolean' },
    zIndex: { control: 'number' },
    /* emit 이벤트 → Actions 탭에 로그 */
    confirm: { action: 'confirm' },
    close: { action: 'close' }
  }
};

/*
  ──────────────────────────────────────────────────────────────────────
  🅰 기본 스토리 (바디 슬롯 예시 포함)
*/
export const 기본 = (args) => ({
  components: { AlertModal },
  setup() {
    const show = ref(args.isVisible);
    /* Storybook Controls로 isVisible 값을 바꿨을 때 반영 */
    return { args, show };
  },
  template: `
    <AlertModal
      v-bind="args"
      :isVisible="show"
      @close="show = false"
      @confirm="show = false; $emit('confirm')"
    >
      <template #body>
        정말 삭제하시겠습니까?
      </template>
    </AlertModal>
  `
});

/*
  ──────────────────────────────────────────────────────────────────────
  🅱 버튼으로 토글 예시 (Docs 보기 좋게)
*/
export const 토글 = (args) => ({
  components: { AlertModal, DefaultButton },
  setup() {
    const show = ref(false);
    return { args, show };
  },
  template: `
    <DefaultButton @click="show = true">모달 열기</DefaultButton>
    <AlertModal
      v-bind="args"
      :isVisible="show"
      @close="show = false"
      @confirm="show = false"
    >
      <template #body>
        이곳에 원하는 내용을 넣을 수 있습니다.
      </template>
    </AlertModal>
  `
});
