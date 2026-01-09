// /store/hrm/userDirectory.js
import { defineStore } from 'pinia'
import AuthUserPermissionApi from '@/api/auth/UserPermissionApi'
import HrmUserApi            from '@/api/hrm/UsersApi'

const COOLDOWN = 3_000

export const useUserDirectoryStore = defineStore('userDirectory', {
  state: () => ({
    users         : null,   // [{ userId, name, email, …, roles:[…] }]
    allRoles      : null,   // [{ roleId, roleNameDetail, serviceName }]
    loadingPromise: null,   // 중복 호출 방지용
    lastFetched   : 0       // 마지막 성공 시각(ms)
  }),

  actions: {
    async ensureLoaded () {
      /* 이미 로딩 중이면 → 같은 promise 리턴 (중복 call 방지)  */
      if (this.loadingPromise) return this.loadingPromise;
      /* 캐시가 있고 3 초 안이면 – 그대로 반환 */
      const now = Date.now()
      if (this.users && now - this.lastFetched < COOLDOWN)
        return this.users

      this.loadingPromise = Promise.all([
        AuthUserPermissionApi.getUsersWithRoles(), // 역할·권한
        HrmUserApi.getUsersBasic()                 // 팀·부서(가벼운 버전)
      ])
      .then(([roleRes, basicRes]) => {
        /* team / department 를 userId ⇒ user 매핑으로 바꿔 두기 */
        const basicMap = Object.fromEntries(
          (basicRes.data || []).map(u => [u.userId, u])
        )

        /* 두 응답을 병합해 users 배열 생성 */
        this.users = (roleRes.data.userWithRolesList || []).map(u => {
          const b = basicMap[u.userId] || {}
          return {
            ...u,
            department : b.department ?? '미지정',
            team       : b.team       ?? '미지정'
          }
        })

        this.allRoles = roleRes.data.authRoles.allRoles
        this.lastFetched = Date.now()
        return this.users
      })
      .finally(() => { this.loadingPromise = null })
      
      return this.loadingPromise
    },
  },

  getters: {
    /* userId → 사용자 */
    getById: state => id =>
      (state.users || []).find(u => u.userId === id) || null,

    /* 조건(service, roleDetail)으로 필터 */
    getByRole: state => (service, roleDetail) =>
      (state.users || []).filter(u =>
        u.roles.some(r =>
          (service     ? r.serviceName     === service     : true) &&
          (roleDetail  ? r.roleNameDetail  === roleDetail : true)
        )),

    /* 특정 사용자가 역할을 갖고 있는지 */
    hasRole: (state, getters) => (userId, service, roleDetail) =>
      getters.getByRole(service, roleDetail).some(u => u.userId === userId)
  }
})
