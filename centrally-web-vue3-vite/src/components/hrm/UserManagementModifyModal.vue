<template>
  <!-- b-modal: 자식 컴포넌트에서 상태를 v-model로 받아 제어 -->
  <!-- b-modal에서 “OK” 버튼 누르면 자동으로 모달이 닫히는 게 디폴트 동작 -->
  <!-- @ok.prevent="onSaveClick"(수동 제어) 형태 -->
  <!-- onSaveClick() 내부에서 직접 innerModalVisible.value = false;를 호출하지 않는 한 모달이 안 닫힘 -->
  <b-modal
    v-model="innerModalVisible"
    :title="$t('hrm.userInfoDetail.title') || $t('hrm.userInfo.title')"
    @ok.prevent="onSaveClick"
    :ok-title="$t('common.button.save')"
    :cancel-title="$t('common.button.cancel')"
    no-close-on-backdrop
    hide-footer
    centered
  >
    <b-form>
      <!-- 아바타 + 기본 정보 -->
      <DefaultFormRow marginBottom="15px" align="left" class="user-meta-row">
        <template v-if="tempUser.profileImgUrl">
          <img 
            class="avatar-40" 
            :src="tempUser.profileImgUrl" 
            alt="profile" 
            @click="openImageModal"
            style="cursor: pointer;"
          />
        </template>
        <template v-else>
          <i class="ri-user-line avatar-placeholder-40"></i>
        </template>
        <div class="meta-col">
          <DefaultLabel :text="$t('hrm.userInfo.name') + ' / ' + $t('hrm.userInfoDetail.birthDate')" size="small" />
          <DefaultFormRow marginBottom="10px">
            <DefaultTextfield
              v-model="tempUser.name"
              :placeholder="$t('hrm.userInfo.name')"
              size="full"
              style="width:100%"
              margin-right="6px"
              :disabled="true"
            />
            <DefaultTextfield
              type="date"
              v-model="tempUser.birth"
              size="full"
              style="width:100%"
            />
          </DefaultFormRow>

          <DefaultLabel :text="$t('hrm.userInfo.email')" size="small" />
          <DefaultTextfield
            v-model="tempUser.email"
            :placeholder="$t('hrm.userInfo.email')"
            size="full"
            :disabled="true"
            style="width:100%"
          />
        </div>
      </DefaultFormRow>
      
      <!-- 고용형태, 입사일/퇴사일 -->
      <DefaultLabel
        :text="`${$t('hrm.userModifyMobile.employmentType')} / ${$t('hrm.userModifyMobile.status')}`"
        forId="employmentDetails"
        size="small"
      />
      <DefaultFormRow marginBottom="15px">
        <DefaultSelect
          v-model="tempUser.employmentTypeId"
          :options="[ 
            { value: 0, label: $t('hrm.userModifyMobile.unspecified') },
            { value: 1, label: '정규직' },
            { value: 2, label: '비정규직' }
          ]"
          size="full"
          style="width: 100%"
          margin-right="5px"
        />
        <DefaultSelect
          v-model="tempUser.status"
          :options="[
            { value: '입사', label: $t('hrm.userModifyMobile.join') },
            { value: '퇴사', label: $t('hrm.userModifyMobile.resign') }
          ]"
          size="full"
          style="width: 100%"
          margin-right="5px"
        />
        <DefaultTextfield
          type="date"
          v-model="computedDate"
          :disabled="false"
          size="full"
          style="width: 100%"
        />
      </DefaultFormRow>

      <!-- 부서, 팀, 직책 한 줄 -->
      <DefaultLabel :text="$t('hrm.userInfo.department') + '/' + $t('hrm.userInfo.team') + '/' + $t('hrm.userInfo.position')" forId="userEmploymentDetails" size="small" />
      <DefaultFormRow marginBottom="2px">
      <DefaultLabel :text="hrmTeamHint" forId="userEmploymentDetails" size="small" color="red" />
      </DefaultFormRow>
      <DefaultFormRow marginBottom="15px">
        <DefaultSelect
          v-model="tempUser.departmentId"
          :options="modalDepartmentsOptions"
          :placeholder="$t('hrm.userModifyMobile.teamSelect')"
          size="full"
          margin-right="5px"
          style="width: 100%"
        />
        <DefaultSelect
          v-model="tempUser.teamId"
          :options="modalTeamsOptions"
          :placeholder="placeholderValue"
          size="full"
          margin-right="5px"
          style="width: 100%"
          :disabled="isTeamDisabled"
        />
        <DefaultSelect
          v-model="tempUser.positionId"
          :options="modalPositionsOptions"
          :placeholder="$t('hrm.userInfo.position')"
          size="full"
          style="width: 100%"
        />
      </DefaultFormRow>

      <!-- 우편번호 및 주소 -->
      <DefaultLabel
        :text="`${$t('hrm.userModifyMobile.zipCode')} / ${$t('hrm.userModifyMobile.address')}`"
        forId="userZipCode"
        size="small"
      />
      <DefaultFormRow marginBottom="5px">
        <DefaultTextfield
          v-model="computedZipCode"
          :disabled="true"
          :placeholder="$t('hrm.userModifyMobile.zipCode')"
          size="small"
        />
      </DefaultFormRow>
      <DefaultFormRow marginBottom="15px">
        <DefaultTextfield
          v-model="computedAddress"
          :disabled="true"
          :placeholder="$t('hrm.userModifyMobile.address')"
          size="full"
          style="width: 100%"
        />
      </DefaultFormRow>

      <!-- 현재 파견지 -->
      <DefaultLabel :text="$t('hrm.userModifyMobile.dispatchLocation')" forId="dispatchLocations" size="small" />
      <DefaultFormRow marginBottom="30px">
        <DefaultTextfield
          id="dispatchLocations"
          v-model="tempUser.dispatchLocations"
          :placeholder="$t('hrm.userModifyMobile.dispatchLocation')"
          size="full"
          style="width: 100%"
        />
      </DefaultFormRow>
    </b-form>
    
    <DefaultFormRow marginBottom="5px" align="between">
      <DefaultFormRow>
        <DefaultButton
          v-if="isAdmin &&  tempUser.enabled"
          @click="onDeleteClick"
          color="red"
          marginRight="5px"
          size="small"
        >
          {{ $t('hrm.userManagement.disableButton') || $t('common.status.disabled') }}
        </DefaultButton>

        <DefaultButton
          v-if="isAdmin && !tempUser.enabled"
          @click="onEnableClick"
          color="blue"
          marginRight="5px"
          size="small"
        >
          {{ $t('hrm.userInfo.enableConfirmTitle') || $t('common.status.enabled') }}
        </DefaultButton>
        <DefaultButton
          size="small"
          @click="openPasswordModal"
          marginRight="5px"
        >
          {{ $t('hrm.userInfoDetail.changePassword') }}
        </DefaultButton>
      </DefaultFormRow>
      <DefaultFormRow align="right">
        <!-- 모달 푸터 -->
        <DefaultButton 
          @click="closeModal"
          color="gray"
          marginRight="5px"
          size="small"
        >
          {{ $t('common.button.close') }}
        </DefaultButton>
        
        <DefaultButton
          @click="onSaveClick"
          marginRight="5px"
          size="small"
        >
          {{ $t('common.button.save') }}
        </DefaultButton>
      </DefaultFormRow>
    </DefaultFormRow>
  </b-modal>

  <!-- 공통 이미지 확대 모달 -->
  <ImageZoomModal
    :visible="imageModalVisible"
    :imageUrl="selectedImageUrl"
    altText="프로필 이미지"
    @close="closeImageModal"
  />

  <UserPasswordModifyModal
    v-if="user && user.userId"
    :visible="isPasswordModalVisible"
    :userId="user.userId"
    :admin-mode="true"
    @close="closePasswordModal"
  />
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import UserPasswordModifyModal from '@/components/auth/UserPasswordModifyModal.vue';
import { cloneDeep } from 'lodash';
// import { isEqualWith } from 'lodash';
import ImageZoomModal from '@/components/common/image/ImageZoomModal.vue';

