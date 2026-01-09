/**
 * 에러 메시지 (한글)
 */
export default {
  // HTTP 상태 코드별 에러
  http: {
    400: '잘못된 요청입니다',
    401: '인증이 필요합니다',
    403: '접근 권한이 없습니다',
    404: '요청하신 페이지를 찾을 수 없습니다',
    405: '허용되지 않은 메서드입니다',
    408: '요청 시간이 초과되었습니다',
    409: '요청이 충돌했습니다',
    500: '서버 내부 오류가 발생했습니다',
    502: '게이트웨이 오류가 발생했습니다',
    503: '서비스를 사용할 수 없습니다',
    504: '게이트웨이 시간 초과',
  },

  // 서비스별 에러 (백엔드 연결 실패 등)
  service: {
    auth: {
      connection: '[A] 인증 서비스와의 연결에 실패했습니다',
      unknown: '[A] 알 수 없는 오류가 발생했습니다',
      unauthorized: '로그아웃 되었습니다\n다시 로그인해주세요',
      roleChanged: '권한이 변경되었습니다\n다시 로그인해주세요',
      forbidden: '[A] 접근 권한이 없습니다',
      unavailable: '[A] 서비스를 사용할 수 없습니다',
    },
    hrm: {
      connection: '[H] 인사 서비스와의 연결에 실패했습니다',
      unknown: '[H] 알 수 없는 오류가 발생했습니다',
      forbidden: '[H] 접근 권한이 없습니다',
      unavailable: '[H] 서비스를 사용할 수 없습니다',
    },
    receipt: {
      connection: '[R] 접수 서비스와의 연결에 실패했습니다',
      unknown: '[R] 알 수 없는 오류가 발생했습니다',
      forbidden: '[R] 접근 권한이 없습니다',
      unavailable: '[R] 서비스를 사용할 수 없습니다',
    },
    info: {
      connection: '[I] 정보 서비스와의 연결에 실패했습니다',
      unknown: '[I] 알 수 없는 오류가 발생했습니다',
      forbidden: '[I] 접근 권한이 없습니다',
      unavailable: '[I] 서비스를 사용할 수 없습니다',
    },
    system: {
      connection: '[S] 시스템 서비스와의 연결에 실패했습니다',
      unknown: '[S] 알 수 없는 오류가 발생했습니다',
      forbidden: '[S] 접근 권한이 없습니다',
      unavailable: '[S] 서비스를 사용할 수 없습니다',
    },
  },

  // 네트워크 에러
  network: {
    offline: '인터넷 연결이 끊어졌습니다',
    timeout: '요청 시간이 초과되었습니다',
    abort: '요청이 취소되었습니다',
  },

  // 입력 검증 에러
  validation: {
    required: '필수 입력 항목입니다',
    email: '올바른 이메일 형식이 아닙니다',
    phone: '올바른 전화번호 형식이 아닙니다',
    password: '비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다',
    passwordMismatch: '비밀번호가 일치하지 않습니다',
    minLength: '최소 {min}자 이상 입력해주세요',
    maxLength: '최대 {max}자까지 입력 가능합니다',
    fileSize: '파일 크기는 {size}MB 이하여야 합니다',
    fileType: '지원하지 않는 파일 형식입니다',
  },

  // 파일 관련 에러
  file: {
    uploadFailed: '파일 업로드에 실패했습니다',
    downloadFailed: '파일 다운로드에 실패했습니다',
    sizeExceeded: '파일 크기가 너무 큽니다',
    typeNotSupported: '지원하지 않는 파일 형식입니다',
    tooManyFiles: '파일 개수를 초과했습니다',
  },
}

