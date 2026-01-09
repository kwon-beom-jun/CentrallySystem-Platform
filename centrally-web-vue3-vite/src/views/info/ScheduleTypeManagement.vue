<template>
  <div class="content content-wrapper">
    <!-- 모바일 스크롤 타이틀 (스크롤 시 서브타이틀 사라짐) -->
    <PageTitle 
      :title="$t('info.scheduleType.title')"
      subtitle="Schedule Type Management"
      icon="ri-list-settings-line"
    />

    <!-- ───────── 일정 유형 관리 구역 헤더 ───────── -->
    <div class="hr-label"><span>{{ $t('info.scheduleType.typeManagement') }}</span></div>

    <section class="soft-box">
      <DefaultFormRow align="between" marginBottom="5px">
        <DefaultLabel
          :text="$t('info.scheduleType.typeManagement')"
          customClass="hr-label"
        />
        <DefaultButton
          @click="showModal"
        >
          {{ $t('info.scheduleType.typeRegister') }}
        </DefaultButton>
      </DefaultFormRow>

      <!-- 일정 유형 테이블 -->
      <DefaultTable
        :columns="columns"
        :data="scheduleTypeData"
        @delete-row="handleDelete"
        :fixedHeader="true"
        :scroll="true"
        :scrollHeight="415"
        :rowClick="handleRowClick"
        :selectHeight="'28px'"
        :buttonHeight="'28px'"
        :minRows="7"
        :noDataImageHeight="415"
      />
    </section>

    <!-- 등록/수정 모달 -->
    <ScheduleTypeCreateModal
      v-model:isVisible="isModalVisible"
      :isCreate="isCreate"
      :form="form"
      @save="handleSave"
    />

    <ConfirmationModal
        :isVisible="isDeleteModalVisible"
        :title="$t('info.scheduleType.deleteConfirmTitle')"
        :confirmText="$t('common.button.delete')"
        :cancelText="$t('common.button.cancel')"
        :disableBackgroundClose="true"
        @confirm="confirmDelete"
        @close="isDeleteModalVisible = false"
    >
      <template #body>
        <div>
          <strong>{{ $t('info.scheduleType.typeLabel') }}</strong>
          <p class="mt-2">'{{ deleteInfo.target?.label }}' {{ $t('info.scheduleType.deleteTypeMessage') }}</p>
        </div>
        <p class="text-danger mt-3" style="font-size: 0.8rem;">{{ $t('info.scheduleType.deleteWarning') }}</p>
      </template>
    </ConfirmationModal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import PageTitle from '@/components/common/title/PageTitle.vue';
import DefaultFormRow from "@/components/common/DefaultFormRow.vue";
import DefaultLabel from "@/components/common/label/DefaultLabel.vue";
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTable  from '@/components/common/table/DefaultTable.vue';
import ConfirmationModal from '@/components/common/modal/AlertModal.vue';
import ScheduleTypeCreateModal from '@/components/info/ScheduleTypeCreateModal.vue';
import ScheduleTypeApi from '@/api/info/ScheduleTypeApi';
import { toast } from 'vue3-toastify';
import { useViewStateStore } from '@/store/viewState'

/* ───── 상태 ───── */
const { t } = useI18n();
const viewState        = useViewStateStore();
const isModalVisible   = ref(false);
const isCreate         = ref(true);
const scheduleTypeData = ref([]);
const form             = ref({ 
  scheduleTypeId: null, 
  code: '', 
  label: '', 
  color: '#3b82f6', 
  displayOrder: 0,
  isActive: true 
});
// 삭제 확인 모달 관련 상태 변수
const isDeleteModalVisible = ref(false);
const deleteInfo = ref({ target: null });

/* ───── 테이블 컬럼 ───── */
const columns = computed(() => [
  { key:'displayOrder', label:t('info.scheduleType.displayOrder'),  width:'auto', minWidth: 100,  align: 'center' },
  { key:'label',        label:t('info.scheduleType.label'),          width:'auto', minWidth: 100,  align: 'center' },
  { key:'code',         label:t('info.scheduleType.code'),          width:'auto', minWidth: 100,  align: 'center' },
  { key:'color',        label:t('info.scheduleType.color'),         width:'auto', minWidth: 100,  align: 'center',
    format: (row) => `<span style="display:inline-block;width:20px;height:20px;background-color:${row.color};border:1px solid #ddd;border-radius:3px;"></span>` },
  { key:'isActive',     label:t('info.scheduleType.isActive'),       width:'auto', minWidth: 100,  align: 'center',
    customClass: (value) => value ? 'text-blue' : 'text-red',
    customValue: (row) => row.isActive ? 'TRUE' : 'FALSE' },
  {
    key:'delete',       label:'', width:85, type:'button',
    buttonText:t('common.button.delete'), buttonColor:'red', buttonSize:'full-small',
    emit:'delete-row'
  }
]);

