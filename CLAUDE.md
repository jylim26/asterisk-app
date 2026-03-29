# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Asterisk 22 LTS 기반 소규모 콜센터 플랫폼. Docker 컨테이너로 운영.

### 현재 완료된 것

- Asterisk 22 Docker 이미지 빌드 및 실행 환경
- PJSIP 내선 2개 (1001, 1002) 등록 및 내선 간 통화
- ARI HTTP 인터페이스 활성화 (8088 포트)

### 다음 작업: ARI App 기본 구조

Spring Boot 프로젝트 생성, Asterisk ARI WebSocket 연결, 기본 통화 제어 API 구현.

## 아키텍처

```
SIP 클라이언트 (소프트폰)
    ↓ SIP (5060/UDP,TCP)
Asterisk 22 LTS (Docker, host 네트워크 모드)
    ├─ ARI (8088/TCP) → ARI App [다음 작업]
    └─ PostgreSQL (5432) [미구현]
```

### 설정 파일 템플릿 시스템

`config/*.conf` 파일은 템플릿. `entrypoint.sh`가 컨테이너 시작 시 `sed`로 환경변수(`${EXTERNAL_IP}`, `${SIP_*_PASSWORD}`, `${ARI_PASSWORD}` 등)를 치환하여 `/etc/asterisk/`에 복사함.

- 설정 원본: `./config/` → 컨테이너 내 `/etc/asterisk-templates` (read-only 마운트)
- 환경변수: `.env` 파일에서 로드 (git에서 제외됨)

### PJSIP 내선 구성

`config/pjsip.conf`에 내선 1001, 1002 정의. NAT: `rtp_symmetric=yes`, `force_rport=yes`, `direct_media=no`. 코덱: ulaw → alaw → opus → g722.

새 내선 추가 시: `.env`에 `SIP_XXXX_PASSWORD` 추가 → `pjsip.conf`에 endpoint/auth/aor 섹션 복제 → `entrypoint.sh`에 sed 치환 라인 추가 → `extensions.conf` 다이얼플랜에 라우팅 추가.

## 빌드 & 테스트 (Asterisk)

```bash
docker-compose build asterisk        # 이미지 빌드 (초회 ~10분)
docker-compose up -d                 # 컨테이너 시작
docker-compose down                  # 중지/제거
docker-compose restart asterisk      # config/*.conf 수정 후 재시작 (재빌드 불필요)

# 검증
docker exec -it asterisk asterisk -rx "core show version"       # 프로세스 상태
docker exec -it asterisk asterisk -rx "pjsip show endpoints"    # 내선 상태
docker exec -it asterisk asterisk -rx "pjsip show contacts"     # 내선 등록
curl -s -u asterisk:<ARI_PASSWORD> http://localhost:8088/ari/asterisk/info  # ARI 연결
```

## 빌드 & 테스트 (ARI App)

```bash
cd ari-app
./gradlew bootRun                    # 로컬 실행
./gradlew test                       # 전체 테스트
./gradlew test --tests "*CallServiceTest"  # 단일 테스트 클래스 실행
```

## 코딩 컨벤션 (ARI App — Spring Boot 3.5 LTS / Java 21)

### 프로젝트 구조

```
ari-app/
├── src/main/java/com/example/ari/
│   ├── global/                        # 앱 전역 설정 및 공통
│   │   ├── config/                    #   WebClient, CORS, Security 등
│   │   ├── error/                     #   글로벌 예외 처리 (GlobalExceptionHandler)
│   │   ├── event/                     #   공통 이벤트 클래스 (infra↔domain 간 매개)
│   │   └── util/                      #   공통 유틸리티
│   ├── infra/                         # 외부 시스템 연동 인프라
│   │   ├── ari/                       #   ARI 클라이언트 (WebSocket/REST)
│   │   │   ├── client/                #     연결 관리, REST 래퍼
│   │   │   ├── event/                 #     이벤트 디스패처, 핸들러 인터페이스
│   │   │   └── dto/                   #     ARI 프로토콜 매핑 객체
│   │   └── backend/                   #   기간계 API 클라이언트
│   │       └── dto/                   #     기간계 요청/응답 객체
│   ├── domain/                        # 핵심 도메인 (비즈니스 로직)
│   │   ├── call/                      #   콜 제어 (model/service/event/dto/exception)
│   │   ├── ivr/                       #   IVR (model/service/event/dto/exception)
│   │   ├── recording/                 #   녹취 (model/service/repository/dto/exception)
│   └── api/                           # API 표현 계층 (Controller)
│       ├── call/                      #   콜 제어 API
│       ├── ivr/                       #   IVR 관리 API
│       └── recording/                 #   녹취 조회/다운로드 API
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
├── Dockerfile
└── build.gradle.kts
```

