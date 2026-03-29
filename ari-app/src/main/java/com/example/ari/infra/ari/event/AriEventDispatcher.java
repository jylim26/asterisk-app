package com.example.ari.infra.ari.event;

import com.example.ari.global.event.AriDomainEvent;
import com.example.ari.global.event.CallEndedEvent;
import com.example.ari.global.event.CallStartedEvent;
import com.example.ari.global.event.ChannelDestroyedEvent;
import com.example.ari.global.event.ChannelStateChangedEvent;
import com.example.ari.global.event.DtmfReceivedEvent;
import com.example.ari.global.event.HangupRequestedEvent;
import com.example.ari.infra.ari.dto.AriChannelDestroyedEvent;
import com.example.ari.infra.ari.dto.AriChannelDtmfReceivedEvent;
import com.example.ari.infra.ari.dto.AriChannelHangupRequestEvent;
import com.example.ari.infra.ari.dto.AriChannelStateChangeEvent;
import com.example.ari.infra.ari.dto.AriEvent;
import com.example.ari.infra.ari.dto.AriStasisEndEvent;
import com.example.ari.infra.ari.dto.AriStasisStartEvent;
import com.example.ari.infra.ari.dto.AriUnknownEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AriEventDispatcher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void dispatch(AriEvent ariEvent) {
        toDomainEvent(ariEvent).ifPresent(domainEvent -> {
            log.debug("도메인 이벤트 발행: type={}, event={}",
                    domainEvent.getClass().getSimpleName(), domainEvent);
            applicationEventPublisher.publishEvent(domainEvent);
        });
    }

    private Optional<AriDomainEvent> toDomainEvent(AriEvent ariEvent) {
        return switch (ariEvent) {
            case AriStasisStartEvent e -> Optional.of(new CallStartedEvent(
                    e.channel().id(),
                    e.channel().name(),
                    e.channel().caller().number(),
                    e.channel().caller().name(),
                    e.timestamp()
            ));
            case AriStasisEndEvent e -> Optional.of(new CallEndedEvent(
                    e.channel().id(),
                    e.timestamp()
            ));
            case AriChannelStateChangeEvent e -> Optional.of(new ChannelStateChangedEvent(
                    e.channel().id(),
                    e.channel().name(),
                    e.channel().state(),
                    e.timestamp()
            ));
            case AriChannelDtmfReceivedEvent e -> Optional.of(new DtmfReceivedEvent(
                    e.channel().id(),
                    e.digit(),
                    e.timestamp()
            ));
            case AriChannelHangupRequestEvent e -> Optional.of(new HangupRequestedEvent(
                    e.channel().id(),
                    e.timestamp()
            ));
            case AriChannelDestroyedEvent e -> Optional.of(new ChannelDestroyedEvent(
                    e.channel().id(),
                    e.cause(),
                    e.causeTxt(),
                    e.timestamp()
            ));
            case AriUnknownEvent e -> {
                log.debug("미인식 ARI 이벤트 무시: type={}", e.type());
                yield Optional.empty();
            }
        };
    }
}
