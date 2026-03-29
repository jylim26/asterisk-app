package com.example.ari.call.event;

import com.example.ari.call.service.CallService;
import com.example.ari.global.event.CallEndedEvent;
import com.example.ari.global.event.CallStartedEvent;
import com.example.ari.global.event.ChannelDestroyedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallEventListener {

    private final CallService callService;

    @EventListener
    public void onCallStarted(CallStartedEvent event) {
        log.debug("CallStartedEvent 수신: channelId={}", event.channelId());
        callService.registerCall(
                event.channelId(),
                event.channelName(),
                event.callerNumber(),
                event.callerName(),
                event.timestamp()
        );
    }

    @EventListener
    public void onCallEnded(CallEndedEvent event) {
        log.debug("CallEndedEvent 수신: channelId={}", event.channelId());
        callService.removeCall(event.channelId());
    }

    @EventListener
    public void onChannelDestroyed(ChannelDestroyedEvent event) {
        log.debug("ChannelDestroyedEvent 수신: channelId={}, cause={}", event.channelId(), event.causeTxt());
        callService.removeCall(event.channelId());
    }
}
