package com.example.ari.call.api;

import com.example.ari.call.domain.CallState;
import com.example.ari.call.dto.CallResponse;
import com.example.ari.call.exception.ChannelNotFoundException;
import com.example.ari.call.exception.InvalidCallStateException;
import com.example.ari.call.service.CallService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CallController.class)
class CallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CallService callService;

    @Test
    @DisplayName("GET /api/v1/calls — 활성 콜 목록 조회")
    void getActiveCalls_shouldReturnList() throws Exception {
        CallResponse call = new CallResponse("ch-1", "PJSIP/1001", "1001", "User1",
                CallState.RINGING, Instant.now(), null, null);
        given(callService.getActiveCalls()).willReturn(List.of(call));

        mockMvc.perform(get("/api/v1/calls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].channelId").value("ch-1"))
                .andExpect(jsonPath("$[0].state").value("RINGING"));
    }

    @Test
    @DisplayName("GET /api/v1/calls/{channelId} — 단일 콜 조회")
    void getCall_shouldReturnCall() throws Exception {
        CallResponse call = new CallResponse("ch-1", "PJSIP/1001", "1001", "User1",
                CallState.RINGING, Instant.now(), null, null);
        given(callService.getCall("ch-1")).willReturn(call);

        mockMvc.perform(get("/api/v1/calls/ch-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channelId").value("ch-1"));
    }

    @Test
    @DisplayName("GET /api/v1/calls/{channelId} — 존재하지 않는 채널 404")
    void getCall_notFound_shouldReturn404() throws Exception {
        given(callService.getCall("unknown")).willThrow(new ChannelNotFoundException("unknown"));

        mockMvc.perform(get("/api/v1/calls/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/calls/{channelId}/answer — 콜 응답")
    void answerCall_shouldReturnAnswered() throws Exception {
        CallResponse call = new CallResponse("ch-1", "PJSIP/1001", "1001", "User1",
                CallState.ANSWERED, Instant.now(), Instant.now(), null);
        given(callService.answerCall("ch-1")).willReturn(call);

        mockMvc.perform(post("/api/v1/calls/ch-1/answer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ANSWERED"));
    }

    @Test
    @DisplayName("POST /api/v1/calls/{channelId}/answer — 이미 응답한 콜 409")
    void answerCall_alreadyAnswered_shouldReturn409() throws Exception {
        given(callService.answerCall("ch-1"))
                .willThrow(new InvalidCallStateException("ch-1", CallState.ANSWERED, CallState.ANSWERED));

        mockMvc.perform(post("/api/v1/calls/ch-1/answer"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_CALL_STATE"));
    }

    @Test
    @DisplayName("POST /api/v1/calls/{channelId}/hangup — 콜 종료")
    void hangupCall_shouldReturnHungUp() throws Exception {
        CallResponse call = new CallResponse("ch-1", "PJSIP/1001", "1001", "User1",
                CallState.HUNGUP, Instant.now(), null, Instant.now());
        given(callService.hangupCall("ch-1")).willReturn(call);

        mockMvc.perform(post("/api/v1/calls/ch-1/hangup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("HUNGUP"));
    }
}
