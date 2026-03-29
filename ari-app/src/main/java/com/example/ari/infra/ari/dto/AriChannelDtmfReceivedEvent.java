package com.example.ari.infra.ari.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AriChannelDtmfReceivedEvent(
        String type,
        Instant timestamp,
        AriChannel channel,
        String digit,
        @JsonProperty("duration_ms") int durationMs
) implements AriEvent {
}
