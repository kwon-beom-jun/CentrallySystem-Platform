<template>
  <!-- 부트스트랩 모달 -->
  <b-modal
    ref="postModal"
    style="--bs-modal-width:1000px"
    v-model="innerVisible"
    :title="boardTitle"
    :keyboard="false"
    :backdrop="'static'"
    centered
    fade
    hide-footer
    @hide="onHideModal"
  >

    <!-- 모달 본문 -->
    <form>
      <div v-show="headerVisible">
        <div class="layer">
          <DefaultLabel :text="$t('info.postModal.labels.title')" forId="postTitle" size="small" :required="true" />
          <DefaultTextfield
            type="text"
            id="postTitle"
            v-model="localForm.title" 
            size="full"
            :disabled="!props.isAdmin" 
            :placeholder="$t('info.postModal.placeholders.title')"
          />
        </div>
        <!-- 열람 범위 (관리자만 설정) -->
        <div class="layer" v-if="props.isAdmin">
          <DefaultLabel :text="$t('info.postModal.labels.visibilityScope')" forId="postScope" size="small" />
          <DefaultSelect
            id="postScope"
            v-model="localForm.visibilityScope"
            :options="visibilityOptions"
            size="full"
            style="width: 100%;"
          />
        </div>

        <!-- 메일 발송 설정 (등록 모드에서만, 자유게시판 제외) -->
        <div class="layer" v-if="props.isAdmin && props.isCreate && !props.hideMailSection">
          <div class="mail-card">
            <div class="mail-card__header">
              <div class="mail-card__title">
                <span class="dot"></span>
                <span>{{ $t('info.postModal.labels.mailSend') }}</span>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="sendMail" />
                <span class="slider"></span>
              </label>
            </div>
            <div v-show="sendMail" class="mail-card__body">
              <div class="row-top">
                <div class="segmented">
                  <button type="button" :class="['seg-btn', mailTargetMode === 'all' && 'active']" @click="mailTargetMode = 'all'">{{ $t('info.postMobile.mail.all') }}</button>
                  <button type="button" :class="['seg-btn', mailTargetMode === 'department' && 'active']" @click="mailTargetMode = 'department'">{{ $t('info.postMobile.mail.selectDepartment') }}</button>
                  <button type="button" :class="['seg-btn', mailTargetMode === 'team' && 'active']" @click="mailTargetMode = 'team'">{{ $t('info.postMobile.mail.selectTeam') }}</button>
                </div>
                <div class="emp-chips">
                  <button type="button" :class="['chip', mailIncludeGuest && 'active']" @click="toggleGuest">{{ $t('info.postMobile.mail.guest') }}</button>
                  <button type="button" :class="['chip', mailIncludeUser && 'active']" @click="toggleUser">{{ $t('info.postMobile.mail.user') }}</button>
                  <button type="button" :class="['chip', mailIncludeManager && 'active']" @click="toggleManager">{{ $t('info.postMobile.mail.manager') }}</button>
                  <button type="button" :class="['chip', mailIncludeAdmin && 'active']" @click="toggleAdmin">{{ $t('info.postMobile.mail.admin') }}</button>
                </div>
              </div>
              
              <div v-show="mailTargetMode === 'department'" class="picker">
                <div class="picker-head">
                  <span class="picker-title">{{ $t('info.postMobile.mail.selectDepartment') }}</span>
                  <span class="badge" v-if="selectedDepartmentIds.length">{{ selectedDepartmentIds.length }}</span>
                </div>
                <div class="picker-body">
                  <div class="checklist">
                    <label v-for="opt in departmentOptions" :key="opt.value" class="checkitem">
                      <input type="checkbox" :value="opt.value" v-model="selectedDepartmentIds" />
                      <span>{{ opt.label }}</span>
                    </label>
                  </div>
                </div>
              </div>

              <div v-show="mailTargetMode === 'team'" class="picker">
                <div class="picker-head">
                  <span class="picker-title">{{ $t('info.postMobile.mail.selectTeam') }}</span>
                  <span class="badge" v-if="selectedTeamIds.length">{{ selectedTeamIds.length }}</span>
                </div>
                <div class="picker-body">
                  <div class="checklist">
                    <label v-for="opt in teamOptions" :key="opt.value" class="checkitem">
                      <input type="checkbox" :value="opt.value" v-model="selectedTeamIds" />
                      <span>{{ opt.label }}</span>
                    </label>
                  </div>
                </div>
              </div>

              <div v-if="mailTargetMode !== 'all'" class="hint">{{ $t('info.postMobile.mail.multipleSelect') }}</div>
            </div>
          </div>
        </div>


        <DefaultFormRow marginBottom="10px" customClass="two-col">
          <div class="col-wrapper">
            <DefaultLabel :text="$t('info.postModal.labels.author')" forId="postAuthor" size="small" />
            <DefaultTextfield
            type="text"
            id="postAuthor"
            v-model="localForm.author" 
            size="full"
            disabled
            :placeholder="$t('info.postModal.placeholders.author')"
            marginRight="5px"
            />
          </div>
          <div class="col-wrapper">
            <DefaultLabel :text="$t('info.postModal.labels.date')" forId="postDate" size="small" />
            <DefaultTextfield
            type="text"
            id="postDate"
            :placeholder="localForm.date" 
            size="full"
            disabled
            />
          </div>
        </DefaultFormRow>

        <!-- (관리자) -->
        <div class="layer" v-if="props.isAdmin">

          <!-- 새 파일 추가 -->
          <DefaultLabel :text="$t('info.postModal.labels.newFile')" size="small" />
          <input 
            type="file"
            class="form-control"
            multiple
            @change="handleFileChange"
          />

          <DefaultFormRow marginTop="10px" marginBottom="5px">
            <DefaultLabel :text="$t('info.postModal.labels.existingAttachments')" size="small" marginBottom="0"/>
            <span :class="['file-size-info-inline', { 'file-size-exceeded': isFileSizeExceeded }]">[최대 20MB 이하 / 현재 용량: {{ formatFileSize(totalFileSize) }}MB]</span>
          </DefaultFormRow>

          <!-- 기존 첨부파일 목록 (수정 모드) -->
          <div v-if="localForm.attachments && localForm.attachments.length > 0">
            <ul class="attachment-list">
              <li
                v-for="(attachment, index) in localForm.attachments"
                :key="attachment.id"
              >
                <IconBadge color="success">기존</IconBadge>
                <span>{{ attachment.fileName }}</span>
                <span v-if="attachment.fileSize" class="file-size-text">({{ formatFileSize(attachment.fileSize) }}MB)</span>
                <!-- x 버튼: 삭제 -->
                <button 
                  type="button"
                  class="btn btn-danger square-btn ms-2"
                  @click="removeExistingFile(index, attachment.id)"
                >
                  x
                </button>
              </li>
            </ul>
          </div>
          <div v-else>
            <!-- 기존 첨부파일 없음 -->
          </div>

          <!-- 새로 첨부할 파일 목록 미리보기 -->
          <div v-if="newFiles.length > 0">
            <ul class="attachment-list">
              <li
                v-for="(file, idx) in newFiles"
                :key="idx"
              >
                <IconBadge color="primary">신규</IconBadge>
                <span>{{ file.name }}</span>
                <span class="file-size-text">({{ formatFileSize(file.size) }}MB)</span>
                <button 
                  type="button"
                  class="btn btn-danger square-btn ms-2"
                  @click="removeNewFile(idx)"
                >
                  x
                </button>
              </li>
            </ul>
          </div>
        </div>

        <!-- (관리자 아님) -->
        <div class="layer" v-else>
          <DefaultLabel :text="$t('info.postModal.labels.attachment')" size="small" />
          <div v-if="localForm.attachments && localForm.attachments.length > 0">
            <ul class="attachment-list">
              <li
                v-for="attachment in localForm.attachments"
                :key="attachment.id"
              >
                {{ attachment.fileName }}
              </li>
            </ul>
          </div>
          <div v-else>
            <span>{{ $t('info.postModal.labels.noAttachment') }}</span>
          </div>
        </div>
      </div>



      <!-- ───── 실선 + 중앙 버튼 ───── -->
      <div
        class="collapse-divider"
        :class="{ open: !headerVisible }"
        @click="toggleHeader"
      >
        <span class="divider-btn">
          {{ headerVisible ? $t('info.postModal.buttons.collapse') : $t('info.postModal.buttons.expand') }}
        </span>
      </div>



      <div class="layer">
        <DefaultLabel :text="$t('info.postModal.labels.content')" forId="postContent" size="small" />
        <QuillyEditor
          ref="quillRef"
          v-model="localForm.content"
          :options="editorOptions"
          class="border rounded editor-box"
          :style="{ height: editorHeight }"
        />
      </div>
    </form>
    
    <DefaultFormRow marginBottom="5px" align="right">
      <!-- 모달 푸터 -->
      <DefaultButton 
        @click="closeModal"
        color="gray"
        marginRight="5px"
        size="small"
      >
        {{ $t('info.postModal.buttons.close') }}
      </DefaultButton>
      
      <DefaultButton
        v-if="props.isAdmin && props.isCreate"
        @click="savePost"
        marginRight="5px"
        size="small"
      >
        {{ $t('info.postModal.buttons.create') }}
      </DefaultButton>

      <DefaultButton
        v-if="props.isAdmin && !props.isCreate"
        @click="savePost"
        marginRight="5px"
        size="small"
      >
        {{ $t('info.postModal.buttons.edit') }}
      </DefaultButton>
    </DefaultFormRow>
  </b-modal>
  <AlertModal
    :isVisible="closeConfirmVisible"
    :disableBackgroundClose="true"
    :title="$t('info.postModal.confirm.closeTitle')"
    :confirmText="$t('common.button.confirm')"
    :cancelText="$t('common.button.cancel')"
    @close="closeConfirmVisible = false"
    @confirm="reallyClose"
  >
    <template #body>
      {{ $t('info.postModal.confirm.closeMessage') }}
    </template>
  </AlertModal>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch, onMounted, onBeforeUnmount, nextTick, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';
