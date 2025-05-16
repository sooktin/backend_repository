package com.sooktin.backend.service.alarm;

import com.sooktin.backend.domain.Alarm;
import com.sooktin.backend.domain.AlarmType;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.alarm.AlarmResponse;
import com.sooktin.backend.dto.alarm.AlarmSendDto;
import com.sooktin.backend.dto.PagedResponse;
import com.sooktin.backend.repository.AlarmRepository;
import com.sooktin.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private static final String MATCH_SUFFIX_MESSAGE = " 님과 매치되었습니다\n지금 당장 확인해보세요!";

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

    // 좋아요 알람
    @Transactional
    public void sendLikeAlarm(User sender, User receiver, Long noteId) {
        String message = sender.getNickname() + " 님이 내 노트에 좋아요를 눌렀습니다\n"
                + sender.getNickname() + " 님의 프로필을 확인해보세요!";

        alarmRepository.save(Alarm.builder()
                .mergedMessage(message)
                .alarmType(AlarmType.LIKE_NOTE)
                .targetId(noteId)
                .receiver(receiver)
                .isChecked(false)
                .build());

        sseService.send(receiver.getId(), "alarm", message);
    }

    // 매칭 알람
    @Transactional
    public void sendMatchAlarm(User userA, User userB) {
        String messageToA = userB.getNickname() + MATCH_SUFFIX_MESSAGE;
        String messageToB = userA.getNickname() + MATCH_SUFFIX_MESSAGE;

        alarmRepository.save(Alarm.builder()
                .mergedMessage(messageToA)
                .alarmType(AlarmType.MATCH)
                .targetId(999L) // TODO: 매칭 대상 ID 설정 필요
                .receiver(userA)
                .isChecked(false)
                .build());

        alarmRepository.save(Alarm.builder()
                .mergedMessage(messageToB)
                .alarmType(AlarmType.MATCH)
                .targetId(999L)
                .receiver(userB)
                .isChecked(false)
                .build());

        sseService.send(userA.getId(), "alarm", messageToA);
        sseService.send(userB.getId(), "alarm", messageToB);
    }

    // 알람 목록 조회
    public ResponseDto<PagedResponse<AlarmResponse>> getAlarms(Long userId, Pageable pageable) {
        Page<Alarm> alarms = alarmRepository.findByReceiverIdOrderByCreatedAtDesc(userId, pageable);
        List<AlarmResponse> content = alarms.stream()
                .map(AlarmResponse::new)
                .toList();

        PagedResponse<AlarmResponse> pagedResponse = new PagedResponse<>(
                content,
                (int) alarms.getTotalElements(),
                alarms.getNumber(),
                alarms.getSize()
        );

        return new ResponseDto<>(200, "알람 목록 조회 성공", pagedResponse);
    }

    // 알람 클릭 시 등록
    @Transactional
    public ResponseDto<Object> checkAlarm(Long alarmId, Long userId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElse(null);

        if (alarm == null) {
            return new ResponseDto<>(404, "알림을 찾을 수 없습니다.", null);
        }

        if (!alarm.getReceiver().getId().equals(userId)) {
            return new ResponseDto<>(403, "본인의 알림만 확인할 수 있습니다.", null);
        }

        alarm.setIsChecked(true);
        return new ResponseDto<>(200, "알림을 확인했습니다.", null);
    }

    // 특정 알람 삭제
    @Transactional
    public ResponseDto<Object> deleteAlarm(Long alarmId, Long userId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElse(null);

        if (alarm == null) {
            return new ResponseDto<>(404, "알림을 찾을 수 없습니다.", null);
        }

        if (!alarm.getReceiver().getId().equals(userId)) {
            return new ResponseDto<>(403, "본인의 알림만 삭제할 수 있습니다.", null);
        }

        alarmRepository.delete(alarm);
        return new ResponseDto<>(200, "알림이 성공적으로 삭제되었습니다.", null);
    }
}