const props = defineProps({
  visible: { type: Boolean, default: false },
  isAdmin: { type: Boolean, default: false },
  user: {
    type: Object,
    default: () => ({
      name: '',
      email: '',
      password: '',
      departmentId: 0,
      teamId: 0,
      positionId: 0,
      joiningDate: '',
      leavingDate: '',
      status: '입사',
      birth: '',
      employmentTypeId: 0,
      zipCode: '',
      address: '',
      dispatchLocations: '',
      enabled: true
    })
  },
  departmentsOptions: { type: Array, default: () => [] },
  teamsOptions: { type: Array, default: () => [] },
  positionsOptions: { type: Array, default: () => [] },
  // 부서 원본 데이터 (부서의 teams 배열 등 포함)
  departmentsData: { type: Array, default: () => [] }
});

const emits = defineEmits([
  'close',
  'confirmSave',
  'confirmDelete',   // 비활성화
  'confirmEnable'    // 활성화
]);

const { t } = useI18n();
const hrmTeamHint = computed(() => t('hrm.userModifyMobile.teamHint'));

const placeholderValue = ref('팀 선택');

// b-modal 제어를 위한 내부 ref
const innerModalVisible = ref(props.visible);

// 모달 안에서 편집할 임시 데이터
const tempUser = ref({ ...props.user });

