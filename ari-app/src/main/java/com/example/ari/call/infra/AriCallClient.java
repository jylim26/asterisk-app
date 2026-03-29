package com.example.ari.call.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AriCallClient {

    private final RestClient ariRestClient;

    public void answerChannel(String channelId) {
        log.debug("ARI 채널 응답 요청: channelId={}", channelId);
        ariRestClient.post()
                .uri("/channels/{channelId}/answer", channelId)
                .retrieve()
                .toBodilessEntity();
    }

    public void hangupChannel(String channelId, String reason) {
        log.debug("ARI 채널 종료 요청: channelId={}, reason={}", channelId, reason);
        ariRestClient.delete()
                .uri("/channels/{channelId}?reason={reason}", channelId, reason)
                .retrieve()
                .toBodilessEntity();
    }

    public void hangupChannel(String channelId) {
        hangupChannel(channelId, "normal");
    }

    public void continueInDialplan(String channelId) {
        log.debug("ARI 다이얼플랜 계속 요청: channelId={}", channelId);
        ariRestClient.post()
                .uri("/channels/{channelId}/continue", channelId)
                .retrieve()
                .toBodilessEntity();
    }

    public void playMedia(String channelId, String mediaUri) {
        log.debug("ARI 미디어 재생 요청: channelId={}, media={}", channelId, mediaUri);
        ariRestClient.post()
                .uri("/channels/{channelId}/play?media={media}", channelId, mediaUri)
                .retrieve()
                .toBodilessEntity();
    }

    public void stopPlayback(String playbackId) {
        log.debug("ARI 재생 중지 요청: playbackId={}", playbackId);
        ariRestClient.delete()
                .uri("/playbacks/{playbackId}", playbackId)
                .retrieve()
                .toBodilessEntity();
    }
}
