package com.example.ari.call.domain;

import com.example.ari.call.exception.InvalidCallStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallTest {

    @Test
    @DisplayName("콜 생성 시 RINGING 상태")
    void create_shouldBeRingingState() {
        Call call = Call.create("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());

        assertThat(call.getState()).isEqualTo(CallState.RINGING);
        assertThat(call.getChannelId()).isEqualTo("ch-1");
        assertThat(call.getCallerNumber()).isEqualTo("1001");
        assertThat(call.getAnsweredAt()).isNull();
        assertThat(call.getEndedAt()).isNull();
    }

    @Test
    @DisplayName("RINGING → ANSWERED 전이 성공")
    void answer_fromRinging_shouldTransitionToAnswered() {
        Call call = Call.create("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());

        call.answer();

        assertThat(call.getState()).isEqualTo(CallState.ANSWERED);
        assertThat(call.getAnsweredAt()).isNotNull();
    }

    @Test
    @DisplayName("ANSWERED 상태에서 answer() 호출 시 예외")
    void answer_fromAnswered_shouldThrow() {
        Call call = Call.create("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());
        call.answer();

        assertThatThrownBy(call::answer)
                .isInstanceOf(InvalidCallStateException.class);
    }

    @Test
    @DisplayName("RINGING → HUNGUP 전이 성공")
    void hangUp_fromRinging_shouldTransitionToHungUp() {
        Call call = Call.create("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());

        call.hangUp();

        assertThat(call.getState()).isEqualTo(CallState.HUNGUP);
        assertThat(call.getEndedAt()).isNotNull();
    }

    @Test
    @DisplayName("ANSWERED → HUNGUP 전이 성공")
    void hangUp_fromAnswered_shouldTransitionToHungUp() {
        Call call = Call.create("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());
        call.answer();

        call.hangUp();

        assertThat(call.getState()).isEqualTo(CallState.HUNGUP);
        assertThat(call.getEndedAt()).isNotNull();
    }

    @Test
    @DisplayName("HUNGUP 상태에서 hangUp() 호출 시 예외")
    void hangUp_fromHungUp_shouldThrow() {
        Call call = Call.create("ch-1", "PJSIP/1001-00000001", "1001", "User1", Instant.now());
        call.hangUp();

        assertThatThrownBy(call::hangUp)
                .isInstanceOf(InvalidCallStateException.class);
    }
}
