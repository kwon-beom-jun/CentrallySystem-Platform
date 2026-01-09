# Jenkins CI/CD 설정 가이드

## 📋 목차
1. [EC2 서버 초기 설정](#1-ec2-서버-초기-설정)
2. [Jenkins 플러그인 설치](#2-jenkins-플러그인-설치)
3. [Jenkins 도구 설정](#3-jenkins-도구-설정)
4. [Jenkins 프로젝트 생성](#4-jenkins-프로젝트-생성)
5. [GitHub 연동](#5-github-연동)
6. [GitHub 웹훅 설정](#6-github-웹훅-설정)
7. [SSH 키 설정](#7-ssh-키-설정)
8. [테스트](#8-테스트)

---

## 1. EC2 서버 초기 설정

### 1-1. EC2 접속
```bash
ssh -i "your-key.pem" ubuntu@YOUR_JENKINS_SERVER_IP
```

### 1-2. Jenkins 설치
```bash
# 패키지 목록 업데이트
sudo apt update

# Jenkins 저장소 키 추가
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee \
  /usr/share/keyrings/jenkins-keyring.asc > /dev/null

# Jenkins 저장소 추가
echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null

# 패키지 목록 다시 업데이트
sudo apt update

# Jenkins 설치
sudo apt install -y jenkins

# Jenkins 서비스 시작 및 자동 시작 설정
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Jenkins 상태 확인
sudo systemctl status jenkins

# jenkins 사용자 확인
id jenkins
# 출력 예시: uid=xxx(jenkins) gid=xxx(jenkins) groups=xxx(jenkins)

# Jenkins 초기 비밀번호 확인 (웹 UI 접속 시 필요)
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### 1-3. 필수 디렉토리 생성 및 권한 설정
```bash
# 아티팩트 저장 디렉토리 생성
sudo mkdir -p /var/lib/jenkins/artifacts/{auth,hrm,gateway,info,receipt,frontend}
sudo mkdir -p /var/lib/jenkins/.gradle
sudo mkdir -p /var/lib/jenkins/.ssh

# 권한 설정 (Jenkins 설치 후 실행)
sudo chown -R jenkins:jenkins /var/lib/jenkins/artifacts
sudo chown -R jenkins:jenkins /var/lib/jenkins/.gradle
sudo chown -R jenkins:jenkins /var/lib/jenkins/.ssh

# 디렉토리 확인
ls -la /var/lib/jenkins/artifacts/
```

### 1-4. Java 17 설치
```bash
# Java 버전 확인
java -version

# Java 17이 없다면 설치
sudo apt update
sudo apt install -y openjdk-17-jdk

# 설치 확인
java -version
# 출력 예시: openjdk version "17.0.x"
```

### 1-5. Git 설치
```bash
# Git 확인
git --version

# 없다면 설치
sudo apt install -y git
```

### 1-6. Node.js 설치 (프론트엔드 빌드용)

#### NodeSource 저장소 직접 설정:
```bash
# 필수 패키지 설치
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

# GPG 키 디렉토리 생성 및 키 추가
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | sudo gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg

# Node.js 저장소 추가 (최신 버전)
echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_lts.x nodistro main" | sudo tee /etc/apt/sources.list.d/nodesource.list

# 저장소 업데이트 및 설치
sudo apt-get update
sudo apt-get install -y nodejs npm

# 설치 확인
node -v
npm -v

# jenkins 사용자로도 확인
sudo su - jenkins -c "node -v"
sudo su - jenkins -c "npm -v"
```

### 1-7. AWS Security Group 설정
**EC2 콘솔에서:**
1. EC2 인스턴스 선택
2. Security 탭 → Security groups 클릭
3. Inbound rules → Edit inbound rules
4. **Add rule**:
   - Type: `Custom TCP`
   - Port range: `8080`
   - Source: `0.0.0.0/0` (개발용) 또는 본인 IP
   - Description: `Jenkins Web UI`
5. Save rules

---

## 2. Jenkins 플러그인 설치

### 2-1. Jenkins 웹 접속
브라우저에서 `http://your-ec2-ip:8080`

### 2-2. 플러그인 설치
1. **Jenkins 관리** (Manage Jenkins) 클릭
2. **플러그인 관리** (Manage Plugins) 클릭
3. **Available plugins** 탭 선택
4. 검색창에서 "GitHub Integration Plugin" 검색
5. **GitHub Integration Plugin** 체크
6. **Install without restart** 클릭
7. 설치 완료 대기 (1-2분)
8. **Go back to the top page** 클릭

**참고:** 기본 설치 시 대부분의 필수 플러그인(Pipeline, Gradle, Workspace Cleanup 등)이 이미 설치되어 있습니다.

---

## 3. Jenkins 도구 설정

### 3-1. JDK 설정
1. **Jenkins 관리** → **Tools** 클릭
2. **JDK installations...** 섹션 찾기
3. **Add JDK** 클릭
4. 입력:
   ```
   Name: JDK17
   JAVA_HOME: /usr/lib/jvm/java-17-openjdk-amd64
   ☐ Install automatically (체크 해제)
   ```
5. **Save** 클릭

### 3-2. Gradle 설정
1. 같은 페이지에서 **Gradle installations...** 섹션 찾기
2. **Add Gradle** 클릭
3. 입력:
   ```
   Name: Gradle-8
   ☑ Install automatically (체크)
   Install from Gradle.org 선택
   Version: Gradle 8.5
   ```
4. **Save** 클릭

---

## 4. Jenkins 프로젝트 생성

### 4-1. 통합 파이프라인 생성
1. Jenkins 홈 → **새로운 Item** 클릭
2. Item name: `centrally-build` 입력
3. **Pipeline** 선택
4. **OK** 클릭

### 4-2. 파이프라인 설정
1. **Pipeline** 섹션으로 스크롤
2. 다음 입력:
   ```
   Definition: Pipeline script from SCM
   SCM: Git
   Repository URL: https://github.com/dummy-username/dummy-repo.git
   Credentials: github-token (섹션 5에서 생성할 것)
   Branch Specifier: */develop
   Script Path: jenkinsfile/Jenkinsfile
   ```
3. **Build Triggers** 섹션:
   - ☑ **GitHub hook trigger for GITScm polling** 체크
4. **Save** 클릭

---

## 5. GitHub 연동

### 5-1. GitHub Personal Access Token 생성
1. GitHub 로그인
2. 우측 상단 프로필 → **Settings**
3. 좌측 하단 **Developer settings**
4. **Personal access tokens** → **Tokens (classic)**
5. **Generate new token** → **Generate new token (classic)**
6. 입력:
   ```
   Note: Jenkins CI/CD
   Expiration: 90 days
   Scopes:
   ☑ repo (전체)
   ☑ admin:repo_hook (전체)
   ```
7. **Generate token** 클릭
8. 🔑 **토큰 복사** (한 번만 보임!)

### 5-2. Jenkins에 자격 증명 추가
1. Jenkins → **Jenkins 관리** → **Credentials**
2. **System** → **Global credentials (unrestricted)**
3. **Add Credentials** 클릭
4. 입력:
   ```
   Kind: Secret text
   Secret: (GitHub 토큰 붙여넣기)
   ID: github-token
   Description: GitHub Personal Access Token
   ```
5. **Create** 클릭

### 5-3. 파이프라인에 자격 증명 연결
1. Jenkins → `centrally-build` → **구성** 클릭
2. Pipeline 섹션 → Repository URL 옆 **Credentials** 드롭다운
3. `github-token` 선택
4. **Save** 클릭

---

## 6. GitHub 웹훅 설정

### 6-1. 웹훅 추가
1. GitHub 저장소 → **Settings** 탭
2. 좌측 **Webhooks** → **Add webhook**
3. 입력:
   ```
   Payload URL: http://YOUR_JENKINS_SERVER_IP:8080/github-webhook/
   (⚠️ 마지막 슬래시 꼭 포함!)
   
   Content type: application/json
   
   Secret: (비워두기)
   
   Which events: Just the push event
   
   ☑ Active
   ```
4. **Add webhook** 클릭

### 6-2. 웹훅 연결 확인
- 초록색 체크마크(✓) 표시 → 성공
- 빨간색 X 표시 → 실패
  - EC2 Security Group 8080 포트 확인
  - Payload URL 끝에 슬래시(`/`) 확인

---

## 7. SSH 키 설정

### 7-1. Jenkins 서버에서 SSH 키 생성
```bash
# Jenkins 서버에 SSH 접속
ssh -i "your-key.pem" ubuntu@YOUR_JENKINS_SERVER_IP

# jenkins 사용자로 전환
sudo su - jenkins

# .ssh 디렉토리 생성 (없는 경우)
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# SSH 키 생성 (프라이빗키 + 퍼블릭키)
ssh-keygen -t rsa -b 4096 -f ~/.ssh/deploy-key -N ""

# 권한 설정 (중요!)
chmod 600 ~/.ssh/deploy-key      # 프라이빗키: 600
chmod 644 ~/.ssh/deploy-key.pub  # 퍼블릭키: 644
chmod 700 ~/.ssh                 # 디렉토리: 700

# 퍼블릭키 출력 (이걸 복사해서 AP 서버에 등록)
cat ~/.ssh/deploy-key.pub
# 출력 예시: ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC... jenkins@ip-xxx-xxx-xxx-xxx

# jenkins 사용자에서 나가기
exit
```

### 7-2. AP 서버에 퍼블릭키 등록
```bash
# AP 서버에 SSH 접속
ssh -i "your-key.pem" ubuntu@YOUR_AP_SERVER_IP

# .ssh 디렉토리 확인 및 생성
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# Jenkins에서 복사한 퍼블릭키를 authorized_keys에 추가
# (위에서 cat ~/.ssh/deploy-key.pub로 확인한 내용을 복사)
echo "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC... jenkins@ip-xxx-xxx-xxx-xxx" >> ~/.ssh/authorized_keys

# 권한 설정 (중요!)
chmod 600 ~/.ssh/authorized_keys
chmod 700 ~/.ssh

# 소유자 확인
ls -la ~/.ssh/
# 출력 예시:
# drwx------ ... .ssh/
# -rw------- ... authorized_keys
```

### 7-3. SSH 연결 테스트
```bash
# Jenkins 서버에서 (jenkins 사용자로)
sudo su - jenkins

# AP 서버 연결 테스트
ssh -i ~/.ssh/deploy-key -o StrictHostKeyChecking=no ubuntu@{AP_SERVER_IP} "echo '✅ SSH 연결 성공!'"

# SCP 테스트 (파일 전송 테스트)
echo "test" > /tmp/test.txt
scp -i ~/.ssh/deploy-key /tmp/test.txt ubuntu@{AP_SERVER_IP}:/tmp/
ssh -i ~/.ssh/deploy-key ubuntu@{AP_SERVER_IP} "cat /tmp/test.txt"
# 출력: test

# 배포 디렉토리 접근 테스트
ssh -i ~/.ssh/deploy-key ubuntu@{AP_SERVER_IP} "ls -la /was/dummy_app/"
```

### 7-4. Jenkinsfile 환경 변수 수정
`jenkinsfile/Jenkinsfile` 파일의 17번 라인을 수정:
```groovy
AP_SERVER = 'ubuntu@{실제_AP_서버_IP}'  // AP 서버 주소로 변경
```

---

## 8. 테스트

### 8-1. 수동 빌드 테스트
1. Jenkins → `centrally-build` 클릭
2. **Build Now** 클릭
3. 좌측 하단 Build History에서 #1 클릭
4. **Console Output** 클릭
5. 로그 확인:
   ```
   ✅ 변경된 서비스 감지
   🔨 서비스 빌드 시작
   ✅ 빌드 성공
   ```

### 8-2. 웹훅 자동 빌드 테스트
```bash
# 로컬에서 아무 서비스나 수정
cd centrally-system-auth/src/main/java/com/cs/auth
echo "// 웹훅 테스트" >> AuthApplication.java

git add .
git commit -m "test: CI/CD 웹훅 테스트"
git push origin develop
```

**Jenkins에서 확인:**
- 10초 내에 자동으로 빌드 시작
- 변경된 서비스만 빌드됨 (변경 감지 작동!)

### 8-3. 빌드 결과물 확인
```bash
# EC2 서버에서 확인
ls -lh /var/lib/jenkins/artifacts/auth/
# 출력:
# auth-build-1-20251128-143522.jar
# auth-latest.jar -> auth-build-1-20251128-143522.jar
# build-info-1.txt
```

---

## ✅ 완료 체크리스트

### EC2 서버 설정
- [ ] Java 17 설치
- [ ] Node.js 설치 (최신 버전)
- [ ] 아티팩트 디렉토리 생성
- [ ] Security Group 8080 포트 오픈

### Jenkins 설정
- [ ] GitHub Integration Plugin 설치
- [ ] JDK17 도구 설정
- [ ] Gradle-8 도구 설정

### Jenkins 프로젝트
- [ ] centrally-build 파이프라인 생성
- [ ] GitHub 연동 (Credentials)
- [ ] GitHub 웹훅 설정
- [ ] Jenkinsfile 경로 설정

### SSH 키 설정
- [ ] Jenkins 서버에서 SSH 키 생성
- [ ] AP 서버에 퍼블릭키 등록
- [ ] SSH 연결 테스트 성공
- [ ] Jenkinsfile의 AP_SERVER IP 수정

### 테스트
- [ ] 수동 빌드 테스트 성공
- [ ] 웹훅 자동 빌드 테스트 성공
- [ ] 변경 감지 기능 확인
- [ ] 아티팩트 저장 확인

---

## 🔧 문제 해결

### 빌드가 시작되지 않을 때
```bash
# Jenkins 로그 확인
sudo tail -f /var/lib/jenkins/jenkins.log

# Jenkins 재시작
sudo systemctl restart jenkins
```

### Gradle 빌드 실패 시
```bash
# EC2에서 직접 빌드 테스트
cd /var/lib/jenkins/workspace/centrally-build
cd centrally-system-auth
./gradlew build --stacktrace
```

### 웹훅이 작동하지 않을 때
1. GitHub → Settings → Webhooks → Recent Deliveries 확인
2. Response 탭에서 에러 메시지 확인
3. Jenkins URL이 public IP인지 확인
4. EC2 Security Group 8080 포트 확인

### SSH 연결 실패 시
```bash
# Jenkins 서버에서 키 권한 확인
sudo su - jenkins
ls -l ~/.ssh/deploy-key  # -rw------- 확인

# AP 서버에서 authorized_keys 권한 확인
ssh ubuntu@{AP_SERVER_IP}
ls -l ~/.ssh/authorized_keys  # -rw------- 확인

# 상세 로그로 연결 시도
ssh -vvv -i ~/.ssh/deploy-key ubuntu@{AP_SERVER_IP}
```

### Permission Denied 에러
```bash
# Jenkins 사용자 권한 확인
sudo chown -R jenkins:jenkins /var/lib/jenkins
```

---

## 📚 Jenkinsfile 구조

통합 Jenkinsfile (`jenkinsfile/Jenkinsfile`)의 주요 스테이지:

```
1. 변경 감지
   ↓ Git diff로 변경된 서비스 파악
2. 환경 확인
   ↓ Java, Gradle, Node.js 버전 체크
3. Core 모듈 빌드
   ↓ centrally-system-core 우선 빌드
4. 프론트엔드 빌드
   ↓ Vue + Vite → dist.tar.gz (변경 시에만)
5. 병렬 빌드
   ↓ 변경된 서비스만 빌드 (2개씩 병렬)
6. 백엔드 배포
   ↓ JAR → AP 서버 전송 + 재시작
7. 프론트엔드 배포
   ↓ dist → Nginx 배포 (변경 시에만)
```

---

## 💡 운영 팁

### 빌드 환경 전환
```groovy
// Jenkinsfile 14번 라인
BUILD_ENV = 'prod'  // 프로덕션
BUILD_ENV = 'dev'   // 개발
```

### 특정 서비스만 재배포
```bash
# AP 서버에서 직접 실행
bash /was/dummy_app/deploy-service.sh auth
bash /was/dummy_app/deploy-service.sh gateway
```

### 롤백
```bash
# AP 서버에서
# 1. 이전 JAR로 복구
cp /var/lib/jenkins/artifacts/auth/auth-build-이전번호-타임스탬프.jar /was/dummy_app/auth.jar

# 2. 재시작
bash /was/dummy_app/deploy-service.sh auth
```

### 로그 모니터링
```bash
# AP 서버에서 실시간 로그
tail -f /was/dummy_app/logs/auth.log
tail -f /was/dummy_app/logs/gateway.log

# 에러만 필터링
tail -f /was/dummy_app/logs/auth.log | grep ERROR
```

---

**마지막 업데이트**: 2025-01-XX  
**버전**: 3.0 (통합 파이프라인 기준으로 정리)
