package com.sooktin.backend.dto.careercard;

import com.sooktin.backend.domain.Experience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerCardDTO {
    private Long id;
    private String major;
    private String job;
    private Long userId;
    private String department;
    private String student_num;
    private List<String> skills;
    private List<String> companiesInExperience;
    private String imageUrl; //imageurls 중 첫번째 주소 반환!


}
