import { defineStore } from 'pinia';

export const useMobileMenuStore = defineStore('mobileMenu', {
  state: () => ({
    menuOpen: false,   // “≡” 풀메뉴(drawer)
    sheetOpen: false,  // “＋” 퀵시트
  }),
  actions: {
    openMenu()  { this.menuOpen  = true;  },
    closeMenu() { this.menuOpen  = false; },
    toggleMenu(){ this.menuOpen  = !this.menuOpen; },

    openSheet() { this.sheetOpen = true;  },
    closeSheet(){ this.sheetOpen = false; },

    /** 두 레이어 모두 한 번에 닫기 */
    reset()     {
      this.menuOpen  = false;
      this.sheetOpen = false;
    },
  },
});
