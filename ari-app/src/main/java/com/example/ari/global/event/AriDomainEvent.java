package com.example.ari.global.event;

public sealed interface AriDomainEvent permits
        CallStartedEvent,
        CallEndedEvent,
        ChannelStateChangedEvent,
        DtmfReceivedEvent,
        HangupRequestedEvent,
        ChannelDestroyedEvent {
}
