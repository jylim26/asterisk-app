package com.example.ari.infra.ari.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AriChannelStateChangeEvent(
        String type,
        Instant timestamp,
        AriChannel channel
) implements AriEvent {
}
