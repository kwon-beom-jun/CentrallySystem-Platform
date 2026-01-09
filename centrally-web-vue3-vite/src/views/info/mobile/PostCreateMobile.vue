<template>
  <div>
    <div class="content content-wrapper">
      <MobileDetailTitle :title="pageTitle" />

      <form class="form-pane modal-body" @submit.prevent="onSave">
        <div class="form-group cs-card--blue">
          <MobileMintLabel
            :text="$t('info.postMobile.labels.title')"
            forId="postTitle"
            size="small"
            :required="true"
            marginBottom="5px"
          />
          <MobileMintTextfield
            id="postTitle"
            v-model="localForm.title"
            :placeholder="$t('info.postMobile.placeholders.title')"
            size="full"
          />
        </div>

        <div class="form-group cs-card--blue" v-if="canAdmin">
          <MobileMintLabel
            :text="$t('info.postMobile.labels.visibilityScope')"
            forId="postScope"
            size="small"
            marginBottom="5px"
          />
          <MobileMintSelect
            id="postScope"
            v-model="localForm.visibilityScope"
            :options="visibilityOptions"
            size="full"
            style="width: 100%"
          />
        </div>

        <!-- 메일 발송 (등록 전용, 자유게시판 제외) -->
        <div
          class="form-group cs-card--blue"
          v-if="canAdmin && boardCode !== BOARD_CODE.COMMUNITY"
        >
          <div class="mail-card">
            <div class="mail-card__header">
              <div class="mail-card__title">
                <span class="dot"></span>
                <span>{{ $t('info.postMobile.labels.mailSend') }}</span>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="sendMail" />
                <span class="slider"></span>
              </label>
            </div>
            <div v-show="sendMail" class="mail-card__body">
              <div class="row-top">
                <div class="segmented">
                  <button
                    type="button"
                    :class="['seg-btn', mailTargetMode === 'all' && 'active']"
                    @click="mailTargetMode = 'all'"
                  >
                    {{ $t('info.postMobile.mail.all') }}
                  </button>
                  <button
                    type="button"
                    :class="['seg-btn', mailTargetMode === 'department' && 'active']"
                    @click="mailTargetMode = 'department'"
                  >
                    {{ $t('info.postMobile.mail.department') }}
                  </button>
                  <button
                    type="button"
                    :class="['seg-btn', mailTargetMode === 'team' && 'active']"
                    @click="mailTargetMode = 'team'"
                  >
                    {{ $t('info.postMobile.mail.team') }}
                  </button>
                </div>
                <div class="emp-chips">
                  <button
                    type="button"
                    :class="['chip', mailIncludeGuest && 'active']"
                    @click="toggleGuest"
                  >
                    {{ $t('info.postMobile.mail.guest') }}
                  </button>
                  <button
                    type="button"
                    :class="['chip', mailIncludeUser && 'active']"
                    @click="toggleUser"
                  >
                    {{ $t('info.postMobile.mail.user') }}
                  </button>
                  <button
                    type="button"
                    :class="['chip', mailIncludeManager && 'active']"
                    @click="toggleManager"
                  >
                    {{ $t('info.postMobile.mail.manager') }}
                  </button>
                  <button
                    type="button"
                    :class="['chip', mailIncludeAdmin && 'active']"
                    @click="toggleAdmin"
                  >
                    {{ $t('info.postMobile.mail.admin') }}
                  </button>
                </div>
              </div>

              <div v-show="mailTargetMode === 'department'" class="picker">
                <div class="picker-head">
                  <span class="picker-title">{{
                    $t('info.postMobile.mail.selectDepartment')
                  }}</span>
                  <span class="badge" v-if="selectedDepartmentIds.length">{{
                    selectedDepartmentIds.length
                  }}</span>
                </div>
                <div class="picker-body">
                  <div class="checklist">
                    <label
                      v-for="opt in departmentOptions"
                      :key="opt.value"
                      class="checkitem"
                    >
                      <input
                        type="checkbox"
                        :value="opt.value"
                        v-model="selectedDepartmentIds"
                      />
                      <span>{{ opt.label }}</span>
                    </label>
                  </div>
                </div>
              </div>

              <div v-show="mailTargetMode === 'team'" class="picker">
                <div class="picker-head">
                  <span class="picker-title">{{
                    $t('info.postMobile.mail.selectTeam')
                  }}</span>
                  <span class="badge" v-if="selectedTeamIds.length">{{
                    selectedTeamIds.length
                  }}</span>
                </div>
                <div class="picker-body">
                  <div class="checklist">
                    <label v-for="opt in teamOptions" :key="opt.value" class="checkitem">
                      <input
                        type="checkbox"
                        :value="opt.value"
                        v-model="selectedTeamIds"
                      />
                      <span>{{ opt.label }}</span>
                    </label>
                  </div>
                </div>
              </div>

              <div v-if="mailTargetMode !== 'all'" class="hint">
                {{ $t('info.postMobile.mail.multipleSelect') }}
              </div>
            </div>
          </div>
        </div>

        <div class="form-group cs-card--blue">
          <MobileMintLabel
            :text="$t('info.postMobile.labels.content')"
            forId="postContent"
            size="small"
            marginBottom="5px"
          />
          <QuillyEditor
            ref="quillRef"
            v-model="localForm.content"
            :options="editorOptions"
            class="border rounded editor-box"
          />
        </div>

        <div class="form-group cs-card--blue" v-if="canAdmin">
          <MobileMintLabel
            :text="$t('info.postMobile.labels.attachment')"
            size="small"
            marginBottom="5px"
          />
          
          <!-- 용량 안내 -->
          <div class="file-size-info" style="margin-bottom: 8px; font-size: 0.75rem; color: #6b7280;">
            <span>최대 20MB 이하</span>
            <span :style="{ marginLeft: '12px', color: isFileSizeExceeded ? '#dc3545 !important' : '#6b7280 !important' }">현재 용량: {{ formatFileSize(totalFileSize) }}MB</span>
          </div>
          
          <div class="file-select-area" role="button" @click="onSelectAreaClick">
            <i class="ri-attachment-2"></i>
            <span class="file-info-title">{{ $t('info.postMobile.file.select') }}</span>
            <span class="file-info-desc">{{
              $t('info.postMobile.file.multipleSelect')
            }}</span>
          </div>
          <input
            ref="fileInputRef"
            type="file"
            class="form-control file-input-hidden"
            multiple
            @change="handleFileChange"
          />
          <ul v-if="newFiles.length" class="attachment-list">
            <li
              v-for="(file, idx) in newFiles"
              :key="`n-${idx}-${file.name}`"
              class="attachment-item"
            >
              <IconBadge color="primary">신규</IconBadge>
              <span class="attachment-name">{{ file.name }}</span>
              <span class="file-size-text">({{ formatFileSize(file.size) }}MB)</span>
              <button
                type="button"
                class="btn btn-danger square-btn"
                @click="removeNewFile(idx)"
              >
                x
              </button>
            </li>
          </ul>
        </div>

        <DefaultFormRow align="right" marginTop="10px">
          <MobileMintButton color="gray" type="button" @click="goBack">{{
            $t('info.postMobile.buttons.goBack')
          }}</MobileMintButton>
          <MobileMintButton type="submit" style="margin-left: 8px">{{
            $t('info.postMobile.buttons.create')
          }}</MobileMintButton>
        </DefaultFormRow>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ROUTES } from '@/config/routeConfig';
