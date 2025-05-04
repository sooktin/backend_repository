package com.sooktin.backend.service.alarm;

import com.sooktin.backend.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private static final Long DEFAULT_TIMEOUT = 60L * 60L * 1000L; // 1시간

    private final EmitterRepository emitterRepository;

    // 최초 구독
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> emitterRepository.remove(userId));
        emitter.onTimeout(() -> emitterRepository.remove(userId));
        emitter.onError(e -> emitterRepository.remove(userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결 성공"));
        } catch (IOException e) {
            log.warn("SSE 연결 초기 메시지 전송 실패 - emitter 제거: {}", e.getMessage());
            emitter.complete();
            emitterRepository.remove(userId);
        }

        return emitter;
    }


    // 알림 전송
    public void send(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitterRepository.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name("alarm")
                    .data(data));
        } catch (IOException | IllegalStateException e) {
            log.warn("알림 전송 실패 - emitter 제거: {}", e.getMessage());
            emitter.complete();
            emitterRepository.remove(userId);
        }
    }

    // 하트비트 전송
    public void sendHeartbeat() {
        emitterRepository.getAllEmitters().forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (IOException | IllegalStateException e) {
                emitter.complete();
                emitterRepository.remove(userId);
            }
        });
    }
}