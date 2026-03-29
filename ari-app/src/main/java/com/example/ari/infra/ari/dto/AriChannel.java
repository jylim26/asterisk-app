package com.example.ari.infra.ari.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AriChannel(
        String id,
        @JsonProperty("protocol_id") String protocolId,
        String name,
        String state,
        AriCallerInfo caller,
        AriCallerInfo connected,
        String accountcode,
        AriDialplan dialplan,
        String creationtime,
        String language
) {
}
