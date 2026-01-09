/**
 * 스크롤에 따른 헤더 배경 효과를 위한 composable
 */
import { onMounted, onUnmounted } from 'vue'

export function useScrollEffect(selector = '.content-mobile-detail-title', threshold = 50) {
  let element = null
  let lastScrollY = 0

  /**
   * 스크롤 이벤트 핸들러
   */
  const handleScroll = () => {
    if (!element) return

    const currentScrollY = window.scrollY
    
    // 스크롤 위치가 임계값보다 클 때 scrolled 클래스 추가
    if (currentScrollY > threshold) {
      if (!element.classList.contains('scrolled')) {
        element.classList.add('scrolled')
      }
    } else {
      if (element.classList.contains('scrolled')) {
        element.classList.remove('scrolled')
      }
    }

    lastScrollY = currentScrollY
  }

  /**
   * 초기화 함수
   */
  const init = () => {
    element = document.querySelector(selector)
    if (element) {
      // 초기 상태 설정
      handleScroll()
      
      // 스크롤 이벤트 리스너 등록 (throttle 적용)
      let ticking = false
      const scrollListener = () => {
        if (!ticking) {
          requestAnimationFrame(() => {
            handleScroll()
            ticking = false
          })
          ticking = true
        }
      }
      
      window.addEventListener('scroll', scrollListener, { passive: true })
      
      return scrollListener
    }
    return null
  }

  let scrollListener = null

  onMounted(() => {
    // DOM이 준비된 후 약간의 지연을 두고 초기화
    setTimeout(() => {
      scrollListener = init()
    }, 100)
  })

  onUnmounted(() => {
    if (scrollListener) {
      window.removeEventListener('scroll', scrollListener)
    }
  })

  return {
    init
  }
}
