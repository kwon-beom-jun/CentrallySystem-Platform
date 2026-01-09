<template>
  <div class="content content-wrapper">
    <PageTitle 
      :title="$t('hrm.permissionsDetail.title')"
      :subtitle="$t('hrm.permissionsDetail.subtitle')"
      icon="ri-key-2-line"
      desktopMarginBottom="35px"
    />

    <!-- 필터 섹션 -->
    <div class="grid-container">
      <div class="grid-item">
        <DefaultLabel :text="$t('hrm.permissionsDetail.departmentSelect')" size="small" forId="departmentSelect" alignment="left" />
        <DefaultSelect
          id="departmentSelect"
          v-model="selectedDepartment"
          :options="departmentsOptions"
          :placeholder="$t('hrm.permissionsDetail.departmentSelect')"
          margin-top="5px"
          size="full"
        />
      </div>

      <div class="grid-item">
        <DefaultLabel :text="$t('hrm.permissionsDetail.teamSelect')" size="small" forId="teamSelect" alignment="left" />
        <DefaultSelect
          id="teamSelect"
          v-model="selectedTeam"
          :options="teamsOptions"
          :placeholder="$t('hrm.permissionsDetail.teamSelect')"
          margin-top="5px"
          size="full"
        />
      </div>

      <div class="grid-item">
        <DefaultLabel :text="$t('hrm.permissionsDetail.serviceSelect')" size="small" forId="serviceNameelect" alignment="left" />
        <DefaultSelect
          id="serviceNameelect"
          v-model="selectedServiceName"
          :options="serviceNameOptions"
          :placeholder="$t('hrm.permissionsDetail.serviceSelect')"
          margin-top="5px"
          size="full"
        />
      </div>

      <div class="grid-item">
        <DefaultLabel :text="$t('hrm.permissionsDetail.userSearch')" size="small" forId="userSearch" alignment="left" />
        <DefaultTextfield
          type="text"
          id="userSearch"
          v-model="searchTerm"
          :placeholder="$t('hrm.permissionsDetail.searchPlaceholder')"
          margin-top="5px"
          size="full"
        />
      </div>
    </div>
    
    <DefaultFormRow align="right" margin-bottom="5px">
      <!-- 권한 추가 버튼 -->
      <DefaultButton
        class="mt-2"
        size="small"
        color="gray"
        @click="openAddPermissionModal"
      >
        {{ $t('hrm.permissionsDetail.addPermission') }}
      </DefaultButton>
    </DefaultFormRow>

    <!-- 권한 테이블 -->
    <DefaultTable
      :columns="columns"
      :data="sortedUsers"
      :rowClick="onRowClick"
      :fixedHeader="true"
      :selectHeight="'30px'"
      :buttonHeight="'30px'"
      @row-updated="handleRowUpdated"
      @delete-row="onDeleteClick"
      :minRows="7"
      :noDataImageHeight="430"
      dynamic-style="430px"
      :heightAdjust="-35"
    />

    <!-- 권한 추가 모달 -->
    <AddPermissionModal 
      v-if="showAddPermissionModal"
      @close="showAddPermissionModal = false"
      @permissionAdded="onPermissionAdded"
    />
  </div>

  <!-- 삭제 확인 AlertModal (권한 변경 모달과 동일 UX) -->
  <AlertModal
    :isVisible="deleteConfirmVisible"
    :disableBackgroundClose="true"
    :title="$t('hrm.permissionsDetail.deleteConfirmTitle')"
    :confirmText="$t('common.button.delete')"
    :cancelText="$t('common.button.cancel')"
    @close="deleteConfirmVisible = false"
    @confirm="deleteNotice"
  >
    <template #body>
      <div v-if="isReceiptEnabled && lastIsCriticalChange">
        <div style="margin-bottom: 5px;">
          {{ $t('hrm.permissionsDetail.receiptWarningTitle') }}<br/>
        </div><br/>
        {{ $t('hrm.permissionsDetail.receiptWarningMessage') }}<br/>
        {{ $t('hrm.permissionsDetail.proxyApprover') }}와
        {{ $t('hrm.permissionsDetail.favorite') }}가
        {{ $t('hrm.permissionsDetail.willBeReleased') }}
      </div>
      <div style="margin-top: 5px;">
        {{ $t('hrm.permissionsDetail.deleteConfirmMessage') }}
      </div>
    </template>
  </AlertModal>

  <AlertModal
    :isVisible="criticalConfirmVisible"
    :disableBackgroundClose="true"
    :title="$t('hrm.permissionsDetail.receiptWarningTitle')"
    :confirmText="$t('common.button.confirm')"
    :cancelText="$t('common.button.cancel')"
    @close="cancelCriticalChange"
    @confirm="confirmCriticalChange"
  >
    <template #body>
      <p>
        {{ $t('hrm.permissionsDetail.receiptChangeWarning') }}<br/>
        <strong style="color:red;">{{ $t('hrm.permissionsDetail.proxyApprover') }}</strong>와
        <strong style="color:red;">{{ $t('hrm.permissionsDetail.favorite') }}</strong>도 {{ $t('hrm.permissionsDetail.willBeReleased') }}
      </p>
      <p style="margin-top: 15px;">
        {{ $t('hrm.permissionsDetail.autoRejectWarning') }}
        <span style="color: red; font-weight: bold;">{{ $t('hrm.permissionsDetail.autoReject') }}</span>
      </p>
      <div style="margin-top: 10px; text-align: left; padding-left: 0px;">
        <ul style="padding-left: 10px; margin-top: 5px; list-style-type: none;">
          <li v-if="receiptsToRejectIds.length > 0">
            <div>
              {{ $t('hrm.permissionsDetail.receiptStatus') }} ({{ receiptsToRejectIds.length }}{{ $t('common.label.count') }})
              → <span style="font-weight: bold; color: red;">{{ $t('hrm.permissionsDetail.rejectProcess') }}</span>
            </div>
            <div class="receipt-list-box">
              <ul>
                <li v-for="code in codesToReject" :key="code">
                  {{ code }}
                </li>
              </ul>
            </div>
          </li>
        </ul>
      </div>
      <div style="margin-top: 15px;">{{ $t('hrm.permissionsDetail.continueConfirm') }}</div>
    </template>
  </AlertModal>

  <AlertModal
    :isVisible="updateConfirmVisible"
    :disableBackgroundClose="true"
    :title="$t('hrm.permissionsDetail.updateConfirmTitle')"
    :confirmText="$t('common.button.modify')"
    :cancelText="$t('common.button.cancel')"
    @close="cancelUpdate"
    @confirm="confirmUpdate"
  >
    <template #body>
      <div v-if="isReceiptEnabled && lastIsCriticalChange">
        <div style="margin-bottom: 5px;">
          {{ $t('hrm.permissionsDetail.receiptWarningTitle') }}<br/>
        </div>
        {{ $t('hrm.permissionsDetail.receiptWarningMessage') }}<br/>
        {{ $t('hrm.permissionsDetail.proxyApprover') }}와
        {{ $t('hrm.permissionsDetail.favorite') }}가
        {{ $t('hrm.permissionsDetail.willBeReleased') }}
      </div>
      <div>{{ $t('hrm.permissionsDetail.updateConfirmMessage') }}</div>
    </template>
  </AlertModal>

  <AlertModal
    :isVisible="rowDetailVisible"
    :disableBackgroundClose="false"
    :title="$t('hrm.permissionsDetail.detailTitle')"
    :confirmText="$t('hrm.permissionsDetail.close')"
    cancelText=""
    @close="rowDetailVisible = false"
    @confirm="rowDetailVisible = false"
  >
    <template #body>
      <div v-if="selectedRowDetail" class="detail-card">
        <dl>
          <div class="detail-row">
            <dt>{{ $t('hrm.permissionsDetail.name') }}</dt>
            <dd>{{ selectedRowDetail.name }}</dd>
          </div>
          <div class="detail-row">
            <dt>{{ $t('hrm.permissionsDetail.email') }}</dt>
            <dd>{{ selectedRowDetail.email }}</dd>
          </div>
          <div class="detail-row">
            <dt>{{ $t('hrm.permissionsDetail.service') }}</dt>
            <dd>{{ selectedRowDetail.serviceName }}</dd>
          </div>
          <div class="detail-row">
            <dt>{{ $t('hrm.permissionsDetail.department') }}</dt>
            <dd>{{ selectedRowDetail.department }}</dd>
          </div>
          <div class="detail-row">
            <dt>{{ $t('hrm.permissionsDetail.team') }}</dt>
            <dd>{{ selectedRowDetail.team }}</dd>
          </div>
          <div class="detail-row">
            <dt>{{ $t('hrm.permissionsDetail.permission') }}</dt>
            <dd>{{ selectedRowDetail.roleNameDetail }}</dd>
          </div>
        </dl>
      </div>
    </template>
  </AlertModal>