import MobileDetailTitle from '@/components/common/title/MobileDetailTitle.vue';
import MobileMintLabel from '@/components/common/label/MobileMintLabel.vue';
import MobileMintTextfield from '@/components/common/textfield/MobileMintTextfield.vue';
import MobileMintSelect from '@/components/common/select/MobileMintSelect.vue';
import MobileMintButton from '@/components/common/button/MobileMintButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import IconBadge from '@/components/common/badge/IconBadge.vue';
import { QuillyEditor } from 'vue-quilly';
import Quill from 'quill';
import { toast } from 'vue3-toastify';
import { useToastStore } from '@/store/toast';
import PostApi from '@/api/info/PostApi';
import BoardApi from '@/api/info/BoardApi';
import { useAuthStore } from '@/store/auth';
import DepartmentApi from '@/api/hrm/DepartmentApi';
import TeamApi from '@/api/hrm/TeamApi';
import { getVisibilityOptions, BOARD_CODE } from '@/constants/infoConstants';

/**
 * 모바일 게시글 등록 페이지 (풀스크린)
 */

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const toastStore = useToastStore();
const { t } = useI18n();

const boardCode = ref('');
const boardId = ref(null);
const canAdmin = ref(false);

const localForm = ref({
  title: '',
  content: '',
  visibilityScope: 'ALL',
});

