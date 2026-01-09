<template>
  <div class="content content-wrapper">
    <!-- 제목 -->
    <MobileDetailTitle 
      v-if="isMobile && isLoaded"
      :title="post.title"
      @back="goBack"
    />
    <PageTitle 
      v-else-if="!isMobile && isLoaded"
      :title="post.title"
      :show-back-button="true"
      @back="goBack"
    />
    
    <!-- 작성자 아바타 + 메타 -->
    <div class="meta-row" v-if="isLoaded">
      <template v-if="authorImgUrl">
        <img 
          class="author-avatar" 
          :src="authorImgUrl" 
          alt="author" 
          @click="openImageModal(authorImgUrl)"
          style="cursor: pointer;"
        />
      </template>
      <template v-else>
        <i class="ri-user-line avatar-placeholder"></i>
      </template>
      <div class="meta-text">
        <div class="article-view-head-detail">
          <DefaultLabel
            size="small"
            class="author-name"
            :text="authorDisplayText"
          />
        </div>
        <div class="date-row">
          <span class="date">{{ post.date }}</span>
          <span class="view-count" v-if="post.viewCount !== undefined && post.viewCount !== null">
            조회수 {{ post.viewCount }}
          </span>
        </div>
      </div>
    </div>

    <!-- 본문 영역 -->
    <div v-if="!isLoaded" class="content-loading-container">
      <div class="content-spinner-wrapper">
        <div class="spinner"></div>
        <p>{{ $t('info.postDetail.loading') }}</p>
      </div>
    </div>

    <template v-else>
      <DefaultFormRow marginTop="40px" align="between">
        <DefaultLabel size="small" :text="$t('info.postDetail.labels.content')" marginTop="10px" alignment="right"/>
        <div>
          <DefaultFormRow marginBottom="5px" align="right">
            <DefaultButton 
              v-if="canPin"
              @click="togglePinPost"
              size="small"
              marginLeft="5px"
              customHeight="28px"
              :customWidth="buttonWidth"
              :color="post.pinned ? 'primary' : 'gray'"
              class="d-flex align-center"
            >
              <span style="line-height: 1;">{{ post.pinned ? $t('info.postDetail.buttons.unpin') : $t('info.postDetail.buttons.pin') }}</span>
              <v-icon size="15" style="padding-left:10px">{{ post.pinned ? 'mdi-pin-off' : 'mdi-pin' }}</v-icon>
            </DefaultButton>
          </DefaultFormRow>
        </div>
      </DefaultFormRow>

      <div
        class="ql-editor content-with-lazy-images"
        v-html="sanitizedContent"
      />

    <DefaultFormRow marginBottom="10px" v-if="isLoaded">
      <div>
        <DefaultLabel
          v-if="post.attachments && post.attachments.length > 0 && nonImageFiles.length > 0"
          :text="$t('info.postDetail.labels.attachment')"
          size="small"
          margin-bottom="5px"
          marginTop="15px" 
        />
        <div v-if="nonImageFiles.length > 0">
          <ul class="attachment-list">
            <li v-for="file in nonImageFiles" :key="file.id">
              <a 
                href="#" 
                @click.prevent="downloadFile(file.fileUrl, file.fileName)"
                rel="noopener noreferrer"
                style="cursor: pointer;"
              >
                <DefaultLabel :text="$t('info.postDetail.labels.download')" size="small" />
                <DefaultLabel :text="file.fileName" size="small" />
              </a>
            </li>
          </ul>
        </div>
      </div>
    </DefaultFormRow>
    </template>

    <!-- 첨부파일 미리보기 영역 -->
    <div class="attachment-section" v-if="isLoaded">
      <DefaultFormRow marginBottom="5px">
        <div>
          <div v-if="imageFiles.length > 0">
            <div class="preview-container">
              <div v-for="file in imageFiles" :key="file.id" class="preview-item">
                <a 
                  href="#" 
                  @click.prevent="downloadFile(file.fileUrl, file.fileName)"
                  rel="noopener noreferrer"
                  style="cursor: pointer;"
                >
                  <img 
                    class="image-preview" 
                    :src="getImagePreviewUrlForPost(file.fileUrl)" 
                    :alt="file.fileName"
                  />
                </a>
              </div>
            </div>
          </div>
        </div>
      </DefaultFormRow>
    </div>

    <hr v-if="isLoaded" />
    <!-- 수정/삭제 -->
    <DefaultFormRow marginBottom="5px" align="right" v-if="isLoaded">
      <DefaultButton 
        @click="openEditModal"
        marginRight="5px"
        v-if="canWrite"
      >
        {{ $t('info.postDetail.buttons.edit') }}
      </DefaultButton>
      <DefaultButton 
        color="red" 
        @click="showDeleteAlert"
        v-if="canWrite"
      >
        {{ $t('info.postDetail.buttons.delete') }}
      </DefaultButton>
    </DefaultFormRow>

    <hr class="comment-hr-line" v-if="isLoaded"/>

    <!-- 댓글 영역 -->
    <div class="comment-section" v-if="isLoaded">
      <LoadingOverlay
        :active="isCommentsLoading"
        :is-full-page="false"
        :loader="'spinner'"
      />
      <div v-show="!isCommentsLoading">
        <h3 class="comment-title">{{ $t('info.postDetail.comments.title') }}</h3>
        <div class="comment-input">
          <textarea v-model="newComment" class="comment-textarea" :placeholder="$t('info.postDetail.comments.placeholder')" rows="3"></textarea>
          <div class="comment-actions">
            <DefaultButton 
              size="small" 
              @click="submitComment" 
              :disabled="!newComment.trim()"
              customHeight="30px"
              customWidth="60px"
            >
              {{ $t('info.postDetail.comments.register') }}
            </DefaultButton>
          </div>
        </div>

        <div class="comment-list">
          <div v-for="c in rootComments" :key="c.id" class="comment-item">
            <div class="comment-header">
              <div class="comment-header-left">
                <img class="comment-avatar" :src="getAvatarUrl(c.writerId)" alt="avatar" />
                <div class="comment-meta">
                  <span class="comment-author">{{ displayAuthor(c.writerId) }}</span>
                  <span class="comment-date">{{ c.creationDate }}</span>
                </div>
              </div>
              <div class="comment-item-actions">
                <button v-if="canDelete(c.writerId)" type="button" class="icon-button" @click.prevent.stop="toggleActions(c.id)">
                  <v-icon size="18">mdi-dots-vertical</v-icon>
                </button>
                  <div 
                  v-if="actionsOpenMap[c.id]"
                  class="comment-actions-menu"
                  v-click-away="{ handler: () => closeActions(c.id) }"
                  @click.stop
                >
                  <div v-if="canEdit(c.writerId)" class="comment-actions-item" @click.prevent.stop="toggleEdit(c.id, c.content)">{{ $t('info.postDetail.comments.edit') }}</div>
                  <div class="comment-actions-item" @click.prevent.stop="confirmDeleteComment(c.id)">{{ $t('info.postDetail.comments.delete') }}</div>
                </div>
              </div>
            </div>
            <div v-if="!editingOpenMap[c.id]" class="comment-body" v-html="sanitizeComment(c.content)"></div>
            <div v-else class="reply-input">
              <textarea v-model="editingDraftMap[c.id]" class="comment-textarea-reply" rows="2"></textarea>
              <div class="comment-actions">
                <DefaultButton 
                  size="xsmall" 
                  customHeight="30px"
                  customWidth="60px"
                  @click="submitEdit(c.id)"
                >
                  {{ $t('info.postDetail.comments.save') }}
                </DefaultButton>
                <DefaultButton
                  size="xsmall"
                  color="gray"
                  customHeight="30px"
                  customWidth="60px"
                  @click="cancelEdit(c.id)"
                >
                  {{ $t('info.postDetail.comments.cancel') }}
                </DefaultButton>
              </div>
            </div>

            <!-- 대댓글 토글/답글 -->
            <div class="reply-toggle-row">
              <div class="reply-toggle" v-if="(childrenByParent[c.id] || []).length > 0" @click="toggleReplies(c.id)">
                {{ $t('info.postDetail.comments.count', { count: (childrenByParent[c.id] || []).length }) }} {{ expandRepliesMap[c.id] ? '∧' : '∨' }}
              </div>
              <DefaultButton 
                size="xsmall" 
                color="gray"
                :customClass="'btn-reply-compact'"
                @click="toggleReply(c.id)"
              >
                {{ $t('info.postDetail.comments.reply') }}
              </DefaultButton>
            </div>

            <!-- 대댓글 입력 -->
            <div v-if="replyOpenMap[c.id]" class="reply-input">
              <textarea v-model="replyDraftMap[c.id]" class="comment-textarea-reply" :placeholder="$t('info.postDetail.comments.replyPlaceholder')" rows="2"></textarea>
              <div class="comment-actions">
                <DefaultButton 
                  size="xsmall" 
                  customHeight="30px"
                  customWidth="60px"
                  @click="submitReply(c.id)"
                >
                  {{ $t('info.postDetail.comments.register') }}
                </DefaultButton>
                <DefaultButton
                  size="xsmall"
                  color="gray"
                  customHeight="30px"
                  customWidth="60px"
                  @click="toggleReply(c.id)"
                >
                  {{ $t('info.postDetail.comments.cancel') }}
                </DefaultButton>
              </div>
            </div>

            <!-- 2 depth 대댓글 목록 -->
            <transition name="reply-fade">
              <div class="reply-list" v-show="expandRepliesMap[c.id] === true" style="will-change: opacity;">
                <div v-for="r in childrenByParent[c.id] || []" :key="r.id" class="reply-item">
                  <div class="comment-header">
                    <div class="comment-header-left">
                      <img class="comment-avatar" :src="getAvatarUrl(r.writerId)" alt="avatar" />
                      <div class="comment-meta">
                        <span class="comment-author">{{ displayAuthor(r.writerId) }}</span>
                        <span class="comment-date">{{ r.creationDate }}</span>
                      </div>
                    </div>
                    <div class="comment-item-actions">
                      <button v-if="canDelete(r.writerId)" type="button" class="icon-button" @click.prevent.stop="toggleActions(r.id)">
                        <v-icon size="18">mdi-dots-vertical</v-icon>
                      </button>
                      <div 
                        v-if="actionsOpenMap[r.id]"
                        class="comment-actions-menu"
                        v-click-away="{ handler: () => closeActions(r.id) }"
                        @click.stop
                      >
                        <div v-if="canEdit(r.writerId)" class="comment-actions-item" @click.prevent.stop="toggleEdit(r.id, r.content)">{{ $t('info.postDetail.comments.edit') }}</div>
                        <div class="comment-actions-item" @click.prevent.stop="confirmDeleteComment(r.id)">{{ $t('info.postDetail.comments.delete') }}</div>
                      </div>
                    </div>
                  </div>
                  <div v-if="!editingOpenMap[r.id]" class="comment-body" v-html="sanitizeComment(r.content)"></div>
                  <div v-else class="reply-input">
                    <textarea v-model="editingDraftMap[r.id]" class="comment-textarea-reply" rows="2"></textarea>
                    <div class="comment-actions">
                      <DefaultButton 
                        size="xsmall" 
                        customHeight="30px"
                        customWidth="60px"
                        @click="submitEdit(r.id)"
                      >
                        {{ $t('info.postDetail.comments.save') }}
                      </DefaultButton>
                      <DefaultButton
                        size="xsmall"
                        color="gray"
                        customHeight="30px"
                        customWidth="60px"
                        @click="cancelEdit(r.id)"
                      >
                        {{ $t('info.postDetail.comments.cancel') }}
                      </DefaultButton>
                    </div>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 삭제 확인 AlertModal -->
  <AlertModal
    :isVisible="deleteConfirmVisible"
    :disableBackgroundClose="false"
    :title="$t('info.postDetail.confirm.deleteTitle')"
    :confirmText="$t('common.button.confirm')"
    :cancelText="$t('common.button.cancel')"
    @close="closeDeleteConfirm"
    @confirm="onConfirmDelete"
  >
    <template #body>
      {{ $t('info.postDetail.confirm.deleteMessage') }}
    </template>
  </AlertModal>

  <!-- 수정 모달 컴포넌트 -->
  <PostCreateModifyModal
    :isVisible="isModalVisible"
    :isAdmin="canWrite"
    :isCreate="isCreate"
    :boardCode="boardCode"
    :boardId="boardId"
    :form="form"
    @close="onCloseModal"
    @save="onSave"
  />

  <!-- 공통 이미지 확대 모달 -->
  <ImageZoomModal
    :visible="imageModalVisible"
    :imageUrl="selectedImageUrl"
    :altText="$t('common.label.image')"
    @close="closeImageModal"
  />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ROUTES } from '@/config/routeConfig';
