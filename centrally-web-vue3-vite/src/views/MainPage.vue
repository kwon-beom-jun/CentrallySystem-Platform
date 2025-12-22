<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <!-- <h2 class="content-title">메인</h2>
      <div class="content-sub-title">
        <p>메인 페이지 입니다</p>
      </div> -->
      <div class="navbar-text">
        <div v-if="!isMobile">
          <video autoplay loop muted playsinline class="main-video">
            <source src="/img/common/main-logo-2100-1000.mp4" type="video/mp4" />
            이 브라우저는 비디오 태그를 지원하지 않습니다
          </video>
        </div>
        <div v-else>
          <!-- 모바일 사용자 카드 (즐겨찾기 포함) - 동적 컴포넌트 -->
          <component :is="selectedCardComponent" :favoriteMenus="favoriteMenus" />
        </div>
      </div>

      <!-- 여기서부터 650px 이하일 때만 나타날 “모바일 사이드 메뉴” -->
      <!-- <div v-if="isMobile"  -->
      <div v-if="false" class="mobile-nav-grid" :style="dynamicGridStyle">
        <div
          class="nav-item"
          v-for="(menu, idx) in mobileMenuItems"
          :key="idx"
          @click="goToMenu(menu.link)"
        >
          <!-- 메뉴 아이콘 및 레이블 사용 안함 -->
          <img :src="menu.icon" class="nav-icon" alt="메뉴 아이콘" />
          <div class="nav-label">{{ menu.label }}</div>
        </div>
      </div>

      <!-- 최신 공지사항 -->
      <DefaultFormRow marginBottom="0px" class="recent-notice-title">
        <div class="notice-title-inner-box">
          <DefaultLabel
            class="font-weight-900"
            :text="isMobile ? $t('main.notice.mobileTitle') : $t('main.notice.title')"
            size="medium"
          />
        </div>
      </DefaultFormRow>
      <div v-if="noticeData.length > 0">
        <DefaultTable
          :columns="noticeColumns"
          :data="noticeData"
          :mobileCard="true"
          cardVariant="main"
          :rowClick="(item) => goToNoticeDetail(item)"
          :minRows="3"
        />
      </div>
      <div v-else>
        <hr class="notice-hr" />
        <DefaultFormRow align="center" marginTop="75px" marginBottom="75px">
          <DefaultLabel
            class="font-weight-900"
            :text="$t('main.notice.noData')"
            size="medium"
          />
        </DefaultFormRow>
        <hr />
      </div>

      <div v-if="canViewReceipt">
        <DefaultFormRow marginBottom="0px" marginTop="30px" class="recent-receipt-title">
          <div class="receipt-title-inner-box">
            <DefaultLabel
              class="font-weight-900"
              :text="isMobile ? $t('main.receipt.mobileTitle') : $t('main.receipt.title')"
              size="medium"
            />
          </div>
        </DefaultFormRow>
        <!-- 테이블 보기 -->
        <div v-if="receiptData.length > 0">
          <DefaultTable
            :columns="receiptColumns"
            :data="receiptData"
            :mobileCard="true"
            cardVariant="main"
            :rowClick="(item) => goToReceipts(item)"
            :minRows="3"
          />
        </div>
        <div v-else>
          <hr class="receipt-hr" />
          <DefaultFormRow align="center" marginTop="75px" marginBottom="75px">
            <DefaultLabel
              class="font-weight-900"
              :text="$t('main.receipt.noData')"
              size="medium"
            />
          </DefaultFormRow>
          <hr />
        </div>
      </div>

      <!-- Centrally 지도 -->
      <DefaultLabel
        class="font-weight-900"
        :text="$t('main.contact.title')"
        size="medium"
        marginTop="30px"
      />
      <hr />
      <MainPageMap :width="'100%'" :height="isMobile ? 200 : 500" />

      <!-- 주소/전화/팩스 정보 -->
      <div class="contact-info">
        <div class="info-item">
          <i class="ri-map-pin-2-fill info-icon icon-address"></i>
          <div class="info-body">
            <div class="info-title">{{ $t('main.contact.address') }}</div>
            <div class="info-text" style="white-space: pre-line">{{ $t('main.contact.addressDetail') }}</div>
          </div>
        </div>
        <hr />

        <div class="info-item">
          <i class="ri-phone-fill info-icon icon-tel"></i>
          <div class="info-body">
            <div class="info-title">{{ $t('main.contact.tel') }}</div>
            <div class="info-text">{{ $t('main.contact.telNumber') }}</div>
          </div>
        </div>
        <hr />

        <div class="info-item">
          <i class="ri-printer-fill info-icon icon-fax"></i>
          <div class="info-body">
            <div class="info-title">{{ $t('main.contact.fax') }}</div>
            <div class="info-text">{{ $t('main.contact.faxNumber') }}</div>
          </div>
        </div>
      </div>
      <hr />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import DefaultLabel from "@/components/common/label/DefaultLabel.vue";