</template>

<script setup>
import { ref, watch, computed, onMounted } from "vue";
import { useI18n } from 'vue-i18n';
import DOMPurify from 'dompurify'
import DefaultSelect from "@/components/common/select/DefaultSelect.vue";
import DefaultTextfield from "@/components/common/textfield/DefaultTextfield.vue";
import DefaultTable from "@/components/common/table/DefaultTable.vue";
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue'
import AddPermissionModal from '@/components/auth/AddPermissionModal.vue';

import HrmUserApi from "@/api/hrm/UsersApi";
import AuthUserApi from "@/api/auth/UsersApi";
import AuthUserPermissionApi from "@/api/auth/UserPermissionApi";
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi';
import ReceiptsRequestApi from '@/api/receipt/ReceiptsRequestApi';
import ReceiptsDefaultApproverApi from '@/api/receipt/ReceiptsDefaultApproverApi';
import ReceiptsDelegateApi from '@/api/receipt/ReceiptsDelegateApi';
import ReceiptsApproverFavoriteApi from '@/api/receipt/ReceiptsApproverFavoriteApi';

import { toast } from 'vue3-toastify';
import { useViewStateStore } from '@/store/viewState';

const { t } = useI18n();
import { 
  checkUserInApprovalLine as checkApprovalLineUtil,
  cleanupReceiptsOnDeactivation,
  isDefaultApprover as checkIsDefaultApprover
} from '@/utils/receipt/receiptUtils';

