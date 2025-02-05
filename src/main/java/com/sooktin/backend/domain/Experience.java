package com.sooktin.backend.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class Experience {
    private String company;  // 회사 또는 경력 설명
    private String period;   // 기간 (예: "2024년", "2020-2022년")

    public Experience(String company, String period) {
        this.company = company;
        this.period = period;
    }
}