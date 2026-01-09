// src/composables/useVisualViewport.ts
import { onMounted, onUnmounted } from 'vue';

export function useVisualViewport () {
  const update = () => {
    const h = (window.visualViewport?.height ?? window.innerHeight) * 0.01;
    document.documentElement.style.setProperty('--vvh', `${h}px`);
  };
  onMounted(() => {
    update();
    window.visualViewport?.addEventListener('resize', update);
    window.addEventListener('resize', update);          // 구형 대응
  });
  onUnmounted(() => {
    window.visualViewport?.removeEventListener('resize', update);
    window.removeEventListener('resize', update);
  });
}
