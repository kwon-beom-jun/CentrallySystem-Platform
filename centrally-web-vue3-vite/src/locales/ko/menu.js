/**
 * 메뉴 다국어 메시지 (한글)
 * menuConfig.js의 모든 메뉴 라벨과 카테고리 i18n 키 정의
 */
export default {
  // AUTH 메뉴
  auth: {
    login: '로그인',
    join: '회원가입',
    oauthCallback: 'OAuth 로그인 콜백',
  },

  // INFO 메뉴
  info: {
    calendar: '캘린더',
    scheduleStatistics: '일정 통계',
    scheduleSettings: '일정 설정',
    scheduleTypeManagement: '일정 유형 관리',
    scheduleNotificationHistory: '알림 발송 이력',
    notice: '공지사항',
    resource: '자료실',
    community: '자유게시판',
    schedule: '일정',
    guide: '가이드',
    postDetail: '게시글 상세',
    postCreateMobile: '게시글 등록(모바일)',
    postEditMobile: '게시글 수정(모바일)',
  },

  // 영수증 메뉴
  receipt: {
    submission: '영수증 등록',
    submissionCreateMobile: '영수증 등록 세부(모바일)',
    submissionEditMobile: '영수증 수정 세부(모바일)',
    personalHistory: '신청 내역',
    detailMobile: '영수증 상세(모바일)',
    annualSummary: '년도별 요약',
    approvalRequest: '결재 신청',
    concurrenceRequest: '합의 신청',
    delegateApproval: '대리 결재',
    delegateConcurrence: '대리 합의',
    approvalDetail: '결재 상세',
    delegateApprovalDetail: '대리 결재 상세',
    concurrenceDetail: '합의 상세',
    delegateConcurrenceDetail: '대리 합의 상세',
    history: '결재 내역',
    delegateHistory: '대리 내역',
    historyDetail: '내역 상세',
    delegateHistoryDetail: '대리 내역 상세',
    approvalSummary: '승인 현황',
    approvalSummaryDetail: '승인 상세',
    reportCeo: '제출용 요약',
    closure: '결제 마감',
    management: '내역 관리',
    metaManagement: '설정 관리',
  },

  // 사원관리 메뉴
  hrm: {
    userManagement: '사용자 관리',
    userModifyMobile: '사용자 수정(모바일)',
    userPermissions: '권한 부여',
    deptTeam: '부서 및 팀 관리',
    orgDirectory: '조직도',
    performanceHistory: '유저 실적 확인',
    performanceManagement: '실적 관리',
    tempUserApproval: '임시 가입자 승인',
    favoriteMenu: '즐겨찾기 관리',
    userInfo: '사용자 정보',
  },

  // 시스템 메뉴
  system: {
    activityLog: '이력 관리',
    statistics: '통계 관리',
    authorityManagement: '권한 관리',
    kafkaOutbox: 'Kafka Outbox',
    componentTest: '컴포넌트 테스트',
    personalHistory: '개인 이력',
  },

  // 공통 메뉴
  common: {
    main: '메인',
    guide: '가이드',
  },

  // 카테고리
  category: {
    scheduleManagement: '일정 관리',
    board: '게시판',
    guide: '가이드',
    registrar: '등록자',
    approver: '결재자',
    proxy: '대리결재자',
    inspector: '검수자',
    manager: '관리자',
    userAuth: '사용자·권한',
    organization: '조직',
    performance: '실적',
    approval: '가입 승인',
    personal: '개인정보',
    systemAdmin: '시스템 관리',
  },
}

