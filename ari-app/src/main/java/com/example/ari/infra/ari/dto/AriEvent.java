package com.example.ari.infra.ari.dto;

import java.time.Instant;

public sealed interface AriEvent permits
        AriStasisStartEvent,
        AriStasisEndEvent,
        AriChannelStateChangeEvent,
        AriChannelDtmfReceivedEvent,
        AriChannelHangupRequestEvent,
        AriChannelDestroyedEvent,
        AriUnknownEvent {

    String type();

    Instant timestamp();
}