import { useAuthStore } from '@/store/auth';
import PostApi from '@/api/info/PostApi';
import CommentApi from '@/api/info/CommentApi';
import UsersApi from '@/api/hrm/UsersApi';
import { infoApi } from '@/api/apiConfig';
import useBreakPoint from '@/composables/useBreakPoint';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import PostCreateModifyModal from '@/components/info/PostCreateModifyModal.vue';
import ImageZoomModal from '@/components/common/image/ImageZoomModal.vue';
import MobileDetailTitle from '@/components/common/title/MobileDetailTitle.vue';
import PageTitle from '@/components/common/title/PageTitle.vue';
import { useToastStore } from '@/store/toast';
import { getImagePreviewUrl } from '@/utils/fileUtils';
import { HRM_API_BASE_URL, HRM_SERVICE_NAME } from '@/constants';
import { INFO_API_BASE_URL, INFO_SERVICE_NAME } from '@/constants';
import { useViewStateStore } from '@/store/viewState';
import DOMPurify from 'dompurify';
import { renderSafeHtml } from '@/utils/commentRender';
import { formatRelativeKST, formatAbsoluteKST } from '@/utils/dateUtils';
import LoadingOverlay from 'vue3-loading-overlay';
import 'vue3-loading-overlay/dist/vue3-loading-overlay.css';