import IconBadge from '@/components/common/badge/IconBadge.vue';
import PostApi from '@/api/info/PostApi';
import DepartmentApi from '@/api/hrm/DepartmentApi';
import TeamApi from '@/api/hrm/TeamApi';
import { cloneDeep } from 'lodash';
import { toast } from 'vue3-toastify';
import Quill from 'quill';
import { QuillyEditor } from 'vue-quilly';
import { getVisibilityOptions, BOARD_CODE } from '@/constants/infoConstants';

const { t } = useI18n();

const props = defineProps({
  isAdmin: Boolean,
  isCreate: Boolean,
  isVisible: Boolean,
  boardCode: String,
  boardId: Number,
  form: Object,
  hideMailSection: { type: Boolean, default: false },  // 메일 발송 섹션 숨김 여부
});

const MAX_BYTES = 5 * 1024 * 1024;   // 5 MB

const emit = defineEmits(['close', 'save']);

const innerVisible = ref(false);
const skipPopstateOnce = ref(false);
const historyPushed = ref(false);

const isMobile = ref(window.innerWidth <= 650);
function handleResize () { isMobile.value = window.innerWidth <= 650; }
onMounted(()   => window.addEventListener('resize', handleResize));
onBeforeUnmount(() => window.removeEventListener('resize', handleResize));

