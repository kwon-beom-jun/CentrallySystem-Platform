<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('hrm.userManagement.title')"
        :subtitle="$t('hrm.userManagement.subtitle')"
        icon="ri-user-settings-line"
      />

      <div class="search-controls">
        <!-- 부서, 팀 선택 그룹 -->
        <div class="d-flex align-items-center department-group">
          <DefaultLabel :text="$t('hrm.userManagement.departmentLabel')" forId="department" size="small" />
          <DefaultSelect
            id="department"
            v-model="selectedDepartment"
            :options="deptFilterOptions"
            :placeholder="$t('hrm.userManagement.departmentPlaceholder')"
            size="small"
            marginRight="10px"
          />
          <DefaultLabel :text="$t('hrm.userManagement.teamLabel')" forId="team" size="small" />
          <DefaultSelect
            id="team"
            v-model="selectedTeam"
            :options="teamsOptions"
            :placeholder="$t('hrm.userManagement.teamPlaceholder')"
            size="small"
            :disabled="isTeamDisabled"
          />
        </div>
        <!-- 이름 검색 그룹 -->
        <div class="d-flex align-items-center name-group">
          <DefaultLabel :text="$t('hrm.userManagement.nameLabel')" forId="nameSearch" size="small" />
          <DefaultTextfield
            type="text"
            id="nameSearch"
            v-model="nameSearch"
            :placeholder="$t('hrm.userManagement.namePlaceholder')"
          />
        </div>
      </div>

      <!-- 테이블 보기 -->
      <DefaultTable
        :columns="columns"
        :heightAdjust=heightSize
        :data="filteredData"
        :rowClick="(item) => showModal(item)"
        :fixedHeader="true"
        :noDataImageHeight="518"
        dynamic-style="518px"
      />
      <!-- 테이블 왼쪽 아래 체크박스 -->
      <DefaultFormRow align="right" marginTop="15px">
          <input
            type="checkbox"
            id="showDisabled"
            v-model="showDisabled"
            style="margin-right: 6px;"
          />
          <label for="showDisabled" style="font-size: 0.95em;">{{ $t('hrm.userManagement.showDisabled') }}</label>
      </DefaultFormRow>
    </div>

    <!-- 사용자 세부 정보 모달 -->
    <UserManagementModifyModal
      :visible="modalVisible"
      :isAdmin="isAdmin"
      :user="selectedUser"
      :departmentsOptions="deptModalOptions"
      :teamsOptions="teamsOptions"
      :positionsOptions="positionsOptions"
      :departmentsData="departments"
      @close="modalVisible = false"
      @confirmSave="handleSaveClick"
      @confirmDelete="handleDeleteClick"
      @confirmEnable="handleEnableClick"
    />

    <!-- 저장 확인 모달 -->
    <AlertModal
      :isVisible="confirmationModalVisible"
      :disableBackgroundClose="true"
      :title="$t('hrm.userManagement.saveConfirmTitle')"
      :cancelText="$t('common.button.cancel')"
      :confirmText="$t('common.button.confirm')"
      @close="confirmationModalVisible = false"
      @confirm="saveUserDetails"
    >
      <template #body>
        <p>{{ $t('hrm.userManagement.saveConfirmMessage') }}</p>
        <div v-if="statusChanged" style="color: red;">{{ $t('hrm.userManagement.statusChangeWarning') }}</div>
      </template>
    </AlertModal>

    <!-- "비활성화 확인" 전용 모달 -->
    <AlertModal
      :isVisible="deleteConfirmVisible"
      :disableBackgroundClose="true"
      :title="$t('hrm.userManagement.disableConfirmTitle')"
      :cancelText="$t('common.button.cancel')"
      :confirmText="$t('hrm.userManagement.disableButton')"
      @close="deleteConfirmVisible = false"
      @confirm="deleteUserConfirmed"
    >
      <template #body>
        <span style="white-space: pre-line">{{ $t('hrm.userManagement.disableConfirmMessage') }}</span>
      </template>
    </AlertModal>

    <!-- 변경된 데이터가 없을 때 모달 -->
    <AlertModal
      :isVisible="noChangesModalVisible"
      :title="$t('hrm.userManagement.alertTitle')"
      :confirmText="$t('common.button.confirm')"
      @close="closeNoChangesModal"
      @confirm="closeNoChangesModal"
    >
      <template #body>
        {{ $t('hrm.userManagement.noChangesMessage') }}
      </template>
    </AlertModal>

    <!-- 저장 완료 모달 -->
    <AlertModal
      :isVisible="saveCompleteModalVisible"
      :title="saveCompleteMessageTitle"
      :confirmText="$t('common.button.confirm')"
      @close="closeSaveCompleteModal"
      @confirm="closeSaveCompleteModal"
    >
      <template #body>
        {{ saveCompleteMessage }}
      </template>
    </AlertModal>

    <AlertModal
      :isVisible="enableConfirmVisible"
      :disableBackgroundClose="true"
      :title="$t('hrm.userManagement.enableConfirmTitle')"
      :cancelText="$t('common.button.cancel')"
      confirmText="활성화"
      @close="enableConfirmVisible = false"
      @confirm="enableUserConfirmed"
    >
      <template #body>
        정말로 활성화하시겠습니까?
      </template>
    </AlertModal>

    <AlertModal
      :isVisible="receiptConfirmVisible"
      :disableBackgroundClose="true"
      title="⚠️ 영수증 결재자 비활성화 안내"
      cancelText="취소"
      confirmText="확인"
      @close="receiptConfirmVisible = false"
      @confirm="proceedToFinalConfirm"
    >
      <template #body>
        <p>
          해당 사용자는 현재 처리 대기 중인 영수증의 <br>
          <span style="color: red; font-weight: bold;">결재선에 포함</span>되어 있습니다.
        </p>
        <p style="margin-top: 15px;">비활성화를 계속 진행하면 아래 규칙에 따라 처리됩니다.</p>

        <div style="margin-top: 10px; text-align: left; padding-left: 0px;">
          <ul style="padding-left: 10px; margin-top: 5px; list-style-type: none;">
            
            <li v-if="receiptsToRejectIds.length > 0">
              <div>
                '신청' 상태 영수증 ({{ receiptsToRejectIds.length }}건)
                → <span style="font-weight: bold; color: red;">반려 처리</span>
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
        또한, 다른 사원에 등록되어있는<br>
        <strong style="color:red;">대리결재자</strong>와
        <strong style="color:red;">즐겨찾기</strong>가
        해제됩니다.
        <div style="margin-top: 15px;">정말로 계속하시겠습니까?</div>
      </template>
    </AlertModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ROUTES } from '@/config/menuConfig';

