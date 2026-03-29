package com.example.ari.infra.ari.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AriCallerInfo(
        String name,
        String number
) {
}
