<template>
  <!-- 모바일 전용 모달 -->
  <div
    class="modal fade"
    id="receiptReportSummaryDetailModal"
    tabindex="-1"
    aria-labelledby="receiptReportSummaryDetailModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <!-- 헤더 -->
        <div class="modal-header">
          <h5 class="modal-title" id="receiptReportSummaryDetailModalLabel">
            {{ $t('receipt.ceoReport.summary') }}
          </h5>
          <button
            type="button"
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
            @click="closeModal"
          ></button>
        </div>

        <!-- 본문 -->
        <div class="modal-body">
          <!-- 이름 / 이메일 -->
          <div class="row mb-2">
            <div class="col-4">
              <DefaultLabel :text="$t('common.label.name')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.userName"
                size="full"
                disabled
              />
            </div>
            <div class="col-8">
              <DefaultLabel :text="$t('common.label.email')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.userEmail"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- 부서 / 팀 -->
          <div class="row mb-2">
            <div class="col-6">
              <DefaultLabel :text="$t('common.label.department')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.departmentName"
                size="full"
                disabled
              />
            </div>
            <div class="col-6">
              <DefaultLabel :text="$t('common.label.team')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.teamName"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- 건수 / 승인금액 -->
          <div class="row mb-2">
            <div class="col-6">
              <DefaultLabel :text="$t('receipt.common.totalReceiptCount')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.count"
                size="full"
                disabled
              />
            </div>
            <div class="col-6">
              <DefaultLabel :text="$t('receipt.common.totalAmount')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="approvedDisplay"
                size="full"
                disabled
              />
            </div>
          </div>
        </div>

        <!-- 푸터 -->
        <div class="modal-footer">
          <DefaultButton @click="closeModal" color="gray" size="small">
            {{ $t('common.button.close') }}
          </DefaultButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import * as bootstrap from 'bootstrap'
import { cloneDeep } from 'lodash'
import DefaultLabel from '@/components/common/label/DefaultLabel.vue'
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue'
import DefaultButton from '@/components/common/button/DefaultButton.vue'

const props = defineProps({
  rowData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close'])
const { t } = useI18n()

const modalInstance = ref(null)
const localData = ref({})
const approvedDisplay = ref('')

watch(
  () => props.rowData,
  (newVal) => {
    if (newVal) {
      localData.value = cloneDeep(newVal)
      approvedDisplay.value = formatCurrency(localData.value.approved) + '원'
    }
  },
  { immediate: true, deep: true }
)

function formatCurrency(val) {
  if (!val) return '0'
  return Number(val).toLocaleString()
}

onMounted(() => {
  const modalEl = document.getElementById('receiptReportSummaryDetailModal')
  modalInstance.value = new bootstrap.Modal(modalEl, {})
  modalEl.addEventListener('hidden.bs.modal', () => {
    emit('close')
  })
  modalInstance.value.show()
})

function closeModal() {
  modalInstance.value.hide()
  emit('close')
}
</script>

<style scoped>
.modal-header {
  display: flex;
  justify-content: space-between;
  font-size: 1.2rem;
}
.col-4, .col-6, .col-8 {
  padding: 3px !important;
}
.mb-2 {
  margin-bottom: 0px !important;
}
</style> 