# ✅ Outbox + Saga 전환 작업 정리

## 1. 공통 구조
| 단계 | Auth 서비스 | Kafka | HRM 서비스 |
|------|-------------|-------|------------|
| ① 가입 API | `users.status = PENDING`<br/>`outbox_events` : **user.registered (READY)** |  |  |
| ② 퍼블리셔 | `user.registered` 발행 | ▶ |  |
| ③ 리스너 |  |  | `UserRegistrationSagaService`<br/>∙ HRM DB `hrm_users` INSERT 시도<br/>∙ 성공 ⇒ **hrm.profile.created**<br/>∙ 실패 ⇒ **hrm.profile.failed**<br/>`outbox_events` (READY) |
| ④ 퍼블리셔 |  | ◀ `hrm.profile.created / failed` |  |
| ⑤ 리스너 | `UserSagaHandler`<br/>∙ created  ⇒ `users.status = ACTIVE`<br/>∙ failed    ⇒ `users.status = FAILED` |  |  |

* 멱등성  
  * `processed_events` 테이블을 Auth·HRM 각각에 두어 **중복 처리 방지**
* 재시도 / DLT  
  * `DefaultErrorHandler + DeadLetterPublishingRecoverer` 설정 (3회 재시도 후 *.DLT)

---

## 2. 코드/패키지 구성

### core 모듈
| 파일 | 설명 |
|------|------|
| `ProcessingStatus` | PENDING / ACTIVE / FAILED 공통 ENUM |

### auth 서비스
| 영역 | 주요 파일 | 내용 |
|------|-----------|------|
| Outbox | `com.cs.auth.outbox.*`<br/>`OutboxEvent / Repository / Publisher` | 0.3초 간격 Kafka 송신 |
| 엔티티 | `AuthUser` | `ProcessingStatus status` 추가 |
| 서비스 | `AuthUserService` | 가입/삭제/활성토글 → **Outbox INSERT** |
| Listener | `HrmEventsListener` | HRM 결과 수신 → 상태 전이 |
| 스케줄러 | `@EnableScheduling` 추가 |

### hrm 서비스
| 영역 | 주요 파일 | 내용 |
|------|-----------|------|
| Outbox | `com.cs.hrm.outbox.*` | 1초 간격 Kafka 송신 |
| Saga | `UserRegisteredListener` | `user.registered` 수신 |
| Saga | `UserRegistrationSagaService` | DB 저장·결과 Outbox 기록 |
| 멱등 | `ProcessedEvent / Repo` | 이벤트 중복 방지 |
| 스케줄러 | `@EnableScheduling` 추가 |

---

## 3. 제거·변경된 레거시 코드
| 모듈 | 삭제/미사용 |
|------|-------------|
| auth | `sendUserJoinedEvent()` 메서드 & 호출부 |
| hrm  | `consumeAuthUserJoined()` (기존 가입 이벤트 리스너) |
| core | 이전 `OutboxEvent / Repository` (공통 위치 → 각 서비스로 이동) |

---

## 4. 테스트 체크리스트
1. **Kafka 토픽**  
   * `user.registered` → `hrm.profile.created | failed`
2. **DB 테이블**  
   * `auth.outbox_events`, `hrm.outbox_events` : READY → SENT  
   * `auth.users.status` : PENDING → ACTIVE / FAILED
3. **DLT**  
   * HRM DB 에러 등 발생 시 `user.registered.DLT` 에 적재되는지 확인

---

## 5. TODO / 향후 과제
- 필요 없는 Producer / Consumer 클래스 완전 제거 or 리팩터링
- Outbox 테이블 인덱스 최적화 (`status, created_at`)
- DLT 브리지 구현 (필요 시 `hrm.profile.failed` 대체 발행)
- 부서·팀 변경 등 HRM 자체 이벤트도 동일 Outbox 패턴으로 이관 예정

> 위 내용 기준으로 **회원 가입 Saga** 는 완전히 Outbox 기반으로 동작합니다.