// 영수증 기능 활성화 여부 (환경 변수에서 가져옴)
const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';
const viewState = useViewStateStore();

// ========== 상태 변수 ==========
const departments = ref([]);    // 부서+팀 정보
const tableRows = ref([]);      // 테이블에 표시할 (유저×서비스) 행들
const allRoles = ref([]);       // 전체 권한(roles) 목록
const deleteConfirmVisible = ref(false); // 모달 토글
const rowToDelete = ref(null);          // 삭제 대상 행
const deleteModalMessage = ref(''); // 삭제 모달용 동적 메시지
const sanitizedDeleteModalMessage = computed(() => DOMPurify.sanitize(deleteModalMessage.value ?? ''))

// 일반 변경 확인 모달을 위한 상태 변수
const updateConfirmVisible = ref(false);
const rowToUpdate = ref(null);
const lastIsCriticalChange = ref(false); // 마지막 체크가 영수증 결재 권한 상실 포함인지 여부

// 중요 권한 변경 확인을 위한 상태 변수
const criticalConfirmVisible = ref(false);
const rowForCriticalChange = ref(null); // 삭제 또는 수정될 행의 정보
const criticalChangeType = ref(''); // 'delete' 또는 'update'

// 필터 상태
const selectedDepartment = ref("");
const selectedTeam = ref("");
const selectedServiceName = ref("");
const searchTerm = ref("");

// 영수증 처리 관련 상태 변수
const receiptsToRejectIds = ref([]);
const codesToReject = ref([]);

// 권한 변경사항 저장 (예: { [userId]: '권한내용' })
const changes = ref({});

// [추가] 모바일 상세보기 모달 상태
const rowDetailVisible = ref(false);
const selectedRowDetail = ref(null);

// ========== 테이블 컬럼 ==========
const columns = computed(() => [
  { key: "name",        label: t('hrm.permissionsDetail.name'),      width: 'auto', minWidth: 60 },
  { key: "email",       label: t('hrm.permissionsDetail.email'),    width: 'auto', minWidth: 100 , mobileDisable:true },
  { key: "serviceName", label: t('hrm.permissionsDetail.service'),    width: 100 , mobileDisable:true },
  { key: "department",  label: t('hrm.permissionsDetail.department'),      width: 100 , mobileDisable:true },
  { key: "team",        label: t('hrm.permissionsDetail.team'),        width: 100 },
  {
    key: 'roleName',
    label: t('hrm.permissionsDetail.permission'),
    width: 100,
    type: 'select',
    getOptions: (row) => {
      // row.serviceName = 서비스명
      // allRoles.value = 전체 권한 [{roleId, roleNameDetail, serviceName}, ...]
      const filtered = allRoles.value.filter(r => r.serviceName === row.serviceName);
      // DefaultSelect가 사용하는 { value, label } 형태
      return filtered.map(r => ({
        value: r.roleName, 
        label: r.roleNameDetail
      }));
    },
  },
  {
    key: "delete",
    label: "",
    width: 80,
    type: "button",
    buttonText: t('common.button.delete'),
    buttonColor: "red",
    buttonSize: "full-small",
    emit: 'delete-row' 
  }
]);

