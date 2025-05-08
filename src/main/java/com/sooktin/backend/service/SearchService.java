package com.sooktin.backend.service;

import com.sooktin.backend.domain.SearchKeyword;
import com.sooktin.backend.global.exception.auth.UnauthorizedException;
import com.sooktin.backend.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final StringRedisTemplate redisTemplate;
    private final SearchKeywordRepository searchKeywordRepository;
    private static final String RECENT_KEYWORDS_PREFIX = "recent_keywords:";
    private static final String POPULAR_KEYWORDS_KEY = "popular_keywords";
    private static final String HOURLY_KEYWORDS_PREFIX = "hourly_keywords:";
    private static final String DAILY_KEYWORDS_PREFIX = "daily_keywords:";
    private static final String WEEKLY_KEYWORDS_PREFIX = "weekly_keywords:";
    private static final String USER_SEARCH_PREFIX = "user_search:";
    private static final int TOP_TEN = 10;

    // 점수 계산을 위한 가중치
    private static final double HOURLY_WEIGHT = 0.15;
    private static final double DAILY_WEIGHT = 0.12;
    private static final double WEEKLY_WEIGHT = 0.04;
    private static final double TOTAL_HOURLY_WEIGHT = 0.05;
    private static final double TOTAL_DAILY_WEIGHT = 0.04;
    private static final double TOTAL_WEEKLY_WEIGHT = 0.03;

    // 과도한 서치 방지용 쿨다운 타임 (60초)
    private static final int SEARCH_COOLDOWN = 60;

    @Transactional
    public void trackSearchKeyword(String keyword, Long userId) {
        if (keyword == null || keyword.trim().isEmpty() || userId == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        // 최근 검색어 저장 (유저별 최대 10개 유지)
        saveRecentKeyword(keyword, userId);

        // 인기검색어: 중복 검색 방지를 위한 체크 (같은 사용자가 같은 키워드를 60초 내에 연속으로 검색하는 것 방지)
        String userSearchKey = USER_SEARCH_PREFIX + userId + ":" + keyword;
        Boolean keyExists = redisTemplate.hasKey(userSearchKey);
        if (keyExists != null && keyExists) {
            // 해당 키워드를 최근에 이미 검색했으므로 카운트 증가 없이 리턴
            return;
        }

        // 사용자 검색 기록 저장 (쿨다운 시간 동안 유지)
        redisTemplate.opsForValue().set(userSearchKey, "1", java.time.Duration.ofSeconds(SEARCH_COOLDOWN));

        // 현재 시간 기준 키워드 저장소 키 생성
        LocalDateTime now = LocalDateTime.now();
        String hourlyKey = HOURLY_KEYWORDS_PREFIX + now.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        String dailyKey = DAILY_KEYWORDS_PREFIX + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String weeklyKey = WEEKLY_KEYWORDS_PREFIX + now.format(DateTimeFormatter.ofPattern("yyyyww"));

        // 시간별, 일별, 주별 카운트 증가
        redisTemplate.opsForZSet().incrementScore(hourlyKey, keyword, 1);
        redisTemplate.opsForZSet().incrementScore(dailyKey, keyword, 1);
        redisTemplate.opsForZSet().incrementScore(weeklyKey, keyword, 1);

        //  인기 검색어 카운트 증가 -> 채민이가 커스텀하세요 => 네!
        // 통합 인기 검색어 카운트 증가
        redisTemplate.opsForZSet().incrementScore(POPULAR_KEYWORDS_KEY, keyword, 1);

        // 시간별 데이터 만료 사건 설정 (각 키에 대한 TTL 설정)
        redisTemplate.expire(hourlyKey, java.time.Duration.ofHours(2)); // 2시간 유지
        redisTemplate.expire(dailyKey, java.time.Duration.ofDays(2)); // 2일 유지
        redisTemplate.expire(weeklyKey, java.time.Duration.ofDays(14)); // 2주 유지


        // 인기 검색어 업데이트
        updatePopularKeywords();
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

    // 인기 검색어 목록 조회 (상위 10개)
    public List<String> getPopularKeywords() {
        // 순서를 바꾸려면 reverseRangeWithScores 대신 rangeWithScores 사용
        Set<ZSetOperations.TypedTuple<String>> topKeywords =
                redisTemplate.opsForZSet().rangeWithScores(POPULAR_KEYWORDS_KEY, 0, TOP_TEN - 1);

        if (topKeywords == null || topKeywords.isEmpty()) {
            return Collections.emptyList();
        }

        // 점수 기준 내림차순으로 정렬된 결과를 리스트로 변환
        List<String> result = new ArrayList<>();
        topKeywords.forEach(tuple -> {
            if (tuple.getValue() != null) {
                result.add(tuple.getValue());
            }
        });

        return result;
    }

    // 인기 검색어 점수와 함꼐 반환 (디버깅 및 분석용)
    public Map<String, Double> getPopularKeywordsWithScores() {
        Set<ZSetOperations.TypedTuple<String>> topKeywords = redisTemplate.opsForZSet().reverseRangeWithScores(POPULAR_KEYWORDS_KEY, 0, TOP_TEN - 1);

        if (topKeywords == null || topKeywords.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> result = new LinkedHashMap<>();
        topKeywords.forEach(tuple -> {
            if (tuple.getValue() != null) {
                result.put(tuple.getValue(), tuple.getScore());
            }
        });

        return result;
    }

    // 시간별 가중치를 적용하여 인기 검색어 점수 재계산
    @Scheduled(cron = "0 0 * * * * ") // 매 시간마다 정각에 실행함
    public void updatePopularKeywords() {
        try {
            log.info("인기 검색어 업데이트 중...냠냠");

            LocalDateTime now = LocalDateTime.now();

            // 현재 시간대의 키
            String currentHourKey = HOURLY_KEYWORDS_PREFIX + now.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
            String currentDayKey = DAILY_KEYWORDS_PREFIX + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String currentWeekKey = WEEKLY_KEYWORDS_PREFIX + now.format(DateTimeFormatter.ofPattern("yyyyww"));

            // 모든 키워드 집합 (시간/일/주 기준)
            Set<String> allKeywords = new HashSet<>();

            // 시간별 데이터 가져오기
            Set<ZSetOperations.TypedTuple<String>> hourlyData = redisTemplate.opsForZSet().rangeWithScores(currentHourKey, 0, -1);

            if(hourlyData != null) {
                hourlyData.forEach(tuple -> {
                    if(tuple.getValue() != null) {
                        allKeywords.add(tuple.getValue());
                    }
                });
            }

            // 일별 데이터 가져오기
            Set<ZSetOperations.TypedTuple<String>> dailyData = redisTemplate.opsForZSet().rangeWithScores(currentDayKey, 0, -1);

            if(dailyData != null) {
                dailyData.forEach(tuple -> {
                    if(tuple.getValue() != null) {
                        allKeywords.add(tuple.getValue());
                    }
                });
            }

            // 주별 데이터 가져오기
            Set<ZSetOperations.TypedTuple<String>> weeklyData = redisTemplate.opsForZSet().rangeWithScores(currentWeekKey, 0, -1);

            if(weeklyData != null) {
                weeklyData.forEach(tuple -> {
                    if(tuple.getValue() != null) {
                        allKeywords.add(tuple.getValue());
                    }
                });
            }

            // 인기 검색어 전체 데이터 가져오기 (가중치 계산용)
            Map<String, Double> popularTotalScores = new HashMap<>();
            Set<ZSetOperations.TypedTuple<String>> totalData = redisTemplate.opsForZSet().rangeWithScores(POPULAR_KEYWORDS_KEY, 0, -1);

            if(totalData != null) {
                totalData.forEach(tuple -> {
                    if(tuple.getValue()!= null && tuple.getScore() != null) {
                        popularTotalScores.put(tuple.getValue(), tuple.getScore());
                    }
                });
            }

            // 각 키워드에 대해 가중치 적용하여 점수 계산
            for(String keyword : allKeywords) {
                double hourlyScore = getScoreOrDefault(hourlyData, keyword) * HOURLY_WEIGHT;
                double dailyScore = getScoreOrDefault(dailyData, keyword) * DAILY_WEIGHT;
                double weeklyScore = getScoreOrDefault(weeklyData, keyword) * WEEKLY_WEIGHT;

                // 전체 검색 횟수 가중치 추가
                double totalScore = popularTotalScores.getOrDefault(keyword, 0.0);
                double totalHourlyScore = totalScore * TOTAL_HOURLY_WEIGHT;
                double totalDailyScore = totalScore * TOTAL_DAILY_WEIGHT;
                double totalWeeklyScore = totalScore * TOTAL_WEEKLY_WEIGHT;

                // 최종 점수 계산
                double finalScore = (hourlyScore + totalHourlyScore) +
                        (dailyScore + totalDailyScore) +
                        (weeklyScore + totalWeeklyScore);

                // 인기 검색어 목록 업데이트
                if(finalScore > 0) {
                    redisTemplate.opsForZSet().add(POPULAR_KEYWORDS_KEY, keyword, finalScore);
                }
            }

            log.info("인기 검색어를 성공적으로 업데이트 했습니닥!!!!뿡");
        } catch (Exception e) {
            log.error("인거 검색어를 업데이트하는데 오류가 있었습니다: ", e);
        }
    }

    // 일별 가중치 초기화 (자정마다 실행)
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyWeights() {
        try {
            log.info("일별 가중치 초기화 중...쩝쩝");
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            String yesterdayKey = DAILY_KEYWORDS_PREFIX + yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 이전 데이터 백업
            backupSearchData(yesterdayKey, "daily");

            log.info("일별 가중치 초기화 완료!!! 꺗");
        } catch (Exception e) {
            log.error("일별 가중치 초기화 중 오류가 있었습니다: ", e);
        }
    }

    // 주별 가중치 초기화 (매주 월요일 자정마다 실행)
    @Scheduled(cron = "0 0 0 * * 1")
    public void resetWeeklyWeights() {
        try {
            log.info("주별 가중치 초기화 중...쩝쩝");
            LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
            String lastWeekKey = WEEKLY_KEYWORDS_PREFIX + lastWeek.format(DateTimeFormatter.ofPattern("yyyyww"));

            // 이전 데이터 백업
            backupSearchData(lastWeekKey, "weekly");

            log.info("주별 가중치 초기화 완료!!! 꺗");
        } catch (Exception e) {
            log.error("주별 가중치 초기화 중 오류가 있었습니다: ", e);
        }
    }

    // 검색 데이터 백업
    private void backupSearchData(String redisKey, String period) {
        Set<ZSetOperations.TypedTuple<String>> data = redisTemplate.opsForZSet().rangeWithScores(redisKey, 0, -1);

        if (data == null || data.isEmpty()) {
            log.info("해당 키를 위해 백업할 데이터 없습니다: {}", redisKey);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String periodKey = redisKey.substring(redisKey.lastIndexOf(":") + 1);

        data.forEach(tuple -> {
            if (tuple.getValue() != null && tuple.getScore() != null) {
                String keyword = tuple.getValue();
                double score = tuple.getScore();

                // 인기 검색어의 전체 횟수 조회
                Double totalCount = redisTemplate.opsForZSet().score(POPULAR_KEYWORDS_KEY, keyword);
                if(totalCount == null) totalCount = 0.0;

                // 시간별/일별/주별 데이터 조회
                Double hourlyCount = 0.0;
                Double dailyCount = 0.0;
                Double weeklyCount = 0.0;

                // 현재 시간 기준 키 생성
                LocalDateTime currentTime = LocalDateTime.now();
                String currentHourKey = HOURLY_KEYWORDS_PREFIX + currentTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
                String currentDayKey = DAILY_KEYWORDS_PREFIX + currentTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String currentWeekKey = WEEKLY_KEYWORDS_PREFIX + currentTime.format(DateTimeFormatter.ofPattern("yyyyww"));

                // 각 기간별 점수 조회
                hourlyCount = redisTemplate.opsForZSet().score(currentHourKey, keyword);
                dailyCount = redisTemplate.opsForZSet().score(currentDayKey, keyword);
                weeklyCount = redisTemplate.opsForZSet().score(currentWeekKey, keyword);

                // null 처리
                if (hourlyCount == null) hourlyCount = 0.0;
                if (dailyCount == null) dailyCount = 0.0;
                if (weeklyCount == null) weeklyCount = 0.0;

                // 가중치가 적용된 최종 점수 계산
                double finalScore = calculateScore(hourlyCount, dailyCount, weeklyCount, totalCount);

                // DB에 저장
                SearchKeyword searchKeyword = SearchKeyword.builder()
                        .keyword(keyword)
                        .hourlyCount(hourlyCount.intValue())
                        .dailyCount(dailyCount.intValue())
                        .weeklyCount(weeklyCount.intValue())
                        .totalCount(totalCount.intValue())
                        .score(finalScore)
                        .searchDate(now)
                        .period(period)
                        .periodKey(periodKey)
                        .build();
                searchKeywordRepository.save(searchKeyword);
            }
        });

        // 백업 후 redis에서는 삭제하지 않고 TTL에 따라 자동 만료되도록 함
        log.info("period key {}를 위한 서치 데이터 {}를 성공적으로 백업 햇슴다!!", periodKey, data.size());
    }

    private double calculateScore(double hourlyCount, double dailyCount, double weeklyCount, double totalCount) {
        double hourlyScore = hourlyCount * HOURLY_WEIGHT;
        double dailyScore = dailyCount * DAILY_WEIGHT;
        double weeklyScore = weeklyCount * WEEKLY_WEIGHT;

        double totalHourlyScore = totalCount * TOTAL_HOURLY_WEIGHT;
        double totalDailyScore = totalCount * TOTAL_DAILY_WEIGHT;
        double totalWeeklyScore = totalCount * TOTAL_WEEKLY_WEIGHT;

        return (hourlyScore + totalHourlyScore) +
                (dailyScore + totalDailyScore) +
                (weeklyScore + totalWeeklyScore);
    }

    // 한 집합에서 키워드의 점수를 가져오거나 기본값 0.0 반환
    private double getScoreOrDefault(Set<ZSetOperations.TypedTuple<String>> dataSet, String keyword) {
        if(dataSet == null) {
            return 0.0;
        }
        return dataSet.stream()
                .filter(tuple -> keyword.equals(tuple.getValue()))
                .findFirst()
                .map(ZSetOperations.TypedTuple::getScore)
                .orElse(0.0);
    }

    /**
     * 키워드의 검색 트렌드 정보를 일별로 조회
     * @param keyword 조회할 키워드
     * @param days 조회할 날짜 수 (최근 N일)
     * @return 일별 검색 트렌드 데이터
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getKeywordTrendByDays(String keyword, int days) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 필수 입력값입니다.");
        }

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<SearchKeyword> trends = searchKeywordRepository.findKeywordTrendByPeriod(keyword, "daily", startDate, endDate);

        return trends.stream().map(t -> {
            Map<String, Object> data = new HashMap<>();
            data.put("date", t.getPeriodKey());
            data.put("count", t.getDailyCount());
            data.put("score", t.getScore());
            return data;
        }).collect(Collectors.toList());
    }

    /**
     * 특정 기간의 인기 검색어 TOP n 목록 조회
     * @param days 조회할 날짜 수 (최근 n일)
     * @param limit 조회할 검색어 수 (상위 n개)
     * @return 인기 검색어 목록 및 점수
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopKeywordsByPeriod(int days, int limit) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<SearchKeyword> topKeywords = searchKeywordRepository.findTopKeywordsByDateRange(startDate, endDate, limit);

        return topKeywords.stream().map(k -> {
            Map<String, Object> data = new HashMap<>();
            data.put("keyword", k.getKeyword());
            data.put("score", k.getScore());
            data.put("totalCount", k.getTotalCount());
            return data;
        }).collect(Collectors.toList());
    }

}