/**
 * 모바일로 전환 시 모달 자동 닫기
 */
watch(() => isMobile.value, (newVal) => {
  if (newVal && innerVisible.value) {
    emit('close');
  }
});

const closeConfirmVisible = ref(false);
const headerVisible = ref(true);

const localForm = ref({});
const newFiles = ref([]);
const deletedFileIds = ref([]);
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
  let total = 0;
  
  // 기존 첨부파일 용량 합계
  if (localForm.value.attachments && localForm.value.attachments.length > 0) {
    total += localForm.value.attachments.reduce((sum, att) => {
      return sum + (att.fileSize || 0);
    }, 0);
  }
  
  // 새로 추가한 파일 용량 합계
  if (newFiles.value && newFiles.value.length > 0) {
    total += newFiles.value.reduce((sum, file) => {
      return sum + (file.size || 0);
    }, 0);
  }
  
  return total;
});

/**
 * 최대 용량 초과 여부 (20MB = 20971520 bytes)
 */
const MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
const isFileSizeExceeded = computed(() => {
  return totalFileSize.value > MAX_FILE_SIZE;
});

const mailTargetMode = ref('all');

// 메일 발송 UI 상태
const sendMail = ref(false);
const selectedDepartmentIds = ref([]);
const selectedTeamIds = ref([]);
const mailIncludeGuest = ref(true);
const mailIncludeUser = ref(true);
const mailIncludeManager = ref(true);
const mailIncludeAdmin = ref(true);
const departmentOptions = ref([]);
const teamOptions = ref([]);