import HrmUserApi from '@/api/hrm/UsersApi';
import AuthUserApi from '@/api/auth/UsersApi';
import MetadataApi from '@/api/hrm/UserManagementMetadataApi';
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi';
import ReceiptsRequestApi from '@/api/receipt/ReceiptsRequestApi'
import ReceiptsApproverFavoriteApi from '@/api/receipt/ReceiptsApproverFavoriteApi'
import ReceiptsDefaultApproverApi from '@/api/receipt/ReceiptsDefaultApproverApi';
import ReceiptsDelegateApi from '@/api/receipt/ReceiptsDelegateApi';

import AlertModal from '@/components/common/modal/AlertModal.vue';
import UserManagementModifyModal from '@/components/hrm/UserManagementModifyModal.vue';
import DefaultTable from '@/components/common/table/DefaultTable.vue';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue'
import { useToastStore } from '@/store/toast'
import { useViewStateStore } from '@/store/viewState'
import { useI18n } from 'vue-i18n'
import { getImagePreviewUrl } from '@/utils/fileUtils'
import { HRM_API_BASE_URL, HRM_SERVICE_NAME } from '@/constants'

const { t } = useI18n()
import { 
  checkUserInApprovalLine as checkApprovalLineUtil, 
  cleanupReceiptsOnDeactivation,
  isDefaultApprover as checkIsDefaultApprover
} from '@/utils/receipt/receiptUtils';

// 최상단에 피처 플래그 변수 추가
const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';
// toast store 사용
const toastStore = useToastStore();
const viewState = useViewStateStore();

// 상수
const MOBILE_BP         = 650          /* 화면폭 기준(px)                */
const MOBILE_PAGE_SIZE  = 0
const DESKTOP_PAGE_SIZE = 55