const { t } = useI18n();

const isLoaded = ref(false);
const canWrite = ref(false);
const canPin = ref(false);
const deleteConfirmVisible = ref(false);
const pendingDeleteCommentId = ref(null);

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const viewState = useViewStateStore();

const boardCode = ref('');
const boardId = ref(null);
const areaRows = ref(10);

const post = ref({
  id: null,
  writerId: null,
  title: '',
  author: '',
  authorEmail: '',
  date: '',
  content: '',
  attachments: [],
  pinned: false,
  authorProfileImg: null,
  viewCount: 0,
});
const author = ref('');
const authorUser = ref(null);

const authorImgUrl = computed(() =>
  post.value.authorProfileImg?.fileUrl || ''
);

// 댓글 상태
const comments = ref([]);
const newComment = ref('');
const replyOpenMap = ref({});
const replyDraftMap = ref({});
const expandRepliesMap = ref({});
const actionsOpenMap = ref({});
const editingOpenMap = ref({});
const editingDraftMap = ref({});

const rootComments = computed(() => comments.value.filter(c => !c.parentCommentId));
const childrenByParent = computed(() => {
  const map = {};
  comments.value.forEach(c => {
    if (c.parentCommentId) {
      if (!map[c.parentCommentId]) map[c.parentCommentId] = [];
      map[c.parentCommentId].push(c);
    }
  });
  return map;
});

