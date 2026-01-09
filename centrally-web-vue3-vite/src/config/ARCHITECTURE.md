## 🏗️ Centrally System 아키텍처 문서

<br/>

### 📌 핵심 개념
---
#### 라우터 파일 ≠ 서비스 ≠ 메뉴 카테고리

```
라우터 파일     →  UI 메뉴 카테고리 기준으로 구성
백엔드 서비스   →  비즈니스 도메인 기준으로 구성  
메뉴 카테고리   →  사용자 경험 기준으로 구성
```

<br/>

### 🎯 백엔드 서비스 구성
---
#### 1. HRM 서비스 (인사관리)
- 사용자 관리
- 조직 관리
- 실적 관리
- 권한 관리
- 즐겨찾기 관리

#### 2. Receipt 서비스 (영수증)
- 영수증 등록/조회
- 영수증 결재
- 영수증 통계

#### 3. Info 서비스 (정보)
- 공지사항 관리
- 자료실 관리
- 자유게시판 관리
- 가이드

#### 4. Gateway 서비스 (시스템)
- 활동 이력 관리
- 개인 이력 관리
- 시스템 로그

#### 5. Auth 서비스 (인증)
- OAuth 인증
- 임시 사용자 승인

<br/><br/>

### 🗂️ 프론트엔드 라우터 구조
---
#### `/info` → `info.js`
**백엔드 서비스**: INFO  
**메뉴**: 정보 워크스페이스

| 경로 | 기능 | 서비스 |
|------|------|--------|
| `/info/notice` | 공지사항 | INFO |
| `/info/resource` | 자료실 | INFO |
| `/info/community` | 자유게시판 | INFO |
| `/info/post-detail` | 게시글 상세 | INFO |
| `/info/post-create-mobile` | 게시글 작성 (모바일) | INFO |
| `/info/post-edit-mobile` | 게시글 수정 (모바일) | INFO |
| `/info/guide` | 가이드 | INFO |

---

#### `/user` → `user.js`
**백엔드 서비스**: HRM + Gateway (혼합)  
**메뉴**: 개인정보 워크스페이스

| 경로 | 기능 | 서비스 |
|------|------|--------|
| `/user/user-information` | 사용자 정보 | HRM |
| `/user/personal-history` | 개인 이력 | **Gateway** |

> ⚠️ **주의**: 개인 이력은 Gateway DB에 저장되지만, UI상으로는 개인정보 카테고리에 속함

---

#### `/management` → `management.js`
**백엔드 서비스**: HRM  
**메뉴**: 사원관리 워크스페이스

| 경로 | 기능 | 서비스 |
|------|------|--------|
| `/management/user-management` | 사용자 관리 | HRM |
| `/management/user-permissions` | 권한 부여 | HRM |
| `/management/dept-team-manager` | 부서/팀 관리 | HRM |
| `/management/org-directory` | 조직도 | HRM |
| `/management/performance-history` | 실적 이력 | HRM |
| `/management/performance-management` | 실적 관리 | HRM |
| `/management/favorite-menu-management` | 즐겨찾기 관리 | HRM |

---

#### `/receipt` → `receipt.js`
**백엔드 서비스**: Receipt  
**메뉴**: 영수증 워크스페이스

| 경로 | 기능 | 서비스 |
|------|------|--------|
| `/receipt/receipt-submission` | 영수증 등록 | Receipt |
| `/receipt/personal-receipt-history` | 개인 영수증 내역 | Receipt |
| `/receipt/annual-receipt-summary` | 연간 요약 | Receipt |
| `/receipt/receipt-approval-request-overview` | 결재 요청 현황 | Receipt |
| ... | ... | Receipt |

---

#### `/system` → `system.js`
**백엔드 서비스**: Gateway + 다중 서비스 (통합)  
**메뉴**: 시스템 워크스페이스

