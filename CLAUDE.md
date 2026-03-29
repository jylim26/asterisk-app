# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Asterisk 22 LTS 기반 소규모 콜센터 플랫폼. Docker 컨테이너로 운영.

### 현재 완료된 것

- Asterisk 22 Docker 이미지 + PJSIP 내선 2개 (1001, 1002)
- ARI WebSocket 연결 관리 (자동 재접속, 지수 백오프)
- ARI 이벤트 수신 및 도메인 이벤트 변환, ARI REST 클라이언트
- 콜 도메인: 수신/응답/종료 + 보류/블라인드 호전환/회의 호전환
- 글로벌 예외 처리 (ErrorResponse 표준 포맷)

### 다음 작업

- IVR 도메인 구현
- 녹취 도메인 구현
- PostgreSQL 연동

## 아키텍처

```
SIP 클라이언트 → Asterisk 22 (Docker, host 네트워크) → ARI (8088) → ARI App (Spring Boot 3.5.3 / Java 21)
                                                                    → PostgreSQL (5432) [미구현]
```

### 설정 파일 템플릿 시스템

`config/*.conf` → `entrypoint.sh`가 `sed`로 환경변수 치환 → `/etc/asterisk/`에 복사. 환경변수는 `.env`에서 로드.

### PJSIP 내선 구성

`config/pjsip.conf`에 내선 1001, 1002 정의. 새 내선 추가 시: `.env`에 비밀번호 → `pjsip.conf`에 endpoint/auth/aor → `entrypoint.sh`에 sed → `extensions.conf`에 라우팅.

## 도메인 모델

### Call (콜)

ARI 채널 1개의 생명주기를 관리하는 도메인 객체. `ConcurrentHashMap<channelId, Call>`로 인메모리 관리.

**상태 머신 (CallState):**
```
RINGING → ANSWERED ⇄ HELD → HUNGUP
              ↓                ↑
         CONSULTING ──→ TRANSFERRED
              ↓
           ANSWERED (cancel)
```
- `RINGING`: Stasis 진입 직후 (StasisStart 이벤트)
- `ANSWERED`: 응답됨. hold/transfer/hangup 가능
- `HELD`: 보류 중 (MOH 재생). unhold/transfer 가능
- `CONSULTING`: 회의 호전환 상담 중. complete/cancel 가능
- `TRANSFERRED`: 호전환 완료 (종료 상태)
- `HUNGUP`: 통화 종료 (종료 상태)

### Bridge (브릿지)

ARI Bridge — 2개 이상 채널의 오디오를 믹싱하는 컨테이너. 회의 호전환 시 사용. `type=mixing`.

### TransferSession (호전환 세션)

회의 호전환의 다단계 워크플로우를 추적하는 조정 객체. `call/domain/`에 위치.
- 상태: `CONSULTING` → `COMPLETED` / `CANCELLED`
- 필드: transferId, originalChannelId, targetChannelId, consultationBridgeId

### 이벤트 흐름

```
ARI WebSocket → AriWebSocketHandler → AriEventDispatcher (ARI DTO → 도메인 이벤트)
→ ApplicationEventPublisher → 각 도메인 @EventListener
```

## 빌드 & 테스트

```bash
# Asterisk
docker-compose build asterisk && docker-compose up -d
docker-compose restart asterisk      # config 수정 후 재시작 (재빌드 불필요)
docker exec -it asterisk asterisk -rx "pjsip show endpoints"

# ARI App
cd ari-app
./gradlew bootRun                    # 로컬 실행
./gradlew test                       # 전체 테스트
./gradlew test --tests "*CallServiceTest"  # 단일 클래스 실행

# 통합 테스트 UI: http://<서버IP>:8080/test.html
```

## 코딩 컨벤션 (ARI App — Spring Boot 3.5 LTS / Java 21)

### 프로젝트 구조

