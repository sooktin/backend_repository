package com.sooktin.backend.service.alarm;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {

    private final SseService sseService;

    @Scheduled(fixedRate = 30000) // 30초마다
    public void heartbeat() {
        sseService.sendHeartbeat();
    }
}