/**
 * 사용자가 중요 권한(시스템, 영수증 결재)을 실제로 잃는지 확인하는 함수
 * serviceName + roleName 기반으로 정확하게 체크
 */
async function checkCriticalPermissionLoss(userId, changedRow, type) {
  // 1. API를 통해 사용자의 현재 전체 권한 목록을 가져옴
  const response = await AuthUserApi.getUserWithRoles(userId);
  const currentRoles = response.data.roles || [];

  // 2. 현재 권한 상태 확인 (serviceName 기반 - 정확한 체크)
  const currentHasSystemPermission = currentRoles.some(r => r.serviceName === 'system');
  const currentHasReceiptApproval = currentRoles.some(r => 
    r.serviceName === 'receipt' && r.roleName !== 'ROLE_RECEIPT_REGISTRAR'
  );

  // 3. 변경 후의 권한 목록을 시뮬레이션
  let simulatedRoles = [];

  if (type === 'delete') {
    // 특정 서비스에 해당하는 모든 권한을 제거
    simulatedRoles = currentRoles.filter(role => 
      role.serviceName !== changedRow.serviceName
    );
  } else { // type === 'update'
    // 기존 권한을 모두 제거하고...
    const otherRoles = currentRoles.filter(role => 
      role.serviceName !== changedRow.serviceName
    );
    // ...새로 선택된 권한 정보를 찾아서 추가
    const newRoleObject = allRoles.value.find(r => 
      r.serviceName === changedRow.serviceName && r.roleName === changedRow.roleName
    );
    simulatedRoles = newRoleObject ? [...otherRoles, newRoleObject] : otherRoles;
  }
  
  // 4. 변경 후 권한 상태 확인 (serviceName 기반 - 정확한 체크)
  const afterHasSystemPermission = simulatedRoles.some(r => r.serviceName === 'system');
  const afterHasReceiptApproval = simulatedRoles.some(r => 
    r.serviceName === 'receipt' && r.roleName !== 'ROLE_RECEIPT_REGISTRAR'
  );

  // 5. 변경 전후 실제 결재 가능 여부 확인
  // 시스템 권한 OR 영수증 결재 권한이 있으면 결재 가능
  const couldApproveBefore = currentHasSystemPermission || currentHasReceiptApproval;
  const canApproveAfter = afterHasSystemPermission || afterHasReceiptApproval;
  
  // 6. 실제 결재 능력을 잃는지 확인
  // (변경 전에는 결재 가능했는데, 변경 후에는 불가능할 때만 true)
  const isLosingApprovalAbility = couldApproveBefore && !canApproveAfter;
  
  return isLosingApprovalAbility;
}

/**
 * row-updated 이벤트 핸들러: 1차 확인 모달을 띄우는 역할만 수행
 */
async function handleRowUpdated(updatedRow) {
  // 모달 중복 방지: 시작 시 모두 끄기
  closeAllModals();
  // 변경할 내용을 임시 저장
  rowToUpdate.value = updatedRow;

  // (1) 중요 권한 + 미결재 영수증 여부 먼저 확인
  const isCritical = await checkCriticalPermissionLoss(updatedRow.userId, updatedRow, 'update');
  lastIsCriticalChange.value = !!isCritical;

  if (isCritical && isReceiptEnabled) {
    // 고정 합의자 여부 확인 (receiptUtils.js의 isDefaultApprover 사용)
    const isDefault = await checkIsDefaultApprover(updatedRow.userId, ReceiptsDefaultApproverApi);
    if (isDefault) {
      toast.error(`[${updatedRow.name}]${t('hrm.permissionsDetail.defaultApproverError')}`);
      await fetchMetadata();
      return;
    }

    // 미결재 영수증 확인 (receiptUtils.js의 checkUserInApprovalLine 사용)
    const result = await checkApprovalLineUtil(updatedRow.userId, ReceiptsSearchApi);
    if (result.hasPending) {
      receiptsToRejectIds.value = result.rejectIds;
      codesToReject.value = result.rejectCodes;
      return showCriticalModal(updatedRow, 'update');
    }
  }

  // (2) 중요하지 않거나, 미결재 영수증 없음 → 바로 최종 확인 모달
  showUpdateConfirm(updatedRow);
}

