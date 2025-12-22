import { ref } from 'vue';
import DefaultTable from './DefaultTable.vue';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';

export default {
  title: 'Common/Table/Default',
  component: DefaultTable,
  argTypes: {
    'row-updated': { action: 'row-updated' },
    'button-click': { action: 'button-click' },
    'selection-change': { action: 'selection-change' }
  }
};

/* 샘플 데이터 & 컬럼 정의 */
const sampleColumns = [
  { key: 'id',     label: '번호', width: 60, align: 'center' },
  { key: 'name',   label: '이름', width: 120, align: 'left'  },
  { key: 'role',   label: '권한', width: 160, type: 'select',
    getOptions: () => [
      { value: 'user',  label: '사용자' },
      { value: 'admin', label: '관리자' }
    ]
  },
  { key: 'email',  label: '이메일', width: 220 },
  { key: 'delete', label: '',      width: 90,  type: 'button',
    buttonText: '삭제', buttonColor: 'red', emit: 'button-click'
  }
];

const sampleRows = [
  { id: 1, name: '홍길동', role: 'user',  email: 'hong@example.com' },
  { id: 2, name: '김철수', role: 'admin', email: 'kim@example.com' },
  { id: 3, name: '이영희', role: 'user',  email: 'lee@example.com' }
];

/* 기본 스토리 */
export const 기본 = () => ({
  components: { DefaultTable, DefaultSelect, DefaultButton },
  setup() {
    const rows = ref([...sampleRows]);
    const selected = ref([]);
    return { sampleColumns, rows, selected };
  },
  template: `
    <DefaultTable
      :columns="sampleColumns"
      :data="rows"
      selectable
      v-model:selectedRows="selected"
      @row-updated="(row)=>{ console.log('updated', row); }"
      @button-click="(row)=>{ rows = rows.filter(r => r.id!==row.id) }"
      :mobileCard="true"
      :fixedHeader="true"
      :scroll="true"
      :scrollHeight="260"
    />
    <p style="margin-top:8px">선택된 행 ID: {{ selected.map(r=>r.id).join(', ') }}</p>
  `
});