/* ───── 데이터 로딩 ───── */
async function fetchScheduleTypes() {
  const { data } = await ScheduleTypeApi.getScheduleTypes();
  // displayOrder >= 0인 항목을 먼저 정렬, 그 다음 displayOrder = -1인 항목 정렬
  const sorted = data.sort((a, b) => {
    const orderA = a.displayOrder ?? 0;
    const orderB = b.displayOrder ?? 0;
    if (orderA >= 0 && orderB >= 0) {
      return orderA - orderB;
    }
    if (orderA < 0) return 1;
    if (orderB < 0) return -1;
    return 0;
  });
  
  scheduleTypeData.value = sorted
    .filter(st => st.enabled !== false) // 삭제된 항목은 표시하지 않음
    .map(st => ({
      scheduleTypeId: st.scheduleTypeId,
      code:           st.code,
      label:          st.label,
      color:          st.color || '#3b82f6',
      displayOrder:   st.displayOrder ?? 0,
      isActive:       st.isActive !== undefined ? st.isActive : true
    }));
}

/* ───── 버튼 & 콜백 ───── */
function showModal() {
  isCreate.value = true;
  form.value = { 
    scheduleTypeId: null, 
    code: '', 
    label: '', 
    color: '#3b82f6', 
    displayOrder: 0,
    isActive: true 
  };
  isModalVisible.value = true;
}

/* 행 클릭 → 수정 모달 */
function handleRowClick(row) {
  isCreate.value = false;
  form.value = {
    scheduleTypeId: row.scheduleTypeId,
    code:           row.code,
    label:          row.label,
    color:          row.color,
    displayOrder:   row.displayOrder,
    isActive:       row.isActive
  };
  isModalVisible.value = true;
  viewState.saveState('scheduleTypeManagement', { scrollY: window.scrollY })
}

/* 모달 저장 완료 */
async function handleSave() {
  await fetchScheduleTypes();
  isModalVisible.value = false;
}

/* 일정 유형 삭제 버튼 클릭 시 모달 오픈 */
function handleDelete(row) {
  // 활성화된 항목이 1개만 남아있으면 삭제 불가
  const activeCount = scheduleTypeData.value.filter(st => st.isActive).length;
  if (activeCount <= 1 && row.isActive) {
    toast.warning(t('info.scheduleType.minActiveRequired'));
    return;
  }
  
  deleteInfo.value = { target: row };
  isDeleteModalVisible.value = true;
}

/* 삭제 확인 후 실행 */
async function confirmDelete() {
  const { target } = deleteInfo.value;
  if (!target) return;

  try {
    await ScheduleTypeApi.deleteScheduleType(target.scheduleTypeId);
    toast.success(t('info.scheduleType.deleteSuccess'));
    await fetchScheduleTypes();
  } catch (e) {
    toast.error(t('common.message.serverError'));
    console.error(e);
  } finally {
    isDeleteModalVisible.value = false;
    deleteInfo.value = { target: null };
  }
}

/* 페이지 진입 시 불러오기 */
onMounted(async ()=>{
  const saved = viewState.getState('scheduleTypeManagement')
  const restore = viewState.canRestore('scheduleTypeManagement')
  await fetchScheduleTypes()
  if (restore && saved && typeof saved.scrollY === 'number') {
    setTimeout(()=>{ window.scrollTo(0, saved.scrollY) }, 100)
  } else {
    window.scrollTo(0, 0)
  }
})
</script>

<style scoped>
.hr-label{
  display:flex;
  align-items:center;
  margin:28px 0 14px;
  font-weight:600;
  font-size:1rem;
  color:#333;
}
.hr-label::before,
.hr-label::after{
  content:"";
  flex:1;
  border-top:1px solid #d7d7d7;
}
.hr-label span{
  white-space:nowrap;
  margin:0 12px;
}
.soft-box{
  background:#f9f9f9;
  border:1px solid #ededed;
  border-radius:6px;
  padding:14px 16px 30px 16px;
  margin-bottom: 50px;
}

@media (max-width: 650px) {
  .hr-label{
    font-size: 0.8rem !important;
  }
  .soft-box{
    margin-bottom: 30px;
  }
}
</style>

