package com.example.ari.infra.ari.client;

import com.example.ari.infra.ari.dto.AriEvent;
import com.example.ari.infra.ari.dto.AriEventDeserializer;
import com.example.ari.infra.ari.event.AriEventDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class AriWebSocketHandler extends TextWebSocketHandler {

    private final AriEventDeserializer ariEventDeserializer;
    private final AriEventDispatcher ariEventDispatcher;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("ARI WebSocket 연결 성공: sessionId={}", session.getId());
        applicationEventPublisher.publishEvent(new AriConnectedEvent());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.trace("ARI WebSocket 메시지 수신: {}", payload);
        try {
            AriEvent ariEvent = ariEventDeserializer.deserialize(payload);
            ariEventDispatcher.dispatch(ariEvent);
        } catch (Exception e) {
            log.error("ARI 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("ARI WebSocket 연결 종료: status={}", status);
        applicationEventPublisher.publishEvent(new AriDisconnectedEvent());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("ARI WebSocket 전송 오류: {}", exception.getMessage());
        applicationEventPublisher.publishEvent(new AriDisconnectedEvent());
    }
}
