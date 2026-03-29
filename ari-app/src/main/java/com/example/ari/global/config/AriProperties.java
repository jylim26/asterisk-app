package com.example.ari.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ari")
public record AriProperties(
        String host,
        int port,
        String username,
        String password,
        String appName
) {

    public String baseUrl() {
        return "http://" + host + ":" + port;
    }

    public String websocketUrl() {
        return "ws://" + host + ":" + port + "/ari/events?api_key=" + username + ":" + password + "&app=" + appName;
    }
}
