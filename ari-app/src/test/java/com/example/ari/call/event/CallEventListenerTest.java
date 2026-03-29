package com.example.ari.call.event;

import com.example.ari.call.dto.CallResponse;
import com.example.ari.call.exception.ChannelNotFoundException;
import com.example.ari.call.infra.AriCallClient;
import com.example.ari.call.service.CallService;
import com.example.ari.global.event.CallEndedEvent;
import com.example.ari.global.event.CallStartedEvent;
import com.example.ari.global.event.ChannelDestroyedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CallEventListenerTest {

    @Mock
    private AriCallClient ariCallClient;

    private CallService callService;
    private CallEventListener callEventListener;

    @BeforeEach
    void setUp() {
        callService = new CallService(ariCallClient);
        callEventListener = new CallEventListener(callService);
    }

    @Test
    @DisplayName("CallStartedEvent 수신 시 콜 등록")
    void onCallStarted_shouldRegisterCall() {
        CallStartedEvent event = new CallStartedEvent("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());

        callEventListener.onCallStarted(event);

        CallResponse call = callService.getCall("ch-1");
        assertThat(call.channelId()).isEqualTo("ch-1");
        assertThat(call.callerNumber()).isEqualTo("1001");
    }

    @Test
    @DisplayName("CallEndedEvent 수신 시 콜 제거")
    void onCallEnded_shouldRemoveCall() {
        callEventListener.onCallStarted(new CallStartedEvent("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now()));

        callEventListener.onCallEnded(new CallEndedEvent("ch-1", Instant.now()));

        assertThatThrownBy(() -> callService.getCall("ch-1"))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("ChannelDestroyedEvent 수신 시 콜 제거")
    void onChannelDestroyed_shouldRemoveCall() {
        callEventListener.onCallStarted(new CallStartedEvent("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now()));

        callEventListener.onChannelDestroyed(new ChannelDestroyedEvent("ch-1", 16, "Normal Clearing", Instant.now()));

        assertThatThrownBy(() -> callService.getCall("ch-1"))
                .isInstanceOf(ChannelNotFoundException.class);
    }
}
