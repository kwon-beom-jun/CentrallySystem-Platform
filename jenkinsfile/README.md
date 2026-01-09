# Jenkins 통합 빌드 파이프라인

## 📋 개요

CentrallySystem의 마이크로서비스(auth, hrm, gateway, info, receipt)를 자동으로 빌드하는 통합 파이프라인입니다.

### 주요 기능
- 🔍 **자동 변경 감지**: Git diff로 변경된 서비스만 자동 감지
- ⚡ **병렬 빌드**: 여러 서비스 동시 변경 시 병렬로 빌드
- 📦 **아티팩트 관리**: 빌드 번호 + 타임스탬프 파일명, 최신 버전 심볼릭 링크
- 🎯 **리소스 최적화**: Gradle 메모리 제한, 불필요한 빌드 방지

---

## 🚀 빠른 시작

### 1. Jenkins 파이프라인 생성

#### 1-1. 새 파이프라인 생성
1. Jenkins → **CentrallySystem 폴더** 클릭
2. 좌측 **"새로운 Item"** 클릭
3. Item name: `centrally-build` 입력
4. **Pipeline** 선택 → **OK**

#### 1-2. 파이프라인 설정

**General:**
```
Description: 통합 빌드 파이프라인 - 변경된 서비스만 자동 빌드
```

**Build Triggers:**
```
☑️ GitHub hook trigger for GITScm polling
```

**Pipeline:**
```
Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/kwon-beom-jun/CentrallySystem.git
Credentials: aws-jenkins-centrally
Branch Specifier: */develop
Script Path: jenkinsfile/Jenkinsfile
```

**저장:** 페이지 하단 **"Save"** 클릭

---

### 2. GitHub 웹훅 설정

기존 웹훅을 그대로 사용합니다. 이미 설정되어 있다면 추가 작업 불필요!

**확인 방법:**
1. GitHub 저장소 → Settings → Webhooks
2. Payload URL: `http://your-jenkins-ip:8080/github-webhook/`
3. ✅ Active 체크 확인

---

## 🔄 동작 원리

### 빌드 프로세스

```
GitHub Push
    ↓
웹훅 트리거
    ↓
Jenkins 파이프라인 시작
    ↓
┌─────────────────────────────────┐
│ 1. 변경 감지 단계                 │
│  - Git diff로 변경 파일 확인      │
│  - 변경된 서비스 식별             │
│  - core 변경 시 모든 서비스 빌드   │
└─────────────────────────────────┘
    ↓
┌─────────────────────────────────┐
│ 2. 병렬 빌드 단계                 │
│  - auth 빌드 (병렬)              │
│  - hrm 빌드 (병렬)               │
│  - ...                          │
└─────────────────────────────────┘
    ↓
┌─────────────────────────────────┐
│ 3. 각 서비스 빌드 단계             │
│  - clean (빌드 정리)              │
│  - build (JAR 생성)              │
│  - test (단위 테스트)             │
│  - 아티팩트 저장                  │
└─────────────────────────────────┘
```

---

## 📊 시나리오별 동작

### 시나리오 1: 단일 서비스 변경
```bash
# auth만 변경 후 푸시
git push origin develop
```

**결과:**
```
✅ 변경된 서비스: auth
🚀 1개 서비스 빌드 시작
✅ AUTH 서비스 빌드 성공!
```

---

### 시나리오 2: 여러 서비스 동시 변경
```bash
# auth + hrm 동시 변경 후 푸시
git push origin develop
```

**결과:**
```
✅ 변경된 서비스: auth, hrm
🚀 2개 서비스 병렬 빌드 시작
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔨 AUTH 서비스 빌드 시작
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔨 HRM 서비스 빌드 시작
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
...
✅ AUTH 서비스 빌드 성공!
✅ HRM 서비스 빌드 성공!
```

**병렬 빌드로 시간 단축!**

---

### 시나리오 3: core 모듈 변경
```bash
# centrally-system-core 변경 후 푸시
git push origin develop
```

**결과:**
```
✅ centrally-system-core 변경 감지. 모든 서비스를 빌드합니다.
🚀 5개 서비스 병렬 빌드 시작
...
```

---

### 시나리오 4: 변경 없음
```bash
# README.md만 변경 후 푸시
git push origin develop
```