/**
 * 1차 확인 모달에서 '변경'을 눌렀을 때 실행되는 함수
 */
async function confirmUpdate() {
  // 최종 확인 모달 닫기
  updateConfirmVisible.value = false;
  const updatedRow = rowToUpdate.value;
  if (!updatedRow) return;

  // 영수증 정리 (receiptUtils.js의 cleanupReceiptsOnDeactivation 사용)
  if (isReceiptEnabled && lastIsCriticalChange.value) {
    await cleanupReceiptsOnDeactivation({
      userId: updatedRow.userId,
      userName: updatedRow.name,
      receiptIds: receiptsToRejectIds.value,
      reason: `${updatedRow.name} 결재 권한이 변경되었습니다.`,
      apis: {
        ReceiptsRequestApi,
        ReceiptsDelegateApi,
        ReceiptsApproverFavoriteApi
      }
    });
  }

  // 권한 업데이트
  await AuthUserPermissionApi.patchUserWithRole({
    userId: updatedRow.userId,
    email: updatedRow.email,
    serviceName: updatedRow.serviceName,
    roleName: updatedRow.roleName,
  });
  await fetchMetadata();
  toast.success(t('hrm.permissionsDetail.updateSuccess'));

  // 상태 초기화
  resetCriticalChangeState();
}

/**
 * 1차 확인 모달에서 '취소'를 눌렀을 때 실행되는 함수
 */
async function cancelUpdate() {
  updateConfirmVisible.value = false;
  rowToUpdate.value = null;
  toast.info(t('hrm.permissionsDetail.updateCancel'));
  // 변경 시도했던 내용을 원상복구하기 위해 목록을 새로고침
  await fetchMetadata();
}


/** 권한 추가 모달 */
const showAddPermissionModal = ref(false);
function openAddPermissionModal() {
  showAddPermissionModal.value = true;
}
function onPermissionAdded() {
  showAddPermissionModal.value = false;
  fetchMetadata();
}

/**
 * 삭제 버튼 클릭 → 중요 권한+결재선 확인 후, 아닐 경우 일반 확인 모달 표시
 */
async function onDeleteClick(row) {
  // 시작 시 모달 정리
  closeAllModals();
  // 1. (최우선) 중요 권한 상실 & 영수증 기능 활성화 & 미결재 영수증 존재 여부 확인
  if (isReceiptEnabled) {
    const isCritical = await checkCriticalPermissionLoss(row.userId, row, 'delete');
    lastIsCriticalChange.value = !!isCritical;
    if (isCritical) {
      // 미결재 영수증 확인 (receiptUtils.js의 checkUserInApprovalLine 사용)
      const result = await checkApprovalLineUtil(row.userId, ReceiptsSearchApi);
      if (result.hasPending) {
        receiptsToRejectIds.value = result.rejectIds;
        codesToReject.value = result.rejectCodes;
        return showCriticalModal(row, 'delete');
      }
    }
  }
  // 2. 일반 삭제 확인 모달
  showDeleteConfirm(row);
}

/**
 * 중요 변경을 최종 확정하는 함수
 * 영수증 목록 확인 후 바로 실행 (두 번째 모달 없음)
 */
async function confirmCriticalChange() {
  if (!rowForCriticalChange.value) return;

  // 1차(상세) 모달 닫기
  criticalConfirmVisible.value = false;

  if (criticalChangeType.value === 'delete') {
    // 삭제: 바로 실행 (두 번째 확인 모달 띄우지 않음)
    rowToDelete.value = { ...rowForCriticalChange.value };
    await deleteNotice();
  } else {
    // 업데이트: 바로 실행 (두 번째 확인 모달 띄우지 않음)
    rowToUpdate.value = { ...rowForCriticalChange.value };
    await confirmUpdate();
  }
}

/** 중요 변경 관련 상태를 초기화하는 함수 */
function resetCriticalChangeState() {
  criticalConfirmVisible.value = false;
  rowForCriticalChange.value = null;
  criticalChangeType.value = '';
}

/** 중요 변경을 취소하는 함수 */
async function cancelCriticalChange() {
  // 상태 초기화를 먼저 수행
  resetCriticalChangeState();
  // 취소 토스트 메시지 표시
  toast.info(t('hrm.permissionsDetail.updateCancel'));
  // 목록 새로고침
  await fetchMetadata();
}

