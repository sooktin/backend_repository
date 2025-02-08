package com.sooktin.backend.dto.careercard;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CreateCareerCardRequest {
    private String major;
    private String studentStatus;
    private Byte grade;
    private String studentNum;

    @Size(max = 30, message = "소속은 최대 30자까지 입력 가능합니다.")
    private String department; // 단일 소속

    @Size(max = 30, message = "직업은 최대 30자까지 입력 가능합니다.") // 직업 필드 추가
    private String job;


    private List<ExperienceRequest> experiences; // 경력 리스트 (+ 기간)

    private List<@Size(max = 100, message = "각 기술은 최대 100자까지 입력 가능합니다.") String> skills; // 기술 리스트
    @Size(max = 3, message = "이미지는 최대 3개까지 등록할 수 있습니다.")
    private List<String> imageUrls; // 이미지 리스트 (최대 3개)

}