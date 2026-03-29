package com.example.ari.call.exception;

import com.example.ari.call.domain.CallState;

public class InvalidCallStateException extends RuntimeException {

    public InvalidCallStateException(String channelId, CallState currentState, CallState targetState) {
        super("콜 상태 전이 불가: channelId=%s, 현재=%s, 시도=%s".formatted(channelId, currentState, targetState));
    }
}
