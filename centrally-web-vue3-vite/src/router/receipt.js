// src/router/receipt.js
import AnnualReceiptSummary from '@/views/receipt/AnnualReceiptSummary.vue';
import PersonalReceiptHistory from '@/views/receipt/PersonalReceiptHistory.vue';
import ReceiptManagement from '@/views/receipt/ReceiptManagement.vue';
import ReceiptClosure from '@/views/receipt/ReceiptClosure.vue';
import ReceiptHistory from '@/views/receipt/ReceiptHistory.vue';
import ReceiptDelegateHistory from '@/views/receipt/ReceiptDelegateHistory.vue';
import ReceiptHistoryDetail from '@/views/receipt/ReceiptHistoryDetail.vue';
import ReceiptDelegateHistoryDetail from '@/views/receipt/ReceiptDelegateHistoryDetail.vue';

import ReceiptApprovalRequestOverview from '@/views/receipt/ReceiptApprovalRequestOverview.vue';
import ReceiptConcurrenceRequestOverview from '@/views/receipt/ReceiptConcurrenceRequestOverview.vue';
import ReceiptApprovalRequestDetails from '@/views/receipt/ReceiptApprovalRequestDetails.vue';
import ReceiptReportForCEO from '@/views/receipt/ReceiptReportForCEO.vue';
import ReceiptConcurrenceRequestDetails from '@/views/receipt/ReceiptConcurrenceRequestDetails.vue';

import ReceiptSubmission from '@/views/receipt/ReceiptSubmission.vue';
import ReceiptMetaManagement from '@/views/receipt/ReceiptMetaManagement.vue';
import ReceiptApprovalSummary from '@/views/receipt/ReceiptApprovalSummary.vue';
import ReceiptApprovalSummaryDetail from '@/views/receipt/ReceiptApprovalSummaryDetail.vue';
import { getRolesFrom } from '@/utils/roleUtils';
import ReceiptDelegateApprovalOverview from '@/views/receipt/ReceiptDelegateApprovalOverview.vue';
import ReceiptDelegateConcurrenceOverview from '@/views/receipt/ReceiptDelegateConcurrenceOverview.vue';
import ReceiptDelegateApprovalDetails from '@/views/receipt/ReceiptDelegateApprovalDetails.vue';
import ReceiptDelegateConcurrenceDetails from '@/views/receipt/ReceiptDelegateConcurrenceDetails.vue';
import ReceiptSubmissionCreateMobile from '@/views/receipt/mobile/ReceiptSubmissionCreateMobile.vue';
import ReceiptSubmissionEditMobile from '@/views/receipt/mobile/ReceiptSubmissionEditMobile.vue';
import ReceiptDetailViewMobile from '@/views/receipt/mobile/ReceiptDetailViewMobile.vue';
import { PATH_NAMES, ROUTE_NAMES, MENUS } from '@/config/routeConfig';
import { ROLE_GROUPS } from '@/config/roleConfig';

