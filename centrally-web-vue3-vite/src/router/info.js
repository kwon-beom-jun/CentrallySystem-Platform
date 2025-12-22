// src/router/info.js
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