// 비밀번호 모달 제어용 상태 및 함수
const isPasswordModalVisible = ref(false);
function openPasswordModal() {
  isPasswordModalVisible.value = true;
}
function closePasswordModal() {
  isPasswordModalVisible.value = false;
}

// 이미지 확대 모달 제어용 상태 및 함수
const imageModalVisible = ref(false);
const selectedImageUrl = ref('');
function openImageModal() {
  selectedImageUrl.value = tempUser.value.profileImgUrl;
  imageModalVisible.value = true;
}
function closeImageModal() {
  imageModalVisible.value = false;
  selectedImageUrl.value = '';
}

// 닫기
function closeModal() {
  emits('close');
}

/**
 * 비활성화 버튼 클릭
 */
function onDeleteClick () {
  emits('confirmDelete', props.user);
}

/**
 * 활성화 버튼 클릭
 */
function onEnableClick () {
  emits('confirmEnable', props.user.userId);   // 부모로 userId 전달
}

watch(
  () => props.visible,
  (newVal, oldVal) => {
    // 항상 b-modal의 visible 상태를 동기화
    innerModalVisible.value = newVal;

    // 열릴 때만 (newVal === true)
    if (newVal === true && oldVal === false) {
      tempUser.value = cloneDeep(props.user);
      if (tempUser.value.teamId === 0) tempUser.value.teamId = '';
    // 고용형태 기본값 표준화: null/'' → 0
    if (tempUser.value.employmentTypeId === '' || tempUser.value.employmentTypeId == null) {
      tempUser.value.employmentTypeId = 0;
    } else {
      tempUser.value.employmentTypeId = Number(tempUser.value.employmentTypeId);
    }
      refreshTeamUI();
    }
  }
);

/* 추가: 팀 선택 불가 여부 계산 ───────────────────────────── */
const isTeamDisabled = computed(() => {
  // (a) 부서가 “미지정”이거나 선택 안 됐으면 비활성
  if (!tempUser.value.departmentId || Number(tempUser.value.departmentId) === 0) {
    return true;
  }
  // (b) 해당 부서에 팀이 1개도 없으면 비활성
  const dept = props.departmentsData.find(
    d => d.departmentId === Number(tempUser.value.departmentId)
  );
  return !dept || !dept.teams || dept.teams.length === 0;
});

watch(
  () => props.user,
  (user) => {
    tempUser.value = cloneDeep(user);
  },
  { immediate: true, deep: true }
);

/**
 * "부서"가 바뀌면 "팀"도 맞춰서 세팅
 */
watch(() => tempUser.value.departmentId, refreshTeamUI);

/**
 * "팀"이 바뀌면, "부서"가 없을 경우 해당 팀의 부서를 자동으로 설정
 */
watch(() => tempUser.value.teamId, (newTeamId) => {
  // 만약 teamId가 존재하는데 departmentId가 비어있거나 0이면, 부서 데이터를 조회해서 설정
  if (newTeamId && (!tempUser.value.departmentId || Number(tempUser.value.departmentId) === 0)) {
    const foundDept = props.departmentsData.find(dep =>
      dep.teams && dep.teams.some(t => Number(t.teamId) === Number(newTeamId))
    );
    if (foundDept) {
      tempUser.value.departmentId = foundDept.departmentId;
    }
  }
});

/**
 * b-modal이 닫혔을 때 부모로 close 이벤트
 */
watch(
  () => innerModalVisible.value,
  (newVal) => {
    if (!newVal) {
      emits('close');
    }
  }
);

/**
 * 입사/퇴사 상태가 바뀌면 날짜 세팅
 */