/**
 * AlertModal confirm 콜백
 * (receiptUtils.js의 cleanupReceiptsOnDeactivation 사용)
 */
async function deleteNotice() {
  if (!rowToDelete.value) return;
  const row = rowToDelete.value;

  // 영수증 정리 (receiptUtils.js의 cleanupReceiptsOnDeactivation 사용)
  if (isReceiptEnabled && lastIsCriticalChange.value) {
    await cleanupReceiptsOnDeactivation({
      userId: row.userId,
      userName: row.name,
      receiptIds: receiptsToRejectIds.value,
      reason: `${row.name} 결재 권한이 삭제되었습니다.`,
      apis: {
        ReceiptsRequestApi,
        ReceiptsDelegateApi,
        ReceiptsApproverFavoriteApi
      }
    });
  }

  await deletePermission(row); // 실제 삭제 호출
  deleteConfirmVisible.value = false;
  rowToDelete.value = null;

  // 상태 초기화
  resetCriticalChangeState();
}

/** 권한 삭제 */
async function deletePermission(row) {
  const params = {
    userId: row.userId,
    email: row.email,
    serviceName: row.serviceName,
    roleName: row.roleName
  };
  const response = await AuthUserPermissionApi.deleteUserWithRole(params);
  if (response.status === 200) {
    toast.success(t('hrm.permissionsDetail.deleteSuccess'));
    fetchMetadata();
  }
}

/**
 * 모든 모달을 닫는다 (배타 표시 보장)
 */
function closeAllModals() {
  updateConfirmVisible.value = false;
  criticalConfirmVisible.value = false;
  deleteConfirmVisible.value = false;
}

/**
 * 업데이트 최종 확인 모달을 연다
 */
function showUpdateConfirm(row) {
  closeAllModals();
  rowToUpdate.value = row;
  updateConfirmVisible.value = true;
}

/**
 * 삭제 최종 확인 모달을 연다
 */
function showDeleteConfirm(row) {
  closeAllModals();
  rowToDelete.value = row;
  deleteConfirmVisible.value = true;
}

/**
 * 중요 변경(영수증 결재 권한 상실) 상세 모달을 연다
 */
function showCriticalModal(row, type) {
  closeAllModals();
  rowForCriticalChange.value = row;
  criticalChangeType.value = type; // 'update' | 'delete'
  criticalConfirmVisible.value = true;
}

// -------------------------------------------------------
//   부서/팀/서비스 SelectBox 옵션
// -------------------------------------------------------
const departmentsOptions = computed(() => [
  { value: "", label: "부서 선택" },
  ...departments.value.map(dep => ({
    value: dep.departmentId,
    label: dep.departmentName,
  }))
]);

const teamsOptions = computed(() => {
  if (!selectedDepartment.value) {
    return [{ value: "", label: "팀 선택" }];
  }
  const foundDept = departments.value.find(
    d => d.departmentId === Number(selectedDepartment.value)
  );
  if (!foundDept || !foundDept.teams) {
    return [{ value: "", label: "팀 선택" }];
  }
  return [
    { value: "", label: "팀 선택" },
    ...foundDept.teams.map(team => ({
      value: team.teamId,
      label: team.teamName,
    }))
  ];
});

const serviceNameOptions = computed(() => {
  const uniqueSrv = [...new Set(allRoles.value.map(r => r.serviceName))];
  return [
    { value: "", label: "서비스 선택" },
    ...uniqueSrv.map(s => ({ value: s, label: s }))
  ];
});

// -------------------------------------------------------
//   부서/팀 SelectBox 연동 로직
// -------------------------------------------------------
watch(selectedDepartment, () => {
  selectedTeam.value = "";
});
watch(selectedTeam, (newTeamId) => {
  if (!newTeamId) return;
  for (const dep of departments.value) {
    const foundTeam = dep.teams?.find(t => t.teamId === Number(newTeamId));
    if (foundTeam) {
      if (dep.departmentId !== Number(selectedDepartment.value)) {
        selectedDepartment.value = dep.departmentId.toString();
      }
      break;
    }
  }
});

