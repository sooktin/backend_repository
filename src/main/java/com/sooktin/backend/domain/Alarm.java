package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "alarms")
public class Alarm extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mergedMessage;

    private Boolean isChecked = false;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    private Long targetId; // usernoteId or 채팅id(??) 값

    @ManyToOne(fetch = FetchType.LAZY)
    private User receiver;
}