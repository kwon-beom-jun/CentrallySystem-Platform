<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <h2 class="content-title">{{ $t('system.componentTest.title') }}</h2>
      <p class="content-sub-title">
        {{ $t('system.componentTest.description') }}<br/>
        DefaultTextfield + DefaultButton + DefaultSelect
      </p>

      <hr/>

      <div v-for="size in sizes" :key="size" class="test-row">
        <p class="test-title">▶ Size: <strong>{{ size }}</strong></p>
        <div class="test-box">
          <DefaultTextfield
            v-model="dummy"
            :size="size"
            :placeholder="$t('common.placeholder.input')"
          />
          <DefaultButton :size="size">
            {{ $t('common.button.confirm') }}
          </DefaultButton>
          <DefaultSelect
            v-model="selected"
            :size="size"
            :options="options"
            :placeholder="$t('common.placeholder.select')"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultSelect from '@/components/common/select/DefaultSelect.vue';

const sizes = [
  'xsmall',
  'small',
  'medium',
  'large',
  'full',
  'full-xsmall',
  'full-small',
  'full-medium',
  'full-large'
];

const dummy = ref('');
const selected = ref('');
const options = [
  { value: '', label: '—' },
  { value: '1', label: '1' },
  { value: '2', label: '2' }
];
</script>

<style scoped>
.content-sub-title {
  font-size: 0.9rem !important;
  margin-bottom: 30px !important;
}

.test-title {
  font-size: 0.8rem;
  margin-bottom: 4px;
  color: #333;
}

.test-box {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.test-box > * {
  /* 박스 안 아이템 높이 비교를 위해 border 넣기 */
  border: 1px dashed #ccc;
  padding: 2px;
}

@media (max-width: 700px) {
  .content-sub-title {
    font-size: 0.8rem !important;
    margin-bottom: 20px !important;
  }
}
</style>