import DefaultTable from "@/components/common/table/DefaultTable.vue";
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import MainPageMap from "@/components/common/map/MainPageMap.vue";
import MobileUserCard from "@/components/common/card/MobileUserCard.vue";
import MobileUserCardVer1 from "@/components/common/card/MobileUserCard-ver1.vue";
import MobileUserCardVer2 from "@/components/common/card/MobileUserCard-ver2.vue";
import MobileUserCardVer3 from "@/components/common/card/MobileUserCard-ver3.vue";
import MobileUserCardVer4 from "@/components/common/card/MobileUserCard-ver4.vue";
import LoginApi from "@/api/auth/LoginApi";
import HrmUserApi from "@/api/hrm/UsersApi";
import ReceiptsApi from "@/api/receipt/ReceiptsApi";
import PostApi from "@/api/info/PostApi";
import BoardApi from "@/api/info/BoardApi";
import { useAuthStore } from "@/store/auth";
import { useFavoritesStore } from "@/store/favorites";
import { toast } from "vue3-toastify";
import { getRolesFrom, hasPermission } from '@/utils/roleUtils';
import { ROLE_GROUPS } from '@/config/roleConfig';
import { ROUTES } from '@/config/routeConfig';
import FavoriteMenuApi from '@/api/hrm/FavoriteMenuApi';
import { BOARD_CODE } from '@/constants/infoConstants';
import { getReceiptStatusClass } from '@/constants/receiptConstants';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// 최상단에 피처 플래그 변수 추가
const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';


/* ================= STORES ================= */
const authStore = useAuthStore();
const favoritesStore = useFavoritesStore();

/* ================= 권한 체크 (roleUtils.js의 hasPermission 사용) ================= */
const roles = computed(() => authStore.roles);
const canViewReceipt = computed(() =>
  isReceiptEnabled && hasPermission(roles.value, getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR))
);

// =========== 즐겨찾기 메뉴 ===========
/**
 * 즐겨찾기 메뉴 - 스토어에서 가져오기 (SecondSidebar와 동기화)
 */
const favoriteMenus = computed(() => {
  return favoritesStore.favoriteMenus.map(item => ({
    favoriteId: parseInt(item.id),
    menuPath: item.path,
    menuLabel: item.label,
    menuIcon: item.icon,
    workspace: item.workspace,
    category: item.category,
    color: item.color
  }));
});

/**
 * 선택된 카드 컴포넌트 (authStore의 cardStyle에 따라 동적으로 선택)
 */
const selectedCardComponent = computed(() => {
  // authStore에서 cardStyle 가져오기
  const cardStyle = authStore.cardStyle || 'default';
  
  const componentMap = {
    'default': MobileUserCard,
    'ver1': MobileUserCardVer1,
    'ver2': MobileUserCardVer2,
    'ver3': MobileUserCardVer3,
    'ver4': MobileUserCardVer4
  };
  
  return componentMap[cardStyle] || MobileUserCard;
});

