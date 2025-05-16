package com.sooktin.backend.dto.alarm;

import com.sooktin.backend.domain.Alarm;
import com.sooktin.backend.domain.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class AlarmSendDto {
    private String mergedMessage;
    private Long alarmId;
    private Long targetId;
    private AlarmType alarmType;

    public AlarmSendDto(Alarm alarm) {
        this.mergedMessage = alarm.getMergedMessage();
        this.targetId = alarm.getTargetId();
        this.alarmType = alarm.getAlarmType();
        this.alarmId = alarm.getId();
    }
}