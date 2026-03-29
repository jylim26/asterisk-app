package com.example.ari.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final AriProperties ariProperties;

    @Bean
    public RestClient ariRestClient() {
        String credentials = ariProperties.username() + ":" + ariProperties.password();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        return RestClient.builder()
                .baseUrl(ariProperties.baseUrl() + "/ari")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .build();
    }
}
