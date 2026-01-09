<template>
  <div class="card">
    <div class="card-header">
      <p class="card-title">{{ item.date }}</p>
    </div>
    <div class="card-body">
      <p class="card-text"><strong>{{ $t('common.card.people') }} : </strong>
        <span @click="togglePeopleList(item)" style="cursor: pointer;">{{ item.people.length }}{{ $t('system.common.countSuffix') || '명' }}</span>
      </p>
      <div v-if="showPeopleList(item)">
        <div class="people-list">
          <div class="people-list-header">{{ $t('common.card.peopleList') }}</div>
          <p v-for="person in item.people" :key="person.name" class="people-list-item">
            {{ person.name }} ({{ person.department }} - {{ person.team }})
          </p>
        </div>
        <hr />
      </div>
      <p class="card-text"><strong>{{ $t('common.card.typeReason') }} : </strong> {{ item.type }} / {{ item.reason }}</p>
      <p class="card-text"><strong>{{ $t('common.card.amount') }} : </strong> {{ item.amount }}</p>
      <p class="card-text"><strong>{{ $t('common.card.amountPerPerson') }} : </strong> {{ calculateAmountPerPerson }}</p>
      <p>
        <strong class="card-text">{{ $t('common.card.receiptPhoto') }} : </strong>
        <a class="card-text" @click.prevent="openPreviewModal(item.receipt)" style="cursor: pointer; color: blue;">
          {{ item.receiptName }}
        </a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// defineProps는 Vue 3의 script setup에서 자동으로 제공되므로 import할 필요가 없음
// eslint-disable-next-line no-undef
defineProps({
  item: Object,
  calculateAmountPerPerson: Function,
  openPreviewModal: Function
});

// =========== 인원 목록 토글 (모바일) ===========
const openedIndex = ref(null);
function togglePeopleList(index) {
  openedIndex.value = openedIndex.value === index ? null : index;
}
function showPeopleList(index) {
  return openedIndex.value === index;
}

const togglePeopleList = (item) => {
  // 클릭한 항목의 명단 상태를 반전시킴
  const currentVisibility = visiblePeopleLists.value[item.date];
  visiblePeopleLists.value[item.date] = !currentVisibility;
};

const showPeopleList = (item) => {
  // 해당 항목의 명단을 열지 여부를 반환
  return visiblePeopleLists.value[item.date];
};
</script>

<style scoped>
.card {
  width: 100%;
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 5px;
}

.card-header {
  font-size: 1.2rem;
  font-weight: bold;
}

.people-list {
  font-size: 0.7rem;
  max-height: 150px;
  overflow-y: auto;
  margin-top: 10px;
  border-top: 1px solid #ddd;
  padding-top: 10px;
}

.people-list-header {
  font-weight: bold;
  margin-bottom: 5px;
}

.people-list-item {
  padding: 1px 0;
  margin: 0;
  margin-left: 10px;
}
</style>