### 레이어 규칙

- `api/` → `domain/`, `infra/` → `global/`, `domain/` → `global/` 방향으로만 의존
- `infra/`와 `domain/`은 서로 직접 참조하지 않음
- `domain/` 내 각 도메인(call, ivr, recording)은 자체 model/service/event/dto/exception을 가짐
- `global/`은 어디서든 참조 가능한 공통 모듈
- ARI 이벤트 전달: `infra/ari/event/`가 WebSocket 이벤트를 `global/event/` 이벤트 객체로 변환 후 `ApplicationEventPublisher`로 발행 → `domain/*/event/`가 `@EventListener`로 구독. infra↔domain 간 직접 의존 없음

### 네이밍

- 클래스: `PascalCase` — Controller/Service/Repository 접미사 명시 (예: `CallController`, `RecordingService`)
- 메서드: `camelCase` — 동사로 시작 (예: `originateCall`, `findByChannelId`)
- 상수: `UPPER_SNAKE_CASE`
- 패키지: 소문자, 단수형 (예: `controller`, `domain`)
- REST 경로: 소문자 kebab-case, 복수 명사 (예: `/api/v1/calls`, `/api/v1/recordings`)

### 코드 스타일

- Java 21 문법 사용: `record`, `sealed class`, 패턴 매칭. **`var` 사용 금지** — 항상 명시적 타입 선언
- **Setter 사용 금지** (전역) — 상태 변경은 의미 있는 도메인 메서드로 표현 (예: `call.hangUp()`)
- DTO는 `record`로 선언 (불변 보장)
- JPA 엔티티:
  - `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — JPA 전용, `public` 기본 생성자 금지
  - 필요한 인자만 받는 `private` 생성자 작성 (AllArgsConstructor 사용 안 함)
  - 외부 생성은 정적 팩토리 메서드 `create(...)` 통해서만 허용
  - 예: `Recording.create(channelId, filename)` → 내부에서 `private` 생성자 호출
- 생성자 주입 사용 (`@RequiredArgsConstructor`), 필드 주입(`@Autowired`) 금지
- 설정값은 `@ConfigurationProperties`로 바인딩, `@Value` 남용 금지
- 로깅: SLF4J (`@Slf4j`) 사용, 문자열 연결 대신 `{}` 플레이스홀더

### 예외 처리

- 비즈니스 예외는 해당 도메인의 `exception/` 패키지에 케이스별 클래스로 생성
- 모든 비즈니스 예외는 `RuntimeException`을 상속
- 네이밍: 상황을 설명하는 이름 (예: `ChannelNotFoundException`, `CallAlreadyHungUpException`)
- `global/error/GlobalExceptionHandler` (`@RestControllerAdvice`)에서 모든 도메인 예외를 캐치하여 일관된 응답 포맷으로 반환
- 컨트롤러에서 try-catch 금지

### ARI 연동

- WebSocket 연결로 Stasis 이벤트 수신, REST API로 채널 제어
- 연결 설정(호스트, 포트, 인증)은 `application.yml`의 커스텀 프로퍼티로 관리
- 재연결 로직 필수 — 네트워크 단절 시 자동 재접속

### 테스트

- 단위 테스트: JUnit 5 + Mockito, `*Test.java` 네이밍
- 통합 테스트: `@SpringBootTest` + Testcontainers(PostgreSQL), `*IntegrationTest.java` 네이밍
- 테스트 실행: `./gradlew test`
