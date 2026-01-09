<template>
  <div>
    <div class="content content-wrapper">
      <MobileDetailTitle :title="$t('hrm.userModifyMobile.title')" />
      <!-- <p class="content-sub-title">모바일 전용 상세 페이지</p> -->

      <div class="form-pane">
        <!-- 프로필 -->
        <div class="form-group cs-card--blue profile-card">
          <div class="profile-wrap">
            <img
              v-if="localUser.profileImgUrl"
              :src="localUser.profileImgUrl"
              alt="profile"
              class="avatar-80"
            />
            <i v-else class="ri-user-line avatar-placeholder-80"></i>
            <div class="profile-meta">
              <div class="row-2col">
                <div>
                  <MobileMintLabel :text="$t('hrm.userModifyMobile.name')" size="small" marginBottom="5px"/>
                  <MobileMintTextfield
                    v-model="localUser.name"
                    :disabled="true"
                    size="full"
                    style="width: 100%"
                  />
                </div>
                <div>
                  <MobileMintLabel :text="$t('hrm.userModifyMobile.position')" size="small" marginBottom="5px"/>
                  <MobileMintSelect
                    v-model="localUser.positionId"
                    :options="positionSelectOptions"
                    size="full"
                    style="width: 100%"
                  />
                </div>
              </div>
              <div class="row">
                <MobileMintLabel :text="$t('hrm.userModifyMobile.email')" size="small" marginBottom="5px"/>
                <MobileMintTextfield
                  v-model="localUser.email"
                  :disabled="true"
                  size="full"
                  style="width: 100%"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 기본 정보: 고용 형태 / 생년월일 한 줄 -->
        <div class="form-group cs-card--blue">
          <MobileMintLabel :text="$t('hrm.userModifyMobile.employmentType')" size="small" marginBottom="5px"/>
          <div class="row-2col">
            <MobileMintSelect
              v-model="localUser.employmentTypeId"
              :options="employmentTypeOptions"
              size="full"
              style="width: 100%"
            />
            <MobileMintTextfield
              type="date"
              v-model="localUser.birth"
              size="full"
              style="width: 100%"
            />
          </div>
        </div>

        <div class="form-group cs-card--blue">
          <MobileMintLabel :text="$t('hrm.userModifyMobile.status')" size="small" marginBottom="5px"/>
          <div class="row-2col">
            <MobileMintSelect
              v-model="localUser.status"
              :options="statusOptions"
              size="full"
              style="width: 100%"
            />
            <MobileMintTextfield
              type="date"
              v-model="computedDate"
              size="full"
              style="width: 100%"
            />
          </div>
        </div>

        <!-- 조직 정보: 부서/팀 한 컨테이너 한 줄 -->
        <div class="form-group cs-card--blue">
          <MobileMintLabel :text="$t('hrm.userModifyMobile.department')" size="small" marginBottom="5px"/>
          <div class="row-2col">
            <MobileMintSelect
              v-model="localUser.departmentId"
              :options="departmentOptions"
              size="full"
              style="width: 100%"
            />
            <MobileMintSelect
              v-model="localUser.teamId"
              :options="teamOptions"
              :disabled="isTeamDisabled"
              :placeholder="teamPlaceholder"
              size="full"
              style="width: 100%"
            />
          </div>
          <div class="tiny-hint">{{ $t('hrm.userModifyMobile.teamHint') }}</div>
        </div>

        <!-- 주소: 우편번호/주소 두 줄 같은 컨테이너 -->
        <div class="form-group cs-card--blue">
          <div class="row">
            <MobileMintLabel :text="$t('hrm.userModifyMobile.zipCode')" size="xsmall" marginBottom="5px"/>
            <MobileMintTextfield v-model="localUser.zipCode" :disabled="true" />
          </div>
          <div class="row">
            <MobileMintLabel :text="$t('hrm.userModifyMobile.address')" size="xsmall" marginBottom="5px"/>
            <MobileMintTextfield
              v-model="localUser.address"
              :disabled="true"
              size="full"
              style="width: 100%"
            />
          </div>
        </div>
        <div class="form-group cs-card--blue">
          <MobileMintLabel :text="$t('hrm.userModifyMobile.dispatchLocation')" size="small" marginBottom="5px"/>
          <MobileMintTextfield
            v-model="localUser.dispatchLocations"
            size="full"
            style="width: 100%"
          />
        </div>

        <DefaultFormRow align="right" marginTop="10px">
          <MobileMintButton color="gray" type="button" @click="goBack"
            >{{ $t('hrm.userModifyMobile.goBack') }}</MobileMintButton
          >
          <MobileMintButton type="button" style="margin-left: 8px" @click="onSave"
            >{{ $t('hrm.userModifyMobile.save') }}</MobileMintButton
          >
        </DefaultFormRow>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { toast } from 'vue3-toastify';
import { useI18n } from 'vue-i18n';
import MobileDetailTitle from '@/components/common/title/MobileDetailTitle.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import MobileMintButton from '@/components/common/button/MobileMintButton.vue';
import MobileMintSelect from '@/components/common/select/MobileMintSelect.vue';
import MobileMintTextfield from '@/components/common/textfield/MobileMintTextfield.vue';
import MobileMintLabel from '@/components/common/label/MobileMintLabel.vue';

