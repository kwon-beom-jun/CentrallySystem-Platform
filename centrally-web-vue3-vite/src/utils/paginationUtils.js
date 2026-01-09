/**
 * 페이징 관련 유틸리티 함수
 */

/**
 * 삭제 후 페이지 번호 조정
 * 
 * 현재 페이지에 데이터가 1개만 남았을 때 삭제하면,
 * 페이지가 빈 상태로 남지 않도록 이전 페이지로 이동합니다.
 * 
 * @param {number} currentPage - 현재 페이지 번호 (1-based)
 * @param {number} currentDataCount - 현재 페이지의 데이터 개수
 * @param {number} deleteCount - 삭제할 데이터 개수 (기본값: 1)
 * @returns {number} 조정된 페이지 번호
 * 
 * @example
 * // 2페이지에 데이터 1개만 남았을 때
 * const newPage = adjustPageAfterDelete(2, 1); // 1 반환
 * 
 * @example
 * // 2페이지에 데이터 3개 있을 때
 * const newPage = adjustPageAfterDelete(2, 3); // 2 반환 (페이지 유지)
 * 
 * @example
 * // 1페이지(첫 페이지)에 데이터 1개만 남았을 때
 * const newPage = adjustPageAfterDelete(1, 1); // 1 반환 (첫 페이지는 유지)
 */
export function adjustPageAfterDelete(currentPage, currentDataCount, deleteCount = 1) {
  // 삭제 후 남은 데이터 개수
  const remainingCount = currentDataCount - deleteCount;
  
  // 첫 페이지가 아니고, 삭제 후 데이터가 0개가 되면 이전 페이지로 이동
  if (currentPage > 1 && remainingCount <= 0) {
    return currentPage - 1;
  }
  
  // 그 외의 경우 현재 페이지 유지
  return currentPage;
}

/**
 * 삭제 후 페이지 조정 및 데이터 재조회 헬퍼
 * 
 * @param {Object} options - 옵션 객체
 * @param {import('vue').Ref<number>} options.currentPageRef - 현재 페이지 ref
 * @param {import('vue').Ref<Array>} options.dataRef - 데이터 배열 ref
 * @param {Function} options.fetchFunction - 데이터 조회 함수 (페이지 번호를 인자로 받음)
 * @param {number} [options.deleteCount=1] - 삭제할 데이터 개수
 * @returns {Promise<void>}
 * 
 * @example
 * import { adjustPageAndRefetch } from '@/utils/paginationUtils';
 * 
 * async function confirmDelete() {
 *   await ReceiptsApi.deleteReceipt(deleteTarget.value.receiptId);
 *   
 *   await adjustPageAndRefetch({
 *     currentPageRef: currentPage,
 *     dataRef: data,
 *     fetchFunction: fetchReceipts,
 *   });
 *   
 *   toast.success('삭제되었습니다.');
 * }
 */
export async function adjustPageAndRefetch({
  currentPageRef,
  dataRef,
  fetchFunction,
  deleteCount = 1,
}) {
  // 삭제 전 페이지 조정
  const adjustedPage = adjustPageAfterDelete(
    currentPageRef.value,
    dataRef.value.length,
    deleteCount
  );
  
  // 페이지 변경
  currentPageRef.value = adjustedPage;
  
  // 데이터 재조회
  await fetchFunction(adjustedPage);
}

/**
 * 게시글 상세에서 삭제 후 목록으로 돌아갈 때 페이지 조정
 * 
 * ViewState에 저장된 페이지 정보를 기반으로 조정합니다.
 * 
 * @param {Object} options - 옵션 객체
 * @param {Object} options.viewState - ViewState 스토어
 * @param {string} options.stateKey - ViewState 키 (예: 'noticeBoard')
 * @param {number} options.totalCount - 삭제 전 전체 데이터 개수
 * @param {number} options.pageSize - 페이지당 데이터 개수
 * @returns {number} 조정된 페이지 번호
 * 
 * @example
 * import { adjustPageAfterDetailDelete } from '@/utils/paginationUtils';
 * 
 * // 게시글 상세에서 삭제 후
 * const adjustedPage = adjustPageAfterDetailDelete({
 *   viewState,
 *   stateKey: 'noticeBoard',
 *   totalCount: 25,  // 삭제 전 전체 게시글 수
 *   pageSize: 10,
 * });
 * 
 * // 조정된 페이지로 라우팅
 * router.push({ name: 'NoticeBoard', query: { page: adjustedPage } });
 */
export function adjustPageAfterDetailDelete({
  viewState,
  stateKey,
  totalCount,
  pageSize,
}) {
  const saved = viewState.getState(stateKey);
  const currentPage = saved?.currentPage || 1;
  
  // 현재 페이지의 시작 인덱스와 끝 인덱스 계산
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  
  // 현재 페이지의 데이터 개수 계산
  const currentPageDataCount = Math.min(totalCount - startIndex, pageSize);
  
  // 삭제 후 조정된 페이지 계산
  return adjustPageAfterDelete(currentPage, currentPageDataCount);
}

/**
 * 총 페이지 수 기반 페이지 조정 및 재조회
 * 
 * 현재 페이지가 총 페이지 수를 초과하는 경우, 마지막 페이지로 조정하여 재조회합니다.
 * 게시판 등에서 상세 화면 삭제 후 목록으로 돌아올 때 사용합니다.
 * 
 * @param {Object} options - 옵션 객체
 * @param {import('vue').Ref<number>} options.currentPageRef - 현재 페이지 ref
 * @param {number} options.totalPages - 총 페이지 수
 * @param {Object} options.viewState - ViewState 스토어
 * @param {string} options.stateKey - ViewState 키 (예: 'infoNoticeBoard')
 * @param {Function} options.fetchFunction - 데이터 조회 함수 (페이지 번호를 인자로 받음)
 * @returns {Promise<boolean>} 페이지 조정이 발생했는지 여부 (true: 조정됨, false: 조정 안됨)
 * 
 * @example
 * import { adjustPageIfExceedsTotal } from '@/utils/paginationUtils';
 * 
 * async function fetchDataFromServer(page = 1, restoreScroll = false) {
 *   const response = await PostApi.getPosts(boardId.value, {
 *     page: page - 1,
 *     size: showPage.value,
 *   });
 *   
 *   currentPage.value = page;
 *   totalPages.value = response.data.totalPages;
 *   
 *   // 페이지 조정 필요 시 자동으로 재조회
 *   const adjusted = await adjustPageIfExceedsTotal({
 *     currentPageRef: currentPage,
 *     totalPages: totalPages.value,
 *     viewState,
 *     stateKey: 'infoNoticeBoard',
 *     fetchFunction: fetchDataFromServer,
 *   });
 *   
 *   if (adjusted) return; // 조정되었다면 재조회 완료
 *   
 *   // 일반적인 처리...
 * }
 */
export async function adjustPageIfExceedsTotal({
  currentPageRef,
  totalPages,
  viewState,
  stateKey,
  fetchFunction,
}) {
  // 현재 페이지가 총 페이지 수보다 크면 마지막 페이지로 이동
  if (totalPages > 0 && currentPageRef.value > totalPages) {
    const adjustedPage = totalPages;
    currentPageRef.value = adjustedPage;
    viewState.saveState(stateKey, {
      currentPage: currentPageRef.value,
      scrollY: 0,
    });
    // 조정된 페이지 데이터 조회
    await fetchFunction(adjustedPage, false);
    return true;
  }
  return false;
}

