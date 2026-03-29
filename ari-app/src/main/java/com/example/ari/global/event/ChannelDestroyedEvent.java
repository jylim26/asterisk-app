package com.example.ari.global.event;

import java.time.Instant;

public record ChannelDestroyedEvent(
        String channelId,
        int cause,
        String causeTxt,
        Instant timestamp
) implements AriDomainEvent {
}
