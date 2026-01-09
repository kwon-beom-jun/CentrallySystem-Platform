# 다국어(i18n) 사용 가이드

## 📁 디렉토리 구조

```
src/locales/
├── index.js           # i18n 설정 및 초기화
├── ko/               # 한글
│   ├── index.js      # 한글 메시지 통합
│   ├── common.js     # 공통 메시지
│   ├── error.js      # 에러 메시지
│   ├── auth.js       # 인증 서비스
│   ├── hrm.js        # 인사관리 서비스
│   ├── info.js       # 정보 서비스
│   ├── receipt.js    # 접수 서비스
│   └── system.js     # 시스템 서비스
└── en/               # 영문
    ├── index.js      # 영문 메시지 통합
    ├── common.js
    ├── error.js
    ├── auth.js
    ├── hrm.js
    ├── info.js
    ├── receipt.js
    └── system.js
```

## 🎯 키 네이밍 컨벤션

**형식**: `서비스.페이지.액션` 또는 `서비스.페이지.상태`

### 예시
```javascript
// 공통
common.button.confirm
common.message.saved
common.status.active

// 에러
error.http.404
error.service.auth.unauthorized

// 인증
auth.login.title
auth.login.loginSuccess

// 인사관리
hrm.userManagement.title
hrm.userInfo.updateSuccess

// 정보
info.notice.title
info.community.saveSuccess

// 접수
receipt.submission.submitSuccess
receipt.approvalRequest.approveConfirm

// 시스템
system.roleManagement.title
system.activityLog.noLogs
```

## 💻 사용 방법

### 1. Vue 컴포넌트에서 사용

#### 템플릿에서 사용
```vue
<template>
  <div>
    <!-- 기본 사용 -->
    <h1>{{ $t('auth.login.title') }}</h1>
    <button>{{ $t('common.button.confirm') }}</button>
    
    <!-- 파라미터 전달 -->
    <p>{{ $t('common.validation.minLength', { min: 8 }) }}</p>
  </div>
</template>
```

#### 스크립트에서 사용
```vue
<script setup>
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

// 메시지 사용
const title = t('auth.login.title')
const confirmMsg = t('common.button.confirm')

// 파라미터 전달
const errorMsg = t('common.validation.required', { field: '이메일' })

// 현재 언어 확인
console.log(locale.value) // 'ko' 또는 'en'
</script>
```

### 2. JavaScript 파일에서 사용

```javascript
import i18n from '@/locales'

const { t } = i18n.global

// 메시지 사용
const message = t('common.message.saved')
console.log(message)

// 파라미터 전달
const error = t('error.validation.minLength', { min: 5 })
```

### 3. 언어 변경

```vue
<script setup>
import { changeLocale } from '@/locales'

// 언어 변경
const switchToEnglish = () => {
  changeLocale('en')
}

const switchToKorean = () => {
  changeLocale('ko')
}
</script>

<template>
  <select @change="changeLocale($event.target.value)">
    <option value="ko">한글</option>
    <option value="en">English</option>
  </select>
</template>
```

### 4. 조건부 메시지

```vue
<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const status = ref('approved')

// 상태에 따른 메시지
const statusMessage = computed(() => {
  return t(`receipt.status.${status.value}`)
})
</script>
```

## 🔧 새로운 메시지 추가하기

### 1. 한글 메시지 추가 (`locales/ko/서비스명.js`)

```javascript
export default {
  // 기존 페이지
  userManagement: {
    title: '사용자 관리',
    // ... 기존 메시지
  },
  
  // 새로운 페이지 추가
  newPage: {
    title: '새 페이지',
    save: '저장',
    cancel: '취소',
    saveSuccess: '저장되었습니다',
  }
}
```

### 2. 영문 메시지 추가 (`locales/en/서비스명.js`)

