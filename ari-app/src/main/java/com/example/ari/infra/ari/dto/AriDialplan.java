package com.example.ari.infra.ari.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AriDialplan(
        String context,
        String exten,
        long priority,
        @JsonProperty("app_name") String appName,
        @JsonProperty("app_data") String appData
) {
}