// 반응형 상태
const isMobile = ref(window.innerWidth <= MOBILE_BP)
const router = useRouter()
const heightSize = computed(() => (isMobile.value ? MOBILE_PAGE_SIZE : DESKTOP_PAGE_SIZE))
window.addEventListener('resize', () => { isMobile.value = window.innerWidth <= MOBILE_BP })

// 관리자 여부
const isAdmin = ref(true);

// 모달 관련 상태
const modalVisible = ref(false);
const confirmationModalVisible = ref(false);
const saveCompleteModalVisible = ref(false);
const noChangesModalVisible = ref(false);
const saveCompleteMessageTitle = ref('저장 완료');
const saveCompleteMessage  = ref('저장되었습니다.');
const deleteConfirmVisible = ref(false);  // 1차 비활성화 확인 모달
const receiptConfirmVisible = ref(false); // 2차 영수증 확인 모달
const enableConfirmVisible = ref(false);  // 활성화 확인 모달
const deletingUser         = ref(null);   // 비활성화 대상 객체 저장 (userId, name, email)
const pendingReceiptIds    = ref([]);     // 반려 처리에 사용할 영수증 ID 목록
const codesToReject        = ref([]);     // '반려' 처리될 영수증 코드 목록
const codesToRemove        = ref([]);     // '제거' 처리될 영수증 코드 목록
const enablingUserId       = ref(null);
const receiptsToRejectIds = ref([]);
const showDisabled = ref(false); // 체크박스 상태

// 테이블 컬럼 설정
const columns = [
  { key: 'name',          label: '이름',         width: 'auto', minWidth: 100 },
  { key: 'email',         label: '이메일',       width: 'auto', minWidth: 100 , mobileDisable:true },
  { key: 'department',    label: '부서',         width: 110  , mobileDisable:true },
  { key: 'team',          label: '팀',           width: 110 },
  { key: 'positionName',  label: '직급',         width: 60 , mobileDisable:true  },
  { key: 'employmentType',label: '고용 형태',     width: 70 , mobileDisable:true },
  { key: 'status',        label: '재직 상태',     width: 70,   align: 'center' , mobileDisable:true },
  /** 컬럼이 enabled이고 값이 true, false이면 테이블 로우가 활성화/비활성화 처리됨 */
  { key: 'enabled', label: '계정', width: 60, align: 'center',
    desktopStyle: { fontWeight: '800' }, /* font-weight 700 적용 */
    /* 셀 내부에 표시할 텍스트 */
    customValue:   (row) => row.enabled ? '활성화'   : '비활성화',
    /* 색상도 true/false 기준으로 처리  */
    customClass:   (val) => val ? 'text-blue' : 'text-red'
  },
];

// 사용자 관련 상태
const selectedUser = ref({});
const originalUser = ref({});
const statusChanged = ref(false);

// 부서, 팀 선택 상태 (선택된 값은 각각의 id, 숫자 타입)
const selectedDepartment = ref('');
const selectedTeam = ref('');

// 부서 데이터 (MetadataApi에서 가져옴, 각 부서에 teams 배열 포함)
const departments = ref([]);
// 고용 타입
const employmentTypes = ref([]);
// 직급 데이터
const positions = ref([]);
// 사용자 데이터 (HrmUserApi)
const data = ref([]);
// 이름 검색어
const nameSearch = ref('');

/** (A) 부모-화면 드롭다운용 – “전체” 포함 */
const deptFilterOptions = computed(() => [
  { value: '',  label: '전체보기'   },   // 리스트 필터 전용
  { value: 0,   label: '미지정' },
  ...departments.value.map(d => ({
    value: d.departmentId,
    label: d.departmentName
  }))
]);

/** (B) 자식 모달용 – “전체” 제외  */
const deptModalOptions = computed(() => [
  { value: 0, label: '미지정' },
  ...departments.value.map(d => ({
    value: d.departmentId,
    label: d.departmentName
  }))
]);

const positionsOptions = computed(() => {
  return positions.value.map(pos => ({
    value: pos.positionId,
    label: pos.positionName
  }));
});

