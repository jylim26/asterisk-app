package com.example.ari.call.exception;

import com.example.ari.global.error.BusinessException;
import org.springframework.http.HttpStatus;

public class ChannelNotFoundException extends BusinessException {

    public ChannelNotFoundException(String channelId) {
        super(HttpStatus.NOT_FOUND, "CHANNEL_NOT_FOUND", "채널을 찾을 수 없습니다: " + channelId);
    }
}