// 게시판 타이틀
const boardTitle = computed(() => {
  const titles = {
    'NOTICE': t('info.postModal.title.notice'),
    'RESOURCE': t('info.postModal.title.resource'),
    'COMMUNITY': t('info.postModal.title.community')
  };
  return titles[props.boardCode] || t('info.postModal.title.default');
});

function closeModal() {
  askBeforeClose();
}

function askBeforeClose () {
  if (!closeConfirmVisible.value) closeConfirmVisible.value = true;
}

function reallyClose() {
  closeConfirmVisible.value = false;
  emit('close');
}

function handleFileChange(e) {
  const files = e.target.files;
  for (let i = 0; i < files.length; i++) {
    newFiles.value.push(files[i]);
  }
  e.target.value = '';
}

function removeExistingFile(idx, attachmentId) {
  localForm.value.attachments.splice(idx, 1);
  deletedFileIds.value.push(attachmentId);
}

function removeNewFile(idx) {
  newFiles.value.splice(idx, 1);
}

function openModal() {
  innerVisible.value = true;
  history.pushState({ modal: true }, '');
  historyPushed.value = true;
}

function handlePopState() {
  if (skipPopstateOnce.value) {
    skipPopstateOnce.value = false;
    return;
  }
  if (innerVisible.value) {
    askBeforeClose();
    history.pushState({ modal: true }, '');
  }
}

/**
 * 저장
 */
async function savePost() {
  if (!localForm.value.title || !localForm.value.title.trim()) {
    toast.warning(t('info.postModal.validation.titleRequired'));
    return;
  }
  if (!localForm.value.content || !localForm.value.content.trim()) {
    toast.warning(t('info.postModal.validation.contentRequired'));
    return;
  }

  const contentBytes = new TextEncoder().encode(localForm.value.content).length;
  if (contentBytes > MAX_BYTES) {
    toast.warning(t('info.postModal.validation.contentSizeExceeded'));
    return;
  }

  // 첨부파일 용량 검증 (20MB)
  if (totalFileSize.value > MAX_FILE_SIZE) {
    toast.warning(t('info.postModal.validation.fileSizeExceeded'));
    return;
  }

  const formData = new FormData();

  // 메일 발송 선택 시 전송용 필드 보강
  if (sendMail.value) {
    localForm.value.sendMail = true;
    localForm.value.mailIncludeGuest = mailIncludeGuest.value;
    localForm.value.mailIncludeUser = mailIncludeUser.value;
    localForm.value.mailIncludeManager = mailIncludeManager.value;
    localForm.value.mailIncludeAdmin = mailIncludeAdmin.value;
    if (mailTargetMode.value === 'all') {
      localForm.value.mailToAll = true;
      localForm.value.departmentIds = [];
      localForm.value.teamIds = [];
    } else if (mailTargetMode.value === 'department') {
      localForm.value.mailToAll = false;
      localForm.value.departmentIds = selectedDepartmentIds.value;
      localForm.value.teamIds = [];
    } else if (mailTargetMode.value === 'team') {
      localForm.value.mailToAll = false;
      localForm.value.departmentIds = [];
      localForm.value.teamIds = selectedTeamIds.value;
    }
  } else {
    localForm.value.sendMail = false;
  }

  const jsonString = JSON.stringify(localForm.value);
  const postBlob = new Blob([jsonString], { type: 'application/json' });
  formData.append('post', postBlob);

  newFiles.value.forEach(f => formData.append('files', f));
  deletedFileIds.value.forEach(id => formData.append('deletedFileIds', id));

  let response;
  if (props.isCreate) {
    response = await PostApi.createPost(props.boardId, formData);
    toast.success(t('info.postModal.success.create'));
  } else {
    response = await PostApi.updatePost({
      postId: localForm.value.id,
      formData,
    });
    toast.success(t('info.postModal.success.edit'));
  }
  emit('save', response.data);

  reallyClose();
}

