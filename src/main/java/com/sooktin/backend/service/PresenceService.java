package com.sooktin.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PresenceService {
    private final RedisTemplate<String,String> redisTemplate;
    private static final String ACTIVE_KEY_PREFIX = "presence:active:";
    private static final int PRESENCE_TIMEOUT = 360; //seconds

    public void setUserActive(String nickname, String roomId) {
        String key = ACTIVE_KEY_PREFIX + roomId;
        redisTemplate.opsForSet().add(key, nickname);
        redisTemplate.expire(key, PRESENCE_TIMEOUT, TimeUnit.SECONDS);
    }

    public void setUserInactive(String nickname, String roomId) {
        String key = ACTIVE_KEY_PREFIX + roomId;
        redisTemplate.opsForSet().remove(key, nickname);
    }

    public Set<String> getActiveViewers(String roomId) {
        String key = ACTIVE_KEY_PREFIX + roomId;
        Set<String> members = redisTemplate.opsForSet().members(key);
        return members != null ? members : Set.of();
    }


    public boolean isUserActive(String username, String roomId) {
        String key = ACTIVE_KEY_PREFIX + roomId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, username);
        return isMember != null && isMember;
    }

    //presence 리프레쉬
    public void refreshPresence(String username, String roomId) {
        setUserActive(username, roomId);
    }
}
