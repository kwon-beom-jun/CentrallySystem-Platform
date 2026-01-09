<template>
  <div class="content content-wrapper">
    <PageTitle 
      :title="$t('hrm.deptTeamDetail.title')"
      :subtitle="$t('hrm.deptTeamDetail.subtitle')"
      icon="ri-building-line"
    />

    <!-- <p class="department-coment"></p> -->
    <DefaultLabel :text="$t('hrm.deptTeamDetail.addNewDepartment')" forId="userSearch" size="small" />
    <!-- 부서 추가 -->
    <div class="d-flex justify-content-between mt-4 department-search-line" style="width: 100%;">
      <div class="d-flex">
        <DefaultTextfield
          type="text"
          v-model="newDepartment"
          size="medium"
          :placeholder="$t('hrm.deptTeamDetail.departmentName')"
        />
        <!-- 저장 버튼 -->
        <DefaultButton 
          @click="addDepartment"
          align="left"
          color="gray"
          size="large">
          {{ $t('hrm.deptTeamDetail.addDepartment') }}
        </DefaultButton>
      </div>
    </div>

    <hr />

    <!-- 부서 및 팀 관리 아코디언 -->
    <div id="departmentAccordion" class="department-list">
      <div class="accordion" v-for="(department, index) in departments" :key="index">
        <div class="accordion-item box-shadow-gray">
          <h2 class="accordion-header" :id="'heading' + index">
            <button 
              class="accordion-button collapsed" type="button" 
              data-bs-toggle="collapse"
              :data-bs-target="'#collapse' + index" 
              aria-expanded="false" 
              :aria-controls="'collapse' + index" 
              @click="selectDepartment(index)">
              <strong>{{ department.departmentName }}</strong>
            </button>
            <button class="btn btn-sm btn-outline-primary ms-2 bnt-department-replace" @click="openEditDepartmentModal(index)">
              {{ $t('hrm.deptTeamDetail.editDepartment') }}
            </button>
            <button class="btn btn-sm btn-outline-danger ms-2 bnt-department-delete" @click="openDeleteDepartmentModal(index)">
              {{ $t('hrm.deptTeamDetail.deleteDepartment') }}
            </button>
          </h2>
          <div 
            :id="'collapse' + index" 
            class="accordion-collapse collapse" 
            :aria-labelledby="'heading' + index"
            data-bs-parent="#departmentAccordion"
          >
            <div class="accordion-body">
              <ul class="list-group">
                <li v-for="(team, teamIndex) in department.teams" :key="teamIndex"
                  class="list-group-item d-flex justify-content-between align-items-center">
                  {{ team.teamName }}
                  <span>
                    <button class="btn btn-sm btn-outline-primary me-2 list-item" @click="openEditTeamModal(index, teamIndex)">
                      {{ $t('hrm.deptTeamDetail.edit') }}
                    </button>
                    <button class="btn btn-sm btn-outline-danger list-item" @click="openDeleteTeamModal(index, teamIndex)">
                      {{ $t('hrm.deptTeamDetail.delete') }}
                    </button>
                  </span>
                </li>
              </ul>

              <!-- 팀 추가 -->
              <div class="btn-team-add-contain">
                <input v-model="newTeam" class="form-control btn-team-add" :placeholder="$t('hrm.deptTeamDetail.addNewTeam')" />
                <!-- 저장 버튼 -->
                <DefaultButton 
                  align="right"
                  size="small"
                  @click="addTeam(index)">
                  {{ $t('hrm.deptTeamDetail.save') }}
                </DefaultButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 부서 삭제 모달 -->
    <AlertModal 
      :isVisible="confirmationDepartmentModalVisible" 
      :disableBackgroundClose="true"
      :cancelText="$t('common.button.cancel')"
      :confirmText="$t('common.button.confirm')" 
      @close="confirmationDepartmentModalVisible = false" 
      @confirm="deleteDepartment">
      <template #body>
        <p style="color: red;"><strong>{{ $t('hrm.deptTeamDetail.deleteDepartmentWarning') }}</strong></p>
        <div style="color: red;">{{ $t('hrm.deptTeamDetail.deleteDepartmentMessage') }}</div>
      </template>
    </AlertModal>

    <!-- 팀 삭제 모달 -->
    <AlertModal 
      :isVisible="confirmationTeamModalVisible" 
      :disableBackgroundClose="true"
      :cancelText="$t('common.button.cancel')"
      :confirmText="$t('common.button.confirm')" 
      @close="confirmationTeamModalVisible = false" 
      @confirm="deleteTeam">
      <template #body>
        <p style="color: red;"><strong>{{ $t('hrm.deptTeamDetail.deleteTeamWarning') }}</strong></p>
        <div style="color: red;">{{ $t('hrm.deptTeamDetail.deleteTeamMessage') }}</div>
      </template>
    </AlertModal>

    <!-- 팀 수정 모달 -->
    <EditModal 
      :title="$t('hrm.deptTeam.editTeam')"
      :isVisible="editTeamModalVisible" 
      :disableBackgroundClose="true"
      v-model:value="editingTeamName"
      :confirmText="$t('common.button.confirm')"
      :cancelText="$t('common.button.cancel')"
      @close="editTeamModalVisible = false"
      @confirm="updateTeamConfirmed"
    ></EditModal>

    <!-- 저장 완료 여부 -->
    <AlertModal 
      :isVisible="isModalVisible" 
      :disableBackgroundClose="true"
      :title="modalTitle" 
      confirmText="확인" 
      @confirm="isModalVisible = false">
      <template #body>
        {{ modalMessage }}
      </template>
    </AlertModal>

    <EditModal 
      :title="$t('hrm.deptTeam.editDepartment')"
      :isVisible="editDepartmentModalVisible"
      v-model:value="editingDepartmentName"
      :disableBackgroundClose="true"
      :confirmText="$t('common.button.confirm')"
      :cancelText="$t('common.button.cancel')"
      @close="editDepartmentModalVisible = false"
      @confirm="updateDepartmentConfirmed"
    />

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import EditModal from '@/components/common/modal/EditModal.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DepartmentApi from "@/api/hrm/DepartmentApi";
import { toast } from 'vue3-toastify';

