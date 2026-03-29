package com.example.ari.infra.ari.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AriChannelDestroyedEvent(
        String type,
        Instant timestamp,
        AriChannel channel,
        int cause,
        @JsonProperty("cause_txt") String causeTxt
) implements AriEvent {
}