watch(() => tempUser.value.status, (newStatus) => {
  const today = new Date().toISOString().split('T')[0];

  if (newStatus === '퇴사') {
    tempUser.value.leavingDate = tempUser.value.leavingDate || today;
    tempUser.value.joiningDate = null;
  } else {
    tempUser.value.joiningDate = tempUser.value.joiningDate || today;
    tempUser.value.leavingDate = null;
  }
});

/**
 * 입사/퇴사 날짜를 표시하는 계산 속성
 */
const computedDate = computed({
  get() {
    const today = new Date().toISOString().split('T')[0];
    return tempUser.value.status === '입사'
      ? (tempUser.value.joiningDate || today)
      : (tempUser.value.leavingDate || today);
  },
  set(value) {
    if (tempUser.value.status === '입사') {
      tempUser.value.joiningDate = value;
      // 퇴사일은 자동으로 null 처리
      tempUser.value.leavingDate = null;
    } else {
      tempUser.value.leavingDate = value;
      // 입사일은 자동으로 null 처리
      tempUser.value.joiningDate = null;
    }
  }
});

const computedZipCode = computed({
  get: () => (tempUser.value.zipCode ? String(tempUser.value.zipCode) : ''),
  set: (value) => (tempUser.value.zipCode = value)
});

const computedAddress = computed({
  get: () => (tempUser.value.address ? String(tempUser.value.address) : ''),
  set: (value) => (tempUser.value.address = value)
});

// 부모가 이미 ‘미지정’을 포함해 주므로 그대로 사용
const modalDepartmentsOptions = computed(() => props.departmentsOptions);

const modalPositionsOptions = computed(() => {
  return [
    { value: 0, label: '미지정' },
    ...props.positionsOptions
  ];
});

/**
 * "팀" 목록은 부서 ID마다 달라지므로 별도 computed 유지
 */
const modalTeamsOptions = computed(() => {
  if (tempUser.value.departmentId && Number(tempUser.value.departmentId) > 0) {
    const dept = props.departmentsData.find(
      dep => Number(dep.departmentId) === Number(tempUser.value.departmentId)
    );
    return dept && dept.teams
      ? dept.teams.map(t => ({ value: t.teamId, label: t.teamName }))
      : [];
  }
  return [];
});

/* ───────────────── 공통 유틸 ───────────────── */
function refreshTeamUI () {
  // ① 부서가 지정돼 있는가?
  if (tempUser.value.departmentId && Number(tempUser.value.departmentId) > 0) {
    const dept = props.departmentsData.find(
      d => d.departmentId === Number(tempUser.value.departmentId)
    );
    const hasTeams = dept && dept.teams && dept.teams.length > 0;

    // 팀 존재 여부에 따라 placeholder 결정
    placeholderValue.value = hasTeams ? '팀 선택' : '팀 없음';

    // 선택돼 있던 teamId가 유효하지 않으면 초기화
    if (!hasTeams ||
        !dept.teams.some(t => t.teamId === Number(tempUser.value.teamId))) {
      tempUser.value.teamId = '';
    }
  } else {                    // ② 부서가 ‘미지정’일 때
    placeholderValue.value = '팀 선택';
    tempUser.value.teamId   = '';
  }
}


/**
 * buildPatchData:
 *  - (A) 원본 user (props.user)와
 *  - (B) 변경된 user (tempUser.value)를 비교해
 *  - 달라진 필드만 모아 반환합니다.
 */
function buildPatchData(originalUser, newUser) {
  const patch = {};

  if (originalUser.name !== newUser.name) {
    patch.name = newUser.name;
  }
  if (originalUser.email !== newUser.email) {
    patch.email = newUser.email;
  }
  if (originalUser.birth !== newUser.birth) {
    patch.birth = newUser.birth;
  }
  if (originalUser.employmentTypeId !== newUser.employmentTypeId) {
    patch.employmentTypeId = newUser.employmentTypeId;
  }
  if (originalUser.status !== newUser.status) {
    // 상태 변화에 따라 joiningDate/leavingDate도 함께 넣을지 결정
    patch.status = newUser.status;
  }
  if (originalUser.joiningDate !== newUser.joiningDate) {
    patch.joiningDate = newUser.joiningDate;
  }
  if (originalUser.leavingDate !== newUser.leavingDate) {
    patch.leavingDate = newUser.leavingDate;
  }

  // departmentId/teamId/positionId 비교
  if (originalUser.departmentId !== newUser.departmentId) {
    patch.departmentId = newUser.departmentId;
  }
  if (originalUser.teamId !== newUser.teamId) {
    patch.teamId = newUser.teamId;
  }
  if (originalUser.positionId !== newUser.positionId) {
    patch.positionId = newUser.positionId;
  }

  // address/zipCode/dispatchLocations
  if (originalUser.address !== newUser.address) {
    patch.address = newUser.address;
  }
  if (originalUser.zipCode !== newUser.zipCode) {
    patch.zipCode = newUser.zipCode;
  }
  if (originalUser.dispatchLocations !== newUser.dispatchLocations) {
    patch.dispatchLocations = newUser.dispatchLocations;
  }

  return patch;
}

