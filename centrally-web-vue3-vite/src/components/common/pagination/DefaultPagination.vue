<template>
  <nav>
    <ul class="pagination">
      <!-- 이전 버튼 -->
      <li 
        class="page-item" 
        :class="{ disabled: isPrevDisabled }"
        @click.prevent="goToPrevGroup"
      >
        <a class="page-link" href="#">{{ $t('common.pagination.prev') }}</a>
      </li>

      <!-- 페이지 번호들 (예: 1~totalPages)-->
      <!-- 일반적으로는 1~N 전체를 출력하거나, 또는 5개씩 잘라서 보여주는 로직을 추가할 수도 있습니다. -->
      <li 
        v-for="page in pageRange" 
        :key="page" 
        class="page-item" 
        :class="{ active: currentPage === page }"
        @click.prevent="goToPage(page)"
      >
        <a class="page-link" href="#">{{ page }}</a>
      </li>

      <!-- 다음 버튼 -->
      <li
        class="page-item"
        :class="{ disabled: isNextDisabled }"
        @click.prevent="goToNextGroup"
      >
        <a class="page-link" href="#">{{ $t('common.pagination.next') }}</a>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

/**
 * props 설명:
 * 1) currentPage: 현재 페이지
 * 2) totalPages: 전체 페이지 수
 * 3) visiblePageCount: 한 번에 보여줄 페이지 번호 수 (선택 사항)
 */
const props = defineProps({
  currentPage: {
    type: Number,
    default: 1
  },
  totalPages: {
    type: Number,
    default: 1
  },
  // 한 화면에서 보여줄 페이지 버튼 수(선택적으로 사용)
  visiblePageCount: {
    type: Number,
    default: 5 
  }
});

// 부모에게 이벤트로 페이지 변경을 알려주기 위한 함수
const emit = defineEmits(['pageChange']);

/**
 * goToPage(page): 해당 페이지로 이동을 요청
 * - 페이지 번호가 유효한 범위(1 ~ totalPages)인지 체크 후 
 *   `pageChange` 이벤트를 emit하여 부모에게 알린다.
 */
function goToPage(page) {
  if (page < 1 || page > props.totalPages) return;
  emit('pageChange', page);
}

/**
 * goToPrevGroup(): 이전 페이지 그룹으로 이동
 * - 현재 보이는 pageRange의 첫 번째 페이지 - 1로 이동
 */
function goToPrevGroup() {
  const firstPage = pageRange.value[0];
  if (firstPage && firstPage > 1) {
    goToPage(firstPage - 1);
  }
}

/**
 * goToNextGroup(): 다음 페이지 그룹으로 이동
 * - 현재 보이는 pageRange의 마지막 페이지 + 1로 이동
 */
function goToNextGroup() {
  const lastPage = pageRange.value[pageRange.value.length - 1];
  if (lastPage && lastPage < props.totalPages) {
    goToPage(lastPage + 1);
  }
}

/**
 * pageRange: 실제로 UI에 표시될 페이지 번호 범위 계산
 * 예) visiblePageCount=5 라면, 현재 페이지를 중심으로 앞뒤로 몇 개 보여줄 것인지 계산
 */
const pageRange = computed(() => {
  const half = Math.floor(props.visiblePageCount / 2);
  let start = props.currentPage - half;
  let end = props.currentPage + half;

  // visiblePageCount가 짝수일 경우 보정
  if (props.visiblePageCount % 2 === 0) {
    end -= 1;
  }

  // 범위 제한
  if (start < 1) {
    start = 1;
    end = props.visiblePageCount;
  }
  if (end > props.totalPages) {
    end = props.totalPages;
    start = Math.max(1, end - props.visiblePageCount + 1);
  }

  const range = [];
  for (let i = start; i <= end; i++) {
    range.push(i);
  }
  return range;
});

/**
 * isPrevDisabled: 이전 버튼 비활성화 여부
 * - 현재 보이는 페이지 그룹의 첫 번째 페이지가 1이면 이전 그룹이 없으므로 비활성화
 */
const isPrevDisabled = computed(() => {
  return pageRange.value.length === 0 || pageRange.value[0] === 1;
});

/**
 * isNextDisabled: 다음 버튼 비활성화 여부
 * - 현재 보이는 페이지 그룹의 마지막 페이지가 전체 페이지 수와 같으면 다음 그룹이 없으므로 비활성화
 */
const isNextDisabled = computed(() => {
  return pageRange.value.length === 0 || pageRange.value[pageRange.value.length - 1] === props.totalPages;
});
</script>

<style scoped>
</style>
