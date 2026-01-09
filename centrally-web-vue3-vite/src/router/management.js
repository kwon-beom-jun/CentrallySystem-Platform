// src/router/management.js
const UserManagementModifyMobile = () => import('@/views/hrm/mobile/UserManagementModifyMobile.vue');
const FavoriteMenuManagement  = () => import('@/views/hrm/FavoriteMenuManagement.vue');
const UserManagement          = () => import('@/views/hrm/UserManagement.vue');
const DeptTeamManager         = () => import('@/views/hrm/DeptTeamManager.vue');
const PerformanceHistory      = () => import('@/views/hrm/PerformanceHistory.vue');
const PerformanceManagement   = () => import('@/views/hrm/PerformanceManagement.vue');
const UserPermissions         = () => import('@/views/hrm/UserPermissions.vue');
const OrgDirectory            = () => import('@/views/hrm/OrgDirectory.vue');
import { getRolesFrom } from '@/utils/roleUtils';
import { PATH_NAMES, ROUTE_NAMES, MENUS } from '@/config/routeConfig';
import { ROLE_GROUPS } from '@/config/roleConfig';

/**
 * 사원관리 라우트
 */
const managementRoutes = [
  {
    // 즐겨찾기 관리
    path: PATH_NAMES.MANAGEMENT.FAVORITE_MENU_MANAGEMENT,
    name: ROUTE_NAMES.MANAGEMENT.FAVORITE_MENU_MANAGEMENT,
    component: FavoriteMenuManagement,
    meta: {
      requiresAuth: false, // 전 사원 사용 가능
      menu: MENUS.HRM.FAVORITE_MENU,
      workspace: 'favorites' // 즐겨찾기 워크스페이스 유지
    },
  },
  {
    // 사용자 관리
    path: PATH_NAMES.MANAGEMENT.USER_MANAGEMENT,
    name: ROUTE_NAMES.MANAGEMENT.USER_MANAGEMENT,
    component: UserManagement,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER),
      menu: MENUS.HRM.USER_MANAGEMENT
    },
  },
  {
    // 사용자 수정 (모바일)
    path: PATH_NAMES.MANAGEMENT.USER_MODIFY_MOBILE,
    name: ROUTE_NAMES.MANAGEMENT.USER_MODIFY_MOBILE,
    component: UserManagementModifyMobile,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER),
      menu: MENUS.HRM.USER_MODIFY_MOBILE
    },
  },
  {
    // 부서/팀 관리
    path: PATH_NAMES.MANAGEMENT.DEPT_TEAM_MANAGER,
    name: ROUTE_NAMES.MANAGEMENT.DEPT_TEAM_MANAGER,
    component: DeptTeamManager,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER),
      menu: MENUS.HRM.DEPT_TEAM
    },
  },
  {
    // 조직도
    path: PATH_NAMES.MANAGEMENT.ORG_DIRECTORY,
    name: ROUTE_NAMES.MANAGEMENT.ORG_DIRECTORY,
    component: OrgDirectory,
    meta: {
      requiresAuth: false, // 전 사원 사용 가능
      menu: MENUS.HRM.ORG_DIRECTORY
    },
  },
  {
    // 실적 이력
    path: PATH_NAMES.MANAGEMENT.PERFORMANCE_HISTORY,
    name: ROUTE_NAMES.MANAGEMENT.PERFORMANCE_HISTORY,
    component: PerformanceHistory,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.HRM_EMPLOYEE),
      menu: MENUS.HRM.PERFORMANCE_HISTORY
    },
  },
  {
    // 실적 관리
    path: PATH_NAMES.MANAGEMENT.PERFORMANCE_MANAGEMENT,
    name: ROUTE_NAMES.MANAGEMENT.PERFORMANCE_MANAGEMENT,
    component: PerformanceManagement,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER),
      menu: MENUS.HRM.PERFORMANCE_MANAGEMENT
    },
  },
  {
    // 사용자 권한
    path: PATH_NAMES.MANAGEMENT.USER_PERMISSIONS,
    name: ROUTE_NAMES.MANAGEMENT.USER_PERMISSIONS,
    component: UserPermissions,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.HRM_MANAGER),
      menu: MENUS.HRM.USER_PERMISSIONS
    },
  },
];

export default managementRoutes;
