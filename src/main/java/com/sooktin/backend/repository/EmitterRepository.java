package com.sooktin.backend.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmitterRepository {

    private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void save(Long userId, SseEmitter emitter) {
        emitterMap.put(userId, emitter);
    }

    public SseEmitter get(Long userId) {
        return emitterMap.get(userId);
    }

    public void remove(Long userId) {
        emitterMap.remove(userId);
    }

    public Map<Long, SseEmitter> getAllEmitters() {
        return emitterMap;
    }
}