// =========== 공지 사항 메인 데이터 / 컬럼 정의 ===========
const noticeData = ref([]);
const noticeColumns = [
  { key: 'id',      label: t('main.notice.postNumber')  , width: 70, align: 'center',
    mobile: { line: 1, inline : true, prefix : '📝\u00a0' + t('main.notice.postNumber') + '\u00a0:\u00a0', suffix : '', bold : false } },
  { key: "title",   label: t('main.notice.title'), width: 200,
    mobile: { line: 2, inline : false, bold: true, customClass: 'ellipsis' } },
  { key: "content", label: t('main.notice.content'), width: 'auto', minWidth: 100,
    mobile: { line: 3, inline : false, bold: false, customClass: 'ellipsis', fontSize: '0.75rem', paddingTop: '5px', paddingBottom: '5px' } },
  { key: "author",  label: t('main.notice.author'), width: 100, align: "center",
    mobile: { line: 1, inline: true, prefix: '', suffix: '', bold:false, align:'right' } },
  { key: "date",    label: t('main.notice.date'), width: 110 },
];

/**
 * 최신 공지 사항 3건 조회
 */
async function fetchLatestNotices() {
  try {
    // 1. NOTICE 게시판 정보 가져오기
    const boardRes = await BoardApi.getBoardByCode(BOARD_CODE.NOTICE);
    const boardId = boardRes.data.id;

    // 2. 해당 게시판의 최신 게시글 3건 조회
    const res = await PostApi.getPosts(boardId, {
      page: 0,
      size: 3,
      pinned: false,  // 순수 최신순 (고정글 우선 아님)
    });

    const contents = res.data.content ?? [];

    // 테이블 바인딩용 가공
    noticeData.value = contents.map((item) => ({
      id: item.id,
      title: item.title,
      content: item.contentPreview || "",  // contentPreview 필드 사용 (서버에서 이미 HTML 제거 및 50자 처리됨)
      author: item.writerName || "미확인",
      date: item.creationDate ? item.creationDate.split(" ")[0] : "",
    }));
  } catch (e) {
    console.error('[MainPage] 공지사항 조회 실패:', e);
    noticeData.value = [];
  }
}

/**
 * 최신 공지 사항 행 클릭 (routeConfig.js의 ROUTES 사용)
 */
function goToNoticeDetail(item) {
  router.push({
    path: ROUTES.INFO.POST_DETAIL,
    query: { postId: item.id }
  });
}

// =========== 영수증 메인 데이터 / 컬럼 정의 ===========
const receiptData = ref([]);
const receiptColumns = [
  {
    key: 'receiptCode', label: t('main.receipt.id'), width: 70, align: "center",
    mobile: { line: 1, inline: true, prefix: '📝\u00a0' + t('main.receipt.id') + '\u00a0:\u00a0', suffix: '', bold: false }},
  {
    key: "type", label: t('main.receipt.type'), width: 200,
    mobile: { line: 2, inline: false, prefix: t('main.receipt.type') + "\u00a0:\u00a0", suffix: "", bold: false },
  },
  { key: "reason", label: t('main.receipt.reason'), width: 'auto', minWidth: 100,
    mobile: { line: 2, inline: false, prefix: t('main.receipt.reason') + '\u00a0:\u00a0', suffix: '', bold: true }},
  {
    key: "status", label: t('main.receipt.status'), width: 100, align: 'center',
    customClass: (value) => getReceiptStatusClass(value),
    mobile: { line: 1, inline: true, prefix: '', suffix: '', bold:true, align:'right' }},
  /* { key: 'amount', label: '금액', width: 60 }, */
  { key: "date", label: t('main.receipt.date'), width: 110,
    mobile: { line: 3, inline: false, prefix: '', suffix: '', bold:false }},
  /* { key: 'approverName', label: '결재자', width: 60 }, */
];
/* ---------- 최신 영수증 3건 조회 ---------- */
async function fetchLatestReceipts() {
  try {
    const userId = authStore.getUserId;
    const res = await ReceiptsApi.getReceiptsByUserId(userId, {
      page: 0,
      size: 3,
    });

    const contents = res.data.content ?? [];

    receiptData.value = contents.map((r) => ({
      receiptCode: r.receiptCode,
      reason: r.reason,
      type: r.category?.categoryName ?? '',
      status: r.status?.description || "신청",
      date: r.submissionDate,
    }));
  } catch (e) {
    console.error(e);
    toast.error(t('main.receipt.loadFailed'));
  }
}
/**
 * 최신 영수증 행 클릭 (routeConfig.js의 ROUTES 사용)
 */
