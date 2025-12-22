/**
 * 가이드 페이지 다국어 메시지 (한글)
 */
export default {
  page: {
    title: '가이드',
    subtitle: '가이드 페이지 입니다 (이미지 클릭 시 확대)',
    expandAll: '모두 펼치기',
    collapseAll: '모두 접기',
  },

  sections: {
    signup: '회원가입',
    password: '비밀번호 찾기',
    social: '소셜 로그인',
    main: '메인 페이지',
    notice: '공지사항',
    receipt: '영수증',
    favorite: '즐겨찾기',
    employee: '사원 관리',
    system: '시스템',
  },

  contact: {
    title: 'CONTACT',
    description: '문의사항이 있으시면 아래 연락처로 연락주세요',
    systemCategory: 'SYSTEM',
    managementCategory: 'MANAGEMENT',
    manager: 'Manager',
    email: 'Email',
    phone: 'Phone',
  },

  signup: {
    process: {
      summary: '회원가입 프로세스',
      title: '📝 회원가입 전체 진행 단계',
      steps: [
        '<strong>정보 입력</strong> → <strong>인증 메일 발송</strong> → <strong>메일 인증</strong> → <strong>임시 가입</strong> → <strong>관리자 승인</strong>',
        '필수 데이터: 이름, 연락처, 이메일, 비밀번호(확인), 인증코드, 필수 약관 동의',
        '관리자가 승인해야 정식 회원 자격과 권한이 부여됩니다.',
      ],
      flow: [
        '정보 입력',
        '인증 메일 발송',
        '메일 인증',
        '임시 가입 완료',
        '관리자 승인<br/>권한 부여',
      ],
      tableTitle: '1️⃣ 정보 입력 (테이블)',
      table: {
        head: ['구분', '필수 여부', '예시'],
        rows: [
          { label: '이름', required: '○', example: '홍길동' },
          { label: '연락처(휴대폰)', required: '○', example: '01012345678' },
          { label: '이메일', required: '○', example: 'user@example.com' },
          { label: '비밀번호 · 확인', required: '○', example: '••••••••' },
          { label: '약관(필수) 동의', required: '○', example: '체크' },
        ],
      },
    },
    checklist: [
      '신규 사용자는 <strong>임시 회원</strong> 상태로 등록<br/>└ 기본 정보(이메일·연락처·부서) 입력',
      '직속 상급자에게 승인 요청',
      '상급자 승인 페이지에서 신규 사용자 승인 후 권한·부서·팀 지정 → 정식 회원 전환',
      '첫 로그인 시 개인정보 확인 및 수정 진행',
    ],
    images: {
      desktopAlt: '회원가입 화면 (데스크탑)',
      mobileAlt: '회원가입 화면 (모바일)',
    },
  },

  password: {
    steps: [
      '이메일 입력 → 인증 코드(6자리) 발급',
      '코드 인증 성공 → 임시 비밀번호를 메일로 전송',
      '로그인 후 ‘내 정보’ → 비밀번호 변경 메뉴에서 재설정',
    ],
    images: {
      desktopAlts: ['비밀번호 찾기 화면 (데스크탑 1)', '비밀번호 찾기 화면 (데스크탑 2)'],
      mobileAlts: ['비밀번호 찾기 화면 (모바일 1)', '비밀번호 찾기 화면 (모바일 2)'],
    },
  },

  social: {
    steps: [
      '회사 이메일로 <strong>사전 회원가입</strong>을 먼저 완료',
      '로그인 화면에서 <strong>Google /Kakao</strong> 중 원하는 소셜 로그인을 선택',
      '표시되는 소셜 로그인 팝업에서 <strong>“확인”</strong> 버튼을 클릭',
      '팝업이 닫힌 뒤, 동일 페이지에서 로그인을 진행하고 <strong>내 정보</strong> 페이지에서 연동 상태 확인',
    ],
    images: {
      desktopAlt: '소셜 로그인 화면 (데스크탑)',
      mobileAlt: '소셜 로그인 화면 (모바일)',
    },
  },

  main: {
    permissionsTitle: '필요 권한',
    permissions: ['모든 로그인 사용자 접근 가능'],
    featuresTitle: '주요 기능',
    features: [
      '<strong>사이드바</strong>: 좌측 네비게이션에서 모든 메뉴 접근 가능',
      '<strong>테마 변경</strong>: 상단 프로필 메뉴에서 라이트/다크 테마 선택',
      '<strong>즐겨찾기</strong>: 자주 사용하는 메뉴를 빠르게 접근',
      '<strong>최신 공지사항</strong>: 최근 3건의 공지사항 미리보기',
      '<strong>영수증 신청</strong>: 최근 3건의 영수증 신청 현황 확인',
    ],
    images: {
      desktopAlt: '메인 페이지 화면 (데스크탑)',
      mobileAlt: '메인 페이지 화면 (모바일)',
    },
  },

  notice: {
    permissionsTitle: '필요 권한',
    permissions: [
      '<strong>일반 사원</strong>: 공지사항 열람 및 검색',
      '<strong>중간관리자</strong>: 공지사항 작성, 수정, 삭제',
      '<strong>관리자</strong>: 모든 기능 + 상단 고정, 열람 범위 설정',
    ],
    featuresTitle: '주요 기능',
    features: [
      '텍스트 서식: 제목(1~3), 볼드, 이탤릭, 글머리/번호 리스트, 코드 블록 지원',
      '멀티미디어: 이미지(Drag&Drop), PDF, 링크 삽입 가능',
      '공지 고정: 관리자 화면에서 “상단 고정” 스위치를 On 하면 메인/모바일 모두 최상단에 노출',
      '수정 이력: 게시물 수정 시 자동으로 버전이 생성되어 히스토리를 확인',
    ],
    images: {
      desktopAlts: ['공지사항 화면 (데스크탑 1)', '공지사항 화면 (데스크탑 2)'],
      mobileAlts: ['공지사항 화면 (모바일 1)', '공지사항 화면 (모바일 2)'],
    },
  },

  receipt: {
    process: {
      summary: '🧾 영수증 처리 프로세스 안내서',
      title: '전체 흐름도',
      flowSteps: ['영수증 등록', '신청·수정·삭제', '결재/합의', '마감'],
      exceptionStep: '예외 처리',
      rejectFlow: ['🔁 반려: 결재단계', '🔁 반려: 마감단계'],
      sections: [
        {
          title: '1️⃣ 영수증 등록',
          items: [
            '등록 시 영수증 상태는 <strong>‘대기’</strong>로 설정됩니다.',
            '<strong>기능 위치:</strong> `영수증 > 영수증 등록 > [영수증 등록] 버튼`',
          ],
          table: {
            title: '필수 입력 항목',
            head: ['항목', '설명'],
            rows: [
              { label: '영수증 사진', description: '실물 사진 첨부' },
              { label: '발행일', description: '영수증에 명시된 날짜' },
              { label: '카테고리', description: '야근 식비, 출장비 등 분류 선택' },
              { label: '결재선', description: '최대 3명까지 설정 가능' },
              { label: '합의자', description: '인원 제한 없음' },
              { label: '사용 사유', description: '텍스트 입력' },
            ],
          },
        },
        {
          title: '2️⃣ 신청·수정·삭제 가능 (대기 상태)',
          items: [
            '<strong>기능 위치:</strong> `영수증 > 영수증 등록`',
            '등록한 영수증은 신청 전까지 <strong>수정</strong>, <strong>삭제</strong>, 또는 <strong>신청</strong>할 수 있습니다.',
            '반려된 영수증도 이 단계로 되돌아와 <strong>재신청 가능</strong>합니다.',
          ],
        },
        {
          title: '3️⃣ 결재 (합의 포함)',
          items: [
            '<strong>기능 위치:</strong><br/>• `영수증 > 영수증 결재 신청 현황` → 테이블 클릭 → 상세 보기 → `[결재]`<br/>• `영수증 > 영수증 합의 신청 현황` → 테이블 클릭 → 상세 보기 → `[결재]`',
            '<strong>결재자에게 보이는 조건:</strong> 본인이 <strong>결재 차례</strong>인 영수증만 보임',
            '<strong>반려 시 주의사항:</strong> 반드시 <strong>반려 사유</strong>를 입력해야 하며, 영수증 히스토리에 반영됩니다.',
          ],
        },
        {
          title: '4️⃣ 마감',
          items: [
            '<strong>기능 위치:</strong> `영수증 > 결재 마감`',
            '결재가 완료된 영수증은 대표님의 컨펌 후 경영관리팀이 마감을 수행합니다.',
            '대표님이 반려한 건은 반려 처리되고, 승인한 건은 마감 완료 처리됩니다.',
          ],
        },
      ],
      exception: {
        title: '5️⃣ 예외 처리',
        cases: [
          {
            title: '❗ 결재 권한 상실 사용자 발생 시',
            items: [
              '결재 도중 결재선의 사용자가 권한 상실 또는 비활성화되면, 해당 영수증은 <strong>자동 반려</strong>되며, 반려 사유에 해당 사용자 정보가 기재됩니다.',
              '예: `결재자 홍길동(비활성화)로 인해 자동 반려 처리`',
            ],
          },
          {
            title: '❗ 관리자 권한으로 강제 상태 변경 가능',
            items: [
              '관리자 권한을 가진 사용자는 영수증 상태를 <strong>수동 변경</strong>할 수 있으며, 이때 반려된 경우 `반려 사유`에 강제 처리한 <strong>관리자 이름</strong>이 기록됩니다.',
            ],
          },
        ],
      },
      reference: {
        title: '참고 정보',
        boxTitle: '✅ 카테고리 관련 사항',
        items: [
          '카테고리는 관리자만 생성 가능합니다.',
          '카테고리별로 <strong>청구 상한 금액</strong>이 존재하며, 상한을 초과하면 등록이 불가합니다.',
        ],
      },
      summaryTable: {
        title: '요약',
        table: {
          head: ['단계', '설명'],
          rows: [
            { label: '1. 등록', description: '필수 정보 입력 후 `대기` 상태로 저장' },
            { label: '2. 신청/수정/삭제', description: '`대기` 상태에서 신청, 수정, 삭제 가능' },
            { label: '3. 결재/합의', description: '결재자·합의자 승인 또는 반려, 반려 사유 필수' },
            { label: '4. 마감', description: '승인된 건을 대표 컨펌 후 최종 마감' },
            { label: '5. 예외 처리', description: '사용자 비활성화 및 권한 상실 시 자동 반려, 관리자 수동 조치 가능' },
          ],
        },
      },
    },
    permissionsTitle: '필요 권한',
    permissions: [
      '<strong>등록자</strong>: 영수증 등록 및 신청',
      '<strong>결재자</strong>: 본인 차례의 영수증 승인/반려',
      '<strong>검수자</strong>: 승인 완료 건 검수',
      '<strong>관리자</strong>: 모든 영수증 접근 및 설정 관리',
    ],
    hierarchyTitle: '권한 계층',
    hierarchySubtitle: '[ 등록자 ＜ 결재자 ＜ 검수자 ＜ 확정자 ＜ 관리자 ]',
    hierarchy: [
      '<strong>등록자</strong>:<br/>&nbsp;• 영수증 이미지 업로드, 합의자(결재자) 지정<br/>&nbsp;• 신청 후 \'내 영수증\' 테이블에서 상태 확인·삭제·수정<br/>&nbsp;• 반려 시 원본 수정하여 재상신',
      '<strong>결재자</strong>:<br/>&nbsp;• 결재선에서 <strong>자신의 차례</strong>인 영수증만 결재 가능<br/>&nbsp;• 자신의 결재 목록 확인 후 "승인/반려" 선택<br/>&nbsp;• 일괄 승인 기능 제공(확정자 포함)',
      '<strong>검수자</strong>:<br/>&nbsp;• 승인 완료 건을 최종 검수, 오류 시 반려 가능<br/>&nbsp;• 월 단위 엑셀 출력 → 상급자 컨펌 후 \'마감\' 처리',
      '<strong>관리자</strong>:<br/>&nbsp;• 모든 영수증 접근·상태 강제 변경<br/>&nbsp;• 기본 결재선, 카테고리, 금액 상한선 설정',
    ],
    processListTitle: '프로세스',
    processList: [
      '등록자: 영수증 업로드 & 결재선 지정',
      '결재자: 건별 또는 일괄 승인 완료',
      '검수자: 승인 건 검수 & 통계 산입',
      '마감: 검수 완료 후 월별 마감 처리 → 회계 시스템 연동',
    ],
    images: {
      desktopAlts: [
        '영수증 등록 화면 (데스크탑)',
        '영수증 등록 모달 (데스크탑)',
        '영수증 수정 화면 (데스크탑)',
      ],
      mobileAlts: [
        '영수증 등록 화면 (모바일)',
        '영수증 등록 모달 (모바일)',
        '영수증 수정 화면 (모바일)',
      ],
    },
  },

  favorite: {
    permissionsTitle: '필요 권한',
    permissions: ['모든 로그인 사용자 접근 가능'],
    featuresTitle: '주요 기능',
    features: [
      '<strong>즐겨찾기 등록</strong>: 자주 사용하는 메뉴를 즐겨찾기에 추가',
      '<strong>카드 디자인</strong>: 모바일에서 5가지 디자인 중 선택 가능',
      '<strong>순서 변경</strong>: 드래그 앤 드롭으로 메뉴 순서 변경',
      '<strong>즐겨찾기 해제</strong>: 더 이상 필요하지 않은 메뉴는 삭제 가능',
    ],
    images: {
      desktopAlts: ['즐겨찾기 화면 (데스크탑 1)', '즐겨찾기 카드 디자인 선택 (데스크탑)'],
      mobileAlts: ['즐겨찾기 화면 (모바일 1)', '즐겨찾기 카드 디자인 선택 (모바일)'],
    },
  },

  employee: {
    permissionsTitle: '필요 권한',
    permissions: [
      '<strong>중간관리자</strong>: 임시 회원 승인, 권한 부여, 부서/팀 관리',
      '<strong>관리자</strong>: 사원 정보 등록·수정·조회·삭제, 권한 설정, 조직도 관리',
    ],
    featuresTitle: '주요 기능',
    features: [
      '임시 회원 승인: 신규 가입 사용자의 승인 및 권한 부여',
      '부서/팀 관리: 조직 구조 생성 및 관리',
      '권한 관리: 사용자별 권한 설정 및 변경',
      '조직도: 부서/팀 구조를 드래그 앤 드롭으로 재배치',
    ],
    images: {
      desktopAlts: ['권한 부여 화면 (데스크탑)', '부서(팀) 관리 화면 (데스크탑)'],
      mobileAlts: ['권한 부여 화면 (모바일)', '부서(팀) 관리 화면 (모바일)'],
    },
  },

  system: {
    permissionsTitle: '필요 권한',
    permissions: ['<strong>시스템 관리자</strong>만 접근 가능'],
    featuresTitle: '주요 기능',
    features: [
      '<strong>시스템 설정</strong>: 모든 모듈 설정 및 모니터링',
      '<strong>감사 로그</strong>: 사용자, API 호출, 데이터 변경 이력 열람',
      '<strong>대시보드</strong>: 인원, 영수증, 부서 지출 차트, 기간·부서 필터 제공',
    ],
    images: {
      desktopAlts: ['대시보드 화면 (데스크탑)'],
      mobileAlts: ['대시보드 화면 (모바일)'],
    },
  },
}

