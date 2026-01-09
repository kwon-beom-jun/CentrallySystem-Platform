<template>
  <b-modal
    v-model="innerModalVisible"
    title=""
    cancel-title="닫기"
    hide-footer
    hide-header
    centered
    dialog-class="user-detail-dialog"
    body-class="p-0"
  >
    <div class="modern-modal-container">
      <!-- 모달 헤더 -->
      <div class="modal-header-custom">
        <h2 class="modal-title">{{ $t('hrm.userInfo.title') }}</h2>
        <button @click="closeModal" class="close-btn">
          <i class="ri-close-line"></i>
        </button>
      </div>

      <!-- 프로필 섹션 -->
      <div class="profile-hero">
        <div class="profile-background"></div>
        <div class="profile-content">
          <div class="avatar-container">
            <div class="avatar-ring">
              <img
                v-if="user.profileImg?.fileUrl"
                :src="user.profileImg.fileUrl"
                :alt="user.name"
                class="profile-avatar"
                @click="openImageModal"
                style="cursor: pointer;"
              />
              <div v-else class="avatar-placeholder-modern">
                <!-- <i class="ri-user-line"></i> -->
              </div>
            </div>
          </div>
          <div class="profile-info">
            <h1 class="user-name-modern">{{ user.name }}</h1>
            <div class="position-badge">
              <i class="ri-user-star-line"></i>
              {{ user.position?.positionName || $t('hrm.userInfoDetail.unregistered') }}
            </div>
          </div>
        </div>
      </div>

      <!-- 정보 카드들 -->
      <div class="info-cards">
        <!-- 조직 정보 -->
        <div class="info-card organization-card">
          <div class="card-header">
            <i class="ri-building-line card-icon"></i>
            <h3>{{ $t('hrm.userInfo.organizationInfo') }}</h3>
          </div>
          <div class="card-content">
            <div class="info-item">
              <i class="ri-building-2-line item-icon"></i>
              <div class="item-content">
                <span class="item-label">{{ $t('hrm.userInfo.department') }} : </span>
                <span class="item-value">{{
                  user.team?.department?.departmentName || $t('hrm.userModifyMobile.unspecified')
                }}</span>
              </div>
            </div>
            <div class="info-item">
              <i class="ri-team-line item-icon"></i>
              <div class="item-content">
                <span class="item-label">{{ $t('hrm.userInfo.team') }} : </span>
                <span class="item-value">{{ user.team?.teamName || $t('hrm.userModifyMobile.unspecified') }}</span>
              </div>
            </div>
            <div class="info-item">
              <i class="ri-calendar-check-line item-icon"></i>
              <div class="item-content">
                <span class="item-label">{{ $t('hrm.userInfo.hireDate') }} : </span>
                <span class="item-value">{{ formatDate(user.joiningDate) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 연락처 정보 -->
        <div class="info-card contact-card">
          <div class="card-header">
            <i class="ri-contacts-line card-icon"></i>
            <h3>{{ $t('hrm.userInfo.contactInfo') }}</h3>
          </div>
          <div class="card-content">
            <div class="info-item">
              <i class="ri-mail-line item-icon"></i>
              <div class="item-content">
                <span class="item-label">{{ $t('hrm.userInfo.email') }} : </span>
                <span class="item-value">{{ user.email }}</span>
              </div>
            </div>
            <div class="info-item">
              <i class="ri-phone-line item-icon"></i>
              <div class="item-content">
                <span class="item-label">{{ $t('hrm.userInfo.phone') }} : </span>
                <span class="item-value">{{ user.phoneNumber || "-" }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </b-modal>

  <!-- 공통 이미지 확대 모달 -->
  <ImageZoomModal
    :visible="imageModalVisible"
    :imageUrl="selectedImageUrl"
    :altText="user.name"
    @close="closeImageModal"
  />
</template>

<script setup>
import { ref, watch, toRefs } from "vue";
import ImageZoomModal from '@/components/common/image/ImageZoomModal.vue';

const props = defineProps({
  visible: { type: Boolean, default: false },
  user: {
    type: Object,
    default: () => ({}),
  },
});

const emits = defineEmits(["close"]);

// props에서 user를 구조분해할당으로 추출
const { user: propsUser } = toRefs(props);

// 로컬 user 상태 - 모달이 닫히는 동안 데이터 유지
const user = ref({});

// b-modal 제어를 위한 내부 ref
const innerModalVisible = ref(props.visible);

// 닫기
function closeModal() {
  emits("close");
}

// props user 데이터 변화 감지하여 로컬 상태 업데이트
watch(
  () => propsUser.value,
  (newUser) => {
    if (newUser && Object.keys(newUser).length > 0) {
      user.value = { ...newUser };
    }
  },
  { immediate: true, deep: true }
);

// props.visible 변화 감지
watch(
  () => props.visible,
  (newVal) => {
    innerModalVisible.value = newVal;
  }
);

// 모달이 닫혔을 때 부모로 close 이벤트
watch(
  () => innerModalVisible.value,
  (newVal) => {
    if (!newVal) {
      emits("close");
      // 모달이 완전히 닫힌 후 약간의 지연을 두고 데이터 초기화
      setTimeout(() => {
        if (!props.visible) {
          user.value = {};
        }
      }, 500); // 500ms 
    }
  }
);

// 날짜 포맷팅
function formatDate(dateString) {
  if (!dateString) return "-";
  //  yyyy-mm-dd 형식
  // const date = new Date(dateString);
  // const year = date.getFullYear().toString().slice(-2); // 마지막 2자리만
  // const month = String(date.getMonth() + 1).padStart(2, '0');
  // const day = String(date.getDate()).padStart(2, '0');
  // return `${year}-${month}-${day}`;
  return new Date(dateString).toLocaleDateString("ko-KR");
}

// 고용형태 텍스트 변환
function getEmploymentType(typeId) {
  switch (typeId) {
    case 1:
      return "정규직";
    case 2:
      return "비정규직";
    default:
      return "미지정";
  }
}

// 고용형태 클래스 반환
function getEmploymentClass(typeId) {
  switch (typeId) {
    case 1:
      return "regular";
    case 2:
      return "contract";
    default:
      return "unspecified";
  }
}

// 이미지 확대 모달 제어
const imageModalVisible = ref(false);
const selectedImageUrl = ref('');

// 이미지 확대 모달 열기
function openImageModal() {
  if (user.value.profileImg?.fileUrl) {
    selectedImageUrl.value = user.value.profileImg.fileUrl;
    imageModalVisible.value = true;
  }
}
// 이미지 확대 모달 닫기
function closeImageModal() {
  imageModalVisible.value = false;
  selectedImageUrl.value = '';
}
</script>

<style scoped>
/* ==================================
   * 모달 너비 & 기본 스타일
  ===================================== */
:deep(.user-detail-dialog) {
  max-width: 600px !important;
  min-width: 375px !important;
  margin: 15px auto !important;
}

:deep(.modal-content) {
  border: none !important;
  border-radius: 16px !important;
  box-shadow: 0 20px 40px -12px rgba(0, 0, 0, 0.2) !important;
  overflow: hidden !important;
}

/* ==================================
   * 내부 레이아웃
  ===================================== */
.modern-modal-container {
  background: #f8f9fa;
}

.modal-header-custom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 22px 18px 22px;
  /* background: linear-gradient(90deg, #5d9bff 0%, #004497 100%); */
  background: linear-gradient(135deg, #99a0ad 0%, #424242 100%);
  color: white;
}

.modal-title {
  font-size: 1.1rem;
  font-weight: 700;
  margin: 0;
  line-height: 20px !important;
}

.close-btn {
  background: transparent;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.2s ease;
}
.close-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: rotate(90deg);
}

/* ==================================
   * 프로필 섹션 (이름/직책 레이아웃 문제 해결)
  ===================================== */
.profile-hero {
  position: relative;
  padding-bottom: 20px;
}

.profile-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 65px;
  /* background: linear-gradient(90deg, #629eff 0%, #004497 100%); */
  background: linear-gradient(135deg, #91949b 0%, #3b3838 100%);
}

.profile-content {
  position: relative;
  display: flex;
  align-items: center; /* 수직 중앙 정렬 */
  padding: 0 22px;
  gap: 16px;
}

.avatar-container {
  flex-shrink: 0;
}

.avatar-ring {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  padding: 4px;
  background: linear-gradient(180deg, #eaf4ff 0%, #ffffff 100%);
  box-shadow: 0 8px 22px rgba(0, 140, 255, 0.12), inset 0 0 0 2px #eaf4ff;
}

.profile-avatar,
.avatar-placeholder-modern {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column; /* 수직 정렬 */
  align-items: flex-start; /* 좌측 정렬 */
}

.user-name-modern {
  font-size: 1.4rem;
  font-weight: 800;
  margin: 0;
  color: #eaf4ff !important;
}

.position-badge {
  margin-top: 10px; /* 이름과 10px 간격 */
  display: inline-flex;
  align-items: center;
  gap: 5px;
  /* background: #004497; */
  background: #2b2b2b;
  color: #ffffff !important;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 500;
}

/* ==================================
   * 정보 카드들
  ===================================== */
.info-cards {
  padding: 20px 18px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.info-card {
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s ease;
}

.info-card:hover {
  transform: translateY(-2px);
  border-color: #ffffff;
  box-shadow: 0 6px 16px rgba(0, 68, 151, 0.14);
}

.contact-card,
.organization-card,
.additional-card {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.card-icon {
  font-size: 1.2rem;
  color: #015cca;
}

.card-header h3 {
  font-size: 0.9rem;
  font-weight: 800;
  margin: 0;
  color: #495057;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  font-size: 0.8rem;
}

.info-item:not(:last-child) {
  border-bottom: 1px solid #f1f3f5;
}

.item-icon {
  font-size: 1rem;
  color: #868e96;
  width: 16px;
  text-align: center;
}

.item-label {
  font-weight: 500;
  color: #495057;
}

.item-value {
  margin-left: auto;
  color: #6c757d;
  text-align: right;
}

.employment-type.regular {
  color: #12b886;
  font-weight: bold;
}

.employment-type.contract {
  color: #f76707;
  font-weight: bold;
}

@media (max-width: 650px) {
  .modal-title {
    font-size: 1rem !important;
    font-weight: 700 !important;
    margin: 0 !important;
    line-height: 20px !important;
  }
  .modal-header-custom {
    padding: 15px 22px 15px 22px !important;
  }

  .user-name-modern {
    font-size: 0.9rem;
  }
  
  .profile-hero {
    position: relative;
    padding-bottom: 0px;
  }

  .profile-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 65px;
    /* background: linear-gradient(to right, #6a82fb, #764ba2); */
    background: linear-gradient(135deg, #91949b 0%, #313131 100%);
  }

  .profile-content {
    position: relative;
    display: flex;
    align-items: center; /* 수직 중앙 정렬 */
    padding: 0 22px;
    gap: 16px;
  }

  .position-badge {
    font-size: 0.65rem;
    padding: 3px 8px;
  }

  .info-cards {
    grid-template-columns: 1fr 1fr;
    padding: 15px 12px;
    gap: 8px;
  }

  .info-card {
    padding: 12px;
  }

  .card-header {
    margin-bottom: 5px;
  }

  .card-header h3 {
    font-size: 0.75rem;
    font-weight: 800 !important;
  }

  .info-item {
    font-size: 0.68rem;
    padding: 6px 0;
  }
}
</style>
