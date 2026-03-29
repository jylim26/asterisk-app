package com.example.ari.call.exception;

public class ChannelNotFoundException extends RuntimeException {

    public ChannelNotFoundException(String channelId) {
        super("채널을 찾을 수 없습니다: " + channelId);
    }
}
