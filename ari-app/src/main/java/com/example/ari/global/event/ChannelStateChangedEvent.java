package com.example.ari.global.event;

import java.time.Instant;

public record ChannelStateChangedEvent(
        String channelId,
        String channelName,
        String state,
        Instant timestamp
) implements AriDomainEvent {
}
