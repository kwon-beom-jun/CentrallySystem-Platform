<template>
  <!-- Bootstrap-Vue Modal -->
  <b-modal
    ref="permissionModal"
    v-model="show"
    :title="$t('auth.permission.title')"
    :keyboard="false"
    :backdrop="'static'"
    centered
    hide-footer
    @hidden="notifyParent"
  >
    <!-- 사용자 검색 ------------------------------------------------ -->
    <DefaultFormRow marginBottom="5px">
      <DefaultLabel :text="$t('auth.permission.userSearch')" forId="userSearch" size="small" />
    </DefaultFormRow>
    <DefaultFormRow>
      <UserSearchDropdown
        inputId="userSearch"
        inputSize="full"
        style="width: 100%"
        :includeCurrentUser="true"
        :keepSearchValue="false"
        :placeholder="$t('auth.userSearch.placeholder')"
        @userSelected="onUserSelected"
      />
    </DefaultFormRow>

    <!-- 선택된 사용자 -------------------------------------------- -->
    <DefaultFormRow marginBottom="5px" marginTop="15px">
      <DefaultLabel :text="$t('auth.permission.selectedUser')" forId="selectedUser" size="small" />
    </DefaultFormRow>
    <DefaultFormRow marginBottom="15px">
      <DefaultTextfield
        type="text"
        id="selectedUser"
        v-model="selectedUserLabel"
        size="full"
        disabled
        style="width: 100%"
      />
    </DefaultFormRow>

    <!-- 서비스 선택 ---------------------------------------------- -->
    <DefaultFormRow marginBottom="5px">
      <DefaultLabel :text="$t('auth.permission.service')" forId="serviceSearch" size="small" />
    </DefaultFormRow>
    <DefaultFormRow marginBottom="15px">
      <DefaultSelect
        id="serviceSearch"
        v-model="selectedService"
        :options="serviceOptions"
        size="full"
        style="width: 100%"
        :placeholder="
          allServiceGranted
            ? $t('auth.permission.allServiceGranted')
            : $t('auth.permission.serviceSelect')
        "
        :disabled="!selectedUserId || allServiceGranted"
      />
    </DefaultFormRow>

    <!-- 권한 선택 -------------------------------------------------- -->
    <DefaultFormRow marginBottom="5px">
      <DefaultLabel :text="$t('auth.permission.role')" forId="permissionSearch" size="small" />
    </DefaultFormRow>
    <DefaultFormRow marginBottom="20px">
      <DefaultSelect
        id="permissionSearch"
        v-model="selectedRoleDetail"
        :options="roleOptionsForSelectedService"
        size="full"
        style="width: 100%"
        :placeholder="$t('auth.permission.roleSelect')"
        :disabled="!selectedService"
      />
    </DefaultFormRow>

    <!-- 버튼 영역 -------------------------------------------------- -->
    <DefaultFormRow marginBottom="5px" align="right">
      <DefaultButton
        size="small"
        color="gray"
        margin-right="5px"
        @click="closeModal"
      >
        {{ $t('common.button.cancel') }}
      </DefaultButton>
      <DefaultButton
        size="small"
        @click="addPermission"
      >
        {{ $t('common.button.add') }}
      </DefaultButton>
    </DefaultFormRow>
  </b-modal>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import UserSearchDropdown from '@/components/auth/UserSearchDropdown.vue';
import AuthUserPermissionApi from '@/api/auth/UserPermissionApi';
import { toast } from 'vue3-toastify';

const { t } = useI18n();

// 모달 이벤트 정의
defineProps({});
const emit = defineEmits(['close', 'permissionAdded']);
const show = ref(false);

// 선택값
const selectedUserId = ref('');
const selectedUserLabel = ref('');  // 새로 추가: 화면에 표시할 사용자 이름(이메일)
const selectedUserEmail = ref('');
const selectedService = ref('');
const selectedRoleDetail = ref('');

// 사용자 옵션, 서비스 옵션, 권한 옵션
const userOptions = ref([]);     // 전체 사용자 목록
// const serviceOptions = ref([]);  // 전체 서비스 목록
const allRoles = ref([]);        // 모든 권한 목록

// 모달 닫기
function closeModal () {     // ② 먼저 fade-out 을 트리거
  show.value = false
}

function notifyParent () {   // ③ ‘완전히 사라졌다’ 시점
  emit('close')
}

// 서비스가 선택되면 해당 서비스의 권한 목록만 필터링
const roleOptionsForSelectedService = computed(() => {
  if (!selectedService.value) return [];
  // 전체 roles 중에서, 현재 선택된 서비스만 추출
  let filtered = allRoles.value.filter(
    role => role.serviceName === selectedService.value
  );

  // "현재 선택된 유저"가 이미 가지고 있는 (serviceName, roleNameDetail) 찾기
  const user = userOptions.value.find(u => u.value === selectedUserId.value);
  if (user) {
    // 이 유저가 가진 권한 중 '현재 선택된 서비스'인 것만 뽑아오기
  const userHasForThisService = user.roles
      .filter(r => r.serviceName === selectedService.value)
      .map(r => r.roleName);

    // 이미 가진 권한은 목록에서 제외
    filtered = filtered.filter(
      r => !userHasForThisService.includes(r.roleNameDetail)
    );
  }

  return filtered.map(r => ({
    value: r.roleName, // enum 코드
    label: r.roleNameDetail
  }));
});