const commenterMap = ref(new Map());
const isCommentsLoading = ref(false);

const { isMobile } = useBreakPoint();
const buttonWidth = computed(() => (isMobile.value ? '100px' : '120px'));

/**
 * 게시글 상세 조회
 */
async function fetchPostDetail(postId) {
  try {
    const response = await PostApi.getPost(postId);
    const data = response.data;
    if (!data) {
      useToastStore().error(t('info.postDetail.error.loadFailed'));
      goBack();
      return;
    }
    post.value = {
      id: data.id,
      boardId: data.boardId,
      writerId: data.writerId,
      title: data.title,
      author: author.value || t('info.board.unknown'),
      authorEmail: post.value.authorEmail,
      date: formatRelativeKST(data.regTime) || formatAbsoluteKST(data.regTime) || '',
      content: data.content,
      attachments: data.attachments || [],
      pinned: data.pinned || false,
      visibilityScope: data.visibilityScope || 'ALL',
      viewCount: data.viewCount || 0,
    };
    boardId.value = data.boardId;
    areaRows.value = (post.value.attachments && post.value.attachments.length > 0) ? 10 : 20;

    isLoaded.value = true;
    
    setTimeout(() => {
      const contentEl = document.querySelector('.ql-editor');
      if (contentEl) {
        const images = contentEl.querySelectorAll('img');
        images.forEach(img => {
          if (img.complete) {
            img.classList.add('loaded');
          } else {
            img.addEventListener('load', () => {
              img.classList.add('loaded');
            });
            img.addEventListener('error', () => {
              img.classList.add('loaded');
            });
          }
        });
      }
    }, 100);
  } catch (error) {
    // 게시글을 찾을 수 없거나 에러 발생 시 메인 페이지로 이동
    console.error('[PostDetail] 게시글 조회 실패:', error);
    useToastStore().setNextPageMessage(t('info.postDetail.error.loadFailed'), 'error');
    router.push(ROUTES.MAIN);
  }
}