const { t } = useI18n();

// 부서 및 팀 데이터
const departments = ref([]);
const newDepartment = ref('');
const newTeam = ref('');
const editingTeamName = ref('');
let editingDeptId = null;
let editingTeamId = null;

// 부서 수정
const editDepartmentModalVisible = ref(false);
const editingDepartmentName = ref(''); 

// 모달 표시를 위한 상태 변수
const isModalVisible = ref(false);
const modalTitle = ref('');
const modalMessage = ref('');

const confirmationDepartmentModalVisible = ref(false);
const confirmationTeamModalVisible = ref(false);
const selectedDepartmentIndex = ref(null);
const selectedTeamIndex = ref(null);

// 수정 모달
const editTeamModalVisible = ref(false);

// 부서 삭제 오픈 모달 함수
const openDeleteDepartmentModal = (index) => {
  selectedDepartmentIndex.value = index;
  confirmationDepartmentModalVisible.value = true;
};

/** 부서 추가 */
async function addDepartment() {
  if (!newDepartment.value.trim()) return;
  
  const response = await DepartmentApi.createDepartment(newDepartment.value);
  console.log(response);
  toast.success(t('hrm.deptTeamDetail.departmentCreateSuccess'));
  newDepartment.value = '';
  fetchMetadata(); // 목록 재조회
}

/** 부서 삭제 */
async function deleteDepartment() {
  if (selectedDepartmentIndex.value === null) return;
  // 선택된 부서 인덱스를 통해 부서 객체에서 departmentId 추출
  const departmentId = departments.value[selectedDepartmentIndex.value].departmentId;

  await DepartmentApi.deleteDepartment(departmentId);
  
  confirmationDepartmentModalVisible.value = false
  toast.success(t('hrm.deptTeamDetail.departmentDeleteSuccess'));
  fetchMetadata();
}

/** 부서 수정 모달 열기 */
function openEditDepartmentModal(index) {
  selectedDepartmentIndex.value = index;
  editingDeptId = departments.value[index].departmentId;
  editingDepartmentName.value = departments.value[index].departmentName;
  editDepartmentModalVisible.value = true;
}

/** 부서 이름 수정 요청 */
async function updateDepartmentConfirmed() {
  if (!editingDepartmentName.value.trim()) return;
  
  await DepartmentApi.updateDepartment(editingDeptId, editingDepartmentName.value);

  toast.success(t('hrm.deptTeamDetail.departmentUpdateSuccess'));
  editDepartmentModalVisible.value = false;
  fetchMetadata();
}

/** 팀 추가 */
async function addTeam(deptIndex) {
  if (!newTeam.value.trim()) return;
  // 실제 부서 ID 추출
  const departmentId = departments.value[deptIndex].departmentId;

  await DepartmentApi.createTeam(departmentId, newTeam.value);

  toast.success(t('hrm.deptTeamDetail.teamCreateSuccess'));
  newTeam.value = '';
  fetchMetadata();
}

