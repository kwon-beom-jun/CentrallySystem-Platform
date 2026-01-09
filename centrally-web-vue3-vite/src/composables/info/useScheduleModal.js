/**
 * 일정 모달 관리 composable
 */
import { ref, nextTick } from 'vue';

export function useScheduleModal() {
  const isModalVisible = ref(false);
  const selectedSchedule = ref(null);
  const isCreate = ref(true);
  const deleteConfirmVisible = ref(false);
  const scheduleToDelete = ref(null);
  
  /**
   * 일정 생성 모달 표시
   */
  function showCreateModal() {
    // 모달이 이미 열려있으면 닫기
    if (isModalVisible.value) {
      isModalVisible.value = false;
      selectedSchedule.value = null;
      isCreate.value = true;
      
      // 다음 틱에서 다시 열기
      nextTick(() => {
        isModalVisible.value = true;
      });
    } else {
      // 모달이 닫혀있으면 바로 열기
      selectedSchedule.value = null;
      isCreate.value = true;
      isModalVisible.value = true;
    }
  }
  
  /**
   * 일정 수정 모달 표시
   */
  function showEditModal(schedule) {
    selectedSchedule.value = schedule;
    isCreate.value = false;
    isModalVisible.value = true;
  }
  
  /**
   * 모달 닫기
   */
  function closeModal() {
    isModalVisible.value = false;
    selectedSchedule.value = null;
    // 모달이 완전히 닫힌 후에 isCreate를 변경하여 타이틀 변경이 보이지 않도록 함
    nextTick(() => {
      setTimeout(() => {
        isCreate.value = true;
      }, 300); // 모달 fade 애니메이션 시간(약 300ms) 후에 변경
    });
  }
  
  /**
   * 삭제 확인 모달 표시
   */
  function showDeleteConfirm(schedule) {
    scheduleToDelete.value = schedule;
    deleteConfirmVisible.value = true;
  }
  
  /**
   * 삭제 확인 모달 닫기
   */
  function closeDeleteConfirm() {
    deleteConfirmVisible.value = false;
    scheduleToDelete.value = null;
  }
  
  return {
    isModalVisible,
    selectedSchedule,
    isCreate,
    deleteConfirmVisible,
    scheduleToDelete,
    showCreateModal,
    showEditModal,
    closeModal,
    showDeleteConfirm,
    closeDeleteConfirm,
  };
}

