// src/router/info.js
const ScheduleCalendar     = () => import('@/views/info/ScheduleCalendar.vue');
const ScheduleDayDetail    = () => import('@/views/info/mobile/ScheduleDayDetail.vue');
const ScheduleStatistics   = () => import('@/views/info/ScheduleStatistics.vue');
const ScheduleSettings     = () => import('@/views/info/ScheduleSettings.vue');
const ScheduleCreateMobile = () => import('@/views/info/mobile/ScheduleCreateMobile.vue');
const ScheduleEditMobile   = () => import('@/views/info/mobile/ScheduleEditMobile.vue');
const ScheduleTypeManagement = () => import('@/views/info/ScheduleTypeManagement.vue');
const ScheduleNotificationHistory = () => import('@/views/info/ScheduleNotificationHistory.vue');
const ScheduleNotificationHistoryDetailMobile = () => import('@/views/info/mobile/ScheduleNotificationHistoryDetailMobile.vue');
const InfoNoticeBoard      = () => import('@/views/info/NoticeBoard.vue');
const InfoResourceBoard    = () => import('@/views/info/ResourceBoard.vue');
const InfoCommunityBoard   = () => import('@/views/info/CommunityBoard.vue');
const InfoPostDetail       = () => import('@/components/info/PostDetail.vue');
const InfoPostCreateMobile = () => import('@/views/info/mobile/PostCreateMobile.vue');
const InfoPostEditMobile   = () => import('@/views/info/mobile/PostEditMobile.vue');
const GuidePage            = () => import('@/views/GuidePage.vue');
import { getRolesFrom } from '@/utils/roleUtils';
import { PATH_NAMES, ROUTE_NAMES, MENUS } from '@/config/routeConfig';
import { ROLE_GROUPS } from '@/config/roleConfig';

/**
 * INFO 라우트
 */
const infoRoutes = [
  {
    // 일정
    path: PATH_NAMES.INFO.SCHEDULE,
    name: ROUTE_NAMES.INFO.SCHEDULE,
    component: ScheduleCalendar,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.SCHEDULE
    },
  },
  {
    // 일정 상세 (모바일)
    path: '/info/schedule-day-detail/:date',
    name: ROUTE_NAMES.INFO.SCHEDULE_DAY_DETAIL,
    component: ScheduleDayDetail,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.SCHEDULE_DAY_DETAIL
    },
  },
  {
    // 일정 통계
    path: PATH_NAMES.INFO.SCHEDULE_STATISTICS,
    name: ROUTE_NAMES.INFO.SCHEDULE_STATISTICS,
    component: ScheduleStatistics,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.SCHEDULE_STATISTICS
    },
  },
  {
    // 일정 설정
    path: PATH_NAMES.INFO.SCHEDULE_SETTINGS,
    name: ROUTE_NAMES.INFO.SCHEDULE_SETTINGS,
    component: ScheduleSettings,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.SCHEDULE_SETTINGS
    },
  },
  {
    // 일정 등록 (모바일)
    path: PATH_NAMES.INFO.SCHEDULE_CREATE_MOBILE,
    name: ROUTE_NAMES.INFO.SCHEDULE_CREATE_MOBILE,
    component: ScheduleCreateMobile,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.SCHEDULE_CREATE_MOBILE
    },
  },
  {
    // 일정 수정 (모바일)
    path: PATH_NAMES.INFO.SCHEDULE_EDIT_MOBILE,
    name: ROUTE_NAMES.INFO.SCHEDULE_EDIT_MOBILE,
    component: ScheduleEditMobile,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.SCHEDULE_EDIT_MOBILE
    },
  },
  {
    // 일정 유형 관리
    path: PATH_NAMES.INFO.SCHEDULE_TYPE_MANAGEMENT,
    name: ROUTE_NAMES.INFO.SCHEDULE_TYPE_MANAGEMENT,
    component: ScheduleTypeManagement,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_ADMIN),
      menu: MENUS.INFO.SCHEDULE_TYPE_MANAGEMENT
    },
  },
  {
    // 알림 발송 이력
    path: PATH_NAMES.INFO.SCHEDULE_NOTIFICATION_HISTORY,
    name: ROUTE_NAMES.INFO.SCHEDULE_NOTIFICATION_HISTORY,
    component: ScheduleNotificationHistory,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_ADMIN),
      menu: MENUS.INFO.SCHEDULE_NOTIFICATION_HISTORY
    },
  },
  {
    // 알림 발송 이력 상세 (모바일)
    path: PATH_NAMES.INFO.SCHEDULE_NOTIFICATION_HISTORY_DETAIL_MOBILE,
    name: ROUTE_NAMES.INFO.SCHEDULE_NOTIFICATION_HISTORY_DETAIL_MOBILE,
    component: ScheduleNotificationHistoryDetailMobile,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_ADMIN),
      menu: MENUS.INFO.SCHEDULE_NOTIFICATION_HISTORY_DETAIL_MOBILE
    },
  },
  {
    // 공지사항
    path: PATH_NAMES.INFO.NOTICE,
    name: ROUTE_NAMES.INFO.NOTICE,
    component: InfoNoticeBoard,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.NOTICE
    },
  },
  {
    // 자료실
    path: PATH_NAMES.INFO.RESOURCE,
    name: ROUTE_NAMES.INFO.RESOURCE,
    component: InfoResourceBoard,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.RESOURCE
    },
  },
  {
    // 자유게시판
    path: PATH_NAMES.INFO.COMMUNITY,
    name: ROUTE_NAMES.INFO.COMMUNITY,
    component: InfoCommunityBoard,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.COMMUNITY
    },
  },
  {
    // 게시글 상세
    path: PATH_NAMES.INFO.POST_DETAIL,
    name: ROUTE_NAMES.INFO.POST_DETAIL,
    component: InfoPostDetail,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.POST_DETAIL
    },
  },
  {
    // 게시글 생성 (모바일)
    path: PATH_NAMES.INFO.POST_CREATE_MOBILE,
    name: ROUTE_NAMES.INFO.POST_CREATE_MOBILE,
    component: InfoPostCreateMobile,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.POST_CREATE_MOBILE
    },
  },
  {
    // 게시글 수정 (모바일)
    path: PATH_NAMES.INFO.POST_EDIT_MOBILE,
    name: ROUTE_NAMES.INFO.POST_EDIT_MOBILE,
    component: InfoPostEditMobile,
    meta: { 
      allowedRoles: getRolesFrom(ROLE_GROUPS.INFO_GUEST),
      menu: MENUS.INFO.POST_EDIT_MOBILE
    },
  },
  {
    // 가이드
    path: PATH_NAMES.INFO.GUIDE,
    name: ROUTE_NAMES.INFO.GUIDE,
    component: GuidePage,
    meta: { 
      menu: MENUS.INFO.GUIDE
    },
  },
];

export default infoRoutes;

