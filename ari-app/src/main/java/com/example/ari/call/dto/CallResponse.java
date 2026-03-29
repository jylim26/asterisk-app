package com.example.ari.call.dto;

import com.example.ari.call.domain.Call;
import com.example.ari.call.domain.CallState;

import java.time.Instant;

public record CallResponse(
        String channelId,
        String channelName,
        String callerNumber,
        String callerName,
        CallState state,
        Instant startedAt,
        Instant answeredAt,
        Instant endedAt
) {

    public static CallResponse from(Call call) {
        return new CallResponse(
                call.getChannelId(),
                call.getChannelName(),
                call.getCallerNumber(),
                call.getCallerName(),
                call.getState(),
                call.getStartedAt(),
                call.getAnsweredAt(),
                call.getEndedAt()
        );
    }
}
