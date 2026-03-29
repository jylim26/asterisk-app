package com.example.ari.global.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.time.Instant;

@Configuration
public class WebSocketConfig {

    @Bean
    public TaskScheduler ariTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ari-reconnect-");
        scheduler.setDaemon(true);
        return scheduler;
    }

    @Bean
    public WebSocketClient ariWebSocketClient() {
        return new StandardWebSocketClient();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer ariInstantDeserializerCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("ari-instant");
            module.addDeserializer(Instant.class, new AriInstantDeserializer());
            builder.modules(module);
        };
    }
}
