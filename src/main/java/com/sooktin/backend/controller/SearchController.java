package com.sooktin.backend.controller;

import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.global.util.ResponseUtil;
import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.CustomUserDetails;
import com.sooktin.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;
    private final AuthenticationService authenticationService;

    // 검색어 기록
    @PatchMapping
    public ResponseEntity<ResponseDto<Object>> trackSearchKeyword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String keyword) {

        if (userDetails == null) {
            return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
        }

        searchService.trackSearchKeyword(keyword, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto<>(200, "검색어가 기록되었습니다.", keyword));
    }

    // 최근 검색어 조회
    @GetMapping("/recent")
    public ResponseEntity<ResponseDto<List<String>>> getRecentKeywords(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
        }

        List<String> recentKeywords = searchService.getRecentKeywords(userDetails.getUserId());
        return ResponseUtil.buildResponse(200, "최근 검색어 목록입니다.", recentKeywords);
    }

    // 인기 검색어 조회 (간단한 목록)
    @GetMapping("/popular")
    public ResponseEntity<ResponseDto<List<String>>> getPopularKeywords() {
        List<String> popularKeywords = searchService.getPopularKeywords();
        return ResponseUtil.buildResponse(200, "인기 검색어 목록입니다.", popularKeywords);
    }

    // 인기 검색어 점수 조회 (디버깅 및 개발용)
    @GetMapping("/popular/scores")
    public ResponseEntity<ResponseDto<Map<String, Double>>> getPopularKeywordsWithScores() {
        Map<String, Double> popularKeywordsWithScores = searchService.getPopularKeywordsWithScores();
        return ResponseUtil.buildResponse(200, "인기 검색어 점수 목록입니다.", popularKeywordsWithScores);
    }

    // 키워드의 검색 트렌드 조회
    @GetMapping("/trends/{keyword}")
    public ResponseEntity<ResponseDto<List<Map<String, Object>>>> getKeywordTrend(
            @PathVariable String keyword,
            @RequestParam(defaultValue = "7") int days) {

        List<Map<String, Object>> trend = searchService.getKeywordTrendByDays(keyword, days);
        return ResponseUtil.buildResponse(200, "키워드 트렌드 데이터입니다.", trend);
    }

    // 특정 기간의 인기 검색어 조회
    @GetMapping("/popular/period")
    public ResponseEntity<ResponseDto<List<Map<String, Object>>>> getTopKeywordsByPeriod(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {

        List<Map<String, Object>> topKeywords = searchService.getTopKeywordsByPeriod(days, limit);
        return ResponseUtil.buildResponse(200, "기간별 인기 검색어 데이터입니다.", topKeywords);
    }
}