const newFiles = ref([]);
const visibilityOptions = getVisibilityOptions();

/**
 * 파일 용량을 MB로 변환 (소수점 2자리 올림)
 */
function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0.00';
  const mb = bytes / (1024 * 1024);
  return Math.ceil(mb * 100) / 100;
}

/**
 * 전체 첨부파일 용량 계산
 */
const totalFileSize = computed(() => {
  if (!newFiles.value || newFiles.value.length === 0) return 0;
  return newFiles.value.reduce((sum, file) => {
    return sum + (file.size || 0);
  }, 0);
});

/**
 * 최대 용량 초과 여부 (20MB = 20971520 bytes)
 */
const MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
const isFileSizeExceeded = computed(() => {
  return totalFileSize.value > MAX_FILE_SIZE;
});

// 메일 발송 상태
const sendMail = ref(false);
const mailTargetMode = ref('all');
const selectedDepartmentIds = ref([]);
const selectedTeamIds = ref([]);
const mailIncludeGuest = ref(true);
const mailIncludeUser = ref(true);
const mailIncludeManager = ref(true);
const mailIncludeAdmin = ref(true);
const departmentOptions = ref([]);
const teamOptions = ref([]);

const quillRef = ref(null);
const fileInputRef = ref(null);
const editorOptions = {
  theme: 'snow',
  modules: {
    toolbar: [
      [{ header: [1, 2, 3, false] }],
      ['bold', 'italic', 'underline', 'strike'],
      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ align: [] }],
      ['link', 'image', 'video'],
      ['clean'],
    ],
  },
};

const pageTitle = computed(() => {
  const titles = {
    NOTICE: t('info.postMobile.title.notice'),
    RESOURCE: t('info.postMobile.title.resource'),
    COMMUNITY: t('info.postMobile.title.community'),
  };
  return titles[boardCode.value] || t('info.postMobile.title.default');
});

/**
 * 파일 선택 변경 처리
 */
function handleFileChange(e) {
  const files = e.target.files;
  for (let i = 0; i < files.length; i++) newFiles.value.push(files[i]);
  e.target.value = '';
}

function removeNewFile(idx) {
  newFiles.value.splice(idx, 1);
}

function goBack() {
  router.back();
}

function onSelectAreaClick() {
  fileInputRef.value?.click?.();
}

function handleResizeRedirect() {
  if (window.innerWidth > 650) {
    const routes = {
      NOTICE: ROUTES.INFO.NOTICE,
      RESOURCE: ROUTES.INFO.RESOURCE,
      COMMUNITY: ROUTES.INFO.COMMUNITY,
    };
    router.replace(routes[boardCode.value] || ROUTES.INFO.NOTICE);
  }
}

function toggleGuest() {
  if (
    !mailIncludeGuest.value &&
    !mailIncludeUser.value &&
    !mailIncludeManager.value &&
    !mailIncludeAdmin.value
  )
    return;
  mailIncludeGuest.value = !mailIncludeGuest.value;
}

function toggleUser() {
  if (
    !mailIncludeGuest.value &&
    !mailIncludeUser.value &&
    !mailIncludeManager.value &&
    !mailIncludeAdmin.value
  )
    return;
  mailIncludeUser.value = !mailIncludeUser.value;
}

function toggleManager() {
  if (
    !mailIncludeGuest.value &&
    !mailIncludeUser.value &&
    !mailIncludeManager.value &&
    !mailIncludeAdmin.value
  )
    return;
  mailIncludeManager.value = !mailIncludeManager.value;
}

