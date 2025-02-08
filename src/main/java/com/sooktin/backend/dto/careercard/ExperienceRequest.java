package com.sooktin.backend.dto.careercard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExperienceRequest {
    @NotBlank(message = "경력 내용을 입력해야 합니다.")
    @Size(max = 50, message = "각 경력은 최대 50자까지 입력 가능합니다.")
    private String company;  // 회사 또는 경력 설명

    @NotBlank(message = "기간을 입력해야 합니다.")
    @Size(max = 20, message = "기간은 최대 20자까지 입력 가능합니다.")
    private String period;  // 근무 기간 (예: "2024년", "2020-2022년")
}