/**
 * 전역 스크롤 효과 - content-mobile-detail-title용
 * composable이 아닌 일반 JavaScript 함수로 구현
 */

let isInitialized = false
let currentScrollListener = null

/**
 * 스크롤 효과 초기화
 */
export function initScrollEffect() {
  // 이미 초기화되었다면 기존 리스너 제거
  if (currentScrollListener) {
    window.removeEventListener('scroll', currentScrollListener)
    currentScrollListener = null
  }

  const element = document.querySelector('.content-mobile-detail-title')
  if (!element) {
    console.log('content-mobile-detail-title 요소를 찾을 수 없습니다.')
    return
  }

  console.log('스크롤 효과 초기화됨:', element)

  const threshold = 10 // 임계값을 낮춰서 테스트
  let ticking = false

  const handleScroll = () => {
    if (!element) return

    const currentScrollY = window.scrollY
    
    // 디버깅용 로그
    // console.log(`스크롤 위치: ${currentScrollY}, 임계값: ${threshold}`)
    
    if (currentScrollY > threshold) {
      if (!element.classList.contains('scrolled')) {
        element.classList.add('scrolled')
        // console.log('scrolled 클래스 추가됨')
      }
    } else {
      if (element.classList.contains('scrolled')) {
        element.classList.remove('scrolled')
        // console.log('scrolled 클래스 제거됨')
      }
    }
  }

  // 스크롤 리스너 (throttle 적용)
  const scrollListener = () => {
    if (!ticking) {
      requestAnimationFrame(() => {
        handleScroll()
        ticking = false
      })
      ticking = true
    }
  }

  // 이벤트 리스너 등록
  window.addEventListener('scroll', scrollListener, { passive: true })
  currentScrollListener = scrollListener

  // 초기 상태 설정
  handleScroll()

  // console.log('스크롤 이벤트 리스너 등록 완료')
}

/**
 * 스크롤 효과 정리
 */
export function cleanupScrollEffect() {
  if (currentScrollListener) {
    window.removeEventListener('scroll', currentScrollListener)
    currentScrollListener = null
    // console.log('스크롤 이벤트 리스너 제거됨')
  }
}