| 경로 | 기능 | 서비스 |
|------|------|--------|
| `/system/activity-log` | 활동 이력 관리 | Gateway |
| `/system/statistics-screen` | 통계 대시보드 | **다중 서비스** |
| `/system/role-management` | 역할 관리 | Gateway |
| `/system/kafka-outbox` | Kafka 모니터링 | Gateway |
| `/system/component-test` | 컴포넌트 테스트 | - |

> ⚠️ **주의**: 통계 대시보드는 HRM, Receipt, Gateway 등 여러 서비스의 데이터를 통합하여 차트로 표시

---

#### `/auth` → `auth.js`
**백엔드 서비스**: Auth  
**메뉴**: 없음 (독립적인 인증 프로세스)

| 경로 | 기능 | 서비스 |
|------|------|--------|
| `/auth/oauth-call-back` | OAuth 콜백 | Auth |
| `/auth/temp-user-approvals` | 임시 사용자 승인 | Auth |

---

<br/><br/>

### 📊 서비스 vs 메뉴 매핑
---
#### 서비스 기준
```
HRM Service
├── 사원관리 워크스페이스 (사용자, 조직, 실적, 권한)
└── 개인정보 워크스페이스 (사용자 정보)

Receipt Service
└── 영수증 워크스페이스 (등록, 결재, 내역, 통계)

INFO Service
└── 정보 워크스페이스 (공지사항, 자료실, 자유게시판, 가이드)

Gateway Service
├── 개인정보 워크스페이스 (개인 이력)
└── 시스템 워크스페이스 (활동 이력, 역할 관리, Kafka)

Auth Service
└── 독립적 인증 프로세스 (OAuth, 임시 승인)

#### 메뉴 카테고리 기준
```
정보 워크스페이스
├── 공지사항 (INFO)
├── 자료실 (INFO)
├── 자유게시판 (INFO)
└── 가이드 (INFO)

개인정보 워크스페이스
├── 사용자 정보 (HRM)
└── 개인 이력 (Gateway) ⚠️ 다른 서비스

사원관리 워크스페이스
├── 사용자·권한 (HRM)
├── 조직 (HRM)
├── 실적 (HRM)
└── 가입 승인 (Auth) ⚠️ 다른 서비스

영수증 워크스페이스
└── 모든 영수증 기능 (Receipt)

시스템 워크스페이스
├── 이력 관리 (Gateway)
├── 통계 대시보드 (다중 서비스) ⚠️ 여러 서비스 통합
└── 시스템 관리 (Gateway)
```

<br/><br/>

### 🔄 데이터 흐름
---
#### 1️⃣ 개인 이력 (Personal History)
```
사용자 행동 발생
    ↓
Gateway Service (이력 수집)
    ↓
Gateway DB 저장
    ↓
개인정보 워크스페이스 > 개인 이력 메뉴 (UI)
```

#### 2️⃣ 활동 이력 (Activity Log)
```
사용자 행동 발생
    ↓
Gateway Service (이력 수집)
    ↓
Gateway DB 저장
    ↓
시스템 워크스페이스 > 이력 관리 메뉴 (UI)
```

#### 3️⃣ 통계 대시보드
```
HRM Service (인사 데이터)
    ↓
Receipt Service (영수증 데이터)  →  통합
    ↓                               ↓