/** 팀 삭제 */
async function deleteTeam() {
  if (selectedDepartmentIndex.value === null || selectedTeamIndex.value === null) return;
  // 실제 부서 ID와 팀 ID 추출
  const departmentId = departments.value[selectedDepartmentIndex.value].departmentId;
  const teamId = departments.value[selectedDepartmentIndex.value].teams[selectedTeamIndex.value].teamId;

  confirmationTeamModalVisible.value = false;

  await DepartmentApi.deleteTeam(departmentId, teamId);
  
  toast.success(t('hrm.deptTeamDetail.teamDeleteSuccess'));
  fetchMetadata();
    
}

/** 팀 수정 (모달) 열기 */
function openEditTeamModal(deptIndex, teamIndex) {
  selectedDepartmentIndex.value = deptIndex;
  selectedTeamIndex.value = teamIndex;
  // 실제 부서 ID와 팀 ID도 저장
  editingDeptId = departments.value[deptIndex].departmentId;
  editingTeamId = departments.value[deptIndex].teams[teamIndex].teamId;
  editingTeamName.value = departments.value[deptIndex].teams[teamIndex].teamName;
  editTeamModalVisible.value = true;
}

/** 팀 수정 (모달 확인 버튼) */
async function updateTeamConfirmed() {
  if (!editingTeamName.value.trim()) return;

  await DepartmentApi.updateTeam(editingDeptId, editingTeamId, editingTeamName.value);

  toast.success(t('hrm.deptTeamDetail.teamUpdateSuccess'));
  editTeamModalVisible.value = false;
  fetchMetadata();
}

// 팀 삭제 확인 모달 오픈 함수
const openDeleteTeamModal = (departmentIndex, teamIndex) => {
  selectedDepartmentIndex.value = departmentIndex;
  selectedTeamIndex.value = teamIndex;
  confirmationTeamModalVisible.value = true;
};

// 새 부서를 열 때마다 newTeam 초기화
// function selectDepartment(index) {
function selectDepartment() {
  // 부서 바꿀 때마다 팀 입력 창을 비움
  newTeam.value = '';
  // 필요하다면 selectedDepartmentIndex.value = index;
}

// -------------------------------------------------------
//   onMounted 시점에 부서/ 팀 데이터 호출
// -------------------------------------------------------
async function fetchMetadata() {
  // 부서/팀 정보
  const departmentRes = await DepartmentApi.getDepartments();
  departments.value = departmentRes.data;
}

onMounted(async () => {
  await fetchMetadata();
});
</script>


<style scoped>
button, input {
  font-weight: 500 !important;
}

.accordion-item {
  margin-bottom: 1rem;
}

.department-coment {
  margin: 0 0 10px 3px !important;
  font-weight: 500;
}

.department-input {
  margin: 0;
}

.list-group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.accordion-button {
  font-size: 0.9rem !important;
}

.list-group-item,
.list-item,
.bnt-department-delete, 
.bnt-department-replace {
  font-size: 0.8rem !important;
}

.btn-team-add {
  font-size: 0.8rem !important;
  height: 38px !important;
}

.mt-5 {
  margin: 0px !important;
}

.accordion-header {
  height: 80px;
  margin-bottom: 10px;
}

.accordion-button {
  padding: 10px !important;
  margin-bottom: 5px;
}

hr {
  margin-top: 0px;
}

input {
  margin-right: 5px;
}

.department-search-line {
  margin: 0 0 10px 0 !important;
}

.accordion-body {
  padding-top: 5px;
}

/* 스크롤 가능하도록 설정 */
.department-list {
  max-height: 500px; /* 스크롤 가능한 높이 설정 */
  /* overflow-y: auto; */
  padding-right: 10px;
  overflow-y: scroll;
}

.btn-team-add-contain {
  margin-top: 10px;
}

.btn-team-add {
  margin-bottom: 5px;
}

@media (max-width: 650px) {
  .accordion-button {
    font-size: 0.75rem !important;
  }

  .accordion-header {
    margin-bottom: 3px;
  }

  .list-group-item, .list-item {
    font-size: 0.65rem !important;
  }
  
  .btn-team-add {
    font-size: 0.65rem !important;
    height: 35px !important;
  }

  .bnt-department-delete, .bnt-department-replace {
    font-size: 0.65rem !important;
    /* padding: 5px !important; */
  }

  .align-items-center {
    margin-bottom: 7px !important;
    padding: 5px 10px 5px 10px;
  }

  .mt-3 {
    margin-top: 0px !important;
  }

  .btn-team-add-contain {
    margin-top: 2px;
  }

  .btn-team-add {
    margin-bottom: 5px !important;
  }
}

@media (max-width: 410px) {
  .input-group {
    width: 100px !important;
    min-width: 140px !important;
  }
}
</style>
