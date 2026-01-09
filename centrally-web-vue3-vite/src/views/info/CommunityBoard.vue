<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle 
        :title="$t('info.board.title.community')"
        subtitle="Community Board"
        icon="ri-chat-3-line"
      />

      <div class="create-post">
        <DefaultButton
          v-if="canWrite"
          align="right"
          marginBottom="10px"
          @click="showModal()"
        >
          {{ $t('info.board.createPost') }}
        </DefaultButton>
      </div>

      <DefaultTable
        :columns="columns"
        :data="data"
        :mobileCard="true"
        :mobileLineGap="'3px'"
        cardVariant="notice"
        :rowClick="(item) => goToDetail(item)"
        :usePagination="true"
        :currentPage="currentPage"
        :totalPages="totalPages"
        :visiblePageCount="visiblePageCount"
        @pageChange="onPageChange"
        :noDataImageHeight="484"
      />
    </div>

    <!-- 모달 컴포넌트 -->
    <PostCreateModifyModal
      :isVisible="isModalVisible"
      :isAdmin="canWrite"
      :isCreate="isCreate"
      :boardCode="'COMMUNITY'"
      :boardId="boardId"
      :form="form"
      :hideMailSection="true"
      @close="onCloseModal"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ROUTES } from '@/config/routeConfig';
import { useAuthStore } from '@/store/auth';
import PostCreateModifyModal from '@/components/info/PostCreateModifyModal.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultTable from '@/components/common/table/DefaultTable.vue';
import PostApi from '@/api/info/PostApi';
import BoardApi from '@/api/info/BoardApi';
import HrmUserApi from "@/api/hrm/UsersApi";
import { toast } from 'vue3-toastify';
import { useViewStateStore } from '@/store/viewState';
import { BOARD_CODE } from '@/constants/infoConstants';
import { adjustPageIfExceedsTotal } from '@/utils/paginationUtils';

const router = useRouter();
const authStore = useAuthStore();
const viewState = useViewStateStore();

const boardId = ref(null);
const canWrite = ref(false);

const BREAK_POINT = 650;
const isMobile = ref(window.innerWidth < BREAK_POINT);
let prevIsMobile = isMobile.value;

function handleResize() {
  const nowMobile = window.innerWidth < BREAK_POINT;
  if (nowMobile !== prevIsMobile) {
    isMobile.value = nowMobile;
    showPage.value = nowMobile ? 4 : 10;
    currentPage.value = 1;
    fetchDataFromServer(1);
    prevIsMobile = nowMobile;
  }
}

const isModalVisible = ref(false);
const isCreate = ref(true);

const currentPage = ref(1);
const showPage = ref(isMobile.value ? 4 : 10);
const totalPages = ref(1);
const visiblePageCount = ref(5);

const form = ref({
  id: null,
  title: '',
  author: '',
  date: '',
  content: '',
  visibilityScope: 'ALL',
});

const data = ref([]);

const { t } = useI18n();

const columns = computed(() => [
  {
    key: 'id',
    label: t('info.board.columns.id'),
    width: 50,
    align: 'center',
    customValue: (item) => item.isFixed ? '💬' : item.id,
    mobile: { line: 1, inline: true, prefix: `📝\u00a0${t('info.board.mobile.postNumber')}\u00a0:\u00a0`, suffix: '', bold: false }
  },
  {
    key: 'title',
    label: t('info.board.columns.title'),
    width: 200,
    mobile: { line: 2, bold: true, customClass: 'ellipsis' }
  },
  { 
    key: "content", 
    label: t('info.board.columns.content'), 
    width: 'auto',
    minWidth: 100,
    mobile: { line: 3, bold: false, customClass: 'ellipsis', fontSize: '0.75rem', paddingBottom: '10px' } 
  },
  {
    key: 'author',
    label: t('info.board.columns.author'),
    width: 80,
    mobile: { line: 1, inline: true, prefix: '', suffix: '', bold: false, align: 'right' }
  },
  {
    key: 'editor',
    label: t('info.board.columns.editor'),
    width: 80,
    mobile: { line: 1, inline: true, prefix: '\u00a0[', suffix: ']', bold: false }
  },
  {
    key: 'date',
    label: t('info.board.columns.date'),
    width: 160,
    align: 'center',
    mobile: { line: 3, inline: false, prefix: '', suffix: '', bold: false }
  }
]);

function onPageChange(newPage) {
  currentPage.value = newPage;
  fetchDataFromServer(newPage);
  window.scrollTo({ top: 0, behavior: 'smooth' });
  viewState.saveState('infoCommunityBoard', {
    currentPage: currentPage.value,
    scrollY: window.scrollY,
  });
}

