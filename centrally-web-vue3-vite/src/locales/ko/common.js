/**
 * 공통 다국어 메시지 (한글)
 */
export default {
  // 공통 버튼 및 액션
  button: {
    confirm: '확인',
    cancel: '취소',
    save: '저장',
    delete: '삭제',
    edit: '수정',
    add: '추가',
    search: '검색',
    reset: '초기화',
    close: '닫기',
    submit: '제출',
    approve: '승인',
    reject: '반려',
    register: '등록',
    modify: '변경',
    download: '다운로드',
    upload: '업로드',
    detail: '상세',
    list: '목록',
    create: '생성',
    update: '업데이트',
    refresh: '새로고침',
    back: '뒷면',
    next: '다음',
    prev: '이전',
    apply: '적용',
    select: '선택',
    selectAll: '전체선택',
    deselectAll: '선택해제',
    expand: '펼치기',
    collapse: '접기',
  },

  // 공통 레이블
  label: {
    name: '이름',
    email: '이메일',
    phone: '연락처',
    department: '부서',
    team: '팀',
    position: '직책',
    rank: '직급',
    status: '상태',
    date: '날짜',
    startDate: '시작일',
    endDate: '종료일',
    createdAt: '생성일',
    updatedAt: '수정일',
    author: '작성자',
    title: '제목',
    content: '내용',
    description: '설명',
    file: '파일',
    image: '이미지',
    category: '카테고리',
    type: '유형',
    count: '개수',
    total: '총',
    all: '전체',
    none: '없음',
    unknown: '알수없음',
    unspecified: '미지정',
    search: '검색',
    filter: '필터',
    sort: '정렬',
    order: '순서',
    page: '페이지',
    default: '기본',
    style1: '스타일 1',
    style2: '스타일 2',
    style3: '스타일 3',
    style4: '스타일 4',
    select: '선택',
  },

  // 공통 상태
  status: {
    active: '활성',
    inactive: '비활성',
    pending: '대기중',
    approved: '승인됨',
    rejected: '반려됨',
    completed: '완료',
    inProgress: '진행중',
    draft: '임시저장',
    published: '게시됨',
    deleted: '삭제됨',
    enabled: '사용',
    disabled: '미사용',
  },

  // 공통 메시지
  message: {
    loading: '로딩 중...',
    noData: '데이터가 없습니다',
    saved: '저장되었습니다',
    deleted: '삭제되었습니다',
    updated: '수정되었습니다',
    created: '생성되었습니다',
    submitted: '제출되었습니다',
    approved: '승인되었습니다',
    rejected: '반려되었습니다',
    copied: '복사되었습니다',
    downloadSuccess: '다운로드되었습니다',
    uploadSuccess: '업로드되었습니다',
    required: '필수 입력 항목입니다',
    invalidFormat: '형식이 올바르지 않습니다',
    confirmDelete: '정말 삭제하시겠습니까?',
    confirmSave: '저장하시겠습니까?',
    confirmCancel: '취소하시겠습니까? 작성 중인 내용이 사라집니다',
    loginRequired: '로그인이 필요합니다',
    permissionDenied: '권한이 없습니다',
    sessionExpired: '세션이 만료되었습니다',
    networkError: '네트워크 연결에 실패했습니다',
    serverError: '서버 오류가 발생했습니다',
    unknownError: '알 수 없는 오류가 발생했습니다',
  },

  // 공통 플레이스홀더
  placeholder: {
    search: '검색어를 입력하세요',
    select: '선택하세요',
    input: '입력하세요',
    email: '이메일을 입력하세요',
    phone: '연락처를 입력하세요',
    name: '이름을 입력하세요',
    title: '제목을 입력하세요',
    content: '내용을 입력하세요',
    date: '날짜를 선택하세요',
  },

  // 공통 검증 메시지
  validation: {
    required: '{field}은(는) 필수 입력 항목입니다',
    email: '올바른 이메일 형식이 아닙니다',
    phone: '올바른 전화번호 형식이 아닙니다',
    minLength: '{field}은(는) 최소 {min}자 이상이어야 합니다',
    maxLength: '{field}은(는) 최대 {max}자 이하여야 합니다',
    number: '숫자만 입력 가능합니다',
    onlyKorean: '한글만 입력 가능합니다',
    onlyEnglish: '영문만 입력 가능합니다',
    password: '비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다',
    passwordMismatch: '비밀번호가 일치하지 않습니다',
  },

  // 회사 정보 모달
  companyInfo: {
    logoAlt: '회사 로고',
    companyName: '회사명',
    address: '주소',
    phone: '대표번호',
    fax: '팩스',
    email: '이메일',
    homepage: '홈페이지',
    ceo: '대표이사',
    businessAreas: '사업분야',
  },

  // 요일
  weekday: {
    sun: '일', mon: '월', tue: '화', wed: '수', thu: '목', fri: '금', sat: '토',
  },
  
  // 사이드바 전용
  sidebar: {
    workspace: {
      home: '홈', info: '정보', receipt: '영수증', management: '사원관리', user: '개인정보', system: '시스템', favorites: '즐겨찾기'
    },
    tooltip: {
      toDark: '다크 모드로 변경',
      toLight: '라이트 모드로 변경',
      companyInfo: '회사 정보',
    },
    message: {
      themeChanged: '테마가 변경되었습니다.',
    },
    controls: {
      collapseAll: '전체 닫기',
      expandAll: '전체 열기',
      collapseSidebar: '사이드바 접기',
      expandSidebar: '사이드바 펴기',
    },
    category: {
      board: '게시판',
      guide: '가이드',
    },
    receipt: {
      registrar: '등록자',
      approver: '결재자',
      proxy: '대리 결재자',
      inspector: '검수자',
      manager: '관리자',
    },
    hrm: {
      userAuth: '사용자·권한',
      organization: '조직',
      performance: '실적',
      approval: '가입 승인',
    },
    user: {
      personal: '개인정보',
      myInfo: '내 정보',
      profileImageAlt: '프로필 사진',
      defaultName: '사용자',
    },
    system: {
      admin: '시스템 관리',
    },
    search: {
      title: '사원 검색',
      placeholder: '이름 또는 이메일로 검색',
      loading: '검색 중...',
      noResults: '검색 결과가 없습니다',
      clearSelection: '선택 취소',
    },
    favorites: {
      emptyTitle: '즐겨찾기한 메뉴가<br>없습니다',
      emptyDesc: '자주 사용하는 메뉴를<br>즐겨찾기에 추가하세요',
      manage: '즐겨찾기 관리',
      infoLine1: '즐겨찾기는 서버에 저장되어',
      infoLine2: '모든 기기에서 동기화됩니다',
    },
    favorite: {
      add: '즐겨찾기',
      removeTitle: '즐겨찾기 제거',
    },
    time: {
      dateFormat: '{year}년 {month}월 {date}일 {day}요일',
    },
    activity: {
      recentHistory: '최근 활동 이력',
      noHistory: '활동 이력이 없습니다',
      unknownAction: '알 수 없는 작업',
      unclassified: '미분류',
      timeAgo: {
        justNow: '방금 전',
        minutesAgo: '{minutes}분 전',
        hoursAgo: '{hours}시간 전',
        daysAgo: '{days}일 전',
      },
      service: {
        auth: '인증',
        hrm: '인사관리',
        info: '정보',
        receipt: '영수증',
        system: '시스템',
        common: '공통',
        default: '기타',
      },
    },
  },

  // 푸터
  footer: {
    copyright: 'CENTRALLY SYSTEM. Copyright 2025 KBJ. All rights reserved.',
  },

  // 모바일 드로어 메뉴
  mobileDrawer: {
    nameUnspecified: '이름 미지정',
    emailUnspecified: '이메일 미지정',
    positionUnspecified: '직책 미지정',
    profileAlt: '프로필',
    noMenu: '메뉴 없음',
    theme: {
      switchToDark: '다크 모드로 변경',
      switchToLight: '라이트 모드로 변경',
      changed: '테마가 변경되었습니다.',
      changeFailed: '테마 변경에 실패했습니다.',
    },
  },

  // 네비게이션 레이아웃
  navigation: {
    notice: '공지',
    receiptRegister: '영수증 등록',
    receiptHistory: '영수증 내역',
    more: '더보기',
    write: '글쓰기',
    receiptApproval: '영수증 결재',
    quickMenu: {
      empty: '사용 가능한 퀵메뉴가 없습니다.',
    },
    receipt: {
      noPermission: '영수증 등록 권한이 없습니다',
      movedToPage: '영수증 등록 페이지로\n이동되었습니다.',
    },
  },

  // 알림
  notification: {
    title: '알림',
    approval: '결재',
    concurrence: '합의',
    approvalWaiting: '결재 대기 {count}건',
    concurrenceRequest: '합의 요청 {count}건',
    summary: '[알림] 결재 {approvals}건 · 합의 {concurrences}건',
  },

  // 카드
  card: {
    participants: '참여',
    typeReason: '구분/사유',
    amount: '금액',
    amountPerPerson: '금액/인원수',
    receiptPhoto: '영수증 사진',
    peopleList: '명단',
    people: '인원',
    peopleCount: '인원수',
  },

  // 모바일 사용자 카드
  mobileUserCard: {
    favorites: '즐겨찾기',
    manage: '관리',
    user: '사용자',
    departmentUnspecified: '부서 미지정',
    positionUnspecified: '직책 미지정',
    greeting: '안녕하세요 👋',
    greetingSimple: '안녕하세요!',
    noFavorites: '등록된 즐겨찾기가 없습니다',
    addFavoritesHint: '설정 버튼을 눌러 추가하세요',
    registerFavorites: '자주 사용하는 메뉴를 등록하세요',
    addFavorites: '자주 사용하는 메뉴를 추가하세요',
    quickMenu: 'Quick Menu',
    addFavoritesGuide: '즐겨찾기를 추가해보세요',
    addFavoritesGuideHint: '우측 상단 ⚙️ 버튼을 눌러 등록하세요',
    settings: '설정',
    profileAlt: '프로필',
  },

  // 주소 검색
  address: {
    search: '주소 검색',
  },

  // 페이지네이션
  pagination: {
    prev: '이전',
    next: '다음',
  },

  // 가이드 헬퍼
  guide: {
    title: '가이드',
    clickHint: '궁금한 점이 있으면<br>저를 클릭하세요',
  },
}
