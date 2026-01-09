<template>
  <!-- BootstrapVue 모달 (또는 다른 모달 라이브러리) -->
  <b-modal
    v-model="innerVisible"
    :title="$t('receipt.common.detail')"
    no-close-on-backdrop
    centered
    fade
    hide-footer
    @hide="onHideModal"
  >
    <form>
      <!-- (1) 이름 + 발행일 -->
      <div class="row layer">
        <!-- 영수증 ID -->
        <div class="col-4 col-left">
          <DefaultLabel :text="$t('receipt.common.receiptCode')" forId="receiptId" size="small" />
          <DefaultTextfield
            id="receiptId"
            v-model="localForm.receiptId"
            size="full"
            disabled
            :placeholder="$t('receipt.common.receiptCode')"
          />
        </div>
        <!-- 이름 -->
        <div class="col-4 col-left">
          <DefaultLabel :text="$t('common.label.name')" forId="receiptUserName" size="small" />
          <DefaultTextfield
            id="receiptUserName"
            v-model="localForm.userName"
            size="full"
            :disabled="!props.editable"
            :placeholder="$t('common.label.name')"
          />
        </div>
        <!-- 발행일 -->
        <div class="col-4">
          <DefaultLabel :text="$t('receipt.common.issueDate')" forId="receiptDate" size="small" />
          <DefaultTextfield
            id="receiptDate"
            v-model="localForm.date"
            size="full"
            :disabled="!props.editable"
            placeholder="YYYY-MM-DD"
          />
        </div>
      </div>

      <!-- 참여 인원 목록 표시 영역 -->
      <div v-if="localForm.participantsList?.length">
        <hr />
        <DefaultLabel :text="$t('receipt.common.participants')" size="small" />
        <div v-for="(p, index) in localForm.participantsList" :key="index" class="participant-item">
          - {{ p.participantName }} / {{ p.department || '미지정' }} / {{ p.team || '미지정' }}
        </div>
        <hr />
      </div>

      <!-- (2) 이메일 (단독 줄) -->
      <div class="row layer">
        <div class="col-12">
          <DefaultLabel :text="$t('common.label.email')" forId="receiptEmail" size="small" />
          <DefaultTextfield
            id="receiptEmail"
            v-model="localForm.userEmail"
            size="full"
            :disabled="!props.editable"
            placeholder="이메일"
          />
        </div>
      </div>

      <!-- (5) 금액 + 금액/인원수 + 상태 -->
      <div class="row layer">
        <!-- 금액 (col-4) -->
        <div class="col-4">
          <DefaultLabel :text="$t('receipt.common.amount')" forId="receiptAmount" size="small" />
          <DefaultTextfield
            id="receiptAmount"
            v-model="localForm.amount"
            size="full"
            :disabled="!props.editable"
            :placeholder="$t('receipt.common.amount')"
          />
        </div>
        <!-- 금액/인원수 (col-4) -->
        <div class="col-4 col-center">
          <DefaultLabel :text="$t('receipt.common.amountPerPerson')" forId="amountPerPerson" size="small" />
          <DefaultTextfield
            id="amountPerPerson"
            v-model="localForm.amountPerPerson"
            size="full"
            :disabled="true"
            :placeholder="$t('receipt.common.amountPerPerson')"
          />
        </div>
        <!-- 상태 (col-4) -->
        <div class="col-4">
          <DefaultLabel :text="$t('common.label.status')" forId="receiptStatus" size="small" />
          <DefaultTextfield
            id="receiptStatus"
            v-model="localForm.statusLabel"
            size="full"
            :disabled="!props.editable"
            placeholder="신청, 승인, 반려 등"
          />
        </div>
      </div>

      <!-- (3) 구분 + 인원수 -->
      <div class="row layer">
        <!-- 구분 (왼쪽 col-8) -->
        <div class="col-8 col-left">
          <DefaultLabel :text="$t('receipt.common.type')" forId="receiptType" size="small" />
          <DefaultTextfield
            id="receiptType"
            v-model="localForm.type"
            size="full"
            :disabled="!props.editable"
            :placeholder="$t('receipt.common.type')"
          />
        </div>
        <!-- 인원수 (오른쪽 col-4) -->
        <div class="col-4">
          <DefaultLabel :text="$t('receipt.common.peopleCountLabel')" forId="peopleCount" size="small" />
          <DefaultTextfield
            id="peopleCount"
            v-model="localForm.peopleCount"
            size="full"
            :disabled="!props.editable"
            :placeholder="$t('receipt.common.peopleCountLabel')"
          />
        </div>
      </div>

      <!-- (4) 사유 (textarea, 전체 col-12) -->
      <div class="row layer">
        <div class="col-12">
          <DefaultLabel :text="$t('receipt.common.reason')" forId="receiptReason" size="small" />
          <DefaultTextarea
            id="receiptReason"
            v-model="localForm.reason"
            size="full"
            :disabled="!props.editable"
            :placeholder="$t('receipt.common.reason')"
            :rows="3"
          />
        </div>
      </div>
    </form>

    <!-- 모달 하단 버튼 -->
    <DefaultFormRow marginBottom="5px" align="right">
      <DefaultButton
        @click="closeModal"
        color="gray"
        marginRight="5px"
        size="small"
      >
        {{ $t('common.button.close') }}
      </DefaultButton>
      <DefaultButton
        v-if="props.editable"
        @click="saveReceipt"
        marginRight="5px"
        size="small"
      >
        {{ $t('common.button.save') }}
      </DefaultButton>
    </DefaultFormRow>
  </b-modal>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue';
import DefaultTextarea from '@/components/common/textarea/DefaultTextarea.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import { cloneDeep } from 'lodash';

const props = defineProps({
  isVisible: Boolean,   // 모달 표시 여부
  editable: Boolean,    // 수정 가능 여부 (ex. 관리자 여부)
  receiptData: {        // 부모가 넘겨주는 영수증 전체 데이터
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(['close', 'save']);
const { t } = useI18n();

// 내부 모달 표시 상태
const innerVisible = ref(false);

// 로컬 복사본 (모달에서 수정/표시할 데이터)
const localForm = ref({});

// 모달 닫기
function closeModal() {
  innerVisible.value = false;
  emit('close');
}

// BootstrapVue 모달이 닫힐 때
function onHideModal() {
  emit('close');
}

// 저장 버튼
function saveReceipt() {
  // 필요한 경우 서버 API 호출 or 상위에 업데이트 등
  emit('save', localForm.value);
  closeModal();
}

// isVisible → 모달 열기 / 닫기
watch(
  () => props.isVisible,
  (newVal) => {
    innerVisible.value = newVal;
    if (newVal) {
      // 모달 열릴 때, 부모에서 받은 receiptData를 깊은 복사
      localForm.value = cloneDeep(props.receiptData);
    }
  },
  { immediate: true }
);
</script>

<style scoped>
hr {
  margin: 10px 0px 5px 0px;
}
.col-center {
  padding: 0px;
}
.col-left {
  padding-right: 0px;
}
.modal-content {
  padding: 30px !important;
}
.layer {
  margin-bottom: 10px;
}
/* 모달 헤더 스타일 (원하면 추가/수정) */
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
}
.form-control {
  font-size: 0.8rem;
}
.participant-item {
  font-size: 0.8rem !important;
  margin: 2px 0;
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
  .form-control {
    font-size: 0.7rem;
  }
  .participant-item {
    font-size: 0.7rem !important;
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
  .form-control {
    font-size: 0.55rem;
  }
  .participant-item {
    font-size: 0.6rem !important;
  }
}
</style>
