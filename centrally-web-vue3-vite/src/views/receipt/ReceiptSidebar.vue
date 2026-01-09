<template>
  <div>
    <!-- 사이드바 -->
    <div v-if="visible" class="sidebar">
      <button class="close-btn" @click="closeSidebar">&times;</button>
      <h5>
        <strong>{{ $t('receipt.common.receiptDetail') }}</strong>
      </h5>

      <hr />
      
      <p v-if="item.date">
        <strong>{{ $t('receipt.common.issueDateLabel') }} </strong> {{ item.date }}
      </p>
      <p v-if="item.type">
        <strong>{{ $t('receipt.common.typeLabel2') }} </strong> {{ item.type }}
      </p>
      <p v-if="item.reason">
        <strong>{{ $t('receipt.common.reasonLabel2') }} </strong> {{ item.reason }}
      </p>
      <p v-if="item.amount">
        <strong>{{ $t('receipt.common.amountLabel2') }} </strong> {{ item.amount }}
      </p>
      <p v-if="item.amount && item.people.length > 0">
        <strong>{{ $t('receipt.common.amountPerPersonLabel') }} </strong> {{ calculateAmountPerPerson(item) }}
      </p>
      <p v-if="item.receiptName">
        <strong>{{ $t('receipt.common.receiptPhotoLabel2') }} </strong>
        <!-- <a @click.prevent="openPreviewModal" style="cursor: pointer; color: blue;">{{ item.receiptName }}</a> -->
        <a @click.prevent="$emit('preview-image', item.receipt)" style="cursor: pointer; color: blue;">{{ item.receiptName }}</a>
      </p>
      <p v-if="item.status" :class="getStatusClass(item.status)">
        <strong class="status-text">{{ $t('receipt.common.paymentStatusLabel') }} </strong>{{ item.status }}
      </p>
      <p v-if="item.rejectionReason">
        <strong>{{ $t('receipt.common.rejectReasonLabel') }} </strong> {{ item.rejectionReason }}
      </p>
      <hr/>
      <p v-if="item.people && item.people.length">
        <strong>{{ $t('receipt.common.peopleCountLabel2') }} </strong> {{ item.people.length }}
      </p>
      <ul v-if="item.people && item.people.length" class="people-list">
        <!-- <li class="people-list-item" v-for="(person, index) in item.people" :key="index">{{ person.department }} | {{ person.team }} | {{ person.name }} {{ person.position }}</li> -->
        <li class="people-list-item" v-for="(person, index) in item.people" :key="index">
          {{ person.name }} ({{ person.department }} | {{ person.team }})
        </li>
      </ul>
    </div>

    <!-- 이미지 미리보기 모달 -->
    <div v-if="isPreviewVisible" class="modal" @click="closePreviewModalOnOutsideClick">
      <div class="modal-content" @click.stop>
        <img :src="item.receipt" class="modal-image" />
        <span class="close" @click="closePreviewModal">&times;</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { RECEIPT_STATUS_LABELS } from '@/constants/receiptConstants';

defineProps({
  item: Object,
  visible: Boolean,
});

const emit = defineEmits(['close', 'preview-image']);
// const emit = defineEmits(['close']);
const isPreviewVisible = ref(false);

// 금액/인원수 계산
const calculateAmountPerPerson = (item) => {
  const amount = parseInt(item.amount.replace(/[^0-9]/g, ''), 10); // 금액에서 숫자만 추출
  const peopleCount = item.people.length;
  return peopleCount > -1 ? Math.floor(amount / (peopleCount+1)).toLocaleString() + ' 원' : 'N/A';
};

const getStatusClass = (status) => {
  if (status === RECEIPT_STATUS_LABELS.REQUEST) return 'status-pending';
  if (status === RECEIPT_STATUS_LABELS.APPROVED) return 'status-approved';
  if (status === RECEIPT_STATUS_LABELS.REJECTED) return 'status-rejected';
  return '';
};

const closeSidebar = () => {
  emit('close');
};

// const openPreviewModal = () => {
//   isPreviewVisible.value = true;
// };

const closePreviewModal = () => {
  isPreviewVisible.value = false;
};

const closePreviewModalOnOutsideClick = (event) => {
  // 클릭된 요소가 모달 외부일 때만 모달을 닫습니다.
  if (event.target.classList.contains('modal')) {
    isPreviewVisible.value = false;
  }
};
</script>

<style scoped>
.sidebar {
  position: fixed;
  right: 0;
  top: 72px; /* 위에서 61px 띄우기 */
  bottom: 42px; /* 아래에서 42px 띄우기 */
  width: 300px;
  background-color: #f9f9f9;
  border-left: 1px solid #ddd;
  padding: 20px;
  transition: all 0.3s ease;
  box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  overflow-y: auto; /* 사이드바 전체 스크롤 가능 */
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 24px;
  cursor: pointer;
  background: none;
  border: none;
}

.modal {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  z-index: 2000;
}

.modal-content {
  position: relative;
  width: 80%;
  max-width: 500px;
  background-color: transparent; /* 테두리 제거 */
  padding: 0;
  text-align: center;
}

.modal-image {
  width: 100%;
  height: auto;
}

.close {
  position: absolute;
  top: -50px; /* 이미지 밖으로 X버튼 이동 */
  right: -30px; /* 이미지 밖으로 X버튼 이동 */
  font-size: 40px;
  cursor: pointer;
  background: none;
  border: none;
  color: white; /* 흰색으로 변경 */
}

/* 인원수 리스트에 대한 스타일 */
.people-list {
  font-size: 0.75rem;
  max-height: 300px; /* 최대 높이를 설정 */
  overflow-y: auto; /* 길어지면 스크롤 표시 */
  border: 1px solid #ddd !important;
  border-radius: 5px;
  background-color: #f9f9f9;
}

.people-list-item {
  margin: 7px 0 7px 0;
}

.status-approved {
  color: green;
  font-weight: bold;
}

.status-pending {
  color: blue;
  font-weight: bold;
}

.status-rejected {
  color: red;
  font-weight: bold;
}

.status-text {
  color: black;
  font-weight: 700;
}

</style>
