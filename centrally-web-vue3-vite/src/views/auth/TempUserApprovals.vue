<!-- src/pages/temp-user/TempUsersPage.vue -->
<template>
  <div class="content content-wrapper">
    <PageTitle 
      :title="$t('auth.tempApprovals.title')"
      :subtitle="$t('auth.tempApprovals.subtitle')"
      icon="ri-user-add-line"
    />
    <!-- HRM 권한 안내 문구 -->
    <InfoNotice 
      color="yellow" 
      icon="ri-alert-line"
      text="사용자 승인 시 반드시 HRM 권한을 부여해주세요."
      marginBottom="15px"
    />
    <!-- 검색 영역 -->
    <DefaultFormRow align="right" marginBottom="10px">
      <DefaultLabel :text="$t('auth.tempApprovals.nameLabel')" forId="nameSearch" size="small" />
      <DefaultTextfield
        type="text"
        id="nameSearch"
        v-model="nameSearch"
        :placeholder="$t('auth.tempApprovals.searchPlaceholder')"
      />
    </DefaultFormRow>
    
    <!-- ───── 테이블 ───── -->
    <DefaultTable
      :columns="columns"
      :data="filteredUsers"
      :fixedHeader="true"
      :rowKey="'userId'"
      @approve-row="approve"
      @delete-row="askDelete"
      :buttonHeight="'28px'"
      :heightAdjust="100"
      :rowClick="openDetailModal"
      :minRows="isMobile ? 6 : 8"
      :noDataImageHeight="468"
    />

    <!-- ───── 삭제 확인 모달 ───── -->
    <AlertModal
      :isVisible="deleteModalVisible"
      :title="$t('auth.tempApprovals.deleteConfirmTitle')"
      :confirmText="$t('common.button.delete')"
      :cancelText="$t('common.button.cancel')"
      @close="deleteModalVisible = false"
      @confirm="confirmDelete"
    >
      <template #body>
        <div style="white-space: pre-line">
          {{ $t('auth.tempApprovals.deleteConfirmMessage', { name: rowToDelete?.name }) }}
        </div>
      </template>
    </AlertModal>

    <!-- ───── 모바일 상세 모달 ───── -->
    <TempUserDetailModal
      v-if="isDetailModalVisible"
      :rowData="selectedRow"
      @close="isDetailModalVisible = false"
      @approve-row="approve"
      @delete-row="askDelete"
    />
  </div>
</template>

<script setup>
/* ───────── imports ───────── */
import { ref, onMounted, computed } from "vue";
import useBreakPoint from '@/composables/useBreakPoint';
import TempUsersApi from "@/api/auth/TempUsersApi";
import DefaultTable from "@/components/common/table/DefaultTable.vue";
import AlertModal from "@/components/common/modal/AlertModal.vue";
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import { toast } from "vue3-toastify";
import TempUserDetailModal from '@/components/auth/TempUserDetailModal.vue';
import PageTitle from '@/components/common/title/PageTitle.vue';
import InfoNotice from '@/components/common/notice/InfoNotice.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

/* ───────── 테이블 정의 ───────── */
const columns = [
  { key: "name", label: t('common.label.name'), width: 'auto', minWidth: 60, align: "center" },
  { key: "email", label: t('common.label.email'), width: 'auto', minWidth: 100, flex: 1 , mobileDisable:true },
  { key: "phone", label: t('common.label.phone'), width: 130 , mobileDisable:true },
  {
    key: "approve",
    label: t('auth.tempApprovals.approve'),
    width: 80,
    align: "center",
    type: "button",
    buttonText: t('auth.tempApprovals.approve'),
    buttonColor: "blue",
    buttonSize: "full-small",
    emit: "approve-row",
  },
  {
    key: "delete",
    label: t('auth.tempApprovals.reject'),
    width: 80,
    align: "center",
    type: "button",
    buttonText: t('common.button.delete'),
    buttonColor: "red",
    buttonSize: "full-small",
    emit: "delete-row",
  },
];

/* ───────── 상태 ───────── */
const users = ref([]);
const nameSearch = ref(''); // 이름 검색
// 반응형 구분 (공용 컴포저블)
const { isMobile } = useBreakPoint();
// 상세 모달 상태
const isDetailModalVisible = ref(false);
const selectedRow = ref(null);
const deleteModalVisible = ref(false); // 모달 on/off
const rowToDelete = ref(null); // 삭제 대상 저장

/**
 * 필터링된 사용자 목록
 */
const filteredUsers = computed(() => {
  let result = users.value;
  
  // 이름 또는 이메일로 검색
  if (nameSearch.value.trim()) {
    const searchTerm = nameSearch.value.trim().toLowerCase();
    result = result.filter(user => 
      user.name?.toLowerCase().includes(searchTerm) || 
      user.email?.toLowerCase().includes(searchTerm)
    );
  }
  
  return result;
});


/* ───────── CRUD ───────── */
async function fetchUsers() {
  const { data } = await TempUsersApi.getUsers();
  users.value = (data ?? []).map((u) => ({
    ...u,
    userId: u.userId,
    name: u.name,
    phone: u.phoneNumber,
    email: u.email,
  }));
}

/* 승인 */
async function approve(row) {
  await TempUsersApi.approveUser(row.userId);
  toast.success(t('auth.tempApprovals.approveSuccessMessage', { name: row.name }));
  await fetchUsers();
}

/* 삭제 버튼 클릭 → 모달 오픈 */
function askDelete(row) {
  rowToDelete.value = row;
  deleteModalVisible.value = true;
}

/* 모달에서 '삭제' 확정 */
async function confirmDelete() {
  deleteModalVisible.value = false;
  if (!rowToDelete.value) return;

  await TempUsersApi.deleteUser(rowToDelete.value.userId);
  toast.success(t('auth.tempApprovals.rejectSuccessMessage', { name: rowToDelete.value.name }));
  await fetchUsers();
  rowToDelete.value = null;
}

/* ───────── 모바일 상세 모달 ───────── */
function openDetailModal(row) {
  if (!isMobile.value) return;
  selectedRow.value = row;
  isDetailModalVisible.value = true;
}

/* ───────── init ───────── */
onMounted(fetchUsers);
</script>

<style scoped>
.content-sub-title {
  margin-bottom: 120px;
}
.name-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 모바일 반응형 */
@media (max-width: 650px) {
  .name-group {
    width: 100%;
  }
}
</style>