/**
 * 저장 버튼 (b-modal의 @ok) 클릭 시
 */
function onSaveClick() {

  // - 비교전 전체 보내는 기존 로직
  // 원본과 비교해서 단순 데이터 변경 여부 체크 (departmentId 제외)
  // const isChanged = !deepEqualIgnoreType(filterUser(props.user), filterUser(tempUser.value));
  // 부모에게 저장 확인 이벤트
  // emits('confirmSave', {
  //   isMismatch: false,
  //   isChanged,
  //   userData: tempUser.value,
  //   // 상태 변경 여부 (입사/퇴사)
  //   statusChangedFlag: tempUser.value.status !== props.user.status
  // });


  // patch 데이터 생성 (원본 vs 변경)
  const patchData = buildPatchData(props.user, tempUser.value);
  patchData.userId = props.user.userId;

  /* ── ① 부서·팀(·직책) 값이 '' 이면 null 로 변환해서 전송 ── */
  ['departmentId', 'teamId'].forEach(key => {
    if (patchData[key] === '') patchData[key] = null;
  });

  // patchData 비어(유저 제외) 있으면 => "변경된 데이터가 없습니다."
  const isChanged = Object.keys(patchData).length > 1;

  if (!isChanged) {
    emits('confirmSave', {
      isMismatch: false,
      isChanged: false,
      userData: patchData,
      statusChangedFlag: false
    });
    return;
  }

  // 상태 변경 여부(입사/퇴사 등) 계산
  const statusChangedFlag = (tempUser.value.status !== props.user.status);

  // 부모에게 저장 확인 이벤트
  emits('confirmSave', {
    isMismatch: false,
    isChanged,
    userData: patchData,
    // 상태 변경 여부 (입사/퇴사)
    statusChangedFlag 
  });
}
</script>

<style scoped>
/* 모달 전체 배경, 다이얼로그, 내용 등에 대한 CSS */
/* (BootstrapVue의 .modal-* 클래스와 겹칠 수 있으므로 필요 없으면 제거해도 됩니다.) */

/* 비밀번호 불일치 경고 */
.userPasswordNotMatch {
  color: red;
  font-size: 0.7rem;
  min-height: 13px !important;
}
/* 폼 그룹(한 줄 표현) */
.user-detail-group {
  margin-bottom: 10px !important;
}
/* 우편번호 그룹 */
#userAddress {
  margin-bottom: 10px;
}

/* 반응형 모달 등은 BootstrapVue가 기본적으로 지원하므로
   직접 .modal, .modal-dialog, .modal-content 등을 커스텀할 필요가 적습니다.

   필요하다면 아래처럼 커스텀할 수 있음 (예시)
   .modal-dialog {
     max-width: 400px;
   }
*/
.user-meta-row {
  display: flex;
  align-items: center;
  gap: 15px;
}
.avatar-40 {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  margin: 20px 20px 20px 15px;
}
.avatar-placeholder-40{
  width:100px;
  height:100px;
  border-radius:50%;
  background:#f3f4f6;
  border:1px solid #e5e7eb;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size:50px;
  color:#6b7280;
  flex-shrink:0;
  margin-left: 10px;
  margin-right: 20px;
}
.meta-col{flex:1;}

@media (max-width: 650px) {
  .avatar-40 {
    width: 80px;
    height: 80px;
    margin-left: 10px;
    margin-right: 20px;
  }
  .avatar-placeholder-40{
    width:80px;
    height:80px;
    margin-left: 10px;
    margin-right: 20px;
  }
}
</style>
