package com.sooktin.backend.dto.careercard;

import com.sooktin.backend.domain.Experience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerCardDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long cardId;
    private String nickname;
    private String major;
    private String student_num;
    private String student_status;
    private Integer grade;
    private String job;
    private Long userId;
    private String department;
    @Builder.Default
    private List<String> skills = new ArrayList<>();
    @Builder.Default
    private List<ExperienceDTO> experiences = new ArrayList<>();
    @Builder.Default
    private List<String> imageUrls =new ArrayList<>(); //imageurls 중 첫번째 주소 반환!

    @Data
    @Builder
    @NoArgsConstructor // 기본 생성자 추가 (필수)
    @AllArgsConstructor
    static class ExperienceDTO {
        private String company;
        private String period;


    }

}
