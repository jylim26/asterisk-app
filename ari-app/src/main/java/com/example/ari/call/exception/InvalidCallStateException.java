package com.example.ari.call.exception;

import com.example.ari.call.domain.CallState;
import com.example.ari.global.error.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidCallStateException extends BusinessException {

    public InvalidCallStateException(String channelId, CallState currentState, CallState targetState) {
        super(HttpStatus.CONFLICT, "INVALID_CALL_STATE",
                "콜 상태 전이 불가: channelId=%s, 현재=%s, 시도=%s".formatted(channelId, currentState, targetState));
    }
}
