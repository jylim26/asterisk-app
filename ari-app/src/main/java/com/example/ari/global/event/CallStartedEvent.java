package com.example.ari.global.event;

import java.time.Instant;

public record CallStartedEvent(
        String channelId,
        String channelName,
        String callerNumber,
        String callerName,
        Instant timestamp
) implements AriDomainEvent {
}