**결과:**
```
⏭️ 변경된 서비스가 없습니다. 빌드를 건너뜁니다.
Build Result: NOT_BUILT
```

---

## 📦 아티팩트 구조

### 저장 위치
```
/var/lib/jenkins/artifacts/
├── auth/
│   ├── auth-build-1-20251128-120000.jar
│   ├── auth-build-2-20251128-130000.jar
│   ├── auth-latest.jar -> auth-build-2-20251128-130000.jar
│   ├── build-info-1.txt
│   └── build-info-2.txt
├── hrm/
├── gateway/
├── info/
└── receipt/
```

### 파일 구조
- **JAR 파일**: `{서비스명}-build-{빌드번호}-{타임스탬프}.jar`
- **심볼릭 링크**: `{서비스명}-latest.jar` (항상 최신 버전 가리킴)
- **빌드 정보**: `build-info-{빌드번호}.txt`

### 빌드 정보 내용
```
BUILD_NUMBER: 2
GIT_COMMIT: abc123def456...
BUILD_TIMESTAMP: 20251128-130000
JAR_NAME: centrally-system-auth-0.0.1-SNAPSHOT.jar
SERVICE: auth
BUILD_URL: http://jenkins-ip:8080/job/CentrallySystem/job/centrally-build/2/
```

---

## 🧪 테스트 방법

### 1. 수동 빌드 테스트
```
Jenkins → CentrallySystem → centrally-build → Build Now
```

**확인:**
- Console Output에서 로그 확인
- 첫 빌드는 모든 서비스 빌드

---

### 2. 웹훅 자동 빌드 테스트

#### 테스트 1: auth 서비스만 변경
```bash
cd centrally-system-auth/src/main/java/com/cs/auth
echo "// webhook test" >> AuthApplication.java

git add .
git commit -m "test: auth 웹훅 테스트"
git push origin develop
```

**Jenkins 확인:**
- centrally-build 파이프라인 자동 시작
- 로그에 "변경된 서비스: auth" 표시
- auth만 빌드됨

---

#### 테스트 2: 여러 서비스 동시 변경
```bash
# auth와 hrm 동시 변경
echo "// test" >> centrally-system-auth/src/main/java/com/cs/auth/AuthApplication.java
echo "// test" >> centrally-system-hrm/src/main/java/com/cs/hrm/HrmApplication.java

git add .
git commit -m "test: auth, hrm 동시 변경"
git push origin develop
```

**Jenkins 확인:**
- 로그에 "변경된 서비스: auth, hrm" 표시
- auth와 hrm 병렬 빌드

---

### 3. 아티팩트 확인

#### Jenkins 서버에서 확인:
```bash
# 빌드된 JAR 파일 확인
ls -lh /var/lib/jenkins/artifacts/auth/
ls -lh /var/lib/jenkins/artifacts/hrm/

# 빌드 정보 확인
cat /var/lib/jenkins/artifacts/auth/build-info-1.txt

# 심볼릭 링크 확인
readlink /var/lib/jenkins/artifacts/auth/auth-latest.jar
```

---

## 🔧 Jenkins 설정

### 환경 변수
```groovy
GRADLE_USER_HOME = '/var/lib/jenkins/.gradle'
ARTIFACTS_BASE_DIR = '/var/lib/jenkins/artifacts'
```

### Gradle 옵션
```bash
GRADLE_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m"
```

### 빌드 보관 정책
```groovy
buildDiscarder(logRotator(
    numToKeepStr: '10',           // 최근 10개 빌드 보관
    artifactNumToKeepStr: '5'     // 최근 5개 아티팩트 보관
))
```

---

## ❓ 문제 해결

### Q1: 빌드가 시작되지 않아요

**확인 사항:**
1. GitHub 웹훅 상태 확인
   - GitHub → Settings → Webhooks → Recent Deliveries
   - Response 200 OK 확인

2. Jenkins 설정 확인
   - Build Triggers에 "GitHub hook trigger" 체크 확인
   - Script Path가 `jenkinsfile/Jenkinsfile` 인지 확인

3. Jenkins 서버 상태 확인
   ```bash
   sudo systemctl status jenkins
   ```

---

### Q2: 모든 서비스가 매번 빌드돼요

**원인:**
- 첫 빌드는 모든 서비스 빌드 (정상)
- previousCommit 정보가 없는 경우