async function fetchDataFromServer(page = 1, restoreScroll = false) {
  const responsePosts = await PostApi.getPosts(boardId.value, {
    page: page - 1,
    size: showPage.value,
    pinned: true
  });

  if (!responsePosts.data.content) {
    toast.error(t('info.board.noPosts'));
    return;
  }

  const authorIds = [...new Set(responsePosts.data.content.map(item => item.writerId))];
  const combinedIds = [...new Set(authorIds)];

  if (!combinedIds || combinedIds.length === 0) {
    data.value = responsePosts.data.content.map(item => ({
      id: item.id,
      title: item.title,
      content: item.contentPreview || '',
      author: item.writerName || t('info.board.unknown'),
      authorEmail: item.createdBy,
      editor: item.writerName || t('info.board.unknown'),
      editorEmail: item.modifiedBy,
      date: item.creationDate,
      isFixed: item.pinned || false,
    }));
  } else {
    const responseUsers = await HrmUserApi.getUsersByIds(combinedIds);
    const userList = responseUsers;

    const userMap = new Map();
    userList.forEach(user => {
      userMap.set(user.userId, user.name);
    });

    data.value = responsePosts.data.content.map(item => ({
      id: item.id,
      title: item.title,
      content: item.contentPreview || '',
      author: userMap.get(item.writerId) || item.writerName || t('info.board.unknown'),
      authorEmail: item.createdBy,
      editor: userMap.get(item.writerId) || item.writerName || t('info.board.unknown'),
      editorEmail: item.modifiedBy,
      date: item.creationDate,
      isFixed: item.pinned || false,
    }));
  }

  currentPage.value = page;
  totalPages.value = responsePosts.data.totalPages;

  // 현재 페이지가 총 페이지 수보다 크면 마지막 페이지로 이동
  const adjusted = await adjustPageIfExceedsTotal({
    currentPageRef: currentPage,
    totalPages: totalPages.value,
    viewState,
    stateKey: 'infoCommunityBoard',
    fetchFunction: fetchDataFromServer,
  });
  if (adjusted) return;

  if (restoreScroll) {
    const saved = viewState.getState('infoCommunityBoard');
    if (saved && typeof saved.scrollY === 'number') {
      setTimeout(() => {
        window.scrollTo(0, saved.scrollY);
      }, 100);
    }
  }
}

function showModal() {
  form.value = {
    id: null,
    title: '',
    author: '',
    date: getLocalDate(),
    content: '',
    visibilityScope: 'ALL',
  };
  if (isMobile.value) {
    viewState.saveState('infoCommunityBoard', {
      currentPage: currentPage.value,
      scrollY: window.scrollY,
    });
    router.push(ROUTES.INFO.POST_CREATE_MOBILE + '?boardCode=COMMUNITY');
  } else {
    isCreate.value = true;
    isModalVisible.value = true;
  }
}

function getLocalDate() {
  const today = new Date();
  today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
  return today.toISOString().split('T')[0];
}

function onCloseModal() {
  isModalVisible.value = false;
}

async function handleSave() {
  isModalVisible.value = false;

  const response = await PostApi.getPosts(boardId.value, {
    page: currentPage.value - 1,
    size: showPage.value,
    pinned: true
  });

  const shouldGoToPrevPage = currentPage.value > 1 && (!response.data.content || response.data.content.length === 0);
  if (shouldGoToPrevPage) {
    currentPage.value = currentPage.value - 1;
    viewState.saveState('infoCommunityBoard', {
      currentPage: currentPage.value,
      scrollY: window.scrollY,
    });
  }

  fetchDataFromServer(currentPage.value);
}

function goToDetail(item) {
  viewState.saveState('infoCommunityBoard', {
    currentPage: currentPage.value,
    scrollY: window.scrollY,
  });
  router.push({
    path: ROUTES.INFO.POST_DETAIL,
    query: { postId: item.id, boardCode: 'COMMUNITY' }
  });
}

let scrollSaveTimer = null;
function handleScroll() {
  if (scrollSaveTimer) clearTimeout(scrollSaveTimer);
  scrollSaveTimer = setTimeout(() => {
    viewState.saveState('infoCommunityBoard', {
      currentPage: currentPage.value,
      scrollY: window.scrollY,
    });
  }, 100);
}

onMounted(async () => {
  const boardResponse = await BoardApi.getBoardByCode(BOARD_CODE.COMMUNITY);
  boardId.value = boardResponse.data.id;

  // USER 이상 작성 가능
  canWrite.value = authStore.roles.includes('ROLE_INFO_USER') 
    || authStore.roles.includes('ROLE_INFO_MANAGER') 
    || authStore.roles.includes('ROLE_INFO_ADMIN');

  const saved = viewState.getState('infoCommunityBoard');
  const restore = viewState.canRestore('infoCommunityBoard');
  if (restore && saved) {
    currentPage.value = saved.currentPage || 1;
  }

  await fetchDataFromServer(currentPage.value, restore);

  window.addEventListener('resize', handleResize);
  window.addEventListener('scroll', handleScroll);
  viewState.allowRestoreFrom('infoCommunityBoard', ['InfoPostDetail', 'InfoPostCreateMobile', 'InfoPostEditMobile']);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  window.removeEventListener('scroll', handleScroll);
  if (scrollSaveTimer) clearTimeout(scrollSaveTimer);
});

</script>

<style scoped>
#dataTable tbody tr:hover td {
  background-color: #e7f1ff !important;
}

.create-post {
  min-height: 48px;
  margin-bottom: 10px;
}

@media (max-width: 650px) {
  .create-post {
    min-height: 0px;
    margin-bottom: 0px;
  }
}
</style>

