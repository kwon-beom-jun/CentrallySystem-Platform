<template>
    <div class="card-layout">
      <div class="card" v-for="item in data" :key="item.date">
        <div class="card-header">
          <p class="card-title">{{ item.date }}</p>
        </div>
        <div class="card-body">
          <p class="card-text"><strong>{{ $t('common.card.participants') }} : </strong>{{ item.people.length }}{{ $t('system.common.countSuffix') || '명' }}</p>
          <p class="card-text"><strong>{{ $t('common.card.typeReason') }} : </strong>{{ item.type }} / {{ item.reason }}</p>
          <p class="card-text"><strong>{{ $t('common.card.amount') }} : </strong>{{ item.amount }}</p>
          <p class="card-text"><strong>{{ $t('common.card.amountPerPerson') }} : </strong>{{ item.amountPerPerson }}</p>
          <p>
            <strong class="card-text">{{ $t('common.card.receiptPhoto') }} : </strong>
            <a class="card-text" @click.prevent="openPreviewModal(item.receipt)" style="cursor: pointer; color: blue;">
              {{ item.receiptName }}
            </a>
          </p>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { defineProps } from 'vue';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  
  // 카드 레이아웃에서 사용할 데이터 전달
  const props = defineProps({
    data: {
      type: Array,
      required: true
    },
    openPreviewModal: {
      type: Function,
      required: true
    }
  });
  </script>
  
  <style scoped>
  .card-layout {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
  }
  
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
  
  .card-body {
    font-size: 1rem;
  }
  
  .card-text {
    margin-bottom: 10px;
  }
  </style>
  