function toggleAdmin() {
  if (
    !mailIncludeGuest.value &&
    !mailIncludeUser.value &&
    !mailIncludeManager.value &&
    !mailIncludeAdmin.value
  )
    return;
  mailIncludeAdmin.value = !mailIncludeAdmin.value;
}

function resetMailSelections() {
  selectedDepartmentIds.value = [];
  selectedTeamIds.value = [];
}

function initializeMailPanel() {
  mailTargetMode.value = 'all';
  resetMailSelections();
  mailIncludeGuest.value = true;
  mailIncludeUser.value = true;
  mailIncludeManager.value = true;
  mailIncludeAdmin.value = true;
}

watch(
  () => mailTargetMode.value,
  () => {
    resetMailSelections();
  },
);
watch(
  () => sendMail.value,
  (isOn) => {
    if (isOn) initializeMailPanel();
    else resetMailSelections();
  },
);

/**
 * 저장 (등록)
 */
async function onSave() {
  if (!localForm.value.title?.trim())
    return toast.warning(t('info.postMobile.validation.titleRequired'));
  if (!localForm.value.content?.trim())
    return toast.warning(t('info.postMobile.validation.contentRequired'));

  // 첨부파일 용량 검증 (20MB)
  if (totalFileSize.value > MAX_FILE_SIZE) {
    return toast.warning(t('info.postMobile.validation.fileSizeExceeded'));
  }

  const formData = new FormData();
  const payload = { ...localForm.value };
  if (sendMail.value) {
    payload.sendMail = true;
    payload.mailIncludeGuest = mailIncludeGuest.value;
    payload.mailIncludeUser = mailIncludeUser.value;
    payload.mailIncludeManager = mailIncludeManager.value;
    payload.mailIncludeAdmin = mailIncludeAdmin.value;
    if (mailTargetMode.value === 'all') {
      payload.mailToAll = true;
      payload.departmentIds = [];
      payload.teamIds = [];
    } else if (mailTargetMode.value === 'department') {
      payload.mailToAll = false;
      payload.departmentIds = selectedDepartmentIds.value;
      payload.teamIds = [];
    } else if (mailTargetMode.value === 'team') {
      payload.mailToAll = false;
      payload.departmentIds = [];
      payload.teamIds = selectedTeamIds.value;
    }
  } else {
    payload.sendMail = false;
  }

  const json = new Blob([JSON.stringify(payload)], { type: 'application/json' });
  formData.append('post', json);
  newFiles.value.forEach((f) => formData.append('files', f));

  const res = await PostApi.createPost(boardId.value, formData);
  const newId = res?.data?.id;
  toastStore.setNextPageMessage(t('info.postMobile.success.create'), 'success');
  if (newId) {
    router.replace(
      ROUTES.INFO.POST_DETAIL + '?postId=' + newId + '&boardCode=' + boardCode.value,
    );
  } else {
    const routes = {
      NOTICE: ROUTES.INFO.NOTICE,
      RESOURCE: ROUTES.INFO.RESOURCE,
      COMMUNITY: ROUTES.INFO.COMMUNITY,
    };
    router.replace(routes[boardCode.value] || ROUTES.INFO.NOTICE);
  }
}

onMounted(async () => {
  handleResizeRedirect();
  window.addEventListener('resize', handleResizeRedirect);

  boardCode.value = route.query.boardCode || 'NOTICE';
  const boardResponse = await BoardApi.getBoardByCode(boardCode.value);
  boardId.value = boardResponse.data.id;

  const roles = authStore.getRoles || [];
  canAdmin.value =
    roles.includes('ROLE_INFO_ADMIN') ||
    (boardCode.value === 'RESOURCE' && roles.includes('ROLE_INFO_MANAGER')) ||
    (boardCode.value === 'COMMUNITY' && roles.includes('ROLE_INFO_USER'));

  quillRef.value?.initialize?.(Quill);

  Promise.all([DepartmentApi.getDepartments(), TeamApi.getTeams()]).then(
    ([deptRes, teamRes]) => {
      departmentOptions.value = (deptRes?.data || deptRes || []).map((d) => ({
        value: d.departmentId,
        label: d.departmentName,
      }));
      teamOptions.value = (teamRes?.data || teamRes || []).map((t) => ({
        value: t.teamId,
        label: t.teamName,
      }));
    },
  );
});