// -------------------------------------------------------
//   테이블 필터링 / 정렬
// -------------------------------------------------------
const filteredUsers = computed(() => {
  return tableRows.value.filter(row => {
    let isMatch = true;

    // (1) 부서 필터
    if (selectedDepartment.value) {
      const chosenDept = departments.value.find(
        dep => dep.departmentId == selectedDepartment.value
      );
      if (chosenDept) {
        isMatch = isMatch && (row.department === chosenDept.departmentName);
      }
    }

    // (2) 팀 필터
    if (selectedTeam.value) {
      const chosenTeam = departments.value
        .flatMap(dep => dep.teams || [])
        .find(t => t.teamId == selectedTeam.value);
      if (chosenTeam) {
        isMatch = isMatch && (row.team === chosenTeam.teamName);
      }
    }

    // (3) 서비스
    if (selectedServiceName.value) {
      isMatch = isMatch && (row.serviceName === selectedServiceName.value);
    }

    // (4) 검색어
    if (searchTerm.value) {
      const lower = searchTerm.value.toLowerCase();
      const nameMatch = row.name.toLowerCase().includes(lower);
      const emailMatch = row.email.toLowerCase().includes(lower);
      isMatch = isMatch && (nameMatch || emailMatch);
    }

    return isMatch;
  });
});

/**
 * 정렬 로직
 * 1차: 이름 오름차순
 * 2차: 서비스이름 오름차순
 */
const sortedUsers = computed(() => {
  return [...filteredUsers.value].sort((a, b) => {
    // 1. 이름을 기준으로 먼저 비교합니다.
    const nameCompare = a.name.localeCompare(b.name, "ko-KR");
    // 2. 만약 이름이 다르다면, 이름 비교 결과를 바로 반환합니다.
    if (nameCompare !== 0) {
      return nameCompare;
    }
    // 3. 이름이 같다면, 서비스 이름을 기준으로 다시 비교하여 반환합니다.
    return a.serviceName.localeCompare(b.serviceName);
  });
});

// -------------------------------------------------------
//   테이블 행 클릭(선택) - 필요 시만 사용
// -------------------------------------------------------
function onRowClick(row) {
  const isMobile = window.innerWidth <= 650;
  if (isMobile) {
    // 모바일 환경: 상세 정보 모달 표시
    selectedRowDetail.value = { ...row };
    rowDetailVisible.value = true;
  } else {
    // 데스크탑 환경: 기존 로직 유지
    changes.value[row.userId] = row.roleNameDetail;
  }
}

// -------------------------------------------------------
//   🔥 [수정됨]: fetchMetadata에서 HrmUserApi + AuthUserPermissionApi 결합
// -------------------------------------------------------
async function fetchMetadata() {
  // 1) HrmUserApi → 부서/팀/사용자 목록 (userId, team, department 등)
  const hrmRes = await HrmUserApi.getUsers();
  const hrmUsers = hrmRes.data || [];

  // 2) AuthUserPermissionApi → 권한 정보
  const permRes = await AuthUserPermissionApi.getUsersWithRoles();
  allRoles.value = permRes.data.authRoles.allRoles || [];

  const userWithRolesList = permRes.data.userWithRolesList || [];

  // (A) 1) 부서 목록 만들기
  //  HrmUserApi.getUsers()에서 user.team?.department?.departmentId 등이 온다고 가정
  //  Map(deptId -> { departmentId, departmentName, teams: [{teamId, teamName}...] })
  const deptMap = new Map();

  hrmUsers.forEach(u => {
    if (u.team && u.team.department) {
      const d = u.team.department;
      if (!deptMap.has(d.departmentId)) {
        deptMap.set(d.departmentId, {
          departmentId: d.departmentId,
          departmentName: d.departmentName,
          teams: []
        });
      }
      const deptObj = deptMap.get(d.departmentId);
      // 중복 팀 체크
      const already = deptObj.teams.some(t => t.teamId === u.team.teamId);
      if (!already) {
        deptObj.teams.push({
          teamId: u.team.teamId,
          teamName: u.team.teamName
        });
      }
    }
  });
  departments.value = Array.from(deptMap.values());

  // (B) 2) userId -> { departmentName, teamName } 매핑
  //    HrmUserApi 결과를 userId 기준으로 접근할 수 있게끔
  const userDeptMap = new Map();
  hrmUsers.forEach(u => {
    let deptName = '미지정';
    let teamName = '미지정';
    if (u.team && u.team.department) {
      deptName = u.team.department.departmentName;
      teamName = u.team.teamName;
    }
    userDeptMap.set(u.userId, { deptName, teamName });
  });

  // (C) userWithRolesList → (유저×서비스) 테이블 행 구성
  const rowData = [];
  userWithRolesList.forEach(u => {
    const { deptName, teamName } = userDeptMap.get(u.userId) || { deptName: '미지정', teamName: '미지정' };

    // 한 사용자가 서비스별로 1개의 권한을 갖는 현재 정책 기준으로 매핑
    // serviceRoleMap = { 'receipt': { roleName, roleNameDetail }, 'hrm': {...} }
    const serviceRoleMap = {};
    (u.roles || []).forEach(role => {
      const srv = role.serviceName;
      serviceRoleMap[srv] = {
        roleName: role.roleName,
        roleNameDetail: role.roleNameDetail,
      };
    });

    Object.keys(serviceRoleMap).forEach(serviceName => {
      const { roleName, roleNameDetail } = serviceRoleMap[serviceName] || {};
      rowData.push({
        userId: u.userId,
        name: u.name,
        email: u.email,
        department: deptName,
        team: teamName,
        serviceName,
        roleName,         // ← 코드값 (셀렉트의 value)
        roleNameDetail,   // ← 표시용 (상세 팝업 등)
      });
    });
  });

  tableRows.value = rowData;
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  const saved = viewState.getState('hrmUserPermissions');
  const restore = viewState.canRestore('hrmUserPermissions');
  if (restore && saved) {
    selectedDepartment.value  = saved.selectedDepartment ?? "";
    selectedTeam.value        = saved.selectedTeam ?? "";
    selectedServiceName.value = saved.selectedServiceName ?? "";
    searchTerm.value          = saved.searchTerm ?? "";
  }
  await fetchMetadata();
  if (restore && saved && typeof saved.scrollY === 'number') {
    requestAnimationFrame(() => { window.scrollTo(0, saved.scrollY) })
  }
});

