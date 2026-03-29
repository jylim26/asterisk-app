package com.example.ari.infra.ari.dto;

import java.time.Instant;

public record AriUnknownEvent(
        String type,
        Instant timestamp,
        String rawJson
) implements AriEvent {
}
