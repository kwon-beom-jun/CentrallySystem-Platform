// src/router/user.js
const UserInformation         = () => import('@/views/hrm/UserInformation.vue');
const PersonalHistory         = () => import('@/views/system/PersonalHistory.vue');
import { PATH_NAMES, ROUTE_NAMES, MENUS } from '@/config/routeConfig';

/**
 * 개인정보 라우트
 */
const userRoutes = [
  {
    // 사용자 정보
    path: PATH_NAMES.USER.USER_INFORMATION,
    name: ROUTE_NAMES.USER.USER_INFORMATION,
    component: UserInformation,
    meta: { 
      requiresAuth: false, // 전 사원 사용 가능
      menu: MENUS.HRM.USER_INFO
    },
  },
  {
    // 개인 이력
    path: PATH_NAMES.USER.PERSONAL_HISTORY,
    name: ROUTE_NAMES.USER.PERSONAL_HISTORY,
    component: PersonalHistory,
    meta: {
      allowedRoles: [], // 모든 사용자
      menu: MENUS.SYSTEM.PERSONAL_HISTORY
    },
  },
];

export default userRoutes;
