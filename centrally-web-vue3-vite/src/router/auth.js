const OAuthCallback    = () => import('@/views/auth/OAuthCallback.vue');
const TempUserApprovals = () => import('@/views/auth/TempUserApprovals.vue');
import { getRolesFrom } from '@/utils/roleUtils';
import { PATH_NAMES, ROUTE_NAMES, MENUS } from '@/config/routeConfig';
import { ROLE_GROUPS } from '@/config/roleConfig';

/**
 * 로그인 확인 필요 없을 시 : meta에 requiresAuth=false 추가
 * menu는 백엔드에서 이력관리에서 사용
 */
const authRoutes = [
  {
    // OAuth 로그인 후 콜백 페이지
    path: PATH_NAMES.AUTH.OAUTH_CALLBACK,
    name: ROUTE_NAMES.AUTH.OAUTH_CALLBACK,
    component: OAuthCallback,
    meta: { 
      requiresAuth: false,
      menu: MENUS.AUTH.OAUTH_CALLBACK
    },
  },
  {
    path: PATH_NAMES.AUTH.TEMP_USER_APPROVALS,
    name: ROUTE_NAMES.AUTH.TEMP_USER_APPROVALS,
    component: TempUserApprovals,
    meta: { 
      // allowedRoles: ['ROLE_HRM_MANAGER'],
      // middle-manager 포함 (리더 이상)
      allowedRoles: getRolesFrom(ROLE_GROUPS.HRM_LEADER),
      menu: MENUS.AUTH.TEMP_USER_APPROVAL
    },
  },
];

export default authRoutes;
