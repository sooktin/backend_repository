package com.sooktin.backend.service;

import com.sooktin.backend.global.exception.auth.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final StringRedisTemplate redisTemplate;
    private static final String RECENT_KEYWORDS_PREFIX = "recent_keywords:";
    private static final String POPULAR_KEYWORDS_KEY = "popular_keywords";

    @Transactional
    public void trackSearchKeyword(String keyword, Long userId) {
        if (keyword == null || keyword.trim().isEmpty() || userId == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        // 최근 검색어 저장 (유저별 최대 10개 유지)
        saveRecentKeyword(keyword, userId);

        //  인기 검색어 카운트 증가 -> 채민이가 커스텀하세요
        redisTemplate.opsForZSet().incrementScore(POPULAR_KEYWORDS_KEY, keyword, 1);
    }

    // 최근 검색어 저장
    private void saveRecentKeyword(String keyword, Long userId) {
        String userKey = RECENT_KEYWORDS_PREFIX + userId;
        redisTemplate.opsForList().remove(userKey, 1, keyword); // 중복 제거
        redisTemplate.opsForList().leftPush(userKey, keyword); // 최신 검색어 저장
        redisTemplate.opsForList().trim(userKey, 0, 9); // 최대 10개 유지
    }


    // 로그인한 사용자의 최근 검색어 조회 (최신순)
    public List<String> getRecentKeywords(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        String userKey = RECENT_KEYWORDS_PREFIX + userId;
        List<String> recentKeywords = redisTemplate.opsForList().range(userKey, 0, 9);
        return recentKeywords != null ? recentKeywords : List.of();
    }
}
