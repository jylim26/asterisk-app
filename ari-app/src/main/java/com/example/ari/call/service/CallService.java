package com.example.ari.call.service;

import com.example.ari.call.domain.Call;
import com.example.ari.call.dto.CallResponse;
import com.example.ari.call.exception.ChannelNotFoundException;
import com.example.ari.call.infra.AriCallClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final AriCallClient ariCallClient;
    private final ConcurrentMap<String, Call> activeCalls = new ConcurrentHashMap<>();

    public void registerCall(String channelId, String channelName, String callerNumber, String callerName, Instant startedAt) {
        Call call = Call.create(channelId, channelName, callerNumber, callerName, startedAt);
        activeCalls.put(channelId, call);
        log.info("콜 등록: channelId={}, caller={}", channelId, callerNumber);
    }

    public CallResponse answerCall(String channelId) {
        Call call = findCallOrThrow(channelId);
        ariCallClient.answerChannel(channelId);
        call.answer();
        log.info("콜 응답: channelId={}", channelId);
        return CallResponse.from(call);
    }

    public CallResponse hangupCall(String channelId) {
        Call call = findCallOrThrow(channelId);
        ariCallClient.hangupChannel(channelId);
        call.hangUp();
        log.info("콜 종료: channelId={}", channelId);
        return CallResponse.from(call);
    }

    public void removeCall(String channelId) {
        Call call = activeCalls.remove(channelId);
        if (call != null) {
            log.info("콜 제거: channelId={}", channelId);
        }
    }

    public CallResponse getCall(String channelId) {
        Call call = findCallOrThrow(channelId);
        return CallResponse.from(call);
    }

    public List<CallResponse> getActiveCalls() {
        return activeCalls.values().stream()
                .map(CallResponse::from)
                .toList();
    }

    private Call findCallOrThrow(String channelId) {
        Call call = activeCalls.get(channelId);
        if (call == null) {
            throw new ChannelNotFoundException(channelId);
        }
        return call;
    }
}
