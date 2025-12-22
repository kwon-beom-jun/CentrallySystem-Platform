<template>
  <!-- style 인라인 바인딩으로 크기 제어 -->
  <div
    ref="mapEl"
    :style="{
      width: toUnit(width),
      height: toUnit(height),
      borderRadius: '8px',
      boxShadow: '0 4px 12px rgba(0,0,0,.15)',
    }"
  />
  </template>
  
<script setup>
import { ref, onMounted } from 'vue'

/* ---------- Props ---------- */
defineProps({
  width : { type: [Number, String], default: 600 },  // px 또는 %
  height: { type: [Number, String], default: 300 },
})

/* width=600 → '600px', width='100%' → '100%' */
const toUnit = v => typeof v === 'number' ? `${v}px` : v

/* ---------- Kakao SDK 로드 ---------- */
/**
 * Kakao Maps SDK 로드 함수
 * @param {Function} cb - SDK 로드 완료 후 실행할 콜백 함수
 */
function loadSdk(cb) {
  if (window.kakao && window.kakao.maps) { 
    kakao.maps.load(cb); 
    return 
  }

  const appkey = import.meta.env.VITE_KAKAO_MAP_JAVASCRIPT_API_KEY;
  
  // 환경 변수 체크
  if (!appkey) {
    console.error('[MainMap] VITE_KAKAO_MAP_JAVASCRIPT_API_KEY 환경 변수가 설정되지 않았습니다.');
    return;
  }

  const s = document.createElement('script');
  const cleanAppkey = appkey.replace(/\/$/, '');
  
  s.src   = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${cleanAppkey}&autoload=false`
  s.async = true
  s.onload = () => {
    if (window.kakao && window.kakao.maps) {
      kakao.maps.load(cb);
    } else {
      console.error('[MainMap] Kakao Maps SDK 로드 실패');
    }
  }
  s.onerror = () => {
    console.error('[MainMap] Kakao Maps SDK 스크립트 로드 중 에러 발생');
  }
  document.head.appendChild(s)
}

/* ---------- 지도 초기화 ---------- */
const mapEl = ref(null)

/**
 * 지도 컴포넌트 마운트 시 실행
 */
onMounted(() => {
  loadSdk(() => {
    if (!mapEl.value) {
      console.warn('[MainMap] 지도 컨테이너 요소를 찾을 수 없습니다.');
      return;
    }

    try {
      const center = new kakao.maps.LatLng(37.5000, 126.9094)
      const map    = new kakao.maps.Map(mapEl.value, { center, level: 3 })

      // 원형 마커 아이콘 생성
      const markerSize = 50
      const markerDiv = document.createElement('div')
      markerDiv.style.width = `${markerSize}px`
      markerDiv.style.height = `${markerSize}px`
      markerDiv.style.borderRadius = '50%'
      markerDiv.style.backgroundImage = 'url(/img/common/logo/main.png)'
      markerDiv.style.backgroundSize = 'cover'
      markerDiv.style.backgroundPosition = 'center'
      markerDiv.style.border = '3px solid white'
      markerDiv.style.boxShadow = '0 2px 8px rgba(0,0,0,0.3)'

      const marker = new kakao.maps.CustomOverlay({
        position: center,
        content: markerDiv,
        yAnchor: 0.5,
        xAnchor: 0.5,
      })
      marker.setMap(map)
    } catch (error) {
      console.error('[MainMap] 지도 초기화 중 에러 발생:', error);
    }
  })
})
</script>
  
<style scoped>
/* ❹ 추가: 지도 영역 */
/* .map-container {
  width: 100%;
  max-width: 600px;
  height: 300px;
  margin: 40px auto 0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,.15);
} */
</style>