/**
 * 댓글 및 사용자 정보 조회
 * - 댓글 목록 조회
 * - 댓글 작성자 + 게시글 작성자 정보 조회 (commenterMap에 저장하여 중복 조회 방지)
 * - 게시글 작성자 프로필 이미지 설정
 * 
 * 실행 시점:
 * - 페이지 처음 로드 시 (onMounted)
 * - 게시글 수정 후 (onSave)
 */
async function fetchCommentsAndUsers() {
  isCommentsLoading.value = true;
  
  // 1. 댓글 조회 및 포맷팅
  const res = await CommentApi.getComments(post.value.id);
  comments.value = (res.data || []).map(c => ({
    ...c,
    creationDate: formatRelativeKST(c.regTime) || formatAbsoluteKST(c.regTime) || ''
  }));
  
  // 2. 아직 조회하지 않은 사용자 ID 수집 (commenterMap에 없는 사용자만)
  // 같은 사용자가 여러 댓글을 달 수 있으므로 중복 제거 후, 이미 조회한 사용자는 제외
  const commentAuthorIds = comments.value.map(c => c.writerId);
  const postAuthorId = post.value.writerId;
  const allAuthorIds = Array.from(new Set([postAuthorId, ...commentAuthorIds]));
  const uncachedIds = allAuthorIds.filter(id => id && !commenterMap.value.has(id));
  
  // 3. 사용자 정보 조회 및 commenterMap에 저장 (중복 API 호출 방지)
  if (uncachedIds.length > 0) {
    const users = await UsersApi.getUsersByIds(uncachedIds);
    users.forEach(u => commenterMap.value.set(u.userId, u));
  }
  
  // 4. 게시글 작성자 프로필 설정
  setPostAuthorProfile();
  
  isCommentsLoading.value = false;
}

/**
 * 게시글 작성자 프로필 설정
 * commenterMap에서 작성자 정보를 가져와 프로필 이미지와 이름 설정
 */
function setPostAuthorProfile() {
  const postAuthorId = post.value.writerId;
  if (!postAuthorId || !commenterMap.value.has(postAuthorId)) return;
  
  const author = commenterMap.value.get(postAuthorId);
  authorUser.value = author;
  // 프로필 이미지 URL을 게이트웨이를 거치는 전체 URL로 변환
  const profileImgUrl = author.profileImgUrl 
    ? getImagePreviewUrl(author.profileImgUrl, HRM_API_BASE_URL, HRM_SERVICE_NAME)
    : null;
  post.value.authorProfileImg = profileImgUrl ? { fileUrl: profileImgUrl } : null;
  post.value.author = author.name || post.value.author;
}

/**
 * 게시글 고정/해제
 */
async function togglePinPost() {
  const newState = !post.value.pinned;

  await PostApi.pinPost({
    postId: post.value.id,
    pinned: newState,
  });

  post.value.pinned = newState;
  const message = newState ? t('info.postDetail.success.pin') : t('info.postDetail.success.unpin');
  useToastStore().success(message);
}

/**
 * 이미지 파일 여부
 */
function isImageFile(fileName) {
  const lower = fileName.toLowerCase();
  return (
    lower.endsWith('.jpg') ||
    lower.endsWith('.jpeg') ||
    lower.endsWith('.png') ||
    lower.endsWith('.gif') ||
    lower.endsWith('.bmp')
  );
}

const imageFiles = computed(() => {
  return post.value.attachments.filter(file => isImageFile(file.fileName));
});
const nonImageFiles = computed(() => {
  return post.value.attachments.filter(file => !isImageFile(file.fileName));
});

/**
 * 이미지 미리보기용 URL (정적 리소스)
 */
function getImagePreviewUrlForPost(fileUrl) {
  return getImagePreviewUrl(fileUrl, INFO_API_BASE_URL, INFO_SERVICE_NAME);
}

/**
 * 파일 다운로드 (PostApi를 통해 게이트웨이를 거쳐 백엔드 컨트롤러로 요청)
 */
async function downloadFile(fileUrl, fileName) {
  try {
    await PostApi.downloadAttachment(fileUrl, fileName);
  } catch (error) {
    console.error('파일 다운로드 오류:', error);
    
    // 백엔드 에러 메시지 추출 (axios 인터셉터에서 Blob을 JSON으로 변환함)
    const errorMessage = error.response?.data?.message 
      || error.response?.data?.error 
      || t('info.postDetail.error.downloadFailed') 
      || '파일 다운로드에 실패했습니다.';
    
    useToastStore().error(errorMessage);
  }
}

