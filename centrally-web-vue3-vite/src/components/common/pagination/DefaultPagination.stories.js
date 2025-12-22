import { ref } from 'vue';
import DefaultPagination from './DefaultPagination.vue';

export default {
  title: 'Common/Pagination/Default',
  component: DefaultPagination,
  args: {
    currentPage: 1,
    totalPages: 10,
    visiblePageCount: 5
  },
  argTypes: {
    currentPage: { control: { type: 'number', min: 1 } },
    totalPages:  { control: { type: 'number', min: 1 } },
    visiblePageCount: { control: { type: 'number', min: 3, step: 2 } },
    pageChange: { action: 'pageChange' }
  }
};

/* 기본: Controls 로 값 조절 */
export const 기본 = (args) => ({
  components: { DefaultPagination },
  setup() {
    const page = ref(args.currentPage);
    return { args, page };
  },
  template: `
    <DefaultPagination
      v-bind="args"
      :currentPage="page"
      @pageChange="(p)=>{ page=p; $emit('pageChange',p); }"
    />
    <p style="margin-top:8px">현재 페이지: {{ page }}</p>
  `
});
