package com.sooktin.backend.dto.alarm;

import com.sooktin.backend.domain.Alarm;
import com.sooktin.backend.domain.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class AlarmResponse {
    private Long id;
    private String mergedMessage;
    private boolean isChecked;
    private Long targetId;
    private AlarmType alarmType;
    private LocalDateTime createdAt;

    public AlarmResponse(Alarm alarm) {
        this.id = alarm.getId();
        this.mergedMessage = alarm.getMergedMessage();
        this.isChecked = alarm.getIsChecked();
        this.targetId = alarm.getTargetId();
        this.alarmType = alarm.getAlarmType();
        this.createdAt = alarm.getCreatedAt();
    }
}