/**
 * 뒤로가기
 */
function goBack() {
  const routes = {
    'NOTICE': ROUTES.INFO.NOTICE,
    'RESOURCE': ROUTES.INFO.RESOURCE,
    'COMMUNITY': ROUTES.INFO.COMMUNITY
  };
  router.push(routes[boardCode.value] || ROUTES.INFO.NOTICE);
}

/**
 * 삭제
 */
function showDeleteAlert() {
  pendingDeleteCommentId.value = null;
  deleteConfirmVisible.value = true;
}

/**
 * 게시글 삭제
 */
async function deletePost() {
  deleteConfirmVisible.value = false;
  await PostApi.deletePost(post.value.id);
  
  goBack();
  useToastStore().setNextPageMessage(t('info.postDetail.success.delete'), 'success');
}

/**
 * 수정 모달
 */
const isModalVisible = ref(false);
const isCreate = ref(false);
const form = ref({});

function openEditModal() {
  isCreate.value = false;
  form.value = JSON.parse(JSON.stringify(post.value));
  if (!form.value.visibilityScope) form.value.visibilityScope = 'ALL';
  const isMobileView = window.innerWidth <= 650;
  if (isMobileView) {
    try { sessionStorage.setItem('postEditing', JSON.stringify(form.value)); } catch (e) {}
    router.push(ROUTES.INFO.POST_EDIT_MOBILE + `?boardCode=${boardCode.value}`);
    return;
  }
  isModalVisible.value = true;
}

function onCloseModal() {
  isModalVisible.value = false;
}

async function onSave() {
  isModalVisible.value = false;
  await fetchPostDetail(post.value.id);
  await fetchCommentsAndUsers();
}

const sanitizedContent = computed(() => renderSafeHtml(post.value.content, { mode: 'content' }));
DOMPurify.addHook('afterSanitizeAttributes', node => {
  if (node.tagName === 'A') {
    node.setAttribute('target', '_blank');
    node.setAttribute('rel', 'noopener noreferrer');
  }
});

function sanitizeComment(html) {
  return renderSafeHtml(html || '', { mode: 'comment', whitespace: 'br' });
}

function canDelete(authorId) {
  const myId = authStore.getUserId;
  const roles = authStore.getRoles || [];
  const isInfoAdmin = roles.includes('ROLE_INFO_ADMIN');
  const isSystem = roles.includes('ROLE_GATE_SYSTEM');
  const isOwner = myId && authorId && Number(myId) === Number(authorId);
  return isInfoAdmin || isSystem || isOwner;
}

function canEdit(authorId) {
  const myId = authStore.getUserId;
  const isOwner = myId && authorId && Number(myId) === Number(authorId);
  return Boolean(isOwner);
}

function formatNameWithDeptTeam(user, fallbackName, authorId) {
  const name = user?.name || fallbackName || `User#${authorId}`;
  const departmentName =
    user?.team?.department?.departmentName ||
    user?.department?.departmentName ||
    user?.departmentName ||
    t('common.label.unspecified');
  const teamName = user?.team?.teamName || user?.teamName || t('common.label.unspecified');
  return `${name} (${departmentName} ${teamName})`;
}

function displayAuthor(authorId) {
  const user = commenterMap.value.get(authorId);
  if (user) return formatNameWithDeptTeam(user, undefined, authorId);
  return `User#${authorId}`;
}

/**
 * 댓글 등록
 */
async function submitComment() {
  if (!newComment.value.trim()) return;
  const res = await CommentApi.addComment({ postId: post.value.id, content: newComment.value });
  const created = res?.data;
  if (created && created.id) {
    comments.value = [
      ...comments.value,
      { ...created, creationDate: formatRelativeKST(created.regTime) || formatAbsoluteKST(created.regTime) || '' }
    ];
    // 작성자 정보가 commenterMap에 없으면 조회하여 추가 (중복 API 호출 방지)
    if (created.writerId && !commenterMap.value.has(created.writerId)) {
      const list = await UsersApi.getUsersByIds([created.writerId]);
      if (list && list.length > 0) {
        list.forEach(u => {
          commenterMap.value.set(u.userId, u);
        });
      }
    }
  }
  newComment.value = '';
}

function toggleReply(parentId) {
  replyOpenMap.value[parentId] = !replyOpenMap.value[parentId];
}