function onHideModal(bvEvent) {
  if (closeConfirmVisible.value) {
    bvEvent.preventDefault();
    return;
  }

  bvEvent.preventDefault();
  askBeforeClose();
}

const quillRef = ref(null);
const editorOptions = {
  theme: 'snow',
  modules: {
    toolbar: [
      [{ header: [1, 2, 3, false] }],
      ['bold', 'italic', 'underline', 'strike'],
      [{ color: ['#000', '#E60000', '#FF9900', '#FFFF00', '#FFFFFF',
           '#008A00', '#0066CC', '#9933FF', 'custom-color'] }, { background: [] }],
      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ align: [] }],
      ['link', 'image', 'video'],
      ['clean']
    ]
  }
};

function toggleHeader() {
  headerVisible.value = !headerVisible.value;
}

const editorHeight = computed(() => {
  const base      = isMobile.value ? 200 : 300;
  const collapsed = isMobile.value ? 400 : 659;
  return `${headerVisible.value ? base : collapsed}px`;
});

watch(
  () => props.isVisible,
  (newVal, oldVal) => {
    if (newVal) {
      localForm.value = cloneDeep(props.form);
      if (!localForm.value || typeof localForm.value.visibilityScope === 'undefined' || localForm.value.visibilityScope === null || localForm.value.visibilityScope === '') {
        localForm.value.visibilityScope = 'ALL';
      }
      newFiles.value = [];
      deletedFileIds.value = [];
      headerVisible.value = true;
      // 메일 패널 초기화
      sendMail.value = false;
      mailTargetMode.value = 'all';
      selectedDepartmentIds.value = [];
      selectedTeamIds.value = [];
      mailIncludeGuest.value = true;
      mailIncludeUser.value = true;
      mailIncludeManager.value = true;
      mailIncludeAdmin.value = true;
      openModal();
    } else if (oldVal && !newVal) {
      innerVisible.value = false;
      if (historyPushed.value) {
        skipPopstateOnce.value = true;
        history.back();
        historyPushed.value = false;
      }
    }
  },
  { immediate: true }
);

function toggleGuest() {
  if (!mailIncludeGuest.value && !mailIncludeUser.value && !mailIncludeManager.value && !mailIncludeAdmin.value) return;
  mailIncludeGuest.value = !mailIncludeGuest.value;
}

function toggleUser() {
  if (!mailIncludeGuest.value && !mailIncludeUser.value && !mailIncludeManager.value && !mailIncludeAdmin.value) return;
  mailIncludeUser.value = !mailIncludeUser.value;
}

function toggleManager() {
  if (!mailIncludeGuest.value && !mailIncludeUser.value && !mailIncludeManager.value && !mailIncludeAdmin.value) return;
  mailIncludeManager.value = !mailIncludeManager.value;
}

function toggleAdmin() {
  if (!mailIncludeGuest.value && !mailIncludeUser.value && !mailIncludeManager.value && !mailIncludeAdmin.value) return;
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
  }
);