/** 사용자가 '모든' 서비스 권한을 이미 갖고 있는지 여부 */
const allServiceGranted = computed(() => {
  if (!selectedUserId.value) return false;   // 사용자 미선택

  // ① 전체 서비스 집합
  const allServices = [...new Set(allRoles.value.map(r => r.serviceName))];

  // ② 선택된 사용자의 서비스 집합
  const user = userOptions.value.find(u => u.value === selectedUserId.value);
  if (!user) return false;

  const owned = new Set((user.roles ?? []).map(r => r.serviceName));

  // ③ ‘전체 ⊆ 보유’ 여부
  return allServices.length > 0 &&
         owned.size === allServices.length;
});

// 서비스 선택이 변경되면 권한 선택값 초기화
watch(selectedService, () => {
  // 새 서비스 선택 시, 이전에 선택한 권한은 초기화합니다.
  selectedRoleDetail.value = '';
});

watch(selectedUserId, () => {
  // 유저가 바뀌면 서비스, 권한을 초기화
  selectedService.value = '';
  selectedRoleDetail.value = '';
});

function onUserSelected(option) {
  if (!option) return;
  selectedUserId.value = option.value;
  selectedUserLabel.value = option.label;
  selectedUserEmail.value = option.email;
  selectedUserLabel.value = `${option.name} (${option.email})`;
}

// 권한 추가
async function addPermission() {
  // userId, serviceName, roleName(코드)가 모두 있어야 함
  if (!selectedUserId.value || !selectedService.value || !selectedRoleDetail.value) {
    toast.error(t('auth.permission.selectAllItems'));
    return;
  }

  const params = {
    userId: selectedUserId.value,
    email:        selectedUserEmail.value,
    serviceName: selectedService.value,
    roleName: selectedRoleDetail.value
  };

  await AuthUserPermissionApi.createUserWithRole(params);
  toast.success(t('auth.permission.addSuccess'));
  emit('permissionAdded');
}

/**
 * serviceOptions를 computed로 선언
 * - selectedUserId가 없으면 전체를 그대로 보여주거나, 빈 배열로 처리
 * - user가 이미 가진 serviceName은 제외
 */
const serviceOptions = computed(() => {
  /** ① 전체 서비스 목록 */
  const allServiceNames = [
    ...new Set(allRoles.value.map(r => r.serviceName)),
  ];

  /** ② 선택한 사용자 */
  const user = userOptions.value.find(u => u.value === selectedUserId.value);
  if (!user) {
    // 사용자를 아직 선택하지 않았을 때 → 전체 서비스 노출
   return allServiceNames.map(s => ({ value: s, label: s }));
  }

  /** ③ 사용자가 이미 보유한 서비스 */
  const owned = new Set((user.roles ?? []).map(r => r.serviceName));

  /** ④ “아직 한 번도 권한을 가진 적 없는 사람”이면 → 전체 서비스 노출 */
  if (owned.size === 0) {
    return allServiceNames.map(s => ({ value: s, label: s }));
  }

  /** ⑤ 그 외: 미보유 서비스만 노출 */
  const remain = allServiceNames.filter(s => !owned.has(s));
  return remain.map(s => ({ value: s, label: s }));
});

// (3) 모달 오픈 시점에 사용자 목록, 전체 권한 목록 가져오기
onMounted(async () => {
  await nextTick();
  show.value = true;

  // 사용자 목록
  const response = await AuthUserPermissionApi.getUsersWithRoles();
  const userList = response.data.userWithRolesList || [];
  userOptions.value = userList.map(u => ({
    value: u.userId,
    label: `${u.name} (${u.email})`,
    email: u.email,
    roles: u.roles ?? []         // ← null/undefined 방지
  }));

  // 전체 권한 목록
  allRoles.value = response.data.authRoles.allRoles || [];

  // 서비스 옵션
  // const uniqueService = [...new Set(allRoles.value.map(r => r.serviceName))];
  // serviceOptions.value = uniqueService.map(svc => ({
  //   value: svc,
  //   label: svc
  // }));
});
</script>

<style scoped>
/* ─── 검색 결과 드롭다운 리스트 ───────────────────────────── */
.search-result {
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 4px;
  max-height: 150px;
  overflow-y: auto;
  margin: 0 0 15px 0;
  list-style: none;
  padding: 0;
}
.search-result li {
  padding: 8px 10px;
  cursor: pointer;
  font-size: 0.7rem;
}
.search-result li:hover {
  background-color: #f2f2f2;
}
</style>
