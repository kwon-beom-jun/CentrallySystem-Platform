// src/router/ststem.js
const ActivityLog       = () => import('@/views/system/ActivityLog.vue');
const StatisticsScreen  = () => import('@/views/system/StatisticsScreen.vue');
const RoleManagement    = () => import('@/views/system/RoleManagement.vue');
const ComponentTest     = () => import('@/views/system/ComponentTest.vue');
const KafkaOutboxMonitor = () => import('@/views/system/KafkaOutboxMonitor.vue');
const PersonalHistory   = () => import('@/views/system/PersonalHistory.vue');
import { getRolesFrom } from '@/utils/roleUtils';
import { PATH_NAMES, ROUTE_NAMES, MENUS } from '@/config/routeConfig';
import { ROLE_GROUPS } from '@/config/roleConfig';

/**
 * 권한
 *   ROLE_GATE_SYSTEM  : 모든 권한
 * 
 * 로그인 확인 필요 없을 시 : meta에 requiresAuth=false 추가
 * menu는 백엔드에서 이력관리에서 사용
 * 
 */
const systemRoutes = [
  {
    // 이력 관리
    path: PATH_NAMES.SYSTEM.ACTIVITY_LOG,
    name: ROUTE_NAMES.SYSTEM.ACTIVITY_LOG,
    component: ActivityLog,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
      menu: MENUS.SYSTEM.ACTIVITY_LOG
    },
  },
  {
    // Kafka Outbox
    path: PATH_NAMES.SYSTEM.KAFKA_OUTBOX,
    name: ROUTE_NAMES.SYSTEM.KAFKA_OUTBOX,
    component: KafkaOutboxMonitor,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
      menu: MENUS.SYSTEM.KAFKA_OUTBOX
    }
  },
  {
    // 통계 관리
    path: PATH_NAMES.SYSTEM.STATISTICS_SCREEN,
    name: ROUTE_NAMES.SYSTEM.STATISTICS_SCREEN,
    component: StatisticsScreen,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
      menu: MENUS.SYSTEM.STATISTICS
    },
  },
  {
    // 권한 관리
    path: PATH_NAMES.SYSTEM.ROLE_MANAGEMENT,
    name: ROUTE_NAMES.SYSTEM.ROLE_MANAGEMENT,
    component: RoleManagement,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
      menu: MENUS.SYSTEM.AUTHORITY_MANAGEMENT
    },
  },
  {
    path: PATH_NAMES.SYSTEM.COMPONENT_TEST,
    name: ROUTE_NAMES.SYSTEM.COMPONENT_TEST,
    component: ComponentTest,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM),
      menu: MENUS.SYSTEM.COMPONENT_TEST
    },
  },
];

export default systemRoutes;