onBeforeUnmount(() => window.removeEventListener('resize', handleResizeRedirect));
</script>

<style scoped>
::deep(.modal) {
  display: block !important;
}
::deep(.modal-backdrop) {
  display: none !important;
}
::deep(.modal-dialog) {
  margin: 0;
  max-width: 100%;
  width: 100vw;
  height: calc(100vh - 130px);
}
::deep(.modal-content) {
  height: 100%;
  border-radius: 0;
}

.modal-body {
  padding-top: 15px;
  max-height: none !important;
  overflow-y: visible !important;
  background-color: #f9fafb !important;
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
.attachment-list {
  font-size: 0.8rem;
  margin-bottom: 0;
  padding: 10px;
}
.square-btn {
  width: 15px;
  height: 15px;
  padding: 0;
  line-height: 1;
  text-align: center;
  border-radius: 4px;
  font-size: 0.55rem;
}
.btn-danger.square-btn {
  color: #ffffff !important;
}
.editor-box {
  min-height: 220px;
}

.file-select-area {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px dashed #9ac9ff;
  background: #f7fbff;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
}
.file-select-area i {
  font-size: 16px;
  color: #3b82f6;
}
.file-info-title {
  font-weight: 600;
  font-size: 0.8rem;
}
.file-info-desc {
  color: #6b7280;
  font-size: 0.7rem;
  margin-left: auto;
}
.file-input-hidden {
  display: none;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #f1f5f9;
}
.attachment-item:last-child {
  border-bottom: none;
}
.attachment-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 6px;
  border-radius: 999px;
  font-size: 0.65rem;
}
.attachment-badge.new {
  background: #eef2ff;
  color: #3730a3;
}
.attachment-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-size-info {
  display: flex;
  align-items: center;
}
.file-size-text {
  margin-left: 6px;
  color: #6b7280;
  font-size: 0.75rem;
  white-space: nowrap;
}

/* 메일 카드 */
.mail-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px;
  background: #fff;
}
.mail-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mail-card__title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #374151;
  font-size: 0.8rem;
}
.dot {
  width: 8px;
  height: 8px;
  background: #3b82f6;
  border-radius: 50%;
}
.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}
.switch input {
  display: none;
}
.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #d1d5db;
  transition: 0.2s;
  border-radius: 999px;
}
.slider:before {
  position: absolute;
  content: '';
  height: 18px;
  width: 18px;
  left: 3px;
  top: 3px;
  background-color: white;
  transition: 0.2s;
  border-radius: 50%;
}
.switch input:checked + .slider {
  background-color: #3b82f6;
}
.switch input:checked + .slider:before {
  transform: translateX(20px);
}
.mail-card__body {
  margin-top: 10px;
  min-height: 40px;
}
.row-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.segmented {
  display: flex;
  gap: 6px;
}
.seg-btn {
  border: 1px solid #d1d5db;
  background: #f9fafb;
  color: #374151;
  border-radius: 999px;
  padding: 5px 9px;
  font-size: 0.7rem;
}
.seg-btn.active {
  background: #3b82f6;
  color: #fff;
  border-color: #3b82f6;
}
.emp-chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.chip {
  border: 1px solid #d1d5db;
  background: #fff;
  color: #374151;
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 0.7rem;
}
.chip.active {
  background: #10b981;
  border-color: #10b981;
  color: #fff;
}
.picker {
  margin-top: 12px;
}
.picker-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.picker-title {
  font-weight: 600;
  color: #374151;
  font-size: 0.75rem;
}
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  background: #eef2ff;
  color: #3730a3;
  border-radius: 999px;
  font-size: 0.65rem;
}
.picker-body {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px;
  max-height: 180px;
  overflow: auto;
}
.checklist {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 6px;
}
.checkitem {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: #374151;
}
.hint {
  margin-top: 8px;
  color: #6b7280;
  font-size: 0.65rem;
}
</style>