function goToReceipts() {
  router.push({
    path: ROUTES.RECEIPT.PERSONAL_HISTORY,
  });
}

// ------------------------------------------------------
// 기존 로직 (isMobile 등) 그대로
// ------------------------------------------------------
const router = useRouter();
const isMobile = ref(window.innerWidth <= 650);
function updateIsMobile() {
  isMobile.value = window.innerWidth <= 650;
}
window.addEventListener("resize", updateIsMobile);

const mobileMenuItems = [
  {
    icon: "/img/common/main/main-notice-1.png",
    label: "정보",
    link: ROUTES.INFO.NOTICE,
  },
  {
    icon: "/img/common/main/main-receipt-5.png",
    label: "영수증 등록",
    link: ROUTES.RECEIPT.SUBMISSION,
  },
  {
    icon: "/img/common/main/main-hrm-5.png",
    label: "인사관리",
    link: ROUTES.MANAGEMENT.USER_MANAGEMENT,
  },
  {
    icon: "/img/common/main/main-person-5.png",
    label: "개인정보",
    link: ROUTES.USER.USER_INFORMATION,
  },
  {
    icon: "/img/common/main/main-system-5.png",
    label: "시스템",
    link: ROUTES.SYSTEM.ACTIVITY_LOG,
  },
];

function goToMenu(routePath) {
  router.push(routePath);
}

// ------------------------------------------------------
// (수정 부분) 동적으로 grid-template-columns, gap 설정
// ------------------------------------------------------
const windowWidth = ref(window.innerWidth);

// 창 크기가 바뀔 때마다 windowWidth 갱신
function handleResize() {
  windowWidth.value = window.innerWidth;
}
window.addEventListener("resize", handleResize);

// dynamicGridStyle: windowWidth에 따라 열 개수와 간격 결정
const dynamicGridStyle = computed(() => {
  let columns = 6;
  let gapPx = 70;
  let topMargin = 30; // 기본값
  const w = windowWidth.value;
  const itemCount = mobileMenuItems.length;

  // 기존 @media 로직을 JS로 옮김 (원하는 기준은 자유롭게 바꿔도 됨)
  if (w <= 400) {
    // topMargin = 15;
    columns = 4;
    gapPx = 30;
  } else if (w <= 500) {
    // topMargin = 25;
    columns = 4;
    gapPx = 40;
  } else if (w <= 600) {
    columns = 5;
    gapPx = 50;
  } else if (w <= 700) {
    columns = 5;
    gapPx = 60;
  } else {
    columns = 6;
    gapPx = 70;
  }

  // 실제 아이템보다 많은 열을 만들지 않는다
  // columns = Math.min(columns, itemCount);
  columns = Math.max(1, Math.min(columns, itemCount));

  return {
    // 그리드 기본 스타일
    display: "grid",
    width: "fit-content",
    margin: `${topMargin}px auto 0`,
    justifyItems: "center",

    // 동적으로 열 / 간격 세팅
    gridTemplateColumns: `repeat(${columns}, auto)`,
    gap: `${gapPx}px`,
  };
});

/**
 * mount 시 한 번 실행
 */
onMounted(async () => {
  handleResize();
  fetchLatestNotices();
  if (canViewReceipt.value) {
    fetchLatestReceipts();
  }
});
</script>

