## CI/CD 배포 가이드 (요약)

이 문서는 **현재 CI/CD 구조에서 실제로 알아야 할 것만** 정리한 요약본입니다.  
설치/초기 Jenkins 세팅은 `JENKINS 설정 가이드.md`를 참고합니다.

---

### 1. 전체 흐름

- **개발자**
  - `develop` 브랜치에 코드 푸시
- **GitHub**
  - Webhook으로 Jenkins `centrally-build` 파이프라인 트리거
- **Jenkins (YOUR_JENKINS_SERVER_IP)**
  - 변경된 디렉토리 기준으로 어떤 서비스가 바뀌었는지 `git diff`로 분석
  - 변경된 서비스만 Gradle 빌드
  - 프론트 코드 변경 시 Vite 빌드 (Vue3)
  - 빌드 결과를 `/var/lib/jenkins/artifacts` 아래에 저장
  - SCP로 AP 서버(YOUR_AP_SERVER_IP)의 `/was/dummy_app`에 JAR / dist 전송
  - SSH로 AP 서버에서 `deploy-service.sh <서비스명>` 실행 (재시작)

---

### 2. 어떤 변경이 어느 서비스 빌드/배포로 이어지는지

| 변경 디렉토리                           | 빌드 대상           | 배포 대상          |
|----------------------------------------|--------------------|--------------------|
| `centrally-system-auth/`               | auth               | auth               |
| `centrally-system-hrm/`                | hrm                | hrm                |
| `centrally-system-gateway/`            | gateway            | gateway            |
| `centrally-system-info/`               | info               | info               |
| `centrally-system-receipt/`            | receipt            | receipt            |
| `centrally-web-vue3-vite/`             | frontend(Vite)     | Vue dist           |
| `centrally-system-core/`               | 모든 서비스         | 모든 서비스         |
| `jenkinsfile/`                         | 모든 서비스         | 모든 서비스         |

- **Config, Eureka**: 현재 Jenkins 파이프라인/배포 대상 **아님** (수동 관리).

---

### 3. Jenkins 쪽에서 중요한 것

- **워크스페이스**
  - `/var/lib/jenkins/workspace/CentrallySystem/centrally-build`
- **아티팩트 디렉토리**
  - `/var/lib/jenkins/artifacts`
  - 예)
    - `/var/lib/jenkins/artifacts/auth/auth-latest.jar`
    - `/var/lib/jenkins/artifacts/gateway/gateway-latest.jar`
    - `/var/lib/jenkins/artifacts/frontend/frontend-latest.tar.gz`
- **환경 변수 (Jenkinsfile `environment` 블록)**
  - `GRADLE_USER_HOME = '/var/lib/jenkins/.gradle'`
  - `ARTIFACTS_BASE_DIR = '/var/lib/jenkins/artifacts'`
  - `BUILD_ENV = 'prod'` → 프론트 빌드 시 `.env.prod` 사용
  - `AP_SERVER = 'ubuntu@{AP_SERVER_IP}'`
  - `SSH_KEY = '/var/lib/jenkins/.ssh/deploy-key'`
  - `AP_BASE_DIR = '/was/dummy_app'`

> Jenkins 관점에서 “배포”는 **SCP + SSH로 AP 서버에서 쉘 스크립트 실행**하는 것뿐입니다.

---

### 4. AP 서버 쪽에서 중요한 것

- **기본 디렉토리**
  - `/was/dummy_app`
  - 안에 있는 JAR 파일들:
    - `gateway.jar`, `auth.jar`, `hrm.jar`, `info.jar`, `receipt.jar`
  - 프론트 dist:
    - `/was/dummy_app/dist` (Nginx `root` 설정이 이 경로)
- **서비스별 포트** (Config `resources-dev-linux` 기준)
  - Gateway: `7070`
  - Auth   : `9111`
  - HRM    : `9999`
  - Info   : `9699`
  - Receipt: `9899`
- **배포 스크립트** (Jenkins가 호출)
  - `/was/dummy_app/deploy-service.sh`
  - 역할 (핵심 요약)
    1. `<서비스명>.jar` 실행 중 프로세스 찾기 → 종료 (우선 SIGTERM, 필요 시 SIGKILL)
    2. `/was/dummy_app/<서비스명>.jar` 새로 실행 (nohup)
    3. `http://localhost:<포트>/actuator/health`로 헬스체크
    4. 로그는 `/was/dummy_app/logs/<서비스명>.log` 에 쌓임

---

### 5. 실제로 쓸 일 있는 명령어만 (AP 서버 기준)

- **특정 서비스만 수동 재배포하고 싶을 때**
  - `bash /was/dummy_app/deploy-service.sh auth`
  - `bash /was/dummy_app/deploy-service.sh gateway`

- **서비스 상태 간단 확인**
  - 실행 중인 Java:
    - `ps aux | grep java`
  - 포트 열려 있는지:
    - `sudo netstat -nltp | grep 9111`   (auth)
    - `sudo netstat -nltp | grep 7070`   (gateway)

- **로그 조금만 보고 싶을 때**
  - `tail -50 /was/dummy_app/logs/auth.log`
  - `tail -50 /was/dummy_app/logs/gateway.log`

---

### 6. 요약

- Jenkins는 **변경된 서비스만 빌드**하고,  
  빌드된 JAR/dist를 `/var/lib/jenkins/artifacts` 에 저장한 뒤,
  **AP 서버 `/was/dummy_app`로 복사 + `deploy-service.sh` 실행**까지 담당합니다.
- AP 서버에서는 모든 것이 **`/was/dummy_app` 폴더 안**에서 돌아가며,  
  문제가 생기면 **해당 서비스 로그 / 프로세스 / 헬스체크**만 보면 됩니다.


