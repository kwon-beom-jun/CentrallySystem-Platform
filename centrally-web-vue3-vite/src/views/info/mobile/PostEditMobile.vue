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
          <ul v-if="combinedFiles.length" class="attachment-list">
            <li v-for="(f, idx) in combinedFiles" :key="f.key" class="attachment-item">
              <IconBadge :color="f.isNew ? 'primary' : 'success'">{{ f.isNew ? '신규' : '기존' }}</IconBadge>
              <template v-if="f.isNew">
                <span class="attachment-name">{{ f.name }}</span>
                <span class="file-size-text">({{ formatFileSize(f.size) }}MB)</span>
                <button
                  type="button"
                  class="btn btn-danger square-btn"
                  @click="removeNewFile(f.index)"
                >
                  x
                </button>
              </template>
              <template v-else>
                <span class="attachment-name">{{ f.fileName }}</span>
                <span v-if="f.fileSize" class="file-size-text">({{ formatFileSize(f.fileSize) }}MB)</span>
                <button
                  type="button"
                  class="btn btn-danger square-btn"
                  @click="removeExistingFile(f.index, f.id)"
                >
                  x
                </button>
              </template>
            </li>
          </ul>
        </div>

        <DefaultFormRow align="right" marginTop="10px">
          <MobileMintButton color="gray" type="button" @click="goBack">{{
            $t('info.postMobile.buttons.goBack')
          }}</MobileMintButton>
          <MobileMintButton type="submit" style="margin-left: 8px">{{
            $t('info.postMobile.buttons.edit')
          }}</MobileMintButton>
        </DefaultFormRow>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
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
import { useAuthStore } from '@/store/auth';

/**
 * 모바일 게시글 수정 페이지 (풀스크린)
 */

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const toastStore = useToastStore();
const { t } = useI18n();

const boardCode = ref('');
const canAdmin = ref(false);

const localForm = ref({
  id: null,
  title: '',
  content: '',
  visibilityScope: 'ALL',
  attachments: [],
});

const newFiles = ref([]);
const deletedFileIds = ref([]);
const visibilityOptions = computed(() => [
  { value: 'ALL', label: t('info.postMobile.visibility.all') },
  { value: 'USER_ABOVE', label: t('info.postMobile.visibility.userAbove') },
  { value: 'MANAGER_ABOVE', label: t('info.postMobile.visibility.managerAbove') },
]);

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
    NOTICE: t('info.postMobile.editTitle.notice'),
    RESOURCE: t('info.postMobile.editTitle.resource'),
    COMMUNITY: t('info.postMobile.editTitle.community'),
  };
  return titles[boardCode.value] || t('info.postMobile.editTitle.default');
});

function handleFileChange(e) {
  const files = e.target.files;
  for (let i = 0; i < files.length; i++) newFiles.value.push(files[i]);
  e.target.value = '';
}

function removeNewFile(idx) {
  newFiles.value.splice(idx, 1);
}

function removeExistingFile(idx, id) {
  localForm.value.attachments.splice(idx, 1);
  deletedFileIds.value.push(id);
}

function goBack() {
  router.back();
}

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
  const json = new Blob([JSON.stringify(payload)], { type: 'application/json' });
  formData.append('post', json);
  newFiles.value.forEach((f) => formData.append('files', f));
  deletedFileIds.value.forEach((id) => formData.append('deletedFileIds', id));

  await PostApi.updatePost({ postId: localForm.value.id, formData });
  toastStore.setNextPageMessage(t('info.postMobile.success.edit'), 'success');
  router.replace(
    ROUTES.INFO.POST_DETAIL +
      '?postId=' +
      localForm.value.id +
      '&boardCode=' +
      boardCode.value,
  );
}

onMounted(async () => {
  handleResizeRedirect();
  window.addEventListener('resize', handleResizeRedirect);

  boardCode.value = route.query.boardCode || 'NOTICE';
  const roles = authStore.getRoles || [];
  canAdmin.value =
    roles.includes('ROLE_INFO_ADMIN') ||
    (boardCode.value === 'RESOURCE' && roles.includes('ROLE_INFO_MANAGER')) ||
    (boardCode.value === 'COMMUNITY' && roles.includes('ROLE_INFO_USER'));

  quillRef.value?.initialize?.(Quill);

  try {
    const s = sessionStorage.getItem('postEditing');
    const parsed = s ? JSON.parse(s) : null;
    if (!parsed || !parsed.id) {
      const routes = {
        NOTICE: ROUTES.INFO.NOTICE,
        RESOURCE: ROUTES.INFO.RESOURCE,
        COMMUNITY: ROUTES.INFO.COMMUNITY,
      };
      return router.replace(routes[boardCode.value] || ROUTES.INFO.NOTICE);
    }
    localForm.value = { ...parsed };
  } catch {
    const routes = {
      NOTICE: ROUTES.INFO.NOTICE,
      RESOURCE: ROUTES.INFO.RESOURCE,
      COMMUNITY: ROUTES.INFO.COMMUNITY,
    };
    router.replace(routes[boardCode.value] || ROUTES.INFO.NOTICE);
  }
});

onBeforeUnmount(() => window.removeEventListener('resize', handleResizeRedirect));

function onSelectAreaClick() {
  fileInputRef.value?.click?.();
}

const combinedFiles = computed(() => {
  const existing = (localForm.value.attachments || []).map((a, i) => ({
    key: `e-${a.id}-${i}`,
    isNew: false,
    index: i,
    ...a,
  }));
  const fresh = (newFiles.value || []).map((f, i) => ({
    key: `n-${i}-${f.name}`,
    isNew: true,
    index: i,
    name: f.name,
    size: f.size,
  }));
  return [...existing, ...fresh];
});

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

function handleResizeRedirect() {
  if (window.innerWidth > 650) {
    if (localForm.value.id) {
      router.replace(
        ROUTES.INFO.POST_DETAIL +
          '?postId=' +
          localForm.value.id +
          '&boardCode=' +
          boardCode.value,
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
}
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
.attachment-badge.old {
  background: #ecfeff;
  color: #155e75;
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

/* 다크모드: theme/pages/info/guide/mobile/post-edit-dark.css */
</style>