```javascript
export default {
  // 기존 페이지
  userManagement: {
    title: 'User Management',
    // ... 기존 메시지
  },
  
  // 새로운 페이지 추가
  newPage: {
    title: 'New Page',
    save: 'Save',
    cancel: 'Cancel',
    saveSuccess: 'Saved successfully',
  }
}
```

### 3. 사용

```vue
<template>
  <h1>{{ $t('hrm.newPage.title') }}</h1>
  <button @click="save">{{ $t('hrm.newPage.save') }}</button>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const save = () => {
  // 저장 로직
  toast.success(t('hrm.newPage.saveSuccess'))
}
</script>
```

## 📦 새로운 서비스 추가하기

### 1. 메시지 파일 생성

```bash
# 한글 메시지 파일
locales/ko/newService.js

# 영문 메시지 파일
locales/en/newService.js
```

### 2. 메시지 내용 작성

```javascript
// locales/ko/newService.js
export default {
  page1: {
    title: '페이지1',
    // ... 메시지
  },
  page2: {
    title: '페이지2',
    // ... 메시지
  }
}
```

### 3. index.js에 등록

```javascript
// locales/ko/index.js
import common from './common'
import error from './error'
// ... 기존 import
import newService from './newService' // 추가

export default {
  common,
  error,
  // ... 기존 서비스
  newService, // 추가
}
```

### 4. 사용

```vue
<template>
  <h1>{{ $t('newService.page1.title') }}</h1>
</template>
```

## 🌍 백엔드와 연동

### Accept-Language 헤더 자동 설정

언어 변경 시 Axios의 `Accept-Language` 헤더가 자동으로 업데이트됩니다.

```javascript
// 사용자가 언어를 'en'으로 변경하면
changeLocale('en')

// 모든 API 요청에 다음 헤더가 자동 추가됨
// Accept-Language: en
```

### 백엔드 에러 메시지

백엔드에서 보낸 에러 메시지는 그대로 표시됩니다.
백엔드도 `Accept-Language` 헤더를 보고 적절한 언어로 응답합니다.

```javascript
// 백엔드 응답 예시
{
  "message": "이미 존재하는 이메일입니다" // 한국어 요청 시
  "message": "Email already exists"      // 영어 요청 시
}
```

## 🎨 VSCode 자동완성 설정

i18n Ally 확장 프로그램 설치 권장:
- 다국어 키 자동완성
- 번역 누락 확인
- 인라인 번역 미리보기

## ⚠️ 주의사항

1. **키는 항상 소문자와 점(.)으로 구성**
   ```javascript
   // ✅ 좋음
   t('common.button.save')
   
   // ❌ 나쁨
   t('Common.Button.Save')
   ```

2. **파라미터는 중괄호로 전달**
   ```javascript
   // ✅ 좋음
   t('common.validation.minLength', { min: 8 })
   
   // ❌ 나쁨
   t('common.validation.minLength', 8)
   ```

3. **번역 누락 시 키가 그대로 표시됨**
   ```javascript
   // 'common.nonexistent' 키가 없으면
   t('common.nonexistent') // 'common.nonexistent' 출력
   ```

4. **공통 메시지는 재사용**
   ```javascript
   // ✅ 좋음 - 공통 메시지 사용
   t('common.button.save')
   
   // ❌ 나쁨 - 중복 정의
   t('hrm.userManagement.saveButton')
   ```

## 🔄 마이그레이션 가이드

### 기존 하드코딩 → i18n 전환

```vue
<!-- Before -->
<template>
  <button>확인</button>
  <p>저장되었습니다</p>
</template>

<!-- After -->
<template>
  <button>{{ $t('common.button.confirm') }}</button>
  <p>{{ $t('common.message.saved') }}</p>
</template>
```

```javascript
// Before
toast.success('저장되었습니다')

// After
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
toast.success(t('common.message.saved'))
```

## 📚 참고 자료

- [Vue I18n 공식 문서](https://vue-i18n.intlify.dev/)
- [프로젝트 다국어 처리 가이드](/doc/개발 자료/다국어 처리.md)

