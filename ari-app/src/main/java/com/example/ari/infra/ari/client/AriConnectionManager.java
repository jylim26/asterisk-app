package com.example.ari.infra.ari.client;

import com.example.ari.global.config.AriProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class AriConnectionManager implements SmartLifecycle {

    private final WebSocketClient webSocketClient;
    private final AriProperties ariProperties;
    private final TaskScheduler ariTaskScheduler;
    private final AriWebSocketHandler ariWebSocketHandler;

    private volatile boolean running = false;
    private volatile WebSocketSession currentSession;
    private final AtomicInteger reconnectAttempt = new AtomicInteger(0);
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);

    @Override
    public void start() {
        running = true;
        log.info("ARI WebSocket 연결 시작: {}", ariProperties.websocketUrl());
        connect();
    }

    @Override
    public void stop() {
        running = false;
        closeSession();
        log.info("ARI WebSocket 연결 관리자 종료");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @EventListener
    public void onConnected(AriConnectedEvent event) {
        reconnectAttempt.set(0);
        reconnecting.set(false);
        log.info("ARI WebSocket 연결 완료, 재연결 카운터 리셋");
    }

    @EventListener
    public void onDisconnected(AriDisconnectedEvent event) {
        if (running && reconnecting.compareAndSet(false, true)) {
            scheduleReconnect();
        }
    }

    private void connect() {
        String url = ariProperties.websocketUrl();
        try {
            CompletableFuture<WebSocketSession> future = webSocketClient.execute(ariWebSocketHandler, url);
            future.whenComplete((WebSocketSession session, Throwable throwable) -> {
                if (throwable != null) {
                    log.error("ARI WebSocket 연결 실패: {}", throwable.getMessage());
                    if (running && reconnecting.compareAndSet(false, true)) {
                        scheduleReconnect();
                    }
                } else {
                    currentSession = session;
                }
            });
        } catch (Exception e) {
            log.error("ARI WebSocket 연결 시도 중 예외 발생: {}", e.getMessage());
            if (running && reconnecting.compareAndSet(false, true)) {
                scheduleReconnect();
            }
        }
    }

    private void scheduleReconnect() {
        AriProperties.Reconnect config = ariProperties.reconnect();
        int attempt = reconnectAttempt.incrementAndGet();

        if (config.maxAttempts() > 0 && attempt > config.maxAttempts()) {
            log.error("ARI WebSocket 최대 재연결 시도 횟수({}) 초과, 재연결 중단", config.maxAttempts());
            reconnecting.set(false);
            return;
        }

        long delayMs = Math.min(
                config.initialDelayMs() * (1L << (attempt - 1)),
                config.maxDelayMs()
        );

        log.info("ARI WebSocket 재연결 예약: attempt={}, delay={}ms", attempt, delayMs);
        ariTaskScheduler.schedule(() -> {
            reconnecting.set(false);
            connect();
        }, Instant.now().plusMillis(delayMs));
    }

    private void closeSession() {
        WebSocketSession session = currentSession;
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                log.warn("ARI WebSocket 세션 종료 중 오류: {}", e.getMessage());
            }
        }
        currentSession = null;
    }
}