// 팀 select 옵션
const teamsOptions = computed(() => {
  if (selectedDepartment.value) {
    const dept = departments.value.find(dep => dep.departmentId === Number(selectedDepartment.value));
    return dept && dept.teams
      ? [
          // { value: '', label: '팀 선택' },
          /* { value: 0, label: '미지정' }, */
          ...dept.teams.map(team => ({
            value: team.teamId,
            label: team.teamName
          }))
        ]
      : [
        // { value: '', label: '팀 선택' }, 
        /* { value: 0, label: '미지정' } */
      ];
  }
  return [
      // { value: '', label: '팀 선택' }, 
      /* { value: 0, label: '미지정' } */
    ];
});

// (팀 선택 → 부서 자동 연동)
watch(selectedTeam, (newTeamId) => {
  if (newTeamId) {
    for (const dep of departments.value) {
      const foundTeam = dep.teams?.find(t => t.teamId === Number(newTeamId));
      if (foundTeam) {
        if (dep.departmentId !== Number(selectedDepartment.value)) {
          selectedDepartment.value = dep.departmentId;
        }
        break;
      }
    }
  }
});

// (부서 선택이 바뀌면 팀 초기화)
watch(selectedDepartment, () => {
  selectedTeam.value = '';
});

// 테이블에 표시할 데이터: 필터
const filteredData = computed(() => {
  return data.value
    .filter(user => {
      // (1) 활성화만 보기(기본) / (2) 체크박스 체크 시 전체 보기
      if (!showDisabled.value && user.enabled === false) {
        return false;
      }
      let filterPass = true;

      // (A) 부서만 선택
      if (selectedDepartment.value && !selectedTeam.value) {
        const deptVal = Number(selectedDepartment.value);
        filterPass = user.departmentId === deptVal;
      }

      // (B) 팀만 선택
      if (!selectedDepartment.value && selectedTeam.value) {
        if (Number(selectedTeam.value) === 0) {
          filterPass = !user.teamId || user.teamId === 0;
        } else {
          filterPass = user.teamId === Number(selectedTeam.value);
        }
      }

      // (C) 부서+팀 모두 선택
      if (selectedDepartment.value && selectedTeam.value) {
        filterPass = (
          user.departmentId === Number(selectedDepartment.value) &&
          user.teamId === Number(selectedTeam.value)
        );
      }

      // (D) 이름 검색 (이메일 포함)
      const nameMatch = nameSearch.value
        ? (user.name && user.name.includes(nameSearch.value)) ||
          (user.email && user.email.includes(nameSearch.value))
        : true;

      return filterPass && nameMatch;
    })
    .map(user => {
      // 부서, 팀 이름
      let departmentName = '미지정';
      let teamName = '미지정';
      const dept = departments.value.find(dep => dep.departmentId === user.departmentId);
      if (dept) {
        departmentName = dept.departmentName || '미지정';
        const t = dept.teams.find(tt => tt.teamId === user.teamId);
        if (t) {
          teamName = t.teamName || '미지정';
        }
      }
      // 고용형태
      const empType = employmentTypes.value.find(et => et.employmentTypeId === Number(user.employmentTypeId));
      const employmentTypeLabel = empType ? empType.employmentTypeName : '미지정';
      // 직급
      const pos = positions.value.find(p => p.positionId === Number(user.position?.positionId));
      const foundPosition = pos 
        ? { positionId: pos.positionId, positionName: pos.positionName }
        : { positionId: 0, positionName: '미지정' };
      
      return {
        ...user,
        department: departmentName,
        team: teamName,
        // position에 객체 통째로 넣기
        // position: foundPosition,
        positionId: foundPosition.positionId, // 자식 컴포넌트에서 사용
        positionName: foundPosition.positionName,
        employmentType: employmentTypeLabel,
        profileImgUrl: user.profileImg?.fileUrl ? getImagePreviewUrl(user.profileImg.fileUrl, HRM_API_BASE_URL, HRM_SERVICE_NAME) : null,
        status: user.leavingDate ? '퇴사' : '입사'
      };
    })
    .sort((a, b) => {
      // 이름 오름차순 정렬 (한글/영어 모두)
      const nameA = a.name || "";
      const nameB = b.name || "";
      return nameA.localeCompare(nameB, 'ko-KR');
    });
});

