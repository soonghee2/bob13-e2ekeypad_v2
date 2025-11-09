# E2E Keypad 학습 프로젝트
<img width="454" height="848" alt="스크린샷 2025-11-09 174427" src="https://github.com/user-attachments/assets/819777cc-fd70-45a2-8456-27890257946d" />

## 프로젝트 개요
- **목적**: 금융권 보안 키패드 입력 방식을 모사하며, 이미지 기반 무작위 키 배열과 전송 구간 암호화를 직접 구현해보는 학습용 프로젝트입니다.
- **구성**: Next.js 기반 입력 UI, Spring Boot 백엔드, 모의 은행 서버(복호화·검증)를 Docker Compose로 묶어 단일 명령으로 구동합니다.
- **성과**
  - 세션마다 키패드 이미지와 숫자→해시 매핑을 동적으로 생성
  - 사용자 입력 해시를 RSA 공개키로 암호화 후 백엔드에 제출
  - 백엔드는 세션 무결성을 확인한 뒤 모의 은행 서버에 안전하게 전달

## 시스템 구성
```
[Next.js 프런트] --(HTTPS/Fetch)--> [Spring Boot Keypad API] --(REST)--> [Bank Server]
         |                              |                                  |
         |-- 공개키 로드 & RSA 암호화 --|-- 세션 TTL 관리 & 난수 해시 생성 --|-- 복호화 후 입력 복구
```

| 구성 요소 | 역할 | 주요 기술 |
| --- | --- | --- |
| 프런트엔드 (`src/main/websrc`) | 키패드 이미지 렌더링, 난수 키 매핑, RSA 암호화, UX | Next.js 14, React 18, Tailwind CSS, Framer Motion, JSEncrypt |
| 백엔드 (`src/main/kotlin`) | 키패드 생성, 세션 저장/검증, 모의 은행 연동 | Kotlin 1.9, Spring Boot 3.2, Validation, RestTemplate |
| 모의 은행 (`bank-server`) | 암호문 복호화, 숫자 매핑 복원, 감사 로그 출력 | Kotlin, Spring Boot, BouncyCastle |

## 주요 기능 요약
- **키패드 이미지 합성**: 0~9와 공백 슬롯을 섞어 PNG 타일을 3x4 그리드로 합성 뒤 Base64로 전달.
- **난수 해시 맵**: `SecureRandom`을 통해 매 요청마다 160-bit 해시를 생성, 세션 스토어에 저장.
- **세션 무결성**: UUID + 해시 타임스탬프 조합과 TTL(기본 5분)을 통해 재사용 차단.
- **사용자 입력 암호화**: 프런트가 6자리 입력을 해시로 펼쳐 RSA 공개키로 암호화 후 전달.
- **은행 서버 시뮬레이션**: 복호화한 해시를 키-해시 맵과 비교해 실제 숫자를 복원, 감사 로그 제공.

## Docker Compose

```bash
docker compose up --build
```

- 기본 포트: 프런트 `3000`, 백엔드 `8080`, 은행 서버 `8081`
- 환경 변수로 포트 및 API URL을 조정할 수 있습니다. 예) `BACKEND_PORT=9090 docker compose up`.

## 로컬 개발 절차

### 1. 공통 준비
- JDK 21, Node.js 20 이상 설치
- 레포지토리 루트에서 `./gradlew` 및 `npm` 사용 가능 여부 확인

### 2. 모의 은행 서버 (선 실행 권장)
```bash
./gradlew :bank-server:bootRun
```
- `BANK_SERVER_PORT`(기본 8081) 환경 변수를 통해 포트 변경 가능

### 3. 백엔드 API
```bash
./gradlew bootRun \
  --args='--server.port=8080 --bank.server.url=http://localhost:8081'
```
- 노출 엔드포인트
  - `GET /api/combined-image`: 키패드 이미지(Base64)와 키 해시 배열, 세션 UUID/타임스탬프 반환
  - `POST /api/submit-hashes`: RSA 암호문, UUID, 타임스탬프로 검증 후 은행 서버에 전달

### 4. 프런트엔드
```bash
cd src/main/websrc
npm install
npm run dev -- --port 3000
```
- `.env.local` 또는 실행 시 `NEXT_PUBLIC_API_BASE_URL`을 지정해 백엔드 주소를 맞춰주세요.
- 공개키(`public/public.pem`)는 빌드 시 포함되며 필요 시 교체 가능합니다.

## 환경 변수 요약

| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `BACKEND_PORT` / `SERVER_PORT` | 8080 | 백엔드 HTTP 포트 |
| `BANK_SERVER_URL` | http://bank:8081 | 백엔드가 요청을 전달할 은행 서버 URL |
| `BANK_PORT` / `BANK_SERVER_PORT` | 8081 | 모의 은행 서버 포트 |
| `NEXT_PUBLIC_API_BASE_URL` | http://backend:8080 | 프런트에서 백엔드로 호출할 URL |
| `NEXT_PUBLIC_BANK_API_URL` | http://bank:8081 | (옵션) 은행 서버를 직접 노출해야 할 때 사용 |

## 폴더 구조 (요약)
```
.
├─ src/main/kotlin         # Spring Boot 애플리케이션
│  ├─ api/                 # REST 컨트롤러
│  ├─ service/             # 키패드 생성, 세션, 은행 연동 로직
│  └─ domain/dto/          # 요청/응답 DTO
├─ src/main/resources      # 키패드 PNG 타일
├─ src/main/websrc         # Next.js 프런트엔드
├─ bank-server             # 모의 은행(Spring Boot) 서브프로젝트
├─ Dockerfile.*            # 각 서비스 빌드 정의
└─ docker-compose.yml      # 전체 스택 오케스트레이션
```

## 학습 메모 & 개선 아이디어
- Redis 같은 외부 세션 스토리지를 연결해 다중 인스턴스 환경을 검증해보기
- 키패드 이미지 합성 과정을 WebP/AVIF 등으로 최적화해 응답 크기 축소
- 접근성 개선(스크린리더 대응, 키보드 네비게이션 지원)
- Cypress 등으로 E2E 테스트를 작성해 UX 회귀 검증

## 문의
- 궁금한 점이나 피드백은 GitHub Issues 또는 별도 연락 채널로 편히 전달해주세요.  
  (이력서/포트폴리오 링크는 요청 시 공유해드릴 수 있습니다.)

감사합니다. 프로젝트 전반을 친절하고 투명하게 설명드릴 수 있도록 계속 다듬어가겠습니다.
