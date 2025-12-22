// 모바일 브레이크포인트 감지를 위한 전역 컴포저블
// 여러 컴포넌트에서 동시에 호출해도 resize 리스너는 한 번만 등록됩니다.

import { ref, onBeforeUnmount } from 'vue'

const MOBILE_MAX = 650  // 필요 시 태블릿, 데스크탑 등의 값도 추가 정의할 수 있습니다.

// 전역 reactive 상태 (컴포저블 여러 번 호출 시 공유)
const isMobile = ref(false)

let listenerAdded = false
function update () {
  isMobile.value = window.innerWidth <= MOBILE_MAX
}

export default function useBreakPoint () {
  if (!listenerAdded) {
    update() // 초기 계산
    window.addEventListener('resize', update)
    listenerAdded = true
  }

  // 호출한 컴포넌트가 언마운트될 때 특별히 listener를 제거할 필요는 없지만
  // 필요시 다음과 같이 옵션을 둘 수도 있다.
  onBeforeUnmount(() => {
    /*
    if (listenerAdded) {
      window.removeEventListener('resize', update)
      listenerAdded = false
    }
    */
  })

  return { isMobile }
} 