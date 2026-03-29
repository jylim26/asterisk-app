package com.example.ari.call.service;

import com.example.ari.call.domain.CallState;
import com.example.ari.call.dto.CallResponse;
import com.example.ari.call.exception.ChannelNotFoundException;
import com.example.ari.call.exception.InvalidCallStateException;
import com.example.ari.call.infra.AriCallClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CallServiceTest {

    @Mock
    private AriCallClient ariCallClient;

    private CallService callService;

    @BeforeEach
    void setUp() {
        callService = new CallService(ariCallClient);
    }

    @Test
    @DisplayName("콜 등록 후 조회 성공")
    void registerCall_shouldBeRetrievable() {
        callService.registerCall("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());

        CallResponse response = callService.getCall("ch-1");

        assertThat(response.channelId()).isEqualTo("ch-1");
        assertThat(response.callerNumber()).isEqualTo("1001");
        assertThat(response.state()).isEqualTo(CallState.RINGING);
    }

    @Test
    @DisplayName("콜 응답 시 ARI 호출 + 상태 전이")
    void answerCall_shouldCallAriAndTransition() {
        callService.registerCall("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());

        CallResponse response = callService.answerCall("ch-1");

        assertThat(response.state()).isEqualTo(CallState.ANSWERED);
        assertThat(response.answeredAt()).isNotNull();
        verify(ariCallClient).answerChannel("ch-1");
    }

    @Test
    @DisplayName("콜 종료 시 ARI 호출 + 상태 전이")
    void hangupCall_shouldCallAriAndTransition() {
        callService.registerCall("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());

        CallResponse response = callService.hangupCall("ch-1");

        assertThat(response.state()).isEqualTo(CallState.HUNGUP);
        assertThat(response.endedAt()).isNotNull();
        verify(ariCallClient).hangupChannel("ch-1");
    }

    @Test
    @DisplayName("존재하지 않는 채널 조회 시 예외")
    void getCall_nonExistent_shouldThrow() {
        assertThatThrownBy(() -> callService.getCall("unknown"))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("이미 응답한 콜 재응답 시 예외")
    void answerCall_alreadyAnswered_shouldThrow() {
        callService.registerCall("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());
        callService.answerCall("ch-1");

        assertThatThrownBy(() -> callService.answerCall("ch-1"))
                .isInstanceOf(InvalidCallStateException.class);
    }

    @Test
    @DisplayName("콜 제거 후 조회 시 예외")
    void removeCall_shouldNotBeRetrievable() {
        callService.registerCall("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());

        callService.removeCall("ch-1");

        assertThatThrownBy(() -> callService.getCall("ch-1"))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("활성 콜 목록 조회")
    void getActiveCalls_shouldReturnAllCalls() {
        callService.registerCall("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());
        callService.registerCall("ch-2", "PJSIP/1002-00000002", "1002", "User2", Instant.now());

        List<CallResponse> calls = callService.getActiveCalls();

        assertThat(calls).hasSize(2);
    }
}