// 사용자 클릭 시 모달 열기
function showModal(user) {
  selectedUser.value = { ...user };
  originalUser.value = { ...user };
  statusChanged.value = false;
  if (isMobile.value) {
    const q = new URLSearchParams({ userId: String(user.userId || '') }).toString();
    router.push(ROUTES.MANAGEMENT.USER_MODIFY_MOBILE + '?' + q);
    return;
  }
  modalVisible.value = true;
}

// 임시 저장할 patchData
const pendingPatchData = ref(null);

// 모달에서 "저장" 클릭 시
function handleSaveClick({ isMismatch, isChanged, userData, statusChangedFlag }) {
  if (isMismatch) {
    saveCompleteMessageTitle.value = '비밀번호 불일치';
    saveCompleteMessage.value = '비밀번호와 재확인의 값이 일치하지 않습니다.';
    saveCompleteModalVisible.value = true;
    return;
  }
  if (!isChanged) {
    noChangesModalVisible.value = true;
    return;
  }
  // selectedUser.value = { ...userData };
  // statusChanged.value = statusChangedFlag;
  // confirmationModalVisible.value = true;

  // 바뀐 데이터(userData)를 'selectedUser'에 즉시 넣지 않고, 
  // pendingPatchData에만 저장(기존 : 모달 창에 데이터가 날라감)
  pendingPatchData.value = userData;
  statusChanged.value = statusChangedFlag;

  // "저장 확인" 모달 열기
  confirmationModalVisible.value = true;
}

// 실제 저장 API (PATCH)
async function saveUserDetails() {
  // 메인 모달 닫기
  modalVisible.value = false;
  // "저장 확인" 모달 닫기
  confirmationModalVisible.value = false;

  // 사용자 정보 업데이트
  await HrmUserApi.patchUser(pendingPatchData.value.userId, pendingPatchData.value);

  // 공통 리프레시 헬퍼 호출 (저장 완료)
  await refreshAfterChange('save');
}

// '변경된 데이터가 없습니다' 모달 닫기
function closeNoChangesModal() {
  noChangesModalVisible.value = false;
}

// 저장 완료 모달 닫기
function closeSaveCompleteModal() {
  saveCompleteModalVisible.value = false;
}

/**
 * fetchMetadata:
 *  - (A) 메타데이터(부서/직급/고용형태) → MetadataApi.getMetadatas()
 *  - (B) 사용자 목록(HrmUserApi.getUsers()) 
 *    → team?.department => departmentId/teamId 추출
 */
async function fetchMetadata() {
  const responseMeta = await MetadataApi.getMetadatas();
  const responseUser = await HrmUserApi.getUsersAll();

  // 부서/직급/고용형태
  departments.value = responseMeta.data.departments || [];
  positions.value = responseMeta.data.positions || [];
  employmentTypes.value = responseMeta.data.employmentTypes || [];

  // 사용자 목록
  data.value = (responseUser.data || []).map(u => mapHrmUser(u));
  // 더미 추가 테스트
  data.value.push(...generateDummyUsers(0));
}
/**
 * 원하는 숫자만큼 더미 사용자를 만들어 돌려준다.
 *  - 부서/팀/직급/고용형태 = ‘미지정’
 *  - userId는 1,000,001부터 순차 부여
 */
function generateDummyUsers(count = 300) {
  const list = [];
  for (let i = 1; i <= count; i++) {
    list.push({
      userId: 1_000_000 + i,
      name:   `더미사용자${String(i).padStart(3, '0')}`,  // 예: 더미사용자001
      email:  `dummy${i}@example.com`,
      departmentId: 0,
      teamId:       0,
      position:     { positionId: 0, positionName: '미지정' },
      employmentTypeId: 0,
      leavingDate:  null,            // 입사 상태
    });
  }
  return list;
}

/**
 * HRM 사용자 -> 기존 로직에서 사용하던 departmentId/teamId 삽입
 */
function mapHrmUser(u) {
  let deptId = 0;
  let tId = 0;
  if (u.team) {
    tId = u.team.teamId || 0;
    if (u.team.department) {
      deptId = u.team.department.departmentId || 0;
    }
  }
  return {
    ...u,
    enabled: u.enabled,
    departmentId: deptId, // 추가
    teamId: tId           // 추가
  };
}