/**
 * 대댓글 등록
 */
async function submitReply(parentId) {
  const text = (replyDraftMap.value[parentId] || '').trim();
  if (!text) return;
  const res = await CommentApi.addComment({ postId: post.value.id, content: text, parentCommentId: parentId });
  const created = res?.data;
  if (created && created.id) {
    comments.value = [
      ...comments.value,
      { ...created, creationDate: formatRelativeKST(created.regTime) || formatAbsoluteKST(created.regTime) || '' }
    ];
    // 작성자 정보가 commenterMap에 없으면 조회하여 추가 (중복 API 호출 방지)
    if (created.writerId && !commenterMap.value.has(created.writerId)) {
      const list = await UsersApi.getUsersByIds([created.writerId]);
      if (list && list.length > 0) {
        list.forEach(u => {
          commenterMap.value.set(u.userId, u);
        });
      }
    }
  }
  replyDraftMap.value[parentId] = '';
  replyOpenMap.value[parentId] = false;
}

const authorDisplay = computed(() => {
  return formatNameWithDeptTeam(authorUser.value || {}, post.value.author, post.value.writerId);
});

const authorDisplayText = computed(() => {
  if (post.value.authorEmail) return `${authorDisplay.value} (${post.value.authorEmail})`;
  return authorDisplay.value;
});

/**
 * 댓글 삭제
 */
async function removeComment(commentId) {
  await CommentApi.deleteComment(commentId);
  const toRemove = new Set([Number(commentId)]);
  comments.value.forEach(c => {
    if (c.parentCommentId && Number(c.parentCommentId) === Number(commentId)) toRemove.add(Number(c.id));
  });
  comments.value = comments.value.filter(c => !toRemove.has(Number(c.id)));
}

function toggleReplies(parentId) {
  expandRepliesMap.value[parentId] = !expandRepliesMap.value[parentId];
}

function toggleEdit(commentId, initialContent) {
  const opened = !!editingOpenMap.value[commentId];
  editingOpenMap.value[commentId] = !opened;
  if (!opened) {
    editingDraftMap.value[commentId] = initialContent || '';
  }
  actionsOpenMap.value[commentId] = false;
}

/**
 * 댓글 수정
 */
async function submitEdit(commentId) {
  const content = (editingDraftMap.value[commentId] || '').trim();
  if (!content) return;
  const res = await CommentApi.updateComment(commentId, { content });
  const updated = res?.data;
  if (updated && updated.id) {
    const idx = comments.value.findIndex(c => Number(c.id) === Number(commentId));
    if (idx !== -1) {
      comments.value = [
        ...comments.value.slice(0, idx),
        { ...comments.value[idx], content: updated.content },
        ...comments.value.slice(idx + 1)
      ];
    }
  }
  editingOpenMap.value[commentId] = false;
}

function cancelEdit(commentId) {
  editingOpenMap.value[commentId] = false;
}

const imageModalVisible = ref(false);
const selectedImageUrl = ref(null);

function openImageModal(url) {
  selectedImageUrl.value = url;
  imageModalVisible.value = true;
}

function closeImageModal() {
  imageModalVisible.value = false;
  selectedImageUrl.value = null;
}

function getAvatarUrl(userId) {
  const user = commenterMap.value.get(userId);
  if (user?.profileImgUrl) {
    // 프로필 이미지 URL을 게이트웨이를 거치는 전체 URL로 변환
    return getImagePreviewUrl(user.profileImgUrl, HRM_API_BASE_URL, HRM_SERVICE_NAME);
  }
  return authorImgUrl.value || '';
}

function confirmDeleteComment(commentId) {
  pendingDeleteCommentId.value = commentId;
  deleteConfirmVisible.value = true;
}

async function doDeleteComment() {
  deleteConfirmVisible.value = false;
  if (pendingDeleteCommentId.value) {
    await removeComment(pendingDeleteCommentId.value);
    pendingDeleteCommentId.value = null;
  }
}

function onConfirmDelete() {
  if (pendingDeleteCommentId.value) {
    return doDeleteComment();
  }
  return deletePost();
}

function closeDeleteConfirm() {
  deleteConfirmVisible.value = false;
  pendingDeleteCommentId.value = null;
}

function toggleActions(id) {
  actionsOpenMap.value[id] = !actionsOpenMap.value[id];
}

function closeActions(id) {
  actionsOpenMap.value[id] = false;
}

