/*
package com.sooktin.backend.dto.careercard;

import com.sooktin.backend.domain.Experience;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateCareerCardResponse {
    private Long cardId; // 카드 ID
    //private Long userId;         // 사용자 ID
    private String nickname;       // 닉네임

    private String major;          // 전공
    private String studentNum;     // 학번
    private String studentStatus;  // 재학 상태
    private Integer grade;            // 학년

    private String job; // 직업
    private String department; // 소속

    private List<Experience> experiences; // 경력 리스트
    private List<String> skills;      // 기술 리스트
    private List<String> imageUrls;   // 이미지 리스트

    private String createdAt;    // 생성일시
    private String modifiedAt;   // 수정일시


    public CreateCareerCardResponse(CareerCardDTO careerCard) {
        this.cardId = careerCard.getId();
        this.nickname = careerCard.getUser().getNickname(); // User에서 가져옴
        this.major = careerCard.getMajor();
        this.studentStatus = careerCard.getStudent_status();
        this.grade = careerCard.getGrade();
        this.studentNum = careerCard.getStudent_num();

        this.job = careerCard.getJob();
        this.department = careerCard.getDepartment(); // 단일 소속
        this.experiences = careerCard.getExperiences();
        this.skills = careerCard.getSkills();
        this.imageUrls = careerCard.getImageUrls();

        this.createdAt = careerCard.getCreated_at().toString();
        this.modifiedAt = careerCard.getModified_at().toString();
        //this.userId = careerCard.getUser().getId();
    }
}*/