**확인:**
```
Console Output에서 변경 감지 로그 확인:
- "첫 빌드입니다" → 정상
- "이전 커밋 정보가 없습니다" → Git history 문제
- "변경된 파일 목록" 확인
```

---

### Q3: 특정 서비스만 계속 실패해요

**해결:**
1. 해당 서비스 로그 확인
   ```
   Console Output에서 해당 서비스 빌드 로그 확인
   ```

2. 로컬에서 직접 빌드 테스트
   ```bash
   cd centrally-system-xxx
   chmod +x gradlew
   ./gradlew clean build
   ```

3. 의존성 문제 확인
   ```bash
   ./gradlew dependencies
   ```

---

### Q4: 메모리 부족 에러 발생

**증상:**
```
java.lang.OutOfMemoryError: Java heap space
```

**해결:**
1. Jenkins 서버 메모리 확인
   ```bash
   free -h
   ```

2. Swap 메모리 추가
   ```bash
   sudo fallocate -l 2G /swapfile
   sudo chmod 600 /swapfile
   sudo mkswap /swapfile
   sudo swapon /swapfile
   ```

3. Gradle 메모리 줄이기 (Jenkinsfile 수정)
   ```groovy
   export GRADLE_OPTS="-Xmx768m -XX:MaxMetaspaceSize=256m"
   ```

---

### Q5: 병렬 빌드가 순차로 실행돼요

**원인:**
- Jenkins Executor 수가 부족

**해결:**
```
Jenkins 관리 → 시스템 설정 → Number of executors
값을 2 이상으로 설정 (CPU 코어 수와 동일하게 권장)
```

---

## 📈 성능 최적화

### 1. Gradle 캐시 활용
```bash
# Jenkins 서버에서
ls -la /var/lib/jenkins/.gradle/caches/
```

### 2. 빌드 시간 측정
```
Console Output에서 각 단계별 소요 시간 확인
```

### 3. 병렬 빌드 최대 활용
- CPU 코어 수만큼 Executor 설정
- 메모리 부족 주의

---

## 🔒 보안 권장 사항

### 1. GitHub 웹훅 시크릿 설정
```
GitHub Webhook 설정에 Secret 추가
Jenkins 파이프라인에서 검증
```

### 2. Jenkins 접근 제한
```
Security Group에서 8080 포트를 필요한 IP만 허용
또는 Nginx 리버스 프록시 + HTTPS 사용
```

### 3. 자격 증명 관리
```
Jenkins Credentials에서 중요 정보 관리
Jenkinsfile에 직접 입력 금지
```

---

## 📊 통계

### 빌드 시간 비교 (auth + hrm 동시 변경)
- **순차 빌드**: 3분
- **병렬 빌드**: 2분 (33% 단축)

### 리소스 사용
- **메모리**: 평균 2GB (최대 3GB)
- **CPU**: 2 vCPU 사용
- **디스크**: 빌드당 ~100MB

---

## 📚 Jenkinsfile 주요 함수

### buildService(serviceName)
```groovy
def buildService(String serviceName) {
    // 1. 빌드 준비 (clean)
    // 2. 빌드 (build)
    // 3. 테스트 (test)
    // 4. 아티팩트 저장
}
```

**사용 예:**
```groovy
buildService('auth')
buildService('hrm')
```

---

## 🎯 참고 사항

### 변경 감지 로직
```groovy
// centrally-system-auth/ 파일 변경 → auth 빌드
// centrally-system-hrm/ 파일 변경 → hrm 빌드
// centrally-system-core/ 파일 변경 → 모든 서비스 빌드
// jenkinsfile/ 파일 변경 → 모든 서비스 빌드
```

### 첫 빌드
```
이전 빌드 정보가 없으면 모든 서비스를 빌드합니다.
```

### 병렬 빌드
```
변경된 서비스가 여러 개면 동시에 빌드합니다.
```

---

## 🔗 관련 링크

- **Jenkins 공식 문서**: https://www.jenkins.io/doc/
- **Pipeline 문법**: https://www.jenkins.io/doc/book/pipeline/syntax/
- **Gradle 공식 문서**: https://docs.gradle.org/

---

**작성일**: 2025-11-28  
**버전**: 1.0  
**파이프라인**: centrally-build