Gateway Service (시스템 데이터)    통계 대시보드
```

<br/><br/>

### 📁 파일 구조
---
#### 라우터 파일 (`src/router/`)
```
router/
├── info.js          → 정보 워크스페이스 라우트
├── user.js          → 개인정보 워크스페이스 라우트
├── management.js    → 사원관리 워크스페이스 라우트
├── receipt.js       → 영수증 워크스페이스 라우트
├── system.js        → 시스템 워크스페이스 라우트
├── auth.js          → 인증 관련 라우트 (메뉴 외)
└── index.js         → 라우터 통합 설정
```

#### 설정 파일 (`src/config/`)
```
config/
├── routeConfig.js   → 모든 경로 상수 정의
├── menuConfig.js    → 메뉴 구조 및 메타데이터
├── roleConfig.js    → 권한 및 워크스페이스 정의
└── ARCHITECTURE.md  → 본 문서
```

<br/><br/>

### 🎨 UI 워크스페이스 구조
---
#### FirstSidebar (1차 사이드바)
```
┌──────────────────┐
│   🏠 홈          │
├──────────────────┤
│   ℹ️ 정보        │ → /info/*
├──────────────────┤
│   🧾 영수증       │ → /receipt/*
├──────────────────┤
│   👥 사원관리     │ → /management/*
├──────────────────┤
│   👤 개인정보     │ → /user/*
├──────────────────┤
│   ⚙️ 시스템      │ → /system/*
├──────────────────┤
│   ⭐ 즐겨찾기     │
└──────────────────┘
```

#### SecondSidebar (2차 사이드바)
각 워크스페이스 선택 시 세부 메뉴 표시

<br/><br/>

### ⚙️ 설정 상수 구조
---
#### PATH_NAMES (경로 세그먼트)
```javascript
PATH_NAMES = {
  INFO: {
    NOTICE: 'notice',  // 세그먼트만
    RESOURCE: 'resource',
    COMMUNITY: 'community',
    GUIDE: 'guide',
  },
  USER: {
    USER_INFORMATION: 'user-information',
    PERSONAL_HISTORY: 'personal-history',
  },
  // ...
}
```

#### ROUTES (전체 경로)
```javascript
ROUTES = {
  INFO: {
    NOTICE: '/info/notice',  // 전체 경로
    RESOURCE: '/info/resource',
    COMMUNITY: '/info/community',
    GUIDE: '/info/guide',
  },
  USER: {
    USER_INFORMATION: '/user/user-information',
    PERSONAL_HISTORY: '/user/personal-history',
  },
  // ...
}
```

#### WORKSPACES (워크스페이스 정의)
```javascript
WORKSPACES = {
  HOME: 'home',
  INFO: 'info',
  RECEIPT: 'receipt',
  MANAGEMENT: 'management',
  USER: 'user',
  SYSTEM: 'system',
  FAVORITES: 'favorites',
}
```

<br/><br/>

### 🔀 라우터 vs 서비스 vs 메뉴 비교표
---
| 라우터 파일 | 라우터 경로 | 백엔드 서비스 | UI 워크스페이스 | 비고 |
|------------|------------|--------------|----------------|------|
| `info.js` | `/info/*` | **INFO** | 정보 | 공지사항, 자료실, 자유게시판, 가이드 |
| `user.js` | `/user/*` | **HRM + Gateway** | 개인정보 | 혼합 서비스 |
| `management.js` | `/management/*` | **HRM** | 사원관리 | 사용자, 조직, 실적 |
| `receipt.js` | `/receipt/*` | **Receipt** | 영수증 | 영수증 관련 모든 기능 |
| `system.js` | `/system/*` | **Gateway + 다중** | 시스템 | 통합 시스템 관리 |
| `auth.js` | `/auth/*` | **Auth** | 없음 | 독립 인증 프로세스 |

<br/><br/>

### 🎯 특수 케이스
---
#### Case 1: 개인 이력 (Personal History)
```
┌─────────────────────────────────────────────────────┐
│  UI 위치: 개인정보 워크스페이스 > 개인 이력              │
│  라우터: /user/personal-history                      │
│  서비스: Gateway (Gateway DB에 저장)                  │
│  라우터 파일: user.js                                 │
└─────────────────────────────────────────────────────┘

왜? → 이력 데이터는 Gateway에서 관리하지만,
      UI상으로는 개인정보 카테고리에 속함
```

#### Case 2: 활동 이력 (Activity Log)
```
┌─────────────────────────────────────────────────────┐
│  UI 위치: 시스템 워크스페이스 > 이력 관리                │
│  라우터: /system/activity-log                        │
│  서비스: Gateway (Gateway DB에 저장)                  │
│  라우터 파일: system.js                               │
└─────────────────────────────────────────────────────┘

왜? → 시스템 관리자용 전체 활동 이력 조회
```

#### Case 3: 통계 대시보드
```
┌─────────────────────────────────────────────────────┐
│  UI 위치: 시스템 워크스페이스 > 통계 화면                │
│  라우터: /system/statistics-screen                   │
│  서비스: HRM + Receipt + Gateway (다중 서비스 통합)    │
│  라우터 파일: system.js                               │
└─────────────────────────────────────────────────────┘

왜? → 여러 서비스의 데이터를 통합하여 차트로 표시
      (부서별 지출, 영수증 통계, 실적 통계 등)
```

#### Case 4: 임시 사용자 승인
```
┌─────────────────────────────────────────────────────┐
│  UI 위치: 사원관리 워크스페이스 > 가입 승인              │
│  라우터: /auth/temp-user-approvals                   │
│  서비스: Auth                                        │
│  라우터 파일: auth.js                                 │
└─────────────────────────────────────────────────────┘

왜? → Auth 서비스이지만 UI상으로는 사원관리 카테고리에 속함
```

<br/><br/>

### 🔍 설계 원칙
---
#### 1. 라우터 파일 = UI 카테고리 기준
- 사용자 경험을 최우선으로 함
- 메뉴 구조와 일치하여 직관적
- 예: `/user/user-information` → 개인정보 워크스페이스

#### 2. 백엔드 서비스 = 비즈니스 도메인 기준
- 데이터 저장소와 API가 서비스별로 분리
- 예: 개인 이력은 Gateway DB에 저장

#### 3. 유연한 매핑
- 라우터 경로와 백엔드 서비스가 1:1로 매핑되지 않아도 OK
- UI 카테고리가 여러 서비스를 통합할 수 있음
- 예: 개인정보 워크스페이스 = HRM + Gateway

<br/><br/>

### 📝 메뉴와 무관한 라우트
---
#### 1. 인증 프로세스 (`/auth/*`)
- OAuth 콜백
- 임시 사용자 승인
- **이유**: 메뉴 시스템과 독립적으로 작동해야 함

#### 3. 전역 페이지
- `/` - 로그인
- `/join` - 가입
- `/main` - 메인 페이지
- **이유**: 모든 워크스페이스와 독립적

<br/><br/>

### 🎯 요약
---
#### 핵심 포인트
1. **라우터 경로** = 사용자가 보는 UI 구조
2. **백엔드 서비스** = 데이터가 저장되는 실제 위치
3. **메뉴 카테고리** = 사용자 경험을 위한 분류

#### 설계 철학
```
사용자 경험 > 기술 구조
```

UI는 사용자 친화적으로, 백엔드는 비즈니스 도메인별로 분리하여  
각자의 장점을 살리는 **하이브리드 아키텍처**를 채택했습니다.

<br/><br/>

### 📌 주의사항
---
#### ⚠️ 라우터 경로 변경 시
1. `routeConfig.js`의 `ROUTES` 섹션 업데이트
2. 해당 라우터 파일 수정
3. `menuConfig.js`의 메뉴 경로 확인
4. UI 컴포넌트에서 사용하는 `ROUTES.*` 참조 확인

#### ⚠️ 새로운 메뉴 추가 시
1. 백엔드 서비스 확인 (HRM? Receipt? INFO? Gateway?)
2. UI 카테고리 결정 (어느 워크스페이스?)
3. 라우터 파일 선택 (카테고리 기준)
4. `menuConfig.js`에 메뉴 정의 추가

#### ⚠️ 서비스와 UI가 다를 때
- **개인 이력**: Gateway 서비스 → 개인정보 워크스페이스
- **통계 대시보드**: 다중 서비스 → 시스템 워크스페이스
- **임시 승인**: Auth 서비스 → 사원관리 워크스페이스

이런 경우는 **정상적인 설계**입니다!

---

*이전 업데이트: 2025-10-23*
*최종 업데이트: 2025-10-27*