import MetadataApi from '@/api/hrm/UserManagementMetadataApi';
import HrmUserApi from '@/api/hrm/UsersApi';
import { useToastStore } from '@/store/toast';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { HRM_API_BASE_URL, HRM_SERVICE_NAME } from '@/constants';

const route = useRoute();
const router = useRouter();
const toastStore = useToastStore();
const { t } = useI18n();

const originalUser = ref({});
const localUser = ref({});

const departments = ref([]);
const positions = ref([]);
const employmentTypes = ref([]);
const teamPlaceholder = ref(t('hrm.userModifyMobile.teamSelect'));

const statusOptions = [
  { value: '입사', label: t('hrm.userModifyMobile.join') },
  { value: '퇴사', label: t('hrm.userModifyMobile.resign') },
];

const employmentTypeOptions = computed(() => {
  return [
    { value: 0, label: t('hrm.userModifyMobile.unspecified') },
    ...employmentTypes.value.map((e) => ({
      value: e.employmentTypeId,
      label: e.employmentTypeName,
    })),
  ];
});

const departmentOptions = computed(() => {
  return [
    { value: 0, label: t('hrm.userModifyMobile.unspecified') },
    ...departments.value.map((d) => ({ value: d.departmentId, label: d.departmentName })),
  ];
});

const teamOptions = computed(() => {
  if (!localUser.value.departmentId) return [];
  const dept = departments.value.find(
    (d) => Number(d.departmentId) === Number(localUser.value.departmentId),
  );
  return dept?.teams?.map((t) => ({ value: t.teamId, label: t.teamName })) || [];
});

const isTeamDisabled = computed(() => {
  if (!localUser.value.departmentId || Number(localUser.value.departmentId) === 0)
    return true;
  const dept = departments.value.find(
    (d) => Number(d.departmentId) === Number(localUser.value.departmentId),
  );
  return !(dept && dept.teams && dept.teams.length > 0);
});

const positionSelectOptions = computed(() => {
  return [
    { value: 0, label: t('hrm.userModifyMobile.unspecified') },
    ...positions.value.map((p) => ({ value: p.positionId, label: p.positionName })),
  ];
});

/**
 * HRM 사용자 응답을 페이지에서 사용하는 형태로 매핑
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
    departmentId: deptId,
    teamId: tId,
    positionId: u.position?.positionId ?? 0,
    positionName: u.position?.positionName ?? t('hrm.userModifyMobile.unspecified'),
    employmentTypeId: Number(u.employmentTypeId ?? 0),
    status: u.leavingDate ? t('hrm.userModifyMobile.resign') : t('hrm.userModifyMobile.join'),
    profileImgUrl: u.profileImg?.fileUrl ? getImagePreviewUrl(u.profileImg.fileUrl, HRM_API_BASE_URL, HRM_SERVICE_NAME) : null,
  };
}

/**
 * 부서 변경 시 팀 UI(placeholder, 선택값 유효성) 갱신
 */
function refreshTeamUI() {
  const deptId = Number(localUser.value.departmentId || 0);
  if (deptId > 0) {
    const dept = departments.value.find((d) => Number(d.departmentId) === deptId);
    const hasTeams = !!(dept && dept.teams && dept.teams.length > 0);
    teamPlaceholder.value = hasTeams ? t('hrm.userModifyMobile.teamSelect') : t('hrm.userModifyMobile.teamNone');
    if (!hasTeams) {
      localUser.value.teamId = '';
      return;
    }
    if (!dept.teams.some((t) => Number(t.teamId) === Number(localUser.value.teamId))) {
      localUser.value.teamId = '';
    }
  } else {
    teamPlaceholder.value = t('hrm.userModifyMobile.teamSelect');
    localUser.value.teamId = '';
  }
}

/**
 * 쿼리에서 userId 읽기
 */
function getUserIdFromQuery() {
  const q = route.query.userId;
  return q ? Number(q) : null;
}

/**
 * 메타데이터 로드 (부서/직급/고용형태)
 */
async function loadMetadata() {
  const responseMeta = await MetadataApi.getMetadatas();
  departments.value = responseMeta.data.departments || [];
  positions.value = responseMeta.data.positions || [];
  employmentTypes.value = responseMeta.data.employmentTypes || [];
}

/**
 * 사용자 로드
 */
async function loadUser(userId) {
  const responseUser = await HrmUserApi.getUserById(userId);
  originalUser.value = mapHrmUser(responseUser.data);
  localUser.value = { ...originalUser.value };
}

/**
 * 입사/퇴사 날짜 계산 속성
 */
const computedDate = computed({
  get() {
    const today = new Date().toISOString().split('T')[0];
    return localUser.value.status === t('hrm.userModifyMobile.join')
      ? localUser.value.joiningDate || today
      : localUser.value.leavingDate || today;
  },
  set(value) {
    if (localUser.value.status === t('hrm.userModifyMobile.join')) {
      localUser.value.joiningDate = value;
      localUser.value.leavingDate = null;
    } else {
      localUser.value.leavingDate = value;
      localUser.value.joiningDate = null;
    }
  },
});

