package com.example.ari.global.event;

import java.time.Instant;

public record CallEndedEvent(
        String channelId,
        Instant timestamp
) implements AriDomainEvent {
}