/**
 * onMounted
 */
onMounted(async () => {
  boardCode.value = route.query.boardCode || 'NOTICE';
  const postId = route.params.postId || route.query.postId;
  const authorParam = route.query.author || route.params.author;
  const emailParam  = route.query.authorEmail || route.params.authorEmail;
  
  if (!postId) {
    goBack();
    return;
  }
  
  author.value = authorParam || '';
  post.value.authorEmail = emailParam || '';
  
  await fetchPostDetail(postId);
  await fetchCommentsAndUsers();
  
  // 권한 확인
  const myId = authStore.getUserId;
  const roles = authStore.getRoles || [];
  const isOwner = myId && post.value.writerId && Number(myId) === Number(post.value.writerId);
  const isAdmin = roles.includes('ROLE_INFO_ADMIN');
  
  canWrite.value = isOwner || isAdmin;
  canPin.value = isAdmin;
});

</script>

<style scoped>
hr {
  color: #cfcfcf; 
}

.article-view-head-detail {
  margin-top: 8px;
  display: flex;
  font-size: 0.85rem;
  color: #6c757d;
  font-weight: 500;
}

.date-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 7px;
}

.date {
  font-size: 0.75rem;
  color: #a0a0a0;
}

.view-count {
  font-size: 0.75rem;
  color: #a0a0a0;
}

.author-name {
  color: #37acc9;
  font-weight: 600;
}

.meta-row{
  display:flex;
  align-items:center;
  gap:13px;
  margin-top:20px;
}
.author-avatar{
  width:55px;
  height:55px;
  border-radius:50%;
  object-fit:cover;
  flex-shrink:0;
}
.avatar-placeholder{
  width:55px;
  height:55px;
  border-radius:50%;
  background:#f3f4f6;
  border:1px solid #e5e7eb;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size:28px;
  color:#6b7280;
  flex-shrink:0;
}
.meta-text{
  display:flex;
  flex-direction:column;
}

.ql-editor {
  border-top:    1px solid #EEEEEE;
  border-bottom: 1px solid #EEEEEE;
  padding: 50px 0 50px 0;
  line-height: 1.7;
  font-size: 0.9rem;
}

.detail-box-body {
  border: 1px solid #616161;
  padding: 16px;
  margin-bottom: 20px;
  border-radius: 2px;
}
.detail-box-content {
  border: 1px solid #b1b1b1;
  padding: 16px;
  margin-bottom: 20px;
  border-radius: 8px;
}
.detail-box {
  border: 1px solid #ddd;
  padding: 16px;
  margin-bottom: 20px;
  border-radius: 8px;
}
.label-title {
  font-weight: bold;
  margin-bottom: 4px;
  display: inline-block;
  width: 80px;
}
.layer {
  margin-bottom: 12px;
}
.attachment-section {
  margin-bottom: 20px;
}
.preview-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.preview-item {
  border: 1px solid #eee;
  padding: 5px;
  border-radius: 4px;
  max-width: 220px;
}
.image-preview {
  width: 130px;
  height: 130px;
  object-fit: cover;
}
.button-group {
  margin-top: 20px;
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
.form-control,
.attachment-list {
  font-size: 0.8rem;
}
.attachment-list {
  margin-bottom: 0px;
}

.attachment-list a * {
  cursor: pointer !important;
}

.content-loading-container {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 40px 0;
}

.content-spinner-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 30px;
}

.content-spinner-wrapper p {
  margin: 0;
  font-size: 0.95rem;
  color: #555;
  font-weight: 500;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.content-with-lazy-images img {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  min-height: 200px;
  border-radius: 4px;
  opacity: 0;
  transition: opacity 0.3s ease-in;
}

.content-with-lazy-images img.loaded {
  opacity: 1;
  background: transparent;
  animation: none;
  min-height: auto;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 650px) {
  .meta-row {
    margin-top: 10px;
  }
  .ql-editor {
    font-size: 0.75rem;
  }
  .date-row {
    margin-top: 10px;
  }
  .date,
  .view-count {
    font-size: 0.65rem;
    color: #818181;
  }
}

@media (max-width: 500px) {
  hr {
    margin: 10px 0px 10px 0px;
  }
  .attachment-section {
    margin-bottom: 0px;
  }
  .image-preview {
    width: 70px;
    height: 70px;
    object-fit: cover;
  }
  .content-spinner-wrapper p {
    font-size: 0.8rem !important;
  }
}
</style>