/**
 * 상태 변경 시 날짜 기본값 보정
 */
watch(
  () => localUser.value.status,
  (newStatus) => {
    const today = new Date().toISOString().split('T')[0];
    if (newStatus === t('hrm.userModifyMobile.resign')) {
      localUser.value.leavingDate = localUser.value.leavingDate || today;
      localUser.value.joiningDate = null;
    } else {
      localUser.value.joiningDate = localUser.value.joiningDate || today;
      localUser.value.leavingDate = null;
    }
  },
);

/**
 * 팀 선택 시 부서 자동 세팅(부서가 비었을 경우)
 */
watch(
  () => localUser.value.teamId,
  (newTeamId) => {
    if (
      newTeamId &&
      (!localUser.value.departmentId || Number(localUser.value.departmentId) === 0)
    ) {
      const foundDept = departments.value.find((dep) =>
        dep.teams?.some((t) => Number(t.teamId) === Number(newTeamId)),
      );
      if (foundDept) localUser.value.departmentId = foundDept.departmentId;
    }
  },
);

// 부서 선택이 바뀌면 팀 UI 갱신
watch(
  () => localUser.value.departmentId,
  () => {
    refreshTeamUI();
  },
);

/**
 * 변경된 필드만 patch로 구성
 */
function buildPatchData(originalUserObj, newUserObj) {
  const patch = {};

  if (originalUserObj.birth !== newUserObj.birth) patch.birth = newUserObj.birth;
  if (originalUserObj.employmentTypeId !== newUserObj.employmentTypeId)
    patch.employmentTypeId = newUserObj.employmentTypeId;
  if (originalUserObj.status !== newUserObj.status) patch.status = newUserObj.status;
  if (originalUserObj.joiningDate !== newUserObj.joiningDate)
    patch.joiningDate = newUserObj.joiningDate;
  if (originalUserObj.leavingDate !== newUserObj.leavingDate)
    patch.leavingDate = newUserObj.leavingDate;
  if (originalUserObj.departmentId !== newUserObj.departmentId)
    patch.departmentId = newUserObj.departmentId;
  if (originalUserObj.teamId !== newUserObj.teamId) patch.teamId = newUserObj.teamId;
  if (originalUserObj.positionId !== newUserObj.positionId)
    patch.positionId = newUserObj.positionId;
  if (originalUserObj.address !== newUserObj.address) patch.address = newUserObj.address;
  if (originalUserObj.zipCode !== newUserObj.zipCode) patch.zipCode = newUserObj.zipCode;
  if (originalUserObj.dispatchLocations !== newUserObj.dispatchLocations)
    patch.dispatchLocations = newUserObj.dispatchLocations;

  patch.userId = newUserObj.userId;
  ['departmentId', 'teamId'].forEach((key) => {
    if (patch[key] === '') patch[key] = null;
  });

  return patch;
}

/**
 * 저장
 */
async function onSave() {
  const patchData = buildPatchData(originalUser.value, localUser.value);
  const changed = Object.keys(patchData).length > 1;
  if (!changed) return toast.info(t('hrm.userModifyMobile.noChanges'));
  await HrmUserApi.patchUser(patchData.userId, patchData);
  toastStore.setNextPageMessage(t('hrm.userModifyMobile.saveSuccess'), 'success');
  router.replace('/hrm/user-management');
}

/**
 * 뒤로가기
 */
function goBack() {
  if (window.history.length > 1) {
    router.back();
    return;
  }
  router.replace('/hrm/user-management');
}

/**
 * 화면 폭이 651px 이상이면 데스크톱 경로로 리다이렉트
 */
function handleResizeRedirect() {
  if (window.innerWidth > 650) {
    router.replace('/hrm/user-management');
  }
}

onMounted(async () => {
  handleResizeRedirect();
  window.addEventListener('resize', handleResizeRedirect);

  const userId = getUserIdFromQuery();
  if (!userId) {
    toast.error(t('hrm.userModifyMobile.invalidAccess'));
    return router.replace('/hrm/user-management');
  }
  await loadMetadata();
  await loadUser(userId);
  refreshTeamUI();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResizeRedirect);
});
</script>

<style scoped>
.form-pane {
  padding-top: 15px;
}
.form-group {
  margin-bottom: 20px;
  background: #ffffff;
  border: 1px solid #e6f2ff;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 4px 14px rgba(0, 140, 255, 0.06);
}
.form-control {
  font-size: 0.8rem;
  height: 33px;
}

.profile-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}
.avatar-80 {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin: 5px 5px 5px 0px;
  object-fit: cover;
}
.avatar-placeholder-80 {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #6b7280;
}
.profile-meta {
  flex: 1;
  min-width: 0;
}
.row {
  display: flex;
  flex-direction: column;
  margin-bottom: 8px;
}
.row-2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 10px;
}
.tiny-hint {
  font-size: 0.65rem;
  color: rgba(255, 0, 0, 0.76);
}

/* 다크모드: theme/pages/hrm/user-detail/mobile/user-management-modify-dark.css */
</style>
