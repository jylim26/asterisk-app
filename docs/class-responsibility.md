# ARI App 클래스 책임 문서

## 애플리케이션 진입점

| 클래스 | 책임 |
|--------|------|
| `AriApplication` | Spring Boot 애플리케이션 진입점. `main()` 메서드로 앱을 기동한다. |

## global/config — 앱 전역 설정

| 클래스 | 책임 |
|--------|------|
| `AriProperties` | ARI 연결 설정값(`host`, `port`, `username`, `password`, `appName`)을 `@ConfigurationProperties`로 바인딩한다. 재연결 설정(`Reconnect` 레코드)을 포함하며, `baseUrl()`/`websocketUrl()` 헬퍼 메서드를 제공한다. |
| `RestClientConfig` | ARI REST API 호출용 `RestClient` 빈을 생성한다. Basic Auth 헤더를 자동으로 설정한다. |
| `WebSocketConfig` | WebSocket 클라이언트(`StandardWebSocketClient`), 재연결용 `TaskScheduler`, ARI 타임스탬프 역직렬화 커스터마이저를 빈으로 등록한다. |

## global/error — 글로벌 예외 처리

| 클래스 | 책임 |
|--------|------|
| `ErrorResponse` | 표준화된 에러 응답 DTO. `timestamp`, `status`, `code`, `message` 필드를 갖는 레코드이며, 정적 팩토리 `of()`로 생성한다. |
| `GlobalExceptionHandler` | `@RestControllerAdvice`로 전역 예외를 캐치한다. `IllegalArgumentException` → 400, `RuntimeException` → 500 응답을 반환한다. |

## global/event — 도메인 이벤트 정의

| 클래스 | 책임 |
|--------|------|
| `AriDomainEvent` | 모든 도메인 이벤트의 sealed 인터페이스. 허용 타입을 제한하여 이벤트 체계의 타입 안전성을 보장한다. |
| `CallStartedEvent` | 통화 시작 이벤트. `channelId`, `channelName`, `callerNumber`, `callerName`, `timestamp`를 전달한다. |
| `CallEndedEvent` | 통화 종료(Stasis End) 이벤트. `channelId`, `timestamp`를 전달한다. |
| `ChannelStateChangedEvent` | 채널 상태 변경 이벤트. `channelId`, `channelName`, `state`, `timestamp`를 전달한다. |
| `DtmfReceivedEvent` | DTMF 수신 이벤트. `channelId`, `digit`, `timestamp`를 전달한다. |
| `HangupRequestedEvent` | 끊기 요청 이벤트. `channelId`, `timestamp`를 전달한다. |
| `ChannelDestroyedEvent` | 채널 파괴 이벤트. `channelId`, `cause`, `causeTxt`, `timestamp`를 전달한다. |

## infra/ari/dto — ARI 프로토콜 매핑 객체

| 클래스 | 책임 |
|--------|------|
| `AriEvent` | ARI WebSocket 이벤트의 sealed 인터페이스. `type()`과 `timestamp()`를 공통 메서드로 정의한다. |
| `AriCallerInfo` | 발신자 정보(`name`, `number`) DTO 레코드. |
| `AriChannel` | Asterisk 채널 정보(`id`, `name`, `state`, `caller`, `dialplan` 등) DTO 레코드. |
| `AriDialplan` | 다이얼플랜 정보(`context`, `exten`, `priority`, `appName`, `appData`) DTO 레코드. |
| `AriStasisStartEvent` | Stasis 앱 진입(통화 수신) 이벤트 DTO. `channel`, `args`를 포함한다. |
| `AriStasisEndEvent` | Stasis 앱 종료 이벤트 DTO. `channel`을 포함한다. |
| `AriChannelStateChangeEvent` | 채널 상태 변경 이벤트 DTO. `channel`을 포함한다. |
| `AriChannelDtmfReceivedEvent` | DTMF 수신 이벤트 DTO. `channel`, `digit`, `durationMs`를 포함한다. |
| `AriChannelHangupRequestEvent` | 끊기 요청 이벤트 DTO. `channel`, `cause`, `soft`를 포함한다. |
| `AriChannelDestroyedEvent` | 채널 파괴 이벤트 DTO. `channel`, `cause`, `causeTxt`를 포함한다. |
| `AriUnknownEvent` | 미인식 ARI 이벤트를 위한 DTO. `rawJson`으로 원본 JSON을 보존한다. |
| `AriEventDeserializer` | ARI WebSocket JSON 메시지를 `AriEvent` 타입 객체로 역직렬화한다. `type` 필드 기반으로 구체 타입을 매핑하며, ARI 타임스탬프 파싱 로직을 포함한다. |

## infra/ari/client — ARI WebSocket 클라이언트

| 클래스 | 책임 |
|--------|------|
| `AriConnectionCallback` | WebSocket 연결/해제 콜백 인터페이스. `onConnected()`, `onDisconnected()` 메서드를 정의한다. |
| `AriConnectionManager` | ARI WebSocket 연결 수명주기를 관리한다. `SmartLifecycle` 구현으로 앱 기동 시 자동 연결하며, 지수 백오프 기반 자동 재연결 로직을 수행한다. |
| `AriWebSocketHandler` | `TextWebSocketHandler`를 확장하여 ARI WebSocket 메시지를 수신한다. 수신한 JSON을 `AriEventDeserializer`로 역직렬화하고 `AriEventDispatcher`에 전달한다. 연결/해제/에러 콜백을 처리한다. |

## infra/ari/event — ARI 이벤트 디스패처

| 클래스 | 책임 |
|--------|------|
| `AriEventDispatcher` | ARI 이벤트(`AriEvent`)를 도메인 이벤트(`AriDomainEvent`)로 변환하여 `ApplicationEventPublisher`로 발행한다. switch 패턴 매칭으로 이벤트 타입별 변환을 수행한다. infra → domain 간 의존을 끊는 매개 역할이다. |

## 이벤트 흐름 요약

```
Asterisk ARI WebSocket
  → AriWebSocketHandler (JSON 수신)
    → AriEventDeserializer (JSON → AriEvent 변환)
      → AriEventDispatcher (AriEvent → AriDomainEvent 변환 + 발행)
        → @EventListener (domain 계층에서 구독)
```