```
ari-app/
├── src/main/java/com/example/ari/
│   ├── global/                        # 앱 전역 설정 및 공통
│   │   ├── config/                    #   RestClient, WebSocket, Properties 등
│   │   ├── error/                     #   글로벌 예외 처리 (GlobalExceptionHandler)
│   │   └── event/                     #   공통 이벤트 클래스 (도메인 간 매개)
│   ├── infra/ari/                     # 공유 ARI 인프라 (WebSocket 연결, 이벤트 역직렬화)
│   │   ├── client/                    #   AriConnectionManager, AriWebSocketHandler
│   │   ├── event/                     #   AriEventDispatcher
│   │   └── dto/                       #   ARI 프로토콜 매핑 객체
│   ├── call/                          # 콜 제어 도메인
│   │   ├── domain/                    #   Call, CallState (도메인 모델)
│   │   ├── service/                   #   CallService (비즈니스 로직)
│   │   ├── infra/                     #   AriCallClient (ARI REST 채널 제어)
│   │   ├── event/                     #   CallEventListener
│   │   ├── exception/                 #   ChannelNotFoundException 등
│   │   ├── dto/                       #   CallResponse 등
│   │   └── api/                       #   CallController
│   ├── ivr/                           # IVR 도메인
│   │   ├── domain/                    #   IvrSession, IvrStep
│   │   ├── service/                   #   IvrService
│   │   ├── infra/                     #   AriIvrClient
│   │   ├── event/                     #   IvrEventListener
│   │   └── ...
│   └── recording/                     # 녹취 도메인 (추후)
│       └── ...
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
├── Dockerfile
└── build.gradle.kts
```

### 패키지 규칙

- **도메인 기준 패키지**: `call/`, `ivr/`, `recording/`이 각각 자체 domain·service·infra·api를 포함
- **도메인 간 직접 참조 금지** — 도메인 간 통신은 `global/event/`를 통해서만
- 각 도메인 내에서는 자유롭게 참조 가능 (예: `call/service/` → `call/infra/` 직접 사용 OK)
- `global/`, `infra/ari/`는 어디서든 참조 가능한 공유 모듈
- ARI 이벤트 전달: `infra/ari/event/AriEventDispatcher`가 WebSocket 이벤트를 `global/event/` 이벤트로 변환 후 `ApplicationEventPublisher`로 발행 → 각 도메인의 `event/`가 `@EventListener`로 구독

### 네이밍

- 클래스: `PascalCase` — Controller/Service/Repository 접미사 명시 (예: `CallController`, `RecordingService`)
- 메서드: `camelCase` — 동사로 시작 (예: `originateCall`, `findByChannelId`)
- 상수: `UPPER_SNAKE_CASE`
- 패키지: 소문자, 단수형 (예: `controller`, `domain`)
- REST 경로: 소문자 kebab-case, 복수 명사 (예: `/api/v1/calls`, `/api/v1/recordings`)

### 코드 스타일

- Java 21 문법 사용: `record`, `sealed class`, 패턴 매칭. **`var` 사용 금지** — 항상 명시적 타입 선언
- **Setter 사용 금지** (전역) — 상태 변경은 의미 있는 도메인 메서드로 표현 (예: `call.hangUp()`)
- **Getter는 Lombok `@Getter`** 사용 — 수동 getter 메서드 작성 금지
- **생성자 주입은 `@RequiredArgsConstructor`** 우선 — 수동 생성자 주입 지양
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

- WebSocket으로 Stasis 이벤트 수신, REST API로 채널/브릿지 제어
- 설정: `application.yml`의 `ari.*` → `AriProperties` record. 재연결: 지수 백오프 (1s→30s)
- 이벤트: WebSocket → `AriEventDispatcher` → `ApplicationEventPublisher` → `@EventListener`

### 테스트

- 단위 테스트: JUnit 5 + Mockito, `*Test.java` 네이밍
- 통합 테스트: `@SpringBootTest` + Testcontainers(PostgreSQL), `*IntegrationTest.java` 네이밍
- 테스트 실행: `./gradlew test`
