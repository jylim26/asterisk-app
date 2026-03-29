package com.example.ari.global.config;

import com.example.ari.global.error.InvalidConfigurationException;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ari")
public record AriProperties(
        String host,
        int port,
        String username,
        String password,
        String appName,
        Reconnect reconnect
) {

    public record Reconnect(
            long initialDelayMs,
            long maxDelayMs,
            int maxAttempts
    ) {
        public Reconnect {
            if (initialDelayMs <= 0) {
                throw new InvalidConfigurationException(
                        "ari.reconnect.initial-delay-ms must be positive, got: " + initialDelayMs);
            }
            if (maxDelayMs <= 0) {
                throw new InvalidConfigurationException(
                        "ari.reconnect.max-delay-ms must be positive, got: " + maxDelayMs);
            }
        }
    }

    public String baseUrl() {
        return "http://" + host + ":" + port;
    }

    public String websocketUrl() {
        return "ws://" + host + ":" + port + "/ari/events?api_key=" + username + ":" + password + "&app=" + appName;
    }
}
