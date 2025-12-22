/**
 * 정보(Info) 서비스 다국어 메시지 (한글)
 * 네이밍: info.페이지.액션/상태
 */
export default {
  // 공지사항
  notice: {
    title: '공지사항',
    list: '공지사항 목록',
    detail: '공지사항 상세',
    create: '공지사항 작성',
    edit: '공지사항 수정',
    delete: '공지사항 삭제',
    subject: '제목',
    content: '내용',
    author: '작성자',
    createdAt: '작성일',
    views: '조회수',
    attachments: '첨부파일',
    important: '중요',
    pinned: '상단고정',
    saveSuccess: '저장되었습니다',
    deleteSuccess: '삭제되었습니다',
    deleteConfirm: '정말 삭제하시겠습니까?',
    noNotices: '등록된 공지사항이 없습니다',
  },

  // 커뮤니티
  community: {
    title: '커뮤니티',
    list: '게시글 목록',
    detail: '게시글 상세',
    create: '게시글 작성',
    edit: '게시글 수정',
    delete: '게시글 삭제',
    subject: '제목',
    content: '내용',
    author: '작성자',
    createdAt: '작성일',
    views: '조회수',
    likes: '좋아요',
    comments: '댓글',
    attachments: '첨부파일',
    category: '카테고리',
    saveSuccess: '저장되었습니다',
    deleteSuccess: '삭제되었습니다',
    deleteConfirm: '정말 삭제하시겠습니까?',
    noPosts: '등록된 게시글이 없습니다',
    like: '좋아요',
    unlike: '좋아요 취소',
  },

  // 자료실
  resource: {
    title: '자료실',
    list: '자료 목록',
    detail: '자료 상세',
    create: '자료 등록',
    edit: '자료 수정',
    delete: '자료 삭제',
    subject: '제목',
    content: '내용',
    author: '작성자',
    createdAt: '등록일',
    views: '조회수',
    downloads: '다운로드',
    attachments: '첨부파일',
    category: '카테고리',
    fileSize: '파일 크기',
    download: '다운로드',
    saveSuccess: '저장되었습니다',
    deleteSuccess: '삭제되었습니다',
    deleteConfirm: '정말 삭제하시겠습니까?',
    noResources: '등록된 자료가 없습니다',
    downloadSuccess: '다운로드되었습니다',
    downloadFailed: '다운로드에 실패했습니다',
  },

  // 댓글
  comment: {
    add: '댓글 작성',
    edit: '댓글 수정',
    delete: '댓글 삭제',
    reply: '답글',
    content: '댓글 내용',
    placeholder: '댓글을 입력하세요',
    saveSuccess: '댓글이 등록되었습니다',
    deleteSuccess: '댓글이 삭제되었습니다',
    deleteConfirm: '정말 삭제하시겠습니까?',
    noComments: '댓글이 없습니다',
  },

  // 첨부파일
  attachment: {
    add: '파일 첨부',
    delete: '파일 삭제',
    download: '다운로드',
    noFiles: '첨부파일이 없습니다',
    maxSize: '최대 파일 크기',
    allowedTypes: '허용된 파일 형식',
    uploadSuccess: '파일이 업로드되었습니다',
    uploadFailed: '파일 업로드에 실패했습니다',
    deleteSuccess: '파일이 삭제되었습니다',
  },

  // 공통 (게시판)
  board: {
    title: {
      notice: '공지사항',
      community: '자유게시판',
      resource: '자료실',
    },
    createPost: '게시글 등록',
    editPost: '게시글 수정',
    noPosts: '게시글이 없습니다',
    unknown: '미확인',
    columns: {
      id: '번호',
      title: '제목',
      content: '내용',
      author: '작성자',
      editor: '수정자',
      date: '작성일',
    },
    mobile: {
      postNumber: '글번호',
    },
  },

  // 모바일 게시글 작성/수정
  postMobile: {
    title: {
      notice: '공지사항 등록',
      resource: '자료실 등록',
      community: '자유게시판 등록',
      default: '게시글 등록',
    },
    editTitle: {
      notice: '공지사항 수정',
      resource: '자료실 수정',
      community: '자유게시판 수정',
      default: '게시글 수정',
    },
    labels: {
      title: '제목',
      visibilityScope: '열람 대상',
      mailSend: '메일 발송',
      content: '내용',
      attachment: '첨부파일',
    },
    placeholders: {
      title: '제목을 입력하세요',
    },
    mail: {
      all: '전체',
      department: '부서',
      team: '팀',
      guest: '게스트',
      user: '일반',
      manager: '운영자',
      admin: '관리자',
      selectDepartment: '부서 선택',
      selectTeam: '팀 선택',
      multipleSelect: '여러 개를 선택할 수 있습니다.',
    },
    file: {
      select: '파일 선택하기',
      multipleSelect: '여러 개 선택 가능',
      new: '신규',
      existing: '기존',
    },
    buttons: {
      goBack: '돌아가기',
      create: '등록',
      edit: '수정',
    },
    validation: {
      titleRequired: '제목을 입력하세요',
      contentRequired: '내용을 입력하세요',
    },
    success: {
      create: '게시글 등록 완료',
      edit: '게시글 수정 완료',
    },
    visibility: {
      all: '전체 (GUEST 포함)',
      userAbove: 'USER 이상',
      managerAbove: 'MANAGER 이상',
    },
  },

  // 게시글 작성/수정 모달
  postModal: {
    title: {
      notice: '공지사항',
      resource: '자료실',
      community: '자유게시판',
      default: '게시글',
    },
    labels: {
      title: '제목',
      visibilityScope: '열람 대상',
      mailSend: '메일 발송',
      author: '작성자',
      date: '작성일',
      content: '내용',
      existingAttachments: '추가된 첨부 파일',
      newFile: '새 파일 추가',
      attachment: '첨부파일',
      noAttachment: '첨부파일 없음',
    },
    placeholders: {
      title: '제목을 입력하세요',
      author: '자동 입력',
    },
    buttons: {
      collapse: '접기',
      expand: '펼치기',
      close: '닫기',
      create: '등록',
      edit: '수정',
    },
    validation: {
      titleRequired: '제목을 입력해주세요',
      contentRequired: '내용을 입력해주세요',
      contentSizeExceeded: '본문은 5 MB를 초과할 수 없습니다.',
    },
    success: {
      create: '게시글 등록 완료',
      edit: '게시글 수정 완료',
    },
    confirm: {
      closeTitle: '확인',
      closeMessage: '작성 중인 게시글을 닫으시겠습니까?',
    },
  },

  // 게시글 상세
  postDetail: {
    loading: '게시글을 불러오는 중...',
    labels: {
      content: '본문',
      attachment: '첨부 파일',
      download: '[다운로드]',
    },
    buttons: {
      unpin: '고정 해제',
      pin: '게시글 고정',
      edit: '수정',
      delete: '삭제',
    },
    comments: {
      title: '댓글',
      placeholder: '댓글을 입력하세요',
      replyPlaceholder: '답글을 입력하세요',
      reply: '답글',
      edit: '수정',
      delete: '삭제',
      save: '저장',
      cancel: '취소',
      register: '등록',
      count: '댓글 {count}개',
    },
    confirm: {
      deleteTitle: '삭제 확인',
      deleteMessage: '삭제하시겠습니까?',
    },
    success: {
      delete: '게시글 삭제 완료',
      pin: '게시글이 고정되었습니다.',
      unpin: '게시글이 고정 해제되었습니다.',
    },
    error: {
      loadFailed: '게시글 정보를 불러올 수 없습니다.',
    },
  },
}

