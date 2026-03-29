package com.example.ari.call.domain;

import com.example.ari.call.exception.InvalidCallStateException;
import lombok.Getter;

import java.time.Instant;

@Getter
public class Call {

    private final String channelId;
    private final String channelName;
    private final String callerNumber;
    private final String callerName;
    private final Instant startedAt;
    private CallState state;
    private Instant answeredAt;
    private Instant endedAt;

    private Call(String channelId, String channelName, String callerNumber, String callerName, Instant startedAt) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.callerNumber = callerNumber;
        this.callerName = callerName;
        this.startedAt = startedAt;
        this.state = CallState.RINGING;
    }

    public static Call create(String channelId, String channelName, String callerNumber, String callerName, Instant startedAt) {
        return new Call(channelId, channelName, callerNumber, callerName, startedAt);
    }

    public void answer() {
        if (state != CallState.RINGING) {
            throw new InvalidCallStateException(channelId, state, CallState.ANSWERED);
        }
        this.state = CallState.ANSWERED;
        this.answeredAt = Instant.now();
    }

    public void hangUp() {
        if (state == CallState.HUNGUP) {
            throw new InvalidCallStateException(channelId, state, CallState.HUNGUP);
        }
        this.state = CallState.HUNGUP;
        this.endedAt = Instant.now();
    }
}