watch(
  () => sendMail.value,
  (isOn) => {
    if (isOn) {
      initializeMailPanel();
    } else {
      resetMailSelections();
    }
  }
);

onMounted(async () => {
  await nextTick();
  quillRef.value.initialize(Quill);
  window.addEventListener('popstate', handlePopState);
  
  // 부서/팀 옵션 로드
  try {
    const [deptRes, teamRes] = await Promise.all([
      DepartmentApi.getDepartments(),
      TeamApi.getTeams(),
    ]);
    departmentOptions.value = (deptRes?.data || deptRes || []).map(d => ({ value: d.departmentId, label: d.departmentName }));
    teamOptions.value = (teamRes?.data || teamRes || []).map(t => ({ value: t.teamId, label: t.teamName }));
  } catch (e) {
    // 전역 에러 처리에 맡김
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('popstate', handlePopState);
});
</script>

<style scoped>
.layer {
  margin-bottom: 10px;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  font-size: 1.5rem;
}
.square-btn {
  width: 15px;
  height: 15px;
  padding: 0;
  line-height: 1;
  text-align: center;
  border-radius: 4px;
  font-size: 0.55rem;
  color: white;
}
.form-control {
  font-size: 0.8rem;
  height: 33px;
}
.attachment-list {
  font-size: 0.8rem;
  margin-bottom: 5px;
}
.attachment-list li {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 5px;
}
.file-size-info {
  display: flex;
  align-items: center;
}
.file-size-info-inline {
  color: #6b7280 !important;
  font-size: 0.67rem;
  vertical-align: middle !important;
}
.file-size-info-inline.file-size-exceeded {
  color: #dc3545 !important;
}
.file-size-text {
  margin-left: 6px;
  color: #6b7280;
  font-size: 0.75rem;
}
.editor-box { 
  height: 300px;
}
:deep(.ql-container) { 
  min-height: 300px; 
}

:deep(.ql-editor) {
  font-size: 1rem;
  line-height: 1.6;
}

.two-col {
  display: grid !important;
  grid-template-columns: 1fr 1fr;
  column-gap: 8px;
}
.col-wrapper {
  width: 100%;
}
.col-wrapper :deep(.form-control) { 
  min-width: 0;
}

/* 메일 발송 카드 스타일 */
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
.switch input { display:none; }
.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #d1d5db;
  transition: .2s;
  border-radius: 999px;
}
.slider:before {
  position: absolute;
  content: "";
  height: 18px; width: 18px;
  left: 3px; top: 3px;
  background-color: white;
  transition: .2s;
  border-radius: 50%;
}
.switch input:checked + .slider { background-color: #3b82f6; }
.switch input:checked + .slider:before { transform: translateX(20px); }

.mail-card__body { margin-top: 10px; min-height: 40px; }
.row-top { display:flex; justify-content: space-between; align-items:center; gap:8px; flex-wrap: wrap; }
.segmented { display:flex; gap:6px; }
.seg-btn {
  border: 1px solid #d1d5db;
  background: #f9fafb;
  color: #374151;
  border-radius: 999px;
  padding: 5px 9px;
  font-size: 0.7rem;
}
.seg-btn.active { background: #3b82f6; color: #fff; border-color: #3b82f6; }
.emp-chips { display:flex; gap:6px; }
.chip {
  border: 1px solid #d1d5db;
  background: #fff;
  color: #374151;
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 0.7rem;
}
.chip.active { background: #10b981; border-color:#10b981; color:#fff; }

.picker { margin-top: 12px; }
.picker-head { display:flex; align-items:center; gap:8px; margin-bottom: 6px; }
.picker-title { font-weight: 600; color:#374151; font-size: 0.75rem; }
.badge { display:inline-flex; align-items:center; justify-content:center; min-width:18px; height:18px; padding:0 6px; background:#eef2ff; color:#3730a3; border-radius:999px; font-size:0.65rem; }
.picker-body { border:1px solid #e5e7eb; border-radius:8px; padding:8px; max-height:180px; overflow:auto; }
.checklist { display:grid; grid-template-columns: repeat(auto-fill, minmax(140px,1fr)); gap:6px; }
.checkitem { display:flex; align-items:center; gap:6px; font-size:0.75rem; color:#374151; }
.hint { margin-top:8px; color:#6b7280; font-size:0.65rem; }

/* 실선 + 버튼 */
.collapse-divider {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin: 12px 0;
}
.collapse-divider::before,
.collapse-divider::after {
  content: '';
  flex: 1 1 0;
  height: 0;
  border-top: 1px solid #bbb;
}
.divider-btn {
  padding: 0 10px;
  font-size: 0.75rem;
  color: #555;
  user-select: none;
}
.collapse-divider.open::before,
.collapse-divider.open::after {
  border-color: #0d6efd;
}
.collapse-divider.open .divider-btn {
  color: #0d6efd;
}

@media (max-width: 650px) {
  .modal-header {
    font-size: 1.2rem;
  }
  .square-btn {
    width: 14px;
    height: 14px;
    padding: 0;
    font-size: 0.5rem;
  }
  .form-control, .attachment-list {
    font-size: 0.7rem;
  }
  .form-control {
    height:30px !important;
  }
}
@media (max-width: 500px) {
  .modal-header {
    font-size: 0.8rem;
  }
  .square-btn {
    width: 12px;
    height: 12px;
    padding: 0;
    font-size: 0.4rem;
  }
  .form-control, .attachment-list {
    font-size: 0.65rem;
  }
  :deep(.ql-container),
  :deep(.ql-editor)    { 
    min-height: 200px !important; 
  }
}

/* ═══════════════════════════════════════════════════════════════
 * 다크모드 스타일
 * ═══════════════════════════════════════════════════════════════ */
body[data-theme="dark"] input[type="file"]::file-selector-button {
  background-color: #4a4a4a !important;
  color: #ffffff !important;
  border: 1px solid #6b7280 !important;
}

body[data-theme="dark"] input[type="file"]::file-selector-button:hover {
  background-color: #5a5a5a !important;
}

body[data-theme="dark"] input[type="file"] {
  color: var(--theme-text-primary) !important;
  background-color: var(--theme-card-bg) !important;
  border-color: var(--theme-border) !important;
}

body[data-theme="dark"] .attachment-list {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .attachment-list a {
  color: var(--theme-primary) !important;
}

body[data-theme="dark"] .mail-card {
  background: var(--theme-card-bg) !important;
  border-color: var(--theme-border) !important;
}

body[data-theme="dark"] .mail-card__title {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .seg-btn {
  background: var(--theme-bg-secondary) !important;
  color: var(--theme-text-primary) !important;
  border-color: var(--theme-border) !important;
}

body[data-theme="dark"] .seg-btn.active {
  background: var(--theme-primary) !important;
  color: #ffffff !important;
  border-color: var(--theme-primary) !important;
}

body[data-theme="dark"] .chip {
  background: var(--theme-bg-secondary) !important;
  color: var(--theme-text-primary) !important;
  border-color: var(--theme-border) !important;
}

body[data-theme="dark"] .chip.active {
  background: #10b981 !important;
  color: #ffffff !important;
}

body[data-theme="dark"] .picker-title {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .picker-body {
  background: var(--theme-bg-secondary) !important;
  border-color: var(--theme-border) !important;
}

body[data-theme="dark"] .checkitem {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .hint {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .collapse-divider::before,
body[data-theme="dark"] .collapse-divider::after {
  border-color: var(--theme-border) !important;
}

body[data-theme="dark"] .divider-btn {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .collapse-divider.open::before,
body[data-theme="dark"] .collapse-divider.open::after {
  border-color: var(--theme-primary) !important;
}

body[data-theme="dark"] .collapse-divider.open .divider-btn {
  color: var(--theme-primary) !important;
}
</style>

