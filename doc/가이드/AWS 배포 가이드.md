# AWS 배포 가이드 - CentrallySystem

## 📋 목차

1. [배포 개요](#배포-개요)
2. [비용 예상](#비용-예상)
3. [사전 준비](#사전-준비)
4. [1단계: AWS 콘솔 로그인](#1단계-aws-콘솔-로그인)
5. [2단계: 보안 그룹 생성](#2단계-보안-그룹-생성)
6. [3단계: EC2 인스턴스 생성](#3단계-ec2-인스턴스-생성)
7. [4단계: Elastic IP 할당](#4단계-elastic-ip-할당)
8. [5단계: SSH 접속](#5단계-ssh-접속)
9. [6단계: Java 17 설치](#6단계-java-17-설치)
10. [7단계: PostgreSQL 설치 및 DB 생성](#7단계-postgresql-설치-및-db-생성)
11. [8단계: Kafka 설치](#8단계-kafka-설치)
12. [9단계: Nginx 설치](#9단계-nginx-설치)
13. [10단계: 애플리케이션 빌드](#10단계-애플리케이션-빌드)
14. [11단계: EC2에 업로드](#11단계-ec2에-업로드)
15. [12단계: 설정 파일 생성](#12단계-설정-파일-생성)
16. [13단계: systemd 서비스 등록](#13단계-systemd-서비스-등록)
17. [14단계: 서비스 시작](#14단계-서비스-시작)
18. [15단계: 테스트](#15단계-테스트)
19. [자동 중지/시작 설정](#자동-중지시작-설정)
20. [문제 해결](#문제-해결)

---

## 배포 개요

### 배포 아키텍처

```
EC2 t3.medium (1대)
├── Java 17
├── Gateway (8080)
├── Eureka (8761)
├── Auth (9001)
├── HRM (9999)
├── Receipt (9899)
├── Info (9699)
├── PostgreSQL (5432)
│   ├── centauth
│   ├── centhrm
│   ├── centrcpt
│   ├── centinfo
│   └── centgate
├── Kafka (9092)
└── Nginx (80/443)
    └── Vue 프론트엔드
```

### 특징

- 소스코드 수정 없음
- properties 파일만 수정
- 1대 EC2에 모든 서비스 설치
- 필요할 때만 실행 가능

---

## 비용 예상

### 인스턴스 사양 선택

| 인스턴스 | vCPU | RAM | 시간당 | 월 (24시간) | 월 (주5일 8시간) | 권장 |
|---------|------|-----|--------|------------|----------------|------|
| t3.small | 2 | 2GB | $0.0208 | $14.98 | $3.66 | ⚠️ 부족 |
| t3.medium | 2 | 4GB | $0.0416 | $29.95 | $7.32 | ✅ 권장 |
| t3.large | 2 | 8GB | $0.0832 | $59.90 | $14.65 | 여유 |

### 총 비용 (t3.medium 기준)

#### 24시간 운영

```
EC2 t3.medium: $29.95/월
EBS 20GB: $1.60/월
네트워크: $0.10/월
총: $31.65/월
```

#### 주 5일, 8시간 운영 (권장)

```
EC2 t3.medium: $7.32/월
EBS 20GB: $1.60/월
네트워크: $0.10/월
총: $9.02/월

크레딧 $200으로: 약 22개월 사용 가능
```

---

## 사전 준비

### 로컬 환경 준비

- [ ] Git Bash 또는 PowerShell
- [ ] SSH 클라이언트 (Windows: PuTTY 또는 기본 SSH)
- [ ] 프로젝트 빌드 가능한 환경

### AWS 계정 준비

- [ ] AWS 계정 생성 완료
- [ ] 결제 정보 등록 완료
- [ ] 크레딧 확인 (있으면)

---

## 1단계: AWS 콘솔 로그인

### 접속

1. https://console.aws.amazon.com 접속
2. 이메일/비밀번호 입력
3. 로그인 완료

### 리전 선택

1. 우측 상단 리전 선택 드롭다운 클릭
2. **서울 (ap-northeast-2)** 선택
   - 한국에서 가장 빠름
   - 네트워크 지연 최소화

---

## 2단계: 보안 그룹 생성

### EC2 콘솔 접속

1. 상단 검색창에 "EC2" 입력
2. "EC2" 클릭
3. 좌측 메뉴 → "네트워크 및 보안" → "보안 그룹"
4. **"보안 그룹 생성"** 클릭

### 기본 세부 정보

```
보안 그룹 이름: centrally-sg
설명: CentrallySystem 보안 그룹
VPC: 기본 VPC 선택
```

### 인바운드 규칙 추가

**"인바운드 규칙 추가" 클릭 후 다음 규칙 추가:**

| 유형 | 프로토콜 | 포트 범위 | 소스 | 설명 |
|------|----------|----------|------|------|
| SSH | TCP | 22 | 내 IP | SSH 접속 |
| HTTP | TCP | 80 | 0.0.0.0/0 | Nginx HTTP |
| HTTPS | TCP | 443 | 0.0.0.0/0 | Nginx HTTPS (선택) |
| 사용자 지정 TCP | TCP | 8080 | 0.0.0.0/0 | Gateway |

### 아웃바운드 규칙

- 기본값 유지 (모든 트래픽 허용)

### 생성

- **"보안 그룹 생성"** 클릭

---

## 3단계: EC2 인스턴스 생성

### 인스턴스 시작

1. EC2 콘솔 → "인스턴스" 메뉴
2. **"인스턴스 시작"** 클릭

### 이름 및 태그

```
이름: CentrallySystem

태그 (선택):
- 키: Project, 값: CentrallySystem
- 키: Environment, 값: Production
```

### AMI 선택 (운영체제)

**권장: Amazon Linux 2023**

- 최신 보안 패치
- AWS 최적화
- 무료
- Java 17 설치 간편

선택:
- "Amazon Linux 2023 AMI" 선택
- 아키텍처: 64비트 (x86)

### 인스턴스 유형 선택

**권장: t3.medium**

```
vCPU: 2개
메모리: 4GB
비용: $0.0416/시간 = $29.95/월 (24시간)
```

검색창에 "t3.medium" 입력 후 선택

### 키 페어 생성

1. **"새 키 페어 생성"** 클릭
2. 설정:
   ```
   키 페어 이름: dummy-key
   키 페어 유형: RSA
   프라이빗 키 파일 형식: 
   - .pem (Mac/Linux, PowerShell, MobaXterm 변환 가능)
   - .ppk (Windows/PuTTY, MobaXterm 직접 사용 가능)
   ```
3. **"키 페어 생성"** 클릭
4. 키 파일 다운로드:
   - `.pem` 선택 시: `dummy-key.pem` 다운로드
   - `.ppk` 선택 시: `dummy-key.ppk` 다운로드 (MobaXterm에서 바로 사용 가능)
5. **안전한 곳에 보관** (이 파일로만 SSH 접속 가능)

**참고:**
- MobaXterm 사용자는 `.ppk` 형식을 선택하면 변환 없이 바로 사용 가능
- `.pem` 형식을 다운로드했다면 MobaXterm에서 `.ppk`로 변환 가능 (5단계 참조)

### 네트워크 설정

```
VPC: 기본 VPC
서브넷: 기본 (ap-northeast-2a 등)
퍼블릭 IP 자동 할당: 활성화
방화벽 (보안 그룹): 기존 보안 그룹 선택
  → centrally-sg 선택
```

### 스토리지 구성

```
볼륨 1 (루트 볼륨):
- 크기: 20GB (권장, 프로젝트 4GB + 여유)
- 볼륨 유형: gp3 (권장, 저렴하고 빠름)
- IOPS: 3000 (기본)
- 처리량: 125 MB/s (기본)
- 종료 시 삭제: 체크 (EC2 삭제 시 EBS도 삭제)
```

### 고급 세부 정보 (선택)

```
종료 방지 활성화: 체크 (실수로 삭제 방지)
```

### 요약 확인

1. 우측 요약 패널에서 설정 확인
2. **"인스턴스 시작"** 클릭
3. 인스턴스 생성 중... (2-3분 대기)
4. "인스턴스가 시작 중입니다" 메시지 확인
5. **"인스턴스 보기"** 클릭

### 인스턴스 상태 확인

```
인스턴스 상태: running
상태 검사: 2/2 검사 통과 (5분 정도 소요)
```

---

## 4단계: Elastic IP 할당

### Elastic IP 생성

1. EC2 콘솔 → 좌측 메뉴 → "네트워크 및 보안" → **"Elastic IP"**
2. **"Elastic IP 주소 할당"** 클릭
3. 설정:
   ```
   네트워크 경계 그룹: ap-northeast-2
   퍼블릭 IPv4 주소 풀: Amazon의 IPv4 주소 풀
   ```
4. **"할당"** 클릭
5. Elastic IP 할당 완료 (예: 52.79.xxx.xxx)

### EC2에 연결

1. 할당된 Elastic IP 선택 (체크박스)
2. **"작업"** → **"Elastic IP 주소 연결"**
3. 설정:
   ```
   리소스 유형: 인스턴스
   인스턴스: CentrallySystem 선택
   프라이빗 IP 주소: 자동 선택
   ```
4. **"연결"** 클릭
5. 연결 완료

**중요:** 이제 이 Elastic IP로 접속합니다 (변하지 않는 고정 IP)

---

## 5단계: SSH 접속

### Windows (PowerShell)

#### 키 파일 권한 설정 (한 번만)

```powershell
# PowerShell 관리자 권한으로 실행
cd C:\Users\%USERNAME%\Downloads

# 권한 설정
icacls dummy-key.pem /inheritance:r
icacls dummy-key.pem /grant:r "%USERNAME%:R"
```

#### SSH 접속

```powershell
# Amazon Linux 2023
ssh -i dummy-key.pem ec2-user@YOUR_EC2_PUBLIC_IP

# Ubuntu
ssh -i dummy-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
```

**예시:**
```powershell
# Amazon Linux 2023
ssh -i dummy-key.pem ec2-user@52.79.xxx.xxx

# Ubuntu
ssh -i dummy-key.pem ubuntu@52.79.xxx.xxx
```

**접속 확인:**
```
The authenticity of host '52.79.xxx.xxx' can't be established.
...
Are you sure you want to continue connecting (yes/no)? yes
```
→ `yes` 입력

**접속 성공:**
```
[ec2-user@ip-172-31-xxx-xxx ~]$
```

### Windows (MobaXterm) - 권장

MobaXterm은 Windows에서 사용하기 편한 SSH 클라이언트입니다. 파일 전송, 터미널 등이 통합되어 있습니다.

#### MobaXterm 설치

1. https://mobaxterm.mobatek.net/download.html 접속
2. **Home Edition (Free)** 다운로드
3. 설치 실행

#### 키 파일 준비

**케이스 1: .ppk 파일을 다운로드한 경우**

- AWS 콘솔에서 `.ppk` 형식으로 다운로드했다면 변환 없이 바로 사용 가능
- 다음 단계로 진행

**케이스 2: .pem 파일을 다운로드한 경우**

MobaXterm은 PuTTY 형식(.ppk)을 사용하므로 변환이 필요합니다.

**방법 1: MobaXterm 내장 변환 도구 사용 (권장)**

1. MobaXterm 실행
2. 좌측 상단 **"Tools"** → **"MobaKeyGen (SSH key generator)"** 클릭
3. **"Conversions"** → **"Import key"** 클릭
4. `dummy-key.pem` 파일 선택
5. **"Save private key"** 클릭
6. `dummy-key.ppk` 파일로 저장

**방법 2: PuTTYgen 사용**

1. PuTTYgen 다운로드 (https://www.putty.org/)
2. PuTTYgen 실행
3. **"Conversions"** → **"Import key"** 클릭
4. `dummy-key.pem` 파일 선택
5. **"Save private key"** 클릭
6. `dummy-key.ppk` 파일로 저장

#### SSH 세션 생성

1. MobaXterm 실행
2. 좌측 상단 **"Session"** 클릭
3. **"SSH"** 선택
4. 설정 입력:
   ```
   Remote host: YOUR_EC2_PUBLIC_IP
   (예: 52.79.xxx.xxx)
   
   Username: 
   - Amazon Linux 2023: ec2-user
   - Ubuntu: ubuntu
   
   Port: 22
   
   Advanced SSH settings:
   - Use private key: 체크
   - 클릭하여 dummy-key.ppk 파일 선택
   ```
5. **"OK"** 클릭

#### SSH 접속

1. 생성된 세션 더블클릭
2. 첫 접속 시 "Are you sure you want to continue connecting?" → **"Yes"** 클릭
3. 접속 성공

**접속 성공 화면:**
```
[ec2-user@ip-172-31-xxx-xxx ~]$
```

#### MobaXterm 장점

- **파일 전송 (SFTP)**: 좌측 파일 탐색기에서 드래그 앤 드롭으로 파일 업로드/다운로드
- **다중 세션**: 여러 서버 동시 접속
- **터미널 탭**: 여러 탭으로 작업
- **X11 포워딩**: GUI 애플리케이션 실행 가능

#### 파일 업로드 (MobaXterm)

1. 좌측 파일 탐색기에서 로컬 파일 확인
2. 우측 터미널에서 `cd ~/app` 등으로 이동
3. 좌측에서 파일을 우측으로 드래그 앤 드롭
4. 업로드 완료

#### 파일 다운로드 (MobaXterm)

1. 우측 터미널에서 파일 확인
2. 우측 파일 탐색기에서 파일을 좌측으로 드래그 앤 드롭
3. 다운로드 완료

### Mac/Linux

#### 키 파일 권한 설정

```bash
chmod 400 dummy-key.pem
```

#### SSH 접속

```bash
# Amazon Linux 2023
ssh -i dummy-key.pem ec2-user@YOUR_EC2_PUBLIC_IP

# Ubuntu
ssh -i dummy-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
```

---

## 6단계: Java 17 설치

### Amazon Linux 2023

```bash
# 시스템 업데이트
sudo yum update -y

# Java 17 설치
sudo yum install java-17-amazon-corretto-devel -y

# 설치 확인
java -version
```

**출력 확인:**
```
openjdk version "17.0.x" 2023-xx-xx LTS
OpenJDK Runtime Environment Corretto-17.0.x (build 17.0.x+x-LTS)
OpenJDK 64-Bit Server VM Corretto-17.0.x (build 17.0.x+x-LTS, mixed mode, sharing)
```

### Ubuntu (선택)

```bash
# 시스템 업데이트
sudo apt update -y

# Java 17 설치
sudo apt install openjdk-17-jdk -y

# 설치 확인
java -version
```

---

## 7단계: PostgreSQL 설치 및 DB 생성

### PostgreSQL 설치 (우분투)

```bash
# PostgreSQL 설치
sudo apt install postgresql -y

# PostgreSQL 시작
sudo systemctl start postgresql
sudo systemctl enable postgresql

# 상태 확인
sudo systemctl status postgresql
```

**출력 확인:**
```
● postgresql.service - PostgreSQL database server
   Loaded: loaded (/usr/lib/systemd/system/postgresql.service; enabled)
   Active: active (running)
```

### PostgreSQL 비밀번호 설정

```bash
# postgres 사용자로 전환
sudo -i -u postgres

# psql 접속
psql

# 비밀번호 설정 (YOUR-PASSWORD를 원하는 비밀번호로 변경)
ALTER USER postgres WITH PASSWORD 'dummy_password_123';
```

**출력:**
```
ALTER ROLE
```

### 데이터베이스 생성

```sql
-- 5개 데이터베이스 생성
CREATE DATABASE centauth;
CREATE DATABASE centhrm;
CREATE DATABASE centrcpt;
CREATE DATABASE centinfo;
CREATE DATABASE centgate;

-- 확인
\l
```

**출력 확인:**
```
                              List of databases
   Name    |  Owner   | Encoding | Collate | Ctype |   Access privileges
-----------+----------+----------+---------+-------+-----------------------
 centauth  | postgres | UTF8     | ...     | ...   |
 centgate  | postgres | UTF8     | ...     | ...   |
 centhrm   | postgres | UTF8     | ...     | ...   |
 centinfo  | postgres | UTF8     | ...     | ...   |
 centrcpt  | postgres | UTF8     | ...     | ...   |
```

### 종료

```sql
\q
exit
```

**중요:** `exit` 명령으로 postgres 사용자에서 나와서 **ec2-user**로 돌아와야 합니다.

### 외부 접속 허용 (로컬에서 접속하려면)

**주의:** `ec2-user` 또는 `ubuntu`로 실행해야 합니다. `postgres` 사용자로 전환한 상태에서는 `sudo`가 작동하지 않습니다.

**PostgreSQL 데이터 디렉토리 위치 확인:**

```bash
# 데이터 디렉토리 위치 확인
sudo -u postgres psql -c "SHOW data_directory;"
```

**경로별 설정 파일 위치:**

- **Amazon Linux 2023:** `/var/lib/pgsql/data/`
- **Ubuntu:** 
  - 설정 파일: `/etc/postgresql/16/main/` (버전에 따라 다를 수 있음)
  - 데이터 파일: `/var/lib/postgresql/16/main/` (데이터만 저장)

```bash
# ec2-user 또는 ubuntu 상태인지 확인 (프롬프트 확인)
# [ec2-user@...]$ 또는 [ubuntu@...]$ ← 이렇게 되어 있어야 함
# [postgres@...]$ ← 이렇게 되어 있으면 exit 입력

# PostgreSQL 설정 파일 수정
# Amazon Linux 2023
sudo vi /var/lib/pgsql/data/postgresql.conf

# Ubuntu (설정 파일은 /etc에 있음)
sudo vi /etc/postgresql/16/main/postgresql.conf

# 찾기 (vi에서 /listen_addresses 입력)
# 수정:
listen_addresses = '*'

# 저장: ESC → :wq → Enter

# 인증 설정
# Amazon Linux 2023
sudo vi /var/lib/pgsql/data/pg_hba.conf

# Ubuntu (설정 파일은 /etc에 있음)
sudo vi /etc/postgresql/16/main/pg_hba.conf

# 맨 아래 추가:
host    all             all             0.0.0.0/0               md5

# 저장 후 재시작
sudo systemctl restart postgresql
```

---

## 8단계: Kafka 설치

### Kafka 다운로드 및 설치

```bash
# 홈 디렉토리로 이동
cd ~

# Kafka 다운로드
wget https://downloads.apache.org/kafka/3.8.1/kafka_2.13-3.8.1.tgz

# 압축 해제
tar -xzf kafka_2.13-3.8.1.tgz

# 디렉토리 이름 변경
mv kafka_2.13-3.8.1 kafka

# Kafka 디렉토리로 이동
cd kafka
```

### Kafka 설정 (KRaft 모드)

```bash
# 클러스터 ID 생성
KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"

# 로그 디렉토리 포맷
bin/kafka-storage.sh format -t $KAFKA_CLUSTER_ID -c config/kraft/server.properties

# Kafka 시작 (백그라운드)
bin/kafka-server-start.sh -daemon config/kraft/server.properties

# 실행 확인 (5초 후)
jps
```

**출력 확인:**
```
xxxxx Kafka
```

### Kafka systemd 서비스 등록

```bash
# 서비스 파일 생성
sudo vi /etc/systemd/system/kafka.service
```

**파일 내용:**
```ini
[Unit]
Description=Apache Kafka Server
After=network.target

[Service]
Type=simple
User=ec2-user
Environment="KAFKA_HEAP_OPTS=-Xmx512m -Xms256m"
ExecStart=/home/ec2-user/kafka/bin/kafka-server-start.sh /home/ec2-user/kafka/config/kraft/server.properties
ExecStop=/home/ec2-user/kafka/bin/kafka-server-stop.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**저장 후 (ESC → :wq → Enter):**

```bash
# systemd 리로드
sudo systemctl daemon-reload

# Kafka 시작
sudo systemctl start kafka

# 자동 시작 설정
sudo systemctl enable kafka

# 상태 확인
sudo systemctl status kafka
```

---

## 9단계: Nginx 설치

### Nginx 설치 (Amazon Linux 2023)

```bash
# Nginx 설치
sudo yum install nginx -y

# Nginx 시작
sudo systemctl start nginx
sudo systemctl enable nginx

# 상태 확인
sudo systemctl status nginx
```

**출력 확인:**
```
● nginx.service - The nginx HTTP and reverse proxy server
   Active: active (running)
```

### Nginx 설정

```bash
# 기본 설정 백업
sudo mv /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak

# 새 설정 파일 생성
sudo vi /etc/nginx/nginx.conf
```

**파일 내용:**
```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log;
pid /run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    keepalive_timeout 65;
    types_hash_max_size 4096;

    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # CentrallySystem 설정
    server {
        listen 80;
        server_name YOUR_EC2_PUBLIC_IP;
        
        client_max_body_size 20M;
        
        # Vue 프론트엔드 (정적 파일)
        location / {
            root /var/www/dummy_app/dist;
            try_files $uri $uri/ /index.html;
        }
        
        # Gateway API
        location /api/ {
            proxy_pass http://localhost:8080/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_buffering off;
        }
        
        # 프로필 이미지
        location /api/hrm/upload/profile/ {
            alias /home/ec2-user/upload/img/profile/;
        }
        
        # 영수증 이미지
        location /api/receipt/upload/receipt/ {
            alias /home/ec2-user/upload/img/receipt/;
        }
    }
}
```

**저장 후:**
```bash
# 설정 테스트
sudo nginx -t

# 재시작
sudo systemctl restart nginx
```

---

## 10단계: 애플리케이션 빌드

### 로컬 PC에서 빌드 (Windows PowerShell)

```powershell
# 프로젝트 디렉토리로 이동
cd C:\path\to\dummy_project

# Gateway 빌드
cd centrally-system-gateway
.\gradlew.bat clean bootJar

# Eureka 빌드
cd ..\centrally-system-eureka
.\gradlew.bat clean bootJar

# Auth 빌드
cd ..\centrally-system-auth
.\gradlew.bat clean bootJar

# HRM 빌드
cd ..\centrally-system-hrm
.\gradlew.bat clean bootJar

# Receipt 빌드
cd ..\centrally-system-receipt
.\gradlew.bat clean bootJar

# Info 빌드
cd ..\centrally-system-info
.\gradlew.bat clean bootJar

# Vue 프론트엔드 빌드
cd ..\centrally-web-vue3-vite
npm run build
```

**빌드 완료 확인:**
```
각 서비스의 build/libs/*.jar 파일 생성
centrally-web-vue3-vite/dist 폴더 생성
```

---

## 11단계: EC2에 업로드

### EC2 디렉토리 생성 (SSH 접속 상태에서)

```bash
# 애플리케이션 디렉토리 생성
mkdir -p ~/app
mkdir -p ~/logs
mkdir -p ~/config
mkdir -p ~/upload/img/profile
mkdir -p ~/upload/img/receipt
mkdir -p ~/upload/img/notice

# Vue 디렉토리 생성
sudo mkdir -p /var/www/dummy_app

# 권한 설정
sudo chown -R ec2-user:ec2-user ~/upload
```

### 로컬에서 파일 업로드

#### 방법 1: PowerShell (scp 명령어)

```powershell
# JAR 파일 업로드
scp -i dummy-key.pem `
  centrally-system-gateway\build\libs\centrally-system-gateway-0.0.1-SNAPSHOT.jar `
  ec2-user@YOUR_EC2_PUBLIC_IP:/home/ec2-user/app/gateway.jar

scp -i dummy-key.pem `
  centrally-system-eureka\build\libs\centrally-system-eureka-0.0.1-SNAPSHOT.jar `
  ec2-user@dummy_ip:/home/ec2-user/app/eureka.jar

scp -i dummy-key.pem `
  centrally-system-auth\build\libs\centrally-system-auth-0.0.1-SNAPSHOT.jar `
  ec2-user@dummy_ip:/home/ec2-user/app/auth.jar

scp -i dummy-key.pem `
  centrally-system-hrm\build\libs\centrally-system-hrm-0.0.1-SNAPSHOT.jar `
  ec2-user@dummy_ip:/home/ec2-user/app/hrm.jar

scp -i dummy-key.pem `
  centrally-system-receipt\build\libs\centrally-system-receipt-0.0.1-SNAPSHOT.jar `
  ec2-user@dummy_ip:/home/ec2-user/app/receipt.jar

scp -i dummy-key.pem `
  centrally-system-info\build\libs\centrally-system-info-0.0.1-SNAPSHOT.jar `
  ec2-user@dummy_ip:/home/ec2-user/app/info.jar

# Vue 빌드 파일 업로드
scp -i dummy-key.pem -r `
  centrally-web-vue3-vite\dist\* `
  ec2-user@dummy_ip:/tmp/

# EC2에서 이동 (SSH 창에서)
sudo mv /tmp/* /var/www/dummy_app/dist/
sudo chown -R nginx:nginx /var/www/dummy_app
```

#### 방법 2: MobaXterm (드래그 앤 드롭) - 권장

MobaXterm을 사용하면 파일 업로드가 매우 간편합니다.

**JAR 파일 업로드:**

1. MobaXterm에서 EC2 세션 접속
2. 우측 터미널에서 업로드할 디렉토리로 이동:
   ```bash
   cd ~/app
   ```
3. 좌측 파일 탐색기에서 로컬 파일 찾기:
   - `centrally-system-gateway\build\libs\centrally-system-gateway-0.0.1-SNAPSHOT.jar`
   - `centrally-system-eureka\build\libs\centrally-system-eureka-0.0.1-SNAPSHOT.jar`
   - `centrally-system-auth\build\libs\centrally-system-auth-0.0.1-SNAPSHOT.jar`
   - `centrally-system-hrm\build\libs\centrally-system-hrm-0.0.1-SNAPSHOT.jar`
   - `centrally-system-receipt\build\libs\centrally-system-receipt-0.0.1-SNAPSHOT.jar`
   - `centrally-system-info\build\libs\centrally-system-info-0.0.1-SNAPSHOT.jar`
4. 각 파일을 우측 `~/app` 디렉토리로 **드래그 앤 드롭**
5. 파일명 변경 (우측 터미널에서):
   ```bash
   cd ~/app
   mv centrally-system-gateway-0.0.1-SNAPSHOT.jar gateway.jar
   mv centrally-system-eureka-0.0.1-SNAPSHOT.jar eureka.jar
   mv centrally-system-auth-0.0.1-SNAPSHOT.jar auth.jar
   mv centrally-system-hrm-0.0.1-SNAPSHOT.jar hrm.jar
   mv centrally-system-receipt-0.0.1-SNAPSHOT.jar receipt.jar
   mv centrally-system-info-0.0.1-SNAPSHOT.jar info.jar
   ```

**Vue 빌드 파일 업로드:**

1. 우측 터미널에서:
   ```bash
   sudo mkdir -p /var/www/dummy_app/dist
   ```
2. 좌측 파일 탐색기에서 `centrally-web-vue3-vite\dist` 폴더의 **모든 파일** 선택
3. 우측 `/var/www/dummy_app/dist` 디렉토리로 **드래그 앤 드롭**
4. 권한 설정 (우측 터미널에서):
   ```bash
   sudo chown -R nginx:nginx /var/www/dummy_app
   ```

**MobaXterm 장점:**
- ✅ 드래그 앤 드롭으로 간편하게 업로드
- ✅ 진행 상황 표시
- ✅ 여러 파일 동시 업로드 가능
- ✅ 파일 탐색기로 디렉토리 구조 확인 가능

---

## 12단계: 설정 파일 생성

### application-aws.properties 생성

```bash
# EC2에서 (SSH 접속 상태)
vi ~/config/application-aws.properties
```

**파일 내용 (YOUR_EC2_PUBLIC_IP를 실제 IP로 변경):**
```properties
# ========== 공통 ==========
spring.profiles.active=aws
kafka.server.ip=localhost:9092

# ========== Gateway ==========
server.port=8080
eureka.server.ip=http://localhost:8761/eureka/
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/centgate
spring.r2dbc.username=postgres
spring.r2dbc.password=dummy_password_123

# ========== Eureka ==========
server.port=8761

# ========== Auth ==========
server.port=9001
server.servlet.context-path=/auth
spring.datasource.url=jdbc:postgresql://localhost:5432/centauth
spring.datasource.username=postgres
spring.datasource.password=dummy_password_123
spring.kafka.bootstrap-servers=localhost:9092
frontend.domain=http://YOUR_EC2_PUBLIC_IP

# ========== HRM ==========
server.port=9999
server.servlet.context-path=/hrm
spring.datasource.url=jdbc:postgresql://localhost:5432/centhrm
spring.datasource.username=postgres
spring.datasource.password=dummy_password_123
spring.kafka.bootstrap-servers=localhost:9092
file.upload.path=/home/ec2-user/upload/img
file.upload.url=http://YOUR_EC2_PUBLIC_IP/api/hrm/upload
hrm.default.profile.img=/img/profile/random/001~018.png

# ========== Receipt ==========
server.port=9899
server.servlet.context-path=/receipt
spring.datasource.url=jdbc:postgresql://localhost:5432/centrcpt
spring.datasource.username=postgres
spring.datasource.password=dummy_password_123
spring.kafka.bootstrap-servers=localhost:9092
file.upload.path=/home/ec2-user/upload/img
file.upload.url=http://YOUR_EC2_PUBLIC_IP/api/receipt/upload

# ========== Info ==========
server.port=9699
server.servlet.context-path=/info
spring.datasource.url=jdbc:postgresql://localhost:5432/centinfo
spring.datasource.username=postgres
spring.datasource.password=dummy_password_123
spring.kafka.bootstrap-servers=localhost:9092
```

**저장: ESC → :wq → Enter**

---

## 13단계: systemd 서비스 등록

### 1. Eureka 서비스

```bash
sudo vi /etc/systemd/system/eureka.service
```

**파일 내용:**
```ini
[Unit]
Description=Centrally Eureka Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/app
ExecStart=/usr/bin/java -jar \
  -Xmx512m -Xms256m \
  -Dspring.config.additional-location=file:/home/ec2-user/config/ \
  -Dspring.profiles.active=aws \
  /home/ec2-user/app/eureka.jar
Restart=always
RestartSec=10
StandardOutput=append:/home/ec2-user/logs/eureka.log
StandardError=append:/home/ec2-user/logs/eureka-error.log

[Install]
WantedBy=multi-user.target
```

### 2. Gateway 서비스

```bash
sudo vi /etc/systemd/system/gateway.service
```

**파일 내용:**
```ini
[Unit]
Description=Centrally Gateway Service
After=network.target eureka.service
Requires=eureka.service

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/app
ExecStart=/usr/bin/java -jar \
  -Xmx512m -Xms256m \
  -Dspring.config.additional-location=file:/home/ec2-user/config/ \
  -Dspring.profiles.active=aws \
  /home/ec2-user/app/gateway.jar
Restart=always
RestartSec=10
StandardOutput=append:/home/ec2-user/logs/gateway.log
StandardError=append:/home/ec2-user/logs/gateway-error.log

[Install]
WantedBy=multi-user.target
```

### 3. Auth 서비스

```bash
sudo vi /etc/systemd/system/auth.service
```

**파일 내용:**
```ini
[Unit]
Description=Centrally Auth Service
After=network.target eureka.service postgresql.service kafka.service
Requires=eureka.service postgresql.service

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/app
ExecStart=/usr/bin/java -jar \
  -Xmx512m -Xms256m \
  -Dspring.config.additional-location=file:/home/ec2-user/config/ \
  -Dspring.profiles.active=aws \
  /home/ec2-user/app/auth.jar
Restart=always
RestartSec=10
StandardOutput=append:/home/ec2-user/logs/auth.log
StandardError=append:/home/ec2-user/logs/auth-error.log

[Install]
WantedBy=multi-user.target
```

### 4. HRM 서비스

```bash
sudo vi /etc/systemd/system/hrm.service
```

**파일 내용:**
```ini
[Unit]
Description=Centrally HRM Service
After=network.target eureka.service postgresql.service kafka.service
Requires=eureka.service postgresql.service

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/app
ExecStart=/usr/bin/java -jar \
  -Xmx512m -Xms256m \
  -Dspring.config.additional-location=file:/home/ec2-user/config/ \
  -Dspring.profiles.active=aws \
  /home/ec2-user/app/hrm.jar
Restart=always
RestartSec=10
StandardOutput=append:/home/ec2-user/logs/hrm.log
StandardError=append:/home/ec2-user/logs/hrm-error.log

[Install]
WantedBy=multi-user.target
```

### 5. Receipt 서비스

```bash
sudo vi /etc/systemd/system/receipt.service
```

**파일 내용:** (HRM과 동일, JAR 파일명만 변경)
```ini
[Unit]
Description=Centrally Receipt Service
After=network.target eureka.service postgresql.service kafka.service
Requires=eureka.service postgresql.service

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/app
ExecStart=/usr/bin/java -jar \
  -Xmx512m -Xms256m \
  -Dspring.config.additional-location=file:/home/ec2-user/config/ \
  -Dspring.profiles.active=aws \
  /home/ec2-user/app/receipt.jar
Restart=always
RestartSec=10
StandardOutput=append:/home/ec2-user/logs/receipt.log
StandardError=append:/home/ec2-user/logs/receipt-error.log

[Install]
WantedBy=multi-user.target
```

### 6. Info 서비스

```bash
sudo vi /etc/systemd/system/info.service
```

**파일 내용:** (Receipt와 동일, JAR 파일명만 변경)

---

## 14단계: 서비스 시작

### systemd 리로드

```bash
sudo systemctl daemon-reload
```

### 서비스 시작 (순서대로)

```bash
# 1. Eureka 먼저 시작 (서비스 디스커버리)
sudo systemctl start eureka
sleep 30

# 2. Gateway 시작
sudo systemctl start gateway
sleep 20

# 3. 마이크로서비스 시작
sudo systemctl start auth
sudo systemctl start hrm
sudo systemctl start receipt
sudo systemctl start info

# 4. 자동 시작 설정
sudo systemctl enable eureka gateway auth hrm receipt info
```

### 서비스 상태 확인

```bash
# 모든 서비스 상태 확인
sudo systemctl status eureka
sudo systemctl status gateway
sudo systemctl status auth
sudo systemctl status hrm
sudo systemctl status receipt
sudo systemctl status info
sudo systemctl status kafka
sudo systemctl status postgresql
sudo systemctl status nginx
```

**출력 확인 (각각):**
```
● eureka.service - Centrally Eureka Service
   Active: active (running)
```

---

## 15단계: 테스트

### 로그 확인

```bash
# 실시간 로그 확인
sudo journalctl -u gateway -f

# 또는 파일 로그
tail -f ~/logs/gateway.log
tail -f ~/logs/auth.log
tail -f ~/logs/hrm.log

# 에러 로그
tail -f ~/logs/gateway-error.log
```

### API 테스트 (EC2 내부)

```bash
# Gateway 확인
curl http://localhost:8080/actuator/health

# Eureka 확인
curl http://localhost:8761

# Auth 확인
curl http://localhost:9001/auth/public-key
```

### 웹 브라우저에서 테스트

```
프론트엔드:
http://dummy_ip

Eureka 대시보드:
http://dummy_ip:8761

API:
http://dummy_ip/api/auth/public-key
```

---

## 자동 중지/시작 설정

### Lambda 함수 생성

#### 1. Lambda 콘솔 접속

```
1. AWS 콘솔 검색창에 "Lambda" 입력
2. "Lambda" 클릭
3. "함수 생성" 클릭
```

#### 2. 함수 생성

```
옵션: 새로 작성
함수 이름: EC2-Scheduler
런타임: Python 3.12
아키텍처: x86_64

"함수 생성" 클릭
```

#### 3. 함수 코드 작성

```python
import boto3
import os

ec2 = boto3.client('ec2', region_name='ap-northeast-2')
INSTANCE_ID = os.environ.get('INSTANCE_ID')

def lambda_handler(event, context):
    action = event.get('action')
    
    if action == 'start':
        ec2.start_instances(InstanceIds=[INSTANCE_ID])
        return {
            'statusCode': 200,
            'body': f'Started instance {INSTANCE_ID}'
        }
    elif action == 'stop':
        ec2.stop_instances(InstanceIds=[INSTANCE_ID])
        return {
            'statusCode': 200,
            'body': f'Stopped instance {INSTANCE_ID}'
        }
    else:
        return {
            'statusCode': 400,
            'body': 'Invalid action'
        }
```

#### 4. 환경 변수 설정

```
구성 → 환경 변수 → 편집
키: INSTANCE_ID
값: i-xxxxxxxxxxxxxxxxx (실제 인스턴스 ID)
```

#### 5. IAM 역할 권한 추가

```
구성 → 권한 → 실행 역할 클릭
정책 연결 → "AmazonEC2FullAccess" 검색 및 연결
```

### EventBridge 스케줄 생성

#### 시작 스케줄 (매일 오전 6시)

```
1. EventBridge 콘솔
2. "규칙" → "규칙 생성"
3. 이름: EC2-Start-6AM
4. 규칙 유형: 스케줄
5. Cron 표현식: 0 21 * * ? * (UTC, 한국시간 -9시간)
6. 대상: Lambda 함수
7. 함수: EC2-Scheduler
8. 입력 구성: 상수 (JSON 텍스트)
   {"action": "start"}
9. 생성
```

#### 중지 스케줄 (매일 자정)

```
1. 규칙 생성
2. 이름: EC2-Stop-Midnight
3. Cron 표현식: 0 15 * * ? * (UTC)
4. 대상: Lambda 함수
5. 입력: {"action": "stop"}
6. 생성
```

---

## 문제 해결

### 서비스가 시작되지 않을 때

```bash
# 로그 확인
sudo journalctl -u gateway -n 50

# 또는
cat ~/logs/gateway-error.log

# 수동 실행으로 에러 확인
cd ~/app
java -jar gateway.jar
```

### 메모리 부족 (Out of Memory)

```bash
# JVM 힙 메모리 줄이기
# systemd 서비스 파일 수정
sudo vi /etc/systemd/system/gateway.service

# -Xmx512m → -Xmx256m으로 변경
# 저장 후 재시작
sudo systemctl daemon-reload
sudo systemctl restart gateway
```

### PostgreSQL 연결 실패

```bash
# PostgreSQL 상태 확인
sudo systemctl status postgresql

# PostgreSQL 재시작
sudo systemctl restart postgresql

# 비밀번호 확인
sudo -i -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'dummy_password_123';"
```

### Kafka 연결 실패

```bash
# Kafka 상태 확인
sudo systemctl status kafka

# Kafka 재시작
sudo systemctl restart kafka

# Kafka 프로세스 확인
jps
```

### Nginx 502 에러

```bash
# Gateway 실행 확인
curl http://localhost:8080/actuator/health

# Nginx 로그 확인
sudo tail -f /var/log/nginx/error.log

# Nginx 재시작
sudo systemctl restart nginx
```

---

## 유용한 명령어 모음

### 모든 서비스 재시작

```bash
sudo systemctl restart eureka
sleep 30
sudo systemctl restart gateway auth hrm receipt info
```

### 모든 서비스 중지

```bash
sudo systemctl stop gateway auth hrm receipt info eureka
```

### 모든 서비스 상태 확인

```bash
sudo systemctl status eureka gateway auth hrm receipt info kafka postgresql nginx
```

### 로그 모니터링

```bash
# 실시간 로그 (여러 창에서)
sudo journalctl -u gateway -f
sudo journalctl -u auth -f
sudo journalctl -u hrm -f
```

### EC2 중지 (AWS 콘솔)

```
1. EC2 콘솔 → 인스턴스
2. CentrallySystem 선택
3. "인스턴스 상태" → "인스턴스 중지"
4. 확인
```

### EC2 시작 (AWS 콘솔)

```
1. EC2 콘솔 → 인스턴스
2. CentrallySystem 선택
3. "인스턴스 상태" → "인스턴스 시작"
4. 약 2분 후 정상 실행
```

---

## 배포 체크리스트

### 배포 전

- [ ] AWS 계정 생성 완료
- [ ] 로컬에서 빌드 테스트 완료
- [ ] properties 파일 준비 완료

### AWS 설정

- [ ] 리전 선택 (서울)
- [ ] 보안 그룹 생성
- [ ] EC2 인스턴스 생성 (t3.medium)
- [ ] Elastic IP 할당 및 연결
- [ ] 키 페어 다운로드 및 보관

### 소프트웨어 설치

- [ ] SSH 접속 성공
- [ ] Java 17 설치
- [ ] PostgreSQL 설치
- [ ] PostgreSQL DB 5개 생성
- [ ] Kafka 설치
- [ ] Nginx 설치

### 애플리케이션 배포

- [ ] 로컬에서 빌드 완료
- [ ] EC2에 JAR 파일 업로드
- [ ] EC2에 Vue 빌드 파일 업로드
- [ ] 설정 파일 생성
- [ ] systemd 서비스 6개 등록

### 실행 및 테스트

- [ ] Eureka 시작 확인
- [ ] Gateway 시작 확인
- [ ] Auth, HRM, Receipt, Info 시작 확인
- [ ] Kafka 실행 확인
- [ ] PostgreSQL 실행 확인
- [ ] Nginx 실행 확인
- [ ] 웹 브라우저에서 접속 테스트

---

## 비용 절감 팁

### 1. 필요할 때만 실행

```
Lambda + EventBridge로 자동 중지/시작
- 평일 09:00 시작
- 평일 18:00 중지
- 주말 중지

절감: 약 70%
```

### 2. 수동 중지

```
사용 후 항상 EC2 중지
- 컴퓨팅 비용: $0
- EBS만: $1.60/월

절감: 약 95%
```

### 3. 비용 알림 설정

```
AWS Billing → Budgets
- 월 예산: $20 설정
- 80% 도달 시 알림
- 100% 도달 시 알림
```

---

## 백업 및 복구

### EBS 스냅샷 생성

```
1. EC2 콘솔 → "Elastic Block Store" → "스냅샷"
2. "스냅샷 생성" 클릭
3. 볼륨 선택
4. 설명: CentrallySystem 백업 YYYY-MM-DD
5. "스냅샷 생성" 클릭
```

### 스냅샷에서 복원

```
1. 스냅샷 선택
2. "작업" → "볼륨 생성"
3. 새 EC2에 볼륨 연결
```

---

## 업데이트 배포

### 애플리케이션 업데이트

```bash
# 로컬에서 빌드
.\gradlew.bat clean bootJar

# EC2에 업로드 (덮어쓰기)
scp -i dummy-key.pem *.jar ec2-user@YOUR_EC2_PUBLIC_IP:/home/ec2-user/app/

# EC2에서 서비스 재시작
sudo systemctl restart gateway auth hrm receipt info
```

### Vue 프론트엔드 업데이트

```bash
# 로컬에서 빌드
npm run build

# EC2에 업로드
scp -i dummy-key.pem -r dist/* ec2-user@YOUR_EC2_PUBLIC_IP:/tmp/

# EC2에서 이동
sudo rm -rf /var/www/dummy_app/dist/*
sudo mv /tmp/* /var/www/dummy_app/dist/
sudo chown -R nginx:nginx /var/www/dummy_app
```

---

## 완료!

배포가 완료되었습니다.

**접속 URL:**
- 프론트엔드: http://YOUR_EC2_PUBLIC_IP
- API: http://YOUR_EC2_PUBLIC_IP/api
- Eureka: http://YOUR_EC2_PUBLIC_IP:8761

**관리:**
- EC2 중지: AWS 콘솔에서 클릭 한 번
- EC2 시작: AWS 콘솔에서 클릭 한 번
- 비용: 주 5일 사용 시 월 $9 정도

