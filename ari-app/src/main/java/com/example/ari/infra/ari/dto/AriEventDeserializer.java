package com.example.ari.infra.ari.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AriEventDeserializer {

    private final ObjectMapper objectMapper;

    private static final Map<String, Class<? extends AriEvent>> EVENT_TYPE_MAP = Map.of(
            "StasisStart", AriStasisStartEvent.class,
            "StasisEnd", AriStasisEndEvent.class,
            "ChannelStateChange", AriChannelStateChangeEvent.class,
            "ChannelDtmfReceived", AriChannelDtmfReceivedEvent.class,
            "ChannelHangupRequest", AriChannelHangupRequestEvent.class,
            "ChannelDestroyed", AriChannelDestroyedEvent.class
    );

    public AriEvent deserialize(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            String type = rootNode.has("type") ? rootNode.get("type").asText() : "Unknown";

            Class<? extends AriEvent> eventClass = EVENT_TYPE_MAP.get(type);
            if (eventClass != null) {
                return objectMapper.treeToValue(rootNode, eventClass);
            }

            Instant timestamp = rootNode.has("timestamp")
                    ? objectMapper.treeToValue(rootNode.get("timestamp"), Instant.class)
                    : Instant.now();

            log.debug("Unknown ARI event type: {}", type);
            return new AriUnknownEvent(type, timestamp, json);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize ARI event: {}", e.getMessage());
            return new AriUnknownEvent("DeserializationError", Instant.now(), json);
        }
    }
}
