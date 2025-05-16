package com.sooktin.backend.controller;

import com.sooktin.backend.dto.PagedResponse;
import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.alarm.AlarmResponse;
import com.sooktin.backend.global.util.ResponseUtil;
import com.sooktin.backend.service.CustomUserDetails;
import com.sooktin.backend.service.alarm.AlarmService;
import com.sooktin.backend.service.alarm.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarms")
public class AlarmController {

    private final AlarmService alarmService;
    private final SseService sseService;

    // sse 구독
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        SseEmitter emitter = sseService.subscribe(userId);

        return ResponseEntity.ok()
                .header("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .header("X-Accel-Buffering", "no") // Nginx에서 중요
                .body(emitter);
    }

    // 알람 목록 조회
    @GetMapping
    public ResponseDto<PagedResponse<AlarmResponse>> getAlarms(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return alarmService.getAlarms(userDetails.getUserId(), pageable);
    }

    // 클릭 시 알림 등록
    @PatchMapping("/{alarmId}/check")
    public ResponseEntity<ResponseDto<Object>> checkAlarm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long alarmId) {

        if (userDetails == null) {
            return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
        }

        alarmService.checkAlarm(alarmId, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto<>(200, "해당 알람이 클릭되었습니다.", alarmId));

    }

    // 특정 알람 삭제
    @DeleteMapping("{alarmId}")
    public ResponseEntity<ResponseDto<Object>> deleteAlarm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long alarmId) {
        if (userDetails == null) {
            return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
        }
        alarmService.deleteAlarm(alarmId, userDetails.getUserId());
        return ResponseUtil.buildResponse(204, "해당 알람을 성공적으로 삭제했습니다.", null);
    }

}