watch([selectedDepartment, selectedTeam, selectedServiceName, searchTerm], () => {
  viewState.saveState('hrmUserPermissions', {
    selectedDepartment: selectedDepartment.value,
    selectedTeam: selectedTeam.value,
    selectedServiceName: selectedServiceName.value,
    searchTerm: searchTerm.value,
    scrollY: window.scrollY,
  })
});
</script>

<style scoped>
.content-sub-title {
  margin-bottom: 30px;
}

/* 테이블 스타일 */
.table {
  width: 100%;
  margin: 0;
}

/* 테이블 컨테이너: 스크롤 추가하여 10개 행 정도 보이도록 */
.table-container {
  margin-bottom: 60px;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 15px;
}

.grid-item {
  display: flex;
  flex-direction: column;
}

.search-item {
  display: flex;
  align-items: center;
}

.search-item input {
  flex: 1; /* 입력 필드가 남은 공간을 모두 차지하도록 설정 */
}

.search-item button {
  white-space: nowrap; /* 버튼 텍스트가 줄바꿈되지 않도록 설정 */
}

.search-item-search {
  display: flex;
  align-items: center;
}

.search-item-search > .default-textfield {
  flex: 1;
  min-width: 0;
}

.form-control, .form-select {
  width: 100%;
}

.text-end {
  text-align: right;
}

.save-button {
  margin-top: 0px !important;
  margin-bottom: 50px;
}

.receipt-list-box {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 10px 15px;
  margin: 5px 0 15px 0;
  max-height: 150px;
  overflow-y: auto;
}

.receipt-list-box ul {
  padding-left: 20px;
  margin-top: 5px;
  margin-bottom: 5px;
}

.receipt-list-box li {
  font-size: 0.8rem;
}

/* 반응형: 모바일 환경에서 폰트 크기 조정 */
.custom-modal-pop-up-body {
  font-size: 0.75rem;
}

/* ───── 모바일 상세 정보 카드 ───── */
.detail-card dl {
  margin: 0;
}

.detail-row {
  display: flex;
  padding: 6px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-top: 5px;
}

.detail-row dt {
  flex: 0 0 70px;
  font-weight: 600;
  color: #555;
}

.detail-row dd {
  margin: 0;
  flex: 1;
  color: #333;
}

@media (max-width: 650px) {
  .form-select {
    font-size: 0.75rem !important;
  }
  .permission-select {
    font-size: 0.6rem !important;
  }
  .grid-container {
    grid-template-columns: repeat(2, 1fr);
  }
  .detail-row dt {
    flex-basis: 60px;
    font-size: 0.75rem;
  }
  .detail-row dd {
    font-size: 0.75rem;
  }
}
</style>