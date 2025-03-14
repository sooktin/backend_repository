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
    private Long id;
    private String major;
    private String job;
    private Long userId;
    private String department;
    private String student_num;
    @Builder.Default
    private List<String> skills = new ArrayList<>();
    @Builder.Default
    private List<String> companiesInExperience = new ArrayList<>();
    @Builder.Default
    private List<String> imageUrls =new ArrayList<>(); //imageurls 중 첫번째 주소 반환!


}
