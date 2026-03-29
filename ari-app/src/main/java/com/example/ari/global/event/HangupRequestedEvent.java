package com.example.ari.global.event;

import java.time.Instant;

public record HangupRequestedEvent(
        String channelId,
        Instant timestamp
) implements AriDomainEvent {
}
