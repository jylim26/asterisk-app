# Asterisk ARI WebSocket 이벤트 레퍼런스

Asterisk 22 LTS 기준, ARI WebSocket을 통해 수신되는 이벤트 스펙 정리.

> 출처: [Asterisk REST API - events.json](https://github.com/asterisk/asterisk/blob/master/rest-api/api-docs/events.json), [channels.json](https://github.com/asterisk/asterisk/blob/master/rest-api/api-docs/channels.json)

## 타임스탬프 포맷

Asterisk는 ISO-8601 형식에 밀리초와 타임존 오프셋을 포함한다.

- 포맷: `%FT%T.%q%z`
- 예시: `2026-03-29T14:30:45.123+0900`
- 타임존: Asterisk 서버의 `/etc/localtime` 기준

## 공통 모델

### Event (모든 이벤트의 베이스)


| 필드            | 타입     | 필수  | 설명                    |
| ------------- | ------ | --- | --------------------- |
| `type`        | string | O   | 이벤트 타입 식별자            |
| `application` | string | O   | 이벤트를 수신하는 Stasis 앱 이름 |
| `timestamp`   | string | O   | ISO-8601 타임스탬프        |
| `asterisk_id` | string |     | Asterisk 인스턴스 ID      |


### Channel


| 필드             | 타입          | 필수  | 설명                                 |
| -------------- | ----------- | --- | ---------------------------------- |
| `id`           | string      | O   | 채널 고유 식별자 (AMI Uniqueid)           |
| `protocol_id`  | string      | O   | 채널 드라이버 프로토콜 ID (예: PJSIP Call-ID) |
| `name`         | string      | O   | 채널 이름 (예: `PJSIP/1001-0000a7e3`)   |
| `state`        | string      | O   | 채널 상태 (아래 참조)                      |
| `caller`       | CallerID    | O   | 발신자 정보                             |
| `connected`    | CallerID    | O   | 연결된 상대방 정보                         |
| `accountcode`  | string      | O   | 계정 코드                              |
| `dialplan`     | DialplanCEP | O   | 현재 다이얼플랜 위치                        |
| `creationtime` | string      | O   | 채널 생성 시각 (ISO-8601)                |
| `language`     | string      | O   | 기본 언어                              |
| `channelvars`  | object      |     | 채널 변수 (key-value)                  |
| `caller_rdnis` | string      |     | Caller RDNIS                       |
| `tenantid`     | string      |     | 테넌트 ID                             |


**Channel State 값:**
`Down`, `Rsrved`, `OffHook`, `Dialing`, `Ring`, `Ringing`, `Up`, `Busy`, `Dialing Offhook`, `Pre-ring`, `Unknown`

### CallerID


| 필드       | 타입     | 필수  | 설명     |
| -------- | ------ | --- | ------ |
| `name`   | string | O   | 발신자 이름 |
| `number` | string | O   | 발신자 번호 |


### DialplanCEP


| 필드         | 타입     | 필수  | 설명                       |
| ---------- | ------ | --- | ------------------------ |
| `context`  | string | O   | 다이얼플랜 컨텍스트               |
| `exten`    | string | O   | 내선 번호                    |
| `priority` | long   | O   | 우선순위                     |
| `app_name` | string | O   | 현재 실행 중인 다이얼플랜 애플리케이션 이름 |
| `app_data` | string | O   | 애플리케이션 파라미터              |


## 현재 구현된 이벤트

### StasisStart

채널이 Stasis 애플리케이션에 진입했을 때 발생.


| 필드                | 타입       | 필수  | 설명                             |
| ----------------- | -------- | --- | ------------------------------ |
| `type`            | string   | O   | `"StasisStart"`                |
| `timestamp`       | string   | O   |                                |
| `application`     | string   | O   |                                |
| `channel`         | Channel  | O   | 진입한 채널                         |
| `args`            | string[] | O   | 애플리케이션 인자 배열                   |
| `replace_channel` | Channel  |     | 교체 대상 채널 (attended transfer 시) |


### StasisEnd

채널이 Stasis 애플리케이션을 떠났을 때 발생.


| 필드            | 타입      | 필수  | 설명            |
| ------------- | ------- | --- | ------------- |
| `type`        | string  | O   | `"StasisEnd"` |
| `timestamp`   | string  | O   |               |
| `application` | string  | O   |               |
| `channel`     | Channel | O   | 떠나는 채널        |


### ChannelStateChange

채널 상태가 변경되었을 때 발생.


| 필드            | 타입      | 필수  | 설명                     |
| ------------- | ------- | --- | ---------------------- |
| `type`        | string  | O   | `"ChannelStateChange"` |
| `timestamp`   | string  | O   |                        |
| `application` | string  | O   |                        |
| `channel`     | Channel | O   | 상태가 변경된 채널             |


### ChannelDtmfReceived

채널에서 DTMF 톤 수신이 완료되었을 때 발생.


| 필드            | 타입      | 필수  | 설명                                       |
| ------------- | ------- | --- | ---------------------------------------- |
| `type`        | string  | O   | `"ChannelDtmfReceived"`                  |
| `timestamp`   | string  | O   |                                          |
| `application` | string  | O   |                                          |
| `channel`     | Channel | O   | DTMF를 수신한 채널                             |
| `digit`       | string  | O   | 수신된 DTMF 문자 (`0`-`9`, `A`-`E`, `#`, `*`) |
| `duration_ms` | int     | O   | DTMF 지속 시간 (밀리초)                         |


### ChannelHangupRequest

채널에 끊기 요청이 발생했을 때.


| 필드            | 타입      | 필수  | 설명                       |
| ------------- | ------- | --- | ------------------------ |
| `type`        | string  | O   | `"ChannelHangupRequest"` |
| `timestamp`   | string  | O   |                          |
| `application` | string  | O   |                          |
| `channel`     | Channel | O   | 끊기 요청된 채널                |
| `cause`       | int     |     | 끊기 원인 코드                 |
| `soft`        | boolean |     | 소프트 끊기 여부                |


### ChannelDestroyed

채널이 파괴되었을 때 발생.


| 필드            | 타입      | 필수  | 설명                   |
| ------------- | ------- | --- | -------------------- |
| `type`        | string  | O   | `"ChannelDestroyed"` |
| `timestamp`   | string  | O   |                      |
| `application` | string  | O   |                      |
| `channel`     | Channel | O   | 파괴된 채널               |
| `cause`       | int     | O   | 끊기 원인 코드             |
| `cause_txt`   | string  | O   | 원인 텍스트 설명            |


## 미구현 이벤트 (향후 지원 후보)

### 채널 이벤트


| 이벤트                      | 설명                          |
| ------------------------ | --------------------------- |
| `ChannelCreated`         | 채널 생성                       |
| `ChannelDialplan`        | 채널의 다이얼플랜 위치 변경             |
| `ChannelEnteredBridge`   | 채널이 브릿지에 참여                 |
| `ChannelLeftBridge`      | 채널이 브릿지에서 이탈                |
| `ChannelTalkingStarted`  | 음성 감지 시작                    |
| `ChannelTalkingFinished` | 음성 감지 종료 (`duration` 필드 포함) |
| `ChannelHold`            | 채널 보류                       |
| `ChannelUnhold`          | 채널 보류 해제                    |
| `ChannelVarset`          | 채널 변수 설정                    |
| `ChannelCallerId`        | Caller ID 변경                |
| `ChannelConnectedLine`   | Connected Line 변경           |
| `ChannelUserevent`       | 사용자 정의 이벤트                  |


### 브릿지 이벤트


| 이벤트                      | 설명          |
| ------------------------ | ----------- |
| `BridgeCreated`          | 브릿지 생성      |
| `BridgeDestroyed`        | 브릿지 파괴      |
| `BridgeMerged`           | 브릿지 병합      |
| `BridgeAttendedTransfer` | Attended 전환 |
| `BridgeBlindTransfer`    | Blind 전환    |


### 녹취 이벤트


| 이벤트                 | 설명    |
| ------------------- | ----- |
| `RecordingStarted`  | 녹취 시작 |
| `RecordingFinished` | 녹취 완료 |
| `RecordingFailed`   | 녹취 실패 |


### 재생 이벤트


| 이벤트                  | 설명        |
| -------------------- | --------- |
| `PlaybackStarted`    | 오디오 재생 시작 |
| `PlaybackFinished`   | 오디오 재생 완료 |
| `PlaybackContinuing` | 오디오 재생 계속 |


### 기타


| 이벤트                   | 설명          |
| --------------------- | ----------- |
| `DeviceStateChanged`  | 디바이스 상태 변경  |
| `EndpointStateChange` | 엔드포인트 상태 변경 |
| `TextMessageReceived` | 텍스트 메시지 수신  |


