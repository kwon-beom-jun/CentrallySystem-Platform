/**
 * 인증(Auth) 서비스 다국어 메시지 (한글)
 * 네이밍: auth.페이지.액션/상태
 */
export default {
  // 로그인 페이지
  login: {
    title: 'LOGIN',
    systemTitle: 'Centrally System',
    systemSubtitle: 'CENTRALLY SYSTEM은 회사 관련 시스템입니다',
    accountLabel: 'Account',
    passwordLabel: 'Password',
    email: '이메일',
    password: '비밀번호',
    emailPlaceholder: '이메일 입력',
    passwordPlaceholder: '패스워드 입력',
    rememberMe: '아이디 저장',
    forgotPassword: 'Forgot login password?',
    noAccount: '계정이 없으신가요?',
    signUp: '회원가입',
    loginButton: '로그인',
    loginSuccess: '로그아웃 성공',
    loginFailed: '로그인에 실패했습니다',
    emailRequired: '아이디와 비밀번호를 입력해주세요',
    passwordRequired: '비밀번호를 입력해주세요',
    invalidCredentials: '이메일 또는 비밀번호가 올바르지 않습니다',
    accountLocked: '계정이 잠겼습니다. 관리자에게 문의하세요',
    socialLogin: '소셜 로그인',
    socialUnknown: '알 수 없는 소셜 로그인 방식입니다.',
    ipWarning: '해당 서비스는 로그인부터 IP 정보가 기록되니 원치 않으신 분들은 이용하지 말아 주시길 바랍니다',
    socialModalTitle: '소셜 로그인',
    socialModalMessage: '소셜 인증이 완료되었습니다.\n현재 소셜 계정과 연동된 계정이 없습니다.\n소셜과 연동을 완료하기 위해 로그인을 부탁드립니다\n(만료 시간 5분)',
    socialModalFooter: '연동할 계정이 없거나,\n상관 없다면 아래 "취소" 버튼을 눌러주세요',
  },

  // 회원가입 페이지
  join: {
    title: 'JOIN',
    systemTitle: 'Centrally System',
    systemSubtitle: 'CENTRALLY SYSTEM은 회사 관련 시스템입니다',
    name: '이름',
    phone: '연락처',
    phonePlaceholder: "'-' 없이 입력하세요",
    email: '이메일',
    password: '비밀번호',
    passwordConfirm: '비밀번호 확인',
    verificationCode: '인증 코드',
    verificationCodePlaceholder: '인증코드',
    sendVerificationEmail: '인증 메일',
    verifyButton: '확인',
    agreeTerms: '이용약관에 동의합니다',
    agreePrivacy: '개인정보 처리방침에 동의합니다',
    signUpButton: '가입하기',
    backButton: '돌아가기',
    signUpSuccess: '회원가입이 완료되었습니다',
    signUpFailed: '회원가입에 실패했습니다',
    emailRequired: '이메일을 입력하세요',
    emailDuplicate: '이미 사용 중인 이메일입니다',
    passwordMismatch: '비밀번호가 일치하지 않습니다',
    verificationCodeRequired: '인증코드를 입력하세요',
    verificationRequired: '이메일 인증을 완료하세요',
    termsRequired: '필수 약관에 동의해 주세요',
    privacyRequired: '개인정보 처리방침에 동의해주세요',
    hasAccount: '이미 계정이 있으신가요?',
    goToLogin: '로그인',
    agreementModalTitle: '약관 동의',
    agreementRequired: '필수',
    agreementOptional: '선택',
    agreementAgreeAll: '모두 동의합니다',
    agreementCompleted: '필수 약관 동의 완료',
    agreementRequired2: '필수 약관 동의 필요',
  },

  // OAuth 콜백
  oauth: {
    processing: 'OAuth 인증 처리 중...',
    success: 'OAuth 인증이 완료되었습니다',
    failed: '소셜 로그인 실패',
    cancelled: 'OAuth 인증이 취소되었습니다',
    duplicateLogin: '중복 로그인이 되어서 문제가 발생하였습니다.\n다시 재로그인 부탁드립니다',
  },

  // 임시 사용자 승인
  tempApprovals: {
    title: '임시 사용자 관리',
    subtitle: '승인 대기 중인 임시 가입자를 관리합니다.',
    list: '승인 대기 목록',
    nameLabel: '이름 :',
    searchPlaceholder: '이름 또는 이메일 검색',
    approve: '승인',
    reject: '반려',
    approveSuccess: '승인되었습니다',
    rejectSuccess: '반려되었습니다',
    approveSuccessMessage: '{name} 님 승인 완료',
    rejectSuccessMessage: '{name} 님 삭제 완료',
    approveConfirm: '승인하시겠습니까?',
    rejectConfirm: '반려하시겠습니까?',
    deleteConfirmTitle: '삭제 확인',
    deleteConfirmMessage: '정말로 {name} 님을 삭제하시겠습니까?',
    noRequests: '승인 대기 중인 요청이 없습니다',
  },

  // 로그아웃
  logout: {
    confirm: '로그아웃 하시겠습니까?',
    success: '로그아웃되었습니다',
    failed: '로그아웃에 실패했습니다',
  },

  // 세션 관리
  session: {
    expired: '세션이 만료되었습니다. 다시 로그인해주세요',
    invalid: '유효하지 않은 세션입니다',
    extended: '세션이 연장되었습니다',
  },

  // 권한 부여 모달
  permission: {
    title: '권한 부여',
    userSearch: '사용자 검색',
    selectedUser: '선택된 사용자',
    service: '서비스',
    role: '권한',
    allServiceGranted: '모든 서비스 권한이 추가되어있습니다',
    serviceSelect: '서비스 선택',
    roleSelect: '권한 선택',
    selectAllItems: '모든 항목을 선택하세요.',
    addSuccess: '권한이 추가되었습니다.',
  },

  // 비밀번호 초기화 모달
  forgotPassword: {
    title: '비밀번호 초기화',
    subtitle: '본인 확인을 위해 이메일 인증 후 임시 비밀번호가 발송됩니다.',
    emailPlaceholder: '가입 이메일',
    codePlaceholder: '인증번호 6자리',
    guideText: '인증메일 발송 후 5분 내에 코드를 입력해 주세요.',
    sendCode: '인증메일 발송',
    verifyCode: '인증 확인',
    emailRequired: '이메일을 입력하세요',
    emailFormat: '이메일 형식을 입력해 주세요',
    emailAndCodeRequired: '이메일과 인증번호를 모두 입력하세요',
    verifySuccess: '[ 인증 확인 완료 ]\n임시 비밀번호가 이메일로 발송되었습니다.',
  },

  // 비밀번호 확인 모달
  passwordVerification: {
    title: '비밀번호 확인',
    message1: '개인정보 보호를 위해',
    message2: '비밀번호를 입력해 주세요',
    currentPassword: '현재 비밀번호',
    currentPasswordPlaceholder: '현재 비밀번호를 입력하세요',
    passwordRequired: '비밀번호를 입력해주세요.',
    verifySuccess: '비밀번호가 확인되었습니다.',
    passwordMismatch: '비밀번호가 일치하지 않습니다.',
  },

  // 비밀번호 변경 모달
  passwordModify: {
    title: '비밀번호 변경',
    currentPassword: '현재 비밀번호',
    newPassword: '새 비밀번호',
    confirmPassword: '비밀번호 확인',
    currentPasswordPlaceholder: '현재 비밀번호를 입력하세요',
    newPasswordPlaceholder: '새 비밀번호를 입력하세요',
    confirmPasswordPlaceholder: '새 비밀번호를 다시 입력하세요',
    passwordMismatch: '새 비밀번호가 일치하지 않습니다.',
    currentPasswordRequired: '현재 비밀번호를 입력해주세요.',
    newPasswordRequired: '새 비밀번호와 비밀번호 확인을 모두 입력해주세요.',
    changeSuccess: '비밀번호가 성공적으로 변경되었습니다.',
  },

  // 임시 사용자 상세 모달
  tempUser: {
    title: '임시 사용자 상세',
    name: '이름',
    email: '이메일',
    phone: '휴대폰',
    approve: '승인',
    delete: '삭제',
  },

  // 사용자 검색 드롭다운
  userSearch: {
    placeholder: '이름(이메일)을 입력하여 검색',
    excludeSelf: '본인 제외',
    noResults: '검색 결과가 없습니다',
  },
}