<style scoped>
.content {
  padding-top: 80px;
}
p {
  margin-bottom: 0px !important;
}
/* 네비게이션 텍스트 및 아이콘 */
.content-sub-title {
  margin-bottom: 10px !important;
}
.navbar-text {
  display: block;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  height: 100%;
  padding-top: 0px;
}
/* 이미지 크기 조정 */
.main-video {
  width: 100% !important;
  height: auto !important;
  object-fit: cover;
  margin-bottom: 50px;
}
.table-container {
  margin-bottom: 20px;
}
.recent-notice-title,
.recent-receipt-title {
  padding-bottom: 10px;
}
/* Contact info 스타일 */
.font-weight-900 {
  font-weight: 900;
}
.contact-info {
  max-width: 100%;
  margin: 25px auto 0;
  font-size: 0.9rem;
  line-height: 1.5;
  color: #333;
}
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  padding: 12px 0;
}
.info-icon {
  width: 40px;
  height: 40px;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 50%;
  border: 2px solid;
}
.icon-address {
  color: #4f46e5;
  background-color: rgba(79, 70, 229, 0.1);
  border-color: #4f46e5;
}
.icon-tel {
  color: #10b981;
  background-color: rgba(16, 185, 129, 0.1);
  border-color: #10b981;
}
.icon-fax {
  color: #f59e0b;
  background-color: rgba(245, 158, 11, 0.1);
  border-color: #f59e0b;
}
.info-body {
  flex: 1;
}
.info-title {
  font-weight: 900;
  margin-bottom: 4px;
}
.info-text {
  white-space: pre-line;
  font-size: 0.825rem;
}
.contact-info hr {
  border: none;
  border-top: 1px solid #e1e1e1;
  margin: 0;
}
.notice-hr {
  margin-top: 0px;
  border: none;
  border-top: 1px solid #6b7280 !important;
}
.receipt-hr {
  margin-top: 0px;
  border: none;
  border-top: 1px solid #6b7280 !important;
}

/* 데이터 없음 영역 하단 hr */
.recent-notice-title + div hr,
.recent-receipt-title + div hr {
  border: none;
  border-top: 1px solid #6b7280 !important;
}

@media (min-width: 1300px) {
  .contact-info {
    display: flex; /* 한 줄로 */
    align-items: flex-start;
    justify-content: center;
  }
  .info-item {
    flex: 1 1 0;
    padding: 0 20px; /* 좌·우 여백 */
  }

  /* 가운데 세로선: 마지막 아이템 제외하고 우측 보더 */
  .info-item:not(:last-child) {
    border-right: 1px solid #e1e1e1;
  }

  /* 가로 hr 은 데스크톱에서 숨김 */
  .contact-info hr {
    display: none;
  }
}

