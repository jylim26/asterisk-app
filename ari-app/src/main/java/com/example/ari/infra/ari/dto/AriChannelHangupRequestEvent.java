package com.example.ari.infra.ari.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AriChannelHangupRequestEvent(
        String type,
        Instant timestamp,
        AriChannel channel,
        Integer cause,
        Boolean soft
) implements AriEvent {
}