// ─────────────────────────── “비활성화”  ───────────────────────────
/** 
 * 비활성화 확인 모달(1차)에서 ‘비활성화’ 버튼 눌렀을 때 
 * - 영수증 서비스 사용 여부에 따라 분기 처리
 */
async function deleteUserConfirmed() {
  deleteConfirmVisible.value = false; // 최종 확인 모달 닫기
  await proceedWithDeactivation();    // 실제 비활성화 처리
}

/**
 * API를 호출하여 사용자가 결재할 문서가 남았는지 확인하는 함수
 * (receiptUtils.js의 checkUserInApprovalLine 사용)
 */
async function checkIfUserInApprovalLine(userId) {
  // 함수 호출 전, 관련 변수들 초기화
  codesToReject.value = [];
  codesToRemove.value = [];
  receiptsToRejectIds.value = [];
  pendingReceiptIds.value = [];

  const result = await checkApprovalLineUtil(userId, ReceiptsSearchApi);
  
  // 결과를 기존 변수에 매핑
  pendingReceiptIds.value = result.receiptIds;
  receiptsToRejectIds.value = result.rejectIds;
  codesToReject.value = result.rejectCodes;

  return result.hasPending;
}

/**
 * 영수증 반려 처리 및 사용자 비활성화를 실제로 진행하는 함수
 * (receiptUtils.js의 cleanupReceiptsOnDeactivation 사용)
 */
async function proceedWithDeactivation() {
  receiptConfirmVisible.value = false;
  modalVisible.value = false;

  const userToDeactivate = deletingUser.value;
  if (!userToDeactivate) return;

  // 영수증 관련 데이터 정리 (공통 함수 사용)
  if (isReceiptEnabled) {
    await cleanupReceiptsOnDeactivation({
      userId: userToDeactivate.userId,
      userName: userToDeactivate.name,
      receiptIds: pendingReceiptIds.value,
      apis: {
        ReceiptsRequestApi,
        ReceiptsDelegateApi,
        ReceiptsApproverFavoriteApi
      }
    });
  }
  
  // 사용자 비활성화
  await AuthUserApi.deleteUser(userToDeactivate.userId);

  // 약간의 지연 후 서버 데이터 재동기화
  // Kafka 이벤트가 HRM 서비스에 반영될 시간을 고려
  setTimeout(async () => {
    await refreshAfterChange();
  }, 1000);
}

/** 자식 모달에서 “비활성화” 클릭 시 호출 */
async function handleDeleteClick(user) {
  deletingUser.value = user; // 대상 저장

  // 영수증 기능이 꺼져있다면, 곧바로 최종 확인 모달만 보여준다.
  if (!isReceiptEnabled) {
    deleteConfirmVisible.value = true;
    return;
  }

  // 고정 합의자인지 먼저 확인 (receiptUtils.js의 isDefaultApprover 사용)
  const isDefault = await checkIsDefaultApprover(user.userId, ReceiptsDefaultApproverApi);
  if (isDefault) {
    toastStore.error(`[${user.name}]님은 고정 합의자로 등록되어 있어 비활성화할 수 없습니다.`);
    return;
  }

  // 결재선 포함 여부 확인 → 반려 대상 목록 수집
  const isUserInLine = await checkIfUserInApprovalLine(user.userId);

  if (isUserInLine) {
    // (1) 결재선 포함 → 상세 리스트 먼저 보여주기
    receiptConfirmVisible.value = true;
  } else {
    // (2) 포함되어 있지 않음 → 바로 최종 확인 모달
    deleteConfirmVisible.value = true;
  }
}

/* 상세(반려 리스트) 모달에서 "확인" 클릭 시 → 최종 확인 모달 호출 */
function proceedToFinalConfirm() {
  receiptConfirmVisible.value = false;
  // 최종 확인 모달 제거
  // deleteConfirmVisible.value   = true;
  // 바로 비활성화 진행
  deleteUserConfirmed();
}

