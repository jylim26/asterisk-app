package com.example.ari.global.event;

import java.time.Instant;

public record DtmfReceivedEvent(
        String channelId,
        String digit,
        Instant timestamp
) implements AriDomainEvent {
}