const receiptRoutes = [
  /**
   *  권한
   *    ROLE_RECEIPT_REGISTRAR : 등록자
   *    ROLE_RECEIPT_APPROVER  : 결재자
   *    ROLE_RECEIPT_MANAGER   : 관리자
   *
   * 등록자 : 등록, 상세 내용, 월별 정산 내역
   * 결재자 : [등록자] + 신청 내역, 개인 신청 내역, 월별 신청 상세 내역
   * 관리자 : [결재자] + 영수증 마감, 권한 부여
   *
   * 로그인 확인 필요 없을 시 : meta에 requiresAuth=false 추가
   * menu는 백엔드에서 이력관리에서 사용
   *
   */

  // --------------------- 공통 영역 ---------------------

  {
    // (공통) 영수증 등록
    path: PATH_NAMES.RECEIPT.SUBMISSION,
    name: ROUTE_NAMES.RECEIPT.SUBMISSION,
    component: ReceiptSubmission,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
      menu: MENUS.RECEIPT.SUBMISSION,
    },
  },
  {
    // (모바일 전용) 영수증 등록 상세 (모달 대체 풀스크린)
    path: PATH_NAMES.RECEIPT.SUBMISSION_CREATE_MOBILE,
    name: ROUTE_NAMES.RECEIPT.SUBMISSION_CREATE_MOBILE,
    component: ReceiptSubmissionCreateMobile,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
      menu: MENUS.RECEIPT.SUBMISSION_CREATE_MOBILE,
    },
  },
  {
    // (모바일 전용) 영수증 수정 페이지 (모달 대체 풀스크린)
    path: PATH_NAMES.RECEIPT.SUBMISSION_EDIT_MOBILE,
    name: ROUTE_NAMES.RECEIPT.SUBMISSION_EDIT_MOBILE,
    component: ReceiptSubmissionEditMobile,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
      menu: MENUS.RECEIPT.SUBMISSION_EDIT_MOBILE,
    },
  },
  {
    // (공통) 영수증 신청 내역 조회
    path: PATH_NAMES.RECEIPT.PERSONAL_HISTORY,
    name: ROUTE_NAMES.RECEIPT.PERSONAL_HISTORY,
    component: PersonalReceiptHistory,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
      menu: MENUS.RECEIPT.PERSONAL_HISTORY,
    },
  },
  {
    // (모바일 전용 공통) 영수증 상세 페이지
    path: PATH_NAMES.RECEIPT.DETAIL_MOBILE,
    name: ROUTE_NAMES.RECEIPT.DETAIL_MOBILE,
    component: ReceiptDetailViewMobile,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
      menu: MENUS.RECEIPT.DETAIL_MOBILE,
    },
  },
  {
    // (공통) 영수증 년도별 요약(마감된것만)
    path: PATH_NAMES.RECEIPT.ANNUAL_SUMMARY,
    name: ROUTE_NAMES.RECEIPT.ANNUAL_SUMMARY,
    component: AnnualReceiptSummary,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR),
      menu: MENUS.RECEIPT.ANNUAL_SUMMARY,
    },
  },

  // --------------------- 결재자, 검수자, 관리자 영역 ---------------------

  {
    // 영수증 결재 신청 내역 조회 현황
    path: PATH_NAMES.RECEIPT.APPROVAL_OVERVIEW,
    name: ROUTE_NAMES.RECEIPT.APPROVAL_OVERVIEW,
    component: ReceiptApprovalRequestOverview,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.APPROVAL_REQUEST,
    },
  },
  {
    // [대리] 영수증 결재 신청 내역 조회 현황 (분리 컴포넌트)
    path: PATH_NAMES.RECEIPT.DELEGATE_APPROVAL_OVERVIEW,
    name: ROUTE_NAMES.RECEIPT.DELEGATE_APPROVAL_OVERVIEW,
    component: ReceiptDelegateApprovalOverview,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.DELEGATE_APPROVAL,
    },
  },

  {
    // 영수증 결재 신청 내역 조회 현황 상세페이지
    path: PATH_NAMES.RECEIPT.APPROVAL_DETAILS,
    name: ROUTE_NAMES.RECEIPT.APPROVAL_DETAILS,
    component: ReceiptApprovalRequestDetails,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.APPROVAL_DETAIL,
    },
  },
  {
    // [대리] 영수증 결재 신청 내역 조회 현황 상세페이지 (분리 컴포넌트)
    path: PATH_NAMES.RECEIPT.DELEGATE_APPROVAL_DETAILS,
    name: ROUTE_NAMES.RECEIPT.DELEGATE_APPROVAL_DETAILS,
    component: ReceiptDelegateApprovalDetails,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.DELEGATE_APPROVAL_DETAIL,
    },
  },
  {
    // 영수증 합의 신청 내역 조회 현황
    path: PATH_NAMES.RECEIPT.CONCURRENCE_OVERVIEW,
    name: ROUTE_NAMES.RECEIPT.CONCURRENCE_OVERVIEW,
    component: ReceiptConcurrenceRequestOverview,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.CONCURRENCE_REQUEST,
    },
  },
  {
    // [대리] 영수증 합의 신청 내역 조회 현황 (분리 컴포넌트)
    path: PATH_NAMES.RECEIPT.DELEGATE_CONCURRENCE_OVERVIEW,
    name: ROUTE_NAMES.RECEIPT.DELEGATE_CONCURRENCE_OVERVIEW,
    component: ReceiptDelegateConcurrenceOverview,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.DELEGATE_CONCURRENCE,
    },
  },
  {
    // 영수증 합의 신청 내역 조회 현황 상세페이지
    path: PATH_NAMES.RECEIPT.CONCURRENCE_DETAILS,
    name: ROUTE_NAMES.RECEIPT.CONCURRENCE_DETAILS,
    component: ReceiptConcurrenceRequestDetails,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.CONCURRENCE_DETAIL,
    },
  },
  {
    // [대리] 영수증 합의 신청 내역 조회 현황 상세페이지 (분리 컴포넌트)
    path: PATH_NAMES.RECEIPT.DELEGATE_CONCURRENCE_DETAILS,
    name: ROUTE_NAMES.RECEIPT.DELEGATE_CONCURRENCE_DETAILS,
    component: ReceiptDelegateConcurrenceDetails,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.DELEGATE_CONCURRENCE_DETAIL,
    },
  },

  {
    // 영수증 결재(합의) 내역 조회
    path: PATH_NAMES.RECEIPT.HISTORY,
    name: ROUTE_NAMES.RECEIPT.HISTORY,
    component: ReceiptHistory,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.HISTORY,
    },
  },
  {
    // [대리] 영수증 결재(합의) 내역 조회
    path: PATH_NAMES.RECEIPT.DELEGATE_HISTORY,
    name: ROUTE_NAMES.RECEIPT.DELEGATE_HISTORY,
    component: ReceiptDelegateHistory,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.DELEGATE_HISTORY,
    },
  },
  {
    // 영수증 내역 상세 조회
    path: PATH_NAMES.RECEIPT.HISTORY_DETAIL,
    name: ROUTE_NAMES.RECEIPT.HISTORY_DETAIL,
    component: ReceiptHistoryDetail,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      menu: MENUS.RECEIPT.HISTORY_DETAIL,
    },
  },
  {
    // [대리] 영수증 내역 상세 조회
    path: PATH_NAMES.RECEIPT.DELEGATE_HISTORY_DETAIL,
    name: ROUTE_NAMES.RECEIPT.DELEGATE_HISTORY_DETAIL,
    component: ReceiptDelegateHistoryDetail,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER),
      delegate: true,
      menu: MENUS.RECEIPT.DELEGATE_HISTORY_DETAIL,
    },
  },

  {
    // 영수증 승인(마감) 현황
    path: PATH_NAMES.RECEIPT.APPROVAL_SUMMARY,
    name: ROUTE_NAMES.RECEIPT.APPROVAL_SUMMARY,
    component: ReceiptApprovalSummary,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR),
      menu: MENUS.RECEIPT.APPROVAL_SUMMARY,
    },
  },
  {
    // 영수증 승인(마감) 현황 상세 (Excel 다운로드)
    path: PATH_NAMES.RECEIPT.APPROVAL_SUMMARY_DETAIL,
    name: ROUTE_NAMES.RECEIPT.APPROVAL_SUMMARY_DETAIL,
    component: ReceiptApprovalSummaryDetail,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR),
      menu: MENUS.RECEIPT.APPROVAL_SUMMARY_DETAIL,
    },
  },
  {
    // 영수증 제출용 요약
    path: PATH_NAMES.RECEIPT.REPORT_FOR_CEO,
    name: ROUTE_NAMES.RECEIPT.REPORT_FOR_CEO,
    component: ReceiptReportForCEO,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR),
      menu: MENUS.RECEIPT.REPORT_CEO,
    },
  },
  {
    // 영수증 결제 마감
    path: PATH_NAMES.RECEIPT.CLOSURE,
    name: ROUTE_NAMES.RECEIPT.CLOSURE,
    component: ReceiptClosure,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR),
      menu: MENUS.RECEIPT.CLOSURE,
    },
  },

  // --------------------- 관리자 영역 ---------------------
  {
    // 영수증 설정
    path: PATH_NAMES.RECEIPT.META_MANAGEMENT,
    name: ROUTE_NAMES.RECEIPT.META_MANAGEMENT,
    component: ReceiptMetaManagement,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_MANAGER),
      menu: MENUS.RECEIPT.META_MANAGEMENT,
    },
  },
  {
    // 영수증 마감
    path: PATH_NAMES.RECEIPT.MANAGEMENT,
    name: ROUTE_NAMES.RECEIPT.MANAGEMENT,
    component: ReceiptManagement,
    meta: {
      allowedRoles: getRolesFrom(ROLE_GROUPS.RECEIPT_MANAGER),
      menu: MENUS.RECEIPT.MANAGEMENT,
    },
  },
  // {
  //   // 영수증 마감
  //   path: 'receipt-management',
  //   name: 'ReceiptClosure',
  //   component: ReceiptClosure,
  //   meta: {
  //     allowedRoles: ['ROLE_RECEIPT_MANAGER'], //관리자
  //     menu: '영수증 마감'
  //   },
  // },
];

export default receiptRoutes;