// 비활성화/활성화 공통 헬퍼
async function refreshAfterChange (action = 'default') {
  await fetchMetadata();            // 서버 다시 조회

  if (action === 'save') {
    saveCompleteMessageTitle.value = '저장 완료';
    saveCompleteMessage.value      = '저장되었습니다.';
  } else {
    saveCompleteMessageTitle.value = '완료';
    saveCompleteMessage.value      = '처리가 완료되었습니다.';
  }

  saveCompleteModalVisible.value = true;
}

// ─────────────────────────── “활성화”  ───────────────────────────
// “활성화” 처리 핸들러 추가
// async function enableUser (userId) {
//   await AuthUserApi.patchUser(userId, { enabled:true });
//   await refreshAfterChange();
// }
// “활성화” 최종 확정 핸들러
async function enableUserConfirmed () {
  enableConfirmVisible.value = false;   // 확인-모달 닫기
  modalVisible.value         = false;   // 편집 모달도 같이 닫기

  await AuthUserApi.patchUser(enablingUserId.value, { enabled: true });

  // 약간의 지연 후 서버 데이터 재동기화 (Kafka 반영 시간 고려)
  setTimeout(async () => {
    await refreshAfterChange();
  }, 1100);
}

function handleEnableClick(userId) {
  enablingUserId.value   = userId;
  enableConfirmVisible.value = true;
}

// 팀 셀렉트 박스를 비활성화할지 여부
const isTeamDisabled = computed(() => {
  if (selectedDepartment.value === '') return true;
  const dept = departments.value.find(
    d => d.departmentId === Number(selectedDepartment.value)
  );
  return !dept || !dept.teams || dept.teams.length === 0; // 팀이 0개면 true
});

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  const saved = viewState.getState('hrmUserManagement');
  const restore = viewState.canRestore('hrmUserManagement');
  if (restore && saved) {
    selectedDepartment.value = saved.selectedDepartment ?? '';
    selectedTeam.value       = saved.selectedTeam ?? '';
    nameSearch.value         = saved.nameSearch ?? '';
  }
  await fetchMetadata();
  if (restore && saved && typeof saved.scrollY === 'number') {
    requestAnimationFrame(() => { window.scrollTo(0, saved.scrollY) })
  }
  // 허용된 이전 라우트를 등록: 모바일 상세에서만 복원 허용
  viewState.allowRestoreFrom('hrmUserManagement', ['UserManagementModifyMobile'])
});

// 화면폭이 모바일로 전환되면, 모달이 열려 있을 경우 자동 닫고 모바일 상세로 이동
watch(isMobile, (nowMobile) => {
  if (nowMobile && modalVisible.value) {
    modalVisible.value = false; // 모바일로 전환 시 모달만 닫기
  }
});

watch([selectedDepartment, selectedTeam, nameSearch, showDisabled], () => {
  viewState.saveState('hrmUserManagement', {
    selectedDepartment: selectedDepartment.value,
    selectedTeam: selectedTeam.value,
    nameSearch: nameSearch.value,
    showDisabled: showDisabled.value,
    scrollY: window.scrollY,
  })
});
</script>

<style scoped>
hr {
  margin-bottom: 0px;
}

/* 테이블 스타일 */
.table {
  width: 100%;
  margin: 0;
}

/* 테이블 컨테이너: 스크롤 추가하여 10개 행 정도 보이도록 */
.table-container {
  margin-bottom: 20px;
}

/* 검색 영역 반응형 */
.label-team {
  margin-left: 10px;
}

.search-controls {
  margin-bottom: 10px;
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
.more-items-text {
  margin: 0;
  font-size: 0.9em;
  color: #6c757d;
}
@media (min-width: 651px) {
  .search-controls {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-end;
  }
  .search-controls > div {
    margin-right: 1rem;
    margin-bottom: 0;
  }
  #nameSearch {
    font-size: 1rem;
  }
}

@media (max-width: 650px) {
  .receipt-list-box li {
    font-size: 0.7rem;
  }
  .search-controls {
    margin-bottom: 0px;
  }
  .name-group {
    margin-bottom: 10px;
  }
  .department-group {
    margin-bottom: 0px;
  }
  .search-controls {
    display: flex;
    flex-direction: column-reverse;
    align-items: flex-end;
  }
  .search-controls > div {
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