/* 
  기존에 @media로 grid-template-columns, gap 지정했던 부분
  → 전부 제거함.
  나머지 margin, display, width, justify-items 등은 그대로 유지 
*/
@media (max-width: 650px) {
  .content {
    margin-left: 0;
    padding: 65px 20px 100px 20px !important;
  }
  .main-video {
    margin-bottom: 30px;
  }
  
  .navbar-text {
    gap: 10px;
    width: 100% !important;
    height: 100%;
  }
  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
  }
  .nav-icon {
    width: 65px;
    height: 65px;
    object-fit: cover;
  }
  .nav-label {
    margin-top: 5px;
    font-size: 0.85rem;
  }
  /* .mobile-nav-grid {
    display: none !important;
  } */
  .info-title {
    font-size: 0.8rem !important;
  }
  .info-text {
    font-size: 0.7rem !important;
  }
  .info-item {
    gap: 15px;
  }
  /* ✨ 1. 부모 컨테이너: 이미지 배치 기준점 역할 */
  .recent-notice-title {
    position: relative;
    /* margin-top을 늘려 이미지가 들어갈 공간 확보 */
    margin: 60px 0 15px 0;
    /* 배경색, 패딩 등은 내부 박스로 이동 */
    padding-bottom: 0px;
  }
  /* ✨ 2. '공지.png' 이미지를 위한 가상요소 (부모 컨테이너에 적용) */
  .recent-notice-title::before {
    content: '';
    position: absolute;
    top: -20px; /* 박스 위로 45px만큼 올림 */
    right: 30px;
    width: 110px;
    height: 110px;
    background-image: url('/img/common/main/recent-notice-title-img.png');
    background-size: contain;
    background-repeat: no-repeat;
    z-index: 4; /* 텍스트와 물결보다 위에 위치 */
    /* opacity: 0.8; */
  }
  /* ✨ 3. 내부 박스: 텍스트와 물결을 담고, 물결을 잘라내는 역할 */
  .notice-title-inner-box {
    /* background-color: #f0f2f5;  */
    background-color: #138ef250; 
    padding: 25px 20px;
    border-radius: 12px;
    width: 100%;
    position: relative;
    overflow: hidden; /* 중요! 이 박스 안에서만 물결이 보이도록 자름 */
  }
  /* ✨ 4. 텍스트 스타일 */
  .notice-title-inner-box > * {
    position: relative;
    z-index: 2; /* 물결보다는 위에 위치 */
    color: #000 !important;
    font-weight: 900 !important;
  }
  /* ✨ 5. 물결 효과 (내부 박스에 적용) */
  .notice-title-inner-box::after {
    content: '';
    position: absolute;
    z-index: 1; /* 가장 아래에 위치 */
    width: 300px;
    height: 300px;
    /* background-color: rgba(0, 0, 0, 0.03); */
    background-color: rgba(0, 0, 255, 0.03);
    border-radius: 50%;
    top: -50px;
    right: -80px;
  }
  .notice-title-inner-box::before {
    content: '';
    position: absolute;
    z-index: 1;
    width: 100px;
    height: 100px;
    /* background-color: rgba(255, 255, 255, 0.4); */
    background-color: rgba(0, 0, 255, 0.12);
    border-radius: 50%;
    bottom: -40px;
    right: 20px;
  }
  /* 1. 부모 컨테이너: '영수증.png' 이미지 배치 기준점 역할 */
  .recent-receipt-title {
    position: relative;
    margin: 60px 0 15px 0;
    padding-bottom: 0px;
  }
  /* 2. '영수증.png' 이미지를 위한 가상요소 */
  .recent-receipt-title::before {
    content: '';
    position: absolute;
    top: 1px;
    right: 25px;
    width: 107px;
    height: 107px;
    background-image: url('/img/common/main/recent-receipt-title-img.png'); /* 영수증 이미지로 변경 */
    background-size: contain;
    background-repeat: no-repeat;
    z-index: 4;
    /* opacity: 0.8; */
  }
  /* 3. 내부 박스: 텍스트와 물결을 담고, 물결을 잘라내는 역할 */
  .receipt-title-inner-box {
    /* background-color: #f0f2f5;  */
    background-color: #138ef250; 
    padding: 25px 20px;
    margin-top: 20px;
    border-radius: 12px;
    width: 100%;
    position: relative;
    overflow: hidden; 
  }
  /* 4. 텍스트 스타일 */
  .receipt-title-inner-box > * {
    position: relative;
    z-index: 2;
    color: #000 !important;
    font-weight: 900 !important;
  }
  /* 5. 물결 효과 (내부 박스에 적용) */
  .receipt-title-inner-box::after {
    content: '';
    position: absolute;
    z-index: 1;
    width: 300px;
    height: 300px;
    /* background-color: rgba(0, 0, 0, 0.03); */
    background-color: rgba(0, 0, 255, 0.03);
    border-radius: 50%;
    top: -50px;
    right: -80px;
  }
  .receipt-title-inner-box::before {
    content: '';
    position: absolute;
    z-index: 1;
    width: 100px;
    height: 100px;
    /* background-color: rgba(255, 255, 255, 0.4); */
    background-color: rgba(0, 0, 255, 0.12);
    border-radius: 50%;
    bottom: -40px;
    right: 20px;
  }
}

@media (max-width: 900px) {
  .nav-icon {
    width: 50px;
    height: 50px;
    object-fit: cover;
  }
}

@media (max-width: 700px) {
  .nav-icon {
    width: 45px;
    height: 45px;
  }
  .nav-label {
    margin-top: 5px;
    font-size: 0.75rem;
  }
}

@media (max-width: 600px) {
  .nav-icon {
    width: 38px;
    height: 38px;
  }
  .nav-label {
    margin-top: 5px;
    font-size: 0.7rem;
  }
}

@media (max-width: 400px) {
  .nav-icon {
    width: 30px;
    height: 30px;
  }
  .nav-label {
    font-size: 0.65rem;
  }
}
</style>
