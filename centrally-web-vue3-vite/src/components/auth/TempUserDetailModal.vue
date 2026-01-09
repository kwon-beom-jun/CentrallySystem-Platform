<template>
  <!-- 모바일 전용 모달 -->
  <div
    class="modal fade"
    id="tempUserDetailModal"
    tabindex="-1"
    aria-labelledby="tempUserDetailModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <!-- 헤더 -->
        <div class="modal-header">
          <h5 class="modal-title" id="tempUserDetailModalLabel">
            {{ $t('auth.tempUser.title') }}
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
          <!-- HRM 권한 안내 문구 -->
          <!-- <InfoNotice 
            color="yellow" 
            icon="ri-alert-line"
            text="사용자 승인 시 반드시 HRM 권한을 부여해주세요."
            marginBottom="15px"
          /> -->
          <!-- 이름 / 이메일 -->
          <div class="row mb-2">
            <div class="col-6">
              <DefaultLabel :text="$t('auth.tempUser.name')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.name"
                size="full"
                disabled
              />
            </div>
            <div class="col-6">
              <DefaultLabel :text="$t('auth.tempUser.email')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.email"
                size="full"
                disabled
              />
            </div>
          </div>

          <!-- 휴대폰 -->
          <div class="row mb-2">
            <div class="col-12">
              <DefaultLabel :text="$t('auth.tempUser.phone')" size="small" />
              <DefaultTextfield
                type="text"
                v-model="localData.phone"
                size="full"
                disabled
              />
            </div>
          </div>
        </div>

        <!-- 푸터 -->
        <div class="modal-footer">
          <DefaultButton @click="approveRow" color="blue" size="small">
            {{ $t('auth.tempUser.approve') }}
          </DefaultButton>
          <DefaultButton @click="deleteRow" color="red" size="small">
            {{ $t('auth.tempUser.delete') }}
          </DefaultButton>
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
import * as bootstrap from 'bootstrap'
import { cloneDeep } from 'lodash'
import DefaultLabel from '@/components/common/label/DefaultLabel.vue'
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue'
import DefaultButton from '@/components/common/button/DefaultButton.vue'
import InfoNotice from '@/components/common/notice/InfoNotice.vue'

const props = defineProps({
  rowData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'approve-row', 'delete-row'])

const modalInstance = ref(null)
const localData = ref({})

watch(
  () => props.rowData,
  (newVal) => {
    if (newVal) {
      localData.value = cloneDeep(newVal)
    }
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  const modalEl = document.getElementById('tempUserDetailModal')
  modalInstance.value = new bootstrap.Modal(modalEl, {})
  // 모달 닫힘 이벤트
  modalEl.addEventListener('hidden.bs.modal', () => {
    emit('close')
  })
  modalInstance.value.show()
})

function closeModal() {
  modalInstance.value.hide()
  emit('close')
}

function approveRow() {
  emit('approve-row', localData.value)
  closeModal()
}

function deleteRow() {
  emit('delete-row', localData.value)
  closeModal()
}
</script>

<style scoped>
.modal-header {
  display: flex;
  justify-content: space-between;
  font-size: 1.2rem;
}
.col-6, .col-12 {
  padding: 3px !important;
}
.mb-2 {
  margin-bottom: 0px !important;
}
</style> 