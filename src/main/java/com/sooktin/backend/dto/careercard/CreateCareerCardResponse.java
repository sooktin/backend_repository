package com.sooktin.backend.dto.careercard;

import com.sooktin.backend.domain.CareerCard;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateCareerCardResponse {
    private Long cardId;
    private String nickname;       // 닉네임
    private String major;          // 전공
    private String studentStatus;  // 재학 상태
    private byte grade;            // 학년
    private String studentNum;     // 학번

    private List<String> departments; // 소속 리스트
    private List<String> experiences; // 경력 리스트
    private List<String> skills;      // 기술 리스트
    private List<String> imageUrls;   // 이미지 리스트

    private String createdAt;    // 생성일시
    private String modifiedAt;   // 수정일시
    private Long userId;         // 사용자 ID

    public CreateCareerCardResponse(CareerCard careerCard) {
        this.cardId = careerCard.getId();
        this.nickname = careerCard.getUser().getNickname();
        this.major = careerCard.getMajor();
        this.studentStatus = careerCard.getStudent_status();
        this.grade = careerCard.getGrade();
        this.studentNum = careerCard.getStudent_num();

        this.departments = careerCard.getDepartments();
        this.experiences = careerCard.getExperiences();
        this.skills = careerCard.getSkills();
        this.imageUrls = careerCard.getImageUrls();

        this.createdAt = careerCard.getCreated_at().toString();
        this.modifiedAt = careerCard.getModified_at().toString();
        this.userId = careerCard.getUser().getId();
    }
}