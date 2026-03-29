package com.example.ari.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final AriProperties ariProperties;

    @Bean
    public RestClient ariRestClient() {
        return RestClient.builder()
                .baseUrl(ariProperties.baseUrl() + "/ari")
                .defaultHeaders(headers -> headers.setBasicAuth(ariProperties.username(), ariProperties.password()))
                .build();
    }
}
