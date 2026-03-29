package com.example.ari.infra.ari.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AriStasisStartEvent(
        String type,
        Instant timestamp,
        AriChannel channel,
        List<String> args
) implements AriEvent {
}
