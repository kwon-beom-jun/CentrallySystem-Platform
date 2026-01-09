// src/directives/clickAway.js
export default {
  /** el: 지시자가 붙은 요소(=사이드바 wrapper)
   *  binding.value = { handler: Function, exclude: [CSS selector...] }
   */
  beforeMount(el, binding) {
    const { handler, exclude = [] } = binding.value;

    el.__clickAway__ = (e) => {
      const t = e.target;

      // ① 사이드바 내부라면 무시
      if (el.contains(t)) return;

      // ② 제외(selectors) 중 하나에 포함되면 무시
      if (exclude.some((sel) => t.closest(sel))) return;

      // ③ 그 외 → 핸들러 실행
      handler(e);
    };

    document.addEventListener('click', el.__clickAway__);
  },
  unmounted(el) {
    document.removeEventListener('click', el.__clickAway__);
    delete el.__clickAway__;
  },
};
