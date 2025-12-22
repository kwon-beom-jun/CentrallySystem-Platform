import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
  // 상태 (state)
  state: () => ({
    user: null,
    userId: null,
    userEmail: null,
    userProfileImgUrl: null,
    roles: [],
    isAuthenticated: false,
    cardStyle: 'default',
    // socialLinks: [],
  }),

  // 게터 (getters)
  getters: {
    isLoggedIn: (state) => state.isAuthenticated,
    getUser: (state) => state.user,
    getUserId: (state) => state.userId,
    getUserEmail: (state) => state.userEmail,
    getUserProfileImgUrl: (state) => state.userProfileImgUrl,
    getRoles: (state) => state.roles,
    getCardStyle: (state) => state.cardStyle || 'default',
  },

  // 액션 (actions)
  actions: {
    // (A) 로그인 시 사용자 정보 세팅
    login(userData) {
      // userData = { username: '...', roles: ['ROLE_HRM_EMPLOYEE', 'ROLE_HRM_MANAGER'] } 등
      this.user = userData.username;
      this.userId = userData.userId;
      this.userEmail = userData.userEmail;
      this.userProfileImgUrl = userData.userProfileImgUrl;
      this.roles = userData.roles || [];
      this.cardStyle = userData.cardStyle || 'default';
      this.isAuthenticated = true;
      // this.socialLinks = payload.socialLinks || [];
    },

    // (B) 로그아웃 시 상태 초기화
    logout() {
      this.user = null;
      this.userId = null;
      this.userEmail = null;
      this.userProfileImgUrl = null;
      this.roles = [];
      this.cardStyle = 'default';
      this.isAuthenticated = false;
      // this.socialLinks = [];
    },

    // (C) 카드 스타일 업데이트
    updateCardStyle(cardStyle) {
      this.cardStyle = cardStyle;
    },

    /** 권한 코드 추가 (중복 방지) */
    /* addRole(roleCode) {
      if (!this.roles.includes(roleCode)) {
        this.roles.push(roleCode);
      }
    }, */
  
    /** 권한 코드 제거 */
    /* removeRole(roleCode) {
      this.roles = this.roles.filter(r => r !== roleCode);
    } */
  },
  // pinia-plugin-persistedstate 설정
  //  - Pinia 상태는 기본적으로 메모리에만 존재하므로,
  //    새로고침(또는 탭/브라우저 종료 후 재접속)하면 모든 상태가 초기화되므로
  //    스토어 상태를 브라우저 저장소(LocalStorage)에 저장
  persist: true,
});