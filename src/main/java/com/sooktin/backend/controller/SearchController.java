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
        return ResponseEntity.ok(new ResponseDto<>(200, "검색어가 기록되었습니다.", null));
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

}
