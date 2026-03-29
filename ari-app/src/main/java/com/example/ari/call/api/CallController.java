package com.example.ari.call.api;

import com.example.ari.call.dto.CallResponse;
import com.example.ari.call.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;

    @GetMapping
    public ResponseEntity<List<CallResponse>> getActiveCalls() {
        List<CallResponse> calls = callService.getActiveCalls();
        return ResponseEntity.ok(calls);
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<CallResponse> getCall(@PathVariable String channelId) {
        CallResponse call = callService.getCall(channelId);
        return ResponseEntity.ok(call);
    }

    @PostMapping("/{channelId}/answer")
    public ResponseEntity<CallResponse> answerCall(@PathVariable String channelId) {
        CallResponse call = callService.answerCall(channelId);
        return ResponseEntity.ok(call);
    }

    @PostMapping("/{channelId}/hangup")
    public ResponseEntity<CallResponse> hangupCall(@PathVariable String channelId) {
        CallResponse call = callService.hangupCall(channelId);
        return ResponseEntity.ok(call);
    }
}
